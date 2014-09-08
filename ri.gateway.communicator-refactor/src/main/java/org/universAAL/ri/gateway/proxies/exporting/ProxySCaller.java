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

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;

/**
 * @author amedrano
 * 
 */
public class ProxySCaller extends ServiceCaller implements ProxyBusMember {

    /**
     * @param context
     */
    public ProxySCaller(final ModuleContext context) {
	super(context);
	// TODO Auto-generated constructor stub
    }

    /** {@inheritDoc} */
    @Override
    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    @Override
    public void handleResponse(final String reqID,
	    final ServiceResponse response) {
	// TODO Auto-generated method stub

    }

    public String getBusMemberId() {
	// TODO Auto-generated method stub
	return null;
    }

    public void addRemoteProxyReference(final BusMemberReference remoteReference) {
	// TODO Auto-generated method stub

    }

    public void removeRemoteProxyReference(
	    final BusMemberReference remoteReference) {
	// TODO Auto-generated method stub

    }

    public void removeRemoteProxyReferences(final Session session) {
	// TODO Auto-generated method stub

    }

    public Collection<BusMemberReference> getRemoteProxiesReferences() {
	// TODO Auto-generated method stub
	return null;
    }

    public Resource[] getSubscriptionParameters() {
	// TODO Auto-generated method stub
	return null;
    }

    public void handleMessage(final Session session,
	    final WrappedBusMessage busMessage) {
	// TODO Auto-generated method stub

    }

    public boolean isCompatible(final Resource[] registrationParameters) {
	// TODO Auto-generated method stub
	return false;
    }

    public void addSubscriptionParameters(final Resource[] newParams) {
	// TODO Auto-generated method stub

    }

    public void removeSubscriptionParameters(final Resource[] newParams) {
	// TODO Auto-generated method stub

    }

}
