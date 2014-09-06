/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

import java.sql.Types;

public class Field {

    public static final int TYPE_NULL = Types.NULL;
    public static final int TYPE_STRING = Types.CHAR;
    public static final int TYPE_DOUBLE = Types.DECIMAL;
    public static final int TYPE_INTEGER = Types.INTEGER;

    private String name;
    private int type;
    private int index;

    public Field (int index, String name, int type) {
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public int getType () {
        return type;
    }

    public String getName () {
        return name;
    }

    public void setIndex (int index) {
        this.index = index;
    }

    public void setType (int type) {
        this.type = type;
    }

    public void setName (String name) {
        this.name = name;
    }

    public int getIndex () {
        return index;
    }

    private String describeType() {
        String s = null;
        switch (type) {
            case TYPE_NULL:
                s = "null";
                break;
            case TYPE_INTEGER:
                s = "integer";
                break;
            case TYPE_DOUBLE:
                s = "double";
                break;
            case TYPE_STRING:
                s = "string";
                break;
        }
        return s;
    }

    public String toString() {
        return "[index: " + index + ", name: " + name + ", type: " + describeType() + "]";
    }
}
