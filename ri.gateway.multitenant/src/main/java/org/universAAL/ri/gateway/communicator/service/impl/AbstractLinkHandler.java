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
import java.net.Socket;
import java.util.UUID;

import org.universAAL.middleware.managers.api.SpaceManager;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.communication.cipher.Cipher;
import org.universAAL.ri.gateway.communicator.service.CommunicationHelper;
import org.universAAL.ri.gateway.log.Logger;
import org.universAAL.ri.gateway.log.LoggerFactory;
import org.universAAL.ri.gateway.protocol.LinkMessage;
import org.universAAL.ri.gateway.protocol.LinkMessage.LinkMessageType;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.MessageReceiver;
import org.universAAL.ri.gateway.protocol.link.ConnectionRequest;
import org.universAAL.ri.gateway.protocol.link.ConnectionResponse;
import org.universAAL.ri.gateway.protocol.link.DisconnectionRequest;
import org.universAAL.ri.gateway.protocol.link.ReconnectionRequest;

/**
 * This class implements an generic link handler that has to be refined
 * depending on the actual role of the peer in the link
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-08-12 14:08:20 +0200
 *          (Tue, 12 Aug 2014) $)
 *
 */
public abstract class AbstractLinkHandler implements Runnable {

	protected final Socket socket;
	protected final InputStream in;
	protected final OutputStream out;
	private boolean stop = false;
	private final Object LOCK_VAR_LOCAL_STOP = new Object();
	protected UUID currentSession = null;
	protected final MessageReceiver communicator;
	protected LinkHandlerStatus state;
	private final Cipher cipher;
	protected final SessionManager refSM = SessionManager.getInstance();

	private static final Logger log = LoggerFactory.createLoggerFactory(Gateway.getInstance().context)
			.getLogger(AbstractLinkHandler.class);

	public enum LinkHandlerStatus {
		/**
		 * During the constructor
		 */
		INITIALIZING,
		/**
		 * Thread containing is started we are executing
		 * {@link AbstractLinkHandler#beforeRun()}<br>
		 * During this phase we should complete the CONNECTION
		 */
		STARTING,
		/**
		 * We are the main loop of execution where we wait for message and we
		 * handle it, and we are executing {@link AbstractLinkHandler#loopRun()}
		 * during each iteration
		 *
		 */
		RUNNING,
		/**
		 * {@link AbstractLinkHandler#stop()} has been invoked so we are
		 * terminating and we are going to execute
		 * {@link AbstractLinkHandler#afterRun()} for cleaning up status and
		 * resources
		 *
		 */
		CLOSING,

		/**
		 * All the cleaning phase have been completed no further state allowed
		 */
		CLOSED
	}

