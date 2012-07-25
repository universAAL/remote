/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.universAAL.ri.wsdlToolkit.invocation;



import java.util.Iterator;
import java.util.Vector;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMNodeImpl;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

import org.universAAL.ri.wsdlToolkit.ioApi.AlignmentInformation;
import org.universAAL.ri.wsdlToolkit.ioApi.ComplexObject;
import org.universAAL.ri.wsdlToolkit.ioApi.NativeObject;
import org.universAAL.ri.wsdlToolkit.ioApi.WSOperation;


public class Axis2InvocationResultHandler_RPC {

    public static InvocationResult parseResult(MessageContext inMsgCtx, WSOperation theParsedOperation) {

        InvocationResult theResult = new InvocationResult();
        Vector parsedOperationOutputsVector = theParsedOperation.getHasOutput().getHasNativeOrComplexObjects();
        Iterator parsedOperationOutputsIter = parsedOperationOutputsVector.iterator();
        Vector vec = new Vector();
        while (parsedOperationOutputsIter.hasNext()) {
            Object parsedOutObj = parsedOperationOutputsIter.next();

            if (parsedOutObj instanceof NativeObject) {
                NativeObject outNO = (NativeObject) parsedOutObj;
                OMElementImpl omElement = findTheNativeObjectNodeInTheResponseBody(inMsgCtx, outNO);
                setNativeObjectValue(outNO, omElement);
            } else if (parsedOutObj instanceof ComplexObject) {
                ComplexObject outCO = (ComplexObject) parsedOutObj;
                if (!outCO.isIsArrayType()) {
                    OMElementImpl omElement = findTheComplexObjectNodeInTheResponseBody(inMsgCtx, outCO);
                    setComplexObjectValues(outCO, omElement);
                } else {
                    Iterator<OMElementImpl> omElement = findTheArrayObjectNodesInTheResponseBody(inMsgCtx, null, outCO);
                    int i = 0;
                    while (omElement.hasNext()) {
                        if (((ComplexObject) outCO).getHasComplexObjects().size() > 0) {
                            if (i == 0) {
                                setComplexObjectValues((ComplexObject) outCO.getHasComplexObjects().get(0), omElement.next());
                            } else {
                                ComplexObject co = ((ComplexObject) outCO.getHasComplexObjects().get(0)).cloneTheCO();
                                clearArrayTypes(co);
                                clearValues(co);
                                setComplexObjectValues(co, omElement.next());
                                co.setHasParent(outCO.getHasParent());
                                outCO.getHasComplexObjects().add(co);
                            }
                        } else {
                            if (i == 0) {
                                setNativeObjectValue((NativeObject) outCO.getHasNativeObjects().get(0), omElement.next());
                            } else {
                                NativeObject co = ((NativeObject) outCO.getHasNativeObjects().get(0)).cloneTheNO();
                                setNativeObjectValue(co, omElement.next());
                                co.setHasParent(outCO);
                                outCO.getHasNativeObjects().add(co);
                            }
                        }
                        i++;
                    }
                }
            }
        }
        for (int i = 0; i < vec.size(); i++) {
            theParsedOperation.getHasOutput().getHasNativeOrComplexObjects().add(vec.get(i));
        }
        theResult.responseHasNativeOrComplexObjects = parsedOperationOutputsVector;
        return theResult;
    }

