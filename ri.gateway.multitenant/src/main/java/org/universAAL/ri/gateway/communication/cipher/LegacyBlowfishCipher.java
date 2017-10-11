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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * @author amedrano
 * 
 */
public class LegacyBlowfishCipher implements SocketCipher {

	private PaddedBufferedBlockCipher cipher;
	private KeyParameter key;
	private InputStream is;
	private OutputStream out;
	/**
	 * The Key for encryption.
	 */
	public static final String HASH_KEY = "hash-key";

	/**
	 * 
	 */
	public LegacyBlowfishCipher() {
		cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(
				new BlowfishEngine()));
	}

	/** {@inheritDoc} */
	public boolean setup(Properties props) {
		key = new KeyParameter(props.getProperty(HASH_KEY).getBytes());
		return key != null;
	}

	/** {@inheritDoc} */
	public ServerSocket createServerSocket(int port, int backlog,
			InetAddress ifAddress) throws IOException {
		ServerSocket ssocket = ServerSocketFactory.getDefault()
				.createServerSocket(port, backlog, ifAddress);
		return ssocket;
	}

	/** {@inheritDoc} */
	public SocketCipher acceptedSocket(Socket socket) {
		try {
			LegacyBlowfishCipher newCipher = new LegacyBlowfishCipher();
			newCipher.cipher = new PaddedBufferedBlockCipher(
					new CBCBlockCipher(new BlowfishEngine()));
			newCipher.key = key;
			newCipher.out = socket.getOutputStream();
			newCipher.is = socket.getInputStream();
			return newCipher;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** {@inheritDoc} */
	public Socket createClientSocket(InetAddress host, int port)
			throws IOException {
		Socket socket = SocketFactory.getDefault().createSocket(host, port);
		out = socket.getOutputStream();
		is = socket.getInputStream();
		return socket;
	}

	private byte[] callCipher(final byte[] data) throws CryptoException {
		final int size = cipher.getOutputSize(data.length);
		byte[] result = new byte[size];
		int olen = cipher.processBytes(data, 0, data.length, result, 0);
		olen += cipher.doFinal(result, olen);

		if (olen < size) {
			final byte[] tmp = new byte[olen];
			System.arraycopy(result, 0, tmp, 0, olen);
			result = tmp;
		}

		return result;
	}

	private synchronized byte[] encrypt(final byte[] data)
			throws CryptoException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}

		cipher.init(true, key);
		return callCipher(data);
	}

	private synchronized byte[] decrypt(final byte[] data)
			throws CryptoException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}

		cipher.init(false, key);
		return callCipher(data);
	}

	/**
	 * 
	 * This method encrypt and send the given data by adding at the begging of
	 * the encrypted data the length of the data that will be sent
	 * 
	 */
	public boolean sendMessage(Message m) throws CryptoException, IOException {

		ByteArrayOutputStream output;
		try {
			output = new ByteArrayOutputStream();
			ObjectOutputStream objOut;
			objOut = new ObjectOutputStream(output);
			objOut.writeObject(m);
			objOut.flush();
			objOut.close();

		} catch (IOException e) {
			throw new RuntimeException("Unable to generates bytes", e);
		}

		byte[] data = output.toByteArray();
		;

		final byte[] encrypted = encrypt(data);

		int size = encrypted.length;

		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(size);
		byte[] intAsByte = buffer.array();
		out.write(intAsByte);
		out.write(encrypted);
		out.flush();
		return true;
	}

	/**
	 * Read data from a stream and decrypt it and convert to a {@link Message}.
	 * The protocol read at the beginning 4 bytes representing the actual size
	 * of the encrypted message that will follow.
	 * 
	 * @return the decrypted message
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws CryptoException
	 */
	public Message readMessage() throws CryptoException, IOException {

		int read = 0;
		int idx = 0;
		byte[] intAsArray = new byte[4];
		do {
			read = is.read(intAsArray, idx, intAsArray.length - idx);
			if (read >= 0) {
				idx += read;
			}
		} while (idx < 4);
		ByteBuffer buffer = ByteBuffer.wrap(intAsArray);
		int size = buffer.getInt();
		byte[] dataBuffer = new byte[size];
		idx = 0;
		do {
			read = is.read(dataBuffer, idx, dataBuffer.length - idx);
			if (read >= 0) {
				idx += read;
			}
		} while (idx < size);

		final byte[] decrypted = decrypt(dataBuffer);
		final ByteArrayInputStream bis = new ByteArrayInputStream(decrypted);
		final ObjectInputStream ois = new ObjectInputStream(bis);
		Message msg;
		try {
			msg = (Message) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			msg = null;
		}

		return msg;
	}
}
