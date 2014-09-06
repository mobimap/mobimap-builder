/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

import java.util.ArrayList;
import java.util.List;

public class Record {

    /** Array of graphic shapes. */
    private List<Shape> shapes = null;

    /** Attribute data */
    private Object[] fields = null;

    /**
     * Pen (contour style)
     */
    private Brush brush;

    /**
     * Brush (fill style)
     */
    private Pen pen;

    /**
     * Creates new Record with a given number of fields.
     * @param fieldCount number of fields
     */
    public Record (int fieldCount) {
        fields = new Object[fieldCount];
        shapes = new ArrayList<Shape>();
    }

    /**
     * Gets a value of field based on a specified field index.
     *
     * @param index index of field, zero based
     * @return field value.
     */
    public Object getField (int index) {
        return (index < 0 || index >= fields.length)
            ? null : fields[index];
    }

    public int getFieldAsInt (int index) {
        int res = 0;
        Object obj = getField (index);
        if (obj != null) {
            if (obj instanceof Integer) {
                res = ((Integer) obj).intValue ();
            } else if (obj instanceof Double) {
                res = (int) ((Double) obj).doubleValue ();
            } else if (obj instanceof String) {
                String s = (String) obj;
                try {
                    res = Integer.decode (s);
                } catch (NumberFormatException ex) {
                }
            }
        }
        return res;
    }

    public String getFieldAsString (int index) {
        String res = null;
        Object obj = getField (index);
        if (obj != null) {
            if (obj instanceof String) {
                res = (String) obj;
            } else {
                res = obj.toString ();
            }
        }
        return res;
    }

    /**
     * Sets field value of specific field.
     * @param index index of field to set value, zero based
     * @param val <code>java.lang.Object</code> to use as new field value
     */
    public void setField (int index, Object val) {
        fields[index] = val;
    }

    public int getShapeCount () {
        return shapes.size ();
    }

    public Shape getShape (int index) {
        return shapes.get (index);
    }

    public void addShape (Shape shape) {
        shapes.add (shape);
    }

    /**
     * Returns array of field values for this record
     * @return data array of objects that are record fields values.
     */
    public Object[] getFields () {
        return fields;
    }

    public Shape[] getShapes () {
        return shapes.toArray (new Shape[0]);
    }

    public void setBrush (Brush brush) {
        this.brush = brush;
    }

    public void setPen (Pen pen) {
        this.pen = pen;
    }

    public Brush getBrush () {
        return brush;
    }

    public Pen getPen () {
        return pen;
    }

    /**
     * Returns copy of record with every field the same except geometry.
     * @return Record
     */
    public Record makeCopy () {
        Record c = new Record (fields.length);
        c.setPen (getPen ());
        c.setBrush (getBrush ());
        c.fields = fields;

        return c;
    }
}
