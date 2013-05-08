package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.UIResponse;
import org.universAAL.ri.gateway.communicator.service.CommunicationException;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.communicator.service.ResponseCallback;
import org.universAAL.ri.gateway.eimanager.ExportManager;
import org.universAAL.ri.gateway.eimanager.ImportEntry;
import org.universAAL.ri.gateway.eimanager.ImportManager;
import org.universAAL.ri.gateway.eimanager.exception.InterruptExecutionException;
import org.universAAL.ri.gateway.eimanager.impl.BusMemberType;
import org.universAAL.ri.gateway.eimanager.impl.EIOperationManager;
import org.universAAL.ri.gateway.eimanager.impl.exporting.ProxyRegistration;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

/**
 * AALSpace Gateway Communicator implementation. Registers one OSGi service to
 * be used by ImportExportManager, one HTTP service for communication with other
 * communicators. Uses ImportExportManager's OSGi service for request/response
 * delegation.
 * 
 * @author skallz
 * 
 */
public class GatewayCommunicatorImpl implements GatewayCommunicator {

	/**
     * 
     */
	private static final long serialVersionUID = 7119632127833531787L;

	/**
	 * Callback registry for asynchronous communication.
	 */
	private final Map<UUID, ResponseCallback> callbacks;

	/**
	 * A reference to ImportManager's used for handling responses from
	 * invocations of remote services etc.
	 */
	private volatile ImportManager importManager;

	/**
	 * A reference to ExportManager used for handling requests from remote
	 * spaces.
	 */
	private volatile ExportManager exportManager;

	/**
	 * Executor used for spawning threads during timed invocation.
	 */
	private final Executor executor = Executors.newCachedThreadPool();

	private List<GatewayAddress> remoteGateways;

	private CommunicationHandler commHandler;

	/**
	 * Initializes the worker with given ImportExportManager reference.
	 * 
	 * @param space
	 *            ImportExportManager reference for request/response delegation
	 * @throws Exception
	 */
	public GatewayCommunicatorImpl() throws Exception {
		callbacks = Collections
				.synchronizedMap(new HashMap<UUID, ResponseCallback>());
		remoteGateways = Collections
				.synchronizedList(new ArrayList<GatewayAddress>());
		commHandler = new SocketCommunicationHandler(this);
	}

	public void addRemoteGateway(final GatewayAddress gwAddrToAdd) {
		remoteGateways.add(gwAddrToAdd);
	}

	public void removeRemoteGateway(final GatewayAddress gwAddrToDelete) {
		remoteGateways.remove(gwAddrToDelete);
	}

	public void addRemoteGateways(
			final Collection<GatewayAddress> gwAddressesToAdd) {
		remoteGateways.addAll(gwAddressesToAdd);
	}

	public void setManagers(final ImportManager importManager,
			final ExportManager exportManager) {
		this.importManager = importManager;
		this.exportManager = exportManager;
	}

	private URL[] getRemoteURLs() {
		URL[] uris = new URL[remoteGateways.size()];
		for (int i = 0; i < remoteGateways.size(); i++) {
			uris[i] = remoteGateways.get(i).getUrl();
		}
		return uris;
	}

	/**
	 * Sends a context event to other AALSpace Gateway Communicators listening
	 * at given URL.
	 * 
	 * @param message
	 *            context event to be sent
	 * @param to
	 *            a list of URLs of remote communicators to which the event
	 *            should be delivered
	 */
	public void sendContextEvent(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		MessageWrapper wrap = new MessageWrapper(MessageType.Context, message,
				"");
		for (URL url : to) {
			sendMessage(wrap, url);
		}
	}

	/**
	 * Sends a ui call to other AALSpace Gateway Communicators listening at
	 * given URL.
	 * 
	 * @param message
	 *            context event to be sent
	 * @param to
	 *            a list of URLs of remote communicators to which the event
	 *            should be delivered
	 */
	public void sendUIRequest(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		MessageWrapper wrap = new MessageWrapper(MessageType.UI, message, "");
		for (URL url : to) {
			sendMessage(wrap, url);
		}
	}

