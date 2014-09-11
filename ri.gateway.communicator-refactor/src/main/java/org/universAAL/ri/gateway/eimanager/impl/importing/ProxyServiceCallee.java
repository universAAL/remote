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
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

@Deprecated
public class ProxyServiceCallee extends ProxyBusMember {

    private ServiceCallee callee;
    private Set<ServiceProfile> profilesSet;

    public ProxyServiceCallee(final ServiceProfile[] realizedServices,
	    final ImportedProxyManager manager, final String targetId,
	    final String remoteBusMemberId, final ModuleContext mc) {
	super(manager, targetId, remoteBusMemberId, mc);
	profilesSet = new HashSet<ServiceProfile>();
	for (final ServiceProfile profile : realizedServices) {
	    profilesSet.add(profile);
	}
	callee = new CustomServiceCalee(mc, realizedServices);
    }

    @Override
    public void removeProxy() {
	callee.close();
    }

    @Override
    public String getId() {
	return callee.getMyID();
    }

    public void refreshProfiles(final ServiceProfile[] profiles) {
	final ServiceCallee old = callee;
	callee = new CustomServiceCalee(mc, profiles);
	old.close();

	profilesSet.clear();
	for (final ServiceProfile profile : profiles) {
	    profilesSet.add(profile);
	}

    }

    public Set<ServiceProfile> getProfilesSet() {
	return profilesSet;
    }

    public void setProfilesSet(final Set<ServiceProfile> profilesSet) {
	this.profilesSet = profilesSet;
    }

    private class CustomServiceCalee extends ServiceCallee {
	protected CustomServiceCalee(final ModuleContext context,
		final ServiceProfile[] realizedServices) {
	    super(context, realizedServices);
	}

	@Override
	public ServiceResponse handleCall(final ServiceCall call) {
	    ServiceResponse response;
	    try {
		response = ((ImportedProxyManager) getManager())
			.realizeRemoteServiceRequest(targetId, call,
				remoteBusMemberId);
		return response;
	    } catch (final IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (final ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    return null;
	}

	@Override
	public void communicationChannelBroken() {
	}
    }

}
