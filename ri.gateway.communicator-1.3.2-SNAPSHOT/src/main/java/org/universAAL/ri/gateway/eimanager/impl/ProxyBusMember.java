package org.universAAL.ri.gateway.eimanager.impl;

import org.universAAL.middleware.container.ModuleContext;

public abstract class ProxyBusMember {
    
    protected ModuleContext mc;
    protected String targetId;
    protected String remoteBusMemberId;
    private final AbstractProxyManager manager;
    
    public ProxyBusMember(AbstractProxyManager manager, String targetId, String remoteBusMemberId, ModuleContext mc){
	this.mc = mc;
	this.targetId = targetId;
	this.manager = manager;
	this.remoteBusMemberId = remoteBusMemberId;
    }
    
    public String getRemoteProxyBusMemberId(){
	return targetId;
    }
    
    public abstract void removeProxy();

    public AbstractProxyManager getManager() {
	return manager;
    }
    
    public abstract String getId();

	public String getRemoteBusMemberId() {
		return remoteBusMemberId;
	}
}
