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
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;

public class ProxyServiceCaller extends ProxyBusMember {

    private final DefaultServiceCaller caller;
    private final String serverNamespace;
    private final String serviceType;
    private Map<String, List<ServiceProfile>> profiles;
    private final ServiceBus bus;

    public ProxyServiceCaller(final ExportedProxyManager manager,
	    final String targetId, final ModuleContext mc,
	    final String serverNamespace, final String serviceType,
	    final Map<String, List<ServiceProfile>> profiles) {
	super(manager, targetId, "", mc);
	caller = new DefaultServiceCaller(mc);
	this.serverNamespace = serverNamespace;
	this.serviceType = serviceType;
	this.profiles = profiles;
	bus = ServiceBusFacade.fetchBus(Gateway.getInstance().context);
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
	final List<ServiceProfile> profilesList = new ArrayList<ServiceProfile>();
	for (final List<ServiceProfile> value : profiles.values()) {
	    profilesList.addAll(value);
	}
	return profilesList.toArray(new ServiceProfile[0]);
    }

    public void setProfiles(final Map<String, List<ServiceProfile>> profiles) {
	this.profiles = profiles;
    }

}
