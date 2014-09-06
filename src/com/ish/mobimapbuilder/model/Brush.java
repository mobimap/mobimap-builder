/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

public class Brush {

    public static final Brush DEFAULT = new Brush(0xffffff);

    private int foreColor;

    /**
     * New brush with specified color
     * @param foreColor int
     */
    public Brush (int foreColor) {
        this.foreColor = foreColor;
    }

    /**
     * Get brush color
     * @return int
     */
    public int getColor () {
        return foreColor;
    }
}
