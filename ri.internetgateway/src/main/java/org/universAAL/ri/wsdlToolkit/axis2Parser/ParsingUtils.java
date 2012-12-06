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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroup;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;

public class ParsingUtils {

	public static XmlSchemaType parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(
			AxisService service, QName schemaTypeName) {
		if (schemaTypeName == null) {
			// -System.out.prinln("NULL!");
			return null;
		}
		ArrayList schemasList = service.getSchema();
		if (schemasList == null)
			return null;
		for (int i = 0; i < schemasList.size(); i++) {
			org.apache.ws.commons.schema.XmlSchema s = service.getSchema(i);
			XmlSchemaType schemaType = s.getTypeByName(schemaTypeName);
			if (schemaType != null) {
				return schemaType;
			}
		}

		return null;

	}

	public static XmlSchemaType parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(
			AxisService service, QName schemaTypeName) {
		if (schemaTypeName == null) {
			// -System.out.prinln("NULL!");
			return null;
		}
		ArrayList schemasList = service.getSchema();
		if (schemasList == null)
			return null;
		for (int i = 0; i < schemasList.size(); i++) {
			org.apache.ws.commons.schema.XmlSchema s = service.getSchema(i);
			System.out.println("2: " + s.getTargetNamespace());
			XmlSchemaElement schemaElement = s.getElementByName(schemaTypeName);
			if (schemaElement != null) {
				return schemaElement.getSchemaType();
			} else {
				/*
				 * XmlSchemaObject
				 * obj1=searchSchemaIncludesForAttribute_ITERATIVE(s,
				 * schemaTypeName); if(obj1!=null){ System.out.println(); }else{
				 * System.out.println(); }
				 */
			}
		}
		return null;

	}

	/*
	 * private static Vector parsedSchemasForAttSearch_ITERATIVE;
	 * 
	 * private static boolean schemaHasAlreadyBeenParsed(String schemaName){
	 * if(parsedSchemasForAttSearch_ITERATIVE==null||schemaName==null)return
	 * false;
	 * 
	 * for(int i=0;i<parsedSchemasForAttSearch_ITERATIVE.size();i++){
	 * if(schemaName.equals(parsedSchemasForAttSearch_ITERATIVE.get(i))){//Na
	 * ftiaksw ton elegxo return true; } } return false; }
	 */

	public static XmlSchemaAttribute parseWSDLschemasInOrderToFindTheSpecificXMLSchemaAttribute(
			AxisService service, QName schemaTypeName) {
		if (schemaTypeName == null) {
			// -System.out.prinln("NULL!");
			return null;
		}
		// parsedSchemasForAttSearch_ITERATIVE=new Vector();

		XmlSchemaObject schemaAttributeObject = null;

		ArrayList schemasList = service.getSchema();
		if (schemasList == null)
			return null;
		for (int i = 0; i < schemasList.size(); i++) {
			org.apache.ws.commons.schema.XmlSchema s = service.getSchema(i);
			System.out.println("2: " + s.getTargetNamespace());

			XmlSchemaObjectTable attsCollectionTable = s.getAttributes();
			if (attsCollectionTable == null)
				continue;
			schemaAttributeObject = attsCollectionTable.getItem(schemaTypeName);
			if (schemaAttributeObject != null) {
				if (schemaAttributeObject.getClass().getName()
						.contains("XmlSchemaAttribute")) {
					return (XmlSchemaAttribute) schemaAttributeObject;
					// break;
				} else {
					System.out.println();
				}
			} else {
				schemaAttributeObject = searchSchemaIncludesForAttribute_ITERATIVE_10_IterationsMax(
						s, schemaTypeName, 0);
				if (schemaAttributeObject != null) {
					if (schemaAttributeObject.getClass().getName()
							.contains("XmlSchemaAttribute")) {
						// break;
						return (XmlSchemaAttribute) schemaAttributeObject;
					} else {
						System.out.println();
					}
				}
			}
		}

		if (schemaAttributeObject == null) {
			return null;
		} else {
			System.out.println();
			return null;
		}

		/*
		 * try{ XmlSchemaAttribute
		 * schemaAttribute=(XmlSchemaAttribute)schemaAttributeObject;
		 * if(schemaAttribute.getSchemaType()!=null){ System.out.println();
		 * return schemaAttribute.getSchemaType(); }else
		 * if(schemaAttribute.getSchemaTypeName()!=null){ XmlSchemaType
		 * xmlSchemaType
		 * =parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
		 * schemaAttribute.getSchemaTypeName()); if(xmlSchemaType==null){
		 * xmlSchemaType
		 * =parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service,
		 * schemaAttribute.getSchemaTypeName()); } if(xmlSchemaType!=null){
		 * return xmlSchemaType; }else{ System.out.println(); } }
		 * }catch(Exception e){ e.printStackTrace(); System.out.println(); }
		 * 
		 * 
		 * return null;
		 */

	}

