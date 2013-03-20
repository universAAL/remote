package org.universAAL.ri.gateway.eimanager.impl.importing;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;


public class ImportEntry extends RegistryEntry {

    private BusMember member;
    private String uuid;
    
    public ImportEntry(String id,BusMember member, String uuid, InternalEIOperation operation) {
	super(id, operation);
	this.member = member;
	this.uuid = uuid;
    }

    public BusMember getMember() {
	return member;
    }

    public void setMember(BusMember member) {
	this.member = member;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }


}
