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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.xml.namespace.QName;

/**
 *
 * Class used for storing simple (native) objects
 *
 * @author kgiannou
 */

public class NativeObject {

	private QName objectName;
	private QName objectType;
	private String additionalInfo;
	private Vector hasAllowedValues = new Vector();
	private String hasValue = "";
	private boolean isOptional = false;

	private Object hasParent; // ComplexObject or WSOperation
	private boolean isInput = true;

	// public String hasMinOccurs;
	// public String hasMaxOccurs;
	public NativeObject cloneTheNO() {
		NativeObject no = new NativeObject();
		if (this.objectName == null) {
			System.out.println();
		}
		no.setObjectName(new QName(this.objectName.getNamespaceURI(), this.getObjectName().getLocalPart(),
				this.getObjectName().getPrefix()));

		if (this.objectType == null) {
			no.setObjectType(new QName(this.objectName.getNamespaceURI(), this.getObjectName().getLocalPart(),
					this.getObjectName().getPrefix()));
		} else {
			no.setObjectType(new QName(this.objectType.getNamespaceURI(), this.getObjectType().getLocalPart(),
					this.getObjectType().getPrefix()));
		}
		// no.setObjectType(new
		// QName(this.objectType.getNamespaceURI(),this.getObjectType().getLocalPart(),this.getObjectType().getPrefix()));
		no.setHasValue(this.getHasValue());
		no.setHasAllowedValues((Vector) this.hasAllowedValues.clone());

		no.setAdditionalInfo(this.additionalInfo);
		no.hasParent = this.hasParent;
		no.isInput = this.isInput;
		no.isOptional = this.isOptional;
		return no;
	}

	public void setHasValue(String v) {
		this.hasValue = v;
	}

	public String getHasValue() {
		return hasValue;
	}

	public void setHasAllowedValues(Vector v) {
		this.hasAllowedValues = v;
	}

	public Vector getHasAllowedValues() {
		return hasAllowedValues;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public QName getObjectName() {
		return objectName;
	}

	public void setObjectName(QName objectName) {
		this.objectName = objectName;
	}

	public QName getObjectType() {
		return objectType;
	}

	public void setObjectType(QName objectType) {
		this.objectType = objectType;
	}

	public Object getHasParent() {
		return hasParent;
	}

	public void setHasParent(Object hasParent) {
		this.hasParent = hasParent;
	}

	public boolean isIsInput() {
		return isInput;
	}

	public void setIsInput(boolean isInput) {
		this.isInput = isInput;
	}

	public boolean isIsOptional() {
		return isOptional;
	}

	public void setIsOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	public boolean equals(NativeObject no) {
		List<String> parents = new ArrayList<String>();
		getParents(this, parents, 20);
		List<String> parents2 = new ArrayList<String>();
		getParents(no, parents2, 20);
		if (this.objectName.getLocalPart().equals(no.getObjectName().getLocalPart())
				&& this.objectType.getLocalPart().equals(no.getObjectType().getLocalPart())) {
			if (parents.size() != parents2.size()) {
				return false;
			}
			for (int i = 0; i < parents.size(); i++) {
				if (!parents.get(i).equals(parents2.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void getParents(Object obj, List list, int parentDepth) {
		if (list.size() > parentDepth) {
			return;
		}
		if (obj instanceof NativeObject) {
			NativeObject no = (NativeObject) obj;
			if (no.getHasParent() instanceof ComplexObject) {
				ComplexObject co = ((ComplexObject) no.getHasParent());
				list.add(co.getObjectName().getLocalPart());
				getParents(co, list, parentDepth);
			}
		} else if (obj instanceof ComplexObject) {
			ComplexObject co = (ComplexObject) obj;
			if (co.getHasParent() instanceof ComplexObject) {
				ComplexObject co1 = ((ComplexObject) co.getHasParent());
				list.add(co1.getObjectName().getLocalPart());
				getParents(co1, list, parentDepth);
			}
		}
	}

	@Override
	public String toString() {
		if (this.getHasAllowedValues().size() == 0) {
			String result = this.getObjectName().getLocalPart() + "  {" + this.getObjectType().getLocalPart() + "}";
			return result;
		} else {
			String allowedValuesString = "";
			for (int i1 = 0; i1 < this.getHasAllowedValues().size(); i1++) {
				allowedValuesString += this.getHasAllowedValues().get(i1) + ", ";
			}
			allowedValuesString = allowedValuesString.substring(0, allowedValuesString.lastIndexOf(","));
			String result = this.getObjectName().getLocalPart() + "  {" + this.getObjectType().getLocalPart() + "}";
			return result;
		}
	}
}