	public static XmlSchemaGroup parseWSDLschemasInOrderToFindTheSpecificXMLSchemaGroup(
			AxisService service, QName schemaTypeName) {
		if (schemaTypeName == null) {
			// -System.out.prinln("NULL!");
			return null;
		}
		// parsedSchemasForAttSearch_ITERATIVE=new Vector();

		XmlSchemaObject schemaGroupObject = null;

		ArrayList schemasList = service.getSchema();
		if (schemasList == null)
			return null;
		for (int i = 0; i < schemasList.size(); i++) {
			org.apache.ws.commons.schema.XmlSchema s = service.getSchema(i);
			System.out.println("2: " + s.getTargetNamespace());

			XmlSchemaObjectTable groupsCollectionTable = s.getGroups();
			if (groupsCollectionTable == null)
				continue;

			schemaGroupObject = groupsCollectionTable.getItem(schemaTypeName);
			if (schemaGroupObject != null) {
				if (schemaGroupObject.getClass().getName()
						.contains("XmlSchemaGroup")) {
					return (XmlSchemaGroup) schemaGroupObject;
					// break;
				} else {
					System.out.println();
				}
			} else {
				System.out.println();
				schemaGroupObject = searchSchemaIncludesForGroup_ITERATIVE_10_IterationsMax(
						s, schemaTypeName, 0);
				if (schemaGroupObject != null) {
					if (schemaGroupObject.getClass().getName()
							.contains("XmlSchemaGroup")) {
						// break;
						return (XmlSchemaGroup) schemaGroupObject;
					} else {
						System.out.println();
					}
				}
			}
		}

		if (schemaGroupObject == null) {
			return null;
		} else {
			System.out.println();
			return null;
		}

		/*
		 * try{ XmlSchemaAttribute
		 * schemaAttribute=(XmlSchemaAttribute)schemaAttributeObject;
		 * if(schemaAttribute.getSchemaType()!=null){ System.out.println();
		 * return schemaAttribute.getSchemaType(); }else
		 * if(schemaAttribute.getSchemaTypeName()!=null){ XmlSchemaType
		 * xmlSchemaType
		 * =parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service,
		 * schemaAttribute.getSchemaTypeName()); if(xmlSchemaType==null){
		 * xmlSchemaType
		 * =parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service,
		 * schemaAttribute.getSchemaTypeName()); } if(xmlSchemaType!=null){
		 * return xmlSchemaType; }else{ System.out.println(); } }
		 * }catch(Exception e){ e.printStackTrace(); System.out.println(); }
		 * 
		 * 
		 * return null;
		 */

	}