    private static void setComplexObjectValues(ComplexObject co, OMElementImpl omElement) {
        if (omElement == null || co == null) {
            return;
        }

        if (!co.isIsArrayType()) {
            if (co.getHasComplexObjects() != null && co.getHasComplexObjects().size() > 0) {
                Iterator cosIter = co.getHasComplexObjects().iterator();
                while (cosIter.hasNext()) {
                    ComplexObject co1 = (ComplexObject) cosIter.next();

                    if (!co1.isIsArrayType()) {
                        OMElementImpl omElement1 = findTheComplexObjectNodeInOMElement(omElement, co1);
                        setComplexObjectValues(co1, omElement1);
                    } else {
                        setArrayObjectValues(co1, omElement);
                    }
                }
            }

            if (co.getHasNativeObjects() != null && co.getHasNativeObjects().size() > 0) {
                Iterator nosIter = co.getHasNativeObjects().iterator();
                while (nosIter.hasNext()) {
                    NativeObject no1 = (NativeObject) nosIter.next();
                    OMElementImpl omElement1 = findTheNativeObjectNodeInOMElement(omElement, no1);
                    setNativeObjectValue(no1, omElement1);
                }
            }
            if (co.getHasNativeObjects() != null && co.getHasNativeObjects().size() == 0 && co.getHasComplexObjects() != null && co.getHasComplexObjects().size() == 0) {
                Iterator iter = omElement.getChildren();
                while (iter.hasNext()) {
                    OMElementImpl omElement2 = (OMElementImpl) iter.next();
                    Iterator iter2 = omElement2.getChildren();
                    boolean hasChildren = false;
                    while (iter2.hasNext()) {
                        hasChildren = true;
                        break;
                    }
                    if (hasChildren) {
                        ComplexObject co1 = new ComplexObject();
                        co1.setHasParent(co);
                        co1.setObjectName(new QName(omElement2.getLocalName()));
                        createNodesAmongChildren(omElement2, co1);
                        co1.setObjectType(new QName(getComplexObjectType(co1)));
                        co.getHasComplexObjects().add(co1);
                    } else {
                        NativeObject no = new NativeObject();
                        no.setObjectName(new QName(omElement2.getLocalName()));
                        no.setHasParent(co);
                        no.setObjectType(new QName("String"));
                        no.setHasAlignmentInformation(new AlignmentInformation());
                        no.setHasValue(omElement2.getText());
                        co.getHasNativeObjects().add(no);
                    }
                }
            }
        } else {
            setArrayObjectValues(co, omElement);
        }
    }

    private static String getComplexObjectType(ComplexObject co) {
        if (co.getHasComplexObjects().size() == 0 && co.getHasNativeObjects().size() != 0) {
            return co.getObjectName().getLocalPart();
        } else if (co.getHasComplexObjects().size() != 0) {
            if (!checkIfComplexObjectIsArrayType(co).equals("")) {
                co.setIsArrayType(true);
                return checkIfComplexObjectIsArrayType(co);
            } else {
                co.setIsArrayType(false);
                return co.getObjectName().getLocalPart();
            }
        }
        return "";
    }

    private static String checkIfComplexObjectIsArrayType(ComplexObject co) {
        Vector vec = co.getHasComplexObjects();
        if (vec.size() != 0) {
            if (vec.get(0) instanceof ComplexObject) {
                String name = ((ComplexObject) vec.get(0)).getObjectName().getLocalPart();
                for (int i = 1; i < vec.size(); i++) {
                    if (!name.equals(((ComplexObject) vec.get(i)).getObjectName().getLocalPart())) {
                        return co.getObjectName().getLocalPart();
                    }
                }
                return name + "[]";
            }
        } else {
            vec = co.getHasNativeObjects();
            if (vec.size() != 0) {
                String name = ((NativeObject) vec.get(0)).getObjectName().getLocalPart();
                for (int i = 1; i < vec.size(); i++) {
                    if (!name.equals(((NativeObject) vec.get(i)).getObjectName().getLocalPart())) {
                        return co.getObjectName().getLocalPart();
                    }
                }
                return name + "[]";
            }
        }
        return "";

    }

