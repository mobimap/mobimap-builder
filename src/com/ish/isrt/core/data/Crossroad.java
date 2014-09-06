//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.*;

public class Crossroad
    extends AbstractObject implements Serializable
{
    public static final int CAPACITY_PC = 500; // размер блока памяти, выделяемого под остановки
    public static final int LIMIT_PC_CON = 500; // лимит на кол-во связей при загрузке из файла
    public static final int INITIAL_STOP_CON = 10; // начальное кол-во связей для остановок

    public int x, y;
    public ConBlock con[];
    public int conN;
    public int distance; // real distance between pc and start
    public int parent; // where I'm from
    public int parentCon;

    private final int capacityCon = 4;

    public Crossroad ()
    {}

    public Crossroad (int id, int uid, int x, int y, int n)
    {
        super (id, uid);
        this.x = x;
        this.y = y;
        this.con = new ConBlock[n];
        this.conN = n;
    }

    public Reference self()
    {
        return new Reference(DataType.CLASS_CROSSROAD, getId());
    }

    public void realloc ()
    {
        if (conN >= con.length)
        {
            ConBlock temp[] = new ConBlock[con.length + capacityCon];
            System.arraycopy (con, 0, temp, 0, con.length);
            con = temp;
        }
    }

    public int pc;

    transient public String fullName;

    public String toString ()
    {
        return (fullName == null) ? "" : fullName;
    }
}
