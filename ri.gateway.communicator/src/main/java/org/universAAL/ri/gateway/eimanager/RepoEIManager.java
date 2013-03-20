package org.universAAL.ri.gateway.eimanager;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.eimanager.impl.EIConstraintEvaluator;
import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;
import org.universAAL.ri.gateway.eimanager.impl.exporting.InternalExportOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public interface RepoEIManager {
    public void memberAdded(BusMember member);
    
    public void memberRemoved(BusMember member);
}
