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
package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextBusFacade;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBusFacade;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.tracker.IBusMemberRegistry;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
//import org.universAAL.middleware.ui.IUIBus;
//import org.universAAL.middleware.ui.UIBusFacade;
//import org.universAAL.middleware.ui.UIHandlerProfile;
//import org.universAAL.middleware.ui.UIRequest;
//import org.universAAL.middleware.ui.UIResponse;
//import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.impl.CommunicatorStarter;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.impl.AbstractProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.BusMemberType;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;

public class ExportedProxyManager extends AbstractProxyManager implements
		IBusMemberRegistryListener {

	private Map<String, ProxyBusMember> generatedProxies;
	private GatewayCommunicator communicator;
	private ServiceBus serviceBus;
	private ContextBus contextBus;
//	private IUIBus uiBus;

	public ExportedProxyManager(final GatewayCommunicator communicator) {
		super();
		generatedProxies = new HashMap<String, ProxyBusMember>();
		this.communicator = communicator;
		serviceBus = ServiceBusFacade.fetchBus(CommunicatorStarter.mc);
		contextBus = ContextBusFacade.fetchBus(CommunicatorStarter.mc);
//		uiBus = UIBusFacade.fetchBus(CommunicatorStarter.mc);

		IBusMemberRegistry registry = (IBusMemberRegistry) CommunicatorStarter.mc
				.getContainer().fetchSharedObject(CommunicatorStarter.mc,
						IBusMemberRegistry.busRegistryShareParams);
		registry.addBusRegistryListener(this, false);
	}

	public ServiceResponse sendServiceRequest(final String sourceId,
			final ServiceCall call, final String memberId) {
		if (generatedProxies.get(sourceId) != null) {
			return ((ProxyServiceCaller) generatedProxies.get(sourceId))
					.invoke(call, memberId);
		} else {
			return null;
		}
	}

	public void handleContextEvent(final String targetId,
			final ContextEvent contextEvent) throws IOException {
		Message message = Serializer.Instance.marshallObject(contextEvent);
		message.setRemoteProxyRegistrationId(targetId);
		communicator.sendContextEvent(message);
	}

//	public void handleUIResponse(final String targetId,
//			final UIResponse response) throws IOException {
//		communicator.sendUIResponse(Serializer.Instance
//				.marshallObject(response));
//	}

//	public void sendUIRequest(final String sourceId, final UIRequest request) {
//		if (generatedProxies.get(sourceId) != null) {
//			((ProxyUICaller) generatedProxies.get(sourceId)).invoke(request);
//		}
//	}

	public ProxyRegistration registerProxies(final ImportRequest importRequest)
			throws IOException, ClassNotFoundException {
		ProxyBusMember member = null;
		ProxyRegistration proxyRegistration = null;
		switch (BusMemberType.valueOf(importRequest.getMember())) {
		case ServiceCaller:
			Map<String,List<ServiceProfile>> profilesMap = (Map<String,List<ServiceProfile>>)serviceBus.getMatchingServices(importRequest.getServiceType());

			member = new ProxyServiceCaller(this, importRequest.getId(),
				CommunicatorStarter.mc, importRequest.getServerNamespace(),
					importRequest.getServiceType(), profilesMap);
			
//			ServiceProfile[] profilesArray = profilesMap.values().toArray(new ServiceProfile[0]);
			
			proxyRegistration = new ProxyRegistration(member.getId(), profilesMap);
			break;
		case ContextSubscriber:
			String[] serializedCpe = importRequest.getCpe();
			ContextEventPattern[] cpe = new ContextEventPattern[serializedCpe.length];
			System.out.println("Export received:");
			for (int i = 0; i < serializedCpe.length; i++) {
				// System.out.println(serializedCpe[i]);
				cpe[i] = Serializer.Instance.unmarshallObject(
						ContextEventPattern.class, serializedCpe[i], Thread
								.currentThread().getContextClassLoader());
			}
			member = new ProxyContextSubscriber(this, CommunicatorStarter.mc, cpe);

			//ContextEventPattern[] matchedEvents = contextBus
			//		.getAllProvisions(member.getId());
			proxyRegistration = new ProxyRegistration(member.getId(),
					cpe);
			break;
		case UICaller:
//			UIHandlerProfile[] uiProfiles = uiBus.getMatchingProfiles(importRequest.getModalityRegex());
//
//			member = new ProxyUICaller(this, importRequest.getId(),
//				CommunicatorStarter.mc, importRequest.getModalityRegex(), uiProfiles);
//
//			proxyRegistration = new ProxyRegistration(member.getId(),
//					uiProfiles);
//			break;
		}
		if (member != null) {
			generatedProxies.put(member.getId(), member);
		}
		return proxyRegistration;
	}

	public void unregisterProxies(final ImportRequest importRequest) {
		String id = importRequest.getId();
		if (generatedProxies.containsKey(id)) {
			generatedProxies.get(id).removeProxy();
			generatedProxies.remove(id);
		}
	}

	public void registryEntryAdded(final RegistryEntry entry) {
		/*
		 * if (entry instanceof ExportEntry) {
		 * registerProxiesIfNecessary((ExportEntry) entry); }
		 */
	}

	public void registryEntryRemoved(final RegistryEntry entry) {
		/*
		 * if (entry instanceof ExportEntry) {
		 * unregisterProxiesIfNecessary((ExportEntry) entry); }
		 */
	}

	public void busMemberAdded(final BusMember arg0, final BusType arg1) {
		reloadServices();
	}

	public void busMemberRemoved(final BusMember arg0, final BusType arg1) {
		reloadServices();
	}

	/*
	 * This method refreshes the ServiceProfiles/UIHandlerProfiles of every proxy that is remotely
	 * imported on remote spaces. If a set of ServiceProfiles/UIHandlerProfiles changes for any of
	 * exported services, the information is sent to other spaces to refresh
	 * their knowledge.
	 */
	private void reloadServices() {
		for (ProxyBusMember p : generatedProxies.values()) {
			if (p instanceof ProxyServiceCaller) {
				ProxyServiceCaller member = (ProxyServiceCaller) p;
				Map<String,List<ServiceProfile>> profilesMap = (Map<String,List<ServiceProfile>>)serviceBus.getMatchingServices(member.getServiceType());
				
				List<ServiceProfile> profilesList = new ArrayList<ServiceProfile>();
				for(List<ServiceProfile> value : profilesMap.values()){
					profilesList.addAll(value);
				}
				
				ServiceProfile[] profiles = profilesList.toArray(new ServiceProfile[0]);
				
				if (!Arrays.equals(profiles, member.getProfiles())) {
					try {
						member.setProfiles(profilesMap);
						Map<String, List<String>> serializedMap = new HashMap<String, List<String>>();
						
						for(String key: profilesMap.keySet()){
							if (serializedMap.get(key) == null){
								serializedMap.put(key, new ArrayList<String>());
							}
							for(ServiceProfile pr : profilesMap.get(key)){
								serializedMap.get(key).add((String) Serializer.Instance
										.marshallObject(pr).getContent());
							}
						}
						
						/*serialized = new String[profiles.values().size()+2*profiles.keySet().s];
						
						
						for (int i = 2; i < profiles.values().size().length + 2; i++) {
							serialized[i] = (String) Serializer.Instance
									.marshallObject(profiles[i]).getContent();
						}*/
						Object reg = new ProxyRegistration(member.getId(),
								serializedMap);
						communicator.sendImportRefresh(new Message(reg));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (p instanceof ProxyUICaller) {
//				ProxyUICaller member = (ProxyUICaller) p;
//				UIHandlerProfile[] profiles = uiBus.getMatchingProfiles(member.getModalityRegex());
//				if (!Arrays.equals(profiles, member.getHandlerProfiles())) {
//					try {
//						member.setHandlerProfiles(profiles);
//						String[] serialized = new String[profiles.length];
//						for (int i = 0; i < profiles.length; i++) {
//							serialized[i] = (String) Serializer.Instance
//									.marshallObject(profiles[i]).getContent();
//						}
//						Object reg = new ProxyRegistration(member.getId(),
//								serialized);
//						communicator.sendImportRefresh(new Message(reg));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
			}
		}
	}

}