	public static XmlSchemaAttributeGroup parseWSDLschemasInOrderToFindTheSpecificXMLSchemaAttributeGroup(
			AxisService service, QName schemaTypeName) {
		if (schemaTypeName == null) {
			// -System.out.prinln("NULL!");
			return null;
		}
		// parsedSchemasForAttSearch_ITERATIVE=new Vector();

		XmlSchemaObject schemaAttributeGroupObject = null;

		ArrayList schemasList = service.getSchema();
		if (schemasList == null)
			return null;
		for (int i = 0; i < schemasList.size(); i++) {
			org.apache.ws.commons.schema.XmlSchema s = service.getSchema(i);
			System.out.println("2: " + s.getTargetNamespace());

			XmlSchemaObjectTable groupsCollectionTable = s.getAttributeGroups();
			if (groupsCollectionTable == null)
				continue;

			schemaAttributeGroupObject = groupsCollectionTable
					.getItem(schemaTypeName);
			if (schemaAttributeGroupObject != null) {
				if (schemaAttributeGroupObject.getClass().getName()
						.contains("XmlSchemaAttributeGroup")) {
					return (XmlSchemaAttributeGroup) schemaAttributeGroupObject;
					// break;
				} else {
					System.out.println();
				}
			} else {
				System.out.println();
				schemaAttributeGroupObject = searchSchemaIncludesForAttributeGroup_ITERATIVE_10_IterationsMax(
						s, schemaTypeName, 0);
				if (schemaAttributeGroupObject != null) {
					if (schemaAttributeGroupObject.getClass().getName()
							.contains("XmlSchemaAttributeGroup")) {
						// break;
						return (XmlSchemaAttributeGroup) schemaAttributeGroupObject;
					} else {
						System.out.println();
					}
				}
			}
		}

		if (schemaAttributeGroupObject == null) {
			return null;
		} else {
			System.out.println();
			return null;
		}

	}

	public static XmlSchemaObject searchSchemaIncludesForGroup_ITERATIVE_10_IterationsMax(
			org.apache.ws.commons.schema.XmlSchema initialSchema,
			QName schemaTypeName, int iterationsCount) {

		System.out.println(iterationsCount);
		if (schemaTypeName == null || iterationsCount > 10) {
			// -System.out.prinln("NULL!");
			return null;
		}

		/*
		 * if(schemaHasAlreadyBeenParsed(initialSchema.getTargetNamespace())){
		 * return null; }
		 * parsedSchemasForAttSearch_ITERATIVE.add(initialSchema.getTargetNamespace
		 * ());
		 */

		XmlSchemaObjectCollection includedSchemas = initialSchema.getIncludes();
		if (includedSchemas == null)
			return null;
		Iterator iter1 = includedSchemas.getIterator();
		while (iter1.hasNext()) {
			Object obj = iter1.next();
			if (obj == null)
				continue;

			org.apache.ws.commons.schema.XmlSchema theIncludedOrImportedSchema = null;

			if (obj.getClass().getName()
					.contains("org.apache.ws.commons.schema.XmlSchemaInclude")) {
				org.apache.ws.commons.schema.XmlSchemaInclude includedSchema = (org.apache.ws.commons.schema.XmlSchemaInclude) obj;
				theIncludedOrImportedSchema = includedSchema.getSchema();
				System.out.println();
			} else if (obj.getClass().getName()
					.contains("org.apache.ws.commons.schema.XmlSchemaImport")) {
				org.apache.ws.commons.schema.XmlSchemaImport importedSchema = (org.apache.ws.commons.schema.XmlSchemaImport) obj;
				theIncludedOrImportedSchema = importedSchema.getSchema();
				// theIncludedOrImportedSchema.get
				System.out.println();
			}
			if (theIncludedOrImportedSchema == null)
				continue;

			XmlSchemaObjectTable groupsCollectionTable = theIncludedOrImportedSchema
					.getGroups();
			if (groupsCollectionTable == null)
				continue;
			XmlSchemaObject schemaGroupObject = groupsCollectionTable
					.getItem(schemaTypeName);
			if (schemaGroupObject != null) {
				// schemaAttributeObject
				return schemaGroupObject;
			} else {
				iterationsCount++;
				XmlSchemaObject newSchemaGroupObject = searchSchemaIncludesForGroup_ITERATIVE_10_IterationsMax(
						theIncludedOrImportedSchema, schemaTypeName,
						iterationsCount);
				if (newSchemaGroupObject != null) {
					return newSchemaGroupObject;
				}
			}
		}

		return null;
	}

