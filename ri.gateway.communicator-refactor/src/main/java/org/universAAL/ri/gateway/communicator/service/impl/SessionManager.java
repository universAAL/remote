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

package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.universAAL.middleware.managers.api.TenantManager;
import org.universAAL.ri.gateway.Gateway;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-08-04 14:31:42
 *          +0200 (lun, 04 ago 2014) $)
 *
 */
public class SessionManager {

    private static SessionManager manager = null;
    private static final Gateway gw = Gateway.getInstance();

    private class SessionKey {

        final static int PEER_IDX = 0;
        final static int SPACE_IDX = 1;
        final static int SCOPE_IDX = 2;

        final String[] keyParts = new String[3];
        final String key;
        public String description = null;

        public SessionKey(final String peer, final String space,
                final String scope) {
            keyParts[SessionKey.PEER_IDX] = peer;
            keyParts[SessionKey.SPACE_IDX] = space;
            keyParts[SessionKey.SCOPE_IDX] = scope;
            key = Arrays.toString(keyParts);
        }

        @Override
        public String toString() {
            return key;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof SessionKey) {
                final SessionKey other = (SessionKey) obj;
                return this.toString().equals(other.toString());
            }
            return false;
        }

    }

    private class SessionStatus {
        boolean connected;
        InputStream in;
        OutputStream out;
    }

    private final Map<SessionKey, UUID> sessions = new HashMap<SessionManager.SessionKey, UUID>();
    private final Map<UUID, SessionKey> uuids = new HashMap<UUID, SessionKey>();
    private final Map<UUID, SessionStatus> links = new HashMap<UUID, SessionManager.SessionStatus>();
    private TenantManager currentTM = null;

    private SessionManager() {

    }

    public static SessionManager getInstance() {
        synchronized (SessionManager.class) {
            if (SessionManager.manager == null) {
                SessionManager.manager = new SessionManager();
            }
        }
        return SessionManager.manager;
    }

    public UUID getSession(final String peerId, final String aalSpaceId,
            final String scopeId) {
        synchronized (sessions) {
            return sessions.get(new SessionKey(peerId, aalSpaceId, scopeId));
        }
    }

    private UUID createSession(final String peerId, final String aalSpaceId,
            final String scopeId) {
        return createSession(peerId, aalSpaceId, scopeId, "AAL Space with Id:"
                + aalSpaceId);
    }

    public UUID createSession(final String peerId, final String aalSpaceId,
            final String scopeId, final String description) {
        if (description == null) {
            createSession(peerId, aalSpaceId, scopeId);
        }
        final SessionKey key = new SessionKey(peerId, aalSpaceId, scopeId);

        final UUID uuid = UUID.randomUUID();
        synchronized (sessions) {
            if (sessions.containsKey(key)) {
                throw new IllegalStateException("Session " + key
                        + " already exists with UUID = " + sessions.get(key));
            }
            sessions.put(key, uuid);
            uuids.put(uuid, key);
        }
        key.description = description;


        if (currentTM == gw.tenantManager.getObject()
                && currentTM != null) {
            currentTM.registerTenant(scopeId, description);
        } else if (gw.tenantManager.getObject() != null) {
            currentTM = gw.tenantManager.getObject();
            changedTenantManager();
        }


        return uuid;
    }

    private void changedTenantManager() {
        final Collection<SessionKey> activeSessions = uuids.values();
        for (final Iterator<SessionKey> i = activeSessions.iterator(); i
                .hasNext();) {
            final SessionKey key = i.next();
            final String scopeId = key.keyParts[SessionKey.SCOPE_IDX];
            //currentTM.registerTenant(scopeId, key.description);
        }
    }

    public void setLink(final UUID session, final InputStream in,
            final OutputStream out) {
        synchronized (sessions) {
            if (sessions.containsValue(session) == false) {
                throw new IllegalArgumentException(
                        "Trying to set a Link for an invalid session");
            }
            synchronized (links) {
                SessionStatus status = links.get(session);
                if (status == null) {
                    status = new SessionStatus();
                    links.put(session, status);
                }
                synchronized (status) {
                    status.connected = true;
                    status.in = in;
                    status.out = out;
                }
            }
        }
    }

    public void close(final UUID session) {
        UUID removed;
        SessionKey keyRemoved;
        synchronized (sessions) {
            if (sessions.containsValue(session) == false) {
                throw new IllegalArgumentException(
                        "Trying to set a Link for an invalid session UUID");
            }
            synchronized (links) {
                final SessionStatus status = links.get(session);
                if (status == null) {
                    throw new IllegalArgumentException(
                            "Trying to close Link for an invalid session UUID");
                }
                links.remove(session);
                synchronized (status) {
                    status.connected = false;
                    try {
                        status.in.close();
                        status.out.flush();
                        status.out.close();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            keyRemoved = uuids.remove(session);
            removed = sessions.remove(keyRemoved);
            if (removed == null) {
                throw new IllegalStateException(
                        "Broken session manger backend, there is unhandled race codintion on data structure or codebase is broken");
            }
        }


        if (currentTM == gw.tenantManager.getObject()
                && currentTM != null) {
            currentTM
                    .unregisterTenant(keyRemoved.keyParts[SessionKey.SCOPE_IDX]);
        } else if (gw.tenantManager.getObject() != null) {
            currentTM = gw.tenantManager.getObject();
            changedTenantManager();
        }

    }

    public OutputStream getOutputStream(final UUID session) {
        SessionStatus info = null;
        synchronized (links) {
            info = links.get(session);
        }
        if (info == null || info.connected == false) {
            // TODO Log the issue
            return null;
        }
        return info.out;
    }

    public InputStream getInputStream(final UUID session) {
        SessionStatus info = null;
        synchronized (links) {
            info = links.get(session);
        }
        if (info == null || info.connected == false) {
            // TODO Log the issue
            return null;
        }
        return info.in;
    }

    public boolean isActive(final UUID session) {
        SessionStatus info = null;
        synchronized (links) {
            info = links.get(session);
        }
        if (info == null || info.connected == false) {
            return false;
        }
        return info.connected;
    }

    public UUID[] getSessionIds() {
        synchronized (sessions) {
            return sessions.values().toArray(new UUID[] {});
        }
    }

    public void storeSession(final UUID sessionId, final String peerId,
            final String aalSpaceId, final String scopeId) {
        final SessionKey key = new SessionKey(peerId, aalSpaceId, scopeId);
        synchronized (sessions) {
            if (sessions.containsKey(key)) {
                throw new IllegalStateException("Session " + key
                        + " already exists with UUID = " + sessions.get(key));
            }
            sessions.put(key, sessionId);
            uuids.put(sessionId, key);
        }
    }

    public String getPeerIdFromSession(final UUID session) {
        synchronized (sessions) {
            if (uuids.containsKey(session)) {
                throw new IllegalStateException("No session with UUID "
                        + session);
            }
            return uuids.get(session).keyParts[SessionKey.PEER_IDX];
        }
    }

    public String getAALSpaceIdFromSession(final UUID session) {
        SessionKey key = uuids.get(session);
        synchronized (sessions) {
            if ( key == null ) {
                throw new IllegalStateException("No session with UUID "
                        + session);
            }
            return key.keyParts[SessionKey.SPACE_IDX];
        }
    }

}
