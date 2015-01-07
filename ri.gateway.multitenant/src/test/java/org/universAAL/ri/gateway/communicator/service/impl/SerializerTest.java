package org.universAAL.ri.gateway.communicator.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.junit.Test;
import org.universAAL.ri.gateway.communication.cipher.Blowfish;
import org.universAAL.ri.gateway.communicator.service.CommunicationHelper;
import org.universAAL.ri.gateway.protocol.ErrorMessage;
import org.universAAL.ri.gateway.protocol.Message;

/**
 * This class test marshalling and unmarshalling of message, to avoid that
 * changes in the code breaks the communication level
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2015-01-05 18:58:58
 *          +0100 (Mon, 05 Jan 2015) $)
 *
 */
public class SerializerTest {

    @Test
    public void testSendMessageToStream() {

        ErrorMessage wrap = new ErrorMessage("Hello World Sending!");
        Blowfish cipher = new Blowfish("A radom key");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            CommunicationHelper.cypherAndSend(wrap, output, cipher);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed due to exception");
        }
        ByteArrayInputStream input = new ByteArrayInputStream(
                output.toByteArray());
        Message readBack = null;
        try {
            readBack = CommunicationHelper.readAndDecypher(input, cipher);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed due to exception");
        }
        assertEquals("Checking message type", wrap.getClass(),
                readBack.getClass());
        assertEquals("Checking message content", wrap.getDescription(),
                ((ErrorMessage) readBack).getDescription());
    }

    @Test
    public void testSendingMessage() {
        final int TCP_TEST_PORT = 7777;
        final int TOTALE_MESSAGE_SENT = 250;
        final double MAXIMUM_WAITING_TIME = 500.0;
        Enumeration<NetworkInterface> ifs = null;
        try {
            ifs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            System.out
                    .println("Unable to test actual communication because we couldn't find any network interfaces");
            return;
        }
        InetAddress using = null;
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
            return;
        }
        final InetAddress selected = using;
        System.out.println(using);
        final ErrorMessage expected = new ErrorMessage("Hello World Sending!");
        final Blowfish cipher = new Blowfish("A radom key");
        Thread serverThread = new Thread(new Runnable() {

            public void run() {
                ServerSocket server;
                try {
                    server = new ServerSocket(TCP_TEST_PORT, 1, selected);
                    Socket serverPart = server.accept();
                    for (int i = 0; i < TOTALE_MESSAGE_SENT; i++) {
                        Message readBack = null;
                        try {
                            readBack = CommunicationHelper.readAndDecypher(
                                    serverPart.getInputStream(), cipher);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail("Failed due to exception");
                        }
                        assertEquals("Checking message type",
                                expected.getClass(), readBack.getClass());
                        assertEquals("Checking message content",
                                expected.getDescription(),
                                ((ErrorMessage) readBack).getDescription());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Failed due to exception");
                }

            }
        });
        serverThread.start();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException ex) {
        }
        try {
            Socket client = new Socket(selected, TCP_TEST_PORT);
            for (int i = 0; i < TOTALE_MESSAGE_SENT; i++) {
                CommunicationHelper.cypherAndSend(expected,
                        client.getOutputStream(), cipher);
                double waiting = Math.random() * MAXIMUM_WAITING_TIME;
                Thread.sleep((long) waiting);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed due to exception");
        }
        try {
            serverThread.join(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse("Expected everythign stopped as expected",
                serverThread.isAlive());
    }

}
