/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.proxies.importing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ReferencesManager;
import org.universAAL.ri.gateway.utils.ArraySet;

/**
 * @author amedrano
 * 
 */
public class ProxySCallee extends ServiceCallee implements ProxyBusMember {

    private final ReferencesManager refsMngr;

    private Resource[] currentRegParam;

    /**
     * @param context
     * @param realizedServices
     */
    public ProxySCallee(final ModuleContext context,
	    final ServiceProfile[] realizedServices) {
	super(context, realizedServices);
	refsMngr = new ReferencesManager();
    }

    /** {@inheritDoc} */
    @Override
    public void communicationChannelBroken() {
	// XXX disconnect?

    }

    /** {@inheritDoc} */
    @Override
    public ServiceResponse handleCall(final ServiceCall call) {
	final Collection<BusMemberReference> refs = refsMngr
		.getRemoteProxiesReferences();
	final List<ServiceResponse> responses = new ArrayList<ServiceResponse>();
	for (final BusMemberReference bmr : refs) {
	    try {
		final Session s = bmr.getChannel();
		if (call.getScopes().contains(s.getScope())) {
		    // in case the scope is the same as the call ignore.
		    continue;
		}
		if (s.getOutgoingMessageOperationChain().check(call)
			.equals(OperationChain.OperationResult.ALLOW)) {
		    // it is allowed to go there
		    final ContextEvent copy = (ContextEvent) call.deepCopy();
		    copy.clearScopes();
		    final Message resp = s.sendRequest(new WrappedBusMessage(
			    bmr.getBusMemberid(), copy));
		    // sends a scope clear call to remote proxy.
		    if (resp != null && resp instanceof WrappedBusMessage) {
			final ServiceResponse sr = (ServiceResponse) ((WrappedBusMessage) resp)
				.getMessage();
			sr.clearScopes();
			sr.addScope(s.getScope());

			responses.add(sr);
		    } else if (resp instanceof ErrorMessage) {
			final ErrorMessage em = (ErrorMessage) resp;
			LogUtils.logError(
				owner,
				getClass(),
				"handleCall",
				"Received Error Message: "
					+ em.getDescription());
		    } else {
			LogUtils.logError(owner, getClass(), "handleCall",
				"unexpected Response.");
		    }
		} else {
		    // it is not allowed, inform caller.
		    final ServiceResponse sr = new ServiceResponse(
			    CallStatus.denied);
		    sr.addScope(s.getScope());
		    responses.add(sr);
		}
	    } catch (final Exception e) {
		LogUtils.logError(owner, getClass(), "handleCall",
			new String[] { "Unexpected exception" }, e);
	    }
	}
	// TODO merge all responses
	if (responses.size() > 0) {
	    return responses.get(0);
	}
	final ServiceResponse sr = new ServiceResponse(CallStatus.denied);
	sr.setResourceComment("Unable to get any response from remote Proxies.");
	return sr;
    }

    public String getBusMemberId() {
	return busResourceURI;
    }

    /** {@inheritDoc} */
    public void addRemoteProxyReference(final BusMemberReference remoteReference) {
	refsMngr.addRemoteProxyReference(remoteReference);
    }

    /** {@inheritDoc} */
    public void removeRemoteProxyReference(
	    final BusMemberReference remoteReference) {
	refsMngr.removeRemoteProxyReference(remoteReference);
    }

    /** {@inheritDoc} */
    public void removeRemoteProxyReferences(final Session session) {
	refsMngr.removeRemoteProxyReferences(session);
    }

    /** {@inheritDoc} */
    public Collection<BusMemberReference> getRemoteProxiesReferences() {
	return refsMngr.getRemoteProxiesReferences();
    }

    /** {@inheritDoc} */
    public Resource[] getSubscriptionParameters() {
	return currentRegParam;
    }

    public void handleMessage(final Session session,
	    final WrappedBusMessage busMessage) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public boolean isCompatible(final Resource[] registrationParameters) {
	return registrationParameters.length > 0
		&& registrationParameters[0] instanceof ServiceProfile
		&& new ArraySet.Equal<Resource>().equal(registrationParameters,
			currentRegParam);
    }

    /** {@inheritDoc} */
    public void addSubscriptionParameters(final Resource[] newParams) {
	currentRegParam = new ArraySet.Union<Resource>().combine(
		currentRegParam, newParams);
	addNewServiceProfiles((ServiceProfile[]) newParams);

    }

    /** {@inheritDoc} */
    public void removeSubscriptionParameters(final Resource[] newParams) {
	currentRegParam = new ArraySet.Difference<Resource>().combine(
		currentRegParam, newParams);
	removeMatchingProfiles((ServiceProfile[]) newParams);
    }

}
