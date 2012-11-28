/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.universAAL.ri.wsdlToolkit.invocation;


import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;


public class Axis2WebServiceInvoker {

    public static InvocationResult invokeWebService(URL wsdlURL, QName operationName, WSOperationInput operationInput,
            WSOperation theParsedOperation, ParsedWSDLDefinition theDefinition) throws Exception {
        ServiceClient theServiceClient = null;

        try {
            InvocationResult result = new InvocationResult();
            theServiceClient = new ServiceClient();

            //OperationClient operationClient=theServiceClient.createClient(operationName);
            OperationClient operationClient = theServiceClient.createClient(ServiceClient.ANON_OUT_IN_OP);

            MessageContext outMsgCtx = new MessageContext();
            Options opts = outMsgCtx.getOptions();

            
            opts.setTimeOutInMilliSeconds(100000);
            opts.setTo(new EndpointReference(theDefinition.getServiceURL()));
            opts.setAction(theParsedOperation.getHasBindingSoapAction());           

            System.out.println(theDefinition.getOperationsUse());
            System.out.println(theDefinition.getbindingStyle());

            if (theDefinition.getbindingStyle().equals("RPC") && theDefinition.getOperationsUse().equals("ENCODED")) {
                outMsgCtx.setEnvelope(Axis2_RpcEncodedMessageBuilder.createSOAPEnvelope_RPC_Encoded(operationName, operationInput, theDefinition));
            } else {
                outMsgCtx.setEnvelope(createSOAPEnvelope(operationName, operationInput));
            }

            operationClient.addMessageContext(outMsgCtx);

            System.out.println(outMsgCtx.getEnvelope().toString());
            result.setHasRequestInString(outMsgCtx.getEnvelope().toString());

            operationClient.execute(true);

            MessageContext inMsgCtx = operationClient.getMessageContext("In");
            //Vector res=Axis2ResultHandler.parseInvocationOutput(inMsgCtx, theParsedOperation);

            InvocationResult res = null;
            if (theDefinition.getbindingStyle().equals("RPC") && theDefinition.getOperationsUse().equals("ENCODED")) {
                res = Axis2InvocationResultHandler_RPC.parseResult(inMsgCtx, theParsedOperation);
            } else {
                res = Axis2InvocationResultHandler.parseResult(inMsgCtx, theParsedOperation);
            }

            result.setHasResponseInString(inMsgCtx.getEnvelope().toString());
            if (res != null) {
                result.setResponseHasNativeOrComplexObjects(res.getResponseHasNativeOrComplexObjects());
            }
            theServiceClient.cleanup();
            theServiceClient.cleanupTransport();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            theServiceClient.cleanup();
            theServiceClient.cleanupTransport();
            return null;
        }

    }

    
    
