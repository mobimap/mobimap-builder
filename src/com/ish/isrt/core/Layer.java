//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.util.*;
import java.awt.Color;

public class Layer
{
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_ZOOM_MIN = "zoomMin";
    public static final String ATTRIBUTE_ZOOM_MAX = "zoomMax";
    public static final String ATTRIBUTE_VISIBILITY = "visibility";

    public static final String ATTRIBUTE_FILL = "fill";
    public static final String ATTRIBUTE_OUTLINE = "outline";

    public static final String ATTRIBUTE_FILE = "file";

    private HashMap<String,String> attributes;

    public Layer ()
    {
        attributes = new HashMap<String,String>();
    }

    public void setAttribute(String name, String value)
    {
        attributes.put(name, value);
    }

    public String getAttribute(String name)
    {
        return attributes.get(name);
    }

    public String getAttribute(String name, String defValue)
    {
        String value = attributes.get(name);
        return value == null? defValue : value;
    }

    public boolean getAttributeAsBoolean(String name, boolean defValue)
    {
        String value = attributes.get(name);

        boolean result = defValue;
        if (value != null)
        {
            result = Boolean.parseBoolean(value);
        }
        return result;
    }

    public int getAttributeAsInt(String name, int defValue)
    {
        String value = attributes.get(name);

        int result = defValue;
        if (value != null)
        {
            try
            {
                result = Integer.parseInt (value);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return result;
    }

    public double getAttributeAsDouble(String name, double defValue)
    {
        String value = attributes.get(name);

        double result = defValue;
        if (value != null)
        {
            try
            {
                result = Double.parseDouble (value);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return result;
    }

    public Color getAttributeAsColor(String name, int defValue)
    {
        String value = attributes.get(name);

        Color result = new Color(defValue);
        if (value != null)
        {
            try
            {
                result = new Color(Integer.parseInt (value, 16));
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return result;
    }

    public Color getAttributeAsColor(String name, Color defValue)
    {
        String value = attributes.get(name);

        Color result = defValue;
        if (value != null)
        {
            try
            {
                result = new Color(Integer.parseInt (value, 16));
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return result;
    }

}
