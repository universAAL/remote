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

import java.util.Collection;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ReferencesManager;
import org.universAAL.ri.gateway.utils.ArraySet;

/**
 * Receives the events the remote {@link BusMember} is interested in, and sends
 * the {@link ContextEvent}s to its representative so it can deliver it to the
 * remote {@link BusMember}.
 * 
 * @author amedrano
 * 
 */
public class ProxyContextSubscriber extends ContextSubscriber implements
	ProxyBusMember {

    private final ReferencesManager refsMngr;

    private Resource[] currentRegParam;

    /**
     * @param connectingModule
     * @param initialSubscriptions
     */
    public ProxyContextSubscriber(final ModuleContext connectingModule,
	    final ContextEventPattern[] initialSubscriptions) {
	super(connectingModule, initialSubscriptions);
	refsMngr = new ReferencesManager();
	currentRegParam = initialSubscriptions;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public void handleMessage(final Session session,
	    final WrappedBusMessage busMessage) {
	// ignored, no message should be received!

    }

    /** {@inheritDoc} */
    public boolean isCompatible(final Resource[] registrationParameters) {
	return registrationParameters.length > 0
		&& registrationParameters[0] instanceof ContextEventPattern
		&& new ArraySet.Equal<Resource>().equal(registrationParameters,
			currentRegParam);
    }

    /** {@inheritDoc} */
    public void addSubscriptionParameters(final Resource[] newParams) {
	currentRegParam = new ArraySet.Union<Resource>().combine(
		currentRegParam, newParams, new Resource[] {});
	addNewRegParams((ContextEventPattern[]) newParams);

    }

    /** {@inheritDoc} */
    public void removeSubscriptionParameters(final Resource[] newParams) {
	currentRegParam = new ArraySet.Difference<Resource>().combine(
		currentRegParam, newParams, new Resource[] {});
	removeMatchingRegParams((ContextEventPattern[]) newParams);
    }

    /** {@inheritDoc} */
    @Override
    public void communicationChannelBroken() {
	// XXX disconnect?

    }

    /** {@inheritDoc} */
    @Override
    public void handleContextEvent(final ContextEvent event) {
	final Collection<BusMemberReference> refs = refsMngr
		.getRemoteProxiesReferences();
	for (final BusMemberReference bmr : refs) {
	    final Session s = bmr.getChannel();
	    if (!event.getScopes().contains(s.getScope())
		    && s.getOutgoingMessageOperationChain().check(event)
			    .equals(OperationChain.OperationResult.ALLOW)) {
		// the origin is not the same as the session
		// and it is allowed to go there
		final ContextEvent copy = (ContextEvent) event.deepCopy();
		copy.clearScopes();
		s.send(new WrappedBusMessage(bmr.getBusMemberid(), copy));
		// sends a scope clear event to remote proxy.
	    }
	}

    }

}
