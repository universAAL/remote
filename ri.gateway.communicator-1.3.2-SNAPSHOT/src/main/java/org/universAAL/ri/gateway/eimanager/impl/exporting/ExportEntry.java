package org.universAAL.ri.gateway.eimanager.impl.exporting;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;


public class ExportEntry extends RegistryEntry {
    
    private BusMember member;
    
    public ExportEntry(String id,BusMember member, InternalEIOperation operation) {
	super(id,operation);
	this.member = member;
    }

    public BusMember getMember() {
	return member;
    }

    public void setMember(BusMember member) {
	this.member = member;
    }

}
