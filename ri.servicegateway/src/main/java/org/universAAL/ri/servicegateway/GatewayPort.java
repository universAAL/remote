/*
	Copyright 2008-2010 Vodafone Italy, http://www.vodafone.it
	Vodafone Omnitel N.V.
	
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
package org.universAAL.ri.servicegateway;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ontology.profile.User;
import org.universAAL.ri.servicegateway.impl.Base64;
import org.universAAL.security.authenticator.client.UserPaswordAuthenticatorClient;

/**
 * Base class for HTTP servlets. To create a servlet, create a subclass and
 * overwrite one of the methods
 * {@link #doGet(HttpServletRequest, HttpServletResponse)} or
 * {@link #doPost(HttpServletRequest, HttpServletResponse)}. Then set the module
 * context and call {@link #register()}. When done, call {@link #unregister()}.
 * 
 * @author
 * @author Carsten Stockloew
 */
public abstract class GatewayPort extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = -513978908843447270L;

	/**
	 * Table that Associates usernames with {@link User}s.
	 */
	protected Hashtable<String, User> loggedUsers;

	private ModuleContext mcontext;

	// the realm is used for HTTP Authentication
	// public final static String REALM = "Help when outside";
	public final static String REALM = "Enter universAAL remote login data";

	private UserPaswordAuthenticatorClient authenticator;

	/**
	 * Simply initialize the logger and the user table for the security
	 */
	public GatewayPort() {
		loggedUsers = new Hashtable<String, User>();
	}

	public GatewayPort(ModuleContext mcontext) {
		this();
		setContext(mcontext);
	}

	/**
	 * Set the HTTP Basic Authentication response with the realm.
	 * 
	 * @param req
	 *            The request from goGet method
	 * @param resp
	 *            The response from doGet method, used to set the header to
	 *            WWW-Authenticate and the status to 401
	 */
	protected void requireCredentials(HttpServletRequest req, HttpServletResponse resp) {
		String s = "Basic realm=\"" + REALM + "\"";
		resp.setHeader("WWW-Authenticate", s);
		resp.setStatus(401);
	}

	/**
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
				int p = userPassString.indexOf(":");
				if (p != -1) {
					String userID = userPassString.substring(0, p);
					String pass = userPassString.substring(p + 1);
					return new String[] { userID, pass };
				}
			}

		}
		return null;
	}

	/**
	 * Handle the login phase
	 * 
	 * @param req
	 *            The request from goGet method, used to retrieve the
	 *            "Authorization" header
	 * @param resp
	 *            The response from doGet method, used to redirect to the
	 *            correct url()
	 */
	public boolean handleAuthorization(HttpServletRequest req, HttpServletResponse resp) {

		String authHeader = req.getHeader("Authorization");
		// if the authorization is not present, requires the credential
		// first element is username, second is password
		String[] userPass = getUserAndPass(authHeader);
		if (userPass == null) {
			// if the authorization is missing, require again the credentials
			requireCredentials(req, resp);
			// return false;
		} else {

			// first check if it is already authorized
			if (loggedUsers.containsKey(userPass[0]))
				return true;
			// username not yet authenticated.
			else {
				// check the credentials
				User u = authenticator.authenticate(userPass[0], userPass[1]);
				if (u != null) {
					loggedUsers.put(userPass[0], u);
					// NEW
					// also
					// add
					// the
					// user URI
					// resp.sendRedirect(url());// removed for the web handler
					return true;
				} else
					requireCredentials(req, resp);
			}

		}
		return false;
	}

	public void logout(User usr) {
		loggedUsers.remove(usr);
	}

	public void setContext(ModuleContext mcontext) {
		this.mcontext = mcontext;
		if (authenticator != null)
			authenticator.close();
		authenticator = new UserPaswordAuthenticatorClient(this.mcontext);
	}

	public ModuleContext getContext() {
		return mcontext;
	}

	/**
	 * The URL where the servlet is registered and accessed by the web client.
	 * 
	 * @return A string starting with "/" to access the service. For example
	 *         "/myservlet"
	 */
	public abstract String url();

	/**
	 * The symbolic data directory where all the resources reside. Each image,
	 * html, javascript used in html, javascript must use the symbolic name
	 * returned by this method. The bundle containing the servlet must also have
	 * the resources in the same symbolic name. For example, if this method
	 * returns /myservicedir, an html code that uses a <em>script</em> tag will
	 * be :
	 * <p>
	 * <code>
	 * &lt;script type="text/javascript" src="/myservicedir/script.js"&gt;
	 * </code>
	 * </p>
	 * 
	 * @return The string representing the symbolic datadir, <em>null</em> if no
	 *         data directory is needed
	 */
	public abstract String dataDir();

	public boolean register() {
		Object sRef = mcontext.getContainer().fetchSharedObject(mcontext, new Object[] { HttpService.class.getName() });
		if (sRef != null) {
			HttpService httpService = (HttpService) sRef;

			try {
				httpService.registerServlet(url(), this, null, null);
			} catch (ServletException e) {
				LogUtils.logError(mcontext, this.getClass(), "register",
						new Object[] { "Exception while registering Servlet." }, e);
				return false;
			} catch (NamespaceException e) {
				LogUtils.logError(mcontext, this.getClass(), "register",
						new Object[] { "Servlet Namespace exception; alias (URI) is already in use." }, e);
				return false;
			}
			LogUtils.logInfo(mcontext, this.getClass(), "register", new Object[] { "Servlet started." }, null);
			return true;

		} else
			LogUtils.logInfo(mcontext, this.getClass(), "register",
					new Object[] { "Servlet cannot be registered: no http service available." }, null);
		return false;
	}

	public boolean unregister() {
		Object sRef = mcontext.getContainer().fetchSharedObject(mcontext, new Object[] { HttpService.class.getName() });
		if (sRef != null) {
			HttpService httpService = (HttpService) sRef;

			try {
				httpService.unregister(url());
			} catch (IllegalArgumentException e) {
				LogUtils.logError(mcontext, this.getClass(), "unregister",
						new Object[] { "Servlet cannot be unregistered: illegal argument." }, e);
				return false;
			}
			authenticator.close();
			LogUtils.logInfo(mcontext, this.getClass(), "unregister", new Object[] { "Servlet stopped." }, null);
			return true;

		} else
			LogUtils.logInfo(mcontext, this.getClass(), "unregister",
					new Object[] { "Servlet cannot be unregistered: no http service available." }, null);
		return false;
	}
}
