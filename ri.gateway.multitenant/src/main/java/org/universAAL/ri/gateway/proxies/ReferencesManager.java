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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.universAAL.ri.gateway.Session;

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
    private final Map<String, Set<BusMemberReference>> references = new HashMap<String, Set<BusMemberReference>>();

    /**
     * Add a reference.
     * 
     * @param remoteReference
     */
    public synchronized void addRemoteProxyReference(
	    final BusMemberReference remoteReference) {
	put(remoteReference.getChannel().getScope(), remoteReference);
    }

    private void put(final String scope,
	    final BusMemberReference remoteReference) {
	Set<BusMemberReference> existing = references.get(scope);
	if (existing == null) {
	    existing = new HashSet<BusMemberReference>();
	}
	existing.add(remoteReference);
	references.put(scope, existing);

    }

    /**
     * Remove a reference.
     * 
     * @param remoteReference
     */
    public synchronized void removeRemoteProxyReference(
	    final BusMemberReference remoteReference) {
	final Set<BusMemberReference> existing = references.get(remoteReference
		.getChannel().getScope());
	if (existing != null) {
	    existing.remove(remoteReference);
	}
    }

    /**
     * Remove all references related to session.
     * 
     * @param session
     */
    public synchronized void removeRemoteProxyReferences(final Session session) {
	references.remove(session.getScope());
    }

    /**
     * Create a copy of the internal Collection of references.
     * 
     * @return
     */
    public Collection<BusMemberReference> getRemoteProxiesReferences() {
	final Set<BusMemberReference> all = new HashSet<BusMemberReference>();
	for (final Set<BusMemberReference> bmrs : references.values()) {
	    all.addAll(bmrs);
	}
	return all;
    }

    public Collection<BusMemberReference> getReferencesFor(final String scope) {
	return references.get(scope);
    }

    public Collection<BusMemberReference> getReferencesFor(
	    final List<String> scopes) {
	final Set<BusMemberReference> all = new HashSet<BusMemberReference>();
	for (final String s : scopes) {
	    final Set<BusMemberReference> srefs = references.get(s);
	    if (srefs != null) {
		all.addAll(srefs);
	    }
	}
	return all;
    }

}
