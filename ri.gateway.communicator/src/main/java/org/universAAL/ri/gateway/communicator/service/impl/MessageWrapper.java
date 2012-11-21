package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

import org.universAAL.ri.gateway.communicator.service.Message;

/**
 * A message wrapper for requests and responses sent between communicators.
 * 
 * @author skallz
 * 
 */
public class MessageWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 900568682837234611L;

    /**
     * An ID to identify during asynchronous communication.
     */
    private UUID id;
    /**
     * Type of the message.
     */
    private MessageType type;
    /**
     * The message.
     */
    private Message message;
    /**
     * where to send back the response (for asynchronous communication only).
     */
    private URL returnTo;
    
    private String sourceId;
    
    /**
     * Wraps a message with a concrete ID (for responses).
     * 
     * @param type
     *            type of the message
     * @param message
     *            the message
     * @param id
     *            ID
     */
    public MessageWrapper(final MessageType type, final Message message,
	    final UUID id, final String sourceId) {
	this.type = type;
	this.message = message;
	this.id = id;
	this.sourceId = sourceId;
    }

    /**
     * Wraps a message with a random ID.
     * 
     * @param type
     *            type of the message
     * @param message
     *            the message
     */
    public MessageWrapper(final MessageType type, final Message message,
	    final String sourceId) {
	this(type, message, UUID.randomUUID(), sourceId);
    }

    /**
     * Wraps an asynchronous request stating where it should be sent back.
     * 
     * @param type
     *            type of the message
     * @param message
     *            the message
     * @param returnTo
     *            where to send it back
     */
    public MessageWrapper(final MessageType type, final Message message,
	    final URL returnTo, final String sourceId) {
	this(type, message, sourceId);
	this.returnTo = returnTo;
    }

    @Override
    public String toString() {
	return String.format("msg:%s id:%s", message, id);
    }

    /**
     * @return the id
     */
    public UUID getId() {
	return id;
    }

    /**
     * @return the type
     */
    public MessageType getType() {
	return type;
    }

    /**
     * @return the message
     */
    public Message getMessage() {
	return message;
    }

    /**
     * @return the returnTo
     */
    public URL getReturnTo() {
	return returnTo;
    }

    public String getSourceId() {
	return sourceId;
    }

    public void setSourceId(final String sourceId) {
	this.sourceId = sourceId;
    }

}
