/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2007-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import java.util.*;

public class CrossroadRecord extends AbstractRecord {
    public int x, y;
    public List<Connection> con;

    public CrossroadRecord (int x, int y) {
        this.x = x;
        this.y = y;
        con = new Vector<Connection>(4, 4);
    }

    public void breakLink (int neigh) {
        for (int i = 0; i < con.size (); i++) {
            if (con.get (i).pc == neigh) {
                con.remove (i);
                break;
            }
        }
    }

    public int getDirection (int neigh) {
        int res = 0;
        for (int i = 0; i < con.size (); i++) {
            if (con.get (i).pc == neigh) {
                res = con.get (i).direction;
                break;
            }
        }
        return res;
    }

    public int getType (int neigh) {
        int res = 0;
        for (int i = 0; i < con.size (); i++) {
            if (con.get (i).pc == neigh) {
                res = con.get (i).type;
                break;
            }
        }
        return res;
    }

    public void addLink (int neigh, int street, int direction, int type) {
        boolean is = true;
        for (int i = 0; i < con.size (); i++) {
            if (con.get (i).pc == neigh) {
                is = false;
                break;
            }
        }

        if (is) {
            Connection c = new Connection (neigh, street, direction, type);
            con.add (c);
        }
    }

    public String toString () {
        String s = "[id: " + id + ", x: " + x + ", y: " + y;
        if (con.size () > 0) {
            s = s + " { ";
            for (Connection c : con) {
                s += "(" + c.pc + ", " + c.street + ")";
            }
        }
        s += "]";
        return s;
    }
}
