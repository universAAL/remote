/*
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
    
    ServiceCall;
    
    
}
