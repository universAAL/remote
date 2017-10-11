package org.universAAL.ri.gateway.communicator.service.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.bouncycastle.crypto.CryptoException;
import org.junit.Test;
import org.universAAL.ri.gateway.communication.cipher.ClearTextCipher;
import org.universAAL.ri.gateway.communication.cipher.SocketCipher;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * This class test marshalling and unmarshalling of message, to avoid that
 * changes in the code breaks the communication level
 * 
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2015-01-05 18:58:58 +0100
 *          (Mon, 05 Jan 2015) $)
 * 
 */
public class SerializerTest {
	final int TCP_TEST_PORT = 7777;

	private InetAddress getNonLoopbackInterface() {
		InetAddress using = null;

		Enumeration<NetworkInterface> ifs = null;
		try {
			ifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			System.out
					.println("Unable to test actual communication because we couldn't find any network interfaces");
			return null;
		}
		while (ifs.hasMoreElements()) {
			NetworkInterface netIf = (NetworkInterface) ifs.nextElement();
			Enumeration<InetAddress> addresses = netIf.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = (InetAddress) addresses.nextElement();
				if (address.isLoopbackAddress())
					continue;
				if (address.isMulticastAddress())
					continue;
				if (address instanceof Inet6Address)
					continue;
				using = address;
				break;
			}
			if (using != null)
				break;
		}
		if (using == null) {
			System.out
					.println("Unable to test actual communication because we couldn't find any no-loopback IPv4 address bound to the interface");
			return null;
		}
		System.out.println(using);
		return using;
	}

	private class PingServer implements Runnable {
		final double MAXIMUM_WAITING_TIME = 500.0;

		private SocketCipher cipher;
		private boolean stop = true;

		/**
		 * 
		 */
		public PingServer(SocketCipher cipher) {
			this.cipher = cipher;
		}

		/** {@inheritDoc} */
		public void run() {
			ServerSocket server;
			SocketCipher c = null;
			try {
				server = cipher.createServerSocket(TCP_TEST_PORT, 1,
						getNonLoopbackInterface());
				// TODO notify readiness to receive.
				Socket serverPart = server.accept();
				stop = false;
				notifyAll();
				c = cipher.acceptedSocket(serverPart);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			while (!stop) {
				try {
					Message m = c.readMessage();
					c.sendMessage(m);
				} catch (CryptoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * @return
		 */
		public boolean isReady() {
			return !stop;
		}
	}

	private void testPing(SocketCipher scipher, SocketCipher ccipher)
			throws Exception {
		// setup server
		PingServer ps = new PingServer(scipher);
		Thread serverThread = new Thread(ps);
		serverThread.start();

		// synchornize with server
		while (!ps.isReady()) {
			try {
				synchronized (ps) {
					ps.wait();
				}
			} catch (InterruptedException e) {
			}
		}

		// connect client
		Socket sc = ccipher.createClientSocket(getNonLoopbackInterface(),
				TCP_TEST_PORT);
		for (int i = 0; i < 250; i++) {
			Message m = new ErrorMessage("testMessage: " + Integer.toString(i));
			ccipher.sendMessage(m);
			Message rm = ccipher.readMessage();
			assertEquals("Sent Message and received Message are not the same",
					((ErrorMessage) m).getDescription(),
					((ErrorMessage) rm).getDescription());
		}
		ps.stop = true;
	}

	private void testSequence(SocketCipher scipher, SocketCipher ccipher)
			throws Exception {
		// setup server
		PingServer ps = new PingServer(scipher);
		Thread serverThread = new Thread(ps);
		serverThread.start();

		// synchornize with server
		while (!ps.isReady()) {
			try {
				ps.wait();
			} catch (InterruptedException e) {
			}
		}

		// connect client
		Socket sc = ccipher.createClientSocket(getNonLoopbackInterface(),
				TCP_TEST_PORT);
		int nsm = 100 + (int) (Math.random() * ((250 - 100) + 1));
		for (int i = 0; i < nsm; i++) {
			Message m = new ErrorMessage("testMessage: " + Integer.toString(i));
			ccipher.sendMessage(m);
		}
		int nrm = 0;
		while (ccipher.readMessage() != null) {
			nrm++;
		}
		assertEquals("Number of Sent and received messages is not equal", nsm,
				nrm);
		ps.stop = true;
	}

	@Test
	public void testSimple() throws Exception {
		SocketCipher sc = new ClearTextCipher();
		SocketCipher cc = new ClearTextCipher();
		testPing(sc, cc);
		testSequence(sc, cc);
	}

	// @Test
	// public void testSSL() throws Exception {
	// SocketCipher sc = new SSLCipher();
	// SocketCipher cc = new SSLCipher();
	// testPing(sc, cc);
	// testSequence(sc, cc);
	// }

}
