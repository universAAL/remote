package org.universAAL.ri.gateway.communicator.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.BindException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.Test;
import org.universAAL.ri.gateway.communication.cipher.ClearTextCipher;
import org.universAAL.ri.gateway.communication.cipher.LegacyBlowfishCipher;
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
	int TCP_TEST_PORT = 7777;
	private PingServer ps;

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
		// System.out.println(using);
		return using;
	}

	private class PingServer implements Runnable {

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
			ServerSocket server = null;
			SocketCipher c = null;
			while (server == null) {
				try {
					server = cipher.createServerSocket(TCP_TEST_PORT, 1,
							getNonLoopbackInterface());
				} catch (BindException be) {
					System.err.println("unable to bind to :"
							+ Integer.toString(TCP_TEST_PORT)
							+ " Trying to reset to: "
							+ Integer.toString(++TCP_TEST_PORT));
				} catch (IOException e1) {
					fail(e1.getMessage());
					return;
				}
			}
			// notify readiness to receive.
			stop = false;
			synchronized (this) {
				notifyAll();
			}
			while (!stop) {
				Socket serverPart = null;
				try {
					serverPart = server.accept();
					c = cipher.acceptedSocket(serverPart);
				} catch (IOException e) {
					continue;
				}
				while (!serverPart.isClosed()) {
					try {
						Message m = c.readMessage();
						c.sendMessage(m);
					} catch (Exception e) {
						break;
					}
				}
			}
			try {
				server.close();
			} catch (IOException e) {
			}
			synchronized (this) {
				notifyAll();
			}
		}
	}

	private void setUpServer(SocketCipher scipher) {
		ps = new PingServer(scipher);
		Thread serverThread = new Thread(ps);
		serverThread.start();

		int i = 3;
		// synchornize with server
		while (ps.stop && i > 0) {
			i--;
			try {
				synchronized (ps) {
					ps.wait(2500);
				}
			} catch (InterruptedException e) {
			}
		}
		if (i <= 0) {
			throw new RuntimeException("unable to start Server");
		}
	}

	private void tearDownServer() {
		ps.stop = true;
		int i = 3;
		// synchornize with server
		while (!ps.stop && i > 0) {
			i--;
			try {
				synchronized (ps) {
					ps.wait(2500);
				}
			} catch (InterruptedException e) {
			}
		}
		if (i <= 0) {
			throw new RuntimeException("unable to stop Server");
		}
	}

	private void testPing(SocketCipher ccipher) throws Exception {
		// connect client
		Socket sc = ccipher.createClientSocket(getNonLoopbackInterface(),
				TCP_TEST_PORT);
		for (int i = 0; i < 250; i++) {
			ErrorMessage m = new ErrorMessage("testPingMessage: "
					+ Integer.toString(i));
			assertTrue(ccipher.sendMessage(m));
			Thread.sleep(10);
			Message rm = ccipher.readMessage();
			assertEquals("Sent Message and received Message are not the same",
					((ErrorMessage) m).getDescription(),
					((ErrorMessage) rm).getDescription());
		}
		sc.close();
	}

	private void testSequence(SocketCipher ccipher) throws Exception {

		// connect client
		Socket sc = ccipher.createClientSocket(getNonLoopbackInterface(),
				TCP_TEST_PORT);
		int nsm = 50 + (int) (Math.random() * ((150 - 50) + 1));
		for (int i = 0; i < nsm; i++) {
			ErrorMessage m = new ErrorMessage("testSeqMessage: "
					+ Integer.toString(i));
			ccipher.sendMessage(m);
		}
		Thread.sleep(10);
		int nrm = 0;
		try {
			while (nrm < nsm && ccipher.readMessage() != null) {
				nrm++;
			}
		} catch (Exception e) {
		}
		sc.close();
		assertEquals("Number of Sent and received messages is not equal", nsm,
				nrm);
	}

	@Test(timeout = 60000)
	public void test1Simple() throws Exception {

		SocketCipher sc = new ClearTextCipher();
		SocketCipher cc = new ClearTextCipher();
		setUpServer(sc);
		testPing(cc);
		testSequence(cc);
		tearDownServer();
	}

	@Test(timeout = 60000)
	public void test2Legacy() throws Exception {
		Properties p = new Properties();
		p.put(LegacyBlowfishCipher.HASH_KEY, "some_Pa$$W0rd");
		SocketCipher sc = new LegacyBlowfishCipher();
		assertTrue(sc.setup(p));
		SocketCipher cc = new LegacyBlowfishCipher();
		assertTrue(cc.setup(p));
		setUpServer(sc);
		testPing(cc);
		testSequence(cc);
		tearDownServer();
	}

	// @Test
	// public void testSSL() throws Exception {
	// SocketCipher sc = new SSLCipher();
	// SocketCipher cc = new SSLCipher();
	// testPing(sc, cc);
	// testSequence(sc, cc);
	// }

}
