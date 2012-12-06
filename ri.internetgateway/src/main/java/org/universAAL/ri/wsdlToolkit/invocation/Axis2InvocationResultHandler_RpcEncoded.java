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
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;

public class Axis2InvocationResultHandler_RpcEncoded {

	public static InvocationResult parseRpc_EncodedResult(
			MessageContext inMsgCtx, WSOperation theParsedOperation) {

		InvocationResult theResult = new InvocationResult();
		try {
			Vector parsedOperationOutputsVector = theParsedOperation
					.getHasOutput().getHasNativeOrComplexObjects();
			Iterator parsedOperationOutputsIter = parsedOperationOutputsVector
					.iterator();
			while (parsedOperationOutputsIter.hasNext()) {
				Object parsedOutObj = parsedOperationOutputsIter.next();

				if (parsedOutObj.getClass().getName().contains("NativeObject")) {
					NativeObject outNO = (NativeObject) parsedOutObj;
					OMElementImpl omElement = findTheObjectNodeInTheRpc_EncodedResponseBody(
							inMsgCtx, outNO, null);
					setNativeObjectValue(outNO, omElement);

				} else if (parsedOutObj.getClass().getName()
						.contains("ComplexObject")) {
					ComplexObject outCO = (ComplexObject) parsedOutObj;
					OMElementImpl omElement = findTheObjectNodeInTheRpc_EncodedResponseBody(
							inMsgCtx, null, outCO);
					setComplexObjectValues(inMsgCtx.getEnvelope().getBody(),
							outCO, omElement);

				}
			}

			theResult.responseHasNativeOrComplexObjects = parsedOperationOutputsVector;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return theResult;

	}

	private static OMElementImpl findOMNode_RpcEncoded_AmongChildren_ITERATIVE(
			SOAPBody body, OMElementImpl inputNode, QName resultQName) {
		if (inputNode == null || resultQName == null)
			return null;

		// Iterator
		// iter1=inputNode.getChildrenWithLocalName(resultQName.getLocalPart());
		Iterator iter1 = inputNode.getChildrenWithName(resultQName);
		while (iter1.hasNext()) {
			org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1
					.next();
			OMAttribute att = childOMElement.getAttribute(new QName("href"));
			if (att != null) {
				System.out.println();
				String hasReferenceId = att.getAttributeValue();
				OMElementImpl result = findOMNodeWithTheSpecific_ID_AmongChildren_ITERATIVE(
						null, body, hasReferenceId);
				return result;

			} else {
				return childOMElement;
			}
		}

		Iterator iter2 = inputNode.getChildren();
		while (iter2.hasNext()) {
			Object obj = iter2.next();
			System.out.println(obj.getClass().getName());
			if (obj.getClass().getName()
					.contains("org.apache.axiom.om.impl.llom.OMTextImpl")) {
				org.apache.axiom.om.impl.llom.OMTextImpl t = (org.apache.axiom.om.impl.llom.OMTextImpl) obj;
				System.out.println(t.getText());

			} else if (obj.getClass().getName()
					.contains("org.apache.axiom.om.impl.llom.OMElementImpl")) {
				org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) obj;
				OMElementImpl result = findOMNode_RpcEncoded_AmongChildren_ITERATIVE(
						body, childOMElement, resultQName);
				return result;
			}
		}

		return null;

	}

	private static OMElementImpl findOMNodeWithTheSpecific_ID_AmongChildren_ITERATIVE(
			OMElementImpl omElement, SOAPBody body, String idToFind) {

		if ((body == null && omElement == null) || idToFind == null)
			return null;

		if (idToFind.contains("#")) {
			idToFind = idToFind.replace("#", "");
			idToFind = idToFind.trim();
		}

		if (body != null) {
			// Iterator
			// iter1=inputNode.getChildrenWithLocalName(resultQName.getLocalPart());
			Iterator iter1 = body.getChildren();
			while (iter1.hasNext()) {
				OMElementImpl elem = (OMElementImpl) iter1.next();
				OMAttribute att = elem.getAttribute(new QName("id"));
				if (att != null && att.getAttributeValue() != null
						&& att.getAttributeValue().equals(idToFind)) {
					return elem;
				}
			}

			Iterator iter2 = body.getChildren();
			while (iter2.hasNext()) {
				Object obj = iter2.next();
				System.out.println(obj.getClass().getName());
				if (obj.getClass().getName()
						.contains("org.apache.axiom.om.impl.llom.OMTextImpl")) {
					org.apache.axiom.om.impl.llom.OMTextImpl t = (org.apache.axiom.om.impl.llom.OMTextImpl) obj;
					System.out.println(t.getText());

				} else if (obj
						.getClass()
						.getName()
						.contains("org.apache.axiom.om.impl.llom.OMElementImpl")) {
					org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) obj;
					OMElementImpl result = findOMNodeWithTheSpecific_ID_AmongChildren_ITERATIVE(
							childOMElement, null, idToFind);
					return result;
				}
			}

			return null;

		} else if (omElement != null) {
			// Iterator
			// iter1=inputNode.getChildrenWithLocalName(resultQName.getLocalPart());
			Iterator iter1 = omElement.getChildren();
			while (iter1.hasNext()) {
				OMElementImpl elem = (OMElementImpl) iter1.next();
				OMAttribute att = elem.getAttribute(new QName("id"));
				if (att != null && att.getAttributeValue() != null
						&& att.getAttributeValue().equals(idToFind)) {
					return elem;
				}
			}

			Iterator iter2 = omElement.getChildren();
			while (iter2.hasNext()) {
				Object obj = iter2.next();
				System.out.println(obj.getClass().getName());
				if (obj.getClass().getName()
						.contains("org.apache.axiom.om.impl.llom.OMTextImpl")) {
					org.apache.axiom.om.impl.llom.OMTextImpl t = (org.apache.axiom.om.impl.llom.OMTextImpl) obj;
					System.out.println(t.getText());

				} else if (obj
						.getClass()
						.getName()
						.contains("org.apache.axiom.om.impl.llom.OMElementImpl")) {
					org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) obj;
					OMElementImpl result = findOMNodeWithTheSpecific_ID_AmongChildren_ITERATIVE(
							childOMElement, null, idToFind);
					return result;
				}
			}

