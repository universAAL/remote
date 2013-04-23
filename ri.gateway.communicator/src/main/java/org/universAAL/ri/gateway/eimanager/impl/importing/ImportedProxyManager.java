package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.impl.AbstractProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;

public class ImportedProxyManager extends AbstractProxyManager {

	private Map<String, List<ProxyBusMember>> generatedProxies;
	private Map<String, List<ServiceProfile>> remoteBusMembersImportedProfiles;
	private GatewayCommunicator communicator;

	public ImportedProxyManager(final GatewayCommunicator communicator) {
		super();
		this.generatedProxies = new HashMap<String, List<ProxyBusMember>>();
		this.remoteBusMembersImportedProfiles = new HashMap<String, List<ServiceProfile>>();
		this.communicator = communicator;
	}

	public void registerProxies(final InternalImportOperation op) {
		ProxyBusMember proxy = null;
		if (!generatedProxies.containsKey(op.getRemoteRegisteredProxyId())) {
			generatedProxies.put(op.getRemoteRegisteredProxyId(),
					new ArrayList<ProxyBusMember>());
		}
		switch (op.getType()) {
		case ServiceCaller:
			Map<String, List<ServiceProfile>> profilesMap = op
					.getRealizedServices();
			for (String key : profilesMap.keySet()) {
				for (ServiceProfile p : profilesMap.get(key)) {
					if (remoteBusMembersImportedProfiles.containsKey(key)) {
						if (remoteBusMembersImportedProfiles.get(key).contains(
								p)) {
							continue;
						}
					}else{
						remoteBusMembersImportedProfiles.put(key, new ArrayList<ServiceProfile>());
					}

					proxy = new ProxyServiceCallee(new ServiceProfile[] { p },
							this, op.getRemoteRegisteredProxyId(), key, Activator.mc);

					generatedProxies.get(op.getRemoteRegisteredProxyId()).add(
							proxy);
					remoteBusMembersImportedProfiles.get(key).add(p);
				}
			}

			break;
		case ContextSubscriber:
			proxy = new ProxyContextPublisher(
					(ContextSubscriber) op.getBusMember(),
					op.getContextProvider(), this,
					op.getRemoteRegisteredProxyId(), Activator.mc);
			generatedProxies.get(op.getRemoteRegisteredProxyId()).add(proxy);
		case UICaller:
			UIHandlerProfile[] profiles = op.getUiHandlerProfiles();
			proxy = new ProxyUIHandler(profiles, this,
					op.getRemoteRegisteredProxyId(), Activator.mc);
			generatedProxies.get(op.getRemoteRegisteredProxyId()).add(proxy);
			break;
		}

	}

	public void unregisterProxies(final InternalImportOperation op) {
		String id = op.getRemoteRegisteredProxyId();
		if (generatedProxies.containsKey(id)) {
			for (ProxyBusMember pr : generatedProxies.get(id)) {
				pr.removeProxy();
				if (pr instanceof ProxyServiceCallee){
					if (remoteBusMembersImportedProfiles.containsKey(pr.getRemoteBusMemberId())){
						for(ServiceProfile p : ((ProxyServiceCallee)pr).getProfilesSet()){
							remoteBusMembersImportedProfiles.get(pr.getRemoteBusMemberId()).remove(p);
						}
					}
				}
			}
			generatedProxies.remove(id);
		}
	}

	public void realizeLocalContextEventPublishment(final String sourceId,
			final ContextEvent event) {
		((ProxyContextPublisher) generatedProxies.get(sourceId))
				.publishContextEvent(event);
	}

	public ServiceResponse realizeRemoteServiceRequest(final String targetId,
			final ServiceCall request, final String remoteBusMemberId) throws IOException,
			ClassNotFoundException {
		Message toSend = Serializer.Instance.marshallObject(request);
		toSend.setRemoteProxyRegistrationId(targetId);
		toSend.setRemoteMemberId(remoteBusMemberId);
		
		Message[] returnMessage = communicator.sendServiceRequest(toSend);
		// TODO reimplement mechanisms for choosing proper response from
		// multiple spaces

		ServiceResponse response = Serializer.Instance.unmarshallObject(
				ServiceResponse.class, returnMessage[0]);
		System.out.println("Received response: " + response);
		return response;
	}

	public void realizeLocalUIResponsePublishment(final String sourceId,
			final UIResponse response) {
		// TODO implement
		// ((ProxyUIHandler)generatedProxies.get(sourceId)).
	}

	public void realizeRemoteUIRequest(final String targetId,
			final UIRequest uiRequest) throws IOException {
		Message toSend = Serializer.Instance.marshallObject(uiRequest);
		toSend.setRemoteProxyRegistrationId(targetId);
		communicator.sendUIRequest(toSend);
	}

	public void refreshProxy(final ProxyRegistration proxyRegistration) throws IOException, ClassNotFoundException {

		ProxyBusMember proxy = null;
		List<ProxyBusMember> members = generatedProxies.get(proxyRegistration
				.getId());
		for (ProxyBusMember m : members) {
			m.removeProxy();
		}
		generatedProxies.remove(proxyRegistration.getId());

		Map<String, List<String>> serializedProfilesMap = (Map<String, List<String>>) proxyRegistration
				.getReturnedValues();

		Map<String, List<ServiceProfile>> profilesMap = new HashMap<String, List<ServiceProfile>>();

		for (String key : serializedProfilesMap.keySet()) {
			if (profilesMap.get(key) == null) {
				profilesMap.put(key, new ArrayList<ServiceProfile>());
			}
			for (String serializedP : serializedProfilesMap.get(key)) {
				profilesMap.get(key).add(
						Serializer.Instance.unmarshallObject(
								ServiceProfile.class, serializedP,
								Activator.class.getClassLoader()));
			}
		}

		for (String key : profilesMap.keySet()) {
			for (ServiceProfile p : profilesMap.get(key)) {
				if (remoteBusMembersImportedProfiles.containsKey(key)) {
					if (remoteBusMembersImportedProfiles.get(key).contains(p)) {
						continue;
					}
				}

				proxy = new ProxyServiceCallee(new ServiceProfile[] { p },
						this, proxyRegistration.getId(),key, Activator.mc);

				generatedProxies.get(proxyRegistration.getId())
						.add(proxy);
			}
		}

	}
}
