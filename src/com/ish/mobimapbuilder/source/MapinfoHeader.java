/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Vector;

import com.ish.mobimapbuilder.model.Field;
import com.ish.mobimapbuilder.util.StreamIterator;
import java.io.Reader;
import java.util.Iterator;

/**
 * Mapinfo file header.
 */
class MapinfoHeader {
    private final static String COLUMNS = "COLUMNS";
    private final static String DATA = "DATA";
    private final static String VERSION = "VERSION";
    private final static String CHARSET = "CHARSET";
    private final static String DELIMITER = "DELIMITER";

    // MIF types in lower case
    private final static String TYPE_CHAR = "char";
    private final static String TYPE_SMALLINT = "smallint";
    private final static String TYPE_INTEGER = "integer";
    private final static String TYPE_FLOAT = "float";
    private final static String TYPE_DECIMAL = "decimal";
    private final static String TYPE_DATE = "date";
    private final static String TYPE_LOGICAL = "logical";

    private List<Field> fields;

    private char delimiter = '\t';
    private int version;

    private StreamIterator iterator;

    public MapinfoHeader (InputStream geometryStream, String encoding) throws DataParserException {
        try {
            Reader geometryReader = new BufferedReader (new InputStreamReader (geometryStream, encoding));
            iterator = new StreamIterator (geometryReader, " \t,", "\"");
        } catch (UnsupportedEncodingException ex) {
            throw new DataParserException ("Unsupported encoding: " + encoding);
        }

        try {
            readHeader ();
        } catch (NullPointerException e) {
            throw new DataParserException ("Error while reading mapinfo header");
        } catch (DataParserException e) {
            throw new DataParserException ("Error while reading mapinfo header. " + e.getMessage ());
        }
    }

    private void readHeader () throws DataParserException {
        while (iterator.hasNext ()) {
            Object token = iterator.next ();

            if (!(token instanceof String)) {
                continue;
            }

            String str = (String) token;

            if (DATA.equalsIgnoreCase (str)) {
                break;
            } else if (VERSION.equalsIgnoreCase (str)) {
                version = fetchInteger(iterator.next(), str);
            } else if (DELIMITER.equalsIgnoreCase (str)) {
                delimiter = fetchString(iterator.next(), str).charAt (0);
            } else if (COLUMNS.equalsIgnoreCase (str)) {
                int fieldCount = fetchInteger(iterator.next(), str);
                fields = new Vector<Field>(fieldCount);

                for (int i = 0; i < fieldCount; i++) {
                    String name = (String) iterator.next ();
                    String type = ((String) iterator.next ()).toLowerCase ();
                    int fieldType = Field.TYPE_NULL;

                    if (TYPE_CHAR.equals (type)) {
                        fieldType = Field.TYPE_STRING;
                        // ignore length
                        fetchList(iterator.next (), name);
                    } else if (TYPE_INTEGER.equals (type) || TYPE_SMALLINT.equals (type)) {
                        fieldType = Field.TYPE_INTEGER;
                    } else if (TYPE_DECIMAL.equals (type)) {
                        fieldType = Field.TYPE_DOUBLE;

                        List params = fetchList(iterator.next (), name);
                        if (params.size() == 2) {
                            int decimals = Integer.parseInt((String)params.get(1));
                            if (decimals == 0) {
                                fieldType = Field.TYPE_INTEGER;
                            }
                        }
                    } else if (TYPE_FLOAT.equals (type)) {
                        fieldType = Field.TYPE_DOUBLE;
                    } else if (TYPE_DATE.equals (type)) {
                        fieldType = Field.TYPE_STRING;
                    } else if (TYPE_LOGICAL.equals (type)) {
                        fieldType = Field.TYPE_STRING;
                    } else {
                        throw new DataParserException("Unknown type: " + type + " of column: " + name);
                    }

                    Field field = new Field (i, name, fieldType);
                    fields.add (field);
                }

//                for (Field f : fields) {
//                    System.out.println (f);
//                }
            }
        }
    }


    private int fetchInteger(Object obj, String prev) throws DataParserException {
        if (!(obj instanceof String)) {
            throw new DataParserException("Scalar value is expected, but '" + obj +
                                          "' was found after token '" + prev + "'");
        }
        try {
            Integer d = Integer.parseInt ((String) obj);
            return d.intValue();
        } catch (NumberFormatException ex) {
            throw new DataParserException("Integer value is expected, but '" + obj +
                                          "' was found after token '" + prev + "'");
        }
    }

    private String fetchString(Object obj, String prev) throws DataParserException {
        if (!(obj instanceof String)) {
            throw new DataParserException("String value is expected, but '" + obj +
                                          "' was found after token '" + prev + "'");
        }
        return (String) obj;
    }

    private List fetchList(Object obj, String prev) throws DataParserException {
        if (!(obj instanceof List)) {
            throw new DataParserException("Value in brackets is expected, but '" + obj +
                                          "' was found after token '" + prev + "'");
        }
        return (List) obj;
    }

    /**
     * Get List of fields
     * @return List
     */
    public List<Field> getFields () {
        return fields;
    }

    public char getDelimiter () {
        return delimiter;
    }

    public int getVersion () {
        return version;
    }

    public Iterator getGeometryIterator() {
        return iterator;
    }
}
