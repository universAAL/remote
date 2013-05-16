package org.universAAL.ri.gateway.eimanager.impl;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.tracker.IBusMemberRegistry.BusType;
import org.universAAL.middleware.tracker.IBusMemberRegistryListener;
import org.universAAL.ri.gateway.eimanager.RepoEIManager;

public class EIManagerRegistryListener implements IBusMemberRegistryListener{
    
    private RepoEIManager manager;
    
    public EIManagerRegistryListener(RepoEIManager manager){
	this.manager = manager;
    }
    
    public void busMemberAdded(BusMember member, BusType type) {
	try {
	    manager.memberAdded(member);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void busMemberRemoved(BusMember member, BusType type) {
	try {
	    manager.memberRemoved(member);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
