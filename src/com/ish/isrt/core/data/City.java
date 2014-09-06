//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

public class City
{
    private String name;
    private int width;
    private int height;
    private double west;
    private double east;
    private double south;
    private double north;
    private boolean geo;

    public City ()
    {
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public void setWidth (int width)
    {
        this.width = width;
    }

    public void setHeight (int height)
    {
        this.height = height;
    }

    public void setWest (double west)
    {
        this.west = west;
    }

    public void setEast (double east)
    {
        this.east = east;
    }

    public void setSouth (double south)
    {
        this.south = south;
    }

    public void setNorth (double north)
    {
        this.north = north;
    }

    public void setGeo (boolean geo)
    {
        this.geo = geo;
    }

    public String getName ()
    {
        return name;
    }

    public int getWidth ()
    {
        return width;
    }

    public int getHeight ()
    {
        return height;
    }

    public double getWest ()
    {
        return west;
    }

    public double getEast ()
    {
        return east;
    }

    public double getSouth ()
    {
        return south;
    }

    public double getNorth ()
    {
        return north;
    }

    public boolean isGeo ()
    {
        return geo;
    }
}
