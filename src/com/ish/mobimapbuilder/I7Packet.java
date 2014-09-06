/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.io.*;

public class I7Packet {
    private String name;
    private String type;
    private byte[] data;

    private ByteArrayInputStream bais = null;
    private ByteArrayOutputStream baos = null;

    public I7Packet () {

    }

    public I7Packet (String name, String type, byte[] data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public InputStream getInputStream () {
        bais = new ByteArrayInputStream (data);
        return bais;
    }

    public OutputStream getOutputStream () {
        baos = new ByteArrayOutputStream ();
        return baos;
    }

    public void close () {
        try {
            if (bais != null) {
                bais.close ();
            }
            if (baos != null) {
                baos.close ();
                data = baos.toByteArray ();
            }
        } catch (IOException ex) {
        }
    }

    public String getType () {
        return type;
    }

    public String getName () {
        return name;
    }

    public void setData (byte[] data) {
        this.data = data;
    }

    public void setType (String type) {
        this.type = type;
    }

    public void setName (String name) {
        this.name = name;
    }

    public byte[] getData () {
        return data;
    }
}
