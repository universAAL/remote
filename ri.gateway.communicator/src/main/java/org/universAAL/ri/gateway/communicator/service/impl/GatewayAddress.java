package org.universAAL.ri.gateway.communicator.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GatewayAddress {
    
    public final String PROTOCOL = "http";
    private URL url;
    
    public GatewayAddress(final String host, final int port) throws MalformedURLException{
	this.url = new URL(PROTOCOL, host, port, "/"
		+ CommunicatorStarter
		.createAlias(null));
    }
    
    public GatewayAddress(final String host, final int port, final String id)
	    throws MalformedURLException {
	this.url = new URL(PROTOCOL, host, port, "/"
		+ CommunicatorStarter
		.createAlias(id));
    }

    public URL getUrl() {
	return url;
    }

}
