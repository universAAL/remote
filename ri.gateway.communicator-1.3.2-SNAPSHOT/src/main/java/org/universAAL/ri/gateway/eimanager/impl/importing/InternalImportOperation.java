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

