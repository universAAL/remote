package org.universAAL.ri.api.manager.server;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;
import org.universAAL.ri.api.manager.Activator;

public class Authenticator implements HttpContext{
    
    private static final String REALM = "universAAL";
    private static Hashtable<String, String> users = new Hashtable<String, String>(); //TODO Clean from time to time?

    public boolean handleSecurity(HttpServletRequest req,
	    HttpServletResponse resp) throws IOException {
	String authHeader = req.getHeader("Authorization");

	if (authHeader != null) {
	    String[] userPass = getUserAndPass(authHeader);
	    if (authenticate(userPass[0],userPass[1])) {
		req.setAttribute(HttpContext.AUTHENTICATION_TYPE, "Basic");
		req.setAttribute(HttpContext.REMOTE_USER, userPass[0]);
		return true;
	    }
	}

	resp.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
	resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	resp.sendError(HttpServletResponse.SC_UNAUTHORIZED,
		"Authorization information missing");
	return false;
    }

    private boolean authenticate(String user, String pass) {
	String storedpass = users.get(user);
	if (storedpass != null) {
	    // user already in the memory list
	    return storedpass.equals(pass);
	} else {
	    // user not in the memory list, check DB
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
	}
    }

    public URL getResource(String name) {
	// TODO Only called from the Servlet, but mine does not
	return null;
    }

    public String getMimeType(String name) {
	// TODO Only called from the Servlet, but mine does not
	return null;
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
		int p = userPassString.indexOf(":");//TODO Check for user= http:...
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