	public static XmlSchemaObject searchSchemaIncludesForAttributeGroup_ITERATIVE_10_IterationsMax(
			org.apache.ws.commons.schema.XmlSchema initialSchema,
			QName schemaTypeName, int iterationsCount) {

		System.out.println(iterationsCount);
		if (schemaTypeName == null || iterationsCount > 10) {
			// -System.out.prinln("NULL!");
			return null;
		}

		/*
		 * if(schemaHasAlreadyBeenParsed(initialSchema.getTargetNamespace())){
		 * return null; }
		 * parsedSchemasForAttSearch_ITERATIVE.add(initialSchema.getTargetNamespace
		 * ());
		 */

		XmlSchemaObjectCollection includedSchemas = initialSchema.getIncludes();
		if (includedSchemas == null)
			return null;
		Iterator iter1 = includedSchemas.getIterator();
		while (iter1.hasNext()) {
			Object obj = iter1.next();
			if (obj == null)
				continue;

			org.apache.ws.commons.schema.XmlSchema theIncludedOrImportedSchema = null;

			if (obj.getClass().getName()
					.contains("org.apache.ws.commons.schema.XmlSchemaInclude")) {
				org.apache.ws.commons.schema.XmlSchemaInclude includedSchema = (org.apache.ws.commons.schema.XmlSchemaInclude) obj;
				theIncludedOrImportedSchema = includedSchema.getSchema();
				System.out.println();
			} else if (obj.getClass().getName()
					.contains("org.apache.ws.commons.schema.XmlSchemaImport")) {
				org.apache.ws.commons.schema.XmlSchemaImport importedSchema = (org.apache.ws.commons.schema.XmlSchemaImport) obj;
				theIncludedOrImportedSchema = importedSchema.getSchema();
				// theIncludedOrImportedSchema.get
				System.out.println();
			}
			if (theIncludedOrImportedSchema == null)
				continue;

			XmlSchemaObjectTable groupsCollectionTable = theIncludedOrImportedSchema
					.getAttributeGroups();
			if (groupsCollectionTable == null)
				continue;
			XmlSchemaObject schemaGroupObject = groupsCollectionTable
					.getItem(schemaTypeName);
			if (schemaGroupObject != null) {
				// schemaAttributeObject
				return schemaGroupObject;
			} else {
				iterationsCount++;
				XmlSchemaObject newSchemaGroupObject = searchSchemaIncludesForAttributeGroup_ITERATIVE_10_IterationsMax(
						theIncludedOrImportedSchema, schemaTypeName,
						iterationsCount);
				if (newSchemaGroupObject != null) {
					return newSchemaGroupObject;
				}
			}
		}

		return null;
	}

