/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.optimizer;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.List;
import java.util.Vector;

import com.ish.mobimapbuilder.model.Point2;
import com.ish.mobimapbuilder.model.Record;
import com.ish.mobimapbuilder.model.Rectangle2;
import com.ish.mobimapbuilder.model.Shape;

public class Splitter {
    private int step;

    public Splitter (int step) {
        this.step = step;
    }

    public List<Record> make (Record initial) {
        List<Record> results = new Vector<Record>();

        Shape[] shapes = initial.getShapes ();

        for (int k = 0; k < shapes.length; k++) {
            Shape shape = shapes[k];
            int coorsN = shape.xCoords.length;

            boolean isClosed = shape.xCoords[0] == shape.xCoords[coorsN - 1] &&
                               shape.yCoords[0] == shape.yCoords[coorsN - 1];

            if (!isClosed) {
                results.add (initial);
                break;
            }

            Polygon polygon = new Polygon ();

            for (int i = 0; i < coorsN; i++) {
                int x = (int) shape.xCoords[i];
                int y = (int) shape.yCoords[i];

                polygon.addPoint (x, y);
            }

            Rectangle2 extent = shape.calculateExtent ();

            for (int x = (int) Math.floor (extent.x); x < Math.ceil (extent.x2); x += step) {
                for (int y = (int) Math.floor (extent.y); y < Math.ceil (extent.y2); y += step) {
                    Area border = new Area (new Rectangle (x, y, step, step));
                    Area cut = new Area (polygon);
                    cut.intersect (border);

                    if (cut.isEmpty ()) {
                        continue;
                    }

                    PathIterator pit = cut.getPathIterator (new AffineTransform ());

                    Record currentRecord = initial.makeCopy (); // new Record();
                    List<Point2> points = null;

                    while (!pit.isDone ()) {
                        double[] coors = new double[6];
                        int a = pit.currentSegment (coors);

                        switch (a) {
                            case PathIterator.SEG_MOVETO:

                                if (points != null) {
                                    createNewShape (currentRecord, points);
                                }

                                points = new Vector<Point2>();
                                points.add (new Point2 (coors[0], coors[1]));
                                break;

                            case PathIterator.SEG_LINETO:
                                points.add (new Point2 (coors[0], coors[1]));
                                break;
                        }

                        pit.next ();
                    }

                    if (points != null) {
                        createNewShape (currentRecord, points);
                    }

                    if (currentRecord.getShapeCount () > 0) {
                        results.add (currentRecord);
                    }
                }
            }
        }

        return results;
    }

    private void createNewShape (Record currentRecord, List<Point2> points) {
        int an = points.size ();
        Shape newShape = new Shape(Shape.Type.POLYGON, an);
        for (int g = 0; g < points.size (); g++) {
            Point2 point = points.get (g);
            newShape.xCoords[g] = point.x;
            newShape.yCoords[g] = point.y;
        }
        currentRecord.addShape(newShape);
    }
}
