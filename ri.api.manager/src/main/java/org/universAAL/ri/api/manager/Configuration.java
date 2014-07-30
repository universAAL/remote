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
		// Non http/https URL
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

    /**
     * Get the path where the folder containing the Derby DB will be
     * read/created. Only used if using Derby DB persistence (default).
     * 
     * @return Path to folder of the DB
     */
    public static String getDerbyPath() {
	return System.getProperty(pkgNameDot + "derbypath", "/RAPIPersistence");
    }

    /**
     * Get the value, in milliseconds, of time at which removal of older stored
     * data will be removed. Only for Derby DB Persistence.
     * 
     * @return Minimum age, in milliseconds, at which the data will be removed
     *         (data older than this will be removed)
     */
    public static Long getRemovalTime() {
	return Long.getLong(pkgNameDot + "derbyremove", -1l);
    }
}
