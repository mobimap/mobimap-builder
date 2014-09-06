package com.ish.mobimapbuilder;

import org.w3c.dom.Node;
import com.ish.mobimapbuilder.xml.XMLUtil;
import org.w3c.dom.NamedNodeMap;
import java.util.Properties;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.*;

public class MobimapConfiguration {

    private Properties params;
    private List<Layer> layers;

    public MobimapConfiguration () {
        super ();
        params = new Properties ();
        layers = new Vector<Layer>();
    }

    public Properties getParams() {
        return params;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void load (Node rootNode) {

        for (Node childNode = rootNode.getFirstChild (); childNode != null; childNode = childNode.getNextSibling ()) {
            NamedNodeMap attribs = childNode.getAttributes ();
            if (attribs == null) {
                continue;
            }

            if (childNode.getNodeName ().equals ("param")) {
                String paramName = XMLUtil.getAttributeValue (attribs, "name");
                String paramValue = XMLUtil.getAttributeValue (attribs, "value");

                if (paramName != null && paramValue != null) {
                    addParam (paramName, paramValue);
                }
            } else if (childNode.getNodeName ().equals ("layers")) {
                parseLayers (childNode);
            }
        }
    }

    private void parseLayers (Node layersNode) {
        for (Node layerNode = layersNode.getFirstChild (); layerNode != null; layerNode = layerNode.getNextSibling ()) {
            if (layerNode.getNodeName ().equals ("layer")) {
                Layer layer = parseLayer (layerNode);
                layers.add (layer);
            }
        }
    }

    private Layer parseLayer (Node layerNode) {
        NamedNodeMap attribs = layerNode.getAttributes ();
        if (attribs == null) {
            return null;
        }

        String name = XMLUtil.getAttributeValue (attribs, "name");
        if (name == null) {
            return null;
        }
        Layer layer = new Layer (name);

        for (Node streamNode = layerNode.getFirstChild (); streamNode != null; streamNode = streamNode.getNextSibling ()) {
            if (streamNode.getNodeName ().equals ("stream")) {
                Stream stream = parseStream (streamNode);
                layer.addStream (stream);
            }
        }

        return layer;
    }

    private Stream parseStream (Node streamNode) {
        NamedNodeMap attribs = streamNode.getAttributes ();
        if (attribs == null) {
            return null;
        }

        Stream stream = new Stream ();
        stream.setSource (XMLUtil.getAttributeValue (attribs, "source"));

        int zoomMin = XMLUtil.getAttributeValueAsInt (attribs, "zoomMin", -1);
        if (zoomMin >= 0) {
            stream.put ("zoomMin", new Integer (zoomMin));
        }
        int zoomMax = XMLUtil.getAttributeValueAsInt (attribs, "zoomMax", -1);
        if (zoomMax >= 0) {
            stream.put ("zoomMax", new Integer (zoomMax));
        }
        stream.put ("filled", XMLUtil.getAttributeValueAsBoolean (attribs, "filled", true));
        stream.put ("outlined", XMLUtil.getAttributeValueAsBoolean (attribs, "outlined", true));
        int fill = XMLUtil.getAttributeValueAsColor (attribs, "fill", -1);
        if (fill >= 0) {
            stream.put ("fill", new Integer (fill));
        }
        int outline = XMLUtil.getAttributeValueAsColor (attribs, "outline", -1);
        if (outline >= 0) {
            stream.put ("outline", new Integer (outline));
        }
        int level = XMLUtil.getAttributeValueAsInt (attribs, "level", -1);
        if (level >= 0) {
            stream.put ("level", new Integer (level));
        }
        return stream;
    }

    private void addParam (String name, String value) {
        params.setProperty (name, value);
    }

    public String toString () {
        String s = "MobimapConfiguration.\n\tParams: " + params + ".";
        if (layers.size () > 0) {
            s += "\n\tLayers: ";
            for (Layer elem : layers) {
                s += "\n\t\t" + elem.toString ();
            }
            s += ".";
        } else {
            s += "\n\tNo layers defined";
        }
        return s;
    }

    public class Layer {
        private String name;
        private List<Stream> streams;

        Layer (String name) {
            this.name = name;
            streams = new Vector<Stream>();
        }

        void addStream (Stream stream) {
            streams.add (stream);
        }

        public String getName () {
            return name;
        }

        public List<Stream> getStreams () {
            return streams;
        }

        public String toString () {
            String s = "Name: \"" + name + "\". Streams: {";
            for (Stream stream : streams) {
                s += " { " + stream.toString () + " } ";
            }
            s += "}";
            return s;
        }
    }


    public static class Stream {
        private String source;
        private Map<String, Object> params;

        Stream () {
            params = new Hashtable<String, Object>();
        }

        void setSource(String id) {
            this.source = id;
        }

        void put (String key, Object value) {
            if (value != null) {
                params.put (key, value);
            }
        }

        public String getSource() {
            return source;
        }

        public Map<String, Object> getParams () {
            return params;
        }

        public String toString () {
            String s = "source: " + source + " params: ";

            Iterator<String> iter = params.keySet ().iterator ();
            while (iter.hasNext ()) {
                String item = iter.next ();
                s += "(" + item + "," + params.get (item) + ")";
            }

            return s;
        }
    }
}
