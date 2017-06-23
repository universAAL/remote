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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.universAAL.middleware.managers.api.SpaceManager;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.SessionEvent;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.log.Logger;
import org.universAAL.ri.gateway.log.LoggerFactory;
import org.universAAL.ri.gateway.protocol.LinkMessage;
import org.universAAL.ri.gateway.protocol.LinkMessage.LinkMessageType;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.MessageReceiver;
import org.universAAL.ri.gateway.protocol.link.DisconnectionRequest;

/**
 * This class implements gateway in Client mode: TCP connection is initialized
 * by the client and the client sends the {@link MessageType#ConnectRequest}
 * message to the Server Gateway.<br>
 * <br>
 * <b>NOTE that:</b> At the moment this mode supports only the routing in
 * {@link Configuration.RoutingMode#FORWARD}, and so far for supporting also the
 * {@link Configuration.RoutingMode#ROUTER} the client should be aware of all
 * the peers to join in advance, which is a very limited case
 *
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-07-23 11:24:23 +0200
 *          (Wed, 23 Jul 2014) $)
 *
 */
public class ClientSocketCommunicationHandler extends AbstractSocketCommunicationHandler {

	public static final Logger log = LoggerFactory.createLoggerFactory(Gateway.getInstance().context)
			.getLogger(ClientSocketCommunicationHandler.class);

	private static final int NUM_THREADS = 1;

	public static final long RECONNECT_WAITING_TIME = 2500;

	private final MessageReceiver communicator;
	private final Executor executor;
	private Thread serverThread;
	// flag for stopping the server thread
	private boolean stopServerThread = false;
	private final UUID currentSession = null;

	private final Object LOCK_VAR_STOP = new Object();
	private final Object LOCK_VAR_LINK_HANDLER = new Object();

	private LinkHandler currentLinkHandler = null;

	private final Configuration config;

	private final Session creator;

	public ClientSocketCommunicationHandler(final Configuration config, final MessageReceiver communicator,
			final Session s) {
		super(s.getCipher());
		this.config = config;
		this.communicator = communicator;

		this.creator = s;

		this.executor = Executors.newFixedThreadPool(ClientSocketCommunicationHandler.NUM_THREADS);
		log.debug("Created client mode gateway comunication");
	}

