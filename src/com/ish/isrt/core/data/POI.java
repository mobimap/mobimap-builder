//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.util.*;

import javax.swing.*;

public class POI
    extends Building
{
    private int categoryId;
    private ImageIcon imageIcon;
    private Properties meta;

    public POI ()
    {
        super();
    }

    public POI (Building b, int categoryId, Properties meta)
    {
        super(b);
        this.categoryId = categoryId;
        this.meta = meta;
    }

    public void setCategoryId (int categoryId)
    {
        this.categoryId = categoryId;
    }

    public void setImageIcon (ImageIcon imageIcon)
    {

        this.imageIcon = imageIcon;
    }

	public void setMeta (Properties meta)
	{
		this.meta = meta;
	}

	public int getCategoryId ()
    {
        return categoryId;
    }

    public ImageIcon getImageIcon ()
    {

        return imageIcon;
    }

	public Properties getMeta ()
	{
		return meta;
	}

	public Reference self()
    {
        return new Reference(DataType.CLASS_POI, getId());
    }
}
