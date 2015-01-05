/*
Copyright 2011-2014 AGH-UST, http://www.agh.edu.pl
Faculty of Computer Science, Electronics and Telecommunications
Department of Computer Science

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
package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.communication.cipher.Cipher;

/**
 * @deprecated Using old Message class, remove when it is using the new Message
 *             class.
 */
public enum Serializer {
    Instance;

    public <T> T unmarshallObject(final Class<T> clazz, final Object obj,
            final ClassLoader cl) throws IOException, ClassNotFoundException {
        final MessageContentSerializer contentSerializer = Gateway
                .getInstance().serializer.getObject();
        // return (T) Base64.decodeToObject((String) obj, Base64.NO_OPTIONS,
        // cl);
        return (T) contentSerializer.deserialize((String) obj);
    }

    public static void cypherAndSend(final byte[] data, final OutputStream out,
            final Cipher cipher) throws IOException, CryptoException {

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
     * Reads a message wrapper from a stream.
     *
     * @param ois
     *            ObjectInputStream to read from a
     * @param cipher
     *            TODO
     * @return a message wrapper
     * @throws IOException
     *             io exception
     * @throws ClassNotFoundException
     *             deserulization exception
     * @throws CryptoException
     */
    public static org.universAAL.ri.gateway.protocol.Message readAndDecypher(
            final InputStream is, final Cipher cipher) throws IOException,
            ClassNotFoundException, CryptoException {

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
        final org.universAAL.ri.gateway.protocol.Message msg = (org.universAAL.ri.gateway.protocol.Message) toObject(decrypted);

        return msg;
    }

    private static byte[] toByteArray(final Object obj) throws IOException {
        byte[] bytes = null;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        bos.close();
        bytes = bos.toByteArray();
        return bytes;
    }

    private static Object toObject(final byte[] bytes) throws IOException,
            ClassNotFoundException {
        Object obj = null;
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final ObjectInputStream ois = new ObjectInputStream(bis);
        obj = ois.readObject();
        return obj;
    }

    public static class EncryptionWrapper implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 8161296430726209292L;

        public EncryptionWrapper(final byte[] payload) {
            this.payload = payload;
        }

        private final byte[] payload;

        public byte[] getPayload() {
            return payload;
        }

    }
}
