//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.export;

import java.util.*;

/**
 * Envelope is a component that combines packet and packet attributes
 */
public class Envelope
{
    public static final String ATTRIBUTE_TYPE = "type";

    private Hashtable<String,Object> attributes;
    private Object data;

    public Envelope ()
    {
        attributes = new Hashtable<String,Object>();
    }

    public Hashtable getAttributes ()
    {
        return attributes;
    }

    public Object getData ()
    {
        return data;
    }

    public void setAttributes (Hashtable attributes)
    {
        this.attributes = attributes;
    }

    public void setData (Object data)
    {
        this.data = data;
    }

    public void addAttribute (String attribute, Object value)
    {
        attributes.put(attribute, value);
    }

    public Object serialize()
    {
        Vector v = new Vector();
        v.add(getAttributes());
        v.add(getData());

        return v;
    }
}
