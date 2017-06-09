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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.protocol.ImportMessage;

/**
 * A place holder for all {@link ProxyBusMember}s.
 * 
 * @author amedrano
 * 
 */
public class ProxyPool {

	/**
	 * Collection ProxyID to {@link ProxyBusMember}.
	 */
	Map<String, ProxyBusMember> map = new HashMap<String, ProxyBusMember>();

	/**
	 * Get the {@link ProxyBusMember} from its busMemberId (which is not always
	 * the same as the {@link BusMember} it represents).
	 * 
	 * @param proxyID
	 * @return
	 */
	public ProxyBusMember get(final String proxyID) {
		return map.get(proxyID);
	}

	/**
	 * Adds {@link ProxyBusMember} to the pool. if {@link ProxyBusMember} is
	 * already added, there is no effect.
	 * 
	 * @param proxy
	 */
	public synchronized void add(final ProxyBusMember proxy) {
		map.put(proxy.getBusMemberId(), proxy);
	}

	/**
	 * Test compatibility of parameters with all {@link ProxyBusMember}s in the
	 * pool.
	 * 
	 * @param newParameters
	 *            the registration parameters required for the compatible
	 *            {@link ProxyBusMember}
	 * @return the first compatible {@link ProxyBusMember} or null if not found.
	 */
	public ProxyBusMember searchCompatible(final Resource[] newParameters) {
		ProxyBusMember match = null;
		final Iterator<ProxyBusMember> it = map.values().iterator();
		while (it.hasNext() && match == null) {
			final ProxyBusMember pbm = it.next();
			if (pbm.isCompatible(newParameters)) {
				match = pbm;
			}
		}
		return match;
	}

	/**
	 * Get all {@link ProxyBusMember}s in the pool.
	 * 
	 * @return the collection of all currently present {@link ProxyBusMember}s
	 *         in the pool
	 */
	public Collection<ProxyBusMember> all() {
		return map.values();
	}

	/**
	 * Safely remove a {@link ProxyBusMember} from the bus. and send a message
	 * to all it's peers to remove the remote proxy. this method will also
	 * {@link ProxyBusMember#close() terminate} the {@link ProxyBusMember}.
	 * 
	 * @param pbm
	 */
	public void removeProxyWithSend(final ProxyBusMember pbm) {
		final Collection<BusMemberReference> allSessionsAssociatedToProxy = pbm.getRemoteProxiesReferences();
		// send importRemove to all sessions
		for (final BusMemberReference bmr : allSessionsAssociatedToProxy) {
			Session s = bmr.getChannel();
			if (s.isActive()) {
				// if session is not active it has been already removed in peer
				bmr.getChannel().send(ImportMessage.importRemove(pbm.getBusMemberId()));
			}
		}
		removeProxy(pbm);
	}

	/**
	 * Check if a Proxy is orphan (is it has no references) if so removes it and
	 * properly closes it.
	 * 
	 * @param pbm
	 *            the Proxy to check.
	 * @return true iff the proxy is orphan => removed.
	 */
	public boolean removeProxyIfOrphan(final ProxyBusMember pbm) {
		if (pbm.getRemoteProxiesReferences().isEmpty()) {
			LogUtils.logDebug(Gateway.getInstance().context, getClass(), "removeProxyIfOrphan",
					"No more References, removing proxy.");
			removeProxy(pbm);
			return true;
		}
		return false;
	}

	private synchronized void removeProxy(final ProxyBusMember pbm) {
		pbm.close();
		map.remove(pbm.getBusMemberId());
	}
}
