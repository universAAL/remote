package org.universAAL.ri.api.manager.push;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ri.api.manager.RemoteAPI;
import org.universAAL.ri.api.manager.RemoteAPIImpl;

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
     */
    public static void sendC(String nodeid, String remoteid, ContextEvent event) throws Exception {
	switch (RemoteAPIImpl.determineEndpoint(remoteid)) {
	case RemoteAPI.REMOTE_POST:
	    PushHTTP.sendC(remoteid, event);
	    break;
	case RemoteAPI.REMOTE_GCM:
	    PushGCM.sendC(nodeid, remoteid, event);
	    break;
	default:
	    throw new Exception("Unable to determine protocol of remote endpoint");
	}
    }

    /**
     * Build a ServiceCall callback message and send it to the client remote
     * node endpoint.
     * 
     * @param remoteid
     *            The client remote node endpoint
     * @param remoteid 
     * @param call
     *            The serialized Service Call to send
     * @return The Service Response that the client remote node will have sent
     *         as response to the callback
     */
    public static ServiceResponse callS(String nodeid, String remoteid, ServiceCall call) throws Exception {
	switch (RemoteAPIImpl.determineEndpoint(remoteid)) {
	case RemoteAPI.REMOTE_POST:
	    return PushHTTP.callS(remoteid, call);
	case RemoteAPI.REMOTE_GCM:
	    return PushGCM.callS(nodeid, remoteid, call);
	default:
	    throw new Exception("Unable to determine protocol of remote endpoint");
	}
    }

}
