/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.xml;

public class XMLAttribute {
    private String name;
    private String value;

    public XMLAttribute (String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setValue (String value) {
        this.value = value;
    }

    public String getName () {
        return name;
    }

    public String getValue () {
        return value;
    }
}
