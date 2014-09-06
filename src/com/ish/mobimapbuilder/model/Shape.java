/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

/**
 * Represents graphical objects: points, polylines of polygons.
 */
public class Shape {

    /**
     * Type of shape
     */
    public enum Type {
        POINT, POLYLINE, POLYGON
    }

    private Type type;

    /** Array of x (longitude) coordinates*/
    public double xCoords[] = null;
    /** Array of y (latitude) coordinates*/
    public double yCoords[] = null;

    /**
     * Create new shape of specified type consisting n points
     * @param type Type
     * @param n int
     */
    public Shape (Type type, int n) {
        this.type = type;
        xCoords = new double[n];
        yCoords = new double[n];
    }

    /**
     * Calculates the extent of this shape.
     * @return spatial extent of this shape.
     */
    public Rectangle2 calculateExtent () {
        if (xCoords == null || yCoords == null) {
            return null;
        }

        double maxX = xCoords[0], maxY = yCoords[0];
        double minX = maxX, minY = maxY;

        for (int i = 1; i < xCoords.length; i++) {

            if (maxX < xCoords[i]) {
                maxX = xCoords[i];
            } else if (minX > xCoords[i]) {
                minX = xCoords[i];
            }

            if (maxY < yCoords[i]) {
                maxY = yCoords[i];
            } else if (minY > yCoords[i]) {
                minY = yCoords[i];
            }
        }

        return new Rectangle2 (minX, minY, maxX, maxY);
    }

    /**
     * Sets coordinates of vertex
     * @param index zero-based index of vertex which coordinates will be set.
     * @param x  x-ccordinate.
     * @param y  y-ccordinate.
     */
    public void setVertex (int index, double x, double y) {
        if (index >= 0 && index < xCoords.length) {
            xCoords[index] = x;
            yCoords[index] = y;
        }
    }

    /**
     * Get vertex by index
     * @param index int
     * @return Point2
     */
    public Point2 getVertex (int index) {
        return new Point2 (xCoords[index], yCoords[index]);
    }

    /**
     * Get shape as list of vertices
     * @return Point2[]
     */
    public Point2[] getVertexes () {
        Point2[] result = new Point2[xCoords.length];
        for (int i=0; i < xCoords.length; i++) {
            result[i] = new Point2 (xCoords[i], yCoords[i]);
        }
        return result;
    }

    /**
     * Get x-coordinate of vertex by index
     * @param index int
     * @return double
     */
    public double getX (int index) {
        return xCoords[index];
    }

    /**
     * Get y-coordinate of vertex by index
     * @param index int
     * @return double
     */
    public double getY (int index) {
        return yCoords[index];
    }

    /**
     * Get x coordinates of all vertices
     * @return double[]
     */
    public double[] getXCoords () {
        return xCoords;
    }

    /**
     * Get y coordinates of all vertices
     * @return double[]
     */
    public double[] getYCoords () {
        return yCoords;
    }

    /**
     * Get type of shape
     * @return Type
     */
    public Type getType() {
        return type;
    }
}
