//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.Serializable;

public class Reference
    implements Serializable
{
    public int classid;
    public int objectid;

    public Reference ()
    {
    }

    public Reference (int classid, int objectid)
    {
        this.classid = classid;
        this.objectid = objectid;
    }

    public Reference (int i)
    {
        classid = i >> 24;
        objectid = i & 0xffffff;
    }

    public String toString ()
    {
        return classid + "/" + objectid;
    }

    public int toInt()
    {
        return (classid << 24) | objectid;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Reference)) return false;
        Reference r = (Reference)o;
        return classid == r.classid && objectid == r.objectid;
    }
}
