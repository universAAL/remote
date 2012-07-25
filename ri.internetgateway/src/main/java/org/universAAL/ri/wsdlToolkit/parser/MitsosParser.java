/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.parser;

import com.ibm.wsdl.PortTypeImpl;
import com.ibm.wsdl.ServiceImpl;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.ibm.wsdl.extensions.schema.SchemaImportImpl;


import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.JOptionPane;
//-import javax.swing.JTextArea;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
//import org.exolab.castor.xml.dtd.Attribute;
//import org.exolab.castor.xml.schema.AttributeDecl;
//import org.exolab.castor.xml.schema.ComplexType;
//import org.exolab.castor.xml.schema.ElementDecl;
//import org.exolab.castor.xml.schema.SimpleType;
//import org.exolab.castor.xml.schema.XMLType;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationOutput;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;



public class MitsosParser{
    
    public static Map namespaces;
    public static String nativeTypePrefix;
    public static String targetNamespacePrefix;
    public static Vector namespacesOfXSDsAlreadyParsedForImports;
    public static HashMap parsedObjectsHashmap;
    public static Definition theWSDLDefinition;
    //-GIORGOS
//    public static WebService ws = new WebService();
//    public static ArrayList operationsList = new ArrayList();
//    
//    public WebService getWebService() {
//        return ws;
//    }
//    
//    public void exposeWS(){
//        if (ws.getName()==null){
//            System.out.println("No web service found in this URI");
//            return;
//        }
//        
//        
//        //System.out.println("\n\n\n/--------------------------------/\n\n\n");
//        System.out.println("Service Name => "+ws.getName());
//        System.out.println("Service Domain => "+ws.getNamespace());
//        System.out.println("Service URL => "+ws.getServiceURL());
//        
//        ArrayList operationsList = ws.getOperationsList();
//        System.out.println();
//        for (int i=0;i<operationsList.size();i++){
//            ServiceOperation operation = (ServiceOperation)operationsList.get(i);
//            System.out.println("Operation "+(i+1)+" => "+operation.getName()+". Domain:"+operation.getDomain());
//            OperationInput operationInput = operation.getInput();
//            OperationOutput operationOutput = operation.getOutput();
//
//            System.out.println("\t\t Input Parameters =>");
//            if (operationInput.getParametersList().size()==0){
//                System.out.println("\t\t               *VOID*");
//            }
//            for(int in=0;in<operationInput.getParametersList().size();in++){
//                Parameter operationParameter = (Parameter)operationInput.getParametersList().get(in);
//                System.out.println("\t\t               "+operationParameter.getParName()+":"+operationParameter.getParType());
//            }System.out.println();
//      
//            //System.out.println("\t Output => "+operationOutput.getResponseName());
//            System.out.println("\t\t Output Parameters =>");
//            
//            Parameter operationParameter = operationOutput.getOutputParameter();
//            if (operationParameter.getParName()!=null){
//                System.out.println("\t\t               "+operationParameter.getParName()+":"+operationParameter.getParType());
//            }
//            else{
//                System.out.println("\t\t               *VOID*");
//            }
//            System.out.println();
//           
//        }
//         
//    }
    
    //- GIORGOS.

    public static ParsedWSDLDefinition parseWSDL(String theURL){
        ////-System.out.println("Parsing wsdl at: ");        
        ////-System.out.println(theURL);
                
        ////-ta.setVisible(false);
        ////-ta.setVisible(true);
        ////-ta.setSize(//-ta.getWidth(), //-ta.getHeight());
        ////-ta.setVisible(true);
        parsedObjectsHashmap=new HashMap();
        ParsedWSDLDefinition theResultDefinition=new ParsedWSDLDefinition();
        getTheWSDLDefinitions(theResultDefinition,theURL);
        return theResultDefinition;
        
    }
    
    private static void getTheWSDLDefinitions(ParsedWSDLDefinition mitsosDefinition, String wsdlURI){
        try{
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

            wsdlReader.setFeature("javax.wsdl.verbose",false);
            wsdlReader.setFeature("javax.wsdl.importDocuments",true);
	
            Definition definition = wsdlReader.readWSDL(wsdlURI);
           // //-System.out.println(definition.getImports().size());
            ////-System.out.println(definition.getNamespaces().size());
            //theDefinitionOfTheWSDL=definition;
            mitsosDefinition.setWsdlURL(new URL(wsdlURI));
            
            //-System.out.println("WSDL Read OK");    
            //-ta.append("\nWSDL Read OK");
            
            if (definition == null)
            {
                System.err.println("definition element is null");
                //-ta.append("\nError:\nDEFINITION was null!");
                return;
            }
            ////-System.out.println("Got definition from WSDL!\n");
            //-ta.append("\n\n\t\t*** DEFINITION ***");
            
            theWSDLDefinition=definition;
            namespaces = definition.getNamespaces();
            nativeTypePrefix=null;
            targetNamespacePrefix=null;
                    
            try{
            Collection col = namespaces.values();
            Set keySet=namespaces.keySet();
            Iterator keyIterator=keySet.iterator();
                for(int i=0;i<col.size();i++){
                    Iterator iter1=col.iterator();
                    while(iter1.hasNext()&&keyIterator.hasNext()){
                        String s=(String)iter1.next();
                        String key=(String)keyIterator.next();
                        //////-System.out.println(s);
                        if(s.startsWith("http://www.w3.org")&&s.endsWith("/XMLSchema")){
                            nativeTypePrefix=key;
                        }else if(s.equals(definition.getTargetNamespace())){
                            targetNamespacePrefix=key;
                        }
                    }
                    //nativeTypePrefix=

                }
            }catch(Exception e){
                nativeTypePrefix=null;
            }
            ////-System.out.println("##### nativeTypePrefix: "+nativeTypePrefix);
            
            //-ta.append("\nnativeTypePrefix: '"+nativeTypePrefix+"'"+"  ("+namespaces.get(nativeTypePrefix)+")");        
            //-ta.append("\ntargetNamespacePrefix: '"+targetNamespacePrefix+"'"+"  ("+definition.getTargetNamespace()+")");        
            
            if(nativeTypePrefix!=null)nativeTypePrefix+=":";
            if(targetNamespacePrefix!=null)targetNamespacePrefix+=":";
            
            
            
            
            parseImports(mitsosDefinition,definition);
                    
            parseNamespacesOfImportedXSDs(definition);
            
            parseServices(mitsosDefinition,definition);
                    
            //-ta.append("\nParsing Finished!");        
            ////-System.out.println("Parsing Finished!");            
        }catch(Exception e){
            e.printStackTrace();
            //JOptionPane.showMessageDialog(ta, e.getMessage());
            //-JOptionPane.showMessageDialog(ta,e.getMessage(),"ERROR when trying to parse the WSDL!",JOptionPane.ERROR_MESSAGE);
            //-ta.append("\nAn Error occured when tryin to parse the WSDL:");
            //-ta.append("\n"+e.getMessage());
            //-ta.append("\n");
            ////-System.out.println("An Error occured when tryin to parse the WSDL!");
        }
    }

    
    
    
        
    private static void parseNamespacesOfImportedXSDs(javax.wsdl.Definition definition){
        try{
            List extElementsList=definition.getTypes().getExtensibilityElements();      
            namespacesOfXSDsAlreadyParsedForImports=new Vector();
            //////-System.out.println("Extensibility Elements Names:");
            if(extElementsList!=null){
               // //-System.out.print(extElementsList.size()+"\n");
                Iterator iter1=extElementsList.iterator();
                while(iter1.hasNext()){
                    try{
                        //WSDL SCHEMA
                        com.ibm.wsdl.extensions.schema.SchemaImpl s1 = (com.ibm.wsdl.extensions.schema.SchemaImpl)iter1.next();                        
                                    
                        //PARSE IMPORTS
                        Map importsMap = s1.getImports();
                        Iterator importsItt = importsMap.values().iterator();
                        while (importsItt.hasNext()) {
                            List importsList = (List) importsItt.next();
                            Iterator importsItt2 = importsList.iterator();                                   
                            
                            //Schema Imported in WSDL
                            SchemaImportImpl schemaImport = (SchemaImportImpl) importsItt2.next();                            
                            namespacesOfXSDsAlreadyParsedForImports.add(schemaImport.getNamespaceURI());
                      //      ////-System.out.println(schemaImport.getNamespaceURI());               
                            javax.wsdl.extensions.schema.Schema schemaImportedInTheWSDL = schemaImport.getReferencedSchema();                             
                            
                            org.w3c.dom.Element impSchElem=schemaImportedInTheWSDL.getElement();
                            if(impSchElem==null)continue;
                                                         
                             NamedNodeMap atts=impSchElem.getAttributes();                             
                             
                        //     ////-System.out.println(atts.getLength());
                             for(int i=0;i<atts.getLength();i++){
                                 Node att=atts.item(i);
                                 if(att.getNodeName()!=null&&att.getNodeName().startsWith("xmlns:")){                                     
                                     String key=att.getNodeName().substring(att.getNodeName().indexOf(":")+1);                                     
                        //             ////-System.out.println(att.getNodeName()+" ## "+key+" || "+att.getNodeValue());
                                     namespaces.put(key, att.getNodeValue());
                                 }
                                 //////-System.out.println(att.getNodeName()+" "+att.getNodeValue());
                             }
                             
                             //Imports of the Imported XSD          
                             
                             parseNamespacesOfImportedXSDsIterative(schemaImportedInTheWSDL);
                             ////-System.out.println(namespaces.toString());
                    
                                     
                            
                                    /*
                                    parseImportedXSDforLiteral(s1, inPartType, ta, mitsosOperationInput,mitsosOperationOutput,inPartType.getNamespaceURI());
                                    //parseImportedXSDusingCastor(n.getAttributes().getNamedItem("schemaLocation").getNodeValue(), inPartType, ta);
                                

                            //PARSE INCLUDES
                                ////-System.out.println(n.getAttributes().getNamedItem("schemaLocation"));
                                if(n.getAttributes()!=null&&
                                        n.getAttributes().getNamedItem("schemaLocation")!=null&&
                                        n.getAttributes().getNamedItem("schemaLocation").getNodeValue()!=null){
                                    ////-System.out.println("FOUND AN XSD INCLUDE!!!");
                                    ////-System.out.println(n.getAttributes().getNamedItem("schemaLocation").getNodeValue()+"\n");
                                    parseIncludedXSDforLiteral(s1, inPartType, ta, mitsosOperationInput,mitsosOperationOutput);
                                

                            }*/
                        }
                    }catch(Exception e){
                        
                    }
                }
            }
            
            
        }catch(Exception e){
            
        }
        
    }
    
    private static void parseNamespacesOfImportedXSDsIterative(javax.wsdl.extensions.schema.Schema schema){
        try{
            Map importsMap = schema.getImports();
            Iterator importsItt = importsMap.values().iterator();
            while (importsItt.hasNext()) {
                List importsList = (List) importsItt.next();
                Iterator importsItt2 = importsList.iterator();                                   

                //Schema Imported in WSDL
                SchemaImportImpl schemaImport = (SchemaImportImpl) importsItt2.next();                            
                
                String namespaceOfImport=schemaImport.getNamespaceURI();
                boolean importHasAlreadyBeenParsed=false;
                for(int i=0;i<namespacesOfXSDsAlreadyParsedForImports.size();i++){
                    if(namespaceOfImport.equals((String)namespacesOfXSDsAlreadyParsedForImports.get(i))){
                        importHasAlreadyBeenParsed=true;
                    }
                }
                
                if(!importHasAlreadyBeenParsed){
                //    ////-System.out.println(schemaImport.getNamespaceURI());               
                    javax.wsdl.extensions.schema.Schema schemaImportedInTheInputSchema = schemaImport.getReferencedSchema();                             

                    org.w3c.dom.Element impSchElem=schemaImportedInTheInputSchema.getElement();
                    if(impSchElem==null)continue;

                     NamedNodeMap atts=impSchElem.getAttributes();                             

              //       ////-System.out.println(atts.getLength());
                     for(int i=0;i<atts.getLength();i++){
                         Node att=atts.item(i);
                         if(att.getNodeName()!=null&&att.getNodeName().startsWith("xmlns:")){                                     
                             String key=att.getNodeName().substring(att.getNodeName().indexOf(":")+1);                                     
               //              ////-System.out.println(att.getNodeName()+" ## "+key+" || "+att.getNodeValue());
                             namespaces.put(key, att.getNodeValue());
                         }
                         //////-System.out.println(att.getNodeName()+" "+att.getNodeValue());
                     }

                     //Imports of the Imported XSD   
                     namespacesOfXSDsAlreadyParsedForImports.add(schemaImport.getNamespaceURI());
                     parseNamespacesOfImportedXSDsIterative(schemaImportedInTheInputSchema);
                }
            }
        }catch(Exception e){
            
        }
    }