    private static void createNodesAmongChildren(OMElementImpl omElement2, ComplexObject co) {
        Iterator iter2 = omElement2.getChildren();
        while (iter2.hasNext()) {
            try {
                Object obj = iter2.next();
                if (obj instanceof OMElementImpl) {
                    OMElementImpl omElement = (OMElementImpl) obj;
                    Iterator iter = omElement.getChildren();
                    boolean hasChildren = false;
                    while (iter.hasNext()) {
                        hasChildren = true;
                        break;
                    }
                    if (hasChildren) {
                        ComplexObject co1 = new ComplexObject();
                        co1.setHasParent(co);
                        co1.setObjectName(new QName(omElement.getLocalName()));
                        createNodesAmongChildren(omElement, co1);
                        co1.setObjectType(new QName(getComplexObjectType(co1)));
                        co.getHasComplexObjects().add(co1);

                    } else {
                        NativeObject no = new NativeObject();
                        no.setObjectName(new QName(omElement.getLocalName()));
                        no.setHasParent(co);
                        no.setHasValue(omElement.getText());
                        no.setObjectType(new QName("String"));
                        no.setHasAlignmentInformation(new AlignmentInformation());
                        co.getHasNativeObjects().add(no);
                    }
                } else {
                    OMTextImpl omElement = (OMTextImpl) obj;
                    NativeObject no = new NativeObject();
                    no.setObjectName(new QName(omElement.getText()));
                    no.setHasParent(co);
                    no.setObjectType(new QName("String"));
                    no.setHasValue(omElement.getText());
                    no.setHasAlignmentInformation(new AlignmentInformation());
                    co.getHasNativeObjects().add(no);
                }
            } catch (Exception ex) {
            }
        }
        return;
    }

