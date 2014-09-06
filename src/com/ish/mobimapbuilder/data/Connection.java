/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

public class Connection
{
    public int pc;
    public int street;
    public int direction;
    public int type;

    public Connection () {pc=street=direction=type=0; }
    public Connection (int pc, int street, int direction, int type)
    {
        this. pc = pc;
        this. street = street;
        this. direction = direction;
        this. type = type;
    }
};
