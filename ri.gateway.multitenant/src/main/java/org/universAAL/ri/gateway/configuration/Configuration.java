/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
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

import org.universAAL.ri.gateway.Session;
import org.universAAL.ri.gateway.operations.OperationChainManager;

/**
 * Interface for configurating a Session.
 * 
 * @author amedrano
 * 
 */
public interface Configuration extends OperationChainManager {

    /**
     * Definition of the Connection mode.
     * 
     * @author amedrano
     * 
     */
    public enum ConnectionMode {
	SERVER, CLIENT
    }

    /**
     * Get the Connection mode intended for the {@link Session} represented by
     * this configuration.
     * 
     * @return the Connection mode.
     */
    ConnectionMode getConnectionMode();

    /**
     * Get the Host the Session should connect to. <br>
     * For {@link ConnectionMode#CLIENT clients} this is the hostname of the
     * server. <br>
     * For {@link ConnectionMode#SERVER servers} this is the interface they have
     * to listen to (default should be 0.0.0.0, i.e: all).
     * 
     * @return the Hostname.
     */
    String getConnectionHost();

    /**
     * Get the port to connect to. <br>
     * For {@link ConnectionMode#CLIENT clients} this is the port which the
     * server is set up at. <br>
     * for {@link ConnectionMode#SERVER servers} this is the port which they
     * have to listen to.
     * 
     * @return the port.
     */
    int getConnectionPort();

    /**
     * Get the encryption key to be used for the communication link.
     * 
     * @return
     */
    String getEncryptionKey();
}
