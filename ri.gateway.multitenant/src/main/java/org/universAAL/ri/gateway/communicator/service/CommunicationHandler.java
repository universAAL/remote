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
package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.communicator.service.impl.MessageWrapper;
import org.universAAL.ri.gateway.communicator.service.impl.SessionManager;

/**
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * 
 * @deprecated update to new Message class, and remove String[] sessions? are
 *             they {@link SessionManager} sessions or {@link Session}s?
 */
@Deprecated
public interface CommunicationHandler {

    public static final String BROADCAST_SESSION = "#BROADCAST";

    /**
     * Sends a prepared wrapped to specified URL and receives a wrapper as a
     * response if any.
     * 
     * @param w
     *            the message
     * @param to
     *            an array of String each representing a UUID so that the target
     *            session will receive this message, a special UUID = #BROADCAST
     *            for sending to all the active session(s)
     * @return wrapper with the response or null of nothing sent back
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public MessageWrapper sendMessage(MessageWrapper toSend,
	    final String[] sessions) throws IOException,
	    ClassNotFoundException, CryptoException;

    public void start() throws Exception;

    public void stop();
}
