/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.universAAL.ri.wsdlToolkit.ioApi;

import java.util.Vector;

public class WSOperationInput {
    //public Vector hasComplexObjects;

    private Vector hasNativeOrComplexObjects = new Vector();
    private String hasUse;//   encoded or literal
    private Vector hasSoapHeaders = new Vector();

    public Vector getHasNativeOrComplexObjects() {
        return hasNativeOrComplexObjects;
    }

    public void setHasNativeOrComplexObjects(Vector hasNativeOrComplexObjects) {
        this.hasNativeOrComplexObjects = hasNativeOrComplexObjects;
    }

    public String getHasUse() {
        return hasUse;
    }

    public void setHasUse(String hasUse) {
        this.hasUse = hasUse;
    }

    public Vector getHasSoapHeaders() {
        return hasSoapHeaders;
    }

    public void setHasSoapHeaders(Vector hasSoapHeaders) {
        this.hasSoapHeaders = hasSoapHeaders;
    }

    public WSOperationInput cloneTheWSOperationInput() {
        WSOperationInput newWSInput = new WSOperationInput();
        newWSInput.setHasUse(this.getHasUse());
        Vector vec = new Vector();
        for (int i = 0; i < this.getHasNativeOrComplexObjects().size(); i++) {
            if (this.getHasNativeOrComplexObjects().get(i) instanceof NativeObject) {
                vec.add(((NativeObject) this.getHasNativeOrComplexObjects().get(i)).cloneTheNO());
            } else if (this.getHasNativeOrComplexObjects().get(i) instanceof ComplexObject) {
                vec.add(((ComplexObject) this.getHasNativeOrComplexObjects().get(i)).cloneTheCO());
            }
        }
        newWSInput.setHasNativeOrComplexObjects(vec);
        vec = new Vector();
        for (int i = 0; i < this.getHasSoapHeaders().size(); i++) {
            if (this.getHasSoapHeaders().get(i) instanceof NativeObject) {
                vec.add(((NativeObject) this.getHasSoapHeaders().get(i)).cloneTheNO());
            } else if (this.getHasSoapHeaders().get(i) instanceof ComplexObject) {
                vec.add(((ComplexObject) this.getHasSoapHeaders().get(i)).cloneTheCO());
            }
        }
        newWSInput.setHasSoapHeaders(vec);
        fillHasParentValues(newWSInput);
        return newWSInput;
    }

    private void fillHasParentValues(Object obj) {
        if(obj instanceof WSOperationInput){
            WSOperationInput in=(WSOperationInput)obj;
            for(int i=0;i<in.getHasNativeOrComplexObjects().size();i++){
                if(in.getHasNativeOrComplexObjects().get(i) instanceof ComplexObject){
                    ComplexObject co = (ComplexObject) in.getHasNativeOrComplexObjects().get(i);
                    co.setHasParent(obj);
                    fillHasParentValues(co);

                }
                else
                    if(in.getHasNativeOrComplexObjects().get(i) instanceof NativeObject){
                    NativeObject no = (NativeObject)  in.getHasNativeOrComplexObjects().get(i);
                    no.setHasParent(obj);
                }
            }
        }
        else
            if(obj instanceof ComplexObject){
                ComplexObject co=(ComplexObject)obj;
                
                for (int j = 0; j < co.getHasNativeObjects().size(); j++) {
                    ((NativeObject) co.getHasNativeObjects().get(j)).setHasParent(co);
                }
                for (int j = 0; j < co.getHasComplexObjects().size(); j++) {
                    ((ComplexObject) co.getHasComplexObjects().get(j)).setHasParent(co);
                    fillHasParentValues(((ComplexObject) co.getHasComplexObjects().get(j)));
                 
                   
                }
                for (int j = 0; j < co.getHasExtendedObjects().size(); j++) {
                    if(co.getHasExtendedObjects().get(j) instanceof NativeObject){
                        ((NativeObject) co.getHasExtendedObjects().get(j)).setHasParent(co);
                    }
                    else
                        if(co.getHasExtendedObjects().get(j) instanceof ComplexObject){
                            ((ComplexObject) co.getHasExtendedObjects().get(j)).setHasParent(co);
                            fillHasParentValues(((ComplexObject) co.getHasExtendedObjects().get(j)));
                            
                        }                   
                }
            }
    }
}
