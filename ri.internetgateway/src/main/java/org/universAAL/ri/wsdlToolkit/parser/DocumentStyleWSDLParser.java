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

import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.ibm.wsdl.extensions.schema.SchemaImportImpl;
import com.ibm.wsdl.extensions.schema.SchemaReferenceImpl;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
//-import javax.swing.JTextArea;
import javax.wsdl.Definition;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;

import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationOutput;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * This class is used for parsing the documentation of a WSDL file.
 * 
 * @author kgiannou
 */


public class DocumentStyleWSDLParser {

	private static ComplexObject getExtendedTypeAsAComplexObject(
			Node theBaseNodeOfTheExtendedType, Schema theWSDLSchema) {

		if (theBaseNodeOfTheExtendedType == null || theWSDLSchema == null)
			return null;

		ComplexObject theComplexObjectRepresentingTheComplexType = new ComplexObject();
		if (theBaseNodeOfTheExtendedType.getAttributes().getNamedItem("name") != null) {

			// -System.out.println("### extension Name: "+theBaseNodeOfTheExtendedType.getAttributes().getNamedItem("name").getNodeValue());
			theComplexObjectRepresentingTheComplexType.setObjectName(new QName(
					theBaseNodeOfTheExtendedType.getAttributes()
							.getNamedItem("name").getNodeValue()));
			theComplexObjectRepresentingTheComplexType.setObjectType(new QName(
					theBaseNodeOfTheExtendedType.getAttributes()
							.getNamedItem("name").getNodeValue()));

			boolean objectExistsInHashmap = MitsosParser.parsedObjectsHashmap
					.containsKey(theBaseNodeOfTheExtendedType.getAttributes()
							.getNamedItem("name").getNodeValue());
			if (objectExistsInHashmap) {
				return (ComplexObject) MitsosParser.parsedObjectsHashmap
						.get(theComplexObjectRepresentingTheComplexType
								.getObjectType());
			}
			MitsosParser.parsedObjectsHashmap.put(
					theComplexObjectRepresentingTheComplexType.getObjectType(),
					theComplexObjectRepresentingTheComplexType);

		}
		String additionalInfo = "";
		String referencedType = "";

		for (int k1 = 0; k1 < theBaseNodeOfTheExtendedType.getAttributes()
				.getLength(); k1++) {
			Node att = theBaseNodeOfTheExtendedType.getAttributes().item(k1);
			if (att.getNodeName().equalsIgnoreCase("name")) {

			} else if (att.getNodeName().equalsIgnoreCase("ref")) {
				// -System.out.println("\tWWWWWWWWW GAMWTOOOOOOOOOOOOOOO REFERENCEEEEEEEEEEEEEEEEEEEEEEEE!!!!!!! ref:::: "+att.getNodeValue());
				referencedType = att.getNodeValue();
			} else {
				additionalInfo += att.getNodeName() + ":" + att.getNodeValue();// att.getNodeName+":"+att.getNodeValue
			}
		}

		NodeList listWithChildrenOfTheComplexTypeBaseNode = theBaseNodeOfTheExtendedType
				.getChildNodes();
		if (listWithChildrenOfTheComplexTypeBaseNode != null) {
			for (int k = 0; k < listWithChildrenOfTheComplexTypeBaseNode
					.getLength(); k++) {
				// -System.out.println("\t"+listWithChildrenOfTheComplexTypeBaseNode.item(k).getNodeName());
				if (listWithChildrenOfTheComplexTypeBaseNode.item(k)
						.getNodeName().contains("sequence")
						|| listWithChildrenOfTheComplexTypeBaseNode.item(k)
								.getNodeName().contains("choice")) {
					// -System.out.println("### parseTypeFromComplexTypeNodeOfXSD ###");
					// -System.out.println("\t\t"+theWSDLSchema.getDocumentBaseURI());
					parseSequenceOrChoiceNode(theWSDLSchema,
							listWithChildrenOfTheComplexTypeBaseNode.item(k),
							0, theComplexObjectRepresentingTheComplexType);

				} else if (listWithChildrenOfTheComplexTypeBaseNode.item(k)
						.getNodeName().contains("complexContent")) {
					parseComplexContentForDocumentType(theWSDLSchema,
							listWithChildrenOfTheComplexTypeBaseNode.item(k),
							0, theComplexObjectRepresentingTheComplexType);
				}
			}
		}
		return theComplexObjectRepresentingTheComplexType;
	}

	private static void addTypesOfExtendedTypeToTheComplexObject(
			ComplexObject co, String extendedTypeName,
			javax.wsdl.extensions.schema.Schema theWSDLxsdSchema) {
		// -System.out.println("Called FROM 2");
		Node theBaseNodeOfTheExtendedType = WSDL_XSD_SchemaToolkit
				.getNodeOfType(extendedTypeName, theWSDLxsdSchema, null);
		if (theBaseNodeOfTheExtendedType != null) {
			// -System.out.println(theBaseNodeOfTheExtendedType.getLocalName());
			ComplexObject extendedTypeCO = getExtendedTypeAsAComplexObject(
					theBaseNodeOfTheExtendedType, theWSDLxsdSchema);
			if (extendedTypeCO != null) {
				// The content of the extendedTypeCO should become content of
				// the co
				// Put the content of the returned extendedTypeCO in the co
				// -System.out.println("WWWWWWWWWWWWWWWWWW");

				if (extendedTypeCO.getHasNativeObjects() != null) {
					for (int i = 0; i < extendedTypeCO.getHasNativeObjects()
							.size(); i++) {
						co.getHasNativeObjects().add(
								extendedTypeCO.getHasNativeObjects().get(i));
					}
				}

				if (extendedTypeCO.getHasComplexObjects() != null) {
					for (int i = 0; i < extendedTypeCO.getHasComplexObjects()
							.size(); i++) {
						co.getHasComplexObjects().add(
								extendedTypeCO.getHasComplexObjects().get(i));
					}
				}

			}

		}

	}

	private static void parseSimpleContentForDocumentType(
			Schema theWSDLxsdSchema, Node inputNode, int iterNumber,
			ComplexObject co) {
		// O inputNode einai aytos pou exei name "complexContent"
		// -System.out.println("*** parseComplexContentForDocumentType ***");
		NodeList childrenOfTheChildrenOfTheDamned = inputNode.getChildNodes();
		// Mesa stous nodes tis listas childrenOfTheChildrenOfTheDamned tha
		// yparxoun oi nodes "extension" (logika tha yparxei moono enas tetoios
		// node)
		// Mesa stous nodes "extension" tha yparxoun oi nodes element pou tha
		// einai ta simple object i complex object pou symplirwnoun ta
		// klironomimena
		if (childrenOfTheChildrenOfTheDamned != null) {

			for (int k2 = 0; k2 < childrenOfTheChildrenOfTheDamned.getLength(); k2++) {
				// ////-System.out.println("FFF "+childrenOfTheChildrenOfTheDamned.item(k2).getNodeName());
				if (!childrenOfTheChildrenOfTheDamned.item(k2).hasAttributes())
					continue;

				if (childrenOfTheChildrenOfTheDamned.item(k2).getNodeName()
						.contains("extension")) {
					if (childrenOfTheChildrenOfTheDamned.item(k2)
							.getAttributes() != null) {
						if (childrenOfTheChildrenOfTheDamned.item(k2)
								.getAttributes().getNamedItem("base") != null) {
							String baseType = childrenOfTheChildrenOfTheDamned
									.item(k2).getAttributes()
									.getNamedItem("base").getNodeValue();
							co.setObjectType(new QName(
									childrenOfTheChildrenOfTheDamned.item(k2)
											.getAttributes()
											.getNamedItem("base")
											.getNodeValue()));
						}
					}
				}
			}
		}
	}

	private static void parseComplexContentForDocumentType(
			Schema theWSDLxsdSchema, Node inputNode, int iterNumber,
			ComplexObject co) {
		// O inputNode einai aytos pou exei name "complexContent"
		// -System.out.println("*** parseComplexContentForDocumentType ***");
		NodeList childrenOfTheChildrenOfTheDamned = inputNode.getChildNodes();
		// Mesa stous nodes tis listas childrenOfTheChildrenOfTheDamned tha
		// yparxoun oi nodes "extension" (logika tha yparxei moono enas tetoios
		// node)
		// Mesa stous nodes "extension" tha yparxoun oi nodes element pou tha
		// einai ta simple object i complex object pou symplirwnoun ta
		// klironomimena
		if (childrenOfTheChildrenOfTheDamned != null) {

			for (int k2 = 0; k2 < childrenOfTheChildrenOfTheDamned.getLength(); k2++) {
				// ////-System.out.println("FFF "+childrenOfTheChildrenOfTheDamned.item(k2).getNodeName());
				if (!childrenOfTheChildrenOfTheDamned.item(k2).hasAttributes())
					continue;

				if (childrenOfTheChildrenOfTheDamned.item(k2).getNodeName()
						.contains("extension")) {
					if (childrenOfTheChildrenOfTheDamned.item(k2)
							.getAttributes() != null) {
						if (childrenOfTheChildrenOfTheDamned.item(k2)
								.getAttributes().getNamedItem("base") != null) {
							String baseType = childrenOfTheChildrenOfTheDamned
									.item(k2).getAttributes()
									.getNamedItem("base").getNodeValue();

							addTypesOfExtendedTypeToTheComplexObject(co,
									baseType, theWSDLxsdSchema);

							// //-System.out.println(childrenOfTheChildrenOfTheDamned.item(k2).getAttributes().getNamedItem("base").getNamespaceURI());

							/*
							 * if(baseType.contains(":")){//DEN Ginetai na einai
							 * simple type to base tou extended type, //ara
							 * vlepw mono ayti tin periptwsi...
							 * 
							 * String key1=baseType.substring(0,
							 * baseType.indexOf(":")); String
							 * xsdNamespace=(String)namespaces.get(key1);
							 * if(xsdNamespace!=null){ //
							 * ////-System.out.println("Namespace Found!!!");
							 * String type1=baseType.substring(key1.length()+1,
							 * baseType.length());
							 * 
							 * parseImportedXSDforLiteral3(s1, type1, ta,
							 * iterNumber+1,true,co, xsdNamespace);
							 * //-System.out.println("WW");
							 * 
							 * }
							 * 
							 * }
							 */
						}

					}
					// parse children "elements" of the extension node -
					// "name"=sequence i choice
					NodeList elementTypesOfExtendedTypeList = childrenOfTheChildrenOfTheDamned
							.item(k2).getChildNodes();
					for (int i = 0; i < elementTypesOfExtendedTypeList
							.getLength(); i++) {
						if (elementTypesOfExtendedTypeList.item(i) != null
								&& elementTypesOfExtendedTypeList.item(i)
										.getNodeName() != null
								&& (elementTypesOfExtendedTypeList.item(i)
										.getNodeName().equals("sequence") || elementTypesOfExtendedTypeList
										.item(i).getNodeName().equals("choice"))) {
							// -System.out.println("### parseComplexContentForDocumentType ###");
							parseSequenceOrChoiceNode(theWSDLxsdSchema,
									elementTypesOfExtendedTypeList.item(i),
									iterNumber, co);

						}
					}

				}
			}
		}

	}

