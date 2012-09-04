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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.ri.servicegateway.impl.Base64;
import org.universAAL.ui.security.authorization.AuthorizatorImpl;

public abstract class GatewayPort extends javax.servlet.http.HttpServlet {
    private static final long serialVersionUID = -513978908843447270L;
    // table that store user -> password pairs
    private Hashtable<String, String> userTable;
    protected Hashtable<String, String> userURIs;// NEW stores user´s URIs for

    private static ModuleContext mcontext;

    // the realm is used for HTTP Authentication
    // public final static String REALM = "Help when outside";
    public final static String REALM = "Enter universAAL remote login data";

    /**
     * Simply initialize the logger and the user table for the security
     */
    public GatewayPort() {
	userTable = new Hashtable<String, String>();
	userURIs = new Hashtable<String, String>();
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
    protected void requireCredentials(HttpServletRequest req,
	    HttpServletResponse resp) {
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
    public boolean handleAuthorization(HttpServletRequest req,
	    HttpServletResponse resp) {

	String authHeader = req.getHeader("Authorization");
	// if the authorization is not present, requires the credential
	// first element is username, second is password
	String[] userPass = getUserAndPass(authHeader);
	if (userPass == null) {
	    // if the authorization is missing, require again the credentials
	    requireCredentials(req, resp);
	    //return false;
	} else {

	    // first check if it is already authorized
	    String tablePass = userTable.get(userPass[0]);
	    if (tablePass != null && userPass[1].equals(tablePass))
		return true;
	    // no password already stored for the username
	    if (tablePass == null) {
		// check the credentials 
		AuthorizatorImpl authorizator = new AuthorizatorImpl(getContext());
		if (authorizator.isAuthorized(userPass[0], userPass[1]) == true)
		{
		    userTable.put(userPass[0], userPass[1]);
		    userURIs.put(userPass[0], authorizator.getAllowedUserURI());// NEW also add the
		    // user URI
		    // resp.sendRedirect(url());// removed for the web handler
		    return true;
		} else
		    requireCredentials(req, resp);
	    }

	}
	return false;
    }

    public void setContext(ModuleContext mcontext) {
	GatewayPort.mcontext = mcontext;
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

}
