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

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.gateway.SessionEvent.SessionStatus;
import org.universAAL.ri.gateway.communication.cipher.SocketCipher;
import org.universAAL.ri.gateway.communicator.service.impl.AbstractSocketCommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.impl.ClientSocketCommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.impl.ServerSocketCommunicationHandler;
import org.universAAL.ri.gateway.configuration.Configuration;
import org.universAAL.ri.gateway.configuration.Configuration.ConnectionMode;
import org.universAAL.ri.gateway.log.Logger;
import org.universAAL.ri.gateway.log.LoggerFactory;
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
import org.universAAL.ri.gateway.utils.BufferedQueue;
import org.universAAL.ri.gateway.utils.CallSynchronizer;

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
		private final SessionEvent.SessionStatus old;
		private final SessionEvent.SessionStatus current;

		public SessionStatusEvent(final Session session,
				final SessionEvent.SessionStatus old,
				final SessionEvent.SessionStatus current) {
			this.session = session;
			this.old = old;
			this.current = current;
		}

		public Session getSession() {
			return session;
		}

		public SessionEvent.SessionStatus getCurrentStatus() {
			return current;
		}

		public SessionEvent.SessionStatus getOldStatus() {
			return old;
		}

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + "[" + session
					+ ": status from " + old + " to " + current + "]";
		}
	}

	private class MessageSynchronizer extends
			CallSynchronizer<Short, Message, Message> {

		/**
		 * @param timeout
		 */
		public MessageSynchronizer(long timeout) {
			super(timeout);
		}

		/**
		 *
		 */
		public MessageSynchronizer() {
			super();
		}

		/** {@ inheritDoc} */
		@Override
		protected void operate(Short callerID, Message input) {
			send(input);
		}

	}

	private class MessageQueueTask implements Runnable, SessionEventListener {

		/** {@inheritDoc} */
		public void run() {
			Session.this.addSessionEventListener(this);
			while (state != SessionStatus.CLOSED) {
				if (messagequeue.size() == 0) {
					synchronized (messagequeue) {
						try {
							messagequeue.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				if (isActive()) {
					Message m = messagequeue.peek();
					boolean sent = false;
					if (m != null) {
						try {
							sent = comunication.sendMessage(m, remoteScope);
						} catch (final Exception e) {
							log.error("Failed to send message due to internal exception, not trying again.", e);
							sent = true;
						}
					} else {
						log.warning("Detected null message in queue, Ignoring.");
						sent = true;
					}
					if (sent ) {
						messagequeue.poll();
					}
				} else {
					synchronized (messagequeue) {
						try {
							messagequeue.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}

		/** {@inheritDoc} */
		public void statusChange(SessionEvent se) {
			if (se.getCurrentStatus() == SessionStatus.CONNECTED) {
				synchronized (messagequeue) {
					messagequeue.notifyAll();
				}
			}

		}

		/** {@inheritDoc} */
		public String getName() {
			return "Session Message Queue";
		}

	}

	public static final Logger log = LoggerFactory.createLoggerFactory(
			Gateway.getInstance().context).getLogger(Session.class);

	private Importer importer;
	private ProxyPool pool;
	private final Configuration config;
	private String remoteScope;
	private AbstractSocketCommunicationHandler comunication;
	private final SocketCipher cipher;
	private CallSynchronizer<Short, Message, Message> synchronizer;
	private final Queue<Message> messagequeue;
	private Thread messagequeuetask;

	private SessionEvent.SessionStatus state;
	private final HashSet<SessionEventListener> listeners = new HashSet<SessionEventListener>();

	private SessionStatusEvent lastSesionStatusEvent;

	private Session(final Configuration config) {
		this.config = config;
		this.cipher = config.getCipher();
		this.state = SessionEvent.SessionStatus.OPENING;
		long timeout = getTimeout();
		if (timeout >= 0) {
			this.synchronizer = new MessageSynchronizer(timeout);
		} else {
			this.synchronizer = new MessageSynchronizer();
		}

		long qs = this.config.getMaxQueueSize();
		if (qs > 0) {
			messagequeue = new BufferedQueue<Message>(qs);
		} else {
			messagequeue = new ConcurrentLinkedQueue<Message>();
		}
		this.messagequeuetask = new Thread(new MessageQueueTask(), "Session_"
				+ config.toString() + "_MessageQueueTask");
		this.messagequeuetask.start();
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
		this.importer = new Importer(this, this.pool);

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

	/**
	 *
	 * @param scope
	 *            the {@link String} representing the scope of this Session. At
	 *            the moment the Scope is represented by the SpaceId of the
	 *            Space that we are connected to
	 */
	public void setScope(final String scope) {
		validateRemoteScope(scope);
		this.remoteScope = scope;
	}

	/**
	 *
	 * @return the {@link String} representing the scope of this Session. At the
	 *         moment the Scope is represented by the SpaceId of the Space that
	 *         we are connected to
	 */
	public String getScope() {
		return remoteScope;
	}

	public Configuration getConfiguration() {
		return config;
	}

	public void send(final Message message) {
		validateRemoteScope(remoteScope);
		// have a max queue length to not overflow when disconnected for long
		// time
		if (!messagequeue.offer(message)) {
			messagequeue.poll();
			messagequeue.offer(message);
		}
		;
		synchronized (messagequeue) {
			messagequeue.notify();
		}
	}

	private void validateRemoteScope(final String scope) {
		if (scope == null) {
			throw new IllegalStateException(
					"Scope cannot set be null, otherwise sending and rieving message will not work");
		}
		/*
		 * try { UUID.fromString(scope); } catch (final Exception e) { throw new
		 * IllegalStateException( "Scope " + scope +
		 * " is not a valid value we are expecting scope to be an UUID", e); }
		 */
	}

	public Message sendRequest(final Message message) throws TimeoutException {
		// validateRemoteScope(remoteScope);
		// final org.universAAL.ri.gateway.communicator.service.Message content
		// = new org.universAAL.ri.gateway.communicator.service.Message(
		// message);
		// MessageWrapper wrap = new MessageWrapper(MessageType.HighReqRsp,
		// content, "");
		// final SessionManager session = SessionManager.getInstance();
		// /*
		// * //INFO Commented out for supporting but Client and Server mode
		// UUID[]
		// * active = session.getSessionIds(); if (active.length != 1) { if
		// * (active.length == 0) { throw new IllegalStateException(
		// * "Trying to send a message but we no active session"); } else {
		// throw
		// * new IllegalStateException(
		// * "Trying to send a message but we too many session"); } }
		// */
		// try {
		// wrap = comunication.sendMessage(wrap, new String[] { remoteScope });
		// if (wrap.getType() != MessageType.HighReqRsp) {
		// throw new IllegalStateException(
		// "Expecting HighReqRsp message, but recieved "
		// + wrap.getType());
		// }
		// return (Message) wrap.getMessage().getContent();
		// } catch (final Exception e) {
		// throw new RuntimeException(
		// "Failed to send message due to internal exception", e);
		// }
		try {
			return synchronizer.performCall(message.getSequence(), message);
		} catch (InterruptedException e) {
			// Just interrupted probably because response is cancelled.
			throw new RuntimeException("Request Interrupted", e);
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
		if (msg.isResponse()) {
			synchronizer.performResponse(msg.getInResponseTo(), msg);
		} else if (msg instanceof ImportMessage) {
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

	public SocketCipher getCipher() {
		return cipher;
	}

	public void stop() {
		synchronizer.purge();
		if (config.getConnectionMode() == ConnectionMode.SERVER) {
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
	public synchronized boolean addSessionEventListener(
			final SessionEventListener listener) {
		final boolean flag = this.listeners.add(listener);
		if (flag) {
			log.debug("Adding SessionEventListener " + listener.getName());
			if (lastSesionStatusEvent != null) {
				listener.statusChange(lastSesionStatusEvent);
			}
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
	public synchronized boolean removeSessionEventListener(
			final SessionEventListener listener) {
		final boolean flag = this.listeners.remove(listener);
		if (flag) {
			log.debug("Removed SessionEventListener " + listener.getName());
		}
		return flag;
	}

	public synchronized void setStatus(final SessionEvent.SessionStatus status) {
		if (state == status) {
			return;
		}
		lastSesionStatusEvent = new SessionStatusEvent(this, state, status);
		log.debug("Generated the new event " + lastSesionStatusEvent);
		state = status;
		notifySessionEventListeners(lastSesionStatusEvent);
	}

	private void notifySessionEventListeners(final SessionStatusEvent e) {
		final SessionEventListener[] notifyList = listeners
				.toArray(new SessionEventListener[] {});
		for (int i = 0; i < notifyList.length; i++) {
			try {
				notifyList[i].statusChange(e);
			} catch (final Throwable ex) {
				log.error(
						"Failed to notify with success "
								+ notifyList[i].getName(), ex);
			}
		}
	}

	public SessionEvent.SessionStatus getStatus() {
		return state;
	}

	/**
	 * @return
	 */
	public boolean isActive() {
		return state == SessionEvent.SessionStatus.CONNECTED;
	}

	/**
	 * To be called when the session is to be closed. <br>
	 * Removes all associations in all proxies with the session, and closes
	 * those left orphan.
	 *
	 * @param session
	 */
	public void removeImports() {
		final Collection<ProxyBusMember> proxies = importer.getImports();
		for (final ProxyBusMember p : proxies) {
			p.removeRemoteProxyReferences(this);
			pool.removeProxyIfOrphan(p);
		}
	}

	/**
	 * Gets timeout configuration for session.
	 *
	 * @return negative infinite timeout, timeout in milisec IOC
	 */
	public long getTimeout() {
		return config.getTimeout();
	}

	/**
	 * Get the configuration that states that the gateway should cache messages
	 * before actual connection.
	 *
	 * @return true IFF the messages should be cached before connection
	 */
	public boolean getCacheBeforeConnect() {
		return config.getCacheBeforeConnect();
	}

}
