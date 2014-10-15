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

import java.io.Serializable;
import java.net.URL;
import java.util.UUID;

import org.universAAL.ri.gateway.communicator.service.Message;

/**
 * A message wrapper for requests and responses sent between communicators.
 * 
 * @author skallz
 * 
 * @deprecated
 */
@Deprecated
public class MessageWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 900568682837234611L;

    /**
     * An ID to identify during asynchronous communication.
     */
    private final UUID id;
    /**
     * Type of the message.
     */
    private final MessageType type;
    /**
     * The message.
     */
    private final Message message;
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
