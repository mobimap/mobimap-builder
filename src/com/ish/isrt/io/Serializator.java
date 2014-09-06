//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.io;

import java.io.DataOutputStream;
import java.util.Vector;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;

public class Serializator
{
    private static final int DATA_EOF = 0x0;
    private static final int DATA_TYPE_BYTE = 0x1;
    private static final int DATA_TYPE_CHAR = 0x2;
    private static final int DATA_TYPE_SHORT = 0x3;
    private static final int DATA_TYPE_INT = 0x4;
    private static final int DATA_TYPE_LONG = 0x5;
    private static final int DATA_TYPE_BOOLEAN = 0x6;
    private static final int DATA_TYPE_DOUBLE = 0x7;
    private static final int DATA_TYPE_STRING_UTF8 = 0x11;
    private static final int DATA_TYPE_STRING_UTF16 = 0x12;
    private static final int DATA_TYPE_VECTOR = 0x21;
    private static final int DATA_TYPE_HASHTABLE = 0x31;
    private static final int DATA_TYPE_BINARY = 0x41;
    private static final int DATA_TYPE_EXCEPTION = 0x7F;

    /**
     * Write single data item into stream
     * @param binaryStream DataOutputStream
     * @param value Object
     * @throws IOException
     */
    public static void writeObject (DataOutputStream binaryStream, Object value)
        throws IOException
    {
        if (value instanceof Integer)
        {
            binaryStream.writeByte (DATA_TYPE_INT);
            binaryStream.writeInt (((Integer)value).intValue());
        }
        else if (value instanceof Byte)
        {
            binaryStream.writeByte (DATA_TYPE_BYTE);
            binaryStream.writeByte (((Byte)value).byteValue());
        }
        else if (value instanceof Short)
        {
            binaryStream.writeByte (DATA_TYPE_SHORT);
            binaryStream.writeShort (((Short)value).shortValue());
        }
        else if (value instanceof Character)
        {
            binaryStream.writeByte (DATA_TYPE_CHAR);
            binaryStream.writeChar (((Character)value).charValue());
        }
        else if (value instanceof Long)
        {
            binaryStream.writeByte (DATA_TYPE_LONG);
            binaryStream.writeLong (((Long)value).longValue());
        }
        else if (value instanceof Boolean)
        {
            binaryStream.writeByte (DATA_TYPE_BOOLEAN);
            binaryStream.writeBoolean (((Boolean)value).booleanValue());
        }
        else if (value instanceof Double)
        {
            binaryStream.writeByte (DATA_TYPE_DOUBLE);
            binaryStream.writeDouble (((Double)value).doubleValue());
        }
        else if (value instanceof String)
        {
            binaryStream.writeByte (DATA_TYPE_STRING_UTF8);
            binaryStream.writeUTF ((String)value);
        }
        else if (value instanceof Vector)
        {
            binaryStream.writeByte (DATA_TYPE_VECTOR);
            Vector v = (Vector)value;
            int vsize = v.size();
            binaryStream.writeInt (vsize);
            for (int i=0; i < vsize; i++)
                writeObject(binaryStream, v.get(i));
        }
        else if (value instanceof Hashtable)
        {
            binaryStream.writeByte (DATA_TYPE_HASHTABLE);
            Hashtable h = (Hashtable)value;
            int hsize = h.size();
            binaryStream.writeInt (hsize);
            for (Enumeration e = h.keys(); e.hasMoreElements() ;)
            {
                Object key = e.nextElement();
                writeObject(binaryStream, key);
                writeObject(binaryStream, h.get (key));
            }
        }
        else if (value instanceof byte[])
        {
            binaryStream.writeByte (DATA_TYPE_BINARY);
            byte[] v = (byte[])value;
            binaryStream.writeInt (v.length);
            binaryStream.write (v);
        }
        else if (value instanceof char[])
        {
            binaryStream.writeByte (DATA_TYPE_STRING_UTF16);
            char[] v = (char[])value;
            binaryStream.writeInt (v.length);
            for (int i=0; i < v.length; i++)
                binaryStream.writeChar(v[i]);
        }
        else if (value instanceof int[])
        {
            binaryStream.writeByte (DATA_TYPE_VECTOR);
            int[] v = (int[])value;
            binaryStream.writeInt (v.length);
            for (int i=0; i < v.length; i++)
                writeObject(binaryStream, new Integer(v[i]));
        }
        else
            throw new RuntimeException("Unsupported object type: " + value.getClass().getName());
    }

}
