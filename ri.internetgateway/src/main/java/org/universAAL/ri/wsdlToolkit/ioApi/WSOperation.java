/*
	Copyright 2007-2014 CERTH-ITI, http://www.iti.gr
	Centre of Research and Technology Hellas 
	Information Technologies Institute

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
package org.universAAL.ri.wsdlToolkit.ioApi;

public class WSOperation {

	private String operationName;
	private WSOperationInput hasInput;
	private WSOperationOutput hasOutput;
	private String hasDocumentation;
	private String hasStyle; // rpc or document
	private ParsedWSDLDefinition belongsToDefinition; // mporei na einai
	private String hasBindingSoapAction;

	// public String hasSoapVersion;

	public WSOperationInput getHasInput() {
		return hasInput;
	}

	public void setHasInput(WSOperationInput hasInput) {
		this.hasInput = hasInput;
	}

	public WSOperationOutput getHasOutput() {
		return hasOutput;
	}

	public void setHasOutput(WSOperationOutput hasOutput) {
		this.hasOutput = hasOutput;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getHasDocumentation() {
		return hasDocumentation;
	}

	public void setHasDocumentation(String hasDocumentation) {
		this.hasDocumentation = hasDocumentation;
	}

	public String getHasStyle() {
		return hasStyle;
	}

	public void setHasStyle(String hasStyle) {
		this.hasStyle = hasStyle;
	}

	public ParsedWSDLDefinition getBelongsToDefinition() {
		return belongsToDefinition;
	}

	public void setBelongsToDefinition(ParsedWSDLDefinition belongsToDefinition) {
		this.belongsToDefinition = belongsToDefinition;
	}

	public String getHasBindingSoapAction() {
		return hasBindingSoapAction;
	}

	public void setHasBindingSoapAction(String hasBindingSoapAction) {
		this.hasBindingSoapAction = hasBindingSoapAction;
	}

}
