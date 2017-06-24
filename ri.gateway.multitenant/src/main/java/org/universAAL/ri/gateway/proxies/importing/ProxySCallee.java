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
import java.util.concurrent.TimeoutException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.MultiServiceResponse;
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
 * Represents remote a {@link ServiceCallee}, with its subscription parameters
 * synchronized, it is able to get the calls meant for the remote
 * {@link ServiceCallee} and send them to the remote proxy which, in time, makes
 * sure a response from the actual {@link ServiceCallee} is transmitted back.
 *
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
	public ProxySCallee(final ModuleContext context, final ServiceProfile[] realizedServices) {
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
		final Collection<BusMemberReference> refs = refsMngr.getRemoteProxiesReferences();
		final List<ServiceResponse> responses = new ArrayList<ServiceResponse>();
		for (final BusMemberReference bmr : refs) {
			try {
				final Session s = bmr.getChannel();
				if (!call.isSerializableTo(s.getScope())) {
					// in case the destination scope is incompatible ignore.
					continue;
				}
				if (s.getOutgoingMessageOperationChain().check(call).equals(OperationChain.OperationResult.ALLOW)) {
					// it is allowed to go there
					final ServiceCall copy = (ServiceCall) call.copy(false);
					copy.clearScopes();
					copy.setOriginScope(null);
					Message resp = null;
					try {
						resp = s.sendRequest(new WrappedBusMessage(bmr.getBusMemberid(), copy));
					} catch (TimeoutException e) {
						// TODO sure you want to report directly a timeout, and
						// not reattempt?
						final ServiceResponse sr = new ServiceResponse(CallStatus.responseTimedOut);
						// Resolve multitenancy
						sr.clearScopes();
						sr.addScope(s.getScope());
						// set the origin of the response
						sr.setOriginScope(s.getScope());

						responses.add(sr);
					}
					// sends a scope-clear call to remote proxy.
					if (resp != null && resp instanceof WrappedBusMessage) {
						final ServiceResponse sr = (ServiceResponse) ((WrappedBusMessage) resp).getMessage();
						// Resolve multitenancy
						sr.clearScopes();
						sr.addScope(s.getScope());
						// set the origin of the response
						sr.setOriginScope(s.getScope());

						responses.add(sr);
					} else if (resp instanceof ErrorMessage) {
						final ErrorMessage em = (ErrorMessage) resp;
						LogUtils.logError(owner, getClass(), "handleCall",
								"Received Error Message: " + em.getDescription());
					} else {
						LogUtils.logError(owner, getClass(), "handleCall", "unexpected Response.");
					}
				} else {
					// it is not allowed, inform caller.
					final ServiceResponse sr = new ServiceResponse(CallStatus.denied);
					sr.addScope(s.getScope());
					responses.add(sr);
				}
			} catch (final Exception e) {
				LogUtils.logError(owner, getClass(), "handleCall", new String[] { "Unexpected exception" }, e);
			}
		}
		// merge all responses
		MultiServiceResponse msr = new MultiServiceResponse(null);
		for (ServiceResponse sr : responses) {
			msr.addResponse(sr);
		}
		if (responses.size() > 0) {
			return msr;
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
	public void removeRemoteProxyReference(final BusMemberReference remoteReference) {
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

	public void handleMessage(final Session session, final WrappedBusMessage busMessage) {
		// no BusMessage sent from remote proxy.

	}

	/** {@inheritDoc} */
	public boolean isCompatible(final Resource[] registrationParameters) {
		return registrationParameters.length > 0 && registrationParameters[0] instanceof ServiceProfile
				&& new ArraySet.Equal<Resource>().equal(registrationParameters, currentRegParam);
	}

	/** {@inheritDoc} */
	public void addSubscriptionParameters(final Resource[] newParams) {
		currentRegParam = new ArraySet.Union<Resource>().combine(currentRegParam, newParams, new Resource[] {});
		addNewServiceProfiles((ServiceProfile[]) newParams);

	}

	/** {@inheritDoc} */
	public void removeSubscriptionParameters(final Resource[] newParams) {
		currentRegParam = new ArraySet.Difference<Resource>().combine(currentRegParam, newParams, new Resource[] {});
		removeMatchingProfiles((ServiceProfile[]) newParams);
	}

}
