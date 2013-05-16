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
package org.universAAL.ri.wsdlToolkit.parser;


import com.ibm.wsdl.InputImpl;
import com.ibm.wsdl.MessageImpl;
import com.ibm.wsdl.OperationImpl;
import com.ibm.wsdl.OutputImpl;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import org.apache.axis.wsdl.symbolTable.DefinedElement;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.Element;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.Type;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationOutput;
import org.w3c.dom.Node;


/**
 * 
 * Class used for parsing WSDL files of SOAP 1.1 protocol.
 * 
 * @author kgiannou
 */

public class Axis1ParserWrapper {

	public static boolean failedDueToAxisCreation;
	public static boolean failedDueToParsing;
	private static HashMap parsedComplexObjectsHashmap;

	public static ParsedWSDLDefinition parseWSDLwithAxis1(String wsdlURL) {
		failedDueToAxisCreation = false;
		failedDueToParsing = false;
		parsedComplexObjectsHashmap = new HashMap();

		ParsedWSDLDefinition theParsedDefinition = new ParsedWSDLDefinition();
		theParsedDefinition.setParsingComments("OK");
		org.apache.axis.wsdl.gen.Parser axis1Parser = new org.apache.axis.wsdl.gen.Parser();

		try {
			theParsedDefinition.setWsdlURL(new URL(wsdlURL));

			System.out.println("\nAccessing WSDL with AXIS 1...");
			try {
				axis1Parser.setImports(true);

				axis1Parser.run(wsdlURL);
				System.out.println("OK");
			} catch (Exception e) {
				theParsedDefinition.setFailedDueToAxis1(true);
				theParsedDefinition.getContainingErrors().add(e.toString());
				System.out.println("ERROR sto " + wsdlURL);
				System.out.println("Parsing with AXIS failed... ");
				System.out.println("REASON: ");
				e.printStackTrace();
				failedDueToAxisCreation = true;
			}

			Definition theAxisDefinition = axis1Parser.getCurrentDefinition();
			SymbolTable theSymbolTable = axis1Parser.getSymbolTable();

			try {
				if (axis1Parser.getCurrentDefinition().getServices() != null) {
					Collection servicesMap = axis1Parser.getCurrentDefinition()
							.getServices().values();
					Iterator iter1 = servicesMap.iterator();
					while (iter1.hasNext()) {
						com.ibm.wsdl.ServiceImpl serv = (com.ibm.wsdl.ServiceImpl) iter1
								.next();
						System.out.println(serv.getQName().getNamespaceURI());
						theParsedDefinition.setWebServiceName(serv.getQName());
						Map portsMap = serv.getPorts();
						if (portsMap.size() != 1) {
							// theParsedDefinition.containingErrors.add("ERROR! the service '"+serv.getQName().getLocalPart()
							// +"' contains "+portsMap.size()+" ports!  ....!=1");
						}

						boolean soapPortFound = false;
						Iterator portsIter = portsMap.values().iterator();
						while (portsIter.hasNext()) {
							try {
								com.ibm.wsdl.PortImpl portImpl = (com.ibm.wsdl.PortImpl) portsIter
										.next();
								Iterator extElemsIter = portImpl
										.getExtensibilityElements().iterator();
								while (extElemsIter.hasNext()) {
									com.ibm.wsdl.extensions.soap.SOAPAddressImpl sai = (com.ibm.wsdl.extensions.soap.SOAPAddressImpl) extElemsIter
											.next();
									theParsedDefinition.setServiceURL(sai
											.getLocationURI());
									soapPortFound = true;
								}
							} catch (Exception e) {
								// e.printStackTrace();
								// theParsedDefinition.containingErrors.add("ERROR! @line ~114");
							}
						}

						if (!soapPortFound) {
							theParsedDefinition
									.getContainingErrors()
									.add("ERROR! the service '"
											+ serv.getQName().getLocalPart()
											+ "' did not contain SOAP PORT!!!  (contains "
											+ portsMap.size()
											+ " ports!  ....!=1)");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			theParsedDefinition.setTargetNamespaceURI(theAxisDefinition
					.getTargetNamespace());
			parseTheWSDLOperations(theSymbolTable, theAxisDefinition,
					theParsedDefinition);

			return theParsedDefinition;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void parseComplexElement(DefinedElement definedElement,
			SymbolTable theSymbolTable, ComplexObject co,
			ParsedWSDLDefinition theParsedDefinition) {
		if (definedElement == null) {
			return;
		}
		System.out.println("\n\n######   PARSING COMPLEX ELEMENT: "
				+ definedElement.getQName().getLocalPart() + "    ######");
		// System.out.println(definedType.getContainedElements().size());

		if (definedElement.getContainedElements() != null) {
			if (definedElement.getContainedElements().size() > 0) {
				Iterator containedElementsIterator = definedElement
						.getContainedElements().iterator();
				// System.out.println("AAA"+definedType.getContainedElements().size());

				while (containedElementsIterator.hasNext()) {
					// Parse ta contained Elements tou complexType
					// Ta contained elements einai ta native i complex objects
					// pou periexei to complex type
					org.apache.axis.wsdl.symbolTable.ElementDecl containedElement = (org.apache.axis.wsdl.symbolTable.ElementDecl) containedElementsIterator
							.next();
					System.out.println();
					System.out.println("\tcontainedElement isBaseType: "
							+ containedElement.getType().isBaseType());

					if (containedElement.getMaxOccursIsUnbounded()
							&& definedElement.getContainedElements().size() == 1) {
						// Array Type
						System.out.println("\tARRAY:   "
								+ containedElement.getQName().getLocalPart());
						if (containedElement.getType() != null
								&& containedElement.getType().getRefType() != null) {
							System.out.println(containedElement.getType()
									.getQName().getLocalPart());// Type tou CO
							QName objectTypeQName = containedElement.getType()
									.getQName();
							if (containedElement.getType().getQName()
									.getLocalPart() != null
									&& containedElement.getType().getQName()
											.getLocalPart().contains("[")) {
							} else {
								objectTypeQName = new QName(
										containedElement.getType().getQName()
												.getNamespaceURI(),
										containedElement.getType().getQName()
												.getLocalPart()
												+ "[]");
							}
							System.out.println(containedElement.getType()
									.getRefType().isBaseType());// apo dw tha
																// katalavw an
																// einai
																// nativeObject
																// i Complex
							// co.setAdditionalInfo(containedElement.getType().getRefType().getQName().getNamespaceURI());//Namespace
							// tou CO
							co.setObjectType(objectTypeQName);

							if (containedElement.getType().getRefType()
									.isBaseType()) {
								// NativeObject
							} else {
								// ComplexObject
								DefinedType containedElementType = (DefinedType) containedElement
										.getType().getRefType();
								parseComplexType(containedElementType,
										theSymbolTable, co, theParsedDefinition);
								System.out.println();
							}

						} else {
							theParsedDefinition.getContainingErrors().add(
									"ERROR 1234 @line ~150");
							System.out.println("ERROR 1234 @line ~150");

						}

					} else {

						System.out.println("\tNOT ARRAY:    "
								+ containedElement.getQName().getLocalPart());
						// Gia kathe containedElement ftiaxnw kainourgio
						// ComplexObject i NO kai to vazw mesa sto CO
						if (containedElement.getType() != null) {
							if (containedElement.getType().isBaseType()) {
								// NativeObject
								NativeObject no1 = new NativeObject();
								no1.setObjectName(new QName(
										containedElement.getQName()
												.getNamespaceURI(),
										containedElement
												.getQName()
												.getLocalPart()
												.substring(
														containedElement
																.getQName()
																.getLocalPart()
																.indexOf(">") + 1),
										containedElement.getQName().getPrefix()));

								System.out.println(containedElement.getType()
										.getQName().getLocalPart());// Type tou
																	// CO1
								// no1.setObjectType(containedElement.getType().getQName().getLocalPart());
								System.out.println(containedElement.getType()
										.isBaseType());// apo dw tha katalavw an
														// einai nativeObject i
														// Complex
								// no1.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());
								no1.setObjectType(containedElement.getType()
										.getQName());
								// no1.setAdditionalInfo(containedElement.getType().getQName().getNamespaceURI());//Namespace
								// tou CO1

								// DefinedType
								// containedElementType=(DefinedType)containedElement.getType();
								// parseComplexType(containedElementType,
								// theSymbolTable, co1);
								co.getHasNativeObjects().add(no1);

							} else {
								// ComplexObject
								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(new QName(
										containedElement.getQName()
												.getNamespaceURI(),
										containedElement
												.getQName()
												.getLocalPart()
												.substring(
														containedElement
																.getQName()
																.getLocalPart()
																.indexOf(">") + 1),
										containedElement.getQName().getPrefix()));

								System.out.println(containedElement.getType()
										.getQName().getLocalPart());// Type tou
																	// CO1
								// co1.setObjectType(containedElement.getType().getQName().getLocalPart());
								System.out.println(containedElement.getType()
										.isBaseType());// apo dw tha katalavw an
														// einai nativeObject i
														// Complex
								// co1.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());
								co1.setObjectType(containedElement.getType()
										.getQName());
								// co1.setAdditionalInfo(containedElement.getType().getQName().getNamespaceURI());//Namespace
								// tou CO1

								DefinedType containedElementType = (DefinedType) containedElement
										.getType();
								parseComplexType(containedElementType,
										theSymbolTable, co1,
										theParsedDefinition);
								co.getHasComplexObjects().add(co1);
								System.out.println();
							}

						} else {
							theParsedDefinition.getContainingErrors().add(
									"ERROR 1234 @line ~193");
							System.out.println("ERROR 1234 @line ~193");
						}
					}

					System.out.println();

				}
			} else {
				// ARRAY TYPE
				System.out.println("\tARRAY:   "
						+ definedElement.getQName().getLocalPart());
				if (definedElement.getRefType() != null) {
					System.out.println(definedElement.getRefType().getQName()
							.getLocalPart());// Type tou CO
					// co.setObjectType(definedElement.getRefType().getQName().getLocalPart()+"[]");
					System.out
							.println(definedElement.getRefType().isBaseType());// apo
																				// dw
																				// tha
																				// katalavw
																				// an
																				// einai
																				// nativeObject
																				// i
																				// Complex
					// co.setAdditionalInfo(definedType.getRefType().getQName().getNamespaceURI());//Namespace
					// tou CO
					// co.setNamespaceURI(definedElement.getRefType().getQName().getNamespaceURI());
					co.setObjectType(new QName(definedElement.getRefType()
							.getQName().getNamespaceURI(), definedElement
							.getRefType().getQName().getLocalPart()
							+ "[]", definedElement.getRefType().getQName()
							.getPrefix()));

					if (definedElement.getRefType().isBaseType()) {
						// NativeObject
					} else {
						// ComplexObject
						if (definedElement
								.getRefType()
								.getClass()
								.getName()
								.contains(
										"org.apache.axis.wsdl.symbolTable.DefinedType")) {
							DefinedType containedElementType = (DefinedType) definedElement
									.getRefType();
							parseComplexType(containedElementType,
									theSymbolTable, co, theParsedDefinition);
							System.out.println();
						} else if (definedElement
								.getRefType()
								.getClass()
								.getName()
								.contains(
										"org.apache.axis.wsdl.symbolTable.UndefinedType")) {
							org.apache.axis.wsdl.symbolTable.UndefinedType containedElementType = (org.apache.axis.wsdl.symbolTable.UndefinedType) definedElement
									.getRefType();
							System.out.println("WWW "
									+ containedElementType.getName());
							// parseComplexType(containedElementType,
							// theSymbolTable, co);
							System.out.println();
						} else {
							System.out
									.println("ERROR UnDefined TYPE...???? @line ~228");
							theParsedDefinition.getContainingErrors().add(
									"ERROR UnDefined TYPE...???? @line ~228");
						}
					}

				} else {
					theParsedDefinition.getContainingErrors().add(
							"ERROR 1234 @line ~233");
					System.out.println("ERROR 1234 @line ~233");
				}
			}

		} else if (definedElement.getRefType() != null) {
			if (definedElement.getRefType().getClass().getName()
					.contains("BaseType")) {
				// co.setObjectType(definedElement.getRefType().getQName().getLocalPart());
				// co.setNamespaceURI(definedElement.getRefType().getQName().getNamespaceURI());
				co.setObjectType(definedElement.getRefType().getQName());

			} else if (definedElement.getRefType().getClass().getName()
					.contains("DefinedType")) {
				System.out.println("WWW2");
				System.out.println("ERROR @line ~245");
				theParsedDefinition.getContainingErrors().add(
						"ERROR 1234 @line ~245");
				// System.exit(-43);
			}
		}
	}

	private static void parseComplexType(DefinedType definedType,
			SymbolTable theSymbolTable, ComplexObject co,
			ParsedWSDLDefinition theParsedDefinition) {
		if (definedType == null) {
			return;
		}
		System.out.println("\n\n######   PARSING COMPLEX TYPE: "
				+ definedType.getQName().getLocalPart() + "    ######");
		// System.out.println(definedType.getContainedElements().size());

		System.out.println(co.getObjectName());

		if (parsedComplexObjectsHashmap.containsKey(definedType.getQName())) {
			ComplexObject co1 = (ComplexObject) (parsedComplexObjectsHashmap
					.get(definedType.getQName()));
			/*
			 * if(co.getObjectName()!=null){ QName coName=new
			 * QName(co.objectName
			 * .getNamespaceURI(),co.objectName.getLocalPart()
			 * ,co.objectName.getPrefix());
			 * 
			 * co=co1.cloneTheCO(); co.setObjectName(coName); }else{
			 * co=co1.cloneTheCO(); }
			 */

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

			/*
			 * for(int i=0;i<co1.getHasExtendedObjects().size();i++){
			 * co.getHasExtendedObjects
			 * ().add(((ComplexObject)co1.getHasExtendedObjects
			 * ().get(i)).cloneTheCO()); }
			 * 
			 * for(int i=0;i<co1.getHasComplexObjects().size();i++){
			 * co.getHasComplexObjects
			 * ().add(((ComplexObject)co1.getHasComplexObjects
			 * ().get(i)).cloneTheCO()); }
			 * 
			 * for(int i=0;i<co1.getHasNativeObjects().size();i++){
			 * co.getHasNativeObjects
			 * ().add(((NativeObject)co1.getHasNativeObjects
			 * ().get(i)).cloneTheNO()); }
			 */

			return;
		} else {
			parsedComplexObjectsHashmap.put(definedType.getQName(), co);
		}

		if (definedType.getBaseType() != null) {
			System.out.println();
		}
		TypeEntry set = definedType.getComplexTypeExtensionBase(theSymbolTable);
		if (set != null) {
			System.out.println();
			Vector vec = set.getContainedElements();

			for (int ii = 0; ii < vec.size(); ii++) {
				org.apache.axis.wsdl.symbolTable.ElementDecl containedElement = (org.apache.axis.wsdl.symbolTable.ElementDecl) vec
						.get(ii);
				if (containedElement.getMaxOccursIsUnbounded()
						&& definedType.getContainedElements().size() == 1) {
					// Array Type
					System.out.println("\tARRAY:   "
							+ containedElement.getQName().getLocalPart());
					if (containedElement.getType() != null
							&& containedElement.getType().getRefType() != null) {
						System.out.println(containedElement.getType()
								.getQName().getLocalPart());// Type tou CO

						if (containedElement.getType().getQName()
								.getLocalPart() != null
								&& containedElement.getType().getQName()
										.getLocalPart().contains("[")) {
							co.setObjectType(containedElement.getType()
									.getQName());
						} else {
							co.setObjectType(new QName(containedElement
									.getType().getQName().getNamespaceURI(),
									containedElement.getType().getQName()
											.getLocalPart()
											+ "[]", containedElement.getType()
											.getQName().getPrefix()));
						}
						System.out.println(containedElement.getType()
								.getRefType().isBaseType());// apo dw tha
															// katalavw an einai
															// nativeObject i
															// Complex
						// co.setAdditionalInfo(containedElement.getType().getRefType().getQName().getNamespaceURI());//Namespace
						// tou CO
						// co.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());

						if (containedElement.getType().getRefType()
								.isBaseType()) {
							// NativeObject
							NativeObject no1 = new NativeObject();
							// no1.objectName="elementOfArray";
							no1.setObjectType(containedElement.getType()
									.getRefType().getQName());
							// no1.setNamespaceURI(containedElement.getType().getRefType().getQName().getNamespaceURI());
							no1.setObjectName(no1.getObjectType());
							co.getHasNativeObjects().add(no1);
							co.setIsArrayType(true);
						} else {
							// ComplexObject
							ComplexObject co1 = new ComplexObject();
							// co1.objectName="elementOfArray";
							DefinedType containedElementType = (DefinedType) containedElement
									.getType().getRefType();
							co1.setObjectType(containedElementType.getQName());

							// co1.setNamespaceURI(containedElementType.getQName().getNamespaceURI());
							parseComplexType(containedElementType,
									theSymbolTable, co1, theParsedDefinition);
							co1.setObjectName(co1.getObjectType());
							co.getHasComplexObjects().add(co1);
							co.setIsArrayType(true);
							System.out.println();
						}
						System.out.println();

					} else {
						System.out.println("ERROR 1234!!! @line ~299");
						theParsedDefinition.getContainingErrors().add(
								"ERROR 1234!!! @line ~299");
					}

				} else {

					System.out.println("\tNOT ARRAY:    "
							+ containedElement.getQName().getLocalPart());
					// Gia kathe containedElement ftiaxnw kainourgio
					// ComplexObject i NO kai to vazw mesa sto CO
					if (containedElement.getType() != null) {
						if (containedElement.getType().isBaseType()) {
							System.out.println(containedElement.getType()
									.getClass().getName());
							// NativeObject
							NativeObject no1 = new NativeObject();
							no1.setObjectName(new QName(containedElement
									.getQName().getNamespaceURI(),
									containedElement
											.getQName()
											.getLocalPart()
											.substring(
													containedElement.getQName()
															.getLocalPart()
															.indexOf(">") + 1),
									containedElement.getQName().getPrefix()));
							System.out.println(containedElement.getType()
									.getQName().getLocalPart());// Type tou CO1
							// no1.setObjectType(containedElement.getType().getQName().getLocalPart());
							System.out.println(containedElement.getType()
									.isBaseType());// apo dw tha katalavw an
													// einai nativeObject i
													// Complex
							// no1.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());
							no1.setObjectType(containedElement.getType()
									.getQName());
							if (containedElement.getMinOccursIs0()
									|| containedElement.getNillable()
									|| containedElement.getOptional()) {
								no1.setIsOptional(true);
							}
							// no1.isOptional=containedElement.getMinOccursIs0();

							// no1.setAdditionalInfo(containedElement.getType().getQName().getNamespaceURI());//Namespace
							// tou CO1
							Node n1 = definedType.getNode();

							/*
							 * if(definedType.getQName().getLocalPart().equals(
							 * "GeoPlaceType")){ System.out.println(""); }
							 */

							// try to find allowed values
							if (n1.getChildNodes() != null) {
								System.out.println(n1.getChildNodes()
										.getLength());
								for (int i = 0; i < n1.getChildNodes()
										.getLength(); i++) {
									Node childNode = n1.getChildNodes().item(i);
									System.out.println(childNode.getNodeName());
									if (childNode.getNodeName() != null
											&& childNode.getNodeName()
													.contains("restriction")) {
										if (childNode.getChildNodes() != null) {
											System.out.println(childNode
													.getChildNodes()
													.getLength());
											for (int i1 = 0; i1 < childNode
													.getChildNodes()
													.getLength(); i1++) {
												Node restrictionNode = childNode
														.getChildNodes().item(
																i1);
												if (restrictionNode != null
														&& restrictionNode
																.getNodeName() != null
														&& restrictionNode
																.getNodeName()
																.contains(
																		"enumeration")) {
													if (restrictionNode
															.getAttributes() != null
															&& restrictionNode
																	.getAttributes()
																	.getNamedItem(
																			"value") != null) {
														System.out
																.println(restrictionNode
																		.getAttributes()
																		.getNamedItem(
																				"value"));
														no1.getHasAllowedValues()
																.add(restrictionNode
																		.getAttributes()
																		.getNamedItem(
																				"value"));
													}
												}
											}
										}
									}
								}
							}

							// DefinedType
							// containedElementType=(DefinedType)containedElement.getType();
							// parseComplexType(containedElementType,
							// theSymbolTable, co1);
							co.getHasNativeObjects().add(no1);

						} else {
							// ComplexObject

							ComplexObject co1 = new ComplexObject();
							co1.setObjectName(new QName(containedElement
									.getQName().getNamespaceURI(),
									containedElement
											.getQName()
											.getLocalPart()
											.substring(
													containedElement.getQName()
															.getLocalPart()
															.indexOf(">") + 1),
									containedElement.getQName().getPrefix()));
							// co1.setObjectName(containedElement.getQName().getLocalPart().substring(
							// containedElement.getQName().getLocalPart().indexOf(">")+1));
							System.out.println(containedElement.getType()
									.getQName().getLocalPart());// Type tou CO1
							// co1.setObjectType(containedElement.getType().getQName().getLocalPart());
							System.out.println(containedElement.getType()
									.isBaseType());// apo dw tha katalavw an
													// einai nativeObject i
													// Complex
							// co1.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());
							// co1.setAdditionalInfo(containedElement.getType().getQName().getNamespaceURI());//Namespace
							// tou CO1
							co1.setObjectType(containedElement.getType()
									.getQName());

							if (containedElement.getMinOccursIs0()
									|| containedElement.getNillable()
									|| containedElement.getOptional()) {
								co1.setIsOptional(true);
							}
							// co1.isOptional=containedElement.getMinOccursIs0();

							DefinedType containedElementType = (DefinedType) containedElement
									.getType();
							parseComplexType(containedElementType,
									theSymbolTable, co1, theParsedDefinition);
							co.getHasComplexObjects().add(co1);
							System.out.println();
						}

					} else {
						System.out.println("ERROR 12345678910!!!");
						theParsedDefinition.getContainingErrors().add(
								"ERROR 12345678910!!! @line ~341");
					}
				}

			}
		}

		if (definedType.getContainedElements() != null) {
			if (definedType.getContainedElements().size() > 0) {
				Iterator containedElementsIterator = definedType
						.getContainedElements().iterator();
				// System.out.println("AAA"+definedType.getContainedElements().size());

				while (containedElementsIterator.hasNext()) {
					// Parse ta contained Elements tou complexType
					// Ta contained elements einai ta native i complex objects
					// pou periexei to complex type
					org.apache.axis.wsdl.symbolTable.ElementDecl containedElement = (org.apache.axis.wsdl.symbolTable.ElementDecl) containedElementsIterator
							.next();
					System.out.println();
					System.out.println("\tcontainedElement isBaseType: "
							+ containedElement.getType().isBaseType());
					System.out.println(containedElement.getClass().getName());

					if (containedElement.getMaxOccursIsUnbounded()
							&& definedType.getContainedElements().size() == 1) {
						// Array Type
						System.out.println("\tARRAY:   "
								+ containedElement.getQName().getLocalPart());
						if (containedElement.getType() != null
								&& containedElement.getType().getRefType() != null) {
							System.out.println(containedElement.getType()
									.getQName().getLocalPart());// Type tou CO

							if (containedElement.getType().getQName()
									.getLocalPart() != null
									&& containedElement.getType().getQName()
											.getLocalPart().contains("[")) {
								co.setObjectType(containedElement.getType()
										.getQName());
							} else {
								co.setObjectType(new QName(
										containedElement.getType().getQName()
												.getNamespaceURI(),
										containedElement.getType().getQName()
												.getLocalPart()
												+ "[]", containedElement
												.getType().getQName()
												.getPrefix()));
							}
							System.out.println(containedElement.getType()
									.getRefType().isBaseType());// apo dw tha
																// katalavw an
																// einai
																// nativeObject
																// i Complex
							// co.setAdditionalInfo(containedElement.getType().getRefType().getQName().getNamespaceURI());//Namespace
							// tou CO
							// co.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());

							if (containedElement.getType().getRefType()
									.isBaseType()) {
								// NativeObject
								NativeObject no1 = new NativeObject();
								// no1.objectName="elementOfArray";
								no1.setObjectType(containedElement.getType()
										.getRefType().getQName());
								// no1.setNamespaceURI(containedElement.getType().getRefType().getQName().getNamespaceURI());
								no1.setObjectName(no1.getObjectType());
								co.getHasNativeObjects().add(no1);
								co.setIsArrayType(true);
							} else {
								// ComplexObject
								ComplexObject co1 = new ComplexObject();
								// co1.objectName="elementOfArray";
								DefinedType containedElementType = (DefinedType) containedElement
										.getType().getRefType();
								co1.setObjectType(containedElementType
										.getQName());

								// co1.setNamespaceURI(containedElementType.getQName().getNamespaceURI());
								parseComplexType(containedElementType,
										theSymbolTable, co1,
										theParsedDefinition);
								co1.setObjectName(co1.getObjectType());
								co.getHasComplexObjects().add(co1);
								co.setIsArrayType(true);
								System.out.println();
							}
							System.out.println();

						} else {
							System.out.println("ERROR 1234!!! @line ~299");
							theParsedDefinition.getContainingErrors().add(
									"ERROR 1234!!! @line ~299");
						}

					} else {

						System.out.println("\tNOT ARRAY:    "
								+ containedElement.getQName().getLocalPart());
						// Gia kathe containedElement ftiaxnw kainourgio
						// ComplexObject i NO kai to vazw mesa sto CO
						if (containedElement.getType() != null) {
							if (containedElement.getType().isBaseType()) {
								System.out.println(containedElement.getType()
										.getClass().getName());
								// NativeObject
								NativeObject no1 = new NativeObject();
								no1.setObjectName(new QName(
										containedElement.getQName()
												.getNamespaceURI(),
										containedElement
												.getQName()
												.getLocalPart()
												.substring(
														containedElement
																.getQName()
																.getLocalPart()
																.indexOf(">") + 1),
										containedElement.getQName().getPrefix()));
								System.out.println(containedElement.getType()
										.getQName().getLocalPart());// Type tou
																	// CO1
								// no1.setObjectType(containedElement.getType().getQName().getLocalPart());
								System.out.println(containedElement.getType()
										.isBaseType());// apo dw tha katalavw an
														// einai nativeObject i
														// Complex
								// no1.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());
								no1.setObjectType(containedElement.getType()
										.getQName());
								if (containedElement.getMinOccursIs0()
										|| containedElement.getNillable()
										|| containedElement.getOptional()) {
									no1.setIsOptional(true);
								}
								// no1.isOptional=containedElement.getMinOccursIs0();

								// no1.setAdditionalInfo(containedElement.getType().getQName().getNamespaceURI());//Namespace
								// tou CO1
								Node n1 = definedType.getNode();

								/*
								 * if(definedType.getQName().getLocalPart().equals
								 * ("GeoPlaceType")){ System.out.println(""); }
								 */

								// try to find allowed values
								if (n1.getChildNodes() != null) {
									System.out.println(n1.getChildNodes()
											.getLength());
									for (int i = 0; i < n1.getChildNodes()
											.getLength(); i++) {
										Node childNode = n1.getChildNodes()
												.item(i);
										System.out.println(childNode
												.getNodeName());
										if (childNode.getNodeName() != null
												&& childNode
														.getNodeName()
														.contains("restriction")) {
											if (childNode.getChildNodes() != null) {
												System.out.println(childNode
														.getChildNodes()
														.getLength());
												for (int i1 = 0; i1 < childNode
														.getChildNodes()
														.getLength(); i1++) {
													Node restrictionNode = childNode
															.getChildNodes()
															.item(i1);
													if (restrictionNode != null
															&& restrictionNode
																	.getNodeName() != null
															&& restrictionNode
																	.getNodeName()
																	.contains(
																			"enumeration")) {
														if (restrictionNode
																.getAttributes() != null
																&& restrictionNode
																		.getAttributes()
																		.getNamedItem(
																				"value") != null) {
															System.out
																	.println(restrictionNode
																			.getAttributes()
																			.getNamedItem(
																					"value"));
															no1.getHasAllowedValues()
																	.add(restrictionNode
																			.getAttributes()
																			.getNamedItem(
																					"value"));
														}
													}
												}
											}
										}
									}
								}

								// DefinedType
								// containedElementType=(DefinedType)containedElement.getType();
								// parseComplexType(containedElementType,
								// theSymbolTable, co1);
								co.getHasNativeObjects().add(no1);

							} else {
								// ComplexObject

								ComplexObject co1 = new ComplexObject();
								co1.setObjectName(new QName(
										containedElement.getQName()
												.getNamespaceURI(),
										containedElement
												.getQName()
												.getLocalPart()
												.substring(
														containedElement
																.getQName()
																.getLocalPart()
																.indexOf(">") + 1),
										containedElement.getQName().getPrefix()));
								// co1.setObjectName(containedElement.getQName().getLocalPart().substring(
								// containedElement.getQName().getLocalPart().indexOf(">")+1));
								System.out.println(containedElement.getType()
										.getQName().getLocalPart());// Type tou
																	// CO1
								// co1.setObjectType(containedElement.getType().getQName().getLocalPart());
								System.out.println(containedElement.getType()
										.isBaseType());// apo dw tha katalavw an
														// einai nativeObject i
														// Complex
								// co1.setNamespaceURI(containedElement.getType().getQName().getNamespaceURI());
								// co1.setAdditionalInfo(containedElement.getType().getQName().getNamespaceURI());//Namespace
								// tou CO1
								co1.setObjectType(containedElement.getType()
										.getQName());

								if (containedElement.getMinOccursIs0()
										|| containedElement.getNillable()
										|| containedElement.getOptional()) {
									co1.setIsOptional(true);
								}
								// co1.isOptional=containedElement.getMinOccursIs0();

								DefinedType containedElementType = (DefinedType) containedElement
										.getType();
								parseComplexType(containedElementType,
										theSymbolTable, co1,
										theParsedDefinition);
								co.getHasComplexObjects().add(co1);
								System.out.println();
							}

						} else {
							System.out.println("ERROR 12345678910!!!");
							theParsedDefinition.getContainingErrors().add(
									"ERROR 12345678910!!! @line ~341");
						}
					}

					System.out.println();

				}
			} else {
				// ARRAY TYPE
				System.out.println("\tARRAY:   "
						+ definedType.getQName().getLocalPart());
				if (definedType.getRefType() != null) {

					System.out.println(definedType.getRefType().getQName()
							.getLocalPart());// Type tou CO
					co.setObjectType(new QName(definedType.getRefType()
							.getQName().getNamespaceURI(), definedType
							.getRefType().getQName().getLocalPart()
							+ "[]", definedType.getRefType().getQName()
							.getPrefix()));
					System.out.println(definedType.getRefType().isBaseType());// apo
																				// dw
																				// tha
																				// katalavw
																				// an
																				// einai
																				// nativeObject
																				// i
																				// Complex
					// co.setAdditionalInfo(definedType.getRefType().getQName().getNamespaceURI());//Namespace
					// tou CO
					// co.setNamespaceURI(definedType.getRefType().getQName().getNamespaceURI());

					if (definedType.getRefType().isBaseType()) {
						// NativeObject
						NativeObject no1 = new NativeObject();
						// no1.objectName="elementOfArray";
						no1.setObjectType(definedType.getRefType().getQName());
						// no1.setNamespaceURI(definedType.getRefType().getQName().getNamespaceURI());
						no1.setObjectName(no1.getObjectType());
						co.getHasNativeObjects().add(no1);
						co.setIsArrayType(true);

					} else {
						// ComplexObject
						if (definedType
								.getRefType()
								.getClass()
								.getName()
								.contains(
										"org.apache.axis.wsdl.symbolTable.DefinedType")) {
							ComplexObject co1 = new ComplexObject();
							DefinedType containedElementType = (DefinedType) definedType
									.getRefType();
							co1.setObjectType(containedElementType.getQName());

							// co1.setNamespaceURI(containedElementType.getQName().getNamespaceURI());
							// co1.setObjectName("elementOfArray");
							parseComplexType(containedElementType,
									theSymbolTable, co1, theParsedDefinition);
							co1.setObjectName(co1.getObjectType());
							co.getHasComplexObjects().add(co1);
							co.setIsArrayType(true);
							System.out.println();
						} else if (definedType
								.getRefType()
								.getClass()
								.getName()
								.contains(
										"org.apache.axis.wsdl.symbolTable.UndefinedType")) {
							org.apache.axis.wsdl.symbolTable.UndefinedType containedElementType = (org.apache.axis.wsdl.symbolTable.UndefinedType) definedType
									.getRefType();
							System.out.println("WWW "
									+ containedElementType.getName());
							// parseComplexType(containedElementType,
							// theSymbolTable, co);
							System.out.println();
						} else {
							System.out
									.println("ERROR UnDefined TYPE...???? @line ~376");
							theParsedDefinition.getContainingErrors().add(
									"ERROR UnDefined TYPE...???? @line ~376");
						}
					}
				} else {
					// theParsedDefinition.getContainingErrors().add("ERROR 123456!!! @line ~381");
					Type t = theSymbolTable.getType(definedType.getQName());
					if (t != null && t.getNode() != null
							&& t.getNode().getAttributes() != null) {
						Node isAbstractNode = t.getNode().getAttributes()
								.getNamedItem("abstract");
						if (isAbstractNode != null
								&& isAbstractNode.getNodeValue() != null
								&& isAbstractNode.getNodeValue()
										.equalsIgnoreCase("true")) {
							System.out.println("ABSTRACT TYPE!!!");
							co.setIsAbstract(true);
						}
					}
				}
			}

		}

		parsedComplexObjectsHashmap.put(definedType.getQName(), co);
	}

	private static void parseSimpleType(Type t, SymbolTable theSymbolTable,
			NativeObject no) {
		no.setObjectType(t.getQName());
		// no.setNamespaceURI(t.getQName().getNamespaceURI());
	}

	private static void parseTheWSDLOperations(SymbolTable theSymbolTable,
			Definition theAxisDefinition,
			ParsedWSDLDefinition theParsedDefinition) {
		if (theAxisDefinition == null) {
			return;
		}

		Map portTypes = theAxisDefinition.getAllPortTypes();

		Collection portTypesCol = portTypes.values();
		Iterator portTypesIterator = portTypesCol.iterator();

		while (portTypesIterator.hasNext()) {
			System.out.println("\n### Port Type ###");
			PortType portType = (PortType) portTypesIterator.next();
			System.out.println("Port Type name: "
					+ portType.getQName().toString());// +"     class: "+p.getBinding().getPortType().getQName().toString());//+"   binding:"+p.getBinding());

			if (portType.getQName().getLocalPart().contains("Http")
					|| portType.getQName().getLocalPart().contains("HTTP")) {
				continue;
			}

			List operationsList = portType.getOperations();
			if (operationsList != null) {
				Iterator operationsIter = operationsList.iterator();
				while (operationsIter.hasNext()) {
					OperationImpl operation = (OperationImpl) operationsIter
							.next();
					System.out.println(operation.getName());

					WSOperation mitsosOperation = new WSOperation();
					mitsosOperation.setOperationName(operation.getName());
					mitsosOperation.setBelongsToDefinition(theParsedDefinition);

					WSOperationInput mitsosOperationInput = new WSOperationInput();

					// GET THE SOAP ACTION URL
					Map bindingsMap = theAxisDefinition.getAllBindings();
					Collection bindingsCol = bindingsMap.values();
					Iterator bindingsIter = bindingsCol.iterator();
					while (bindingsIter.hasNext()) {
						com.ibm.wsdl.BindingImpl binding = (com.ibm.wsdl.BindingImpl) bindingsIter
								.next();

						if (binding.getQName().getLocalPart().contains("Http")
								|| binding.getQName().getLocalPart()
										.contains("HTTP")) {
							continue;
						}

						List bindingOperations = binding.getBindingOperations();
						Iterator bindingOperationsIter = bindingOperations
								.iterator();
						while (bindingOperationsIter.hasNext()) {
							com.ibm.wsdl.BindingOperationImpl bindingOperation = (com.ibm.wsdl.BindingOperationImpl) bindingOperationsIter
									.next();
							if (bindingOperation != null
									&& bindingOperation.getOperation() != null
									&& bindingOperation.getOperation().equals(
											operation)) {
								List extElementsList = bindingOperation
										.getExtensibilityElements();
								Iterator extElementsIter = extElementsList
										.iterator();
								while (extElementsIter.hasNext()) {
									Object obj = extElementsIter.next();
									if (obj != null
											&& obj.getClass()
													.getName()
													.contains(
															"SOAPOperationImpl")) {
										try {
											com.ibm.wsdl.extensions.soap.SOAPOperationImpl extElement = (com.ibm.wsdl.extensions.soap.SOAPOperationImpl) obj;
											mitsosOperation
													.setHasBindingSoapAction(extElement
															.getSoapActionURI());
											// System.out.println(extElement.getElementType());
											mitsosOperation
													.setHasStyle(extElement
															.getStyle());
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else if (obj != null
											&& obj.getClass()
													.getName()
													.contains(
															"SOAPOperationImpl")) {
										try {
											com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl extElement = (com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl) obj;
											mitsosOperation
													.setHasBindingSoapAction(extElement
															.getSoapActionURI());
											// System.out.println(extElement.getElementType());
											mitsosOperation
													.setHasStyle(extElement
															.getStyle());
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										System.out.println();
									}

								}
								com.ibm.wsdl.BindingInputImpl bindingInput = (com.ibm.wsdl.BindingInputImpl) bindingOperation
										.getBindingInput();
								List extElemsList = bindingInput
										.getExtensibilityElements();
								Iterator iter = extElemsList.iterator();
								while (iter.hasNext()) {
									Object obj = iter.next();
									if (obj instanceof com.ibm.wsdl.extensions.soap.SOAPHeaderImpl) {
										com.ibm.wsdl.extensions.soap.SOAPHeaderImpl sh = (com.ibm.wsdl.extensions.soap.SOAPHeaderImpl) obj;

										// to message exei mesa to type pou
										// prepei na vrw edw kai na valw sto
										// ComplexObject tou Header
										// QName partQName=new
										// QName(sh.getPart(),
										// sh.getMessage().getNamespaceURI());

										com.ibm.wsdl.MessageImpl msg = (com.ibm.wsdl.MessageImpl) theAxisDefinition
												.getMessage(sh.getMessage());
										if (msg.getParts() != null
												&& msg.getParts().values() != null) {
											Iterator iter1 = msg.getParts()
													.values().iterator();
											while (iter1.hasNext()) {
												com.ibm.wsdl.PartImpl partImpl = (com.ibm.wsdl.PartImpl) iter1
														.next();

												Type typeGotFromTheSymbolTable = theSymbolTable
														.getType(partImpl
																.getTypeName());
												if (typeGotFromTheSymbolTable != null) {

													Object nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
															typeGotFromTheSymbolTable,
															theSymbolTable,
															sh.getMessage(),
															theParsedDefinition);
													if (nativeOrComplexObject != null) {
														if (nativeOrComplexObject
																.getClass()
																.getName()
																.contains(
																		"NativeObject")) {
															NativeObject no = (NativeObject) nativeOrComplexObject;
															if (sh.getPart() != null
																	&& sh.getPart()
																			.length() > 0) {
																no.setObjectName(new QName(
																		sh.getPart()));
															}
															if (sh.getRequired() != null
																	&& sh.getRequired() == false) {
																no.setIsOptional(true);
															}
															mitsosOperationInput
																	.getHasSoapHeaders()
																	.add(no);
														} else if (nativeOrComplexObject
																.getClass()
																.getName()
																.contains(
																		"ComplexObject")) {
															ComplexObject co = (ComplexObject) nativeOrComplexObject;
															if (sh.getPart() != null
																	&& sh.getPart()
																			.length() > 0) {
																co.setObjectName(new QName(
																		sh.getPart()));
															}
															if (sh.getRequired() != null
																	&& sh.getRequired() == false) {
																co.setIsOptional(true);
															}
															mitsosOperationInput
																	.getHasSoapHeaders()
																	.add(co);
														}

													}

												} else {
													Element elementGotFromTheSymbolTable = theSymbolTable
															.getElement(sh
																	.getMessage());
													if (elementGotFromTheSymbolTable != null) {
														Object nativeOrComplexObject = parseElementGotFromTheSymbolTable(
																elementGotFromTheSymbolTable,
																theSymbolTable,
																sh.getMessage(),
																theParsedDefinition);
														if (nativeOrComplexObject != null) {
															if (nativeOrComplexObject
																	.getClass()
																	.getName()
																	.contains(
																			"NativeObject")) {
																NativeObject no = (NativeObject) nativeOrComplexObject;
																if (sh.getPart() != null
																		&& sh.getPart()
																				.length() > 0) {
																	no.setObjectName(new QName(
																			sh.getPart()));
																}
																if (sh.getRequired() != null
																		&& sh.getRequired() == false) {
																	no.setIsOptional(true);
																}
																mitsosOperationInput
																		.getHasSoapHeaders()
																		.add(no);
															} else if (nativeOrComplexObject
																	.getClass()
																	.getName()
																	.contains(
																			"ComplexObject")) {
																ComplexObject co = (ComplexObject) nativeOrComplexObject;
																if (sh.getPart() != null
																		&& sh.getPart()
																				.length() > 0) {
																	co.setObjectName(new QName(
																			sh.getPart()));
																}
																if (sh.getRequired() != null
																		&& sh.getRequired() == false) {
																	co.setIsOptional(true);
																}
																mitsosOperationInput
																		.getHasSoapHeaders()
																		.add(co);
															}
														}
													} else {
														System.out
																.println("ERROR @line ~589!!!");
														theParsedDefinition
																.getContainingErrors()
																.add("ERROR @line ~589!!!");
													}
												}
												// partImpl.getTypeName()
												System.out.println();
											}
										}

									}
								}
								System.out
										.println(bindingOperation
												.getBindingInput().getClass()
												.getName());
							}
						}

					}
					// mitsosOperation.hasBindingSoapAction=operation.get
					// mitsosOperation.setHasStyle(hasStyle)

					InputImpl operationInput = (InputImpl) operation.getInput();
					OutputImpl operationOutput = (OutputImpl) operation
							.getOutput();

					System.out.println(operationInput.getMessage().getQName()
							.toString()
							+ "    "
							+ operationInput.getMessage().getParts().size());
					System.out.println(operationInput.getMessage().getClass()
							.getName());
					System.out
							.println("\t"
									+ operationInput.getMessage().getParts()
											.toString());

					MessageImpl inputMessageOfOperation = (MessageImpl) operationInput
							.getMessage();

					if (inputMessageOfOperation.getParts() != null
							&& inputMessageOfOperation.getParts().size() > 0) {
						Map inputPartsMap = inputMessageOfOperation.getParts();
						Collection inputPartsCol = inputPartsMap.values();
						if (inputPartsCol != null) {
							Iterator inputPartsIterator = inputPartsCol
									.iterator();
							while (inputPartsIterator.hasNext()) {
								com.ibm.wsdl.PartImpl inputPart = (com.ibm.wsdl.PartImpl) inputPartsIterator
										.next();

								if (inputPart.getTypeName() != null) {
									Type typeGotFromTheSymbolTable = theSymbolTable
											.getType(inputPart.getTypeName());

									if (typeGotFromTheSymbolTable != null) {
										Object nativeOrComplexObject = null;
										if (inputPart.getElementName() != null) {
											nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
													typeGotFromTheSymbolTable,
													theSymbolTable,
													inputPart.getElementName(),
													theParsedDefinition);
										} else if (inputPart.getName() != null) {
											nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
													typeGotFromTheSymbolTable,
													theSymbolTable,
													new QName(inputPart
															.getName()),
													theParsedDefinition);
										} else {
											nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
													typeGotFromTheSymbolTable,
													theSymbolTable,
													inputPart.getTypeName(),
													theParsedDefinition);
										}
										if (nativeOrComplexObject != null) {
											mitsosOperationInput
													.getHasNativeOrComplexObjects()
													.add(nativeOrComplexObject);
										}

									} else {
										/*
										 * Element elem =
										 * theSymbolTable.getElement
										 * (inputPart.getElementName());
										 * System.out
										 * .println(elem.getQName().toString());
										 * QName
										 * elemName=inputPart.getElementName();
										 */
										System.out
												.println("ERROR @line ~283!!!");
										theParsedDefinition
												.getContainingErrors().add(
														"ERROR @line ~447!!!");
										// document literal???
									}

								} else if (inputPart.getElementName() != null) {
									// LITERAL TYPE!!!
									Type typeGotFromTheSymbolTable = theSymbolTable
											.getType(inputPart.getElementName());

									if (typeGotFromTheSymbolTable != null) {

										Object nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
												typeGotFromTheSymbolTable,
												theSymbolTable,
												inputPart.getElementName(),
												theParsedDefinition);
										if (nativeOrComplexObject != null) {
											mitsosOperationInput
													.getHasNativeOrComplexObjects()
													.add(nativeOrComplexObject);
										}

									} else {
										Element elementGotFromTheSymbolTable = theSymbolTable
												.getElement(inputPart
														.getElementName());
										if (elementGotFromTheSymbolTable != null) {
											Object nativeOrComplexObject = parseElementGotFromTheSymbolTable(
													elementGotFromTheSymbolTable,
													theSymbolTable,
													inputPart.getElementName(),
													theParsedDefinition);
											if (nativeOrComplexObject != null) {
												mitsosOperationInput
														.getHasNativeOrComplexObjects()
														.add(nativeOrComplexObject);
											}
										} else {
											System.out
													.println("ERROR @line ~471!!!");
											theParsedDefinition
													.getContainingErrors()
													.add("ERROR @line ~471!!!");
										}
									}
								}

							}

						}
					} else {
					}

					mitsosOperation.setHasInput(mitsosOperationInput);

					// Parse the Operation Outputs
					MessageImpl outputMessageOfOperation = (MessageImpl) operationOutput
							.getMessage();
					WSOperationOutput mitsosOperationOutput = new WSOperationOutput();

					if (outputMessageOfOperation.getParts() != null
							&& outputMessageOfOperation.getParts().size() > 0) {
						Map outputPartsMap = outputMessageOfOperation
								.getParts();
						Collection outputPartsCol = outputPartsMap.values();
						if (outputPartsCol != null) {
							Iterator outputPartsIterator = outputPartsCol
									.iterator();
							while (outputPartsIterator.hasNext()) {
								com.ibm.wsdl.PartImpl outputPart = (com.ibm.wsdl.PartImpl) outputPartsIterator
										.next();
								// System.out.println("\t\t"+outputPart.getName()+"   "+outputPart.getTypeName().toString());

								if (outputPart.getTypeName() != null) {
									Type typeGotFromTheSymbolTable = theSymbolTable
											.getType(outputPart.getTypeName());

									if (typeGotFromTheSymbolTable != null) {

										Object nativeOrComplexObject = null;
										if (outputPart.getElementName() != null) {
											nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
													typeGotFromTheSymbolTable,
													theSymbolTable,
													outputPart.getElementName(),
													theParsedDefinition);
										} else if (outputPart.getName() != null) {
											nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
													typeGotFromTheSymbolTable,
													theSymbolTable,
													new QName(outputPart
															.getName()),
													theParsedDefinition);
										} else {
											nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
													typeGotFromTheSymbolTable,
													theSymbolTable,
													outputPart.getTypeName(),
													theParsedDefinition);
										}

										if (nativeOrComplexObject != null) {
											mitsosOperationOutput
													.getHasNativeOrComplexObjects()
													.add(nativeOrComplexObject);
										}
									} else {
										/*
										 * Element elem =
										 * theSymbolTable.getElement
										 * (inputPart.getElementName());
										 * System.out
										 * .println(elem.getQName().toString());
										 * QName
										 * elemName=inputPart.getElementName();
										 */
										System.out
												.println("ERROR @line ~514!!!");
										theParsedDefinition
												.getContainingErrors().add(
														"ERROR @line ~514!!!");
										// document literal???
									}
								} else if (outputPart.getElementName() != null) {
									// LITERAL TYPE!!!
									Type typeGotFromTheSymbolTable = theSymbolTable
											.getType(outputPart
													.getElementName());

									if (typeGotFromTheSymbolTable != null) {

										Object nativeOrComplexObject = parseTypeGotFromTheSymbolTable(
												typeGotFromTheSymbolTable,
												theSymbolTable,
												outputPart.getElementName(),
												theParsedDefinition);
										if (nativeOrComplexObject != null) {
											mitsosOperationOutput
													.getHasNativeOrComplexObjects()
													.add(nativeOrComplexObject);
										}
									} else {

										Element elementGotFromTheSymbolTable = theSymbolTable
												.getElement(outputPart
														.getElementName());
										if (elementGotFromTheSymbolTable != null) {
											Object nativeOrComplexObject = parseElementGotFromTheSymbolTable(
													elementGotFromTheSymbolTable,
													theSymbolTable,
													outputPart.getElementName(),
													theParsedDefinition);
											if (nativeOrComplexObject != null) {
												mitsosOperationOutput
														.getHasNativeOrComplexObjects()
														.add(nativeOrComplexObject);
											}
										} else {
											System.out
													.println("ERROR @line ~538!!!");
											theParsedDefinition
													.getContainingErrors()
													.add("ERROR @line ~538!!!");
										}
										// document literal???
									}
								}

							}

						}
					} else {
					}
					mitsosOperation.setHasOutput(mitsosOperationOutput);
					theParsedDefinition.getWsdlOperations()
							.add(mitsosOperation);
				}
			}
		}

	}

	private static Object parseElementGotFromTheSymbolTable(
			Element elementGotFromTheSymbolTable, SymbolTable theSymbolTable,
			QName objectName, ParsedWSDLDefinition theParsedDefinition) {
		if (elementGotFromTheSymbolTable == null) {
			return null;
		}
		System.out.println(elementGotFromTheSymbolTable.getClass().getName());
		if (elementGotFromTheSymbolTable.getClass().getName()
				.contains("org.apache.axis.wsdl.symbolTable.DefinedElement")) {
			org.apache.axis.wsdl.symbolTable.DefinedElement definedElement = (org.apache.axis.wsdl.symbolTable.DefinedElement) elementGotFromTheSymbolTable;
			// ComplexObject
			ComplexObject co = new ComplexObject();
			co.setObjectName(objectName);

			co.setObjectType(definedElement.getQName());
			// co.setNamespaceURI(definedElement.getQName().getNamespaceURI());
			// co.setAdditionalInfo(definedType.getQName().getNamespaceURI());
			parseComplexElement(definedElement, theSymbolTable, co,
					theParsedDefinition);

			return co;

		} else {
			theParsedDefinition.getContainingErrors().add(
					"ERROR @ line ~ 577!!!");
			System.out.println("ERROR @ line ~ 577!!!");
		}

		return null;

	}

	private static Object parseTypeGotFromTheSymbolTable(
			Type typeGotFromTheSymbolTable, SymbolTable theSymbolTable,
			QName objectName, ParsedWSDLDefinition theParsedDefinition) {
		if (typeGotFromTheSymbolTable == null) {
			return null;
		}

		System.out.println(typeGotFromTheSymbolTable.getQName().getLocalPart());

		System.out.println("\t"
				+ typeGotFromTheSymbolTable.getClass().getName());
		if (typeGotFromTheSymbolTable.getClass().getName()
				.contains("org.apache.axis.wsdl.symbolTable.BaseType")) {
			org.apache.axis.wsdl.symbolTable.BaseType nativeType = (org.apache.axis.wsdl.symbolTable.BaseType) typeGotFromTheSymbolTable;
			if (nativeType.getQName() != null
					&& nativeType.getQName().getLocalPart().equals("Vector")) {
				// WW VECTOR!!!
				ComplexObject co = new ComplexObject();
				co.setObjectName(objectName);
				co.setObjectType(nativeType.getQName());

				co.setIsArrayType(true);
				return co;
			} else {
				// nativeObject
				NativeObject no = new NativeObject();
				no.setObjectName(objectName);
				parseSimpleType(nativeType, theSymbolTable, no);
				return no;
			}

		} else if (typeGotFromTheSymbolTable.getClass().getName()
				.contains("org.apache.axis.wsdl.symbolTable.DefinedType")) {
			// ComplexObject
			ComplexObject co = new ComplexObject();
			co.setObjectName(objectName);

			org.apache.axis.wsdl.symbolTable.DefinedType definedType = (org.apache.axis.wsdl.symbolTable.DefinedType) typeGotFromTheSymbolTable;
			co.setObjectType(definedType.getQName());

			// co.setNamespaceURI(definedType.getQName().getNamespaceURI());
			// co.setAdditionalInfo(definedType.getQName().getNamespaceURI());
			parseComplexType(definedType, theSymbolTable, co,
					theParsedDefinition);

			// if(ARRAY) transform CO to NO?

			return co;

		} else {
			System.out.println("ERROR @ line ~ 617!!!");
			theParsedDefinition.getContainingErrors().add(
					"ERROR @ line ~ 617!!!");
			// System.exit(-13);
		}
		return null;
	}

	public static void main(String[] args) {
		// parseWSDLwithAxis1("http://160.40.50.84/superMarketWS/MySMarketWS.asmx?wsdl");
		parseWSDLwithAxis1("http://www.verona.miz.it/mpk4/server?wsdl");

	}
}
