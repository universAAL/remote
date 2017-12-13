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
package org.universAAL.ri.rest.manager.server;

import org.universAAL.middleware.container.ModuleContext;

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
	private static final String pkgNameDot = "org.universAAL.ri.rest.manager.";
	/**
	 * Configuration suffix for GCM key
	 */
	private static final String CONF_GCM_KEY = "gcm.key";
	/**
	 * Configuration suffix for GCM dry
	 */
	private static final String CONF_GCM_DRY = "gcm.dry";
	/**
	 * Configuration suffix for log debug
	 */
	private static final String CONF_LOG_DBG = "debug.log";
	/**
	 * Configuration suffix for DB path
	 */
	private static final String CONF_DB_PTH = "db.path";
	/**
	 * Configuration suffix for DB auto removal
	 */
	private static final String CONF_DB_REM = "db.remove";
	/**
	 * Configuration suffix for DB encryption key
	 */
	private static final String CONF_DB_KEY = "db.cryptokey";
	/**
	 * Configuration suffix for DB user
	 */
	private static final String CONF_DB_USR = "db.user";
	/**
	 * Configuration suffix for DB password
	 */
	private static final String CONF_DB_PWD = "db.pass";
	/**
	 * Configuration suffix for database implementation class
	 */
	private static final String CONF_DB_CLASS = "db.class";
	// /**
	// * Configuration suffix for servlet context path
	// */
	// private static final String CONF_SERV_CTXT = "serv.context";
	// /**
	// * Configuration suffix for hardcoded servlet
	// */
	// private static final String CONF_SERV_HARD = "serv.hard";
	// /**
	// * Configuration suffix for GET debug mode
	// */
	// private static final String CONF_SERV_GET = "debug.get";
	/**
	 * Configuration suffix for GET debug mode
	 */
	private static final String CONF_SERV_USR = "serv.user";
	/**
	 * Configuration suffix for GET debug mode
	 */
	private static final String CONF_SERV_PWD = "serv.pass";
	/**
	 * Configuration suffix for host and port
	 */
	private static final String CONF_HOSTPORT = "serv.host";
	/**
	 * Configuration suffix for allowing new users to sign up
	 */
	private static final String CONF_ALLOWNEW = "allownewusers";

	/**
	 * Get the Server Application Google Cloud Messaging Key. There is a
	 * different one for each server.
	 * 
	 * @return App Server GCM key
	 */
	public static String getGCMKey() {
		return System.getProperty(pkgNameDot + CONF_GCM_KEY, "AIzaSyB5UFo9DM6tYgAjfM2M68JR-2oBdAGii8w");
	}

	/**
	 * Get if DRY mode: messages to GCM are handled OK, but not actually
	 * delivered to clients. For testing purposes.
	 * 
	 * @return true if in DRY mode (no messages sent to clients)
	 */
	public static boolean getGCMDry() {
		return System.getProperty(pkgNameDot + CONF_GCM_DRY, "false").equals("true");
	}

	/**
	 * Determine if additional INFO logging is available. This is used when the
	 * information to be logged may contain sensitive data.
	 * 
	 * @return true if everything should be logged
	 */
	public static boolean getLogDebug() {
		return System.getProperty(pkgNameDot + CONF_LOG_DBG, "false").equals("true");
	}

	/**
	 * Get the path where the folder containing the DB will be read/created.
	 * Only used if using Derby DB persistence (default).
	 * 
	 * @return Path to folder of the DB
	 */
	public static String getDBPath(ModuleContext context) {
		return System.getProperty(pkgNameDot + CONF_DB_PTH, context.getDataFolder() + "/RESTPersistence");
	}

	/**
	 * Get the value, in milliseconds, of time at which removal of older stored
	 * data will be removed. Only for Derby DB Persistence.
	 * 
	 * @return Minimum age, in milliseconds, at which the data will be removed
	 *         (data older than this will be removed)
	 */
	public static Long getRemovalTime() {
		return Long.getLong(pkgNameDot + CONF_DB_REM, -1l);
	}

	//
	// /**
	// * Get the context path part of the URL to be used as "alias" when
	// * registering the servlet.
	// *
	// * @return Context Path of URL
	// */
	// public static String getContext() {
	// return System.getProperty(pkgNameDot + CONF_SERV_CTXT, "/universaal");
	// }
	//
	// /**
	// * Determine if hardcoded registration of the servlet, with basic
	// * authentication, has been set. If not, the servelt will have to be
	// * registered by the web container using its own configuration methods
	// * (might require packaging this project as a .war), and also the
	// * authenticatino measures.
	// *
	// * @return true if hardcoded registration of the servlet is enabled
	// */
	// public static boolean getHardcoded() {
	// return System.getProperty(pkgNameDot + CONF_SERV_HARD, "true").equals(
	// "true");
	// }
	//
	// /**
	// * Get if GET debug mode is enabled. If so, GET HTTP Request will be
	// * accepted just like if they were POST.
	// *
	// * @return true if GET requests are accepted.
	// */
	// public static boolean getGETenabled() {
	// return System.getProperty(pkgNameDot + CONF_SERV_GET, "true").equals(
	// "true");
	// }
	//
	public static String getServerUSR() {
		return System.getProperty(pkgNameDot + CONF_SERV_USR, "RESTAPI");
	}

	public static String getServerPWD() {
		return System.getProperty(pkgNameDot + CONF_SERV_PWD, "RESTAPI");
	}

	public static String getDBKey() {
		return System.getProperty(pkgNameDot + CONF_DB_KEY, "R1e2m3o4t5e6A7P8I9");
	}

	public static String getDBUser() {
		return System.getProperty(pkgNameDot + CONF_DB_USR);
	}

	public static String getDBPass() {
		return System.getProperty(pkgNameDot + CONF_DB_PWD);
	}

	public static String getDBClass() {
		return System.getProperty(pkgNameDot + CONF_DB_CLASS,
				"org.universAAL.ri.rest.manager.server.persistence.PersistenceDerby");
	}

	public static String getHostPort() {
		return System.getProperty(pkgNameDot + CONF_HOSTPORT, "http://localhost:9000/");
	}
	
	public static boolean getAllowNewUsers() {
		return System.getProperty(pkgNameDot + CONF_ALLOWNEW, "true").equals("true");
	}
}
