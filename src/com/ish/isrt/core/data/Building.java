//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.Serializable;

public class Building
    extends AbstractObject implements Serializable
{
    public int x, y;
    public String name;
    public int icon;
    public int pc1, pc2;
    transient public String fullName;

    public Building ()
    {

    }

    public Building (int id, int uid, int x, int y, String name)
    {
        super (id, uid);
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public Building (Building src)
    {
        x = src.x;
        y = src.y;
        name = src.name;
        id = src.id;
        uid = src.uid;
    }

    public Reference self()
    {
        return new Reference(DataType.CLASS_BUILDING, getId());
    }

    public String toString ()
    {
        return (fullName == null) ? "" : fullName;
    }

    public int getX ()
    {
        return x;
    }

    public int getY ()
    {
        return y;
    }

    public String getName ()
    {
        return name;
    }

    public String getFullName ()
    {
        return fullName;
    }

    public void setY (int y)
    {
        this.y = y;
    }

    public void setX (int x)
    {
        this.x = x;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public void setFullName (String fullName)
    {
        this.fullName = fullName;
    }

};
