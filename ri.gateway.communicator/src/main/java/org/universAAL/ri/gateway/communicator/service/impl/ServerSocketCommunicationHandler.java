/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
    Faculty of Computer Science, Electronics and Telecommunications
    Department of Computer Science

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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.ComunicationEventListener;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.link.protocol.ConnectionRequest;
import org.universAAL.ri.gateway.link.protocol.ConnectionResponse;
import org.universAAL.ri.gateway.link.protocol.DisconnectionRequest;
import org.universAAL.ri.gateway.link.protocol.ReconnectionRequest;

import com.google.common.net.HostAndPort;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class ServerSocketCommunicationHandler implements CommunicationHandler {

    private static final int NUM_THREADS = 1;
    private GatewayCommunicator communicator;
    private Executor executor;
    private ServerSocket server;
    private Thread serverThread;
    private final Set<ComunicationEventListener> listeners;

    public ServerSocketCommunicationHandler(
            final GatewayCommunicator communicator) {
        this.communicator = communicator;
        this.listeners = Collections
                .synchronizedSet(new HashSet<ComunicationEventListener>());

        final String hashKey = CommunicatorStarter.properties
                .getProperty(GatewayCommunicator.HASH_KEY);

        SecurityUtils.Instance.initialize(hashKey);

        this.executor = Executors.newFixedThreadPool(NUM_THREADS);
        log("Created " + ServerSocketCommunicationHandler.class.getName());
    }

    private void log(String msg) {
        LogUtils.logInfo(Activator.mc, ServerSocketCommunicationHandler.class,
                "log", msg);
    }

    public void start() throws IOException {
        final HostAndPort serverConfig = GatewayConfiguration.getInstance().getServerGateway();
        log("Starting Server Gateway on TCP server on port " + serverConfig);

        InetAddress addr = InetAddress.getByName(serverConfig.getHostText());
        server = new ServerSocket();
        server.bind(new InetSocketAddress(addr, serverConfig.getPort()));
        serverThread = new Thread(new Runnable() {
            public void run() {
                log("TCP server started on port " + serverConfig);
                Thread.currentThread().setName("Space Gateway :: Server");
                while (!(Thread.currentThread().isInterrupted())) {
                    try {
                        final Socket socket = server.accept();
                        log("Got request ... processing ...");
                        executor.execute(new LinkHandler(socket));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });
        serverThread.start();
    }

    private class LinkHandler implements Runnable {

        private Socket socket;
        private InputStream in;
        private OutputStream out;

        public LinkHandler(final Socket socket) {
            this.socket = socket;
        }

        public void run() {
            Thread.currentThread().setName("Space Gateway :: LinkHandler ");
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();

                Activator.mc.logInfo(null, "handleMessage", null);
                MessageWrapper msg = Serializer.unmarshalMessage(in);
                Activator.mc.logInfo(null,
                        "handleMessage: type: %s" + msg.getType(), null);

                if (handleSessionProtocol(msg) == false) {
                    handleGateayProtocol(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        private boolean handleGateayProtocol(MessageWrapper msg)
                throws Exception {
            communicator.handleMessage(msg, out);
            return true;
        }

        private boolean handleSessionProtocol(MessageWrapper msg) {
            AALSpaceManager spaceManager = Activator.spaceManager.getObject();
            SessionManager sessionManger = SessionManager.getInstance();
            switch (msg.getType()) {
            case ConnectRequest: {
                ConnectionRequest request = Serializer.Instance.unmarshall(
                        ConnectionRequest.class, msg.getMessage());
                UUID session = sessionManger.getSession(request.getPeerId(),
                        request.getAALSpaceId(), request.getScopeId());
                if (session == null) {
                    session = sessionManger.createSession(request.getPeerId(),
                            request.getAALSpaceId(), request.getScopeId());
                } else {
                    // TODO Log a clash of session
                }
                sessionManger.setLink(session, in, out);
                // TODO Check if it can registers
                // tenatManager.getTenant(request.getScopeId());
                String source = spaceManager.getMyPeerCard().getPeerID();
                ConnectionResponse response = new ConnectionResponse(source,
                        request.getAALSpaceId(), session);
                MessageWrapper responseMessage = new MessageWrapper(
                        MessageType.ConnectResponse,
                        Serializer.Instance.marshall(response), source);
                try {
                    Serializer.sendMessageToStream(responseMessage, out);
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO Close the session
                }
                return true;
            }

            case Disconnect: {
                DisconnectionRequest request = Serializer.Instance.unmarshall(
                        DisconnectionRequest.class, msg.getMessage());
                // request.getPeerId()
                UUID session = sessionManger.getSession(request.getPeerId(),
                        request.getAALSpaceId(), request.getScopeId());
                if (session == null) {
                    // TODO Log someone is trying to disconnect from an invalid
                    // session
                    return true;
                }
                sessionManger.close(session);
                return true;
            }
            case Reconnect: {
                ReconnectionRequest request = Serializer.Instance.unmarshall(
                        ReconnectionRequest.class, msg.getMessage());
                // request.getPeerId()
                UUID session = sessionManger.getSession(request.getPeerId(),
                        request.getAALSpaceId(), request.getScopeId());
                if (session == null
                        || request.getSessionId().equals(session) == false) {
                    // TODO someone is trying to reconnect but we don't have the
                    // link active so we create a new session
                    session = sessionManger.createSession(request.getPeerId(),
                            request.getAALSpaceId(), request.getScopeId());
                }
                sessionManger.setLink(session, in, out);
                // TODO Check if it can registers
                // tenatManager.getTenant(request.getScopeId());
                String source = spaceManager.getMyPeerCard().getPeerID();
                ConnectionResponse response = new ConnectionResponse(source,
                        request.getAALSpaceId(), session);
                MessageWrapper responseMessage = new MessageWrapper(
                        MessageType.ConnectResponse,
                        Serializer.Instance.marshall(response), source);
                try {
                    Serializer.sendMessageToStream(responseMessage, out);
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO Close the session
                }
                return true;
            }
            default:
                return false;

            }
        }
    }

    public MessageWrapper sendMessage(final MessageWrapper toSend,
            final String[] sessions) throws IOException,
            ClassNotFoundException, CryptoException {

        // TODO Stefano Lenzi: Use the target to select the scope where to send
        // the message, it should be an UUID

        MessageWrapper resp = null;
        SessionManager sessionManager = SessionManager.getInstance();

        List<UUID> activeSessions = new ArrayList<UUID>();

        for (int i = 0; i < sessions.length; i++) {
            if (CommunicationHandler.BROADCAST_SESSION == sessions[i]
                    || CommunicationHandler.BROADCAST_SESSION
                            .equals(sessions[i])) {
                // TODO Broadcast, something like
                // sessionManager.getAllSessions();
            }
            final UUID currentSession = UUID.fromString(sessions[i]);
            if (sessionManager.isActive(currentSession) == false) {
                // TODO that we haven't sent a file: either because the other
                // peer gently left or because the the link failed and gateway
                // are training to reconnect each other
                continue;
            }
            activeSessions.add(currentSession);
        }
        for (UUID currentSession : activeSessions) {

            OutputStream out = sessionManager.getOutputStream(currentSession);
            InputStream in = sessionManager.getInputStream(currentSession);

            if (out == null || in == null) {
                // TODO log that we found an invalid-session
                continue;
            }

            try {
                Serializer.sendMessageToStream(toSend, out);
                // if the other side sends a Wrapper, we should read it
                if (!toSend.getType().equals(MessageType.Context)
                        && !toSend.getType().equals(MessageType.UIResponse)) {
                    resp = Serializer.unmarshalMessage(in);
                }
            } catch (EOFException ex) {
                // no response (which is not an error) so we just return null
            }
        }
        // TODO either we change the return type to void/boolean or to
        // MessageWrapper[]
        return resp;
    }

    public void handleGateayProtocol() {
        // TODO Auto-generated method stub

    }

    public void stop() {
        try {
            server.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        serverThread.interrupt();
    }

    public void addComunicationEventListener(ComunicationEventListener cel) {
        listeners.add(cel);
    }

    public void removeComunicationEventListener(ComunicationEventListener cel) {
        listeners.remove(cel);
    }
}