			return null;
		}

		return null;
	}

	private static OMElementImpl findTheObjectNodeInTheRpc_EncodedResponseBody(
			MessageContext inMsgCtx, NativeObject no, ComplexObject co) {

		if (inMsgCtx == null || inMsgCtx.getEnvelope() == null
				|| inMsgCtx.getEnvelope().getBody() == null)
			return null;

		SOAPEnvelope response = inMsgCtx.getEnvelope();
		System.out.println(response);
		SOAPBody body = response.getBody();

		QName objectQName = null;

		if (no != null) {
			objectQName = no.getObjectName();

		} else if (co != null) {
			objectQName = co.getObjectName();

		}

		// Search first mesa sta children tou body
		Iterator iter1 = body.getChildrenWithName(objectQName);
		while (iter1.hasNext()) {
			org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1
					.next();
			OMAttribute att = childOMElement.getAttribute(new QName("href"));
			if (att != null && att.getAttributeValue() != null) {
				String referenceId = att.getAttributeValue();
				OMElementImpl result = findOMNodeWithTheSpecific_ID_AmongChildren_ITERATIVE(
						null, body, referenceId);
				return result;
			} else {
				return childOMElement;
			}
		}

		// An den exei vrethei mesa sta children tou body, search mesa sta
		// children twn children tou body (ITERATIVE!!!)
		Iterator iter2 = body.getChildren();
		while (iter2.hasNext()) {
			OMElementImpl childNode = (OMElementImpl) iter2.next();
			OMElementImpl result = findOMNode_RpcEncoded_AmongChildren_ITERATIVE(
					body, childNode, objectQName);
			return result;
		}

		return null;

	}

	private static Iterator findOMNodesAmongChildren_ITERATIVE(
			OMElementImpl inputNode, QName resultQName) {
		if (inputNode == null || resultQName == null)
			return null;

		// Iterator
		// iter1=inputNode.getChildrenWithLocalName(resultQName.getLocalPart());
		Iterator iter1 = inputNode.getChildrenWithName(resultQName);
		if (iter1.hasNext())
			return iter1;

		Iterator iter2 = inputNode.getChildren();
		while (iter2.hasNext()) {
			Object obj = iter2.next();
			System.out.println(obj.getClass().getName());
			if (obj.getClass().getName()
					.contains("org.apache.axiom.om.impl.llom.OMTextImpl")) {
				org.apache.axiom.om.impl.llom.OMTextImpl t = (org.apache.axiom.om.impl.llom.OMTextImpl) obj;
				System.out.println(t.getText());

			} else if (obj.getClass().getName()
					.contains("org.apache.axiom.om.impl.llom.OMElementImpl")) {
				org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) obj;
				Iterator result = findOMNodesAmongChildren_ITERATIVE(
						childOMElement, resultQName);
				return result;

			}
		}

		return null;

	}

	private static void setNativeObjectValue(NativeObject no,
			OMElementImpl omElement) {

		if (omElement == null || no == null)
			return;
		no.setHasValue(omElement.getText());

	}

	private static void setComplexObjectValues(SOAPBody soapBody,
			ComplexObject co, OMElementImpl omElement) {

		if (omElement == null || co == null)
			return;

		if (!co.isIsArrayType()) {
			if (co.getHasComplexObjects() != null
					&& co.getHasComplexObjects().size() > 0) {
				Iterator cosIter = co.getHasComplexObjects().iterator();
				while (cosIter.hasNext()) {
					ComplexObject co1 = (ComplexObject) cosIter.next();

					if (!co1.isIsArrayType()) {
						OMElementImpl omElement1 = findOMNode_RpcEncoded_AmongChildren_ITERATIVE(
								soapBody, omElement, co1.getObjectName());
						// OMElementImpl
						// omElement1=findTheObjectNodeInTheResponseBody(inMsgCtx,
						// null, co1);
						setComplexObjectValues(soapBody, co1, omElement1);

					} else {
						setArrayObjectValues(soapBody, co1, omElement);

					}
				}
			}

			if (co.getHasNativeObjects() != null
					&& co.getHasNativeObjects().size() > 0) {
				Iterator nosIter = co.getHasNativeObjects().iterator();
				while (nosIter.hasNext()) {
					NativeObject no1 = (NativeObject) nosIter.next();
					OMElementImpl omElement1 = findOMNode_RpcEncoded_AmongChildren_ITERATIVE(
							soapBody, omElement, no1.getObjectName());
					setNativeObjectValue(no1, omElement1);
				}
			}

		} else {
			setArrayObjectValues(soapBody, co, omElement);
		}
	}

	private static void setArrayObjectValues(SOAPBody soapBody,
			ComplexObject co, OMElementImpl inputOmElement) {

		OMElementImpl element = null;
		if (inputOmElement == null)
			return;
		if ((inputOmElement.getLocalName() != null && inputOmElement
				.getLocalName().equals(co.getObjectName().getLocalPart()))) {
			element = inputOmElement;
		} else {
			element = findOMNode_RpcEncoded_AmongChildren_ITERATIVE(soapBody,
					inputOmElement, co.getObjectName());
		}
		if (element == null)
			return;

		Iterator childrenElementsIter = element.getChildren();
		Vector arrayCantainedElems = new Vector();
		while (childrenElementsIter.hasNext()) {
			try {
				OMElementImpl el = (OMElementImpl) childrenElementsIter.next();
				OMAttribute att = el.getAttribute(new QName("href"));
				if (att != null && att.getAttributeValue() != null) {
					String hasReferenceId = att.getAttributeValue();
					OMElementImpl element111 = findOMNodeWithTheSpecific_ID_AmongChildren_ITERATIVE(
							null, soapBody, hasReferenceId);
					if (element111 != null) {
						arrayCantainedElems.add(element111);
					}
				}
			} catch (Exception e) {

			}
		}

		if (co.getHasComplexObjects() != null
				&& co.getHasComplexObjects().size() > 0) {
			// Vriskw to type tou ComplexObject pou periexetai sto Array
			ComplexObject co1 = null;
			Iterator cosIter = co.getHasComplexObjects().iterator();
			while (cosIter.hasNext()) {
				// to iteration edw tha exei mono mia epanalipsi (tha treksei
				// mono mia fora)
				co1 = (ComplexObject) cosIter.next();
			}

			// Parse ComplexObjects of response
			if (co1 != null) {
				// Iterator iter1=findOMNodesAmongChildren_ITERATIVE(element,
				// co1.objectName);

				Iterator iter1 = arrayCantainedElems.iterator();
				if (iter1 != null) {
					// Edw tha mpei mono an yparxei domi p.x.
					// "<hasResources><helpResource/><helpResource/></hasResources>"
					boolean firstRun = true;
					while (iter1.hasNext()) {
						org.apache.axiom.om.impl.llom.OMElementImpl elem = (org.apache.axiom.om.impl.llom.OMElementImpl) iter1
								.next();
						if (firstRun) {
							setComplexObjectValues(soapBody, co1, elem);
						} else {
							ComplexObject co2 = co1.cloneTheCO();
							setComplexObjectValues(soapBody, co2, elem);
							co.getHasComplexObjects().add(co2);
						}
						firstRun = false;
					}
				} else {
					// Edw mporei na mpei an yparxei domi p.x.
					// "<hasResources><helpResource/></hasResources>" xwris na
					// yparxoun ta node <helpResource>,</helpResource>
					// P.X. otan to array pou epestrafi exei mono ena object
					setComplexObjectValues(soapBody, co1, element);
				}
			}
		}

		if (co.getHasNativeObjects() != null
				&& co.getHasNativeObjects().size() > 0) {
			Iterator nosIter = co.getHasNativeObjects().iterator();

			int counter = 0;
			while (nosIter.hasNext()) {
				// to iteration edw tha exei mono mia epanalipsi (tha treksei
				// mono mia fora)
				NativeObject no1 = (NativeObject) nosIter.next();
				Iterator iter1 = arrayCantainedElems.iterator();
				int counter2 = 0;
				while (iter1.hasNext()) {
					org.apache.axiom.om.impl.llom.OMElementImpl elem = (org.apache.axiom.om.impl.llom.OMElementImpl) iter1
							.next();
					if (counter2 == counter) {
						no1.setHasValue(elem.getText());
					}
					counter2++;
				}
				counter++;
			}
		}

		System.out.println();
	}

}
