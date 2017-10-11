/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 *
 * Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
 *  Institute of Information Science and Technologies
 *  of the Italian National Research Council
 *
 *  Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
 *  Faculty of Computer Science, Electronics and Telecommunications
 *  Department of Computer Science
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.configuration;

import org.universAAL.ri.gateway.communication.cipher.SocketCipher;

/**
 * Keys for the properties file.
 * 
 * @author amedrano
 * 
 */
public interface PropertiesFileKeys {
	/**
	 * System property used for specifying remote Space Gateways addresses that
	 * is running in Server Mode and that we should connect to
	 * 
	 */
	String REMOTE_HOST = "remote-gateway-host";

	/**
	 * System property used for specifying the socket port of the Space Gateway
	 * to connec to, in case that it is running in Server-Mode it is the socket
	 * to listen to.
	 */
	String SOCKET_PORT = "socket-port";

	/**
	 * System property used to determine if the connection should be set up as a
	 * client (use value CLIENT) or as a server (use value SERVER).
	 */
	String CONNECTION_MODE = "connection-mode";

	/**
	 * System property that defines the turtle file that defines which imports,
	 * exports, and messages are allowed or denied.
	 */
	String SECURITY_DEFINITION = "security-definition-file";

	/**
	 * System property indicating the {@link SocketCipher} that should be loaded
	 * to manage channel encryption. Each {@link SocketCipher} should have its
	 * own configuration entries.
	 */
	String CHIPER_CLASS = "cipher.class";

	/**
	 * System property defining the time in milliseconds for which the gateway
	 * should wait.
	 */
	String TIMEOUT = "timeout";

	/**
	 * System property indicating if the gateway should cache messages before
	 * the connection is actually established. "true" or "false".
	 */
	String PRE_CONN_CACHE = "pre-connection-cache";

	/**
	 * System property indicating the maximum size of message queues
	 */
	String QUEUES = "cache-max-size";

	/**
	 * System property indicating the maximum number of export attempts.
	 */
	String EXPORT_ATTEMPTS = "export-attempts";

	/**
	 * System property indicating the number of threads to be used for server
	 * mode.
	 */
	String SERVER_THREADS = "server-threads";

}
