package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceBus;
import org.universAAL.middleware.service.ServiceBusFacade;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyServiceCaller extends ProxyBusMember {

	private DefaultServiceCaller caller;
	private String serverNamespace;
	private String serviceType;
	private Map<String, List<ServiceProfile>> profiles;
	private ServiceBus bus;

	public ProxyServiceCaller(final ExportedProxyManager manager,
			final String targetId, final ModuleContext mc,
			final String serverNamespace, final String serviceType,
			final Map<String, List<ServiceProfile>> profiles) {
		super(manager, targetId, "", mc);
		caller = new DefaultServiceCaller(mc);
		this.serverNamespace = serverNamespace;
		this.serviceType = serviceType;
		this.profiles = profiles;
		bus = ServiceBusFacade.fetchBus(Activator.mc);
	}

	public ServiceResponse invoke(final ServiceCall call, final String memberId) {
		return ((ServiceCallee) ((AbstractBus) bus).getBusMember(memberId))
				.handleCall(call);
	}

	/*
	 * DEPRECATED
	 * 
	 * private void prepareRequestedOutput(final List outputs) { if (outputs !=
	 * null && !outputs.isEmpty()) { for (int i = outputs.size() - 1; i > -1;
	 * i--) { ProcessOutput po = (ProcessOutput) outputs.remove(i);
	 * 
	 * Object val = po.getParameterValue(); if (val == null) continue;
	 * 
	 * String poSuffixUri = po.getURI().substring( po.getURI().indexOf("#") +
	 * 1);
	 * 
	 * ProcessOutput substitutedWithServerURI = new ProcessOutput(
	 * serverNamespace + poSuffixUri);
	 * substitutedWithServerURI.setParameterValue(val);
	 * outputs.add(substitutedWithServerURI); } } }
	 */

	@Override
	public void removeProxy() {
		caller.close();
	}

	@Override
	public String getId() {
		return caller.getMyID();
	}

	public String getServiceType() {
		return serviceType;
	}

	public ServiceProfile[] getProfiles() {
		List<ServiceProfile> profilesList = new ArrayList<ServiceProfile>();
		for (List<ServiceProfile> value : profiles.values()) {
			profilesList.addAll(value);
		}
		return profilesList.toArray(new ServiceProfile[0]);
	}

	public void setProfiles(final Map<String, List<ServiceProfile>> profiles) {
		this.profiles = profiles;
	}

}
