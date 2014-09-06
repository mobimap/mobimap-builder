/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.target;

import java.io.*;
import java.util.zip.*;

import com.ish.mobimapbuilder.*;
import org.w3c.dom.*;

abstract public class Target
{
    private String id;
    private String type;
	private String fileName;
    private String fileFormat;
    private boolean plain; // true if put in container

    private Project project;

    protected Writer fileWriter = null;

    private I7Packet packet;

	public Target(String type)
	{
        this.type = type;
        plain = false;
	}

    abstract public boolean load (Node node);
    abstract public void save () throws IOException;

    protected void loadParameters (String nodeName, NamedNodeMap attribs)
    {
        if (attribs != null)
        {
            String value = getAttributeValue (attribs, "value");

            if ("id".equals(nodeName))
                id = value;
            else if ("file".equals(nodeName))
            {
				fileName = value;
                fileFormat = getAttributeValue (attribs, "format");
			}
            else if ("plain".equals(nodeName))
            {
				plain = Boolean.parseBoolean(value);
			}
        }
    }
    protected String getAttributeValue (NamedNodeMap attribs, String attributeName) throws DOMException
    {
        String value = null;
        if (attribs.getNamedItem (attributeName) != null)
            value = attribs.getNamedItem (attributeName).getNodeValue ();
        return value;
    }

    public void init() throws IOException
    {
        packet = new I7Packet();
        OutputStream pos = packet.getOutputStream();
        OutputStream os = plain? pos: new GZIPOutputStream(pos);
        fileWriter = new BufferedWriter (
            new OutputStreamWriter (
            os, "UTF-8"));
    }
    public I7Packet pack()
    {
		try {
			fileWriter.close ();
		}
		catch (IOException ex) {
		}
        packet.setName(id);
        packet.setType(type);
        packet.close();

        return packet;
    }

    public String getId ()
    {
        return id;
    }

	public String getFileName ()
    {
        return fileName;
    }

	public Writer getFileWriter ()
	{
		return fileWriter;
	}

	public void setProject (Project project)
	{
		this.project = project;
	}

	public Project getProject ()
	{
		return project;
	}

	public String getType ()
	{
		return type;
	}

	public boolean isPlain ()
	{
		return plain;
	}

	public String getFileFormat ()
	{
		return fileFormat;
	}
}