    private static void parseBindingOperations(ParsedWSDLDefinition mitsosDefinition,Definition definition, Binding binding) {
        
         String bindingStyle=null;                         
         List l1=binding.getExtensibilityElements();
         for(int i=0;i<l1.size();i++){
          //   ////-System.out.println(l1.get(i).getClass());
             try{
                 com.ibm.wsdl.extensions.soap.SOAPBindingImpl sbimpl=(com.ibm.wsdl.extensions.soap.SOAPBindingImpl)l1.get(i);
          //       ////-System.out.println(sbimpl.getStyle());
                 bindingStyle=sbimpl.getStyle();
             }catch(Exception e){
                 try{
                     com.ibm.wsdl.extensions.soap12.SOAP12BindingImpl sbimpl=(com.ibm.wsdl.extensions.soap12.SOAP12BindingImpl)l1.get(i);
            //         ////-System.out.println(sbimpl.getStyle());
                     bindingStyle=sbimpl.getStyle();
                 }
                 catch(Exception e1){
//                     System.out.println(".-.-"+ws.getServiceURL());
//-                    com.ibm.wsdl.extensions.http.HTTPBindingImpl httpbimpl=(com.ibm.wsdl.extensions.http.HTTPBindingImpl)l1.get(i);
            //        ////-System.out.println(httpbimpl.toString());  
                    bindingStyle="http";
                 }
             }finally{
                 ////-System.out.println("PERIERGOOOOOOOOOOO!!!!!!!!!!!");               
             }
         }
         
        ////-System.out.println("###### BINDING STYLE: "+bindingStyle);
        
        
        List opers=binding.getBindingOperations();
        if(opers!=null){
            ////-System.out.println("### "+opers.size()+" Operations are defined in this binding ###");
            //-ta.append("\n\tOperations:");
            //-GIORGOS
//                PortTypeImpl pti = (PortTypeImpl)(definition.getAllPortTypes().values().iterator().next());
//                ws.setName(pti.getQName().getLocalPart());
//                ws.setServiceURL(definition.getDocumentBaseURI());
//                ws.setNamespace(definition.getTargetNamespace());
//                operationsList.clear();
             //-GIORGOS.
            for(int i=0;i<opers.size();i++){
                BindingOperation bindingOper=(BindingOperation)opers.get(i);
                
                if(bindingStyle==null){
                    List l11=bindingOper.getExtensibilityElements();
                    for(int i11=0;i11<l11.size();i11++){
          //              ////-System.out.println("GAMWTOOOO W class: "+l11.get(i11).getClass());
                        try{
                            com.ibm.wsdl.extensions.soap.SOAPOperationImpl soapOperImpl=
                                    (com.ibm.wsdl.extensions.soap.SOAPOperationImpl) l11.get(i11);
                            //-System.out.println("EYRIKAAAAAAAAAA WWWWWWWWWW: "+soapOperImpl.getStyle());
                            bindingStyle=soapOperImpl.getStyle();
                        }catch(Exception e){
                            try{
                                com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl soapOperImpl=
                                        (com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl) l11.get(i11);
                                bindingStyle=soapOperImpl.getStyle();
                            }catch(Exception e1){
                                com.ibm.wsdl.extensions.http.HTTPBindingImpl httpbimpl=(com.ibm.wsdl.extensions.http.HTTPBindingImpl)l1.get(i);
                //                ////-System.out.println(httpbimpl.toString());  
                                bindingStyle="http";
                            }
                        }finally{
                            ////-System.out.println("PERIERGOOOOOOOOOOO!!!!!!!!!!!");               
                        }
                    }
                }
                
                String operationUse="unknown";
                
                List extensibilityElementsList=bindingOper.getBindingInput().getExtensibilityElements();
                if(extensibilityElementsList!=null){
                    for(int f1=0;f1<extensibilityElementsList.size();f1++){
                        javax.wsdl.extensions.ExtensibilityElement extel=(javax.wsdl.extensions.ExtensibilityElement)extensibilityElementsList.get(f1);
                //        ////-System.out.println(extel.getElementType());
                        try{
                            com.ibm.wsdl.extensions.soap.SOAPBodyImpl soapBodyImpl=(com.ibm.wsdl.extensions.soap.SOAPBodyImpl)extensibilityElementsList.get(f1);
                            
                            if(soapBodyImpl.getUse().equals("encoded")){
                                ////-System.out.println("USE: ENCODED ("+soapBodyImpl.getUse()+")");
                                operationUse="encoded";
                            }else if(soapBodyImpl.getUse().equals("literal")){
                                ////-System.out.println("USE: LITERAL ("+soapBodyImpl.getUse()+")");                                
                                operationUse="literal";
                            }
                        }catch(Exception e){
                            //e.printStackTrace();
                            try{
                                com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl soapBodyImpl=(com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl)extensibilityElementsList.get(f1);

                                if(soapBodyImpl.getUse().equals("encoded")){
                                    ////-System.out.println("USE: ENCODED ("+soapBodyImpl.getUse()+")");
                                    operationUse="encoded";
                                }else if(soapBodyImpl.getUse().equals("literal")){
                                    ////-System.out.println("USE: LITERAL ("+soapBodyImpl.getUse()+")");                                
                                    operationUse="literal";
                                }
                            }catch(Exception e1){
                                //e1.printStackTrace();
                                try{
                                    com.ibm.wsdl.extensions.mime.MIMEContentImpl mimecontimpl=(com.ibm.wsdl.extensions.mime.MIMEContentImpl)extensibilityElementsList.get(f1);
                                    ////-System.out.println(mimecontimpl.toString());  
                                    //operationUse="encoded";
                                }catch(Exception e2){
                                    //operationUse="encoded";
                                }
                            }
                        }
                        //////-System.out.println("AAAAAAAAAAAAAAAAAAAAAAA "+extel.getElementType());
                    }
                }
                
                String stringToAppend="";
                try{
                    if(bindingStyle.equals("http")){
                        if(i==0){
                            stringToAppend="\n\t\t"+bindingOper.getName()+" (HTTP-based Operation)";
                        }else{
                            stringToAppend="\n\n\t\t"+bindingOper.getName()+" (HTTP-based Operation)";
                        }
                    }else{
                        if(i==0){
                           stringToAppend="\n\t\t"+bindingOper.getName()+" ("+bindingStyle+"/"+operationUse+")";
                        }else{
                            stringToAppend="\n\n\t\t"+bindingOper.getName()+" ("+bindingStyle+"/"+operationUse+")";
                        }
                    }
                }catch(Exception e){//if bindingStyle==null
                    if(i==0){
                        stringToAppend="\n\t\t"+bindingOper.getName()+" ("+bindingStyle+"/"+operationUse+")";
                    }else{
                        stringToAppend="\n\n\t\t"+bindingOper.getName()+" ("+bindingStyle+"/"+operationUse+")";
                    }
                }
                
                //-ta.append(stringToAppend);
                WSOperation mitsosParsedOperation=new WSOperation();
                mitsosParsedOperation.setOperationName(stringToAppend);
                                
                ////-System.out.println("Binding Operation name: "+bindingOper.getName());
                
                //javax.wsdl.extensions.ExtensibilityElement extel=(javax.wsdl.extensions.ExtensibilityElement)bindingOper.getBindingInput().getExtensibilityElements().get(0);
                ////-System.out.println("####### BINDING INPUT: "+bindingOper.getBindingInput().getExtensibilityElements().size());
                
                ////-System.out.println(bindingOper.getBindingInput().getNativeAttributeNames().size()+
                      //  " "+bindingOper.getBindingInput().getNativeAttributeNames().get(0));
                
                Operation operation=bindingOper.getOperation();
                parseOperation(mitsosParsedOperation, definition,operation, operationUse, bindingStyle);
                mitsosDefinition.getWsdlOperations().add(mitsosParsedOperation);
                
            }
        //-GIORGOS        
//        ws.setOperationsList(operationsList);
       
        //-GIORGOS.
        }    
    }

