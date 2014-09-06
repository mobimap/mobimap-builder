/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.io.*;
import java.util.*;
import java.awt.Color;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.ish.mobimapbuilder.model.Layer;
import com.ish.mobimapbuilder.source.*;
import com.ish.mobimapbuilder.data.City;
import com.ish.mobimapbuilder.target.*;
import com.ish.mobimapbuilder.xml.XMLUtil;

public class ProjectLoader {
    Project project;
    Logger logger;

    public ProjectLoader (Logger logger) {
        project = new Project ();
        this.logger = logger;
    }

    public boolean load (String fileName) {
        logger.message("Loading project...");

        try {
            InputStream is = new FileInputStream (fileName);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
            DocumentBuilder docbuilder = factory.newDocumentBuilder ();
            Document xmldoc = docbuilder.parse (is);

            Element projectNode = xmldoc.getDocumentElement ();

            for (Node layerNode = projectNode.getFirstChild (); layerNode != null; layerNode = layerNode.getNextSibling ()) {
                String layerNodeName = layerNode.getNodeName ();
                if (layerNodeName.equals ("layer")) {
                    Node attrType = layerNode.getAttributes ().getNamedItem ("type");
                    if (attrType == null) {
                        continue;
                    }

                    String type = attrType.getNodeValue ();

                    int layerType = (type.compareTo ("streets") == 0 ?
                                   Layer.TYPE_STREETS :
                                   (type.compareTo ("graphic") == 0 ?
                                    Layer.TYPE_GRAPHIC :
                                    (type.compareTo ("addresses") == 0 ?
                                     Layer.TYPE_ADDRESSES :
                                     -1)));

                    String layerName = XMLUtil.getAttributeValue (layerNode.getAttributes (), "name");
                    Layer layer = new Layer (layerType, layerName);

                    for (Node layerChildNode = layerNode.getFirstChild ();
                                               layerChildNode != null;
                                               layerChildNode = layerChildNode.getNextSibling ()) {
                        NamedNodeMap attribs = layerChildNode.getAttributes ();
                        if (attribs == null) {
                            continue;
                        }

                        String value = XMLUtil.getAttributeValue (attribs, "value");

                        if (layerChildNode.getNodeName ().equals ("geometry")) {
                            layer.setGeometryFileName (value);
                        }
                        else if (layerChildNode.getNodeName ().equals ("data")) {
                            layer.setDataFileName (value);
                            layer.setEncoding (XMLUtil.getAttributeValue (attribs, "encoding", "CP1251"));
                        }
                        else if (layerChildNode.getNodeName ().equals ("fill")) {
                            layer.setFillColor (new Color (Integer.parseInt (value, 16)));
                        }
                        else if (layerChildNode.getNodeName ().equals ("outline")) {
                            layer.setOutlineColor (new Color (Integer.parseInt (value, 16)));
                        }
                        else if (layerChildNode.getNodeName ().equals ("target")) {
                            layer.setTargetId (value);
                        }
                        else if (layerChildNode.getNodeName ().equals ("field")) {
                            String source = XMLUtil.getAttributeValue (attribs, "source");
                            String target = XMLUtil.getAttributeValue (attribs, "target");

                            if (source != null && target != null) {
                                layer.addFieldAssociation (source, target);
                            }
                        }
                        else if (layerChildNode.getNodeName ().equals ("param")) {
                            String paramName = XMLUtil.getAttributeValue (attribs, "name");
                            String paramValue = XMLUtil.getAttributeValue (attribs, "value");

                            if (paramName != null && paramValue != null) {
                                layer.addParam (paramName, paramValue);
                            }
                        }
                        else if (layerChildNode.getNodeName ().equals ("optimizer")) {
                            String method = XMLUtil.getAttributeValue (attribs, "method");

                            if (method != null && value != null) {
                                layer.addOptimizerMethod (method, value);
                            }
                        }
                        else if (layerChildNode.getNodeName ().equals ("table")) {
                            String tableSource = XMLUtil.getAttributeValue (attribs, "source");
                            String tableTarget = XMLUtil.getAttributeValue (attribs, "target");

                            if (tableSource == null || tableTarget == null) {
                                continue;
                            }

                            List<String> caseSources = new ArrayList<String>();
                            List<String> caseTargets = new ArrayList<String>();

                            for (Node caseNode = layerChildNode.getFirstChild ();
                                                 caseNode != null;
                                                 caseNode = caseNode.getNextSibling ()) {
                                NamedNodeMap caseAttribs = caseNode.getAttributes ();
                                if (!"case".equals (caseNode.getNodeName ())) {
                                    continue;
                                }
                                if (caseAttribs == null) {
                                    continue;
                                }

                                String source = XMLUtil.getAttributeValue (caseAttribs, "source");
                                String target = XMLUtil.getAttributeValue (caseAttribs, "target");

                                if (source == null || target == null) {
                                    continue;
                                }

                                caseSources.add (source);
                                caseTargets.add (target);
                            }
                            layer.addTable (tableSource, tableTarget, caseSources, caseTargets);
                        }
                    }
                    project.addLayer (layer);
                }
                else if (layerNodeName.compareTo ("transform") == 0) {
                    NamedNodeMap attribs = layerNode.getAttributes ();
                    if (attribs == null) {
                        continue;
                    }

                    String x = XMLUtil.getAttributeValue (attribs, "dx");
                    String y = XMLUtil.getAttributeValue (attribs, "dy");
                    if (x != null) {
                        project.setTransformX (Double.parseDouble (x));
                    }
                    if (y != null) {
                        project.setTransformY (Double.parseDouble (y));
                    }
                }
                else if (layerNodeName.compareTo ("geo") == 0) {
                    CoordinatesConverter cc = CoordinatesConverter.getInstance (layerNode);
                    project.setCoordinatesConverter (cc);
                }
                else if (layerNodeName.compareTo ("targets") == 0) {
                    for (Node targetNode = layerNode.getFirstChild ();
                                           targetNode != null;
                                           targetNode = targetNode.getNextSibling ()) {
                        String nodeName = targetNode.getNodeName ();

                        Target target = null;
                        if (nodeName.compareTo ("data-target") == 0) {
                            target = new DataTarget ();
                        }
                        else if (nodeName.compareTo ("graphic-target") == 0) {
                            target = new GraphicTarget ();
                        }

                        if (target != null) {
                            if (target.load (targetNode)) {
                                project.addTarget (target);
                                target.setProject (project);
                            }
                        }
                    }
                }
                else if (layerNodeName.compareTo ("city") == 0) {
                    NamedNodeMap attribs = layerNode.getAttributes ();
                    if (attribs == null) {
                        continue;
                    }

                    City city = new City ();
                    city.load (layerNode);
                    project.setCity (city);

                    String name = XMLUtil.getAttributeValue (attribs, "name");
                    String x = XMLUtil.getAttributeValue (attribs, "x");
                    String y = XMLUtil.getAttributeValue (attribs, "y");

                    if (name != null && x != null && y != null) {
                        project.setCityName (name);
                        project.setCityX (Integer.parseInt (x));
                        project.setCityY (Integer.parseInt (y));
                    }
                }
                else if (layerNodeName.compareTo ("param") == 0) {
                    NamedNodeMap attribs = layerNode.getAttributes ();
                    if (attribs == null) {
                        continue;
                    }

                    String name = XMLUtil.getAttributeValue (attribs, "name");
                    String value = XMLUtil.getAttributeValue (attribs, "value");

                    if (Project.PARAM_SOURCE_CS.equals (name)) {
                        project.setSourceCS (value);
                    }

                    if (name != null && value != null) {
                        project.addParam (name, value);
                    }
                }
                else if (layerNodeName.compareTo ("mobimap") == 0) {
                    MobimapConfiguration mc = new MobimapConfiguration();
                    mc.load(layerNode);
                    project.setMobimapConfiguration(mc);
                    logger.message(mc);
                }
            }
            return true;
        } catch (FileNotFoundException ex) {
            logger.message ("ERROR. File '" + fileName + "' isn't found");
        } catch (IOException ex) {
            logger.message ("ERROR. Can't read file '" + fileName + "' ");
        } catch (SAXException ex) {
            logger.message ("ERROR. Can't parse XML in file '" + fileName + "' ");
        } catch (ParserConfigurationException ex) {
        }
        return false;
    }

    public void parse () {

        logger.message("\nReading layers...");

        List<Layer> layers = project.getLayers ();

        DataSourceLoader dp = new DataSourceLoader ();

        for (ListIterator<Layer> it = layers.listIterator (); it.hasNext (); ) {
            Layer layer = it.next ();
            logger.message (layer);
            try {
                dp.loadLayer (layer);

                int recordN = layer.getRecords().size();
                logger.message(recordN + " records are read.");

                logger.message("Structure:");
                String[] structure = layer.describeFields();
                for (String s: structure) {
                    logger.message("\t" + s);
                }

            } catch (Exception ex) {
                logger.message ("Error while processing layer: " + ex.getMessage ());
            }
        }
    }

    public void export () {

        logger.message("\nMaking layers...");

        Exporter exporter = new Exporter (logger);
        exporter.make (project);
    }
}
