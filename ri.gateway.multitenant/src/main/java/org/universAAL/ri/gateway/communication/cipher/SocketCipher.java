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
package org.universAAL.ri.gateway.communication.cipher;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * Interface used for Serializer to marshall and unmarshall messages.
 * 
 * Implementations must have a public parameterless constructor, as reflections
 * will be used to instantiate it.
 * 
 * After instantiation the first call will be
 * {@link SocketCipher#setup(Properties)} to set it up. Then one of
 * {@link SocketCipher#createClientSocket(InetAddress, int)} or
 * {@link SocketCipher#createServerSocket(int, int, InetAddress)}. Followed by
 * any number of calls for {@link SocketCipher#sendMessage(Message)} or
 * {@link SocketCipher#readMessage()}.
 * 
 * 
 * @author amedrano
 * 
 */
public interface SocketCipher {

	/**
	 * Set up the Cipher with the properties.
	 * 
	 * @param props
	 * @return
	 */
	public boolean setup(Properties props);

	/**
	 * Create a {@link ServerSocket}.
	 * 
	 * @see ServerSocketFactory#createServerSocket(int, int, InetAddress)
	 * @param port
	 *            the port to listen to.
	 * @param backlog
	 *            the number of allowed pending connections.
	 * @param ifAddress
	 *            the address to which to bind.
	 * @return The Server Socket.
	 * @throws IOException
	 */
	public ServerSocket createServerSocket(int port, int backlog,
			InetAddress ifAddress) throws IOException;

	/**
	 * Used to set the socket, specially to add the accepted socket.
	 * 
	 * @param sock
	 */
	public SocketCipher acceptedSocket(Socket sock);

	/**
	 * Create and connect a {@link Socket}. Automatically
	 * {@link SocketCipher#setSocket(Socket) sets the Socket}.
	 * 
	 * @see SocketFactory#createSocket(InetAddress, int)
	 * @param host
	 *            The address of the server.
	 * @param port
	 *            The port of the server.
	 * @return An opened socked.
	 * @throws IOException
	 */
	public Socket createClientSocket(InetAddress host, int port)
			throws IOException;

	/**
	 * Marshalls, ciphers and sends the message.
	 * 
	 * @param m
	 *            message to be sent.
	 * @return
	 * @throws CryptoException
	 * @throws EOFException
	 *             This means the underlying socket is closed.
	 */
	public boolean sendMessage(Message m) throws CryptoException, IOException;

	/**
	 * Read the next message in the socket.
	 * 
	 * @return
	 * @throws CryptoException
	 * @throws EOFException
	 *             This means the underlying socket is closed.
	 */
	public Message readMessage() throws CryptoException, IOException;

}
