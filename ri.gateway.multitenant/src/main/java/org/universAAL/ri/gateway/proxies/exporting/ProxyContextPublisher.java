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
package org.universAAL.ri.gateway.proxies.exporting;

import java.util.Collection;

import org.universAAL.middleware.bus.permission.AccessControl;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextBus;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ReferencesManager;
import org.universAAL.ri.gateway.proxies.importing.ProxyContextSubscriber;

/**
 * A {@link ContextPublisher} which will listen to remote
 * {@link ProxyContextSubscriber}' messages and reinject them in to the local
 * bus. <br>
 *
 * All exported {@link ContextSubscriber}s may be represented locally by the
 * same {@link ProxyContextPublisher}.
 *
 * @author amedrano
 *
 */
public class ProxyContextPublisher extends ContextPublisher implements ProxyBusMember {

	private final ReferencesManager refsMngr;

	/**
	 * Create a generic (all
	 *
	 * @return
	 */
	private static ContextProvider constructProvider() {
		final ContextProvider cp = new ContextProvider();
		// cp is never used anyway...
		cp.setType(ContextProviderType.reasoner);
		cp.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
		return cp;
	}

	public ProxyContextPublisher(final ModuleContext context) {
		super(context, constructProvider());
		refsMngr = new ReferencesManager();
	}

	public String getBusMemberId() {
		return getMyID();
	}

	public void addRemoteProxyReference(final BusMemberReference remoteReference) {
		refsMngr.addRemoteProxyReference(remoteReference);
	}

	public void removeRemoteProxyReference(final BusMemberReference remoteReference) {
		refsMngr.removeRemoteProxyReference(remoteReference);

	}

	public void removeRemoteProxyReferences(final Session session) {
		refsMngr.removeRemoteProxyReferences(session);
	}

	public Collection<BusMemberReference> getRemoteProxiesReferences() {
		return refsMngr.getRemoteProxiesReferences();
	}

	public Resource[] getSubscriptionParameters() {
		return null;
	}

	public void handleMessage(final Session session, final WrappedBusMessage busMessage) {
		final ScopedResource m = busMessage.getMessage();
		if (m instanceof ContextEvent
				&& session.getIncomingMessageOperationChain().check(m).equals(OperationChain.OperationResult.ALLOW)) {
			// resolve multitenancy
			m.clearScopes();
			m.addScope(session.getScope());
			// set Origin to avoid message looping
			m.setOriginScope(session.getScope());
			// inject context Event
			if (AccessControl.INSTANCE.checkPermission(owner, getURI(), (ContextEvent) m)) {
				// XXX should gateway be checking it's own permissions?
				((ContextBus) theBus).brokerContextEvent(busResourceURI, (ContextEvent) m);
			}
		}

	}

	/**
	 * As long as the registration parameters are {@link ContextEventPattern},
	 * they will always match. This means there will be only one proxy exporter
	 * for all {@link ContextSubscriber }s in the exported pool; since
	 * registration parameters changing is ignored there is no merging problem.
	 */
	public boolean isCompatible(final Resource[] registrationParameters) {
		return registrationParameters.length > 0 && registrationParameters[0] instanceof ContextEventPattern;
	}

	public void addSubscriptionParameters(final Resource[] newParams) {
		// ignored

	}

	public void removeSubscriptionParameters(final Resource[] newParams) {
		// ignored

	}

	@Override
	public void communicationChannelBroken() {
		// XXX maybe it should close connection.

	}

}
