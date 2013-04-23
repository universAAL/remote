package org.universAAL.ri.gateway.eimanager;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.eimanager.impl.importing.InternalImportOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RegistryEntry;

public class ImportEntry extends RegistryEntry {

    private BusMember member;
    private String uuid;
    private boolean success;
    private String errorMessage;
    
    public ImportEntry(final String id, final BusMember member,
	    final String uuid, final InternalImportOperation operation, boolean success, String errorMessage) {
	super(id, operation);
	operation.setImportEntry(this);
	this.member = member;
	this.uuid = uuid;
	this.success = success;
	this.errorMessage = errorMessage;
    }

    public BusMember getMember() {
	return member;
    }

    public void setMember(final BusMember member) {
	this.member = member;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(final String uuid) {
	this.uuid = uuid;
    }

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
