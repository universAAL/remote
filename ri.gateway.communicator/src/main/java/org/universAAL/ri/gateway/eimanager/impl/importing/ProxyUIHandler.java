package org.universAAL.ri.gateway.eimanager.impl.importing;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyUIHandler extends ProxyBusMember {
    
    private UIHandler handler;
    
    public ProxyUIHandler(UIHandlerProfile handlerProfile, ImportedProxyManager manager, String targetId,
	    ModuleContext mc) {
	super(manager, targetId, mc);

	handler = new UIHandler(mc, handlerProfile) {
	    
	    public void handleUICall(UIRequest uicall) {
		((ImportedProxyManager)getManager()).realizeRemoteUIRequest(uicall);
	    }
	    
	    public Resource cutDialog(String dialogID) {
		return null;
	    }
	    
	    public void communicationChannelBroken() {
	    }
	    
	    public void adaptationParametersChanged(String dialogID,
		    String changedProp, Object newVal) {
	    }
	};
    }

    public void removeProxy() {
	handler.close();
    }
    
    public String getId() {
	return handler.getMyID();
    }

}
