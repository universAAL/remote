package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.ri.gateway.communicator.service.CommunicationHandler;
import org.universAAL.ri.gateway.communicator.service.GatewayCommunicator;

public class SocketCommunicationHandler implements CommunicationHandler {

    private final static Logger log = LoggerFactory
	    .getLogger(SocketCommunicationHandler.class);

    private static final int NUM_THREADS = 1;
    private static int PORT;
    private GatewayCommunicator communicator;
    private Executor executor;
    private ServerSocket server;
    private Thread serverThread;

    public SocketCommunicationHandler(GatewayCommunicator communicator) {
	this.communicator = communicator;

	final String localPort = CommunicatorStarter.properties
		.getProperty(GatewayCommunicator.LOCAL_SOCKET_PORT);
	if (localPort == null) {
	    throw new RuntimeException("Local socket port is not "
		    + "specified during middleware startup in '"
		    + GatewayCommunicator.LOCAL_SOCKET_PORT + "' property.");
	}

	PORT = Integer.valueOf(localPort);

	this.executor = Executors.newFixedThreadPool(NUM_THREADS);
	log.info("Created " + SocketCommunicationHandler.class.getName());
    }

    public void start() throws IOException {
	log.info("Starting TCP server on port " + PORT);
	server = new ServerSocket(PORT);
	serverThread = new Thread(new Runnable() {
	    public void run() {
		log.info("TCP server started on port " + PORT);
		while (!(Thread.currentThread().isInterrupted())) {
		    try {
			final Socket socket = server.accept();
			log.info("Got request ... processing ...");
			executor.execute(new Handler(socket));
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}

	    }
	});
	serverThread.start();
    }

    private class Handler implements Runnable {

	private Socket socket;

	public Handler(Socket socket) {
	    this.socket = socket;
	}

	public void run() {
	    try {
		communicator.handleMessage(socket.getInputStream(),
			socket.getOutputStream());
	    } catch (Exception e) {
		e.printStackTrace();
	    } finally {
		if (socket != null) {
		    try {
			socket.close();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	}

    }

    public MessageWrapper sendMessage(MessageWrapper toSend, URL target)
	    throws IOException, ClassNotFoundException {
	MessageWrapper resp = null;
	Socket socket = new Socket(target.getHost(), target.getPort());
	try {
	    Serializer.sendMessageToStream(toSend, socket.getOutputStream());
	    // if the other side sends a Wrapper, we should read it
	    resp = Serializer.unmarshalMessage(socket.getInputStream());
	} finally {
	    if (socket != null && !socket.isClosed()) {
		socket.close();
	    }
	}
	return resp;
    }

    public void stop() {
	try {
	    server.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	serverThread.interrupt();
    }

}
