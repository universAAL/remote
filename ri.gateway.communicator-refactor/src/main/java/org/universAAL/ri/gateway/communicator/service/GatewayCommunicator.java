/*
    Copyright 2014-2014 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
    Faculty of Computer Science, Electronics and Telecommunications
    Department of Computer Science

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
package org.universAAL.ri.gateway.communicator.service;

import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import org.universAAL.ri.gateway.communicator.service.impl.MessageWrapper;

/**
 * This interface is used by the ImportExportManager for communication with
 * AALSpace Gateway Communicator via an OSGi service.
 * 
 * @author skallz
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * 
 * @deprecated
 * 
 */
@Deprecated
public interface GatewayCommunicator {

    public enum RoutingMode {
	ROUTER, FORWARD
    }

    public enum ConnectionMode {
	SERVER, CLIENT
    }

    String HASH_KEY = "hash-key";

    /**
     * System property used for specifying remote AAL Space Gateways addresses
     * (host ip and port) in comma separated manner. Example:
     * 192.168.192.1:8001,192.168.192.2.8001
     * 
     * @deprecated
     */
    @Deprecated
    String REMOTE_GATEWAYS_PROP = "remote-gateways";

    /**
     * System property used for specifying remote AAL Space Gateways addresses
     * (host ip and port) that is running in Server Mode and that we should
     * connect to
     * 
     */
    String REMOTE_SOCKET = "remote-gateway-socket";

    /**
     * System property used for specifying the server port of the AAL Space
     * Gateway in case that it is running in Server-Mode
     */
    String LOCAL_SOCKET_PORT = "local-socket-port";

    String CONNECTION_MODE = "connection-mode";

    String ROUTING_MODE = "routing-mode";

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
     * Name of the property with which the OSGi service will be registered. Used
     * to enable two different AALSpace Gateway Communitators in one AALSpace -
     * mainly for testing purposes.
     */
    // String OSGI_ID_PROPERTY = "org.universAAL.ri.id";

    /**
     * Sends a service request to another AALSpace Gateway Communicator
     * listening at given URL, waits for the response and returns it.
     * 
     * Equal to sendServiceRequest(message, to, 0);
     * 
     * @param message
     *            request massage to be sent to the remote communicator
     * @param to
     *            remote communicator's URL
     * @return response message
     */
    Message[] sendServiceRequest(Message message, URL[] to);

    Message[] sendServiceRequest(Message message);

    /**
     * Sends a service request to another AALSpace Gateway Communicator
     * listening at given URL, waits for the response and returns it if arrived
     * before timing out.
     * 
     * @param message
     *            request massage to be sent to the remote communicator
     * @param to
     *            remote communicator's URL
     * @param timeout
     *            time in milliseconds to wait for the response
     * @return response message
     * @throws TimeoutException
     *             when timed out
     */
    Message[] sendServiceRequest(Message message, URL[] to, long timeout)
	    throws TimeoutException;

    Message[] sendServiceRequest(Message message, long timeout)
	    throws TimeoutException;

    /**
     * Sends a service request to another AALSpace Gateway Communicator
     * listening at given URL, registers callback which will be notified once
     * the response arrives.
     * 
     * @param message
     *            request massage to be sent to the remote communicator
     * @param returnTo
     *            local communicator's URL to send back the response to
     * @param to
     *            remote communicator's URL
     * @param callback
     *            callback which will be notified once the response arrives
     */
    void sendServiceRequestAsync(Message message, URL returnTo, URL to,
	    ResponseCallback callback);

    /**
     * Sends a context event to other AALSpace Gateway Communicators listening
     * at given URL.
     * 
     * @param message
     *            context event to be sent
     * @param to
     *            a list of URLs of remote communicators to which the event
     *            should be delivered
     */
    void sendContextEvent(Message message, URL[] to);

    void sendContextEvent(Message message);

    /**
     * Sends a ui response to other AALSpace Gateway Communicators listening at
     * given URL.
     * 
     * @param message
     *            ui response
     * @param to
     *            a list of URLs of remote communicators to which the event
     *            should be delivered
     */
    void sendUIResponse(Message message, URL[] to);

    void sendUIResponse(Message message);

    /**
     * Sends a ui request to other AALSpace Gateway Communicators listening at
     * given URL.
     * 
     * @param message
     *            ui request
     * @param to
     *            a list of URLs of remote communicators to which the event
     *            should be delivered
     */
    void sendUIRequest(Message message, URL[] to);

    void sendUIRequest(Message message);

    Message[] sendImportRequest(Message message, URL[] to);

    Message sendImportRequest(Message message);

    void sendImportRefresh(Message message, URL[] to);

    void sendImportRefresh(Message message);

    void sendImportRemoval(Message message, URL[] to);

    void sendImportRemoval(Message message);

    void handleMessage(final MessageWrapper msg, final OutputStream out)
	    throws Exception;

    void stop();

    void start() throws Exception;
}
