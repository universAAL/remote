/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.axis2Parser;



import java.util.Iterator;
import javax.xml.namespace.QName;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;


public class SimpleTypesParser {

    public static ComplexObject parseSimpleType(XmlSchemaElement schElemOfType, XmlSchemaType xmlSchemaType, NativeObject no, ParsedWSDLDefinition theDefinition,
            AxisService service) {

        /*
        if(no!=null&&no.getObjectName()!=null&&no.getObjectName().getLocalPart().contains("nilReason")){
            System.out.println();
        }*/
        if(schElemOfType!=null){
            System.out.println("\t\t\t\t### SIMPLE TYPE "+schElemOfType.getName()+" PARSING ###");

            if(schElemOfType.getMaxOccurs()>1){
                System.out.println("www");
                //arrayCOforNO=getArrayCoForNo(schElemOfType, xmlSchemaType, no, theDefinition);
                //return;
            }

            if(schElemOfType.getMinOccurs()==0||schElemOfType.isNillable()){
                no.setIsOptional(true);
            }

            
            
            if(schElemOfType.getQName()!=null){
                no.setObjectName(schElemOfType.getQName());
            }else if(schElemOfType.getRefName()!=null){
                no.setObjectName(schElemOfType.getRefName());
            }else if(schElemOfType.getName()!=null){
                no.setObjectName(new QName(schElemOfType.getName()));
            }else{
                System.out.println();
            }

            if(schElemOfType.getSchemaTypeName()!=null){
                no.setObjectType(schElemOfType.getSchemaTypeName());
                //no.setNamespaceURI(schElemOfType.getSchemaTypeName().getNamespaceURI());
                //no.additionalInfo=schElemOfType.getSchemaTypeName().getNamespaceURI();
            }

            if(schElemOfType.getSchemaType()!=null){
                try{
                    org.apache.ws.commons.schema.XmlSchemaSimpleType st=
                            (org.apache.ws.commons.schema.XmlSchemaSimpleType)schElemOfType.getSchemaType();
                    if(st.getContent()!=null){
                        if(st.getContent()!=null&&st.getContent().getClass().getName().contains("XmlSchemaSimpleTypeRestriction")){
                            org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction restriction=
                                    (org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction)st.getContent();

                            if(restriction.getBaseTypeName()!=null){
                                no.setObjectType( restriction.getBaseTypeName());
                                //no.setNamespaceURI(restriction.getBaseTypeName().getNamespaceURI());
                            }

                            Iterator iter1=restriction.getFacets().getIterator();
                            //no.additionalInfo+="    (Allowed Values:";
                            while(iter1.hasNext()){
                                Object obj=iter1.next();
                                if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaEnumerationFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaEnumerationFacet:: "+((org.apache.ws.commons.schema.XmlSchemaEnumerationFacet)obj).getValue().toString());
                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaMinInclusiveFacet:: "+((org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaMaxInclusiveFacet:: "+((org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaPatternFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaPatternFacet:: "+((org.apache.ws.commons.schema.XmlSchemaPatternFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaMaxExclusiveFacet:: "+((org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaMinExclusiveFacet:: "+((org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaNumericFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaNumericFacet:: "+((org.apache.ws.commons.schema.XmlSchemaNumericFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet")){
                                    no.getHasAllowedValues().add("XmlSchemaWhiteSpaceFacet:: "+((org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet)obj).getValue().toString());

                                }else{
                                    no.getHasAllowedValues().add(obj.getClass().getName()+":: "+((org.apache.ws.commons.schema.XmlSchemaFacet)obj).getValue().toString());
                                    //no.hasAllowedValues.add(((org.apache.ws.commons.schema.XmlSchemaFacet)obj).getValue().toString());
                                    //System.out.println("ERROR at FACETS @line ~1066! ...WARNING");
                                }
                            }
                            System.out.println();
                            //no.additionalInfo+=")";

                        }else if(st.getContent()!=null&&st.getContent().getClass().getName().contains("XmlSchemaSimpleTypeList")){

                            System.out.println();
                            XmlSchemaSimpleTypeList typeList = (XmlSchemaSimpleTypeList)st.getContent();
                            if(typeList.getItemTypeName()!=null){
                                no.setObjectType(typeList.getItemTypeName());
                            }else{
                                if(typeList.getItemType()!=null&&typeList.getItemType().getQName()!=null){
                                    no.setObjectType(typeList.getItemType().getQName());
                                }else if(typeList.getItemType()!=null&&typeList.getItemType().getName()!=null){
                                    no.setObjectType(new QName(typeList.getItemType().getName()));
                                }else{
                                    theDefinition.getContainingErrors().add("ERROR @line ~3564");
                                    System.out.println("ERROR @line ~3564");
                                }
                            }
                            if(no.getObjectName()==null){
                                no.setObjectName(new QName("http://www.w3.org/2001/XMLSchema","list"));
                            }
                            System.out.println();
                            no.setAdditionalInfo("isListType");
                            System.out.println();

                        }else if(st.getContent()!=null&&st.getContent().getClass().getName().contains("XmlSchemaSimpleTypeUnion")){
                            XmlSchemaSimpleTypeUnion unionElement=(XmlSchemaSimpleTypeUnion)st.getContent();
                            System.out.println();

                            ComplexObject co1=new ComplexObject();
                            co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
                            co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
                            co1.setIsAbstract(true);

                            AdditionalTypesParser.
                                    parseXMLSchemaSimpleTypeUnionElement(service, unionElement, co1, theDefinition, false);

                            System.out.println();
                            return co1;
                        }

                    }else if(st.getContent()==null){
                        //System.out.println("ERROR WARNING @line ~1151... content was null!");
                    }
                    else{
                        theDefinition.getContainingErrors().add("ERROR @line ~1154");
                        System.out.println("ERROR @line ~1154");
                    }
                }catch(Exception e){
                    theDefinition.getContainingErrors().add(e.toString());
                    e.printStackTrace();
                    //-System.exit(-13);
                }
            }else{                
                XmlSchemaType xmlSchemaType1=null;
                if(schElemOfType.getSchemaTypeName()!=null){
                    xmlSchemaType1 = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service, schElemOfType.getSchemaTypeName());

                    if (xmlSchemaType1 == null) {
                        xmlSchemaType1 = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service, schElemOfType.getSchemaTypeName());
                    }
                }else if(schElemOfType.getRefName()!=null){
                    xmlSchemaType1 = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service, schElemOfType.getRefName());

                    if (xmlSchemaType1 == null) {
                        xmlSchemaType1 = ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service, schElemOfType.getRefName());
                    }
                }

                if(xmlSchemaType1!=null){
                    ComplexObject unionCO=SimpleTypesParser.parseSimpleType(null, xmlSchemaType1, no, theDefinition, service);
                    if(unionCO!=null){
                        return unionCO;
                    }                    
                }else{
                    System.out.println();
                }
            }

            //-System.out.prinln("\t\t\t\tSIMPLE OK");
        }else if(xmlSchemaType!=null){
            //-System.out.prinln("\t\t\t\t### SIMPLE TYPE "+xmlSchemaType.getName()+" PARSING ###");
            try{
                if(no.getObjectName()==null){
                    no.setObjectName(xmlSchemaType.getQName());
                }
                if(no.getObjectName()==null){
                    System.out.println();
                }
                //no.objectType=xmlSchemaType.getQName().getLocalPart();
                //no.additionalInfo=xmlSchemaType.getQName().getNamespaceURI();
                //no.namespaceURI=xmlSchemaType.getQName().getNamespaceURI();
                if(xmlSchemaType.getClass().getName().contains("XmlSchemaSimpleType")){
                    XmlSchemaSimpleType xmlSchemaSimpleType=(XmlSchemaSimpleType)xmlSchemaType;
                    if(xmlSchemaSimpleType.getContent()!=null&&xmlSchemaSimpleType.getContent().getClass().getName().contains("XmlSchemaSimpleTypeRestriction")){
                        XmlSchemaSimpleTypeRestriction restrictionContent=(XmlSchemaSimpleTypeRestriction)xmlSchemaSimpleType.getContent();
                        no.setObjectType(restrictionContent.getBaseTypeName());
                        //no.namespaceURI=restrictionContent.getBaseTypeName().getNamespaceURI();
                        XmlSchemaObjectCollection facetsCol=restrictionContent.getFacets();
                        if(facetsCol!=null){
                            Iterator iter1=facetsCol.getIterator();
                            while(iter1.hasNext()){
                                Object obj=iter1.next();
                                if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaEnumerationFacet")){
                                    no.getHasAllowedValues().add("enumeration:: "+((org.apache.ws.commons.schema.XmlSchemaEnumerationFacet)obj).getValue().toString());
                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet")){
                                    no.getHasAllowedValues().add("min inclusive:: "+((org.apache.ws.commons.schema.XmlSchemaMinInclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet")){
                                    no.getHasAllowedValues().add("max inclusive:: "+((org.apache.ws.commons.schema.XmlSchemaMaxInclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaPatternFacet")){
                                    no.getHasAllowedValues().add("pattern:: "+((org.apache.ws.commons.schema.XmlSchemaPatternFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet")){
                                    no.getHasAllowedValues().add("max exclusive:: "+((org.apache.ws.commons.schema.XmlSchemaMaxExclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet")){
                                    no.getHasAllowedValues().add("min exclusive:: "+((org.apache.ws.commons.schema.XmlSchemaMinExclusiveFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaNumericFacet")){
                                    no.getHasAllowedValues().add("numeric: "+((org.apache.ws.commons.schema.XmlSchemaNumericFacet)obj).getValue().toString());

                                }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet")){
                                    no.getHasAllowedValues().add("whiteSpace:: "+((org.apache.ws.commons.schema.XmlSchemaWhiteSpaceFacet)obj).getValue().toString());

                                }else{
                                    no.getHasAllowedValues().add(obj.getClass().getName()+":: "+((org.apache.ws.commons.schema.XmlSchemaFacet)obj).getValue().toString());
                                    //System.out.println("ERROR at FACETS @line ~1066! ...WARNING");
                                }
                            }
                        }
                    }else{
                        if(xmlSchemaSimpleType.getContent()!=null&&xmlSchemaSimpleType.getContent().getClass().getName().contains("XmlSchemaSimpleTypeList")){
                            System.out.println();
                            XmlSchemaSimpleTypeList typeList = (XmlSchemaSimpleTypeList)xmlSchemaSimpleType.getContent();
                            if(typeList.getItemTypeName()!=null){
                                no.setObjectType(typeList.getItemTypeName());
                            }else{
                                if(typeList.getItemType()!=null&&typeList.getItemType().getQName()!=null){
                                    no.setObjectType(typeList.getItemType().getQName());
                                }else if(typeList.getItemType()!=null&&typeList.getItemType().getName()!=null){
                                    no.setObjectType(new QName(typeList.getItemType().getName()));
                                }else{
                                    theDefinition.getContainingErrors().add("ERROR @line ~3564");
                                    System.out.println("ERROR @line ~3564");
                                }
                            }
                            if(no.getObjectName()==null){
                                no.setObjectName(new QName("http://www.w3.org/2001/XMLSchema","list"));
                            }
                            System.out.println();
                            no.setAdditionalInfo("isListType");
                            System.out.println();
                            //no.setObjectType(restrictionContent.getBaseTypeName());
                        }else if(xmlSchemaSimpleType.getContent()!=null&&xmlSchemaSimpleType.getContent().getClass().getName().contains("XmlSchemaSimpleTypeUnion")){
                            XmlSchemaSimpleTypeUnion unionElement=(XmlSchemaSimpleTypeUnion)xmlSchemaSimpleType.getContent();
                            System.out.println();

                            ComplexObject co1=new ComplexObject();
                            co1.setObjectName(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
                            co1.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", "XmlSchemaChoice"));
                            co1.setIsAbstract(true);

                            AdditionalTypesParser.
                                    parseXMLSchemaSimpleTypeUnionElement(service, unionElement, co1, theDefinition, false);

                            System.out.println();
                            return co1;
                        }
                        else if(!xmlSchemaSimpleType.isMixed()){
                            System.out.println();
                            if(no.getObjectType()==null){
                                if(xmlSchemaSimpleType.getQName()!=null){
                                    no.setObjectType(xmlSchemaSimpleType.getQName());
                                }else if(xmlSchemaSimpleType.getName()!=null){
                                    no.setObjectType(new QName("http://www.w3.org/2001/XMLSchema", xmlSchemaSimpleType.getName()));
                                }else{
                                    theDefinition.getContainingErrors().add("ERROR @line ~230");
                                    System.out.println("ERROR @line ~230");
                                }
                            }

                        }else{
                            theDefinition.getContainingErrors().add("ERROR @line ~236");
                            System.out.println("ERROR @line ~236");
                        }
                    }
                }


            }catch(Exception e){
                theDefinition.getContainingErrors().add(e.toString());
                e.printStackTrace();
                //-System.exit(-7);
            }

            //-System.out.prinln("\t\t\t\tSIMPLE OK");
        }else{
            no.setObjectType(new QName("Object"));
            System.out.println();
        }
        return null;
    }


    public static void parseSimpleContent(AxisService service, XmlSchemaSimpleContent simpleContent, ComplexObject co,
            ParsedWSDLDefinition theDefinition) {
        System.out.println(simpleContent.getContent().getClass());
        if(simpleContent==null)return;
        System.out.println("parsing SIMPLE CONTENT...");
        //Try to parse extension...
        if(simpleContent.getContent().getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction")){
            org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction restriction=
                        (org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction)simpleContent.getContent();

            //Parse to baseTypeName
            NativeObject baseNO=null;
            ComplexObject baseCO=null;
            
            org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType=null;
            XmlSchemaElement el1=service.getSchemaElement(restriction.getBaseTypeName());
            if(el1!=null){
                xmlSchemaType=el1.getSchemaType();

            }else{
                xmlSchemaType=ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service, restriction.getBaseTypeName());
                if(xmlSchemaType==null){
                    xmlSchemaType=ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service, restriction.getBaseTypeName());
                }
            }

            if(xmlSchemaType!=null){

                if(xmlSchemaType.getClass().getName().contains("XmlSchemaSimpleType")){
                    baseNO=new NativeObject();
                    baseNO.setObjectName(new QName("value"));

                    ComplexObject unionCO=SimpleTypesParser.parseSimpleType(null, xmlSchemaType, baseNO, theDefinition, service);
                    if(unionCO==null){
                        if(baseNO.getAdditionalInfo()!=null&&baseNO.getAdditionalInfo().contains("isListType")){
                            System.out.println();
                            baseCO=new ComplexObject();
                            baseCO.setObjectName(baseNO.getObjectName());
                            baseCO.setObjectType(new QName(baseNO.getObjectType().getNamespaceURI(),
                                    baseNO.getObjectType().getLocalPart()+"[]",baseNO.getObjectType().getPrefix()));
                            NativeObject no123=baseNO.cloneTheNO();
                            no123.setAdditionalInfo(null);
                            baseCO.getHasNativeObjects().add(no123);
                            baseCO.setIsArrayType(true);
                            baseNO=null;
                            co.getHasComplexObjects().add(baseCO);
                        }else{
                            co.getHasNativeObjects().add(baseNO);
                        }
                    }else{
                        ComplexObject co2=new ComplexObject();
                        co2.setObjectName(baseNO.getObjectName());
                        if(xmlSchemaType.getQName()!=null){
                            co2.setObjectType(xmlSchemaType.getQName());
                        }else{
                            System.out.println();
                        }
                        co2.getHasComplexObjects().add(unionCO);
                        co.getHasComplexObjects().add(co2);
                    }

                }else if(xmlSchemaType.getClass().getName().contains("XmlSchemaComplexType")){
                    baseCO=new ComplexObject();
                    baseCO.setObjectName(new QName("value"));

                    ComplexTypesParser.parseComplexType(service, null, xmlSchemaType, baseCO, theDefinition, false);
                    for(int i=0;i<baseCO.getHasNativeObjects().size();i++){
                        co.getHasNativeObjects().add(baseCO.getHasNativeObjects().get(i));
                    }
                    for(int i=0;i<baseCO.getHasComplexObjects().size();i++){
                        co.getHasComplexObjects().add(baseCO.getHasComplexObjects().get(i));
                    }
                }
            }else{
                Object res123= ParsingUtils.tryToFindAndParseAttributeForSpecificObject(theDefinition, service, restriction.getBaseTypeName());
                //to res123 se ayti tin periptwi einai to baseCO

                if(res123!=null){
                    if(res123.getClass().getName().contains("NativeObject")){
                        //CHECK THIS!!!!
                        //An mpei edw prepi na ELEGKSW AN EINAI SWSTO POU MPAINEI ETSI TO res123 mesa sto co
                        co.getHasNativeObjects().add(res123);

                    }else if(res123.getClass().getName().contains("ComplexObject")){
                        baseCO=(ComplexObject)res123;
                        for(int i=0;i<baseCO.getHasNativeObjects().size();i++){
                            co.getHasNativeObjects().add(baseCO.getHasNativeObjects().get(i));
                        }
                        for(int i=0;i<baseCO.getHasComplexObjects().size();i++){
                            co.getHasComplexObjects().add(baseCO.getHasComplexObjects().get(i));
                        }
                    }
                }else{
                    if(restriction.getBaseTypeName().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema")){
                        NativeObject no1=new NativeObject();
                        no1.setObjectName(new QName("value"));
                        no1.setObjectType(restriction.getBaseTypeName());
                        co.getHasNativeObjects().add(no1);
                    }else{
                        System.out.println("ERROR!!! De vrethike to baseType OUTE SAN Attribute!!! @line ~343 @SimpleTypesParser");
                    }

                }
            }

            //Parse ta ypoloipa objects tou co
            org.apache.ws.commons.schema.XmlSchemaObjectCollection ctCollection=
                    (org.apache.ws.commons.schema.XmlSchemaObjectCollection)restriction.getAttributes();
            Iterator containedObjectsIter=ctCollection.getIterator();
            while(containedObjectsIter.hasNext()){
                Object obj=containedObjectsIter.next();
                try{
                    if(obj.getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaAttribute")){
                        org.apache.ws.commons.schema.XmlSchemaAttribute objectXMLSchemaAttribute=
                                (org.apache.ws.commons.schema.XmlSchemaAttribute)obj;

                        //System.out.println(objectXMLSchemaAttribute.getSchemaTypeName());

                        Object res1=AdditionalTypesParser.parseXmlSchemaAttribute(objectXMLSchemaAttribute, service, theDefinition);
                        if(res1!=null){
                            if(res1.getClass().getName().contains("NativeObject")){
                                NativeObject no12=(NativeObject)res1;
                                //System.out.println(no12.objectName);
                                co.getHasNativeObjects().add(no12);
                            }else if(res1.getClass().getName().contains("ComplexObject")){
                                ComplexObject co12=(ComplexObject)res1;
                                //System.out.println(co12.objectName);
                                co.getHasComplexObjects().add(co12);
                            }
                        }
                    }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")){
                        System.out.println();
                        ComplexObject co1=new ComplexObject();
                        AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(service,
                                (org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef)obj, co1, theDefinition);

                        if(co1!=null){
                            for(int i=0;i<co1.getHasComplexObjects().size();i++){
                                co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
                            }
                            for(int i=0;i<co1.getHasNativeObjects().size();i++){
                                co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
                            }
                        }else{
                            System.out.println();
                        }
                    }else{
                        System.out.println();
                    }
                }catch(Exception e){
                    theDefinition.getContainingErrors().add(e.toString());
                    e.printStackTrace();
                    //System.exit(-1);
                }
            }

            //System.out.println("SIMPLE CONTENT OK");
        }else{
            try{
                org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension extension=
                        (org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension)simpleContent.getContent();
                System.out.println(extension.getBaseTypeName());

                //Parse to baseTypeName
                NativeObject baseNO=null;
                ComplexObject baseCO=null;

                //baseNO.setObjectName(new QName("value"));

                //ISWS EDW XREIAZETAI KAPWS NA VAZW KAI TO PREFIX tou type...?
                //baseNO.setObjectType(extension.getBaseTypeName());

                org.apache.ws.commons.schema.XmlSchemaType xmlSchemaType=null;
                XmlSchemaElement el1=service.getSchemaElement(extension.getBaseTypeName());
                if(el1!=null){
                    xmlSchemaType=el1.getSchemaType();

                }else{
                    xmlSchemaType=ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaType(service, extension.getBaseTypeName());
                    if(xmlSchemaType==null){
                        xmlSchemaType=ParsingUtils.parseWSDLschemasInOrderToFindTheSpecificXMLSchemaElement(service, extension.getBaseTypeName());
                    }
                }

                if(xmlSchemaType!=null){

                    if(xmlSchemaType.getClass().getName().contains("XmlSchemaSimpleType")){
                        baseNO=new NativeObject();
                        baseNO.setObjectName(new QName("value"));

                        ComplexObject unionCO=SimpleTypesParser.parseSimpleType(null, xmlSchemaType, baseNO, theDefinition, service);
                        if(unionCO==null){
                            if(baseNO.getAdditionalInfo()!=null&&baseNO.getAdditionalInfo().contains("isListType")){
                                System.out.println();
                                baseCO=new ComplexObject();
                                baseCO.setObjectName(baseNO.getObjectName());
                                baseCO.setObjectType(new QName(baseNO.getObjectType().getNamespaceURI(),
                                        baseNO.getObjectType().getLocalPart()+"[]",baseNO.getObjectType().getPrefix()));
                                NativeObject no123=baseNO.cloneTheNO();
                                no123.setAdditionalInfo(null);
                                baseCO.getHasNativeObjects().add(no123);
                                baseCO.setIsArrayType(true);
                                baseNO=null;
                                co.getHasComplexObjects().add(baseCO);
                            }else{
                                co.getHasNativeObjects().add(baseNO);
                            }
                        }else{
                            ComplexObject co2=new ComplexObject();
                            co2.setObjectName(baseNO.getObjectName());
                            if(xmlSchemaType.getQName()!=null){
                                co2.setObjectType(xmlSchemaType.getQName());
                            }else{
                                System.out.println();
                            }
                            co2.getHasComplexObjects().add(unionCO);
                            co.getHasComplexObjects().add(co2);
                        }

                    }else if(xmlSchemaType.getClass().getName().contains("XmlSchemaComplexType")){
                        baseCO=new ComplexObject();
                        baseCO.setObjectName(new QName("value"));

                        ComplexTypesParser.parseComplexType(service, null, xmlSchemaType, baseCO, theDefinition, false);
                        for(int i=0;i<baseCO.getHasNativeObjects().size();i++){
                            co.getHasNativeObjects().add(baseCO.getHasNativeObjects().get(i));
                        }
                        for(int i=0;i<baseCO.getHasComplexObjects().size();i++){
                            co.getHasComplexObjects().add(baseCO.getHasComplexObjects().get(i));
                        }
                    }

                }else{

                    Object res123= ParsingUtils.tryToFindAndParseAttributeForSpecificObject(theDefinition, service, extension.getBaseTypeName());
                    //to res123 se ayti tin periptwi einai to baseCO

                    if(res123!=null){
                        if(res123.getClass().getName().contains("NativeObject")){
                            //CHECK THIS!!!!
                            //An mpei edw prepi na ELEGKSW AN EINAI SWSTO POU MPAINEI ETSI TO res123 mesa sto co
                            co.getHasNativeObjects().add(res123);

                        }else if(res123.getClass().getName().contains("ComplexObject")){
                            baseCO=(ComplexObject)res123;
                            for(int i=0;i<baseCO.getHasNativeObjects().size();i++){
                                co.getHasNativeObjects().add(baseCO.getHasNativeObjects().get(i));
                            }
                            for(int i=0;i<baseCO.getHasComplexObjects().size();i++){
                                co.getHasComplexObjects().add(baseCO.getHasComplexObjects().get(i));
                            }
                        }
                    }else{
                        if(extension.getBaseTypeName().getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema")){
                            NativeObject no1=new NativeObject();
                            no1.setObjectName(new QName("value"));
                            no1.setObjectType(extension.getBaseTypeName());
                            co.getHasNativeObjects().add(no1);
return;
                        }else{
                            System.out.println("ERROR!!! De vrethike to baseType OUTE SAN Attribute!!! @line ~343 @SimpleTypesParser");
                        }

                    }
                }

               
                //Parse ta ypoloipa objects tou co
                org.apache.ws.commons.schema.XmlSchemaObjectCollection ctCollection=
                        (org.apache.ws.commons.schema.XmlSchemaObjectCollection)extension.getAttributes();
                Iterator containedObjectsIter=ctCollection.getIterator();
                while(containedObjectsIter.hasNext()){
                    Object obj=containedObjectsIter.next();
                    try{
                        if(obj.getClass().getName().equals("org.apache.ws.commons.schema.XmlSchemaAttribute")){
                            org.apache.ws.commons.schema.XmlSchemaAttribute objectXMLSchemaAttribute=
                                    (org.apache.ws.commons.schema.XmlSchemaAttribute)obj;
                            
                            Object res1=AdditionalTypesParser.parseXmlSchemaAttribute(objectXMLSchemaAttribute, service, theDefinition);
                            if(res1!=null){
                                if(res1.getClass().getName().contains("NativeObject")){
                                    NativeObject no12=(NativeObject)res1;
                                    //System.out.println(no12.objectName);
                                    co.getHasNativeObjects().add(no12);
                                }else if(res1.getClass().getName().contains("ComplexObject")){
                                    ComplexObject co12=(ComplexObject)res1;
                                    //System.out.println(co12.objectName);
                                    co.getHasComplexObjects().add(co12);
                                }
                            }
                        }else if(obj.getClass().getName().contains("org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef")){
                            System.out.println();
                            ComplexObject co1=new ComplexObject();
                            AdditionalTypesParser.parseXmlSchemaAttributeGroupRefElement(service,
                                    (org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef)obj, co1, theDefinition);

                            if(co1!=null){
                                for(int i=0;i<co1.getHasComplexObjects().size();i++){
                                    co.getHasComplexObjects().add(co1.getHasComplexObjects().get(i));
                                }
                                for(int i=0;i<co1.getHasNativeObjects().size();i++){
                                    co.getHasNativeObjects().add(co1.getHasNativeObjects().get(i));
                                }
                            }else{
                                System.out.println();
                            }
                            
                        }else{
                            System.out.println();
                        }

                        /*
                        boolean typeParsed=false;
                        if(objectXMLSchemaAttribute.getSchemaType().getClass().toString().contains("org.apache.ws.commons.schema.XmlSchemaSimpleType")){
                            NativeObject no1=new NativeObject();
                            parseXmlSchemaAttribute(objectXMLSchemaAttribute);
                            typeParsed=true;
                            co.hasNativeObjects.add(no1);
                        }else if(objectXMLSchemaAttribute.getSchemaType().getClass().toString().contains("org.apache.ws.commons.schema.XmlSchemaComplexType")){
                            ComplexObject co1=new ComplexObject();
                            //parseComplexType(service, objectXMLSchemaElement, null, co1);
                            typeParsed=true;
                            co.getHasComplexObjects().add(co1);
                        }

                        if(!typeParsed){
                            System.out.println("ERROR 1!!!!!!!!!!!!!!!!!!");
                            System.exit(-1);
                        }*/
                    }catch(Exception e){
                        theDefinition.getContainingErrors().add(e.toString());
                        e.printStackTrace();
                        //System.exit(-1);
                    }
                }

                //System.out.println("SIMPLE CONTENT OK");

                //ComplexObject baseCO=new ComplexObject();
                //baseCO.setObjectName(new QName("baseCO"));
            }catch(Exception e){
                theDefinition.getContainingErrors().add(e.toString());
                e.printStackTrace();
            }
        }


        if(co.getObjectType()==null&&co.getObjectName()!=null){
            co.setObjectType(co.getObjectName());
        }
    }


}
