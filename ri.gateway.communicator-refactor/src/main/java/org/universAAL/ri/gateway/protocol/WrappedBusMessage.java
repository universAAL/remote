/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.protocol;

import org.universAAL.middleware.bus.msg.BusMessage;
import org.universAAL.middleware.rdf.ScopedResource;

/**
 * Message used to forward a {@link BusMessage} (ServiceRequest,
 * ServiceResponse, ContextEvent, UIRequest UIResponse).
 * 
 * @author amedrano
 * 
 */
public class WrappedBusMessage extends Message {

    /**
     * The serial Version.
     */
    private static final long serialVersionUID = 7828528558396004815L;

    /**
     * Destination of this {@link WrappedBusMessage}, i.e: the BusMemberId of
     * the proxy.
     */
    private final String destination;

    /**
     * The content of the message.
     */
    private String content;

    /**
     * Constructor of a wrapped Message.
     * 
     * @param destination
     *            the remote BusMemberId how should handle the message.
     * @param busMessage
     *            The message it self.
     */
    public WrappedBusMessage(final String destination,
	    final ScopedResource busMessage) {
	super();
	this.destination = destination;
    }

    @Override
    public String toString() {
	if (content != null) {
	    return "content: " + content.toString();
	} else {
	    return "content: " + "null";
	}
    }

    @Override
    public boolean equals(final Object obj) {
	if (obj == null || !(obj instanceof WrappedBusMessage)) {
	    return false;
	}
	final WrappedBusMessage o = (WrappedBusMessage) obj;
	if (content == null && o.content == null) {
	    return true;
	}
	return content.equals(o.content);
    }

    @Override
    public int hashCode() {
	if (content == null) {
	    return 0;
	}
	return content.hashCode();
    }

    /**
     * Returns content of the message.
     * 
     * @return content of the message
     */
    public String getContent() {
	return this.content;
    }

    /**
     * Get the destination Proxy BusMemberID.
     * 
     * @return
     */
    public String getRemoteProxyRegistrationId() {
	return destination;
    }

    /**
     * Un marshal the Content into a BusMessage.
     * 
     * @return
     */
    public ScopedResource getMessage() {

    }

}
