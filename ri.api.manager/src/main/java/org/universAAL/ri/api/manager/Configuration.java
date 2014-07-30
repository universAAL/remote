package org.universAAL.ri.api.manager;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper class to centralized access to configuration parameters, which should
 * help when porting to uAAL Config Manager.
 * 
 * @author alfiva
 * 
 */
public class Configuration {
    /**
     * Used for prefix in properties
     */
    private static final String pkgNameDot = "org.universAAL.ri.api.manager.";

    /**
     * Determines the type of endpoint
     * 
     * @param remote
     *            String representation of the endpoint
     * @return RemoteAPI.REMOTE_POST if the endpoint is http or https
     *         <p>
     *         RemoteAPI.REMOTE_GCM if the endpoint is an Android GCM key
     *         <p>
     *         RemoteAPI.REMOTE_UNKNOWN if anything else
     */
    public static int determineEndpoint(String remote) {
	try {
	    URL attempt = new URL(remote);
	    if (attempt.getProtocol().toLowerCase().startsWith("http")) {
		return RemoteAPI.REMOTE_POST;
	    } else {
		// Non http/https URL TODO check at registration if allowed
		return RemoteAPI.REMOTE_UNKNOWN;
	    }
	} catch (MalformedURLException e) {
	    // Assume that if it is not a URL it is a GCM key
	    return RemoteAPI.REMOTE_GCM;
	}
    }

    /**
     * Get the Server Application Google Cloud Messaging Key. There is a
     * diffferent one for each server.
     * 
     * @return App Server GCM key
     */
    public static String getGCMKey() {
	return System.getProperty(pkgNameDot + "gcmkey",
		"AIzaSyB5UFo9DM6tYgAjfM2M68JR-2oBdAGii8w");
    }

    /**
     * Get if DRY mode: messages to GCM are handled OK, but not actually
     * delivered to clients. For testing purposes.
     * 
     * @return true if in DRY mode (no messages sent to clients)
     */
    public static boolean getGCMDry() {
	return System.getProperty(pkgNameDot + "gcmdry", "false")
		.equals("true");
    }

    /**
     * Determine if additional debug logging is available. This is used when the
     * information to be logged may contain sensitive data.
     * 
     * @return true if everything should be logged
     */
    public static boolean getLogDebug() {
	return System.getProperty(pkgNameDot + "logdebug", "false").equals(
		"true");
    }

    public static String getDerbyPath() {
	return System.getProperty(pkgNameDot + "derbypath",
		"/RAPIPersistence");
    }
}
