package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.IOException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.ui.UICaller;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyUICaller extends ProxyBusMember{

    private ProxiedUICaller caller;
    
    public ProxyUICaller(ExportedProxyManager manager, String targetId,
	    ModuleContext mc) {
	super(manager, targetId, mc);
	caller = new ProxiedUICaller(mc);
    }
    
    public void invoke(UIRequest req) {
	caller.sendUIRequest(req);
    }
    
    public void removeProxy() {
	caller.close();
    }

    class ProxiedUICaller extends UICaller {

	protected ProxiedUICaller(ModuleContext context) {
	    super(context);
	}

	public void communicationChannelBroken() {
	}

	public void dialogAborted(String dialogID) {
	}

	public void handleUIResponse(UIResponse input) {
	    try {
		((ExportedProxyManager)getManager()).handleUIResponse(targetId, input);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }
    
    public String getId() {
	return caller.getMyID();
    }
}
