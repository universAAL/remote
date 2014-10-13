package org.universAAL.ri.gateway.communicator.service.impl;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import org.universAAL.ri.gateway.communication.cipher.Blowfish;

/**
 * This class test marshalling and unmarshalling of message, to avoid that
 * changes in the code breaks the communication level
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
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

}
