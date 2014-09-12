/*******************************************************************************
 * Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
 * Institute of Information Science and Technologies
 * of the Italian National Research Council
 *   
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
package org.universAAL.ri.gateway;

import java.io.IOException;

import org.universAAL.ri.gateway.communicator.service.impl.ServerSocketCommunicationHandler;
import org.universAAL.ri.gateway.configuration.Configuration;

/**
 * @author amedrano
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * 
 */
public class Server {

    private ServerSocketCommunicationHandler server;

    /**
     * @param fc
     * 
     */
    public Server(final Configuration fc) {
	server = new ServerSocketCommunicationHandler(fc);
	try {
	    server.start();
	} catch (Exception e) {
	    throw new IllegalStateException(
		    "Failed to start the actual server due a thrwon Exception so this object is invalid",
		    e);
	}
    }

    public void stop() {
	if (server != null) {
	    server.stop();
	}
    }

}
