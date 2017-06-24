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

import java.net.URL;
import java.util.Vector;
import javax.xml.namespace.QName;

/**
 *
 * Class used for storing the parsed definition of a WSDL file
 *
 * @author kgiannou
 */

public class ParsedWSDLDefinition {
	private URL wsdlURL;
	private String serviceURL;
	private QName WebServiceName;
	private String targetNamespaceURI;
	private Vector wsdlOperations = new Vector();

	private Vector containingErrors = new Vector();
	private boolean failedDueToAxis1 = false;
	private boolean failedDueToAxis2 = false;

	private String parsingComments = "-";
	private String parsingTool = "";

	private String bindingStyle; // rpc/document
	private String operationsUse; // encoded/literal

	private Vector xsdDependencies = new Vector();

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	String documentation;

	public String getOperationsUse() {
		return operationsUse;
	}

	public void setOperationsUse(String operationsUse) {
		this.operationsUse = operationsUse;
	}

	public String getbindingStyle() {
		return bindingStyle;
	}

	public void setBindingStyle(String bindingStyle) {
		this.bindingStyle = bindingStyle;
	}

	public String getTargetNamespaceURI() {
		return targetNamespaceURI;
	}

	public void setTargetNamespaceURI(String targetNamespaceURI) {
		this.targetNamespaceURI = targetNamespaceURI;
	}

	public QName getWebServiceName() {
		return WebServiceName;
	}

	public void setWebServiceName(QName WebServiceName) {
		this.WebServiceName = WebServiceName;
	}

	public Vector getWsdlOperations() {
		return wsdlOperations;
	}

	public void setWsdlOperations(Vector wsdlOperations) {
		this.wsdlOperations = wsdlOperations;
	}

	public URL getWsdlURL() {
		return wsdlURL;
	}

	public void setWsdlURL(URL wsdlURL) {
		this.wsdlURL = wsdlURL;
	}

	public String getParsingComments() {
		return parsingComments;
	}

	public void setParsingComments(String parsingComments) {
		this.parsingComments = parsingComments;
	}

	public Vector getContainingErrors() {
		return containingErrors;
	}

	public void setContainingErrors(Vector containingErrors) {
		this.containingErrors = containingErrors;
	}

	public boolean isFailedDueToAxis1() {
		return failedDueToAxis1;
	}

	public void setFailedDueToAxis1(boolean failedDueToAxis1) {
		this.failedDueToAxis1 = failedDueToAxis1;
	}

	public boolean isFailedDueToAxis2() {
		return failedDueToAxis2;
	}

	public void setFailedDueToAxis2(boolean failedDueToAxis2) {
		this.failedDueToAxis2 = failedDueToAxis2;
	}

	public String getParsingTool() {
		return parsingTool;
	}

	public void setParsingTool(String parsingTool) {
		this.parsingTool = parsingTool;
	}

	public String getServiceURL() {
		return serviceURL;
	}

	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}

	public String getBindingStyle() {
		return bindingStyle;
	}

	public Vector getXsdDependencies() {
		return xsdDependencies;
	}

	public void setXsdDependencies(Vector xsdDependencies) {
		this.xsdDependencies = xsdDependencies;
	}

}
