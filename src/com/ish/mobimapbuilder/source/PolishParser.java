/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Types;
import java.util.*;

import com.ish.mobimapbuilder.model.Field;
import com.ish.mobimapbuilder.model.Layer;
import com.ish.mobimapbuilder.model.Record;
import com.ish.mobimapbuilder.model.Shape;

/**
 * Polish format parser
 */
public class PolishParser implements SourceParser {
    public static final String GEOMETRY_POINT = "point";
    public static final String GEOMETRY_POLYLINE = "polyline";
    public static final String GEOMETRY_POLYGON = "polygon";

    private static final int TYPE_POINT = 1;
    private static final int TYPE_POLYLINE = 2;
    private static final int TYPE_POLYGON = 3;

    private static final String POLISH_POINT[] = {"POI", "RGN10", "RGN20"};
    private static final String POLISH_POLYLINE[] = {"POLYLINE", "RGN40"};
    private static final String POLISH_POLYGON[] = {"POLYGON", "RGN80"};
    private static final String POLISH_END = "[END";

    private static final String ATTRIBUTE_DATA = "data";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_LABEL = "label";
    private static final String ATTRIBUTE_HOUSE_NUMBER = "housenumber";
    private static final String ATTRIBUTE_STREET_DESC = "streetdesc";

    private static final String FIELDS[] = {"type", "typeHex", "geometry", "label", "housenumber", "streetdesc",
                                           "dirindicator", "category"};
    private static final int FIELDS_INDEX_TYPE = 0;
    private static final int FIELDS_INDEX_TYPE_HEX = 1;
    private static final int FIELDS_INDEX_GEOMETRY = 2;

    private static final String PARAM_POLISH_FILTER = "polish.filter";

    private int level;

    private Map<Integer, Boolean> filter;

    /**
     * Load data from source. Only dataStream must be defined, geometryStream is ignored.
     * @param layer Layer
     * @param dataStream InputStream
     * @param geomeryStream InputStream
     * @throws DataParserException
     */
    public void load (Layer layer, InputStream dataStream, InputStream geomeryStream) throws DataParserException {
        try {
            loadFromStream (dataStream, layer, layer.getEncoding ());
        } catch (IOException ex) {
            throw new DataParserException ("Error occur: " + ex.getMessage ());
        }
    }

    public void loadFromStream (InputStream is, Layer layer, String encoding) throws IOException {
        level = layer.getParamAsInt ("polish.level", -1);
        if (level < 0) {
            level = 0;
        }

        String filterS = layer.getParam (PARAM_POLISH_FILTER);
        if (filterS != null) {
            filter = parseFilter (filterS);
        }

        setLayerFields (layer);

        BufferedReader reader = new BufferedReader (new InputStreamReader (is, encoding));

        String line = null;
        while ((line = reader.readLine ()) != null) {
            line = line.trim ();

            if (line.length () < 1) { // empty
                continue;
            }

            if (line.charAt (0) == '[') { // tag
                // read attributes
                String command = line.substring (1, line.length () - 1);
                Map<String, List<String>> attributes = new Hashtable<String, List<String>>();
                while (!(line = reader.readLine ()).startsWith (POLISH_END)) {
                    int eqpos = line.indexOf ('=');
                    if (eqpos < 0) {
                        continue;
                    }

                    String key = line.substring (0, eqpos).toLowerCase ();
                    String value = line.substring (eqpos + 1);

                    List ls = attributes.get (key);
                    if (ls == null) {
                        ls = new ArrayList<String>();
                        attributes.put (key, ls);
                    }

                    ls.add (value);
                }

                int geometryType = getGeometryType (command);
                if (geometryType == 0) {
                    continue;
                }

                // create new record
                Record record = new Record (FIELDS.length);

                // set field values
                List<String> typeL = attributes.get (ATTRIBUTE_TYPE);
                if (typeL == null) {
                    continue;
                }

                String typeS = typeL.get (0);
                int type = 0;
                try {
                    type = Integer.parseInt (typeS.substring (2), 16);
                } catch (NumberFormatException ex) {
                }

                if (type == 0) {
                    continue;
                }

                if (!isAppropriate (layer, geometryType, type, attributes)) {
                    continue;
                }

                // all attributes
                for (int j = 0; j < FIELDS.length; j++) {
                    List<String> val = attributes.get (FIELDS[j]);
                    if (val == null) {
                        continue;
                    }

                    record.setField (j, val.get (0).trim ());
                }

                // type is converted from hexadecimal into decimal
                record.setField (FIELDS_INDEX_TYPE, type);
                record.setField (FIELDS_INDEX_TYPE_HEX, "0x" + Integer.toHexString (type));
                record.setField (FIELDS_INDEX_GEOMETRY, (geometryType == TYPE_POINT) ? GEOMETRY_POINT :
                                 (geometryType == TYPE_POLYGON) ? GEOMETRY_POLYGON : GEOMETRY_POLYLINE);

                // read shape
                List<Shape> shapes = readShape (attributes, geometryType);

                if (shapes == null) {
                    continue;
                }

                // add shapes to record
                for (int i = 0; i < shapes.size (); i++) {
                    record.addShape (shapes.get (i));
                }
                // add record to layer
                layer.addRecord (record);
            }
        }

        reader.close ();

        System.out.println ("Records read: " + layer.getRecords ().size ());
    }

