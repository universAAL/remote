package org.universAAL.ri.gateway.eimanager.impl;

import org.universAAL.middleware.container.ModuleContext;

public abstract class ProxyBusMember {
    
    protected ModuleContext mc;
    protected String targetId;
    private final AbstractProxyManager manager;
    
    public ProxyBusMember(AbstractProxyManager manager, String targetId, ModuleContext mc){
	this.mc = mc;
	this.targetId = targetId;
	this.manager = manager;
    }
    
    public String getTargetBusMember(){
	return targetId;
    }
    
    public abstract void removeProxy();

    public AbstractProxyManager getManager() {
	return manager;
    }
    
    public abstract String getId();
}
