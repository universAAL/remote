/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.universAAL.ri.wsdlToolkit.parser;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.logging.Level;


public class StdOutErrLevel extends Level {

    /**
     * Private constructor
     */
    private StdOutErrLevel(String name, int value) {
        super(name, value);
    }
    /**
     * Level for STDOUT activity.
     */
    public static Level STDOUT =
        new StdOutErrLevel("STDOUT", Level.INFO.intValue()+53);
    /**
     * Level for STDERR activity
     */
    public static Level STDERR =
        new StdOutErrLevel("STDERR", Level.INFO.intValue()+54);

    /**
     * Method to avoid creating duplicate instances when deserializing the
     * object.
     * @return the singleton instance of this <code>Level</code> value in this
     * classloader
     * @throws ObjectStreamException If unable to deserialize
     */
    protected Object readResolve()
        throws ObjectStreamException {
        if (this.intValue() == STDOUT.intValue())
            return STDOUT;
//        if (this.intValue() == STDERR.intValue())
//            return STDERR;
        throw new InvalidObjectException("Unknown instance :" + this);
    }

}
