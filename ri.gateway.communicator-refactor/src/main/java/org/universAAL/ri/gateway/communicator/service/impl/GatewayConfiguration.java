/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

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

package org.universAAL.ri.gateway.communicator.service.impl;

import java.util.Properties;

import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator.ConnectionMode;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator.RoutingMode;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-07-23 14:25:34
 *          +0200 (Wed, 23 Jul 2014) $)
 *
 */
public class GatewayConfiguration {

    private static GatewayConfiguration config = null;
    private RoutingMode routingMode;
    private ConnectionMode connectionMode;
    private Properties properties;

    private GatewayConfiguration() {

    }

    public static GatewayConfiguration getInstance() {
        synchronized (GatewayConfiguration.class) {
            if (config == null) {
                config = new GatewayConfiguration();
            }
        }
        return config;
    }

    public boolean isRouterMode() {
        return routingMode == GatewayCommunicator.RoutingMode.ROUTER;
    }

    public boolean isServerMode() {
        return connectionMode == GatewayCommunicator.ConnectionMode.SERVER;
    }

    /*
    private void validateConfigurations() {
        final String METHOD = "validateConfigurations";

        if (GatewayCommunicator.ConnectionMode.SERVER
                .toString()
                .equalsIgnoreCase(
                        properties
                                .getProperty(GatewayCommunicator.CONNECTION_MODE))) {
            connectionMode = GatewayCommunicator.ConnectionMode.SERVER;
            try {
                int port = Integer.valueOf(properties
                        .getProperty(GatewayCommunicator.LOCAL_SOCKET_PORT));
                server = HostAndPort.fromParts("0.0.0.0", port);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (server == null) {
                try {
                    server = HostAndPort
                            .fromString(properties
                                    .getProperty(GatewayCommunicator.LOCAL_SOCKET_PORT));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (server == null) {
                throw new IllegalArgumentException(
                        "When selecting in Server Mode the property "
                                + GatewayCommunicator.LOCAL_SOCKET_PORT
                                + " MUST BE SET and represent the port where to execute the TCP server");
            }
        } else {
            connectionMode = GatewayCommunicator.ConnectionMode.CLIENT;
            try {
                server = HostAndPort.fromString(properties
                        .getProperty(GatewayCommunicator.REMOTE_SOCKET));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (server == null) {
                try {
                    server = HostAndPort
                            .fromString(properties
                                    .getProperty(GatewayCommunicator.REMOTE_GATEWAYS_PROP));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (server == null) {
                throw new IllegalArgumentException(
                        "When selecting in Client Mode the property "
                                + GatewayCommunicator.REMOTE_SOCKET
                                + " MUST BE SET and represent the host:port where to find Gateway that run in server mode");
            }
        }

        if (GatewayCommunicator.RoutingMode.ROUTER.toString().equalsIgnoreCase(
                properties.getProperty(GatewayCommunicator.ROUTING_MODE))) {
            routingMode = GatewayCommunicator.RoutingMode.ROUTER;
        } else {
            routingMode = GatewayCommunicator.RoutingMode.FORWARD;
        }

    }
	*/
    
    public void setProperty(Properties p) {
        this.properties = p;
        //validateConfigurations();
    }

}
