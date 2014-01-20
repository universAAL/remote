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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.wsdl.extensions.schema.Schema;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * 
 * This class is used for parsing XSD schemas.
 * 
 * @author kgiannou
 */

public class WSDL_XSD_SchemaToolkit {

	private static HashMap URIsAlreadyParsed;


	public static Node getNodeOfType_ITERATIVE(String nameOfNode,
			javax.wsdl.extensions.schema.Schema importedOrIncludedSchema,
			String previousXSDURI, Schema theSchemaOfTheWSDL) {

		if (nameOfNode == null || importedOrIncludedSchema == null)
			return null;

		if (URIsAlreadyParsed.containsKey(importedOrIncludedSchema
				.getDocumentBaseURI())) {
			return null;
		}

		// -System.out.println("### ### ITERATIVE Looking for Node:  "+nameOfNode
		// +"  in: "+importedOrIncludedSchema.getDocumentBaseURI());
		// -System.out.println("############# Previous XSD URI:"+previousXSDURI);
		// -System.out.println(importedOrIncludedSchema.getDocumentBaseURI().substring(importedOrIncludedSchema.getDocumentBaseURI().lastIndexOf("/")));

		if (nameOfNode.equals("gmd:PT_FreeText_PropertyType")) {
			// -System.out.println("AAA1");
		}
		Node res = null;

		String theNodeNameAttValue = "";
		String theNodeNamespaceKey = null;

		if (nameOfNode.contains(":")) {
			theNodeNamespaceKey = nameOfNode.substring(0,
					nameOfNode.indexOf(":"));
			theNodeNameAttValue = nameOfNode
					.substring(nameOfNode.indexOf(":") + 1);
		} else {
			theNodeNamespaceKey = null;
			theNodeNameAttValue = nameOfNode;
		}

		URIsAlreadyParsed
				.put(importedOrIncludedSchema.getDocumentBaseURI(), "");
		if (importedOrIncludedSchema != null) {
			org.w3c.dom.Element impSchElem = importedOrIncludedSchema
					.getElement();

			// EDW NA LAVW YP'OPSIN KAI TA "group" elements
			NodeList elementNodesList = impSchElem
					.getElementsByTagName("element");
			NodeList complexTypeNodesList = impSchElem
					.getElementsByTagName("complexType");
			NodeList simpleTypeNodesList = impSchElem
					.getElementsByTagName("simpleType");
			NodeList groupTypeNodesList = impSchElem
					.getElementsByTagName("group");
			// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength()+" "
			// +groupTypeNodesList.getLength());

			if (elementNodesList != null && complexTypeNodesList != null
					&& simpleTypeNodesList != null
					&& groupTypeNodesList != null) {
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
										.getElementsByTagName(prefix
												+ "element");
								complexTypeNodesList = impSchElem
										.getElementsByTagName(prefix
												+ "complexType");
								simpleTypeNodesList = impSchElem
										.getElementsByTagName(prefix
												+ "simpleType");
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

			if (elementNodesList != null) {
				for (int i = 0; i < elementNodesList.getLength(); i++) {
					Node n = elementNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: ELEMENT");
							return n;
						}
					}
				}
			}

			if (complexTypeNodesList != null) {
				for (int i = 0; i < complexTypeNodesList.getLength(); i++) {
					Node n = complexTypeNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: COMPLEX TYPE");
							return n;
						}
					}
				}
			}

			if (simpleTypeNodesList != null) {
				for (int i = 0; i < simpleTypeNodesList.getLength(); i++) {
					Node n = simpleTypeNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: SIMPLE TYPE");
							return n;
						}
					}
				}
			}

			if (groupTypeNodesList != null) {
				for (int i = 0; i < groupTypeNodesList.getLength(); i++) {
					Node n = groupTypeNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						// -System.out.println(n.getAttributes().getNamedItem("name").getNodeValue());
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: GROUP");
							return n;
						}
					}
				}
			}

			List includesList = importedOrIncludedSchema.getIncludes();
			if (includesList != null) {
				Iterator includesIter = includesList.iterator();
				while (includesIter.hasNext()) {
					SchemaReferenceImpl schImpl = (SchemaReferenceImpl) includesIter
							.next();
					Schema includedSchema = schImpl.getReferencedSchema();
					// -System.out.println("\t\t\tCALLED FROM 1");
					if (includedSchema.getDocumentBaseURI() != null
							&& previousXSDURI != null) {
						if (!includedSchema.getDocumentBaseURI().equals(
								previousXSDURI)) {
							Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
									includedSchema,
									importedOrIncludedSchema
											.getDocumentBaseURI(),
									theSchemaOfTheWSDL);
							if (n1 != null)
								return n1;
						} else {
							// -System.out.println("AAAAAAAAAAAAAAAAAAA!~!!!!!!!!!!!!!!!!");
						}
					}

					else if (includedSchema.getDocumentBaseURI() != null
							&& importedOrIncludedSchema.getDocumentBaseURI() != null) {
						Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
								includedSchema,
								importedOrIncludedSchema.getDocumentBaseURI(),
								theSchemaOfTheWSDL);
						if (n1 != null)
							return n1;
					}
					// Node n1=getNodeOfType(nameOfNode,includedSchema,null);
					// if(n1!=null)return n1;
				}
			}

			Map importsMap = importedOrIncludedSchema.getImports();
			if (importsMap != null) {
				Collection col = importsMap.values();
				Iterator importsIter = col.iterator();
				while (importsIter.hasNext()) {
					try {
						SchemaImportImpl schImpl = (SchemaImportImpl) importsIter
								.next();
						Schema importedSchema = schImpl.getReferencedSchema();
						if (importedSchema.getDocumentBaseURI() != null
								&& previousXSDURI != null) {
							if (!importedSchema.getDocumentBaseURI().equals(
									previousXSDURI)) {
								Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
										importedSchema,
										importedOrIncludedSchema
												.getDocumentBaseURI(),
										theSchemaOfTheWSDL);
								if (n1 != null)
									return n1;
							} else {
								// -System.out.println("AAAAAAAAAAAAAAAAAAA!~!!!!!!!!!!!!!!!!");
							}
						} else if (importedSchema.getDocumentBaseURI() != null
								&& importedOrIncludedSchema
										.getDocumentBaseURI() != null) {
							Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
									importedSchema,
									importedOrIncludedSchema
											.getDocumentBaseURI(),
									theSchemaOfTheWSDL);
							if (n1 != null)
								return n1;
						}

					} catch (Exception e) {
						// e.printStackTrace();
						try {
							importsIter = col.iterator();
							while (importsIter.hasNext()) {
								Vector schImpls = (Vector) importsIter.next();
								if (schImpls.size() > 0) {
									for (int i = 0; i < schImpls.size(); i++) {
										SchemaImportImpl schImpl = (SchemaImportImpl) schImpls
												.get(i);
										Schema importedSchema = schImpl
												.getReferencedSchema();

										if (importedSchema.getDocumentBaseURI() != null
												&& previousXSDURI != null) {
											if (!importedSchema
													.getDocumentBaseURI()
													.equals(previousXSDURI)) {
												Node n1 = getNodeOfType_ITERATIVE(
														nameOfNode,
														importedSchema,
														importedOrIncludedSchema
																.getDocumentBaseURI(),
														theSchemaOfTheWSDL);
												if (n1 != null)
													return n1;
											} else {
												// -System.out.println("AAAAAAAAAAAAAAAAAAA!~!!!!!!!!!!!!!!!!");
											}
										} else if (importedSchema
												.getDocumentBaseURI() != null
												&& importedOrIncludedSchema
														.getDocumentBaseURI() != null) {
											Node n1 = getNodeOfType_ITERATIVE(
													nameOfNode,
													importedSchema,
													importedOrIncludedSchema
															.getDocumentBaseURI(),
													theSchemaOfTheWSDL);
											if (n1 != null)
												return n1;
										}
									}
								}
							}
						} catch (Exception e1) {
							e.printStackTrace();

						}

					} finally {
					}
				}

			}
		}
		return null;

	}

	public static Node getNodeOfType(String nameOfNode,
			javax.wsdl.extensions.schema.Schema theSchemaOfTheWSDL,
			String previousXSDURI) {

		// previousXSDURI used gia na glitwsw stackOverFlow an to ena XSD kanei
		// import ena allo pou kanei import to "ena"
		URIsAlreadyParsed = new HashMap();
		if (nameOfNode.equals("gmd:PT_FreeText_PropertyType")) {
			// -System.out.println("AAA1");
		}
		Node res = null;
		// -System.out.println("### ### Looking for Node:  "+nameOfNode
		// +"  in: "+theSchemaOfTheWSDL.getDocumentBaseURI());
		// -System.out.println("############# Previous XSD URI:"+previousXSDURI);
		if (nameOfNode == null || theSchemaOfTheWSDL == null)
			return null;

		String theNodeNameAttValue = "";
		String theNodeNamespaceKey = null;

		if (nameOfNode.contains(":")) {
			theNodeNamespaceKey = nameOfNode.substring(0,
					nameOfNode.indexOf(":"));
			theNodeNameAttValue = nameOfNode
					.substring(nameOfNode.indexOf(":") + 1);
		} else {
			theNodeNamespaceKey = null;
			theNodeNameAttValue = nameOfNode;
		}

		Schema nsSchema = null;
		if (theNodeNamespaceKey != null) {
			String theNodeNamespace = (String) MitsosParser.namespaces
					.get(theNodeNamespaceKey);
			if (theNodeNamespace != null) {
				Schema nsSchema1 = getTheImportedOrIncludedSchemaWithTheSpecificNS(
						theSchemaOfTheWSDL, theNodeNamespace);
				if (nsSchema1 != null) {
					nsSchema = nsSchema1;
				} else {
					nsSchema = theSchemaOfTheWSDL;
				}
			}
		} else
			nsSchema = theSchemaOfTheWSDL;

		URIsAlreadyParsed.put(nsSchema.getDocumentBaseURI(), "");
		if (nsSchema != null) {
			org.w3c.dom.Element impSchElem = nsSchema.getElement();

			// EDW NA LAVW YP'OPSIN KAI TA "group" elements
			NodeList elementNodesList = impSchElem
					.getElementsByTagName("element");
			NodeList complexTypeNodesList = impSchElem
					.getElementsByTagName("complexType");
			NodeList simpleTypeNodesList = impSchElem
					.getElementsByTagName("simpleType");
			NodeList groupTypeNodesList = impSchElem
					.getElementsByTagName("group");
			// -System.out.println(elementNodesList.getLength()+" "+complexTypeNodesList.getLength()+" "+simpleTypeNodesList.getLength()+" "
			// +groupTypeNodesList.getLength());

			if (elementNodesList != null && complexTypeNodesList != null
					&& simpleTypeNodesList != null
					&& groupTypeNodesList != null) {
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
										.getElementsByTagName(prefix
												+ "element");
								complexTypeNodesList = impSchElem
										.getElementsByTagName(prefix
												+ "complexType");
								simpleTypeNodesList = impSchElem
										.getElementsByTagName(prefix
												+ "simpleType");
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

			if (elementNodesList != null) {
				for (int i = 0; i < elementNodesList.getLength(); i++) {
					Node n = elementNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: ELEMENT");
							return n;
						}
					}
				}
			}

			if (complexTypeNodesList != null) {
				for (int i = 0; i < complexTypeNodesList.getLength(); i++) {
					Node n = complexTypeNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: COMPLEX TYPE");
							return n;
						}
					}
				}
			}

			if (simpleTypeNodesList != null) {
				for (int i = 0; i < simpleTypeNodesList.getLength(); i++) {
					Node n = simpleTypeNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: SIMPLE TYPE");
							return n;
						}
					}
				}
			}

			if (groupTypeNodesList != null) {
				for (int i = 0; i < groupTypeNodesList.getLength(); i++) {
					Node n = groupTypeNodesList.item(i);
					if (n.hasAttributes()
							&& n.getAttributes().getNamedItem("name") != null) {
						// -System.out.println(n.getAttributes().getNamedItem("name").getNodeValue());
						if (n.getAttributes().getNamedItem("name")
								.getNodeValue().equals(theNodeNameAttValue)) {
							// -System.out.println("getNodeOfType: GROUP");
							return n;
						}
					}
				}
			}

			List includesList = nsSchema.getIncludes();
			if (includesList != null) {
				Iterator includesIter = includesList.iterator();
				while (includesIter.hasNext()) {
					SchemaReferenceImpl schImpl = (SchemaReferenceImpl) includesIter
							.next();
					Schema includedSchema = schImpl.getReferencedSchema();
					// -System.out.println("\t\t\tCALLED FROM 1");
					if (includedSchema.getDocumentBaseURI() != null
							&& previousXSDURI != null) {
						if (!includedSchema.getDocumentBaseURI().equals(
								previousXSDURI)) {
							Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
									includedSchema,
									theSchemaOfTheWSDL.getDocumentBaseURI(),
									theSchemaOfTheWSDL);
							if (n1 != null)
								return n1;
						} else {
							// -System.out.println("AAAAAAAAAAAAAAAAAAA!~!!!!!!!!!!!!!!!!");
						}
					}

					else if (includedSchema.getDocumentBaseURI() != null
							&& theSchemaOfTheWSDL.getDocumentBaseURI() != null) {
						Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
								includedSchema,
								theSchemaOfTheWSDL.getDocumentBaseURI(),
								theSchemaOfTheWSDL);
						if (n1 != null)
							return n1;
					}
					// Node n1=getNodeOfType(nameOfNode,includedSchema,null);
					// if(n1!=null)return n1;
				}
			}

			Map importsMap = nsSchema.getImports();
			if (importsMap != null) {
				Collection col = importsMap.values();
				Iterator importsIter = col.iterator();
				while (importsIter.hasNext()) {
					try {
						SchemaImportImpl schImpl = (SchemaImportImpl) importsIter
								.next();
						Schema importedSchema = schImpl.getReferencedSchema();
						if (importedSchema.getDocumentBaseURI() != null
								&& previousXSDURI != null) {
							if (!importedSchema.getDocumentBaseURI().equals(
									previousXSDURI)) {
								Node n1 = getNodeOfType_ITERATIVE(
										nameOfNode,
										importedSchema,
										theSchemaOfTheWSDL.getDocumentBaseURI(),
										theSchemaOfTheWSDL);
								if (n1 != null)
									return n1;
							} else {
								// -System.out.println("AAAAAAAAAAAAAAAAAAA!~!!!!!!!!!!!!!!!!");
							}
						} else if (importedSchema.getDocumentBaseURI() != null
								&& theSchemaOfTheWSDL.getDocumentBaseURI() != null) {
							Node n1 = getNodeOfType_ITERATIVE(nameOfNode,
									importedSchema,
									theSchemaOfTheWSDL.getDocumentBaseURI(),
									theSchemaOfTheWSDL);
							if (n1 != null)
								return n1;
						}

					} catch (Exception e) {
						// e.printStackTrace();
						// System.out.println(".-.-"+e.getMessage()+".-|||.-"+MitsosParser.ws.getServiceURL());//e.printStackTrace();
						try {
							importsIter = col.iterator();
							while (importsIter.hasNext()) {
								Vector schImpls = (Vector) importsIter.next();
								if (schImpls.size() > 0) {
									for (int i = 0; i < schImpls.size(); i++) {
										SchemaImportImpl schImpl = (SchemaImportImpl) schImpls
												.get(i);
										Schema importedSchema = schImpl
												.getReferencedSchema();

										if (importedSchema.getDocumentBaseURI() != null
												&& previousXSDURI != null) {
											if (!importedSchema
													.getDocumentBaseURI()
													.equals(previousXSDURI)) {
												Node n1 = getNodeOfType_ITERATIVE(
														nameOfNode,
														importedSchema,
														theSchemaOfTheWSDL
																.getDocumentBaseURI(),
														theSchemaOfTheWSDL);
												if (n1 != null)
													return n1;
											} else {
												// -System.out.println("AAAAAAAAAAAAAAAAAAA!~!!!!!!!!!!!!!!!!");
											}
										} else if (importedSchema
												.getDocumentBaseURI() != null
												&& theSchemaOfTheWSDL
														.getDocumentBaseURI() != null) {
											Node n1 = getNodeOfType_ITERATIVE(
													nameOfNode,
													importedSchema,
													theSchemaOfTheWSDL
															.getDocumentBaseURI(),
													theSchemaOfTheWSDL);
											if (n1 != null)
												return n1;
										}
									}
								}
							}
						} catch (Exception e1) {
							e.printStackTrace();

						}

					} finally {
					}
				}

			}
		}

		return res;

	}

	public static javax.wsdl.extensions.schema.Schema getTheImportedOrIncludedSchemaWithTheSpecificNS(
			javax.wsdl.extensions.schema.Schema theSchemaOfTheWSDL,
			String xsdNamespaceURI) {

		if (theSchemaOfTheWSDL == null)
			return null;

		Map importsMap = theSchemaOfTheWSDL.getImports();
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

				// -System.out.println(schemaImportInitial.getNamespaceURI());

				boolean currentSchemaIsTheRightOne = false;

				if (schemaImportInitial.getNamespaceURI() != null
						&& schemaImportInitial.getNamespaceURI().equals(
								xsdNamespaceURI)) {
					currentSchemaIsTheRightOne = true;
					schemaImport = schemaImportInitial;
					javax.wsdl.extensions.schema.Schema importedSchema = schemaImport
							.getReferencedSchema();
					if (importedSchema != null) {
						// -System.out.println("Base URI 1: "+importedSchema.getDocumentBaseURI());
					} else {
						// importedSchema

						List extElementsList = MitsosParser.theWSDLDefinition
								.getTypes().getExtensibilityElements();

						if (extElementsList != null) {
							// -System.out.print(extElementsList.size()+"\n");
							Iterator iter1 = extElementsList.iterator();
							while (iter1.hasNext()) {
								try {
									com.ibm.wsdl.extensions.schema.SchemaImpl theWSDLxsdSchema = (com.ibm.wsdl.extensions.schema.SchemaImpl) iter1
											.next();
									// -System.out.println(theWSDLxsdSchema.getElement().getAttribute("targetNamespace"));
									if (theWSDLxsdSchema.getElement()
											.getAttribute("targetNamespace")
											.equals(xsdNamespaceURI)) {
										return theWSDLxsdSchema;
									}
								} catch (Exception e) {
									// -System.out.println(e.getMessage());
								}
							}
						}

					}
					return importedSchema;

				} else {
					javax.wsdl.extensions.schema.Schema s11 = schemaImportInitial
							.getReferencedSchema();
					if (s11 != null) {
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
										&& schemaImportInitial
												.getNamespaceURI().equals(
														xsdNamespaceURI)) {
									currentSchemaIsTheRightOne = true;
									schemaImport = schemaImportInitial;
									javax.wsdl.extensions.schema.Schema importedSchema = schemaImport
											.getReferencedSchema();
									// -System.out.println("Base URI 2: "+importedSchema.getDocumentBaseURI());
									return importedSchema;
								}
							}
						}
					}
				}

				if (schemaImport != null
						&& schemaImport.getNamespaceURI() != null
						&& schemaImport.getNamespaceURI().equals(
								xsdNamespaceURI)) {
					javax.wsdl.extensions.schema.Schema importedSchema = schemaImport
							.getReferencedSchema();
					// -System.out.println("Base URI 3: "+importedSchema.getDocumentBaseURI());
					return importedSchema;
				}
			}
		}
		return null;

	}

}
