package org.universAAL.ri.gateway.eimanager;

import org.universAAL.middleware.bus.member.BusMember;


public interface RepoEIManager {
    public void memberAdded(BusMember member);
    
    public void memberRemoved(BusMember member);
}
