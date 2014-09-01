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

import org.universAAL.ri.gateway.operations.ImportOpertaionChain;
import org.universAAL.ri.gateway.operations.OperationChain;

/**
 * @author amedrano
 * 
 */
public class ConfigurationFile extends UpdatedPropertiesFile implements
	Configuration, PropertiesFileKeys {

    public ConfigurationFile(final File propFile) {
	super(propFile);
    }

    @Override
    public String getComments() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    protected void addDefaults(final Properties defaults) {
	// TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public RoutingMode getRoutingMode() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public ConnectionMode getConnectionMode() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public String getConnectionHost() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public int getConnectionPort() {
	// TODO Auto-generated method stub
	return 0;
    }

    /** {@inheritDoc} */
    public ImportOpertaionChain getImportOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public ExportOperationChain getExportOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public OperationChain getIncomingMessageOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

    /** {@inheritDoc} */
    public OperationChain getOutgoingMessageOperationChain() {
	// TODO Auto-generated method stub
	return null;
    }

}
