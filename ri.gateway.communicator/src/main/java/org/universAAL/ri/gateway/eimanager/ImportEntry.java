/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science 

See the NOTICE file distributed with this work for additional
information regarding copyright ownership

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
