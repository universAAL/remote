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
package org.universAAL.ri.wsdlToolkit.axis2Parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;

/**
 *
 * This class is used for parsing complex elements of a wsdl document
 *
 * @author kgiannou
 */

public class ComplexTypesParser {

	public static HashMap parsedComplexObjectsHashmap;

	public static void parseComplexType(AxisService service, XmlSchemaElement schElemOfType,
			XmlSchemaType xmlSchemaOfType, ComplexObject co, ParsedWSDLDefinition theDefinition,
			boolean calledFromAbstractTypeParser) {

		org.apache.ws.commons.schema.XmlSchemaComplexType ct = null;
		QName complexTypeName = ParsingUtils.getComplexTypeSchemaTypeName(schElemOfType, xmlSchemaOfType);

		if (complexTypeName != null) {
			System.out.println();
			if (complexTypeName.getLocalPart().equalsIgnoreCase("Events")) {
				System.out.println();
			}
			if (parsedComplexObjectsHashmap.containsKey(complexTypeName)) {
				ComplexObject co1 = (ComplexObject) (parsedComplexObjectsHashmap.get(complexTypeName));
				co.setObjectType(co1.getObjectType());
				co.setAdditionalInfo(co1.getAdditionalInfo());
				co.setHasParent(co1.getHasParent());
				co.setIsAbstract(co1.isIsAbstract());
				co.setIsArrayType(co1.isIsArrayType());
				co.setIsInput(co1.isIsInput());
				co.setIsOptional(co1.isIsOptional());

				co.setHasNativeObjects(co1.getHasNativeObjects());
				co.setHasComplexObjects(co1.getHasComplexObjects());
				co.setHasExtendedObjects(co1.getHasExtendedObjects());

				if (co.getObjectName() == null) {
					if (schElemOfType != null) {
						if (schElemOfType.getQName() != null) {
							co.setObjectName(schElemOfType.getQName());
							if (schElemOfType.getMinOccurs() == 0 || schElemOfType.isNillable()) {
								co.setIsOptional(true);
							}
							if (schElemOfType.getMaxOccurs() > 1 && !co.isIsArrayType()) {
								ComplexObject co3 = new ComplexObject();
								co3.setObjectName(new QName(co.getObjectType().getNamespaceURI(),
										co.getObjectType().getLocalPart().replace("[]", ""),
										co.getObjectType().getPrefix()));
								co3.setObjectType(new QName(co.getObjectType().getNamespaceURI(),
										co.getObjectType().getLocalPart().replace("[]", ""),
										co.getObjectType().getPrefix()));
								co3.setHasParent(co);
								co3.setIsOptional(co.isIsOptional());
								co3.setIsAbstract(co.isIsAbstract());
								co3.setIsInput(co.isIsInput());
								co3.setHasComplexObjects(co.getHasComplexObjects());
								for (int i = 0; i < co.getHasComplexObjects().size(); i++) {
									((ComplexObject) co.getHasComplexObjects().get(i)).setHasParent(co3);
								}
								co3.setHasExtendedObjects(co.getHasExtendedObjects());
								for (int i = 0; i < co.getHasExtendedObjects().size(); i++) {
									if (co.getHasExtendedObjects().get(i) instanceof ComplexObject) {
										((ComplexObject) co.getHasExtendedObjects().get(i)).setHasParent(co3);
									}
									if (co.getHasExtendedObjects().get(i) instanceof NativeObject) {
										((NativeObject) co.getHasExtendedObjects().get(i)).setHasParent(co3);
									}
								}
								co3.setHasNativeObjects(co.getHasNativeObjects());
								for (int i = 0; i < co.getHasNativeObjects().size(); i++) {
									((NativeObject) co.getHasNativeObjects().get(i)).setHasParent(co3);
								}
								co.setHasComplexObjects(new Vector());
								co.setHasExtendedObjects(new Vector());
								co.setHasNativeObjects(new Vector());
								co.getHasComplexObjects().add(co3);
								co3.setHasParent(co);
								co.setIsArrayType(true);
								if (!co.getObjectType().getLocalPart().contains("[]")) {
									co.setObjectType(new QName(co.getObjectType().getNamespaceURI(),
											co.getObjectType().getLocalPart() + "[]", co.getObjectType().getPrefix()));
								} else {
									co.setObjectType(new QName(co.getObjectType().getNamespaceURI(),
											co.getObjectType().getLocalPart(), co.getObjectType().getPrefix()));
								}
							}
						}
					}

				}
				return;
			} else {
				parsedComplexObjectsHashmap.put(complexTypeName, co);
			}
		}

		if (schElemOfType != null) {

			System.out.println("\t\t\t\t### COMPLEX TYPE " + schElemOfType.getName() + "  ["
					+ schElemOfType.getSchemaTypeName() + "]      PARSING...");

			if (schElemOfType.getMinOccurs() == 0 || schElemOfType.isNillable()) {
				co.setIsOptional(true);
			}
			co.setObjectName(schElemOfType.getQName());

			if (schElemOfType.getSchemaTypeName() != null) {
				co.setObjectType(schElemOfType.getSchemaTypeName());

				try {
					ct = (org.apache.ws.commons.schema.XmlSchemaComplexType) schElemOfType.getSchemaType();
				} catch (Exception e) {
					theDefinition.getContainingErrors().add(e.toString());
					e.printStackTrace();
					// -System.exit(-5);
				}
			} else if (schElemOfType.getSchemaType() != null) {
				try {
					ct = (org.apache.ws.commons.schema.XmlSchemaComplexType) schElemOfType.getSchemaType();
				} catch (Exception e) {
					theDefinition.getContainingErrors().add(e.toString());
					e.printStackTrace();
					// -System.exit(-5);
				}
			} else {
			}
		} else if (xmlSchemaOfType != null) {
			System.out.println("\t\t\t\t### COMPLEX TYPE " + xmlSchemaOfType.getName() + "  [" + "]      PARSING...");

			try {
				ct = (org.apache.ws.commons.schema.XmlSchemaComplexType) xmlSchemaOfType;
				// To object name se ayti tin periptwsi exei ginei set apo prin
				// apo tin klisi tis synartisis
				co.setObjectType(ct.getQName());

			} catch (Exception e) {
				theDefinition.getContainingErrors().add(e.toString());
				e.printStackTrace();
				// -System.exit(-6);
			}
		}

		if (ct == null) {
			return;
		}

		if (ct.getParticle() != null && ct.getParticle().getClass().getName().contains("XmlSchemaAll")) {
			XmlSchemaAll xsa = (XmlSchemaAll) ct.getParticle();
			AdditionalTypesParser.parseXmlSchemaAllType(service, xsa, co, theDefinition);
			if (co.getObjectName() == null) {
				System.out.println();
			}
			ParsingUtils.checkIfCOisAnyObjectType(co);

			System.out.println();

			return;
		}
		if (schElemOfType != null && schElemOfType.getMaxOccurs() > 1) {
			// Call operation for Handling of Array Types Here
			if (ct.getQName() != null) {
				co.setObjectType(ct.getQName());
			} else if (ct.getName() != null) {
				co.setObjectType(new QName(ct.getName()));
			} else {
				// theDefinition.containingErrors.add("WARNING @ line ~1118...
				// ArrayType was null!");
			}

			co.setIsArrayType(true);
			AdditionalTypesParser.parseArrayType(service, co, schElemOfType.getSchemaTypeName(), theDefinition,
					schElemOfType.getSchemaType(), calledFromAbstractTypeParser);

			if (co.getObjectType() == null) {
				co.setObjectType(co.getObjectName());
			}
			co.setObjectType(new QName(co.getObjectType().getNamespaceURI(), co.getObjectType().getLocalPart() + "[]",
					co.getObjectType().getPrefix()));
			return;
		}

		XmlSchemaSequence sequenceOfComplexType = null;
		XmlSchemaChoice choiceOfComplexType = null;

		if (ct.getParticle() != null) {
			try {
				sequenceOfComplexType = (XmlSchemaSequence) ct.getParticle();
			} catch (Exception e) {
				try {
					choiceOfComplexType = (XmlSchemaChoice) ct.getParticle();
				} catch (Exception e1) {
					System.out.println();
				}
			}
		}

		if (sequenceOfComplexType == null) {
			// Kati den pige kala edw...
			// Check ti ginetai se MIZAR gia "### COMPLEX TYPE address
			// PARSING..."
			// EINAI TYPE pou kanei extend to "Address"
			// exei <extension base="mpk:AddressType">

			if (choiceOfComplexType != null) {
				System.out.println();

				ComplexObject co1 = new ComplexObject();
				co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
				co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
				co1.setIsAbstract(true);

				AdditionalTypesParser.parseXMLSchemaChoiceElement(service, choiceOfComplexType, co1, theDefinition,
						calledFromAbstractTypeParser);

				if (choiceOfComplexType.getMinOccurs() == 0) {
					co1.setIsOptional(true);
				}

				if (choiceOfComplexType.getMaxOccurs() > 1) {
					// Array Type
					ComplexObject arrayCO = new ComplexObject();
					arrayCO.setObjectName(co1.getObjectName());
					arrayCO.setObjectType(new QName(co1.getObjectType().getLocalPart() + "[]"));
					arrayCO.setIsArrayType(true);
					arrayCO.getHasComplexObjects().add(co1);
					arrayCO.setIsOptional(co1.isIsOptional());
					co.getHasComplexObjects().add(arrayCO);
				} else {
					co.getHasComplexObjects().add(co1);
				}
			} else {
				XmlSchemaComplexContent complexContent = null;

				// MITSOS 8-12-08 START
				if (ct.getContentModel() != null) {
					if (ct.getContentModel().getClass().getName().contains("XmlSchemaComplexContent")) {
						try {
							complexContent = (XmlSchemaComplexContent) ct.getContentModel();
							System.out.println(complexContent.getContent().getClass());

							parseComplexContent(service, complexContent, co, theDefinition,
									calledFromAbstractTypeParser, ct.getQName());

							if (co.getObjectType() == null) {
								co.setObjectType(co.getObjectName());
							}

							System.out.println();
						} catch (Exception e) {
							System.out.println();
							try {
							} catch (Exception e1) {
								e.printStackTrace();
								theDefinition.getContainingErrors().add(e1.toString());
							}
						}

					} else if (ct.getContentModel().getClass().getName().contains("XmlSchemaSimpleContent")) {
						XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) ct.getContentModel();
						SimpleTypesParser.parseSimpleContent(service, simpleContent, co, theDefinition);

					} else {
						System.out.println("ERROR 1 @ Axis2Parser.java @ LINE ~805!!! ...8-12-08 change");
						theDefinition.getContainingErrors()
								.add("ERROR 1 @ Axis2Parser.java @ LINE ~805!!! ...8-12-08 change");
						// System.exit(-1);
					}

				} else {
					if (ct.isAbstract()) {
					} else {
						XmlSchemaObjectCollection attsCol = ct.getAttributes();
						if (attsCol != null) {
						} else {
							System.out.println("WSDL Warning... Complex Type " + ct.getName()
									+ " is not well defined!  ...@Axis2Parser.java @ LINE ~1643");
							theDefinition.getContainingErrors().add("WSDL Warning... Complex Type " + ct.getName()
									+ " is not well defined!  ...@Axis2Parser.java @ LINE ~812");
						}
					}
				}

				// MITSOS 8-12-08 END
				if (co.getObjectType() == null) {
					System.out.println();
				}
			}

			XmlSchemaObjectCollection attsCol = ct.getAttributes();
			if (attsCol != null) {
				Iterator iter2 = attsCol.getIterator();
				while (iter2.hasNext()) {
					Object obj = iter2.next();
					if (obj.getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
						org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;
						Object res1 = AdditionalTypesParser.parseXmlSchemaAttribute(att, service, theDefinition);
						if (res1 != null) {
							if (res1.getClass().getName().contains("NativeObject")) {
								NativeObject no12 = (NativeObject) res1;
								// System.out.println(no12.objectName);
								co.getHasNativeObjects().add(no12);
							} else if (res1.getClass().getName().contains("ComplexObject")) {
								ComplexObject co12 = (ComplexObject) res1;
								// System.out.println(co12.objectName);
								co.getHasComplexObjects().add(co12);
							}
						}
					} else if (obj.getClass().getName()
							.contains("org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")) {
						System.out.println();
						ComplexObject co1 = new ComplexObject();
						AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(service,
								(org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef) obj, co1, theDefinition);

						if (co1 != null) {
							for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
								co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
							}
							for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
								co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
							}
						} else {
							System.out.println();
						}
						System.out.println();
					} else {
						System.out.println();
					}
				}
			}
			ParsingUtils.checkIfCOisAnyObjectType(co);
			if (co.getObjectType() == null) {
				System.out.println();
			}
			return;
		}
		if (!calledFromAbstractTypeParser) {
			// If to complexObject einai Abstract...
			// kane parse ola ta types pou kanoun extend to AbstractType kai
			// valta sti lista.
			if (ct.isAbstract()) {
				co.setIsAbstract(true);
				System.out.println(ct.getQName().toString());
				AdditionalTypesParser.parseAllExtensionTypesOfTheAbstractType(service, co, ct.getQName(),
						theDefinition);
				System.out.println(ct.getClass().getName());
			}
		}

		// EDW MPAINEI AN DEN YPARXEI ComplexContent kai einai apla ena
		// ComplexType
		if (sequenceOfComplexType == null || sequenceOfComplexType.getItems() == null) {
			System.out.println();
		}

		Iterator particleIter = sequenceOfComplexType.getItems().getIterator();
		while (particleIter.hasNext()) {
			Object newObj1 = particleIter.next();
			if (newObj1 != null
					&& newObj1.getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaElement")) {

				org.apache.ws.commons.schema.XmlSchemaElement newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaElement) newObj1;
				System.out.println("\t\t\t\t\t" + newSimpleOrComplexObjectElement.getName() + "   "
						+ newSimpleOrComplexObjectElement.getSchemaTypeName());

				if (newSimpleOrComplexObjectElement.getSchemaType() != null) {
					System.out.println("\t\t\t\t\t\t#"
							+ newSimpleOrComplexObjectElement.getSchemaType().getClass().toString() + "#");

					boolean typeParsed = false;
					if (newSimpleOrComplexObjectElement.getSchemaType().getClass().toString()
							.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
						NativeObject no1 = new NativeObject();
						ComplexObject unionCO = SimpleTypesParser.parseSimpleType(newSimpleOrComplexObjectElement, null,
								no1, theDefinition, service);
						if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
								|| newSimpleOrComplexObjectElement.isNillable()) {
							no1.setIsOptional(true);
						}
						if (unionCO != null) {
							if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
								ComplexObject noArrayCO = new ComplexObject();

								noArrayCO.setObjectName(no1.getObjectName());
								noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
										no1.getObjectType().getLocalPart() + "[]", no1.getObjectType().getPrefix()));
								noArrayCO.setIsArrayType(true);
								noArrayCO.getHasComplexObjects().add(unionCO);
								co.getHasComplexObjects().add(noArrayCO);
								typeParsed = true;

							} else {
								typeParsed = true;
								co.getHasComplexObjects().add(unionCO);
							}
							System.out.println();
						} else {
							if (no1 != null && no1.getAdditionalInfo() != null
									&& no1.getAdditionalInfo().contains("isListType")) {
								if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
									ComplexObject noArrayCO = new ComplexObject();
									noArrayCO.setObjectName(no1.getObjectName());
									noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart() + "[][]",
											no1.getObjectType().getPrefix()));
									noArrayCO.setIsArrayType(true);
									ComplexObject noArrayCO_ListNO = new ComplexObject();
									noArrayCO_ListNO.setObjectName(no1.getObjectName());
									noArrayCO_ListNO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart() + "[]",
											no1.getObjectType().getPrefix()));
									noArrayCO_ListNO.setIsArrayType(true);
									noArrayCO_ListNO.setIsOptional(no1.isIsOptional());
									noArrayCO_ListNO.getHasNativeObjects().add(no1);
									noArrayCO.getHasComplexObjects().add(noArrayCO_ListNO);
									co.getHasComplexObjects().add(noArrayCO);
									typeParsed = true;

								} else {
									typeParsed = true;
									ComplexObject noArrayCO = new ComplexObject();
									noArrayCO.setObjectName(no1.getObjectName());
									noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart() + "[]",
											no1.getObjectType().getPrefix()));
									noArrayCO.setIsArrayType(true);
									noArrayCO.getHasNativeObjects().add(no1);
									noArrayCO.setIsOptional(no1.isIsOptional());
									co.getHasComplexObjects().add(noArrayCO);
								}
							} else {
								if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
									ComplexObject noArrayCO = new ComplexObject();

									noArrayCO.setObjectName(no1.getObjectName());
									noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart() + "[]",
											no1.getObjectType().getPrefix()));
									noArrayCO.setIsArrayType(true);
									noArrayCO.getHasNativeObjects().add(no1);
									co.getHasComplexObjects().add(noArrayCO);
									typeParsed = true;

								} else {
									typeParsed = true;
									co.getHasNativeObjects().add(no1);
								}
							}
						}

					} else if (newSimpleOrComplexObjectElement.getSchemaType().getClass().toString()
							.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {

						if (newSimpleOrComplexObjectElement.getSchemaType() == null
								|| newSimpleOrComplexObjectElement.getSchemaType().getName() == null) {
							System.out.println();
						}

						ComplexObject co1 = new ComplexObject();
						// edw prepei na mpei elegxos an to
						// newSimpleOrComplexObjectElement->schemaType einai
						// idio me to ct
						// gia na apofygw to StackOverflowError
						// DE XREAIZETAI PLEON 10-2-2010
						parseComplexType(service, newSimpleOrComplexObjectElement, null, co1, theDefinition,
								calledFromAbstractTypeParser);
						typeParsed = true;

						// if (newSimpleOrComplexObjectElement.getMaxOccurs() >
						// 1 && !co.isIsArrayType()) {
						// co1.setIsArrayType(true);
						// if
						// (!co1.getObjectType().getLocalPart().contains("[]"))
						// {
						// co1.setObjectType(new
						// QName(co1.getObjectType().getNamespaceURI(),
						// co1.getObjectType().getLocalPart() + "[]",
						// co1.getObjectType().getPrefix()));
						// }
						// if (co1.getHasComplexObjects().size() != 0 &&
						// !((ComplexObject)
						// co1.getHasComplexObjects().get(0)).getObjectName().getLocalPart().equals(co1.getObjectName().getLocalPart()))
						// {
						// ComplexObject co3 = new ComplexObject();
						// co3.setObjectName(new
						// QName(co1.getObjectType().getNamespaceURI(),
						// co1.getObjectType().getLocalPart().replace("[]", ""),
						// co1.getObjectType().getPrefix()));
						// co3.setObjectType(new
						// QName(co1.getObjectType().getNamespaceURI(),
						// co1.getObjectType().getLocalPart().replace("[]", ""),
						// co1.getObjectType().getPrefix()));
						// co3.setHasParent(co1);
						// co3.setIsOptional(co1.isIsOptional());
						// co3.setIsAbstract(co1.isIsAbstract());
						// co3.setIsInput(co1.isIsInput());
						//
						// ComplexObject tmp = new ComplexObject();
						// tmp = co1;
						// co1 = co3;
						// co3 = tmp;
						//
						// co3.getHasComplexObjects().add(co1);
						// co.getHasComplexObjects().add(co3);
						// co1.setHasParent(co3);
						// co3.setHasParent(co);
						// }
						// } else {
						co.getHasComplexObjects().add(co1);
						co1.setHasParent(co);
						// }
					}
					if (co.getObjectType() != null && co.getObjectType().getLocalPart().contains("ArrayOfError")) {
						System.out.println();
					}
					if (!typeParsed) {
						System.out.println("ERROR 1!!!!!!!!!!!!!!!!!! @line ~862");
						theDefinition.getContainingErrors().add("ERROR 1!!!!!!!!!!!!!!!!!! @line ~862");
						// System.exit(-1);
					}
				} else {
					// MITSOS 22-1-2010 START
					// if(newSimpleOrComplexObjectElement.getSchemaTypeName()!=null&&
					// newSimpleOrComplexObjectElement.getSchemaTypeName().getLocalPart().equals("anyType")){
					XmlSchemaType newSchemaType = null;
					if (newSimpleOrComplexObjectElement.getSchemaTypeName() != null) {
						// try to find schema type
						newSchemaType = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
								newSimpleOrComplexObjectElement.getSchemaTypeName());
						if (newSchemaType == null) {
							newSchemaType = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
									service, newSimpleOrComplexObjectElement.getSchemaTypeName());
						}
					}
					if (newSchemaType != null) {
						// GET SCHEMA TYPE NAME if not found, proceed as
						// before...
						if (newSchemaType.getClass().toString()
								.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
							NativeObject no1 = new NativeObject();
							if (newSimpleOrComplexObjectElement.getQName() != null) {
								no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
							} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
								no1.setObjectName(newSimpleOrComplexObjectElement.getRefName());
							} else if (newSimpleOrComplexObjectElement.getName() != null) {
								no1.setObjectName(new QName(newSimpleOrComplexObjectElement.getName()));
							} else {
								no1.setObjectName(new QName("UNDEFINED variable name"));
								theDefinition.getContainingErrors()
										.add("WARNING @line ~583... UNDEFINED Variable name!!!");
								System.out.println("WARNING @line ~583... UNDEFINED Variable name!!!");
							}
							ComplexObject unionCO = SimpleTypesParser.parseSimpleType(null, newSchemaType, no1,
									theDefinition, service);
							if (unionCO == null) {
								if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
										|| newSimpleOrComplexObjectElement.isNillable()) {
									no1.setIsOptional(true);
								}
								if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1
										|| (no1.getAdditionalInfo() != null
												&& no1.getAdditionalInfo().contains("isListType"))) {
									ComplexObject noArrayCO = new ComplexObject();
									noArrayCO.setObjectName(no1.getObjectName());
									noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart() + "[]",
											no1.getObjectType().getPrefix()));
									noArrayCO.setIsArrayType(true);
									noArrayCO.getHasNativeObjects().add(no1);
									noArrayCO.setIsOptional(no1.isIsOptional());
									co.getHasComplexObjects().add(noArrayCO);

								} else {
									co.getHasNativeObjects().add(no1);
								}
							} else {
								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(no1.getObjectName());
								if (newSchemaType.getQName() != null) {
									co1.setObjectType(newSchemaType.getQName());
								} else {
									System.out.println();
								}
								co1.getHasComplexObjects().add(unionCO);
								co.getHasComplexObjects().add(co1);
							}

						} else if (newSchemaType.getClass().toString()
								.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {
							ComplexObject co1 = new ComplexObject();
							if (newSimpleOrComplexObjectElement.getQName() != null) {
								co1.setObjectName(newSimpleOrComplexObjectElement.getQName());// Panta
																								// prin
																								// apo
																								// ayto
																								// ton
																								// tropo
																								// klisis
																								// prepei
																								// na
																								// exw
																								// dwsei
																								// prwta
																								// to
																								// onoma
																								// tou
																								// co
							} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
								co1.setObjectName(newSimpleOrComplexObjectElement.getRefName());
							} else if (newSimpleOrComplexObjectElement.getName() != null) {
								co1.setObjectName(new QName(newSimpleOrComplexObjectElement.getName()));
							} else {
								co1.setObjectName(new QName("UNDEFINED variable name"));
								theDefinition.getContainingErrors()
										.add("WARNING @line ~2248... UNDEFINED Variable name!!!");
								System.out.println("WARNING @line ~2248... UNDEFINED Variable name!!!");
							}
							// Panta prin apo ayto ton tropo klisis prepei na
							// exw dwsei prwta to onoma tou co
							parseComplexType(service, null, newSchemaType, co1, theDefinition,
									calledFromAbstractTypeParser);

							if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
									|| newSimpleOrComplexObjectElement.isNillable()) {
								co1.setIsOptional(true);
							}
							if (newSimpleOrComplexObjectElement != null
									&& newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
								ComplexObject coArrayCO = new ComplexObject();
								coArrayCO.setObjectName(co1.getObjectName());

								coArrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
										co1.getObjectType().getLocalPart() + "[]", co1.getObjectType().getPrefix()));
								coArrayCO.setIsArrayType(true);
								coArrayCO.getHasComplexObjects().add(co1);
								coArrayCO.setIsOptional(co1.isIsOptional());
								co.getHasComplexObjects().add(coArrayCO);
							} else {
								co.getHasComplexObjects().add(co1);
							}

						}

					} else {
						try {
							if (newSimpleOrComplexObjectElement.getRefName() == null) {
								// if(schemaTypeName==null)-> Vector
								if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
									System.out.println("WWW Vector!!!");
									if (newSimpleOrComplexObjectElement.getSchemaTypeName() != null
											&& newSimpleOrComplexObjectElement.getSchemaTypeName().getLocalPart()
													.equals("anyType")
											&& newSimpleOrComplexObjectElement.getName() != null
											&& newSimpleOrComplexObjectElement.getName().equals("item")) {

										System.out.println(newSimpleOrComplexObjectElement.getName());
										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(newSimpleOrComplexObjectElement.getQName());
										co1.setObjectType(newSimpleOrComplexObjectElement.getSchemaTypeName());
										co1.setIsArrayType(true);
										// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
										co.getHasComplexObjects().add(co1);
									} else if (newSimpleOrComplexObjectElement.getSchemaTypeName() == null
											|| newSimpleOrComplexObjectElement.getName() == null) {
										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(newSimpleOrComplexObjectElement.getQName());
										co1.setObjectType(newSimpleOrComplexObjectElement.getQName());
										co1.setIsArrayType(true);
										// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
										co.getHasComplexObjects().add(co1);
									}
								} else {
									ComplexObject co1 = new ComplexObject();
									co1.setObjectName(newSimpleOrComplexObjectElement.getQName());
									co1.setObjectType(newSimpleOrComplexObjectElement.getSchemaTypeName());
									// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
									if (newSimpleOrComplexObjectElement.getSchemaType() == null) {
										XmlSchemaType xmlSchemaType = ParsingUtils
												.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
														newSimpleOrComplexObjectElement.getSchemaTypeName());
										if (xmlSchemaType == null) {
											xmlSchemaType = ParsingUtils
													.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service,
															newSimpleOrComplexObjectElement.getSchemaTypeName());
										}
										NativeObject no1 = new NativeObject();
										// no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
										if (newSimpleOrComplexObjectElement.getQName() != null) {
											no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
										} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
											no1.setObjectName(newSimpleOrComplexObjectElement.getRefName());
										} else if (newSimpleOrComplexObjectElement.getName() != null) {
											no1.setObjectName(new QName(newSimpleOrComplexObjectElement.getName()));
										} else {
											no1.setObjectName(new QName("UNDEFINED variable name"));
											theDefinition.getContainingErrors()
													.add("WARNING @line ~737... UNDEFINED Variable name!!!");
											System.out.println("WARNING @line ~737... UNDEFINED Variable name!!!");
										}

										ComplexObject unionCO = SimpleTypesParser.parseSimpleType(null, xmlSchemaType,
												no1, theDefinition, service);
										if (unionCO == null) {
											if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1
													|| (no1.getAdditionalInfo() != null
															&& no1.getAdditionalInfo().contains("isListType"))) {
												ComplexObject noArrayCO = new ComplexObject();

												noArrayCO.setObjectName(no1.getObjectName());
												noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
														no1.getObjectType().getLocalPart() + "[]",
														no1.getObjectType().getPrefix()));
												noArrayCO.setIsArrayType(true);
												noArrayCO.getHasNativeObjects().add(no1);
												co.getHasComplexObjects().add(noArrayCO);

											} else {
												co.getHasNativeObjects().add(no1);
											}
										} else {
											ComplexObject co2 = new ComplexObject();
											co2.setObjectName(no1.getObjectName());
											if (xmlSchemaType.getQName() != null) {
												co2.setObjectType(xmlSchemaType.getQName());
											} else {
												System.out.println();
											}
											co2.getHasComplexObjects().add(unionCO);
											co.getHasComplexObjects().add(co2);
										}

									} else {
										parseComplexType(service, newSimpleOrComplexObjectElement, null, co1,
												theDefinition, calledFromAbstractTypeParser);
										theDefinition.getContainingErrors().add("WARNING @ ~777 ...");
										System.out.println("WARNING @ ~777 ...");
										co.getHasComplexObjects().add(co1);
									}

								}
							} else {
								System.out.println("DEN HTAN VECTOR... Exei referenced type...? 2");
								System.out.println(newSimpleOrComplexObjectElement.getName());
								System.out.println(newSimpleOrComplexObjectElement.getRefName());

								try {
									if (newSimpleOrComplexObjectElement.getRefName() != null) {
										org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType = ParsingUtils
												.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
														newSimpleOrComplexObjectElement.getRefName());
										if (xmlSchemaType == null) {
											xmlSchemaType = ParsingUtils
													.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service,
															newSimpleOrComplexObjectElement.getRefName());
										}

										if (xmlSchemaType != null) {

											boolean typeParsed = false;
											if (xmlSchemaType.getClass().toString()
													.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
												NativeObject no1 = new NativeObject();

												if (newSimpleOrComplexObjectElement.getQName() != null) {
													no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
												} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
													no1.setObjectName(newSimpleOrComplexObjectElement.getRefName());
												} else if (newSimpleOrComplexObjectElement.getName() != null) {
													no1.setObjectName(
															new QName(newSimpleOrComplexObjectElement.getName()));
												} else {
													no1.setObjectName(new QName("UNDEFINED variable name"));
													theDefinition.getContainingErrors()
															.add("WARNING @line ~2248... UNDEFINED Variable name!!!");
													System.out.println(
															"WARNING @line ~2248... UNDEFINED Variable name!!!");
												}
												ComplexObject unionCO = SimpleTypesParser.parseSimpleType(null,
														xmlSchemaType, no1, theDefinition, service);
												if (unionCO == null) {
													if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
															|| newSimpleOrComplexObjectElement.isNillable()) {
														no1.setIsOptional(true);
													}
													typeParsed = true;
													if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1
															|| (no1.getAdditionalInfo() != null && no1
																	.getAdditionalInfo().contains("isListType"))) {
														ComplexObject noArrayCO = new ComplexObject();

														noArrayCO.setObjectName(no1.getObjectName());
														noArrayCO.setObjectType(
																new QName(no1.getObjectType().getNamespaceURI(),
																		no1.getObjectType().getLocalPart() + "[]",
																		no1.getObjectType().getPrefix()));
														noArrayCO.setIsArrayType(true);
														noArrayCO.getHasNativeObjects().add(no1);
														noArrayCO.setIsOptional(no1.isIsOptional());
														co.getHasComplexObjects().add(noArrayCO);
														typeParsed = true;

													} else {
														typeParsed = true;
														co.getHasNativeObjects().add(no1);
													}
												} else {
													ComplexObject co2 = new ComplexObject();
													co2.setObjectName(no1.getObjectName());
													if (xmlSchemaType.getQName() != null) {
														co2.setObjectType(xmlSchemaType.getQName());
													} else {
														System.out.println();
													}
													co2.getHasComplexObjects().add(unionCO);
													co.getHasComplexObjects().add(co2);
												}

											} else if (xmlSchemaType.getClass().toString()
													.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {
												ComplexObject co1 = new ComplexObject();
												if (newSimpleOrComplexObjectElement.getQName() != null) {
													co1.setObjectName(newSimpleOrComplexObjectElement.getQName());// Panta
																													// prin
																													// apo
																													// ayto
																													// ton
																													// tropo
																													// klisis
																													// prepei
																													// na
																													// exw
																													// dwsei
																													// prwta
																													// to
																													// onoma
																													// tou
																													// co
												} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
													co1.setObjectName(newSimpleOrComplexObjectElement.getRefName());
												} else if (newSimpleOrComplexObjectElement.getName() != null) {
													co1.setObjectName(
															new QName(newSimpleOrComplexObjectElement.getName()));
												} else {
													co1.setObjectName(new QName("UNDEFINED variable Name"));
													theDefinition.getContainingErrors()
															.add("WARNING @line ~2424... UNDEFINED Variable name!!!");
													System.out.println(
															"WARNING @line ~2424... UNDEFINED Variable name!!!");
													System.out.println();
												}
												parseComplexType(service, null, xmlSchemaType, co1, theDefinition,
														calledFromAbstractTypeParser);

												if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
														|| newSimpleOrComplexObjectElement.isNillable()) {
													co1.setIsOptional(true);
												}

												if (newSimpleOrComplexObjectElement != null
														&& newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
													ComplexObject coArrayCO = new ComplexObject();
													coArrayCO.setObjectName(co1.getObjectName());
													// co1.setObjectType(new
													// QName("XA!"));
													coArrayCO.setObjectType(
															new QName(co1.getObjectType().getNamespaceURI(),
																	co1.getObjectType().getLocalPart() + "[]",
																	co1.getObjectType().getPrefix()));
													coArrayCO.setIsArrayType(true);
													coArrayCO.getHasComplexObjects().add(co1);
													coArrayCO.setIsOptional(co1.isIsOptional());
													co.getHasComplexObjects().add(coArrayCO);

												} else {
													co.getHasComplexObjects().add(co1);
												}
												typeParsed = true;
											}
											if (!typeParsed) {
												theDefinition.getContainingErrors()
														.add("ERROR 1!!!!!!!!!!!!!!!!!! ...@line ~2499");
												System.out.println("ERROR 1!!!!!!!!!!!!!!!!!! ...@line ~2499");
												// System.exit(-1);
											}
										} else {// if schemaType ==null -> den
												// mporese na vrethei sta
												// elements kai schemaTypes...
											System.out.println("wx aman 21-1-2010");

											Object res123 = ParsingUtils.tryToFindAndParseAttributeForSpecificObject(
													theDefinition, service,
													newSimpleOrComplexObjectElement.getRefName());
											if (res123 != null) {
												if (res123.getClass().getName().contains("NativeObject")) {
													co.getHasNativeObjects().add(res123);
													continue;
												} else if (res123.getClass().getName().contains("ComplexObject")) {
													co.getHasComplexObjects().add(res123);
													continue;
												}
											} else {
												System.out.println("XA! W!");
												ComplexObject co123 = new ComplexObject();
												if (newSimpleOrComplexObjectElement.getQName() != null) {
													co123.setObjectName(newSimpleOrComplexObjectElement.getQName());
												} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
													co123.setObjectName(newSimpleOrComplexObjectElement.getRefName());
												} else if (newSimpleOrComplexObjectElement.getName() != null) {
													co123.setObjectName(
															new QName(newSimpleOrComplexObjectElement.getName()));
												} else {
													co123.setObjectName(new QName("UNDEFINED variable name!"));
													theDefinition.getContainingErrors()
															.add("ERROR @line ~2527... UNDEFINED Variable name!!!");
													System.out
															.println("ERROR @line ~2527... UNDEFINED Variable name!!!");
												}
												co123.setObjectType(new QName("Object"));
												co.getHasComplexObjects().add(co123);
											}
											continue;
										}
									}
								} catch (Exception e1) {
									theDefinition.getContainingErrors().add(e1.toString());
									e1.printStackTrace();
								}
							}
						} catch (Exception e) {
							theDefinition.getContainingErrors().add(e.toString());
							e.printStackTrace();
						}
					}
				}
			} else {
				// EINAI XmlSchemaAny
				try {
					org.apache.ws.commons.schema.XmlSchemaAny newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaAny) newObj1;
					ComplexObject co1 = new ComplexObject();
					co1.setObjectName(new QName("any"));
					co1.setObjectType(new QName("Object"));

					if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
						co1.setIsOptional(true);
					}

					if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
						// Array Type
						ComplexObject arrayCO = new ComplexObject();
						arrayCO.setObjectName(co1.getObjectName());
						// arrayCO.setObjectType(new
						// QName(co1.getObjectType().getLocalPart()+"[]"));
						arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
								co1.getObjectType().getLocalPart() + "[]", co1.getObjectType().getPrefix()));
						arrayCO.setIsArrayType(true);
						arrayCO.getHasComplexObjects().add(co1);
						arrayCO.setIsOptional(co1.isIsOptional());
						co.getHasComplexObjects().add(arrayCO);
					} else {
						co.getHasComplexObjects().add(co1);
					}

					// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
					// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
					System.out.println("aaa!");
				} catch (Exception e) {
					try {
						if (newObj1.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaChoice")) {
							org.apache.ws.commons.schema.XmlSchemaChoice newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaChoice) newObj1;

							ComplexObject co1 = new ComplexObject();
							co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
							co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
							co1.setIsAbstract(true);

							AdditionalTypesParser.parseXMLSchemaChoiceElement(service, newSimpleOrComplexObjectElement,
									co1, theDefinition, calledFromAbstractTypeParser);

							if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
								co1.setIsOptional(true);
							}

							if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
								// Array Type
								ComplexObject arrayCO = new ComplexObject();
								arrayCO.setObjectName(co1.getObjectName());
								// arrayCO.setObjectType(new
								// QName(co1.getObjectType()+"[]"));
								arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
										co1.getObjectType().getLocalPart() + "[]", co1.getObjectType().getPrefix()));
								arrayCO.setIsArrayType(true);
								arrayCO.getHasComplexObjects().add(co1);
								arrayCO.setIsOptional(co1.isIsOptional());
								co.getHasComplexObjects().add(arrayCO);
							} else {
								co.getHasComplexObjects().add(co1);
							}

							// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
							// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
							System.out.println("aaa!");
						} else if (newObj1.getClass().getName()
								.contains("org.apache.ws.commons.schema.XmlSchemaGroupRef")) {
							System.out.println();
							ComplexObject co1 = new ComplexObject();
							AdditionalTypesParser.parseXmlSchemaGroupRefElement(service,
									(org.apache.ws.commons.schema.XmlSchemaGroupRef) newObj1, co1, theDefinition);

							if (co1 != null) {
								for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
									co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
								}
								for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
									co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
								}
							} else {
								System.out.println();
							}
							System.out.println();
						} else if (newObj1.getClass().getName()
								.contains("org.apache.ws.commons.schema.XmlSchemaSequence")) {
							XmlSchemaSequence ctSequence = (XmlSchemaSequence) newObj1;
							System.out.println();
							if (ctSequence != null) {// Change made on 25-1-2010
								Iterator containedObjectsIter = ctSequence.getItems().getIterator();
								while (containedObjectsIter.hasNext()) {
									Object obj1 = containedObjectsIter.next();
									if (obj1.getClass().getName().contains("XmlSchemaElement")) {
										org.apache.ws.commons.schema.XmlSchemaElement objectXMLSchemaElement = (XmlSchemaElement) obj1;

										if (objectXMLSchemaElement.getSchemaType() != null) {
											boolean typeParsed = false;
											if (objectXMLSchemaElement.getSchemaType().getClass().toString()
													.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
												NativeObject no1 = new NativeObject();
												no1.setObjectName(objectXMLSchemaElement.getQName());
												ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
														objectXMLSchemaElement, null, no1, theDefinition, service);
												if (unionCO != null) {
													if (objectXMLSchemaElement.getMaxOccurs() > 1) {
														ComplexObject noArrayCO = new ComplexObject();

														noArrayCO.setObjectName(no1.getObjectName());
														noArrayCO.setObjectType(
																new QName(no1.getObjectType().getNamespaceURI(),
																		no1.getObjectType().getLocalPart() + "[]",
																		no1.getObjectType().getPrefix()));
														noArrayCO.setIsArrayType(true);
														noArrayCO.getHasComplexObjects().add(unionCO);
														co.getHasComplexObjects().add(noArrayCO);
														typeParsed = true;

													} else {
														typeParsed = true;
														co.getHasComplexObjects().add(unionCO);
													}
													System.out.println();
												} else {
													typeParsed = true;
													if (no1 != null && no1.getAdditionalInfo() != null
															&& no1.getAdditionalInfo().contains("isListType")) {
														if (objectXMLSchemaElement.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();
															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[][]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);

															ComplexObject noArrayCO_ListNO = new ComplexObject();
															noArrayCO_ListNO.setObjectName(no1.getObjectName());
															noArrayCO_ListNO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO_ListNO.setIsArrayType(true);
															noArrayCO_ListNO.setIsOptional(no1.isIsOptional());
															noArrayCO_ListNO.getHasNativeObjects().add(no1);

															noArrayCO.getHasComplexObjects().add(noArrayCO_ListNO);
															co.getHasComplexObjects().add(noArrayCO);
															typeParsed = true;

														} else {
															typeParsed = true;
															ComplexObject noArrayCO = new ComplexObject();
															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);
															noArrayCO.getHasNativeObjects().add(no1);
															noArrayCO.setIsOptional(no1.isIsOptional());
															co.getHasComplexObjects().add(noArrayCO);
														}
													} else {
														if (objectXMLSchemaElement.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();

															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);
															noArrayCO.getHasNativeObjects().add(no1);
															co.getHasComplexObjects().add(noArrayCO);
															typeParsed = true;

														} else {
															typeParsed = true;
															co.getHasNativeObjects().add(no1);
														}
													}
												}
											} else if (objectXMLSchemaElement.getSchemaType().getClass().toString()
													.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {

												ComplexObject co1 = new ComplexObject();
												parseComplexType(service, objectXMLSchemaElement, null, co1,
														theDefinition, calledFromAbstractTypeParser);
												typeParsed = true;
												co.getHasComplexObjects().add(co1);
											} else {
												System.out.println();
											}

											if (!typeParsed) {
												theDefinition.getContainingErrors()
														.add("ERROR 1!! ERROR @ line ~523!!!");
												System.out.println("ERROR 1!! ERROR @ line ~523!!!");
												// -System.exit(-1);
											}
										} else {
											System.out.println(objectXMLSchemaElement.getName());
											if (objectXMLSchemaElement.getSchemaTypeName() != null) {
												org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType1 = null;
												xmlSchemaType1 = ParsingUtils
														.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
																objectXMLSchemaElement.getSchemaTypeName());

												if (xmlSchemaType1 == null) {
													xmlSchemaType1 = ParsingUtils
															.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
																	service,
																	objectXMLSchemaElement.getSchemaTypeName());
												}

												if (xmlSchemaType1 == null) {
													// failedDueToAxisCreation=true;
													// return null;
													System.out.println(
															"ERROR @line ~524... Rpc/Encoded strange IMPORT!!!");
													continue;
												} else {
													if (xmlSchemaType1.getClass().getName().contains(
															"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
														XmlSchemaSimpleTypeContent content1 = ((XmlSchemaSimpleType) xmlSchemaType1)
																.getContent();
														if (content1.getClass().getName().contains(
																"org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction")) {
															System.out.println("123");
															org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction restr = (org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction) content1;
															XmlSchemaObjectCollection facetsCol = (XmlSchemaObjectCollection) restr
																	.getFacets();
															NativeObject no = new NativeObject();
															no.setObjectName(objectXMLSchemaElement.getQName());
															no.setObjectType(restr.getBaseTypeName());
															// no.setNamespaceURI(restr.getBaseTypeName().getNamespaceURI());
															// no.setAdditionalInfo(restr.getBaseTypeName().getNamespaceURI()+"
															// ");

															if (facetsCol != null && facetsCol.getCount() > 0) {
																Iterator iter1 = facetsCol.getIterator();
																while (iter1.hasNext()) {
																	org.apache.ws.commons.schema.XmlSchemaEnumerationFacet facet = (org.apache.ws.commons.schema.XmlSchemaEnumerationFacet) iter1
																			.next();
																	no.setAdditionalInfo(no.getAdditionalInfo()
																			+ facet.getValue() + "   ");
																	no.getHasAllowedValues().add(facet.getValue());
																}
															}

															co.getHasNativeObjects().add(no);
														} else {
														}
														// System.out.println(xmlSchemaType1.getBaseSchemaType().getClass().getName());
													} else if (xmlSchemaType1.getClass().getName().contains(
															"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
														System.out.println();

													}
												}
											}
										}
									} else if (obj1.getClass().getName().contains("XmlSchemaAny")) {
										System.out.println();
										// EINAI XmlSchemaAny
										org.apache.ws.commons.schema.XmlSchemaAny newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaAny) obj1;

										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(new QName("any"));
										co1.setObjectType(new QName("Object"));

										if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
											co1.setIsOptional(true);
										}

										if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
											// Array Type
											ComplexObject arrayCO = new ComplexObject();
											arrayCO.setObjectName(co1.getObjectName());
											// arrayCO.setObjectType(new
											// QName(co1.getObjectType().getLocalPart()+"[]"));
											arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
													co1.getObjectType().getLocalPart() + "[]",
													co1.getObjectType().getPrefix()));
											arrayCO.setIsArrayType(true);
											arrayCO.getHasComplexObjects().add(co1);
											arrayCO.setIsOptional(co1.isIsOptional());
											co.getHasComplexObjects().add(arrayCO);
										} else {
											co.getHasComplexObjects().add(co1);
										}

										// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
										// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
										System.out.println("aaa!");
									} else if (obj1.getClass().getName().contains("XmlSchemaChoice")) {

										org.apache.ws.commons.schema.XmlSchemaChoice newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaChoice) obj1;

										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(
												new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
										co1.setObjectType(
												new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
										co1.setIsAbstract(true);

										AdditionalTypesParser.parseXMLSchemaChoiceElement(service,
												newSimpleOrComplexObjectElement, co1, theDefinition,
												calledFromAbstractTypeParser);

										if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
											co1.setIsOptional(true);
										}

										if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
											// Array Type
											ComplexObject arrayCO = new ComplexObject();
											arrayCO.setObjectName(co1.getObjectName());
											// arrayCO.setObjectType(new
											// QName(co1.getObjectType()+"[]"));
											arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
													co1.getObjectType().getLocalPart() + "[]",
													co1.getObjectType().getPrefix()));
											arrayCO.setIsArrayType(true);
											arrayCO.getHasComplexObjects().add(co1);
											arrayCO.setIsOptional(co1.isIsOptional());
											co.getHasComplexObjects().add(arrayCO);
										} else {
											co.getHasComplexObjects().add(co1);
										}

										// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
										// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
										System.out.println("aaa!");

									} else if (obj1.getClass().getName().contains("XmlSchemaGroupRef")) {
										System.out.println();
										ComplexObject co1 = new ComplexObject();
										AdditionalTypesParser.parseXmlSchemaGroupRefElement(service,
												(org.apache.ws.commons.schema.XmlSchemaGroupRef) obj1, co1,
												theDefinition);

										if (co1 != null) {
											for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
												co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
											}
											for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
												co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
											}
										} else {
											System.out.println();
										}
										System.out.println();
									} else {
										System.out.println();
									}
								}
							}

						} else {
							System.out.println();
						}

						System.out.println();
					} catch (Exception e1) {
						e1.printStackTrace();
						System.out.println("w! @line ~1303");
					}

				}
			}
		}

		// EDW GET ATTRIBUTES DEFINED mesa sto complexType (sto ct)
		// Parse Attributes...
		if (ct != null) {
			XmlSchemaObjectCollection attsCol = ct.getAttributes();
			if (attsCol != null) {
				Iterator iter2 = attsCol.getIterator();
				while (iter2.hasNext()) {
					Object obj = iter2.next();
					if (obj.getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
						org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;
						Object res1 = AdditionalTypesParser.parseXmlSchemaAttribute(att, service, theDefinition);
						if (res1 != null) {
							if (res1.getClass().getName().contains("NativeObject")) {
								NativeObject no12 = (NativeObject) res1;
								// System.out.println(no12.objectName);
								co.getHasNativeObjects().add(no12);
							} else if (res1.getClass().getName().contains("ComplexObject")) {
								ComplexObject co12 = (ComplexObject) res1;
								// System.out.println(co12.objectName);
								co.getHasComplexObjects().add(co12);
							}
						}
					} else if (obj.getClass().getName()
							.contains("org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")) {
						System.out.println();
						ComplexObject co1 = new ComplexObject();
						AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(service,
								(org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef) obj, co1, theDefinition);

						if (co1 != null) {
							for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
								co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
							}
							for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
								co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
							}
						} else {
							System.out.println();
						}
						System.out.println();
					} else {
						System.out.println();
					}
				}
			}
		}

		ParsingUtils.checkIfCOisAnyObjectType(co);

		if (co.getObjectType() == null) {
			System.out.println();
		}

		if (parsedComplexObjectsHashmap.containsKey(complexTypeName)) {
			parsedComplexObjectsHashmap.put(complexTypeName, co);

			System.out.println();
		}

	}

	public static void parseComplexContent(AxisService service, XmlSchemaComplexContent complexContent,
			ComplexObject co, ParsedWSDLDefinition theDefinition, boolean calledFromAbstractTypeParser,
			QName typeName) {
		if (complexContent == null) {
			return;
		}
		// -System.out.prinln("parsing COMPLEX CONTENT...");
		// Try to parse extension...

		try {
			if (complexContent.getContent() != null && complexContent.getContent().getClass().toString()
					.contains("org.apache.ws.commons.schema.XmlSchemaComplexContentExtension")) {
				XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) complexContent
						.getContent();

				// -System.out.prinln(extension.getBaseTypeName());
				// Parse to baseTypeName
				ComplexObject baseCO = new ComplexObject();
				baseCO.setObjectName(new QName("baseCO"));

				org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType = null;
				XmlSchemaElement el1 = service.getSchemaElement(extension.getBaseTypeName());

				if (el1 != null) {
					xmlSchemaType = el1.getSchemaType();

				} else {

					xmlSchemaType = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
							extension.getBaseTypeName());
					if (xmlSchemaType == null) {
						xmlSchemaType = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service,
								extension.getBaseTypeName());
					}
				}

				if (xmlSchemaType != null) {
					parseComplexType(service, null, xmlSchemaType, baseCO, theDefinition, calledFromAbstractTypeParser);
					for (int i = 0; i < baseCO.getHasNativeObjects().size(); i++) {
						co.getHasNativeObjects().add(baseCO.getHasNativeObjects().get(i));
					}
					for (int i = 0; i < baseCO.getHasComplexObjects().size(); i++) {
						co.getHasComplexObjects().add(baseCO.getHasComplexObjects().get(i));
					}
				} else {
					// EDW MPAiNEI AN DEN VREI TO BASETYPE STA ELEMENTS KAI
					// SCHEMATYPES
					Object res123 = ParsingUtils.tryToFindAndParseAttributeForSpecificObject(theDefinition, service,
							extension.getBaseTypeName());
					// to res123 se ayti tin periptwi einai to baseCO

					if (res123 != null) {
						if (res123.getClass().getName().contains("NativeObject")) {
							// CHECK THIS!!!!
							// An mpei edw prepi na ELEGKSW AN EINAI SWSTO POU
							// MPAINEI ETSI TO res123 mesa sto co
							co.getHasNativeObjects().add(res123);

						} else if (res123.getClass().getName().contains("ComplexObject")) {
							baseCO = (ComplexObject) res123;
							for (int i = 0; i < baseCO.getHasNativeObjects().size(); i++) {
								co.getHasNativeObjects().add(baseCO.getHasNativeObjects().get(i));
							}
							for (int i = 0; i < baseCO.getHasComplexObjects().size(); i++) {
								co.getHasComplexObjects().add(baseCO.getHasComplexObjects().get(i));
							}
						}
					} else {
						System.out.println(
								"ERROR!!! De vrethike to baseType OUTE SAN Attribute!!! @line ~1432 @ComplexTypesParser");
					}
				}

				// Parse ta ypoloipa objects tou co
				if (extension.getParticle() != null
						&& extension.getParticle().getClass().getName().contains("XmlSchemaChoice")) {
					System.out.println();
					org.apache.ws.commons.schema.XmlSchemaChoice ctChoice = (XmlSchemaChoice) extension.getParticle();

					ComplexObject co1 = new ComplexObject();
					co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
					co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
					co1.setIsAbstract(true);

					AdditionalTypesParser.parseXMLSchemaChoiceElement(service, ctChoice, co1, theDefinition,
							calledFromAbstractTypeParser);

					if (ctChoice.getMinOccurs() == 0) {
						co1.setIsOptional(true);
					}

					if (ctChoice.getMaxOccurs() > 1) {
						// Array Type
						ComplexObject arrayCO = new ComplexObject();
						arrayCO.setObjectName(co1.getObjectName());
						// arrayCO.setObjectType(new
						// QName(co1.getObjectType()+"[]"));
						arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
								co1.getObjectType().getLocalPart() + "[]", co1.getObjectType().getPrefix()));
						arrayCO.setIsArrayType(true);
						arrayCO.getHasComplexObjects().add(co1);
						arrayCO.setIsOptional(co1.isIsOptional());
						co.getHasComplexObjects().add(arrayCO);
					} else {
						co.getHasComplexObjects().add(co1);
					}

					// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
					// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
					System.out.println("aaa!");

				} else {
					org.apache.ws.commons.schema.XmlSchemaSequence ctSequence = (XmlSchemaSequence) extension
							.getParticle();
					// -System.out.prinln("AAA");

					if (co.getObjectType() == null && co.getObjectName() != null) {
						co.setObjectType(co.getObjectName());
					}

					if (ctSequence != null) {// Change made on 25-1-2010
						Iterator containedObjectsIter = ctSequence.getItems().getIterator();
						while (containedObjectsIter.hasNext()) {
							Object obj1 = containedObjectsIter.next();
							if (obj1.getClass().getName().contains("XmlSchemaElement")) {
								org.apache.ws.commons.schema.XmlSchemaElement objectXMLSchemaElement = (XmlSchemaElement) obj1;

								if (objectXMLSchemaElement.getSchemaType() != null) {
									boolean typeParsed = false;
									if (objectXMLSchemaElement.getSchemaType().getClass().toString()
											.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
										NativeObject no1 = new NativeObject();
										no1.setObjectName(objectXMLSchemaElement.getQName());
										ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
												objectXMLSchemaElement, null, no1, theDefinition, service);
										if (unionCO != null) {
											if (objectXMLSchemaElement.getMaxOccurs() > 1) {
												ComplexObject noArrayCO = new ComplexObject();

												noArrayCO.setObjectName(no1.getObjectName());
												noArrayCO.setObjectType(new QName(no1.getObjectType().getNamespaceURI(),
														no1.getObjectType().getLocalPart() + "[]",
														no1.getObjectType().getPrefix()));
												noArrayCO.setIsArrayType(true);
												noArrayCO.getHasComplexObjects().add(unionCO);
												co.getHasComplexObjects().add(noArrayCO);
												typeParsed = true;

											} else {
												typeParsed = true;
												co.getHasComplexObjects().add(unionCO);
											}
											System.out.println();
										} else {
											typeParsed = true;
											if (no1 != null && no1.getAdditionalInfo() != null
													&& no1.getAdditionalInfo().contains("isListType")) {
												if (objectXMLSchemaElement.getMaxOccurs() > 1) {
													ComplexObject noArrayCO = new ComplexObject();
													noArrayCO.setObjectName(no1.getObjectName());
													noArrayCO.setObjectType(
															new QName(no1.getObjectType().getNamespaceURI(),
																	no1.getObjectType().getLocalPart() + "[][]",
																	no1.getObjectType().getPrefix()));
													noArrayCO.setIsArrayType(true);

													ComplexObject noArrayCO_ListNO = new ComplexObject();
													noArrayCO_ListNO.setObjectName(no1.getObjectName());
													noArrayCO_ListNO.setObjectType(
															new QName(no1.getObjectType().getNamespaceURI(),
																	no1.getObjectType().getLocalPart() + "[]",
																	no1.getObjectType().getPrefix()));
													noArrayCO_ListNO.setIsArrayType(true);
													noArrayCO_ListNO.setIsOptional(no1.isIsOptional());
													noArrayCO_ListNO.getHasNativeObjects().add(no1);

													noArrayCO.getHasComplexObjects().add(noArrayCO_ListNO);
													co.getHasComplexObjects().add(noArrayCO);
													typeParsed = true;

												} else {
													typeParsed = true;
													ComplexObject noArrayCO = new ComplexObject();
													noArrayCO.setObjectName(no1.getObjectName());
													noArrayCO.setObjectType(
															new QName(no1.getObjectType().getNamespaceURI(),
																	no1.getObjectType().getLocalPart() + "[]",
																	no1.getObjectType().getPrefix()));
													noArrayCO.setIsArrayType(true);
													noArrayCO.getHasNativeObjects().add(no1);
													noArrayCO.setIsOptional(no1.isIsOptional());
													co.getHasComplexObjects().add(noArrayCO);
												}
											} else {
												if (objectXMLSchemaElement.getMaxOccurs() > 1) {
													ComplexObject noArrayCO = new ComplexObject();

													noArrayCO.setObjectName(no1.getObjectName());
													noArrayCO.setObjectType(
															new QName(no1.getObjectType().getNamespaceURI(),
																	no1.getObjectType().getLocalPart() + "[]",
																	no1.getObjectType().getPrefix()));
													noArrayCO.setIsArrayType(true);
													noArrayCO.getHasNativeObjects().add(no1);
													co.getHasComplexObjects().add(noArrayCO);
													typeParsed = true;

												} else {
													typeParsed = true;
													co.getHasNativeObjects().add(no1);
												}
											}
										}
									} else if (objectXMLSchemaElement.getSchemaType().getClass().toString()
											.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {

										ComplexObject co1 = new ComplexObject();
										parseComplexType(service, objectXMLSchemaElement, null, co1, theDefinition,
												calledFromAbstractTypeParser);

										if (co1.getHasNativeObjects().size() != 0
												&& ((NativeObject) co1.getHasNativeObjects().get(0)).getObjectName()
														.getLocalPart().equals("value")) {
											NativeObject no = new NativeObject();
											no.setObjectName(co1.getObjectName());
											no.setObjectType(
													((NativeObject) co1.getHasNativeObjects().get(0)).getObjectType());
											no.setIsInput(co1.isIsInput());
											no.setIsOptional(co1.isIsOptional());
											co.getHasNativeObjects().add(no);

										} else {

											typeParsed = true;
											co.getHasComplexObjects().add(co1);
										}
									} else {
										System.out.println();
									}

									if (!typeParsed) {
										theDefinition.getContainingErrors().add("ERROR 1!! ERROR @ line ~523!!!");
										System.out.println("ERROR 1!! ERROR @ line ~523!!!");
										// -System.exit(-1);
									}
								} else {
									System.out.println(objectXMLSchemaElement.getName());
									if (objectXMLSchemaElement.getSchemaTypeName() != null
											|| objectXMLSchemaElement.getRefName() != null) {
										org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType1 = null;
										xmlSchemaType1 = ParsingUtils
												.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
														objectXMLSchemaElement.getSchemaTypeName());

										if (xmlSchemaType1 == null) {
											xmlSchemaType1 = ParsingUtils
													.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service,
															objectXMLSchemaElement.getRefName());
										}

										if (xmlSchemaType1 == null) {
											xmlSchemaType1 = ParsingUtils
													.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
															objectXMLSchemaElement.getRefName());

											if (xmlSchemaType1 == null) {
												xmlSchemaType1 = ParsingUtils
														.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
																service, objectXMLSchemaElement.getRefName());
											}
										}

										if (xmlSchemaType1 == null) {
											// failedDueToAxisCreation=true;
											// return null;
											System.out.println("ERROR @line ~524... Rpc/Encoded strange IMPORT!!!");
											continue;
										} else {
											if (xmlSchemaType1.getClass().getName()
													.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
												XmlSchemaSimpleTypeContent content1 = ((XmlSchemaSimpleType) xmlSchemaType1)
														.getContent();
												if (content1 != null) {
													if (content1.getClass().getName().contains(
															"org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction")) {
														System.out.println("123");
														org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction restr = (org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction) content1;
														XmlSchemaObjectCollection facetsCol = (XmlSchemaObjectCollection) restr
																.getFacets();
														NativeObject no = new NativeObject();
														no.setObjectName(objectXMLSchemaElement.getQName());
														no.setObjectType(restr.getBaseTypeName());
														// no.setNamespaceURI(restr.getBaseTypeName().getNamespaceURI());
														// no.setAdditionalInfo(restr.getBaseTypeName().getNamespaceURI()+"
														// ");

														if (facetsCol != null && facetsCol.getCount() > 0) {
															Iterator iter1 = facetsCol.getIterator();
															while (iter1.hasNext()) {
																org.apache.ws.commons.schema.XmlSchemaEnumerationFacet facet = (org.apache.ws.commons.schema.XmlSchemaEnumerationFacet) iter1
																		.next();
																no.setAdditionalInfo(no.getAdditionalInfo()
																		+ facet.getValue() + "   ");
																no.getHasAllowedValues().add(facet.getValue());
															}
														}

														co.getHasNativeObjects().add(no);
													} else {
														System.out.println();
													}
												} else {
													NativeObject no1 = new NativeObject();
													if (objectXMLSchemaElement.getQName() != null) {
														no1.setObjectName(objectXMLSchemaElement.getQName());
													} else if (objectXMLSchemaElement.getRefName() != null) {
														no1.setObjectName(objectXMLSchemaElement.getRefName());
													} else if (objectXMLSchemaElement.getName() != null) {
														no1.setObjectName(new QName(objectXMLSchemaElement.getName()));
													} else {
														System.out.println();
													}

													ComplexObject unionCO = SimpleTypesParser.parseSimpleType(null,
															xmlSchemaType1, no1, theDefinition, service);
													if (unionCO != null) {
														if (objectXMLSchemaElement.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();

															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);
															noArrayCO.getHasComplexObjects().add(unionCO);
															co.getHasComplexObjects().add(noArrayCO);
														} else {
															co.getHasComplexObjects().add(unionCO);
														}
														System.out.println();
													} else {
														if (no1 != null && no1.getAdditionalInfo() != null
																&& no1.getAdditionalInfo().contains("isListType")) {
															if (objectXMLSchemaElement.getMaxOccurs() > 1) {
																ComplexObject noArrayCO = new ComplexObject();
																noArrayCO.setObjectName(no1.getObjectName());
																noArrayCO.setObjectType(
																		new QName(no1.getObjectType().getNamespaceURI(),
																				no1.getObjectType().getLocalPart()
																						+ "[][]",
																				no1.getObjectType().getPrefix()));
																noArrayCO.setIsArrayType(true);

																ComplexObject noArrayCO_ListNO = new ComplexObject();
																noArrayCO_ListNO.setObjectName(no1.getObjectName());
																noArrayCO_ListNO.setObjectType(
																		new QName(no1.getObjectType().getNamespaceURI(),
																				no1.getObjectType().getLocalPart()
																						+ "[]",
																				no1.getObjectType().getPrefix()));
																noArrayCO_ListNO.setIsArrayType(true);
																noArrayCO_ListNO.setIsOptional(no1.isIsOptional());
																noArrayCO_ListNO.getHasNativeObjects().add(no1);

																noArrayCO.getHasComplexObjects().add(noArrayCO_ListNO);
																co.getHasComplexObjects().add(noArrayCO);
															} else {
																ComplexObject noArrayCO = new ComplexObject();
																noArrayCO.setObjectName(no1.getObjectName());
																noArrayCO.setObjectType(
																		new QName(no1.getObjectType().getNamespaceURI(),
																				no1.getObjectType().getLocalPart()
																						+ "[]",
																				no1.getObjectType().getPrefix()));
																noArrayCO.setIsArrayType(true);
																noArrayCO.getHasNativeObjects().add(no1);
																noArrayCO.setIsOptional(no1.isIsOptional());
																co.getHasComplexObjects().add(noArrayCO);
															}
														} else {
															if (objectXMLSchemaElement.getMaxOccurs() > 1) {
																ComplexObject noArrayCO = new ComplexObject();

																noArrayCO.setObjectName(no1.getObjectName());
																noArrayCO.setObjectType(
																		new QName(no1.getObjectType().getNamespaceURI(),
																				no1.getObjectType().getLocalPart()
																						+ "[]",
																				no1.getObjectType().getPrefix()));
																noArrayCO.setIsArrayType(true);
																noArrayCO.getHasNativeObjects().add(no1);
																co.getHasComplexObjects().add(noArrayCO);
															} else {
																co.getHasNativeObjects().add(no1);
															}
														}
													}
												}
											} else if (xmlSchemaType1.getClass().getName()
													.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {
												ComplexObject co1 = new ComplexObject();
												if (objectXMLSchemaElement.getQName() != null) {
													co1.setObjectName(objectXMLSchemaElement.getQName());
												} else if (objectXMLSchemaElement.getRefName() != null) {
													co1.setObjectName(objectXMLSchemaElement.getRefName());
												} else if (objectXMLSchemaElement.getName() != null) {
													co1.setObjectName(new QName(objectXMLSchemaElement.getName()));
												} else {
													System.out.println();
												}

												co1.setObjectType(xmlSchemaType1.getQName());

												parseComplexType(service, null, xmlSchemaType1, co1, theDefinition,
														calledFromAbstractTypeParser);
												co.getHasComplexObjects().add(co1);
												System.out.println();
											} else {
												System.out.println();
											}
										}
									} else {
										System.out.println();
									}

								}
							} else if (obj1.getClass().getName().contains("XmlSchemaAny")) {
								// EINAI XmlSchemaAny
								org.apache.ws.commons.schema.XmlSchemaAny newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaAny) obj1;

								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(new QName("any"));
								co1.setObjectType(new QName("Object"));

								if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
									co1.setIsOptional(true);
								}

								if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
									// Array Type
									ComplexObject arrayCO = new ComplexObject();
									arrayCO.setObjectName(co1.getObjectName());
									// arrayCO.setObjectType(new
									// QName(co1.getObjectType().getLocalPart()+"[]"));
									arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
											co1.getObjectType().getLocalPart() + "[]",
											co1.getObjectType().getPrefix()));
									arrayCO.setIsArrayType(true);
									arrayCO.getHasComplexObjects().add(co1);
									arrayCO.setIsOptional(co1.isIsOptional());
									co.getHasComplexObjects().add(arrayCO);
								} else {
									co.getHasComplexObjects().add(co1);
								}

								// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
								// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
								System.out.println("aaa!");
							} else if (obj1.getClass().getName().contains("XmlSchemaChoice")) {

								org.apache.ws.commons.schema.XmlSchemaChoice newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaChoice) obj1;

								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
								co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
								co1.setIsAbstract(true);

								AdditionalTypesParser.parseXMLSchemaChoiceElement(service,
										newSimpleOrComplexObjectElement, co1, theDefinition,
										calledFromAbstractTypeParser);

								if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
									co1.setIsOptional(true);
								}

								if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
									// Array Type
									ComplexObject arrayCO = new ComplexObject();
									arrayCO.setObjectName(co1.getObjectName());
									// arrayCO.setObjectType(new
									// QName(co1.getObjectType()+"[]"));
									arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
											co1.getObjectType().getLocalPart() + "[]",
											co1.getObjectType().getPrefix()));
									arrayCO.setIsArrayType(true);
									arrayCO.getHasComplexObjects().add(co1);
									arrayCO.setIsOptional(co1.isIsOptional());
									co.getHasComplexObjects().add(arrayCO);
								} else {
									co.getHasComplexObjects().add(co1);
								}

								// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
								// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
								System.out.println("aaa!");

							} else if (obj1.getClass().getName().contains("XmlSchemaGroupRef")) {
								System.out.println();
								ComplexObject co1 = new ComplexObject();
								AdditionalTypesParser.parseXmlSchemaGroupRefElement(service,
										(org.apache.ws.commons.schema.XmlSchemaGroupRef) obj1, co1, theDefinition);

								if (co1 != null) {
									for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
										co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
									}
									for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
										co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
									}
								} else {
									System.out.println();
								}
								System.out.println();
							} else {
								System.out.println();
							}
						}
					}

					// parse Attributes HERE
					// CODE ADDED on 25-1-2010
					// EDW GET ATTRIBUTES DEFINED mesa sto complexContent (sto
					// objectXMLSchemaElement)
					// Parse Attributes...????????????
					if (extension != null) {
						XmlSchemaObjectCollection attsCol = extension.getAttributes();// =objectXMLSchemaElement.getAttributes();
						if (attsCol != null) {
							Iterator iter2 = attsCol.getIterator();
							while (iter2.hasNext()) {
								Object obj = iter2.next();
								if (obj.getClass().getName()
										.equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
									org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;
									Object res1 = AdditionalTypesParser.parseXmlSchemaAttribute(att, service,
											theDefinition);
									if (res1 != null) {
										if (res1.getClass().getName().contains("NativeObject")) {
											NativeObject no12 = (NativeObject) res1;
											// System.out.println(no12.objectName);
											co.getHasNativeObjects().add(no12);
										} else if (res1.getClass().getName().contains("ComplexObject")) {
											ComplexObject co12 = (ComplexObject) res1;
											// System.out.println(co12.objectName);
											co.getHasComplexObjects().add(co12);
										}
									}
								} else if (obj.getClass().getName()
										.contains("org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")) {
									System.out.println();
									ComplexObject co1 = new ComplexObject();
									AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(service,
											(org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef) obj, co1,
											theDefinition);

									if (co1 != null) {
										for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
											co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
										}
										for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
											co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
										}
									} else {
										System.out.println();
									}
									System.out.println();
								} else {
									System.out.println();
								}
							}
						}
					}
					System.out.println();
				}
			} else if (complexContent.getContent() != null && complexContent.getContent().getClass().toString()
					.contains("org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction")) {
				org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction restriction = (org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction) complexContent
						.getContent();
				if (restriction != null) {
					// Parse ta objects tou co
					if (restriction.getParticle() != null
							&& restriction.getParticle().getClass().getName().contains("XmlSchemaChoice")) {
						System.out.println();
						org.apache.ws.commons.schema.XmlSchemaChoice ctChoice = (XmlSchemaChoice) restriction
								.getParticle();

						ComplexObject co1 = new ComplexObject();
						co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
						co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
						co1.setIsAbstract(true);

						AdditionalTypesParser.parseXMLSchemaChoiceElement(service, ctChoice, co1, theDefinition,
								calledFromAbstractTypeParser);

						if (ctChoice.getMinOccurs() == 0) {
							co1.setIsOptional(true);
						}

						if (ctChoice.getMaxOccurs() > 1) {
							// Array Type
							ComplexObject arrayCO = new ComplexObject();
							arrayCO.setObjectName(co1.getObjectName());
							// arrayCO.setObjectType(new
							// QName(co1.getObjectType()+"[]"));
							arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
									co1.getObjectType().getLocalPart() + "[]", co1.getObjectType().getPrefix()));
							arrayCO.setIsArrayType(true);
							arrayCO.getHasComplexObjects().add(co1);
							arrayCO.setIsOptional(co1.isIsOptional());
							co.getHasComplexObjects().add(arrayCO);
						} else {
							co.getHasComplexObjects().add(co1);
						}

						// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
						// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
						System.out.println("aaa!");

					} else {
						try {
							org.apache.ws.commons.schema.XmlSchemaSequence ctSequence = (XmlSchemaSequence) restriction
									.getParticle();
							// -System.out.prinln("AAA");

							if (co.getObjectType() == null && co.getObjectName() != null) {
								co.setObjectType(co.getObjectName());
							}

							if (ctSequence != null) {// Change made on 25-1-2010
								Iterator containedObjectsIter = ctSequence.getItems().getIterator();
								while (containedObjectsIter.hasNext()) {
									Object obj1 = containedObjectsIter.next();
									if (obj1.getClass().getName().contains("XmlSchemaElement")) {
										org.apache.ws.commons.schema.XmlSchemaElement objectXMLSchemaElement = (XmlSchemaElement) obj1;

										if (objectXMLSchemaElement.getSchemaType() != null) {
											boolean typeParsed = false;
											if (objectXMLSchemaElement.getSchemaType().getClass().toString()
													.contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
												NativeObject no1 = new NativeObject();
												no1.setObjectName(objectXMLSchemaElement.getQName());
												ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
														objectXMLSchemaElement, null, no1, theDefinition, service);
												if (unionCO != null) {
													if (objectXMLSchemaElement.getMaxOccurs() > 1) {
														ComplexObject noArrayCO = new ComplexObject();

														noArrayCO.setObjectName(no1.getObjectName());
														noArrayCO.setObjectType(
																new QName(no1.getObjectType().getNamespaceURI(),
																		no1.getObjectType().getLocalPart() + "[]",
																		no1.getObjectType().getPrefix()));
														noArrayCO.setIsArrayType(true);
														noArrayCO.getHasComplexObjects().add(unionCO);
														co.getHasComplexObjects().add(noArrayCO);
														typeParsed = true;

													} else {
														typeParsed = true;
														co.getHasComplexObjects().add(unionCO);
													}
													System.out.println();
												} else {
													typeParsed = true;
													if (no1 != null && no1.getAdditionalInfo() != null
															&& no1.getAdditionalInfo().contains("isListType")) {
														if (objectXMLSchemaElement.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();
															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[][]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);

															ComplexObject noArrayCO_ListNO = new ComplexObject();
															noArrayCO_ListNO.setObjectName(no1.getObjectName());
															noArrayCO_ListNO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO_ListNO.setIsArrayType(true);
															noArrayCO_ListNO.setIsOptional(no1.isIsOptional());
															noArrayCO_ListNO.getHasNativeObjects().add(no1);

															noArrayCO.getHasComplexObjects().add(noArrayCO_ListNO);
															co.getHasComplexObjects().add(noArrayCO);
															typeParsed = true;

														} else {
															typeParsed = true;
															ComplexObject noArrayCO = new ComplexObject();
															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);
															noArrayCO.getHasNativeObjects().add(no1);
															noArrayCO.setIsOptional(no1.isIsOptional());
															co.getHasComplexObjects().add(noArrayCO);
														}
													} else {
														if (objectXMLSchemaElement.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();

															noArrayCO.setObjectName(no1.getObjectName());
															noArrayCO.setObjectType(
																	new QName(no1.getObjectType().getNamespaceURI(),
																			no1.getObjectType().getLocalPart() + "[]",
																			no1.getObjectType().getPrefix()));
															noArrayCO.setIsArrayType(true);
															noArrayCO.getHasNativeObjects().add(no1);
															co.getHasComplexObjects().add(noArrayCO);
															typeParsed = true;

														} else {
															typeParsed = true;
															co.getHasNativeObjects().add(no1);
														}
													}
												}
											} else if (objectXMLSchemaElement.getSchemaType().getClass().toString()
													.contains("org.apache.ws.commons.schema.XmlSchemaComplexType")) {

												ComplexObject co1 = new ComplexObject();
												parseComplexType(service, objectXMLSchemaElement, null, co1,
														theDefinition, calledFromAbstractTypeParser);
												typeParsed = true;
												co.getHasComplexObjects().add(co1);

											} else {
												System.out.println();
											}

											if (!typeParsed) {
												theDefinition.getContainingErrors()
														.add("ERROR 1!! ERROR @ line ~523!!!");
												System.out.println("ERROR 1!! ERROR @ line ~523!!!");
												// -System.exit(-1);
											}
										} else {
											System.out.println(objectXMLSchemaElement.getName());
											if (objectXMLSchemaElement.getSchemaTypeName() != null) {
												org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType1 = null;
												xmlSchemaType1 = ParsingUtils
														.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
																objectXMLSchemaElement.getSchemaTypeName());

												if (xmlSchemaType1 == null) {
													xmlSchemaType1 = ParsingUtils
															.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
																	service,
																	objectXMLSchemaElement.getSchemaTypeName());
												}

												if (xmlSchemaType1 == null) {
													// failedDueToAxisCreation=true;
													// return null;
													System.out.println(
															"ERROR @line ~524... Rpc/Encoded strange IMPORT!!!");
													continue;
												} else {
													if (xmlSchemaType1.getClass().getName().contains(
															"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
														XmlSchemaSimpleTypeContent content1 = ((XmlSchemaSimpleType) xmlSchemaType1)
																.getContent();
														if (content1.getClass().getName().contains(
																"org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction")) {
															System.out.println("123");
															org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction restr = (org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction) content1;
															XmlSchemaObjectCollection facetsCol = (XmlSchemaObjectCollection) restr
																	.getFacets();
															NativeObject no = new NativeObject();
															no.setObjectName(objectXMLSchemaElement.getQName());
															no.setObjectType(restr.getBaseTypeName());
															// no.setNamespaceURI(restr.getBaseTypeName().getNamespaceURI());
															// no.setAdditionalInfo(restr.getBaseTypeName().getNamespaceURI()+"
															// ");

															if (facetsCol != null && facetsCol.getCount() > 0) {
																Iterator iter1 = facetsCol.getIterator();
																while (iter1.hasNext()) {
																	org.apache.ws.commons.schema.XmlSchemaEnumerationFacet facet = (org.apache.ws.commons.schema.XmlSchemaEnumerationFacet) iter1
																			.next();
																	no.setAdditionalInfo(no.getAdditionalInfo()
																			+ facet.getValue() + "   ");
																	no.getHasAllowedValues().add(facet.getValue());
																}
															}

															co.getHasNativeObjects().add(no);
														} else {
														}
														// System.out.println(xmlSchemaType1.getBaseSchemaType().getClass().getName());
													} else if (xmlSchemaType1.getClass().getName().contains(
															"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
														System.out.println();
													}
												}
											}
										}
									} else if (obj1.getClass().getName().contains("XmlSchemaAny")) {
										System.out.println();
										// EINAI XmlSchemaAny
										org.apache.ws.commons.schema.XmlSchemaAny newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaAny) obj1;

										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(new QName("any"));
										co1.setObjectType(new QName("Object"));

										if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
											co1.setIsOptional(true);
										}

										if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
											// Array Type
											ComplexObject arrayCO = new ComplexObject();
											arrayCO.setObjectName(co1.getObjectName());
											// arrayCO.setObjectType(new
											// QName(co1.getObjectType().getLocalPart()+"[]"));
											arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
													co1.getObjectType().getLocalPart() + "[]",
													co1.getObjectType().getPrefix()));
											arrayCO.setIsArrayType(true);
											arrayCO.getHasComplexObjects().add(co1);
											arrayCO.setIsOptional(co1.isIsOptional());
											co.getHasComplexObjects().add(arrayCO);
										} else {
											co.getHasComplexObjects().add(co1);
										}

										// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
										// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
										System.out.println("aaa!");
									} else if (obj1.getClass().getName().contains("XmlSchemaChoice")) {

										org.apache.ws.commons.schema.XmlSchemaChoice newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaChoice) obj1;

										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(
												new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
										co1.setObjectType(
												new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
										co1.setIsAbstract(true);

										AdditionalTypesParser.parseXMLSchemaChoiceElement(service,
												newSimpleOrComplexObjectElement, co1, theDefinition,
												calledFromAbstractTypeParser);

										if (newSimpleOrComplexObjectElement.getMinOccurs() == 0) {
											co1.setIsOptional(true);
										}

										if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
											// Array Type
											ComplexObject arrayCO = new ComplexObject();
											arrayCO.setObjectName(co1.getObjectName());
											// arrayCO.setObjectType(new
											// QName(co1.getObjectType()+"[]"));
											arrayCO.setObjectType(new QName(co1.getObjectType().getNamespaceURI(),
													co1.getObjectType().getLocalPart() + "[]",
													co1.getObjectType().getPrefix()));
											arrayCO.setIsArrayType(true);
											arrayCO.getHasComplexObjects().add(co1);
											arrayCO.setIsOptional(co1.isIsOptional());
											co.getHasComplexObjects().add(arrayCO);
										} else {
											co.getHasComplexObjects().add(co1);
										}

										// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"
										// "+newSimpleOrComplexObjectElement.getSchemaTypeName());
										System.out.println("aaa!");

									} else if (obj1.getClass().getName().contains("XmlSchemaGroupRef")) {
										System.out.println();
										ComplexObject co1 = new ComplexObject();
										AdditionalTypesParser.parseXmlSchemaGroupRefElement(service,
												(org.apache.ws.commons.schema.XmlSchemaGroupRef) obj1, co1,
												theDefinition);

										if (co1 != null) {
											for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
												co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
											}
											for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
												co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
											}
										} else {
											System.out.println();
										}
										System.out.println();
									} else {
										System.out.println();
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("ERROR @ComplexTypesParser:2224");
						}
					}

					System.out.println();

					XmlSchemaObjectCollection attsCol = restriction.getAttributes();// =objectXMLSchemaElement.getAttributes();
					if (attsCol != null) {
						Iterator iter2 = attsCol.getIterator();
						while (iter2.hasNext()) {
							Object obj = iter2.next();
							if (obj.getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
								org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;
								Object res1 = AdditionalTypesParser.parseXmlSchemaAttribute(att, service,
										theDefinition);
								if (res1 != null) {
									if (res1.getClass().getName().contains("NativeObject")) {
										NativeObject no12 = (NativeObject) res1;
										// System.out.println(no12.objectName);
										co.getHasNativeObjects().add(no12);
									} else if (res1.getClass().getName().contains("ComplexObject")) {
										ComplexObject co12 = (ComplexObject) res1;
										// System.out.println(co12.objectName);
										co.getHasComplexObjects().add(co12);
									}
								}
							} else if (obj.getClass().getName()
									.contains("org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")) {
								System.out.println();
								ComplexObject co1 = new ComplexObject();
								AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(service,
										(org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef) obj, co1,
										theDefinition);

								if (co1 != null) {
									for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
										co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
									}
									for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
										co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
									}
								} else {
									System.out.println();
								}
								System.out.println();
							} else {
								System.out.println();
							}
						}
					}

				} else {
					System.out.println();
				}

				System.out.println();
			} else {
				theDefinition.getContainingErrors().add("ERROR NOT FOUND ComplexContentExtension");
				System.out.println("ERROR NOT FOUND ComplexContentExtension");
			}
			// -System.out.prinln("COMPLEX CONTENT OK");
		} catch (Exception e) {
			theDefinition.getContainingErrors().add(e.toString());
			e.printStackTrace();
			// -System.exit(-1);
		}

		if (co.getObjectType() == null && co.getObjectName() != null) {
			co.setObjectType(co.getObjectName());
		}

	}
}
