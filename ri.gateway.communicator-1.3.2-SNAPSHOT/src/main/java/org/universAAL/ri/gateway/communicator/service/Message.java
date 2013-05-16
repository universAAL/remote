package org.universAAL.ri.gateway.communicator.service;

import java.io.Serializable;

/**
 * This class represents a generic message sent between inside and outside world
 * of AALSpace.
 * 
 * @author skallz
 */
public class Message implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2259161945843015685L;

    /**
     * Content of the message.
     */
    private Object content;
    
    private String remoteProxyRegistrationId;
    
    private String remoteMemberId;
    
    /**
     * Initializes with a message to send.
     * 
     * @param content
     *            content of the message
     */
    public Message(final Object content) {
	this.content = content;
    }

    /**
     * Returns content of the message.
     * 
     * @return content of the message
     */
    public Object getContent() {
	return content;
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
	if (obj == null || !(obj instanceof Message)) {
	    return false;
	}
	Message o = (Message) obj;
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

    public String getRemoteProxyRegistrationId() {
	return remoteProxyRegistrationId;
    }

    public void setRemoteProxyRegistrationId(String remoteProxyRegistrationId) {
	this.remoteProxyRegistrationId = remoteProxyRegistrationId;
    }

	public String getRemoteMemberId() {
		return remoteMemberId;
	}

	public void setRemoteMemberId(String remoteMemberId) {
		this.remoteMemberId = remoteMemberId;
	}

}
