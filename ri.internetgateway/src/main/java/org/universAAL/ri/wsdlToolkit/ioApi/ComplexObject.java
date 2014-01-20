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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.namespace.QName;

/**
 * 
 * Class used for storing complex objects
 * 
 * @author kgiannou
 */

public class ComplexObject {
	private QName objectName;
	private QName objectType;
	private Vector<ComplexObject> hasComplexObjects = new Vector<ComplexObject>();
	private Vector<NativeObject> hasNativeObjects = new Vector<NativeObject>();
	private String additionalInfo;
	private boolean isAbstract = false;
	private Vector hasExtendedObjects = new Vector();
	private boolean isArrayType = false;
	private boolean isOptional = false;

	private Object hasParent; // ComplexObject or WSOperation
	private boolean isInput = true;

	// public String hasMinOccurs;
	// public String hasMaxOccurs;

	public ComplexObject cloneTheCO() {
		ComplexObject newCO = new ComplexObject();
		newCO.objectName = new QName(this.objectName.getNamespaceURI(),
				this.objectName.getLocalPart(), this.objectName.getPrefix());
		newCO.objectType = new QName(this.objectType.getNamespaceURI(),
				this.objectType.getLocalPart(), this.objectType.getPrefix());

		// System.out.println(this.objectName.getLocalPart());
		/*
		 * if(!this.getObjectType().getLocalPart().contains("XmlSchemaChoice")){
		 * if(ITIWSDLParser.clonedTypes.containsKey(this.getObjectType())){
		 * Integer
		 * countSoFar=(Integer)ITIWSDLParser.clonedTypes.get(this.getObjectType
		 * ()); countSoFar++;
		 * ITIWSDLParser.clonedTypes.put(this.getObjectType(),countSoFar);
		 * }else{ ITIWSDLParser.clonedTypes.put(this.getObjectType(),1); } }
		 */
		newCO.hasComplexObjects = new Vector();

		Iterator iter1 = this.hasComplexObjects.iterator();
		while (iter1.hasNext()) {
			ComplexObject co = (ComplexObject) iter1.next();
			// co.setHasParent(newCO);
			co.setHasParent(this);

			/*
			 * if(ITIWSDLParser.checkIfCOhasAlreadyBeenCloned(co.getObjectType())
			 * ){ System.out.println(co.getObjectName().getLocalPart()+"\t"+co.
			 * getObjectType().getLocalPart()); }
			 */

			// if(ITIWSDLParser.checkIfCOhasAlreadyBeenCloned(co.getObjectType())||checkIfCOhasParentTheSameCO(co)){
			if (checkIfCOhasParentTheSameCO(co)) {
				ComplexObject co1 = new ComplexObject();
				co1.objectName = new QName(co.objectName.getNamespaceURI(),
						co.objectName.getLocalPart(), co.objectName.getPrefix());
				co1.objectType = new QName(co.objectType.getNamespaceURI(),
						co.objectType.getLocalPart(), co.objectType.getPrefix());
				co1.hasComplexObjects = new Vector();
				co1.hasNativeObjects = new Vector();
				co1.hasExtendedObjects = new Vector();
				co1.isInput = co.isInput;
				// co1.setHasParent(newCO);
				co1.setHasParent(this);
				co1.isAbstract = co.isAbstract;
				co1.isArrayType = co.isArrayType;
				co1.isOptional = co.isOptional;
				co1.additionalInfo = co.additionalInfo;

				/*
				 * if(co.getObjectType().getLocalPart().contains("XmlSchemaChoice"
				 * )){ System.out.println(); }
				 */

				co1.hasExtendedObjects = new Vector();
				for (int i = 0; i < co.getHasExtendedObjects().size(); i++) {
					Object obj = co.getHasExtendedObjects().get(i);
					if (obj.getClass().getName().contains("ComplexObject")) {
						ComplexObject inExtCO = (ComplexObject) obj;
						ComplexObject extCO = new ComplexObject();
						extCO.objectName = new QName(
								inExtCO.objectName.getNamespaceURI(),
								inExtCO.objectName.getLocalPart(),
								inExtCO.objectName.getPrefix());
						extCO.objectType = new QName(
								inExtCO.objectType.getNamespaceURI(),
								inExtCO.objectType.getLocalPart(),
								inExtCO.objectType.getPrefix());
						extCO.hasComplexObjects = new Vector();
						extCO.hasNativeObjects = new Vector();
						extCO.hasExtendedObjects = new Vector();
						extCO.isInput = inExtCO.isInput;
						// extCO.setHasParent(co1);
						extCO.setHasParent(co);
						extCO.isAbstract = inExtCO.isAbstract;
						extCO.isArrayType = inExtCO.isArrayType;
						extCO.additionalInfo = inExtCO.additionalInfo;
						extCO.isOptional = inExtCO.isOptional;
						co1.hasExtendedObjects.add(extCO);
					} else {// NativeObject
						NativeObject inExtNO = (NativeObject) obj;
						NativeObject extNO = inExtNO.cloneTheNO();
						co1.hasExtendedObjects.add(extNO);
					}
				}

				newCO.hasComplexObjects.add(co1);
				System.out.println();

			} else {
				/*
				 * if(co.objectType.getLocalPart().contains("EnvelopeType")){//
				 * RS_Identifier Object parent1=this.getHasParent(); Vector
				 * parentNames=new Vector(); while(parent1!=null){
				 * //System.out.println
				 * (((ComplexObject)parent1).getObjectName().getLocalPart());
				 * parent1=((ComplexObject)parent1).getHasParent();
				 * if(parent1!=null){
				 * parentNames.insertElementAt(((ComplexObject
				 * )parent1).getObjectName().getLocalPart(),0); } } for(int
				 * i=0;i<parentNames.size();i++){
				 * System.out.println((String)parentNames.get(i)); }
				 * System.out.println(); }
				 */

				ComplexObject co1 = co.cloneTheCO();
				newCO.hasComplexObjects.add(co1);
			}
		}

		newCO.hasNativeObjects = new Vector();
		Iterator iter2 = this.hasNativeObjects.iterator();
		while (iter2.hasNext()) {
			NativeObject no = (NativeObject) iter2.next();
			NativeObject no1 = no.cloneTheNO();
			newCO.hasNativeObjects.add(no1);
		}

		newCO.hasExtendedObjects = new Vector();
		Iterator iter3 = this.hasExtendedObjects.iterator();
		while (iter3.hasNext()) {
			Object obj = iter3.next();
			if (obj.getClass().getName().indexOf("NativeObject") > -1) {
				NativeObject no = (NativeObject) obj;
				// no.setHasParent(newCO);
				no.setHasParent(this);
				NativeObject no1 = no.cloneTheNO();
				newCO.hasExtendedObjects.add(no1);
			} else if (obj.getClass().getName().indexOf("ComplexObject") > -1) {
				ComplexObject co = (ComplexObject) obj;
				// co.setHasParent(newCO);
				co.setHasParent(this);
				/*
				 * if(co.getHasComplexObjects().size()>0&&ITIWSDLParser.
				 * checkIfCOhasAlreadyBeenCloned(co.getObjectType())){
				 * System.out
				 * .println("EXT:  "+co.getObjectName().getLocalPart()+
				 * "\t"+co.getObjectType().getLocalPart()); }
				 */
				// if(ITIWSDLParser.checkIfCOhasAlreadyBeenCloned(co.getObjectType())||checkIfCOhasParentTheSameCO(co)){
				if (checkIfCOhasParentTheSameCO(co)) {
					ComplexObject co1 = new ComplexObject();
					co1.objectName = new QName(co.objectName.getNamespaceURI(),
							co.objectName.getLocalPart(),
							co.objectName.getPrefix());
					co1.objectType = new QName(co.objectType.getNamespaceURI(),
							co.objectType.getLocalPart(),
							co.objectType.getPrefix());
					co1.hasComplexObjects = new Vector();
					co1.hasNativeObjects = new Vector();
					co1.hasExtendedObjects = new Vector();

					co1.isInput = co.isInput;
					co1.hasParent = co.hasParent;
					co1.isAbstract = co.isAbstract;
					co1.isArrayType = co.isArrayType;
					co1.isOptional = co.isOptional;
					co1.additionalInfo = co.additionalInfo;

					co1.hasExtendedObjects = new Vector();
					for (int i = 0; i < co.getHasExtendedObjects().size(); i++) {
						Object obj1 = co.getHasExtendedObjects().get(i);
						if (obj1.getClass().getName().contains("ComplexObject")) {
							ComplexObject inExtCO = (ComplexObject) obj1;
							ComplexObject extCO = new ComplexObject();
							extCO.objectName = new QName(
									inExtCO.objectName.getNamespaceURI(),
									inExtCO.objectName.getLocalPart(),
									inExtCO.objectName.getPrefix());
							extCO.objectType = new QName(
									inExtCO.objectType.getNamespaceURI(),
									inExtCO.objectType.getLocalPart(),
									inExtCO.objectType.getPrefix());
							extCO.hasComplexObjects = new Vector();
							extCO.hasNativeObjects = new Vector();
							extCO.hasExtendedObjects = new Vector();
							extCO.isInput = inExtCO.isInput;
							// extCO.hasParent=inExtCO.hasParent;
							extCO.setHasParent(co1);
							extCO.isAbstract = inExtCO.isAbstract;
							extCO.isArrayType = inExtCO.isArrayType;
							extCO.additionalInfo = inExtCO.additionalInfo;
							extCO.isOptional = inExtCO.isOptional;

							co1.hasExtendedObjects.add(extCO);
						} else {// NativeObject
							NativeObject inExtNO = (NativeObject) obj1;
							NativeObject extNO = inExtNO.cloneTheNO();
							extNO.setHasParent(co1);
							co1.hasExtendedObjects.add(extNO);
						}
					}

					newCO.hasExtendedObjects.add(co1);
					System.out.println();

				} else {
					/*
					 * if(co.objectType.getLocalPart().contains("EnvelopeType")){
					 * Object parent1=this.getHasParent(); Vector
					 * parentNames=new Vector(); while(parent1!=null){
					 * //System.out
					 * .println(((ComplexObject)parent1).getObjectName
					 * ().getLocalPart());
					 * parent1=((ComplexObject)parent1).getHasParent();
					 * if(parent1!=null){
					 * parentNames.insertElementAt(((ComplexObject
					 * )parent1).getObjectName().getLocalPart(),0); } } for(int
					 * i=0;i<parentNames.size();i++){
					 * System.out.println((String)parentNames.get(i)); }
					 * System.out.println(); }
					 */
					ComplexObject co1 = co.cloneTheCO();
					newCO.hasExtendedObjects.add(co1);
				}
			}
		}

		// newCO.hasComplexObjects=(Vector)this.hasComplexObjects.clone();
		// newCO.hasNativeObjects=(Vector)this.hasNativeObjects.clone();

		newCO.isInput = this.isInput;
		newCO.hasParent = this.hasParent;
		newCO.isAbstract = this.isAbstract;
		newCO.isArrayType = this.isArrayType;
		newCO.isOptional = this.isOptional;
		newCO.additionalInfo = this.additionalInfo;
		// newCO.hasExtendedObjects=(Vector)this.hasExtendedObjects.clone();

		return newCO;
	}

