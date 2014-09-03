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

import java.io.Serializable;

import org.universAAL.middleware.bus.member.BusMember;
import org.universAAL.ri.gateway.Session;

/**
 * An identifier of a concrete {@link BusMember} across all Scopes.
 * 
 * @author amedrano
 * 
 */
public class BusMemberReference implements Serializable {

    /**
     * The serial Version.
     */
    private static final long serialVersionUID = -1141333844448577928L;

    /**
     * {@link Session} to use in order to reach the {@link BusMember}
     */
    private final Session sender;

    /**
     * The {@link BusMember} id at the remote side identified.
     */
    private final String busMemberid;

    /**
     * Constructor for a {@link BusMember} identifier.
     * 
     * @param session
     * @param busMemberid
     */
    public BusMemberReference(final Session session, final String busMemberid) {
	super();
	if (session == null || busMemberid == null) {
	    throw new RuntimeException("Scope or BusmemberId Must not be null");
	}
	this.sender = session;
	this.busMemberid = busMemberid;
    }

    /**
     * Get the {@link Session} to reach the {@link BusMember}.
     * 
     * @return the MessageSender.
     */
    public Session getChannel() {
	return sender;
    }

    /**
     * Get the {@link BusMember} id within its scope.
     * 
     * @return the id.
     */
    public String getBusMemberid() {
	return busMemberid;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
	return obj instanceof BusMemberReference
		&& ((BusMemberReference) obj).sender.equals(sender)
		&& ((BusMemberReference) obj).busMemberid.equals(busMemberid);
    }

}
