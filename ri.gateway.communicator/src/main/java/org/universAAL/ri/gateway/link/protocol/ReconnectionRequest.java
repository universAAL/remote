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
package org.universAAL.ri.gateway.link.protocol;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class ReconnectionRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7160538663305545199L;
    private String peerId;
    private String aalSpaceId;
    private UUID sessionId;

    public ReconnectionRequest(final String peer, final String space, final UUID session) {
        super();
        this.peerId = peer;
        this.aalSpaceId = space;
        this.sessionId = session;
    }

    @Override
    public String toString() {
        return "ReconnectionRequest ["
                + (peerId != null ? "peerId =" + peerId  + ", " : "")
                + (aalSpaceId != null ? "aalSpaceId =" + aalSpaceId  + ", " : "")
                + (aalSpaceId != null ? "scopeId=" + aalSpaceId + ", " : "")
                + (sessionId != null ? "sessionId=" + sessionId + " " : "")
                + "]";
    }

    public String getAALSpaceId() {
        return aalSpaceId;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getScopeId() {
        return aalSpaceId;
    }

    public UUID getSessionId() {
        return sessionId;
    }
}
