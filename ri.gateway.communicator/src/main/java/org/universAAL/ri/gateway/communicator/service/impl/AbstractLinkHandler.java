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

import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.ri.gateway.communicator.Activator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.link.protocol.ConnectionRequest;
import org.universAAL.ri.gateway.link.protocol.ConnectionResponse;
import org.universAAL.ri.gateway.link.protocol.DisconnectionRequest;
import org.universAAL.ri.gateway.link.protocol.ReconnectionRequest;

/**
 * This class implements an generic link handler that has to be refined
 * depending on the actual role of the peer in the link
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * 
 */
public abstract class AbstractLinkHandler implements Runnable {

    protected final Socket socket;
    protected final InputStream in;
    protected final OutputStream out;
    private boolean stop = false;
    private final Object LOCK_VAR_LOCAL_STOP = new Object();
    protected UUID currentSession = null;
    protected final GatewayCommunicator communicator;
    protected LinkHandlerStatus state;
    private static final Logger log = LoggerFactory.createLoggerFactory(
	    Activator.mc).getLogger(AbstractLinkHandler.class);

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

    public AbstractLinkHandler(final Socket socket,
	    final GatewayCommunicator communicator) {
	this.state = LinkHandlerStatus.INITIALIZING;
	this.socket = socket;
	this.communicator = communicator;
	try {
	    in = socket.getInputStream();
	    out = socket.getOutputStream();
	} catch (final Exception e) {
	    cleanUpSession();
	    log.error("SESSION BROKEN due to exception", e);
	    throw new IllegalStateException(e);
	}
    }

    protected abstract boolean beforeRun();

    protected abstract boolean loopRun();

    protected abstract boolean afterRun();

    public void run() {
	this.state = LinkHandlerStatus.STARTING;
	Thread.currentThread().setName("Space Gateway :: AbstractLinkHandler ");
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
	final AALSpaceManager spaceManager = Activator.spaceManager.getObject();
	final SessionManager sessionManger = SessionManager.getInstance();
	final String spaceId = spaceManager.getAALSpaceDescriptor()
		.getSpaceCard().getSpaceID();
	final String peerId = spaceManager.getMyPeerCard().getPeerID();

	if (spaceId.equals(sessionManger
		.getAALSpaceIdFromSession(currentSession)) == false) {
	    throw new IllegalStateException(
		    "We joined a different AAL Space, during the automatic reconnection of the Gateway,");
	}

	if (peerId.equals(sessionManger.getPeerIdFromSession(currentSession)) == false) {
	    throw new IllegalStateException(
		    "Between the automatic reconnection of the Gateway we changed our PeerId something strange happened");
	}

	final ReconnectionRequest request = new ReconnectionRequest(peerId,
		spaceId, currentSession);
	final MessageWrapper responseMessage = new MessageWrapper(
		MessageType.Reconnect, Serializer.Instance.marshall(request),
		peerId);
	try {
	    Serializer.sendMessageToStream(responseMessage, out);
	    final MessageWrapper rsp = getNextMessage(in);
	    if (rsp.getType() != MessageType.ConnectResponse) {
		throw new IllegalArgumentException("Expected "
			+ MessageType.ConnectResponse + " message after a "
			+ MessageType.Reconnect + " but recieved "
			+ rsp.getType());
	    }
	    final ConnectionResponse response = Serializer.Instance.unmarshall(
		    ConnectionResponse.class, rsp.getMessage());
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
	    sessionManger.setLink(currentSession, socket.getInputStream(),
		    socket.getOutputStream());
	    return true;
	} catch (final Exception e) {
	    e.printStackTrace();
	}
	return false;
    }

    protected void cleanUpSession() {
	try {
	    if (currentSession != null) {
		SessionManager.getInstance().close(currentSession);
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
	final AALSpaceManager spaceManager = Activator.spaceManager.getObject();
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
	    final MessageWrapper rsp = getNextMessage(in);
	    if (rsp.getType() != MessageType.ConnectResponse) {
		throw new IllegalArgumentException("Expected "
			+ MessageType.ConnectResponse + " message after a "
			+ MessageType.ConnectRequest + " but recieved "
			+ rsp.getType());
	    }
	    final ConnectionResponse response = Serializer.Instance.unmarshall(
		    ConnectionResponse.class, rsp.getMessage());
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

    protected abstract MessageWrapper getNextMessage(InputStream in)
	    throws Exception;

    protected boolean disconnect() {
	final AALSpaceManager spaceManager = Activator.spaceManager.getObject();
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

    protected boolean handleGatewayProtocol(final MessageWrapper msg) {
	try {
	    communicator.handleMessage(msg, out);
	} catch (final Exception ex) {
	    final String txt = "Exception while handling Gateway message "
		    + msg;
	    log.info(txt);
	    log.debug(txt, ex);
	    return false;
	}
	return true;
    }

    protected abstract boolean handleSessionProtocol(final MessageWrapper msg);

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
