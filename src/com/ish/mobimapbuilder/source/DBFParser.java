/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.ish.mobimapbuilder.model.Field;
import org.apache.commons.io.input.SwappedDataInputStream;

/**
 * Reader and parser for DBF format
 */
public class DBFParser {

    private static final int DBF_HEADER_SIZE = 32;
    private static final int DBF_FIELD_NAME_LEN = 11;

    private static final Integer DEFAULT_INTEGER = Integer.valueOf(0);
    private static final Double DEFAULT_DOUBLE = Double.valueOf(0);

    private int totalCount = 0;
    private int readCount = 0;

    private DataInput input = null;
    private String encoding = null;

    private DBFField[] fields = null;

    public DBFParser (InputStream in, String encoding) throws DataParserException {

        this.encoding = encoding;
        input = new SwappedDataInputStream (in);

        try {
            readHeader ();
        } catch (IOException ex) {
            throw new DataParserException ("Error reading DBF header, " + ex.getMessage ());
        }
    }


    /**
     * Read DBF header
     * @throws IOException
     * @throws DataParserException
     */
    private void readHeader () throws IOException, DataParserException {
        input.skipBytes (4);

        totalCount = input.readInt ();
        int fieldBlockSize = input.readShort ();
        int fieldsCount = (fieldBlockSize - DBF_HEADER_SIZE - 1) / DBF_HEADER_SIZE;

        input.skipBytes (22);

        fields = new DBFField[fieldsCount];
        for (int index = 0; index < fieldsCount; index++) {
            fields[index] = readField (index);
        }

        byte terminator = input.readByte (); // end of field defs marker
        if (terminator != 0x0D) {
            throw new DataParserException ("DBF header terminator is expected");
        }
    }

    /**
     * Read single DBF field
     * @param index int
     * @return DBFField
     * @throws IOException
     * @throws DataParserException
     */
    private DBFField readField (int index) throws IOException, DataParserException {

        byte[] nameBinary = new byte[DBF_FIELD_NAME_LEN];
        input.readFully(nameBinary);
        String name = (new String(nameBinary, encoding)).trim();

        char dbftype = (char) input.readUnsignedByte ();
        input.skipBytes (4);

        int size = (int) input.readUnsignedByte ();
        int attr = (int) input.readUnsignedByte ();
        int type = Field.TYPE_NULL;

        switch (dbftype) {
            case 'C':
            case 'c':
            case 'D':
            case 'M':
            case 'G':
            case 'L':
                type = Field.TYPE_STRING;
                break;
            case 'N':
            case 'n':
            case 'F':
            case 'f':
                if (attr == 0) {
                    type = Field.TYPE_INTEGER;
                } else {
                    type = Field.TYPE_DOUBLE;
                }
                break;
            default:
                throw new DataParserException ("Unknown DBF field type: " + (char) dbftype);
        }
        input.skipBytes (14);

        return new DBFField (index, name, type, size);
    }

    /**
     * Read next DBF record. Returns null if record is deleted or Object[] if record is active.
     * @return Object[]
     * @throws IOException
     * @throws DataParserException
     */
    public Object[] readNextRecord () throws DataParserException {

        if (readCount >= totalCount) {
            throw new DataParserException ("Attempt to read more records than DBF contains");
        }

        try {
            return readNextRecordInternal ();
        } catch (IOException ex) {
            throw new DataParserException("Error reading data file, " + ex);
        }
    }

    private Object[] readNextRecordInternal () throws DataParserException, IOException {

        byte mark = input.readByte ();
        if (mark != 0x20) {
            // record is deleted;
            return null;
        }

        Object[] result = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            DBFField field = fields[i];
            int size = field.getSize ();
            byte[] bytes = new byte[size];
            input.readFully (bytes);

            String str = null;
            try {
                str = (new String (bytes, encoding)).trim();
            } catch (UnsupportedEncodingException ex) {
                throw new DataParserException ("Unsupported encoding: " + encoding);
            }

            Object value = null;
            switch (field.getType ()) {
                case Field.TYPE_STRING:
                    value = str;
                    break;
                case Field.TYPE_DOUBLE:
                    try {
                        value = Double.parseDouble (str);
                    } catch (NumberFormatException ex) {
                        value = DEFAULT_DOUBLE;
                    }
                    break;
                case Field.TYPE_INTEGER:
                    try {
                        value = Integer.parseInt (str);
                    } catch (NumberFormatException ex) {
                        value = DEFAULT_INTEGER;
                    }
                    break;
            }
            result[i] = value;
        }

        readCount++;

        return result;
    }

    /**
     * Get List of fields
     * @return List
     */
    public List<Field> getFields () {
        List<Field> list = new ArrayList<Field>(fields.length);
        for (Field f : fields) {
            list.add (f);
        }
        return list;
    }
}
