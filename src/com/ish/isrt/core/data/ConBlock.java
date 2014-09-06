//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.*;

public class ConBlock
    implements Serializable
{
    public int pc;
    public int street;
    public int direction;
    public int type;
    public boolean trans;

    public ConBlock ()
    {
        pc = street = direction = 0;
        trans = false;
    }

    public ConBlock (int pc, int street, int direction)
    {
        this.pc = pc;
        this.street = street;
        this.direction = direction;
    }

    public int weight;
    public int transport;
};