    public static InvocationResult invokeWebService( 
            WSOperation theParsedOperation, ParsedWSDLDefinition theDefinition) throws Exception {
        ServiceClient theServiceClient = null;

        try {
        	URL wsdlURL=theDefinition.getWsdlURL();
        	String namespaceURI=theDefinition.getTargetNamespaceURI();
        	String operationName=theParsedOperation.getOperationName();
            InvocationResult result = new InvocationResult();
            theServiceClient = new ServiceClient();

            //OperationClient operationClient=theServiceClient.createClient(operationName);
            OperationClient operationClient = theServiceClient.createClient(ServiceClient.ANON_OUT_IN_OP);

            MessageContext outMsgCtx = new MessageContext();
            Options opts = outMsgCtx.getOptions();

            //opts.setTo(new EndpointReference("http://iti-181/WebServices/Service.asmx"));
            //opts.setAction("http://www.iti.gr/webservices/test1/EstimateSum");
            opts.setTimeOutInMilliSeconds(100000);
            opts.setTo(new EndpointReference(theDefinition.getServiceURL()));
            opts.setAction(theParsedOperation.getHasBindingSoapAction());
            //opts.setAction("http://www.iti.gr/webservices/test1/EstimateSum");

            System.out.println(theDefinition.getOperationsUse());
            System.out.println(theDefinition.getbindingStyle());

            if (theDefinition.getbindingStyle().equals("RPC") && theDefinition.getOperationsUse().equals("ENCODED")) {
                outMsgCtx.setEnvelope(Axis2_RpcEncodedMessageBuilder.createSOAPEnvelope_RPC_Encoded(new QName(namespaceURI, operationName), theParsedOperation.getHasInput(), theDefinition));
            } else {
                outMsgCtx.setEnvelope(createSOAPEnvelope(new QName(namespaceURI, operationName), theParsedOperation.getHasInput()));
            }

            operationClient.addMessageContext(outMsgCtx);

            System.out.println(outMsgCtx.getEnvelope().toString());
            result.setHasRequestInString(outMsgCtx.getEnvelope().toString());

            operationClient.execute(true);

            MessageContext inMsgCtx = operationClient.getMessageContext("In");
            //Vector res=Axis2ResultHandler.parseInvocationOutput(inMsgCtx, theParsedOperation);

            InvocationResult res = null;
            if (theDefinition.getbindingStyle().equals("RPC") && theDefinition.getOperationsUse().equals("ENCODED")) {
                res = Axis2InvocationResultHandler_RPC.parseResult(inMsgCtx, theParsedOperation);
            } else {
                res = Axis2InvocationResultHandler.parseResult(inMsgCtx, theParsedOperation);
            }


            result.setHasResponseInString(inMsgCtx.getEnvelope().toString());
            if (res != null) {
                result.setResponseHasNativeOrComplexObjects(res.getResponseHasNativeOrComplexObjects());
            }
            theServiceClient.cleanup();
            theServiceClient.cleanupTransport();
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            theServiceClient.cleanup();
            theServiceClient.cleanupTransport();
            return null;
        }
    }
    
    
    
    
    
    
    
    
    
    private static SOAPEnvelope createSOAPEnvelope(QName operationName, WSOperationInput operationInput) {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();

        OMNamespace omNs = fac.createOMNamespace(operationName.getNamespaceURI(), "opNS");

        OMElement messageBody = createSOAPBody(fac, operationName, operationInput.getHasNativeOrComplexObjects(), omNs);

        envelope.getBody().addChild(messageBody);

        addOperationHeaderToEnvelope(fac, envelope, operationInput.getHasSoapHeaders(), omNs);

        return envelope;
    }

    private static void addOperationHeaderToEnvelope(SOAPFactory fac, SOAPEnvelope envelope, Vector operationHeaderObjects, OMNamespace operationNs) {
        Iterator headerObjectsIter = operationHeaderObjects.iterator();
        while (headerObjectsIter.hasNext()) {
            ComplexObject headerCO = (ComplexObject) headerObjectsIter.next();

            //to 'http://www.xignite.com/xrates.asmx?WSDL' thelei na mpei sto header node to type:Header anti gia to p.x. name:DrawYieldCurveHeader...
            headerCO.setObjectName(headerCO.getObjectType());

            if (headerCO.isIsArrayType()) {
                //ARRAY TYPE!!!
                Vector<OMElement> valueA = createOMElementForArrayTypeObjectInput(fac, headerCO, operationNs);
                for (int i = 0; i < valueA.size(); i++) {
                    envelope.getHeader().addChild(valueA.get(i));
                }
            } else {
                OMElement valueA = createOMElementForComplexObjectInput(fac, headerCO, operationNs);
                if (valueA != null) {
                    envelope.getHeader().addChild(valueA);
                }
            }

            /*
            Iterator nativeObjsIter=headerCO.hasNativeObjects.iterator();
            while(nativeObjsIter.hasNext()){                 
            NativeObject no=(NativeObject)nativeObjsIter.next();;
            //OMNamespace inputObjectNs = fac.createOMNamespace(no.getNamespaceURI(), "ns1");
            OMElement valueA = createOMElementForNativeObjectInput(fac, no, operationNs);
            envelope.getHeader().addChild(valueA);
            }
            
            Iterator complexObjsIter=headerCO.hasComplexObjects.iterator();
            while(complexObjsIter.hasNext()){
            ComplexObject co=(ComplexObject)complexObjsIter.next();
            if(co.isArrayType){
            //ARRAY TYPE!!!
            OMElement valueA = createOMElementForArrayTypeObjectInput(fac, co, operationNs);
            envelope.getHeader().addChild(valueA);
            }else{
            OMElement valueA = createOMElementForComplexObjectInput(fac, co, operationNs);
            envelope.getHeader().addChild(valueA);
            }
            }*/
            System.out.println(envelope);
        }
//        envelope.getHeader().addHeaderBlock(arg0, arg1)
    }

