//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.io.*;

import javax.swing.ImageIcon;

public class Icon
    extends AbstractObject implements Serializable
{
    private String fileName;
    private Reference[] places;

    private byte[] iconData;
    private ImageIcon image;

    public Reference self()
    {
        return new Reference(DataType.MASK_ICON, getId());
    }

    public byte[] getIconData ()
    {
        return iconData;
    }

    public String getFileName ()
    {
        return fileName;
    }

    public Reference[] getPlaces ()
    {
        return places;
    }

    public ImageIcon getImage ()
    {
        return image;
    }

    public void setIconData (byte[] iconData)
    {
        this.iconData = iconData;
    }

    public void setFileName (String fileName)
    {
        this.fileName = fileName;
    }

    public void setImage (ImageIcon image)
    {
        this.image = image;
    }

    public void setPlaces (Reference[] places)
    {
        this.places = places;
    }

    public boolean loadIconData()
    {
        boolean success = false;
        try
        {
            InputStream input = this.getClass ().getResourceAsStream ("/" + fileName);
            if (input == null)
                input = new FileInputStream (fileName);

            if (input != null) {
                final int READ_AT_ONCE_LIMIT = 4096;
                ByteArrayOutputStream bos = new ByteArrayOutputStream(READ_AT_ONCE_LIMIT);

                byte[] buf = new byte[READ_AT_ONCE_LIMIT];
                int howmany;
                while ((howmany = input.read(buf)) > 0)
                {
                    bos.write(buf, 0, howmany);
                }
                input.close();

                success = true;
                byte[] contents = bos.toByteArray();
                setIconData(contents);
                image = new ImageIcon(contents);
            }
        }
        catch (IOException ex2)
        {
        }
        return success;
    }

    public static boolean loadIconData(Icon icon)
    {
        return icon.loadIconData();
    }
}
