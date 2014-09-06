//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.*;

public class Route
    extends AbstractObject implements Serializable
{
    public static final int TRANSPORT_SPEED = 8;

    public String name;
    public String fullName;
    public int category;
    transient public int places[];
    transient public int placesN;
    public int pc[];
    public int pcN;

    public Reference self ()
    {
        return new Reference (DataType.CLASS_POI, getId ());
    }

    public String toString ()
    {
        return fullName;
    }
}
