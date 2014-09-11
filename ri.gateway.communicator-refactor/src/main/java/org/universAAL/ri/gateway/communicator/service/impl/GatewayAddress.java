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
package org.universAAL.ri.gateway.communicator.service.impl;

import java.net.MalformedURLException;
import java.net.URL;

public class GatewayAddress {

    /**
     * Alias' prefix under which the HTTP servlet will be registered.
     */
    static String ALIAS_PREFIX = "gateway-communicator";

    public final String PROTOCOL = "http";
    private final URL url;

    public GatewayAddress(final String host, final int port)
	    throws MalformedURLException {
	this.url = new URL(PROTOCOL, host, port, "/" + createAlias(null));
    }

    public GatewayAddress(final String host, final int port, final String id)
	    throws MalformedURLException {
	this.url = new URL(PROTOCOL, host, port, "/" + createAlias(id));
    }

    public URL getUrl() {
	return url;
    }

    static String createAlias(final String id) {
	if (id != null && !"".equals(id)) {
	    return ALIAS_PREFIX + "-" + id;
	} else {
	    return ALIAS_PREFIX;
	}
    }

}
