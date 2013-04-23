package org.universAAL.ri.gateway.eimanager.impl.importing;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIHandler;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyUIHandler extends ProxyBusMember {

	private UIHandler handler;
	private Set<ServiceProfile> profilesSet;
	
	public ProxyUIHandler(UIHandlerProfile[] handlerProfiles,
			ImportedProxyManager manager, final String targetId,
			ModuleContext mc) {
		super(manager, targetId,"", mc);
		
		profilesSet = new HashSet<ServiceProfile>();
		
		handler = new UIHandler(mc, handlerProfiles) {

			public void handleUICall(UIRequest uicall) {
				try {
					((ImportedProxyManager) getManager())
							.realizeRemoteUIRequest(targetId, uicall);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public Resource cutDialog(String dialogID) {
				return null;
			}

			public void communicationChannelBroken() {
			}

			public void adaptationParametersChanged(String dialogID,
					String changedProp, Object newVal) {
			}
		};
	}

	public void removeProxy() {
		handler.close();
	}

	public String getId() {
		return handler.getMyID();
	}
	
	public Set<ServiceProfile> getProfilesSet() {
		return profilesSet;
	}

	public void setProfilesSet(Set<ServiceProfile> profilesSet) {
		this.profilesSet = profilesSet;
	}
}
