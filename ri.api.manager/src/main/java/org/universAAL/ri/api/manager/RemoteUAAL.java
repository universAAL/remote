package org.universaal.ri.api.manager;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.support.utils.ICListener;
import org.universAAL.support.utils.ISListener;
import org.universAAL.support.utils.UAAL;
import org.universaal.ri.api.manager.server.CloudManager;

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
     * Basic constructor. Use this one instead of the UAAL one.
     * 
     * @param context
     *            The uAAL context
     * @param remote
     *            The client remote node endpoint information.
     */
    public RemoteUAAL(ModuleContext context, String remote) {
	super(context);
	remoteID = remote;
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
	    CloudManager.sendC(remoteID, event);
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
	    return CloudManager.callS(remoteID, call);
	}
    }
}
