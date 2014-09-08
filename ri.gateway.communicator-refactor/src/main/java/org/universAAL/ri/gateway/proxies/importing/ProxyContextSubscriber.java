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

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.BusMemberReference;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;

/**
 * @author amedrano
 *
 */
public class ProxyContextSubscriber extends ContextSubscriber implements
	ProxyBusMember {

    /**
     * @param connectingModule
     * @param initialSubscriptions
     */
    public ProxyContextSubscriber(ModuleContext connectingModule,
	    ContextEventPattern[] initialSubscriptions) {
	super(connectingModule, initialSubscriptions);
	// TODO Auto-generated constructor stub
    }

    /**{@inheritDoc} */
    public String getBusMemberId() {
	// TODO Auto-generated method stub
	return null;
    }

    /**{@inheritDoc} */
    public void addRemoteProxyReference(BusMemberReference remoteReference) {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    public void removeRemoteProxyReference(BusMemberReference remoteReference) {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    public void removeRemoteProxyReferences(Session session) {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    public Collection<BusMemberReference> getRemoteProxiesReferences() {
	// TODO Auto-generated method stub
	return null;
    }

    /**{@inheritDoc} */
    public Resource[] getSubscriptionParameters() {
	// TODO Auto-generated method stub
	return null;
    }

    /**{@inheritDoc} */
    public void handleMessage(Session session, WrappedBusMessage busMessage) {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    public boolean isCompatible(Resource[] registrationParameters) {
	// TODO Auto-generated method stub
	return false;
    }

    /**{@inheritDoc} */
    public void addSubscriptionParameters(Resource[] newParams) {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    public void removeSubscriptionParameters(Resource[] newParams) {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    @Override
    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    /**{@inheritDoc} */
    @Override
    public void handleContextEvent(ContextEvent event) {
	// TODO Auto-generated method stub

    }

}
