/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import java.awt.Color;
import java.util.Map;

public class CategoryRecord
    extends AbstractRecord
{
    public static final int CATEGORY_ROOT=1;
    public static final int CATEGORY_HOUSE=2;
    public static final int CATEGORY_STREET=3;
    public static final int CATEGORY_SEARCH=4;
    public static final int CATEGORY_MOBILE=5;
    public static final int CATEGORY_ONLINE=64;

    public String name = "";
    public String icon = null;
    public int parent;
    public Color color = Color.GRAY;
    public Map<String,String> attributes;

    public CategoryRecord()
    {
    }
}
