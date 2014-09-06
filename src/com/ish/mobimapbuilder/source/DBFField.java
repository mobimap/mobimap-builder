/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import com.ish.mobimapbuilder.model.Field;

public class DBFField extends Field {

    private int size;

    public DBFField (int index, String name, int type, int size) {
        super (index, name, type);
        this.size = size;
    }

    public int getSize () {
        return size;
    }
}
