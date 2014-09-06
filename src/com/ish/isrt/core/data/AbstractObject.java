//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

abstract public class AbstractObject
{
    /**
     * Local object id.
     */
    public int id = 0;

    /**
     * Global object id. Used for accessing objects from DB-server
     */
    public int uid = 0;

    public AbstractObject ()
    {}

    public AbstractObject (int id, int uid)
    {
        this.id = id;
        this.uid = uid;
    }

    abstract public Reference self();

    public int getId ()
    {
        return id;
    }

    public int getUid ()
    {
        return uid;
    }

    public void setId (int id)
    {
        this.id = id;
    }

    public void setUid (int uid)
    {
        this.uid = uid;
    }
}
