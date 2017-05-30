/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    See the NOTICE file distributed with this work for additional
    information regarding copyright ownership

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.universAAL.ri.gateway.protocol.link;

import java.io.Serializable;

import org.universAAL.ri.gateway.protocol.LinkMessage;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-08-08 11:09:43 +0200
 *          (Fri, 08 Aug 2014) $)
 *
 */
public class ConnectionRequest extends LinkMessage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5607749107488373257L;
    /**
     *
     */
    private final String peerId;
    private final String aalSpaceId;
    private final String description;

    /**
     *
     * @param peer
     *            The Id of the peer that runs the Gateway
     * @param space
     *            The Id of the AAL Space where the peer is connected to
     * @param description
     *            A friendly name description of the connection
     */
    public ConnectionRequest(final String peer, final String space,
	    final String description) {
	super();
	this.peerId = peer;
	this.aalSpaceId = space;
	this.description = description;
    }

    @Override
    public String toString() {
	return "ConnectionRequest ["
		+ (peerId != null ? "peerId =" + peerId + ", " : "")
		+ (aalSpaceId != null ? "scopeId =" + aalSpaceId + ", " : "")
		+ (aalSpaceId != null ? "scopeId=" + aalSpaceId + ", " : "")
		+ (description != null ? "description=" + description + " "
			: "") + "]";
    }

    public String getAALSpaceId() {
	return aalSpaceId;
    }

    public String getPeerId() {
	return peerId;
    }

    /**
     *
     * This method return the ScopeId<br>
     * <br>
     * <b>NOTE:</b>From version 3.2.0 to now the ScopeId has ALWAYS the same
     * value of the SpaceId, but it may differ in the future
     *
     * @return the scope of the session.
     *
     */
    public String getScopeId() {
	return aalSpaceId;
    }

    public String getDescription() {
	return description;
    }

    @Override
    public int getType() {
	return LinkMessageType.CONNECTION_REQUEST.ordinal();
    }

}
