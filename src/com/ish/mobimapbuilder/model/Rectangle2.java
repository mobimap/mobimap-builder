/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

/**
 * Represents a rectangle which coordinates are float values
 */
public class Rectangle2 {

    /**
     * <code>x, y, x2, y2</code> - coordinates of rectangle corners.
     * <code>x</code> should always be less than <code>x2</code>,
     * <code>y</code> should always be less than <code>y2</code>,
     */
    public double x;
    public double y;
    public double x2;
    public double y2;

    /**
     * Creates uninitialized FloatRectangle object
     */
    public Rectangle2 () {
    }

    /**
     * Creates copy of <code>rec</code> rectangle.
     * @param rec rectangle to copy
     */
    public Rectangle2 (Rectangle2 rec) {
        setBounds (rec.x, rec.y, rec.x2, rec.y2);
    }

    /**
     * Creates rectangle based on topleft and bottomright coordinates
     * @param rec rectangle to copy
     */
    public Rectangle2 (Point2 tl, Point2 br) {
        setBounds (tl.x, br.y, br.x, tl.y);
    }

    /**
     * Constructs FloatRectangle object and initializes it with coordinate
     * values.
     * @param x1 X coordinate of point 1
     * @param y1 Y coordinate of point 1
     * @param x2 X coordinate of point 2
     * @param y2 Y coordinate of point 2
     */
    public Rectangle2 (double x1, double y1, double x2, double y2) {
        setBounds (x1, y1, x2, y2);
    }

    public Rectangle2 (boolean isvrat) {
        setBounds (Double.POSITIVE_INFINITY,
                   Double.POSITIVE_INFINITY,
                   Double.NEGATIVE_INFINITY,
                   Double.NEGATIVE_INFINITY);
    }

    /**
     * Modifies coordinates of this rectangle setting it to new values.
     * @param x1 new X coordinate of point 1
     * @param y1 new Y coordinate of point 1
     * @param x2 new X coordinate of point 2
     * @param y2 new Y coordinate of point 2
     */
    public void setBounds (double x1, double y1, double x2, double y2) {
        this.x = x1;
        this.y = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Determines whether this rectangle intersects with <code>r</code>
     * rectangle.
     * @param r FloatRectangle to check intersection with.
     * @return <code>true</code> if these rectangles intersect.
     */
    public boolean intersects (Rectangle2 r) {
        return!((r == null) || (r.x2 <= x) ||
                (r.y2 <= y) ||
                (r.x >= x2) ||
                (r.y >= y2));
    }

    /**
     * Determines whether this rectangle intersects with <code>r</code>
     * rectangle.
     * @param r FloatRectangle to check intersection with.
     * @return <code>true</code> if these rectangles intersect.
     */
    public boolean equals (Rectangle2 r) {
        return (!((r == null) ||
                  (r.x2 != x2) ||
                  (r.y2 != y2) ||
                  (r.x != x) ||
                  (r.y != y)));
    }

    /**
     * Calculates bounding rectangle of this rectangle and <code>r</code>
     * @param r FloatRectangle to find bounding rectangle
     * @return minimal FloatRectangle that contains both this
     * rectangle and <code>r</code>
     */
    public void union (Rectangle2 r) {
        if (r != null) {
            this.x = Math.min (this.x, r.x);
            this.x2 = Math.max (this.x2, r.x2);
            this.y = Math.min (this.y, r.y);
            this.y2 = Math.max (this.y2, r.y2);
        }
    }

}
