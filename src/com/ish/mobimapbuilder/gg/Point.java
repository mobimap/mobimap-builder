/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2007-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.gg;

class Point implements CellParameters {
    public int x;
    public int y;

    public Point (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals (Object other) {
        if (!(other instanceof Point)) {
            return false;
        }
        return hashCode () == other.hashCode ();
    }

    public int hashCode () {
        int a = x / CELL_SIZE;
        int b = y / CELL_SIZE;

//        if (a < 0) {
//            a = 0;
//        }
//        if (b < 0) {
//            b = 0;
//        }
        return (a << 16) | b;
    }
}
