package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.io.IOException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyServiceCallee extends ProxyBusMember {
    
    private ServiceCallee callee;
    
    public ProxyServiceCallee(ServiceProfile[] realizedServices, ImportedProxyManager manager, final String targetId,
	    ModuleContext mc) {
	super(manager, targetId, mc);

	callee = new ServiceCallee(mc, realizedServices) {
	    
	    @Override
	    public ServiceResponse handleCall(ServiceCall call) {
		ServiceResponse response;
		try {
		    response = ((ImportedProxyManager)getManager()).realizeRemoteServiceRequest(targetId, call.getRequest());
		    return response;
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (ClassNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return null;	
	    }
	    
	    @Override
	    public void communicationChannelBroken() {
	    }
	};
    }

    public void removeProxy() {
	callee.close();
    }

    public String getId() {
	return callee.getMyID();
    }
}