	public static XmlSchemaObject searchSchemaIncludesForAttribute_ITERATIVE_10_IterationsMax(
			org.apache.ws.commons.schema.XmlSchema initialSchema,
			QName schemaTypeName, int iterationsCount) {

		System.out.println(iterationsCount);
		if (schemaTypeName == null || iterationsCount > 10) {
			// -System.out.prinln("NULL!");
			return null;
		}

		/*
		 * if(schemaHasAlreadyBeenParsed(initialSchema.getTargetNamespace())){
		 * return null; }
		 * parsedSchemasForAttSearch_ITERATIVE.add(initialSchema.getTargetNamespace
		 * ());
		 */

		XmlSchemaObjectCollection includedSchemas = initialSchema.getIncludes();
		if (includedSchemas == null)
			return null;
		Iterator iter1 = includedSchemas.getIterator();
		while (iter1.hasNext()) {
			Object obj = iter1.next();
			if (obj == null)
				continue;

			org.apache.ws.commons.schema.XmlSchema theIncludedOrImportedSchema = null;

			if (obj.getClass().getName()
					.contains("org.apache.ws.commons.schema.XmlSchemaInclude")) {
				org.apache.ws.commons.schema.XmlSchemaInclude includedSchema = (org.apache.ws.commons.schema.XmlSchemaInclude) obj;
				theIncludedOrImportedSchema = includedSchema.getSchema();
				System.out.println();
			} else if (obj.getClass().getName()
					.contains("org.apache.ws.commons.schema.XmlSchemaImport")) {
				org.apache.ws.commons.schema.XmlSchemaImport importedSchema = (org.apache.ws.commons.schema.XmlSchemaImport) obj;
				theIncludedOrImportedSchema = importedSchema.getSchema();
				// theIncludedOrImportedSchema.get
				System.out.println();
			}
			if (theIncludedOrImportedSchema == null)
				continue;

			XmlSchemaObjectTable attsCollectionTable = theIncludedOrImportedSchema
					.getAttributes();
			if (attsCollectionTable == null)
				continue;
			XmlSchemaObject schemaAttributeObject = attsCollectionTable
					.getItem(schemaTypeName);
			if (schemaAttributeObject != null) {
				// schemaAttributeObject
				return schemaAttributeObject;
			} else {
				iterationsCount++;
				XmlSchemaObject newSchemaAttributeObject = searchSchemaIncludesForAttribute_ITERATIVE_10_IterationsMax(
						theIncludedOrImportedSchema, schemaTypeName,
						iterationsCount);
				if (newSchemaAttributeObject != null) {
					return newSchemaAttributeObject;
				}
			}
		}

		return null;
	}

	public static void checkIfCOisAnyObjectType(ComplexObject co) {
		System.out.println("e");
		if (co.getObjectType() == null) {
			if (co.getHasComplexObjects().size() == 1
					&& co.getHasNativeObjects().size() == 0) {
				// Ypopsifio pros ISOPEDWSI
				ComplexObject co1 = (ComplexObject) co.getHasComplexObjects()
						.get(0);
				if (co1.getObjectName() != null || co1.getObjectType() != null) {
					if (co1.getObjectName().getLocalPart().equals("any")
							&& co1.getObjectType().getLocalPart()
									.equals("Object")) {
						co.setObjectType(new QName("Object"));
						co.setHasNativeObjects(new Vector());
						co.setHasComplexObjects(new Vector());
					} else {
						// Let CO live
					}
				} else {
					// isopedwse
					co.setObjectType(new QName("Object"));
					co.setHasNativeObjects(new Vector());
					co.setHasComplexObjects(new Vector());
				}
			}

			// koita ta native omoiws
			if (co.getHasComplexObjects().size() == 0
					&& co.getHasNativeObjects().size() == 1) {
				// Ypopsifio pros ISOPEDWSI
				NativeObject no1 = (NativeObject) co.getHasNativeObjects().get(
						0);
				if (no1.getObjectName() != null || no1.getObjectType() != null) {
					if (no1.getObjectName().getLocalPart().equals("any")
							&& no1.getObjectType().getLocalPart()
									.equals("Object")) {
						co.setObjectType(new QName("Object"));
						co.setHasNativeObjects(new Vector());
						co.setHasComplexObjects(new Vector());
					} else {
						// Let CO live
					}
				} else {
					// isopedwse
					co.setObjectType(new QName("Object"));
					co.setHasNativeObjects(new Vector());
					co.setHasComplexObjects(new Vector());
				}
			}

			// An ti skapoularei kai exei akoma NULL objectType dwse onoma sto
			// objectType
			if (co.getObjectType() == null) {
				if (co.getObjectName() != null) {
					co.setObjectType(co.getObjectName());
				} else {
					System.out.println();
				}
			} else {
				System.out.println();
			}

			/*
			 * if(co.getHasComplexObjects()==null&&co.getHasComplexObjects()==null
			 * ){ co.setObjectType(new QName("Object")); }else
			 * if(co.getHasComplexObjects
			 * ()!=null&&co.getHasComplexObjects().size()==1){ ComplexObject
			 * co1=(ComplexObject)co.getHasComplexObjects().get(0);
			 * if(co1.getObjectName()!=null&&co1.getObjectType()!=null){
			 * if(co1.getObjectName().getLocalPart().equals("any")&&
			 * co1.getObjectType().getLocalPart().equals("Object")){
			 * co.setHasComplexObjects(new Vector()); co.setObjectType(new
			 * QName("Object")); } }else{ co.setObjectType(new QName("Object"));
			 * } }
			 */
			System.out.println("OK 123456");
		}
		// -System.out.prinln("\t\t\t\tCOMPLEX OK");
	}

