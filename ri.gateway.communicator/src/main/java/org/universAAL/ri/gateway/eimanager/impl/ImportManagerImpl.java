/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

See the NOTICE file distributed with this work for additional
information regarding copyright ownership

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.universAAL.ri.gateway.eimanager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.RemoteSpacesManager;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.ImportExecutor;
import org.universAAL.ri.gateway.eimanager.ImportManager;
import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportProcessExecutor;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportedProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.importing.InternalImportOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;
import org.universAAL.ri.gateway.eimanager.impl.registry.IRegistryListener;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public class ImportManagerImpl implements ImportManager, ImportExecutor,
	IRegistryListener, RemoteSpacesManager {

    private ImportProcessExecutor importExecutor;
    private Thread importThread;

    private ImportedProxyManager manager;

    private Map<String, BlockingQueue<ImportEntry>> importingMock;

    private BlockingQueue<InternalImportOperation> busMembersToImport;
    private GatewayCommunicator communicator;

    public ImportManagerImpl(final GatewayCommunicator communicator) {
	this.communicator = communicator;
	busMembersToImport = new ArrayBlockingQueue<InternalImportOperation>(5);

	importingMock = new HashMap<String, BlockingQueue<ImportEntry>>();

	importExecutor = new ImportProcessExecutor(busMembersToImport);
	importThread = new Thread(importExecutor);
	importThread.start();

	manager = new ImportedProxyManager(communicator);

	EIRepoAccessManager.Instance.addListener(this);

	Activator.mc.getContainer().shareObject(Activator.mc, this,
		new Object[] { RemoteSpacesManager.class.getName() });
    }

    public void shutdown() {
	importThread.interrupt();
    }

    /*
     * public void addExportPremise(ImportPremise premise) {
     * importPremises.add(premise); } public void
     * removeExportPremise(ImportPremise premise) {
     * importPremises.remove(premise); }
     */

    /*
     * delegate methods for manager of lower size
     */
    public void sendContextEvent(final String sourceId, final ContextEvent event) {
	manager.realizeLocalContextEventPublishment(sourceId, event);
    }

    public void sendUIResponse(final String sourceId, final UIResponse response) {
	manager.realizeLocalUIResponsePublishment(sourceId, response);
    }

    public void removeRemoteBusMember(final BusMember sourceMember,
	    final String targetMemberIdRegex) {
	throw new RuntimeException("Not yet implemented");

	/*
	 * busMembersToImport.add(new InternalImportOperation(sourceMember,
	 * RepoOperation.Purge, targetMemberIdRegex));
	 * 
	 * communicator.sendImportRemoval(null, new URL[]{});
	 */
    }

    /*
     * Methods for integrating with bus tracking capabilities
     */
    public void memberAdded(final BusMember member) {
	// TODO implement in next release
    }

    public void memberRemoved(final BusMember member) {
	// TODO implement in next release
    }

    public void registryEntryAdded(final RegistryEntry entry) {
	if (entry instanceof ImportEntry) {
	    InternalImportOperation op = (InternalImportOperation) ((ImportEntry) entry)
		    .getOperation();
	    System.out.println("Registering proxy for : "
		    + op.getType().toString());
	    manager.registerProxies(op);
	    importingMock.get(op.getUuid()).add((ImportEntry) entry);
	    System.out.println("Proxy registered");
	}
    }

    public void registryEntryRemoved(final RegistryEntry entry) {
	if (entry instanceof ImportEntry) {
	    ImportEntry importEntry = (ImportEntry) entry;
	    InternalImportOperation op = (InternalImportOperation) importEntry
		    .getOperation();
	    manager.unregisterProxies(op);
	    importingMock.get(op.getUuid()).add(importEntry);
	}
    }

    public ImportEntry importRemoteService(final BusMember sourceMember,
	    final String serviceType, final String serverNamespace)
	    throws IOException, ClassNotFoundException {
	String uuid = UUID.randomUUID().toString();
	importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
	System.out.println("Importing RemoteService");
	internalImportRemoteService(uuid, sourceMember, serviceType,
		serverNamespace);
	try {
	    System.out.println("Waiting for proxy registration");
	    ImportEntry entry = importingMock.get(uuid).take();
	    System.out.println("Continuing");
	    return entry;
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private boolean performInterceptorChainExecution(String uuid, ImportRequest request, EIOperationManager.Type type){
    		try {
				EIOperationManager.Instance.executeImportOperationChain(request, type);
			} catch (InterruptExecutionException e) {
				importingMock.get(uuid).add(new ImportEntry("", null, "", null, false, e.getMessage()));
				return false;
			}
    		return true;
    }
    
    public void internalImportRemoteService(final String uuid,
	    final BusMember sourceMember, final String targetServiceType,
	    final String serverNamespace) throws IOException,
	    ClassNotFoundException {
	InternalImportOperation internal = new InternalImportOperation(
		sourceMember, RepoOperation.Publish, uuid);

	ImportRequest importRequest = new ImportRequest(internal.getType(),
		uuid);
	importRequest.setServiceType(targetServiceType);
	importRequest.setServerNamespace(serverNamespace);

	if (!performInterceptorChainExecution(uuid,importRequest, EIOperationManager.Type.Service)){
		return;
	}
	
	System.out.println("Sending ImportRequest internalImportRemoteService");
	Message m = communicator.sendImportRequest(Serializer.Instance
		.marshall(importRequest));
	String registeredRemoteProxyId = ((ProxyRegistration) m.getContent())
		.getId();
	Map<String, List<String>> serializedProfilesMap = (Map<String, List<String>> ) ((ProxyRegistration) m
			.getContent()).getReturnedValues();
	//String[] serializedProfiles = (String[]) ((ProxyRegistration) m
	//	.getContent()).getReturnedValues();

	System.out.println("Got ImportResponse. ServiceProfiles count: "
		+ serializedProfilesMap.values().size());
	
	Map<String, List<ServiceProfile>> profilesMap = new HashMap<String, List<ServiceProfile>>();
	
	for(String key : serializedProfilesMap.keySet()){
		if (profilesMap.get(key) == null){
			profilesMap.put(key, new ArrayList<ServiceProfile>());
		}
		for(String serializedP : serializedProfilesMap.get(key)){
			profilesMap.get(key).add( Serializer.Instance.unmarshallObject(
		    ServiceProfile.class, serializedP,
		    Activator.class.getClassLoader()));
		}
	}
	/*
	for (int i = 0; i < serializedProfiles.length; i++) {
	    profiles[i] = Serializer.Instance.unmarshallObject(
		    ServiceProfile.class, serializedProfiles[i],
		    Activator.class.getClassLoader());
	}
	*/
	internal.setRealizedServices(profilesMap);
	internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
	busMembersToImport.add(internal);
    }

    public ImportEntry importRemoteContextEvents(final BusMember sourceMember,
	    final ContextEventPattern[] cpe) throws IOException,
	    ClassNotFoundException {
	String uuid = UUID.randomUUID().toString();
	importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
	System.out.println("Importing RemoteContextEvents");
	internalImportRemoteContextEvents(uuid, sourceMember, cpe);
	try {
	    System.out.println("Waiting for proxy registration");
	    importingMock.get(uuid).take();
	    ImportEntry entry = importingMock.get(uuid).take();
	    System.out.println("Continuing");
	    return entry;
	} catch (InterruptedException e) {
		e.printStackTrace();
	    return null;
	}
    }

    private void internalImportRemoteContextEvents(final String uuid,
	    final BusMember sourceMember, final ContextEventPattern[] cpe)
	    throws IOException, ClassNotFoundException {
	InternalImportOperation internal = new InternalImportOperation(
		sourceMember, RepoOperation.Publish, uuid);
	
	ImportRequest importRequest = new ImportRequest(internal.getType(),
		uuid);
	String[] serializedCpe = new String[cpe.length];
	String[] subjectURIS = new String[cpe.length];
	System.out.println("Import sent:");
	for (int i = 0; i < cpe.length; i++) {
	    serializedCpe[i] = (String) Serializer.Instance.marshallObject(
		    cpe[i]).getContent();
	    subjectURIS[i] = cpe[i].getIndices().getSubjectTypes()[i];
	    System.out.println(serializedCpe[i]);
	}
	importRequest.setCpe(serializedCpe);
	importRequest.setSubjectURIs(subjectURIS);
	if (!performInterceptorChainExecution(uuid,importRequest, EIOperationManager.Type.Context)){
		return;
	}
	
	System.out
		.println("Sending ImportRequest internalImportRemoteContextEvents");
	Message m = communicator.sendImportRequest(Serializer.Instance
		.marshall(importRequest));
	String registeredRemoteProxyId = ((ProxyRegistration) m.getContent())
		.getId();
	String[] serializedCEP = (String[]) ((ProxyRegistration) m.getContent())
		.getReturnedValues();
	ContextEventPattern[] patterns = new ContextEventPattern[serializedCEP.length];
	System.out.println("Import received:");
	for (int i = 0; i < serializedCEP.length; i++) {
	    System.out.println(serializedCEP[i]);
	    patterns[i] = Serializer.Instance.unmarshallObject(
		    ContextEventPattern.class, serializedCEP[i],
		    Activator.class.getClassLoader());
	}

	ContextProvider info = new ContextProvider(ContextProvider.MY_URI);
	info.setType(ContextProviderType.controller);
	info.setProvidedEvents(patterns);

	System.out.println("Got ImportResponse. ContextProvider events count: "
		+ ((info.getProvidedEvents() == null) ? " is null " : info
			.getProvidedEvents().length));

	internal.setContextProvider(info);
	internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
	busMembersToImport.add(internal);
    }

    public void refreshProxy(final ProxyRegistration proxyRegistration) throws IOException, ClassNotFoundException {
    	manager.refreshProxy(proxyRegistration);
    }

    public boolean unimportRemoteService(final ImportEntry importEntry)
	    throws IOException, ClassNotFoundException {
	InternalImportOperation operation = (InternalImportOperation) importEntry
		.getOperation();
	String uuid = operation.getUuid();
	importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
	System.out.println("Unimporting RemoteService");

	operation.setOp(RepoOperation.Purge);

	ImportRequest importRequest = new ImportRequest(operation.getType(),
		uuid);
	importRequest.setId(operation.getRemoteRegisteredProxyId());

	System.out.println("Sending ImportRequest internalImportRemoteService");
	communicator.sendImportRemoval(Serializer.Instance
		.marshall(importRequest));
	busMembersToImport.add(operation);
	try {
	    System.out.println("Waiting for proxy unregistration");
	    importingMock.get(uuid).take();
	    System.out.println("Continuing");
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    public ImportEntry importRemoteUI(final BusMember sourceMember,
    	    final String uiType)
    	    throws IOException, ClassNotFoundException {
    	String uuid = UUID.randomUUID().toString();
    	importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
    	System.out.println("Importing RemoteUI");
    	internalImportRemoteUI(uuid, sourceMember, uiType);
    	try {
    	    System.out.println("Waiting for proxy registration");
    	    ImportEntry entry = importingMock.get(uuid).take();
    	    System.out.println("Continuing");
    	    return entry;
    	} catch (InterruptedException e) {
    	    e.printStackTrace();
    	    return null;
    	}
        }
    
    public void internalImportRemoteUI(final String uuid,
    	    final BusMember sourceMember, final String targetUIType) throws IOException,
    	    ClassNotFoundException {
    	InternalImportOperation internal = new InternalImportOperation(
    		sourceMember, RepoOperation.Publish, uuid);

    	ImportRequest importRequest = new ImportRequest(internal.getType(),
    		uuid);
    	importRequest.setModalityRegex(targetUIType);

    	if (!performInterceptorChainExecution(uuid,importRequest, EIOperationManager.Type.Service)){
    		return;
    	}
    	//TODO
    	/*
    	System.out.println("Sending ImportRequest internalImportRemoteService");
    	Message m = communicator.sendImportRequest(Serializer.Instance
    		.marshall(importRequest));
    	String registeredRemoteProxyId = ((ProxyRegistration) m.getContent())
    		.getId();
    	String[] serializedProfiles = (String[]) ((ProxyRegistration) m
    		.getContent()).getReturnedValues();

    	System.out.println("Got ImportResponse. ServiceProfiles count: "
    		+ serializedProfiles.length);
    	ServiceProfile[] profiles = new ServiceProfile[serializedProfiles.length];
    	for (int i = 0; i < serializedProfiles.length; i++) {
    	    profiles[i] = Serializer.Instance.unmarshallObject(
    		    ServiceProfile.class, serializedProfiles[i],
    		    Activator.class.getClassLoader());
    	}

    	internal.setRealizedServices(profiles);
    	internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
    	busMembersToImport.add(internal);
    */
        }
    
}
