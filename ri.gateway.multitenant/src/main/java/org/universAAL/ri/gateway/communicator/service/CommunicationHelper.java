/*
    Copyright 2015-2015 CNR-ISTI, http://isti.cnr.it
    Institute of Information Science and Technologies
    of the Italian National Research Council

    See the NOTICE file distributed with this work for additional
    information regarding copyright ownership

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.universAAL.ri.gateway.communicator.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.ri.gateway.communication.cipher.Cipher;
import org.universAAL.ri.gateway.protocol.Message;

/**
 *
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano "Kismet" Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * @since 3.3.0
 *
 */
public class CommunicationHelper {

    /**
     *
     * This method encrypt and send the given data by adding at the begging of
     * the encrypted data the length of the data that will be sent
     *
     * @param msg
     *            the {@link Message} to encrypt and to send over the stream
     * @param out
     *            the stream where to send the data
     * @param cipher
     *            the {@link Cipher} to use for encrypting the original data
     *            content
     *
     * @throws IOException
     * @throws CryptoException
     */
    public static void cypherAndSend(final Message msg, final OutputStream out,
            final Cipher cipher) throws IOException, CryptoException {
        cypherAndSend(msg.getBytes(), out, cipher);
    }

    /**
     *
     * This method encrypt and send the given data by adding at the begging of
     * the encrypted data the length of the data that will be sent
     *
     * @param data
     *            the data to encrypt and to send over the stream
     * @param out
     *            the stream where to send the data
     * @param cipher
     *            the {@link Cipher} to use for encrypting the original data
     *            content
     *
     * @throws IOException
     * @throws CryptoException
     */
    private static void cypherAndSend(final byte[] data,
            final OutputStream out, final Cipher cipher) throws IOException,
            CryptoException {

        final byte[] encrypted = cipher.encrypt(data);

        int size = encrypted.length;

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(size);
        byte[] intAsByte = buffer.array();
        out.write(intAsByte);
        out.write(encrypted);
        out.flush();
    }

    /**
     * Read data from a stream and decrypt it and convert to a {@link Message}.
     * The protocol read at the beginning 4 bytes representing the actual size
     * of the encrypted message that will follow.
     *
     * @param ois
     *            ObjectInputStream to read from a
     * @param cipher
     *            the {@link Cipher} to use for decrypting the data
     * @return the decrypted message
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws CryptoException
     */
    public static Message readAndDecypher(final InputStream is,
            final Cipher cipher) throws IOException, ClassNotFoundException,
            CryptoException {

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

        final byte[] decrypted = cipher.decrypt(dataBuffer);
        final ByteArrayInputStream bis = new ByteArrayInputStream(decrypted);
        final ObjectInputStream ois = new ObjectInputStream(bis);
        final Message msg = (Message) ois.readObject();

        return msg;
    }
}
