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
import java.net.URL;
import java.util.Properties;

import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.ri.gateway.Gateway;
import org.universAAL.ri.gateway.operations.DenyDefault;
import org.universAAL.ri.gateway.operations.MessageOperationChain;
import org.universAAL.ri.gateway.operations.OperationChainManager;
import org.universAAL.ri.gateway.operations.ParameterCheckOpertaionChain;

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
    
    private OperationChainManager chainMNG = null;

    public ConfigurationFile(final File propFile) {
	super(propFile);
    }

    private OperationChainManager getChainManager() {
	if (chainMNG == null || checkPropertiesVersion()) {
	    try {
		String secFile = getProperty(SECURITY_DEFINITION);
		if (secFile != null) {
		    chainMNG = new TurtleFileSecurityDefinition(
			    new URL(secFile));
		} else {
		    chainMNG = new NoSecurityDefinition();
		}
	    } catch (Exception e) {
		LogUtils.logError(Gateway.getInstance().context, getClass(),
			"getChainManager", new String[] {
				"unable to load file", "default to DenyAll" },
			e);
		chainMNG = new DenyDefault();
	    }
	}
	return chainMNG;
    }
    
    @Override
    public String getComments() {
	return "Configuration of a connection instance";
    }

    @Override
    protected void addDefaults(final Properties defaults) {
	defaults.put(ROUTING_MODE, ROUTER);
	defaults.put(CONNECTION_MODE, CLIENT);
    }

    /** {@inheritDoc} */
    public RoutingMode getRoutingMode() {
	if (getProperty(ROUTING_MODE).equalsIgnoreCase(ROUTER)) {
	    return RoutingMode.ROUTER;
	} else if (getProperty(ROUTING_MODE).equalsIgnoreCase(FORWARD)) {
	    return RoutingMode.FORWARD;
	}
	return null;
    }

    /** {@inheritDoc} */
    public ConnectionMode getConnectionMode() {
	if (getProperty(CONNECTION_MODE).equalsIgnoreCase(SERVER)) {
	    return ConnectionMode.SERVER;
	} else if (getProperty(CONNECTION_MODE).equalsIgnoreCase(CLIENT)) {
	    return ConnectionMode.CLIENT;
	}
	return null;
    }

    /** {@inheritDoc} */
    public String getConnectionHost() {
	return getProperty(REMOTE_HOST);
    }

    /** {@inheritDoc} */
    public int getConnectionPort() {
	return Integer.parseInt(getProperty(SOCKET_PORT));
    }

    /** {@inheritDoc} */
    public ParameterCheckOpertaionChain getImportOperationChain() {
    	return getChainManager().getImportOperationChain();
    }

    /** {@inheritDoc} */
    public ParameterCheckOpertaionChain getExportOperationChain() {
    	return getChainManager().getExportOperationChain();
    }

    /** {@inheritDoc} */
    public MessageOperationChain getIncomingMessageOperationChain() {
    	return getChainManager().getIncomingMessageOperationChain();
    }

    /** {@inheritDoc} */
    public MessageOperationChain getOutgoingMessageOperationChain() {
    	return getChainManager().getOutgoingMessageOperationChain();
    }

    public String getEncryptionKey() {
	return getProperty(HASH_KEY);
    }

}
