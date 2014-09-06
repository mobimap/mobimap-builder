/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ish.mobimapbuilder.model.*;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

public class MapinfoParser implements SourceParser {

    // Graphic object types accoring to MIF specification
    private static final String TYPE_NONE = "NONE";
    private static final String TYPE_POINT = "POINT";
    private static final String TYPE_LINE = "LINE";
    private static final String TYPE_PLINE = "PLINE";
    private static final String TYPE_REGION = "REGION";
    private static final String TYPE_ARC = "ARC";
    private static final String TYPE_TEXT = "TEXT";
    private static final String TYPE_RECTANGLE = "RECT";
    private static final String TYPE_ROUND_RECTANGLE = "ROUNDRECT";
    private static final String TYPE_ELLIPSE = "ELLIPSE";
    private static final String TYPE_MULTIPOINT = "MULTIPOINT";

    private static final String TYPE_PLINE_MULTIPLE = "MULTIPLE";

    private static final String OPTION_PEN = "PEN";
    private static final String OPTION_BRUSH = "BRUSH";
    private static final String OPTION_CENTER = "CENTER";

    private final StrTokenizer tokenizer;
    private final Map<String, PrimitiveParser> parsers;

    private int dataRecordsCounter = 0;

    private BufferedReader dataReader;

    public MapinfoParser () {
        tokenizer = new StrTokenizer ();
        tokenizer.setEmptyTokenAsNull (false);
        tokenizer.setIgnoreEmptyTokens (false);
        tokenizer.setQuoteMatcher (StrMatcher.quoteMatcher ());
        tokenizer.setTrimmerMatcher (StrMatcher.trimMatcher ());

        parsers = new HashMap<String, PrimitiveParser>();
        parsers.put (TYPE_NONE, new NonePrimitiveParser ());
        parsers.put (TYPE_POINT, new PointPrimitiveParser ());
        parsers.put (TYPE_LINE, new LinePrimitiveParser ());
        parsers.put (TYPE_PLINE, new PolylinePrimitiveParser ());
        parsers.put (TYPE_REGION, new RegionPrimitiveParser ());
        parsers.put (TYPE_ARC, new ArcPrimitiveParser ());
        parsers.put (TYPE_TEXT, new TextPrimitiveParser ());
        parsers.put (TYPE_RECTANGLE, new RectanglePrimitiveParser ());
        parsers.put (TYPE_ROUND_RECTANGLE, new RoundRectanglePrimitiveParser ());
        parsers.put (TYPE_ELLIPSE, new EllipsePrimitiveParser ());
        parsers.put (TYPE_MULTIPOINT, new MultipointPrimitiveParser ());
    }

    /**
     * Load layer data from data and geometry streams
     * @param layer Layer
     * @param dataStream InputStream
     * @param geomeryStream InputStream
     * @throws DataParserException
     */
    public void load (Layer layer, InputStream dataStream, InputStream geomeryStream) throws DataParserException {

        String encoding = layer.getEncoding ();

        try {
            dataReader = new BufferedReader (new InputStreamReader (dataStream, layer.getEncoding ()));
        } catch (UnsupportedEncodingException ex) {
            throw new DataParserException ("Unsupported encoding: " + encoding);
        }

        MapinfoHeader header = new MapinfoHeader (geomeryStream, encoding);
        layer.setFields (header.getFields ());

        tokenizer.setDelimiterMatcher (StrMatcher.charSetMatcher (Character.toString (header.getDelimiter ())));

        readGeometry (header, layer);

//        System.out.println (dataRecordsCounter + " lines read from data file");
    }

