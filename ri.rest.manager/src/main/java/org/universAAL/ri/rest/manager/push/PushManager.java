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
package org.universAAL.ri.rest.manager.push;

import java.net.MalformedURLException;
import java.net.URL;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.ServiceCall;

/**
 * Class that manages the push of callbacks to client remote node endpoints.
 *
 * @author alfiva
 *
 */
public class PushManager {

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

	public static void pushContextEvent(String callback, String id, ContextEvent event) throws PushException {
		switch (determineEndpoint(callback)) {
		case REMOTE_POST:
			PushREST.pushContextEvent(callback, event);
			break;
		case REMOTE_GCM:
			PushGCM.pushContextEvent(callback, id, event);
			break;
		default:
			throw new PushException("Unable to determine protocol of remote endpoint");
		}
	}

	public static void pushServiceCall(String callback, String id, ServiceCall call, String origin)
			throws PushException {
		switch (determineEndpoint(callback)) {
		case REMOTE_POST:
			PushREST.pushServiceCall(callback, call, origin);
			break;
		case REMOTE_GCM:
			PushGCM.pushServiceCall(callback, id, call, origin);
			break;
		default:
			throw new PushException("Unable to determine protocol of remote endpoint");
		}
	}

	/**
	 * Determines the type of endpoint
	 *
	 * @param remote
	 *            String representation of the endpoint
	 * @return RemoteAPI.REMOTE_POST if the endpoint is http or https
	 *         <p>
	 *         RemoteAPI.REMOTE_GCM if the endpoint is an Android GCM key
	 *         <p>
	 *         RemoteAPI.REMOTE_UNKNOWN if anything else
	 */
	public static int determineEndpoint(String remote) {
		if (remote==null || remote.isEmpty() || remote.equals("null")){
			return REMOTE_UNKNOWN;
		}
		try {
			URL attempt = new URL(remote);
			if (attempt.getProtocol().toLowerCase().startsWith("http")) {
				return REMOTE_POST;
			} else {
				// Non http/https URL
				return REMOTE_UNKNOWN;
			}
		} catch (MalformedURLException e) {
			// Assume that if it is not a URL it is a GCM key
			return REMOTE_GCM;
		}
	}

}
