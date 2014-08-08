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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
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
public class ServerSocketCommunicationHandler extends
	AbstractSocketCommunicationHandler {

    public static final Logger log = LoggerFactory.createLoggerFactory(
	    Activator.mc).getLogger(ServerSocketCommunicationHandler.class);

    private static final int NUM_THREADS = 1;
    private final GatewayCommunicator communicator;
    // private Executor executor;
    private ServerSocket server;
    private Thread serverThread;
    private final Set<ComunicationEventListener> listeners;
    private final ExecutorService executor;

    public ServerSocketCommunicationHandler(
	    final GatewayCommunicator communicator) {
	this.communicator = communicator;
	this.listeners = Collections
		.synchronizedSet(new HashSet<ComunicationEventListener>());

	final String hashKey = CommunicatorStarter.properties
		.getProperty(GatewayCommunicator.HASH_KEY);

	SecurityUtils.Instance.initialize(hashKey);

	this.executor = Executors.newCachedThreadPool();
	/*
	 * //TODO Define a maxiumum number of threads
	 */
	// this.executor = Executors.newFixedThreadPool(NUM_THREADS);
	ServerSocketCommunicationHandler.log.info("Created "
		+ ServerSocketCommunicationHandler.class.getName());
    }

    public void start() throws IOException {
	final HostAndPort serverConfig = GatewayConfiguration.getInstance()
		.getServerGateway();
	ServerSocketCommunicationHandler.log
		.debug("Starting Server Gateway on TCP server on port "
			+ serverConfig);

	final InetAddress addr = InetAddress.getByName(serverConfig
		.getHostText());
	server = new ServerSocket();
	server.bind(new InetSocketAddress(addr, serverConfig.getPort()));
	serverThread = new Thread(new Runnable() {
	    public void run() {
		ServerSocketCommunicationHandler.log
			.debug("TCP server started on port " + serverConfig);
		Thread.currentThread().setName("Space Gateway :: Server");
		while (!(Thread.currentThread().isInterrupted())) {
		    try {
			final Socket socket = server.accept();
			ServerSocketCommunicationHandler.log
				.debug("Got new incoming connection");
			executor.execute(new LinkHandler(socket));
		    } catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

	    }
	});
	serverThread.start();
    }

    private class LinkHandler implements Runnable {

	private final Socket socket;
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

		while (socket != null && !socket.isClosed()) {
		    final MessageWrapper msg = readMessage(in);

		    if (handleSessionProtocol(msg) == false) {
			handleGatewayProtocol(msg);
		    }

		}
	    } catch (final Exception e) {
		e.printStackTrace();
	    } finally {
		if (socket != null) {
		    try {
			socket.close();
		    } catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
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
	    case ConnectRequest: {
		final ConnectionRequest request = Serializer.Instance
			.unmarshall(ConnectionRequest.class, msg.getMessage());
		UUID session = sessionManger.getSession(request.getPeerId(),
			request.getAALSpaceId(), request.getScopeId());
		if (session == null) {
		    session = sessionManger.createSession(request.getPeerId(),
			    request.getAALSpaceId(), request.getScopeId(),
			    request.getDescription());
		    ServerSocketCommunicationHandler.log
			    .debug("CREATED SESSION with " + session
				    + " pointing at <"
				    + request.getAALSpaceId() + ","
				    + request.getPeerId() + ">");
		} else {
		    ServerSocketCommunicationHandler.log
			    .warning("SESSION CLASH: the client may be restarted without persistance before the session was broken and deleted. We just create a new session");
		}
		sessionManger.setLink(session, in, out);
		// TODO Check if it can registers
		// tenatManager.getTenant(request.getScopeId());
		final String source = spaceManager.getMyPeerCard().getPeerID();
		final ConnectionResponse response = new ConnectionResponse(
			source, request.getAALSpaceId(), session);
		final MessageWrapper responseMessage = new MessageWrapper(
			MessageType.ConnectResponse,
			Serializer.Instance.marshall(response), source);
		try {
		    Serializer.sendMessageToStream(responseMessage, out);
		} catch (final Exception e) {
		    e.printStackTrace();
		    // TODO Close the session
		}
		return true;
	    }

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
		    return true;
		}
		sessionManger.close(session);
		return true;
	    }
	    case Reconnect: {
		final ReconnectionRequest request = Serializer.Instance
			.unmarshall(ReconnectionRequest.class, msg.getMessage());
		// request.getPeerId()
		UUID session = sessionManger.getSession(request.getPeerId(),
			request.getAALSpaceId(), request.getScopeId());
		if (session == null
			|| request.getSessionId().equals(session) == false) {
		    // TODO someone is trying to reconnect but we don't have the
		    // link active so we create a new session
		    // XXX we should set the session description properly
		    session = sessionManger
			    .createSession(request.getPeerId(),
				    request.getAALSpaceId(),
				    request.getScopeId(), null);
		}
		sessionManger.setLink(session, in, out);
		// TODO Check if it can registers
		// tenatManager.getTenant(request.getScopeId());
		final String source = spaceManager.getMyPeerCard().getPeerID();
		final ConnectionResponse response = new ConnectionResponse(
			source, request.getAALSpaceId(), session);
		final MessageWrapper responseMessage = new MessageWrapper(
			MessageType.ConnectResponse,
			Serializer.Instance.marshall(response), source);
		try {
		    Serializer.sendMessageToStream(responseMessage, out);
		} catch (final Exception e) {
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

    public void stop() {
	try {
	    server.close();
	} catch (final IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	serverThread.interrupt();
    }

}
