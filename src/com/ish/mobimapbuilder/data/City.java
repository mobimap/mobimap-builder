/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import com.ish.mobimapbuilder.model.*;
import com.ish.mobimapbuilder.xml.*;
import org.w3c.dom.*;

public class City {
    public static final String CS_GEO = "geo";
    public static final String CS_METRIC = "metric";
    public static final String CS_BOUNDS = "bounds";

    private String name;
    private Rectangle2 bounds;

    public City () {
    }

    public void load (Node cityNode) {
        NamedNodeMap attribs = cityNode.getAttributes ();
        if (attribs == null) {
            throw new RuntimeException ("name is required attribute for <city>");
        }

        name = XMLUtil.getAttributeValue (attribs, "name");
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setBounds (Rectangle2 bounds) {
        this.bounds = bounds;
    }

    public String getName () {
        return name;
    }

    public Rectangle2 getBounds () {
        return bounds;
    }

}
