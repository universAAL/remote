package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.impl.AbstractProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ImportedProxyManager extends AbstractProxyManager {

    private Map<String, ProxyBusMember> generatedProxies;
    private GatewayCommunicator communicator;

    public ImportedProxyManager(GatewayCommunicator communicator) {
	super();
	this.generatedProxies = new HashMap<String, ProxyBusMember>();
	this.communicator = communicator;
    }

    public void registerProxies(InternalImportOperation op) {
	ProxyBusMember proxy = null;
	switch (op.getType()) {
	case ServiceCaller:
	    proxy = new ProxyServiceCallee(op.getRealizedServices(), this,
		    op.getRemoteRegisteredProxyId(), Activator.mc);
	    break;
	case ContextSubscriber:
	    proxy = new ProxyContextPublisher((ContextSubscriber)op.getBusMember(),op.getContextProvider(), this, op.getRemoteRegisteredProxyId(),
		    Activator.mc);
	case UICaller:
	    //TODO implement
	    //proxy = new ProxyUIHandler(op.getUiHandlerProfile(), this,
	    //op.getRemoteRegisteredProxyId(), Activator.mc);
	    break;
	}
	if (proxy != null) {
	    generatedProxies.put(op.getRemoteRegisteredProxyId(), proxy);
	}
    }

    public void unregisterProxies(InternalImportOperation op) {
	String id = op.getRemoteRegisteredProxyId();
	if (generatedProxies.containsKey(id)) {
	    generatedProxies.get(id).removeProxy();
	    generatedProxies.remove(id);
	}
    }

    public void realizeLocalContextEventPublishment(String sourceId,
	    ContextEvent event) {
	((ProxyContextPublisher) generatedProxies.get(sourceId))
		.publishContextEvent(event);
    }

    public ServiceResponse realizeRemoteServiceRequest(String targetId, ServiceRequest request) throws IOException, ClassNotFoundException {
	Message toSend = Serializer.Instance.marshallObject(request);
	toSend.setRemoteProxyRegistrationId(targetId);
	
	Message[] returnMessage = communicator.sendServiceRequest(
		toSend);
	//TODO reimplement mechanisms for choosing proper response from multiple spaces
	
	ServiceResponse response = Serializer.Instance.unmarshallObject(ServiceResponse.class,
		returnMessage[0]);
	System.out.println("Received response: " + response);
	return response;
    }

    public void realizeLocalUIResponsePublishment(String sourceId,
	    UIResponse response) {
	// TODO implement
	// ((ProxyUIHandler)generatedProxies.get(sourceId)).
    }
    
    public void realizeRemoteUIRequest(UIRequest uiRequest) {
	// TODO implement
	//communicator.sendUIRequest(
	//	Serializer.Instance.marshallObject(uiRequest));
    }

}
