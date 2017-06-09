/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.ri.api.manager;

/**
 * Interface that defines the methods of the Remote API, that will be available
 * through the server HTTP endpoint, according to the R-API documentation.
 * 
 * @author alfiva
 * 
 */
public interface RemoteAPI {
	/**
	 * Value for REGISTER method parameter in the HTTP request
	 */
	public static final String METHOD_REGISTER = "REGISTER";
	/**
	 * Value for SENDC method parameter in the HTTP request
	 */
	public static final String METHOD_SENDC = "SENDC";
	/**
	 * Value for SUBSCRIBEC method parameter in the HTTP request
	 */
	public static final String METHOD_SUBSCRIBEC = "SUBSCRIBEC";
	/**
	 * Value for CALLS method parameter in the HTTP request
	 */
	public static final String METHOD_CALLS = "CALLS";
	/**
	 * Value for PROVIDES method parameter in the HTTP request
	 */
	public static final String METHOD_PROVIDES = "PROVIDES";
	/**
	 * Value for UNREGISTER method parameter in the HTTP request
	 */
	public static final String METHOD_UNREGISTER = "UNREGISTER";
	/**
	 * Value for RESPONSE method parameter in the HTTP request
	 */
	public static final String METHOD_RESPONSES = "RESPONSES";
	/**
	 * Parameter in the HTTP request for the authorization identification
	 */
	public static final String KEY_AUTH = "auth";
	/**
	 * Parameter in the HTTP request for the method identification
	 */
	public static final String KEY_METHOD = "method";
	/**
	 * Parameter in the HTTP request for the main parameter of the method
	 */
	public static final String KEY_PARAM = "param";
	/**
	 * Parameter in the HTTP request (optional) for the version of the client
	 */
	public static final String KEY_VERSION = "v";
	/**
	 * Parameter in the HTTP request for the identification of the addressee
	 */
	public static final String KEY_TO = "to";
	/**
	 * Parameter in a HTTP response for a ServiceCall status
	 */
	public static final String KEY_STATUS = "status";
	/**
	 * Parameter in a HTTP response for a ServiceCall URI
	 */
	public static final String KEY_CALL = "call";
	/**
	 * Delimiter in a HTTP response to indicate the serialized ontological
	 * result in turtle starts in the next line
	 */
	public static final String FLAG_TURTLE = "TURTLE";
	/**
	 * Identifier for remote endpoints using HTTP POST
	 */
	public static final int REMOTE_POST = 0;
	/**
	 * Identifier for remote endpoints using Google Cloud Messaging
	 */
	public static final int REMOTE_GCM = 1;
	/**
	 * Identifier for remote endpoints using unknown protocols
	 */
	public static final int REMOTE_UNKNOWN = -1;

	/**
	 * Register a client remote node in the server.
	 * <p>
	 * This method must be called once before any other method. Subsequent calls
	 * will only update the remote endpoint reference, not restart the
	 * registration.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @param remote
	 *            Identifies the client endpoint where the push responses and
	 *            callbacks will be sent by the server
	 * @return The encryption key used to encrypt GCM messages, or null if this
	 *         does not apply
	 * @throws Exception
	 *             If any problem arose during the registration and it was not
	 *             successful
	 */
	String register(String id, String remote) throws Exception;

	/**
	 * Sends a Context Event.
	 * <p>
	 * The server will automatically create a Context Publisher if this is the
	 * first time the method is called. If the passed event contains
	 * ContextProvider information, it is used to instantiate the Publisher.
	 * Otherwise the missing information is automatically generated: The
	 * Publisher is described as either a Gauge or a Controller depending on if
	 * you are providing Services (you called method PROVIDES before). The URI
	 * of the Provider is then set to
	 * <i>http://ontology.universAAL.org/SimpleUAAL
	 * .owl#ContextEventsProvider</i> and it cannot be changed. The pattern that
	 * describes the provided events of this provider is empty, which means it
	 * can publish any type of event. Notice this Provider info is set ONLY THE
	 * FIRST TIME and cannot be changed by later calls.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @param cevent
	 *            The serialized ContextEvent
	 * @throws Exception
	 *             If it was not possible to send the event
	 */
	void sendC(String id, String cevent) throws Exception;

	/**
	 * Subscribes for Context Events.
	 * <p>
	 * The server will automatically create a new Context Subscriber each time
	 * this method is called. All Context Events that match the pattern will be
	 * sent back to the client endpoint. Since every call to this method creates
	 * a new subscriber, you should be careful with how many times you call this
	 * method. Usually, a limited number of subscriptions is needed. Remember
	 * that you can combine different patterns to be handled by a single
	 * subscriber.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @param cpattern
	 *            The serialized ContextEventPattern
	 * @throws Exception
	 *             If it was not possible to subscribe to the events
	 */
	void subscribeC(String id, String cpattern) throws Exception;

	/**
	 * Calls a Service with a Service Request.
	 * <p>
	 * The server will automatically create an internal Default Service Caller
	 * if this is the first time the method is called. The call to the service
	 * is synchronous: this method returns the response of the call straight
	 * from Service Bus.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @param srequest
	 *            The serialized ServiceRequest
	 * @return The serialized ServiceResponse
	 * @throws Exception
	 *             If it was not possible to issue the request
	 */
	String callS(String id, String srequest) throws Exception;

	/**
	 * Registers a Service Profile.
	 * <p>
	 * The server will automatically create a new Service Callee each time this
	 * method is called. All ServiceCalls addressed to this profile will be sent
	 * back to the client endpoint. Since every call to this method creates a
	 * new callee, you should be careful with how many times you call this
	 * method. Usually, a limited number of callees is needed.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @param sprofile
	 *            The serialized ServiceProfile
	 * @throws Exception
	 *             If it was not possible to register the profile
	 */
	void provideS(String id, String sprofile) throws Exception;

	/**
	 * Unregister a client remote node in the server.
	 * <p>
	 * It frees all associated resources allocated to the client by previous
	 * calls. No callbacks will be sent to the client endpoint after this, and
	 * no further method calls will be allowed.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @throws Exception
	 *             If it was not possible to unregister the client
	 */
	void unregister(String id) throws Exception;

	/**
	 * Unregister all the client remote node in the server.
	 * <p>
	 * This is only for use when the Remote API module is shutting down. It
	 * frees all associated resources allocated to the all client by previous
	 * calls. No callbacks will be sent to the client endpoint after this, and
	 * no further method calls will be allowed. This avoids uAAL wrappers being
	 * kept running in the MW when there is no one to answer to them. Unlike
	 * unregister(), it does not throw Exception: if a node fails to be
	 * unregistered, an error is logged and it continues with the rest.
	 * 
	 */
	void unregisterAll();

	/**
	 * Get the encryption key to use to encrypt messages via GCM if proceeds.
	 * 
	 * @param id
	 *            The token that uniquely identifies the client remote node
	 * @return The encryption key or null if it does not apply.
	 */
	String getCryptKey(String id);
}
