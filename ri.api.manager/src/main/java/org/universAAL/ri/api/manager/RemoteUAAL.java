package org.universAAL.ri.api.manager;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.support.utils.ICListener;
import org.universAAL.support.utils.ISListener;
import org.universAAL.support.utils.UAAL;
import org.universAAL.ri.api.manager.push.PushManager;

/**
 * Extension of the Utility API UAAL helper class that adds a few small tweaks
 * to acomodate remote node information.
 * 
 * @author alfiva
 * 
 */
public class RemoteUAAL extends UAAL {

    /**
     * Remote node endpoint information. Currently, for clients running in
     * PCs/servers, it should be the URL where a server should be getting the
     * POST callbacks (context events and service calls) sent by this server.
     * For Android devices, it should be a Google Cloud Messaging Key.
     */
    private String remoteID;
    
    /**
     * Sadly I had to include this because when using GCM, the remoteID can be
     * changed by the GCM server for whatever reason. Thus the listeners, if
     * using GCM, must be able to update this.remoteID and the Persistence DB,
     * and they need the node ID for that.
     */
    private String nodeID;

    /**
     * Basic constructor. Use this one instead of the UAAL one.
     * 
     * @param context
     *            The uAAL context
     * @param node
     *            The client remote node unique identifier
     * @param remote
     *            The client remote node endpoint information.
     */
    public RemoteUAAL(ModuleContext context, String node, String remote) {
	super(context);
	remoteID = remote;
	nodeID=node;
    }

    /**
     * Get the client remote node endpoint information.
     * 
     * @return remote endpoint.
     */
    public String getRemoteID() {
	return remoteID;
    }

    /**
     * Set the client remote node endpoint information.
     * 
     * @param remote
     *            endpoint.
     */
    public void setRemoteID(String remote) {
	this.remoteID = remote;
    }

    /**
     * Use this method to create a Listener to be used in the sendC() method of
     * the Utility API, instead of creating it ex profeso.
     * 
     * @return A ICListener
     */
    public ICListener createCListener() {
	return new CListener();
    }

    /**
     * Use this method to create a Listener to be used in the callS() method of
     * the Utility API, instead of creating it ex profeso.
     * 
     * @return A ISListener
     */
    public ISListener createSListener() {
	return new SListener();
    }

    /**
     * Custom ICListener to be used in the sendC() method of the Utility API. It
     * has access to the client remote node endpoint information.
     * 
     * @author alfiva
     * 
     */
    public class CListener implements ICListener {
	/**
	 * This is called everytime a ContextEvent is addressed to a remote
	 * node. It will pack the callback message and send it to the client
	 * remote node endpoint.
	 * 
	 * @param event
	 *            The event to send back to the client
	 */
	public void handleContextEvent(ContextEvent event) {
	    try {
		PushManager.sendC(nodeID, remoteID, event);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    /**
     * Custom ISListener to be used in the callS() method of the Utility API. It
     * has access to the client remote node endpoint information.
     * 
     * @author alfiva
     * 
     */
    public class SListener implements ISListener {
	/**
	 * This is called everytime a ServiceCall is addressed to a remote node.
	 * It will pack the callback message and send it to the client remote
	 * node endpoint.
	 * 
	 * @param call
	 *            The call to send back to the client
	 * @return The response that the client will have created
	 */
	public ServiceResponse handleCall(ServiceCall call) {
	    try {
		return PushManager.callS(nodeID, remoteID, call);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return new ServiceResponse(CallStatus.serviceSpecificFailure);
	    }
	}
    }
}
