package org.apache.xerces.jaxp.datatype;

import java.io.Serializable;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */
public class SerializedXMLGregorianCalendarImpl implements Serializable {
    SerializedXMLGregorianCalendar serializedXMLGregorianCalendar;
    public SerializedXMLGregorianCalendarImpl(){
        this.serializedXMLGregorianCalendar = new SerializedXMLGregorianCalendar( "test" );
    }
}
