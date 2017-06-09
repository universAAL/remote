/*******************************************************************************
 * Copyright 2015 2011 Universidad PolitÃ©cnica de Madrid
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

import org.universAAL.ri.gateway.operations.AllowDefault;
import org.universAAL.ri.gateway.operations.MessageOperationChain;
import org.universAAL.ri.gateway.operations.OperationChainManager;
import org.universAAL.ri.gateway.operations.ParameterCheckOpertaionChain;

/**
 * @author amedrano
 *
 */
public class NoSecurityDefinition implements OperationChainManager {

	/** {@inheritDoc} */
	public ParameterCheckOpertaionChain getImportOperationChain() {
		return new AllowDefault();
	}

	/** {@inheritDoc} */
	public ParameterCheckOpertaionChain getExportOperationChain() {
		return new AllowDefault();
	}

	/** {@inheritDoc} */
	public MessageOperationChain getIncomingMessageOperationChain() {
		return new AllowDefault();
	}

	/** {@inheritDoc} */
	public MessageOperationChain getOutgoingMessageOperationChain() {
		return new AllowDefault();
	}

}