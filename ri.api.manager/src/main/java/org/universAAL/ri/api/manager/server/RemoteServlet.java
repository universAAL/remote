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
package org.universAAL.ri.api.manager.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.universAAL.ri.api.manager.Activator;
import org.universAAL.ri.api.manager.RemoteAPI;
import org.universAAL.ri.api.manager.push.PushGCM;

/**
 * An HTTP Servlet that handles the calls to the Remote API server.
 * 
 * @author alfiva
 * 
 */
public class RemoteServlet extends javax.servlet.http.HttpServlet{

    private static final long serialVersionUID = -1931914654539856412L;
    
    /**
     * Singleton instance of the Remote API implementation
     */
    private RemoteAPI remoteAPI;

    /**
     * Basic constructor
     * 
     * @param rAPI
     *            The singleton instance of the Remote API implementation
     */
    public RemoteServlet(RemoteAPI rAPI) {
	remoteAPI = rAPI;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	doPost(req, resp);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	resp.setContentType("text/plain");
	resp.setStatus(HttpServletResponse.SC_ACCEPTED);

	String user;
	if(Activator.isCustom()){
	    // Use own auth method
	    // No matter if the "login" user is set by the sender, because it is
	    // intercepted by HttpContext (Authenticator) and if it fails it will
	    // not get this far.
	    user = req.getRemoteUser();
	}else{
	    // Use servlet container auth method
	    user = req.getUserPrincipal().getName();
	}

	String method = req.getParameter(RemoteAPI.KEY_METHOD);
	String param = req.getParameter(RemoteAPI.KEY_PARAM);
	
	if (user == null || method == null || param == null) {
	    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
		    "Missing parameter");
	    return;
	}

	if (remoteAPI == null) {
	    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    "API not available");
	    return;
	} else {
	    String servResp = null;
	    try {
		if (RemoteAPI.METHOD_REGISTER.equals(method)) {
		    remoteAPI.register(user, param);
		    Activator.getPersistence().storeRegister(user, param);
		} else if (RemoteAPI.METHOD_SENDC.equals(method)) {
		    remoteAPI.sendC(user, param);
		} else if (RemoteAPI.METHOD_SUBSCRIBEC.equals(method)) {
		    remoteAPI.subscribeC(user, param);
		    Activator.getPersistence().storeSubscriber(user, param);
		} else if (RemoteAPI.METHOD_CALLS.equals(method)) {
		    servResp = remoteAPI.callS(user, param);
		} else if (RemoteAPI.METHOD_PROVIDES.equals(method)) {
		    remoteAPI.provideS(user, param);
		    Activator.getPersistence().storeCallee(user, param);
		} else if (RemoteAPI.METHOD_UNREGISTER.equals(method)) {
		    remoteAPI.unregister(user);
		    Activator.getPersistence().removeRegister(user);
		} else if (RemoteAPI.METHOD_RESPONSES.equals(method)) {
		    PushGCM.handleResponse(param,user);
		} else {
		    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
			    "No such method");
		    return;
		}
	    } catch (Exception e) {
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			e.toString()+"\n"+e.getMessage());
		return;
	    }
	    resp.setStatus(HttpServletResponse.SC_OK);
	    PrintWriter os = resp.getWriter();
	    if (servResp != null)
		os.print(servResp);
	    os.flush();
	    os.close();
	}
    }
}
