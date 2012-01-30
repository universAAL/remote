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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.service.ProfilingService;
import org.universAAL.ri.servicegateway.impl.Base64;

public abstract class GatewayPort extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = -513978908843447270L;
	// table that store user -> password pairs
	private Hashtable<String, String> userTable;
	protected Hashtable<String, String> userURIs;// NEW stores user´s URIs for
	// usernames
	private final Logger log;
	// used to call the profiling service
	private ServiceCaller caller;
	private static final String PROFILE_CLIENT_NAMESPACE = "http://ontology.universAAL/ProfileClient.owl#";
	private static final String OUTPUT_USER = PROFILE_CLIENT_NAMESPACE
			+ "oUser";
	// the realm is used for HTTP Authentication
	public final static String REALM = "Help when outside";

	/**
	 * Simply initialize the logger and the user table for the security
	 */
	public GatewayPort() {
		log = LoggerFactory.getLogger(this.getClass().getName());
		userTable = new Hashtable<String, String>();
		userURIs = new Hashtable<String, String>();
	}

	/**
	 * Set the HTTP Basic Authentication response with the realm
	 * "Help When Outside"
	 * 
	 * @param req
	 *            The request from goGet method
	 * @param resp
	 *            The response from doGet method, used to set the header to
	 *            WWW-Authenticate and the status to 401
	 */
	private void requireCredentials(HttpServletRequest req,
			HttpServletResponse resp) {
		String s = "Basic realm=\"" + REALM + "\"";
		resp.setHeader("WWW-Authenticate", s);
		resp.setStatus(401);
	}

	private Object getReturnValue(List outputs, String expectedOutput) {
		Object returnValue = null;
		int testCount = 0;
		if (outputs == null)
			log.info("ProfileConsumer: No info found!");
		else
			for (Iterator i = outputs.iterator(); i.hasNext();) {
				testCount++;
				ProcessOutput output = (ProcessOutput) i.next();
				if (output.getURI().equals(expectedOutput))
					if (returnValue == null)
						returnValue = output.getParameterValue();
					else
						log.info("ProfileConsumer: redundant return value!");
				else
					log.info("ProfileConsumer - output ignored: "
							+ output.getURI());
			}
		return returnValue;
	}

	/**
	 * 
	 * @param auth
	 *            The BASE64 encoded user:pass values. If null or empty, returns
	 *            false
	 * @return A String array of two elements containing the user and pass as
	 *         first and second element
	 */
	protected String[] getUserPass(String auth) {
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
	 * Allows or denies the authorization to use the servlets by the requesting
	 * user. Using the profiling service it queries the profiling database to
	 * search a valid user
	 * 
	 * @param user
	 *            The username entered in the authentication box
	 * @param pass
	 *            The password entered in the authentication box
	 * @return returns the user URI if authorized or null if not authorized
	 */
	private String allowedCredentials(String user, String pass) {

		ServiceRequest getUserByCredentials = new ServiceRequest(
				new ProfilingService(null), null);

		Restriction resUsername = Restriction.getFixedValueRestriction(
				User.PROP_USERNAME, user);
		Restriction resPassword = Restriction.getFixedValueRestriction(
				User.PROP_PASSWORD, pass);
		getUserByCredentials.getRequestedService().addInstanceLevelRestriction(
				resUsername,
				new String[] { ProfilingService.PROP_CONTROLS,
						User.PROP_USERNAME });
		getUserByCredentials.getRequestedService().addInstanceLevelRestriction(
				resPassword,
				new String[] { ProfilingService.PROP_CONTROLS,
						User.PROP_PASSWORD });

		ProcessOutput outUser = new ProcessOutput(OUTPUT_USER);
		PropertyPath ppUser = new PropertyPath(null, true,
				new String[] { ProfilingService.PROP_CONTROLS });
		getUserByCredentials.addSimpleOutputBinding(outUser, ppUser
				.getThePath());

		try {
			if (caller == null)
				throw new Exception("Default Service Caller not initialized");
			ServiceResponse sr = caller.call(getUserByCredentials);

			if (sr.getCallStatus() == CallStatus.succeeded) {
				try {

					Object o = getReturnValue(sr.getOutputs(), OUTPUT_USER);
					if (o instanceof User) {
						User userObject = (User) o;
						log.info("Authentication succeed. User URI: "
								+ userObject.getURI());
						// user authorized: return URI
						return userObject.getURI();
					} else {
						log.info("Authentication failed. No such user");
					}
				} catch (Exception e) {
					log.error("Exception: " + e.getMessage());
				}
			} else {
				log.info("List of parameters has not been retrieved");
				log.info(sr.getCallStatus().toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		// user not authorized: return NULL
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
	public boolean handleAuthorization(HttpServletRequest req,
			HttpServletResponse resp) {

		String authHeader = req.getHeader("Authorization");
		// if the authorization is not present, requires the credential
		// first element is username, second is password
		String[] userPass = getUserPass(authHeader);
		if (userPass == null) {
			// if the authorization is missing, require again the credentials
			requireCredentials(req, resp);
			return false;
		} else {
			// try {
			// first check if it is already authorized
			String tablePass = userTable.get(userPass[0]);
			if (tablePass != null && userPass[1].equals(tablePass))
				return true;
			// no password already stored for the username
			if (tablePass == null) {
				// check the credentials //NEW gets the user URI
				String allowedURI = allowedCredentials(userPass[0], userPass[1]);
				if (allowedURI != null)// NEW instead of allowed==true
				{
					// add the entry on the table
					userTable.put(userPass[0], userPass[1]);
					userURIs.put(userPass[0], allowedURI);// NEW also add the
					// user URI
					// resp.sendRedirect(url());// removed for the web handler
					return true;
				} else
					requireCredentials(req, resp);
			}

			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
		return false;
	}

	/**
	 * Set the context from the bundle where this class belongs. The
	 * BundleContext is a parameter of the service caller for the profiling
	 * service
	 * 
	 * @param bundleContext
	 *            The bundle context from the bundle where this class belongs
	 */

	public void setContext(ModuleContext mcontext) {

		caller = new DefaultServiceCaller(mcontext);
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

}
