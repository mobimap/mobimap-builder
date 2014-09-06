//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

public class Address
    extends Building
{
    private int streetId;

    public Address ()
    {
        super();
    }

    public Address (Building b, int streetId)
    {
        super(b);
        this.streetId = streetId;
    }

    public Address (int id, int uid, int x, int y, String name, int streetId)
    {
        this.id = id;
        this.uid = uid;
        this.x = x;
        this.y = y;
        this.name = name;
        this.streetId = streetId;
    }

    public Reference self()
    {
        return new Reference(DataType.CLASS_ADDRESS, getId());
    }

    public void setStreetId (int streetId)
    {
        this.streetId = streetId;
    }

    public int getStreetId ()
    {
        return streetId;
    }
}
