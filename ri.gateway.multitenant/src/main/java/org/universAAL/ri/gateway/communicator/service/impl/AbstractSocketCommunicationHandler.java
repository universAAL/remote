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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.communication.cipher.Cipher;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.CommunicationHelper;
import org.universAAL.ri.gateway.log.Logger;
import org.universAAL.ri.gateway.log.LoggerFactory;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * This class implements an abstract gateway based on TCP connection
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-08-08 14:46:31 +0200
 *          (Fri, 08 Aug 2014) $)
 * 
 */
public abstract class AbstractSocketCommunicationHandler implements
		CommunicationHandler {

	public static final Logger log = LoggerFactory.createLoggerFactory(
			Gateway.getInstance().context).getLogger(
			AbstractSocketCommunicationHandler.class);
	protected Cipher cipher;

	public AbstractSocketCommunicationHandler(final Cipher cipher) {
		this.cipher = cipher;
	}

	protected Message readMessage(final InputStream in) throws Exception {
		AbstractSocketCommunicationHandler.log
				.debug("Reading a message on the link");
		final Message msg = CommunicationHelper.readAndDecypher(in, cipher);
		AbstractSocketCommunicationHandler.log.debug("Read message " + msg
				+ " going to handle it");
		return msg;
	}

	public boolean sendMessage(final Message msg, final String scope) {

		final SessionManager refSM = SessionManager.getInstance();

		final List<UUID> targetLinks;

		log.debug("Sending a messages as multicast or unicast");
		targetLinks = new ArrayList<UUID>();
		UUID[] sessions = refSM.getSessionIds();

		for (int i = 0; i < sessions.length; i++) {
			String spaceId = refSM.getSpaceIdFromSession(sessions[i]);
			if (scope.equals(spaceId)) {
				targetLinks.add(sessions[i]);
			}
		}
		log.debug("Found the following target "
				+ Arrays.toString(targetLinks.toArray())
				+ " for the message that had the following scopes " + scope);
		for (UUID link : targetLinks) {
			if (refSM.isActive(link) == false) {
				/*
				 * The session is not active so we are not sending to it
				 */
				log.warning("The selected session " + link
						+ " is UNACTIVE so no message will be sent to it");
				return false;
			}
			final OutputStream out = refSM.getOutputStream(link);
			final InputStream in = refSM.getInputStream(link);

			if (out == null || in == null) {
				// log that we found an invalid-session
				log.error("The selected session " + link
						+ " is INVALID so no message will be sent to it", null);
				return false;
			}

			try {
				CommunicationHelper.cypherAndSend(msg, out, cipher);
			} catch (final EOFException e) {
				log.debug("Connection closed");
				return false;
			} catch (final Exception ex) {
				log.error("Unable to send msg " + msg + " due to exception", ex);
				return false;
			}
		}
		return true;
	}

}
