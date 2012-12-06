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

import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroup;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupBase;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;

public class AdditionalTypesParser {

	public static ComplexObject parseXmlSchemaGroup_NEW(AxisService service,
			XmlSchemaGroup group, ParsedWSDLDefinition theDefinition) {
		System.out.println();
		ComplexObject co = new ComplexObject();
		co.setObjectName(group.getName());
		co.setObjectType(group.getName());

		XmlSchemaGroupBase baseParticle = group.getParticle();
		XmlSchemaObjectCollection col1 = baseParticle.getItems();
		Iterator iter1 = col1.getIterator();
		while (iter1.hasNext()) {
			Object obj1 = iter1.next();
			if (obj1.getClass().getName().contains("XmlSchemaElement")) {
				parseXmlSchemaElement(service, obj1, co, theDefinition);
			} else if (obj1.getClass().getName().contains("XmlSchemaAttribute")) {
				System.out.println();
			} else {
				System.out.println();
			}
			System.out.println();
		}

		return co;

	}

	public static ComplexObject parseXmlSchemaAttributeGroup_NEW(
			AxisService service, XmlSchemaAttributeGroup group,
			ParsedWSDLDefinition theDefinition) {
		System.out.println();
		ComplexObject co = new ComplexObject();
		co.setObjectName(group.getName());
		co.setObjectType(group.getName());

		XmlSchemaObjectCollection col1 = group.getAttributes();
		Iterator iter1 = col1.getIterator();
		while (iter1.hasNext()) {
			Object obj1 = iter1.next();
			if (obj1.getClass().getName()
					.contains("XmlSchemaAttributeGroupRef")) {
				ComplexObject co1 = new ComplexObject();
				AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(
						service, (XmlSchemaAttributeGroupRef) obj1, co1,
						theDefinition);
				if (co1 != null) {
					for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
						co.getHasComplexObjects().add(
								co1.getHasComplexObjects().get(i));
					}
					for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
						co.getHasNativeObjects().add(
								co1.getHasNativeObjects().get(i));
					}
				} else {
					System.out.println();
				}
				System.out.println();
			} else if (obj1.getClass().getName().contains("XmlSchemaAttribute")) {
				Object nativeOrComplexObject = AdditionalTypesParser
						.parseXmlSchemaAttribute((XmlSchemaAttribute) obj1,
								service, theDefinition);
				if (nativeOrComplexObject.getClass().getName()
						.contains("NativeObject")) {
					co.getHasNativeObjects().add(nativeOrComplexObject);
				} else {
					co.getHasComplexObjects().add(nativeOrComplexObject);
				}
			} else {
				System.out.println();
			}
			System.out.println();
		}

		return co;

	}

	public static void parseXmlSchemaGroupRefElement(AxisService service,
			XmlSchemaGroupRef xsgr, ComplexObject co,
			ParsedWSDLDefinition theDefinition) {

		ComplexObject co1 = ParsingUtils
				.tryToFindAndParseGroupForSpecificObject(theDefinition,
						service, xsgr.getRefName());
		if (co1 != null) {
			for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
				co.getHasComplexObjects()
						.add(co1.getHasComplexObjects().get(i));
			}
			for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
				co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
			}
		}
		System.out.println();

	}

	public static void parseXmlSchemaAttributeGroupRefElement(
			AxisService service, XmlSchemaAttributeGroupRef xsgr,
			ComplexObject co, ParsedWSDLDefinition theDefinition) {

		ComplexObject co1 = ParsingUtils
				.tryToFindAndParseAttributeGroupForSpecificObject(
						theDefinition, service, xsgr.getRefName());
		if (co1 != null) {
			for (int i = 0; i < co1.getHasComplexObjects().size(); i++) {
				co.getHasComplexObjects()
						.add(co1.getHasComplexObjects().get(i));
			}
			for (int i = 0; i < co1.getHasNativeObjects().size(); i++) {
				co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
			}
		}
		System.out.println();

	}

	public static void parseXmlSchemaElement(AxisService service,
			Object newObj1, ComplexObject co, ParsedWSDLDefinition theDefinition) {

		org.apache.ws.commons.schema.XmlSchemaElement newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaElement) newObj1;
		System.out.println("\t\t\t\t\t"
				+ newSimpleOrComplexObjectElement.getName() + "   "
				+ newSimpleOrComplexObjectElement.getSchemaTypeName());

		if (newSimpleOrComplexObjectElement.getSchemaType() != null) {
			System.out.println("\t\t\t\t\t\t#"
					+ newSimpleOrComplexObjectElement.getSchemaType()
							.getClass().toString() + "#");

			boolean typeParsed = false;
			if (newSimpleOrComplexObjectElement
					.getSchemaType()
					.getClass()
					.toString()
					.contains(
							"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
				NativeObject no1 = new NativeObject();
				ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
						newSimpleOrComplexObjectElement, null, no1,
						theDefinition, service);
				if (unionCO != null) {
					if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
						ComplexObject noArrayCO = new ComplexObject();

						noArrayCO.setObjectName(no1.getObjectName());
						noArrayCO.setObjectType(new QName(no1.getObjectType()
								.getNamespaceURI(), no1.getObjectType()
								.getLocalPart() + "[]", no1.getObjectType()
								.getPrefix()));
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
					if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
							|| newSimpleOrComplexObjectElement.isNillable()) {
						no1.setIsOptional(true);
					}
					if (no1 != null && no1.getAdditionalInfo() != null
							&& no1.getAdditionalInfo().contains("isListType")) {
						if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
							ComplexObject noArrayCO = new ComplexObject();
							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[][]",
									no1.getObjectType().getPrefix()));
							noArrayCO.setIsArrayType(true);

							ComplexObject noArrayCO_ListNO = new ComplexObject();
							noArrayCO_ListNO.setObjectName(no1.getObjectName());
							noArrayCO_ListNO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
							noArrayCO_ListNO.setIsArrayType(true);
							noArrayCO_ListNO.setIsOptional(no1.isIsOptional());
							noArrayCO_ListNO.getHasNativeObjects().add(no1);

							noArrayCO.getHasComplexObjects().add(
									noArrayCO_ListNO);
							co.getHasComplexObjects().add(noArrayCO);
							typeParsed = true;

						} else {
							typeParsed = true;
							ComplexObject noArrayCO = new ComplexObject();
							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
							noArrayCO.setIsArrayType(true);
							noArrayCO.getHasNativeObjects().add(no1);
							noArrayCO.setIsOptional(no1.isIsOptional());
							co.getHasComplexObjects().add(noArrayCO);
						}
					} else {
						if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
							ComplexObject noArrayCO = new ComplexObject();

							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
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
			} else if (newSimpleOrComplexObjectElement
					.getSchemaType()
					.getClass()
					.toString()
					.contains(
							"org.apache.ws.commons.schema.XmlSchemaComplexType")) {

				if (newSimpleOrComplexObjectElement.getSchemaType() == null
						|| newSimpleOrComplexObjectElement.getSchemaType()
								.getName() == null) {
					System.out.println();
				}
				ComplexObject co1 = new ComplexObject();
				ComplexTypesParser.parseComplexType(service,
						newSimpleOrComplexObjectElement, null, co1,
						theDefinition, false);
				typeParsed = true;
				co.getHasComplexObjects().add(co1);
			}
			if (co.getObjectType() != null
					&& co.getObjectType().getLocalPart()
							.contains("ArrayOfError")) {
				System.out.println();
			}
			if (!typeParsed) {
				System.out.println("ERROR 1!!!!!!!!!!!!!!!!!! @line ~862");
				theDefinition.getContainingErrors().add(
						"ERROR 1!!!!!!!!!!!!!!!!!! @line ~862");
				// System.exit(-1);
			}
		} else {
			XmlSchemaType newSchemaType = null;
			if (newSimpleOrComplexObjectElement.getSchemaTypeName() != null) {
				// try to find schema type
				newSchemaType = ParsingUtils
						.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
								service, newSimpleOrComplexObjectElement
										.getSchemaTypeName());
				if (newSchemaType == null) {
					newSchemaType = ParsingUtils
							.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
									service, newSimpleOrComplexObjectElement
											.getSchemaTypeName());
				}

			}
			if (newSchemaType != null) {
				// GET SCHEMA TYPE NAME if not found, proceed as before...
				if (newSchemaType
						.getClass()
						.toString()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
					NativeObject no1 = new NativeObject();

					if (newSimpleOrComplexObjectElement.getQName() != null) {
						no1.setObjectName(newSimpleOrComplexObjectElement
								.getQName());
					} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
						no1.setObjectName(newSimpleOrComplexObjectElement
								.getRefName());
					} else if (newSimpleOrComplexObjectElement.getName() != null) {
						no1.setObjectName(new QName(
								newSimpleOrComplexObjectElement.getName()));
					} else {
						no1.setObjectName(new QName("UNDEFINED variable name"));
						theDefinition
								.getContainingErrors()
								.add("WARNING @line ~583... UNDEFINED Variable name!!!");
						System.out
								.println("WARNING @line ~583... UNDEFINED Variable name!!!");
					}

					ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
							null, newSchemaType, no1, theDefinition, service);
					if (unionCO == null) {
						if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
								|| newSimpleOrComplexObjectElement.isNillable()) {
							no1.setIsOptional(true);
						}

						if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1
								|| (no1.getAdditionalInfo() != null && no1
										.getAdditionalInfo().contains(
												"isListType"))) {
							ComplexObject noArrayCO = new ComplexObject();

							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
							noArrayCO.setIsArrayType(true);
							noArrayCO.getHasNativeObjects().add(no1);
							noArrayCO.setIsOptional(no1.isIsOptional());
							co.getHasComplexObjects().add(noArrayCO);

						} else {
							co.getHasNativeObjects().add(no1);
						}
					} else {
						ComplexObject co2 = new ComplexObject();
						co2.setObjectName(no1.getObjectName());
						if (newSchemaType.getQName() != null) {
							co2.setObjectType(newSchemaType.getQName());
						} else {
							System.out.println();
						}
						co2.getHasComplexObjects().add(unionCO);
						co.getHasComplexObjects().add(co2);
					}

				} else if (newSchemaType
						.getClass()
						.toString()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaComplexType")) {

					ComplexObject co1 = new ComplexObject();

					if (newSimpleOrComplexObjectElement.getQName() != null) {
						co1.setObjectName(newSimpleOrComplexObjectElement
								.getQName());// Panta prin apo ayto ton tropo
												// klisis prepei na exw dwsei
												// prwta to onoma tou co
					} else if (newSimpleOrComplexObjectElement.getRefName() != null) {
						co1.setObjectName(newSimpleOrComplexObjectElement
								.getRefName());
					} else if (newSimpleOrComplexObjectElement.getName() != null) {
						co1.setObjectName(new QName(
								newSimpleOrComplexObjectElement.getName()));
					} else {
						co1.setObjectName(new QName("UNDEFINED variable name"));
						theDefinition
								.getContainingErrors()
								.add("WARNING @line ~2248... UNDEFINED Variable name!!!");
						System.out
								.println("WARNING @line ~2248... UNDEFINED Variable name!!!");
					}
					// Panta prin apo ayto ton tropo klisis prepei na exw dwsei
					// prwta to onoma tou co
					ComplexTypesParser.parseComplexType(service, null,
							newSchemaType, co1, theDefinition, false);

					if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
							|| newSimpleOrComplexObjectElement.isNillable()) {
						co1.setIsOptional(true);
					}

					if (newSimpleOrComplexObjectElement != null
							&& newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
						ComplexObject coArrayCO = new ComplexObject();
						coArrayCO.setObjectName(co1.getObjectName());
						// co1.setObjectType(new QName("XA!"));
						if (co1.getObjectType() == null) {
							System.out.println();
						}
						coArrayCO.setObjectType(new QName(co1.getObjectType()
								.getNamespaceURI(), co1.getObjectType()
								.getLocalPart() + "[]", co1.getObjectType()
								.getPrefix()));
						coArrayCO.setIsArrayType(true);
						coArrayCO.getHasComplexObjects().add(co1);
						coArrayCO.setIsOptional(co1.isIsOptional());
						co.getHasComplexObjects().add(coArrayCO);
					} else {
						co.getHasComplexObjects().add(co1);
					}

				}
				// }
			} else {
				try {
					if (newSimpleOrComplexObjectElement.getRefName() == null) {
						// if(schemaTypeName==null)-> Vector
						if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
							System.out.println("WWW Vector!!!");
							if (newSimpleOrComplexObjectElement
									.getSchemaTypeName() != null
									&& newSimpleOrComplexObjectElement
											.getSchemaTypeName().getLocalPart()
											.equals("anyType")
									&& newSimpleOrComplexObjectElement
											.getName() != null
									&& newSimpleOrComplexObjectElement
											.getName().equals("item")) {

								System.out
										.println(newSimpleOrComplexObjectElement
												.getName());
								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(newSimpleOrComplexObjectElement
										.getQName());
								co1.setObjectType(newSimpleOrComplexObjectElement
										.getSchemaTypeName());
								co1.setIsArrayType(true);
								// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
								co.getHasComplexObjects().add(co1);
							} else if (newSimpleOrComplexObjectElement
									.getSchemaTypeName() == null
									|| newSimpleOrComplexObjectElement
											.getName() == null) {
								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(newSimpleOrComplexObjectElement
										.getQName());
								co1.setObjectType(newSimpleOrComplexObjectElement
										.getQName());
								co1.setIsArrayType(true);
								// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
								co.getHasComplexObjects().add(co1);
							}
						} else {
							ComplexObject co1 = new ComplexObject();
							co1.setObjectName(newSimpleOrComplexObjectElement
									.getQName());
							co1.setObjectType(newSimpleOrComplexObjectElement
									.getSchemaTypeName());
							// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
							if (newSimpleOrComplexObjectElement.getSchemaType() == null) {
								XmlSchemaType xmlSchemaType = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
												service,
												newSimpleOrComplexObjectElement
														.getSchemaTypeName());
								if (xmlSchemaType == null) {
									xmlSchemaType = ParsingUtils
											.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
													service,
													newSimpleOrComplexObjectElement
															.getSchemaTypeName());
								}
								NativeObject no1 = new NativeObject();
								// no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
								if (newSimpleOrComplexObjectElement.getQName() != null) {
									no1.setObjectName(newSimpleOrComplexObjectElement
											.getQName());
								} else if (newSimpleOrComplexObjectElement
										.getRefName() != null) {
									no1.setObjectName(newSimpleOrComplexObjectElement
											.getRefName());
								} else if (newSimpleOrComplexObjectElement
										.getName() != null) {
									no1.setObjectName(new QName(
											newSimpleOrComplexObjectElement
													.getName()));
								} else {
									no1.setObjectName(new QName(
											"UNDEFINED variable name"));
									theDefinition
											.getContainingErrors()
											.add("WARNING @line ~737... UNDEFINED Variable name!!!");
									System.out
											.println("WARNING @line ~737... UNDEFINED Variable name!!!");
								}

								ComplexObject unionCO = SimpleTypesParser
										.parseSimpleType(null, xmlSchemaType,
												no1, theDefinition, service);
								if (unionCO == null) {
									if (newSimpleOrComplexObjectElement
											.getMaxOccurs() > 1
											|| (no1.getAdditionalInfo() != null && no1
													.getAdditionalInfo()
													.contains("isListType"))) {
										ComplexObject noArrayCO = new ComplexObject();

										noArrayCO.setObjectName(no1
												.getObjectName());
										noArrayCO.setObjectType(new QName(no1
												.getObjectType()
												.getNamespaceURI(), no1
												.getObjectType().getLocalPart()
												+ "[]", no1.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);
										noArrayCO.getHasNativeObjects()
												.add(no1);
										co.getHasComplexObjects()
												.add(noArrayCO);

									} else {
										co.getHasNativeObjects().add(no1);
									}
								} else {
									ComplexObject co2 = new ComplexObject();
									co2.setObjectName(no1.getObjectName());
									if (xmlSchemaType.getQName() != null) {
										co2.setObjectType(xmlSchemaType
												.getQName());
									} else {
										System.out.println();
									}
									co2.getHasComplexObjects().add(unionCO);
									co.getHasComplexObjects().add(co2);
								}

							} else {
								ComplexTypesParser.parseComplexType(service,
										newSimpleOrComplexObjectElement, null,
										co1, theDefinition, false);
								theDefinition.getContainingErrors().add(
										"WARNING @ ~777 ...");
								System.out.println("WARNING @ ~777 ...");
								co.getHasComplexObjects().add(co1);
							}
						}
					} else {
						System.out
								.println("DEN HTAN VECTOR... Exei referenced type...? 2");
						System.out.println(newSimpleOrComplexObjectElement
								.getName());
						System.out.println(newSimpleOrComplexObjectElement
								.getRefName());

						try {
							if (newSimpleOrComplexObjectElement.getRefName() != null) {
								org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
												service,
												newSimpleOrComplexObjectElement
														.getRefName());
								if (xmlSchemaType == null) {
									xmlSchemaType = ParsingUtils
											.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
													service,
													newSimpleOrComplexObjectElement
															.getRefName());
								}

								if (xmlSchemaType != null) {

									boolean typeParsed = false;
									if (xmlSchemaType
											.getClass()
											.toString()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
										NativeObject no1 = new NativeObject();

										if (newSimpleOrComplexObjectElement
												.getQName() != null) {
											no1.setObjectName(newSimpleOrComplexObjectElement
													.getQName());
										} else if (newSimpleOrComplexObjectElement
												.getRefName() != null) {
											no1.setObjectName(newSimpleOrComplexObjectElement
													.getRefName());
										} else if (newSimpleOrComplexObjectElement
												.getName() != null) {
											no1.setObjectName(new QName(
													newSimpleOrComplexObjectElement
															.getName()));
										} else {
											no1.setObjectName(new QName(
													"UNDEFINED variable name"));
											theDefinition
													.getContainingErrors()
													.add("WARNING @line ~2248... UNDEFINED Variable name!!!");
											System.out
													.println("WARNING @line ~2248... UNDEFINED Variable name!!!");
										}

										ComplexObject unionCO = SimpleTypesParser
												.parseSimpleType(null,
														xmlSchemaType, no1,
														theDefinition, service);
										if (unionCO == null) {
											if (newSimpleOrComplexObjectElement
													.getMinOccurs() == 0
													|| newSimpleOrComplexObjectElement
															.isNillable()) {
												no1.setIsOptional(true);
											}
											typeParsed = true;
											if (newSimpleOrComplexObjectElement
													.getMaxOccurs() > 1
													|| (no1.getAdditionalInfo() != null && no1
															.getAdditionalInfo()
															.contains(
																	"isListType"))) {
												ComplexObject noArrayCO = new ComplexObject();

												noArrayCO.setObjectName(no1
														.getObjectName());
												noArrayCO
														.setObjectType(new QName(
																no1.getObjectType()
																		.getNamespaceURI(),
																no1.getObjectType()
																		.getLocalPart()
																		+ "[]",
																no1.getObjectType()
																		.getPrefix()));
												noArrayCO.setIsArrayType(true);
												noArrayCO.getHasNativeObjects()
														.add(no1);
												noArrayCO.setIsOptional(no1
														.isIsOptional());
												co.getHasComplexObjects().add(
														noArrayCO);
												typeParsed = true;

											} else {
												typeParsed = true;
												co.getHasNativeObjects().add(
														no1);
											}
										} else {
											ComplexObject co2 = new ComplexObject();
											co2.setObjectName(no1
													.getObjectName());
											if (xmlSchemaType.getQName() != null) {
												co2.setObjectType(xmlSchemaType
														.getQName());
											} else {
												System.out.println();
											}
											co2.getHasComplexObjects().add(
													unionCO);
											co.getHasComplexObjects().add(co2);
										}

									} else if (xmlSchemaType
											.getClass()
											.toString()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaComplexType")) {

										ComplexObject co1 = new ComplexObject();
										if (newSimpleOrComplexObjectElement
												.getQName() != null) {
											co1.setObjectName(newSimpleOrComplexObjectElement
													.getQName());// Panta prin
																	// apo ayto
																	// ton tropo
																	// klisis
																	// prepei na
																	// exw dwsei
																	// prwta to
																	// onoma tou
																	// co
										} else if (newSimpleOrComplexObjectElement
												.getRefName() != null) {
											co1.setObjectName(newSimpleOrComplexObjectElement
													.getRefName());
										} else if (newSimpleOrComplexObjectElement
												.getName() != null) {
											co1.setObjectName(new QName(
													newSimpleOrComplexObjectElement
															.getName()));
										} else {
											co1.setObjectName(new QName(
													"UNDEFINED variable Name"));
											theDefinition
													.getContainingErrors()
													.add("WARNING @line ~2424... UNDEFINED Variable name!!!");
											System.out
													.println("WARNING @line ~2424... UNDEFINED Variable name!!!");
											System.out.println();
										}
										ComplexTypesParser.parseComplexType(
												service, null, xmlSchemaType,
												co1, theDefinition, false);

										if (newSimpleOrComplexObjectElement
												.getMinOccurs() == 0
												|| newSimpleOrComplexObjectElement
														.isNillable()) {
											co1.setIsOptional(true);
										}

										if (newSimpleOrComplexObjectElement != null
												&& newSimpleOrComplexObjectElement
														.getMaxOccurs() > 1) {
											ComplexObject coArrayCO = new ComplexObject();
											coArrayCO.setObjectName(co1
													.getObjectName());
											// co1.setObjectType(new
											// QName("XA!"));
											coArrayCO.setObjectType(new QName(
													co1.getObjectType()
															.getNamespaceURI(),
													co1.getObjectType()
															.getLocalPart()
															+ "[]", co1
															.getObjectType()
															.getPrefix()));
											coArrayCO.setIsArrayType(true);
											coArrayCO.getHasComplexObjects()
													.add(co1);
											coArrayCO.setIsOptional(co1
													.isIsOptional());
											co.getHasComplexObjects().add(
													coArrayCO);

										} else {
											co.getHasComplexObjects().add(co1);
										}
										typeParsed = true;
									}

									if (!typeParsed) {
										theDefinition
												.getContainingErrors()
												.add("ERROR 1!!!!!!!!!!!!!!!!!! ...@line ~2499");
										System.out
												.println("ERROR 1!!!!!!!!!!!!!!!!!! ...@line ~2499");
										// System.exit(-1);
									}
								} else {// if schemaType ==null -> den mporese
										// na vrethei sta elements kai
										// schemaTypes...
									System.out.println("wx aman 21-1-2010");

									Object res123 = ParsingUtils
											.tryToFindAndParseAttributeForSpecificObject(
													theDefinition, service,
													newSimpleOrComplexObjectElement
															.getRefName());
									if (res123 != null) {
										if (res123.getClass().getName()
												.contains("NativeObject")) {
											co.getHasNativeObjects()
													.add(res123);
										} else if (res123.getClass().getName()
												.contains("ComplexObject")) {
											co.getHasComplexObjects().add(
													res123);
										}

									} else {
										System.out.println("XA! W!");
										ComplexObject co123 = new ComplexObject();
										if (newSimpleOrComplexObjectElement
												.getQName() != null) {
											co123.setObjectName(newSimpleOrComplexObjectElement
													.getQName());
										} else if (newSimpleOrComplexObjectElement
												.getRefName() != null) {
											co123.setObjectName(newSimpleOrComplexObjectElement
													.getRefName());
										} else if (newSimpleOrComplexObjectElement
												.getName() != null) {
											co123.setObjectName(new QName(
													newSimpleOrComplexObjectElement
															.getName()));
										} else {
											co123.setObjectName(new QName(
													"UNDEFINED variable name!"));
											theDefinition
													.getContainingErrors()
													.add("ERROR @line ~2527... UNDEFINED Variable name!!!");
											System.out
													.println("ERROR @line ~2527... UNDEFINED Variable name!!!");
										}

										co123.setObjectType(new QName("Object"));
										co.getHasComplexObjects().add(co123);
									}
								}

							}

						} catch (Exception e1) {
							theDefinition.getContainingErrors().add(
									e1.toString());
							e1.printStackTrace();

						}

					}
				} catch (Exception e) {
					theDefinition.getContainingErrors().add(e.toString());
					e.printStackTrace();

					// System.exit(-7);
				}
			}

			// }
			// MITSOS 8-12 END

		}
	}

	public static void parseXmlSchemaAllType(AxisService service,
			XmlSchemaAll xsa, ComplexObject co,
			ParsedWSDLDefinition theDefinition) {
		System.out.println(xsa.getItems().getCount());
		Iterator iter1 = xsa.getItems().getIterator();
		while (iter1.hasNext()) {
			// System.out.println(iter1.next().getClass().getName());

			org.apache.ws.commons.schema.XmlSchemaElement newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaElement) iter1
					.next();
			System.out.println("\t\t\t\t\t"
					+ newSimpleOrComplexObjectElement.getName() + "   "
					+ newSimpleOrComplexObjectElement.getSchemaTypeName());

			if (newSimpleOrComplexObjectElement.getName() != null
					&& newSimpleOrComplexObjectElement.getName().equals(
							"FullName")) {
				System.out.println("OEOEO");
			}

			// IF ARRAY TI KANOUME OEO???????

			if (newSimpleOrComplexObjectElement.getSchemaType() != null) {
				System.out.println("\t\t\t\t\t\t#"
						+ newSimpleOrComplexObjectElement.getSchemaType()
								.getClass().toString() + "#");

				boolean typeParsed = false;
				if (newSimpleOrComplexObjectElement
						.getSchemaType()
						.getClass()
						.toString()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
					NativeObject no1 = new NativeObject();
					no1.setObjectName(newSimpleOrComplexObjectElement
							.getQName());
					ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
							newSimpleOrComplexObjectElement, null, no1,
							theDefinition, service);
					typeParsed = true;
					if (unionCO != null) {
						if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
							ComplexObject noArrayCO = new ComplexObject();

							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
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
						if (no1 != null
								&& no1.getAdditionalInfo() != null
								&& no1.getAdditionalInfo().contains(
										"isListType")) {
							if (newSimpleOrComplexObjectElement.getMaxOccurs() > 1) {
								ComplexObject noArrayCO = new ComplexObject();
								noArrayCO.setObjectName(no1.getObjectName());
								noArrayCO.setObjectType(new QName(no1
										.getObjectType().getNamespaceURI(), no1
										.getObjectType().getLocalPart()
										+ "[][]", no1.getObjectType()
										.getPrefix()));
								noArrayCO.setIsArrayType(true);

								ComplexObject noArrayCO_ListNO = new ComplexObject();
								noArrayCO_ListNO.setObjectName(no1
										.getObjectName());
								noArrayCO_ListNO.setObjectType(new QName(no1
										.getObjectType().getNamespaceURI(), no1
										.getObjectType().getLocalPart() + "[]",
										no1.getObjectType().getPrefix()));
								noArrayCO_ListNO.setIsArrayType(true);
								noArrayCO_ListNO.setIsOptional(no1
										.isIsOptional());
								noArrayCO_ListNO.getHasNativeObjects().add(no1);

								noArrayCO.getHasComplexObjects().add(
										noArrayCO_ListNO);
								co.getHasComplexObjects().add(noArrayCO);
								typeParsed = true;

							} else {
								typeParsed = true;
								ComplexObject noArrayCO = new ComplexObject();
								noArrayCO.setObjectName(no1.getObjectName());
								noArrayCO.setObjectType(new QName(no1
										.getObjectType().getNamespaceURI(), no1
										.getObjectType().getLocalPart() + "[]",
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
								noArrayCO.setObjectType(new QName(no1
										.getObjectType().getNamespaceURI(), no1
										.getObjectType().getLocalPart() + "[]",
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
						/*
						 * if(newSimpleOrComplexObjectElement!=null&&
						 * newSimpleOrComplexObjectElement.getMaxOccurs()>1){
						 * ComplexObject noArrayCO=new ComplexObject();
						 * noArrayCO.setObjectName(no1.getObjectName());
						 * noArrayCO.setObjectType(new
						 * QName(no1.getObjectType().
						 * getNamespaceURI(),no1.getObjectType()+"[]",
						 * no1.getObjectType().getPrefix()));
						 * noArrayCO.setIsArrayType(true);
						 * noArrayCO.getHasNativeObjects().add(no1);
						 * noArrayCO.setIsOptional(no1.isIsOptional());
						 * co.getHasComplexObjects().add(noArrayCO);
						 * 
						 * }else{ co.getHasNativeObjects().add(no1); }
						 */

						// co.getHasNativeObjects().add(no1);
					}
				} else if (newSimpleOrComplexObjectElement
						.getSchemaType()
						.getClass()
						.toString()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
					ComplexObject co1 = new ComplexObject();
					// PARSE XML Schema All Type
					ComplexTypesParser.parseComplexType(service,
							newSimpleOrComplexObjectElement, null, co1,
							theDefinition, false);
					typeParsed = true;
					co.getHasComplexObjects().add(co1);
				}

				if (!typeParsed) {
					theDefinition.getContainingErrors().add(
							"ERROR 1!!!!! @line ~636");
					System.out.println("ERROR 1!!!!! @line ~636");
					System.exit(-1);
				}
			} else {
				// MITSOS 8-12-08 START
				// if(newSimpleOrComplexObjectElement.getSchemaTypeName()!=null&&
				// newSimpleOrComplexObjectElement.getSchemaTypeName().getLocalPart().equals("anyType")){
				try {
					System.out.println("WWW Vector!!!");
					ComplexObject co1 = new ComplexObject();
					co1.setObjectName(newSimpleOrComplexObjectElement
							.getQName());
					co1.setObjectType(newSimpleOrComplexObjectElement
							.getSchemaTypeName());
					co1.setIsArrayType(true);
					// co1.setNamespaceURI(newSimpleOrComplexObjectElement.getSchemaTypeName().getNamespaceURI());
					co.getHasComplexObjects().add(co1);
				} catch (Exception e) {

					// e.printStackTrace();
					System.out
							.println("DEN HTAN VECTOR... Exei referenced type...? 1");
					System.out.println(newSimpleOrComplexObjectElement
							.getName());
					System.out.println(newSimpleOrComplexObjectElement
							.getRefName());

					try {
						if (newSimpleOrComplexObjectElement.getRefName() != null) {
							org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType = ParsingUtils
									.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
											service,
											newSimpleOrComplexObjectElement
													.getRefName());
							if (xmlSchemaType == null) {
								xmlSchemaType = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
												service,
												newSimpleOrComplexObjectElement
														.getRefName());
							}

							if (xmlSchemaType == null) {
								Object res123 = ParsingUtils
										.tryToFindAndParseAttributeForSpecificObject(
												theDefinition, service,
												newSimpleOrComplexObjectElement
														.getRefName());
								if (res123 != null) {
									if (res123.getClass().getName()
											.contains("NativeObject")) {
										co.getHasNativeObjects().add(res123);
										continue;
									} else if (res123.getClass().getName()
											.contains("ComplexObject")) {
										co.getHasComplexObjects().add(res123);
										continue;
									}

								} else {
									System.out.println("XA! W!");
									ComplexObject co123 = new ComplexObject();
									if (newSimpleOrComplexObjectElement
											.getQName() != null) {
										co123.setObjectName(newSimpleOrComplexObjectElement
												.getQName());
									} else if (newSimpleOrComplexObjectElement
											.getRefName() != null) {
										co123.setObjectName(newSimpleOrComplexObjectElement
												.getRefName());
									} else if (newSimpleOrComplexObjectElement
											.getName() != null) {
										co123.setObjectName(new QName(
												newSimpleOrComplexObjectElement
														.getName()));
									} else {
										co123.setObjectName(new QName(
												"UNDEFINED variable name!"));
										theDefinition
												.getContainingErrors()
												.add("ERROR @line ~1695... UNDEFINED Variable name!!!");
										System.out
												.println("ERROR @line ~1695... UNDEFINED Variable name!!!");
									}

									co123.setObjectType(new QName("Object"));
									co.getHasComplexObjects().add(co123);
								}
								continue;
							}

							boolean typeParsed = false;
							if (xmlSchemaType
									.getClass()
									.toString()
									.contains(
											"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
								NativeObject no1 = new NativeObject();
								// no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
								if (newSimpleOrComplexObjectElement.getQName() != null) {
									no1.setObjectName(newSimpleOrComplexObjectElement
											.getQName());
								} else if (newSimpleOrComplexObjectElement
										.getRefName() != null) {
									no1.setObjectName(newSimpleOrComplexObjectElement
											.getRefName());
								} else if (newSimpleOrComplexObjectElement
										.getName() != null) {
									no1.setObjectName(new QName(
											newSimpleOrComplexObjectElement
													.getName()));
								} else {
									no1.setObjectName(new QName(
											"UNDEFINED variable name"));
									theDefinition
											.getContainingErrors()
											.add("WARNING @line ~202... UNDEFINED Variable name!!!");
									System.out
											.println("WARNING @line ~202... UNDEFINED Variable name!!!");
								}

								ComplexObject unionCO = SimpleTypesParser
										.parseSimpleType(null, xmlSchemaType,
												no1, theDefinition, service);
								if (unionCO == null) {
									if (newSimpleOrComplexObjectElement
											.getMinOccurs() == 0
											|| newSimpleOrComplexObjectElement
													.isNillable()) {
										no1.setIsOptional(true);
									}
									typeParsed = true;

									if ((newSimpleOrComplexObjectElement != null && newSimpleOrComplexObjectElement
											.getMaxOccurs() > 1)
											|| (no1.getAdditionalInfo() != null && no1
													.getAdditionalInfo()
													.contains("isListType"))) {
										ComplexObject noArrayCO = new ComplexObject();
										noArrayCO.setObjectName(no1
												.getObjectName());
										noArrayCO.setObjectType(new QName(no1
												.getObjectType()
												.getNamespaceURI(), no1
												.getObjectType() + "[]", no1
												.getObjectType().getPrefix()));
										noArrayCO.setIsArrayType(true);
										noArrayCO.getHasNativeObjects()
												.add(no1);
										noArrayCO.setIsOptional(no1
												.isIsOptional());
										co.getHasComplexObjects()
												.add(noArrayCO);

									} else {
										co.getHasNativeObjects().add(no1);
									}
								} else {
									ComplexObject co2 = new ComplexObject();
									co2.setObjectName(no1.getObjectName());
									if (xmlSchemaType.getQName() != null) {
										co2.setObjectType(xmlSchemaType
												.getQName());
									} else {
										System.out.println();
									}
									co2.getHasComplexObjects().add(unionCO);
									co.getHasComplexObjects().add(co2);
								}

							} else if (xmlSchemaType
									.getClass()
									.toString()
									.contains(
											"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
								ComplexObject co1 = new ComplexObject();

								// Panta prin apo ayto ton tropo klisis prepei
								// na exw dwsei prwta to onoma tou co
								if (newSimpleOrComplexObjectElement.getQName() != null) {
									co1.setObjectName(newSimpleOrComplexObjectElement
											.getQName());// Panta prin apo ayto
															// ton tropo klisis
															// prepei na exw
															// dwsei prwta to
															// onoma tou co
								} else if (newSimpleOrComplexObjectElement
										.getRefName() != null) {
									co1.setObjectName(newSimpleOrComplexObjectElement
											.getRefName());
								} else if (newSimpleOrComplexObjectElement
										.getName() != null) {
									co1.setObjectName(new QName(
											newSimpleOrComplexObjectElement
													.getName()));
								} else {
									co1.setObjectName(new QName(
											"UNDEFINED variable name"));
									theDefinition
											.getContainingErrors()
											.add("WARNING @line ~1678... UNDEFINED Variable name!!!");
									System.out
											.println("WARNING @line ~1678... UNDEFINED Variable name!!!");
								}
								ComplexTypesParser.parseComplexType(service,
										null, xmlSchemaType, co1,
										theDefinition, false);
								if (newSimpleOrComplexObjectElement
										.getMinOccurs() == 0
										|| newSimpleOrComplexObjectElement
												.isNillable()) {
									co1.setIsOptional(true);
								}

								if (newSimpleOrComplexObjectElement != null
										&& newSimpleOrComplexObjectElement
												.getMaxOccurs() > 1) {
									ComplexObject coArrayCO = new ComplexObject();
									coArrayCO
											.setObjectName(co1.getObjectName());
									// co1.setObjectType(new QName("XA!"));
									coArrayCO.setObjectType(new QName(co1
											.getObjectType().getNamespaceURI(),
											co1.getObjectType().getLocalPart()
													+ "[]", co1.getObjectType()
													.getPrefix()));
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
								System.out.println("ERROR 1!!!!! @line ~684");
								theDefinition.getContainingErrors().add(
										"ERROR 1!!!!! @line ~684");
								// System.exit(-1);
							}

						}

					} catch (Exception e1) {
						theDefinition.getContainingErrors().add(e1.toString());
						e1.printStackTrace();

					}
				}
				// }
				// MITSOS 8-12 END

			}
		}

	}

	public static void parseXMLSchemaChoiceElement(AxisService service,
			org.apache.ws.commons.schema.XmlSchemaChoice choiceElement,
			ComplexObject choiceCO, ParsedWSDLDefinition theDefinition,
			boolean calledFromAbstractTypeParser) {
		XmlSchemaObjectCollection col1 = choiceElement.getItems();
		if (col1 != null) {
			Iterator iter1 = col1.getIterator();
			while (iter1.hasNext()) {
				Object newObj2 = iter1.next();
				if (newObj2 != null
						&& newObj2
								.getClass()
								.getName()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaElement")) {

					org.apache.ws.commons.schema.XmlSchemaElement newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaElement) newObj2;
					System.out.println("\t\t\t\t\t"
							+ newSimpleOrComplexObjectElement.getName()
							+ "   "
							+ newSimpleOrComplexObjectElement
									.getSchemaTypeName());

					if (newSimpleOrComplexObjectElement.getSchemaType() != null) {
						System.out.println("\t\t\t\t\t\t#"
								+ newSimpleOrComplexObjectElement
										.getSchemaType().getClass().toString()
								+ "#");

						boolean typeParsed = false;
						if (newSimpleOrComplexObjectElement
								.getSchemaType()
								.getClass()
								.toString()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
							NativeObject no1 = new NativeObject();
							ComplexObject unionCO = SimpleTypesParser
									.parseSimpleType(
											newSimpleOrComplexObjectElement,
											null, no1, theDefinition, service);
							if (newSimpleOrComplexObjectElement.getMinOccurs() == 0
									|| newSimpleOrComplexObjectElement
											.isNillable()) {
								no1.setIsOptional(true);
							}
							if (unionCO != null) {
								if (newSimpleOrComplexObjectElement
										.getMaxOccurs() > 1) {
									ComplexObject noArrayCO = new ComplexObject();

									noArrayCO
											.setObjectName(no1.getObjectName());
									noArrayCO.setObjectType(new QName(no1
											.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart()
													+ "[]", no1.getObjectType()
													.getPrefix()));
									noArrayCO.setIsArrayType(true);
									noArrayCO.getHasComplexObjects().add(
											unionCO);
									choiceCO.getHasExtendedObjects().add(
											noArrayCO);
									typeParsed = true;

								} else {
									typeParsed = true;
									ComplexObject co1 = new ComplexObject();
									co1.setObjectName(no1.getObjectName());
									co1.setObjectType(no1.getObjectType());
									co1.getHasComplexObjects().add(unionCO);
									choiceCO.getHasExtendedObjects().add(co1);
								}
								System.out.println();
							} else {
								if (no1 != null
										&& no1.getAdditionalInfo() != null
										&& no1.getAdditionalInfo().contains(
												"isListType")) {
									if (newSimpleOrComplexObjectElement
											.getMaxOccurs() > 1) {
										ComplexObject noArrayCO = new ComplexObject();
										noArrayCO.setObjectName(no1
												.getObjectName());
										noArrayCO.setObjectType(new QName(no1
												.getObjectType()
												.getNamespaceURI(), no1
												.getObjectType().getLocalPart()
												+ "[][]", no1.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);

										ComplexObject noArrayCO_ListNO = new ComplexObject();
										noArrayCO_ListNO.setObjectName(no1
												.getObjectName());
										noArrayCO_ListNO
												.setObjectType(new QName(no1
														.getObjectType()
														.getNamespaceURI(), no1
														.getObjectType()
														.getLocalPart()
														+ "[]", no1
														.getObjectType()
														.getPrefix()));
										noArrayCO_ListNO.setIsArrayType(true);
										noArrayCO_ListNO.setIsOptional(no1
												.isIsOptional());
										noArrayCO_ListNO.getHasNativeObjects()
												.add(no1);

										noArrayCO.getHasComplexObjects().add(
												noArrayCO_ListNO);
										choiceCO.getHasExtendedObjects().add(
												noArrayCO);
										typeParsed = true;

									} else {
										typeParsed = true;
										ComplexObject noArrayCO = new ComplexObject();
										noArrayCO.setObjectName(no1
												.getObjectName());
										noArrayCO.setObjectType(new QName(no1
												.getObjectType()
												.getNamespaceURI(), no1
												.getObjectType().getLocalPart()
												+ "[]", no1.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);
										noArrayCO.getHasNativeObjects()
												.add(no1);
										noArrayCO.setIsOptional(no1
												.isIsOptional());
										choiceCO.getHasExtendedObjects().add(
												noArrayCO);
									}
								} else {
									if (newSimpleOrComplexObjectElement
											.getMaxOccurs() > 1) {
										ComplexObject noArrayCO = new ComplexObject();

										noArrayCO.setObjectName(no1
												.getObjectName());
										noArrayCO.setObjectType(new QName(no1
												.getObjectType()
												.getNamespaceURI(), no1
												.getObjectType().getLocalPart()
												+ "[]", no1.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);
										noArrayCO.getHasNativeObjects()
												.add(no1);
										choiceCO.getHasExtendedObjects().add(
												noArrayCO);
										typeParsed = true;

									} else {
										typeParsed = true;
										choiceCO.getHasExtendedObjects().add(
												no1);
									}
								}
								/*
								 * if(newSimpleOrComplexObjectElement.getMaxOccurs
								 * ()>1){ ComplexObject noArrayCO=new
								 * ComplexObject();
								 * 
								 * noArrayCO.setObjectName(no1.getObjectName());
								 * noArrayCO.setObjectType(new
								 * QName(no1.getObjectType().getNamespaceURI(),
								 * no1.getObjectType().getLocalPart()+"[]",no1.
								 * getObjectType().getPrefix()));
								 * noArrayCO.setIsArrayType(true);
								 * noArrayCO.getHasNativeObjects().add(no1);
								 * choiceCO
								 * .getHasExtendedObjects().add(noArrayCO);
								 * typeParsed=true;
								 * 
								 * }else{ typeParsed=true;
								 * choiceCO.getHasExtendedObjects().add(no1); }
								 */
							}

						} else if (newSimpleOrComplexObjectElement
								.getSchemaType()
								.getClass()
								.toString()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaComplexType")) {

							ComplexObject co1 = new ComplexObject();
							// edw prepei na mpei elegxos an to
							// newSimpleOrComplexObjectElement->schemaType einai
							// idio me to ct
							// gia na apofygw to StackOverflowError
							ComplexTypesParser
									.parseComplexType(service,
											newSimpleOrComplexObjectElement,
											null, co1, theDefinition,
											calledFromAbstractTypeParser);
							typeParsed = true;
							choiceCO.getHasExtendedObjects().add(co1);

						}

						if (!typeParsed) {
							System.out
									.println("ERROR 1!!!!!!!!!!!!!!!!!! @line ~2110 Axis2");
							theDefinition
									.getContainingErrors()
									.add("ERROR 1!!!!!!!!!!!!!!!!!! @line ~2110 Axis2");
							// System.exit(-1);
						}
					} else {
						System.out.println();
						// parse type me to refName
						XmlSchemaType theSchemaType = null;

						if (newSimpleOrComplexObjectElement.getSchemaType() == null) {
							// parse wsdl to find REQUIRED schemaType
							if (newSimpleOrComplexObjectElement
									.getSchemaTypeName() != null) {
								theSchemaType = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
												service,
												newSimpleOrComplexObjectElement
														.getSchemaTypeName());
							} else if (newSimpleOrComplexObjectElement
									.getRefName() != null) {
								theSchemaType = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
												service,
												newSimpleOrComplexObjectElement
														.getRefName());
								if (theSchemaType == null) {
									theSchemaType = ParsingUtils
											.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
													service,
													newSimpleOrComplexObjectElement
															.getRefName());
								}
							}
							System.out.println();
						} else {
							theSchemaType = newSimpleOrComplexObjectElement
									.getSchemaType();
						}

						if (theSchemaType == null) {
							Object res123 = ParsingUtils
									.tryToFindAndParseAttributeForSpecificObject(
											theDefinition, service,
											newSimpleOrComplexObjectElement
													.getRefName());
							if (res123 != null) {
								if (res123.getClass().getName()
										.contains("NativeObject")) {
									choiceCO.getHasExtendedObjects()
											.add(res123);
									continue;
								} else if (res123.getClass().getName()
										.contains("ComplexObject")) {
									choiceCO.getHasExtendedObjects()
											.add(res123);
									continue;
								}

							} else {
								System.out.println("XA! W!");
								ComplexObject co123 = new ComplexObject();
								if (newSimpleOrComplexObjectElement.getQName() != null) {
									co123.setObjectName(newSimpleOrComplexObjectElement
											.getQName());
								} else if (newSimpleOrComplexObjectElement
										.getRefName() != null) {
									co123.setObjectName(newSimpleOrComplexObjectElement
											.getRefName());
								} else if (newSimpleOrComplexObjectElement
										.getName() != null) {
									co123.setObjectName(new QName(
											newSimpleOrComplexObjectElement
													.getName()));
								} else {
									co123.setObjectName(new QName(
											"UNDEFINED variable name!"));
									theDefinition
											.getContainingErrors()
											.add("ERROR @line ~2837... UNDEFINED Variable name!!!");
									System.out
											.println("ERROR @line ~2837... UNDEFINED Variable name!!!");
								}

								co123.setObjectType(new QName("Object"));
								choiceCO.getHasExtendedObjects().add(co123);
							}
							continue;
						}

						if (theSchemaType
								.getClass()
								.toString()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
							ComplexObject co1 = new ComplexObject();
							if (newSimpleOrComplexObjectElement.getQName() != null) {
								co1.setObjectName(newSimpleOrComplexObjectElement
										.getQName());
							} else if (newSimpleOrComplexObjectElement
									.getRefName() != null) {
								co1.setObjectName(newSimpleOrComplexObjectElement
										.getRefName());
							} else if (newSimpleOrComplexObjectElement
									.getName() != null) {
								co1.setObjectName(new QName(
										newSimpleOrComplexObjectElement
												.getName()));
							} else {
								co1.setObjectName(new QName(
										"UNDEFINED variable name"));
								theDefinition
										.getContainingErrors()
										.add("WARNING @line ~2740... UNDEFINED Variable name!!!");
								System.out
										.println("WARNING @line ~2740... UNDEFINED Variable name!!!");
							}

							ComplexTypesParser.parseComplexType(service, null,
									theSchemaType, co1, theDefinition,
									calledFromAbstractTypeParser);

							if (newSimpleOrComplexObjectElement != null
									&& (newSimpleOrComplexObjectElement
											.getMinOccurs() == 0 || newSimpleOrComplexObjectElement
											.isNillable())) {
								co1.setIsOptional(true);
							}

							if (newSimpleOrComplexObjectElement != null
									&& newSimpleOrComplexObjectElement
											.getMaxOccurs() > 1) {
								ComplexObject coArrayCO = new ComplexObject();
								coArrayCO.setObjectName(co1.getObjectName());
								// co1.setObjectType(new QName("XA!"));
								coArrayCO.setObjectType(new QName(co1
										.getObjectType().getNamespaceURI(), co1
										.getObjectType().getLocalPart() + "[]",
										co1.getObjectType().getPrefix()));
								coArrayCO.setIsArrayType(true);
								coArrayCO.getHasComplexObjects().add(co1);
								coArrayCO.setIsOptional(co1.isIsOptional());
								choiceCO.getHasExtendedObjects().add(coArrayCO);

							} else {
								choiceCO.getHasExtendedObjects().add(co1);
							}

						} else if (theSchemaType
								.getClass()
								.toString()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
							NativeObject no1 = new NativeObject();
							// no1.objectName = "elementOfArray";
							// no1.setNamespaceURI(elem1.getSchemaTypeName().getNamespaceURI());
							no1.setObjectType(newSimpleOrComplexObjectElement
									.getSchemaTypeName());
							// no1.setObjectName(newSimpleOrComplexObjectElement.getQName());
							// no1.additionalInfo =
							// elem1.getSchemaTypeName().getNamespaceURI();
							if (newSimpleOrComplexObjectElement.getQName() != null) {
								no1.setObjectName(newSimpleOrComplexObjectElement
										.getQName());
							} else if (newSimpleOrComplexObjectElement
									.getRefName() != null) {
								no1.setObjectName(newSimpleOrComplexObjectElement
										.getRefName());
							} else if (newSimpleOrComplexObjectElement
									.getName() != null) {
								no1.setObjectName(new QName(
										newSimpleOrComplexObjectElement
												.getName()));
							} else {
								no1.setObjectName(new QName(
										"UNDEFINED variable name"));
								theDefinition
										.getContainingErrors()
										.add("WARNING @line ~495... UNDEFINED Variable name!!!");
								System.out
										.println("WARNING @line ~495... UNDEFINED Variable name!!!");
							}

							ComplexObject unionCO = SimpleTypesParser
									.parseSimpleType(null, theSchemaType, no1,
											theDefinition, service);
							if (unionCO == null) {
								if (newSimpleOrComplexObjectElement
										.getMinOccurs() == 0
										|| newSimpleOrComplexObjectElement
												.isNillable()) {
									no1.setIsOptional(true);
								}

								if ((newSimpleOrComplexObjectElement != null && newSimpleOrComplexObjectElement
										.getMaxOccurs() > 1)
										|| (no1.getAdditionalInfo() != null && no1
												.getAdditionalInfo().contains(
														"isListType"))) {
									ComplexObject noArrayCO = new ComplexObject();
									noArrayCO
											.setObjectName(no1.getObjectName());
									noArrayCO.setObjectType(new QName(no1
											.getObjectType().getNamespaceURI(),
											no1.getObjectType().getLocalPart()
													+ "[]", no1.getObjectType()
													.getPrefix()));
									noArrayCO.setIsArrayType(true);
									noArrayCO.getHasNativeObjects().add(no1);
									noArrayCO.setIsOptional(no1.isIsOptional());
									choiceCO.getHasExtendedObjects().add(
											noArrayCO);
								} else {
									choiceCO.getHasExtendedObjects().add(no1);
								}
							} else {
								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(no1.getObjectName());
								if (theSchemaType.getQName() != null) {
									co1.setObjectType(theSchemaType.getQName());
								} else {
									System.out.println();
								}
								co1.getHasComplexObjects().add(unionCO);
								choiceCO.getHasExtendedObjects().add(co1);
							}

							System.out.println("");
						} else {
							theDefinition.getContainingErrors().add(
									"ERROR @ line ~980!!!");
							System.out.println("ERROR @ line ~980!!!");
						}
					}
				} else if (newObj2
						.getClass()
						.getName()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaSequence")) {
					ComplexObject sequenceCO = new ComplexObject();
					sequenceCO.setObjectName(new QName("allowedCO"));
					sequenceCO.setObjectType(new QName("allowedCO"));

					System.out.println();
					org.apache.ws.commons.schema.XmlSchemaSequence xmlSchemaSequence = (org.apache.ws.commons.schema.XmlSchemaSequence) newObj2;
					Iterator iter11 = xmlSchemaSequence.getItems()
							.getIterator();
					while (iter11.hasNext()) {
						Object newObj21 = iter11.next();
						if (newObj21 != null
								&& newObj21
										.getClass()
										.getName()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaElement")) {

							org.apache.ws.commons.schema.XmlSchemaElement newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaElement) newObj21;
							System.out.println("\t\t\t\t\t"
									+ newSimpleOrComplexObjectElement.getName()
									+ "   "
									+ newSimpleOrComplexObjectElement
											.getSchemaTypeName());

							XmlSchemaType schemaType = null;
							if (newSimpleOrComplexObjectElement.getSchemaType() != null) {
								schemaType = newSimpleOrComplexObjectElement
										.getSchemaType();
							} else if (newSimpleOrComplexObjectElement
									.getRefName() != null) {
								schemaType = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
												service,
												newSimpleOrComplexObjectElement
														.getRefName());
								if (schemaType == null) {
									schemaType = ParsingUtils
											.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
													service,
													newSimpleOrComplexObjectElement
															.getRefName());
								}

								System.out.println();
							} else {
								System.out.println();
							}

							// if(newSimpleOrComplexObjectElement.getSchemaType()!=null){
							if (schemaType != null) {
								System.out.println("\t\t\t\t\t\t#"
										+ schemaType.getClass().toString()
										+ "#");

								boolean typeParsed = false;
								if (schemaType
										.getClass()
										.toString()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
									NativeObject no1 = new NativeObject();
									ComplexObject unionCO = SimpleTypesParser
											.parseSimpleType(
													newSimpleOrComplexObjectElement,
													null, no1, theDefinition,
													service);
									if (newSimpleOrComplexObjectElement
											.getMinOccurs() == 0
											|| newSimpleOrComplexObjectElement
													.isNillable()) {
										no1.setIsOptional(true);
									}
									if (unionCO != null) {
										if (newSimpleOrComplexObjectElement
												.getMaxOccurs() > 1) {
											ComplexObject noArrayCO = new ComplexObject();

											noArrayCO.setObjectName(no1
													.getObjectName());
											noArrayCO.setObjectType(new QName(
													no1.getObjectType()
															.getNamespaceURI(),
													no1.getObjectType()
															.getLocalPart()
															+ "[]", no1
															.getObjectType()
															.getPrefix()));
											noArrayCO.setIsArrayType(true);
											noArrayCO.getHasComplexObjects()
													.add(unionCO);
											sequenceCO.getHasComplexObjects()
													.add(noArrayCO);
											typeParsed = true;

										} else {
											typeParsed = true;
											sequenceCO.getHasComplexObjects()
													.add(unionCO);
										}
										System.out.println();
									} else {
										if (no1 != null
												&& no1.getAdditionalInfo() != null
												&& no1.getAdditionalInfo()
														.contains("isListType")) {
											if (newSimpleOrComplexObjectElement
													.getMaxOccurs() > 1) {
												ComplexObject noArrayCO = new ComplexObject();
												noArrayCO.setObjectName(no1
														.getObjectName());
												noArrayCO
														.setObjectType(new QName(
																no1.getObjectType()
																		.getNamespaceURI(),
																no1.getObjectType()
																		.getLocalPart()
																		+ "[][]",
																no1.getObjectType()
																		.getPrefix()));
												noArrayCO.setIsArrayType(true);

												ComplexObject noArrayCO_ListNO = new ComplexObject();
												noArrayCO_ListNO
														.setObjectName(no1
																.getObjectName());
												noArrayCO_ListNO
														.setObjectType(new QName(
																no1.getObjectType()
																		.getNamespaceURI(),
																no1.getObjectType()
																		.getLocalPart()
																		+ "[]",
																no1.getObjectType()
																		.getPrefix()));
												noArrayCO_ListNO
														.setIsArrayType(true);
												noArrayCO_ListNO
														.setIsOptional(no1
																.isIsOptional());
												noArrayCO_ListNO
														.getHasNativeObjects()
														.add(no1);

												noArrayCO
														.getHasComplexObjects()
														.add(noArrayCO_ListNO);
												sequenceCO
														.getHasComplexObjects()
														.add(noArrayCO);
												typeParsed = true;

											} else {
												typeParsed = true;
												ComplexObject noArrayCO = new ComplexObject();
												noArrayCO.setObjectName(no1
														.getObjectName());
												noArrayCO
														.setObjectType(new QName(
																no1.getObjectType()
																		.getNamespaceURI(),
																no1.getObjectType()
																		.getLocalPart()
																		+ "[]",
																no1.getObjectType()
																		.getPrefix()));
												noArrayCO.setIsArrayType(true);
												noArrayCO.getHasNativeObjects()
														.add(no1);
												noArrayCO.setIsOptional(no1
														.isIsOptional());
												sequenceCO
														.getHasComplexObjects()
														.add(noArrayCO);
											}
										} else {
											if (newSimpleOrComplexObjectElement
													.getMaxOccurs() > 1) {
												ComplexObject noArrayCO = new ComplexObject();

												noArrayCO.setObjectName(no1
														.getObjectName());
												noArrayCO
														.setObjectType(new QName(
																no1.getObjectType()
																		.getNamespaceURI(),
																no1.getObjectType()
																		.getLocalPart()
																		+ "[]",
																no1.getObjectType()
																		.getPrefix()));
												noArrayCO.setIsArrayType(true);
												noArrayCO.getHasNativeObjects()
														.add(no1);
												sequenceCO
														.getHasComplexObjects()
														.add(noArrayCO);
												typeParsed = true;

											} else {
												typeParsed = true;
												sequenceCO
														.getHasNativeObjects()
														.add(no1);
											}
										}
									}

								} else if (schemaType
										.getClass()
										.toString()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
									ComplexObject co1 = new ComplexObject();
									// edw prepei na mpei elegxos an to
									// newSimpleOrComplexObjectElement->schemaType
									// einai idio me to ct
									// gia na apofygw to StackOverflowError
									if (newSimpleOrComplexObjectElement
											.getSchemaType() != null) {
										ComplexTypesParser
												.parseComplexType(
														service,
														newSimpleOrComplexObjectElement,
														null, co1,
														theDefinition,
														calledFromAbstractTypeParser);
										sequenceCO.getHasComplexObjects().add(
												co1);
									} else {
										co1.setObjectName(schemaType.getQName());
										co1.setObjectType(schemaType.getQName());
										ComplexTypesParser.parseComplexType(
												service, null, schemaType, co1,
												theDefinition,
												calledFromAbstractTypeParser);
										if (newSimpleOrComplexObjectElement != null
												&& (newSimpleOrComplexObjectElement
														.getMinOccurs() == 0 || newSimpleOrComplexObjectElement
														.isNillable())) {
											co1.setIsOptional(true);
										}

										if (newSimpleOrComplexObjectElement != null
												&& newSimpleOrComplexObjectElement
														.getMaxOccurs() > 1) {
											ComplexObject coArrayCO = new ComplexObject();
											coArrayCO.setObjectName(co1
													.getObjectName());
											// co1.setObjectType(new
											// QName("XA!"));
											coArrayCO.setObjectType(new QName(
													co1.getObjectType()
															.getNamespaceURI(),
													co1.getObjectType()
															.getLocalPart()
															+ "[]", co1
															.getObjectType()
															.getPrefix()));
											coArrayCO.setIsArrayType(true);
											coArrayCO.getHasComplexObjects()
													.add(co1);
											coArrayCO.setIsOptional(co1
													.isIsOptional());
											sequenceCO.getHasComplexObjects()
													.add(coArrayCO);
										} else {
											sequenceCO.getHasComplexObjects()
													.add(co1);
										}
									}
									typeParsed = true;
								} else {
									System.out.println();
								}

								if (!typeParsed) {
									System.out
											.println("ERROR 1!!!!!!!!!!!!!!!!!! @line ~2648 Axis2");
									theDefinition
											.getContainingErrors()
											.add("ERROR 1!!!!!!!!!!!!!!!!!! @line ~2648 Axis2");
									// System.exit(-1);
								}
								System.out.println();
							} else {
								System.out.println();
							}
						} else if (newObj21 != null
								&& newObj21
										.getClass()
										.getName()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaChoice")) {
							org.apache.ws.commons.schema.XmlSchemaChoice schemaChoice = (org.apache.ws.commons.schema.XmlSchemaChoice) newObj21;
							ComplexObject newChoiceCO = new ComplexObject();
							newChoiceCO.setObjectName(new QName(
									"http://www.w3.org/2001/XMLSchema",
									"XmlSchemaChoice"));
							newChoiceCO.setObjectType(new QName(
									"http://www.w3.org/2001/XMLSchema",
									"XmlSchemaChoice"));
							newChoiceCO.setIsAbstract(true);

							AdditionalTypesParser
									.parseXMLSchemaChoiceElement(service,
											schemaChoice, newChoiceCO,
											theDefinition,
											calledFromAbstractTypeParser);

							if (schemaChoice.getMinOccurs() == 0) {
								newChoiceCO.setIsOptional(true);
							}

							if (schemaChoice.getMaxOccurs() > 1) {
								// Array Type
								ComplexObject new_arrayCO = new ComplexObject();
								new_arrayCO.setObjectName(newChoiceCO
										.getObjectName());
								// arrayCO.setObjectType(new
								// QName(co1.getObjectType()+"[]"));
								new_arrayCO
										.setObjectType(new QName(newChoiceCO
												.getObjectType()
												.getNamespaceURI(), newChoiceCO
												.getObjectType().getLocalPart()
												+ "[]", newChoiceCO
												.getObjectType().getPrefix()));
								new_arrayCO.setIsArrayType(true);
								new_arrayCO.getHasComplexObjects().add(
										newChoiceCO);
								new_arrayCO.setIsOptional(newChoiceCO
										.isIsOptional());
								sequenceCO.getHasComplexObjects().add(
										new_arrayCO);
							} else {
								sequenceCO.getHasComplexObjects().add(
										newChoiceCO);
							}

							// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"   "+newSimpleOrComplexObjectElement.getSchemaTypeName());
							System.out.println("aaa!");
						} else if (newObj21 != null
								&& newObj21
										.getClass()
										.getName()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaAny")) {
							try {
								org.apache.ws.commons.schema.XmlSchemaAny newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaAny) newObj21;

								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(new QName("any"));
								co1.setObjectType(new QName("Object"));

								if (newSimpleOrComplexObjectElement
										.getMinOccurs() == 0) {
									co1.setIsOptional(true);
								}

								if (newSimpleOrComplexObjectElement
										.getMaxOccurs() > 1) {
									// Array Type
									ComplexObject arrayCO = new ComplexObject();
									arrayCO.setObjectName(co1.getObjectName());
									arrayCO.setObjectType(new QName(co1
											.getObjectType() + "[]"));
									arrayCO.setIsArrayType(true);
									arrayCO.getHasComplexObjects().add(co1);
									arrayCO.setIsOptional(co1.isIsOptional());
									sequenceCO.getHasComplexObjects().add(
											arrayCO);
								} else {
									sequenceCO.getHasComplexObjects().add(co1);
								}

								// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"   "+newSimpleOrComplexObjectElement.getSchemaTypeName());
								System.out.println("aaa!");
							} catch (Exception e) {
								e.printStackTrace();
								System.out.println("w!");
							}
							System.out.println("eee");
						} else {
							System.out.println();
						}
					}
					choiceCO.getHasExtendedObjects().add(sequenceCO);
					System.out.println();
				}
			}
		}
	}

	public static void parseXMLSchemaSimpleTypeUnionElement(
			AxisService service,
			org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion unionElement,
			ComplexObject unionCO, ParsedWSDLDefinition theDefinition,
			boolean calledFromAbstractTypeParser) {
		QName[] memberTypeNames = unionElement.getMemberTypesQNames();
		if (memberTypeNames == null || memberTypeNames.length == 0) {
			XmlSchemaObjectCollection col1 = unionElement.getBaseTypes();
			Iterator iter1 = col1.getIterator();
			while (iter1.hasNext()) {
				Object obj = iter1.next();
				if (obj != null
						&& obj.getClass().getName()
								.contains("XmlSchemaSimpleType")) {
					XmlSchemaSimpleType xmlSchemaSimpleType = (XmlSchemaSimpleType) obj;
					NativeObject no1 = new NativeObject();
					// no1.objectName = "elementOfArray";
					no1.setObjectType(xmlSchemaSimpleType.getQName());
					no1.setObjectName(new QName("value"));

					ComplexObject unionCO2 = SimpleTypesParser.parseSimpleType(
							null, xmlSchemaSimpleType, no1, theDefinition,
							service);
					if (unionCO2 == null) {
						if (no1.getAdditionalInfo() != null
								&& no1.getAdditionalInfo().contains(
										"isListType")) {
							ComplexObject noArrayCO = new ComplexObject();
							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
							noArrayCO.setIsArrayType(true);
							noArrayCO.getHasNativeObjects().add(no1);
							// noArrayCO.setIsOptional(no1.isIsOptional());
							unionCO.getHasExtendedObjects().add(noArrayCO);

						} else {
							unionCO.getHasExtendedObjects().add(no1);
						}
					} else {
						ComplexObject co2 = new ComplexObject();
						co2.setObjectName(no1.getObjectName());
						if (xmlSchemaSimpleType.getQName() != null) {
							co2.setObjectType(xmlSchemaSimpleType.getQName());
						} else {
							System.out.println();
						}
						co2.getHasComplexObjects().add(unionCO2);
						unionCO.getHasExtendedObjects().add(co2);
					}

				} else {
					System.out.println();
				}
			}
		} else {
			for (int i = 0; i < memberTypeNames.length; i++) {
				QName memberQName = memberTypeNames[i];
				XmlSchemaType xmlSchemaType = ParsingUtils
						.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
								service, memberQName);
				if (xmlSchemaType == null) {
					xmlSchemaType = ParsingUtils
							.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
									service, memberQName);
				}
				if (xmlSchemaType == null) {
					System.out.println();
					if (memberQName.getNamespaceURI().equals(
							"http://www.w3.org/2001/XMLSchema")) {
						NativeObject no1 = new NativeObject();
						// no1.objectName = "elementOfArray";
						no1.setObjectType(memberQName);
						no1.setObjectName(memberQName);
						unionCO.getHasExtendedObjects().add(no1);
					} else {
						continue;
					}
				} else {
					if (xmlSchemaType.getClass().getName()
							.contains("XmlSchemaSimpleType")) {
						NativeObject no1 = new NativeObject();
						// no1.objectName = "elementOfArray";
						no1.setObjectType(xmlSchemaType.getQName());
						no1.setObjectName(memberQName);

						ComplexObject newCO = SimpleTypesParser
								.parseSimpleType(null, xmlSchemaType, no1,
										theDefinition, service);

						if (newCO == null) {
							if (no1.getAdditionalInfo() != null
									&& no1.getAdditionalInfo().contains(
											"isListType")) {
								ComplexObject noArrayCO = new ComplexObject();
								noArrayCO.setObjectName(no1.getObjectName());
								noArrayCO.setObjectType(new QName(no1
										.getObjectType().getNamespaceURI(), no1
										.getObjectType().getLocalPart() + "[]",
										no1.getObjectType().getPrefix()));
								noArrayCO.setIsArrayType(true);
								noArrayCO.getHasNativeObjects().add(no1);
								// noArrayCO.setIsOptional(no1.isIsOptional());
								unionCO.getHasExtendedObjects().add(noArrayCO);

							} else {
								unionCO.getHasExtendedObjects().add(no1);
							}
						} else {
							unionCO.getHasExtendedObjects().add(newCO);
						}
					} else {
						System.out.println();
					}
				}
			}
		}
	}

	public static Object parseXmlSchemaAttribute(
			XmlSchemaAttribute xmlSchemaAttribute, AxisService service,
			ParsedWSDLDefinition theDefinition) {
		// RETURNS NativeObjet or ComplexObject
		if (xmlSchemaAttribute == null)
			return null;

		System.out.println(xmlSchemaAttribute.getName());
		if (xmlSchemaAttribute.getSchemaTypeName() != null
				&& xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI() != null
				&& xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI()
						.contains("http://www.w3.org/2001/XMLSchema")) {
			NativeObject no = new NativeObject();
			no.setObjectName(xmlSchemaAttribute.getQName());
			no.setObjectType(xmlSchemaAttribute.getSchemaTypeName());
			// no.setNamespaceURI(xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI());
			// no.additionalInfo=xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI();
			return no;

		} else {
			ComplexObject co = new ComplexObject();
			co.setObjectName(xmlSchemaAttribute.getQName());
			co.setObjectType(xmlSchemaAttribute.getSchemaTypeName());

			// FIND THE TYPE!
			// parse it...
			XmlSchemaType xmlSchemaType1 = ParsingUtils
					.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
							service, xmlSchemaAttribute.getSchemaTypeName());
			if (xmlSchemaType1 == null) {
				xmlSchemaType1 = ParsingUtils
						.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
								service, xmlSchemaAttribute.getSchemaTypeName());
			}

			if (xmlSchemaType1 == null) {
				xmlSchemaType1 = xmlSchemaAttribute.getSchemaType();
			}

			if (xmlSchemaType1 == null) {
				if (xmlSchemaAttribute.getRefName() != null) {
					xmlSchemaType1 = ParsingUtils
							.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
									service, xmlSchemaAttribute.getRefName());
					if (xmlSchemaType1 == null) {
						xmlSchemaType1 = ParsingUtils
								.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
										service,
										xmlSchemaAttribute.getRefName());
					}
				}
				System.out.println();
			}

			if (xmlSchemaType1 == null) {
				Object res123 = ParsingUtils
						.tryToFindAndParseAttributeForSpecificObject(
								theDefinition, service,
								xmlSchemaAttribute.getRefName());
				if (res123 != null) {
					return res123;
				} else {
					System.out.println("XA! W!");
					NativeObject no = new NativeObject();
					if (xmlSchemaAttribute.getQName() != null) {
						no.setObjectName(xmlSchemaAttribute.getQName());
					} else if (xmlSchemaAttribute.getRefName() != null) {
						no.setObjectName(xmlSchemaAttribute.getRefName());
					} else if (xmlSchemaAttribute.getName() != null) {
						no.setObjectName(new QName(xmlSchemaAttribute.getName()));
					} else {
						no.setObjectName(new QName("UNDEFINED variable name!"));
						theDefinition
								.getContainingErrors()
								.add("ERROR @line ~2968... UNDEFINED Variable name!!!");
						System.out
								.println("ERROR @line ~2968... UNDEFINED Variable name!!!");
					}

					no.setObjectType(new QName("Object"));
					return no;
				}
			}

			System.out.println(xmlSchemaType1.getName());
			if (xmlSchemaType1.getClass().getName()
					.contains("XmlSchemaComplexType")) {
				ComplexObject co1 = new ComplexObject();

				// PARSE XML Schema Attribute
				ComplexTypesParser.parseComplexType(service, null,
						xmlSchemaType1, co1, theDefinition, false);
				co1.setObjectName(co1.getObjectType());
				co.getHasComplexObjects().add(co1);

			} else if (xmlSchemaType1.getClass().getName()
					.contains("XmlSchemaSimpleType")) {
				NativeObject no1 = new NativeObject();

				ComplexObject unionCO = SimpleTypesParser.parseSimpleType(null,
						xmlSchemaType1, no1, theDefinition, service);
				if (unionCO == null) {
					if ((co.getHasComplexObjects() == null || co
							.getHasComplexObjects().size() == 0)
							&& (co.getHasNativeObjects() == null || co
									.getHasNativeObjects().size() == 0)) {
						if (co.getObjectName() != null) {
							no1.setObjectName(co.getObjectName());
							if (no1.getObjectType() == null) {
								no1.setObjectType(co.getObjectType());
							}
							return no1;
						} else {
							System.out.println();
						}
					}

					if (no1.getObjectName() == null) {
						no1.setObjectName(new QName("UNDEFINED variable name"));
						theDefinition
								.getContainingErrors()
								.add("ERROR @line ~3001... UNDEFINED Variable name!!!");
						System.out
								.println("ERROR @line ~3001... UNDEFINED Variable name!!!");
						System.out.println();
					}
					co.getHasNativeObjects().add(no1);
				} else {
					co.getHasComplexObjects().add(unionCO);
				}

			}

			// co.setNamespaceURI(xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI());
			// co.additionalInfo=xmlSchemaAttribute.getSchemaTypeName().getNamespaceURI();
			return co;

		}
	}

	public static void parseAllExtensionTypesOfTheAbstractType(
			AxisService service, ComplexObject co, QName abstractTypeName,
			ParsedWSDLDefinition theDefinition) {

		ArrayList schemasList = service.getSchema();
		if (schemasList == null)
			return;

		for (int i = 0; i < schemasList.size(); i++) {
			org.apache.ws.commons.schema.XmlSchema s = service.getSchema(i);

			XmlSchemaObjectTable typesTable = s.getSchemaTypes();
			Iterator typesIter = typesTable.getValues();
			while (typesIter.hasNext()) {
				org.apache.ws.commons.schema.XmlSchemaType type = (org.apache.ws.commons.schema.XmlSchemaType) typesIter
						.next();

				if (type.getClass()
						.getName()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
					org.apache.ws.commons.schema.XmlSchemaComplexType type1 = (org.apache.ws.commons.schema.XmlSchemaComplexType) type;

					if (type1.getContentModel() != null
							&& type1.getContentModel().getContent() != null
							&& type1.getContentModel()
									.getContent()
									.getClass()
									.getName()
									.contains(
											"XmlSchemaComplexContentExtension")) {
						XmlSchemaComplexContentExtension extensionContent = (XmlSchemaComplexContentExtension) type1
								.getContentModel().getContent();
						if (extensionContent.getBaseTypeName().equals(
								abstractTypeName)) {
							System.out.println(extensionContent
									.getBaseTypeName());
							ComplexObject co1 = new ComplexObject();
							co1.setObjectName(new QName("extendedObject"));
							co1.setObjectType(type1.getQName());
							// co1.setNamespaceURI(type1.getQName().getNamespaceURI());

							XmlSchemaType xmlSchemaType1 = ParsingUtils
									.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
											service, type1.getQName());
							if (xmlSchemaType1 == null) {
								xmlSchemaType1 = ParsingUtils
										.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
												service, type1.getQName());
							}

							if (xmlSchemaType1.getClass().getName()
									.contains("XmlSchemaComplexType")) {
								ComplexTypesParser.parseComplexType(service,
										null, xmlSchemaType1, co1,
										theDefinition, true);

							} else {
								System.out.println("ERROR @line ~348");
								theDefinition.getContainingErrors().add(
										"ERROR @line ~348");
							}

							// if(co1.getObjectName().getLocalPart().equals(type1))

							co.getHasExtendedObjects().add(co1);
							System.out.println("");
						}
					}

				}

			}

		}
	}

	public static void parseArrayType(AxisService service,
			ComplexObject arrayCO, QName qName,
			ParsedWSDLDefinition theDefinition,
			org.apache.ws.commons.schema.XmlSchemaType inXmlSchemaType,
			boolean calledFromAbstractTypeParser) {

		// qName -> prepei na einai to type tou contained Object

		ComplexObject containedCO = null;
		NativeObject containedNO = null;

		org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType = null;
		XmlSchemaElement el1 = service.getSchemaElement(qName);
		if (el1 != null) {
			xmlSchemaType = el1.getSchemaType();

		} else {

			xmlSchemaType = ParsingUtils
					.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
							service, qName);
			if (xmlSchemaType == null) {
				xmlSchemaType = ParsingUtils
						.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
								service, qName);
			}
		}

		if (xmlSchemaType == null) {
			xmlSchemaType = inXmlSchemaType;
		}

		if (xmlSchemaType != null) {
			try {
				// -System.out.prinln(xmlSchemaType.getClass());
				if (xmlSchemaType
						.getClass()
						.getName()
						.contains(
								"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
					containedNO = new NativeObject();
					containedNO.setObjectName(xmlSchemaType.getQName());
					// EINAI OK TO onoma pou orizw gia to containedNO...???

					ComplexObject unionCO2 = SimpleTypesParser.parseSimpleType(
							null, xmlSchemaType, containedNO, theDefinition,
							service);
					if (unionCO2 == null) {
						if ((el1 != null && el1.getMaxOccurs() > 1)
								|| (containedNO.getAdditionalInfo() != null && containedNO
										.getAdditionalInfo().contains(
												"isListType"))) {
							ComplexObject noArrayCO = new ComplexObject();
							noArrayCO
									.setObjectName(containedNO.getObjectName());
							noArrayCO.setObjectType(new QName(containedNO
									.getObjectType().getNamespaceURI(),
									containedNO.getObjectType().getLocalPart()
											+ "[]", containedNO.getObjectType()
											.getPrefix()));
							noArrayCO.setIsArrayType(true);
							noArrayCO.getHasNativeObjects().add(containedNO);
							noArrayCO.setIsOptional(containedNO.isIsOptional());
							arrayCO.getHasComplexObjects().add(noArrayCO);

						} else {
							arrayCO.getHasNativeObjects().add(containedNO);
						}
					} else {
						ComplexObject co2 = new ComplexObject();
						co2.setObjectName(containedNO.getObjectName());
						if (xmlSchemaType.getQName() != null) {
							co2.setObjectType(xmlSchemaType.getQName());
						} else {
							System.out.println();
						}
						co2.getHasComplexObjects().add(unionCO2);
						arrayCO.getHasComplexObjects().add(co2);
					}

					// arrayCO.getHasNativeObjects().add(containedNO);
					return;
				}

				containedCO = new ComplexObject();
				// ct: the ComplexType of the complex objects (containedCO) that
				// can be contained in the arrayCO
				org.apache.ws.commons.schema.XmlSchemaComplexType ct = (org.apache.ws.commons.schema.XmlSchemaComplexType) xmlSchemaType;

				if (ct.getQName() != null) {
					containedCO.setObjectType(ct.getQName());
					containedCO.setObjectName(ct.getQName());
				} else {
					System.out.println();
				}
				arrayCO.getHasComplexObjects().add(containedCO);

				Iterator iter1 = null;

				if (ct.getContentModel() == null) {

					if (ct.getParticle() != null) {
						if (ct.getParticle()
								.getClass()
								.getName()
								.equals("org.apache.ws.commons.schema.XmlSchemaSequence")) {
							org.apache.ws.commons.schema.XmlSchemaSequence seq = (org.apache.ws.commons.schema.XmlSchemaSequence) ct
									.getParticle();
							if (seq != null) {
								XmlSchemaObjectCollection col1 = seq.getItems();
								iter1 = col1.getIterator();
							} else {
								System.out.println();
							}
						} else if (ct
								.getParticle()
								.getClass()
								.getName()
								.equals("org.apache.ws.commons.schema.XmlSchemaChoice")) {
							org.apache.ws.commons.schema.XmlSchemaChoice schemaChoice = (org.apache.ws.commons.schema.XmlSchemaChoice) ct
									.getParticle();
							if (schemaChoice != null) {
								System.out.println();
								containedCO.setObjectName(new QName(
										"http://www.w3.org/2001/XMLSchema",
										"XmlSchemaChoice"));
								containedCO.setObjectType(new QName(
										"http://www.w3.org/2001/XMLSchema",
										"XmlSchemaChoice"));
								containedCO.setIsAbstract(true);

								AdditionalTypesParser
										.parseXMLSchemaChoiceElement(service,
												schemaChoice, containedCO,
												theDefinition,
												calledFromAbstractTypeParser);

								if (schemaChoice.getMinOccurs() == 0) {
									containedCO.setIsOptional(true);
								}

								if (schemaChoice.getMaxOccurs() > 1) {
									// Array Type
									try {
										arrayCO.getHasComplexObjects().remove(
												containedCO);
									} catch (Exception e) {
										e.printStackTrace();
									}
									ComplexObject new_arrayCO = new ComplexObject();
									new_arrayCO.setObjectName(containedCO
											.getObjectName());
									// arrayCO.setObjectType(new
									// QName(co1.getObjectType()+"[]"));
									new_arrayCO.setObjectType(new QName(
											containedCO.getObjectType()
													.getNamespaceURI(),
											containedCO.getObjectType()
													.getLocalPart() + "[]",
											containedCO.getObjectType()
													.getPrefix()));
									new_arrayCO.setIsArrayType(true);
									new_arrayCO.getHasComplexObjects().add(
											containedCO);
									new_arrayCO.setIsOptional(containedCO
											.isIsOptional());
									arrayCO.getHasComplexObjects().add(
											new_arrayCO);
								} else {
									// arrayCO.getHasComplexObjects().add(containedCO);
								}

								// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"   "+newSimpleOrComplexObjectElement.getSchemaTypeName());
								System.out.println("aaa!");

							} else {
								System.out.println();
							}
						} else {
							System.out.println("ERROR... Element not parsed ");
						}
					} else {
						// parse attributes
						System.out.println();
					}

				} else {
					System.out.println(ct.getContentModel().getClass());
					if (ct.getContentModel().getClass().getName()
							.contains("XmlSchemaSimpleContent")) {
						XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) ct
								.getContentModel();

						if (containedCO.getObjectName() == null) {
							containedCO.setObjectName(arrayCO.getObjectName());
						}
						if (containedCO.getObjectType() == null) {
							if (arrayCO.getObjectType() != null) {
								containedCO.setObjectType(arrayCO
										.getObjectType());
							} else {
								containedCO.setObjectType(arrayCO
										.getObjectName());
							}
						}
						SimpleTypesParser.parseSimpleContent(service,
								simpleContent, containedCO, theDefinition);
						if (containedCO.getObjectName() == null
								|| containedCO.getObjectName().getLocalPart()
										.equals("Error")) {
							System.out.println();
							// prepei na vrw ti name kai type tha tou valw
						}
						System.out.println();
					} else {
						org.apache.ws.commons.schema.XmlSchemaComplexContent cc = (org.apache.ws.commons.schema.XmlSchemaComplexContent) ct
								.getContentModel();
						System.out.println(cc.getContent().getClass());
						if (cc.getContent()
								.getClass()
								.getName()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaComplexContentExtension")) {
							// COMPLEX CONTENT!!!
							if (containedCO.getObjectName() == null) {
								containedCO.setObjectName(arrayCO
										.getObjectName());
							}
							if (containedCO.getObjectType() == null) {
								if (arrayCO.getObjectType() != null) {
									containedCO.setObjectType(arrayCO
											.getObjectType());
								} else {
									containedCO.setObjectType(arrayCO
											.getObjectName());
								}
							}
							ComplexTypesParser.parseComplexContent(service, cc,
									containedCO, theDefinition,
									calledFromAbstractTypeParser,
									containedCO.getObjectType());

							// if(containedCO.getObjectType()==null)
							// containedCO.objectType=arrayCO.objectName;

							return;

						} else if (cc
								.getContent()
								.getClass()
								.getName()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction")) {
							org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction restriction = (org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction) cc
									.getContent();
							if (restriction.getParticle() != null) // CHECK
																	// ADDED
																	// DJ,GIORGOS,
																	// 5-12-2008
							{
								System.out.println(restriction.getParticle()
										.getClass());
								org.apache.ws.commons.schema.XmlSchemaSequence seq = (org.apache.ws.commons.schema.XmlSchemaSequence) restriction
										.getParticle();
								iter1 = seq.getItems().getIterator();
							} else if (restriction.getAttributes() != null) {
								iter1 = restriction.getAttributes()
										.getIterator();
							}// CHECK END ADDED DJ,GIORGOS, 5-12-2008
						}
					}
				}

				if (iter1 != null) // CHECK ADDED DJ,GIORGOS - 5-12-2008
				{
					while (iter1.hasNext()) {
						org.apache.ws.commons.schema.XmlSchemaObject abstractElem = (org.apache.ws.commons.schema.XmlSchemaObject) iter1
								.next();

						if (abstractElem instanceof org.apache.ws.commons.schema.XmlSchemaElement) {
							org.apache.ws.commons.schema.XmlSchemaElement elem1 = (org.apache.ws.commons.schema.XmlSchemaElement) abstractElem;
							if (elem1 != null) {
								// System.out.println(elem1.getSchemaType().getClass());
								XmlSchemaType theSchemaType = null;

								if (elem1.getSchemaType() == null) {
									// parse wsdl to find REQUIRED schemaType
									if (elem1.getSchemaTypeName() != null) {
										theSchemaType = ParsingUtils
												.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
														service,
														elem1.getSchemaTypeName());
										/*
										 * if (xmlSchemaType == null) {
										 * xmlSchemaType =
										 * parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement
										 * (service,
										 * schElemOfType.getSchemaTypeName()); }
										 */
									} else if (elem1.getRefName() != null) {
										theSchemaType = ParsingUtils
												.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
														service,
														elem1.getRefName());
										if (theSchemaType == null) {
											theSchemaType = ParsingUtils
													.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
															service,
															elem1.getRefName());
										}
									}
									System.out.println();
								} else {
									theSchemaType = elem1.getSchemaType();
								}

								if (theSchemaType == null) {
									Object res123 = ParsingUtils
											.tryToFindAndParseAttributeForSpecificObject(
													theDefinition, service,
													elem1.getRefName());
									if (res123 != null) {
										if (res123.getClass().getName()
												.contains("NativeObject")) {
											containedCO.getHasNativeObjects()
													.add(res123);
											continue;
										} else if (res123.getClass().getName()
												.contains("ComplexObject")) {
											containedCO.getHasComplexObjects()
													.add(res123);
											continue;
										}

									} else {
										System.out.println("XA! W!");
										ComplexObject co123 = new ComplexObject();
										if (elem1.getQName() != null) {
											co123.setObjectName(elem1
													.getQName());
										} else if (elem1.getRefName() != null) {
											co123.setObjectName(elem1
													.getRefName());
										} else if (elem1.getName() != null) {
											co123.setObjectName(new QName(elem1
													.getName()));
										} else {
											co123.setObjectName(new QName(
													"UNDEFINED variable name!"));
											theDefinition
													.getContainingErrors()
													.add("ERROR @line ~1036... UNDEFINED Variable name!!!");
											System.out
													.println("ERROR @line ~1036... UNDEFINED Variable name!!!");
										}

										co123.setObjectType(new QName("Object"));
										containedCO.getHasComplexObjects().add(
												co123);
									}
									continue;
								}

								if (theSchemaType
										.getClass()
										.toString()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
									ComplexObject co1 = new ComplexObject();
									if (elem1.getQName() != null) {
										co1.setObjectName(elem1.getQName());
									} else if (elem1.getRefName() != null) {
										co1.setObjectName(elem1.getRefName());
									} else if (elem1.getName() != null) {
										co1.setObjectName(new QName(elem1
												.getName()));
									} else {
										co1.setObjectName(new QName(
												"UNDEFINED variable name"));
										theDefinition
												.getContainingErrors()
												.add("WARNING @line ~1024... UNDEFINED Variable name!!!");
										System.out
												.println("WARNING @line ~1024... UNDEFINED Variable name!!!");
									}

									ComplexTypesParser.parseComplexType(
											service, null, theSchemaType, co1,
											theDefinition,
											calledFromAbstractTypeParser);

									if (elem1 != null
											&& (elem1.getMinOccurs() == 0 || elem1
													.isNillable())) {
										co1.setIsOptional(true);
									}

									if (elem1 != null
											&& elem1.getMaxOccurs() > 1) {
										ComplexObject coArrayCO = new ComplexObject();
										coArrayCO.setObjectName(co1
												.getObjectName());
										// co1.setObjectType(new QName("XA!"));
										coArrayCO.setObjectType(new QName(co1
												.getObjectType()
												.getNamespaceURI(), co1
												.getObjectType().getLocalPart()
												+ "[]", co1.getObjectType()
												.getPrefix()));
										coArrayCO.setIsArrayType(true);
										coArrayCO.getHasComplexObjects().add(
												co1);
										coArrayCO.setIsOptional(co1
												.isIsOptional());
										containedCO.getHasComplexObjects().add(
												coArrayCO);

									} else {
										containedCO.getHasComplexObjects().add(
												co1);
									}

									// co.isArrayType=true;
									// co.objectType =
									// elem1.getSchemaTypeName().getLocalPart()
									// + "[]";
									// co.setNamespaceURI(elem1.getSchemaTypeName().getNamespaceURI());
								} else if (theSchemaType
										.getClass()
										.toString()
										.contains(
												"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
									NativeObject no1 = new NativeObject();
									// no1.objectName = "elementOfArray";
									// no1.setNamespaceURI(elem1.getSchemaTypeName().getNamespaceURI());
									no1.setObjectType(elem1.getSchemaTypeName());
									// no1.setObjectName(elem1.getQName());
									// no1.additionalInfo =
									// elem1.getSchemaTypeName().getNamespaceURI();
									if (elem1.getQName() != null) {
										no1.setObjectName(elem1.getQName());
									} else if (elem1.getRefName() != null) {
										no1.setObjectName(elem1.getRefName());
									} else if (elem1.getName() != null) {
										no1.setObjectName(new QName(elem1
												.getName()));
									} else {
										no1.setObjectName(new QName(
												"UNDEFINED variable name"));
										theDefinition
												.getContainingErrors()
												.add("WARNING @line ~1051... UNDEFINED Variable name!!!");
										System.out
												.println("WARNING @line ~1051... UNDEFINED Variable name!!!");
									}

									ComplexObject unionCO = SimpleTypesParser
											.parseSimpleType(null,
													theSchemaType, no1,
													theDefinition, service);
									if (unionCO == null) {
										// no1.objectName=no1.objectType;
										if (elem1 != null
												&& (elem1.getMinOccurs() == 0 || elem1
														.isNillable())) {
											no1.setIsOptional(true);
										}
										if ((elem1 != null && elem1
												.getMaxOccurs() > 1)
												|| (no1.getAdditionalInfo() != null && no1
														.getAdditionalInfo()
														.contains("isListType"))) {
											ComplexObject noArrayCO = new ComplexObject();
											noArrayCO.setObjectName(no1
													.getObjectName());
											noArrayCO.setObjectType(new QName(
													no1.getObjectType()
															.getNamespaceURI(),
													no1.getObjectType()
															.getLocalPart()
															+ "[]", no1
															.getObjectType()
															.getPrefix()));
											noArrayCO.setIsArrayType(true);
											noArrayCO.getHasNativeObjects()
													.add(no1);
											noArrayCO.setIsOptional(no1
													.isIsOptional());
											containedCO.getHasComplexObjects()
													.add(noArrayCO);

										} else {
											containedCO.getHasNativeObjects()
													.add(no1);
										}
									} else {
										ComplexObject co1 = new ComplexObject();
										co1.setObjectName(no1.getObjectName());
										if (theSchemaType.getQName() != null) {
											co1.setObjectType(theSchemaType
													.getQName());
										} else {
											System.out.println();
										}
										co1.getHasComplexObjects().add(unionCO);
										containedCO.getHasExtendedObjects()
												.add(co1);
									}

								} else {
									theDefinition.getContainingErrors().add(
											"ERROR @ line ~980!!!");
									System.out.println("ERROR @ line ~980!!!");
								}
							}
						} else if (abstractElem instanceof org.apache.ws.commons.schema.XmlSchemaAttribute) {
							org.apache.ws.commons.schema.XmlSchemaAttribute elem1 = (org.apache.ws.commons.schema.XmlSchemaAttribute) abstractElem;
							if (elem1 != null) {
								if (elem1.getSchemaType() != null) {
									if (elem1
											.getSchemaType()
											.getClass()
											.toString()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
										ComplexObject co1 = new ComplexObject();
										if (elem1.getQName() != null) {
											co1.setObjectName(elem1.getQName());// Panta
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
										} else if (elem1.getRefName() != null) {
											co1.setObjectName(elem1
													.getRefName());
										} else if (elem1.getName() != null) {
											co1.setObjectName(new QName(elem1
													.getName()));
										} else {
											co1.setObjectName(new QName(
													"UNDEFINED variable name"));
											theDefinition
													.getContainingErrors()
													.add("WARNING @line ~1095... UNDEFINED Variable name!!!");
											System.out
													.println("WARNING @line ~1095... UNDEFINED Variable name!!!");
										}

										ComplexTypesParser.parseComplexType(
												service, null,
												elem1.getSchemaType(), co1,
												theDefinition,
												calledFromAbstractTypeParser);

										// co.objectType =
										// elem1.getSchemaTypeName().getLocalPart()
										// + "[]";
										// co.setNamespaceURI(elem1.getSchemaTypeName().getNamespaceURI());
										containedCO.getHasComplexObjects().add(
												co1);
										// co.isArrayType=true;
									} else if (elem1
											.getSchemaType()
											.getClass()
											.toString()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
										NativeObject no1 = new NativeObject();
										// no1.objectName = co.objectName;
										no1.setObjectType(elem1
												.getSchemaTypeName());
										// no1.setObjectName(elem1.getQName());
										if (elem1.getQName() != null) {
											no1.setObjectName(elem1.getQName());
										} else if (elem1.getRefName() != null) {
											no1.setObjectName(elem1
													.getRefName());
										} else if (elem1.getName() != null) {
											no1.setObjectName(new QName(elem1
													.getName()));
										} else {
											no1.setObjectName(new QName(
													"UNDEFINED variable name"));
											theDefinition
													.getContainingErrors()
													.add("WARNING @line ~1128... UNDEFINED Variable name!!!");
											System.out
													.println("WARNING @line ~1128... UNDEFINED Variable name!!!");
										}

										// no1.setNamespaceURI(elem1.getSchemaTypeName().getNamespaceURI());
										// no1.additionalInfo =
										// elem1.getSchemaTypeName().getNamespaceURI();

										ComplexObject unionCO = SimpleTypesParser
												.parseSimpleType(null,
														elem1.getSchemaType(),
														no1, theDefinition,
														service);
										if (unionCO == null) {
											if (no1.getAdditionalInfo() != null
													&& no1.getAdditionalInfo()
															.contains(
																	"isListType")) {
												ComplexObject noArrayCO = new ComplexObject();
												noArrayCO.setObjectName(no1
														.getObjectName());
												noArrayCO
														.setObjectType(new QName(
																no1.getObjectType()
																		.getNamespaceURI(),
																no1.getObjectType()
																		.getLocalPart()
																		+ "[]",
																no1.getObjectType()
																		.getPrefix()));
												noArrayCO.setIsArrayType(true);
												noArrayCO.getHasNativeObjects()
														.add(no1);
												// noArrayCO.setIsOptional(no1.isIsOptional());
												containedCO
														.getHasComplexObjects()
														.add(noArrayCO);

											} else {
												containedCO
														.getHasNativeObjects()
														.add(no1);
											}
										} else {
											ComplexObject co1 = new ComplexObject();
											co1.setObjectName(no1
													.getObjectName());
											if (elem1.getSchemaType()
													.getQName() != null) {
												co1.setObjectType(elem1
														.getSchemaType()
														.getQName());
											} else {
												System.out.println();
											}
											co1.getHasComplexObjects().add(
													unionCO);
											containedCO.getHasExtendedObjects()
													.add(co1);
										}

									} else {
										theDefinition.getContainingErrors()
												.add("ERROR @ line ~394!!!");
										System.out
												.println("ERROR @ line ~394!!!");
									}
								} else {
									// TODO Analyze this attribute
									// theDefinition.getContainingErrors().add("ERROR - WARNING @ line ~663!!!... mpike edw... check ti ginetai edw...");
									// System.out.println("ERROR - WARNING @ line ~663!!!");

									if (arrayCO.getObjectType() != null) {
										String type1 = arrayCO.getObjectType()
												.getLocalPart()
												.replace("[]", "");
										QName qName1 = new QName(
												qName.getNamespaceURI(), type1,
												qName.getPrefix());

										XmlSchemaType xmlSchemaType1 = ParsingUtils
												.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
														service, qName1);
										if (xmlSchemaType1 == null) {
											xmlSchemaType1 = ParsingUtils
													.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
															service, qName1);
										}

										System.out.println(xmlSchemaType1
												.getName());
										if (xmlSchemaType1
												.getClass()
												.getName()
												.contains(
														"XmlSchemaComplexType")) {
											ComplexObject co1 = new ComplexObject();
											// co1.objectName="elementOfArray";
											if (elem1.getQName() != null) {
												co1.setObjectName(elem1
														.getQName());// Panta
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
											} else if (elem1.getRefName() != null) {
												co1.setObjectName(elem1
														.getRefName());
											} else if (elem1.getName() != null) {
												co1.setObjectName(new QName(
														elem1.getName()));
											} else {
												co1.setObjectName(new QName(
														"UNDEFINED variable name"));
												theDefinition
														.getContainingErrors()
														.add("WARNING @line ~1162... UNDEFINED Variable name!!!");
												System.out
														.println("WARNING @line ~1162... UNDEFINED Variable name!!!");
											}

											ComplexTypesParser
													.parseComplexType(service,
															null,
															xmlSchemaType1,
															co1, theDefinition,
															calledFromAbstractTypeParser);

											// co.objectType =
											// xmlSchemaType1.getQName().getLocalPart()
											// + "[]";
											// co.setNamespaceURI(xmlSchemaType1.getQName().getNamespaceURI());
											containedCO.getHasComplexObjects()
													.add(co1);
											// containedCO.isArrayType=true;
										} else if (xmlSchemaType1
												.getClass()
												.getName()
												.contains("XmlSchemaSimpleType")) {
											NativeObject no1 = new NativeObject();
											// no1.objectName =
											// "elementOfArray";
											no1.setObjectType(elem1
													.getSchemaTypeName());
											// no1.setObjectName(elem1.getQName());
											if (elem1.getQName() != null) {
												no1.setObjectName(elem1
														.getQName());
											} else if (elem1.getRefName() != null) {
												no1.setObjectName(elem1
														.getRefName());
											} else if (elem1.getName() != null) {
												no1.setObjectName(new QName(
														elem1.getName()));
											} else {
												no1.setObjectName(new QName(
														"UNDEFINED variable name"));
												theDefinition
														.getContainingErrors()
														.add("WARNING @line ~1208... UNDEFINED Variable name!!!");
												System.out
														.println("WARNING @line ~1208... UNDEFINED Variable name!!!");
											}
											// no1.setNamespaceURI(elem1.getSchemaTypeName().getNamespaceURI());
											// no1.additionalInfo =
											// elem1.getSchemaTypeName().getNamespaceURI();
											ComplexObject unionCO = SimpleTypesParser
													.parseSimpleType(
															null,
															elem1.getSchemaType(),
															no1, theDefinition,
															service);
											if (unionCO == null) {
												if (no1.getAdditionalInfo() != null
														&& no1.getAdditionalInfo()
																.contains(
																		"isListType")) {
													ComplexObject noArrayCO = new ComplexObject();
													noArrayCO.setObjectName(no1
															.getObjectName());
													noArrayCO
															.setObjectType(new QName(
																	no1.getObjectType()
																			.getNamespaceURI(),
																	no1.getObjectType()
																			.getLocalPart()
																			+ "[]",
																	no1.getObjectType()
																			.getPrefix()));
													noArrayCO
															.setIsArrayType(true);
													noArrayCO
															.getHasNativeObjects()
															.add(no1);
													// noArrayCO.setIsOptional(no1.isIsOptional());
													containedCO
															.getHasComplexObjects()
															.add(noArrayCO);

												} else {
													containedCO
															.getHasNativeObjects()
															.add(no1);
												}
											} else {
												ComplexObject co1 = new ComplexObject();
												co1.setObjectName(no1
														.getObjectName());
												if (elem1.getSchemaType()
														.getQName() != null) {
													co1.setObjectType(elem1
															.getSchemaType()
															.getQName());
												} else {
													System.out.println();
												}
												co1.getHasComplexObjects().add(
														unionCO);
												containedCO
														.getHasExtendedObjects()
														.add(co1);
											}
										}

									} else {
										theDefinition.getContainingErrors()
												.add("ERROR @ line ~426!!!");
										System.out
												.println("ERROR @ line ~426!!!");
										// System.exit(-1);
									}
								}
							}
						} else if (abstractElem instanceof org.apache.ws.commons.schema.XmlSchemaChoice) {
							System.out.println();
							org.apache.ws.commons.schema.XmlSchemaChoice elem1 = (org.apache.ws.commons.schema.XmlSchemaChoice) abstractElem;
							ComplexObject co1 = new ComplexObject();
							co1.setObjectName(new QName(
									"http://www.w3.org/2001/XMLSchema",
									"XmlSchemaChoice"));
							co1.setObjectType(new QName(
									"http://www.w3.org/2001/XMLSchema",
									"XmlSchemaChoice"));
							co1.setIsAbstract(true);

							parseXMLSchemaChoiceElement(service, elem1, co1,
									theDefinition, calledFromAbstractTypeParser);

							if (elem1.getMinOccurs() == 0) {
								co1.setIsOptional(true);
							}

							if (elem1.getMaxOccurs() > 1) {
								// Array Type
								ComplexObject arrayCO1 = new ComplexObject();
								arrayCO1.setObjectName(co1.getObjectName());
								arrayCO1.setObjectType(new QName(co1
										.getObjectType() + "[]"));
								arrayCO1.setIsArrayType(true);
								arrayCO1.getHasComplexObjects().add(co1);
								arrayCO1.setIsOptional(co1.isIsOptional());
								containedCO.getHasComplexObjects()
										.add(arrayCO1);
							} else {
								containedCO.getHasComplexObjects().add(co1);
							}

						} else {
							org.apache.ws.commons.schema.XmlSchemaObject elem1 = null;
							theDefinition.getContainingErrors().add(
									"ERROR @ Axis2 line ~1140!!!");
							System.out.println("ERROR @ Axis2 line ~1140!!!");
						}
					}
				}

				// Parse Attributes...
				XmlSchemaObjectCollection attsCol = ct.getAttributes();
				if (attsCol != null) {
					Iterator iter2 = attsCol.getIterator();
					while (iter2.hasNext()) {
						Object obj = iter2.next();
						if (obj.getClass()
								.getName()
								.equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
							org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;

							Object res1 = parseXmlSchemaAttribute(att, service,
									theDefinition);
							if (res1 != null) {
								if (res1.getClass().getName()
										.contains("NativeObject")) {
									NativeObject no12 = (NativeObject) res1;
									// System.out.println(no12.objectName);
									containedCO.getHasNativeObjects().add(no12);
								} else if (res1.getClass().getName()
										.contains("ComplexObject")) {
									ComplexObject co12 = (ComplexObject) res1;
									// System.out.println(co12.objectName);
									containedCO.getHasComplexObjects()
											.add(co12);
								}
							}
						} else if (obj
								.getClass()
								.getName()
								.contains(
										"org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")) {
							System.out.println();
							ComplexObject co1 = new ComplexObject();
							AdditionalTypesParser
									.parseXmlSchemaAttributeGroupRefElement(
											service,
											(org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef) obj,
											co1, theDefinition);

							if (co1 != null) {
								for (int i = 0; i < co1.getHasComplexObjects()
										.size(); i++) {
									containedCO.getHasComplexObjects().add(
											co1.getHasComplexObjects().get(i));
								}
								for (int i = 0; i < co1.getHasNativeObjects()
										.size(); i++) {
									containedCO.getHasNativeObjects().add(
											co1.getHasNativeObjects().get(i));
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

				if (containedCO.getObjectName() == null) {
					containedCO.setObjectName(arrayCO.getObjectName());
				}
				if (containedCO.getObjectType() == null) {
					if (arrayCO.getObjectType() != null) {
						containedCO.setObjectType(arrayCO.getObjectType());
					} else {
						containedCO.setObjectType(arrayCO.getObjectName());
					}
				}

				if (containedCO.getObjectName() == null) {
					System.out.println();
				}

			} catch (Exception e) {
				theDefinition.getContainingErrors().add(e.toString());
				e.printStackTrace();
				// -System.exit(-5);
			}
		}

		System.out.println(containedCO.getObjectName());

	}

	public static ComplexObject getArrayCoForNo(XmlSchemaElement schElemOfType,
			XmlSchemaType xmlSchemaType, NativeObject no,
			ParsedWSDLDefinition theDefinition) {

		ComplexObject arrayCO = new ComplexObject();
		if (schElemOfType != null) {
			if (schElemOfType.getMinOccurs() == 0 || schElemOfType.isNillable()) {
				arrayCO.setIsOptional(true);
			}

		}
		return arrayCO;
	}

}
