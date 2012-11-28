/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.invocation;


import java.net.URL;
import javax.xml.namespace.QName;


public class ITIWebServiceInvocator {

    /*
    public static InvocationResult invokeWebService(URL wsdlURL, QName operationName, WSOperationInput operationInput,
            WSOperation theParsedOperation, ParsedWSDLDefinition theDefinition) throws Exception{

        InvocationResult theResult=null;

        if(theDefinition.getbindingStyle().equals("RPC")&&theDefinition.getOperationsUse().equals("ENCODED")){
            theResult=Axis1WebServiceInvoker.invokeWebService(wsdlURL, operationName, operationInput, theParsedOperation, theDefinition);
        }else{
            theResult=Axis2WebServiceInvoker.invokeWebService(wsdlURL, operationName, operationInput, theParsedOperation, theDefinition);
        }

        return theResult;

    }*/

}
