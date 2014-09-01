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
package org.universAAL.ri.gateway.proxies;

import java.util.Collection;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.protocol.ImportMessage;

/**
 * @author amedrano
 * 
 */
public class ProxyPool {

    /**
     * Get the {@link ProxyBusMember} from its busMemberId (which is not the
     * same as the {@link BusMember} it represents).
     * 
     * @param proxyID
     * @return
     */
    public ProxyBusMember get(final String proxyID) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * Adds {@link ProxyBusMember} to the pool. if {@link ProxyBusMember} is
     * already added, there is no effect.
     * 
     * @param proxy
     */
    public void add(final ProxyBusMember proxy) {
	// TODO Auto-generated method stub
    }

    public ProxyBusMember searchCompatible(final Resource[] newParameters) {
	// TODO Auto-generated method stub
	return null;
    }

    public Collection<ProxyBusMember> all() {
	// TODO Auto-generated method stub
	return null;
    }

    public void removeProxyWithSend(final ProxyBusMember pbm) {
	final Collection<BusMemberIdentifier> allSessionsAssociatedToProxy = pbm
		.getRemoteProxiesReferences();
	// send importRemove to all sessions
	for (final BusMemberIdentifier bmID : allSessionsAssociatedToProxy) {
	    bmID.getChannel().Send(
		    ImportMessage.importRemove(pbm.getBusMemberId()));
	}
	removeProxy(pbm);
    }

    private void removeProxy(final ProxyBusMember pbm) {
	pbm.close();
	// TODO remove from collection.

    }

    /**
     * To be called when a specific session is to be closed. <br>
     * Removes all associations in all proxies with the session, and closes
     * those left orphan.
     * 
     * @param session
     */
    public void sessionEnding(final Session session) {
	final Collection<ProxyBusMember> proxies = all();
	for (final ProxyBusMember p : proxies) {
	    p.removeRemoteProxyReferences(session);
	    if (p.getRemoteProxiesReferences().isEmpty()) {
		p.close();
	    }
	}
    }
}
