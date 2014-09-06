//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.parsers.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class I7MapParser
    extends DefaultHandler
{
    private InputStream is = null;

    private Project project;
    private Properties params;

    public I7MapParser (InputStream is)
    {
        this.is = is;
    }

    public Project make ()
    {
        project = new Project();
        params = project.getParams();

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
        return project;
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
        if (qName.equals ("param"))
        {
            String name = XMLUtil.getAttribute(attributes, "name");
            String value = XMLUtil.getAttribute(attributes, "value");
            if (name != null && value != null)
                params.put(name, value);
        }
        else if (qName.equals ("layer"))
        {
            String type = XMLUtil.getAttribute (attributes, "type");
            if (type == null)
                return;

            Layer layer = new Layer ();
            for (int i = 0; i < attributes.getLength (); i++)
            {
                String name = attributes.getQName (i);
                String value = attributes.getValue (i);

                layer.setAttribute (name, value);
            }
            project.addLayer (layer);
        }
        else if (qName.equals ("data"))
        {
            String file = XMLUtil.getAttribute (attributes, "file");
            if (file != null)
                project.setDataFileName(file);
        }
    }
}
