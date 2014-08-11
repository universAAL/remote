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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.ri.gateway.communicator.Activator;
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

    private final Object LOCK_VAR_STOP = new Object();
    private final Object LOCK_VAR_LINK_HANDLER = new Object();

    private LinkHandler currentLinkHandler = null;

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
	    public void run() {
		Thread.currentThread().setName("Space Gateway :: Client");
		while (!isStop()) {
		    try {
			final InetAddress addr = InetAddress
				.getByName(serverConfig.getHostText());
			final Socket socket = new Socket(addr, serverConfig
				.getPort());
			ClientSocketCommunicationHandler.log
				.debug("Client mode gateway connected to "
					+ serverConfig);
			synchronized (LOCK_VAR_LINK_HANDLER) {
			    currentLinkHandler = new LinkHandler(socket);
			    currentLinkHandler.run();
			}
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
		    } catch (final IOException e) {
			ClientSocketCommunicationHandler.log
				.error("Link betwewn client and server broken due to exception we will try to restore it",
					e);
			e.printStackTrace();
		    }
		}
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

	private final Socket socket;
	private InputStream in;
	private OutputStream out;
	private boolean stop = false;
	private final Object LOCK_VAR_LOCAL_STOP = new Object();

	public LinkHandler(final Socket socket) {
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
	    } else if (currentSession != null
		    && SessionManager.getInstance().isActive(currentSession) == false) {
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
	    } else {
		return true;
	    }
	}

	public void run() {
	    Thread.currentThread().setName("Space Gateway :: LinkHandler ");
	    try {
		in = socket.getInputStream();
		out = socket.getOutputStream();

		if (currentSession == null) {
		    ClientSocketCommunicationHandler.log
			    .debug("FIRST loading trying to create a SESSION");
		    if (connect() == false) {
			ClientSocketCommunicationHandler.log
				.debug("Creation of the session failed");
			cleanUpSocket();
			return;
		    } else {
			ClientSocketCommunicationHandler.log
				.debug("Session created with sessionId "
					+ currentSession);
		    }
		} else {
		    ClientSocketCommunicationHandler.log
			    .debug("SESSION was BROKEN by a link failure, trying to RESTORE it");
		    if (reconnect() == false) {
			ClientSocketCommunicationHandler.log
				.debug("Failed to RESTORE the SESSION");
			cleanUpSocket();
			return;
		    } else {
			ClientSocketCommunicationHandler.log
				.debug("Session with sessionId "
					+ currentSession + "");
		    }
		}
		ClientSocketCommunicationHandler.log
			.debug("SESSION (RE)ESTABILISHED with "
				+ currentSession);
		while (socket != null && !socket.isClosed() && !isStop()) {
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
	    try {
		if (currentSession != null) {
		    SessionManager.getInstance().close(currentSession);
		}
	    } catch (final Exception ex) {
		ex.printStackTrace();
	    }
	    /*
	     * if closing the session failed we try to close manually the socket
	     * and it's stream
	     */

	    manualCloseSocket();
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
	    boolean result = true;
	    try {
		Serializer.sendMessageToStream(responseMessage, out);
	    } catch (final Exception e) {
		e.printStackTrace();
		result = false;
	    }

	    cleanUpSocket();
	    return result;

	}

	private void manualCloseSocket() {

	    try {
		if (in != null) {
		    in.close();
		}
	    } catch (final IOException e) {
		e.printStackTrace();
	    }
	    try {
		if (out != null) {
		    out.flush();
		}
	    } catch (final IOException e) {
		e.printStackTrace();
	    }
	    try {
		if (out != null) {
		    out.close();
		}
	    } catch (final IOException e) {
		e.printStackTrace();
	    }
	    try {
		if (socket != null && socket.isClosed() == false) {
		    this.socket.close();
		}
	    } catch (final IOException e) {
		e.printStackTrace();
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

	private boolean isStop() {
	    synchronized (LOCK_VAR_LOCAL_STOP) {
		return stop;
	    }
	}

	private void stop() {
	    synchronized (LOCK_VAR_LOCAL_STOP) {
		stop = true;
	    }

	}
    }

    public boolean isStop() {
	synchronized (LOCK_VAR_STOP) {
	    return stopServerThread;
	}
    }

    public void stop() {
	synchronized (LOCK_VAR_STOP) {
	    stopServerThread = true;
	}
	synchronized (LOCK_VAR_LINK_HANDLER) {
	    currentLinkHandler.stop();
	    currentLinkHandler.disconnect();
	}
    }

}
