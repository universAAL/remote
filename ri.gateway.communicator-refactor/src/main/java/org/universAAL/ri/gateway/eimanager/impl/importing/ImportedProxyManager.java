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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIHandlerProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.impl.Serializer;
import org.universAAL.ri.gateway.eimanager.impl.AbstractProxyManager;
import org.universAAL.ri.gateway.eimanager.impl.ProxyBusMember;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;

public class ImportedProxyManager extends AbstractProxyManager {

    private final Map<String, List<ProxyBusMember>> generatedProxies;
    private final Map<String, List<ServiceProfile>> remoteBusMembersImportedProfiles;
    private final GatewayCommunicator communicator;

    public ImportedProxyManager(final GatewayCommunicator communicator) {
	super();
	this.generatedProxies = new HashMap<String, List<ProxyBusMember>>();
	this.remoteBusMembersImportedProfiles = new HashMap<String, List<ServiceProfile>>();
	this.communicator = communicator;
    }

    public void registerProxies(final InternalImportOperation op) {
	ProxyBusMember proxy = null;
	if (!generatedProxies.containsKey(op.getRemoteRegisteredProxyId())) {
	    generatedProxies.put(op.getRemoteRegisteredProxyId(),
		    new ArrayList<ProxyBusMember>());
	}
	switch (op.getType()) {
	case ServiceCaller:
	    final Map<String, List<ServiceProfile>> profilesMap = op
		    .getRealizedServices();
	    for (final String key : profilesMap.keySet()) {
		for (final ServiceProfile p : profilesMap.get(key)) {
		    if (remoteBusMembersImportedProfiles.containsKey(key)) {
			if (remoteBusMembersImportedProfiles.get(key).contains(
				p)) {
			    continue;
			}
		    } else {
			remoteBusMembersImportedProfiles.put(key,
				new ArrayList<ServiceProfile>());
		    }

		    proxy = new ProxyServiceCallee(new ServiceProfile[] { p },
			    this, op.getRemoteRegisteredProxyId(), key,
			    Gateway.getInstance().context);

		    generatedProxies.get(op.getRemoteRegisteredProxyId()).add(
			    proxy);
		    remoteBusMembersImportedProfiles.get(key).add(p);
		}
	    }

	    break;
	case ContextSubscriber:
	    proxy = new ProxyContextPublisher(
		    (ContextSubscriber) op.getBusMember(),
		    op.getContextProvider(), this,
		    op.getRemoteRegisteredProxyId(),
		    Gateway.getInstance().context);
	    generatedProxies.get(op.getRemoteRegisteredProxyId()).add(proxy);
	    break;
	case UICaller:
	    final UIHandlerProfile[] profiles = op.getUiHandlerProfiles();
	    proxy = new ProxyUIHandler(profiles, this,
		    op.getRemoteRegisteredProxyId(),
		    Gateway.getInstance().context);
	    generatedProxies.get(op.getRemoteRegisteredProxyId()).add(proxy);
	    break;
	}

    }

    public void unregisterProxies(final InternalImportOperation op) {
	final String id = op.getRemoteRegisteredProxyId();
	if (generatedProxies.containsKey(id)) {
	    for (final ProxyBusMember pr : generatedProxies.get(id)) {
		pr.removeProxy();
		if (pr instanceof ProxyServiceCallee) {
		    if (remoteBusMembersImportedProfiles.containsKey(pr
			    .getRemoteBusMemberId())) {
			for (final ServiceProfile p : ((ProxyServiceCallee) pr)
				.getProfilesSet()) {
			    remoteBusMembersImportedProfiles.get(
				    pr.getRemoteBusMemberId()).remove(p);
			}
		    }
		}
	    }
	    generatedProxies.remove(id);
	}
    }

    public void realizeLocalContextEventPublishment(final String sourceId,
	    final ContextEvent event) {
	final List<ProxyBusMember> members = generatedProxies.get(sourceId);
	for (final ProxyBusMember member : members) {
	    ((ProxyContextPublisher) member).publishContextEvent(event);
	}
    }

    public ServiceResponse realizeRemoteServiceRequest(final String targetId,
	    final ServiceCall request, final String remoteBusMemberId)
	    throws IOException, ClassNotFoundException {
	final Message toSend = Serializer.Instance.marshallObject(request);
	toSend.setRemoteProxyRegistrationId(targetId);
	toSend.setRemoteMemberId(remoteBusMemberId);

	final Message[] returnMessage = communicator.sendServiceRequest(toSend);
	// TODO reimplement mechanisms for choosing proper response from
	// multiple spaces

	final ServiceResponse response = Serializer.Instance.unmarshallObject(
		ServiceResponse.class, returnMessage[0]);
	System.out.println("Received response: " + response);
	return response;
    }

    public void realizeLocalUIResponsePublishment(final String sourceId,
	    final UIResponse response) {
	// TODO implement
	// ((ProxyUIHandler)generatedProxies.get(sourceId)).
    }

    public void realizeRemoteUIRequest(final String targetId,
	    final UIRequest uiRequest) throws IOException {
	final Message toSend = Serializer.Instance.marshallObject(uiRequest);
	toSend.setRemoteProxyRegistrationId(targetId);
	communicator.sendUIRequest(toSend);
    }

    public void refreshProxy(final ProxyRegistration proxyRegistration)
	    throws IOException, ClassNotFoundException {

	ProxyBusMember proxy = null;
	final List<ProxyBusMember> members = generatedProxies
		.get(proxyRegistration.getId());
	if (members != null) {
	    for (final ProxyBusMember m : members) {
		m.removeProxy();
	    }
	}

	generatedProxies.remove(proxyRegistration.getId());

	final Map<String, List<String>> serializedProfilesMap = (Map<String, List<String>>) proxyRegistration
		.getReturnedValues();

	final Map<String, List<ServiceProfile>> profilesMap = new HashMap<String, List<ServiceProfile>>();

	for (final String key : serializedProfilesMap.keySet()) {
	    if (profilesMap.get(key) == null) {
		profilesMap.put(key, new ArrayList<ServiceProfile>());
	    }
	    for (final String serializedP : serializedProfilesMap.get(key)) {
		profilesMap.get(key).add(
			Serializer.Instance.unmarshallObject(
				ServiceProfile.class, serializedP,
				Gateway.class.getClassLoader()));
	    }
	}

	for (final String key : profilesMap.keySet()) {
	    for (final ServiceProfile p : profilesMap.get(key)) {
		if (remoteBusMembersImportedProfiles.containsKey(key)) {
		    if (remoteBusMembersImportedProfiles.get(key).contains(p)) {
			continue;
		    }
		}

		proxy = new ProxyServiceCallee(new ServiceProfile[] { p },
			this, proxyRegistration.getId(), key,
			Gateway.getInstance().context);
		if (generatedProxies.get(proxyRegistration.getId()) == null) {
		    generatedProxies.put(proxyRegistration.getId(),
			    new ArrayList<ProxyBusMember>());
		}
		generatedProxies.get(proxyRegistration.getId()).add(proxy);
	    }
	}

    }
}
