package org.universAAL.ri.gateway.eimanager.impl.importing;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;
import org.universAAL.ri.gateway.eimanager.impl.registry.RepoOperation;

public class InternalImportOperation extends InternalEIOperation {
    
    private String uuid;
    private String remoteRegisteredProxyId;
    
    private ServiceProfile[] realizedServices;

    private ContextProvider contextProvider;
    
    private UIHandlerProfile uiHandlerProfile;
    
    public InternalImportOperation(BusMember member, RepoOperation op, String uuid) {
	super(member, op);
	this.uuid = uuid;
    }

    public ServiceProfile[] getRealizedServices() {
	return realizedServices;
    }

    public void setRealizedServices(ServiceProfile[] realizedServices) {
	this.realizedServices = realizedServices;
    }

    public String getRemoteRegisteredProxyId() {
	return remoteRegisteredProxyId;
    }

    public void setRemoteRegisteredProxyId(String remoteRegisteredProxyId) {
	this.remoteRegisteredProxyId = remoteRegisteredProxyId;
    }

    public String getUuid() {
	return uuid;
    }

    public void setUuid(String uuid) {
	this.uuid = uuid;
    }

    public ContextProvider getContextProvider() {
	return contextProvider;
    }

    public void setContextProvider(ContextProvider contextProvider) {
	this.contextProvider = contextProvider;
    }

    public UIHandlerProfile getUiHandlerProfile() {
	return uiHandlerProfile;
    }

    public void setUiHandlerProfile(UIHandlerProfile uiHandlerProfile) {
	this.uiHandlerProfile = uiHandlerProfile;
    }
    
   
}
