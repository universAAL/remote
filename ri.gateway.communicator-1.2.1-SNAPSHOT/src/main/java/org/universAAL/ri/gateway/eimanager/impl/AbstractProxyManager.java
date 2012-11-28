package org.universAAL.ri.gateway.eimanager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.universAAL.ri.gateway.eimanager.impl.registry.IRegistryListener;

public abstract class AbstractProxyManager implements ProxyManager{
    
    protected Map<String, ProxyBusMember> proxiedBusMembers;
    
    public AbstractProxyManager(){
	proxiedBusMembers = new HashMap<String, ProxyBusMember>();
    }
    
    public void unregisterAllProxies() {
	Collection<ProxyBusMember> values =proxiedBusMembers.values();
	for(ProxyBusMember member : values){
	    member.removeProxy();
	}
    }
}
