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
package org.universAAL.ri.wsdlToolkit.invocation;

import java.util.Vector;

/**
 *
 * This class contains the result returned from a web service invocation
 *
 * @author kgiannou
 */

public class InvocationResult {
	private String hasRequestInString;
	private String hasResponseInString;

	public Vector responseHasNativeOrComplexObjects = new Vector();

	public void setHasRequestInString(String v) {
		this.hasRequestInString = v;
	}

	public String getHasRequestInString() {
		return this.hasRequestInString;
	}

	public void setHasResponseInString(String v) {
		this.hasResponseInString = v;
	}

	public String getHasResponseInString() {
		return this.hasResponseInString;
	}

	public void setResponseHasNativeOrComplexObjects(Vector v) {
		this.responseHasNativeOrComplexObjects = v;
	}

	public Vector getResponseHasNativeOrComplexObjects() {
		return this.responseHasNativeOrComplexObjects;
	}

}
