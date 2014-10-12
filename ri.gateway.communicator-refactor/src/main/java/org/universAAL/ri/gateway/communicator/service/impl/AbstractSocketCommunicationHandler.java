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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.log.Logger;
import org.universAAL.log.LoggerFactory;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.communication.cipher.Cipher;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;

/**
 * This class implements an abstract gateway based on TCP connection
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-08-08 14:46:31
 *          +0200 (Fri, 08 Aug 2014) $)
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

    protected MessageWrapper readMessage(final InputStream in) throws Exception {
        AbstractSocketCommunicationHandler.log
                .debug("Reading a message on the link");
        final MessageWrapper msg = Serializer.unmarshalMessage(in, cipher);
        AbstractSocketCommunicationHandler.log.debug("Read message "
                + msg.getType() + " going to handle it");
        return msg;
    }

    public MessageWrapper sendMessage(final MessageWrapper toSend,
            final String[] scopes) throws IOException, ClassNotFoundException,
            CryptoException {

        // TODO Stefano Lenzi: Use the target to select the scope where to send
        // the message, it should be an UUID

        MessageWrapper resp = null;
        final SessionManager refSM = SessionManager.getInstance();

        final List<UUID> targetLinks;
        Arrays.sort(scopes);

        if (isBroadcat(scopes)) {
            targetLinks = Arrays.asList(refSM.getSessionIds());
            if (scopes.length > 1) {
                log.warning("Sending a message with multiple scopes, but on of them is BROADCAST so we sent to all");
            }
            log.debug("The message is meant to be send as BROADCAST");
        } else {
            /*
             * is a multi-cast or unicast so we have to find the target manually
             */
            log.debug("Sending a messages as multicast or unicast");
            targetLinks = new ArrayList<UUID>();
            UUID[] sessions = refSM.getSessionIds();

            for (int i = 0; i < sessions.length; i++) {
                String spaceId = refSM.getAALSpaceIdFromSession(sessions[i]);
                if (Arrays.binarySearch(scopes, spaceId) >= 0) {
                    targetLinks.add(sessions[i]);
                }
            }
            log.debug("Found the following target "
                    + Arrays.toString(targetLinks.toArray())
                    + " for the message that had the following scopes "
                    + Arrays.toString(scopes));
        }
        for (UUID link : targetLinks) {
            if (refSM.isActive(link) == false) {
                /*
                 * The session is not active so we are not sending to it
                 */
                log.warning("The selected session "+link+" is UNACTIVE so no message will be sent to it");
                continue;
            }
            final OutputStream out = refSM.getOutputStream(link);
            final InputStream in = refSM.getInputStream(link);

            if (out == null || in == null) {
                // TODO log that we found an invalid-session
                continue;
            }

            try {
                Serializer.sendMessageToStream(toSend, out, cipher);

                if (toSend.getType() == MessageType.HighReqRsp) {
                    resp = Serializer.unmarshalMessage(in, cipher);
                }
            } catch (final EOFException ex) {
                // no response (which is not an error) so we just return null
            }
        }

        // TODO either we change the return type to void/boolean or to
        // MessageWrapper[]
        return resp;
    }

    private boolean isBroadcat(String[] scopes) {
        for (int i = 0; i < scopes.length; i++) {
            if (CommunicationHandler.BROADCAST_SESSION == scopes[i]
                    || CommunicationHandler.BROADCAST_SESSION.equals(scopes[i])) {
                return true;
            }
        }
        return false;
    }

}
