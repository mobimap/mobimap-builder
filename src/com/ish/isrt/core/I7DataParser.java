//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.io.*;
import javax.xml.parsers.*;

import java.awt.*;

import java.util.Properties;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.ish.isrt.core.data.*;

/**
 * I7 data parser
 * <p>Title: i7 project</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2000-2007</p>
 *
 * <p>Company: Iliya Shakhat</p>
 *
 * @author not attributable
 * @version 7.0
 */
public class I7DataParser
    extends DefaultHandler
{
    private InputStream is = null;
    private Storage storage = null;

    private Crossroad currentPc = null;
    private int currentConN = 0;
    private Icon currentPhoto = null;
    private int currentPlaceN = 0;

    public I7DataParser (InputStream is, Storage storage)
    {
        this.is = is;
        this.storage = storage;
    }

    public void make ()
    {
        try
        {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance ();
            parserFactory.setValidating (false);
            parserFactory.setNamespaceAware (false);
            SAXParser parser = parserFactory.newSAXParser ();
            parser.parse (is, this);
        }
        catch (IOException exception)
        {
            exception.printStackTrace ();
        }
        catch (SAXException exception)
        {
            exception.printStackTrace ();
        }
        catch (ParserConfigurationException exception)
        {
            exception.printStackTrace ();
        }
        catch (FactoryConfigurationError exception)
        {
            exception.printStackTrace ();
        }
    }

    /**
     * SAX parser. Start element handler
     * @param uri String
     * @param localName String
     * @param qName String
     * @param attributes Attributes
     * @throws SAXException
     */
    public void startElement (String uri, String localName, String qName,
                              Attributes attributes) throws SAXException
    {
        if (qName.equals (DataXmlConst.TAG_CITY))
        {
            City city = new City();
            city.setName (attributes.getValue (DataXmlConst.ATTR_NAME));
            city.setWidth (getAttributeAsInt (attributes, DataXmlConst.ATTR_X));
            city.setHeight (getAttributeAsInt (attributes, DataXmlConst.ATTR_Y));

            if (getAttribute(attributes, "east") != null)
            {
                city.setEast (getAttributeAsDouble (attributes, "east"));
                city.setWest (getAttributeAsDouble (attributes, "west"));
                city.setSouth (getAttributeAsDouble (attributes, "south"));
                city.setNorth (getAttributeAsDouble (attributes, "north"));
                city.setGeo(true);
            }
            storage.setCity(city);
        }
        else if (qName.equals (DataXmlConst.TAG_CROSSROADS_STREAM))
        {
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT)) + 1;
            storage.setCrossroads(new Crossroad[n]);
        }
        else if (qName.equals (DataXmlConst.TAG_STREETS_STREAM))
        {
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT)) + 1;
            storage.setStreets (new Street[n]);
        }
        else if (qName.equals (DataXmlConst.TAG_POINTS_STREAM))
        {
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT)) + 1;
            storage.setPois (new Vector<POI>(n));
        }
        else if (qName.equals (DataXmlConst.TAG_ADDRESSES_STREAM))
        {
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT)) + 1;
            storage.setAddresses (new Vector<Address>(n));
        }
        else if (qName.equals (DataXmlConst.TAG_CATEGORIES_STREAM))
        {
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT)) + 1;
            storage.setCategories (new Hashtable<Integer,Category>(n));
        }
        else if (qName.equals (DataXmlConst.TAG_ICONS_STREAM))
        {
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT)) + 1;
            storage.setIcons (new Icon[n]);
        }
        else if (qName.equals (DataXmlConst.TAG_CROSSROAD))
        {
            int id = atoi (attributes.getValue (DataXmlConst.ATTR_ID));
            int uid = atoi (attributes.getValue (DataXmlConst.ATTR_UID));
            int x = atoi (attributes.getValue (DataXmlConst.ATTR_X));
            int y = atoi (attributes.getValue (DataXmlConst.ATTR_Y));
            int n = atoi (attributes.getValue (DataXmlConst.ATTR_COUNT));

            currentPc = new Crossroad (id, uid, x, y, n);
            storage.getCrossroads()[id] = currentPc;

            currentConN = 0;
        }
        else if (qName.equals (DataXmlConst.TAG_CON))
        {
            int pc = atoi (attributes.getValue (DataXmlConst.ATTR_CROSSROAD));
            int street = atoi (attributes.getValue (DataXmlConst.ATTR_STREET));
            int type = atoi (attributes.getValue (DataXmlConst.ATTR_TYPE));
            int direction = atoi (attributes.getValue (DataXmlConst.ATTR_DIRECTION));

            ConBlock ct = new ConBlock (pc, street, direction);
            ct.type = type;
            if (currentPc != null)
                currentPc.con[currentConN++] = ct;
        }
        else if (qName.equals (DataXmlConst.TAG_STREET))
        {
            int id = atoi (attributes.getValue (DataXmlConst.ATTR_ID));
            int uid = atoi (attributes.getValue (DataXmlConst.ATTR_UID));
            String name = attributes.getValue (DataXmlConst.ATTR_NAME);
            String type = attributes.getValue (DataXmlConst.ATTR_TYPE);
            String village = attributes.getValue (DataXmlConst.ATTR_VILLAGE);

            storage.getStreets()[id] = new Street (id, uid, name, type, village);
        }
        else if (qName.equals (DataXmlConst.TAG_POINT))
        {
            int id = atoi (attributes.getValue (DataXmlConst.ATTR_ID));
            int uid = atoi (attributes.getValue (DataXmlConst.ATTR_UID));
            int x = atoi (attributes.getValue (DataXmlConst.ATTR_X));
            int y = atoi (attributes.getValue (DataXmlConst.ATTR_Y));
            String name = attributes.getValue (DataXmlConst.ATTR_NAME);
            int category = atoi (attributes.getValue (DataXmlConst.ATTR_CATEGORY));

            Properties meta = new Properties();
            int n = attributes.getLength();
            for (int i=0; i < n; i++)
            {
                String at = attributes.getQName(i);
                if (at.startsWith("meta."))
                {
                    String k = at.substring(5);
                    String v = attributes.getValue(i);
                    meta.setProperty(k, v);
                }
            }

            if (name != null)
            {
                if (id >= storage.getPois().size())
                    storage.getPois().setSize(id + 1);
                storage.getPois().set(id, new POI (new Building (id, uid, x, y, name), category, meta));
            }
        }
        else if (qName.equals (DataXmlConst.TAG_ADDRESS))
        {
            int id = atoi (attributes.getValue (DataXmlConst.ATTR_ID));
            int uid = atoi (attributes.getValue (DataXmlConst.ATTR_UID));
            int x = atoi (attributes.getValue (DataXmlConst.ATTR_X));
            int y = atoi (attributes.getValue (DataXmlConst.ATTR_Y));
            String name = attributes.getValue (DataXmlConst.ATTR_NAME);
//            Reference ref = getAttributeAsRef (attributes, DataXmlConst.ATTR_REFERENCE);
            int street = atoi (attributes.getValue (DataXmlConst.ATTR_STREET));

            if (name != null)
            {
                if (id >= storage.getAddresses().size())
                    storage.getAddresses().setSize(id + 1);
                storage.getAddresses().set(id, new Address (id, uid, x, y, name, street));
            }
        }
        else if (qName.equals (DataXmlConst.TAG_CATEGORY))
        {
            Category category = new Category ();
            int id = getAttributeAsInt (attributes, DataXmlConst.ATTR_ID);
            category.setUid (getAttributeAsInt (attributes, DataXmlConst.ATTR_UID));
            category.setName (getAttribute (attributes, DataXmlConst.ATTR_NAME));
            category.setParent (getAttributeAsInt (attributes, DataXmlConst.ATTR_PARENT));
            category.setColor (getAttributeAsColor (attributes, DataXmlConst.ATTR_COLOR));
            category.setFontName (getAttribute (attributes, DataXmlConst.ATTR_FONT_NAME));
            category.setOnline (getAttributeAsBoolean (attributes, DataXmlConst.ATTR_ONLINE));
            category.setFontSize (getAttributeAsInt (attributes, DataXmlConst.ATTR_FONT_SIZE));
            category.setFontStyle (getAttributeAsInt (attributes, DataXmlConst.ATTR_FONT_STYLE));
            category.setPointSize (getAttributeAsInt (attributes,
                DataXmlConst.ATTR_POINT_SIZE));
            category.setLabel (getAttribute (attributes, DataXmlConst.ATTR_LABEL));
            category.setShowIcon(XMLUtil.getAttributeAsBoolean(attributes, "showIcon", true));
            category.setShowList(XMLUtil.getAttributeAsBoolean(attributes, "showList", true));
            category.setShowLabel(XMLUtil.getAttributeAsBoolean(attributes, "showLabel", true));
            category.setShowDot(XMLUtil.getAttributeAsBoolean(attributes, "showDot", true));

            if (category.getName() != null)
            {
                category.setId (id);
                category.setUid (id);  /////////////////////////// uid == id
                storage.getCategories().put(id, category);

                int n = attributes.getLength();
                Map<String,String> catAttributes = new Hashtable(n);
                for (int i = 0; i < n; i++)
                {
                    String atName = attributes.getQName(i);
                    String atValue = attributes.getValue(i);
                    catAttributes.put(atName, atValue);
                }
                category.setAttributes(catAttributes);
            }
        }
        else if (qName.equals (DataXmlConst.TAG_ICON))
        {
            currentPhoto = new Icon ();
            int id = getAttributeAsInt (attributes, DataXmlConst.ATTR_ID);
            currentPhoto.uid = getAttributeAsInt (attributes, DataXmlConst.ATTR_UID);
            currentPhoto.setFileName (getAttribute (attributes, DataXmlConst.ATTR_FILE));
            int n = getAttributeAsInt (attributes, DataXmlConst.ATTR_COUNT);

            if (checkId (id, storage.getIcons().length) && n > 0)
            {
                currentPhoto.id = id;
                currentPhoto.setPlaces(new Reference[n]);
                storage.getIcons()[id] = currentPhoto;
                currentPlaceN = 0;
            }
        }
        else if (qName.equals (DataXmlConst.TAG_PLACE))
        {
            currentPhoto.getPlaces()[currentPlaceN++] = getAttributeAsRef (attributes,
                DataXmlConst.ATTR_REFERENCE);
        }
    }

    /**
     * Converts String to int. 0 is default value
     * @param s String
     * @return int
     */
    private int atoi (String s)
    {
        int res = 0;
        if (s != null)
        {
            try
            {
                res = Integer.parseInt (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute
     * @param attributes Attributes
     * @param attrName String
     * @return String
     */
    private String getAttribute (Attributes attributes, String attrName)
    {
        return attributes.getValue (attrName);
    }

    /**
     * Get attribute as integer
     * @param attributes Attributes
     * @param attrName String
     * @return int
     */
    private int getAttributeAsInt (Attributes attributes, String attrName)
    {
        int res = 0;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Integer.parseInt (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as integer
     * @param attributes Attributes
     * @param attrName String
     * @return double
     */
    private double getAttributeAsDouble (Attributes attributes, String attrName)
    {
        double res = 0;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Double.parseDouble (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as reference
     * @param attributes Attributes
     * @param attrName String
     * @return Reference
     */
    private Reference getAttributeAsRef (Attributes attributes, String attrName)
    {
        Reference res = null;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            int n = s.indexOf (DataXmlConst.SEPARATOR);
            if (n > 0)
            {
                try
                {
                    int classid = Integer.parseInt (s.substring (0, n));
                    int objectid = Integer.parseInt (s.substring (n + 1));
                    res = new Reference (classid, objectid);
                }
                catch (NumberFormatException ex)
                {
                }
            }
        }
        return res;
    }

    /**
     * Get value of node's attrinute converted to Color
     * @param attributes Attributes
     * @param attrName String
     * @return int
     */
    private Color getAttributeAsColor (Attributes attributes, String attrName)
    {
        Color res = Color.GRAY;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = new Color (Integer.parseInt (s, 16));
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Get attribute as boolean
     * @param attributes Attributes
     * @param attrName String
     * @return boolean
     */
    private boolean getAttributeAsBoolean (Attributes attributes, String attrName)
    {
        boolean res = false;
        String s = attributes.getValue (attrName);
        if (s != null)
        {
            try
            {
                res = Boolean.parseBoolean (s);
            }
            catch (NumberFormatException ex)
            {
            }
        }
        return res;
    }

    /**
     * Check if id is in legal range
     * @param id int
     * @param count int
     * @return boolean
     */
    private boolean checkId (int id, int count)
    {
        return id > 0 && id < count;
    }

}
