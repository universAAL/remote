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

import com.ibm.wsdl.PartImpl;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.util.WSDLDefinitionWrapper;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationOutput;


/**
 * 
 * This class is responsible for parsing a wsdl file
 * 
 * @author kgiannou
 */

public class Axis2ParserWrapper {

	public static boolean failedDueToAxisCreation;
	public static boolean failedDueParsing;

	public static ParsedWSDLDefinition parseWSDLwithAxis2(String wsdlURL) {
		failedDueToAxisCreation = false;
		failedDueParsing = false;
		ComplexTypesParser.parsedComplexObjectsHashmap = new HashMap();
		ParsedWSDLDefinition theDefinition = new ParsedWSDLDefinition();
		theDefinition.setParsingComments("OK");

		try {
			URL url = new URL(wsdlURL);
			theDefinition.setWsdlURL(new URL(wsdlURL));
			// URL url=new
			// URL("http://www-1.munimadrid.es/MappingService/wsdl/es/indra/askit/mappingws/MappingService.wsdl");
			// Needs XML validation OFF gia na paiksei???

			// URL url=new URL("http://www.verona.miz.it/mpk4/server?wsdl");
			// URL url=new
			// URL("http://138.4.10.246:8080/AskItHemService/AskItHemService?wsdl");
			// URL url=new
			// URL("http://195.251.117.248/TransportRequestWS/TransportRequestWS.asmx?WSDL");

			org.apache.axis2.client.Options options = new org.apache.axis2.client.Options();
			// options.setTimeOutInMilliSeconds(15000);
			options.setProperty("javax.wsdl.verbose", false);
			options.setProperty("javax.wsdl.importDocuments", true);

			// System.out.println("\nAccessing WSDL with AXIS...");
			org.apache.axis2.description.AxisService service = null;
			try {
				service = org.apache.axis2.description.AxisService
						.createClientSideAxisService(url, null, null, options);
				// -System.out.println("OK");
				getAllDependencies(theDefinition, service);
			} catch (Exception e) {
				theDefinition.setFailedDueToAxis2(true);
				theDefinition.getContainingErrors().add(e.toString());
				System.out.println("ERROR sto " + wsdlURL);
				System.out.println("Parsing with AXIS failed... ");
				System.out.println("REASON: ");
				e.printStackTrace();
				failedDueToAxisCreation = true;
			}

			if (service == null) {
				return null;
			}
			System.out.println(service.getName());
			theDefinition.setWebServiceName(new QName(service
					.getTargetNamespace(), service.getName(), service
					.getTargetNamespacePrefix()));
			theDefinition.setTargetNamespaceURI(service.getTargetNamespace());
			theDefinition.setServiceURL(service.getEndpointURL());

			boolean foundTheEndpoint = false;
			try {
				Iterator endpointsIter = service.getEndpoints().values()
						.iterator();
				while (endpointsIter.hasNext()) {
					AxisEndpoint ae = (AxisEndpoint) endpointsIter.next();
					if (!ae.getName().endsWith("12") && !foundTheEndpoint) {
						theDefinition.setServiceURL(ae.getEndpointURL());
						foundTheEndpoint = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				theDefinition.getContainingErrors().add("ERROR! @line ~93");
			}

			if (!foundTheEndpoint) {
				try {
					Iterator endpointsIter = service.getEndpoints().values()
							.iterator();
					while (endpointsIter.hasNext()) {
						AxisEndpoint ae = (AxisEndpoint) endpointsIter.next();
						if (!foundTheEndpoint) {
							theDefinition.setServiceURL(ae.getEndpointURL());
							foundTheEndpoint = true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					theDefinition.getContainingErrors().add("ERROR! @line ~93");
				}
			}

			// -System.out.println("\nParsing WSDL, please wait...");
			// -System.out.println(service.getNamespaceMap().size());
			Iterator opersIter = service.getOperations();

			while (opersIter.hasNext()) {
				AxisOperation oper = (AxisOperation) opersIter.next();
				// if (operationName.equals("") ||
				// operationName.equalsIgnoreCase(oper.getName().getLocalPart()))
				{

					WSOperation parsedOperation = new WSOperation();

					// -System.out.prinln("\n\n#########     OPERATION      ########");
					System.out.println(oper.getName() + "      [style: "
							+ oper.getStyle() + "]");
					System.out.println(oper.getSoapAction());
					parsedOperation.setOperationName(oper.getName()
							.getLocalPart());
					AxisMessage acx = (AxisMessage) oper
							.getChild(OutInAxisOperation.WSDL_MESSAGE_IN_MESSAGE);
					if (acx != null && acx.getElementQName() != null
							&& acx.getElementQName().getLocalPart() != null) {
						parsedOperation.setOperationName(acx.getElementQName()
								.getLocalPart());
					}

					parsedOperation.setHasStyle(oper.getStyle());
					parsedOperation
							.setHasDocumentation(oper.getDocumentation());
					System.out.println("SoapAction: " + oper.getSoapAction());
					parsedOperation.setHasBindingSoapAction(oper
							.getSoapAction());

					WSOperationInput operationInput = new WSOperationInput();
					WSOperationOutput operationOutput = new WSOperationOutput();

					parsedOperation.setHasInput(operationInput);
					parsedOperation.setHasOutput(operationOutput);
					parsedOperation.setBelongsToDefinition(theDefinition);

					theDefinition.getWsdlOperations().add(parsedOperation);

					Iterator msgIter = null;
					if (oper.getClass().getName()
							.contains("OutOnlyAxisOperation")) {
						System.out.println();
						AxisMessage m1 = oper
								.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
						Vector v1 = new Vector();
						v1.add(m1);
						msgIter = v1.iterator();

						// Change direction gia na diorthwthei to sfalma tou
						// Axis2 kai na mpei swsta to message se Input i Output
						if (m1.getDirection().equals("in")) {
							m1.setDirection("out");
						} else if (m1.getDirection().equals("out")) {
							m1.setDirection("in");
						}
					} else if (oper.getClass().getName()
							.contains("InOnlyAxisOperation")) {
						AxisMessage m1 = oper
								.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
						Vector v1 = new Vector();
						v1.add(m1);
						msgIter = v1.iterator();

						// Change direction gia na diorthwthei to sfalma tou
						// Axis2 kai na mpei swsta to message se Input i Output
						if (m1.getDirection().equals("in")) {
							m1.setDirection("out");
						} else if (m1.getDirection().equals("out")) {
							m1.setDirection("in");
						}
					} else {
						msgIter = oper.getMessages();
					}

					while (msgIter.hasNext()) {
						System.out.println("\nMESSAGE");
						AxisMessage msg = (AxisMessage) msgIter.next();

						// Get ELEMENT FROM the Service SCHEMA
						org.apache.ws.commons.schema.XmlSchemaElement el = null;
						if (msg.getElementQName() != null) {
							el = service
									.getSchemaElement(msg.getElementQName());
						} else {
							System.out.println();
						}

						if (el == null
								&& (oper.getClass().getName()
										.contains("InOnlyAxisOperation") || oper
										.getClass().getName()
										.contains("OutOnlyAxisOperation"))) {
							QName elementQName = null;
							System.out.println();
							ArrayList params = service.getParameters();
							for (int i = 0; i < params.size(); i++) {
								Parameter param = (Parameter) params.get(i);
								if (param.getName()
										.contains("wsdl4jDefinition")) {
									WSDLDefinitionWrapper definitionWrapper = (WSDLDefinitionWrapper) param
											.getValue();
									Map map1 = definitionWrapper.getMessages();
									Collection col1 = map1.values();
									Iterator iter2 = col1.iterator();
									while (iter2.hasNext()) {
										com.ibm.wsdl.MessageImpl msg1 = (com.ibm.wsdl.MessageImpl) iter2
												.next();
										if (msg1.getQName().getLocalPart()
												.equals(msg.getName())) {
											Map map2 = msg1.getParts();
											Collection col2 = map2.values();
											if (col2.size() == 1) {
												Iterator iter3 = col2
														.iterator();
												while (iter3.hasNext()) {
													PartImpl partImpl = (PartImpl) iter3
															.next();
													elementQName = partImpl
															.getElementName();
												}
											} else {
												System.out.println();
											}
										}

									}
								}
							}
							el = service.getSchemaElement(elementQName);
						}

						if (el == null) {
							continue;
						}
						// EDW FAINETAI AN EINAI SimpleType i ComplexType to
						// input / output
						// -System.out.prinln("\t\tSchemaType class: "+st.getClass());
						org.apache.ws.commons.schema.XmlSchemaComplexType ct = null;
						org.apache.ws.commons.schema.XmlSchemaSimpleType st1 = null;
						if (el.getSchemaType() != null
								&& el.getSchemaType().getClass().getName()
										.contains("XmlSchemaComplexType")) {
							ct = (org.apache.ws.commons.schema.XmlSchemaComplexType) el
									.getSchemaType();
						} else {
							st1 = (org.apache.ws.commons.schema.XmlSchemaSimpleType) el
									.getSchemaType();
						}

						if (ct != null) {

							if (ct != null) {
								if (ct.getParticle() != null) {
									// -System.out.prinln("\t\t" +
									// "Particle class: " +
									// ct.getParticle().getClass());
								}
								if (ct.getAttributes() != null) {
									// -System.out.prinln("\t\t" +
									// ct.getAttributes().getCount());
								}
							}

							org.apache.ws.commons.schema.XmlSchemaSequence particleSequence = null;
							if (ct.getParticle() != null
									&& ct.getParticle().getClass().getName()
											.contains("XmlSchemaSequence")) {
								particleSequence = (org.apache.ws.commons.schema.XmlSchemaSequence) ct
										.getParticle();

							} else if (ct.getParticle() != null
									&& ct.getParticle().getClass().getName()
											.contains("XmlSchemaAll")) {
								System.out.println();
								ComplexObject dummyInputOrOutputComplexObject = new ComplexObject();
								dummyInputOrOutputComplexObject
										.setObjectName(ct.getQName());

								XmlSchemaAll xsa = (XmlSchemaAll) ct
										.getParticle();
								AdditionalTypesParser.parseXmlSchemaAllType(
										service, xsa,
										dummyInputOrOutputComplexObject,
										theDefinition);
								if (dummyInputOrOutputComplexObject
										.getObjectName() == null) {
									System.out.println();
								}
								ParsingUtils
										.checkIfCOisAnyObjectType(dummyInputOrOutputComplexObject);

								System.out.println();

								if (msg.isWrapped()) {
									if (msg.getDirection().equals("in")) {
										operationInput
												.setHasSoapHeaders(parseSoapHeadersOfOperation(
														service, msg,
														theDefinition));

										System.out.println("\t\tREQUEST");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									} else if (msg.getDirection().equals("out")) {
										System.out.println("\t\tRESPONSE");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									}
								} else { // if NOT WRAPPED
									// TWRA GINETAI TO IDIO OPWS sto WRAPPED apo
									// panw...
									if (msg.getDirection().equals("in")) {
										operationInput
												.setHasSoapHeaders(parseSoapHeadersOfOperation(
														service, msg,
														theDefinition));

										System.out.println("\t\tREQUEST");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									} else if (msg.getDirection().equals("out")) {
										System.out.println("\t\tRESPONSE");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									}
								}
								System.out.println();
							} else {
								System.out.println();
								// theDefinition.getContainingErrors().add("WARNING @Axis2 line ~294 strange Input/Output Element encountered!");
							}

							if (particleSequence != null) {

								// operationInput.hasUse=msg.getSoapHeaders().toString();
								ComplexObject dummyInputOrOutputComplexObject = new ComplexObject();

								if (particleSequence.getMinOccurs() == 0) {
									dummyInputOrOutputComplexObject
											.setIsOptional(true);
								}

								System.out.println();

								// EDW NA GINEI CHECK sta namespaces gia na mpei
								// to katallilo prefix
								dummyInputOrOutputComplexObject
										.setObjectName(msg.getElementQName());

								/*
								 * if(dummyInputOrOutputComplexObject.getObjectName
								 * ().getLocalPart().contains("HotelAvailRQ")){
								 * System.out.println(); }
								 */

								if (particleSequence.getItems() != null) {
									System.out.println("\t\t"
											+ particleSequence.getItems()
													.getCount());
								}
								Iterator particleIter = particleSequence
										.getItems().getIterator();

								while (particleIter.hasNext()) {
									Object newObj1 = particleIter.next();
									if (newObj1 != null
											&& newObj1
													.getClass()
													.getName()
													.equals("org.apache.ws.commons.schema.XmlSchemaElement")) {

										org.apache.ws.commons.schema.XmlSchemaElement schElemOfType = (org.apache.ws.commons.schema.XmlSchemaElement) newObj1;

										System.out.println("\t\t\t"
												+ schElemOfType.getName()
												+ "   "
												+ schElemOfType
														.getSchemaTypeName());

										if (schElemOfType.getSchemaType() != null) {
											System.out.println("\t\t\t\t#"
													+ schElemOfType
															.getSchemaType()
															.getClass()
															.toString() + "#");

											boolean typeParsed = false;
											if (schElemOfType
													.getSchemaType()
													.getClass()
													.toString()
													.contains(
															"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
												NativeObject no1 = new NativeObject();
												no1.setObjectName(schElemOfType
														.getQName());
												ComplexObject unionCO = SimpleTypesParser
														.parseSimpleType(
																schElemOfType,
																null, no1,
																theDefinition,
																service);
												typeParsed = true;
												if (unionCO != null) {
													if (schElemOfType
															.getMaxOccurs() > 1) {
														ComplexObject noArrayCO = new ComplexObject();
														noArrayCO
																.setObjectName(no1
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
																.getHasComplexObjects()
																.add(unionCO);
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(noArrayCO);
														typeParsed = true;

													} else {
														typeParsed = true;
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(unionCO);
													}
													System.out.println();
												} else {
													if (no1 != null
															&& no1.getAdditionalInfo() != null
															&& no1.getAdditionalInfo()
																	.contains(
																			"isListType")) {
														if (schElemOfType
																.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();
															noArrayCO
																	.setObjectName(no1
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
															noArrayCO
																	.setIsArrayType(true);

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
															dummyInputOrOutputComplexObject
																	.getHasComplexObjects()
																	.add(noArrayCO);
															typeParsed = true;

														} else {
															typeParsed = true;
															ComplexObject noArrayCO = new ComplexObject();
															noArrayCO
																	.setObjectName(no1
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
															noArrayCO
																	.setIsOptional(no1
																			.isIsOptional());
															dummyInputOrOutputComplexObject
																	.getHasComplexObjects()
																	.add(noArrayCO);
														}
													} else {
														if (schElemOfType
																.getMaxOccurs() > 1) {
															ComplexObject noArrayCO = new ComplexObject();

															noArrayCO
																	.setObjectName(no1
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
															dummyInputOrOutputComplexObject
																	.getHasComplexObjects()
																	.add(noArrayCO);
															typeParsed = true;

														} else {
															typeParsed = true;
															dummyInputOrOutputComplexObject
																	.getHasNativeObjects()
																	.add(no1);
														}
													}
												}

											} else if (schElemOfType
													.getSchemaType()
													.getClass()
													.toString()
													.contains(
															"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
												ComplexObject co1 = new ComplexObject();

												ComplexTypesParser
														.parseComplexType(
																service,
																schElemOfType,
																null, co1,
																theDefinition,
																false);
												typeParsed = true;

												if (schElemOfType
														.getMaxOccurs() > 1) {
													if (!co1.isIsArrayType()) {
														ComplexObject co2 = new ComplexObject();
														co2.setObjectName(co1
																.getObjectName());
														co2.setObjectType(new QName(
																co1.getObjectType()
																		.getNamespaceURI(),
																co1.getObjectType()
																		.getLocalPart()
																		+ "[]",
																co1.getObjectType()
																		.getPrefix()));
														co2.setIsArrayType(true);
														co2.getHasComplexObjects()
																.add(co1);
														//
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(co2);
													} else {
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(co1);
													}
												} else {
													dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.add(co1);
												}

											}

											if (!typeParsed) {
												System.out
														.println("ERROR 1!!!!!!!!!!!!!!!!!! @line ~191");
												theDefinition
														.getContainingErrors()
														.add("ERROR 1!!!!!!!!!!!!!!!!!! @line ~191");
												// -System.exit(-1);
											}
										} else {

											// PREPEI NA PSAKSW TO schemaType
											// sto service
											org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType = null;
											if (schElemOfType
													.getSchemaTypeName() != null) {
												xmlSchemaType = ParsingUtils
														.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
																service,
																schElemOfType
																		.getSchemaTypeName());
												/*
												 * if (xmlSchemaType == null) {
												 * xmlSchemaType =
												 * parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement
												 * (service,
												 * schElemOfType.getSchemaTypeName
												 * ()); }
												 */
											} else if (schElemOfType
													.getRefName() != null) {
												xmlSchemaType = ParsingUtils
														.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
																service,
																schElemOfType
																		.getRefName());
												if (xmlSchemaType == null) {
													xmlSchemaType = ParsingUtils
															.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
																	service,
																	schElemOfType
																			.getRefName());
												}
											}

											if (xmlSchemaType == null) {
												// failedDueToAxisCreation=true;
												// return null;
												Object res123 = ParsingUtils
														.tryToFindAndParseAttributeForSpecificObject(
																theDefinition,
																service,
																schElemOfType
																		.getRefName());
												if (res123 != null) {
													if (res123
															.getClass()
															.getName()
															.contains(
																	"NativeObject")) {
														dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.add(res123);
														continue;
													} else if (res123
															.getClass()
															.getName()
															.contains(
																	"ComplexObject")) {
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(res123);
														continue;
													}

												} else {
													System.out
															.println("XA! W!");
													System.out
															.println("XA! W!");
													ComplexObject co123 = new ComplexObject();
													if (schElemOfType
															.getQName() != null) {
														co123.setObjectName(schElemOfType
																.getQName());
													} else if (schElemOfType
															.getRefName() != null) {
														co123.setObjectName(schElemOfType
																.getRefName());
													} else if (schElemOfType
															.getName() != null) {
														co123.setObjectName(new QName(
																schElemOfType
																		.getName()));
													} else {
														co123.setObjectName(new QName(
																"UNDEFINED variable name!"));
														theDefinition
																.getContainingErrors()
																.add("ERROR @line ~324... UNDEFINED Variable name!!!");
														System.out
																.println("ERROR @line ~324... UNDEFINED Variable name!!!");
													}

													co123.setObjectType(new QName(
															"Object"));
													dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.add(co123);
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
												// no1.setObjectName(schElemOfType.getQName());
												if (schElemOfType.getQName() != null) {
													no1.setObjectName(schElemOfType
															.getQName());
												} else if (schElemOfType
														.getRefName() != null) {
													no1.setObjectName(schElemOfType
															.getRefName());
												} else if (schElemOfType
														.getName() != null) {
													no1.setObjectName(new QName(
															schElemOfType
																	.getName()));
												} else {
													no1.setObjectName(new QName(
															"UNDEFINED variable name"));
													theDefinition
															.getContainingErrors()
															.add("WARNING @line ~457... UNDEFINED Variable name!!!");
													System.out
															.println("WARNING @line ~457... UNDEFINED Variable name!!!");
												}

												ComplexObject unionCO = SimpleTypesParser
														.parseSimpleType(null,
																xmlSchemaType,
																no1,
																theDefinition,
																service);
												if (unionCO == null) {
													if (schElemOfType != null
															&& (schElemOfType
																	.getMinOccurs() == 0 || schElemOfType
																	.isNillable())) {
														no1.setIsOptional(true);
													}

													System.out.println();
													typeParsed = true;
													if (schElemOfType
															.getMaxOccurs() > 1
															|| (no1.getAdditionalInfo() != null && no1
																	.getAdditionalInfo()
																	.contains(
																			"isListType"))) {
														ComplexObject noArrayCO = new ComplexObject();
														noArrayCO
																.setObjectName(no1
																		.getObjectName());
														noArrayCO
																.setObjectType(new QName(
																		no1.getObjectType()
																				.getNamespaceURI(),
																		no1.getObjectType()
																				+ "[]",
																		no1.getObjectType()
																				.getPrefix()));
														noArrayCO
																.setIsArrayType(true);
														noArrayCO
																.getHasNativeObjects()
																.add(no1);
														noArrayCO
																.setIsOptional(no1
																		.isIsOptional());
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(noArrayCO);
														typeParsed = true;

													} else {
														typeParsed = true;
														dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.add(no1);
													}
												} else {
													ComplexObject co1 = new ComplexObject();
													co1.setObjectName(no1
															.getObjectName());
													if (xmlSchemaType
															.getQName() != null) {
														co1.setObjectType(xmlSchemaType
																.getQName());
													} else {
														System.out.println();
													}
													co1.getHasComplexObjects()
															.add(unionCO);
													dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.add(co1);
												}

											} else if (xmlSchemaType
													.getClass()
													.toString()
													.contains(
															"org.apache.ws.commons.schema.XmlSchemaComplexType")) {

												ComplexObject co1 = new ComplexObject();
												if (schElemOfType.getQName() != null) {
													co1.setObjectName(schElemOfType
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
												} else if (schElemOfType
														.getRefName() != null) {
													co1.setObjectName(schElemOfType
															.getRefName());
												} else if (schElemOfType
														.getName() != null) {
													co1.setObjectName(new QName(
															schElemOfType
																	.getName()));
												} else {
													co1.setObjectName(new QName(
															"UNDEFINED variable name"));
													theDefinition
															.getContainingErrors()
															.add("WARNING @line ~411... UNDEFINED Variable name!!!");
													System.out
															.println("WARNING @line ~411... UNDEFINED Variable name!!!");
												}

												if (schElemOfType
														.getMinOccurs() == 0
														|| schElemOfType
																.isNillable()) {
													co1.setIsOptional(true);
												}

												System.out.println();

												ComplexTypesParser
														.parseComplexType(
																service, null,
																xmlSchemaType,
																co1,
																theDefinition,
																false);
												// if(co1.isIsArrayType()){
												// if(co1.getHasNativeObjects().size()+co1.getHasComplexObjects().size()>1){
												// ComplexObject co2=new
												// ComplexObject();
												// co2.setObjectName(co1.getObjectName());
												// co2.setObjectType(new
												// QName(co1.getObjectType().getNamespaceURI(),
												// co1.getObjectType().getLocalPart().replace("[]",
												// ""),
												// co1.getObjectType().getPrefix()));
												// co2.setHasParent(co1);
												// co2.setIsOptional(co1.isIsOptional());
												// co2.setIsAbstract(co1.isIsAbstract());
												// co2.setIsArrayType(false);
												// co2.setIsInput(co1.isIsInput());
												// co2.setHasComplexObjects(co1.getHasComplexObjects());
												// }
												// }
												typeParsed = true;

												if (schElemOfType != null
														&& (schElemOfType
																.getMinOccurs() == 0 || schElemOfType
																.isNillable())) {
													co1.setIsOptional(true);
												}

												if (schElemOfType != null
														&& schElemOfType
																.getMaxOccurs() > 1) {
													try {
														if (!((ComplexObject) co1
																.getHasComplexObjects()
																.get(0))
																.getObjectName()
																.getLocalPart()
																.equals(co1
																		.getObjectType()
																		.getLocalPart()
																		.replace(
																				"[]",
																				""))) {
															ComplexObject coArrayCO = new ComplexObject();
															coArrayCO
																	.setObjectName(co1
																			.getObjectName());
															// co1.setObjectType(new
															// QName("XA!"));
															if (!co1.getObjectType()
																	.getLocalPart()
																	.contains(
																			"[]")) {
																coArrayCO
																		.setObjectType(new QName(
																				co1.getObjectType()
																						.getNamespaceURI(),
																				co1.getObjectType()
																						.getLocalPart()
																						+ "[]",
																				co1.getObjectType()
																						.getPrefix()));
															} else {
																coArrayCO
																		.setObjectType(new QName(
																				co1.getObjectType()
																						.getNamespaceURI(),
																				co1.getObjectType()
																						.getLocalPart(),
																				co1.getObjectType()
																						.getPrefix()));
															}
															coArrayCO
																	.setIsArrayType(true);
															coArrayCO
																	.getHasComplexObjects()
																	.add(co1);
															coArrayCO
																	.setIsOptional(co1
																			.isIsOptional());
															dummyInputOrOutputComplexObject
																	.getHasComplexObjects()
																	.add(coArrayCO);
														} else {
															co1.setIsArrayType(true);
															dummyInputOrOutputComplexObject
																	.getHasComplexObjects()
																	.add(co1);
														}
													} catch (Exception ex) {
														ComplexObject coArrayCO = new ComplexObject();
														coArrayCO
																.setObjectName(co1
																		.getObjectName());
														// co1.setObjectType(new
														// QName("XA!"));
														if (!co1.getObjectType()
																.getLocalPart()
																.contains("[]")) {
															coArrayCO
																	.setObjectType(new QName(
																			co1.getObjectType()
																					.getNamespaceURI(),
																			co1.getObjectType()
																					.getLocalPart()
																					+ "[]",
																			co1.getObjectType()
																					.getPrefix()));
														} else {
															coArrayCO
																	.setObjectType(new QName(
																			co1.getObjectType()
																					.getNamespaceURI(),
																			co1.getObjectType()
																					.getLocalPart(),
																			co1.getObjectType()
																					.getPrefix()));
														}
														coArrayCO
																.setIsArrayType(true);
														coArrayCO
																.getHasComplexObjects()
																.add(co1);
														coArrayCO
																.setIsOptional(co1
																		.isIsOptional());
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(coArrayCO);
													}
												} else {
													dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.add(co1);
												}

												// }
											}

											if (!typeParsed) {
												// -System.out.prinln("ERROR 1!!!!!!!!!!!!!!!!!!");
												// -System.exit(-1);
											}

										}

									} else if (newObj1
											.getClass()
											.getName()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaAny")) {
										// EINAI XmlSchemaAny

										try {
											org.apache.ws.commons.schema.XmlSchemaAny newSimpleOrComplexObjectElement = (org.apache.ws.commons.schema.XmlSchemaAny) newObj1;

											ComplexObject co1 = new ComplexObject();
											co1.setObjectName(new QName("any"));
											co1.setObjectType(new QName(
													"Object"));

											if (newSimpleOrComplexObjectElement
													.getMinOccurs() == 0) {
												co1.setIsOptional(true);
											}

											if (newSimpleOrComplexObjectElement
													.getMaxOccurs() > 1) {
												// Array Type
												ComplexObject arrayCO = new ComplexObject();
												arrayCO.setObjectName(co1
														.getObjectName());
												arrayCO.setObjectType(new QName(
														co1.getObjectType()
																+ "[]"));
												arrayCO.setIsArrayType(true);
												arrayCO.getHasComplexObjects()
														.add(co1);
												arrayCO.setIsOptional(co1
														.isIsOptional());
												dummyInputOrOutputComplexObject
														.getHasComplexObjects()
														.add(arrayCO);
											} else {
												dummyInputOrOutputComplexObject
														.getHasComplexObjects()
														.add(co1);
											}

											// System.out.println("\t\t\t\t\t"+newSimpleOrComplexObjectElement.getName()+"   "+newSimpleOrComplexObjectElement.getSchemaTypeName());
											System.out.println("aaa!");
										} catch (Exception e) {
											e.printStackTrace();
											System.out.println("w!");
										}
										System.out.println("eee");

									} else if (newObj1
											.getClass()
											.getName()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaChoice")) {
										// EINAI XmlSchemaAny
										try {
											System.out.println();
											org.apache.ws.commons.schema.XmlSchemaChoice elem1 = (org.apache.ws.commons.schema.XmlSchemaChoice) newObj1;
											ComplexObject co1 = new ComplexObject();
											co1.setObjectName(new QName(
													"http://www.w3.org/2001/XMLSchema",
													"XmlSchemaChoice"));
											co1.setObjectType(new QName(
													"http://www.w3.org/2001/XMLSchema",
													"XmlSchemaChoice"));
											co1.setIsAbstract(true);

											AdditionalTypesParser
													.parseXMLSchemaChoiceElement(
															service, elem1,
															co1, theDefinition,
															false);

											if (elem1.getMinOccurs() == 0) {
												co1.setIsOptional(true);
											}

											if (elem1.getMaxOccurs() > 1) {
												// Array Type
												ComplexObject arrayCO1 = new ComplexObject();
												arrayCO1.setObjectName(co1
														.getObjectName());
												arrayCO1.setObjectType(new QName(
														co1.getObjectType()
																+ "[]"));
												arrayCO1.setIsArrayType(true);
												arrayCO1.getHasComplexObjects()
														.add(co1);
												arrayCO1.setIsOptional(co1
														.isIsOptional());
												dummyInputOrOutputComplexObject
														.getHasComplexObjects()
														.add(arrayCO1);
											} else {
												dummyInputOrOutputComplexObject
														.getHasComplexObjects()
														.add(co1);
											}
										} catch (Exception e) {
											e.printStackTrace();
											System.out.println("w!");
										}
										System.out.println("eee");

									} else if (newObj1
											.getClass()
											.getName()
											.contains(
													"org.apache.ws.commons.schema.XmlSchemaGroupRef")) {
										System.out.println();
										ComplexObject co1 = new ComplexObject();
										AdditionalTypesParser
												.parseXmlSchemaGroupRefElement(
														service,
														(org.apache.ws.commons.schema.XmlSchemaGroupRef) newObj1,
														co1, theDefinition);

										if (co1 != null) {
											for (int i = 0; i < co1
													.getHasComplexObjects()
													.size(); i++) {
												dummyInputOrOutputComplexObject
														.getHasComplexObjects()
														.add(co1.getHasComplexObjects()
																.get(i));
											}
											for (int i = 0; i < co1
													.getHasNativeObjects()
													.size(); i++) {
												dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.add(co1.getHasNativeObjects()
																.get(i));
											}
										} else {
											System.out.println();
										}
										System.out.println();

									} else {
										System.out
												.println("ERROR! Unknown Object Type");
									}

								}

								// Parse Attributes...
								if (ct != null) {
									XmlSchemaObjectCollection attsCol = ct
											.getAttributes();
									if (attsCol != null) {
										Iterator iter2 = attsCol.getIterator();
										while (iter2.hasNext()) {
											Object obj = iter2.next();
											if (obj.getClass()
													.getName()
													.equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
												org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;
												Object res1 = AdditionalTypesParser
														.parseXmlSchemaAttribute(
																att, service,
																theDefinition);
												if (res1 != null) {
													if (res1.getClass()
															.getName()
															.contains(
																	"NativeObject")) {
														NativeObject no12 = (NativeObject) res1;
														// System.out.println(no12.objectName);
														dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.add(no12);
													} else if (res1
															.getClass()
															.getName()
															.contains(
																	"ComplexObject")) {
														ComplexObject co12 = (ComplexObject) res1;
														// System.out.println(co12.objectName);
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
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
																co1,
																theDefinition);

												if (co1 != null) {
													for (int i = 0; i < co1
															.getHasComplexObjects()
															.size(); i++) {
														dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.add(co1.getHasComplexObjects()
																		.get(i));
													}
													for (int i = 0; i < co1
															.getHasNativeObjects()
															.size(); i++) {
														dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.add(co1.getHasNativeObjects()
																		.get(i));
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

								if (msg.isWrapped()) {
									if (msg.getDirection().equals("in")) {
										operationInput
												.setHasSoapHeaders(parseSoapHeadersOfOperation(
														service, msg,
														theDefinition));

										System.out.println("\t\tREQUEST");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									} else if (msg.getDirection().equals("out")) {
										System.out.println("\t\tRESPONSE");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									}
								} else { // if NOT WRAPPED
									// TWRA GINETAI TO IDIO OPWS sto WRAPPED apo
									// panw...
									if (msg.getDirection().equals("in")) {
										operationInput
												.setHasSoapHeaders(parseSoapHeadersOfOperation(
														service, msg,
														theDefinition));

										System.out.println("\t\tREQUEST");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									} else if (msg.getDirection().equals("out")) {
										System.out.println("\t\tRESPONSE");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									}
								}

							} else if (ct != null) {

								System.out.println();
								// CODE ADDED on 12-4-2010
								ComplexObject dummyInputOrOutputComplexObject = new ComplexObject();

								XmlSchemaObjectCollection attsCol = ct
										.getAttributes();

								// EDW NA GINEI CHECK sta namespaces gia na mpei
								// to katallilo prefix
								dummyInputOrOutputComplexObject
										.setObjectName(msg.getElementQName());

								if (attsCol != null) {
									Iterator iter2 = attsCol.getIterator();
									while (iter2.hasNext()) {
										Object obj = iter2.next();
										if (obj.getClass()
												.getName()
												.equals("org.apache.ws.commons.schema.XmlSchemaAttribute")) {
											org.apache.ws.commons.schema.XmlSchemaAttribute att = (org.apache.ws.commons.schema.XmlSchemaAttribute) obj;
											Object res1 = AdditionalTypesParser
													.parseXmlSchemaAttribute(
															att, service,
															theDefinition);
											if (res1 != null) {
												if (res1.getClass()
														.getName()
														.contains(
																"NativeObject")) {
													NativeObject no12 = (NativeObject) res1;
													// System.out.println(no12.objectName);
													dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.add(no12);
												} else if (res1
														.getClass()
														.getName()
														.contains(
																"ComplexObject")) {
													ComplexObject co12 = (ComplexObject) res1;
													// System.out.println(co12.objectName);
													dummyInputOrOutputComplexObject
															.getHasComplexObjects()
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
												for (int i = 0; i < co1
														.getHasComplexObjects()
														.size(); i++) {
													dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.add(co1.getHasComplexObjects()
																	.get(i));
												}
												for (int i = 0; i < co1
														.getHasNativeObjects()
														.size(); i++) {
													dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.add(co1.getHasNativeObjects()
																	.get(i));
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

								if (ct.getContentModel() != null
										&& ct.getContentModel()
												.getClass()
												.getName()
												.equals("org.apache.ws.commons.schema.XmlSchemaComplexContent")) {
									try {
										ComplexTypesParser
												.parseComplexContent(
														service,
														(org.apache.ws.commons.schema.XmlSchemaComplexContent) ct
																.getContentModel(),
														dummyInputOrOutputComplexObject,
														theDefinition, false,
														ct.getQName());
									} catch (Exception e) {
										e.printStackTrace();

										System.out.println();
									}

									System.out.println("a");
								} else if (ct.getContentModel() != null
										&& ct.getContentModel()
												.getClass()
												.getName()
												.equals("org.apache.ws.commons.schema.XmlSchemaSimpleContent")) {
									XmlSchemaSimpleContent simpleContent = (XmlSchemaSimpleContent) ct
											.getContentModel();
									SimpleTypesParser.parseSimpleContent(
											service, simpleContent,
											dummyInputOrOutputComplexObject,
											theDefinition);
								} else {
									System.out.println();
								}

								if (msg.isWrapped()) {
									if (msg.getDirection().equals("in")) {
										operationInput
												.setHasSoapHeaders(parseSoapHeadersOfOperation(
														service, msg,
														theDefinition));

										System.out.println("\t\tREQUEST");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									} else if (msg.getDirection().equals("out")) {
										System.out.println("\t\tRESPONSE");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									}
								} else { // if NOT WRAPPED
									// TWRA GINETAI TO IDIO OPWS sto WRAPPED apo
									// panw...
									if (msg.getDirection().equals("in")) {
										operationInput
												.setHasSoapHeaders(parseSoapHeadersOfOperation(
														service, msg,
														theDefinition));

										System.out.println("\t\tREQUEST");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationInput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									} else if (msg.getDirection().equals("out")) {
										System.out.println("\t\tRESPONSE");
										System.out.println(msg.getName());
										if (dummyInputOrOutputComplexObject
												.getHasComplexObjects().size() == 0
												&& dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.size() == 1) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(0));
										} else {
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasNativeObjects()
																.get(i));
											}
											for (int i = 0; i < dummyInputOrOutputComplexObject
													.getHasComplexObjects()
													.size(); i++) {
												operationOutput
														.getHasNativeOrComplexObjects()
														.add(dummyInputOrOutputComplexObject
																.getHasComplexObjects()
																.get(i));
											}
										}
									}
								}
							} else {
								System.out.println();
							}

						} else if (st1 != null) {
							ComplexObject dummyInputOrOutputComplexObject = new ComplexObject();
							NativeObject no = new NativeObject();
							no.setObjectName(el.getQName());
							ComplexObject unionCO = SimpleTypesParser
									.parseSimpleType(el, null, no,
											theDefinition, service);
							if (unionCO != null) {
								if (el.getMaxOccurs() > 1) {
									ComplexObject noArrayCO = new ComplexObject();
									noArrayCO.setObjectName(no.getObjectName());
									noArrayCO.setObjectType(new QName(no
											.getObjectType().getNamespaceURI(),
											no.getObjectType().getLocalPart()
													+ "[]", no.getObjectType()
													.getPrefix()));
									noArrayCO.setIsArrayType(true);
									noArrayCO.getHasComplexObjects().add(
											unionCO);
									dummyInputOrOutputComplexObject
											.getHasComplexObjects().add(
													noArrayCO);

								} else {
									dummyInputOrOutputComplexObject
											.getHasComplexObjects()
											.add(unionCO);
								}
								System.out.println();
							} else {
								if (no != null
										&& no.getAdditionalInfo() != null
										&& no.getAdditionalInfo().contains(
												"isListType")) {
									if (el.getMaxOccurs() > 1) {
										ComplexObject noArrayCO = new ComplexObject();
										noArrayCO.setObjectName(no
												.getObjectName());
										noArrayCO.setObjectType(new QName(no
												.getObjectType()
												.getNamespaceURI(), no
												.getObjectType().getLocalPart()
												+ "[][]", no.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);

										ComplexObject noArrayCO_ListNO = new ComplexObject();
										noArrayCO_ListNO.setObjectName(no
												.getObjectName());
										noArrayCO_ListNO
												.setObjectType(new QName(no
														.getObjectType()
														.getNamespaceURI(), no
														.getObjectType()
														.getLocalPart()
														+ "[]", no
														.getObjectType()
														.getPrefix()));
										noArrayCO_ListNO.setIsArrayType(true);
										noArrayCO_ListNO.setIsOptional(no
												.isIsOptional());
										noArrayCO_ListNO.getHasNativeObjects()
												.add(no);

										noArrayCO.getHasComplexObjects().add(
												noArrayCO_ListNO);
										dummyInputOrOutputComplexObject
												.getHasComplexObjects().add(
														noArrayCO);

									} else {
										ComplexObject noArrayCO = new ComplexObject();
										noArrayCO.setObjectName(no
												.getObjectName());
										noArrayCO.setObjectType(new QName(no
												.getObjectType()
												.getNamespaceURI(), no
												.getObjectType().getLocalPart()
												+ "[]", no.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);
										noArrayCO.getHasNativeObjects().add(no);
										noArrayCO.setIsOptional(no
												.isIsOptional());
										dummyInputOrOutputComplexObject
												.getHasComplexObjects().add(
														noArrayCO);
									}
								} else {
									if (el.getMaxOccurs() > 1) {
										ComplexObject noArrayCO = new ComplexObject();

										noArrayCO.setObjectName(no
												.getObjectName());
										noArrayCO.setObjectType(new QName(no
												.getObjectType()
												.getNamespaceURI(), no
												.getObjectType().getLocalPart()
												+ "[]", no.getObjectType()
												.getPrefix()));
										noArrayCO.setIsArrayType(true);
										noArrayCO.getHasNativeObjects().add(no);
										dummyInputOrOutputComplexObject
												.getHasComplexObjects().add(
														noArrayCO);

									} else {
										dummyInputOrOutputComplexObject
												.getHasNativeObjects().add(no);
									}
								}
							}

							if (msg.isWrapped()) {
								if (msg.getDirection().equals("in")) {
									operationInput
											.setHasSoapHeaders(parseSoapHeadersOfOperation(
													service, msg, theDefinition));

									System.out.println("\t\tREQUEST");
									System.out.println(msg.getName());
									if (dummyInputOrOutputComplexObject
											.getHasComplexObjects().size() == 0
											&& dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size() == 1) {
										operationInput
												.getHasNativeOrComplexObjects()
												.add(dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.get(0));
									} else {
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasNativeObjects().size(); i++) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(i));
										}
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasComplexObjects().size(); i++) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.get(i));
										}
									}
								} else if (msg.getDirection().equals("out")) {
									System.out.println("\t\tRESPONSE");
									System.out.println(msg.getName());
									if (dummyInputOrOutputComplexObject
											.getHasComplexObjects().size() == 0
											&& dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size() == 1) {
										operationOutput
												.getHasNativeOrComplexObjects()
												.add(dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.get(0));
									} else {
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasNativeObjects().size(); i++) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(i));
										}
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasComplexObjects().size(); i++) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.get(i));
										}
									}
								}
							} else { // if NOT WRAPPED
								// TWRA GINETAI TO IDIO OPWS sto WRAPPED apo
								// panw...
								if (msg.getDirection().equals("in")) {
									operationInput
											.setHasSoapHeaders(parseSoapHeadersOfOperation(
													service, msg, theDefinition));

									System.out.println("\t\tREQUEST");
									System.out.println(msg.getName());
									if (dummyInputOrOutputComplexObject
											.getHasComplexObjects().size() == 0
											&& dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size() == 1) {
										operationInput
												.getHasNativeOrComplexObjects()
												.add(dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.get(0));
									} else {
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasNativeObjects().size(); i++) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(i));
										}
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasComplexObjects().size(); i++) {
											operationInput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.get(i));
										}
									}
								} else if (msg.getDirection().equals("out")) {
									System.out.println("\t\tRESPONSE");
									System.out.println(msg.getName());
									if (dummyInputOrOutputComplexObject
											.getHasComplexObjects().size() == 0
											&& dummyInputOrOutputComplexObject
													.getHasNativeObjects()
													.size() == 1) {
										operationOutput
												.getHasNativeOrComplexObjects()
												.add(dummyInputOrOutputComplexObject
														.getHasNativeObjects()
														.get(0));
									} else {
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasNativeObjects().size(); i++) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasNativeObjects()
															.get(i));
										}
										for (int i = 0; i < dummyInputOrOutputComplexObject
												.getHasComplexObjects().size(); i++) {
											operationOutput
													.getHasNativeOrComplexObjects()
													.add(dummyInputOrOutputComplexObject
															.getHasComplexObjects()
															.get(i));
										}
									}
								}
							}
						}

						// Replace the modified direction of Message with the
						// Original created from Axis2
						if (oper.getClass().getName()
								.contains("OutOnlyAxisOperation")) {
							if (msg.getDirection().equals("in")) {
								msg.setDirection("out");
							} else if (msg.getDirection().equals("out")) {
								msg.setDirection("in");
							}
						} else if (oper.getClass().getName()
								.contains("InOnlyAxisOperation")) {
							if (msg.getDirection().equals("in")) {
								msg.setDirection("out");
							} else if (msg.getDirection().equals("out")) {
								msg.setDirection("in");
							}
						}
					}

					if (oper != null && oper.getFaultMessages() != null) {
						Iterator faultMsgsIter = oper.getFaultMessages()
								.iterator();
						while (faultMsgsIter.hasNext()) {
							Object obj = faultMsgsIter.next();
							System.out.println("\nMESSAGE");
							AxisMessage msg = (AxisMessage) obj;

							Object obj1 = getFaultMsgAsComplexObject(service,
									msg, theDefinition);
							if (obj1 != null) {
								operationOutput.getHasFaultMessageObjects()
										.add(obj1);
							} else {
								continue;
							}
							System.out.println();
						}
						System.out.println();
					}
					// System.out.println("OPER FOUND");
					// if(!operationName.equals("")){
					// // break;
					// }
				}
			}

			// ParsingUtils.updateAllComplexObjectsOfTheCOsHashMap();

		} catch (Exception e) {
			e.printStackTrace();
			theDefinition.setParsingComments(e.toString());
			failedDueParsing = true;
		}

		return theDefinition;

	}