    private static void parseHttpType(Definition definition, String name) {
        List extElementsList=definition.getTypes().getExtensibilityElements();        
        //////-System.out.println("Extensibility Elements Names:");
        if(extElementsList!=null){
            //-System.out.print(extElementsList.size()+"\n");
            Iterator iter1=extElementsList.iterator();
            while(iter1.hasNext()){
                try{
                    com.ibm.wsdl.extensions.schema.SchemaImpl s1 = (com.ibm.wsdl.extensions.schema.SchemaImpl)iter1.next();
                    ////-System.out.println(s1.toString());  
                    parseTypeIterative(s1,name,0,false,null);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static void parseImportedXSD(com.ibm.wsdl.extensions.schema.SchemaImpl schemaImpl, QName inPartType,
            WSOperationInput operationInputs, WSOperationOutput operationOutputs, NativeObject no, ComplexObject co) {        
               
        Map importsMap = schemaImpl.getImports();
        Iterator importsItt = importsMap.values().iterator();
        while (importsItt.hasNext()) {
            List importsList = (List) importsItt.next();
            Iterator importsItt2 = importsList.iterator();
            while (importsItt2.hasNext()) {
                SchemaImportImpl schemaImport = (SchemaImportImpl) importsItt2.next();
                javax.wsdl.extensions.schema.Schema importedSchema = schemaImport.getReferencedSchema();                
                org.w3c.dom.Element impSchElem=importedSchema.getElement();
                                                        
                if(impSchElem==null)continue;
                
                /*
                Attr attr=impSchElem.getAttributeNode(inPartType.getLocalPart());                
                NodeList nl=impSchElem.getElementsByTagName("xs:complexType");
                for(int i=0;i<nl.getLength();i++){
                    Node nn1=nl.item(i);
                    ////-System.out.println(nn1.getNodeName());
                }
                */
                
                NodeList childrenOfChildOfSchema = impSchElem.getChildNodes();
                if(childrenOfChildOfSchema!=null){
                    for(int j=0;j<childrenOfChildOfSchema.getLength();j++){
                        Node n1=childrenOfChildOfSchema.item(j);
            //            ////-System.out.println("ABCDEFG "+n1.getNodeName());
                        
                        if(n1.getAttributes()==null)continue;
                        
                        if(n1.getAttributes().getNamedItem("name")!=null&&
                                n1.getAttributes().getNamedItem("name").getNodeValue()!=null&&
                                n1.getAttributes().getNamedItem("name").getNodeValue().equals(inPartType.getLocalPart())){
                            
                            NodeList childrenOfTheDamned=n1.getChildNodes();
                            if(childrenOfTheDamned!=null){
                                for(int k=0;k<childrenOfTheDamned.getLength();k++){
                      //              ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
                                    NodeList childrenOfTheChildrenOfTheDamned=childrenOfTheDamned.item(k).getChildNodes();
                                    if(childrenOfTheChildrenOfTheDamned!=null){
                                        for(int k2=0;k2<childrenOfTheChildrenOfTheDamned.getLength();k2++){
                                            if(childrenOfTheChildrenOfTheDamned.item(k2).getNodeName().contains("element")){
                                                NamedNodeMap attributesOfType=childrenOfTheChildrenOfTheDamned.item(k2).getAttributes();                                            
                          //                      ////-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
                                                String attName="";
                                                String attType="";
                                                String additionalInfo="";

                                                //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                                for(int k1=0;k1<attributesOfType.getLength();k1++){
                                                    Node att=attributesOfType.item(k1);                                                       
                                                    if(att.getNodeName().equalsIgnoreCase("name")){
                            //                            ////-System.out.println("\tName: "+att.getNodeValue());  
                                                        attName=att.getNodeValue();
                                                    }else if(att.getNodeName().equalsIgnoreCase("type")){
                              //                          ////-System.out.println("\tType: "+att.getNodeValue());
                                                        attType=att.getNodeValue();
                                                    }else{
                               //                         ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                        additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                    }                                          
                                                }
                                                //-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

                                                //Find the Type of the attName...
                                                //ITERATIVE PROCESS.........
                                                if(attType.startsWith(targetNamespacePrefix)){
                                                    //PSAXNW GIA TO TYPE MESA STO definition
                                    //                ////-System.out.println("#########################################  COMPLEX!!!! ITERATIVE");
                                                    String type1=attType.substring(4, attType.length());
                                                    if(type1.startsWith("ArrayOf")){                                                    
                                                        type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");  
                                                    }else if(type1.endsWith("Array")){
                                                        type1=type1.substring(0, type1.length()-5); 
                                                        //-ta.append("  ("+type1+"[])");  
                                                    }
                                                    else if(attType.endsWith("[]")){                                                    
                                                        type1=type1.replace("[]", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");  
                                                    }
                                                    
                                                    ComplexObject co1=new ComplexObject();
                                                     co1.setObjectName(new QName(attName));
                                                     co1.setObjectType(new QName(attType));
                                                     co1.setAdditionalInfo(additionalInfo);
                                                     
                                                    Node parsedAttribute=DocumentStyleWSDLParser.parseTypeIterativeForXSDImport(importedSchema,type1,0,false, co1);
                                                                                                                                                            
                                                     if(operationInputs!=null){                                                       
                                                        operationInputs.getHasNativeOrComplexObjects().add(co);
                                                    }else if(operationOutputs!=null){                                                    
                                                        operationOutputs.getHasNativeOrComplexObjects().add(co);
                                                    }
                                                    
                                                }else{ 
                                                   // if(attType.startsWith(nativeTypePrefix)){
                                                        //EINAI NATIVE TYPE
                                                        if(operationInputs!=null){
                                                            NativeObject no1=new NativeObject();
                                                            no1.setObjectName(new QName(attName));
                                                            no1.setObjectType(new QName(attType));
                                                            no1.setAdditionalInfo(additionalInfo);
                                                            operationInputs.getHasNativeOrComplexObjects().add(no1);
                                                        }else if(operationOutputs!=null){
                                                            NativeObject no1=new NativeObject();
                                                            no1.setObjectName(new QName(attName));
                                                            no1.setObjectType(new QName(attType));
                                                            no1.setAdditionalInfo(additionalInfo);
                                                            operationOutputs.getHasNativeOrComplexObjects().add(no1);
                                                        }
                                                    //}else{
                                                    
                                                   // }                            

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
    
    
    /*
    private static void parseImportedXSDforLiteral2_OLD(javax.wsdl.extensions.schema.Schema s1, String typeName, JTextArea ta, int iterNumber, 
            boolean fromLiteral, ComplexObject co, String xsdImportNamespace) {
        
//        //-System.out.println(typeName+"     ("+xsdImportNamespace+")");
        
        Map importsMap = s1.getImports();
        Iterator importsItt = importsMap.values().iterator();
        boolean typeParsingFinished=false;                    
                
        while (importsItt.hasNext()) {
            
            if(typeParsingFinished)break;
            
            List importsList = (List) importsItt.next();
            Iterator importsItt2 = importsList.iterator();
            while (importsItt2.hasNext()) {
                SchemaImportImpl schemaImportInitial = (SchemaImportImpl) importsItt2.next();
                SchemaImportImpl schemaImport=null;
                
            //    ////-System.out.println(schemaImportInitial.getNamespaceURI());
                
                boolean currentSchemaIsTheRightOne=false;                
                if(schemaImportInitial.getNamespaceURI()!=null&&schemaImportInitial.getNamespaceURI().equals(xsdImportNamespace)){
                    currentSchemaIsTheRightOne=true;  
                    schemaImport=schemaImportInitial;
                }else{
                    javax.wsdl.extensions.schema.Schema s11=schemaImportInitial.getReferencedSchema();
                    Map importsMap1 = s11.getImports();
                    Iterator importsItt1 = importsMap1.values().iterator();
                    while (importsItt1.hasNext()) {                        
                        if(currentSchemaIsTheRightOne)break;
                        List importsList1 = (List) importsItt1.next();
                        Iterator importsItt21 = importsList1.iterator();
                        while (importsItt21.hasNext()) {
                            
                            if(currentSchemaIsTheRightOne)break;                            
                            
                            schemaImportInitial = (SchemaImportImpl) importsItt21.next();
           //                 ////-System.out.println(schemaImportInitial.getNamespaceURI());
                            if(schemaImportInitial.getNamespaceURI()!=null&&schemaImportInitial.getNamespaceURI().equals(xsdImportNamespace)){
                                currentSchemaIsTheRightOne=true;  
                                schemaImport=schemaImportInitial;                                                             
                            }
                        }                
                    }
                }
                
                if(schemaImport!=null&&schemaImport.getNamespaceURI()!=null&&schemaImport.getNamespaceURI().equals(xsdImportNamespace)){
                    javax.wsdl.extensions.schema.Schema importedSchema = schemaImport.getReferencedSchema();                
                    org.w3c.dom.Element impSchElem=importedSchema.getElement();
                    if(impSchElem==null)continue;
                    
                    
                    //Attr attr=impSchElem.getAttributeNode(typeName);
                    //Attr att1=impSchElem.getAttributeNodeNS(xsdImportNamespace, typeName);
                    //if(attr!=null){
                    //    //-System.out.println(attr.getOwnerElement().getNodeName());
                    //}
                    
                    
                    //NodeList elementNodeList=impSchElem.getElementsByTagName("element");
                    //NodeList complexTypeNodeList=impSchElem.getElementsByTagName("complexType");                    
                    //NodeList simpleTypeNodeList=impSchElem.getElementsByTagName("simpleType");
                    ////-System.out.println(xsdImportNamespace);
                    ////-System.out.println(elementNodeList.getLength()+" "+complexTypeNodeList.getLength()+" "+simpleTypeNodeList.getLength());
                     

                    NodeList childrenOfChildOfSchema = impSchElem.getChildNodes();
                    
                    
                            
                    if(childrenOfChildOfSchema!=null){
                        for(int j=0;j<childrenOfChildOfSchema.getLength();j++){
                            
                            if(typeParsingFinished)break;
                            
                            Node n1=childrenOfChildOfSchema.item(j);
                           // //-System.out.println("ABCDEFG "+n1.getNodeName());

                            if(n1.getAttributes()==null)continue;

                            if(n1.getAttributes().getNamedItem("name")!=null){
            //                        ////-System.out.println("GAMWTO WWW WWW WWW: "+n1.getAttributes().getNamedItem("name").getNodeValue());
                            }

                            if(n1.getAttributes().getNamedItem("name")!=null&&
                                    n1.getAttributes().getNamedItem("name").getNodeValue()!=null&&
                                    n1.getAttributes().getNamedItem("name").getNodeValue().equals(typeName)){
                                //VRETHIKE TO TYPE!!!
                                
                                if(n1.getNodeName().contains("element")){
                                    NodeList childrenOfTheDamned=n1.getChildNodes();
                                    if(childrenOfTheDamned!=null){
                                        if(childrenOfTheDamned.getLength()>0){
                                            for(int k=0;k<childrenOfTheDamned.getLength();k++){
                 //                               ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
                                                NodeList childrenOfTheChildrenOfTheDamned=childrenOfTheDamned.item(k).getChildNodes();
                                                if(childrenOfTheChildrenOfTheDamned!=null){
                                                    for(int k2=0;k2<childrenOfTheChildrenOfTheDamned.getLength();k2++){
                                                        if(childrenOfTheChildrenOfTheDamned.item(k2).getNodeName().contains("element")){
                                                            NamedNodeMap attributesOfType=childrenOfTheChildrenOfTheDamned.item(k2).getAttributes();                                            
                                       //                     ////-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
                                                            String attName="";
                                                            String attType="";
                                                            String additionalInfo="";

                                                            //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                                            for(int k1=0;k1<attributesOfType.getLength();k1++){
                                                                Node att=attributesOfType.item(k1);                                                       
                                                                if(att.getNodeName().equalsIgnoreCase("name")){
                                               //                     ////-System.out.println("\tName: "+att.getNodeValue());  
                                                                    attName=att.getNodeValue();
                                                                }else if(att.getNodeName().equalsIgnoreCase("type")){
                                          //                          ////-System.out.println("\tType: "+att.getNodeValue());
                                                                    attType=att.getNodeValue();
                                                                }else{
                                           //                         ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                                    additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                                }                                          
                                                            }

                                                            //-ta.append("\n\t\t\tName: "+attName);
                                                            additionalInfo=additionalInfo.trim();
                                                            //-ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");

                                                            ////-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

                                                            //Find the Type of the attName...
                                                            //ITERATIVE PROCESS.........
                                                            if(attType.startsWith(targetNamespacePrefix)){ 
                                                                String type1=attType.substring(targetNamespacePrefix.length(), attType.length());

                                                                ComplexObject newComplexObject=new ComplexObject();
                                                                newComplexObject.objectName=attName;
                                                                newComplexObject.additionalInfo=additionalInfo;
                                                                newComplexObject.objectType=type1;                                                

                                                                if(type1.startsWith("ArrayOf")){                                                    
                                                                    type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                                    //-ta.append("  ("+type1+"[])");       
                                                                    newComplexObject.objectType=type1+"[]";
                                                                }else if(type1.endsWith("Array")){
                                                                    type1=type1.substring(0, type1.length()-5); 
                                                                    //-ta.append("  ("+type1+"[])");          
                                                                    newComplexObject.objectType=type1+"[]";
                                                                }
                                                                else if(attType.endsWith("[]")){                                                    
                                                                    type1=type1.replace("[]", "");                                               
                                                                    //-ta.append("  ("+type1+"[])");          
                                                                    newComplexObject.objectType=type1+"[]";
                                                                }                                        

                                                                parseTypeIterativeForXSDImport(s1, type1, ta, iterNumber+1,fromLiteral,newComplexObject);        

                                                                co.hasComplexObjects.add(newComplexObject);
                                                                typeParsingFinished=true;
                                                            }else{
                                                                if(nativeTypePrefix!=null&&attType.startsWith(nativeTypePrefix)){
                                                                     NativeObject newNativeObject=new NativeObject();
                                                                     newNativeObject.objectName=attName;
                                                                     newNativeObject.additionalInfo=additionalInfo;
                                                                     newNativeObject.objectType=attType;     
                                                                     co.hasNativeObjects.add(newNativeObject); 
                                                                     typeParsingFinished=true;
                                                                }else{
                                                                    if(attType.contains(":")){
                                                                        String key1=attType.substring(0, attType.indexOf(":"));
                                                                        String xsdNamespace=(String)namespaces.get(key1);
                                                                        if(xsdNamespace!=null){
                                                             //               ////-System.out.println("Namespace Found!!!");   
                                                                            String type1=attType.substring(key1.length()+1, attType.length());
                                                                            ComplexObject newComplexObject=new ComplexObject();
                                                                            newComplexObject.objectName=attName;
                                                                            newComplexObject.additionalInfo=additionalInfo;
                                                                            newComplexObject.objectType=type1;    

                                                                            parseImportedXSDforLiteral3(s1, type1, iterNumber+1,fromLiteral,newComplexObject,
                                                                                    xsdNamespace);
                                                                            co.hasComplexObjects.add(newComplexObject);
                                                                            typeParsingFinished=true;

                                                                        }else{
                                                           //                 ////-System.out.println("Namespace was null... Will be treated as NATIVE TYPE...");
                                                                             NativeObject newNativeObject=new NativeObject();
                                                                             newNativeObject.objectName=attName;
                                                                             newNativeObject.additionalInfo=additionalInfo;
                                                                             newNativeObject.objectType=attType;     
                                                                             co.hasNativeObjects.add(newNativeObject);  
                                                                             typeParsingFinished=true;
                                                                        }
                                                                    }                                                   

                                                                }

                                                            }
                                                        }else {//EINAI SIMPLE TYPE
                                                            if(childrenOfTheChildrenOfTheDamned.item(k2).getNodeName().contains("constraint")){
                                                                //PARSE THE ENUMERATION...
                                                            }
                                                        }


                                                    }
                                                }

                                            }
                                        }else{
                                            //find "type" attribute and parse the type defined here
                                            if(n1.getAttributes()!=null&&n1.getAttributes().getNamedItem("type")!=null){
                                        //        ////-System.out.println("asdfsdf   "+n1.getAttributes().getNamedItem("type").getNodeValue());
                                                String typeA=n1.getAttributes().getNamedItem("type").getNodeValue();
                                                
                                                if(typeA!=null&&typeA.contains(":")){
                                                    String key1=typeA.substring(0, typeA.indexOf(":"));
                                                    String xsdNamespace=(String)namespaces.get(key1);
                                                    if(xsdNamespace!=null){
                                             //           ////-System.out.println("Namespace Found!!!");  
                                                        String type1=typeA.substring(key1.length()+1, typeA.length());
                                                        parseImportedXSDforLiteral3(s1, type1, iterNumber+1,fromLiteral,co,
                                                                                    xsdNamespace);
                                                    }
                                                }else{
                                                    try{
                                                        parseImportedXSDforLiteral3(s1, typeA, ta, iterNumber+1,fromLiteral,co,
                                                                                        xsdImportNamespace);
                                                        typeParsingFinished=true;
                                                    }catch(Exception e1){
                                                        
                                                    }
                                                }
                                                
                                                
                                            }
                                        }

                                    }
                                }else if(n1.getNodeName().contains("simpleType")){
                                    String attName=typeName;
                                    String attType="";
                                    String additionalInfo=""; //edw tha mpoun ta restrictions...
                                    
                                    if(n1.getAttributes().getNamedItem("name").getNodeValue()!=null)
                                        attName=n1.getAttributes().getNamedItem("name").getNodeValue();
                                    NodeList childrenOfTheDamned=n1.getChildNodes();
                                    if(childrenOfTheDamned!=null){
                                        for(int k=0;k<childrenOfTheDamned.getLength();k++){
                                //            ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
                                            if(childrenOfTheDamned.item(k).getNodeName().contains("restriction")){
                                                                                                
                                                if(childrenOfTheDamned.item(k).getAttributes().getNamedItem("base")!=null){
                                                    attType=childrenOfTheDamned.item(k).getAttributes().getNamedItem("base").getNodeValue();
                                                }
                                                
                                                NodeList childrenOfTheChildrenOfTheDamned=childrenOfTheDamned.item(k).getChildNodes();
                                                if(childrenOfTheChildrenOfTheDamned!=null){
                                                    for(int k2=0;k2<childrenOfTheChildrenOfTheDamned.getLength();k2++){
                                                        Node restrictionEnumNode=childrenOfTheChildrenOfTheDamned.item(k2);
                                                        if(!restrictionEnumNode.hasAttributes())continue;
                                                        if(additionalInfo.equals(""))additionalInfo="Accepted Values: ";
                                                                           
                                                        String str=restrictionEnumNode.getAttributes().getNamedItem("value").getNodeValue();
                                                        if(additionalInfo.equals("Accepted Values: ")){
                                                            additionalInfo+=str+"";
                                                        }else{
                                                            additionalInfo+=", "+str;
                                                        }
                                                    }
                                                }
                                                
                                                 NativeObject newNativeObject=new NativeObject();
                                                 newNativeObject.objectName=attName;
                                                 newNativeObject.additionalInfo=additionalInfo;
                                                 newNativeObject.objectType=attType;     
                                                 co.hasNativeObjects.add(newNativeObject); 
                                                 typeParsingFinished=true;
                                                    
                                            }
                                        }
                                    }
                                    
                                }else if(n1.getNodeName().contains("complexType")){
                                    NodeList childrenOfTheDamned=n1.getChildNodes();
                                    if(childrenOfTheDamned!=null){
                                        for(int k=0;k<childrenOfTheDamned.getLength();k++){
                                           // //-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
                                            if(childrenOfTheDamned.item(k).getNodeName().contains("sequence")||childrenOfTheDamned.item(k).getNodeName().contains("choice")){
                                                parseSequenceOrChoice(s1,childrenOfTheDamned.item(k), iterNumber, co, ta);
                                                typeParsingFinished=true;
                                               

                                            }else if(childrenOfTheDamned.item(k).getNodeName().contains("complexContent")){
                                                parseComplexContentForDocumentType(s1,childrenOfTheDamned.item(k), iterNumber, co, ta);
                                                
                                                
                                                

                                            //}else if(childrenOfTheDamned.item(k).getNodeName().contains("extension")){
                                                                                                
                                                
                                            }
                                            

                                        }
                                    }
                                    
                                }else{
                                    //-System.out.println("GIA VALE ME TO NOUS...");
                                }

                            }                       
                        }
                    }                
                }
            }
        }
    }
    */
      
    
    
   
       
       
    

    
   
    
    
    /*
    private static void parseImportedXSDforLiteralITERATIVE(javax.wsdl.extensions.schema.Schema schema, QName inPartType, JTextArea ta,
            WSOperationInput operationInputs, WSOperationOutput operationOutputs){
         
        Map importsMap = schema.getImports();
        Iterator importsItt = importsMap.values().iterator();
        while (importsItt.hasNext()) {
            List importsList = (List) importsItt.next();
            Iterator importsItt2 = importsList.iterator();
            while (importsItt2.hasNext()) {
                SchemaImportImpl schemaImport = (SchemaImportImpl) importsItt2.next();
                javax.wsdl.extensions.schema.Schema importedSchema = schemaImport.getReferencedSchema();                
                org.w3c.dom.Element impSchElem=importedSchema.getElement();
                if(impSchElem==null)continue;
                
                NodeList childrenOfChildOfSchema = impSchElem.getChildNodes();
                if(childrenOfChildOfSchema!=null){
                    for(int j=0;j<childrenOfChildOfSchema.getLength();j++){
                        Node n1=childrenOfChildOfSchema.item(j);
                        ////-System.out.println("ABCDEFG "+n1.getNodeName());
                        
                        if(n1.getAttributes()==null)continue;
                        
                        //SE AYTO TO EPIPEDO EINAI TA IMPORTED SCHEMA pou yparxoun...
                        if(n1.getAttributes().getNamedItem("name")!=null){
                            Node nn1=n1.getAttributes().getNamedItem("name");
                            ////-System.out.println("WWW GAMWTOOOO W:   " +nn1.getNodeValue());
                        }
                        
                       if(n1.getNodeName()!=null&&n1.getNodeName().contains("import")){                            
                            
                            parseImportedXSDforLiteralITERATIVE(importedSchema, inPartType, ta,
                                operationInputs, operationOutputs);                                
                            
                        }
                        
                        if(n1.getAttributes().getNamedItem("name")!=null&&
                                n1.getAttributes().getNamedItem("name").getNodeValue()!=null&&
                                n1.getAttributes().getNamedItem("name").getNodeValue().equals(inPartType.getLocalPart())){
                            
                            NodeList childrenOfTheDamned=n1.getChildNodes();
                            if(childrenOfTheDamned!=null){
                                for(int k=0;k<childrenOfTheDamned.getLength();k++){
                                    ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
                                    NodeList childrenOfTheChildrenOfTheDamned=childrenOfTheDamned.item(k).getChildNodes();
                                    if(childrenOfTheChildrenOfTheDamned!=null){
                                        for(int k2=0;k2<childrenOfTheChildrenOfTheDamned.getLength();k2++){
                                            if(childrenOfTheChildrenOfTheDamned.item(k2).getNodeName().contains("element")){
                                                NamedNodeMap attributesOfType=childrenOfTheChildrenOfTheDamned.item(k2).getAttributes();                                            
                                                ////-System.out.println("WWW GAMWTOOOO...... TYPES... FROM IMPORT!!!!!!");
                                                String attName="";
                                                String attType="";
                                                String additionalInfo="";

                                                //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                                for(int k1=0;k1<attributesOfType.getLength();k1++){
                                                    Node att=attributesOfType.item(k1);                                                       
                                                    if(att.getNodeName().equalsIgnoreCase("name")){
                                                        ////-System.out.println("\tName: "+att.getNodeValue());  
                                                        attName=att.getNodeValue();
                                                    }else if(att.getNodeName().equalsIgnoreCase("type")){
                                                        ////-System.out.println("\tType: "+att.getNodeValue());
                                                        attType=att.getNodeValue();
                                                    }else{
                                                        ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                        additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                    }                                          
                                                }
                                                
                                                //-ta.append("\n\t\t\tName: "+attName);
                                                additionalInfo=additionalInfo.trim();
                                                //-ta.append("\n\t\t\tType: "+attType+"  ("+additionalInfo+")");
                                                
                                                ////-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

                                                //Find the Type of the attName...
                                                //ITERATIVE PROCESS.........
                                                if(attType.startsWith(nativeTypePrefix)){
                                                    //EINAI NATIVE TYPE
                                                    ////-ta.append("\n");
                                                    
                                                    //Create new native type kai valto mesa sta INPUTS i OUTPUTS tou current Operation
                                                    if(operationInputs!=null){
                                                        NativeObject no=new NativeObject();
                                                        no.objectName=attName;
                                                        no.objectType=attType;
                                                        no.additionalInfo=additionalInfo;
                                                        operationInputs.getHasNativeOrComplexObjects().add(no);
                                                    }else if(operationOutputs!=null){
                                                        NativeObject no=new NativeObject();
                                                        no.objectName=attName;
                                                        no.objectType=attType;
                                                        no.additionalInfo=additionalInfo;
                                                        operationOutputs.getHasNativeOrComplexObjects().add(no);
                                                    }
                                                }else if(attType.startsWith(targetNamespacePrefix)){
                                                    //PSAXNW GIA TO TYPE MESA STO definition
                                                    ////-System.out.println("#########################################  COMPLEX!!!! ITERATIVE");
                                                    String type1=attType.substring(4, attType.length());
                                                    
                                                    ComplexObject co=new ComplexObject();
                                                     co.objectName=attName;
                                                     co.objectType=attType;
                                                    
                                                    if(type1.startsWith("ArrayOf")){                                                    
                                                        type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");  
                                                    }else if(type1.endsWith("Array")){
                                                        type1=type1.substring(0, type1.length()-5); 
                                                        //-ta.append("  ("+type1+"[])");  
                                                    }
                                                    else if(attType.endsWith("[]")){                                                    
                                                        type1=type1.replace("[]", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");  
                                                    }                                                                                   
                                                    co.objectType=type1;
                                                     
                                                     co.additionalInfo=additionalInfo;                                                   
                                                    
                                                    Node parsedAttribute=parseTypeIterativeForXSDImport(importedSchema,type1,ta,0,true,co);
                                                    
                                                    if(operationInputs!=null){                                                       
                                                        operationInputs.getHasNativeOrComplexObjects().add(co);
                                                    }else if(operationOutputs!=null){                                                    
                                                        operationOutputs.getHasNativeOrComplexObjects().add(co);
                                                    }
                                                    
                                                }else{

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
     */ 
    
    

    
    
        
    
    private static void parseOperation(WSOperation mitsosOperation, Definition definition, Operation operation,  String operationUse, String bindingStyle){
        Map inputPartsMap = operation.getInput().getMessage().getParts();
        Collection inputParts = inputPartsMap.values();
        Iterator inputPartIter = inputParts.iterator();

        if(operationUse==null)operationUse="unknown";
        if(bindingStyle==null){
            bindingStyle="unknown";
        }else if(bindingStyle.equals("http"))operationUse="http";
                
        ////-System.out.println("\n######## INPUT ########");
        //-ta.append("\n\t\t#Inputs:");
                
        WSOperationInput mitsosOperationInput=new WSOperationInput();
                        
        while (inputPartIter.hasNext())
        {
            Part part = (Part)inputPartIter.next();
                        
                                    
            if(operationUse.equals("encoded")){
                if(bindingStyle.equals("rpc")){
                    try{
                        String inPartName = part.getName();
                        //-ta.append("\n\n\t\t\tName: "+inPartName);
                        QName inPartType=part.getTypeName();            
                        String s11111=inPartType.getLocalPart();
                        //-ta.append("\n\t\t\tType: "+inPartType.getLocalPart());
                                                
                        String stringToAppendForArray=null;
                        
                        if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("Array")){
                            stringToAppendForArray="  ( "+inPartType.getLocalPart().substring(0,inPartType.getLocalPart().length()-5) +"[] )";                                                        
                        }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().startsWith("ArrayOf")){
                            stringToAppendForArray="  ( "+inPartType.getLocalPart().replaceFirst("ArrayOf", "")+"[] )";                                                    
                        }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("[]")){                                                    
                            stringToAppendForArray="  ( "+inPartType.getLocalPart()+" )";                                       
                        } 
                        if(stringToAppendForArray!=null){
                            //-ta.append(stringToAppendForArray);
                        }
                        
                        NativeObject no=null;
                        ComplexObject co=null;
                        
                        if(stringToAppendForArray==null){//DEN EINAI ARRAY
                            if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                //IT IS A NATIVE TYPE
                                no=new NativeObject();
                                no.setObjectName(new QName(inPartName));
                                no.setObjectType(new QName(inPartType.getLocalPart()+" (NATIVE)"));
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(no);
                            }else{
                                //IT IS A COMPLEX TYPE
                                co=new ComplexObject();
                                co.setObjectName(new QName(inPartName));
                                co.setObjectType(new QName(inPartType.getLocalPart()+" (COMPLEX)"));
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(co);
                            }
                        }else{
                            if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                //IT IS A NATIVE TYPE
                                no=new NativeObject();
                                no.setObjectName(new QName(inPartName));
                                no.setObjectType(new QName(stringToAppendForArray+" (ARRAY))"));
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(no);
                            }else{
                                //IT IS A COMPLEX TYPE
                                co=new ComplexObject();
                                co.setObjectName(new QName(inPartName));
                                co.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(co);
                            }
                        }
                        

                        ////-System.out.println(inPartName);            
                        ////-System.out.println(part.getTypeName().getLocalPart());
                        //////-System.out.println(part.getElementName().getLocalPart());

                        parseType(definition, inPartType,  no, co);                        
                        
                        /*
                        if(stringToAppendForArray==null){//DEN EINAI ARRAY
                            if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){                                
                                //IT IS A NATIVE TYPE
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(no);
                            }else{
                                //IT IS A COMPLEX TYPE                                
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(co);
                            }
                        }else{
                            if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                //IT IS A NATIVE TYPE                                
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(no);
                            }else{
                                //IT IS A COMPLEX TYPE                                
                                mitsosOperationInput.getHasNativeOrComplexObjects().add(co);
                            }
                        }
                        */
                        
                    }catch(Exception e){
                        //e.printStackTrace();
                        ////-System.out.println("Parse exception:"+e.getMessage());
                        ////-System.out.println("Continue...");
                    }
                }else if(operationUse.equals("document")){
                    //-ta.append("\n\t\t\tCAUTION!!! Not supported yet:  style: "+bindingStyle+" / use:"+operationUse);         
                }else{
                    //-ta.append("\n\t\t\tCAUTION!!!:  style: "+bindingStyle+" / use:"+operationUse);
                }
            }else if(operationUse.equals("literal")){                
                    try{             
                        if(bindingStyle.equals("document")){
                            try{
                                if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("Array")){
                                    //-ta.append("  ( "+part.getElementName().getLocalPart().substring(0,part.getElementName().getLocalPart().length()-5) +"[] )");
                                }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().startsWith("ArrayOf")){
                                    //-ta.append("  ( "+part.getElementName().getLocalPart().replaceFirst("ArrayOf", "")+"[] )");                        
                                }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("[]")){                                                    
                                    //-ta.append("  ( "+part.getElementName().getLocalPart()+" )");           
                                }                    
                            }catch(Exception e){

                            }

                            if(part.getElementName()!=null){
                                DocumentStyleWSDLParser.parseDocumentType2(definition, part.getElementName(),  mitsosOperationInput, null, part);              
                            }else if(part.getName()!=null){
                                DocumentStyleWSDLParser.parseDocumentType2(definition, part.getElementName(), mitsosOperationInput, null, part);              
                            }else{
                                DocumentStyleWSDLParser.parseDocumentType2(definition, part.getElementName(), mitsosOperationInput, null, part);              
                            }
                        }else if(bindingStyle.equals("rpc")){
                            //KWDIKAS GIA Nuremberg POI SEarch WSs WSDL   
                            ////-ta.append("\n\t\t\tCAUTION!!! Not supported yet:  style: "+bindingStyle+" / use:"+operationUse);  
                            try{
                                String inPartName = part.getName();
                                //-ta.append("\n\n\t\t\tName: "+inPartName);
                                QName inPartType=part.getTypeName();            
                                String s11111=inPartType.getLocalPart();
                                //-ta.append("\n\t\t\tType: "+inPartType.getLocalPart());

                                String stringToAppendForArray=null;
                        
                                if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("Array")){
                                    stringToAppendForArray="  ( "+inPartType.getLocalPart().substring(0,inPartType.getLocalPart().length()-5) +"[] )";                                                        
                                }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().startsWith("ArrayOf")){
                                    stringToAppendForArray="  ( "+inPartType.getLocalPart().replaceFirst("ArrayOf", "")+"[] )";                                                    
                                }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("[]")){                                                    
                                    stringToAppendForArray="  ( "+inPartType.getLocalPart()+" )";                                       
                                } 
                                if(stringToAppendForArray!=null){
                                    //-ta.append(stringToAppendForArray);
                                }

                                
                                NativeObject no=null;
                                ComplexObject co=null;
                                        
                                if(stringToAppendForArray==null){//DEN EINAI ARRAY
                                    if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                        //IT IS A NATIVE TYPE
                                        no=new NativeObject();
                                        no.setObjectName(new QName(inPartName));
                                        no.setObjectType(new QName(inPartType.getLocalPart()+" (NATIVE)"));
                                        mitsosOperationInput.getHasNativeOrComplexObjects().add(no);
                                    }else{
                                        //IT IS A COMPLEX TYPE
                                        co=new ComplexObject();
                                        co.setObjectName(new QName(inPartName));
                                        co.setObjectType(new QName(inPartType.getLocalPart()+" (COMPLEX)"));
                                        mitsosOperationInput.getHasNativeOrComplexObjects().add(co);
                                    }
                                }else{
                                    if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                        //IT IS A NATIVE TYPE
                                        no=new NativeObject();
                                        no.setObjectName(new QName(inPartName));
                                        no.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                        mitsosOperationInput.getHasNativeOrComplexObjects().add(no);
                                    }else{
                                        //IT IS A COMPLEX TYPE
                                        co=new ComplexObject();
                                        co.setObjectName(new QName(inPartName));
                                        co.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                        mitsosOperationInput.getHasNativeOrComplexObjects().add(co);
                                    }
                                }
                                
                                ////-System.out.println(inPartName);            
                                ////-System.out.println(part.getTypeName().getLocalPart());
                                //////-System.out.println(part.getElementName().getLocalPart());

                                parseType(definition, inPartType,  no, co);
                            }catch(Exception e){
                                //e.printStackTrace();
                                ////-System.out.println("Parse exception:"+e.getMessage());
                                ////-System.out.println("Continue...");
                            }
                        }else{
                            //-ta.append("\n\t\t\tCAUTION!!!:  style: "+bindingStyle+" / use:"+operationUse);    
                        }
                    }catch(Exception e){
                        ////-System.out.println("Parse exception:"+e.getMessage());
                        ////-System.out.println("Continue...");
                        //e.printStackTrace();
                    }
                                
                }else if(operationUse.equals("http")){
                    ////-ta.append("\n\t\t\tCAUTION!!!:  http Service operation!: "+bindingStyle+" / use:"+operationUse);
                    try{
                        if(part.getName()!=null&&part.getName().endsWith("Array")){
                            //-ta.append("  ( "+part.getName().substring(0,part.getName().length()-5) +"[] )");
                        }else if(part.getName()!=null&&part.getName().startsWith("ArrayOf")){
                            //-ta.append("  ( "+part.getName().replaceFirst("ArrayOf", "")+"[] )");                        
                        }else if(part.getName()!=null&&part.getName().endsWith("[]")){                                                    
                            //-ta.append("  ( "+part.getName()+" )");           
                        }                    
                    }catch(Exception e){

                    }

                    if(part.getName()!=null){
                        //parseHttpType(definition, part.getName(), ta);              
                        parseHttpType(definition, part.getName());              
                    }  
                                    
                    
                }else {
                    if(bindingStyle.equals("rpc")){
                        //-ta.append("\n\t\t\tCAUTION!!!:  style: "+bindingStyle+" / use:"+operationUse);
                    }else{
                        //-ta.append("\n\t\t\tCAUTION!!!:  style: "+bindingStyle+" / use:"+operationUse);
                    }
                }
            
                /*
                try{
                    String inPartName = part.getName();
                    //-ta.append("\n\n\t\t\tName: "+inPartName);
                    QName inPartType=part.getTypeName();            
                    String s11111=inPartType.getLocalPart();
                    //-ta.append("\n\t\t\tType: "+inPartType.getLocalPart());
                    
                    if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("Array")){
                        //-ta.append("  ( "+inPartType.getLocalPart().substring(0,inPartType.getLocalPart().length()-5) +"[] )");
                    }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().startsWith("ArrayOf")){
                        //-ta.append("  ( "+inPartType.getLocalPart().replaceFirst("ArrayOf", "")+"[] )");                        
                    }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("[]")){                                                    
                        //-ta.append("  ( "+inPartType.getLocalPart()+" )");           
                    }  

                    ////-System.out.println(inPartName);            
                    ////-System.out.println(part.getTypeName().getLocalPart());
                    //////-System.out.println(part.getElementName().getLocalPart());

                    parseType(definition, inPartType, ta);
                }catch(Exception e){
                    //-ta.append("\n\t\t\tElement: "+part.getElementName().getLocalPart());
                        
                        if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("Array")){
                            //-ta.append("  ( "+part.getElementName().getLocalPart().substring(0,part.getElementName().getLocalPart().length()-5) +"[] )");
                        }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().startsWith("ArrayOf")){
                            //-ta.append("  ( "+part.getElementName().getLocalPart().replaceFirst("ArrayOf", "")+"[] )");                        
                        }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("[]")){                                                    
                            //-ta.append("  ( "+part.getElementName().getLocalPart()+" )");           
                        }    
                    parseType(definition, part.getElementName(), ta);
                    //parseElementType(definition, part.getElementName(), ta);                

                }   
                 */                      
                    
               
        }   
        
        mitsosOperation.setHasInput(mitsosOperationInput);
                
        
        WSOperationOutput mitsosOperationOutput=new WSOperationOutput();
        
        Map outputPartsMap = operation.getOutput().getMessage().getParts();
        Collection outputParts = outputPartsMap.values();
        Iterator outputPartIter = outputParts.iterator();
        
        ////-System.out.println("\n######## OUTPUT ########");
        //-ta.append("\n\t\t#Outputs:");
        while (outputPartIter.hasNext())
        {
            
            Part part = (Part)outputPartIter.next();
                       
            if(operationUse.equals("encoded")){                
                try{
                    if(bindingStyle.equals("rpc")){
                        String inPartName = part.getName();
                        //-ta.append("\n\n\t\t\tName: "+inPartName);
                        QName inPartType=part.getTypeName();            
                        String s11111=inPartType.getLocalPart();
                        //-ta.append("\n\t\t\tType: "+inPartType.getLocalPart());

                        String stringToAppendForArray=null;
                        
                        if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("Array")){
                            stringToAppendForArray="  ( "+inPartType.getLocalPart().substring(0,inPartType.getLocalPart().length()-5) +"[] )";                                                        
                        }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().startsWith("ArrayOf")){
                            stringToAppendForArray="  ( "+inPartType.getLocalPart().replaceFirst("ArrayOf", "")+"[] )";                                                    
                        }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("[]")){                                                    
                            stringToAppendForArray="  ( "+inPartType.getLocalPart()+" )";                                       
                        } 
                        if(stringToAppendForArray!=null){
                            //-ta.append(stringToAppendForArray);
                        }
                        
                        NativeObject no=null;
                        ComplexObject co=null;
                        
                        if(stringToAppendForArray==null){//DEN EINAI ARRAY
                            if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                //IT IS A NATIVE TYPE
                                no=new NativeObject();
                                no.setObjectName(new QName(inPartName));
                                no.setObjectType(new QName(inPartType.getLocalPart()+" (NATIVE)"));
                                mitsosOperationOutput.getHasNativeOrComplexObjects().add(no);
                            }else{
                                //IT IS A COMPLEX TYPE
                                co=new ComplexObject();
                                co.setObjectName(new QName(inPartName));
                                co.setObjectType(new QName(inPartType.getLocalPart()+" (COMPLEX)"));
                                mitsosOperationOutput.getHasNativeOrComplexObjects().add(co);
                            }
                        }else{
                            if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                //IT IS A NATIVE TYPE
                                no=new NativeObject();
                                no.setObjectName(new QName(inPartName));
                                no.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                mitsosOperationOutput.getHasNativeOrComplexObjects().add(no);
                            }else{
                                //IT IS A COMPLEX TYPE
                                co=new ComplexObject();
                                co.setObjectName(new QName(inPartName));
                                co.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                mitsosOperationOutput.getHasNativeOrComplexObjects().add(co);
                            }
                        }

                        
                        ////-System.out.println(inPartName);            
                        ////-System.out.println(part.getTypeName().getLocalPart());
                        //////-System.out.println(part.getElementName().getLocalPart());

                        parseType(definition, inPartType,  no, co);
                    }else if(bindingStyle.equals("document")){
                        // document/encoded  
                        //-ta.append("\n\t\t\tCAUTION!!!:  style: "+bindingStyle+" / use:"+operationUse+"  --NOT SUPPORTED!!!");
                    }
                }catch(Exception e){
                    //e.printStackTrace();
                    ////-System.out.println("Parse exception:"+e.getMessage());
                    ////-System.out.println("Continue...");
                }
                
            }else if(operationUse.equals("literal")){
                try{
                    if(bindingStyle.equals("rpc")){
                        // rpc/literal
                        try{
                            String inPartName = part.getName();
                            //-ta.append("\n\n\t\t\tName: "+inPartName);
                            QName inPartType=part.getTypeName();            
                            String s11111=inPartType.getLocalPart();
                            //-ta.append("\n\t\t\tType: "+inPartType);

                            String stringToAppendForArray=null;
                        
                            if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("Array")){
                                stringToAppendForArray="  ( "+inPartType.getLocalPart().substring(0,inPartType.getLocalPart().length()-5) +"[] )";                                                        
                            }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().startsWith("ArrayOf")){
                                stringToAppendForArray="  ( "+inPartType.getLocalPart().replaceFirst("ArrayOf", "")+"[] )";                                                    
                            }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("[]")){                                                    
                                stringToAppendForArray="  ( "+inPartType.getLocalPart()+" )";                                       
                            } 
                            if(stringToAppendForArray!=null){
                                //-ta.append(stringToAppendForArray);
                            }
                            
                            NativeObject no=null;
                            ComplexObject co=null;

                            if(stringToAppendForArray==null){//DEN EINAI ARRAY
                                if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                    //IT IS A NATIVE TYPE
                                    no=new NativeObject();
                                    no.setObjectName(new QName(inPartName));
                                    no.setObjectType(new QName(inPartType.getLocalPart()+" (NATIVE)"));
                                    mitsosOperationOutput.getHasNativeOrComplexObjects().add(no);
                                }else{
                                    //IT IS A COMPLEX TYPE
                                    co=new ComplexObject();
                                    co.setObjectName(new QName(inPartName));
                                    co.setObjectType(new QName(inPartType.getLocalPart()+" (COMPLEX)"));
                                    mitsosOperationOutput.getHasNativeOrComplexObjects().add(co);
                                }
                            }else{
                                if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                                    //IT IS A NATIVE TYPE
                                    no=new NativeObject();
                                    no.setObjectName(new QName(inPartName));
                                    no.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                    mitsosOperationOutput.getHasNativeOrComplexObjects().add(no);
                                }else{
                                    //IT IS A COMPLEX TYPE
                                    co=new ComplexObject();
                                    co.setObjectName(new QName(inPartName));
                                    co.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                                    mitsosOperationOutput.getHasNativeOrComplexObjects().add(co);
                                }
                            }

