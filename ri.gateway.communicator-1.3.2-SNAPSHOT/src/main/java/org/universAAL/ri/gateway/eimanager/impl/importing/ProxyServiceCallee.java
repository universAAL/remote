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

public class ProxyServiceCallee extends ProxyBusMember {

	private ServiceCallee callee;
	private Set<ServiceProfile> profilesSet;

	public ProxyServiceCallee(final ServiceProfile[] realizedServices,
			final ImportedProxyManager manager, final String targetId, final String remoteBusMemberId,
			final ModuleContext mc) {
		super(manager, targetId, remoteBusMemberId, mc);
		profilesSet = new HashSet<ServiceProfile>();
		for (ServiceProfile profile : realizedServices) {
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
		ServiceCallee old = callee;
		callee = new CustomServiceCalee(mc, profiles);
		old.close();

		profilesSet.clear();
		for (ServiceProfile profile : profiles) {
			profilesSet.add(profile);
		}

	}

	public Set<ServiceProfile> getProfilesSet() {
		return profilesSet;
	}

	public void setProfilesSet(Set<ServiceProfile> profilesSet) {
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
						.realizeRemoteServiceRequest(targetId,
								call, remoteBusMemberId);
				return response;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
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
