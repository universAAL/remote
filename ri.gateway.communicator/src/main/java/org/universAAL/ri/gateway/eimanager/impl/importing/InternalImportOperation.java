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
package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.util.List;
import java.util.Map;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public class InternalImportOperation extends InternalEIOperation {

    private String uuid;
    private String remoteRegisteredProxyId;

    private Map<String, List<ServiceProfile>> realizedServices;

    private ContextProvider contextProvider;

    private UIHandlerProfile[] uiHandlerProfile;

    private ImportEntry importEntry;

    public InternalImportOperation(final BusMember member,
	    final RepoOperation op, final String uuid) {
	super(member, op);
	this.uuid = uuid;
    }

    public String getRemoteRegisteredProxyId() {
	return remoteRegisteredProxyId;
    }

    public void setRemoteRegisteredProxyId(final String remoteRegisteredProxyId) {
	this.remoteRegisteredProxyId = remoteRegisteredProxyId;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(final String uuid) {
	this.uuid = uuid;
    }

    public ContextProvider getContextProvider() {
	return contextProvider;
    }

    public void setContextProvider(final ContextProvider contextProvider) {
	this.contextProvider = contextProvider;
    }

    public UIHandlerProfile[] getUiHandlerProfiles() {
	return uiHandlerProfile;
    }

    public void setUiHandlerProfiles(final UIHandlerProfile uiHandlerProfile[]) {
	this.uiHandlerProfile = uiHandlerProfile;
    }

    public ImportEntry getImportEntry() {
	return importEntry;
    }

    public void setImportEntry(final ImportEntry importEntry) {
	if (this.importEntry != null) {
	    throw new IllegalArgumentException("Cannot do it twice");
	}
	this.importEntry = importEntry;
    }

	public Map<String, List<ServiceProfile>> getRealizedServices() {
		return realizedServices;
	}

	public void setRealizedServices(Map<String, List<ServiceProfile>> realizedServices) {
		this.realizedServices = realizedServices;
	}
}