	/**
	 * Sends a ui response to other AALSpace Gateway Communicators listening at
	 * given URL.
	 * 
	 * @param message
	 *            ui response
	 * @param to
	 *            a list of URLs of remote communicators to which the event
	 *            should be delivered
	 */
	public void sendUIResponse(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		MessageWrapper wrap = new MessageWrapper(MessageType.UIResponse,
				message, "");
		for (URL url : to) {
			sendMessage(wrap, url);
		}
	}

	/**
	 * Sends a service request to another AALSpace Gateway Communicator
	 * listening at given URL, waits for the response and returns it.
	 * 
	 * Equal to sendServiceRequest(message, to, 0);
	 * 
	 * @param message
	 *            request massage to be sent to the remote communicator
	 * @param to
	 *            remote communicator's URL
	 * @return response message
	 */
	public Message[] sendServiceRequest(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		Message[] returnedValues = new Message[to.length];
		for (int i = 0; i < to.length; i++) {
			MessageWrapper wrapReq = new MessageWrapper(
					MessageType.ServiceRequest, message, "");
			MessageWrapper wrapResp = sendMessage(wrapReq, to[i]);
			returnedValues[i] = wrapResp.getMessage();
		}
		return returnedValues;
	}

	public Message sendServiceRequest(final Message message, final URL to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		MessageWrapper wrapReq = new MessageWrapper(MessageType.ServiceRequest,
				message, "");
		MessageWrapper wrapResp = sendMessage(wrapReq, to);
		return wrapResp.getMessage();
	}

	/**
	 * Sends a service request to another AALSpace Gateway Communicator
	 * listening at given URL, waits for the response and returns it if arrived
	 * before timing out.
	 * 
	 * @param message
	 *            request massage to be sent to the remote communicator
	 * @param to
	 *            remote communicator's URL
	 * @param timeout
	 *            time in milliseconds to wait for the response
	 * @return response message
	 * @throws TimeoutException
	 *             when timed out
	 */
	public Message[] sendServiceRequest(final Message message, final URL[] to,
			final long timeout) throws TimeoutException {
		if (to == null || message == null || timeout < 0) {
			throw new IllegalArgumentException();
		}
		if (timeout == 0) {
			return sendServiceRequest(message, to);
		}

		Message[] returnedValues = new Message[to.length];
		for (int i = 0; i < to.length; i++) {
			final int index = i;
			FutureTask<Message> task = new FutureTask<Message>(
					new Callable<Message>() {
						public Message call() throws Exception {
							return sendServiceRequest(message, to[index]);
						}
					});
			executor.execute(task);
			try {
				while (true) {
					try {
						returnedValues[i] = task.get(timeout,
								TimeUnit.MILLISECONDS);
						break;
					} catch (InterruptedException e) {
						// ignore interruption and keep waiting
						continue;
					}
				}
			} catch (ExecutionException ex) {
				throw new CommunicationException(ex);
			}
		}
		return returnedValues;
	}

	/**
	 * Sends a service request to another AALSpace Gateway Communicator
	 * listening at given URL, registers callback which will be notified once
	 * the response arrives.
	 * 
	 * @param message
	 *            request massage to be sent to the remote communicator
	 * @param returnTo
	 *            local communicator's URL to send back the response to
	 * @param to
	 *            remote communicator's URL
	 * @param callback
	 *            callback which will be notified once the response arrives
	 */
	public void sendServiceRequestAsync(final Message message,
			final URL returnTo, final URL to, final ResponseCallback callback) {
		if (to == null || message == null || returnTo == null
				|| callback == null) {
			throw new IllegalArgumentException();
		}

		// sending the message
		MessageWrapper wrapReq = new MessageWrapper(
				MessageType.ServiceRequestAsync, message, returnTo, "");
		callbacks.put(wrapReq.getId(), callback);
		MessageWrapper wrapResp = sendMessage(wrapReq, to);
		if (wrapResp != null) {
			// there should be no response
			throw new CommunicationException("protocol failure");
		}
	}

