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
package org.universAAL.ri.gateway;

import java.io.Serializable;

import org.universAAL.middleware.bus.member.BusMember;

/**
 * An identifier of a concrete {@link BusMember} across all Scopes.
 * 
 * @author amedrano
 * 
 */
public class BusMemberIdentifier implements Serializable {

    /**
     * The serial Version.
     */
    private static final long serialVersionUID = -1141333844448577928L;

    /**
     * The scope (or tenantID) of the {@link BusMember}
     */
    private final String scope;

    /**
     * The {@link BusMember} id whithin the scope.
     */
    private final String busMemberid;

    /**
     * Constructor for a {@link BusMember} identifier.
     * 
     * @param scope
     * @param busMemberid
     */
    public BusMemberIdentifier(final String scope, final String busMemberid) {
	super();
	if (scope == null || busMemberid == null) {
	    throw new RuntimeException("Scope or BusmemberId Must not be null");
	}
	this.scope = scope;
	this.busMemberid = busMemberid;
    }

    /**
     * Constructor for a {@link BusMember} identifier, given a local
     * {@link BusMember}
     * 
     * @param scope
     * @param busMemberid
     */
    public BusMemberIdentifier(final BusMember bm) {
	super();
	// TODO find local scope
	this.scope = "";
	this.busMemberid = bm.getURI();
    }

    /**
     * Get the scope of the BusMember.
     * 
     * @return the scope.
     */
    public String getScope() {
	return scope;
    }

    /**
     * Get the {@link BusMember} id within its scope.
     * 
     * @return the id.
     */
    public String getBusMemberid() {
	return busMemberid;
    }

}
