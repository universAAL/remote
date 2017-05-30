/*******************************************************************************
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
package org.universAAL.ri.gateway.protocol;

import java.util.concurrent.TimeoutException;

/**
 * Interface to be implemented by artifacts capable of sending a message to a
 * remote ASG, and used by dose classes that need to send messages to remote
 * peers.
 * 
 * @author amedrano
 * 
 */
public interface MessageSender {

    /**
     * Send Asynchronously a {@link Message} independent of which level it is,
     * to a remote ASG. <br>
     * It may also used to send responses.
     * 
     * @param message
     */
    void send(Message message);

    /**
     * Send a request Synchronously to which a response is expected.
     * 
     * @param message
     *            to send, typically a request message
     * @return the response message, typically a response message.
     * @throws TimeoutException
     */
    Message sendRequest(Message message) throws TimeoutException;

}