	public static Object tryToFindAndParseAttributeForSpecificObject(
			ParsedWSDLDefinition theDefinition, AxisService service,
			QName objectName) {
		XmlSchemaAttribute att = ParsingUtils
				.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaAttribute(
						service, objectName);

		if (att != null) {
			System.out.println("");
			Object res1 = AdditionalTypesParser.parseXmlSchemaAttribute(att,
					service, theDefinition);
			if (res1 != null) {
				if (res1.getClass().getName().contains("NativeObject")) {
					NativeObject no12 = (NativeObject) res1;
					// System.out.println(no12.objectName);
					return no12;
				} else if (res1.getClass().getName().contains("ComplexObject")) {
					ComplexObject co12 = (ComplexObject) res1;
					// System.out.println(co12.objectName);
					return co12;
				} else {
					return null;
				}
			} else {
				return null;
			}

		} else {
			return null;
		}
	}

	public static ComplexObject tryToFindAndParseGroupForSpecificObject(
			ParsedWSDLDefinition theDefinition, AxisService service,
			QName objectName) {
		XmlSchemaGroup group = ParsingUtils
				.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaGroup(
						service, objectName);
		if (group != null) {
			System.out.println("");
			ComplexObject res1 = AdditionalTypesParser.parseXmlSchemaGroup_NEW(
					service, group, theDefinition);
			return res1;
		} else {
			return null;
		}

	}

	public static ComplexObject tryToFindAndParseAttributeGroupForSpecificObject(
			ParsedWSDLDefinition theDefinition, AxisService service,
			QName objectName) {
		XmlSchemaAttributeGroup group = ParsingUtils
				.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaAttributeGroup(
						service, objectName);
		if (group != null) {
			System.out.println("");
			ComplexObject res1 = AdditionalTypesParser
					.parseXmlSchemaAttributeGroup_NEW(service, group,
							theDefinition);
			return res1;
		} else {
			return null;
		}

	}

	public static QName getComplexTypeSchemaTypeName(
			XmlSchemaElement schElemOfType, XmlSchemaType xmlSchemaOfType) {
		QName typeName = null;

		if (schElemOfType != null) {
			if (schElemOfType.getSchemaTypeName() != null) {
				typeName = schElemOfType.getSchemaTypeName();
			} else if (schElemOfType.getSchemaType() != null) {
				if (schElemOfType.getSchemaType().getQName() != null) {
					typeName = schElemOfType.getSchemaType().getQName();
				}
			}
		} else if (xmlSchemaOfType != null) {
			if (xmlSchemaOfType.getQName() != null) {
				typeName = xmlSchemaOfType.getQName();
			}
		}

		if (typeName == null) {
			return null;
		}

		return typeName;
	}

