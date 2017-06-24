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

import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;

/**
 *
 * Class used for building rpc encoding SOAP envelope
 *
 * @author kgiannou
 */

public class Axis2_RpcEncodedMessageBuilder {

	public static SOAPEnvelope createSOAPEnvelope_RPC_Encoded(QName operationName, WSOperationInput operationInput,
			ParsedWSDLDefinition theDefinition) {
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();

		OMNamespace omNs = fac.createOMNamespace(operationName.getNamespaceURI(), "opNS");

		OMElement messageBody = createSOAPBody(fac, operationName, operationInput.getHasNativeOrComplexObjects(), omNs);
		envelope.getBody().addChild(messageBody);

		addOperationHeaderToEnvelope(fac, envelope, operationInput.getHasSoapHeaders(), omNs);

		System.out.println(envelope);
		return envelope;
	}

	private static OMElement createSOAPBody(SOAPFactory fac, QName operationName, Vector operationInputs,
			OMNamespace operationNs) {
		OMElement method = fac.createOMElement(operationName.getLocalPart(), operationNs);

		Iterator operInputsIter = operationInputs.iterator();
		while (operInputsIter.hasNext()) {
			Object inputObject = operInputsIter.next();
			if (inputObject.getClass().getName().contains("NativeObject")) {
				NativeObject no = (NativeObject) inputObject;
				// OMNamespace inputObjectNs =
				// fac.createOMNamespace(no.getNamespaceURI(), "ns1");
				OMElement valueA = createOMElementForNativeObjectInput(fac, no, operationNs);
				if (valueA != null) {
					method.addChild(valueA);
				}

			} else if (inputObject.getClass().getName().contains("ComplexObject")) {
				ComplexObject co = (ComplexObject) inputObject;
				if (co.isIsArrayType()) {
					// ARRAY TYPE!!!
					if (co.getHasComplexObjects().size() > 0) {
						OMElement valueA = createOMElementForArrayTypeObjectInput(fac, co, operationNs);
						if (valueA != null) {
							method.addChild(valueA);
						}
					} else if (co.getHasNativeObjects().size() > 0) {
						Vector v = createOMElementForArrayTypeObject_ContainingNativeObjs_Input(fac, co, operationNs);
						if (v != null) {
							Iterator iter111 = v.iterator();
							while (iter111.hasNext()) {
								OMElement valueForNO111 = (OMElement) iter111.next();
								if (valueForNO111 != null) {
									method.addChild(valueForNO111);
								}
							}
						}
					}
				} else {
					OMElement valueA = createOMElementForComplexObjectInput(fac, co, operationNs);
					if (valueA != null) {
						method.addChild(valueA);
					}
				}

			} else {
				System.out.println("ERROR 1!!!!!");
			}
		}

		return method;
	}

