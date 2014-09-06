/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.io.*;
import java.security.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import com.ish.isrt.core.data.*;
import org.w3c.dom.*;
import org.w3c.dom.Element;

public class I7Container {
    private final static int MAGIC_BYTES = 0x49374646;
    private final static String COPYRIGHT = "I7 PROJECT";

    private String fileName;
    private String cityName;
    private boolean plain;

    private List packets;

    private byte[] binaryContent;

    public I7Container () {
        packets = new Vector ();
    }

    public void addPacket (I7Packet packet) {
        packets.add (packet);
    }

    public void save () {
        ByteArrayOutputStream baos = new ByteArrayOutputStream (10000);
        if (plain) {
            savePlain (baos);
        }
        else {
            saveToContainer (baos);
        }

        binaryContent = baos.toByteArray ();

        if (fileName != null) {
            File file = new File (fileName);
            try {
                FileOutputStream fos = new FileOutputStream (file);
                fos.write (binaryContent);
                fos.close ();
                System.out.println ("File written: " + fileName);
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
        }
    }

    public byte[] getBinaryContent () {
        return binaryContent;
    }

    private void savePlain (OutputStream outputStream) {
        try {
//            validateFileName();
            DataOutputStream fos = new DataOutputStream (outputStream);

            int n = packets.size ();

            for (int i = 0; i < n; i++) {
                I7Packet packet = (I7Packet) packets.get (i);

                byte[] data = packet.getData ();
                fos.write (data);
            }
            fos.close ();
        } catch (IOException ex) {
        }
    }

    private void saveToContainer (OutputStream outputStream) {
        try {
//            validateFileName();
            DataOutputStream fos = new DataOutputStream (outputStream);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
            DocumentBuilder docBuilder = factory.newDocumentBuilder ();
            Document xmlDoc = docBuilder.newDocument ();

            int n = packets.size ();
            fos.writeInt (MAGIC_BYTES);
            fos.writeInt (n);

            for (int i = 0; i < n; i++) {
                I7Packet packet = (I7Packet) packets.get (i);

                // metadata
                ByteArrayOutputStream baos = new ByteArrayOutputStream (100);

                Element metaElem = xmlDoc.createElement (DataXmlConst.I7_META);
                xmlDoc.appendChild (metaElem);

                Element elem = xmlDoc.createElement (DataXmlConst.I7_STREAM);
                elem.setTextContent (packet.getName ());
                metaElem.appendChild (elem);

                elem = xmlDoc.createElement (DataXmlConst.I7_TYPE);
                elem.setTextContent (packet.getType ());
                metaElem.appendChild (elem);

                elem = xmlDoc.createElement (DataXmlConst.I7_TIMESTAMP);
                elem.setTextContent (DateFormat.getDateTimeInstance (DateFormat.MEDIUM,
                                     DateFormat.LONG).format (new Date ()));
                metaElem.appendChild (elem);

                elem = xmlDoc.createElement (DataXmlConst.I7_CITY);
                elem.setTextContent (cityName);
                metaElem.appendChild (elem);

                elem = xmlDoc.createElement (DataXmlConst.I7_CREATOR);
                elem.setTextContent (COPYRIGHT);
                metaElem.appendChild (elem);

                Source input = new DOMSource (xmlDoc);
                Result output = new StreamResult (baos);
                TransformerFactory xformFactory = TransformerFactory.newInstance ();
                Transformer transformer = xformFactory.newTransformer ();
                transformer.transform (input, output);

                // header
                byte[] header = baos.toByteArray ();
                fos.writeInt (header.length);
                fos.write (header);

                // body
                byte[] data = packet.getData ();
                fos.writeInt (data.length);
                fos.write (data);

                // sign
                try {
                    MessageDigest md = MessageDigest.getInstance ("MD5");
                    md.reset ();
                    md.update (data);
                    byte dg[] = md.digest ();

                    fos.writeInt (dg.length);
                    fos.write (dg);
                } catch (NoSuchAlgorithmException ex1) {
                    System.err.println (ex1.getMessage ());
                }
            }
        } catch (Exception ex) {
            System.err.println (ex.getMessage ());
        }
    }

    private void validateFileName () {
        if (fileName == null) {
            fileName = "mmi" + System.currentTimeMillis () + "_" + this.hashCode ();
        }
    }

    /**
     * Loads container from file
     */
    public void load () {
        packets.clear ();

        try {
            DataInputStream fis = new DataInputStream (new FileInputStream (fileName));

            int magic = fis.readInt ();
            if (magic != MAGIC_BYTES) {
                throw new Exception ("Illegal magic number!");
            }

            int n = fis.readInt ();

            for (int i = 0; i < n; i++) {
                // read header
                int headerLen = fis.readInt ();
                byte[] header = new byte[headerLen];
                fis.read (header);

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
                DocumentBuilder docbuilder = factory.newDocumentBuilder ();
                Document xmldoc = docbuilder.parse (new ByteArrayInputStream (header));

                Element metaNode = xmldoc.getDocumentElement ();
                String dataType = null;
                String dataName = null;

                for (Node propNode = metaNode.getFirstChild (); propNode != null; propNode = propNode.getNextSibling ()) {
                    String propNodeName = propNode.getNodeName ();
                    if (propNodeName.equals (DataXmlConst.I7_TYPE)) {
                        dataType = propNode.getTextContent ();
                    }
                    else if (propNodeName.equals (DataXmlConst.I7_STREAM)) {
                        dataName = propNode.getTextContent ();
                    }
                }
                if (dataType == null) {
                    throw new Exception ("Illegal data type!");
                }

                // read data
                int dataLen = fis.readInt ();
                byte[] data = new byte[dataLen];
                fis.read (data);

                // read sign
                int signLen = fis.readInt ();
                byte[] sign = new byte[signLen];
                fis.read (sign);

                try {
                    MessageDigest md = MessageDigest.getInstance ("MD5");
                    md.reset ();
                    md.update (data);
                    byte dg[] = md.digest ();

                    boolean equal = true;
                    for (int j = 0; j < dg.length; j++) {
                        equal &= sign[j] == dg[j];
                    }

                    if (!equal) {
                        throw new Exception ("Data is corrupted!");
                    }
                } catch (NoSuchAlgorithmException ex1) {
                    System.err.println (ex1.getMessage ());
                }

                I7Packet packet = new I7Packet (dataName, dataType, data);
                packets.add (packet);
            }
        } catch (Exception ex) {
            System.err.println (ex.getMessage ());
        }
    }

    /**
     * Get number of packets
     * @return int
     */
    public int getPacketCount () {
        return packets.size ();
    }

    /**
     * Get packet with index i
     * @param i int
     * @return I7Packet
     */
    public I7Packet getPacket (int i) {
        return (I7Packet) packets.get (i);
    }

    public void setFileName (String fileName) {
        this.fileName = fileName;
    }

    public void setCityName (String sourceFileName) {
        this.cityName = sourceFileName;
    }

    public void setPlain (boolean plain) {
        this.plain = plain;
    }

    public String getFileName () {
        return fileName;
    }

    public String getCityName () {
        return cityName;
    }

    public boolean isPlain () {
        return plain;
    }
}