	private static void parseSequenceOrChoiceNode(
			javax.wsdl.extensions.schema.Schema theWSDLxsdSchema,
			Node inputNode, int iterNumber, ComplexObject co) {

		// -System.out.println("\tparse sequence for Node: "+inputNode.getNodeName()+"    "+co.objectName+"   ##  "+
		// co.getHasComplexObjects().size()+" "+co.getHasNativeObjects().size());

		if (co.getObjectName().equals("administrativeAreaId")) {
			// -System.out.println("AAA");
		}

		// -System.out.println("XSD: "+theWSDLxsdSchema.getDocumentBaseURI());
		try {
			// -System.out.println("Node name:"+
			// inputNode.getAttributes().getNamedItem("name").getNodeValue());
		} catch (Exception e) {

		}
		NodeList listOfChildrenOfTheSequenceNode = inputNode.getChildNodes();
		if (listOfChildrenOfTheSequenceNode != null) {

			if (listOfChildrenOfTheSequenceNode.getLength() == 0) {
				// -System.out.println("AAA");
			}

			for (int k2 = 0; k2 < listOfChildrenOfTheSequenceNode.getLength(); k2++) {
				// -System.out.println("\tFFF "+listOfChildrenOfTheSequenceNode.item(k2).getNodeName());
				if (!listOfChildrenOfTheSequenceNode.item(k2).hasAttributes())
					continue;

				if (listOfChildrenOfTheSequenceNode.item(k2).getNodeName()
						.contains("element")
						|| listOfChildrenOfTheSequenceNode.item(k2)
								.getNodeName().contains("group")) {
					// child of the Sequence Node
					NamedNodeMap attributesOfType = listOfChildrenOfTheSequenceNode
							.item(k2).getAttributes();
					// //-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
					String attName = "";
					String attType = "";
					String additionalInfo = "";
					String referencedType = "";

					// EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
					for (int k1 = 0; k1 < attributesOfType.getLength(); k1++) {
						Node att = attributesOfType.item(k1);
						if (att.getNodeName().equalsIgnoreCase("name")) {
							// -System.out.println("\tName: "+att.getNodeValue());
							attName = att.getNodeValue();
						} else if (att.getNodeName().equalsIgnoreCase("type")) {
							// -System.out.println("\tType: "+att.getNodeValue());
							attType = att.getNodeValue();
						} else if (att.getNodeName().equalsIgnoreCase("ref")) {
							// -System.out.println("Reference to another type Found!!!");
							referencedType = att.getNodeValue();
							// -System.out.println(att.getNodeValue());
						} else {
							// -System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
							additionalInfo += att.getNodeName() + ":"
									+ att.getNodeValue() + "   ";
						}
					}

					if (!referencedType.equals("")) {
						// PARSE REFERENCED TYPE HERE
						String key1 = referencedType.substring(0,
								referencedType.indexOf(":"));
						String xsdNamespace = (String) MitsosParser.namespaces
								.get(key1);
						if (xsdNamespace != null) {
							String type1 = referencedType.substring(
									key1.length() + 1, referencedType.length());
							// //-System.out.println("Namespace Found!!!: "+xsdNamespace+"    For type: "+type1);
							// ComplexObject newComplexObject=new
							// ComplexObject();
							// newComplexObject.objectName=attName;
							// newComplexObject.setAdditionalInfo(additionalInfo);
							// newComplexObject.objectType=type1;

							boolean objectAlreadyExistsInHashMap = MitsosParser.parsedObjectsHashmap
									.containsKey(type1);

							parseTypeIterativeFromSpecificNamespace(
									theWSDLxsdSchema, null, xsdNamespace,
									type1, co);
							// parseImportedXSDforLiteral3(s1, type1,
							// iterNumber+1,true,co,
							// xsdNamespace);

							// co.getHasComplexObjects().add(newComplexObject);

						} else {
							// //-System.out.println("Namespace was null... Will be treated as NATIVE TYPE...");
							NativeObject newNativeObject = new NativeObject();
							newNativeObject.setObjectName(new QName(attName));
							newNativeObject.setAdditionalInfo(additionalInfo);
							newNativeObject.setObjectType(new QName(attType));

							// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
							// newNativeObject);

							co.getHasNativeObjects().add(newNativeObject);

						}

					} else {

						// //-ta.append("\n\t\t\tName: "+attName);
						additionalInfo = additionalInfo.trim();
						// //-ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");

						// //-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

						// Find the Type of the attName...
						// ITERATIVE PROCESS.........
						if (attType
								.startsWith(MitsosParser.targetNamespacePrefix)) {
							String type1 = attType
									.substring(
											MitsosParser.targetNamespacePrefix
													.length(), attType.length());

							ComplexObject newComplexObject = new ComplexObject();
							newComplexObject.setObjectName(new QName(attName));
							newComplexObject.setAdditionalInfo(additionalInfo);
							newComplexObject.setObjectType(new QName(type1));

							if (type1.startsWith("ArrayOf")) {
								type1 = type1.replaceFirst("ArrayOf", "");
								// //-ta.append("  ("+type1+"[])");
								newComplexObject.setObjectType(new QName(type1
										+ "[]"));
							} else if (type1.endsWith("Array")) {
								type1 = type1.substring(0, type1.length() - 5);
								// //-ta.append("  ("+type1+"[])");
								newComplexObject.setObjectType(new QName(type1
										+ "[]"));
							} else if (attType.endsWith("[]")) {
								type1 = type1.replace("[]", "");
								// //-ta.append("  ("+type1+"[])");
								newComplexObject.setObjectType(new QName(type1
										+ "[]"));
							}

							boolean objectAlreadyExistsInHashMap = MitsosParser.parsedObjectsHashmap
									.containsKey(type1);

							String xsdNamespace = (String) MitsosParser.namespaces
									.get(MitsosParser.targetNamespacePrefix
											.replace(":", ""));
							// parseTypeIterativeForXSDImport(s1, type1,
							// iterNumber+1,true,newComplexObject);
							if (xsdNamespace != null) {
								if (!objectAlreadyExistsInHashMap) {
									MitsosParser.parsedObjectsHashmap.put(
											newComplexObject.getObjectType(),
											newComplexObject);
									parseTypeIterativeFromSpecificNamespace(
											theWSDLxsdSchema, null,
											xsdNamespace, type1,
											newComplexObject);
									co.getHasComplexObjects().add(
											newComplexObject);
								} else {
									try {
										// //-System.out.println(MitsosParser.parsedObjectsHashmap.get(type1).getClass());
										ComplexObject co1 = (ComplexObject) MitsosParser.parsedObjectsHashmap
												.get(type1);
										co1.setObjectName(newComplexObject
												.getObjectName());
										co.getHasComplexObjects().add(co1);
									} catch (Exception e) {
										NativeObject no1 = (NativeObject) MitsosParser.parsedObjectsHashmap
												.get(type1);
										no1.setObjectName(newComplexObject
												.getObjectName());
										co.getHasNativeObjects().add(no1);
									}
								}
							} else {
								// -System.out.println("WWWWWWWWWW CHECK THIS!!!!!!!!!");
							}

						} else {
							if (MitsosParser.nativeTypePrefix != null
									&& attType
											.startsWith(MitsosParser.nativeTypePrefix)) {
								NativeObject newNativeObject = new NativeObject();
								newNativeObject
										.setObjectName(new QName(attName));
								newNativeObject
										.setAdditionalInfo(additionalInfo);
								newNativeObject
										.setObjectType(new QName(attType));

								// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
								// newNativeObject);
								co.getHasNativeObjects().add(newNativeObject);

							} else {
								if (attType.contains(":")) {
									String key1 = attType.substring(0,
											attType.indexOf(":"));
									String xsdNamespace = (String) MitsosParser.namespaces
											.get(key1);
									if (xsdNamespace != null) {
										if (xsdNamespace
												.equals("http://www.w3.org/2001/XMLSchema")) {
											// NATIVE TYPE
											NativeObject newNativeObject = new NativeObject();
											newNativeObject
													.setObjectName(new QName(
															attName));
											newNativeObject
													.setAdditionalInfo(additionalInfo);
											newNativeObject
													.setObjectType(new QName(
															attType));

											// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
											// newNativeObject);
											co.getHasNativeObjects().add(
													newNativeObject);
										} else {
											// //-System.out.println("Namespace Found!!!");
											String type1 = attType.substring(
													key1.length() + 1,
													attType.length());
											ComplexObject newComplexObject = new ComplexObject();
											newComplexObject
													.setObjectName(new QName(
															attName));
											newComplexObject
													.setAdditionalInfo(additionalInfo);
											newComplexObject
													.setObjectType(new QName(
															type1));

											boolean objectExistsInHashmap = MitsosParser.parsedObjectsHashmap
													.containsKey(type1);

											if (!objectExistsInHashmap) {
												MitsosParser.parsedObjectsHashmap
														.put(newComplexObject
																.getObjectType(),
																newComplexObject);
												parseTypeIterativeFromSpecificNamespace(
														theWSDLxsdSchema, null,
														xsdNamespace, type1,
														newComplexObject);
												co.getHasComplexObjects().add(
														newComplexObject);
											} else {
												try {

													// PROSOXI!!!!!!!!!!!
													// EDW MPAINEI
													// SYNEXEIA!!!!!!

													// //-System.out.println(MitsosParser.parsedObjectsHashmap.get(type1).getClass());
													ComplexObject co1 = (ComplexObject) MitsosParser.parsedObjectsHashmap
															.get(type1);
													co1.setObjectName(newComplexObject
															.getObjectName());
													co.getHasComplexObjects()
															.add(co1);
												} catch (Exception e) {
													NativeObject no1 = (NativeObject) MitsosParser.parsedObjectsHashmap
															.get(type1);
													no1.setObjectName(newComplexObject
															.getObjectName());
													co.getHasNativeObjects()
															.add(no1);
												}
											}
										}

									} else {
										// //-System.out.println("Namespace was null... Will be treated as NATIVE TYPE...");
										NativeObject newNativeObject = new NativeObject();
										newNativeObject
												.setObjectName(new QName(
														attName));
										newNativeObject
												.setAdditionalInfo(additionalInfo);
										newNativeObject
												.setObjectType(new QName(
														attType));

										// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
										// newNativeObject);
										co.getHasNativeObjects().add(
												newNativeObject);

									}
								} else {
									// //-System.out.println("type Namespace was null... Will be treated as NATIVE TYPE...");
									NativeObject newNativeObject = new NativeObject();
									newNativeObject.setObjectName(new QName(
											attName));
									newNativeObject
											.setAdditionalInfo(additionalInfo);
									newNativeObject.setObjectType(new QName(
											attType));

									// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
									// newNativeObject);
									co.getHasNativeObjects().add(
											newNativeObject);
								}
							}

						}
					}
					// -System.out.println("AAA");
					// EDW PREPEI NA ELEGKSW AN sto co yparxoun ComplexObjects
					// me mono ena NativeObject mesa
					// Ayta einai stin ousia NativeObjects

				} else {// EINAI SIMPLE TYPE
						// XAZOMARA EINAI AYTO MALLON, PREPEI na einai element,
						// opote se normal synthikes den tha mpei edw
						// -System.out.println("BBB");
						// KAI OMWS MPAINEI KAI EDW!!!!!!!!! ...gia <group>

					NativeObject newNativeObject = new NativeObject();
					// newNativeObject.objectName=attName;
					// newNativeObject.setAdditionalInfo(additionalInfo);
					// newNativeObject.objectType=attType;
					// co.getHasNativeObjects().add(newNativeObject);

					if (listOfChildrenOfTheSequenceNode.item(k2).getNodeName()
							.contains("constraint")) {
						// PARSE THE ENUMERATION...
					}
				}

				String cObjs = "";
				for (int k111 = 0; k111 < co.getHasComplexObjects().size(); k111++) {
					cObjs += ((ComplexObject) co.getHasComplexObjects().get(
							k111)).getObjectName()
							+ " ";
				}

				String nObjs = "";
				for (int k111 = 0; k111 < co.getHasNativeObjects().size(); k111++) {
					cObjs += ((NativeObject) co.getHasNativeObjects().get(k111))
							.getObjectName() + " ";
				}

				// -System.out.println("\tFinished: "+inputNode.getNodeName()+"    "+co.objectName+"   ##  "+
				// co.getHasComplexObjects().size()+" "+co.getHasNativeObjects().size());
				// -System.out.println("\t\tCOs: "+cObjs);
				// -System.out.println("\t\tNOs: "+nObjs);

			}
		}

	}

