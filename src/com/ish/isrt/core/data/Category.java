//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.Serializable;

import java.awt.Color;
import javax.swing.ImageIcon;

public class Category
    extends AbstractObject
    implements Serializable, Comparable
{
    public static final int CATEGORY_ROOT = 0;
    public static final int CATEGORY_STREET = 3;
    public static final int CATEGORY_SEARCH = 4;
    public static final int CATEGORY_FIRST_AVAILABLE = 7;

    private Icon icon;
    private ImageIcon imageIcon;
    private boolean online;

    private String name;
    private int parent;
    private Color color;
    public boolean visibility = true;

    private String fontName;
    private int fontSize;
    private int fontStyle;

    private int pointSize;

    private String label;
    private boolean downloaded;
    private java.util.Map<String,String> attributes;
    private boolean showIcon = true;
    private boolean showList = true;
    private boolean showLabel = true;
    private boolean showDot;

    public Reference self()
    {
        return new Reference(DataType.CLASS_CATEGORY, getId());
    }

    public int compareTo (Object c)
    {
        return name.compareTo(((Category)c).name);
    }

    public int getFontStyle ()
    {
        return fontStyle;
    }

    public String getName ()
    {
        return name;
    }

    public Color getColor ()
    {
        return color;
    }

    public int getParent ()
    {
        return parent;
    }

    public boolean isOnline ()
    {
        return online;
    }

    public int getFontSize ()
    {
        return fontSize;
    }

    public String getFontName ()
    {
        return fontName;
    }

    public int getPointSize ()
    {
        return pointSize;
    }

    public String getLabel ()
    {
        return label;
    }

    public boolean isDownloaded ()
    {
        return downloaded;
    }

    public Icon getIcon ()
    {
        return icon;
    }

    public ImageIcon getImageIcon ()
    {
        return imageIcon;
    }

    public java.util.Map getAttributes ()
    {
        return attributes;
    }

    public boolean isShowIcon ()
    {
        return showIcon;
    }

    public boolean isShowList ()
    {
        return showList;
    }

    public boolean isShowLabel ()
    {
        return showLabel;
    }

    public boolean isShowDot ()
    {
        return showDot;
    }

    public void setFontStyle (int fontStyle)
    {
        this.fontStyle = fontStyle;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public void setColor (Color color)
    {
        this.color = color;
    }

    public void setParent (int parent)
    {
        this.parent = parent;
    }

    public void setOnline (boolean online)
    {
        this.online = online;
    }

    public void setFontName (String fontName)
    {
        this.fontName = fontName;
    }

    public void setFontSize (int fontSize)
    {
        this.fontSize = fontSize;
    }

    public void setPointSize (int pointSize)
    {
        this.pointSize = pointSize;
    }

    public void setLabel (String label)
    {
        this.label = label;
    }

    public void setDownloaded (boolean downloaded)
    {
        this.downloaded = downloaded;
    }

    public void setIcon (Icon icon)
    {
        this.icon = icon;
    }

    public void setImageIcon (ImageIcon imageIcon)
    {
        this.imageIcon = imageIcon;
    }

    public void setAttributes (java.util.Map attributes)
    {
        this.attributes = attributes;
    }

    public void setShowIcon (boolean showIcon)
    {
        this.showIcon = showIcon;
    }

    public void setShowList (boolean showList)
    {
        this.showList = showList;
    }

    public void setShowLabel (boolean showLabel)
    {
        this.showLabel = showLabel;
    }

    public void setShowDot (boolean showDot)
    {
        this.showDot = showDot;
    }

    public String getAttribute(String atName)
    {
        return attributes.get(atName);
    }

}