	/*
	 * static HashMap updatedComplexTypes; public static void
	 * updateAllComplexObjectsOfTheCOsHashMap() {
	 * if(ComplexTypesParser.parsedComplexObjectsHashmap
	 * ==null||ComplexTypesParser.parsedComplexObjectsHashmap.values()==null)
	 * return;
	 * 
	 * updatedComplexTypes=new HashMap(); Iterator
	 * iter=ComplexTypesParser.parsedComplexObjectsHashmap.values().iterator();
	 * while(iter.hasNext()){ try{ ComplexObject co=(ComplexObject)iter.next();
	 * if(co.getHasComplexObjects()==null)continue;
	 * 
	 * for(int i=0;i<co.getHasComplexObjects().size();i++){ ComplexObject
	 * co1=(ComplexObject)co.getHasComplexObjects().get(i); QName co1Type =
	 * co1.getObjectType();
	 * if(co1Type.getLocalPart().contains("NutritionalPlans")){
	 * System.out.println(); } if(updatedComplexTypes.containsKey(co1Type)){
	 * System.out.println(); QName co1Name= new
	 * QName(co1.getObjectName().getNamespaceURI(),
	 * co1.getObjectName().getLocalPart(), co1.getObjectName().getPrefix());
	 * ComplexObject newCO1=(ComplexObject)updatedComplexTypes.get(co1Type);
	 * co1=newCO1; co1.setObjectName(co1Name);
	 * //updatedComplexTypes.put(co2Type, co2); System.out.println(); }else{
	 * if(ComplexTypesParser.parsedComplexObjectsHashmap.containsKey(co1Type)){
	 * QName co1Name= new QName(co1.getObjectName().getNamespaceURI(),
	 * co1.getObjectName().getLocalPart(), co1.getObjectName().getPrefix());
	 * ComplexObject
	 * newCO1=(ComplexObject)ComplexTypesParser.parsedComplexObjectsHashmap
	 * .get(co1Type); co1=newCO1; co1.setObjectName(co1Name);
	 * System.out.println(); updatedComplexTypes.put(co1Type, co1);
	 * updateAllComplexObjectsOfTheCO_ITERATIVE(co1); }else{
	 * updateAllComplexObjectsOfTheCO_ITERATIVE(co1); } } }
	 * 
	 * }catch(Exception e){ e.printStackTrace(); System.out.println(); } }
	 * System.out.println();
	 * 
	 * }
	 * 
	 * private static void
	 * updateAllComplexObjectsOfTheCO_ITERATIVE(ComplexObject co1) {
	 * if(co1.getHasComplexObjects()==null)return; Iterator
	 * iter1=co1.getHasComplexObjects().iterator(); while(iter1.hasNext()){ try{
	 * ComplexObject co2=(ComplexObject)iter1.next(); QName
	 * co2Type=co2.getObjectType();
	 * if(co2Type.getLocalPart().contains("NutritionalPlans")){
	 * System.out.println(); } if(updatedComplexTypes.containsKey(co2Type)){
	 * System.out.println(); QName co2Name= new
	 * QName(co2.getObjectName().getNamespaceURI(),
	 * co2.getObjectName().getLocalPart(), co2.getObjectName().getPrefix());
	 * ComplexObject newCO2=(ComplexObject)updatedComplexTypes.get(co2Type);
	 * co2=newCO2; co2.setObjectName(co2Name);
	 * //updatedComplexTypes.put(co2Type, co2); System.out.println(); }else{
	 * if(ComplexTypesParser.parsedComplexObjectsHashmap.containsKey(co2Type)){
	 * QName co2Name= new QName(co2.getObjectName().getNamespaceURI(),
	 * co2.getObjectName().getLocalPart(), co2.getObjectName().getPrefix());
	 * ComplexObject
	 * newCO2=(ComplexObject)ComplexTypesParser.parsedComplexObjectsHashmap
	 * .get(co2Type); co2=newCO2; co2.setObjectName(co2Name);
	 * updatedComplexTypes.put(co2Type, co2); System.out.println();
	 * updateAllComplexObjectsOfTheCO_ITERATIVE(co2); }else{
	 * updateAllComplexObjectsOfTheCO_ITERATIVE(co2); } }
	 * 
	 * }catch(Exception e){ e.printStackTrace(); System.out.println(); }
	 * 
	 * } }
	 */

}
