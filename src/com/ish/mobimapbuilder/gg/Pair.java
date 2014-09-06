/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2007-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.gg;

class Pair {
    public int first;
    public int second;

    public Pair (int first, int second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals (Object otherObject) {
        Pair other = (Pair) otherObject;
        return other.first == first && other.second == second;
    }

    public int hashCode () {
        return first * 112237 + second;
    }
}
