/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.configuration;

import java.io.File;
import java.util.Properties;

import org.universAAL.ri.gateway.operations.ExportOpertaionChain;
import org.universAAL.ri.gateway.operations.ImportOpertaionChain;
import org.universAAL.ri.gateway.operations.MessageOperationChain;

/**
 * @author amedrano
 * 
 */
public class ConfigurationFile extends UpdatedPropertiesFile implements
	Configuration, PropertiesFileKeys {

    private static final String CLIENT = "CLIENT";
    private static final String SERVER = "SERVER";
    private static final String FORWARD = "FORWARD";
    private static final String ROUTER = "ROUTER";
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ConfigurationFile(final File propFile) {
	super(propFile);
    }

    @Override
    public String getComments() {
	return "Configuration of a connection instance";
    }

    @Override
    protected void addDefaults(final Properties defaults) {
	// TODO add Deny all imports and exports by default.
	defaults.put(ROUTING_MODE, ROUTER);
	defaults.put(CONNECTION_MODE, CLIENT);
    }

    /** {@inheritDoc} */
    public RoutingMode getRoutingMode() {
	if (((String) get(ROUTING_MODE)).equalsIgnoreCase(ROUTER)) {
	    return RoutingMode.ROUTER;
	} else if (((String) get(ROUTING_MODE)).equalsIgnoreCase(FORWARD)) {
	    return RoutingMode.FORWARD;
	}
	return null;
    }

    /** {@inheritDoc} */
    public ConnectionMode getConnectionMode() {
	if (((String) get(CONNECTION_MODE)).equalsIgnoreCase(SERVER)) {
	    return ConnectionMode.SERVER;
	} else if (((String) get(CONNECTION_MODE)).equalsIgnoreCase(CLIENT)) {
	    return ConnectionMode.CLIENT;
	}
	return null;
    }

    /** {@inheritDoc} */
    public String getConnectionHost() {
	return (String) get(REMOTE_HOST);
    }

    /** {@inheritDoc} */
    public int getConnectionPort() {
	return Integer.parseInt((String) get(SOCKET_PORT));
    }

    /** {@inheritDoc} */
    public ImportOpertaionChain getImportOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public ExportOpertaionChain getExportOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public MessageOperationChain getIncomingMessageOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public MessageOperationChain getOutgoingMessageOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

}
