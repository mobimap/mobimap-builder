/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.xml;

import java.io.*;
import java.util.List;

public class XMLOutput {

    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final char EOL = '\n';

    private Writer writer;

    private int level = 0;

    private static final int PADDING_LIMIT = 80;
    private StringBuffer padding = new StringBuffer (PADDING_LIMIT);

    public XMLOutput (OutputStream out) {
        try {
            writer = new BufferedWriter (new OutputStreamWriter (out, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }

        init ();
    }

    public XMLOutput (Writer out) {
        writer = out;
        init ();
    }

    private void init () {
        level = 0;
        try {
            writer.write (XML_HEADER);
            writer.write (EOL);
        } catch (IOException ex1) {
        }
    }

    public void openTag (String tagName) throws IOException {
        writer.write (padding + "<" + tagName + ">" + EOL);
        increaseLevel ();
    }

    public void openAndCloseTag (String tagName) throws IOException {
        writer.write (padding + "<" + tagName + "/>" + EOL);
    }


    public void openTag (String tagName, String[] attributeNames,
                         String[] attributeValues) throws IOException {
        writer.write (padding + "<" + tagName);
        if (attributeNames != null && attributeValues != null) {
            if (attributeNames.length != attributeValues.length) {
                throw new IllegalArgumentException ("attributeNames.length != attributeValues.length");
            }

            for (int i = 0; i < attributeNames.length; i++) {
                String at = attributeNames[i];
                String vl = encode (attributeValues[i]);

                writer.write (" " + at + "=\"" + vl + "\"");
            }
        }
        writer.write (">" + EOL);
        increaseLevel ();
    }

    public void closeTag (String tagName) throws IOException {
        decreaseLevel ();
        writer.write (padding + "</" + tagName + ">" + EOL);
    }

    public void openAndCloseTag (String tagName, String[] attributeNames,
                                 String[] attributeValues) throws IOException {
        writer.write (padding + "<" + tagName);
        if (attributeNames != null && attributeValues != null) {
            if (attributeNames.length != attributeValues.length) {
                throw new IllegalArgumentException ("attributeNames.length != attributeValues.length");
            }

            for (int i = 0; i < attributeNames.length; i++) {
                String at = attributeNames[i];
                String vl = encode (attributeValues[i]);

                writer.write (" " + at + "=\"" + vl + "\"");
            }
        }
        writer.write ("/>" + EOL);
    }

    public void openTag (String tagName, List<XMLAttribute> attributes) throws IOException {
        writer.write (padding + "<" + tagName);
        if (attributes != null) {
            for (int i = 0; i < attributes.size (); i++) {
                String at = attributes.get (i).getName ();
                String vl = encode (attributes.get (i).getValue ());

                writer.write (" " + at + "=\"" + vl + "\"");
            }
        }
        writer.write (">" + EOL);
        increaseLevel ();
    }

    public void openAndCloseTag (String tagName, List<XMLAttribute> attributes) throws IOException {
        writer.write (padding + "<" + tagName);
        if (attributes != null) {
            for (int i = 0; i < attributes.size (); i++) {
                String at = attributes.get (i).getName ();
                String vl = encode (attributes.get (i).getValue ());

                writer.write (" " + at + "=\"" + vl + "\"");
            }
        }
        writer.write ("/>" + EOL);
    }

    private void increaseLevel () {
        level++;
        padding.append (' ');
    }

    private void decreaseLevel () {
        level--;
        if (padding.length () == 0) {
            throw new IllegalStateException ("There're more closing tags than opening");
        }
        padding.setLength (padding.length () - 1);
    }

    private String encode (String s) {
        int pos = s.indexOf ('&');
        if (pos >= 0) {
            if (s.length () - pos > 4) {
                if (s.substring (pos, pos + 5).equals ("&amp;")) {
                    return s;
                }
            }

            return s.substring (0, pos) + "&amp;" + s.substring (pos + 1);
        }

        if (s.indexOf ('>') >= 0) {
            s = s.replaceAll (">", "&gt;");
        }

        if (s.indexOf ('<') >= 0) {
            s = s.replaceAll ("<", "&lt;");
        }

        if (s.indexOf ('"') >= 0) {
            s = s.replaceAll ("\\\"", "&lt;");
        }

        return s;
    }
}
