package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextBusFacade;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBusFacade;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.impl.AbstractProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.BusMemberType;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;

public class ExportedProxyManager extends AbstractProxyManager {

    private Map<String, ProxyBusMember> generatedProxies;
    private GatewayCommunicator communicator;

    public ExportedProxyManager(final GatewayCommunicator communicator) {
	super();
	generatedProxies = new HashMap<String, ProxyBusMember>();
	this.communicator = communicator;
    }

    public ServiceResponse sendServiceRequest(final String sourceId,
	    final ServiceRequest req) {
	if (generatedProxies.get(sourceId) != null) {
	    return ((ProxyServiceCaller) generatedProxies.get(sourceId))
		    .invoke(req);
	} else {
	    return null;
	}
    }

    public void handleContextEvent(final String targetId, final ContextEvent contextEvent) throws IOException {
	Message message = Serializer.Instance.marshallObject(contextEvent);
	message.setRemoteProxyRegistrationId(targetId);
	communicator.sendContextEvent(message);
    }
    
    
    public void handleUIResponse(final String targetId, final UIResponse response) throws IOException {
	communicator.sendUIResponse(
		Serializer.Instance.marshallObject(response));
    }
    

    public void sendUIRequest(final String sourceId, final UIRequest request) {
	if (generatedProxies.get(sourceId) != null) {
	    ((ProxyUICaller) generatedProxies.get(sourceId)).invoke(request);
	}
    }

    public ProxyRegistration registerProxies(final ImportRequest importRequest) throws IOException, ClassNotFoundException {
	ProxyBusMember member = null;
	ProxyRegistration proxyRegistration = null;
	switch (BusMemberType.valueOf(importRequest.getMember())) {
	case ServiceCaller:
	    member = new ProxyServiceCaller(this, importRequest.getId(),
		    Activator.mc, importRequest.getServerNamespace());
	    
	    ServiceBus serviceBus = ServiceBusFacade.fetchBus(Activator.mc);
	    ServiceProfile[] profiles = serviceBus.getMatchingService("NOT_USED", importRequest.getServiceType());
	    proxyRegistration = new ProxyRegistration(member.getId(), profiles);
	    break;
	case ContextSubscriber:
	    String[] serializedCpe = importRequest.getCpe();
	    ContextEventPattern[] cpe = new ContextEventPattern[serializedCpe.length];
	    System.out.println("Export received:");
	    for(int i = 0; i < serializedCpe.length ; i++){
		//System.out.println(serializedCpe[i]);
		cpe[i] = Serializer.Instance.unmarshallObject(ContextEventPattern.class, serializedCpe[i], Thread.currentThread().getContextClassLoader());
	    }
	    member = new ProxyContextSubscriber(this,
		    Activator.mc, cpe);
	    
	    ContextBus contextBus = ContextBusFacade.fetchBus(Activator.mc);
	    ContextEventPattern[] matchedEvents = contextBus.getAllProvisions(member.getId());
	    proxyRegistration = new ProxyRegistration(member.getId(), matchedEvents);
	    break;
	case UICaller:
	    member = new ProxyUICaller(this, importRequest.getId(),
		    Activator.mc);
	    break;
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

}
