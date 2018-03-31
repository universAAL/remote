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
import java.security.Key;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * @author amedrano
 * 
 */
public class StreamCipher implements SocketCipher {

	private static final String CIPHER_ALGORITHM = "cipher.algorithm";
	private static final String KEY = "cipher.key";
	private static final String KEY_ALGORITHM = "cipher.key.algorithm";
	private ObjectOutputStream os;
	private ObjectInputStream is;
	private Cipher cipher;
	private Cipher decipher;
	private Key key;

	/**
	 * 
	 */
	public StreamCipher() {
	}

	/** {@inheritDoc} */
	public boolean setup(Properties props) {
		try {
			cipher = Cipher.getInstance(props.getProperty(CIPHER_ALGORITHM));
			key = new SecretKeySpec(props.getProperty(KEY).getBytes(),
					props.getProperty(KEY_ALGORITHM));
			cipher.init(Cipher.ENCRYPT_MODE, key);
			decipher = Cipher.getInstance(props.getProperty(CIPHER_ALGORITHM));
			decipher.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			LogUtils.logError(Gateway.getInstance().context, getClass(),
					"setup", new String[] { "unexpected Exception" }, e);
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	public ServerSocket createServerSocket(int port, int backlog,
			InetAddress ifAddress) throws IOException {
		return ServerSocketFactory.getDefault().createServerSocket(port,
				backlog, ifAddress);
	}

	/** {@inheritDoc} */
	public SocketCipher acceptedSocket(Socket socket) {
		try {
			StreamCipher newCipher = new StreamCipher();
			newCipher.cipher = Cipher.getInstance(cipher.getAlgorithm());
			newCipher.cipher.init(Cipher.ENCRYPT_MODE, key);
			newCipher.decipher = Cipher.getInstance(decipher.getAlgorithm());
			newCipher.decipher.init(Cipher.DECRYPT_MODE, key);

			CipherOutputStream cos = new CipherOutputStream(
					socket.getOutputStream(), cipher);
			newCipher.os = new ObjectOutputStream(cos);
			CipherInputStream cis = new CipherInputStream(
					socket.getInputStream(), decipher);
			newCipher.is = new ObjectInputStream(cis);
			return newCipher;
		} catch (Exception e) {
			LogUtils.logError(Gateway.getInstance().context, getClass(),
					"acceptedSocket", new String[] { "unexpected Exception" },
					e);
			return null;
		}
	}

	/** {@inheritDoc} */
	public Socket createClientSocket(InetAddress host, int port)
			throws IOException {
		Socket socket = SocketFactory.getDefault().createSocket(host, port);
		CipherOutputStream cos = new CipherOutputStream(
				socket.getOutputStream(), cipher);
		os = new ObjectOutputStream(cos);
		CipherInputStream cis = new CipherInputStream(socket.getInputStream(),
				decipher);
		is = new ObjectInputStream(cis);
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
			LogUtils.logError(Gateway.getInstance().context, getClass(),
					"readMessage", new String[] { "unexpected Exception" }, e);
			return null;
		}
	}

}
