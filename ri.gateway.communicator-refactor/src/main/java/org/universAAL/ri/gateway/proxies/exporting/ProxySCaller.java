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

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.operations.OperationChain;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ReferencesManager;
import org.universAAL.ri.gateway.proxies.importing.ProxySCallee;
import org.universAAL.ri.gateway.utils.ArraySet;

/**
 * Proxy that receives {@link ServiceCall}s and injects them to the bus in other
 * to get a {@link ServiceResponse} from the represented {@link ServiceCallee}. <br>
 * 
 * An instance of this proxy is used per each exported {@link ServiceCallee}. <br>
 * 
 * Important: this class is not a {@link BusMember}, like the other proxies;
 * thus it does not register to buses.
 * 
 * @author amedrano
 * 
 */
public class ProxySCaller extends ServiceCaller implements ProxyBusMember {

    private ReferencesManager refsMngr;

    private Resource[] currentRegParam;

    private String localSCeeId;

    /**
     * Constructor.
     * 
     * @param context
     * @param profiles
     * @param proxiedBusMember
     */
    public ProxySCaller(final ModuleContext context,
	    final ServiceProfile[] profiles, final String proxiedBusMember) {
	super(context);
	refsMngr = new ReferencesManager();
	currentRegParam = profiles;
	localSCeeId = proxiedBusMember;
    }

    /** {@inheritDoc} */
    public String getBusMemberId() {
	return localSCeeId;
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
	final ScopedResource m = busMessage.getMessage();
	if (m instanceof ServiceCall
		&& session.getIncomingMessageOperationChain().check(m)
			.equals(OperationChain.OperationResult.ALLOW)) {
	    // resolve multitenancy
	    m.clearScopes();
	    // Origin Scope.
	    m.setProperty(ScopedResource.PROP_ORIG_SCOPE, session.getScope());
	    // invoke service
	    ServiceResponse sr = inject((ServiceCall) m, localSCeeId);
	    if (sr.isSerializableTo(session.getScope())
		    && session.getOutgoingMessageOperationChain().check(sr)
			    .equals(OperationChain.OperationResult.ALLOW)) {
		// security check for the response
		sr.clearScopes();
		sr.setProperty(ScopedResource.PROP_ORIG_SCOPE, null);
		session.send(new WrappedBusMessage(busMessage, sr));
	    } else {
		sr = new ServiceResponse(CallStatus.denied);
		sr.setResourceComment("Denied by Outgoing message policy in remote.");
		session.send(new WrappedBusMessage(busMessage, sr));
	    }
	} else if (m instanceof ServiceCall) {
	    final ServiceResponse sr = new ServiceResponse(CallStatus.denied);
	    sr.setResourceComment("Denied by Incomming message policy in remote.");
	    session.send(new WrappedBusMessage(busMessage, sr));
	}

    }

    /**
     * a ProxyScaller is created per Exported {@link ServiceCallee}. Thus
     * everything must be the same to be compatible. <br>
     * In future a single ProxySCaller may be able to manage all
     * {@link ProxySCallee}s.
     */
    public boolean isCompatible(final Resource[] registrationParameters) {
	return registrationParameters.length > 0
		&& registrationParameters[0] instanceof ServiceProfile
		&& new ArraySet.Equal<Resource>().equal(registrationParameters,
			currentRegParam);
    }

    /** {@inheritDoc} */
    public void addSubscriptionParameters(final Resource[] newParams) {
	currentRegParam = new ArraySet.Union<Resource>().combine(
		currentRegParam, newParams, new Resource[] {});
    }

    /** {@inheritDoc} */
    public void removeSubscriptionParameters(final Resource[] newParams) {
	currentRegParam = new ArraySet.Difference<Resource>().combine(
		currentRegParam, newParams, new Resource[] {});
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
	refsMngr = null;
	localSCeeId = null;
	currentRegParam = null;
    }

    @Override
    public void communicationChannelBroken() {
	// XXX disconnect?
    }

    @Override
    public void handleResponse(final String reqID,
	    final ServiceResponse response) {
	// no response should be received, call is injected.

    }

}
