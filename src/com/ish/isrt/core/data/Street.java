//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.Serializable;

public class Street
    extends AbstractObject
    implements Serializable
{
    private String name;

    transient public java.util.Vector pc;

    public java.util.Vector element;
    public int elementN;

    private String fullName;
    private String village;
    private String type;

    public Street ()
    {
        element = new java.util.Vector (2, 5);
    }

    public Street (int id, int uid, String name, String type, String village)
    {
        super (id, uid);
        element = new java.util.Vector (2, 5);
        this.name = name;
        this.type = type;
        this.village = village;
    }

    public Reference self()
    {
        return new Reference(DataType.CLASS_STREET, getId());
    }

    public String toString ()
    {
        return fullName;
    }

    public String getName ()
    {
        return name;
    }

    public void setFullName (String fullName)
    {
        this.fullName = fullName;
    }

    public void setVillage (String village)
    {
        this.village = village;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String getFullName ()
    {
        return fullName;
    }

    public String getVillage ()
    {
        return village;
    }

    public String getType ()
    {
        return type;
    }
}