	/**
	 * Sends a prepared wrapped to specified URL and receives a wrapper as a
	 * response if any.
	 * 
	 * @param w
	 *            the message
	 * @param to
	 *            where to send the message
	 * @return wrapper with the response or null of nothing sent back
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private MessageWrapper sendMessage(final MessageWrapper w, final URL to) {
		if (w == null || to == null) {
			throw new IllegalArgumentException();
		}

		logInfo("sending message: %s to %s", w, to);
		MessageWrapper resp = null;
		try {
			resp = commHandler.sendMessage(w, to);
		} catch (IOException e) {
			throw new CommunicationException(e);
		} catch (ClassNotFoundException e) {
			throw new CommunicationException(e);
		} catch (CryptoException e) {
			throw new CommunicationException(e);
		}
		return resp;
	}

	/**
	 * Util logging function.
	 * 
	 * @param format
	 *            format
	 * @param args
	 *            args
	 */
	private static void logInfo(final String format, final Object... args) {
		String callingMethod = Thread.currentThread().getStackTrace()[2]
				.getMethodName();
		System.out.format("[%s] %s%n", callingMethod,
				String.format(format, args));
		// LogUtils.logInfo(Activator.mc, Activator.class.getClass(),
		// callingMethod.getMethodName(),
		// new Object[] { String.format(format, args) }, null);
	}

	public Message[] sendImportRequest(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		Message[] resultsValue = new Message[to.length];
		MessageWrapper wrap = new MessageWrapper(MessageType.ImportRequest,
				message, "");
		for (int i = 0; i < to.length; i++) {
			MessageWrapper wrapper = sendMessage(wrap, to[i]);
			if (wrapper == null){
				throw new CommunicationException("Import from " + to[i].toString() + " resulted in null value MessageWrapper object. Is hash-key property the same in both spaces???");
			}
			resultsValue[i] = wrapper.getMessage();
		}
		return resultsValue;
	}

	public void sendImportRefresh(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		MessageWrapper wrap = new MessageWrapper(MessageType.ImportRefresh,
				message, "");
		for (URL url : to) {
			sendMessage(wrap, url);
		}
	}

	public void sendImportRemoval(final Message message, final URL[] to) {
		if (to == null || message == null) {
			throw new IllegalArgumentException();
		}
		MessageWrapper wrap = new MessageWrapper(MessageType.ImportRemoval,
				message, "");
		for (URL url : to) {
			sendMessage(wrap, url);
		}
	}

	public Message[] sendServiceRequest(final Message message) {
		return this.sendServiceRequest(message, getRemoteURLs());
	}

	public Message[] sendServiceRequest(final Message message,
			final long timeout) throws TimeoutException {
		return this.sendServiceRequest(message, getRemoteURLs(), timeout);
	}

	public void sendContextEvent(final Message message) {
		this.sendContextEvent(message, getRemoteURLs());
	}

	public void sendUIResponse(final Message message) {
		this.sendUIResponse(message, getRemoteURLs());
	}

	public void sendUIRequest(final Message message) {
		this.sendUIRequest(message, getRemoteURLs());
	}

	public Message sendImportRequest(final Message message) {
		return this.sendImportRequest(message, getRemoteURLs())[0];
	}

	public void sendImportRefresh(final Message message) {
		this.sendImportRefresh(message, getRemoteURLs());
	}

	public void sendImportRemoval(final Message message) {
		this.sendImportRemoval(message, getRemoteURLs());
	}

