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

import java.util.Vector;

/**
 *
 * Class used for storing the output of a web service operation
 *
 * @author kgiannou
 */

public class WSOperationOutput {

	private Vector hasNativeOrComplexObjects = new Vector();
	private Vector hasFaultMessageObjects = new Vector();
	private Vector hasSoapHeaders = new Vector();

	public Vector getHasNativeOrComplexObjects() {
		return hasNativeOrComplexObjects;
	}

	public void setHasNativeOrComplexObjects(Vector hasNativeOrComplexObjects) {
		this.hasNativeOrComplexObjects = hasNativeOrComplexObjects;
	}

	public Vector getHasSoapHeaders() {
		return hasSoapHeaders;
	}

	public void setHasSoapHeaders(Vector hasSoapHeaders) {
		this.hasSoapHeaders = hasSoapHeaders;
	}

	public WSOperationOutput cloneTheWSOperationOutput() {
		WSOperationOutput newWSOutput = new WSOperationOutput();
		Vector vec = new Vector();
		for (int i = 0; i < this.getHasNativeOrComplexObjects().size(); i++) {
			if (this.getHasNativeOrComplexObjects().get(i) instanceof NativeObject) {
				vec.add(((NativeObject) this.getHasNativeOrComplexObjects().get(i)).cloneTheNO());
			} else if (this.getHasNativeOrComplexObjects().get(i) instanceof ComplexObject) {
				vec.add(((ComplexObject) this.getHasNativeOrComplexObjects().get(i)).cloneTheCO());
			}
		}
		newWSOutput.setHasNativeOrComplexObjects(vec);
		vec = new Vector();
		for (int i = 0; i < this.getHasSoapHeaders().size(); i++) {
			if (this.getHasSoapHeaders().get(i) instanceof NativeObject) {
				vec.add(((NativeObject) this.getHasSoapHeaders().get(i)).cloneTheNO());
			} else if (this.getHasSoapHeaders().get(i) instanceof ComplexObject) {
				vec.add(((ComplexObject) this.getHasSoapHeaders().get(i)).cloneTheCO());
			}
		}
		newWSOutput.setHasSoapHeaders(vec);
		fillHasParentValues(newWSOutput);
		return newWSOutput;
	}

	private void fillHasParentValues(Object obj) {
		if (obj instanceof WSOperationInput) {
			WSOperationInput in = (WSOperationInput) obj;
			for (int i = 0; i < in.getHasNativeOrComplexObjects().size(); i++) {
				if (in.getHasNativeOrComplexObjects().get(i) instanceof ComplexObject) {
					ComplexObject co = (ComplexObject) in.getHasNativeOrComplexObjects().get(i);
					co.setHasParent(obj);
					fillHasParentValues(co);
				} else if (in.getHasNativeOrComplexObjects().get(i) instanceof NativeObject) {
					NativeObject no = (NativeObject) in.getHasNativeOrComplexObjects().get(i);
					no.setHasParent(obj);
				}
			}
		} else if (obj instanceof ComplexObject) {
			ComplexObject co = (ComplexObject) obj;
			for (int j = 0; j < co.getHasNativeObjects().size(); j++) {
				((NativeObject) co.getHasNativeObjects().get(j)).setHasParent(co);
			}
			for (int j = 0; j < co.getHasComplexObjects().size(); j++) {
				((ComplexObject) co.getHasComplexObjects().get(j)).setHasParent(co);
				fillHasParentValues(((ComplexObject) co.getHasComplexObjects().get(j)).getHasComplexObjects());
				fillHasParentValues(((ComplexObject) co.getHasComplexObjects().get(j)).getHasNativeObjects());
			}
		}
	}

	public Vector getHasFaultMessageObjects() {
		return hasFaultMessageObjects;
	}

	public void setHasFaultMessageObjects(Vector hasFaultMessageObjects) {
		this.hasFaultMessageObjects = hasFaultMessageObjects;
	}
}
