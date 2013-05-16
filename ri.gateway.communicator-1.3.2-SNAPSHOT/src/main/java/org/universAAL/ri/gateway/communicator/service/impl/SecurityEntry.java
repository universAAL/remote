package org.universAAL.ri.gateway.communicator.service.impl;

public class SecurityEntry {
	private String entryRegex;
	private SecurityAction action;
	private Type type;
	
	public SecurityEntry(SecurityAction action, Type type, String entryRegex) {
		this.action = action;
		this.type = type;
		this.entryRegex = entryRegex;
	}

	public String getEntryRegex() {
		return entryRegex;
	}

	public void setEntryRegex(String entryRegex) {
		this.entryRegex = entryRegex;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public SecurityAction getAction() {
		return action;
	}

	public void setAction(SecurityAction action) {
		this.action = action;
	}
}

