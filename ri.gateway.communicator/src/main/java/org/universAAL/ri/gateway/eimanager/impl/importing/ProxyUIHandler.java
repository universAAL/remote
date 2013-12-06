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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
//import org.universAAL.middleware.ui.UIHandler;
//import org.universAAL.middleware.ui.UIHandlerProfile;
//import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.ri.gateway.eimanager.impl.AbstractProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyUIHandler extends ProxyBusMember {

public ProxyUIHandler(AbstractProxyManager manager, String targetId,
	    String remoteBusMemberId, ModuleContext mc) {
	super(manager, targetId, remoteBusMemberId, mc);
	// TODO Auto-generated constructor stub
    }

//	private UIHandler handler;
	private Set<ServiceProfile> profilesSet;
	
//	public ProxyUIHandler(UIHandlerProfile[] handlerProfiles,
//			ImportedProxyManager manager, final String targetId,
//			ModuleContext mc) {
//		super(manager, targetId,"", mc);
//		
//		profilesSet = new HashSet<ServiceProfile>();
//		
//		handler = new UIHandler(mc, handlerProfiles) {
//
//			public void handleUICall(UIRequest uicall) {
//				try {
//					((ImportedProxyManager) getManager())
//							.realizeRemoteUIRequest(targetId, uicall);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//
//			public Resource cutDialog(String dialogID) {
//				return null;
//			}
//
//			public void communicationChannelBroken() {
//			}
//
//			public void adaptationParametersChanged(String dialogID,
//					String changedProp, Object newVal) {
//			}
//		};
//	}

	public void removeProxy() {
//		handler.close();
	}

	public String getId() {
		return null;//handler.getMyID();
	}
	
	public Set<ServiceProfile> getProfilesSet() {
		return profilesSet;
	}

	public void setProfilesSet(Set<ServiceProfile> profilesSet) {
		this.profilesSet = profilesSet;
	}
}