	public void handleMessage(final InputStream in, final OutputStream out) {
		MessageWrapper wrapOut = null;
		MessageWrapper wrapIn = null;
		try{
			logInfo("handleMessage");
			wrapIn = Serializer.unmarshalMessage(in);
			logInfo("handleMessage: type: %s", wrapIn.getType());

			switch (wrapIn.getType()) {
			case ImportRequest:
				ImportRequest request = Serializer.Instance.unmarshall(
						ImportRequest.class, wrapIn.getMessage());
				
				EIOperationManager.Type type = null;
				switch (BusMemberType.valueOf(request.getMember())) {
				case ServiceCaller:
					type = EIOperationManager.Type.Service;
					break;
				case ContextSubscriber:
					type = EIOperationManager.Type.Context;
					break;
				case UICaller:
					type = EIOperationManager.Type.UI;
					break;
				}
				
				try {
					EIOperationManager.Instance.executeExportOperationChain(request, type);
				} catch (InterruptExecutionException e) {
					ProxyRegistration errorRegistration = new ProxyRegistration(e.getMessage());
					wrapOut = new MessageWrapper(MessageType.ImportResponse,
							Serializer.Instance.marshall(errorRegistration),
							wrapIn.getId(), "");
					logInfo("Sending ImportResponse with failed interceptor execution: %s", errorRegistration);
					Serializer.sendMessageToStream(wrapOut, out);
					return;
				}
				
				logInfo("Got ImportRequest: %s", request);
				ProxyRegistration registration = this.exportManager
						.registerProxies(request);
				
				
				String[] serialized = null;
				
				switch (BusMemberType.valueOf(request.getMember())) {
				case ServiceCaller:
					Map<String, List<ServiceProfile>> profiles = (Map<String, List<ServiceProfile>>) registration
							.getReturnedValues();
					if (profiles != null) {
						logInfo("Profiles count: %d", profiles.values().size());
					}
					
					Map<String, List<String>> serializedMap = new HashMap<String, List<String>>();
					
					for(String key: profiles.keySet()){
						if (serializedMap.get(key) == null){
							serializedMap.put(key, new ArrayList<String>());
						}
						for(ServiceProfile p : profiles.get(key)){
							serializedMap.get(key).add((String) Serializer.Instance
									.marshallObject(p).getContent());
						}
					}
					
					/*serialized = new String[profiles.values().size()+2*profiles.keySet().s];
					
					
					for (int i = 2; i < profiles.values().size().length + 2; i++) {
						serialized[i] = (String) Serializer.Instance
								.marshallObject(profiles[i]).getContent();
					}*/
					registration.setReturnedValues(serializedMap);
					wrapOut = new MessageWrapper(MessageType.ImportResponse,
							Serializer.Instance.marshall(registration),
							wrapIn.getId(), "");
					logInfo("Sending ImportResponse: %s", registration);
					Serializer.sendMessageToStream(wrapOut, out);
					break;
				case ContextSubscriber:
					ContextEventPattern[] cpe = (ContextEventPattern[]) registration
							.getReturnedValues();
					serialized = new String[cpe.length];
					System.out.println("Export sent:");
					for (int i = 0; i < cpe.length; i++) {
						serialized[i] = (String) Serializer.Instance
								.marshallObject(cpe[i]).getContent();
						// System.out.println(serialized[i]);
						// ContextEventPattern deserialized =
						// Serializer.Instance.unmarshallObject(ContextEventPattern.class,
						// serialized[i]);
						// System.out.println(deserialized.toStringRecursive());
					}
					if (cpe != null) {
						logInfo("ContextEventPattern count: %d", cpe.length);
					}
					// TODO because of BUG during deserialization of context
					// event we do not register
					// proxypublisher in local space
					registration.setReturnedValues(new String[] {});
					// registration.setReturnedValues(serialized);

					wrapOut = new MessageWrapper(MessageType.ImportResponse,
							Serializer.Instance.marshall(registration),
							wrapIn.getId(), "");
					logInfo("Sending ImportResponse: %s", "");
					Serializer.sendMessageToStream(wrapOut, out);
					break;
					
				case UICaller:
					throw new Exception("Not yet implemented");
				}
				break;
			case ImportRefresh:
				this.importManager.refreshProxy(Serializer.Instance.unmarshall(
						ProxyRegistration.class, wrapIn.getMessage()));
				break;
			case ImportRemoval:
				this.exportManager.unregisterProxies(Serializer.Instance
						.unmarshall(ImportRequest.class, wrapIn.getMessage()));
				break;
			case ServiceRequest:
				// send the request to the bus
				ServiceResponse response = this.exportManager
						.sendServiceRequest(wrapIn.getMessage()
								.getRemoteProxyRegistrationId(),
								Serializer.Instance.unmarshallObject(
										ServiceCall.class,
										wrapIn.getMessage()),wrapIn.getMessage()
										.getRemoteMemberId());
				// wrapper for the response message
				wrapOut = new MessageWrapper(MessageType.ServiceResponseAsync,
						Serializer.Instance.marshallObject(response),
						wrapIn.getId(), "");
				// send back the response
				logInfo("sending back the response: %s", wrapOut);
				Serializer.sendMessageToStream(wrapOut, out);
				break;
			case ServiceRequestAsync:
//				DEPRECATED
//				Runnable task = new Runnable() {
//					public void run() {
//						// send the request to the bus
//						ServiceResponse response = null;
//						try {
//							response = exportManager.sendServiceRequest(wrapIn
//									.getSourceId(), Serializer.Instance
//									.unmarshallObject(ServiceRequest.class,
//											wrapIn.getMessage()));
//
//							// wrapper for the response message
//							MessageWrapper wrapOut = new MessageWrapper(
//									MessageType.ServiceResponseAsync,
//									Serializer.Instance
//											.marshallObject(response),
//									wrapIn.getId(), "");
//							// send back the response
//							logInfo("sending back the response: %s", wrapOut);
//							sendMessage(wrapOut, wrapIn.getReturnTo());
//						} catch (Exception e) {
//							logInfo("ERROR: %s", e);
//							throw new RuntimeException(e);
//						}
//					}
//				};
//				executor.execute(task);
				break;
			case UI:
				this.exportManager.sendUIRequest(wrapIn.getSourceId(),
						Serializer.Instance.unmarshallObject(UIRequest.class,
								wrapIn.getMessage()));
				logInfo("published ui request to the bus: %s", wrapIn);
				break;
			case ServiceResponseAsync:
				ResponseCallback call = callbacks.get(wrapIn.getId());
				if (call == null) {
					throw new Exception("couldn't find callback");
				}
				call.collectResponse(wrapIn.getMessage());
				break;
			case Context:
				ContextEvent remoteContextEvent = Serializer.Instance
						.unmarshallObject(ContextEvent.class,
								wrapIn.getMessage());
				String targetId = wrapIn.getMessage()
						.getRemoteProxyRegistrationId();
				this.importManager.sendContextEvent(targetId,
						remoteContextEvent);
				break;

			case UIResponse:
				// send the request to the bus
				this.importManager.sendUIResponse(wrapIn.getSourceId(),
						Serializer.Instance.unmarshallObject(UIResponse.class,
								wrapIn.getMessage()));
				logInfo("published ui request to the bus: %s", wrapIn);
				break;
			default:
				throw new UnsupportedOperationException();
			}
		} catch (Exception ex) {
			logInfo("ERROR: %s", ex);
			ex.printStackTrace();
			try{
				wrapOut = new MessageWrapper(MessageType.Error,
						Serializer.Instance.marshallObject(ex.getMessage()),
						UUID.randomUUID(), "");
				if (wrapIn != null){
					sendMessage(wrapOut, wrapIn.getReturnTo());
				}else{
					Serializer.sendMessageToStream(wrapOut, out);
				}
			}catch(Exception e){
				//intentionally skipped
			}
			
		}
	}

	public void stop() {
		commHandler.stop();
	}

	public void start() throws Exception {
		commHandler.start();
	}

}