    private static OMElement createSOAPBody(SOAPFactory fac, QName operationName, Vector operationInputs, OMNamespace operationNs) {
        OMElement method = fac.createOMElement(operationName.getLocalPart(), operationNs);

        Iterator operInputsIter = operationInputs.iterator();
        while (operInputsIter.hasNext()) {
            Object inputObject = operInputsIter.next();
            if (inputObject.getClass().getName().contains("NativeObject")) {
                NativeObject no = (NativeObject) inputObject;
                //OMNamespace inputObjectNs = fac.createOMNamespace(no.getNamespaceURI(), "ns1");
                OMElement valueA = createOMElementForNativeObjectInput(fac, no, operationNs);
                if (valueA != null) {
                    method.addChild(valueA);
                }

            } else if (inputObject.getClass().getName().contains("ComplexObject")) {
                ComplexObject co = (ComplexObject) inputObject;
                if (co.isIsArrayType()) {
                    //ARRAY TYPE!!!
                    Vector<OMElement> valueA = createOMElementForArrayTypeObjectInput(fac, co, operationNs);
                    for (int i = 0; i < valueA.size(); i++) {
                        method.addChild(valueA.get(i));
                    }
                } else {
                    OMElement valueA = createOMElementForComplexObjectInput(fac, co, operationNs);
                    if (valueA != null) {
                        method.addChild(valueA);
                    }
                }

            } else {
                System.out.println("ERROR 1!!!!!");
            }
        }

        return method;
    }

    public static boolean theComplexObjectCarriesValues_ITERATIVE(ComplexObject co) {

        Iterator iter1 = co.getHasNativeObjects().iterator();
        while (iter1.hasNext()) {
            NativeObject no = (NativeObject) iter1.next();
            if (no.getHasValue() != null && no.getHasValue().length() > 0) {
                return true;
            }
        }

        Iterator iter2 = co.getHasComplexObjects().iterator();
        while (iter2.hasNext()) {
            ComplexObject co1 = (ComplexObject) iter2.next();
            if (theComplexObjectCarriesValues_ITERATIVE(co1)) {
                return true;
            }
        }

        return false;
    }

    private static OMElement createOMElementForComplexObjectInput(SOAPFactory fac, ComplexObject co, OMNamespace operationNamespace) {

//        if (co.isIsOptional() && (!theComplexObjectCarriesValues_ITERATIVE(co))) {
//            return null;
//        }
 if (co.isIsOptional() && (!theComplexObjectCarriesValues_ITERATIVE(co))&&hasOptionalParent(co)) {
            return null;
        }
//        if(!areAllParentsOptional(co.getHasParent())){
//            return null;
//        }

        //if(!theComplexObjectCarriesValues_ITERATIVE(co))return null;
//QName name=null;
// if(co.getHasParent() instanceof ComplexObject){
//     name=((ComplexObject)co.getHasParent()).getObjectName();
// }
// else{
   QName name=co.getObjectName();
// }


        OMElement valueA = fac.createOMElement(name);
        Vector nosVector = co.getHasNativeObjects();
        Iterator iter1 = nosVector.iterator();
        while (iter1.hasNext()) {
            NativeObject no = (NativeObject) iter1.next();
            OMElement valueForNO = createOMElementForNativeObjectInput(fac, no, operationNamespace);
            if (valueForNO != null) {
                valueA.addChild(valueForNO);
            }
        }

        Iterator iter2 = co.getHasComplexObjects().iterator();
        while (iter2.hasNext()) {
            ComplexObject co1 = (ComplexObject) iter2.next();
            OMElement valueForCO1 = null;

            if (co1.isIsArrayType()) {
                //ARRAY TYPE!!!
                Vector<OMElement> valueForCO2 = new Vector<OMElement>();
                valueForCO2 = createOMElementForArrayTypeObjectInput(fac, co1, operationNamespace);
                //4-1-10
                //Iterator iter123=valueForCO1.getChildElements();
                //while(iter123.hasNext()){
                //    valueA.addChild((OMNode)iter123.next());
                //}
                for (int i = 0; i < valueForCO2.size(); i++) {
                    valueA.addChild(valueForCO2.get(i));
                }
            } else {
                valueForCO1 = createOMElementForComplexObjectInput(fac, co1, operationNamespace);
                
                if (valueForCO1 != null) {
                    valueA.addChild(valueForCO1);
                }
            }

            //TI ROLO VARAEI AYTO EDW DEN KSERW...
            if (/*!co1.isIsArrayType()&&*/valueForCO1 != null) {
                valueA.addChild(valueForCO1);
            }
        }
        return valueA;
    }