	private static void parseTypeFromElementNodeOfXSD(
			javax.wsdl.extensions.schema.Schema theWSDLxsdSchema, Node n,
			ComplexObject co) {
		// -System.out.println("** parseTypeFromElementNodeOfXSD **");
		NodeList childrenOfTheDamned = n.getChildNodes();
		boolean foundChildren = false;
		if (childrenOfTheDamned != null) {
			if (childrenOfTheDamned.getLength() > 0) {
				foundChildren = true;
			}
		}

		if (!foundChildren) {
			// find "type" attribute and parse the type defined here
			if (n.getAttributes() != null
					&& (n.getAttributes().getNamedItem("type") != null || n
							.getAttributes().getNamedItem("ref") != null)) {
				String typeA = null;

				if (n.getAttributes().getNamedItem("type") != null) {
					// -System.out.println("asdfsdf   "+n.getAttributes().getNamedItem("type").getNodeValue());
					typeA = n.getAttributes().getNamedItem("type")
							.getNodeValue();
				} else if (n.getAttributes().getNamedItem("ref") != null) {
					// -System.out.println("asdfsdf   "+n.getAttributes().getNamedItem("ref").getNodeValue());
					typeA = n.getAttributes().getNamedItem("ref")
							.getNodeValue();
				}

				if (typeA != null && typeA.contains(":")) {
					String key1 = typeA.substring(0, typeA.indexOf(":"));
					String xsdNamespace = (String) MitsosParser.namespaces
							.get(key1);
					if (xsdNamespace != null) {
						// //-System.out.println("Namespace Found!!!");
						if (!xsdNamespace
								.equals("http://www.w3.org/2001/XMLSchema")) {
							String type1 = typeA.substring(key1.length() + 1,
									typeA.length());

							boolean objectAlreadyExistsInHashMap = MitsosParser.parsedObjectsHashmap
									.containsKey(type1);

							// parseTypeIterativeForXSDImport(s1, type1,
							// iterNumber+1,true,newComplexObject);

							if (!objectAlreadyExistsInHashMap) {

								MitsosParser.parsedObjectsHashmap.put(
										co.getObjectType(), co);
								parseTypeIterativeFromSpecificNamespace(
										theWSDLxsdSchema, null, xsdNamespace,
										type1, co);
								// -System.out.println("WWW");
							} else {
								try {
									// //-System.out.println(MitsosParser.parsedObjectsHashmap.get(type1).getClass());
									ComplexObject co1 = (ComplexObject) MitsosParser.parsedObjectsHashmap
											.get(type1);
									co1.setObjectName(co.getObjectName());
									co = co1;
								} catch (Exception e) {
									NativeObject no1 = (NativeObject) MitsosParser.parsedObjectsHashmap
											.get(type1);
									no1.setObjectName(co.getObjectName());
									co.getHasNativeObjects().add(no1);
								}
							}

						} else {
							// EINAI NATIVE TYPE
							NamedNodeMap nnm = n.getAttributes();
							String additionalInfo = "";
							for (int i = 0; i < nnm.getLength(); i++) {
								if (nnm.item(i) != null) {
									if (nnm.item(i).getNodeName() != null) {
										if (!nnm.item(i).getNodeName()
												.equals("name")
												&& !nnm.item(i).getNodeName()
														.equals("type")) {
											additionalInfo += " "
													+ nnm.item(i)
															.getNodeValue();
										}
									}
								}
							}
							NativeObject newNativeObject = new NativeObject();
							newNativeObject.setObjectName(co.getObjectName());
							newNativeObject.setAdditionalInfo(additionalInfo);
							newNativeObject.setObjectType(new QName(typeA));

							// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
							// newNativeObject);
							co.getHasNativeObjects().add(newNativeObject);

						}

					} else {
						// EINAI NATIVE TYPE
						NamedNodeMap nnm = n.getAttributes();
						String additionalInfo = "";
						for (int i = 0; i < nnm.getLength(); i++) {
							if (nnm.item(i) != null) {
								if (nnm.item(i).getNodeName() != null) {
									if (!nnm.item(i).getNodeName()
											.equals("name")
											&& !nnm.item(i).getNodeName()
													.equals("type")) {
										additionalInfo += " "
												+ nnm.item(i).getNodeValue();
									}
								}
							}
						}
						NativeObject newNativeObject = new NativeObject();
						newNativeObject.setObjectName(co.getObjectName());
						newNativeObject.setAdditionalInfo(additionalInfo);
						newNativeObject.setObjectType(new QName(typeA));

						// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
						// newNativeObject);
						co.getHasNativeObjects().add(newNativeObject);

					}
				} else {
					// EINAI NATIVE TYPE
					NamedNodeMap nnm = n.getAttributes();
					String additionalInfo = "";
					for (int i = 0; i < nnm.getLength(); i++) {
						if (nnm.item(i) != null) {
							if (nnm.item(i).getNodeName() != null) {
								if (!nnm.item(i).getNodeName().equals("name")
										&& !nnm.item(i).getNodeName()
												.equals("type")) {
									additionalInfo += " "
											+ nnm.item(i).getNodeValue();
								}
							}
						}
					}
					NativeObject newNativeObject = new NativeObject();
					newNativeObject.setObjectName(co.getObjectName());
					newNativeObject.setAdditionalInfo(additionalInfo);
					newNativeObject.setObjectType(new QName(typeA));

					// //MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
					// newNativeObject);
					co.getHasNativeObjects().add(newNativeObject);

				}
			}
		} else {
			// Parse all Children of the element node here
			for (int i = 0; i < childrenOfTheDamned.getLength(); i++) {
				Node n1 = (Node) childrenOfTheDamned.item(i);
				// -System.out.println(n1.getNodeName());
				if (n1.getNodeName() != null) {
					if (n1.getNodeName().contains("complexType")) {
						parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema, n1,
								co);
					} else if (n1.getNodeName().contains("simpleType")) {
						parseTypeFromSimpleTypeNodeOfXSD(n1, co);
					} else if (n1.getNodeName().contains("element")) {
						parseTypeFromElementNodeOfXSD(theWSDLxsdSchema, n1, co);
					}
				}
			}

		}

	}

	private static void parseTypeFromComplexTypeNodeOfXSD(
			javax.wsdl.extensions.schema.Schema theWSDLxsdSchema, Node n,
			ComplexObject co) {

		/*
		 * try{ if(n.getAttributes().getNamedItem("type").equals(
		 * "AdministrativeAreaCodeType")){ //-System.out.println("AAA"); }
		 * }catch(Exception e){
		 * 
		 * }
		 */
		// -System.out.println("** parseTypeFromComplexTypeNodeOfXSD **");
		NodeList listWithChildrenOfTheComplexTypeNode = n.getChildNodes();
		if (listWithChildrenOfTheComplexTypeNode != null) {
			for (int k = 0; k < listWithChildrenOfTheComplexTypeNode
					.getLength(); k++) {
				// -System.out.println("\t"+listWithChildrenOfTheComplexTypeNode.item(k).getNodeName());
				if (listWithChildrenOfTheComplexTypeNode.item(k).getNodeName()
						.contains("sequence")
						|| listWithChildrenOfTheComplexTypeNode.item(k)
								.getNodeName().contains("choice")) {
					// -System.out.println("### parseTypeFromComplexTypeNodeOfXSD ###");

					parseSequenceOrChoiceNode(theWSDLxsdSchema,
							listWithChildrenOfTheComplexTypeNode.item(k), 0, co);

				} else if (listWithChildrenOfTheComplexTypeNode.item(k)
						.getNodeName().contains("complexContent")) {
					parseComplexContentForDocumentType(theWSDLxsdSchema,
							listWithChildrenOfTheComplexTypeNode.item(k), 0, co);
				} else if (listWithChildrenOfTheComplexTypeNode.item(k)
						.getNodeName().contains("simpleContent")) {
					parseSimpleContentForDocumentType(theWSDLxsdSchema,
							listWithChildrenOfTheComplexTypeNode.item(k), 0, co);
				}
			}
		}

	}

	private static void parseTypeFromSimpleTypeNodeOfXSD(Node n,
			ComplexObject co) {
		// o,ti vrethei mesa sto n prepei na mpei san new objects mea sto co
		// -System.out.println("** parseTypeFromSimpleTypeNodeOfXSD **");
		String attName = co.getObjectName().getLocalPart();
		String attType = "";
		String additionalInfo = ""; // edw tha mpoun ta restrictions...

		if (n.getAttributes().getNamedItem("name").getNodeValue() != null)
			attName = n.getAttributes().getNamedItem("name").getNodeValue();
		NodeList childrenOfTheDamned = n.getChildNodes();
		if (childrenOfTheDamned != null) {
			for (int k = 0; k < childrenOfTheDamned.getLength(); k++) {
				// ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
				if (childrenOfTheDamned.item(k).getNodeName()
						.contains("restriction")) {

					if (childrenOfTheDamned.item(k).getAttributes()
							.getNamedItem("base") != null) {
						attType = childrenOfTheDamned.item(k).getAttributes()
								.getNamedItem("base").getNodeValue();
					}

					NodeList childrenOfTheChildrenOfTheDamned = childrenOfTheDamned
							.item(k).getChildNodes();
					if (childrenOfTheChildrenOfTheDamned != null) {
						for (int k2 = 0; k2 < childrenOfTheChildrenOfTheDamned
								.getLength(); k2++) {
							Node restrictionEnumNode = childrenOfTheChildrenOfTheDamned
									.item(k2);
							if (!restrictionEnumNode.hasAttributes())
								continue;
							if (additionalInfo.equals(""))
								additionalInfo = "Accepted Values: ";

							String str = restrictionEnumNode.getAttributes()
									.getNamedItem("value").getNodeValue();
							if (additionalInfo.equals("Accepted Values: ")) {
								additionalInfo += str + "";
							} else {
								additionalInfo += ", " + str;
							}
						}
					}

					NativeObject newNativeObject = new NativeObject();
					newNativeObject.setObjectName(new QName(attName));
					newNativeObject.setAdditionalInfo(additionalInfo);
					newNativeObject.setObjectType(new QName(attType));

					// MitsosParser.parsedObjectsHashmap.put(newNativeObject.objectType,
					// newNativeObject);
					co.getHasNativeObjects().add(newNativeObject);
				}
			}
		}

	}

	private static void parseTypeIterativeFromSpecificNamespace(
			javax.wsdl.extensions.schema.Schema theWSDLxsdSchema,
			Schema currentSchema, String xsdNamespace, String type1,
			ComplexObject co) {
		// TO PERIEXOMENO TOU type1 pou tha vrw prepei na mpei mesa sto co
		// an to type1 periexei complexObjects, nativeObjects etc. tha ftiaksw
		// edw kainourgia kai tha ta valw mesa sto co

		// -System.out.println("******** parseTypeIterativeFromSpecificNamespace ***********");
		// -System.out.println("\t\t\t"+type1);

		if (type1.equals("JS_JourneyPlanningCapabilities")) {
			// -System.out.println("PT_FreeText_PropertyType");
		}

		// Get the XSD Schema within which the type is defined
		javax.wsdl.extensions.schema.Schema theImportedSchema = null;
		if (currentSchema == null) {
			theImportedSchema = WSDL_XSD_SchemaToolkit
					.getTheImportedOrIncludedSchemaWithTheSpecificNS(
							theWSDLxsdSchema, xsdNamespace);
		} else {
			theImportedSchema = currentSchema;
		}
		if (theImportedSchema == null)
			theImportedSchema = theWSDLxsdSchema;

		// -System.out.println("WWW::  "+theImportedSchema.getDocumentBaseURI());
		org.w3c.dom.Element impSchElem = theImportedSchema.getElement();
		if (impSchElem == null)
			return;

		NodeList elementNodesList = impSchElem.getElementsByTagName("element");
		NodeList complexTypeNodesList = impSchElem
				.getElementsByTagName("complexType");
		NodeList simpleTypeNodesList = impSchElem
				.getElementsByTagName("simpleType");
		NodeList groupTypeNodesList = impSchElem.getElementsByTagName("group");

		// -System.out.println(theImportedSchema.getDocumentBaseURI());
		// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength()+" "+groupTypeNodesList.getLength());

		if (elementNodesList != null && complexTypeNodesList != null
				&& simpleTypeNodesList != null && groupTypeNodesList != null) {
			if (elementNodesList.getLength() == 0
					&& complexTypeNodesList.getLength() == 0
					&& simpleTypeNodesList.getLength() == 0
					&& groupTypeNodesList.getLength() == 0) {
				elementNodesList = impSchElem
						.getElementsByTagName(MitsosParser.nativeTypePrefix
								+ "element");
				complexTypeNodesList = impSchElem
						.getElementsByTagName(MitsosParser.nativeTypePrefix
								+ "complexType");
				simpleTypeNodesList = impSchElem
						.getElementsByTagName(MitsosParser.nativeTypePrefix
								+ "simpleType");
				groupTypeNodesList = impSchElem
						.getElementsByTagName(MitsosParser.nativeTypePrefix
								+ "group");
				// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength());

				if (elementNodesList.getLength() == 0
						&& complexTypeNodesList.getLength() == 0
						&& simpleTypeNodesList.getLength() == 0
						&& groupTypeNodesList.getLength() == 0) {
					Vector xmlSchemaKeys = new Vector();
					Set entries = MitsosParser.namespaces.entrySet();
					Iterator iter1 = entries.iterator();
					while (iter1.hasNext()) {
						Entry entry = (Entry) iter1.next();
						if (entry.getValue() != null
								&& entry.getValue().equals(
										"http://www.w3.org/2001/XMLSchema")) {
							xmlSchemaKeys.add(entry.getKey());
						}
					}

					if (xmlSchemaKeys.size() > 0) {
						for (int i = 0; i < xmlSchemaKeys.size(); i++) {
							String ns = (String) xmlSchemaKeys.get(i);
							String prefix = ns + ":";

							elementNodesList = impSchElem
									.getElementsByTagName(prefix + "element");
							complexTypeNodesList = impSchElem
									.getElementsByTagName(prefix
											+ "complexType");
							simpleTypeNodesList = impSchElem
									.getElementsByTagName(prefix + "simpleType");
							groupTypeNodesList = impSchElem
									.getElementsByTagName(prefix + "group");
							// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength());

							if (elementNodesList.getLength() != 0
									|| complexTypeNodesList.getLength() != 0
									|| simpleTypeNodesList.getLength() != 0
									|| groupTypeNodesList.getLength() != 0) {
								break;
							}

						}
					}
				}

				// importNodesList=e1.getElementsByTagName(MitsosParser.nativeTypePrefix+"import");
				// includeNodesList=e1.getElementsByTagName(MitsosParser.nativeTypePrefix+"include");
			}
		}

		// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength()+" "+groupTypeNodesList.getLength());

		if (complexTypeNodesList != null) {
			for (int i = 0; i < complexTypeNodesList.getLength(); i++) {
				Node n = complexTypeNodesList.item(i);
				if (n.hasAttributes()
						&& n.getAttributes().getNamedItem("name") != null) {
					if (n.getAttributes().getNamedItem("name").getNodeValue()
							.equals(type1)) {
						parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema, n,
								co);
						return;
					}
				}
			}
		}

		if (simpleTypeNodesList != null) {
			for (int i = 0; i < simpleTypeNodesList.getLength(); i++) {
				Node n = simpleTypeNodesList.item(i);
				if (n.hasAttributes()
						&& n.getAttributes().getNamedItem("name") != null) {
					if (n.getAttributes().getNamedItem("name").getNodeValue()
							.equals(type1)) {
						parseTypeFromSimpleTypeNodeOfXSD(n, co);
						return;
					}
				}
			}
		}

		if (groupTypeNodesList != null) {
			for (int i = 0; i < groupTypeNodesList.getLength(); i++) {
				Node n = groupTypeNodesList.item(i);
				if (n.hasAttributes()
						&& n.getAttributes().getNamedItem("name") != null) {
					if (n.getAttributes().getNamedItem("name").getNodeValue()
							.equals(type1)) {
						parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema, n,
								co);
						return;
					}
				}
			}
		}

		if (elementNodesList != null) {
			for (int i = 0; i < elementNodesList.getLength(); i++) {
				Node n = elementNodesList.item(i);
				if (n.hasAttributes()
						&& n.getAttributes().getNamedItem("name") != null) {
					if (n.getAttributes().getNamedItem("name").getNodeValue()
							.equals(type1)) {
						parseTypeFromElementNodeOfXSD(theWSDLxsdSchema, n, co);
						return;
					}
				}
			}
		}

		if (xsdNamespace.length() > 0) {
			String prefix = null;
			if (MitsosParser.namespaces.containsValue(xsdNamespace)) {
				Set entrySet = MitsosParser.namespaces.entrySet();
				Iterator iter12 = entrySet.iterator();
				while (iter12.hasNext()) {
					java.util.Map.Entry e = (java.util.Map.Entry) iter12.next();
					String key = (String) e.getKey();
					String value = (String) e.getValue();
					if (value.equals(xsdNamespace)) {
						prefix = key;
						type1 = prefix + ":" + type1;
						break;
					}
				}
			}
		}
		Node n = WSDL_XSD_SchemaToolkit.getNodeOfType(type1, theWSDLxsdSchema,
				null);
		if (n != null && n.getNodeName() != null) {
			if (n.getNodeName().equals("element")) {
				parseTypeFromElementNodeOfXSD(theWSDLxsdSchema, n, co);
				return;
			} else if (n.getNodeName().equals("complexType")) {
				parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema, n, co);
				return;
			} else if (n.getNodeName().equals("group")) {
				parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema, n, co);
				return;
			} else if (n.getNodeName().equals("simpleType")) {
				parseTypeFromSimpleTypeNodeOfXSD(n, co);
				return;
			}
		}

		/*
		 * List includesList=theImportedSchema.getIncludes();
		 * if(includesList!=null){ Iterator
		 * includesIter=includesList.iterator(); while(includesIter.hasNext()){
		 * SchemaReferenceImpl schImpl=(SchemaReferenceImpl)includesIter.next();
		 * Schema includedSchema=schImpl.getReferencedSchema();
		 * if(WSDL_XSD_SchemaToolkit.nodeExistsInSchema(type1, includedSchema)){
		 * //-System.out.println("### Looking in schema: "+includedSchema.
		 * getDocumentBaseURI());
		 * parseTypeIterativeFromSpecificNamespace(theWSDLxsdSchema,
		 * includedSchema, xsdNamespace, type1, co); return; } } }
		 * 
		 * Map importsMap=theImportedSchema.getImports(); if(importsMap!=null){
		 * Collection col=importsMap.values(); Iterator
		 * importsIter=col.iterator(); while(importsIter.hasNext()){
		 * SchemaImportImpl schImpl=(SchemaImportImpl)importsIter.next(); Schema
		 * importedSchema=schImpl.getReferencedSchema();
		 * if(WSDL_XSD_SchemaToolkit.nodeExistsInSchema(type1, importedSchema)){
		 * parseTypeIterativeFromSpecificNamespace(theWSDLxsdSchema,
		 * importedSchema, xsdNamespace, type1, co); return; } } }
		 */

	}

	public static void parseDocumentType2(Definition definition,
			QName inPartType, WSOperationInput mitsosOperationInput,
			WSOperationOutput mitsosOperationOutput, javax.wsdl.Part part) {

		ComplexObject dummyCo = new ComplexObject();
		// -System.out.println(inPartType.toString());
		if (inPartType.getPrefix() != null
				&& inPartType.getPrefix().length() > 0) {
			dummyCo.setObjectType(new QName(inPartType.getPrefix() + ":"
					+ inPartType.getLocalPart()));
		} else {
			dummyCo.setObjectType(new QName(inPartType.getLocalPart()));
		}

		if (mitsosOperationInput != null)
			dummyCo.setObjectName(new QName("INPUT"));
		else if (mitsosOperationOutput != null)
			dummyCo.setObjectName(new QName("OUTPUT"));

		List extElementsList = definition.getTypes().getExtensibilityElements();
		// ////-System.out.println("Extensibility Elements Names:");
		if (extElementsList != null) {
			// -System.out.print(extElementsList.size()+"\n");
			Iterator iter1 = extElementsList.iterator();

			boolean typeParsingFinished = false;

			while (iter1.hasNext()) {
				try {
					com.ibm.wsdl.extensions.schema.SchemaImpl theWSDLxsdSchema = (com.ibm.wsdl.extensions.schema.SchemaImpl) iter1
							.next();
					// -System.out.println(theWSDLxsdSchema.toString());
					// -System.out.println(theWSDLxsdSchema.getElementType().getLocalPart());
					/*
					 * parseTypeIterativeFromSpecificNamespace(javax.wsdl.extensions
					 * .schema.Schema theWSDLxsdSchema, Schema currentSchema,
					 * String xsdNamespace, String type1, ComplexObject co){
					 */

					org.w3c.dom.Element e1 = theWSDLxsdSchema.getElement();

					NodeList elementNodesList = e1
							.getElementsByTagName("element");
					NodeList complexTypeNodesList = e1
							.getElementsByTagName("complexType");
					NodeList simpleTypeNodesList = e1
							.getElementsByTagName("simpleType");
					NodeList groupTypeNodesList = e1
							.getElementsByTagName("group");

					NodeList importNodesList = e1
							.getElementsByTagName("import");
					NodeList includeNodesList = e1
							.getElementsByTagName("include");

					if (elementNodesList != null
							&& complexTypeNodesList != null
							&& simpleTypeNodesList != null
							&& groupTypeNodesList != null) {
						if (elementNodesList.getLength() == 0
								&& complexTypeNodesList.getLength() == 0
								&& simpleTypeNodesList.getLength() == 0
								&& groupTypeNodesList.getLength() == 0) {
							elementNodesList = e1
									.getElementsByTagName(MitsosParser.nativeTypePrefix
											+ "element");
							complexTypeNodesList = e1
									.getElementsByTagName(MitsosParser.nativeTypePrefix
											+ "complexType");
							simpleTypeNodesList = e1
									.getElementsByTagName(MitsosParser.nativeTypePrefix
											+ "simpleType");
							groupTypeNodesList = e1
									.getElementsByTagName(MitsosParser.nativeTypePrefix
											+ "group");

							if (elementNodesList.getLength() == 0
									&& complexTypeNodesList.getLength() == 0
									&& simpleTypeNodesList.getLength() == 0
									&& groupTypeNodesList.getLength() == 0) {
								// Cover tin periptwsi pou yparxoun tags p.x.
								// xs:element, enw to Namespace
								// tou XML (nativeNamespacePrefix) einai
								// diaforetiko (p.x. s:)
								Vector xmlSchemaKeys = new Vector();
								Set entries = MitsosParser.namespaces
										.entrySet();
								Iterator iter2 = entries.iterator();
								while (iter2.hasNext()) {
									Entry entry = (Entry) iter2.next();
									if (entry.getValue() != null
											&& entry.getValue()
													.equals("http://www.w3.org/2001/XMLSchema")) {
										xmlSchemaKeys.add(entry.getKey());
									}
								}

								if (xmlSchemaKeys.size() > 0) {
									for (int i = 0; i < xmlSchemaKeys.size(); i++) {
										String ns = (String) xmlSchemaKeys
												.get(i);
										String prefix = ns + ":";

										elementNodesList = e1
												.getElementsByTagName(prefix
														+ "element");
										complexTypeNodesList = e1
												.getElementsByTagName(prefix
														+ "complexType");
										simpleTypeNodesList = e1
												.getElementsByTagName(prefix
														+ "simpleType");
										groupTypeNodesList = e1
												.getElementsByTagName(prefix
														+ "group");

										if (elementNodesList.getLength() != 0
												|| complexTypeNodesList
														.getLength() != 0
												|| simpleTypeNodesList
														.getLength() != 0
												|| groupTypeNodesList
														.getLength() != 0) {
											break;
										}

									}
								}
							}

							// importNodesList=e1.getElementsByTagName(MitsosParser.nativeTypePrefix+"import");
							// includeNodesList=e1.getElementsByTagName(MitsosParser.nativeTypePrefix+"include");
						}
					}

					// //-System.out.println(e1.ge.getDocumentBaseURI());
					// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength()+" "+groupTypeNodesList.getLength());
					if (importNodesList != null && includeNodesList != null) {
						// -System.out.println(importNodesList.getLength()+" "+includeNodesList.getLength());
					}

					if (elementNodesList != null && !typeParsingFinished) {
						for (int i = 0; i < elementNodesList.getLength(); i++) {
							Node n = elementNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue()
										.equals(inPartType.getLocalPart())) {

									parseTypeFromElementNodeOfXSD(
											theWSDLxsdSchema, n, dummyCo);
									typeParsingFinished = true;
									break;

								}
							}
						}
					}

					if (complexTypeNodesList != null && !typeParsingFinished) {
						for (int i = 0; i < complexTypeNodesList.getLength(); i++) {
							Node n = complexTypeNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue()
										.equals(inPartType.getLocalPart())) {

									parseTypeFromComplexTypeNodeOfXSD(
											theWSDLxsdSchema, n, dummyCo);
									typeParsingFinished = true;
									break;
								}
							}
						}
					}

					if (simpleTypeNodesList != null && !typeParsingFinished) {
						for (int i = 0; i < simpleTypeNodesList.getLength(); i++) {
							Node n = simpleTypeNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue()
										.equals(inPartType.getLocalPart())) {

									parseTypeFromSimpleTypeNodeOfXSD(n, dummyCo);
									typeParsingFinished = true;
									break;
								}
							}
						}
					}

					if (groupTypeNodesList != null && !typeParsingFinished) {
						for (int i = 0; i < groupTypeNodesList.getLength(); i++) {
							Node n = groupTypeNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue()
										.equals(inPartType.getLocalPart())) {

									parseTypeFromComplexTypeNodeOfXSD(
											theWSDLxsdSchema, n, dummyCo);
									typeParsingFinished = true;
									break;
								}
							}
						}
					}

					if (typeParsingFinished)
						break;
					Node n = null;

					String prefix = null;
					if (MitsosParser.namespaces.containsValue(inPartType
							.getNamespaceURI())) {
						Set entrySet = MitsosParser.namespaces.entrySet();
						Iterator iter12 = entrySet.iterator();
						while (iter12.hasNext()) {
							java.util.Map.Entry e = (java.util.Map.Entry) iter12
									.next();
							String key = (String) e.getKey();
							String value = (String) e.getValue();
							if (value.equals(inPartType.getNamespaceURI())) {
								prefix = key;
								break;
							}
						}
					}

					if (prefix != null) {
						n = WSDL_XSD_SchemaToolkit.getNodeOfType(prefix + ":"
								+ inPartType.getLocalPart(), theWSDLxsdSchema,
								null);
					} else {
						if (inPartType.getPrefix() != null
								&& inPartType.getPrefix().length() > 0) {
							n = WSDL_XSD_SchemaToolkit.getNodeOfType(
									inPartType.getPrefix() + ":"
											+ inPartType.getLocalPart(),
									theWSDLxsdSchema, null);
						} else {
							n = WSDL_XSD_SchemaToolkit.getNodeOfType(
									inPartType.getLocalPart(),
									theWSDLxsdSchema, null);
						}
					}

					if (n != null && n.getNodeName() != null) {
						if (n.getNodeName().contains("element")) {
							parseTypeFromElementNodeOfXSD(theWSDLxsdSchema, n,
									dummyCo);
							typeParsingFinished = true;
							break;

						} else if (n.getNodeName().contains("complexType")) {
							parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema,
									n, dummyCo);
							typeParsingFinished = true;
							break;

						} else if (n.getNodeName().contains("group")) {
							parseTypeFromComplexTypeNodeOfXSD(theWSDLxsdSchema,
									n, dummyCo);
							typeParsingFinished = true;
							break;

						} else if (n.getNodeName().contains("simpleType")) {
							parseTypeFromSimpleTypeNodeOfXSD(n, dummyCo);
							typeParsingFinished = true;
							break;
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
					// -System.exit(-5);

				}
			}

			if (part.getName() != null && part.getName().equals("parameters")) {
				if (mitsosOperationInput != null) {
					Iterator nativeObjsIter = dummyCo.getHasNativeObjects()
							.iterator();
					while (nativeObjsIter.hasNext()) {
						mitsosOperationInput.getHasNativeOrComplexObjects()
								.add(nativeObjsIter.next());
					}
					Iterator complexObjsIter = dummyCo.getHasComplexObjects()
							.iterator();
					while (complexObjsIter.hasNext()) {
						mitsosOperationInput.getHasNativeOrComplexObjects()
								.add(complexObjsIter.next());
					}
				} else if (mitsosOperationOutput != null) {
					Iterator nativeObjsIter = dummyCo.getHasNativeObjects()
							.iterator();
					while (nativeObjsIter.hasNext()) {
						mitsosOperationOutput.getHasNativeOrComplexObjects()
								.add(nativeObjsIter.next());
					}
					Iterator complexObjsIter = dummyCo.getHasComplexObjects()
							.iterator();
					while (complexObjsIter.hasNext()) {
						mitsosOperationOutput.getHasNativeOrComplexObjects()
								.add(complexObjsIter.next());
					}
				}
			} else {
				dummyCo.setObjectName(new QName(part.getName()));
				if (mitsosOperationInput != null) {
					// if(dummyCo.getHasNativeObjects().size()==1){
					// mitsosOperationInput.hasNativeOrComplexObjects.add(ta);
					// }
					if (dummyCo.getHasComplexObjects().size() == 0
							&& dummyCo.getHasNativeObjects().size() == 1) {
						NativeObject no1 = (NativeObject) dummyCo
								.getHasNativeObjects().get(0);
						no1.setObjectName(new QName(part.getName()));
						mitsosOperationInput.getHasNativeOrComplexObjects()
								.add(no1);
					} else {
						mitsosOperationInput.getHasNativeOrComplexObjects()
								.add(dummyCo);
					}
					/*
					 * Iterator
					 * nativeObjsIter=dummyCo.getHasNativeObjects().iterator();
					 * while(nativeObjsIter.hasNext()){
					 * mitsosOperationInput.hasNativeOrComplexObjects
					 * .add(nativeObjsIter.next()); } Iterator
					 * complexObjsIter=dummyCo
					 * .getHasComplexObjects().iterator();
					 * while(complexObjsIter.hasNext()){
					 * mitsosOperationInput.hasNativeOrComplexObjects
					 * .add(complexObjsIter.next()); }
					 */
				} else if (mitsosOperationOutput != null) {
					if (dummyCo.getHasComplexObjects().size() == 0
							&& dummyCo.getHasNativeObjects().size() == 1) {
						NativeObject no1 = (NativeObject) dummyCo
								.getHasNativeObjects().get(0);
						no1.setObjectName(new QName(part.getName()));
						mitsosOperationOutput.getHasNativeOrComplexObjects()
								.add(no1);
					} else {
						mitsosOperationOutput.getHasNativeOrComplexObjects()
								.add(dummyCo);
					}
					/*
					 * Iterator
					 * nativeObjsIter=dummyCo.getHasNativeObjects().iterator();
					 * while(nativeObjsIter.hasNext()){
					 * mitsosOperationOutput.hasNativeOrComplexObjects
					 * .add(nativeObjsIter.next()); } Iterator
					 * complexObjsIter=dummyCo
					 * .getHasComplexObjects().iterator();
					 * while(complexObjsIter.hasNext()){
					 * mitsosOperationOutput.hasNativeOrComplexObjects
					 * .add(complexObjsIter.next()); }
					 */
				}
			}

		}
	}

	public static void parseDocumentType_Deprecated(Definition definition,
			QName inPartType, WSOperationInput mitsosOperationInput,
			WSOperationOutput mitsosOperationOutput) {
		// ////-System.out.println("####### PARSING TYPES ########");

		// ////-System.out.println("\n\t\t\t\t\t Looking for TYPE: "+inPartType+"\n");
		List extElementsList = definition.getTypes().getExtensibilityElements();
		// ////-System.out.println("Extensibility Elements Names:");
		if (extElementsList != null) {
			// -System.out.print(extElementsList.size()+"\n");
			Iterator iter1 = extElementsList.iterator();
			while (iter1.hasNext()) {
				try {
					com.ibm.wsdl.extensions.schema.SchemaImpl s1 = (com.ibm.wsdl.extensions.schema.SchemaImpl) iter1
							.next();
					// //-System.out.println(s1.toString());

					org.w3c.dom.Element e1 = s1.getElement();
					NodeList children = e1.getChildNodes();

					// //-System.out.println(children.getLength());
					for (int i = 0; i < children.getLength(); i++) {
						Node n = children.item(i);
						// -System.out.println("### ####  MIIIIITS LITERALLLL "+n.getNodeName()+" "+n.getNodeType()+" "+n.getNodeValue());

						if (n.getNodeName() != null
								&& n.getNodeName().contains("import")) {

							// PARSE IMPORTED XSD...
							// //-System.out.println(n.getAttributes().getNamedItem("schemaLocation"));
							if (n.getAttributes() != null
									&& n.getAttributes().getNamedItem(
											"schemaLocation") != null
									&& n.getAttributes()
											.getNamedItem("schemaLocation")
											.getNodeValue() != null) {
								// //-System.out.println("FOUND AN XSD IMPORT!!!");
								// //-System.out.println(n.getAttributes().getNamedItem("schemaLocation").getNodeValue()+"\n");
								parseImportedXSDforLiteral(s1, inPartType,
										mitsosOperationInput,
										mitsosOperationOutput,
										inPartType.getNamespaceURI());
								// parseImportedXSDusingCastor(n.getAttributes().getNamedItem("schemaLocation").getNodeValue(),
								// inPartType, ta);
							}

						} else if (n.getNodeName() != null
								&& n.getNodeName().contains("include")) {
							// //-System.out.println(n.getAttributes().getNamedItem("schemaLocation"));
							if (n.getAttributes() != null
									&& n.getAttributes().getNamedItem(
											"schemaLocation") != null
									&& n.getAttributes()
											.getNamedItem("schemaLocation")
											.getNodeValue() != null) {
								// //-System.out.println("FOUND AN XSD INCLUDE!!!");
								// //-System.out.println(n.getAttributes().getNamedItem("schemaLocation").getNodeValue()+"\n");
								parseIncludedXSDforLiteral(s1, inPartType,
										mitsosOperationInput,
										mitsosOperationOutput);
							}

						}

						try {
							// //-System.out.println("FFFFFFFFFFFFFF");
							// //-System.out.println(n.getAttributes().getNamedItem("name"));
							// //-System.out.println(n.getAttributes().getNamedItem("name").getNodeType());
							// //-System.out.println(n.getAttributes().getNamedItem("name").getNodeValue());
							// //-System.out.println("FFFFFFFFFFFFFF");
						} catch (Exception e) {

						}

						if (n.getAttributes() != null
								&& n.getAttributes().getNamedItem("name") != null
								&& n.getAttributes().getNamedItem("name")
										.getNodeValue() != null
								&& n.getAttributes().getNamedItem("name")
										.getNodeValue()
										.equals(inPartType.getLocalPart())) {
							// -System.out.println("Element Found!!!!!!!!!!!!");
							NodeList childrenOfChildOfSchema = n
									.getChildNodes();
							if (childrenOfChildOfSchema != null) {
								for (int j = 0; j < childrenOfChildOfSchema
										.getLength(); j++) {
									Node n1 = childrenOfChildOfSchema.item(j);
									// -System.out.println("GAAAAAAAAAAAOOOOOOO "+n1.getNodeName());

									if (n1.getNodeName() != null
											&& !n1.getNodeName()
													.equals("#text")) {
										NodeList n2nodes = n1.getChildNodes();

										if (n2nodes != null
												&& n2nodes.getLength() > 0) {
											for (int i5 = 0; i5 < n2nodes
													.getLength(); i5++) {
												Node n2 = n2nodes.item(i5);
												NodeList childrenOfTheDamned = n2
														.getChildNodes();
												if (childrenOfTheDamned != null) {
													for (int k = 0; k < childrenOfTheDamned
															.getLength(); k++) {
														// -System.out.println("\tGAAAAAAAAAAAAAAAAAAOOOOOOOOOOO "+childrenOfTheDamned.item(k).getNodeName());

														if (childrenOfTheDamned
																.item(k)
																.getNodeName()
																.contains(
																		"element")) {
															NamedNodeMap attributesOfType = childrenOfTheDamned
																	.item(k)
																	.getAttributes();
															// //-System.out.println("WWW GAMWTOOOO...... TYPES...");
															String attName = "";
															String attType = "";
															String additionalInfo = "";

															// EDW EINAI OOOOLH
															// H MAGKIA MOU ME
															// TA
															// TYPES!!!!!!!!!!!!!!!
															// WWW GAMWTOOOO
															// WWWWWWWWWWWWWWWWWWWWWWWWWWWWWW!!!!!!!!!!!!!!!!!!!!!!!!
															// WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
															// WWWWW
															// W
															// W
															for (int k1 = 0; k1 < attributesOfType
																	.getLength(); k1++) {
																Node att = attributesOfType
																		.item(k1);
																if (att.getNodeName()
																		.equalsIgnoreCase(
																				"name")) {
																	// -System.out.println("\tName: "+att.getNodeValue());
																	attName = att
																			.getNodeValue();
																} else if (att
																		.getNodeName()
																		.equalsIgnoreCase(
																				"type")) {
																	// -System.out.println("\tType: "+att.getNodeValue());
																	attType = att
																			.getNodeValue();
																} else {
																	// -System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
																	additionalInfo += att
																			.getNodeName()
																			+ ":"
																			+ att.getNodeValue()
																			+ "   ";
																}
															}

															// -ta.append("\n\t\t\tName: "+attName);
															additionalInfo = additionalInfo
																	.trim();
															// -ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");

															// Find the Type of
															// the attName...
															// ITERATIVE
															// PROCESS.........
															if (attType
																	.startsWith(MitsosParser.nativeTypePrefix)) {
																// EINAI NATIVE
																// TYPE
																NativeObject no = new NativeObject();
																no.setObjectName(new QName(
																		attName));
																no.setAdditionalInfo(additionalInfo);
																no.setObjectType(new QName(
																		attType));
																if (mitsosOperationInput != null) {
																	mitsosOperationInput
																			.getHasNativeOrComplexObjects()
																			.add(no);
																} else if (mitsosOperationOutput != null) {
																	mitsosOperationOutput
																			.getHasNativeOrComplexObjects()
																			.add(no);
																}

															} else if (attType
																	.startsWith(MitsosParser.targetNamespacePrefix)) {
																// PSAXNW GIA TO
																// TYPE MESA STO
																// definition

																String type1 = attType
																		.substring(
																				MitsosParser.targetNamespacePrefix
																						.length(),
																				attType.length());
																ComplexObject co = new ComplexObject();
																co.setObjectName(new QName(
																		attName));
																co.setObjectType(new QName(
																		type1));

																if (type1
																		.startsWith("ArrayOf")) {
																	type1 = type1
																			.replaceFirst(
																					"ArrayOf",
																					"");
																	// -ta.append("  ("+type1+"[])");
																} else if (type1
																		.endsWith("Array")) {
																	type1 = type1
																			.substring(
																					0,
																					type1.length() - 5);
																	// -ta.append("  ("+type1+"[])");
																} else if (attType
																		.endsWith("[]")) {
																	type1 = type1
																			.replace(
																					"[]",
																					"");
																	// -ta.append("  ("+type1+"[])");
																}
																co.setObjectType(new QName(
																		type1));
																co.setAdditionalInfo(additionalInfo);

																MitsosParser
																		.parseTypeIterative(
																				s1,
																				type1,
																				0,
																				true,
																				co);

																if (mitsosOperationInput != null) {
																	mitsosOperationInput
																			.getHasNativeOrComplexObjects()
																			.add(co);
																} else if (mitsosOperationOutput != null) {
																	mitsosOperationOutput
																			.getHasNativeOrComplexObjects()
																			.add(co);
																}

															} else {

															}
														}
													}
												}
											}
										} else {
											// To Operation den exei INPUTS
											// -ta.append("\n\t\t\t\t\t-");
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// Parse EXT ELEMENTS
		// schema...
		// PSAXNW TO TYPE STA EXTENSIBILITY ELEMENTS
		// MitsosParser.parseExtElements(definition, inPartType, ta, null,
		// null);
	}

	private static void parseImportedXSDforLiteral3(
			javax.wsdl.extensions.schema.Schema theXSDschemaDefinedInTheWSDL,
			String typeName, int iterNumber, boolean fromLiteral,
			ComplexObject co, String xsdImportNamespace) {

		// -System.out.println("\n *** LOOKING FOR TYPE *** :      "+typeName);

		MitsosParser.parsedObjectsHashmap.put(co.getObjectType(), co);

		Map importsMap = theXSDschemaDefinedInTheWSDL.getImports();
		Iterator importsItt = importsMap.values().iterator();
		boolean typeParsingFinished = false;

		while (importsItt.hasNext()) {

			if (typeParsingFinished)
				break;

			List importsList = (List) importsItt.next();
			Iterator importsItt2 = importsList.iterator();
			while (importsItt2.hasNext()) {
				SchemaImportImpl schemaImportInitial = (SchemaImportImpl) importsItt2
						.next();
				SchemaImportImpl schemaImport = null;

				// ////-System.out.println(schemaImportInitial.getNamespaceURI());

				boolean currentSchemaIsTheRightOne = false;
				if (schemaImportInitial.getNamespaceURI() != null
						&& schemaImportInitial.getNamespaceURI().equals(
								xsdImportNamespace)) {
					currentSchemaIsTheRightOne = true;
					schemaImport = schemaImportInitial;
				} else {
					javax.wsdl.extensions.schema.Schema s11 = schemaImportInitial
							.getReferencedSchema();
					Map importsMap1 = s11.getImports();
					Iterator importsItt1 = importsMap1.values().iterator();
					while (importsItt1.hasNext()) {
						if (currentSchemaIsTheRightOne)
							break;
						List importsList1 = (List) importsItt1.next();
						Iterator importsItt21 = importsList1.iterator();
						while (importsItt21.hasNext()) {

							if (currentSchemaIsTheRightOne)
								break;

							schemaImportInitial = (SchemaImportImpl) importsItt21
									.next();
							// ////-System.out.println(schemaImportInitial.getNamespaceURI());
							if (schemaImportInitial.getNamespaceURI() != null
									&& schemaImportInitial.getNamespaceURI()
											.equals(xsdImportNamespace)) {
								currentSchemaIsTheRightOne = true;
								schemaImport = schemaImportInitial;
							}
						}
					}
				}

				if (schemaImport != null
						&& schemaImport.getNamespaceURI() != null
						&& schemaImport.getNamespaceURI().equals(
								xsdImportNamespace)) {
					javax.wsdl.extensions.schema.Schema importedSchema = schemaImport
							.getReferencedSchema();
					org.w3c.dom.Element impSchElem = importedSchema
							.getElement();
					if (impSchElem == null)
						continue;

					Attr attr = impSchElem.getAttributeNode(typeName);
					Attr att1 = impSchElem.getAttributeNodeNS(
							xsdImportNamespace, typeName);
					if (attr != null) {
						// -System.out.println(attr.getOwnerElement().getNodeName());
					}

					NodeList elementNodesList = impSchElem
							.getElementsByTagName("element");
					NodeList complexTypeNodesList = impSchElem
							.getElementsByTagName("complexType");
					NodeList simpleTypeNodesList = impSchElem
							.getElementsByTagName("simpleType");
					// -System.out.println(xsdImportNamespace);
					// //-System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength());

					boolean typeFoundOK = false;
					if (elementNodesList != null) {
						for (int i = 0; i < elementNodesList.getLength(); i++) {
							Node n = elementNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue().equals(typeName)) {
									// -System.out.println("ELEMENT");
									parseTypeFromElementNodeOfXSD(
											theXSDschemaDefinedInTheWSDL, n, co);
									typeParsingFinished = true;
									break;// Stamataei so psaksimo sta
											// elementNodes
								}
							}
						}
						if (typeParsingFinished)
							break;// Stamataei to psaksimo
					}

					if (complexTypeNodesList != null) {
						for (int i = 0; i < complexTypeNodesList.getLength(); i++) {
							Node n = complexTypeNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue().equals(typeName)) {
									// -System.out.println("COMPLEX TYPE");
									parseTypeFromComplexTypeNodeOfXSD(
											theXSDschemaDefinedInTheWSDL, n, co);
									typeParsingFinished = true;
									break;
								}
							}
						}
						if (typeParsingFinished)
							break;// Stamataei to psaksimo
					}

					if (simpleTypeNodesList != null) {
						for (int i = 0; i < simpleTypeNodesList.getLength(); i++) {
							Node n = simpleTypeNodesList.item(i);
							if (n.hasAttributes()
									&& n.getAttributes().getNamedItem("name") != null) {
								if (n.getAttributes().getNamedItem("name")
										.getNodeValue().equals(typeName)) {
									// -System.out.println("SIMPLE TYPE");
									parseTypeFromSimpleTypeNodeOfXSD(n, co);
									typeParsingFinished = true;
									break;
								}
							}
						}
						if (typeParsingFinished)
							break;// Stamataei to psaksimo
					}
				}
			}
		}
	}

	private static void parseImportedXSDforLiteral(SchemaImpl schemaImpl,
			QName inPartType, WSOperationInput operationInputs,
			WSOperationOutput operationOutputs, String xsdImportNamespace) {
		Map importsMap = schemaImpl.getImports();
		Iterator importsItt = importsMap.values().iterator();
		while (importsItt.hasNext()) {
			List importsList = (List) importsItt.next();
			Iterator importsItt2 = importsList.iterator();
			while (importsItt2.hasNext()) {
				SchemaImportImpl schemaImport = (SchemaImportImpl) importsItt2
						.next();
				// ////-System.out.println(schemaImport.getNamespaceURI());
				if (schemaImport.getNamespaceURI() != null
						&& schemaImport.getNamespaceURI().equals(
								xsdImportNamespace)) {
					javax.wsdl.extensions.schema.Schema importedSchema = schemaImport
							.getReferencedSchema();
					org.w3c.dom.Element impSchElem = importedSchema
							.getElement();
					if (impSchElem == null)
						continue;

					NodeList childrenOfChildOfSchema = impSchElem
							.getChildNodes();
					if (childrenOfChildOfSchema != null) {
						for (int j = 0; j < childrenOfChildOfSchema.getLength(); j++) {
							Node n1 = childrenOfChildOfSchema.item(j);
							// ////-System.out.println("ABCDEFG "+n1.getNodeName());

							if (n1.getAttributes() == null)
								continue;

							if (n1.getAttributes().getNamedItem("name") != null) {
								// ////-System.out.println("GAMWTO WWW WWW WWW: "+n1.getAttributes().getNamedItem("name").getNodeValue());
							}

							if (n1.getAttributes().getNamedItem("name") != null
									&& n1.getAttributes().getNamedItem("name")
											.getNodeValue() != null
									&& n1.getAttributes().getNamedItem("name")
											.getNodeValue()
											.equals(inPartType.getLocalPart())) {
								// VRETHIKE TO TYPE!!!

								NodeList childrenOfTheDamned = n1
										.getChildNodes();
								if (childrenOfTheDamned != null) {
									for (int k = 0; k < childrenOfTheDamned
											.getLength(); k++) {
										// ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
										NodeList childrenOfTheChildrenOfTheDamned = childrenOfTheDamned
												.item(k).getChildNodes();
										if (childrenOfTheChildrenOfTheDamned != null) {
											for (int k2 = 0; k2 < childrenOfTheChildrenOfTheDamned
													.getLength(); k2++) {
												if (childrenOfTheChildrenOfTheDamned
														.item(k2).getNodeName()
														.contains("element")) {
													NamedNodeMap attributesOfType = childrenOfTheChildrenOfTheDamned
															.item(k2)
															.getAttributes();
													// ////-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
													String attName = "";
													String attType = "";
													String additionalInfo = "";

													// EDW EINAI OOOOLH H MAGKIA
													// MOU ME TA
													// TYPES!!!!!!!!!!!!!!!
													for (int k1 = 0; k1 < attributesOfType
															.getLength(); k1++) {
														Node att = attributesOfType
																.item(k1);
														if (att.getNodeName()
																.equalsIgnoreCase(
																		"name")) {
															// ////-System.out.println("\tName: "+att.getNodeValue());
															attName = att
																	.getNodeValue();
														} else if (att
																.getNodeName()
																.equalsIgnoreCase(
																		"type")) {
															// ////-System.out.println("\tType: "+att.getNodeValue());
															attType = att
																	.getNodeValue();
														} else {
															// ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
															additionalInfo += att
																	.getNodeName()
																	+ ":"
																	+ att.getNodeValue()
																	+ "   ";
														}
													}

													// -ta.append("\n\t\t\tName: "+attName);
													additionalInfo = additionalInfo
															.trim();
													// -ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");

													// //-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

													// Find the Type of the
													// attName...
													// ITERATIVE
													// PROCESS.........
													if (attType
															.startsWith(MitsosParser.targetNamespacePrefix)) {
														// EINAI COMPLEX TYPE
														// //-ta.append("\n");
														// PSAXNW GIA TO TYPE
														// MESA STO definition
														// ////-System.out.println("#########################################  COMPLEX!!!! ITERATIVE");
														String type1 = attType
																.substring(
																		4,
																		attType.length());

														ComplexObject co = new ComplexObject();
														co.setObjectName(new QName(
																attName));
														co.setObjectType(new QName(
																attType));

														if (type1
																.startsWith("ArrayOf")) {
															type1 = type1
																	.replaceFirst(
																			"ArrayOf",
																			"");
															// -ta.append("  ("+type1+"[])");
														} else if (type1
																.endsWith("Array")) {
															type1 = type1
																	.substring(
																			0,
																			type1.length() - 5);
															// -ta.append("  ("+type1+"[])");
														} else if (attType
																.endsWith("[]")) {
															type1 = type1
																	.replace(
																			"[]",
																			"");
															// -ta.append("  ("+type1+"[])");
														}
														co.setObjectType(new QName(
																type1));

														co.setAdditionalInfo(additionalInfo);

														Node parsedAttribute = parseTypeIterativeForXSDImport(
																importedSchema,
																type1, 0, true,
																co);

														if (operationInputs != null) {
															operationInputs
																	.getHasNativeOrComplexObjects()
																	.add(co);
														} else if (operationOutputs != null) {
															operationOutputs
																	.getHasNativeOrComplexObjects()
																	.add(co);
														}

													} else {

														// Create new native
														// type kai valto mesa
														// sta INPUTS i OUTPUTS
														// tou current Operation
														if (MitsosParser.nativeTypePrefix != null
																&& attType
																		.startsWith(MitsosParser.nativeTypePrefix)) {
															if (operationInputs != null) {
																NativeObject no = new NativeObject();
																no.setObjectName(new QName(
																		attName));
																no.setObjectType(new QName(
																		attType));
																no.setAdditionalInfo(additionalInfo);
																operationInputs
																		.getHasNativeOrComplexObjects()
																		.add(no);
															} else if (operationOutputs != null) {
																NativeObject no = new NativeObject();
																no.setObjectName(new QName(
																		attName));
																no.setObjectType(new QName(
																		attType));
																no.setAdditionalInfo(additionalInfo);
																operationOutputs
																		.getHasNativeOrComplexObjects()
																		.add(no);
															}
														} else {
															if (attType
																	.contains(":")) {
																String key1 = attType
																		.substring(
																				0,
																				attType.indexOf(":"));
																String xsdNamespace = (String) MitsosParser.namespaces
																		.get(key1);
																if (xsdNamespace != null) {
																	// ////-System.out.println("Namespace Found!!!");
																	String type1 = attType
																			.substring(
																					key1.length() + 1,
																					attType.length());
																	ComplexObject newComplexObject = new ComplexObject();
																	newComplexObject
																			.setObjectName(new QName(
																					attName));
																	newComplexObject
																			.setAdditionalInfo(additionalInfo);
																	newComplexObject
																			.setObjectType(new QName(
																					type1));

																	// AN to
																	// type1
																	// anhkei se
																	// allo
																	// Namespace...
																	parseImportedXSDforLiteral3(
																			schemaImpl,
																			type1,
																			1,
																			true,
																			newComplexObject,
																			xsdNamespace);

																	if (operationInputs != null) {
																		operationInputs
																				.getHasNativeOrComplexObjects()
																				.add(newComplexObject);
																	} else if (operationOutputs != null) {
																		operationOutputs
																				.getHasNativeOrComplexObjects()
																				.add(newComplexObject);
																	}

																} else {
																	// ////-System.out.println("Namespace was null... Will be treated as NATIVE TYPE...");
																	if (operationInputs != null) {
																		NativeObject no = new NativeObject();
																		no.setObjectName(new QName(
																				attName));
																		no.setObjectType(new QName(
																				attType));
																		no.setAdditionalInfo(additionalInfo);
																		operationInputs
																				.getHasNativeOrComplexObjects()
																				.add(no);
																	} else if (operationOutputs != null) {
																		NativeObject no = new NativeObject();
																		no.setObjectName(new QName(
																				attName));
																		no.setObjectType(new QName(
																				attType));
																		no.setAdditionalInfo(additionalInfo);
																		operationOutputs
																				.getHasNativeOrComplexObjects()
																				.add(no);
																	}
																}
															} else {
																// ////-System.out.println("type Namespace was null... Will be treated as NATIVE TYPE...");
																if (operationInputs != null) {
																	NativeObject no = new NativeObject();
																	no.setObjectName(new QName(
																			attName));
																	no.setObjectType(new QName(
																			attType));
																	no.setAdditionalInfo(additionalInfo);
																	operationInputs
																			.getHasNativeOrComplexObjects()
																			.add(no);
																} else if (operationOutputs != null) {
																	NativeObject no = new NativeObject();
																	no.setObjectName(new QName(
																			attName));
																	no.setObjectType(new QName(
																			attType));
																	no.setAdditionalInfo(additionalInfo);
																	operationOutputs
																			.getHasNativeOrComplexObjects()
																			.add(no);
																}
															}
														}

													}
												}
											}
										}

									}
								}

							}
						}
					}
				}
			}
		}
	}

	private static void parseIncludedXSDforLiteral(SchemaImpl schemaImpl,
			QName inPartType, WSOperationInput operationInputs,
			WSOperationOutput operationOutputs) {

		// Hashtable importedXSDsNamespaces=new Hashtable();
		List includesList = schemaImpl.getIncludes();
		Iterator importsItt = includesList.iterator();
		// Iterator importsItt = importsMap.values().iterator();
		while (importsItt.hasNext()) {
			com.ibm.wsdl.extensions.schema.SchemaReferenceImpl schemaImport = (com.ibm.wsdl.extensions.schema.SchemaReferenceImpl) importsItt
					.next();

			javax.wsdl.extensions.schema.Schema importedSchema = schemaImport
					.getReferencedSchema();
			org.w3c.dom.Element impSchElem = importedSchema.getElement();
			if (impSchElem == null)
				continue;

			NodeList childrenOfChildOfSchema = impSchElem.getChildNodes();
			if (childrenOfChildOfSchema != null) {
				for (int j = 0; j < childrenOfChildOfSchema.getLength(); j++) {
					Node n1 = childrenOfChildOfSchema.item(j);
					// ////-System.out.println("ABCDEFG "+n1.getNodeName());

					if (n1.getAttributes() == null)
						continue;

					// SE AYTO TO EPIPEDO EINAI TA IMPORTED SCHEMA pou
					// yparxoun...
					if (n1.getAttributes().getNamedItem("name") != null) {
						Node nn1 = n1.getAttributes().getNamedItem("name");
						// ////-System.out.println("WWW GAMWTOOOO W:   "
						// +nn1.getNodeValue());
					}

					/*
					 * if(n1.getNodeName()!=null&&n1.getNodeName().contains("import"
					 * )){
					 * 
					 * importedXSDsNamespaces.put("one", new Integer(1));
					 * 
					 * 
					 * }
					 */

					if (n1.getAttributes().getNamedItem("name") != null
							&& n1.getAttributes().getNamedItem("name")
									.getNodeValue() != null
							&& n1.getAttributes().getNamedItem("name")
									.getNodeValue()
									.equals(inPartType.getLocalPart())) { // VRETHIKE
																			// TO
																			// TYPE!!!!!!!

						NodeList childrenOfTheDamned = n1.getChildNodes();
						if (childrenOfTheDamned != null) {
							if (childrenOfTheDamned.getLength() > 0) {
								for (int k = 0; k < childrenOfTheDamned
										.getLength(); k++) {
									// ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
									NodeList childrenOfTheChildrenOfTheDamned = childrenOfTheDamned
											.item(k).getChildNodes();
									if (childrenOfTheChildrenOfTheDamned != null) {
										for (int k2 = 0; k2 < childrenOfTheChildrenOfTheDamned
												.getLength(); k2++) {
											if (childrenOfTheChildrenOfTheDamned
													.item(k2).getNodeName()
													.contains("element")) {
												NamedNodeMap attributesOfType = childrenOfTheChildrenOfTheDamned
														.item(k2)
														.getAttributes();
												// ////-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
												String attName = "";
												String attType = "";
												String additionalInfo = "";

												// EDW EINAI OOOOLH H MAGKIA MOU
												// ME TA TYPES!!!!!!!!!!!!!!!
												for (int k1 = 0; k1 < attributesOfType
														.getLength(); k1++) {
													Node att = attributesOfType
															.item(k1);
													if (att.getNodeName()
															.equalsIgnoreCase(
																	"name")) {
														// ////-System.out.println("\tName: "+att.getNodeValue());
														attName = att
																.getNodeValue();
													} else if (att
															.getNodeName()
															.equalsIgnoreCase(
																	"type")) {
														// ////-System.out.println("\tType: "+att.getNodeValue());
														attType = att
																.getNodeValue();
													} else {
														// ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
														additionalInfo += att
																.getNodeName()
																+ ":"
																+ att.getNodeValue()
																+ "   ";
													}
												}

												// -ta.append("\n\t\t\tName: "+attName);
												additionalInfo = additionalInfo
														.trim();
												// -ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");

												// //-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

												// Find the Type of the
												// attName...
												// ITERATIVE PROCESS.........
												if (attType
														.startsWith(MitsosParser.nativeTypePrefix)) {
													// EINAI NATIVE TYPE
													// //-ta.append("\n");

													// Create new native type
													// kai valto mesa sta INPUTS
													// i OUTPUTS tou current
													// Operation
													if (operationInputs != null) {
														NativeObject no = new NativeObject();
														no.setObjectName(new QName(
																attName));
														no.setObjectType(new QName(
																attType));
														no.setAdditionalInfo(additionalInfo);
														operationInputs
																.getHasNativeOrComplexObjects()
																.add(no);
													} else if (operationOutputs != null) {
														NativeObject no = new NativeObject();
														no.setObjectName(new QName(
																attName));
														no.setObjectType(new QName(
																attType));
														no.setAdditionalInfo(additionalInfo);
														operationOutputs
																.getHasNativeOrComplexObjects()
																.add(no);
													}
												} else if (attType
														.startsWith(MitsosParser.targetNamespacePrefix)) {
													// PSAXNW GIA TO TYPE MESA
													// STO definition
													// ////-System.out.println("#########################################  COMPLEX!!!! ITERATIVE");
													String type1 = attType
															.substring(
																	4,
																	attType.length());

													ComplexObject co = new ComplexObject();
													co.setObjectName(new QName(
															attName));
													co.setObjectType(new QName(
															attType));

													if (type1
															.startsWith("ArrayOf")) {
														type1 = type1
																.replaceFirst(
																		"ArrayOf",
																		"");
														// -ta.append("  ("+type1+"[])");
													} else if (type1
															.endsWith("Array")) {
														type1 = type1
																.substring(
																		0,
																		type1.length() - 5);
														// -ta.append("  ("+type1+"[])");
													} else if (attType
															.endsWith("[]")) {
														type1 = type1.replace(
																"[]", "");
														// -ta.append("  ("+type1+"[])");
													}
													co.setObjectType(new QName(
															type1));

													co.setAdditionalInfo(additionalInfo);

													Node parsedAttribute = parseTypeIterativeForXSDImport(
															importedSchema,
															type1, 0, true, co);

													if (operationInputs != null) {
														operationInputs
																.getHasNativeOrComplexObjects()
																.add(co);
													} else if (operationOutputs != null) {
														operationOutputs
																.getHasNativeOrComplexObjects()
																.add(co);
													}

												} else {

												}
											}
										}
									}

								}
							} else {
								// des ta attributes///
								// ////-System.out.println("MITTTSSSSSSSSSSSSOOOOOOOOOOOOOOOOOOOOOOSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
								if (n1.getAttributes().getNamedItem("type")
										.getNodeValue() != null) {
									// ////-System.out.println(n1.getAttributes().getNamedItem("type").getNodeValue());
									// ////-System.out.println("MITTTSSSSSSSSSSSSOOOOOOOOOOOOOOOOOOOOOOSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");

									NamedNodeMap attributesOfType = n1
											.getAttributes();
									// ////-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
									String attName = "";
									String attType = "";
									String additionalInfo = "";

									for (int k1 = 0; k1 < attributesOfType
											.getLength(); k1++) {
										Node att = attributesOfType.item(k1);
										if (att.getNodeName().equalsIgnoreCase(
												"name")) {
											// ////-System.out.println("\tName: "+att.getNodeValue());
											attName = att.getNodeValue();
										} else if (att.getNodeName()
												.equalsIgnoreCase("type")) {
											// ////-System.out.println("\tType: "+att.getNodeValue());
											attType = att.getNodeValue();
										} else {
											// ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
											additionalInfo += att.getNodeName()
													+ ":" + att.getNodeValue()
													+ "   ";
										}
									}

									// -ta.append("\n\t\t\tName: "+attName);
									additionalInfo = additionalInfo.trim();
									// -ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");

									if (n1.getAttributes()
											.getNamedItem("type")
											.getNodeValue()
											.startsWith(
													MitsosParser.targetNamespacePrefix)) {// COMPLEX
																							// TYPE
										// PSAXNW GIA TO TYPE MESA STO
										// definition
										// ////-System.out.println("#########################################  COMPLEX!!!! ITERATIVE");
										String type1 = attType.substring(4,
												attType.length());

										ComplexObject co = new ComplexObject();
										co.setObjectName(new QName(attName));
										co.setObjectType(new QName(attType));

										if (type1.startsWith("ArrayOf")) {
											type1 = type1.replaceFirst(
													"ArrayOf", "");
											// -ta.append("  ("+type1+"[])");
										} else if (type1.endsWith("Array")) {
											type1 = type1.substring(0,
													type1.length() - 5);
											// -ta.append("  ("+type1+"[])");
										} else if (attType.endsWith("[]")) {
											type1 = type1.replace("[]", "");
											// -ta.append("  ("+type1+"[])");
										}
										co.setObjectType(new QName(type1));

										co.setAdditionalInfo(additionalInfo);

										Node parsedAttribute = parseTypeIterativeForXSDImport(
												importedSchema, type1, 0, true,
												co);

										if (operationInputs != null) {
											operationInputs
													.getHasNativeOrComplexObjects()
													.add(co);
										} else if (operationOutputs != null) {
											operationOutputs
													.getHasNativeOrComplexObjects()
													.add(co);
										}

									} else if (n1.getAttributes()
											.getNamedItem("type")
											.getNodeValue().contains(":")) {
										if (MitsosParser.nativeTypePrefix != null
												&& n1.getAttributes()
														.getNamedItem("type")
														.getNodeValue()
														.startsWith(
																MitsosParser.nativeTypePrefix)) {

										} else {// to namespace parapempei se
												// allo imported i included XSD

											try {
												Collection col = MitsosParser.namespaces
														.values();
												Set keySet = MitsosParser.namespaces
														.keySet();
												Iterator keyIterator = keySet
														.iterator();
												// ////-System.out.println("Looking for Namespace with prefix: "+n1.getAttributes().getNamedItem("type").getNodeValue());
												for (int i = 0; i < col.size(); i++) {
													Iterator iter1 = col
															.iterator();
													while (iter1.hasNext()
															&& keyIterator
																	.hasNext()) {
														String s = (String) iter1
																.next();
														String key = (String) keyIterator
																.next();
														// ////-System.out.println("\t"+key+" "+
														// s);

														if (key.contains(n1
																.getAttributes()
																.getNamedItem(
																		"type")
																.getNodeValue())) {
															// ////-System.out.println("GOT IT!!!... namespace: "+s);
															// CHECK Imports /
															// Includes for the
															// namespace found
															// HERE...

														}
													}
													// nativeTypePrefix=

												}
											} catch (Exception e) {
												MitsosParser.nativeTypePrefix = null;
											}

											/*
											 * parseImportedXSD
											 * schemaImpl.getImports();
											 * 
											 * schemaImpl.getIncludes();
											 */
										}

									} else {// NATIVE TYPE
										// EINAI NATIVE TYPE
										// //-ta.append("\n");

										// Create new native type kai valto mesa
										// sta INPUTS i OUTPUTS tou current
										// Operation
										if (operationInputs != null) {
											NativeObject no = new NativeObject();
											no.setObjectName(new QName(attName));
											no.setObjectType(new QName(attType));
											no.setAdditionalInfo(additionalInfo);
											operationInputs
													.getHasNativeOrComplexObjects()
													.add(no);
										} else if (operationOutputs != null) {
											NativeObject no = new NativeObject();
											no.setObjectName(new QName(attName));
											no.setObjectType(new QName(attType));
											no.setAdditionalInfo(additionalInfo);
											operationOutputs
													.getHasNativeOrComplexObjects()
													.add(no);
										}
									}
								}

							}
						}

					}

				}
			}
		}
	}

	public static Node parseTypeIterativeForXSDImport(
			javax.wsdl.extensions.schema.Schema s1, String typeName,
			int iterNumber, boolean fromLiteral, ComplexObject co) {
		Node result = null;
		// String inType1="name=\""+typeName+"\"";
		try {
			org.w3c.dom.Element e1 = s1.getElement();
			NodeList children = e1.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node n = children.item(i);
				// //-System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
				// //-System.out.println(n.getNodeName());
				NamedNodeMap atts = n.getAttributes();
				if (atts != null) {// &&!n.getNodeName().equals("#text")){
					for (int jj = 0; jj < atts.getLength(); jj++) {
						// //-System.out.println("---MITS "+typeName+" "+atts.item(jj).getNodeName()+" "+atts.item(jj).getNodeValue());
					}
					// ////-System.out.println("--- "+typeName+" "+atts.getNamedItem("name").getNodeValue());
					if (atts.getNamedItem("name") != null
							&& atts.getNamedItem("name").getNodeValue() != null
							&& atts.getNamedItem("name").getNodeValue()
									.equals(typeName)) {
						// VRETHIKE TO ZITOUMENO TYPE
						// //-System.out.println("#####################    EYRIKA EYRIKA!!!!!!!!!!!!!!!!!");
						NodeList childrenOfChildOfSchema = n.getChildNodes();
						if (childrenOfChildOfSchema != null) {
							for (int j = 0; j < childrenOfChildOfSchema
									.getLength(); j++) {
								Node n1 = childrenOfChildOfSchema.item(j);
								// ////-System.out.println(n1.getNodeName());
								NodeList childrenOfTheDamned = n1
										.getChildNodes();
								if (childrenOfTheDamned != null) {
									for (int k = 0; k < childrenOfTheDamned
											.getLength(); k++) {
										// ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
										if (childrenOfTheDamned.item(k)
												.getNodeName()
												.contains("element")) {
											NamedNodeMap attributesOfType = childrenOfTheDamned
													.item(k).getAttributes();
											// //-System.out.println("WWW GAMWTOOOO...... TYPES... ITERATIVE ");//
											// +typeName +" "+n.getNodeName() );
											String attName = "";
											String attType = "";
											String additionalInfo = "";
											for (int k1 = 0; k1 < attributesOfType
													.getLength(); k1++) {
												Node att = attributesOfType
														.item(k1);
												if (att.getNodeName()
														.equalsIgnoreCase(
																"name")) {
													// //-System.out.println("\tName: "+att.getNodeValue());
													attName = att
															.getNodeValue();
												} else if (att.getNodeName()
														.equalsIgnoreCase(
																"type")) {
													// //-System.out.println("\tType: "+att.getNodeValue());
													attType = att
															.getNodeValue();
												} else {
													// //-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
													additionalInfo += att
															.getNodeName()
															+ ":"
															+ att.getNodeValue()
															+ "   ";
												}
											}

											String prefix = "\n\t\t\t\t";
											if (!fromLiteral) {
												prefix += "\t";
											}
											if (iterNumber > 0) {
												for (int ff1 = 0; ff1 < iterNumber; ff1++) {
													prefix += "\t";
												}
											}

											// -ta.append(prefix+"-"+attName+" ["+attType+"]  "+additionalInfo);
											if (attType
													.startsWith(MitsosParser.targetNamespacePrefix)) {
												String type1 = attType
														.substring(
																MitsosParser.targetNamespacePrefix
																		.length(),
																attType.length());

												ComplexObject newComplexObject = new ComplexObject();
												newComplexObject
														.setObjectName(new QName(
																attName));
												newComplexObject
														.setAdditionalInfo(additionalInfo);
												newComplexObject
														.setObjectType(new QName(
																type1));

												if (type1.startsWith("ArrayOf")) {
													type1 = type1.replaceFirst(
															"ArrayOf", "");
													// -ta.append("  ("+type1+"[])");
													newComplexObject
															.setObjectType(new QName(
																	type1
																			+ "[]"));
												} else if (type1
														.endsWith("Array")) {
													type1 = type1.substring(0,
															type1.length() - 5);
													// -ta.append("  ("+type1+"[])");
													newComplexObject
															.setObjectType(new QName(
																	type1
																			+ "[]"));
												} else if (attType
														.endsWith("[]")) {
													type1 = type1.replace("[]",
															"");
													// -ta.append("  ("+type1+"[])");
													newComplexObject
															.setObjectType(new QName(
																	type1
																			+ "[]"));
												}

												parseTypeIterativeForXSDImport(
														s1, type1,
														iterNumber + 1,
														fromLiteral,
														newComplexObject);

												co.getHasComplexObjects().add(
														newComplexObject);
											} else {
												if (MitsosParser.nativeTypePrefix != null
														&& attType
																.startsWith(MitsosParser.nativeTypePrefix)) {
													NativeObject newNativeObject = new NativeObject();
													newNativeObject
															.setObjectName(new QName(
																	attName));
													newNativeObject
															.setAdditionalInfo(additionalInfo);
													newNativeObject
															.setObjectType(new QName(
																	attType));
													co.getHasNativeObjects()
															.add(newNativeObject);
												} else {
													if (attType.contains(":")) {
														String key1 = attType
																.substring(
																		0,
																		attType.indexOf(":"));
														String xsdNamespace = (String) MitsosParser.namespaces
																.get(key1);
														if (xsdNamespace != null) {
															// //-System.out.println("Namespace Found!!!");
															// //-System.out.println("Namespace Found!!!");
															String type1 = attType
																	.substring(
																			key1.length() + 1,
																			attType.length());
															ComplexObject newComplexObject = new ComplexObject();
															newComplexObject
																	.setObjectName(new QName(
																			attName));
															newComplexObject
																	.setAdditionalInfo(additionalInfo);
															newComplexObject
																	.setObjectType(new QName(
																			type1));
															parseImportedXSDforLiteral3(
																	s1,
																	type1,
																	iterNumber + 1,
																	fromLiteral,
																	newComplexObject,
																	xsdNamespace);
															co.getHasComplexObjects()
																	.add(newComplexObject);

														} else {
															// //-System.out.println("Namespace was null... Will be treated as NATIVE TYPE...");
															NativeObject newNativeObject = new NativeObject();
															newNativeObject
																	.setObjectName(new QName(
																			attName));
															newNativeObject
																	.setAdditionalInfo(additionalInfo);
															newNativeObject
																	.setObjectType(new QName(
																			attType));
															co.getHasNativeObjects()
																	.add(newNativeObject);
														}
													} else {
														// //-System.out.println("type Namespace was null... Will be treated as NATIVE TYPE...");
														NativeObject newNativeObject = new NativeObject();
														newNativeObject
																.setObjectName(new QName(
																		attName));
														newNativeObject
																.setAdditionalInfo(additionalInfo);
														newNativeObject
																.setObjectType(new QName(
																		attType));
														co.getHasNativeObjects()
																.add(newNativeObject);
													}

												}

											}

										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

}