                            ////-System.out.println(inPartName);            
                            ////-System.out.println(part.getTypeName().getLocalPart());
                            //////-System.out.println(part.getElementName().getLocalPart());

                            parseType(definition, inPartType, no, co);
                        }catch(Exception e){
                            //e.printStackTrace();
                            ////-System.out.println("Parse exception:"+e.getMessage());
                            ////-System.out.println("Continue...");
                        }
                    }else if(bindingStyle.equals("document")){
                        // document/literal  
                        if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("Array")){
                            //-ta.append("  ( "+part.getElementName().getLocalPart().substring(0,part.getElementName().getLocalPart().length()-5) +"[] )");
                        }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().startsWith("ArrayOf")){
                            //-ta.append("  ( "+part.getElementName().getLocalPart().replaceFirst("ArrayOf", "")+"[] )");                        
                        }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("[]")){                                                    
                            //-ta.append("  ( "+part.getElementName().getLocalPart()+" )");           
                        }  

                        DocumentStyleWSDLParser.parseDocumentType2(definition, part.getElementName(),  null ,mitsosOperationOutput, part);                           
                    }
                }catch(Exception e){
                    //e.printStackTrace();
                    ////-System.out.println("Parse exception:"+e.getMessage());
                    ////-System.out.println("Continue...");
                }

            }else{
                try{
                    String inPartName = part.getName();
                    //-ta.append("\n\n\t\t\tName: "+inPartName);
                    QName inPartType=part.getTypeName();            
                    //String s11111=inPartType.getLocalPart();
                    //-ta.append("\n\t\t\tType: "+inPartType.getLocalPart());
                    
                    String stringToAppendForArray=null;
                    //-GIORGOS
                    if (inPartType==null){
                        //System.out.println(ws.getServiceURL()+" unsuccessfully parsed");
                        return;
                    }
                    //-GIORGOS.    
                    if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("Array")){
                        stringToAppendForArray="  ( "+inPartType.getLocalPart().substring(0,inPartType.getLocalPart().length()-5) +"[] )";                                                        
                    }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().startsWith("ArrayOf")){
                        stringToAppendForArray="  ( "+inPartType.getLocalPart().replaceFirst("ArrayOf", "")+"[] )";                                                    
                    }else if(inPartType.getLocalPart()!=null&&inPartType.getLocalPart().endsWith("[]")){                                                    
                        stringToAppendForArray="  ( "+inPartType.getLocalPart()+" )";                                       
                    } 
                    if(stringToAppendForArray!=null){
                        //-ta.append(stringToAppendForArray);
                    }

                    NativeObject no=null;
                    ComplexObject co=null;
                    
                    if(stringToAppendForArray==null){//DEN EINAI ARRAY
                        if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                            //IT IS A NATIVE TYPE
                            no=new NativeObject();
                            no.setObjectName(new QName(inPartName));
                            no.setObjectType(new QName(inPartType.getLocalPart()+" (NATIVE)"));
                            mitsosOperationOutput.getHasNativeOrComplexObjects().add(no);
                        }else{
                            //IT IS A COMPLEX TYPE
                            co=new ComplexObject();
                            co.setObjectName(new QName(inPartName));
                            co.setObjectType(new QName(inPartType.getLocalPart()+" (COMPLEX)"));
                            mitsosOperationOutput.getHasNativeOrComplexObjects().add(co);
                        }
                    }else{
                        if(inPartType.getNamespaceURI().equals(namespaces.get(nativeTypePrefix.replace(":", "")))){
                            //IT IS A NATIVE TYPE
                            no=new NativeObject();
                            no.setObjectName(new QName(inPartName));
                            no.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                            mitsosOperationOutput.getHasNativeOrComplexObjects().add(no);
                        }else{
                            //IT IS A COMPLEX TYPE
                            co=new ComplexObject();
                            co.setObjectName(new QName(inPartName));
                            co.setObjectType(new QName(stringToAppendForArray+" (ARRAY)"));
                            mitsosOperationOutput.getHasNativeOrComplexObjects().add(co);
                        }
                    }

                    ////-System.out.println(inPartName);            
                    ////-System.out.println(part.getTypeName().getLocalPart());
                    //////-System.out.println(part.getElementName().getLocalPart());

                    parseType(definition, inPartType,  no, co);
                    
                }catch(Exception e){
                    e.printStackTrace();
                    /*
                    //-ta.append("\n\t\t\tType (El): "+part.getElementName().getLocalPart());
                    if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("Array")){
                        //-ta.append("  ( "+part.getElementName().getLocalPart().substring(0,part.getElementName().getLocalPart().length()-5) +"[] )");
                    }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().startsWith("ArrayOf")){
                        //-ta.append("  ( "+part.getElementName().getLocalPart().replaceFirst("ArrayOf", "")+"[] )");                        
                    }else if(part.getElementName().getLocalPart()!=null&&part.getElementName().getLocalPart().endsWith("[]")){                                                    
                        //-ta.append("  ( "+part.getElementName().getLocalPart()+" )");           
                    }   
                    parseType(definition, part.getElementName(), ta);*/
                    //parseElementType(definition, part.getElementName(), ta);   
                }  
            }
            
                          
        }
        
        mitsosOperation.setHasOutput(mitsosOperationOutput);
        ///////////////////////////////////////////////////////////////////////
        //- GIORGOS
        ///////////////////////////////////////////////////////////////////////
