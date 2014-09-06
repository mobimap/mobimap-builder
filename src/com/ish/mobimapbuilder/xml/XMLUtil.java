/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.xml;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;

public class XMLUtil {
    /**
     * Get string value of specified attribute. Return null if attribute isn't defined.
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @return String
     * @throws DOMException
     */
    public static String getAttributeValue (NamedNodeMap attribs, String attributeName) throws DOMException {
        String value = null;
        if (attribs.getNamedItem (attributeName) != null) {
            value = attribs.getNamedItem (attributeName).getNodeValue ();
        }
        return value;
    }

    /**
     * Get string value of specified attribute. If attribute isn't defined return defValue.
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue String
     * @return String
     * @throws DOMException
     */
    public static String getAttributeValue (NamedNodeMap attribs, String attributeName, String defValue) throws
        DOMException {
        if (attribs.getNamedItem (attributeName) != null) {
            return attribs.getNamedItem (attributeName).getNodeValue ();
        } else {
            return defValue;
        }
    }

    /**
     * Get value of specified attribute as double. If attribute isn't defined return defValue.
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue double
     * @return double
     * @throws DOMException
     */
    public static double getAttributeValueAsDouble (NamedNodeMap attribs, String attributeName, double defValue) throws
        DOMException {
        String v = getAttributeValue (attribs, attributeName);
        double result = defValue;

        if (v != null) {
            try {
                result = Double.parseDouble (v);
            } catch (NumberFormatException ex) {
            }
        }
        return result;
    }

    /**
     * Get value of specified attribute as integer. If attribute isn't defined return defValue.
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue int
     * @return int
     * @throws DOMException
     */
    public static int getAttributeValueAsInt (NamedNodeMap attribs, String attributeName, int defValue) throws
        DOMException {
        String v = getAttributeValue (attribs, attributeName);
        int result = defValue;

        if (v != null) {
            try {
                result = Integer.parseInt (v);
            } catch (NumberFormatException ex) {
            }
        }
        return result;
    }

    /**
     * Get value of specified attribute as color. If attribute isn't defined return defValue.
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue int
     * @return int
     * @throws DOMException
     */
    public static int getAttributeValueAsColor (NamedNodeMap attribs, String attributeName, int defValue) throws
        DOMException {
        String v = getAttributeValue (attribs, attributeName);
        int result = defValue;

        if (v != null) {
            try {
                result = Integer.parseInt (v, 16);
            } catch (NumberFormatException ex) {
            }
        }
        return result;
    }

    /**
     * Get value of specified attribute as boolean. If attribute isn't defined return defValue.
     * @param attribs NamedNodeMap
     * @param attributeName String
     * @param defValue boolean
     * @return boolean
     * @throws DOMException
     */
    public static boolean getAttributeValueAsBoolean (NamedNodeMap attribs, String attributeName, boolean defValue) throws
        DOMException {
        String v = getAttributeValue (attribs, attributeName);
        boolean result = defValue;

        if (v != null) {
            try {
                result = "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
            } catch (NumberFormatException ex) {
            }
        }
        return result;
    }
}