    private static void setArrayObjectValues(ComplexObject co, OMElementImpl omElement) {
        try {
            if (co.getHasNativeObjects() != null && co.getHasNativeObjects().size() > 0) {
                NativeObject no1 = (NativeObject) co.getHasNativeObjects().get(0);
                Iterator<OMElementImpl> vec = findTheArrayObjectNodesInOMElement(omElement, no1, null);
                int i = 0;
                while (vec.hasNext()) {
                    if (i == 0) {
                        setNativeObjectValue(no1, vec.next());
                    } else {
                        NativeObject no2 = no1.cloneTheNO();
                        setNativeObjectValue(no2, vec.next());
                        co.getHasNativeObjects().add(no2);
                    }
                    i++;
                }
            } else if (co.getHasComplexObjects() != null && co.getHasComplexObjects().size() > 0) {
                ComplexObject co1 = (ComplexObject) co.getHasComplexObjects().get(0);
                Iterator<OMElementImpl> vec = findTheArrayObjectNodesInOMElement(omElement, null, co);
                int i = 0;
                while (vec.hasNext()) {
                    if (i == 0) {
                        setComplexObjectValues(co1, vec.next());
                    } else {
                        ComplexObject co2 = co1.cloneTheCO();
                        clearArrayTypes(co2);
                        clearValues(co2);
                        setComplexObjectValues(co2, vec.next());
                        co.getHasComplexObjects().add(co2);
                    }
                    i++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void clearValues(ComplexObject co) {

        for (int i = 0; i < co.getHasComplexObjects().size(); i++) {
            ComplexObject co1 = (ComplexObject) co.getHasComplexObjects().get(i);
            if (co1.isIsArrayType()) {
                if (co1.getHasComplexObjects().size() != 1) {
                    for (int j = co1.getHasComplexObjects().size() - 1; j > 0; j--) {
                        ComplexObject co2 = (ComplexObject) co1.getHasComplexObjects().get(j);
                        clearValues(co2);
                    }
                }
                if (co1.getHasNativeObjects().size() != 1) {
                    for (int j = co1.getHasNativeObjects().size() - 1; j > 0; j--) {
                        NativeObject no2 = (NativeObject) co1.getHasNativeObjects().get(j);
                        no2.setHasValue("");
                    }
                }

            }
            for (int j = 0; j < co.getHasComplexObjects().size(); j++) {
                clearArrayTypes((ComplexObject) co.getHasComplexObjects().get(j));
            }
        }
        for (int i = 0; i < co.getHasNativeObjects().size(); i++) {
            NativeObject no = (NativeObject) co.getHasNativeObjects().get(i);
            no.setHasValue("");
        }
    }

    private static void clearArrayTypes(ComplexObject co) {

        for (int i = 0; i < co.getHasComplexObjects().size(); i++) {
            ComplexObject co1 = (ComplexObject) co.getHasComplexObjects().get(i);
            if (co1.isIsArrayType()) {
                if (co1.getHasComplexObjects().size() != 1) {
                    for (int j = co1.getHasComplexObjects().size() - 1; j > 0; j--) {
                        ComplexObject co2 = (ComplexObject) co1.getHasComplexObjects().get(j);
                        co1.getHasComplexObjects().remove(co2);
                    }
                }
                if (co1.getHasNativeObjects().size() != 1) {
                    for (int j = co1.getHasNativeObjects().size() - 1; j > 0; j--) {
                        NativeObject no2 = (NativeObject) co1.getHasNativeObjects().get(j);
                        co1.getHasNativeObjects().remove(no2);
                    }
                }

            }
            for (int j = 0; j < co.getHasComplexObjects().size(); j++) {
                clearArrayTypes((ComplexObject) co.getHasComplexObjects().get(j));
            }
        }
    }

    private static void setNativeObjectValue(NativeObject no, OMElementImpl omElement) {
        if (omElement == null || no == null) {
            return;
        }
        no.setHasValue(omElement.getText());
    }

    private static OMElementImpl findTheNativeObjectNodeInTheResponseBody(MessageContext inMsgCtx, NativeObject no) {

        if (inMsgCtx == null || inMsgCtx.getEnvelope() == null || inMsgCtx.getEnvelope().getBody() == null) {
            return null;
        }
        SOAPEnvelope response = inMsgCtx.getEnvelope();
        //   System.out.println(response);
        SOAPBody body = response.getBody();
        QName objectQName = null;
        objectQName = no.getObjectName();


        Iterator iter1 = body.getChildren();
        while (iter1.hasNext()) {
            org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1.next();
            Iterator it2 = childOMElement.getChildren();
            while (it2.hasNext()) {
                OMElementImpl childOMElement2 = (OMElementImpl) it2.next();
                if (childOMElement2.getQName().getLocalPart().equals(objectQName.getLocalPart())) {
                    return childOMElement2;
                }
            }
        }
        return null;
    }

    private static OMElementImpl findTheNativeObjectNodeInOMElement(OMElementImpl omElement, NativeObject no) {
        QName objectQName = null;
        objectQName = no.getObjectName();
        Iterator iter1 = omElement.getChildrenWithName(objectQName);
        while (iter1.hasNext()) {

            org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1.next();
            return childOMElement;
        }
        return null;
    }

    private static OMElementImpl findTheComplexObjectNodeInTheResponseBody(MessageContext inMsgCtx, ComplexObject co) {
        if (inMsgCtx == null || inMsgCtx.getEnvelope() == null || inMsgCtx.getEnvelope().getBody() == null) {
            return null;
        }
        SOAPEnvelope response = inMsgCtx.getEnvelope();
        //   System.out.println(response);
        SOAPBody body = response.getBody();
        QName objectQName = null;
        if (co != null) {
            objectQName = co.getObjectName();
        }

        Iterator iter1 = body.getChildren();
        Object obj=null;
        while (iter1.hasNext()) {
            try{
                 obj=iter1.next();
            OMElementImpl childOMElement = (OMElementImpl) obj;
            Iterator it2 = childOMElement.getChildren();
            while (it2.hasNext()) {
                Object obj2=null;
                obj2=it2.next();
                try{
                   OMElementImpl childOMElement2 = (OMElementImpl) obj2;
                //OMElementImpl childOMElement2 = (OMElementImpl) it2.next();
                if (childOMElement2.getQName().getLocalPart().equals(objectQName.getLocalPart())) {
                    return childOMElement2;
                }
                }catch(Exception ex){
                    OMTextImpl childOMElement2=(OMTextImpl) obj2;
                    System.out.println(childOMElement2.getText());
                }
            }
            }catch(Exception ex){
               
                org.apache.axiom.om.impl.llom.OMTextImpl childOMElement = (org.apache.axiom.om.impl.llom.OMTextImpl) obj;
                System.out.println(childOMElement.getText());
            }
        }
        return null;
    }

    private static OMElementImpl findTheComplexObjectNodeInOMElement(OMElementImpl omElement, ComplexObject co) {

        QName objectQName = null;
        if (co != null) {
            objectQName = co.getObjectName();
        }

        Iterator iter1 = omElement.getChildrenWithName(objectQName);
        while (iter1.hasNext()) {
            org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1.next();
            return childOMElement;
        }
        return null;
    }

    private static Iterator findTheArrayObjectNodesInTheResponseBody(MessageContext inMsgCtx, NativeObject no, ComplexObject co) {
        if (inMsgCtx == null || inMsgCtx.getEnvelope() == null || inMsgCtx.getEnvelope().getBody() == null) {
            return null;
        }
        Vector<OMElementImpl> vec = new Vector<OMElementImpl>();
        SOAPEnvelope response = inMsgCtx.getEnvelope();
        SOAPBody body = response.getBody();
        QName objectQName = null;
        if (no != null) {
            objectQName = no.getObjectName();
        } else if (co != null) {
            objectQName = co.getObjectName();

        }
        //Search first mesa sta children tou body
        Iterator iter1 = body.getChildren();
        while (iter1.hasNext()) {
            org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1.next();
            Iterator it2 = childOMElement.getChildren();
            while (it2.hasNext()) {
                OMElementImpl childOMElement2 = (OMElementImpl) it2.next();
                if (childOMElement2.getQName().getLocalPart().equals(objectQName.getLocalPart())) {
                    return childOMElement2.getChildElements();
                }
            }



        }
        return null;
    }

    private static Iterator<OMElementImpl> findTheArrayObjectNodesInOMElement(OMElementImpl omElement, NativeObject no, ComplexObject co) {

        //  Vector<OMElementImpl> vec = new Vector<OMElementImpl>();

        QName objectQName = null;
        if (no != null) {
            objectQName = no.getObjectName();
        } else if (co != null) {
            objectQName = co.getObjectName();
        }
        Iterator iter1 = omElement.getChildrenWithName(objectQName);
        while (iter1.hasNext()) {
            org.apache.axiom.om.impl.llom.OMElementImpl childOMElement = (OMElementImpl) iter1.next();

            return childOMElement.getChildElements();
        }
        return null;
    }

    public static Vector parseInvocationOutput(MessageContext inMsgCtx, WSOperation theParsedOperation) {
        Vector result = new Vector();
        Vector parsedOperationOutputsVector = theParsedOperation.getHasOutput().getHasNativeOrComplexObjects();
        SOAPEnvelope response = inMsgCtx.getEnvelope();
        if (response != null) {
            //     System.out.println(response);
            //    System.out.println();
            if (response.getBody() != null) {
                SOAPBody body = response.getBody();
                if (body.getChildren() != null) {
                    Iterator iter1 = body.getChildren();
                    while (iter1.hasNext()) {
                        Object childObj = iter1.next();
                        if (childObj.getClass().getName().contains("org.apache.axiom.om.impl.llom.OMElementImpl")) {
                            org.apache.axiom.om.impl.llom.OMElementImpl elem1 = (org.apache.axiom.om.impl.llom.OMElementImpl) childObj;
                            System.out.println(elem1.getLocalName());
                            if (elem1.getLocalName().endsWith("Response")) {
                                Iterator iter2 = elem1.getChildren();
                                while (iter2.hasNext()) {
                                    org.apache.axiom.om.impl.llom.OMElementImpl elem2 =
                                            (org.apache.axiom.om.impl.llom.OMElementImpl) iter2.next();
                                    System.out.println("### " + elem2.getLocalName());
                                    System.out.println(elem2.getText());
                                    Iterator parsedOperationOutputsIter = parsedOperationOutputsVector.iterator();
                                    while (parsedOperationOutputsIter.hasNext()) {
                                        Object parsedOutObj = parsedOperationOutputsIter.next();
                                        System.out.println(parsedOutObj.getClass().getName());
                                        if (parsedOutObj.getClass().getName().contains("NativeObject")) {
                                            NativeObject no1 = (NativeObject) parsedOutObj;
                                            if (no1.getObjectName().getLocalPart().equals(elem2.getLocalName())) {
                                                no1.setHasValue(elem2.getText());
                                                result.add(no1);
                                                return result;
                                            }
                                        } else {
                                            System.out.println("ERROR! CANNOT parse ComplexType output yet...");
                                        }
                                    }
                                }
                            } else {
                                System.out.println("ERROR @ parsing output 2!!!");
                            }

                        } else {
                            System.out.println("ERROR @ parsing output 1!!!");
                        }

                    }
                }
            }
        }

        return result;

    }
}
