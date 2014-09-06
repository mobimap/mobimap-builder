/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.util;

/**
 * String operations
 */
public class StringUtils {

    public final static String EMPTY_STRING = "";

    /**
     * Get file extension. Returns null if file name has no '.' (dot) character.
     * @param name String
     * @return String
     */
    public static String extractFileExtension(String name) {
        if (isNullOrEmpty(name)) {
            throw new IllegalArgumentException("getFileExtension requires non-empty parameter");
        }
        int pos = name.lastIndexOf('.');
        if (pos < 0) {
            return null;
        }
        return name.substring(pos + 1);
    }

    /**
     * Check if specified string is null or empty
     */
    public static boolean isNullOrEmpty (String str) {
        return ((str == null) || (str.trim ().length () < 1));
    }

    /**
     * Check if specified string is null or empty or contains integer zero
     * @param str String
     * @return boolean
     */
    public static boolean isNullOrEmptyOrZero (String str) {
        boolean r = isNullOrEmpty (str);
        if (!r) {
            int i = 0;
            boolean isNumeric = false;
            try {
                i = Integer.parseInt (str);
                isNumeric = true;
            } catch (NumberFormatException ex) {
            }
            r = isNumeric && (i == 0);
        }
        return r;
    }
}
