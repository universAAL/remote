package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;

/**
 * Interface used by callbacks that will be notified about response arrivals.
 * 
 * @author skallz
 * 
 */
public interface ResponseCallback {

    /**
     * Is invoked upon arrival of the response for which this callback was
     * registered.
     * 
     * @param response
     *            the response message
     * @throws IOException 
     */
    void collectResponse(Message response) throws IOException;

}
