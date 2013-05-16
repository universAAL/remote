package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.bouncycastle.crypto.CryptoException;
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

    public SocketCommunicationHandler(final GatewayCommunicator communicator) {
	this.communicator = communicator;

	final String localPort = CommunicatorStarter.properties
		.getProperty(GatewayCommunicator.LOCAL_SOCKET_PORT);
	if (localPort == null) {
	    throw new RuntimeException("Local socket port is not "
		    + "specified during middleware startup in '"
		    + GatewayCommunicator.LOCAL_SOCKET_PORT + "' property.");
	}

	final String hashKey = CommunicatorStarter.properties
			.getProperty(GatewayCommunicator.HASH_KEY);
	
	SecurityUtils.Instance.initialize(hashKey);
	
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
		Thread.currentThread().setName("Space Gateway :: Server");
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

	public Handler(final Socket socket) {
	    this.socket = socket;
	}

	public void run() {
	    Thread.currentThread().setName("Space Gateway :: ClientHandler");
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

    public MessageWrapper sendMessage(final MessageWrapper toSend, final URL target)
	    throws IOException, ClassNotFoundException, CryptoException {
	MessageWrapper resp = null;
	Socket socket = new Socket(target.getHost(), target.getPort());
	try {
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
	    Serializer.sendMessageToStream(toSend, os);
	    // if the other side sends a Wrapper, we should read it
	    if (!toSend.getType().equals(MessageType.Context) && !toSend.getType().equals(MessageType.UIResponse)){
	    	resp = Serializer.unmarshalMessage(is);
	    }
	} catch (EOFException ex) {
	    // no response (which is not an error) so we just return null
	    return null;
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
