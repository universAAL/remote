/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.ioApi;

import javax.xml.namespace.QName;


public class AlignmentInformation {
    private QName[] hasParserPath;
    private QName[] hasOntologicalPath;


    /**
     * Returs an array of QName objects containing the full ontological hierachical path from the WS to current object
     * @return An array of QName objects containing the full ontological hierachical path from the WS to current object
     */
    public QName[] getHasOntologicalPath() {
        return hasOntologicalPath;
    }

      /**
     * Sets the full ontological hierachical path from the WS to current object
     * @param hasOntologicalPath An array of QName objects containing full ontological hierachical path from the WS to current object
     */
    public void setHasOntologicalPath(QName[] hasOntologicalPath) {
        this.hasOntologicalPath = hasOntologicalPath;
    }

   /**
     * Returs an array of QName objects containing the full parser hierachical path from the WS to current object
     * @return An array of QName objects containing the full parser hierachical path from the WS to current object
     */
    public QName[] getHasParserPath() {
        return hasParserPath;
    }

      /**
     * Sets the full parser hierachical path from the WS to current object
     * @param hasParserPath An array of QName objects containing full parser hierachical path from the WS to current object
     */
    public void setHasParserPath(QName[] hasParserPath) {
        this.hasParserPath = hasParserPath;
    }

}
