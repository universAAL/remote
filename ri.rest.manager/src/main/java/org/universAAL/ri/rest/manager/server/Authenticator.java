/*
	Copyright 2015 ITACA-SABIEN, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (SABIEN)
	
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
package org.universAAL.ri.rest.manager.server;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;

public class Authenticator implements ContainerRequestFilter {
    public void filter(ContainerRequestContext context) {
	Message m = JAXRSUtils.getCurrentMessage();
	AuthorizationPolicy policy = (AuthorizationPolicy) m.get(AuthorizationPolicy.class);
	if (policy != null) {
	    String username = policy.getUserName();
	    String password = policy.getPassword();
	    if (isAuthenticated(username, password)) {
		// let request to continue
		return;
		// TODO initialize org.apache.cxf.security.SecurityContext with
		// Principals representing the user and its roles (if
		// available).
	    }
	}
	// else > authentication failed or is not present,
	// request the authentication, add the realm
	// name if needed to the value of WWW-Authenticate
	Response resp = Response.status(401).header("WWW-Authenticate", "Basic").build();
	context.abortWith(resp);
	// ClassResourceInfo cri = m.getExchange().get(OperationResourceInfo.class).getClassResourceInfo();
    }

    private boolean isAuthenticated(String username, String password) {
	// TODO Auto-generated method stub
	return (username.equals("usr") && password.equals("pwd"));
    }
}