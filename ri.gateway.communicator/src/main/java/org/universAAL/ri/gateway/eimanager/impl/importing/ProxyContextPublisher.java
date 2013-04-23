package org.universAAL.ri.gateway.eimanager.impl.importing;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyContextPublisher extends ProxyBusMember {
    
    private DefaultContextPublisher publisher;
    
    private ContextSubscriber subscriber;
    
    public ProxyContextPublisher(ContextSubscriber subscriber, ContextProvider info, ImportedProxyManager manager, String targetId,
	    ModuleContext mc) {
	super(manager, targetId,"", mc);
	//TODO modify
	//this.publisher = new DefaultContextPublisher(mc, info);
	this.subscriber = subscriber;
    }
    
    public void publishContextEvent(ContextEvent event){
	subscriber.handleContextEvent(event);
	//publisher.publish(event);
    }
    
    public void removeProxy() {
	publisher.close();
    }
    
    public String getId() {
	return (publisher != null)? publisher.getMyID() : "SHOULD NOT BE SEEN";
    }
}
