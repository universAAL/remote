/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.invocation;

//import javax.xml.rpc.namespace.QName;

import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.QName;


import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.utils.Options;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.ParsedWSDLDefinition;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperationInput;



public class Axis1WebServiceInvoker {

    public static InvocationResult invokeWebService(URL wsdlURL, QName operationName, WSOperationInput operationInput,
            WSOperation theParsedOperation, ParsedWSDLDefinition theDefinition) throws Exception{

        InvocationResult theInvocationResult=null;

        if(checkIfOperationHasOnlyNativeObjects(theParsedOperation)){
            theInvocationResult=invokeSimpleRPCService(theParsedOperation, operationInput);
        }else{
            System.out.println("NATIVE OBJECTS NOT YET SUPPORTED!!!!!");
        }
        Object[] inputsAsObjectArray=getInputAsObjectArray(operationInput.getHasNativeOrComplexObjects(), theParsedOperation, theDefinition);
        return null;


    }

    private static boolean checkIfOperationHasOnlyNativeObjects(WSOperation theParsedOperation){
        boolean hasOnlyNOs=true;
        Iterator iter1=theParsedOperation.getHasInput().getHasNativeOrComplexObjects().iterator();
        while(iter1.hasNext()){
            try{
                NativeObject no=(NativeObject)iter1.next();
            }catch(Exception e){
                hasOnlyNOs=false;
            }
        }

        Iterator iter2=theParsedOperation.getHasOutput().getHasNativeOrComplexObjects().iterator();
        while(iter2.hasNext()){
            try{
                NativeObject no=(NativeObject)iter2.next();
            }catch(Exception e){
                hasOnlyNOs=false;
            }
        }

        return hasOnlyNOs;
    }

    private static Object[] getInputAsObjectArray(Vector operationInputs,
            WSOperation theParsedOperation, ParsedWSDLDefinition theDefinition){

        if(operationInputs==null||operationInputs.size()==0)return null;

        Object[] result=new Object[operationInputs.size()];

        Iterator operInputsIter=operationInputs.iterator();
        while(operInputsIter.hasNext()){
            Object inputObject=operInputsIter.next();
            if(inputObject.getClass().getName().contains("NativeObject")){
                NativeObject no=(NativeObject)inputObject;
                Object obj=new Object();
                //Cast obj to the primitive type

            }else if(inputObject.getClass().getName().contains("ComplexObject")){
                ComplexObject co=(ComplexObject)inputObject;
                Object obj=new Object();

            }else{
                System.out.println("ERROR 1!!!!!");
            }
        }

        return result;
    }


    
//    public static void invokeService1(){
//        String wsdlURL="http://160.40.10.35:8080/AthensWS/services/AthensWS?wsdl";
//        String serviceURL="http://160.40.10.35:8080/AthensWS/services/AthensWS";
//        System.out.println(wsdlURL);
//        
//
//        try{
//            Service service = new Service();
//            Call call= (Call) service.createCall();
//            call.setTargetEndpointAddress(new java.net.URL(serviceURL));
//            call.setOperationName(new QName("getPOIsDetailsWithBoundingBox"));
//
//            /*
//            call.addParameter(new QName("param_Name_IN_1"), new QName("ns1", "param_Type_IN_1"), javax.xml.rpc.ParameterMode.IN);
//            call.addParameter(new QName("param_Name_IN_2"), new QName("ns2", "param_Type_IN_2"), javax.xml.rpc.ParameterMode.IN);
//
//            call.addParameter(new QName("param_Name_OUT_1"), new QName("ns1", "param_Type_OUT_1"), javax.xml.rpc.ParameterMode.IN);
//
//            call.initialize();
//
//            SOAPEnvelope envelope=new SOAPEnvelope();
//            SOAPBodyElement bodyElement=new SOAPBodyElement();
//            bodyElement.setQName(new QName("nsURI_1","varName1"));
//            bodyElement.setType(new QName("typeURIIIII", "type_MITSOS", "ff"));
//
//
//
//            //bodyElement.setValue("gamwto 1");
//
//            envelope.addBodyElement(bodyElement);
//
//
//            SOAPBodyElement bodyElement2=new SOAPBodyElement();
//            bodyElement2.setQName(new QName("nsURI_2","varName2"));
//            bodyElement2.setValue("gamwto 2");
//
//            envelope.addBodyElement(bodyElement2);
//            //org.apache.axis.message.EnvelopeBuilder envBuilder=new org.apache.axis.message.EnvelopeBuilder(envelope, "");
//            System.out.println(envelope);
//            MessageElement elem=new MessageElement();
//            bodyElement.addChild(elem);
//*/
//
//
//            Object[] inputs=new Object[5];
//            inputs[0]=22.967414;
//            inputs[1]=40.640232;
//            inputs[2]=22.915216;
//            inputs[3]=40.632097;
//            inputs[4]="1";
//
//            TypeMapping tm=call.getTypeMapping();
//            System.out.println(tm==null);
//
//            Class[] definedClasses=tm.getAllClasses();
//            Class c=definedClasses[4];
//            System.out.println(c.isPrimitive());
//
//
//            
//            
//            System.out.println("Contacting service...");
//            Object result=call.invoke(inputs);
//            System.out.println("OK");
//            System.out.println(result.getClass().getName());
//            java.util.Vector res=(java.util.Vector) result;
//            System.out.println(call.getMessageContext().getRequestMessage().getSOAPEnvelope());
//            System.out.println(call.getMessageContext().getResponseMessage().getSOAPEnvelope());
//
//
//
//            System.out.println(res.size());
//
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//        org.apache.axis.wsdl.gen.Parser axis1Parser=new org.apache.axis.wsdl.gen.Parser();
//
//        System.out.println("\nAccessing WSDL with AXIS 1...");
//        try{
//            axis1Parser.setImports(true);
//            axis1Parser.run(wsdlURL);
//            System.out.println("OK");
//        }
//        catch(Exception e){
//            System.out.println("Parsing with AXIS failed... ");
//            System.out.println("REASON: ");
//            e.printStackTrace();
//        }
//        
//    }

    public static void main(String[] args){
       // invokeService1();
    }

    private static InvocationResult invokeSimpleRPCService(WSOperation theParsedOperation, WSOperationInput operationInput) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
