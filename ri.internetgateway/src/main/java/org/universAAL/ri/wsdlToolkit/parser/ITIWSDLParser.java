/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.universAAL.ri.wsdlToolkit.parser;


import com.ibm.wsdl.ServiceImpl;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;


import org.universAAL.ri.wsdlToolkit.axis2Parser.Axis2ParserWrapper;
import org.universAAL.ri.wsdlToolkit.ioApi.AlignmentInformation;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationOutput;
import org.w3c.dom.Element;


public class ITIWSDLParser {

    //private static boolean changeUserDirDuringParsing=false;
    //public static HashMap clonedTypes;
    public static ParsedWSDLDefinition parseWSDLwithAxis(String wsdlURL, boolean Axis2Enabled, boolean Axis1Enabled) {


        try {







            //String styleAndUse="";//getWSDLStyleAndUse(wsdlURL);
            String styleAndUse = getWSDLStyleAndUse(wsdlURL);
            String style = styleAndUse.substring(0, styleAndUse.indexOf("#"));
            String use = styleAndUse.substring(styleAndUse.indexOf("#") + 1);
            if (style != null && use != null) {
                style = style.toUpperCase();
                use = use.toUpperCase();
            }

            String previousUserDir = System.getProperty("user.dir", ".");
            try {
                if (wsdlURL != null && wsdlURL.startsWith("file:")) {
                    String localFileFolder = getFolderOfLocalWSDLfile(wsdlURL);
                    System.setProperty("user.dir", localFileFolder);
                    System.out.println(System.getProperty("user.dir", "."));
                }
            } catch (Exception e) {
            }
 
            if (!Axis1Enabled) {
               
                //if(true){//gia na kalei mono me Axis2 kai MitsosParser
                if (styleAndUse.equals("rpc#encoded")) {
                    //Use ONLY MITSOS Parser (and/or Axis1)
                    System.out.println("RPC/ENCODED!!!!!!!");
                    ParsedWSDLDefinition parsedDef = MitsosParser.parseWSDL(wsdlURL);
                    parsedDef.setBindingStyle("RPC");
                    parsedDef.setOperationsUse("ENCODED");
                    fixTheParserPaths(parsedDef);


                    parsedDef.setDocumentation(getDocumentation(wsdlURL));

                    try {
                        if (wsdlURL != null && wsdlURL.startsWith("file:")) {
                            System.setProperty("user.dir", previousUserDir);
                            System.out.println(System.getProperty("user.dir", "."));
                        }
                    } catch (Exception e) {
                    }
                    
                    return parsedDef;

                } else {
                    //Use Axis2, if it fails (use Axis1, if it fails) use MITSOS
                    System.out.println("NOT RPC ENCODED!!!!!!!");
                    if (Axis2Enabled) {
                        ParsedWSDLDefinition parsedDef = Axis2ParserWrapper.parseWSDLwithAxis2(wsdlURL);
                        if (parsedDef == null) {
                            //try to call MitsosParser
                            System.out.println("\n\nTrying to Parse WSDL with MitsosParser...");
                            parsedDef = MitsosParser.parseWSDL(wsdlURL);
                        }

                        parsedDef.setBindingStyle(style);
                        parsedDef.setOperationsUse(use);
                        fixTheParserPaths(parsedDef);


                        parsedDef.setDocumentation(getDocumentation(wsdlURL));

                        try {
                            if (wsdlURL != null && wsdlURL.startsWith("file:")) {
                                System.setProperty("user.dir", previousUserDir);
                                System.out.println(System.getProperty("user.dir", "."));
                            }
                        } catch (Exception e) {
                        }
                       
                        return parsedDef;

                    } else {
                        System.out.println("\n\nTrying to Parse WSDL with MitsosParser...");
                        ParsedWSDLDefinition parsedDef = MitsosParser.parseWSDL(wsdlURL);

                        parsedDef.setBindingStyle(style);
                        parsedDef.setOperationsUse(use);
                        fixTheParserPaths(parsedDef);


                        parsedDef.setDocumentation(getDocumentation(wsdlURL));

                        try {
                            if (wsdlURL != null && wsdlURL.startsWith("file:")) {
                                System.setProperty("user.dir", previousUserDir);
                                System.out.println(System.getProperty("user.dir", "."));
                            }
                        } catch (Exception e) {
                        }
                       
                        return parsedDef;
                    }
                }
            } else {
                //ParsedWSDLDefinition parsedDef=Axis1ParserWrapper.parseWSDLwithAxis1(wsdlURL);
                //return parsedDef;

                if (styleAndUse.equals("rpc#encoded")) {
                  

                } else {
                    //Use Axis2, if it fails (use Axis1, if it fails) use MITSOS
                    System.out.println("NOT RPC ENCODED!!!!!!!");
                    if (Axis2Enabled) {
                        ParsedWSDLDefinition parsedDef = Axis2ParserWrapper.parseWSDLwithAxis2(wsdlURL);

                        if (parsedDef == null) {
                           
                        } else {
                            parsedDef.setParsingTool("Axis2");
                            parsedDef.setBindingStyle(style);
                            parsedDef.setOperationsUse(use);
                        }

                        fixTheParserPaths(parsedDef);
                        parsedDef.setDocumentation(getDocumentation(wsdlURL));

                        try {
                            if (wsdlURL != null && wsdlURL.startsWith("file:")) {
                                System.setProperty("user.dir", previousUserDir);
                                System.out.println(System.getProperty("user.dir", "."));
                            }
                        } catch (Exception e) {
                        }
                       
                        return parsedDef;

                    } else {
                        System.out.println("\n\nTrying to Parse WSDL with Axis 1...");
                        ParsedWSDLDefinition parsedDef = MitsosParser.parseWSDL(wsdlURL);

                        parsedDef.setBindingStyle(style);
                        parsedDef.setOperationsUse(use);
                        fixTheParserPaths(parsedDef);
                        parsedDef.setDocumentation(getDocumentation(wsdlURL));
                        try {
                            if (wsdlURL != null && wsdlURL.startsWith("file:")) {
                                System.setProperty("user.dir", previousUserDir);
                                System.out.println(System.getProperty("user.dir", "."));
                            }
                        } catch (Exception e) {
                        }
                        
                        return parsedDef;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
           
            return null;
        }
		return null;



    }



    private static String getFolderOfLocalWSDLfile(String fileURL) {
        if (fileURL.contains("\\")) {
            int i1 = fileURL.lastIndexOf("\\");
            String res = fileURL.substring(0, i1);
            res = res.replace("file:", "");
            return res;
        } else {
            return System.getProperty("user.dir", ".");
        }


    }

    public static String getWSDLStyleAndUse(String wsdlURL) {
        String bindingStyle = "";
        String operationUse = "";

        try {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

            wsdlReader.setFeature("javax.wsdl.verbose", false);
            wsdlReader.setFeature("javax.wsdl.importDocuments", true);


            Definition definition = wsdlReader.readWSDL(wsdlURL);

            Map servicesMap = definition.getServices();
            if (servicesMap != null) {

                java.util.Collection services = servicesMap.values();
                java.util.Iterator servIter = services.iterator();

                while (servIter.hasNext()) {
                    ServiceImpl serv = (ServiceImpl) servIter.next();
                    Map servicePorts = serv.getPorts();
                    Collection ports = servicePorts.values();
                    Iterator portsIter = ports.iterator();
                    bindingStyle = null;
                    while (portsIter.hasNext()) {
                        Port p = (Port) portsIter.next();
                        Binding binding = p.getBinding();

                        //if(bindingStyle==null)
                        List l1 = binding.getExtensibilityElements();
                        for (int i = 0; i < l1.size(); i++) {
                            try {
                                com.ibm.wsdl.extensions.soap.SOAPBindingImpl sbimpl = (com.ibm.wsdl.extensions.soap.SOAPBindingImpl) l1.get(i);
                                bindingStyle = sbimpl.getStyle();
                            } catch (Exception e) {
                                try {
                                    com.ibm.wsdl.extensions.soap12.SOAP12BindingImpl sbimpl = (com.ibm.wsdl.extensions.soap12.SOAP12BindingImpl) l1.get(i);
                                    bindingStyle = sbimpl.getStyle();
                                } catch (Exception e1) {
                                    //e1.printStackTrace();
                                }

                            }

                        }

                        List opers = binding.getBindingOperations();
                        if (opers != null) {
                            for (int i = 0; i < opers.size(); i++) {
                                BindingOperation bindingOper = (BindingOperation) opers.get(i);

                                if (bindingStyle == null) {
                                    List l11 = bindingOper.getExtensibilityElements();
                                    for (int i11 = 0; i11 < l11.size(); i11++) {
                                        try {
                                            com.ibm.wsdl.extensions.soap.SOAPOperationImpl soapOperImpl =
                                                    (com.ibm.wsdl.extensions.soap.SOAPOperationImpl) l11.get(i11);
                                            bindingStyle = soapOperImpl.getStyle();
                                        } catch (Exception e) {
                                            try {
                                                com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl soapOperImpl =
                                                        (com.ibm.wsdl.extensions.soap12.SOAP12OperationImpl) l11.get(i11);
                                                bindingStyle = soapOperImpl.getStyle();
                                            } catch (Exception e1) {
                                                //e1.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                if (operationUse == null || operationUse.equals("") || operationUse.equals("unknown")) {
                                    operationUse = "unknown";
                                }

                                List extensibilityElementsList = bindingOper.getBindingInput().getExtensibilityElements();
                                if (extensibilityElementsList != null) {
                                    for (int f1 = 0; f1 < extensibilityElementsList.size(); f1++) {
                                        javax.wsdl.extensions.ExtensibilityElement extel = (javax.wsdl.extensions.ExtensibilityElement) extensibilityElementsList.get(f1);
                                        try {
                                            com.ibm.wsdl.extensions.soap.SOAPBodyImpl soapBodyImpl = (com.ibm.wsdl.extensions.soap.SOAPBodyImpl) extensibilityElementsList.get(f1);

                                            if (soapBodyImpl.getUse().equals("encoded")) {
                                                operationUse = "encoded";
                                            } else if (soapBodyImpl.getUse().equals("literal")) {
                                                operationUse = "literal";
                                            }
                                        } catch (Exception e) {
                                            try {
                                                com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl soapBodyImpl = (com.ibm.wsdl.extensions.soap12.SOAP12BodyImpl) extensibilityElementsList.get(f1);
                                                if (soapBodyImpl.getUse().equals("encoded")) {
                                                    operationUse = "encoded";
                                                } else if (soapBodyImpl.getUse().equals("literal")) {
                                                    operationUse = "literal";
                                                }
                                            } catch (Exception e1) {
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

        }


        return bindingStyle + "#" + operationUse;

    }

    //kgiannou
    public static String getDocumentation(String wsdlURL) {
        String res = new String();
//        try {
//            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
//            WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
//
//            wsdlReader.setFeature("javax.wsdl.verbose", false);
//            wsdlReader.setFeature("javax.wsdl.importDocuments", true);
//
//
//            Definition definition = wsdlReader.readWSDL(wsdlURL);
//            Element element = definition.getDocumentationElement();
//            try {
//                String documentation = element.getTextContent();
//                if (documentation != null) {
//                    //efi
//                    documentation = removeHTMLTags(documentation);
//                    res = documentation;
//                }
//            } catch (Exception ex) {
//                System.out.println("No Documentation Found");
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        return res;
    }

    private static String removeHTMLTags(String documentation) {
        //remove html tags from string
        String documentation_without_HTML = documentation.replaceAll("\\<.*?>", "");

        //remove special characters
        documentation_without_HTML = documentation_without_HTML.replaceAll("[^a-zA-Z 0-9]+", "");


        return documentation_without_HTML;
    }

    public static void cloneAllParsedComplexandNativeObjects(ParsedWSDLDefinition theDefinition) {
        if (theDefinition == null || theDefinition.getWsdlOperations() == null) {
            return;
        }

        Iterator operIter = theDefinition.getWsdlOperations().iterator();
        while (operIter.hasNext()) {
            WSOperation oper = (WSOperation) operIter.next();
            if (oper == null) {
                continue;
            }

            WSOperationInput input = oper.getHasInput();
            if (input != null && input.getHasNativeOrComplexObjects() != null) {
                for (int i = 0; i < input.getHasNativeOrComplexObjects().size(); i++) {
                    Object obj = input.getHasNativeOrComplexObjects().get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (obj.getClass().getName().contains("NativeObject")) {
                        NativeObject no = (NativeObject) obj;
                        NativeObject newNo = no.cloneTheNO();
                        //newNo.setObjectName(new QName("WX"));
                        input.getHasNativeOrComplexObjects().set(i, newNo);
                        System.out.println();

                    } else if (obj.getClass().getName().contains("ComplexObject")) {
                        ComplexObject co = (ComplexObject) obj;
                        //clonedTypes=new HashMap();
                        //clonedTypes.add(co.getObjectType());
                        ComplexObject newCo = co.cloneTheCO();
                        //newCo.setObjectName(new QName("WX"));
                        input.getHasNativeOrComplexObjects().set(i, newCo);

                        //co.setObjectName(new QName("XAXA!"));
                        //System.out.println();
                    }
                }
            }

            if (input != null && input.getHasSoapHeaders() != null) {
                for (int i = 0; i < input.getHasSoapHeaders().size(); i++) {
                    Object obj = input.getHasSoapHeaders().get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (obj.getClass().getName().contains("NativeObject")) {
                        NativeObject no = (NativeObject) obj;
                        NativeObject newNo = no.cloneTheNO();
                        //newNo.setObjectName(new QName("WX"));
                        input.getHasSoapHeaders().set(i, newNo);
                        System.out.println();

                    } else if (obj.getClass().getName().contains("ComplexObject")) {
                        ComplexObject co = (ComplexObject) obj;
                        //clonedTypes=new HashMap();
                        //clonedTypes.add(co.getObjectType());
                        ComplexObject newCo = co.cloneTheCO();
                        //newNo.setObjectName(new QName("WX"));
                        input.getHasSoapHeaders().set(i, newCo);
                        System.out.println();
                    }
                }
            }


            WSOperationOutput output = oper.getHasOutput();

            if (output != null && output.getHasNativeOrComplexObjects() != null) {
                for (int i = 0; i < output.getHasNativeOrComplexObjects().size(); i++) {
                    Object obj = output.getHasNativeOrComplexObjects().get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (obj.getClass().getName().contains("NativeObject")) {
                        NativeObject no = (NativeObject) obj;
                        NativeObject newNo = no.cloneTheNO();
                        //newNo.setObjectName(new QName("WX"));
                        output.getHasNativeOrComplexObjects().set(i, newNo);
                        System.out.println();

                    } else if (obj.getClass().getName().contains("ComplexObject")) {
                        ComplexObject co = (ComplexObject) obj;
                        //clonedTypes=new HashMap();
                        //clonedTypes.add(co.getObjectType());
                        //System.out.println("\n\n#############   ROOT CO   ###############");
                        //System.out.println("\t\t\t"+co.getObjectName().getLocalPart()+"\t"+co.getObjectType().getLocalPart());
                        //System.out.println("#############   ROOT CO   ###############\n");
                        ComplexObject newCo = co.cloneTheCO();
                        //newCo.setObjectName(new QName("WX"));
                        output.getHasNativeOrComplexObjects().set(i, newCo);

                        //co.setObjectName(new QName("XAXA!"));
                        //System.out.println();
                    }
                }
            }

            if (output != null && output.getHasSoapHeaders() != null) {
                for (int i = 0; i < output.getHasSoapHeaders().size(); i++) {
                    Object obj = output.getHasSoapHeaders().get(i);
                    if (obj == null) {
                        continue;
                    }
                    if (obj.getClass().getName().contains("NativeObject")) {
                        NativeObject no = (NativeObject) obj;
                        NativeObject newNo = no.cloneTheNO();
                        //newNo.setObjectName(new QName("WX"));
                        output.getHasSoapHeaders().set(i, newNo);
                        System.out.println();

                    } else if (obj.getClass().getName().contains("ComplexObject")) {
                        ComplexObject co = (ComplexObject) obj;
                        //clonedTypes=new HashMap();
                        //clonedTypes.add(co.getObjectType());
                        ComplexObject newCo = co.cloneTheCO();
                        //newNo.setObjectName(new QName("WX"));
                        output.getHasSoapHeaders().set(i, newCo);
                        System.out.println();
                    }
                }
            }
        }

    }

    public static boolean checkIfCOhasAlreadyBeenCloned(QName coType) {
        /*
        if(clonedTypes==null)return false;
        Object obj=clonedTypes.get(coType);
        if(obj!=null){
        if(((Integer)obj)>20){
        return true;
        }else{
        return false;
        }
        }else{
        return false;
        }*/
        /*
        for(int i=0;i<clonedTypes.size();i++){
        if(((QName)clonedTypes.get(i)).equals(coType)){
        return true;
        }
        }*/
        return false;
    }

    public static void fixTheParserPaths(ParsedWSDLDefinition theDefinition) {
        if (theDefinition == null || theDefinition.getWsdlOperations() == null) {
            return;
        }
       cloneAllParsedComplexandNativeObjects(theDefinition);



        Iterator operIter = theDefinition.getWsdlOperations().iterator();
        while (operIter.hasNext()) {

            Vector inQNamesSoFar = new Vector();
            Vector outQNamesSoFar = new Vector();

            inQNamesSoFar.add(new QName(theDefinition.getServiceURL(), theDefinition.getWsdlURL().getFile()));
            outQNamesSoFar.add(new QName(theDefinition.getServiceURL(), theDefinition.getWsdlURL().getPath()));

//location  [C:/Documents and Settings/dgiakoum/Desktop/Accessible WS Assessment DIST/DIST/Example WSDLs/Service1.wsdl::getMap::INPUTS::location]
            WSOperation oper = (WSOperation) operIter.next();
            inQNamesSoFar.add(new QName(theDefinition.getServiceURL(), oper.getOperationName()));

            AlignmentInformation operAI = new AlignmentInformation();
            QName[] operQNarray = new QName[inQNamesSoFar.size() + 1];
            for (int i = 0; i < inQNamesSoFar.size(); i++) {
                operQNarray[i] = (QName) inQNamesSoFar.get(i);
            }
            operQNarray[operQNarray.length - 1] = new QName(oper.getOperationName());
            operAI.setHasParserPath(operQNarray);
            oper.setHasAlignmentInformation(operAI);


            inQNamesSoFar.add(new QName("INPUTS"));
            WSOperationInput input = oper.getHasInput();

            Iterator inObjsIter = input.getHasNativeOrComplexObjects().iterator();
            while (inObjsIter.hasNext()) {
                Object obj = inObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    no.setHasParent(oper);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[inQNamesSoFar.size() + 1];
                    for (int i = 0; i < inQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) inQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = no.getObjectName();
                    ai.setHasParserPath(qNarray);
                    no.setHasAlignmentInformation(ai);

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    co.setHasParent(oper);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[inQNamesSoFar.size() + 1];
                    for (int i = 0; i < inQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) inQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = co.getObjectName();
                    ai.setHasParserPath(qNarray);
                    co.setHasAlignmentInformation(ai);

                    Vector newQNamesVector = (Vector) inQNamesSoFar.clone();
                    newQNamesVector.add(qNarray[qNarray.length - 1]);

                    fixComplexObjectPathIterative(co, newQNamesVector, 0);
                }
            }

            Iterator inHeaderObjsIter = input.getHasSoapHeaders().iterator();
            while (inHeaderObjsIter.hasNext()) {
                Object obj = inHeaderObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    no.setHasParent(oper);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[inQNamesSoFar.size() + 1];
                    for (int i = 0; i < inQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) inQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = no.getObjectName();
                    ai.setHasParserPath(qNarray);
                    no.setHasAlignmentInformation(ai);

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    co.setHasParent(oper);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[inQNamesSoFar.size() + 1];
                    for (int i = 0; i < inQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) inQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = co.getObjectName();
                    ai.setHasParserPath(qNarray);
                    co.setHasAlignmentInformation(ai);

                    Vector newQNamesVector = (Vector) inQNamesSoFar.clone();
                    newQNamesVector.add(qNarray[qNarray.length - 1]);

                    fixComplexObjectPathIterative(co, newQNamesVector, 0);
                }
            }


            WSOperationOutput output = oper.getHasOutput();

            outQNamesSoFar.add(new QName(theDefinition.getServiceURL(), oper.getOperationName()));
            outQNamesSoFar.add(new QName("OUTPUTS"));

            Iterator outObjsIter = output.getHasNativeOrComplexObjects().iterator();
            while (outObjsIter.hasNext()) {
                Object obj = outObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    no.setHasParent(oper);
                    no.setIsInput(false);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[outQNamesSoFar.size() + 1];
                    for (int i = 0; i < outQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) outQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = no.getObjectName();
                    ai.setHasParserPath(qNarray);
                    no.setHasAlignmentInformation(ai);

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    co.setHasParent(oper);
                    co.setIsInput(false);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[outQNamesSoFar.size() + 1];
                    for (int i = 0; i < outQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) outQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = co.getObjectName();
                    ai.setHasParserPath(qNarray);
                    co.setHasAlignmentInformation(ai);

                    Vector newQNamesVector = (Vector) outQNamesSoFar.clone();
                    newQNamesVector.add(qNarray[qNarray.length - 1]);

                    fixComplexObjectPathIterative(co, newQNamesVector, 0);
                }
            }

            Iterator outHeaderObjsIter = output.getHasSoapHeaders().iterator();
            while (outHeaderObjsIter.hasNext()) {
                Object obj = outHeaderObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    no.setHasParent(oper);
                    no.setIsInput(false);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[outQNamesSoFar.size() + 1];
                    for (int i = 0; i < outQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) outQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = no.getObjectName();
                    ai.setHasParserPath(qNarray);
                    no.setHasAlignmentInformation(ai);

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    co.setHasParent(oper);
                    co.setIsInput(false);

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[outQNamesSoFar.size() + 1];
                    for (int i = 0; i < outQNamesSoFar.size(); i++) {
                        qNarray[i] = (QName) outQNamesSoFar.get(i);
                    }
                    qNarray[qNarray.length - 1] = co.getObjectName();
                    ai.setHasParserPath(qNarray);
                    co.setHasAlignmentInformation(ai);

                    Vector newQNamesVector = (Vector) outQNamesSoFar.clone();
                    newQNamesVector.add(qNarray[qNarray.length - 1]);

                    fixComplexObjectPathIterative(co, newQNamesVector, 0);
                }
            }
        }

        System.out.println();

    }

    /*
     * Returns null if object is not found
     * */
    public static Object getTheObjectWithTheSpecificPath(ParsedWSDLDefinition theDefinition, QName[] parserPathOfObject) {

        Iterator operIter = theDefinition.getWsdlOperations().iterator();
        while (operIter.hasNext()) {

            WSOperation oper = (WSOperation) operIter.next();

            WSOperationInput input = oper.getHasInput();
            Iterator inObjsIter = input.getHasNativeOrComplexObjects().iterator();
            while (inObjsIter.hasNext()) {
                Object obj = inObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    if (Arrays.deepEquals(no.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return no;
                    }


                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    if (Arrays.deepEquals(co.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return co;
                    } else {
                        Object retObj = findParserPathAmongContainedOfComplexObject_ITERATIVE(co, parserPathOfObject);
                        if (retObj != null) {
                            return retObj;
                        }
                    }
                }
            }

            Iterator inHeaderObjsIter = input.getHasSoapHeaders().iterator();
            while (inHeaderObjsIter.hasNext()) {
                Object obj = inHeaderObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    if (Arrays.deepEquals(no.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return no;
                    }

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    if (Arrays.deepEquals(co.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return co;
                    } else {
                        Object retObj = findParserPathAmongContainedOfComplexObject_ITERATIVE(co, parserPathOfObject);
                        if (retObj != null) {
                            return retObj;
                        }
                    }
                }
            }


            WSOperationOutput output = oper.getHasOutput();
            Iterator outObjsIter = output.getHasNativeOrComplexObjects().iterator();
            while (outObjsIter.hasNext()) {
                Object obj = outObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    if (Arrays.deepEquals(no.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return no;
                    }

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    if (Arrays.deepEquals(co.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return co;
                    } else {
                        Object retObj = findParserPathAmongContainedOfComplexObject_ITERATIVE(co, parserPathOfObject);
                        if (retObj != null) {
                            return retObj;
                        }
                    }
                }
            }

            Iterator outHeaderObjsIter = output.getHasSoapHeaders().iterator();
            while (outHeaderObjsIter.hasNext()) {
                Object obj = outHeaderObjsIter.next();
                if (obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no = (NativeObject) obj;
                    if (Arrays.deepEquals(no.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return no;
                    }

                } else if (obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co = (ComplexObject) obj;
                    if (Arrays.deepEquals(co.getHasAlignmentInformation().getHasParserPath(), parserPathOfObject)) {
                        return co;
                    } else {
                        Object retObj = findParserPathAmongContainedOfComplexObject_ITERATIVE(co, parserPathOfObject);
                        if (retObj != null) {
                            return retObj;
                        }
                    }
                }
            }

        }

        return null;
    }

    private static Object findParserPathAmongContainedOfComplexObject_ITERATIVE(ComplexObject co, QName[] parserPathToFind) {
        if (co == null) {
            return null;
        }

        Iterator iter1 = co.getHasNativeObjects().iterator();
        while (iter1.hasNext()) {
            NativeObject no1 = (NativeObject) iter1.next();
            if (Arrays.deepEquals(no1.getHasAlignmentInformation().getHasParserPath(), parserPathToFind)) {
                return no1;
            }
        }

        Iterator iter2 = co.getHasComplexObjects().iterator();
        while (iter2.hasNext()) {
            ComplexObject co1 = (ComplexObject) iter2.next();
            if (Arrays.deepEquals(co1.getHasAlignmentInformation().getHasParserPath(), parserPathToFind)) {
                return co1;
            } else {
                Object retObj = findParserPathAmongContainedOfComplexObject_ITERATIVE(co1, parserPathToFind);
                if (retObj != null) {
                    return retObj;
                }
            }
        }
        return null;
    }

    private static void fixComplexObjectPathIterative(ComplexObject co, Vector qNamesSoFar, int count) {
        if (co == null) {
            return;
        }

        Iterator iter1 = co.getHasNativeObjects().iterator();
        while (iter1.hasNext()) {
            NativeObject no1 = (NativeObject) iter1.next();
            no1.setHasParent(co);
            no1.setIsInput(co.isIsInput());

            AlignmentInformation ai = new AlignmentInformation();
            QName[] qNarray = new QName[qNamesSoFar.size() + 1];
            for (int i = 0; i < qNamesSoFar.size(); i++) {
                //qNarray[i] = (QName) qNamesSoFar.get(i);
                if (qNamesSoFar.get(i) != null) {
                    QName qName1 = new QName(((QName) qNamesSoFar.get(i)).getNamespaceURI(),
                            ((QName) qNamesSoFar.get(i)).getLocalPart(), ((QName) qNamesSoFar.get(i)).getPrefix());
                    qNarray[i] = qName1;
                } else {
                    qNarray[i] = new QName("");
                }
            }
            qNarray[qNarray.length - 1] = no1.getObjectName();
            ai.setHasParserPath(qNarray);
            no1.setHasAlignmentInformation(ai);
        }

        Iterator iter2 = co.getHasComplexObjects().iterator();
        while (iter2.hasNext()) {
            ComplexObject co1 = (ComplexObject) iter2.next();
            co1.setHasParent(co);
            co1.setIsInput(co.isIsInput());

            AlignmentInformation ai = new AlignmentInformation();
            QName[] qNarray = new QName[qNamesSoFar.size() + 1];
            for (int i = 0; i < qNamesSoFar.size(); i++) {
                if (qNamesSoFar.get(i) != null) {
                    QName qName1 = new QName(((QName) qNamesSoFar.get(i)).getNamespaceURI(),
                            ((QName) qNamesSoFar.get(i)).getLocalPart(), ((QName) qNamesSoFar.get(i)).getPrefix());
                    qNarray[i] = qName1;
                } else {
                    qNarray[i] = new QName("");
                }
            }
            qNarray[qNarray.length - 1] = co1.getObjectName();
            ai.setHasParserPath(qNarray);
            co1.setHasAlignmentInformation(ai);

            Vector newQNamesVector = (Vector) qNamesSoFar.clone();
            newQNamesVector.add(qNarray[qNarray.length - 1]);//isws prepei ki edw na tou dwsw to kainourgio ftiaxnontas kainourio QName

            count++;
            if (count < 30) {
                fixComplexObjectPathIterative(co1, newQNamesVector, count);
            }
        }

        if (co.getHasExtendedObjects() != null) {
            Iterator iter3 = co.getHasExtendedObjects().iterator();
            while (iter3.hasNext()) {
                Object obj = iter3.next();
                if (obj != null && obj.getClass().getName().contains("NativeObject")) {
                    NativeObject no1 = (NativeObject) obj;
                    no1.setHasParent(co);
                    no1.setIsInput(co.isIsInput());

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[qNamesSoFar.size() + 1];
                    for (int i = 0; i < qNamesSoFar.size(); i++) {
                        //qNarray[i] = (QName) qNamesSoFar.get(i);
                        if (qNamesSoFar.get(i) != null) {
                            QName qName1 = new QName(((QName) qNamesSoFar.get(i)).getNamespaceURI(),
                                    ((QName) qNamesSoFar.get(i)).getLocalPart(), ((QName) qNamesSoFar.get(i)).getPrefix());
                            qNarray[i] = qName1;
                        } else {
                            qNarray[i] = new QName("");
                        }
                    }
                    qNarray[qNarray.length - 1] = no1.getObjectName();
                    ai.setHasParserPath(qNarray);
                    no1.setHasAlignmentInformation(ai);
                } else if (obj != null && obj.getClass().getName().contains("ComplexObject")) {
                    ComplexObject co1 = (ComplexObject) obj;
                    co1.setHasParent(co);
                    co1.setIsInput(co.isIsInput());

                    AlignmentInformation ai = new AlignmentInformation();
                    QName[] qNarray = new QName[qNamesSoFar.size() + 1];
                    for (int i = 0; i < qNamesSoFar.size(); i++) {
                        if (qNamesSoFar.get(i) != null) {
                            QName qName1 = new QName(((QName) qNamesSoFar.get(i)).getNamespaceURI(),
                                    ((QName) qNamesSoFar.get(i)).getLocalPart(), ((QName) qNamesSoFar.get(i)).getPrefix());
                            qNarray[i] = qName1;
                        } else {
                            qNarray[i] = new QName("");
                        }
                    }
                    qNarray[qNarray.length - 1] = co1.getObjectName();
                    ai.setHasParserPath(qNarray);
                    co1.setHasAlignmentInformation(ai);

                    Vector newQNamesVector = (Vector) qNamesSoFar.clone();
                    newQNamesVector.add(qNarray[qNarray.length - 1]);//isws prepei ki edw na tou dwsw to kainourgio ftiaxnontas kainourio QName

                    count++;
                    if (count < 30) {
                        fixComplexObjectPathIterative(co1, newQNamesVector, count);
                    }
                }
            }
        }

    }
}
