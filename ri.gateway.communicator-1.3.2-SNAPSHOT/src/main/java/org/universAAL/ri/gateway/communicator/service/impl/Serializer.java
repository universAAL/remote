package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.bouncycastle.crypto.CryptoException;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.tracker.Activator;
import org.universAAL.ri.gateway.communicator.service.Message;

public enum Serializer {
    Instance;

    private static MessageContentSerializerEx contentSerializer = null;

    static {
	contentSerializer = (MessageContentSerializerEx) Activator.mc
		.getContainer()
		.fetchSharedObject(
			Activator.mc,
			new Object[] { MessageContentSerializerEx.class.getName() });
    }

    public Message marshallObject(final Object objectToMarshall) throws IOException {
	return new Message(contentSerializer.serialize(objectToMarshall));
	//Object serialized = Base64.encodeObject(objectToMarshall);
	//Message m = new Message(serialized);
	//return m;
    }

    public <T> T unmarshallObject(final Class<T> clazz, final Message message) throws IOException, ClassNotFoundException {
	//return (T) Base64.decodeToObject((String) message.getContent());
	return (T) contentSerializer.deserialize((String) message.getContent());
    }
    
    public <T> T unmarshallObject(final Class<T> clazz, Object obj, ClassLoader cl) throws IOException, ClassNotFoundException {
//	return (T) Base64.decodeToObject((String) obj, Base64.NO_OPTIONS, cl);
	return (T) contentSerializer.deserialize((String) obj);
    }
    
    public Message marshall(Object obj) {
	return new Message(obj);
    }
    
    public <T> T unmarshall(final Class<T> clazz, final Message message){
	return (T)message.getContent();
    }
    
    /**
     * Deserializes the wrapper and sends to a ready stream.
     * 
     * @param wrp
     *            the wrapper
     * @param out
     *            the stream
     * @throws IOException
     *             io exception
     * @throws CryptoException 
     */
    public static void sendMessageToStream(final MessageWrapper wrp,
	    final OutputStream out) throws IOException, CryptoException {
    
    byte[] encrypted = SecurityUtils.Instance.encrypt(toByteArray(wrp));
    
    EncryptionWrapper enc = new EncryptionWrapper(encrypted);	
    
    ObjectOutputStream oos = new ObjectOutputStream(out);
	oos.writeObject(enc);
	oos.flush();
    }
    
    /**
     * Reads a message wrapper from a stream.
     * 
     * @param is
     *            input stream to read from a
     * @return a message wrapper
     * @throws IOException
     *             io exception
     * @throws ClassNotFoundException
     *             deserulization exception
     * @throws CryptoException 
     */
    public static MessageWrapper unmarshalMessage(final InputStream is)
	    throws IOException, ClassNotFoundException, CryptoException {
    
    ObjectInputStream ois = new ObjectInputStream(is);
    EncryptionWrapper enc = (EncryptionWrapper) ois.readObject();
    
    byte[] decrypted = SecurityUtils.Instance.decrypt(enc.getPayload());
	MessageWrapper wrap = (MessageWrapper) toObject(decrypted);

	//ois.close();
	return wrap;
    }
    
    private static byte[] toByteArray (Object obj) throws IOException
    {
      byte[] bytes = null;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos); 
        oos.writeObject(obj);
        oos.flush(); 
        oos.close(); 
        bos.close();
        bytes = bos.toByteArray ();
      return bytes;
    }
        
    private static Object toObject (byte[] bytes) throws IOException, ClassNotFoundException
    {
      Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
        ObjectInputStream ois = new ObjectInputStream (bis);
        obj = ois.readObject();
      return obj;
    }
    
}