	public ComplexObject cloneTheCO_MOD() {
		ComplexObject newCO = new ComplexObject();
		newCO.objectName = new QName(this.objectName.getNamespaceURI(),
				this.objectName.getLocalPart(), this.objectName.getPrefix());
		newCO.objectType = new QName(this.objectType.getNamespaceURI(),
				this.objectType.getLocalPart(), this.objectType.getPrefix());

		System.out.println(this.objectName.getLocalPart());

		newCO.hasComplexObjects = new Vector();

		Iterator iter1 = this.hasComplexObjects.iterator();
		while (iter1.hasNext()) {
			ComplexObject co = (ComplexObject) iter1.next();
			co.setHasParent(this);

			if (checkIfCOhasParentTheSameCO(co)) {
				ComplexObject co1 = new ComplexObject();
				co1.objectName = new QName(co.objectName.getNamespaceURI(),
						co.objectName.getLocalPart(), co.objectName.getPrefix());
				co1.objectType = new QName(co.objectType.getNamespaceURI(),
						co.objectType.getLocalPart(), co.objectType.getPrefix());
				co1.hasComplexObjects = new Vector();
				co1.hasNativeObjects = new Vector();
				co1.hasExtendedObjects = new Vector();
				co1.isInput = co.isInput;
				co1.hasParent = co.hasParent;
				co1.isAbstract = co.isAbstract;
				co1.isArrayType = co.isArrayType;

				co1.additionalInfo = co.additionalInfo;

				if (co.getObjectType().getLocalPart()
						.contains("XmlSchemaChoice")) {
					System.out.println();
				}

				co1.hasExtendedObjects = new Vector();
				for (int i = 0; i < co.getHasExtendedObjects().size(); i++) {
					Object obj = co.getHasExtendedObjects().get(i);
					if (obj.getClass().getName().contains("ComplexObject")) {
						ComplexObject inExtCO = (ComplexObject) obj;
						ComplexObject extCO = new ComplexObject();
						extCO.objectName = new QName(
								inExtCO.objectName.getNamespaceURI(),
								inExtCO.objectName.getLocalPart(),
								inExtCO.objectName.getPrefix());
						extCO.objectType = new QName(
								inExtCO.objectType.getNamespaceURI(),
								inExtCO.objectType.getLocalPart(),
								inExtCO.objectType.getPrefix());
						extCO.hasComplexObjects = new Vector();
						extCO.hasNativeObjects = new Vector();
						extCO.hasExtendedObjects = new Vector();
						extCO.isInput = inExtCO.isInput;
						extCO.hasParent = inExtCO.hasParent;
						extCO.isAbstract = inExtCO.isAbstract;
						extCO.isArrayType = inExtCO.isArrayType;
						extCO.additionalInfo = inExtCO.additionalInfo;

						co1.hasExtendedObjects.add(extCO);
					} else {// NativeObject
						NativeObject inExtNO = (NativeObject) obj;
						NativeObject extNO = inExtNO.cloneTheNO();
						co1.hasExtendedObjects.add(extNO);
					}
				}

				newCO.hasComplexObjects.add(co1);
				System.out.println();

			} else {
				ComplexObject co1 = co.cloneTheCO();
				newCO.hasComplexObjects.add(co1);
			}
		}

		newCO.hasNativeObjects = new Vector();
		Iterator iter2 = this.hasNativeObjects.iterator();
		while (iter2.hasNext()) {
			NativeObject no = (NativeObject) iter2.next();
			NativeObject no1 = no.cloneTheNO();
			newCO.hasNativeObjects.add(no1);
		}

		newCO.hasExtendedObjects = new Vector();
		Iterator iter3 = this.hasExtendedObjects.iterator();
		while (iter3.hasNext()) {
			Object obj = iter3.next();
			if (obj.getClass().getName().indexOf("NativeObject") > -1) {
				NativeObject no = (NativeObject) obj;
				no.setHasParent(this);
				NativeObject no1 = no.cloneTheNO();
				newCO.hasExtendedObjects.add(no1);
			} else if (obj.getClass().getName().indexOf("ComplexObject") > -1) {
				ComplexObject co = (ComplexObject) obj;
				co.setHasParent(this);
				if (checkIfCOhasParentTheSameCO(co)) {
					ComplexObject co1 = new ComplexObject();
					co1.objectName = new QName(co.objectName.getNamespaceURI(),
							co.objectName.getLocalPart(),
							co.objectName.getPrefix());
					co1.objectType = new QName(co.objectType.getNamespaceURI(),
							co.objectType.getLocalPart(),
							co.objectType.getPrefix());
					co1.hasComplexObjects = new Vector();
					co1.hasNativeObjects = new Vector();
					co1.hasExtendedObjects = new Vector();

					co1.isInput = co.isInput;
					co1.hasParent = co.hasParent;
					co1.isAbstract = co.isAbstract;
					co1.isArrayType = co.isArrayType;

					co1.additionalInfo = co.additionalInfo;

					co1.hasExtendedObjects = new Vector();
					for (int i = 0; i < co.getHasExtendedObjects().size(); i++) {
						Object obj1 = co.getHasExtendedObjects().get(i);
						if (obj1.getClass().getName().contains("ComplexObject")) {
							ComplexObject inExtCO = (ComplexObject) obj1;
							ComplexObject extCO = new ComplexObject();
							extCO.objectName = new QName(
									inExtCO.objectName.getNamespaceURI(),
									inExtCO.objectName.getLocalPart(),
									inExtCO.objectName.getPrefix());
							extCO.objectType = new QName(
									inExtCO.objectType.getNamespaceURI(),
									inExtCO.objectType.getLocalPart(),
									inExtCO.objectType.getPrefix());
							extCO.hasComplexObjects = new Vector();
							extCO.hasNativeObjects = new Vector();
							extCO.hasExtendedObjects = new Vector();
							extCO.isInput = inExtCO.isInput;
							extCO.hasParent = inExtCO.hasParent;
							extCO.isAbstract = inExtCO.isAbstract;
							extCO.isArrayType = inExtCO.isArrayType;
							extCO.additionalInfo = inExtCO.additionalInfo;

							co1.hasExtendedObjects.add(extCO);
						} else {// NativeObject
							NativeObject inExtNO = (NativeObject) obj1;
							NativeObject extNO = inExtNO.cloneTheNO();
							co1.hasExtendedObjects.add(extNO);
						}
					}

					newCO.hasExtendedObjects.add(co1);
					System.out.println();

				} else {
					ComplexObject co1 = co.cloneTheCO();
					newCO.hasExtendedObjects.add(co1);
				}
			}
		}

		// newCO.hasComplexObjects=(Vector)this.hasComplexObjects.clone();
		// newCO.hasNativeObjects=(Vector)this.hasNativeObjects.clone();

		newCO.isInput = this.isInput;
		newCO.hasParent = this.hasParent;
		newCO.isAbstract = this.isAbstract;
		newCO.isArrayType = this.isArrayType;

		newCO.additionalInfo = this.additionalInfo;
		// newCO.hasExtendedObjects=(Vector)this.hasExtendedObjects.clone();

		return newCO;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Vector getHasComplexObjects() {
		return hasComplexObjects;
	}

	public void setHasComplexObjects(Vector hasComplexObjects) {
		this.hasComplexObjects = hasComplexObjects;
	}

	public Vector getHasNativeObjects() {
		return hasNativeObjects;
	}

	public void setHasNativeObjects(Vector hasNativeObjects) {
		this.hasNativeObjects = hasNativeObjects;
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

	public Vector getHasExtendedObjects() {
		return hasExtendedObjects;
	}

	public void setHasExtendedObjects(Vector hasExtendedObjects) {
		this.hasExtendedObjects = hasExtendedObjects;
	}

	public Object getHasParent() {
		return hasParent;
	}

	public void setHasParent(Object hasParent) {
		this.hasParent = hasParent;
	}

	public boolean isIsAbstract() {
		return isAbstract;
	}

	public void setIsAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public boolean isIsArrayType() {
		return isArrayType;
	}

	public void setIsArrayType(boolean isArrayType) {
		this.isArrayType = isArrayType;
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

	private boolean checkIfCOhasParentTheSameCO(ComplexObject co) {
		if (co.getObjectType() == null) {
			System.out.println();
		}

		if (co.getHasParent() == null
				|| !co.getHasParent().getClass().getName()
						.contains("ComplexObject")
				|| co.getObjectType().getLocalPart()
						.contains("XmlSchemaChoice")) {
			return false;
		}

		ComplexObject parent = (ComplexObject) co.getHasParent();
		int check_parents_LIMIT = 60;
		int counter = 0;
		while (parent != null && counter < check_parents_LIMIT) {
			if (parent.getObjectType() != null
					&& parent.getObjectType().equals(co.getObjectType())) {
				// System.out.println("############ AMAN AMAN AMAN NEWWWWWW!");
				return true;
			}
			if (parent.getHasParent() instanceof ComplexObject) {
				parent = (ComplexObject) parent.getHasParent();
			}
			counter++;

		}
		return false;
	}

	private boolean checkIfCOhasParentTheSameCO_MOD(ComplexObject co) {
		if (co.getHasParent() == null
				|| !co.getHasParent().getClass().getName()
						.contains("ComplexObject")) {
			return false;
		}

		ComplexObject parent = (ComplexObject) co.getHasParent();
		int check_parents_LIMIT = 30;

		if (co.getObjectType().getLocalPart().equals("XmlSchemaChoice")) {
			int counter = 0;
			while (parent != null && counter < check_parents_LIMIT) {
				if (parent.getObjectType() != null
						&& parent.getObjectType().getLocalPart()
								.equals("XmlSchemaChoice")) {
					if (parent.getHasExtendedObjects().size() >= co
							.getHasExtendedObjects().size()) {
						boolean isTheSameCO = true;
						for (int i = 0; i < co.getHasExtendedObjects().size(); i++) {
							Object extObj = co.getHasExtendedObjects().get(i);
							if (extObj.getClass().getName()
									.contains("ComplexObject")) {
								QName extObjName = ((ComplexObject) extObj)
										.getObjectName();
								boolean extObjExists = false;
								for (int j = 0; j < parent
										.getHasExtendedObjects().size(); j++) {
									Object parExtObj = parent
											.getHasExtendedObjects().get(i);
									if (parExtObj.getClass().getName()
											.contains("ComplexObject")) {
										if (((ComplexObject) parExtObj)
												.getObjectName().equals(
														extObjName)) {
											extObjExists = true;
											break;
										}
									}
								}
								if (!extObjExists) {
									isTheSameCO = false;
								}
							}
							if (extObj.getClass().getName()
									.contains("NativeObject")) {
								QName extObjName = ((NativeObject) extObj)
										.getObjectName();
								boolean extObjExists = false;
								for (int j = 0; j < parent
										.getHasExtendedObjects().size(); j++) {
									Object parExtObj = parent
											.getHasExtendedObjects().get(i);
									if (parExtObj.getClass().getName()
											.contains("NativeObject")) {
										if (((NativeObject) parExtObj)
												.getObjectName().equals(
														extObjName)) {
											extObjExists = true;
											break;
										}
									}
								}
								if (!extObjExists) {
									isTheSameCO = false;
								}
							}
						}
						if (isTheSameCO) {
							return true;
						}
					}
				}
				parent = (ComplexObject) parent.getHasParent();
				counter++;
			}
			System.out.println();
			return false;

		} else {

			int counter = 0;
			while (parent != null && counter < check_parents_LIMIT) {
				if (parent.getObjectType() != null
						&& parent.getObjectType().equals(co.getObjectType())) {
					// System.out.println("############ AMAN AMAN AMAN NEWWWWWW!");
					return true;
				}
				parent = (ComplexObject) parent.getHasParent();
				counter++;
			}
			return false;
		}

	}

	@Override
	public String toString() {
		String result = this.getObjectName().getLocalPart() + "  {"
				+ this.getObjectType().getLocalPart() + "}    NS="
				+ this.getObjectType().getNamespaceURI();
		return result;
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

}
