/*******************************************************************************
 * Copyright 2017 2011 Universidad Politécnica de Madrid
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * @author amedrano
 * 
 */
public class ClearTextCipher implements SocketCipher {

	private Socket socket;
	private ServerSocket ssocket;
	private ObjectOutputStream os;
	private ObjectInputStream is;

	/**
	 * 
	 */
	public ClearTextCipher() {
	}

	/** {@inheritDoc} */
	public boolean setup(Properties props) {
		// no config required
		return true;
	}

	/** {@inheritDoc} */
	public ServerSocket createServerSocket(int port, int backlog,
			InetAddress ifAddress) throws IOException {
		ssocket = ServerSocketFactory.getDefault().createServerSocket(port,
				backlog, ifAddress);
		return ssocket;
	}

	/** {@inheritDoc} */
	public SocketCipher acceptedSocket(Socket sock) {

		try {
			ClearTextCipher newCipher = new ClearTextCipher();

			newCipher.os = new ObjectOutputStream(sock.getOutputStream());
			newCipher.is = new ObjectInputStream(sock.getInputStream());
			return newCipher;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** {@inheritDoc} */
	public Socket createClientSocket(InetAddress host, int port)
			throws IOException {
		socket = SocketFactory.getDefault().createSocket(host, port);
		os = new ObjectOutputStream(socket.getOutputStream());
		is = new ObjectInputStream(socket.getInputStream());
		return socket;
	}

	/** {@inheritDoc} */
	public boolean sendMessage(Message m) throws CryptoException, IOException {
		if (os == null)
			return false;
		os.writeObject(m);
		return true;

	}

	/** {@inheritDoc} */
	public Message readMessage() throws CryptoException, IOException {
		if (is == null)
			return null;
		try {
			return (Message) is.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
