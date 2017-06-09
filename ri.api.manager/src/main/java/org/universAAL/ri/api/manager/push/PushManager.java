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
package org.universAAL.ri.api.manager.push;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ri.api.manager.Configuration;
import org.universAAL.ri.api.manager.RemoteAPI;
import org.universAAL.ri.api.manager.exceptions.PushException;

/**
 * Class that manages the push of callbacks to client remote node endpoints.
 * 
 * @author alfiva
 * 
 */
public class PushManager {

	/**
	 * Build a Context Event callback message and send it to the client remote
	 * node endpoint.
	 * 
	 * @param remoteid
	 *            The client remote node endpoint
	 * @param event
	 *            The serialized Context Event to send
	 * @param toURI
	 */
	public static void sendC(String nodeid, String remoteid, ContextEvent event, String toURI) throws PushException {
		switch (Configuration.determineEndpoint(remoteid)) {
		case RemoteAPI.REMOTE_POST:
			PushHTTP.sendC(remoteid, event, toURI);
			break;
		case RemoteAPI.REMOTE_GCM:
			PushGCM.sendC(nodeid, remoteid, event, toURI);
			break;
		default:
			throw new PushException("Unable to determine protocol of remote endpoint");
		}
	}

	/**
	 * Build a ServiceCall callback message and send it to the client remote
	 * node endpoint.
	 * 
	 * @param remoteid
	 *            The client remote node endpoint
	 * @param call
	 *            The serialized Service Call to send
	 * @param toURI
	 * @return The Service Response that the client remote node will have sent
	 *         as response to the callback
	 */
	public static ServiceResponse callS(String nodeid, String remoteid, ServiceCall call, String toURI)
			throws PushException {
		switch (Configuration.determineEndpoint(remoteid)) {
		case RemoteAPI.REMOTE_POST:
			return PushHTTP.callS(remoteid, call, toURI);
		case RemoteAPI.REMOTE_GCM:
			return PushGCM.callS(nodeid, remoteid, call, toURI);
		default:
			throw new PushException("Unable to determine protocol of remote endpoint");
		}
	}

}
