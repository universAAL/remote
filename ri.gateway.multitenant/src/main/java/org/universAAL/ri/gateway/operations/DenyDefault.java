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
package org.universAAL.ri.gateway.operations;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ScopedResource;

/**
 * Dummy class to allow everything. <H1>WARNING</H1> not secure at all.
 * 
 * @author amedrano
 * 
 */
public class DenyDefault implements MessageOperationChain,
	ParameterCheckOpertaionChain, OperationChainManager {

    public OperationResult check(final Resource[] params) {
	return OperationResult.DENY;
    }

    public OperationResult check(final ScopedResource message) {
	return OperationResult.DENY;
    }

    /** {@inheritDoc} */
    public ParameterCheckOpertaionChain getImportOperationChain() {
	return this;
    }

    /** {@inheritDoc} */
    public ParameterCheckOpertaionChain getExportOperationChain() {
	return this;
    }

    /** {@inheritDoc} */
    public MessageOperationChain getIncomingMessageOperationChain() {
	return this;
    }

    /** {@inheritDoc} */
    public MessageOperationChain getOutgoingMessageOperationChain() {
	return this;
    }

}
