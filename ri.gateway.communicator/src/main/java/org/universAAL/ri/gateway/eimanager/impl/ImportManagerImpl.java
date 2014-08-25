/*

Copyright 2014 Universidad Politécnica de Madrid, http://www.upm.es/
Life Supporting Technologies

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

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
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

	private final ImportProcessExecutor importExecutor;
	private final Thread importThread;

	private final ImportedProxyManager manager;

	private final Map<String, BlockingQueue<ImportEntry>> importingMock;

	private final BlockingQueue<InternalImportOperation> busMembersToImport;
	private final GatewayCommunicator communicator;

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

	public void registryEntryAdded(final RegistryEntry entry) {
		if (entry instanceof ImportEntry) {
			final InternalImportOperation op = (InternalImportOperation) ((ImportEntry) entry)
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
			final ImportEntry importEntry = (ImportEntry) entry;
			final InternalImportOperation op = (InternalImportOperation) importEntry
					.getOperation();
			manager.unregisterProxies(op);
			importingMock.get(op.getUuid()).add(importEntry);
		}
	}

	public ImportEntry importRemoteService(final BusMember sourceMember,
			final String serviceType, final String serverNamespace)
			throws IOException, ClassNotFoundException {
		final String uuid = UUID.randomUUID().toString();
		importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
		System.out.println("Importing RemoteService");
		internalImportRemoteService(uuid, sourceMember, serviceType,
				serverNamespace);
		try {
			System.out.println("Waiting for proxy registration");
			final ImportEntry entry = importingMock.get(uuid).take();
			System.out.println("Continuing");
			return entry;
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean performInterceptorChainExecution(final String uuid,
			final ImportRequest request, final EIOperationManager.Type type) {
		try {
			EIOperationManager.Instance.executeImportOperationChain(request,
					type);
		} catch (final InterruptExecutionException e) {
			importingMock.get(uuid).add(
					new ImportEntry("", null, "", null, false, e.getMessage()));
			return false;
		}
		return true;
	}

	public void internalImportRemoteService(final String uuid,
			final BusMember sourceMember, final String targetServiceType,
			final String serverNamespace) throws IOException,
			ClassNotFoundException {
		final InternalImportOperation internal = new InternalImportOperation(
				sourceMember, RepoOperation.Publish, uuid);

		final ImportRequest importRequest = new ImportRequest(
				internal.getType(), uuid);
		importRequest.setServiceType(targetServiceType);
		importRequest.setServerNamespace(serverNamespace);

		if (!performInterceptorChainExecution(uuid, importRequest,
				EIOperationManager.Type.Service)) {
			return;
		}

		System.out.println("Sending ImportRequest internalImportRemoteService");
		final Message m = communicator.sendImportRequest(Serializer.Instance
				.marshall(importRequest));
		final String registeredRemoteProxyId = ((ProxyRegistration) m
				.getContent()).getId();
		final Map<String, List<String>> serializedProfilesMap = (Map<String, List<String>>) ((ProxyRegistration) m
				.getContent()).getReturnedValues();
		// String[] serializedProfiles = (String[]) ((ProxyRegistration) m
		// .getContent()).getReturnedValues();

		System.out.println("Got ImportResponse. ServiceProfiles count: "
				+ serializedProfilesMap.values().size());

		final Map<String, List<ServiceProfile>> profilesMap = new HashMap<String, List<ServiceProfile>>();

		for (final String key : serializedProfilesMap.keySet()) {
			if (profilesMap.get(key) == null) {
				profilesMap.put(key, new ArrayList<ServiceProfile>());
			}
			for (final String serializedP : serializedProfilesMap.get(key)) {
				profilesMap.get(key).add(
						Serializer.Instance.unmarshallObject(
								ServiceProfile.class, serializedP,
								Activator.class.getClassLoader()));
			}
		}
		/*
		 * for (int i = 0; i < serializedProfiles.length; i++) { profiles[i] =
		 * Serializer.Instance.unmarshallObject( ServiceProfile.class,
		 * serializedProfiles[i], Activator.class.getClassLoader()); }
		 */
		internal.setRealizedServices(profilesMap);
		internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
		busMembersToImport.add(internal);
	}

	public ImportEntry importRemoteContextEvents(final BusMember sourceMember,
			final ContextEventPattern[] cpe) throws IOException,
			ClassNotFoundException {
		final String uuid = UUID.randomUUID().toString();
		importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
		System.out.println("Importing RemoteContextEvents");
		internalImportRemoteContextEvents(uuid, sourceMember, cpe);
		try {
			System.out.println("Waiting for proxy registration");
			importingMock.get(uuid).take();
			final ImportEntry entry = importingMock.get(uuid).take();
			System.out.println("Continuing");
			return entry;
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void internalImportRemoteContextEvents(final String uuid,
			final BusMember sourceMember, final ContextEventPattern[] cpe)
			throws IOException, ClassNotFoundException {
		final InternalImportOperation internal = new InternalImportOperation(
				sourceMember, RepoOperation.Publish, uuid);

		final ImportRequest importRequest = new ImportRequest(
				internal.getType(), uuid);
		final String[] serializedCpe = new String[cpe.length];
		final String[] subjectURIS = new String[cpe.length];
		System.out.println("Import sent:");
		for (int i = 0; i < cpe.length; i++) {
			serializedCpe[i] = (String) Serializer.Instance.marshallObject(
					cpe[i]).getContent();
			subjectURIS[i] = cpe[i].getIndices().getSubjectTypes()[i];
			System.out.println(serializedCpe[i]);
		}
		importRequest.setCpe(serializedCpe);
		importRequest.setSubjectURIs(subjectURIS);
		if (!performInterceptorChainExecution(uuid, importRequest,
				EIOperationManager.Type.Context)) {
			return;
		}

		System.out
				.println("Sending ImportRequest internalImportRemoteContextEvents");
		final Message m = communicator.sendImportRequest(Serializer.Instance
				.marshall(importRequest));
		final String registeredRemoteProxyId = ((ProxyRegistration) m
				.getContent()).getId();
		final String[] serializedCEP = (String[]) ((ProxyRegistration) m
				.getContent()).getReturnedValues();
		final ContextEventPattern[] patterns = new ContextEventPattern[serializedCEP.length];
		System.out.println("Import received:");
		for (int i = 0; i < serializedCEP.length; i++) {
			System.out.println(serializedCEP[i]);
			patterns[i] = Serializer.Instance.unmarshallObject(
					ContextEventPattern.class, serializedCEP[i],
					Activator.class.getClassLoader());
		}

		final ContextProvider info = new ContextProvider(ContextProvider.MY_URI);
		info.setType(ContextProviderType.controller);
		info.setProvidedEvents(patterns);

		System.out.println("Got ImportResponse. ContextProvider events count: "
				+ ((info.getProvidedEvents() == null) ? " is null " : info
						.getProvidedEvents().length));

		internal.setContextProvider(info);
		internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
		busMembersToImport.add(internal);
	}

	public void refreshProxy(final ProxyRegistration proxyRegistration)
			throws IOException, ClassNotFoundException {
		manager.refreshProxy(proxyRegistration);
	}

	public boolean unimportRemoteService(final ImportEntry importEntry)
			throws IOException, ClassNotFoundException {
		final InternalImportOperation operation = (InternalImportOperation) importEntry
				.getOperation();
		final String uuid = operation.getUuid();
		importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
		System.out.println("Unimporting RemoteService");

		operation.setOp(RepoOperation.Purge);

		final ImportRequest importRequest = new ImportRequest(
				operation.getType(), uuid);
		importRequest.setId(operation.getRemoteRegisteredProxyId());

		System.out.println("Sending ImportRequest internalImportRemoteService");
		communicator.sendImportRemoval(Serializer.Instance
				.marshall(importRequest));
		busMembersToImport.add(operation);
		try {
			System.out.println("Waiting for proxy unregistration");
			importingMock.get(uuid).take();
			System.out.println("Continuing");
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ImportEntry importRemoteUI(final BusMember sourceMember,
			final String uiType) throws IOException, ClassNotFoundException {
		final String uuid = UUID.randomUUID().toString();
		importingMock.put(uuid, new ArrayBlockingQueue<ImportEntry>(1));
		System.out.println("Importing RemoteUI");
		internalImportRemoteUI(uuid, sourceMember, uiType);
		try {
			System.out.println("Waiting for proxy registration");
			final ImportEntry entry = importingMock.get(uuid).take();
			System.out.println("Continuing");
			return entry;
		} catch (final InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void internalImportRemoteUI(final String uuid,
			final BusMember sourceMember, final String targetUIType)
			throws IOException, ClassNotFoundException {
		final InternalImportOperation internal = new InternalImportOperation(
				sourceMember, RepoOperation.Publish, uuid);

		final ImportRequest importRequest = new ImportRequest(
				internal.getType(), uuid);
		importRequest.setModalityRegex(targetUIType);

		if (!performInterceptorChainExecution(uuid, importRequest,
				EIOperationManager.Type.Service)) {
			return;
		}
		// TODO
		/*
		 * System.out.println("Sending ImportRequest internalImportRemoteService"
		 * ); Message m = communicator.sendImportRequest(Serializer.Instance
		 * .marshall(importRequest)); String registeredRemoteProxyId =
		 * ((ProxyRegistration) m.getContent()) .getId(); String[]
		 * serializedProfiles = (String[]) ((ProxyRegistration) m
		 * .getContent()).getReturnedValues();
		 * 
		 * System.out.println("Got ImportResponse. ServiceProfiles count: " +
		 * serializedProfiles.length); ServiceProfile[] profiles = new
		 * ServiceProfile[serializedProfiles.length]; for (int i = 0; i <
		 * serializedProfiles.length; i++) { profiles[i] =
		 * Serializer.Instance.unmarshallObject( ServiceProfile.class,
		 * serializedProfiles[i], Activator.class.getClassLoader()); }
		 * 
		 * internal.setRealizedServices(profiles);
		 * internal.setRemoteRegisteredProxyId(registeredRemoteProxyId);
		 * busMembersToImport.add(internal);
		 */
	}

	public void busMemberAdded(final BusMember member, final BusType type) {
		// TODO Auto-generated method stub

	}

	public void busMemberRemoved(final BusMember member, final BusType type) {
		// TODO Auto-generated method stub

	}

	public void regParamsAdded(final String busMemberID, final Resource[] params) {
		// TODO Auto-generated method stub

	}

	public void regParamsRemoved(final String busMemberID,
			final Resource[] params) {
		// TODO Auto-generated method stub

	}

}
