/*******************************************************************************
 * Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
 * Institute of Information Science and Technologies
 * of the Italian National Research Council
 *
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway;

import java.util.HashSet;
import java.util.UUID;

import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.gateway.communication.cipher.Blowfish;
import org.universAAL.ri.gateway.communication.cipher.Cipher;
import org.universAAL.ri.gateway.communicator.service.impl.AbstractSocketCommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.impl.ClientSocketCommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.impl.MessageType;
import org.universAAL.ri.gateway.communicator.service.impl.MessageWrapper;
import org.universAAL.ri.gateway.communicator.service.impl.ServerSocketCommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.impl.SessionManager;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.configuration.Configuration.ConnectionMode;
import org.universAAL.ri.gateway.operations.MessageOperationChain;
import org.universAAL.ri.gateway.operations.OperationChainManager;
import org.universAAL.ri.gateway.operations.ParameterCheckOpertaionChain;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.ImportMessage;
import org.universAAL.ri.gateway.protocol.Message;
import org.universAAL.ri.gateway.protocol.MessageReceiver;
import org.universAAL.ri.gateway.protocol.MessageSender;
import org.universAAL.ri.gateway.protocol.WrappedBusMessage;
import org.universAAL.ri.gateway.proxies.ProxyBusMember;
import org.universAAL.ri.gateway.proxies.ProxyPool;

/**
 * Representation of a one to one link between 2 ASGs. It is in charge of
 * connecting the communication layer with the Importer and proxies.
 *
 * @author amedrano
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public class Session implements MessageSender, MessageReceiver,
		OperationChainManager {

	public class SessionStatusEvent implements SessionEvent {
		private final Session session;
		private final SessionStatus old;
		private final SessionStatus current;

		public SessionStatusEvent(Session session, SessionStatus old,
				SessionStatus current) {
			this.session = session;
			this.old = old;
			this.current = current;
		}

		public Session getSession() {
			return session;
		}

		public SessionStatus getCurrentStatus() {
			return current;
		}

		public SessionStatus getOldStatus() {
			return old;
		}

		public String toString() {
			return this.getClass().getSimpleName() + "[" + session
					+ ": status from " + old + " to " + current + "]";
		}
	}

	public enum SessionStatus {
		/**
		 * This is the first status as soon session object is created. The valid
		 * next status are: CONNECTING or CLOSED
		 */
		OPENING,
		/**
		 * This status is reached as soon as network is active the session will
		 * try to connect, or when the connection was already active but the
		 * link used by the session brake. The valid next status are CONNECTED
		 * or CLOSED
		 */
		CONNECTING,
		/**
		 * The status is 100% up and running. The valid next status are CLOSED
		 * or CONNECTING
		 */
		CONNECTED,
		/**
		 * The session as been request to close and it is now closed. No other
		 * status are valid
		 */
		CLOSED

	}

	public static final Logger log = LoggerFactory.createLoggerFactory(
			Gateway.getInstance().context).getLogger(Session.class);

	private Importer importer;
	private ProxyPool pool;
	private final Configuration config;
	private String remoteScope;
	private AbstractSocketCommunicationHandler comunication;
	private final Cipher cipher;

	private SessionStatus state;
	private HashSet<SessionEventListener> listeners = new HashSet<SessionEventListener>();

	private Session(final Configuration config) {
		this.config = config;
		this.cipher = new Blowfish(config.getEncryptionKey());
		this.state = SessionStatus.OPENING;
	}

	public Session(final Configuration config, final ProxyPool proxyPool,
			final ServerSocketCommunicationHandler com) {
		this(config);
		this.pool = proxyPool;
		this.importer = new Importer(this, this.pool);

		if (config.getConnectionMode() != ConnectionMode.SERVER) {
			throw new IllegalStateException(
					"Configuration requires to run in Client mode, but we are creating the session as it was a Server");
		}
		this.comunication = com;
	}

	public Session(final Configuration config, final ProxyPool proxyPool) {
		this(config);
		this.pool = proxyPool;

		if (config.getConnectionMode() != ConnectionMode.CLIENT) {
			throw new UnsupportedOperationException(
					"Single session supports only the " + ConnectionMode.CLIENT);
		}
		comunication = new ClientSocketCommunicationHandler(config, this, this);
		try {
			comunication.start();
		} catch (final Exception e) {
			LogUtils.logError(Gateway.getInstance().context, getClass(),
					"Constructor", new String[] { "Unexpected Exceotion" }, e);
			throw new RuntimeException(e);
		}
	}

	public void setScope(final String scope) {
		validateRemoteScope(scope);
		this.remoteScope = scope;
	}

	public String getScope() {
		return remoteScope;
	}

	public Configuration getConfiguration() {
		return config;
	}

	public void send(final Message message) {
		validateRemoteScope(remoteScope);
		final org.universAAL.ri.gateway.communicator.service.Message content = new org.universAAL.ri.gateway.communicator.service.Message(
				message);
		final MessageWrapper wrap = new MessageWrapper(MessageType.HighPush,
				content, "");
		final SessionManager session = SessionManager.getInstance();
		/*
		 * //INFO Commented out for supporting but Client and Server mode UUID[]
		 * active = session.getSessionIds(); if (active.length != 1) { if
		 * (active.length == 0) { throw new IllegalStateException(
		 * "Trying to send a message but we no active session"); } else { throw
		 * new IllegalStateException(
		 * "Trying to send a message but we too many session"); } }
		 */
		try {
			comunication.sendMessage(wrap, new String[] { remoteScope });
		} catch (final Exception e) {
			throw new RuntimeException(
					"Failed to send message due to internal exception", e);
		}
	}

	private void validateRemoteScope(final String scope) {
		if (scope == null) {
			throw new IllegalStateException(
					"Scope cannot set be null, otherwise sending and rieving message will not work");
		}
		try {
			UUID.fromString(scope);
		} catch (final Exception e) {
			throw new IllegalStateException(
					"Scope "
							+ scope
							+ " is not a valid value we are expecting scope to be an UUID",
					e);
		}
	}

	public Message sendRequest(final Message message) {
		validateRemoteScope(remoteScope);
		final org.universAAL.ri.gateway.communicator.service.Message content = new org.universAAL.ri.gateway.communicator.service.Message(
				message);
		MessageWrapper wrap = new MessageWrapper(MessageType.HighReqRsp,
				content, "");
		final SessionManager session = SessionManager.getInstance();
		/*
		 * //INFO Commented out for supporting but Client and Server mode UUID[]
		 * active = session.getSessionIds(); if (active.length != 1) { if
		 * (active.length == 0) { throw new IllegalStateException(
		 * "Trying to send a message but we no active session"); } else { throw
		 * new IllegalStateException(
		 * "Trying to send a message but we too many session"); } }
		 */
		try {
			wrap = comunication.sendMessage(wrap, new String[] { remoteScope });
			if (wrap.getType() != MessageType.HighReqRsp) {
				throw new IllegalStateException(
						"Expecting HighReqRsp message, but recieved "
								+ wrap.getType());
			}
			return (Message) wrap.getMessage().getContent();
		} catch (final Exception e) {
			throw new RuntimeException(
					"Failed to send message due to internal exception", e);
		}
	}

	public ParameterCheckOpertaionChain getImportOperationChain() {
		return config.getImportOperationChain();
	}

	public ParameterCheckOpertaionChain getExportOperationChain() {
		return config.getExportOperationChain();
	}

	public MessageOperationChain getIncomingMessageOperationChain() {
		return config.getIncomingMessageOperationChain();
	}

	public MessageOperationChain getOutgoingMessageOperationChain() {
		return config.getOutgoingMessageOperationChain();
	}

	/**
	 * Receives the Message from the communication layer, delivering it to the
	 * appropriate subcomponent.
	 */
	public void handleMessage(final Message msg) {
		if (msg instanceof ImportMessage) {
			importer.handleImportMessage((ImportMessage) msg);
		} else if (msg instanceof WrappedBusMessage) {
			final WrappedBusMessage wbm = (WrappedBusMessage) msg;
			final ProxyBusMember pbm = pool.get(wbm
					.getRemoteProxyRegistrationId());
			if (pbm != null) {
				pbm.handleMessage(this, wbm);
			}
		} else if (msg instanceof ErrorMessage) {
			final ErrorMessage em = (ErrorMessage) msg;
			LogUtils.logError(Gateway.getInstance().context, getClass(),
					"handleMessage",
					"Received Error Message: " + em.getDescription());
		}
	}

	public Cipher getCipher() {
		return cipher;
	}

	public void stop() {
		if (config.getConnectionMode() != ConnectionMode.SERVER) {
			LogUtils.logInfo(
					Gateway.getInstance().context,
					getClass(),
					"stop",
					"Nothing todo when stopping Session created by the Server, clean up will be performed when closing the server");
		} else {
			LogUtils.logInfo(Gateway.getInstance().context, getClass(), "stop",
					"Closing client session");
			comunication.stop();
		}
	}

	/**
	 * Add a listener even it has not been already added
	 * 
	 * @param listener
	 *            the listener to add
	 * @return true if the listener has been registered
	 */
	public boolean addSessionEventListener(SessionEventListener listener) {
		boolean flag = this.listeners.add(listener);
		if (flag) {
			log.debug("Adding SessionEventListener " + listener.getName());
		}
		return flag;
	}

	/**
	 * Remove a listener from the listeners if and only if the listener has
	 * already been added
	 * 
	 * @param listener
	 *            the listener to remove
	 * @return true if the listener has been removed
	 */
	public boolean removeSessionEventListener(SessionEventListener listener) {
		boolean flag = this.listeners.remove(listener);
		if (flag) {
			log.debug("Removed SessionEventListener " + listener.getName());
		}
		return flag;
	}

	public void setStatus(SessionStatus status) {
		if (state == status) {
			return;
		}
		SessionStatusEvent e = new SessionStatusEvent(this, state, status);
		log.debug("Generated the new event " + e);
		notifySessionEventListeners(e);
	}

	private void notifySessionEventListeners(SessionStatusEvent e) {
		SessionEventListener[] notifyList = listeners
				.toArray(new SessionEventListener[] {});
		for (int i = 0; i < notifyList.length; i++) {
			try {
				notifyList[i].statusChange(e);
			} catch (Throwable ex) {
				log.error(
						"Failed to notify with success "
								+ notifyList[i].getName(), ex);
			}
		}
	}

	public SessionStatus getStatus() {
		return state;
	}

	/**
	 * @return
	 */
	public boolean isActive() {
		return state == SessionStatus.CONNECTED;
		// TODO Manage "activeness" within this class.
		// final SessionManager sm = SessionManager.getInstance();
		// UUID id = UUID.fromString(getScope());
		// return sm.isActive(id);
	}

}