	public AbstractLinkHandler(final Socket socket, final MessageReceiver communicator, final Cipher cipher) {
		this.state = LinkHandlerStatus.INITIALIZING;
		this.socket = socket;
		this.communicator = communicator;
		this.cipher = cipher;
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (final Exception e) {
			cleanUpSession();
			log.error("SESSION BROKEN due to exception", e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * The method is invoked before the main loop during which theh
	 * {@link #loopRun()} is executed
	 *
	 * @return true if the intialization was a SUCCES false will exit the
	 *         execution
	 */
	protected abstract boolean beforeRun();

	/**
	 * The actual action that is performed eveytime. The method should return
	 * <code>false</code> to stop the looping
	 *
	 * @return false to stop the looping
	 */
	protected abstract boolean loopRun();

	/**
	 * The method is invoked after the main loop during which theh
	 * {@link #loopRun()} for cleaning up
	 *
	 * @return false if cleaning up failed
	 */
	protected abstract boolean afterRun();

	public void run() {
		this.state = LinkHandlerStatus.STARTING;
		Thread.currentThread().setName("GW :: AbstractLinkHandler ");
		if (!beforeRun()) {
			this.state = LinkHandlerStatus.CLOSED;
			return;
		}
		this.state = LinkHandlerStatus.RUNNING;
		while (loopRun() && !isStopping()) {
			;
		}
		this.state = LinkHandlerStatus.CLOSING;
		afterRun();
		cleanUpSession();
		this.state = LinkHandlerStatus.CLOSED;
	}

	protected boolean reconnect() {
		final SpaceManager spaceManager = Gateway.getInstance().spaceManager.getObject();
		final String spaceId = spaceManager.getSpaceDescriptor().getSpaceCard().getSpaceID();
		final String peerId = spaceManager.getMyPeerCard().getPeerID();

		if (spaceId.equals(refSM.getSpaceIdFromSession(currentSession)) == false) {
			throw new IllegalStateException(
					"We joined a different Space, during the automatic reconnection of the Gateway,");
		}

		if (peerId.equals(refSM.getPeerIdFromSession(currentSession)) == false) {
			throw new IllegalStateException(
					"Between the automatic reconnection of the Gateway we changed our PeerId something strange happened");
		}

		final ReconnectionRequest request = new ReconnectionRequest(peerId, spaceId, currentSession);
		try {
			CommunicationHelper.cypherAndSend(request, out, cipher);
			final Message rsp = getNextMessage(in);
			LinkMessage linkMessage = null;
			if (rsp instanceof LinkMessage) {
				linkMessage = (LinkMessage) rsp;
			}
			if (linkMessage == null || linkMessage.getType() != LinkMessageType.CONNECTION_RESPONSE.ordinal()) {
				throw new IllegalArgumentException("Expected " + LinkMessageType.CONNECTION_RESPONSE
						+ " message after a " + LinkMessageType.RECONNECTION_REQUEST + " but recieved " + rsp);
			}
			final ConnectionResponse response = (ConnectionResponse) linkMessage;
			if (response.getScopeId().equals(currentSession)) {
				/*
				 * Session has been restore
				 */
			} else {
				/*
				 * The server may be rebooted so we have to initialize again the
				 * session //TODO
				 */
				currentSession = response.getSessionId();
			}
			refSM.setLink(currentSession, in, out);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected void cleanUpSession() {
		try {
			if (currentSession != null) {
				refSM.close(currentSession);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		/*
		 * if closing the session failed we try to close manually the socket and
		 * it's stream
		 */

		manualCloseSocket();
	}

	protected boolean connect() {
		final SpaceManager spaceManager = Gateway.getInstance().spaceManager.getObject();
		final String spaceId = spaceManager.getSpaceDescriptor().getSpaceCard().getSpaceID();
		final String spaceName = spaceManager.getSpaceDescriptor().getSpaceCard().getSpaceName();
		final String peerId = spaceManager.getMyPeerCard().getPeerID();
		final ConnectionRequest request = new ConnectionRequest(peerId, spaceId, spaceName);
		try {
			CommunicationHelper.cypherAndSend(request, out, cipher);
			final Message rsp = getNextMessage(in);
			LinkMessage linkMessage = null;
			if (rsp instanceof LinkMessage) {
				linkMessage = (LinkMessage) rsp;
			}
			if (linkMessage == null || linkMessage.getType() != LinkMessageType.CONNECTION_RESPONSE.ordinal()) {
				throw new IllegalArgumentException("Expected " + LinkMessageType.CONNECTION_RESPONSE
						+ " message after a " + LinkMessageType.CONNECTION_REQUEST + " but recieved " + rsp);
			}
			final ConnectionResponse response = (ConnectionResponse) linkMessage;
			/*
			 * We have to check if it is a real new session or if the server is
			 * recovering an old one, in that case we don't have to store the
			 * session
			 */
			if (refSM.isDuplicatedSession(response.getSessionId(), response.getPeerId(), response.getSpaceId(),
					response.getScopeId()) == false) {
				refSM.storeSession(response.getSessionId(), response.getPeerId(), response.getSpaceId(),
						response.getScopeId());
			}
			/*
			 * CurrenteSession and Streams must be updated anyhow because even
			 * if we are recovering the old TCP link is no more valid
			 */
			currentSession = response.getSessionId();
			refSM.setLink(currentSession, in, out);
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected abstract Message getNextMessage(InputStream in) throws Exception;

	protected boolean disconnect() {
		final SpaceManager spaceManager = Gateway.getInstance().spaceManager.getObject();
		final String spaceId = spaceManager.getSpaceDescriptor().getSpaceCard().getSpaceID();
		final String peerId = spaceManager.getMyPeerCard().getPeerID();
		final DisconnectionRequest request = new DisconnectionRequest(peerId, spaceId, currentSession);
		boolean result = true;
		try {
			CommunicationHelper.cypherAndSend(request, out, cipher);
		} catch (final Exception e) {
			e.printStackTrace();
			result = false;
		}

		cleanUpSession();
		return result;

	}

	protected void manualCloseSocket() {

		try {
			if (in != null) {
				log.info("Closing OutputStream on the link");
				in.close();
			}
		} catch (final IOException e) {
			log.debug("Closing InputStream of the link", e);
		}
		try {
			if (out != null) {
				log.info("Flushing OutputStream on the link");
				out.flush();
			}
		} catch (final IOException e) {
			log.debug("Closing InputStream of the link", e);
		}
		try {
			if (out != null) {
				log.info("Closing OutputStream on the link");
				out.close();
			}
		} catch (final IOException e) {
			log.debug("Closing OutputStream of the link", e);
		}
		try {
			if (socket != null && socket.isClosed() == false) {
				log.info("Closing Socket on the link");
				this.socket.close();
			}
		} catch (final IOException e) {
			log.debug("Closing Socket of the link", e);
		}

	}

	protected boolean handleGatewayProtocol(final Message msg) {
		try {
			if (msg instanceof LinkMessage == true) {
				log.info("The handling of Message " + msg
						+ " is not expected to be performed by the Upper Layer of Gateway because it is LinkMessage thus the message is SKIPPED");
				return false;
			}
			communicator.handleMessage(msg);
		} catch (final Exception ex) {
			final String txt = "Exception while handling Gateway message " + msg;
			log.info(txt);
			log.debug(txt, ex);
			return false;
		}
		return true;
	}

	protected abstract boolean handleSessionProtocol(final Message msg);

	public boolean isStopping() {
		synchronized (LOCK_VAR_LOCAL_STOP) {
			return stop;
		}
	}

	public void stop() {
		synchronized (LOCK_VAR_LOCAL_STOP) {
			stop = true;
		}

	}
}