    /**
     * Read shape from Data# tag
     * @param attributes Map
     * @param geometryType int
     * @return List<Shape>
     */
    private List<Shape> readShape (Map<String, List<String>> attributes, int geometryType) {
        String dataAttr = ATTRIBUTE_DATA + level;
        if (attributes.get (dataAttr) == null) {
            return null;
        }

        List<Shape> shapes = new Vector ();

        List<String> list = attributes.get (dataAttr);
        for (String data : list) {
            List<Double> xs = new Vector<Double>();
            List<Double> ys = new Vector<Double>();

            while (data.length () > 0) {
                int open = data.indexOf ('(');
                int close = data.indexOf (')', open);

                if (open < 0 || close < 0) {
                    break;
                }

                String pair = data.substring (open + 1, close);
                int comma = pair.indexOf (',');
                try {
                    double a = Double.parseDouble (pair.substring (0, comma));
                    double b = Double.parseDouble (pair.substring (comma + 1));

                    xs.add (a);
                    ys.add (b);
                } catch (NumberFormatException ex) {
                }

                data = data.substring (close + 1).trim ();
            }

            // close polygon if it's open
            if (geometryType == TYPE_POLYGON) {
                double firstX = xs.get (0);
                double firstY = ys.get (0);
                double lastX = xs.get (xs.size () - 1);
                double lastY = ys.get (ys.size () - 1);
                if (firstX != lastX || firstY != lastY) {
                    xs.add (firstX);
                    ys.add (firstY);
                }
            }

            int n = xs.size ();
            Shape shape = new Shape ((geometryType == TYPE_POLYGON)? Shape.Type.POLYGON: Shape.Type.POLYLINE, n);

            for (int i = 0; i < n; i++) {
                double x = ys.get (i); // exchange lat and lon
                double y = xs.get (i);

                shape.setVertex(i, x, y);
            }
            shapes.add (shape);
        }
        return shapes;
    }

    private void setLayerFields (Layer layer) {
        List<Field> fields = new Vector (FIELDS.length);

        for (int i = 0; i < FIELDS.length; i++) {
            fields.add (new Field (i, "polish." + FIELDS[i], Types.CHAR));
        }

        layer.setFields (fields);
    }

    /**
     * Get geometry type depending on tag
     * @param tag String
     * @return int
     */
    private int getGeometryType (String tag) {
        for (int i = 0; i < POLISH_POINT.length; i++) {
            if (POLISH_POINT[i].equals (tag)) {
                return TYPE_POINT;
            }
        }

        for (int i = 0; i < POLISH_POLYLINE.length; i++) {
            if (POLISH_POLYLINE[i].equals (tag)) {
                return TYPE_POLYLINE;
            }
        }

        for (int i = 0; i < POLISH_POLYGON.length; i++) {
            if (POLISH_POLYGON[i].equals (tag)) {
                return TYPE_POLYGON;
            }
        }

        return 0;
    }

    /**
     * True if elelent satisfies layer properties
     * @param layer Layer
     * @param geometryType int
     * @param type int
     * @param attributes Map
     * @return boolean
     */
    private boolean isAppropriate (Layer layer, int geometryType,
                                   int type, Map<String, List<String>> attributes) {
        if (filter == null) {
            int layerType = layer.getType ();

            if (layerType == Layer.TYPE_STREETS) {
                if (type >= 0x0000 && type <= 0x000C && geometryType == TYPE_POLYLINE) {
                    return true;
                } else {
                    return false;
                }
            }

            if (layerType == Layer.TYPE_ADDRESSES) {
                if (type == 0x006C) {
                    return true;
                }

                if (attributes.get (ATTRIBUTE_HOUSE_NUMBER) != null &&
                    attributes.get (ATTRIBUTE_STREET_DESC) != null) {
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            int full = (geometryType << 24) | type;
            return filter.get (full) != null;
        }
    }

    /**
     * Parse filter. Filter is set by string, which content is {<geometryType,type>}
     * @param filter String
     * @return Map
     */
    private Map<Integer, Boolean> parseFilter (String filter) {
        Map<Integer, Boolean> data = new HashMap<Integer, Boolean>();

        StringTokenizer st = new StringTokenizer (filter, " ;");
        while (st.hasMoreTokens ()) {
            String token = st.nextToken ();
            int ps = token.indexOf (',');
            if (ps <= 0 || ps == token.length () - 1) {
                continue;
            }

            String gt = token.substring (0, ps);
            String t = token.substring (ps + 1);

            int geometryType = GEOMETRY_POINT.equalsIgnoreCase (gt) ? TYPE_POINT :
                               (GEOMETRY_POLYLINE.equalsIgnoreCase (gt) ? TYPE_POLYLINE :
                                (GEOMETRY_POLYGON.equalsIgnoreCase (gt) ? TYPE_POLYGON : -1));
            if (geometryType < 0) {
                continue;
            }

            int type = 0;
            try {
                if (t.startsWith ("0x")) {
                    type = Integer.parseInt (t.substring (2), 16);
                } else {
                    type = Integer.parseInt (t);
                }
            } catch (NumberFormatException ex) {
            }
            if (type == 0) {
                continue;
            }

            int full = (geometryType << 24) | type;
            data.put (full, true);
        }
        return data;
    }
}
