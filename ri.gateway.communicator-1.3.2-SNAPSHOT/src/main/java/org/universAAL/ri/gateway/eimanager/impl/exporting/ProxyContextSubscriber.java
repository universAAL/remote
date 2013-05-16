package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.IOException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyContextSubscriber extends ProxyBusMember {
    
    private ContextSubscriber subscriber;
    
    public ProxyContextSubscriber(ExportedProxyManager manager, ModuleContext mc, ContextEventPattern[] subscriptions) {
	super(manager,"","", mc);
	subscriber = new ProxiedContextSubscriber(mc, subscriptions);
    }

    class ProxiedContextSubscriber extends ContextSubscriber {

	protected ProxiedContextSubscriber(ModuleContext context,
		ContextEventPattern[] initialSubscriptions) {
	    super(context, initialSubscriptions);
	}
	
	public void communicationChannelBroken() {
	}

	public void handleContextEvent(ContextEvent event) {
	    try {
		((ExportedProxyManager)getManager()).handleContextEvent(subscriber.getMyID(), event);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	
    }

    public void removeProxy() {
	subscriber.close();
    }

    public String getId() {
	return subscriber.getMyID();
    }
}
