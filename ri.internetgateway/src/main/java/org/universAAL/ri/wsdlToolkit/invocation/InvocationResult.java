/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.invocation;

import java.util.Vector;


public class InvocationResult {
    private String hasRequestInString;
    private String hasResponseInString;

    public Vector responseHasNativeOrComplexObjects=new Vector();

    public void setHasRequestInString(String v){
        this.hasRequestInString=v;
    }

    public String getHasRequestInString(){
        return this.hasRequestInString;
    }

    public void setHasResponseInString(String v){
        this.hasResponseInString=v;
    }

    public String getHasResponseInString(){
        return this.hasResponseInString;
    }

    public void setResponseHasNativeOrComplexObjects(Vector v){
        this.responseHasNativeOrComplexObjects=v;
    }

    public Vector getResponseHasNativeOrComplexObjects(){
        return this.responseHasNativeOrComplexObjects;
    }





}
