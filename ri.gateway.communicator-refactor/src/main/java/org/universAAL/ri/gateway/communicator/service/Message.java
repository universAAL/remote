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
package org.universAAL.ri.gateway.communicator.service;

import java.io.Serializable;

/**
 * This class represents a generic message sent between inside and outside world
 * of AALSpace.
 * 
 * @author skallz
 * @deprecated
 */
@Deprecated
public class Message implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2259161945843015685L;

    /**
     * Content of the message.
     */
    private final Object content;

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
	final Message o = (Message) obj;
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
    public Object getContent() {
	return content;
    }

    public String getRemoteProxyRegistrationId() {
	return remoteProxyRegistrationId;
    }

    public void setRemoteProxyRegistrationId(
	    final String remoteProxyRegistrationId) {
	this.remoteProxyRegistrationId = remoteProxyRegistrationId;
    }

    public String getRemoteMemberId() {
	return remoteMemberId;
    }

    public void setRemoteMemberId(final String remoteMemberId) {
	this.remoteMemberId = remoteMemberId;
    }

}
