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

/**
 * Exception for unexpected behavior during communication with remote
 * communicators the response message (like connection failure,
 * (de)serialization failure).
 * 
 * @author skallz
 */
public class CommunicationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7480117622944127235L;

    /**
     * @see CommunicationException(Exception ex);
     * @param ex
     *            the cause
     */
    public CommunicationException(final Exception ex) {
	super(ex);
    }

    /**
     * @see CommunicationException(String msg);
     * @param msg
     *            the message
     */
    public CommunicationException(final String msg) {
	super(msg);
    }

}
