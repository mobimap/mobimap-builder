/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ish.mobimapbuilder.model.Layer;
import com.ish.mobimapbuilder.model.Record;
import com.ish.mobimapbuilder.model.Shape;
import org.apache.commons.io.EndianUtils;
import com.ish.mobimapbuilder.model.Pen;
import com.ish.mobimapbuilder.model.Brush;

public class ShapefileParser implements SourceParser {

    // Shape types
    public static final int SHP_NULL = 0;
    public static final int SHP_POINT = 1;
    public static final int SHP_POLYLINE = 3;
    public static final int SHP_POLYGON = 5;
    public static final int SHP_MULTIPOINT = 8;
    public static final int SHP_POINTZ = 11;
    public static final int SHP_POLYLINEZ = 13;
    public static final int SHP_POLYGONZ = 15;
    public static final int SHP_MULTIPOINTZ = 18;
    private static final int SHP_POINTM = 21;
    private static final int SHP_POLYLINEM = 23;
    private static final int SHP_POLYGONM = 25;
    private static final int SHP_MULTIPOINTM = 28;
    private static final int SHP_MULTIPATCH = 31;

    private DataInputStream input;
    private DBFParser dbfParser;

    private int fileLength;
    private int offset;

    public void load (Layer layer, InputStream dataStream, InputStream geomeryStream) throws DataParserException {

        dbfParser = new DBFParser (dataStream, layer.getEncoding ());
        layer.setFields (dbfParser.getFields ());

        input = new DataInputStream (geomeryStream);

        try {
            readHeader ();
        } catch (IOException ex) {
            throw new DataParserException ("Error reading .shp header, " + ex);
        }

        try {
            readGeometry (layer);
        } catch (IOException ex) {
            throw new DataParserException ("Error reading .shp body, " + ex);
        }
    }

    private void readHeader () throws IOException, DataParserException {

        int sign = input.readInt ();

        // signature
        if (sign != 9994) {
            throw new DataParserException ("Bad signature of .shp file = " + sign);
        }

        // Read 5 unused ints
        input.skipBytes (20);

        fileLength = input.readInt () * 2; // file length is stored in 16-bit words

        int version = EndianUtils.readSwappedInteger (input);
        if (version != 1000) {
            throw new DataParserException ("Bad version of .shp file = " + version);
        }

        int shapeType = EndianUtils.readSwappedInteger (input);

        switch (shapeType) {
            case SHP_POINTZ:
                shapeType = ShapefileParser.SHP_POINT;
                break;
            case ShapefileParser.SHP_POLYLINEZ:
                shapeType = ShapefileParser.SHP_POLYLINE;
                break;
            case ShapefileParser.SHP_POLYGONZ:
                shapeType = ShapefileParser.SHP_POLYGON;
                break;
            case ShapefileParser.SHP_MULTIPOINTZ:
                shapeType = ShapefileParser.SHP_MULTIPOINT;
                break;
        }

        if (shapeType != ShapefileParser.SHP_POINT && shapeType != ShapefileParser.SHP_MULTIPOINT &&
            shapeType != ShapefileParser.SHP_POLYLINE && shapeType != ShapefileParser.SHP_POLYGON) {
            throw new DataParserException ("Unsupported shape type: " + shapeType);
        }

        // skip bounds information
        input.skipBytes (64);
    }

    private void readGeometry (Layer layer) throws DataParserException, IOException {
        offset = 100; // data block starts at 100 byte
        while (offset < fileLength) {

            Record record = readRecord (layer);
            Object recObj = dbfParser.readNextRecord ();

            if (record != null && recObj != null) {
                Object[] fields = (Object[]) recObj;

                for (int i = 0; i < fields.length; i++) {
                    record.setField (i, fields[i]);
                }

                layer.addRecord (record);
            }
        }
    }

    private Record readRecord (Layer layer) throws DataParserException, IOException {

        input.readInt ();
        int recordLen = input.readInt () * 2;
        offset += recordLen + 8; // + record header size

        int shapeType = EndianUtils.readSwappedInteger (input);

        if (shapeType == SHP_NULL) {
            // null-shape
            return null;
        }

        int fieldCount = layer.getFieldCount ();
        Record record = new Record (fieldCount);

        switch (shapeType) {
            case SHP_POINT:
            case SHP_POINTM:
            case SHP_POINTZ: {
                Shape shape = new Shape (Shape.Type.POINT, 1);

                shape.xCoords[0] = EndianUtils.readSwappedDouble (input);
                shape.yCoords[0] = EndianUtils.readSwappedDouble (input);

                if (shapeType == SHP_POINTM) {
                    skipDoubles(1); // skip M (measure)
                } else if (shapeType == SHP_POINTZ) {
                    skipDoubles(2); // skip M (measure) and Z
                }
                record.addShape(shape);
            }
            break;

            case SHP_MULTIPOINT:
            case SHP_MULTIPOINTM:
            case SHP_MULTIPOINTZ: {
                skipDoubles(4); // skip bounding box
                int n = EndianUtils.readSwappedInteger (input);

                for (int points = 0; points < n; points++) {
                    Shape shape = new Shape (Shape.Type.POINT, 1);

                    for (int i = 0; i < n; i++) {
                        shape.xCoords[i] = EndianUtils.readSwappedDouble (input);
                        shape.yCoords[i] = EndianUtils.readSwappedDouble (input);
                    }
                    record.addShape(shape);
                }

                if (shapeType == SHP_MULTIPOINTM) {
                    skipDoubles(2 + n); // skip M
                } else if (shapeType == SHP_MULTIPOINTZ) {
                    skipDoubles((2 + n) * 2); // skip M ans Z
                }
            }
            break;

            case SHP_POLYLINE:
            case SHP_POLYGON:
            case SHP_POLYLINEM:
            case SHP_POLYGONM:
            case SHP_POLYLINEZ:
            case SHP_POLYGONZ: {
                skipDoubles(4); // skip bounding box
                int numparts = EndianUtils.readSwappedInteger (input);
                int numpoints = EndianUtils.readSwappedInteger (input);

                int[] indexes = new int[numparts];
                for (int i = 0; i < numparts; i++) {
                    indexes[i] = EndianUtils.readSwappedInteger (input);
                }

                boolean isPolygon = shapeType == SHP_POLYGON || shapeType == SHP_POLYLINEM || shapeType == SHP_POLYLINEZ;

                for (int i = 0; i < numparts; i++) {
                    int pointsInSeg = ((i == numparts - 1) ? numpoints : indexes[i + 1]) - indexes[i];
                    Shape shape = new Shape (isPolygon? Shape.Type.POLYGON: Shape.Type.POLYLINE, pointsInSeg);

                    for (int j = 0; j < pointsInSeg; j++) {
                        shape.xCoords[j] = EndianUtils.readSwappedDouble (input);
                        shape.yCoords[j] = EndianUtils.readSwappedDouble (input);
                    }
                    record.addShape(shape);
                }

                if (shapeType == SHP_POLYLINEM || shapeType == SHP_POLYGONM) {
                    skipDoubles(2 + numpoints); // skip M
                } else if (shapeType == SHP_MULTIPOINTZ || shapeType == SHP_POLYGONZ) {
                    skipDoubles((2 + numpoints) * 2); // skip M ans Z
                }

                record.setPen(Pen.DEFAULT);
                if (isPolygon) {
                    record.setBrush(Brush.DEFAULT);
                }
            }
            break;
        }
        return record;
    }

    private void skipDoubles(int n) throws IOException {
        input.skipBytes(n * 8);
    }
}
