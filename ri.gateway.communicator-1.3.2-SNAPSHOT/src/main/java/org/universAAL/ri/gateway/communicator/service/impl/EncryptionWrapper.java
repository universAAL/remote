package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.Serializable;

public class EncryptionWrapper implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8161296430726209292L;

	public EncryptionWrapper(byte[] payload){
		this.payload = payload;
	}
	
	private byte[] payload;

	public byte[] getPayload() {
		return payload;
	}

}
