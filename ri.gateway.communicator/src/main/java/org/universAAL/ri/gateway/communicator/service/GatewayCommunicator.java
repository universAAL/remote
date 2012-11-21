package org.universAAL.ri.gateway.communicator.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeoutException;

/**
 * This interface is used by the ImportExportManager for communication with
 * AALSpace Gateway Communicator via an OSGi service.
 * 
 * @author skallz
 * 
 */
public interface GatewayCommunicator {

    /**
     * System property used for specifying remote AAL Space Gateways addresses
     * (host ip and port) in comma separated manner. Example: 192.168.192.1:8001,192.168.192.2.8001
     */
    String REMOTE_GATEWAYS_PROP = "remote-gateways";

    String LOCAL_SOCKET_PORT = "local-socket-port";
    
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
    
    void sendImportRemoval(Message message, URL[] to);
    
    void sendImportRemoval(Message message);
    
    void handleMessage(InputStream in, OutputStream out) throws Exception;
    
    void stop();
    
    void start() throws Exception;
}
