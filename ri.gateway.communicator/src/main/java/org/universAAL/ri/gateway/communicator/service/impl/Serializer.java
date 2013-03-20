package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.tracker.Activator;
import org.universAAL.middleware.xsd.util.Base64;
import org.universAAL.ri.gateway.communicator.service.Message;
import org.universAAL.ri.gateway.eimanager.impl.importing.ImportRequest;

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
     */
    public static void sendMessageToStream(final MessageWrapper wrp,
	    final OutputStream out) throws IOException {
	ObjectOutputStream oos = new ObjectOutputStream(out);
	oos.writeObject(wrp);
	oos.flush();
//	oos.close();
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
     */
    public static MessageWrapper unmarshalMessage(final InputStream is)
	    throws IOException, ClassNotFoundException {
	ObjectInputStream ois = new ObjectInputStream(is);
	MessageWrapper wrap = (MessageWrapper) ois.readObject();
//	if (ois.available() != 0) {
//	    throw new IllegalArgumentException("more than one object sent");
//	}
//	ois.close();
	return wrap;
    }
}
