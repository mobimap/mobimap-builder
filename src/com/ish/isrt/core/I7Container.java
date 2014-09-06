//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.io.*;
import java.security.*;
import java.util.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.Element;
import com.ish.isrt.core.data.*;

public class I7Container
{
    private final static int MAGIC_BYTES = 0x49374646;
    private final static String COPYRIGHT = "I7 PROJECT";

    private String fileName;
    private String cityName;

    private List packets;

	public I7Container ()
	{
        packets = new Vector();
	}

    /**
     * Adds packet to container
     * @param packet I7Packet
     */
    public void addPacket (I7Packet packet)
    {
        packets.add(packet);
    }

    /**
     * Loads container from file
     */
    public void load()
    {
        packets.clear();

		try {
            byte[] bin = CityLoader.binaryContents.get(fileName);
            InputStream is = new ByteArrayInputStream(bin);
			DataInputStream fis = new DataInputStream (new BufferedInputStream(is));

            int magic = fis.readInt();
            if (magic != MAGIC_BYTES) throw new Exception("Illegal magic number!");

            int n = fis.readInt();

            for (int i = 0; i < n; i++)
            {
                // read header
                int headerLen = fis.readInt();
                byte[] header = new byte[headerLen];
                fis.read(header);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docbuilder = factory.newDocumentBuilder();
                Document xmldoc = docbuilder.parse (new ByteArrayInputStream(header));

                Element metaNode = xmldoc.getDocumentElement();
                String dataType = null;
                String dataName = null;

                for (Node propNode = metaNode.getFirstChild(); propNode != null; propNode = propNode.getNextSibling() )
                {
					String propNodeName = propNode.getNodeName ();
					if (propNodeName.equals (DataXmlConst.I7_TYPE))
                        dataType = propNode.getTextContent();
                    else if (propNodeName.equals (DataXmlConst.I7_STREAM))
                        dataName = propNode.getTextContent();
                }
                if (dataType == null)
                    throw new Exception("Illegal data type!");

                // read data
                int dataLen = fis.readInt();
                byte[] data = new byte[dataLen];
                fis.read(data);

                // read sign
                int signLen = fis.readInt();
                byte[] sign = new byte[signLen];
                fis.read(sign);

                try {
                    MessageDigest md = MessageDigest.getInstance ("MD5");
                    md.reset ();
                    md.update (data);
                    byte dg [] = md. digest ();

                    boolean equal = true;
                    for (int j=0; j < dg.length; j++)
                        equal &= sign[j] == dg[j];

                    if (!equal)
                        throw new Exception("Data is corrupted!");
                }
                catch (NoSuchAlgorithmException ex1) {
                    System.err.println (ex1.getMessage());
                }

                I7Packet packet = new I7Packet(dataName, dataType, data);
                packets.add(packet);
            }
		}
		catch (Exception ex) {
            System.err.println (ex.getMessage());
		}
    }

    /**
     * Get number of packets
     * @return int
     */
    public int getPacketCount()
    {
        return packets.size();
    }
    /**
     * Get packet with index i
     * @param i int
     * @return I7Packet
     */
    public I7Packet getPacket(int i)
    {
        return (I7Packet) packets.get(i);
    }

	public void setFileName (String fileName)
	{
		this.fileName = fileName;
	}

	public void setCityName (String sourceFileName)
	{
		this.cityName = sourceFileName;
	}

	public String getFileName ()
	{
		return fileName;
	}

	public String getCityName ()
	{
		return cityName;
	}
}
