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

import java.util.Hashtable;
//import java.util.List;

//import javax.security.auth.Subject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
//import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

//import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
//import org.apache.cxf.interceptor.security.DefaultSecurityContext;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;
//import org.apache.cxf.security.SecurityContext;
import org.universAAL.ri.rest.manager.Activator;

public class Authenticator implements ContainerRequestFilter {
    
    /**
     * Authentication realm
     */
    private static final String REALM = "universAAL";
    /**
     * In memory list of user-pwd pairs, to avoid constant use of the DB
     */
    private static Hashtable<String, String> users = new Hashtable<String, String>(); //TODO Clean from time to time?
    
    public void filter(ContainerRequestContext context) {
	Message m = JAXRSUtils.getCurrentMessage();
	AuthorizationPolicy policy = (AuthorizationPolicy) m.get(AuthorizationPolicy.class);
	if (policy != null) {
	    String username = policy.getUserName();
	    String password = policy.getPassword();
	    if (isAuthenticated(username, password) && isAuthorized(username, context.getUriInfo().getPath())) {
		// initialize org.apache.cxf.security.SecurityContext with
		// Principals representing the user and its roles (if available).
		// m.put(SecurityContext.class, new DefaultSecurityContext(new SimplePrincipal(username), new Subject())); 
		// let request continue
		return;
	    }
	}
	// else > authentication failed or is not present,
	// request the authentication, add the realm
	// name if needed to the value of WWW-Authenticate
	Response resp = Response.status(401).header("WWW-Authenticate", "Basic realm=\"" + REALM + "\"").build();
	context.abortWith(resp);
	// ClassResourceInfo cri = m.getExchange().get(OperationResourceInfo.class).getClassResourceInfo();
    }

    private boolean isAuthenticated(String username, String password) {
	// TODO Evaluate if it is worth it to have an in-memory user-pass list
	// to ease the pressure on the DB, if that means implementing additional
	// managing features to keep it sync with the DB.
	if (Activator.getPersistence().checkUser(username)) {
	    // user in the DB
	    if (Activator.getPersistence().checkUserPWD(username, password)) {
		// good pwd
		users.put(username, password);
		return true;
	    } else {
		// This user does not have the same PWD it registered.
		return false;
	    }
	} else {
	    // user not in DB
	    Activator.getPersistence().storeUserPWD(username, password);
	    users.put(username, password);
	    return true;
	    // New users are always welcome
	}
    }
    
    private boolean isAuthorized(String username, String p) {
	//TODO Rough authorization strategy. Only allow access to space if you created it.
//	List<PathSegment> s = JAXRSUtils.getPathSegments(p, false);
//	if(s.size()>2){ // /uaal and /uaal/spaces for everyone TODO ??
//	    PathSegment segment = s.get(2);
//	    String space=segment.toString();
//	    return Activator.getPersistence().checkUserSpace(username, space));
//	}
	//TODO Use native mechanisms for authorization
	return true;
    }

}