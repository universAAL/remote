package org.universAAL.ri.gateway.communicator.service.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.junit.Test;
import org.universAAL.ri.gateway.communication.cipher.Blowfish;
import org.universAAL.ri.gateway.communicator.service.Message;

/**
 * This class test marshalling and unmarshalling of message, to avoid that
 * changes in the code breaks the communication level
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate: 2014-10-13 14:01:58
 *          +0200 (Mon, 13 Oct 2014) $)
 *
 */
public class SerializerTest {

    @Test
    public void testSendMessageToStream() {
        MessageWrapper wrap = new MessageWrapper(MessageType.HighPush,
                new Message("Hello World Sending!"), "JUnit");
        Blowfish cipher = new Blowfish("A radom key");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            Serializer.sendMessageToStream(wrap, output, cipher);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed due to exception");
        }
        ByteArrayInputStream input = new ByteArrayInputStream(
                output.toByteArray());
        MessageWrapper readBack = null;
        try {
            readBack = Serializer.unmarshalMessage(input, cipher);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed due to exception");
        }
        assertEquals("Checking message type", wrap.getType(),
                readBack.getType());
        assertEquals("Checking message content",
                wrap.getMessage().getContent(), readBack.getMessage()
                        .getContent());
        assertEquals("Checking message source", wrap.getSourceId(),
                readBack.getSourceId());
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
        final MessageWrapper expected = new MessageWrapper(
                MessageType.HighPush, new Message("Hello World Sending!"),
                "JUnit");
        final Blowfish cipher = new Blowfish("A radom key");
        Thread serverThread = new Thread(new Runnable() {

            public void run() {
                ServerSocket server;
                try {
                    server = new ServerSocket(TCP_TEST_PORT, 1, selected);
                    Socket serverPart = server.accept();
                    for (int i = 0; i < TOTALE_MESSAGE_SENT; i++) {
                        MessageWrapper readBack = Serializer.unmarshalMessage(
                                serverPart.getInputStream(), cipher);
                        assertEquals("Checking message type",
                                expected.getType(), readBack.getType());
                        assertEquals("Checking message content", expected
                                .getMessage().getContent(), readBack
                                .getMessage().getContent());
                        assertEquals("Checking message source",
                                expected.getSourceId(), readBack.getSourceId());
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
                Serializer.sendMessageToStream(expected,
                        client.getOutputStream(), cipher);
                double waiting =  Math.random() * MAXIMUM_WAITING_TIME;
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