    private static Vector<OMElement> createOMElementForArrayTypeObjectInput(SOAPFactory fac, ComplexObject co, OMNamespace operationNamespace) {
        //OMElement valueA = fac.createOMElement(co.getObjectName(), operationNamespace);
        // OMElement valueA = fac.createOMElement(co.getObjectName());
        Vector<OMElement> valueA = new Vector<OMElement>();
        if (co.getHasComplexObjects().size() > 0) {
            Iterator iter2 = co.getHasComplexObjects().iterator();
            while (iter2.hasNext()) {
                ComplexObject co1 = (ComplexObject) iter2.next();
                co1.setObjectName(co.getObjectName());
                Vector<OMElement> valueForCO1 = null;

                if (co1.isIsArrayType()) {
                    //ARRAY TYPE!!!
                    valueForCO1 = createOMElementForArrayTypeObjectInput(fac, co1, operationNamespace);
                    //4-1-10
                    //Iterator iter123=valueForCO1.getChildElements();
                    //while(iter123.hasNext()){
                    //    valueA.addChild((OMNode)iter123.next());
                    // }
                    for (int i = 0; i < valueForCO1.size(); i++) {
                        valueA.add(valueForCO1.get(i));
                    }
                } else {
                    OMElement valueForCO2 = null;
                    valueForCO2 = createOMElementForComplexObjectInput(fac, co1, operationNamespace);
                    if (valueForCO2 != null) {

                        valueA.add(valueForCO2);

                    }
                }

                if (valueForCO1 != null) {
                    for (int i = 0; i < valueForCO1.size(); i++) {
                        valueA.add(valueForCO1.get(i));
                    }
                }
            }

        } else if (co.getHasNativeObjects().size() > 0) {
            Vector nosVector = co.getHasNativeObjects();
            Iterator iter1 = nosVector.iterator();
            while (iter1.hasNext()) {
                NativeObject no = (NativeObject) iter1.next();
                OMElement valueForNO = createOMElementForNativeObjectInput(fac, no, operationNamespace);
                if (valueForNO != null) {
                    valueA.add(valueForNO);
                }
            }
        }
        return valueA;

    }

    private static OMElement createOMElementForNativeObjectInput(SOAPFactory fac, NativeObject no, OMNamespace operationNamespace) {
        //OMElement valueA = fac.createOMElement(no.getObjectName(), operationNamespace);
//        if (no.isIsOptional() && (no.getHasValue() == null || no.getHasValue().length() == 0||no.getHasValue().equals(""))) {
//            return null;
//        }
        if (no.getHasValue().equals("") && hasOptionalParent(no)) {
            return null;
        }
        if(no.getHasValue().equals("")&&no.isIsOptional()){
            return null;
        }
        OMElement valueA = fac.createOMElement(no.getObjectName());
        valueA.setText(no.getHasValue());
        return valueA;
    }


     private static boolean hasOptionalParent(Object obj) {
        if (obj instanceof ComplexObject) {
            ComplexObject co = (ComplexObject) obj;
            if(co.isIsOptional()){
                return true;
            }
            Object obj1 = co.getHasParent();
            if (obj1 instanceof ComplexObject) {
                return hasOptionalParent(((ComplexObject) obj1));
            }
            else{
                return co.isIsOptional();
            }
        } else if (obj instanceof NativeObject) {
            NativeObject no = (NativeObject)obj;
            Object obj1 = no.getHasParent();
            if (obj1 instanceof ComplexObject) {
                return hasOptionalParent(((ComplexObject) obj1));
            }
            else
                return no.isIsOptional();
        } else {
            return false;
        }

    }



}
