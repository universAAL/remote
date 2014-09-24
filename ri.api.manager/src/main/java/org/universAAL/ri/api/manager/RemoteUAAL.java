/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.ri.api.manager;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
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
     * These lists contain the URIs of CEPs and SProfiles registered so far.
     * They are used to disallow registration of the same CEP/Profile more than
     * once (which may happen with unreliable clients that dont unregister).
     */ //TODO migrate this to the original UAAL
    private List<String> cepsList = new ArrayList<String>();
    private List<String> sprofilesList = new ArrayList<String>();

    /**
     * Get the list of context event patterns of subscribers registered so far.
     * 
     * @return The list of URIs of context event patterns of subscribers
     */
    public List<String> getCEPsList() {
	return cepsList;
    }

    /**
     * Get the list of service profiles of callees registered so far.
     * 
     * @return The list of URIs of service profiles of callees
     */
    public List<String> getSProfilesList() {
	return sprofilesList;
    }
    
    @Override
    public void subscribeC(ContextEventPattern[] p, ICListener l) {
	// This is like super. , but handles the list of registered CEPs
	for (int i = 0; i < p.length; i++) {
	    if (!cepsList.contains(p[i].getURI())) {
		// Only register if not already
		super.subscribeC(p, l);
		cepsList.add(p[i].getURI());
	    }// TODO else log
	}
    }

    @Override
    public void provideS(ServiceProfile[] p, ISListener l) {
	// This is like super. , but handles the list of registered Profiles
	for (int i = 0; i < p.length; i++) {
	    if (!sprofilesList.contains(p[i].getURI())) {
		// Only register if not already
		super.provideS(p, l);
		sprofilesList.add(p[i].getURI());
	    }// TODO else log
	}
    }

    @Override
    public void terminate() {
	// This is like super. , but handles the list of registered CEPs and Profiles
	super.terminate();
	cepsList.clear();
	sprofilesList.clear();
    }

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
     * @param uri
     *            The URI of the context event pattern being used for this
     *            listener
     * 
     * @return A ICListener
     */
    public ICListener createCListener(String uri) {
	return new CListener(uri);
    }

    /**
     * Use this method to create a Listener to be used in the callS() method of
     * the Utility API, instead of creating it ex profeso.
     * 
     * @param uri
     *            The URI of the service profile being used for this listener
     * 
     * @return A ISListener
     */
    public ISListener createSListener(String uri) {
	return new SListener(uri);
    }

    /**
     * Custom ICListener to be used in the sendC() method of the Utility API. It
     * has access to the client remote node endpoint information.
     * 
     * @author alfiva
     * 
     */
    public class CListener implements ICListener {
	private String toURI;
	protected CListener(String uri){
	    toURI=uri;
	}
	/**
	 * This is called everytime a ContextEvent is addressed to a remote
	 * node. It will pack the callback message and send it to the client
	 * remote node endpoint.
	 * 
	 * This is called from the single ContextStrategy thread. This method
	 * will perform network operations, which will take time, so it uses an
	 * inner thread every time is called.
	 * 
	 * @param event
	 *            The event to send back to the client
	 */
	public void handleContextEvent(final ContextEvent event) {
	    new Thread("RemoteUAAL_CListener") {
		public void run() {
		    try {
			if(event.isSerializableTo(nodeID)){ //MULTITENANT The call is for this scope
			    PushManager.sendC(nodeID, remoteID, event, toURI);
			} //MULTITENANT The call is NOT for this scope > ignore
		    } catch (Exception e) {
			e.printStackTrace();
			Activator.logE("CListener.handleContextEvent",
				"Unable to send the proxied Context Event to the remote node. "
					+ e.getMessage());
		    }
		}
	    }.start();
	}
    }

    /**
     * Custom ISListener to be used in the callS() method of the Utility API. It
     * has access to the client remote node endpoint information.
     * 
     * This is called from the single ServiceStrategy thread. This method will
     * perform network operations, which will take time. Unfortunately it cannot
     * use a Thread for them because it is a synchronous execution that must
     * return a response, and it would block anyway.
     * 
     * @author alfiva
     * 
     */
    public class SListener implements ISListener {
	private String toURI;
	protected SListener(String uri){
	    toURI=uri;
	}
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
		if(call.isSerializableTo(nodeID)){ //MULTITENANT The call is for this scope
		    return PushManager.callS(nodeID, remoteID, call, toURI);
		}else{ //MULTITENANT The call is NOT for this scope > answer Denied
		    return new ServiceResponse(CallStatus.denied);
		}
	    } catch (Exception e) {
		e.printStackTrace();
		Activator.logE("CListener.handleCall",
			"Unable to send the proxied Service Call to the remote node. "
				+ e.getMessage());
		return new ServiceResponse(CallStatus.serviceSpecificFailure);
	    }
	}
    }//TODO Change UAALutils to use async handleRequest also/instead of handleCall, which will allow threading here
}
