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

/**
 * Keys for the properties file.
 * 
 * @author amedrano
 * 
 */
public interface PropertiesFileKeys {
    /**
     * System property used for specifying remote AAL Space Gateways addresses
     * that is running in Server Mode and that we should connect to
     * 
     */
    String REMOTE_HOST = "remote-gateway-host";

    /**
     * System property used for specifying the socket port of the AAL Space
     * Gateway to connec to, in case that it is running in Server-Mode it is the
     * socket to listen to.
     */
    String SOCKET_PORT = "socket-port";

    String CONNECTION_MODE = "connection-mode";

    String ROUTING_MODE = "routing-mode";

    String SECURITY_DEFINITION = "security-definition-file";
    
    
    /**
     * Property used for specifying bus members that are allowed to be imported
     * into this AAL Space. List of comma separated regular expressions related
     * to service, context or ui URI.
     */
    String IMPORT_SECURITY_CONSTRAINT_ALLOW = "import-security-constraint-allow";

    /**
     * Property used for specifying bus members that are allowed to be exported
     * to remote AAL Spaces. List of comma separated regular expressions related
     * to service, context or ui URI.
     */
    String EXPORT_SECURITY_CONSTRAINT_ALLOW = "export-security-constraint-allow";

    /**
     * Property used for specifying bus members that are denied to be imported
     * into this AAL Space. List of comma separated regular expressions related
     * to service, context or ui URI.
     */
    String IMPORT_SECURITY_CONSTRAINT_DENY = "import-security-constraint-deny";

    /**
     * Property used for specifying bus members that are denied to be exported
     * to remote AAL Spaces. List of comma separated regular expressions related
     * to service, context or ui URI.
     */
    String EXPORT_SECURITY_CONSTRAINT_DENY = "export-security-constraint-deny";

    /**
     * Alias' prefix under which the HTTP servlet will be registered.
     */
    String ALIAS_PREFIX = "gateway-communicator";

    /**
     * The Key for encryption.
     */
    String HASH_KEY = "hash-key";
}
