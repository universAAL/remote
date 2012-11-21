package org.universAAL.ri.gateway.eimanager.impl.registry;

import org.universAAL.ri.gateway.eimanager.impl.InternalEIOperation;

public abstract class RegistryEntry {
    private String id;
    private InternalEIOperation operation;
    
    public RegistryEntry(String id, InternalEIOperation operation){
	this.id = id;
	this.operation = operation;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public InternalEIOperation getOperation() {
	return operation;
    }

    public void setOperation(InternalEIOperation operation) {
	this.operation = operation;
    }
    
}