	public void start() throws IOException {
		final String serverConfig = config.getConnectionHost() + ":" + config.getConnectionPort();
		log.info("Starting Client Gateway by connecting to Gateway Server at " + serverConfig);
		serverThread = new Thread(new Runnable() {
			public void run() {
				Thread.currentThread().setName("GW :: Client");
				while (!isStop()) {
					try {
						Thread.sleep(RECONNECT_WAITING_TIME);
					} catch (final InterruptedException e) {
					}
					final Socket socket;
					try {
						final InetAddress addr = InetAddress.getByName(config.getConnectionHost());
						socket = new Socket(addr, config.getConnectionPort());
						creator.setStatus(SessionEvent.SessionStatus.CONNECTING);
					} catch (ConnectException ce) {
						final String msg = "Server appears to be down: \"" + ce.getMessage() + "\" retrying in "
								+ RECONNECT_WAITING_TIME + "ms";
						log.info(msg);
						continue;
					} catch (final Exception ex) {
						final String msg = "Failed to estabilished a link between client and server broken due to exception we retry in a bit";
						log.info(msg);
						log.debug(msg, ex);
						continue;
					}
					try {
						log.debug("Client mode gateway connected to " + serverConfig);
						synchronized (LOCK_VAR_LINK_HANDLER) {
							currentLinkHandler = new LinkHandler(socket, communicator, creator);
						}
						currentLinkHandler.run();
						creator.setStatus(SessionEvent.SessionStatus.CONNECTING);
						log.debug("Link is down, so we are goging to try again in "
								+ ClientSocketCommunicationHandler.RECONNECT_WAITING_TIME + " ms");
					} catch (final Exception e) {
						creator.setStatus(SessionEvent.SessionStatus.CONNECTING);
						log.error("Link between client and server broken due to exception we will try to restore it",
								e);
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
	private class LinkHandler extends AbstractLinkHandler {

		private final Session session;

		public LinkHandler(final Socket socket, final MessageReceiver communicator, final Session s) {
			super(socket, communicator, cipher);
			session = s;
		}

		@Override
		protected boolean beforeRun() {
			Thread.currentThread().setName("Space Gateway :: LinkHandler ");
			if (currentSession == null) {
				log.debug("FIRST loading trying to create a SESSION");
				if (connect() == false) {
					log.debug("Creation of the session failed");
					cleanUpSession();
					return false;
				} else {
					session.setScope(SessionManager.getInstance().getAALSpaceIdFromSession(currentSession));
					session.setStatus(SessionEvent.SessionStatus.CONNECTED);
					log.debug("Session created with sessionId " + currentSession);
				}
			} else {
				log.debug("SESSION was BROKEN by a link failure, trying to RESTORE it");
				if (reconnect() == false) {
					log.debug("Failed to RESTORE the SESSION");
					cleanUpSession();
					return false;
				} else {
					session.setScope(SessionManager.getInstance().getAALSpaceIdFromSession(currentSession));
					session.setStatus(SessionEvent.SessionStatus.CONNECTED);
					log.debug("Session with sessionId " + currentSession + " re-established");
				}
			}
			log.debug("SESSION (RE)ESTABILISHED with " + currentSession);
			// session.setScope(currentSession.toString());
			// session.setScope(SessionManager.getInstance()
			// .getAALSpaceIdFromSession(currentSession));
			return true;
		}

		@Override
		protected boolean loopRun() {
			if (socket != null && !socket.isClosed()) {
				Message msg;
				try {
					msg = getNextMessage(in);
				} catch (final Exception e) {
					if (e instanceof EOFException) {
						log.info("Failed to read message of the stream beacuse it was closed from the other side");
						return false;
					} else {
						log.debug("Failed to read message from stream", e);
						return false;
					}
				}
				if (handleSessionProtocol(msg) == false) {
					handleGatewayProtocol(msg);
				}
				return true;
			} else {
				return false;
			}
		}

		@Override
		protected boolean afterRun() {
			return true;
		}

		@Override
		protected Message getNextMessage(final InputStream in) throws Exception {
			return readMessage(in);
		}

		@Override
		protected boolean handleSessionProtocol(final Message msg) {
			final SpaceManager spaceManager = Gateway.getInstance().spaceManager.getObject();
			final SessionManager sessionManger = SessionManager.getInstance();
			LinkMessage link = null;
			if (msg instanceof LinkMessage) {
				link = (LinkMessage) msg;
			}
			if (link == null) {
				return false;
			} else if (link.getType() == LinkMessageType.RECONNECTION_REQUEST.ordinal()
					|| link.getType() == LinkMessageType.CONNECTION_REQUEST.ordinal()) {
				throw new IllegalArgumentException("Receieved unexpected message " + link.getType());
			} else if (link.getType() == LinkMessageType.DISCONNECTION_REQUEST.ordinal()) {
				final DisconnectionRequest request = (DisconnectionRequest) link;
				// request.getPeerId()
				final UUID session = sessionManger.getSession(request.getPeerId(), request.getAALSpaceId(),
						request.getScopeId());
				if (session == null) {
					// TODO Log someone is trying to disconnect from an invalid
					// session
					ClientSocketCommunicationHandler.log.warning("Trying to close a-non existing session with <"
							+ request.getAALSpaceId() + "," + request.getPeerId() + ">, we just ignore it");
					return true;
				}
				this.session.setStatus(SessionEvent.SessionStatus.CLOSED);
				sessionManger.close(session);
				return true;
			}

			throw new IllegalStateException(
					"Unable to handle the message msg it is neither a LinkMessage nor other known message types: "
							+ msg);
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
			if (currentLinkHandler != null) {
				currentLinkHandler.stop();
				currentLinkHandler.disconnect();
				creator.setStatus(SessionEvent.SessionStatus.CLOSED);
			}
		}
	}

}
