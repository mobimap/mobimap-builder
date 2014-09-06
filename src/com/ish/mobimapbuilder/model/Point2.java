/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

/**
 * A point representing a location in (x, y) coordinate space,
 * specified in double precision.
 */
public class Point2 {

    /**
     * The <code>x</code> coordinate.
     */
    public double x;

    /**
     * The <code>y</code> coordinate.
     */
    public double y;

    /**
     * Constructs and initializes a point at the origin (0, 0) of the
     * coordinate space.
     */
    public Point2 () {
        this (0, 0);
    }

    /**
     * Constructs and initializes a point at the specified (x, y)
     * location in the coordinate space.
     *
     * @param x - the x coordinate
     * @param y - the y coordinate
     */
    public Point2 (double x, double y) {
        this.x = x;
        this.y = y;
    }
}
