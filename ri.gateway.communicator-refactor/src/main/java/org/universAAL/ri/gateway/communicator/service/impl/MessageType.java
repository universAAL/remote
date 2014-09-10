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

/**
 * Type for MessageWrapper in order for the servlet to distinguish the message
 * purpose.
 *
 * @author skallz
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 *
 */
public enum MessageType {
    /**
     * standard synchronous request; the response goes back in the same
     * connection.
     */
    ServiceRequest,
    /**
     * will not send the reply.
     */
    ServiceRequestAsync,
    /**
     * a response for an asynchronous request.
     */
    ServiceResponseAsync,
    /**
     * a context event.
     */
    Context,
    /**
     * an UI request.
     */
    UI,

    /**
     * an UI response.
     */
    UIResponse, ImportRequest, ImportRemoval, ImportResponse, ImportRefresh,

    Error,

    ServiceCall,

    /**
     * This is the first message sent when establishing a link
     */
    ConnectRequest, ConnectResponse,

    /**
     * This is the first message sent when try to recover a link already that was established
     */
    Reconnect,

    /**
     * This is the message for closing a link
     */
    Disconnect, 
    
    /**
     * This is a one way message of the higher level
     */
    HighPush,
    
    /**
     * This is a one request-response message of the higher level
     */
    HighReqRsp;


}
