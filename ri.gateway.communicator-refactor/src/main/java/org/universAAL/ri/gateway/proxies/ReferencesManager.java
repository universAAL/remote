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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.universAAL.ri.gateway.protocol.MessageSender;

/**
 * Utility class to help {@link ProxyBusMember}s to manage their references.
 * 
 * @author amedrano
 * 
 */
public class ReferencesManager {

    /**
     * Internal collection.
     */
    private final Map<String, BusMemberReference> references = new HashMap<String, BusMemberReference>();

    /**
     * Add a reference.
     * 
     * @param remoteReference
     */
    public synchronized void addRemoteProxyReference(
	    final BusMemberReference remoteReference) {
	references
		.put(remoteReference.getChannel().getScope(), remoteReference);
    }

    /**
     * Remove a reference.
     * 
     * @param remoteReference
     */
    public synchronized void removeRemoteProxyReference(
	    final BusMemberReference remoteReference) {
	references.remove(remoteReference);
    }

    /**
     * Remove all references related to session.
     * 
     * @param session
     */
    public synchronized void removeRemoteProxyReferences(
	    final MessageSender session) {
	final Set<BusMemberReference> refs = new HashSet<BusMemberReference>(
		references.values());
	for (final BusMemberReference bmr : refs) {
	    if (bmr.getChannel().equals(session)) {
		references.remove(bmr);
	    }
	}

    }

    /**
     * Create a copy of the internal Collection of references.
     * 
     * @return
     */
    public Collection<BusMemberReference> getRemoteProxiesReferences() {
	return new HashSet<BusMemberReference>(references.values());

    }

}
