/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

public class Pen {

    public static final Pen DEFAULT = new Pen(1, 0x000000);

    private int width;
    private int color;

    public Pen (int width, int color) {
        this.width = width;
        this.color = color;
    }

    public void setWidth (int width) {
        this.width = width;
    }

    public void setColor (int color) {
        this.color = color;
    }

    public int getWidth () {
        return width;
    }

    public int getColor () {
        return color;
    }
}
