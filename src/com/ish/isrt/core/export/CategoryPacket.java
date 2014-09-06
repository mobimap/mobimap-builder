//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.export;

import java.util.*;

import com.ish.isrt.core.data.Category;
import com.ish.isrt.core.data.Icon;

public class CategoryPacket
{
    public static final String NAME = "Categories";

    private List<Category> categories;

    private static final char FIELD_CATEGORY_ID = 'i';
    private static final char FIELD_CATEGORY_PARENT = 'P';
    private static final char FIELD_CATEGORY_COLOR = 'C';
    private static final char FIELD_CATEGORY_NAME = 'N';
    private static final char FIELD_CATEGORY_ICON = 'I';
    private static final char FIELD_CATEGORY_FONTSTYLE = 'F';
    private static final char FIELD_CATEGORY_IS_ONLINE = 'O';
    private static final char FIELD_CATEGORY_STARTUP_VISIBILITY = 'V';
    private static final char FIELD_CATEGORY_SHOW = 'S';

    private static final int CATEGORY_SHOW_ICON = 1;
    private static final int CATEGORY_SHOW_LIST = 2;
    private static final int CATEGORY_SHOW_LABEL = 4;
    private static final int CATEGORY_SHOW_DOT = 8;

    public CategoryPacket ()
    {
    }
    public CategoryPacket (List<Category> categories)
    {
        this.categories = categories;
    }

    public Envelope serialize()
    {
        int count = categories.size();
        Vector data = new Vector();

        for (int i=0; i < count; i++)
        {
            Hashtable record = new Hashtable();
            Category c = categories.get(i);
            record.put(FIELD_CATEGORY_ID, c.getId());
            record.put(FIELD_CATEGORY_PARENT, c.getParent());
            record.put(FIELD_CATEGORY_COLOR, c.getColor().getRGB());
            record.put(FIELD_CATEGORY_FONTSTYLE, c.getFontStyle());
            record.put(FIELD_CATEGORY_NAME, c.getName());
            record.put(FIELD_CATEGORY_IS_ONLINE, c.isOnline());

            int show = 0x7F; // all flags ON by default
            if (!c.isShowIcon()) show &= ~CATEGORY_SHOW_ICON;
            if (!c.isShowList()) show &= ~CATEGORY_SHOW_LIST;
            if (!c.isShowLabel()) show &= ~CATEGORY_SHOW_LABEL;
            if (!c.isShowDot()) show &= ~CATEGORY_SHOW_DOT;
            record.put(FIELD_CATEGORY_SHOW, new Byte((byte)show));

            String startupVisibility = c.getAttribute("startupVisibility");
            if (startupVisibility != null)
            {
                record.put(FIELD_CATEGORY_STARTUP_VISIBILITY,
                           "true".equals(startupVisibility)? Boolean.TRUE: Boolean.FALSE);
            }

            Icon icon = c.getIcon();
            if (icon != null)
            {
                byte[] iconData = icon.getIconData();
                if (iconData != null)
                {
                    record.put(FIELD_CATEGORY_ICON, iconData);
                }
            }

            data.add(record);
        }

        Envelope container = new Envelope();
        container.addAttribute(Envelope.ATTRIBUTE_TYPE, NAME);
        container.setData(data);

        return container;
    }

    public void setCategories (List<Category> categories)
    {
        this.categories = categories;
    }

}