    /**
     * Read geometry data and store it into layer
     * @param header MapinfoHeader
     * @param layer Layer
     * @throws DataParserException
     */
    private void readGeometry (MapinfoHeader header, Layer layer) throws DataParserException {
        Iterator iterator = header.getGeometryIterator ();

        Record record = null;

        while (iterator.hasNext ()) {
            Object objToken = iterator.next ();

            if (!(objToken instanceof String)) {
                continue;
            }

            String token = ((String) objToken).toUpperCase ();
            PrimitiveParser parser = parsers.get (token);

            if (parser != null) {
                record = parser.read (iterator, new Record (layer.getFieldCount ()));
                readNextDataRecord (record, layer.getFields ());
                if (record != null) {
                    layer.addRecord (record);
                }
            } else {
                if (OPTION_PEN.equals (token)) {
                    Pen pen = readPen (iterator);
                    if (record != null && pen != null) {
                        record.setPen (pen);
                    }
                } else if (OPTION_BRUSH.equals (token)) {
                    Brush brush = readBrush (iterator);
                    if (record != null) {
                        record.setBrush (brush);
                    }
                } else if (OPTION_CENTER.equals (token)) {
                    skip (iterator, 2);
                }
            }
        }
    }

    /**
     * Read pen parameters
     * @param iterator Iterator
     * @return Pen
     * @throws DataParserException
     */
    private Pen readPen (Iterator iterator) throws DataParserException {
        Object obj = iterator.next ();
        if (!(obj instanceof List)) {
            throw new DataParserException ("Pen parameters are expected, but " + obj + " is read");
        }

        List params = (List) obj;
        if (params.size () != 3) {
            throw new DataParserException ("Pen parameters should have 3 attributes, but " + params.size () +
                                           " is read");
        }

        int width = (int) fetchDouble (params.get (0));
        int pattern = (int) fetchDouble (params.get (1));
        int color = (int) fetchDouble (params.get (2));

        if (width > 0 && pattern > 1) { // pattern == 1 is invisible by MIF spec.
            return new Pen (width, color);
        } else {
            return null;
        }
    }

    /**
     * Read brush parameters
     * @param iterator Iterator
     * @return Brush
     * @throws DataParserException
     */
    private Brush readBrush (Iterator iterator) throws DataParserException {
        Object obj = iterator.next ();
        if (!(obj instanceof List)) {
            throw new DataParserException ("Brush parameters are expected, but " + obj + " is read");
        }

        List params = (List) obj;
        if (params.size () < 2) {
            throw new DataParserException ("Brush parameters should have at least 2 attributes, but " +
                                           params.size () + " is read");
        }

        int pattern = (int) fetchDouble (params.get (0));
        int forecolor = (int) fetchDouble (params.get (1));
        int backcolor = -1;
        if (params.size () > 2) {
            backcolor = (int) fetchDouble (params.get (1));
        }

        return new Brush (forecolor);
    }

    // ************************** PRIMITIVE PARSERS *****************************

