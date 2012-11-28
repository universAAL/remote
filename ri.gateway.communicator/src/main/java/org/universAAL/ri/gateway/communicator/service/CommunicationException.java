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
