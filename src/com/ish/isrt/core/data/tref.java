//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

public class tref
{
    public int classid;
    public int objectid;

	public tref ()
	{
	}
    public tref (int classid, int objectid)
    {
        this.classid = classid;
        this.objectid = objectid;
    }
    public String toString()
    {
        return classid + "/" + objectid;
    }
}
