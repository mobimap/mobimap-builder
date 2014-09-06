//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.awt.Color;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.Attributes;
import com.ish.isrt.core.data.DataXmlConst;
import com.ish.isrt.core.data.Reference;

public class XMLUtil
{
    private XMLUtil ()
    {
    }

    public static String getAttributeValue (NamedNodeMap attribs, String attributeName)
        throws DOMException
    {
        String value = null;
        if (attribs.getNamedItem (attributeName) != null)
            value = attribs.getNamedItem (attributeName).getNodeValue ();
        return value;
    }

    /**
     * Get attribute
     * @param attributes Attributes
     * @param attrName String
     * @return String
     */
    public static String getAttribute (Attributes attributes, String attrName)
    {
        return attributes.getValue (attrName);
    }

    /**
     * Get attribute as integer
     * @param attributes Attributes
     * @param attrName String
     * @return int
     */
    public static int getAttributeAsInt (Attributes attributes, String attrName)
    {
        int res = 0;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Integer.parseInt (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as integer. If attribute is null return defValue
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue int
     * @return int
     * @throws DOMException
     */
    public static int getAttributeAsInt (NamedNodeMap attribs, String attributeName, int defValue)
        throws DOMException
    {
        String v = getAttributeValue(attribs, attributeName);
        int result = defValue;

        if (v != null)
        {
            try {
                result = Integer.parseInt (v);
            }
            catch (NumberFormatException ex) {
            }
        }
        return result;
    }

    /**
     * Get attribute as double
     * @param attributes Attributes
     * @param attrName String
     * @return double
     */
    public static double getAttributeAsDouble (Attributes attributes, String attrName)
    {
        double res = 0;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Double.parseDouble (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as double. If attribute is null return defValue
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue double
     * @return double
     * @throws DOMException
     */
    public static double getAttributeAsDouble (NamedNodeMap attribs, String attributeName, double defValue)
        throws DOMException
    {
        String v = getAttributeValue(attribs, attributeName);
        double result = defValue;

        if (v != null)
        {
            try {
                result = Double.parseDouble (v);
            }
            catch (NumberFormatException ex) {
            }
        }
        return result;
    }

    /**
     * Get attribute as reference
     * @param attributes Attributes
     * @param attrName String
     * @return Reference
     */
    public static Reference getAttributeAsRef (Attributes attributes, String attrName)
    {
        Reference res = null;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            int n = s.indexOf (DataXmlConst.SEPARATOR);
            if (n > 0)
            {
                try
                {
                    int classid = Integer.parseInt (s.substring (0, n));
                    int objectid = Integer.parseInt (s.substring (n + 1));
                    res = new Reference (classid, objectid);
                }
                catch (NumberFormatException ex)
                {
                }
            }
        }
        return res;
    }

    /**
     * Get value of node's attrinute converted to Color
     * @param attributes Attributes
     * @param attrName String
     * @return int
     */
    public static Color getAttributeAsColor (Attributes attributes, String attrName)
    {
        Color res = Color.GRAY;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = new Color (Integer.parseInt (s, 16));
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as boolean
     * @param attributes Attributes
     * @param attrName String
     * @return boolean
     */
    public static boolean getAttributeAsBoolean (Attributes attributes, String attrName)
    {
        boolean res = false;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Boolean.parseBoolean (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as boolean. If attribute is empty return defValue
     * @param attributes Attributes
     * @param attrName String
     * @param defValue boolean
     * @return boolean
     */
    public static boolean getAttributeAsBoolean (Attributes attributes, String attrName, boolean defValue)
    {
        boolean res = defValue;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Boolean.parseBoolean (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Parse string value as boolean. Return default value if source
     * string is null.
     * @param value String
     * @param defValue boolean
     * @return boolean
     */
    public static boolean getAsBoolean(String value, boolean defValue)
    {
        boolean result = defValue;
        if (value != null)
        {
            result = Boolean.parseBoolean(value);
        }
        return result;
    }

    /**
     * Parse string value as integer number. Return default value if source
     * string is null.
     * @param value String
     * @param defValue int
     * @return int
     */
    public static int getAsInt(String value, int defValue)
    {
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

    /**
     * Parse string value as double number. Return default value if source
     * string is null.
     * @param value String
     * @param defValue double
     * @return double
     */
    public static double getAsDouble(String value, double defValue)
    {
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

    /**
     * Parse string value as float number. Return default value if source
     * string is null.
     * @param value String
     * @param defValue float
     * @return float
     */
    public static float getAsFloat(String value, float defValue)
    {
        float result = defValue;
        if (value != null)
        {
            try
            {
                result = Float.parseFloat (value);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return result;
    }

    /**
     * Parse string value as Color. Return default value if source
     * string is null.
     * @param value String
     * @param defValue int - color value in RGB
     * @return Color
     */
    public static Color getAsColor(String value, int defValue)
    {
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

    /**
     * Parse string value as Color. Return default value if source
     * string is null.
     * @param value String
     * @param defValue Color
     * @return Color
     */
    public static Color getAsColor(String value, Color defValue)
    {
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
