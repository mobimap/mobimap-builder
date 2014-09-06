/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import com.ish.isrt.core.data.*;
import java.util.*;

public class IconRecord
    extends AbstractRecord
{
    public static final int PHOTO_IMAGE=1, PHOTO_ICON=2;

    //public int id;
    public String file;
	public int type;
    public List<tref> places;

    public IconRecord ()
    {
        places = new Vector<tref>();
    }
}
