package org.universAAL.ri.gateway.eimanager.impl.exporting;

import java.io.Serializable;

public class ProxyRegistration implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 7598585122564189371L;
    private String id;
    private Object returnedValues;
    
    public ProxyRegistration(String id, Object returnedValues) {
	super();
	this.id = id;
	this.returnedValues = returnedValues;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Object getReturnedValues() {
	return returnedValues;
    }

    public void setReturnedValues(Object returnedValues) {
	this.returnedValues = returnedValues;
    }
    
    
}
