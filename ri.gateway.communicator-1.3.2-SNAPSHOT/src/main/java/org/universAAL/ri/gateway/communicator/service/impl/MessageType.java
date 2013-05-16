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