	private static void addOperationHeaderToEnvelope(SOAPFactory fac, SOAPEnvelope envelope,
			Vector operationHeaderObjects, OMNamespace operationNs) {
		Iterator headerObjectsIter = operationHeaderObjects.iterator();
		while (headerObjectsIter.hasNext()) {
			ComplexObject headerCO = (ComplexObject) headerObjectsIter.next();

			// to 'http://www.xignite.com/xrates.asmx?WSDL' thelei na mpei sto
			// header node to type:Header anti gia to p.x.
			// name:DrawYieldCurveHeader...
			headerCO.setObjectName(headerCO.getObjectType());

			if (headerCO.isIsArrayType()) {
				// ARRAY TYPE!!!
				if (headerCO.getHasComplexObjects().size() > 0) {
					OMElement valueA = createOMElementForArrayTypeObjectInput(fac, headerCO, operationNs);
					if (valueA != null) {
						envelope.getHeader().addChild(valueA);
					}
				} else if (headerCO.getHasNativeObjects().size() > 0) {
					Vector v = createOMElementForArrayTypeObject_ContainingNativeObjs_Input(fac, headerCO, operationNs);
					if (v != null) {
						Iterator iter111 = v.iterator();
						while (iter111.hasNext()) {
							OMElement valueForNO111 = (OMElement) iter111.next();
							if (valueForNO111 != null) {
								envelope.getHeader().addChild(valueForNO111);
							}
						}
					}
				}
			} else {
				OMElement valueA = createOMElementForComplexObjectInput(fac, headerCO, operationNs);
				if (valueA != null) {
					envelope.getHeader().addChild(valueA);
				}
			}

			/*
			 * Iterator nativeObjsIter=headerCO.hasNativeObjects.iterator();
			 * while(nativeObjsIter.hasNext()){ NativeObject
			 * no=(NativeObject)nativeObjsIter.next();; //OMNamespace
			 * inputObjectNs = fac.createOMNamespace(no.getNamespaceURI(),
			 * "ns1"); OMElement valueA =
			 * createOMElementForNativeObjectInput(fac, no, operationNs);
			 * envelope.getHeader().addChild(valueA); }
			 *
			 * Iterator complexObjsIter=headerCO.hasComplexObjects.iterator();
			 * while(complexObjsIter.hasNext()){ ComplexObject
			 * co=(ComplexObject)complexObjsIter.next(); if(co.isIsArrayType()){
			 * //ARRAY TYPE!!! OMElement valueA =
			 * createOMElementForArrayTypeObjectInput(fac, co, operationNs);
			 * envelope.getHeader().addChild(valueA); }else{ OMElement valueA =
			 * createOMElementForComplexObjectInput(fac, co, operationNs);
			 * envelope.getHeader().addChild(valueA); } }
			 */
			System.out.println(envelope);
		}
		// envelope.getHeader().addHeaderBlock(arg0, arg1)
	}

	private static OMElement createOMElementForNativeObjectInput(SOAPFactory fac, NativeObject no,
			OMNamespace operationNs) {
		// OMElement valueA = fac.createOMElement(no.getObjectName(),
		// operationNamespace);
		// if(no.isOptional&&(no.hasValue==null||no.hasValue.length()==0))return
		// null;
		if ((no.getHasValue() == null || no.getHasValue().length() == 0))
			return null;
		OMNamespace schemaInstanceNs = fac.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		OMElement valueA = fac.createOMElement(no.getObjectName());
		// valueA.addAttribute("type",
		// no.getObjectType().getNamespaceURI()+":"+no.getObjectType().getLocalPart(),
		// schemaInstanceNs);

		if (no.getObjectType().getPrefix() != null && no.getObjectType().getPrefix().length() > 0) {
			valueA.addAttribute("xmlns:" + no.getObjectType().getPrefix(), no.getObjectType().getNamespaceURI(), null);
			valueA.addAttribute("type", no.getObjectType().getPrefix() + ":" + no.getObjectType().getLocalPart(),
					schemaInstanceNs);
		} else {
			valueA.addAttribute("xmlns:xsd_default", no.getObjectType().getNamespaceURI(), null);
			valueA.addAttribute("type", "xsd_default:" + no.getObjectType().getLocalPart(), schemaInstanceNs);
		}

		valueA.setText(no.getHasValue());
		return valueA;
	}

	private static OMElement createOMElementForComplexObjectInput(SOAPFactory fac, ComplexObject co,
			OMNamespace operationNamespace) {
		System.out.println();
		if (!Axis2WebServiceInvoker.theComplexObjectCarriesValues_ITERATIVE(co))
			return null;

		OMElement coNode = fac.createOMElement(co.getObjectName());
		Vector nosVector = co.getHasNativeObjects();
		Iterator iter1 = nosVector.iterator();
		while (iter1.hasNext()) {
			NativeObject no = (NativeObject) iter1.next();
			OMElement valueForNO = createOMElementForNativeObjectInput(fac, no, operationNamespace);
			if (valueForNO != null) {
				coNode.addChild(valueForNO);
			}
		}

		Iterator iter2 = co.getHasComplexObjects().iterator();
		while (iter2.hasNext()) {
			ComplexObject co1 = (ComplexObject) iter2.next();

			if (co1.isIsArrayType()) {
				// ARRAY TYPE!!!
				if (co1.getHasComplexObjects().size() > 0) {
					OMElement valueForCO1 = createOMElementForArrayTypeObjectInput(fac, co1, operationNamespace);
					if (valueForCO1 != null) {
						coNode.addChild(valueForCO1);
					}
				} else if (co1.getHasNativeObjects().size() > 0) {
					Vector v = createOMElementForArrayTypeObject_ContainingNativeObjs_Input(fac, co1,
							operationNamespace);
					if (v != null) {
						Iterator iter111 = v.iterator();
						while (iter111.hasNext()) {
							OMElement valueForNO111 = (OMElement) iter111.next();
							if (valueForNO111 != null) {
								coNode.addChild(valueForNO111);
							}
						}
					}
				}
			} else {
				OMElement valueForCO1 = createOMElementForComplexObjectInput(fac, co1, operationNamespace);
				if (valueForCO1 != null) {
					coNode.addChild(valueForCO1);
				}
			}

		}
		return coNode;
	}

