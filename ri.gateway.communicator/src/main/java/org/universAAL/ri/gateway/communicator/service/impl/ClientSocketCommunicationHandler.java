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
import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.ComunicationEventListener;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator.RoutingMode;
import org.universAAL.ri.gateway.link.protocol.ConnectionRequest;
import org.universAAL.ri.gateway.link.protocol.ConnectionResponse;
import org.universAAL.ri.gateway.link.protocol.DisconnectionRequest;
import org.universAAL.ri.gateway.link.protocol.ReconnectionRequest;

import com.google.common.net.HostAndPort;

/**
 * This class implements gateway in Client mode: TCP connection is initialized
 * by the client and the client sends the {@link MessageType#ConnectRequest}
 * message to the Server Gateway.<br>
 * <br>
 * <b>NOTE that:</b> At the moment this mode supports only the routing in
 * {@link RoutingMode#FORWARD}, and so far for supporting also the
 * {@link RoutingMode#ROUTER} the client should be aware of all the peers to
 * join in advance, which is a very limited case
 * 
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-07-23 11:24:23
 *          +0200 (Wed, 23 Jul 2014) $)
 * 
 */
public class ClientSocketCommunicationHandler extends
	AbstractSocketCommunicationHandler {

    public static final Logger log = LoggerFactory.createLoggerFactory(
	    Activator.mc).getLogger(ClientSocketCommunicationHandler.class);

    private static final int NUM_THREADS = 1;

    public static final long RECONNECT_WAITING_TIME = 2500;
    private final GatewayCommunicator communicator;
    private final Executor executor;
    private Thread serverThread;
    // flag for stoppping the server thread
    private boolean stopServerThread = false;
    private final Set<ComunicationEventListener> listeners;
    private UUID currentSession = null;

    public ClientSocketCommunicationHandler(
	    final GatewayCommunicator communicator) {
	this.communicator = communicator;
	this.listeners = Collections
		.synchronizedSet(new HashSet<ComunicationEventListener>());

	final String hashKey = CommunicatorStarter.properties
		.getProperty(GatewayCommunicator.HASH_KEY);

	SecurityUtils.Instance.initialize(hashKey);

	this.executor = Executors
		.newFixedThreadPool(ClientSocketCommunicationHandler.NUM_THREADS);
	ClientSocketCommunicationHandler.log
		.debug("Created client mode gateway comunication");
    }

    public void start() throws IOException {
	final HostAndPort serverConfig = GatewayConfiguration.getInstance()
		.getServerGateway();
	ClientSocketCommunicationHandler.log
		.info("Starting Client Gateway by connecting to Gateway Server at "
			+ serverConfig);

	serverThread = new Thread(new Runnable() {

	    LinkHandler linkHandler = new LinkHandler();
	    Thread linkHandlerThread = new Thread(linkHandler);

	    public void run() {
		Thread.currentThread().setName("Space Gateway :: Server");

		while (!stopServerThread) {
		    try {
			final InetAddress addr = InetAddress
				.getByName(serverConfig.getHostText());
			final Socket socket = new Socket(addr, serverConfig
				.getPort());
			ClientSocketCommunicationHandler.log
				.debug("Client mode gateway connected to "
					+ serverConfig);
			linkHandler.setSocket(socket);
			while (!linkHandler.checkConnection()) {
			    ClientSocketCommunicationHandler.log
				    .debug("Link is down, so we are goging to try again in a "
					    + ClientSocketCommunicationHandler.RECONNECT_WAITING_TIME
					    + "ms");
			    try {
				Thread.sleep(ClientSocketCommunicationHandler.RECONNECT_WAITING_TIME);
			    } catch (final InterruptedException e) {
				ClientSocketCommunicationHandler.log.debug(
					"Ignored exception", e);
			    }
			}
			linkHandlerThread.start();

		    } catch (final IOException e) {
			ClientSocketCommunicationHandler.log
				.error("Link betwewn client and server broken due to exception we will try to restore it",
					e);
			e.printStackTrace();
		    }
		}
		// stop the Link Handler
		linkHandler.disconnect();
		linkHandlerThread.interrupt();

	    }
	});
	serverThread.start();
    }

    /**
     * This thread initializes the connection of the server gateway and managed
     * the messages
     * 
     * 
     */
    private class LinkHandler implements Runnable {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private boolean stop = false;

	public void setSocket(final Socket socket) {
	    this.socket = socket;
	    try {
		in = socket.getInputStream();
		out = socket.getOutputStream();
	    } catch (final Exception e) {
		cleanUpSocket();
		ClientSocketCommunicationHandler.log.error(
			"SESSION BROKEN due to exception", e);
		e.printStackTrace();

	    }
	}

	/**
	 * Checks if the connection can be established with the Gateway Server
	 * 
	 * @return
	 */
	public boolean checkConnection() {
	    if (currentSession == null) {
		ClientSocketCommunicationHandler.log
			.debug("FIRST loading trying to create a SESSION");
		if (connect() == false) {
		    ClientSocketCommunicationHandler.log
			    .debug("Creation of the session failed");
		    cleanUpSocket();
		    return false;
		} else {
		    ClientSocketCommunicationHandler.log
			    .debug("Session created with sessionId "
				    + currentSession);
		    return true;
		}
	    } else {
		ClientSocketCommunicationHandler.log
			.debug("SESSION was BROKEN by a link failure, trying to RESTORE it");
		if (reconnect() == false) {
		    ClientSocketCommunicationHandler.log
			    .debug("Failed to RESTORE the SESSION");
		    cleanUpSocket();
		    return false;
		} else {
		    ClientSocketCommunicationHandler.log
			    .debug("Session with sessionId " + currentSession
				    + "");
		    return true;
		}
	    }
	}

	public void setStop() {
	    stop = true;
	}

	public void run() {
	    try {
		// waiting for messages
		while (socket != null && !socket.isClosed()) {
		    if (stop) {
			if (!disconnect()) {
			    ClientSocketCommunicationHandler.log.error(
				    "Error during request of disconnection",
				    null);
			}
			cleanUpSocket();
			return;
		    }
		    final MessageWrapper msg = readMessage(in);
		    if (handleSessionProtocol(msg) == false) {
			handleGatewayProtocol(msg);
		    }
		}
	    } catch (final Exception e) {
		ClientSocketCommunicationHandler.log.error(
			"SESSION BROKEN due to exception", e);
		e.printStackTrace();
	    } finally {
		cleanUpSocket();
	    }
	}

	private boolean reconnect() {
	    final AALSpaceManager spaceManager = Activator.spaceManager
		    .getObject();
	    final SessionManager sessionManger = SessionManager.getInstance();
	    final String spaceId = spaceManager.getAALSpaceDescriptor()
		    .getSpaceCard().getSpaceID();
	    final String peerId = spaceManager.getMyPeerCard().getPeerID();

	    if (spaceId.equals(sessionManger
		    .getAALSpaceIdFromSession(currentSession)) == false) {
		throw new IllegalStateException(
			"We joined a different AAL Space, during the automatic reconnection of the Gateway,");
	    }

	    if (peerId.equals(sessionManger
		    .getPeerIdFromSession(currentSession)) == false) {
		throw new IllegalStateException(
			"Between the automatic reconnection of the Gateway we changed our PeerId something strange happened");
	    }

	    final ReconnectionRequest request = new ReconnectionRequest(peerId,
		    spaceId, currentSession);
	    final MessageWrapper responseMessage = new MessageWrapper(
		    MessageType.Reconnect,
		    Serializer.Instance.marshall(request), peerId);
	    try {
		Serializer.sendMessageToStream(responseMessage, out);
		final MessageWrapper rsp = readMessage(in);
		if (rsp.getType() != MessageType.ConnectResponse) {
		    throw new IllegalArgumentException("Expected "
			    + MessageType.ConnectResponse + " message after a "
			    + MessageType.Reconnect + " but recieved "
			    + rsp.getType());
		}
		final ConnectionResponse response = Serializer.Instance
			.unmarshall(ConnectionResponse.class, rsp.getMessage());
		if (response.getScopeId().equals(currentSession)) {
		    /*
		     * Session has been restore
		     */
		} else {
		    /*
		     * The server may be rebooted so we have to initialize again
		     * the session //TODO
		     */
		    currentSession = response.getSessionId();
		}
		sessionManger.setLink(currentSession, socket.getInputStream(),
			socket.getOutputStream());
		return true;
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	    return false;
	}

	private void cleanUpSocket() {
	    if (socket != null && socket.isClosed() == false) {
		try {
		    socket.close();
		} catch (final IOException e) {
		    e.printStackTrace();
		}
	    }
	}

	private boolean connect() {
	    final AALSpaceManager spaceManager = Activator.spaceManager
		    .getObject();
	    final SessionManager sessionManger = SessionManager.getInstance();
	    final String spaceId = spaceManager.getAALSpaceDescriptor()
		    .getSpaceCard().getSpaceID();
	    final String spaceName = spaceManager.getAALSpaceDescriptor()
		    .getSpaceCard().getSpaceName();
	    final String peerId = spaceManager.getMyPeerCard().getPeerID();
	    final ConnectionRequest request = new ConnectionRequest(spaceId,
		    peerId, spaceName);
	    final MessageWrapper responseMessage = new MessageWrapper(
		    MessageType.ConnectRequest,
		    Serializer.Instance.marshall(request), peerId);
	    try {
		Serializer.sendMessageToStream(responseMessage, out);
		final MessageWrapper rsp = readMessage(in);
		if (rsp.getType() != MessageType.ConnectResponse) {
		    throw new IllegalArgumentException("Expected "
			    + MessageType.ConnectResponse + " message after a "
			    + MessageType.ConnectRequest + " but recieved "
			    + rsp.getType());
		}
		final ConnectionResponse response = Serializer.Instance
			.unmarshall(ConnectionResponse.class, rsp.getMessage());
		sessionManger.storeSession(response.getSessionId(),
			response.getPeerId(), response.getAALSpaceId(),
			response.getScopeId());
		currentSession = response.getSessionId();
		sessionManger.setLink(currentSession, socket.getInputStream(),
			socket.getOutputStream());
		return true;
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	    return false;
	}

	private boolean disconnect() {
	    final AALSpaceManager spaceManager = Activator.spaceManager
		    .getObject();
	    final String spaceId = spaceManager.getAALSpaceDescriptor()
		    .getSpaceCard().getSpaceID();
	    final String peerId = spaceManager.getMyPeerCard().getPeerID();
	    final DisconnectionRequest disconnectionRequest = new DisconnectionRequest(
		    peerId, spaceId, currentSession);
	    final MessageWrapper responseMessage = new MessageWrapper(
		    MessageType.Disconnect,
		    Serializer.Instance.marshall(disconnectionRequest), peerId);
	    try {
		Serializer.sendMessageToStream(responseMessage, out);
		return true;
	    } catch (final Exception e) {
		e.printStackTrace();
		return false;
	    }

	}

	private boolean handleGatewayProtocol(final MessageWrapper msg)
		throws Exception {
	    communicator.handleMessage(msg, out);
	    return true;
	}

	private boolean handleSessionProtocol(final MessageWrapper msg) {
	    final AALSpaceManager spaceManager = Activator.spaceManager
		    .getObject();
	    final SessionManager sessionManger = SessionManager.getInstance();
	    switch (msg.getType()) {
	    case Reconnect:
	    case ConnectRequest:
		throw new IllegalArgumentException(
			"Receieved unexpected message " + msg.getType());

	    case Disconnect: {
		final DisconnectionRequest request = Serializer.Instance
			.unmarshall(DisconnectionRequest.class,
				msg.getMessage());
		// request.getPeerId()
		final UUID session = sessionManger.getSession(
			request.getPeerId(), request.getAALSpaceId(),
			request.getScopeId());
		if (session == null) {
		    // TODO Log someone is trying to disconnect from an invalid
		    // session
		    ClientSocketCommunicationHandler.log
			    .warning("Trying to close a-non existing session with <"
				    + request.getAALSpaceId()
				    + ","
				    + request.getPeerId()
				    + ">, we just ignore it");
		    return true;
		}
		sessionManger.close(session);
		return true;
	    }

	    default:
		return false;

	    }
	}
    }

    public void stop() {
	stopServerThread = true;

    }

}
