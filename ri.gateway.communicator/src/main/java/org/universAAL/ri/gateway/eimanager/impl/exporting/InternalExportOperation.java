package org.universAAL.ri.gateway.eimanager.impl.exporting;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public class InternalExportOperation extends InternalEIOperation {
    
    public InternalExportOperation(BusMember member, RepoOperation op) {
	super(member, op);
    }

   
}
