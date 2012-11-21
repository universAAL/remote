package org.universAAL.ri.gateway.eimanager.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.sodapop.BusMember;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.RemoteSpacesManager;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.ImportExecutor;
import org.universAAL.ri.gateway.eimanager.ImportManager;
import org.universAAL.ri.gateway.eimanager.ImportPremise;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportProcessExecutor;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportedProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.importing.InternalImportOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.EIRepoAccessManager;
import org.universAAL.ri.gateway.eimanager.impl.registry.IRegistryListener;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public class ImportManagerImpl implements ImportManager, ImportExecutor, IRegistryListener, RemoteSpacesManager {

    private ImportProcessExecutor importExecutor;
    private Thread importThread;

    private ImportedProxyManager manager;
    
    private Map<String, ServiceProfile[]> importedServiceProfiles;
    
    private Set<ImportPremise> importPremises;

    private Map<String,BlockingQueue<Object>> importingMock;
    
    private BlockingQueue<InternalImportOperation> busMembersToImport;
    private GatewayCommunicator communicator;

    public ImportManagerImpl(final GatewayCommunicator communicator) {
	this.communicator = communicator;
	busMembersToImport = new ArrayBlockingQueue<InternalImportOperation>(5);
	
	importedServiceProfiles = new HashMap<String, ServiceProfile[]>();
	importingMock = new HashMap<String, BlockingQueue<Object>>();
	
	importExecutor = new ImportProcessExecutor(busMembersToImport);
	importThread = new Thread(importExecutor);
	importThread.start();

	manager = new ImportedProxyManager(communicator);
	
	EIRepoAccessManager.Instance.addListener(this);
	
	Activator.mc.getContainer().shareObject(Activator.mc, this, new Object[]{RemoteSpacesManager.class.getName()});
    }

    public void shutdown() {
	importThread.interrupt();
    }
    
    /*
    public void addExportPremise(ImportPremise premise) {
	importPremises.add(premise);
    }
    public void removeExportPremise(ImportPremise premise) {
	importPremises.remove(premise);
    }*/

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
	
	/*busMembersToImport.add(new InternalImportOperation(sourceMember,
		RepoOperation.Purge, targetMemberIdRegex));
	
	communicator.sendImportRemoval(null, new URL[]{});*/
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
	if (entry instanceof ImportEntry){
	    InternalImportOperation op = (InternalImportOperation)((ImportEntry)entry).getOperation();
	    System.out.println("Registering proxy for : " + op.getType().toString());
	    manager.registerProxies(op);
	    importingMock.get(op.getUuid()).add(new Object());
	    System.out.println("Proxy registered");
	}
    }
    public void registryEntryRemoved(final RegistryEntry entry) {
	if (entry instanceof ImportEntry){
	    manager.unregisterProxies((InternalImportOperation)((ImportEntry)entry).getOperation());
	}
    }

    public boolean importRemoteService(final BusMember sourceMember, final String serviceType, final String serverNamespace) throws IOException, ClassNotFoundException {
	String uuid = UUID.randomUUID().toString();
	importingMock.put(uuid, new ArrayBlockingQueue<Object>(1));
	System.out.println("Importing RemoteService");
	internalImportRemoteService(uuid, sourceMember, serviceType, serverNamespace);
	try {
	    System.out.println("Waiting for proxy registration");
	    importingMock.get(uuid).take();
	    System.out.println("Continuing");
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }
    
    public void internalImportRemoteService(final String uuid, final BusMember sourceMember,
	    final String targetServiceType, String serverNamespace) throws IOException, ClassNotFoundException {
	InternalImportOperation internal = new InternalImportOperation(sourceMember,
		RepoOperation.Publish, uuid);
	
	ImportRequest importRequest = new ImportRequest(internal.getType(), uuid);
	importRequest.setServiceType(targetServiceType);
	importRequest.setServerNamespace(serverNamespace);
	
	System.out.println("Sending ImportRequest internalImportRemoteService");
	Message m = communicator.sendImportRequest(Serializer.Instance.marshall(importRequest));
	String registeredRemoteProxyId = ((ProxyRegistration) m.getContent()).getId();
	String[] serializedProfiles = (String[])((ProxyRegistration) m.getContent()).getReturnedValues();
	
	System.out.println("Got ImportResponse. ServiceProfiles count: " + serializedProfiles.length);
	ServiceProfile[] profiles = new ServiceProfile[serializedProfiles.length];
	for(int i = 0 ; i < serializedProfiles.length ; i++){
	    profiles[i] = Serializer.Instance.unmarshallObject(ServiceProfile.class, serializedProfiles[i], Activator.class.getClassLoader());
	}
	
	internal.setRealizedServices(profiles);
	internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
	busMembersToImport.add(internal);
    }
    
    public boolean importRemoteContextEvents(BusMember sourceMember, ContextEventPattern[] cpe) throws IOException, ClassNotFoundException{
	String uuid = UUID.randomUUID().toString();
	importingMock.put(uuid, new ArrayBlockingQueue<Object>(1));
	System.out.println("Importing RemoteContextEvents");
	internalImportRemoteContextEvents(uuid, sourceMember, cpe);
	try {
	    System.out.println("Waiting for proxy registration");
	    importingMock.get(uuid).take();
	    System.out.println("Continuing");
	} catch (InterruptedException e) {
	    return false;
	}
	return true;
    }
    
    private void internalImportRemoteContextEvents(String uuid,
	    BusMember sourceMember, ContextEventPattern[] cpe) throws IOException, ClassNotFoundException {
	InternalImportOperation internal = new InternalImportOperation(sourceMember,
		RepoOperation.Publish, uuid);
	
	ImportRequest importRequest = new ImportRequest(internal.getType(), uuid);
	String[] serializedCpe = new String[cpe.length];
	System.out.println("Import sent:");
	for(int i = 0 ; i < cpe.length ; i++){
	    serializedCpe[i] = (String)Serializer.Instance.marshallObject(cpe[i]).getContent();
	    System.out.println(serializedCpe[i]);
	}
	importRequest.setCpe(serializedCpe);
	
	System.out.println("Sending ImportRequest internalImportRemoteContextEvents");
	Message m = communicator.sendImportRequest(Serializer.Instance.marshall(importRequest));
	String registeredRemoteProxyId = ((ProxyRegistration) m.getContent()).getId();
	String[] serializedCEP = (String[])((ProxyRegistration) m.getContent()).getReturnedValues();
	ContextEventPattern[] patterns = new ContextEventPattern[serializedCEP.length];
	System.out.println("Import received:");
	for(int i = 0 ; i < serializedCEP.length ; i++){
	    System.out.println(serializedCEP[i]);
	    patterns[i] = Serializer.Instance.unmarshallObject(ContextEventPattern.class, serializedCEP[i], Activator.class.getClassLoader());
	}
	
	ContextProvider info = new ContextProvider(ContextProvider.MY_URI);
	info.setType(ContextProviderType.controller);
	info.setProvidedEvents(patterns);
	
	System.out.println("Got ImportResponse. ContextProvider events count: " + ((info.getProvidedEvents() == null)? " is null " : info.getProvidedEvents().length));
	
	internal.setContextProvider(info);
	internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
	busMembersToImport.add(internal);
    }

}