//        ServiceOperation so = new ServiceOperation();
//        so.setName(mitsosOperation.getOperationName().split(" ")[0].trim());
//        WSOperationInput mi = mitsosOperation.getHasInput();
//        WSOperationOutput mo = mitsosOperation.getHasOutput();
//        // mitsosOperation OK. Mou leipei to domain 
//        
//        
//        //build Operations:I/O:Parameters
//        OperationInput opIn = new OperationInput();
//        ArrayList inputs = new ArrayList();
//        
//        for (int i=0;i<mi.getHasNativeOrComplexObjects().size();i++){
//            Parameter par = new Parameter();
//            if (mi.getHasNativeOrComplexObjects().get(i).getClass().toString().contains("NativeObject")){
//                NativeObject no = (NativeObject) mi.getHasNativeOrComplexObjects().get(i);
//                par.setParName(no.getObjectName());
//                par.setParType(no.getObjectType().split(" ")[0].trim().replaceAll("s:", "").replaceAll("xsd:", ""));
//            }
//            else {
//                ComplexObject co = (ComplexObject) mi.getHasNativeOrComplexObjects().get(i);
//                par.setParName(co.getObjectName());
//                String a = co.getObjectType().trim();
//                if (a.startsWith("(")){
//                    a=a.split(" ")[1];
//                }
//                else{
//                    a=a.split(" ")[0];
//                }
//                
//                par.setParType(a);
//                //par.setParType(co.getObjectType().split(" ")[0].trim().replaceAll("s:", "").replaceAll("xsd:", ""));
//           
//            }
//            inputs.add(par);
//        }
//        opIn.setParametersList(inputs);
//
//        
//        OperationOutput opOut = new OperationOutput();
//        Parameter par = new Parameter();
//  
//        for (int i=0;i<mo.getHasNativeOrComplexObjects().size();i++){
//            if (mo.getHasNativeOrComplexObjects().get(i).getClass().toString().contains("NativeObject")){
//                NativeObject no = (NativeObject) mo.getHasNativeOrComplexObjects().get(i);
//                par.setParName(no.getObjectName());
//                par.setParType(no.getObjectType().split(" ")[0].trim().replaceAll("s:", "").replaceAll("xsd:", ""));
//                ////-//-System.out.println("Type NO ."+no.getObjectType());
//            }
//            else {
//                ComplexObject co = (ComplexObject) mo.getHasNativeOrComplexObjects().get(i);
//                par.setParName(co.getObjectName());
//                String a = co.getObjectType().trim();
//                if (a.startsWith("(")){
//                    a=a.split(" ")[1];
//                }
//                else{
//                    a=a.split(" ")[0];
//                }
//                
//                par.setParType(a);
//                
//                //par.setParType(co.getObjectType().trim());
//                //par.setParType(co.getObjectType().split(" ")[0].trim().replaceAll("s:", "").replaceAll("xsd:", ""));
//                ////-//-System.out.print("Type CO ."+co.getObjectType().trim()+".");
//                //String a =co.getObjectType().trim();
//                //String b = "(";
//                ////-//-System.out.println(a.);
//                
//                
//            }
//        }
//        
//        opOut.setOutputParameter(par);
//        
//        so.setInput(opIn);
//        so.setOutput(opOut);
//        /*
//        // display 
//        //-//-System.out.println("\tOperation: "+so.getName());
//        
//        //-//-System.out.println("\t\t Input Parameters =>");
//        for(int in=0;in<opIn.getParametersList().size();in++){
//            Parameter operationParameter = (Parameter)opIn.getParametersList().get(in);
//            //-//-System.out.println("\t\t               "+operationParameter.getParName()+":"+operationParameter.getParType());
//        }//-//-System.out.println();
//        
//        //-//-System.out.println("\t\t Output Parameters =>");
//        Parameter operationParameter = opOut.getOutputParameter();
//        if (operationParameter!=null){
//            //-//-System.out.println("\t\t               "+operationParameter.getParName()+":"+operationParameter.getParType());
//        }
//        else{
//            //-//-System.out.println();
//        }//-//-System.out.println();
//        */
//        operationsList.add(so);
        
        //- GIORGOS.

    }

        
    private static void parseServices(ParsedWSDLDefinition mitsosDefinition, Definition definition){
        // find service
            Map servicesMap = definition.getServices();           
            //-ta.append("\n\t\t### SERVICES ###\n");
            if(servicesMap!=null){
                if(servicesMap.size()==1){
                    //-ta.append(servicesMap.size()+" SERVICE found:");
                }else{
                    //-ta.append(servicesMap.size()+" SERVICES found:");
                }
                ////-System.out.println(servicesMap.size()+" SERVICES found\n");

                java.util.Collection services = servicesMap.values();
                java.util.Iterator servIter=services.iterator();

                 while (servIter.hasNext())
                {
                    // Operation operation=(Operation)servIter.next();
                     //-ta.append("\n\tSERVICE");
                     ServiceImpl serv=(ServiceImpl)servIter.next();
                    // ////-System.out.println(serv.toString());
                     ////-System.out.println("Service name: "+serv.getQName().getLocalPart());                      
                     //-ta.append("\n\tName: "+serv.getQName().getLocalPart());
                     Map servicePorts=serv.getPorts();
                     Collection ports=servicePorts.values();
                     Iterator portsIter=ports.iterator();
                     while(portsIter.hasNext()){
                         Port p=(Port)portsIter.next();
                         //-System.out.println("Port name: "+p.getName()+"   binding:"+p.getBinding());   
                         //-ta.append("\n\tPort: "+p.getName());
                         
                         Binding binding=p.getBinding();
                                              
                                                  
                         parseBindingOperations(mitsosDefinition, definition, binding);
                     }
                 }
            }else{
                //-ta.append("NULL SERVICES found!\n");
                return;
            }
    }
    
    private static void parseImports(ParsedWSDLDefinition mitsosDefinition,Definition definition){
        
        Map importsMap=definition.getImports();
            Collection importsCollection=importsMap.values();
            if(importsCollection!=null){
                ////-System.out.println("IMPORTS: "+importsCollection.size());
                //-ta.append("\nIMPORTS found: "+importsCollection.size());
                java.util.Iterator impsIter=importsCollection.iterator();                                   
                
                 while (impsIter.hasNext())
                {
                     try{
                        Import imp=(Import)impsIter.next();
                        //-ta.append("\tIMPORT URL: "+imp.getLocationURI());
                     }catch(Exception e){
                         try{
                             Vector impsVector= (Vector)impsIter.next();
                             if(impsVector!=null){
                                 for(int i=0;i<impsVector.size();i++){
                                    Import imp=(Import)impsVector.get(i);
                                    //-ta.append("\tIMPORT URL: "+imp.getLocationURI());
                                 }
                             }
                         }catch(Exception e1){
                             e1.printStackTrace();
                         }
                     }
                     ////-System.out.println("\tIMPORT URL: "+imp.getLocationURI());
                }        
                
                return;
            }
            else{
                //-ta.append("\nIMPORTS found: NULL");
                return;
            }
    }

    private static void parseType(Definition definition, QName inPartType, NativeObject no, ComplexObject co) {
        ////-System.out.println("####### PARSING TYPES ########");
        Types types=definition.getTypes();
                        
        //PSAXNW TO TYPE STA NATIVE ATTRIBUTES
        List nativeAttributeNamesList=types.getNativeAttributeNames();
        ////-System.out.println("Native Attribute Names:");
        if(nativeAttributeNamesList!=null){
            //-System.out.print(nativeAttributeNamesList.size()+"\n");
            Iterator iter1=nativeAttributeNamesList.iterator();
            while(iter1.hasNext()){
                try{
                    QName n1 = (QName)iter1.next();
                    ////-System.out.println(n1.toString());
                    //EDW PREPEI NA MPEI KWDIKAS GIA NA ELEGXEI AN EINAI TO EPITHYMITO (An exei to idio onoma me tin eisodo pou irthe)
                }catch(Exception e){                    
                    e.printStackTrace();
                }
            }
        }else{
            ////-System.out.println("\tNULL");
        }
        
        //PSAXNW TO TYPE STA EXTENSION ATTRIBUTES
        Map extensionAttributesMap=types.getExtensionAttributes();
        ////-System.out.println("Extension Attribute Names:");
        if(extensionAttributesMap!=null){
            Collection extAttsCol=extensionAttributesMap.values();
            if(extAttsCol!=null){
                //-System.out.print(extAttsCol.size()+"\n");
                Iterator iter2 = extAttsCol.iterator();
                while(iter2.hasNext()){
                    try{
                        QName n1 = (QName)iter2.next();
                        ////-System.out.println(n1.toString());
                        //EDW PREPEI NA MPEI KWDIKAS GIA NA ELEGXEI AN EINAI TO EPITHYMITO (An exei to idio onoma me tin eisodo pou irthe)
                    }catch(Exception e){                    
                        e.printStackTrace();
                    }
                }
            }else{
                ////-System.out.println("NULL");
            }
        }else{
            ////-System.out.println("NULL");
        }
               
        //Parse EXT ELEMENTS
        //schema...
        //PSAXNW TO TYPE STA EXTENSIBILITY ELEMENTS
        parseExtElements(definition, inPartType, no, co);
                
    }
    
    public static void parseExtElements(Definition definition, QName inPartType,  NativeObject no, ComplexObject co) {
        //EDW prepei na vrw an mesa sto ###schema### pou synithws orizetai yparxei to type inPartType            
        
        String inPartTypeName=inPartType.getLocalPart();
        if(inPartTypeName.startsWith("ArrayOf")){                                                    
            inPartTypeName=inPartTypeName.replaceFirst("ArrayOf", "");                                                                    
        }else if(inPartTypeName.endsWith("Array")){
            inPartTypeName=inPartTypeName.substring(0, inPartTypeName.length()-5);                 
        }
        else if(inPartTypeName.endsWith("[]")){                                                    
            inPartTypeName=inPartTypeName.replace("[]", "");                                                                    
        }      
        
        ////-System.out.println("\n\t\t\t\t\t Looking for TYPE: "+inPartType+"\n");
        List extElementsList=definition.getTypes().getExtensibilityElements();        
        //////-System.out.println("Extensibility Elements Names:");
        if(extElementsList!=null){
            //-System.out.print(extElementsList.size()+"\n");
            Iterator iter1=extElementsList.iterator();
            while(iter1.hasNext()){
                try{
                    com.ibm.wsdl.extensions.schema.SchemaImpl s1 = (com.ibm.wsdl.extensions.schema.SchemaImpl)iter1.next();
                    ////-System.out.println(s1.toString());                    
                                                                               
                    org.w3c.dom.Element e1 = s1.getElement();                    
                    //s1.get
                    NodeList children=e1.getChildNodes();
                    ////-System.out.println(children.getLength());
                    for(int i=0;i<children.getLength();i++){
                        Node n=children.item(i);
                        ////-System.out.println("### ####  MIIIIITS "+n.getNodeName()+" "+n.getNodeType()+" "+n.getNodeValue());
                        if(n.getNodeName()!=null&&n.getNodeName().contains(":import")){
                           
                            //PARSE IMPORTED XSD...
                            ////-System.out.println(n.getAttributes().getNamedItem("schemaLocation"));
                            if(n.getAttributes()!=null&&
                                    n.getAttributes().getNamedItem("schemaLocation")!=null&&
                                    n.getAttributes().getNamedItem("schemaLocation").getNodeValue()!=null){
                                ////-System.out.println("FOUND AN XSD IMPORT!!!");
                                ////-System.out.println(n.getAttributes().getNamedItem("schemaLocation").getNodeValue()+"\n");
                                parseImportedXSD(s1, inPartType,  null,null, no, co);
                                //parseImportedXSDusingCastor(n.getAttributes().getNamedItem("schemaLocation").getNodeValue(), inPartType, ta);
                            }
                            
                        }
                        
                       
                        /*
                        try{
                        ////-System.out.println("FFFFFFFFFFFFFF");
                        ////-System.out.println(n.getAttributes().getNamedItem("name"));
                        ////-System.out.println("FFFFFFFFFFFFFF");
                        }catch(Exception e){
                            
                        }
                         */ 
                        
                        if(n.getAttributes()!=null&&n.getAttributes().getNamedItem("name")!=null&&
                                n.getAttributes().getNamedItem("name").getNodeValue().equals(inPartTypeName)){
                            NodeList childrenOfChildOfSchema = n.getChildNodes();                        
                            if(childrenOfChildOfSchema!=null){
                                for(int j=0;j<childrenOfChildOfSchema.getLength();j++){
                                    Node n1=childrenOfChildOfSchema.item(j);
                                    //////-System.out.println("GAAAAAAAAAAAOOOOOOO "+n1.getNodeName());
                                    NodeList childrenOfTheDamned=n1.getChildNodes();
                                    if(childrenOfTheDamned!=null){
                                        for(int k=0;k<childrenOfTheDamned.getLength();k++){
                                            //////-System.out.println("\tGAAAAAAAAAAAAAAAAAAOOOOOOOOOOO "+childrenOfTheDamned.item(k).getNodeName());

                                            if(childrenOfTheDamned.item(k).getNodeName().contains("element")){
                                                NamedNodeMap attributesOfType=childrenOfTheDamned.item(k).getAttributes();                                            
                                                ////-System.out.println("WWW GAMWTOOOO...... TYPES...");
                                                String attName="";
                                                String attType="";
                                                String additionalInfo="";

                                                //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                                //WWW GAMWTOOOO WWWWWWWWWWWWWWWWWWWWWWWWWWWWWW!!!!!!!!!!!!!!!!!!!!!!!!
                                                //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
                                                //WWWWW
                                                //W
                                                //W                                            
                                                for(int k1=0;k1<attributesOfType.getLength();k1++){
                                                    Node att=attributesOfType.item(k1);                                                       
                                                    if(att.getNodeName().equalsIgnoreCase("name")){
                                                        ////-System.out.println("\tName: "+att.getNodeValue());  
                                                        attName=att.getNodeValue();
                                                    }else if(att.getNodeName().equalsIgnoreCase("type")){
                                                        ////-System.out.println("\tType: "+att.getNodeValue());
                                                        attType=att.getNodeValue();
                                                    }else{
                                                        ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                        additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                    }                                          
                                                }


                                                //-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

                                                //Find the Type of the attName...
                                                //ITERATIVE PROCESS.........
                                                if(attType.startsWith(nativeTypePrefix)){
                                                    //EINAI NATIVE TYPE
                                                     NativeObject no1=new NativeObject();
                                                     no1.setObjectName(new QName(attName));
                                                     no1.setObjectType(new QName(attType+" (NATIVE)"));
                                                     co.getHasNativeObjects().add(no1);

                                                }else if(attType.startsWith(targetNamespacePrefix)){
                                                    //PSAXNW GIA TO TYPE MESA STO definition                                        
                                                    
                                                    ComplexObject co1=new ComplexObject();
                                                    co1.setObjectName(new QName(attName));
                                                    co1.setAdditionalInfo(additionalInfo);
                                                    co1.setObjectType(new QName(attType+" (COMPLEX)"));

                                                    String type1=attType.substring(4, attType.length());
                                                    if(type1.startsWith("ArrayOf")){                                                    
                                                        type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }else if(type1.endsWith("Array")){
                                                        type1=type1.substring(0, type1.length()-5); 
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));

                                                    }
                                                    else if(attType.endsWith("[]")){                                                    
                                                        type1=type1.replace("[]", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }                                            
                                                    
                                                    parseTypeIterative(s1, type1,  0, false, co1);       
                                                    co.getHasComplexObjects().add(co1);
                                                                                                
                                                    
                                                }else{                                                    
                                                    //PSAXNW GIA TO TYPE MESA STO definition                                        
                                                    ComplexObject co1=new ComplexObject();
                                                    co1.setObjectName(new QName(attName));
                                                    co1.setAdditionalInfo(additionalInfo);
                                                    co1.setObjectType(new QName(attType+" (COMPLEX)"));
                                                    
                                                    String type1=attType.substring(attType.indexOf(":")+1, attType.length());
                                                    if(type1.startsWith("ArrayOf")){                                                    
                                                        type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }else if(type1.endsWith("Array")){
                                                        type1=type1.substring(0, type1.length()-5); 
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }
                                                    else if(attType.endsWith("[]")){                                                    
                                                        type1=type1.replace("[]", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }                                                
                                                    parseTypeIterative(s1, type1,  0, false, co1);  
                                                    co.getHasComplexObjects().add(co1);

                                                }                                            
                                            }else if(childrenOfTheDamned.item(k).getNodeName().contains("extension")){
                                                      parseExtendedType(s1,childrenOfTheDamned.item(k), 0, co);                                         
                                                      
                                                      NodeList childrenOfChildOfSchema1 = childrenOfTheDamned.item(k).getChildNodes();     
                                                      
                                                        if(childrenOfChildOfSchema1!=null){
                                                            for(int j1=0;j1<childrenOfChildOfSchema1.getLength();j1++){
                                                                Node n11=childrenOfChildOfSchema1.item(j1);
                                                                ////-System.out.println("GAAAAAAAAAAAOOOOOOO "+n1.getNodeName());
                                                                NodeList childrenOfTheDamned1=n11.getChildNodes();
                                                                if(childrenOfTheDamned1!=null){
                                                                    for(int k5=0;k5<childrenOfTheDamned1.getLength();k5++){
                                                                        ////-System.out.println("\tGAAAAAAAAAAAAAAAAAAOOOOOOOOOOO "+childrenOfTheDamned1.item(k5).getNodeName());

                                                                        if(childrenOfTheDamned1.item(k5).getNodeName().contains("element")){
                                                                            NamedNodeMap attributesOfType=childrenOfTheDamned1.item(k5).getAttributes();                                            
                                                                            ////-System.out.println("WWW GAMWTOOOO...... TYPES...");
                                                                            String attName="";
                                                                            String attType="";
                                                                            String additionalInfo="";

                                                                            //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                                                            //WWW GAMWTOOOO WWWWWWWWWWWWWWWWWWWWWWWWWWWWWW!!!!!!!!!!!!!!!!!!!!!!!!
                                                                            //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
                                                                            //WWWWW
                                                                            //W
                                                                            //W                                            
                                                                            for(int k51=0;k51<attributesOfType.getLength();k51++){
                                                                                Node att=attributesOfType.item(k51);                                                       
                                                                                if(att.getNodeName().equalsIgnoreCase("name")){
                                                                                    ////-System.out.println("\tName: "+att.getNodeValue());  
                                                                                    attName=att.getNodeValue();
                                                                                }else if(att.getNodeName().equalsIgnoreCase("type")){
                                                                                    ////-System.out.println("\tType: "+att.getNodeValue());
                                                                                    attType=att.getNodeValue();
                                                                                }else{
                                                                                    ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                                                    additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                                                }                                          
                                                                            }


                                                                            //-ta.append("\n\t\t\t\t-"+attName+" ["+attType+"]  "+additionalInfo);

                                                                            //Find the Type of the attName...
                                                                            //ITERATIVE PROCESS.........
                                                                            if(attType.startsWith(nativeTypePrefix)){
                                                                                //EINAI NATIVE TYPE
                                                                                 NativeObject no1=new NativeObject();
                                                                                 no1.setObjectName(new QName(attName));
                                                                                 no1.setObjectType(new QName(attType+" (NATIVE)"));
                                                                                 co.getHasNativeObjects().add(no1);

                                                                            }else if(attType.startsWith(targetNamespacePrefix)){
                                                                                //PSAXNW GIA TO TYPE MESA STO definition                                        
                                                                                ComplexObject co1=new ComplexObject();
                                                                                co1.setObjectName(new QName(attName));
                                                                                co1.setAdditionalInfo(additionalInfo);
                                                                                co1.setObjectType(new QName(attType+" (COMPLEX)"));

                                                                                String type1=attType.substring(4, attType.length());
                                                                                if(type1.startsWith("ArrayOf")){                                                    
                                                                                    type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }else if(type1.endsWith("Array")){
                                                                                    type1=type1.substring(0, type1.length()-5); 
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }
                                                                                else if(attType.endsWith("[]")){                                                    
                                                                                    type1=type1.replace("[]", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }                                                
                                                                                parseTypeIterative(s1, type1,  0, false,co1);
                                                                                co.getHasComplexObjects().add(co1);

                                                                            }else{                                                    
                                                                                //PSAXNW GIA TO TYPE MESA STO definition         
                                                                                
                                                                                ComplexObject co1=new ComplexObject();
                                                                                co1.setObjectName(new QName(attName));
                                                                                co1.setAdditionalInfo(additionalInfo);
                                                                                co1.setObjectType(new QName(attType+" (COMPLEX)"));

                                                                                String type1=attType.substring(attType.indexOf(":")+1, attType.length());
                                                                                if(type1.startsWith("ArrayOf")){                                                    
                                                                                    type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }else if(type1.endsWith("Array")){
                                                                                    type1=type1.substring(0, type1.length()-5); 
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }
                                                                                else if(attType.endsWith("[]")){                                                    
                                                                                    type1=type1.replace("[]", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }                                                
                                                                                parseTypeIterative(s1, type1,  0, false,co1);     
                                                                                co.getHasComplexObjects().add(co1);
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
                        
                        
                        
                        //////-System.out.println("########################");
                        //////-System.out.println("Name:   "+n.getNodeName());
                        //////-System.out.println("Type:   "+n.getNodeType());
                        //////-System.out.println("Value:   "+n.getNodeValue());
                        //////-System.out.println(n.getTextContent());
                        
                        
                        //EDW SYNITHWS YPARXEI NEKRA...
                        //DEN YPARXEI TIPOTA NA ME VOLEVEI EDW
                        NamedNodeMap nnm=n.getAttributes();                            
                        if(nnm!=null){
                           // ////-System.out.println("Atts length: "+nnm.getLength());
                            for(int j=0;j<nnm.getLength();j++){
                                Node n1=nnm.item(j);
                             //   ////-System.out.println("\t"+n1.getNodeName());
                             //   ////-System.out.println("\t"+n1.getNodeValue());
                                if(n1.getNodeName()!=null&&n1.getNodeName().equals("name")){
                                    if(n1.getNodeValue()!=null&&n1.getNodeValue().equals(inPartTypeName)){
                                        ////-System.out.println("\nWWWWWWWWWWWW GAMWTOOOOOOOOOOOOOOOOOOO WWWWWWWWWWWWWWWWWWWWW");
                                        ////-System.out.println("Vrethike to zitoumeno complexType!!!: "+n1.getNodeValue());
                                                                                
                                        NamedNodeMap nnm1=n1.getAttributes();
                                        if(nnm1!=null){
                                            ////-System.out.println("attributes: "+nnm1.getLength());
                                        }else{
                                            ////-System.out.println("attributes: NULL");
                                        }
                                        
                                                                                
                                        NodeList typesBelongingToTheComplexType=n1.getChildNodes();
                                        if(typesBelongingToTheComplexType!=null){
                                            for(int k=0;k<typesBelongingToTheComplexType.getLength();k++){                                                
                                                ////-System.out.println("\tWWW: "+typesBelongingToTheComplexType.item(k).getNodeName());
                                                ////-System.out.println("\tWWW: "+typesBelongingToTheComplexType.item(k).getNodeValue());
                                                ////-System.out.println("\tWWW: "+typesBelongingToTheComplexType.item(k).getNodeType());
                                                
                                                                                                
                                                NodeList typesBelongingToTheComplexType111=typesBelongingToTheComplexType.item(k).getChildNodes();
                                                ////-System.out.println("\tWWW1: "+typesBelongingToTheComplexType111.getLength());
                                                
                                                ////-System.out.println("\thasChildNodes:"+typesBelongingToTheComplexType.item(k).hasChildNodes());
                                                ////-System.out.println("\thasAttributes:"+typesBelongingToTheComplexType.item(k).hasAttributes());
                                                
                                                //KAPOU EDW EXW MPERDEMA...
                                                //PREPEI NA vrw mesa apo ayto to node ta types (tha einai elements - child nodes?)
                                                
                                                
                                                
                                                if(typesBelongingToTheComplexType.item(k).hasChildNodes()){//&&
                                                        //typesBelongingToTheComplexType.item(k).getNodeName().equals("#text")){
                                                    
                                                    //EDW einai ta types twn complexTypes
                                                    NodeList baseTypeNodes=typesBelongingToTheComplexType.item(k).getChildNodes();
                                                    if(baseTypeNodes!=null){
                                                        for(int k1=0;k1<baseTypeNodes.getLength();k1++){
                                                            Node baseN=baseTypeNodes.item(k1);
                                                            ////-System.out.println("\t\tbase Type:  "+baseN.getNodeName());
                                                        }                                                        
                                                    }
                                                }else{
                                                    String typeOfArray=typesBelongingToTheComplexType.item(k).getNodeValue();
                                                    String typeName=typeOfArray.replace("Array", "");
                                                    
                                                    ////-System.out.println("GAV GAV GAV "+typeName);
                                                    
                                                                                                        
                                                }
                                                
                                                if(typesBelongingToTheComplexType.item(k).hasAttributes()){
                                                    ////-System.out.println("HAS ATTRIBUTES!!!");
                                                    
                                                }
                                                
                                            }                                                
                                        }
                                        
                                                                                
                                         ////-System.out.println("WWWWWWWWWWWW GAMWTOOOOOOOOOOOOOOOOOOO WWWWWWWWWWWWWWWWWWWWW\n");
                                                ////-ta.append(str);
                                    }
                                }                                
                            }
                        }else{
                            //////-System.out.println("Atts length: NULL");
                        }
                        
                        //////-System.out.println("########################");
                    }                   
                    
                }catch(Exception e){                    
//                    System.out.println(".-.-"+ws.getServiceURL()+".-.-"+e.getMessage());
                    e.printStackTrace();
                }
            }
        }else{
            ////-System.out.println("\tNULL");
        }
        
        
    }
    
    private static void parseExtendedType(com.ibm.wsdl.extensions.schema.SchemaImpl s1, Node _extension_node,  int iterNumber, ComplexObject co){
               
        String baseType=_extension_node.getAttributes().getNamedItem("base").getNodeValue();
        ////-System.out.println("##EXTENDED BASE TYPE:\n"+baseType+"##EXTENDED TYPE:\n");
        baseType=baseType.substring(baseType.indexOf(":")+1,baseType.length());
        ////-System.out.println(baseType);
        
        QName inPartType=new QName(baseType);
        
        ////-System.out.println("\n\t\t\t\t\t Looking for extended TYPE: "+baseType+"\n");       
        
        //////-System.out.println("Extensibility Elements Names:");
                    
        try{
            //com.ibm.wsdl.extensions.schema.SchemaImpl s1 = (com.ibm.wsdl.extensions.schema.SchemaImpl)iter1.next();
            ////-System.out.println(s1.toString());                    

            org.w3c.dom.Element e1 = s1.getElement();                    
            //s1.get
            NodeList children=e1.getChildNodes();
            ////-System.out.println(children.getLength());
            for(int i=0;i<children.getLength();i++){
                Node n=children.item(i);
                ////-System.out.println("### ####  MIIIIITS "+n.getNodeName()+" "+n.getNodeType()+" "+n.getNodeValue());
                if(n.getNodeName()!=null&&n.getNodeName().contains(":import")){

                    //PARSE IMPORTED XSD...
                    ////-System.out.println(n.getAttributes().getNamedItem("schemaLocation"));
                    if(n.getAttributes()!=null&&
                            n.getAttributes().getNamedItem("schemaLocation")!=null&&
                            n.getAttributes().getNamedItem("schemaLocation").getNodeValue()!=null){
                        ////-System.out.println("FOUND AN XSD IMPORT!!!");
                        ////-System.out.println(n.getAttributes().getNamedItem("schemaLocation").getNodeValue()+"\n");
                        parseImportedXSD(s1, inPartType, null,null,null,null);
                        //parseImportedXSDusingCastor(n.getAttributes().getNamedItem("schemaLocation").getNodeValue(), inPartType, ta);
                    }

                }

               /* 
                try{
                ////-System.out.println("FFFFFFFFFFFFFF");
                ////-System.out.println(n.getAttributes().getNamedItem("name"));
                ////-System.out.println("FFFFFFFFFFFFFF");
                }catch(Exception e){

                }
                */ 

                if(n.getAttributes()!=null&&n.getAttributes().getNamedItem("name")!=null&&
                        n.getAttributes().getNamedItem("name").getNodeValue().equals(baseType)){
                    NodeList childrenOfChildOfSchema = n.getChildNodes();                        
                    if(childrenOfChildOfSchema!=null){
                        for(int j=0;j<childrenOfChildOfSchema.getLength();j++){
                            Node n1=childrenOfChildOfSchema.item(j);
                            ////-System.out.println("GAAAAAAAAAAAOOOOOOO "+n1.getNodeName());
                            NodeList childrenOfTheDamned=n1.getChildNodes();
                            if(childrenOfTheDamned!=null){
                                for(int k=0;k<childrenOfTheDamned.getLength();k++){
                                    ////-System.out.println("\tGAAAAAAAAAAAAAAAAAAOOOOOOOOOOO "+childrenOfTheDamned.item(k).getNodeName());

                                    if(childrenOfTheDamned.item(k).getNodeName().contains("element")){
                                        NamedNodeMap attributesOfType=childrenOfTheDamned.item(k).getAttributes();                                            
                                        ////-System.out.println("WWW GAMWTOOOO...... TYPES...");
                                        String attName="";
                                        String attType="";
                                        String additionalInfo="";

                                        //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                        //WWW GAMWTOOOO WWWWWWWWWWWWWWWWWWWWWWWWWWWWWW!!!!!!!!!!!!!!!!!!!!!!!!
                                        //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
                                        //WWWWW
                                        //W
                                        //W                                            
                                        for(int k1=0;k1<attributesOfType.getLength();k1++){
                                            Node att=attributesOfType.item(k1);                                                       
                                            if(att.getNodeName().equalsIgnoreCase("name")){
                                                ////-System.out.println("\tName: "+att.getNodeValue());  
                                                attName=att.getNodeValue();
                                            }else if(att.getNodeName().equalsIgnoreCase("type")){
                                                ////-System.out.println("\tType: "+att.getNodeValue());
                                                attType=att.getNodeValue();
                                            }else{
                                                ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                            }                                          
                                        }

                                        String prefix="\n\t\t\t\t";
                                            
                                        /*
                                        if(!fromLiteral){
                                                prefix+="\t"; 
                                            }*/
                                        
                                            if(iterNumber>0){
                                                //prefix+="\t\t";
                                                for(int ff1=0;ff1<iterNumber;ff1++){
                                                    prefix+="\t";                                                                                                        
                                                }                                                
                                            }  

                                        //-ta.append(prefix+"-"+attName+" ["+attType+"]  "+additionalInfo);

                                        //Find the Type of the attName...
                                        //ITERATIVE PROCESS.........
                                        if(attType.startsWith(nativeTypePrefix)){
                                            //EINAI NATIVE TYPE
                                            NativeObject no1=new NativeObject();
                                            no1.setObjectName(new QName(attName));
                                            no1.setObjectType(new QName(attType+" (NATIVE)"));
                                            co.getHasNativeObjects().add(no1);

                                        }else if(attType.startsWith(targetNamespacePrefix)){
                                            //PSAXNW GIA TO TYPE MESA STO definition                                        
                                            
                                            ComplexObject co1=new ComplexObject();
                                            co1.setObjectName(new QName(attName));
                                            co1.setAdditionalInfo(additionalInfo);
                                            co1.setObjectType(new QName(attType+" (COMPLEX)"));

                                            String type1=attType.substring(4, attType.length());
                                            if(type1.startsWith("ArrayOf")){                                                    
                                                type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                //-ta.append("  ("+type1+"[])");
                                                co1.setObjectType(new QName(type1+"[]"));
                                            }else if(type1.endsWith("Array")){
                                                type1=type1.substring(0, type1.length()-5); 
                                                //-ta.append("  ("+type1+"[])");
                                                co1.setObjectType(new QName(type1+"[]"));
                                            }
                                            else if(attType.endsWith("[]")){                                                    
                                                type1=type1.replace("[]", "");                                                    
                                                //-ta.append("  ("+type1+"[])");
                                                co1.setObjectType(new QName(type1+"[]"));
                                            }                                                
                                            parseTypeIterative(s1, type1, iterNumber, false, co1);
                                            co.getHasComplexObjects().add(co1);

                                        }else{                                                    
                                            //PSAXNW GIA TO TYPE MESA STO definition                                        

                                            ComplexObject co1=new ComplexObject();
                                            co1.setObjectName(new QName(attName));
                                            co1.setAdditionalInfo(additionalInfo);
                                            co1.setObjectType(new QName(attType+" (COMPLEX)"));
                                            
                                            String type1=attType.substring(attType.indexOf(":")+1, attType.length());
                                            if(type1.startsWith("ArrayOf")){                                                    
                                                type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                //-ta.append("  ("+type1+"[])");
                                                co1.setObjectType(new QName(type1+"[]"));
                                            }else if(type1.endsWith("Array")){
                                                type1=type1.substring(0, type1.length()-5); 
                                                //-ta.append("  ("+type1+"[])");
                                                co1.setObjectType(new QName(type1+"[]"));
                                            }
                                            else if(attType.endsWith("[]")){                                                    
                                                type1=type1.replace("[]", "");                                                    
                                                //-ta.append("  ("+type1+"[])");
                                                co1.setObjectType(new QName(type1+"[]"));
                                            }                                                
                                            parseTypeIterative(s1, type1,  iterNumber, false, co1);        
                                            co.getHasComplexObjects().add(co1);

                                        }                                            
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }       
        
    }
    
    
    
    public static Node parseTypeIterative(com.ibm.wsdl.extensions.schema.SchemaImpl s1, String typeName,  int iterNumber, boolean fromLiteral,
            ComplexObject co){
        
        if(co==null)co=new ComplexObject();
        
        Node result=null;                                                          
        //String inType1="name=\""+typeName+"\"";
        ////-System.out.println("############################## ITERATIVE: "+typeName);
        try{
            org.w3c.dom.Element e1 = s1.getElement();   
            //System.out.println("-.-."+ws.getServiceURL());
            NodeList children=e1.getChildNodes();
            for(int i=0;i<children.getLength();i++){
                Node n=children.item(i);
                //////-System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                //////-System.out.println(n.getNodeName());
                NamedNodeMap atts=n.getAttributes();    
                if(atts!=null){                                                   
                    
                    if(atts.getNamedItem("name")!=null&&atts.getNamedItem("name").getNodeValue()!=null&&
                            atts.getNamedItem("name").getNodeValue().equals(typeName)){
                        //VRETHIKE TO ZITOUMENO TYPE                        
                        NodeList childrenOfChildOfSchema = n.getChildNodes();
                        if(childrenOfChildOfSchema!=null){
                            for(int j=0;j<childrenOfChildOfSchema.getLength();j++){
                                Node n1=childrenOfChildOfSchema.item(j);
                                //////-System.out.println(n1.getNodeName());
                                NodeList childrenOfTheDamned=n1.getChildNodes();
                                if(childrenOfTheDamned!=null){
                                    for(int k=0;k<childrenOfTheDamned.getLength();k++){
                                        ////-System.out.println("\t"+childrenOfTheDamned.item(k).getNodeName());
                                        if(childrenOfTheDamned.item(k).getNodeName().contains("element")){
                                            NamedNodeMap attributesOfType=childrenOfTheDamned.item(k).getAttributes();                                            
                                            //////-System.out.println("WWW GAMWTOOOO...... TYPES... ITERATIVE ");// +typeName +" "+n.getNodeName() );
                                            String attName="";
                                            String attType="";
                                            String additionalInfo="";
                                            for(int k1=0;k1<attributesOfType.getLength();k1++){
                                                Node att=attributesOfType.item(k1);                                                       
                                                if(att.getNodeName().equalsIgnoreCase("name")){                                        
                                                    //////-System.out.println("\tName: "+att.getNodeValue());  
                                                    attName=att.getNodeValue();                                            
                                                }else if(att.getNodeName().equalsIgnoreCase("type")){
                                                    //////-System.out.println("\tType: "+att.getNodeValue());
                                                    attType=att.getNodeValue();
                                                }else{
                                                    //////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                    additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                }                                       
                                            }
                                            
                                            String prefix="\n\t\t\t\t";
                                            
                                            if(!fromLiteral){
                                                prefix+="\t";
                                            }
                                            
                                            if(iterNumber>0){
                                                for(int ff1=0;ff1<iterNumber;ff1++){
                                                    prefix+="\t";                                                                                                        
                                                }                                                
                                            }                                      
                                            
                                            
                                            //-ta.append(prefix+"-"+attName+" ["+attType+"]  "+additionalInfo);  
                                            
                                            if(attType.startsWith(nativeTypePrefix)){
                                                    //EINAI NATIVE TYPE
                                                 NativeObject no1=new NativeObject();
                                                 no1.setObjectName(new QName(attName));
                                                 no1.setObjectType(new QName(attType+" (NATIVE)"));
                                                 co.getHasNativeObjects().add(no1);

                                                }else if(attType.startsWith(targetNamespacePrefix)){
                                                    //PSAXNW GIA TO TYPE MESA STO definition                                        

                                                    ComplexObject co1=new ComplexObject();
                                                    co1.setObjectName(new QName(attName));
                                                    co1.setAdditionalInfo(additionalInfo);
                                                    co1.setObjectType(new QName(attType+" (COMPLEX)"));
                                                    
                                                    String type1=attType.substring(4, attType.length());
                                                    if(type1.startsWith("ArrayOf")){                                                    
                                                        type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                          co1.setObjectType(new QName(type1+"[]"));
                                                    }else if(type1.endsWith("Array")){
                                                        type1=type1.substring(0, type1.length()-5); 
                                                        //-ta.append("  ("+type1+"[])");
                                                          co1.setObjectType(new QName(type1+"[]"));
                                                    }
                                                    else if(attType.endsWith("[]")){                                                    
                                                        type1=type1.replace("[]", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                          co1.setObjectType(new QName(type1+"[]"));
                                                    }                              
                                                    
                                                    parseTypeIterative(s1, type1,  iterNumber+1, fromLiteral, co1);
                                                    co.getHasComplexObjects().add(co1);

                                                }else{                                                    
                                                    //PSAXNW GIA TO TYPE MESA STO definition                                        
                                                    
                                                    ComplexObject co1=new ComplexObject();
                                                    co1.setObjectName(new QName(attName));
                                                    co1.setAdditionalInfo(additionalInfo);
                                                    co1.setObjectType(new QName(attType+" (COMPLEX)"));
                                                
                                                    String type1=attType.substring(attType.indexOf(":")+1, attType.length());
                                                    if(type1.startsWith("ArrayOf")){                                                    
                                                        type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }else if(type1.endsWith("Array")){
                                                        type1=type1.substring(0, type1.length()-5); 
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }
                                                    else if(attType.endsWith("[]")){                                                    
                                                        type1=type1.replace("[]", "");                                                    
                                                        //-ta.append("  ("+type1+"[])");
                                                        co1.setObjectType(new QName(type1+"[]"));
                                                    }                                                
                                                    parseTypeIterative(s1, type1, iterNumber+1, fromLiteral, co1);     
                                                    co.getHasComplexObjects().add(co1);

                                                }                                           
                                            
                                        }else if(childrenOfTheDamned.item(k).getNodeName().contains("extension")){
                                                      parseExtendedType(s1,childrenOfTheDamned.item(k), iterNumber+1,co);                                          
                                                      
                                                      NodeList childrenOfChildOfSchema1 = childrenOfTheDamned.item(k).getChildNodes();     
                                                      
                                                        if(childrenOfChildOfSchema1!=null){
                                                            for(int j1=0;j1<childrenOfChildOfSchema1.getLength();j1++){
                                                                Node n11=childrenOfChildOfSchema1.item(j1);
                                                                ////-System.out.println("GAAAAAAAAAAAOOOOOOO "+n1.getNodeName());
                                                                NodeList childrenOfTheDamned1=n11.getChildNodes();
                                                                if(childrenOfTheDamned1!=null){
                                                                    for(int k5=0;k5<childrenOfTheDamned1.getLength();k5++){
                                                                        ////-System.out.println("\tGAAAAAAAAAAAAAAAAAAOOOOOOOOOOO "+childrenOfTheDamned1.item(k5).getNodeName());

                                                                        if(childrenOfTheDamned1.item(k5).getNodeName().contains("element")){
                                                                            NamedNodeMap attributesOfType=childrenOfTheDamned1.item(k5).getAttributes();                                            
                                                                            ////-System.out.println("WWW GAMWTOOOO...... TYPES...");
                                                                            String attName="";
                                                                            String attType="";
                                                                            String additionalInfo="";

                                                                            //EDW EINAI OOOOLH H MAGKIA MOU ME TA TYPES!!!!!!!!!!!!!!!
                                                                            //WWW GAMWTOOOO WWWWWWWWWWWWWWWWWWWWWWWWWWWWWW!!!!!!!!!!!!!!!!!!!!!!!!
                                                                            //WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
                                                                            //WWWWW
                                                                            //W
                                                                            //W                                            
                                                                            for(int k51=0;k51<attributesOfType.getLength();k51++){
                                                                                Node att=attributesOfType.item(k51);                                                       
                                                                                if(att.getNodeName().equalsIgnoreCase("name")){
                                                                                    ////-System.out.println("\tName: "+att.getNodeValue());  
                                                                                    attName=att.getNodeValue();
                                                                                }else if(att.getNodeName().equalsIgnoreCase("type")){
                                                                                    ////-System.out.println("\tType: "+att.getNodeValue());
                                                                                    attType=att.getNodeValue();
                                                                                }else{
                                                                                    ////-System.out.println("\t"+att.getNodeName()+": "+att.getNodeValue());
                                                                                    additionalInfo+=att.getNodeName()+":"+att.getNodeValue()+"   ";
                                                                                }                                          
                                                                            }
                                                                            
                                                                            String prefix="\n\t\t\t\t";
                                            
                                                                            if(!fromLiteral){
                                                                                prefix+="\t";
                                                                            }
                                            
                                                                            if(iterNumber>0){
                                                                                for(int ff1=0;ff1<iterNumber;ff1++){
                                                                                    prefix+="\t";                                                                                                        
                                                                                }                                                
                                                                            }   
                                                                            
                                                                            //-ta.append(prefix+"-"+attName+" ["+attType+"]  "+additionalInfo);

                                                                            //Find the Type of the attName...
                                                                            //ITERATIVE PROCESS.........
                                                                            if(attType.startsWith(nativeTypePrefix)){
                                                                                //EINAI NATIVE TYPE
                                                                                NativeObject no1=new NativeObject();
                                                                                 no1.setObjectName(new QName(attName));
                                                                                 no1.setObjectType(new QName(attType+" (NATIVE)"));
                                                                                 co.getHasNativeObjects().add(no1);

                                                                            }else if(attType.startsWith(targetNamespacePrefix)){
                                                                                //PSAXNW GIA TO TYPE MESA STO definition                                        
                                                                                
                                                                                ComplexObject co1=new ComplexObject();
                                                                                co1.setObjectName(new QName(attName));
                                                                                co1.setAdditionalInfo(additionalInfo);
                                                                                co1.setObjectType(new QName(attType+" (COMPLEX)"));
                                                

                                                                                String type1=attType.substring(4, attType.length());
                                                                                if(type1.startsWith("ArrayOf")){                                                    
                                                                                    type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }else if(type1.endsWith("Array")){
                                                                                    type1=type1.substring(0, type1.length()-5); 
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }
                                                                                else if(attType.endsWith("[]")){                                                    
                                                                                    type1=type1.replace("[]", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                    co1.setObjectType(new QName(type1+"[]"));
                                                                                }                                                
                                                                                parseTypeIterative(s1, type1,  iterNumber+1, false,co1);
                                                                                co.getHasComplexObjects().add(co1);

                                                                            }else{                                                    
                                                                                //PSAXNW GIA TO TYPE MESA STO definition                                        

                                                                                ComplexObject co1=new ComplexObject();
                                                                                co1.setObjectName(new QName(attName));
                                                                                co1.setAdditionalInfo(additionalInfo);
                                                                                co1.setObjectType(new QName(attType+" (COMPLEX)"));
                                                                                
                                                                                String type1=attType.substring(attType.indexOf(":")+1, attType.length());
                                                                                if(type1.startsWith("ArrayOf")){                                                    
                                                                                    type1=type1.replaceFirst("ArrayOf", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                     co1.setObjectType(new QName(type1+"[]"));
                                                                                }else if(type1.endsWith("Array")){
                                                                                    type1=type1.substring(0, type1.length()-5); 
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                     co1.setObjectType(new QName(type1+"[]"));
                                                                                }
                                                                                else if(attType.endsWith("[]")){                                                    
                                                                                    type1=type1.replace("[]", "");                                                    
                                                                                    //-ta.append("  ("+type1+"[])");
                                                                                     co1.setObjectType(new QName(type1+"[]"));
                                                                                }                                                
                                                                                parseTypeIterative(s1, type1,  iterNumber+1, false, co1);   
                                                                                co.getHasComplexObjects().add(co1);
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
        }catch(Exception e){
//            System.out.println(".-.-"+ws.getServiceURL());
            e.printStackTrace();
            return null;
        }        
        
        return result;
    }      


}
