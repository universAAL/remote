package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.Serializable;

public class ProxyRegistration implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 7598585122564189371L;
    private String id;
    private Object returnedValues;
    private boolean success;
    private String errorMessage;
    
    public ProxyRegistration(final String id, final Object returnedValues) {
	super();
	this.id = id;
	this.returnedValues = returnedValues;
	this.success = true;
	this.errorMessage = "";
    }

    public ProxyRegistration(String errorMessage){
    	this.id = "";
    	this.returnedValues = "";
    	this.success = false;
    	this.errorMessage = errorMessage;
    }
    
    public String getId() {
	return id;
    }

    public void setId(final String id) {
	this.id = id;
    }

    public Object getReturnedValues() {
	return returnedValues;
    }

    public void setReturnedValues(final Object returnedValues) {
	this.returnedValues = returnedValues;
    }
    
    @Override
    public String toString() {
	return String.format("Proxy: id: %s, returns: %s", id, returnedValues);
    }

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
    
}