    private interface PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException;
    }


    private static class NonePrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            return null;
        }
    }


    private static class PointPrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            double x = fetchDouble (iterator.next ());
            double y = fetchDouble (iterator.next ());

            Shape shape = new Shape (Shape.Type.POINT, 1);
            shape.setVertex (0, x, y);
            record.addShape (shape);

            return record;
        }
    }


    private static class LinePrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            double x1 = fetchDouble (iterator.next ());
            double y1 = fetchDouble (iterator.next ());
            double x2 = fetchDouble (iterator.next ());
            double y2 = fetchDouble (iterator.next ());

            Shape shape = new Shape (Shape.Type.POLYLINE, 2);
            shape.setVertex (0, x1, y1);
            shape.setVertex (1, x2, y2);
            record.addShape (shape);

            return record;
        }
    }


    private static class PolylinePrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {

            int numSections = 1;
            Object token = iterator.next ();

            String s = ((String) token).toUpperCase ();
            if (TYPE_PLINE_MULTIPLE.equals (s)) {
                numSections = (int) fetchDouble (iterator.next ());
                token = iterator.next ();
            }

            for (int section = 0; section < numSections; section++) {

                if (section > 0) {
                    // on first iteration token is already read, but on next iteration it's need to be read
                    // this operation cannot be moved to the end of for-iteration, because it will always read
                    // token next to the end of polilyne
                    token = iterator.next ();
                }

                int numPoints = (int) fetchDouble (token);

                Shape shape = new Shape (Shape.Type.POLYLINE, numPoints);

                for (int point = 0; point < numPoints; point++) {
                    double x = fetchDouble (iterator.next ());
                    double y = fetchDouble (iterator.next ());
                    shape.setVertex (point, x, y);
                }

                record.addShape (shape);
            }

            return record;
        }
    }


    private static class RegionPrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {

            int numSections = (int) fetchDouble (iterator.next ());

            for (int section = 0; section < numSections; section++) {
                int numPoints = (int) fetchDouble (iterator.next ());

                Shape shape = new Shape (Shape.Type.POLYGON, numPoints);

                for (int point = 0; point < numPoints; point++) {
                    double x = fetchDouble (iterator.next ());
                    double y = fetchDouble (iterator.next ());
                    shape.setVertex (point, x, y);
                }

                record.addShape (shape);
            }

            return record;
        }
    }


    private static class ArcPrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            skip (iterator, 6);
            return null;
        }
    }


    private static class TextPrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            skip (iterator, 5);
            return null;
        }
    }


    private static class RectanglePrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            double x1 = fetchDouble (iterator.next ());
            double y1 = fetchDouble (iterator.next ());
            double x2 = fetchDouble (iterator.next ());
            double y2 = fetchDouble (iterator.next ());

            Shape shape = new Shape (Shape.Type.POLYGON, 5);
            shape.setVertex (0, x1, y1);
            shape.setVertex (1, x1, y2);
            shape.setVertex (2, x2, y2);
            shape.setVertex (3, x2, y1);
            shape.setVertex (4, x1, y1);
            record.addShape (shape);

            return record;
        }
    }


    private static class RoundRectanglePrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            skip (iterator, 5);
            return null;
        }
    }


    private static class EllipsePrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {
            skip (iterator, 4);
            return null;
        }
    }


    private static class MultipointPrimitiveParser implements PrimitiveParser {
        public Record read (Iterator iterator, Record record) throws DataParserException {

            int numPoints = (int) fetchDouble (iterator.next ());

            for (int point = 0; point < numPoints; point++) {
                double x = fetchDouble (iterator.next ());
                double y = fetchDouble (iterator.next ());
                Shape shape = new Shape (Shape.Type.POINT, 1);
                shape.setVertex (0, x, y);
                record.addShape (shape);
            }

            return record;
        }
    }


    /**
     * Ignore specified number of tokens
     * @param iterator Iterator
     * @param n int
     */
    private static void skip (Iterator iterator, int n) {
        for (int i = 0; i < n; i++) {
            iterator.next ();
        }
    }

    /**
     * Fetch double value from object
     * @param obj Object
     * @return double
     * @throws DataParserException
     */
    private static double fetchDouble (Object obj) throws DataParserException {
        if (!(obj instanceof String)) {
            throw new DataParserException ("Scalar value is expected, but '" + obj + "' was found");
        }
        try {
            Double d = Double.parseDouble ((String) obj);
            return d.doubleValue ();
        } catch (NumberFormatException ex) {
            throw new DataParserException ("Double value is expected, but '" + obj + "' was found");
        }
    }

    // ************************** DATA STREAM READER *****************************

    private void readNextDataRecord (Record record, List<Field> fields) throws DataParserException {
        String str = null;
        try {
            str = dataReader.readLine ().trim ();
            dataRecordsCounter++;
        } catch (IOException ex) {
            throw new DataParserException ("Error reading data stream");
        }

        tokenizer.reset (str);

        for (int i = 0; tokenizer.hasNext () && i < fields.size(); i++) {
            String field = (String) tokenizer.next ();
            Object value = null;
            switch (fields.get (i).getType ()) {
                case Field.TYPE_STRING:
                    value = field;
                    break;
                case Field.TYPE_DOUBLE:
                    value = parseDouble (field);
                    break;
                case Field.TYPE_INTEGER:
                    value = parseInteger (field);
                    break;
            }
            if (record != null) {
                record.setField (i, value);
            }
        }
    }

    private Double parseDouble (String s) {
        try {
            return Double.valueOf (s);
        } catch (NumberFormatException e) {
            return new Double (0.0);
        }
    }

    private Integer parseInteger (String s) {
        try {
            return Integer.decode (s);
        } catch (NumberFormatException e) {
            return new Integer (0);
        }
    }
}