	private static OMElement createOMElementForArrayTypeObjectInput(SOAPFactory fac, ComplexObject co,
			OMNamespace operationNamespace) {
		// OMElement valueA = fac.createOMElement(co.getObjectName(),
		// operationNamespace);

		// Mipws prepei na lavw yp'opsin kai to if(co.isOptional)???
		if (!Axis2WebServiceInvoker.theComplexObjectCarriesValues_ITERATIVE(co))
			return null;

		if (co.getHasNativeObjects().size() > 0) {
			System.out.println();
		}

		OMElement valueA = fac.createOMElement(co.getObjectName());

		if (co.getHasComplexObjects().size() > 0) {
			Iterator iter2 = co.getHasComplexObjects().iterator();
			while (iter2.hasNext()) {
				ComplexObject co1 = (ComplexObject) iter2.next();
				co1.setObjectName(co1.getObjectType());

				if (co1.isIsArrayType()) {
					// ARRAY TYPE!!!
					if (co1.getHasComplexObjects().size() > 0) {
						OMElement valueForCO1 = createOMElementForArrayTypeObjectInput(fac, co1, operationNamespace);
						if (valueForCO1 != null) {
							valueA.addChild(valueForCO1);
						}
					} else if (co1.getHasNativeObjects().size() > 0) {
						Vector v = createOMElementForArrayTypeObject_ContainingNativeObjs_Input(fac, co1,
								operationNamespace);
						if (v != null) {
							Iterator iter111 = v.iterator();
							while (iter111.hasNext()) {
								OMElement valueForNO111 = (OMElement) iter111.next();
								if (valueForNO111 != null) {
									valueA.addChild(valueForNO111);
								}
							}
						}
					}
				} else {
					OMElement valueForCO1 = createOMElementForComplexObjectInput(fac, co1, operationNamespace);
					if (valueForCO1 != null) {
						valueA.addChild(valueForCO1);
					}
				}

			}

		} else if (co.getHasNativeObjects().size() > 0) {
			Vector nosVector = co.getHasNativeObjects();
			Iterator iter1 = nosVector.iterator();
			while (iter1.hasNext()) {
				NativeObject no = (NativeObject) iter1.next();
				OMElement valueForNO = createOMElementForNativeObjectInput(fac, no, operationNamespace);
				if (valueForNO != null) {
					valueA.addChild(valueForNO);
				}
			}
		}
		return valueA;

	}

	private static Vector createOMElementForArrayTypeObject_ContainingNativeObjs_Input(SOAPFactory fac,
			ComplexObject co, OMNamespace operationNamespace) {
		// OMElement valueA = fac.createOMElement(co.getObjectName(),
		// operationNamespace);

		// Mipws prepei na lavw yp'opsin kai to if(co.isOptional)???
		if (!Axis2WebServiceInvoker.theComplexObjectCarriesValues_ITERATIVE(co))
			return null;

		Vector result = new Vector();

		if (co.getHasNativeObjects().size() > 0) {
			System.out.println();
		}

		if (co.getHasNativeObjects().size() > 0) {
			Vector nosVector = co.getHasNativeObjects();
			Iterator iter1 = nosVector.iterator();
			while (iter1.hasNext()) {
				NativeObject no = (NativeObject) iter1.next();
				no.setObjectName(co.getObjectName());
				OMElement valueForNO = createOMElementForNativeObjectInput(fac, no, operationNamespace);
				result.add(valueForNO);
			}
		}
		return result;

	}

}