	private static Vector parseSoapHeadersOfOperation(AxisService service,
			AxisMessage msg, ParsedWSDLDefinition theDefinition) {
		Vector result = new Vector();
		if (msg == null || msg.getSoapHeaders() == null
				|| msg.getSoapHeaders().size() == 0) {
			return result;
		}

		ArrayList headersList = msg.getSoapHeaders();
		Iterator iter1 = headersList.iterator();
		while (iter1.hasNext()) {
			// System.out.println(iter1.next().getClass().getName());
			try {
				org.apache.axis2.wsdl.SOAPHeaderMessage sh = (org.apache.axis2.wsdl.SOAPHeaderMessage) iter1
						.next();
				System.out.println(sh.getMessage().toString());
				// Prepei na vrw to element me QName: sh.getElement() mesa sta
				// types tou WSDL kai na to valw san Native i Complex Object

				ComplexObject headerCO = new ComplexObject();
				headerCO.setObjectName(sh.getMessage());
				headerCO.setObjectType(sh.getElement());
				// headerCO.setNamespaceURI(sh.getElement().getNamespaceURI());

				// Den kserw omws kata poso fernei panta to Axis swsta tin
				// pliroforia gia to an einai required to header...
				headerCO.setIsOptional(!sh.isRequired());

				XmlSchemaType xmlSchemaType1 = ParsingUtils
						.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
								service, sh.getElement());
				if (xmlSchemaType1 == null) {
					xmlSchemaType1 = ParsingUtils
							.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
									service, sh.getElement());
				}

				if (xmlSchemaType1.getClass().getName()
						.contains("XmlSchemaComplexType")) {
					ComplexTypesParser.parseComplexType(service, null,
							xmlSchemaType1, headerCO, theDefinition, true);
				} else {
					System.out.println("ERROR @line ~413");
					theDefinition.getContainingErrors().add("ERROR @line ~413");
				}

				result.add(headerCO);

			} catch (Exception e) {
				e.printStackTrace();
				theDefinition.getContainingErrors().add("ERROR! @line ~368");
			}

		}
		return result;
	}

	private static Object getFaultMsgAsComplexObject(AxisService service,
			AxisMessage msg, ParsedWSDLDefinition theDefinition) {
		try {
			if (msg == null) {
				return null;
			}
			// Get ELEMENT FROM the Service SCHEMA
			org.apache.ws.commons.schema.XmlSchemaElement schElemOfType = service
					.getSchemaElement(msg.getElementQName());

			if (schElemOfType == null) {
				System.out.println();
				return null;
			}

			if (schElemOfType
					.getSchemaType()
					.getClass()
					.toString()
					.contains(
							"org.apache.ws.commons.schema.XmlSchemaSimpleType")) {
				NativeObject no1 = new NativeObject();
				no1.setObjectName(schElemOfType.getQName());
				ComplexObject unionCO = SimpleTypesParser.parseSimpleType(
						schElemOfType, null, no1, theDefinition, service);
				if (unionCO != null) {
					System.out.println();
					if (schElemOfType.getMaxOccurs() > 1) {
						ComplexObject noArrayCO = new ComplexObject();

						noArrayCO.setObjectName(no1.getObjectName());
						noArrayCO.setObjectType(new QName(no1.getObjectType()
								.getNamespaceURI(), no1.getObjectType()
								.getLocalPart() + "[]", no1.getObjectType()
								.getPrefix()));
						noArrayCO.setIsArrayType(true);
						noArrayCO.getHasComplexObjects().add(unionCO);
						return noArrayCO;
					} else {
						return unionCO;
					}
				} else {
					if (no1 != null && no1.getAdditionalInfo() != null
							&& no1.getAdditionalInfo().contains("isListType")) {
						if (schElemOfType.getMaxOccurs() > 1) {
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
							return noArrayCO;

						} else {
							ComplexObject noArrayCO = new ComplexObject();
							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
							noArrayCO.setIsArrayType(true);
							noArrayCO.getHasNativeObjects().add(no1);
							noArrayCO.setIsOptional(no1.isIsOptional());
							return noArrayCO;

						}
					} else {
						if (schElemOfType.getMaxOccurs() > 1) {
							ComplexObject noArrayCO = new ComplexObject();

							noArrayCO.setObjectName(no1.getObjectName());
							noArrayCO.setObjectType(new QName(no1
									.getObjectType().getNamespaceURI(), no1
									.getObjectType().getLocalPart() + "[]", no1
									.getObjectType().getPrefix()));
							noArrayCO.setIsArrayType(true);
							noArrayCO.getHasNativeObjects().add(no1);
							return noArrayCO;

						} else {
							return no1;
						}
					}
				}

			} else if (schElemOfType
					.getSchemaType()
					.getClass()
					.toString()
					.contains(
							"org.apache.ws.commons.schema.XmlSchemaComplexType")) {
				ComplexObject co1 = new ComplexObject();
				ComplexTypesParser.parseComplexType(service, schElemOfType,
						null, co1, theDefinition, false);
				return co1;
				// dummyInputOrOutputComplexObject.getHasComplexObjects().add(co1);
			}

			return null;
		} catch (Exception e) {
			return null;
		}
	}

	static private void getAllDependencies(ParsedWSDLDefinition theDefinition,
			org.apache.axis2.description.AxisService service) {
		theDefinition.setXsdDependencies(new Vector());
		for (int i = 0; i < service.getSchema().size(); i++) {
			getXmlSchemaDependencies(theDefinition, ((XmlSchema) service
					.getSchema().get(i)));
		}
		for (int i = 0; i < theDefinition.getXsdDependencies().size(); i++) {
			if (((String) theDefinition.getXsdDependencies().get(i))
					.contains("../")) {
				while (((String) theDefinition.getXsdDependencies().get(i))
						.contains("../")) {
					theDefinition
							.getXsdDependencies()
							.setElementAt(
									((String) theDefinition
											.getXsdDependencies().get(i)).replace(
											"../", ""), i);
				}
				theDefinition.getXsdDependencies().setElementAt(
						"./"
								+ ((String) theDefinition.getXsdDependencies()
										.get(i)), i);
			}
		}
	}

	static private void getXmlSchemaDependencies(
			ParsedWSDLDefinition theDefinition, XmlSchema xml) {
		Iterator it = xml.getIncludes().getIterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj != null) {
				if (obj instanceof XmlSchemaImport) {
					XmlSchemaImport xmlSchemaImport = (XmlSchemaImport) obj;
					if (xmlSchemaImport.getSchema() != null) {
						if (!theDefinition.getXsdDependencies().contains(
								xmlSchemaImport.getSchema().getSourceURI())) {
							// String path=xmlSchemaImport.getSchemaLocation();
							String path = xmlSchemaImport.getSchema()
									.getSourceURI();
							theDefinition.getXsdDependencies().add(path);
							// System.out.println(path);
							getXmlSchemaDependencies(theDefinition,
									xmlSchemaImport.getSchema());
						}
					}
				} else if (obj instanceof XmlSchemaInclude) {
					XmlSchemaInclude xmlSchemaInclude = (XmlSchemaInclude) obj;
					if (!theDefinition.getXsdDependencies().contains(
							xmlSchemaInclude.getSchema().getSourceURI())) {
						// String path=xmlSchemaInclude.getSchemaLocation();

						String path = xmlSchemaInclude.getSchema()
								.getSourceURI();
						theDefinition.getXsdDependencies().add(path);
						// System.out.println(path);
						getXmlSchemaDependencies(theDefinition,
								xmlSchemaInclude.getSchema());
					}
				}
			}
		}
	}
}
