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
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.universAAL.ri.api.manager.Activator;
import org.universAAL.ri.api.manager.RemoteAPI;

/**
 * Implementatino of OSGi HttpContext to be used by the Servlet. It is used only
 * for Authentication purposes, and only if the hardcoded registration of
 * servlet is enabled.
 * 
 * @author alfiva
 * 
 */
public class Authenticator implements HttpContext {

	/**
	 * Authentication realm
	 */
	private static final String REALM = "universAAL";
	/**
	 * In memory list of user-pwd pairs, to avoid constant use of the DB
	 */
	private static Hashtable<String, String> users = new Hashtable<String, String>(); // TODO
																						// Clean
																						// from
																						// time
																						// to
																						// time?

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.
	 * HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public boolean handleSecurity(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String authHeader = req.getHeader("Authorization");

		if (authHeader != null) {
			String[] userPass = getUserAndPass(authHeader);
			if (authenticate(userPass[0], userPass[1],
					RemoteAPI.METHOD_REGISTER.equals(req.getParameter(RemoteAPI.KEY_METHOD)))) {
				req.setAttribute(HttpContext.AUTHENTICATION_TYPE, "Basic");
				req.setAttribute(HttpContext.REMOTE_USER, userPass[0]);
				return true;
			}
		}

		resp.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization information missing or incorrect");
		return false;
	}

	/**
	 * Method that checks the proper authentication of a user-pwd pair. User-PWD
	 * are kept in memory to avoid abusing the DB. If the pair is not there, it
	 * is checked against the DB. If the user is not present, it is stored with
	 * the PWD. If the request is a REGISTER, the in-memory pairs are ignored,
	 * in case the PWD has been changed externally in the DB and needs to be
	 * re-checked.
	 * 
	 * @param user
	 *            User
	 * @param pass
	 *            Password
	 * @param isregister
	 *            If the request is attempting to do a REGISTER
	 * @return true if authentication is correct and no errors occured
	 */
	private boolean authenticate(String user, String pass, boolean isregister) {
		String storedpass = users.get(user);
		if (isregister || storedpass == null) {
			// user not in the memory list or logging in for 1st time, check DB
			if (Activator.getPersistence().checkUser(user)) {
				// user in the DB
				if (Activator.getPersistence().checkUserPWD(user, pass)) {
					// good pwd
					users.put(user, pass);
					return true;
				} else {
					// This user does not have the same PWD it registered.
					// Impostor!
					return false;
				}
			} else {
				// user not in DB
				Activator.getPersistence().storeUserPWD(user, pass);
				users.put(user, pass);
				return true;
				// New users are always welcome
			}
		} else {
			// user already in the memory list and not attempting to register
			// for 1st time
			return storedpass.equals(pass);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
	 */
	public URL getResource(String name) {
		// Only called from the Servlet, but mine does not
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
	 */
	public String getMimeType(String name) {
		// Only called from the Servlet, but mine does not
		return null;
	}

	/**
	 * Parses the Authorization header for BASIC authentication
	 * 
	 * @param auth
	 *            The BASE64 encoded user:pass values. If null or empty, returns
	 *            false
	 * @return A String array of two elements containing the user and pass as
	 *         first and second element
	 */
	public String[] getUserAndPass(String auth) {
		if (auth == null || auth.isEmpty())
			return null;
		StringTokenizer authTokenizer = new StringTokenizer(auth, " ");
		if (authTokenizer.hasMoreTokens()) {
			// assume BASIC authentication type
			String authType = authTokenizer.nextToken();
			if ("basic".equalsIgnoreCase(authType)) {
				String credentials = authTokenizer.nextToken();

				String userPassString = new String(Base64.decode(credentials));
				// The decoded string is in the form
				// "userID:password".
				int p = -1;
				if (userPassString.startsWith("http")) {
					// There will be a first : after http/s
					p = userPassString.indexOf(":", userPassString.indexOf(":"));
				} else {
					p = userPassString.indexOf(":");
				}
				if (p != -1) {
					String userID = userPassString.substring(0, p);
					String pass = userPassString.substring(p + 1);
					return new String[] { userID, pass };
				}
			}

		}
		return null;
	}

}
