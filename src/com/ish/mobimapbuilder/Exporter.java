/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.io.*;
import java.util.*;
import java.util.List;

import java.awt.Color;

import com.ish.mobimapbuilder.model.*;
import com.ish.mobimapbuilder.data.*;
import com.ish.mobimapbuilder.target.*;
import com.ish.mobimapbuilder.transform.*;
import com.ish.mobimapbuilder.gg.*;
import com.ish.mobimapbuilder.util.*;

public class Exporter {
    Logger logger;

    public Exporter (Logger logger) {
        this.logger = logger;
    }

    /**
     * Transcode specified project
     * @param project Project
     */
    public void make (Project project) {

        // init targets
        initTargets (project);

        // process input
        processInput (project);

        // make layers
        List<Layer> layers = project.getLayers ();

        for (ListIterator<Layer> it = layers.listIterator (); it.hasNext (); ) {
            try {
                Layer layer = it.next ();
                logger.message(layer);

                switch (layer.getType ()) {
                case Layer.TYPE_GRAPHIC:
                    makeGraphicLayer (project, layer);
                    break;
                case Layer.TYPE_STREETS:
                    makeStreetsLayer (project, layer);
                    break;
                case Layer.TYPE_ADDRESSES:
                    makeAddressesLayer (project, layer);
                    break;
                }

                logger.message(layer.getRecords().size() + " records interpreted");

            } catch (ImporterException ex) {
                logger.message ("Importer Exception: " + ex.getMessage ());
            }
        }

        logger.message("\nSaving results...");

        // save and close targets
        List<I7Container> model = saveTargets (project);

        // send data to mobimap II
        MobimapConfiguration mobimapConfiguration = project.getMobimapConfiguration();
        if (mobimapConfiguration != null) {
            logger.message ("\nGenerating Mobimap files...");
            com.ish.isrt.Builder2IsrtFacade mf = new com.ish.isrt.Builder2IsrtFacade ();
            Properties ps = mobimapConfiguration.getParams ();
            ps.setProperty (".transformX", Double.toString (project.getTransformX ()));
            ps.setProperty (".transformY", Double.toString (project.getTransformY ()));

            String ox = project.getParam ("default.originx");
            String oy = project.getParam ("default.originy");

            if (ox != null && oy != null) {
                double oxd = Double.parseDouble (ox);
                double oyd = Double.parseDouble (oy);

                String nox = null;
                String noy = null;

                if (Project.CS_GEO.equals (project.getSourceCS ())) {
                    Geo2Meters gm = project.getGeo2meters ();
                    nox = Double.toString (gm.getX (oxd, oyd));
                    noy = Double.toString (gm.getY (oxd, oyd));
                }
                else {
                    nox = Double.toString (oxd + project.getTransformX ());
                    noy = Double.toString (oyd + project.getTransformY ());
                }

                if (nox != null && noy != null) {
                    ps.setProperty ("default.originx", nox);
                    ps.setProperty ("default.originy", noy);
                }
            }
            // call facade
            mf.process (model, mobimapConfiguration);
        }
    }

    /**
     * Inits targets
     * @param project Project
     */
    private void initTargets (Project project) {
        Map<String, Target> targets = project.getTargets ();
        Set<String> keys = targets.keySet ();
        for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
            String key = it.next ();
            Target target = targets.get (key);
            try {
                target.init ();
            } catch (IOException ex1) {
                logger.message ("Can't init target: " + ex1.getMessage ());
            }
        }
    }

    /**
     * Saves and closes targets
     * @param project Project
     * @return List
     */
    private List<I7Container> saveTargets (Project project) {
        List<I7Container> model = new ArrayList<I7Container>();
        Map<String, Target> targets = project.getTargets ();
        Set<String> keys = targets.keySet ();
        for (Iterator<String> it = keys.iterator (); it.hasNext (); ) {
            String key = it.next ();
            Target target = targets.get (key);

            try {
                target.save ();
            } catch (IOException ex) {
                logger.message ("Error saving target: " + target.getId ());
                continue;
            }

            I7Container container = new I7Container ();
            container.setFileName (target.getFileName ());
            container.setCityName (project.getCityName ());
            container.setPlain (target.isPlain ());
            container.addPacket (target.pack ());
            container.save ();
            model.add (container);
        }
        return model;
    }

    private void processInput (Project project) {
        // find bounds
        Rectangle2 bounds = findBounds (project);

        // transform
        City city = project.getCity ();
        city.setBounds (bounds);

        if (Project.CS_GEO.equals (project.getSourceCS ())) {
            // source in geo
            Geo2Meters gm = new Geo2Meters (bounds.x, bounds.y, bounds.x2, bounds.y2);
            project.setGeo2meters (gm);

            for (Layer layer : project.getLayers ()) {
                for (Record record : layer.getRecords ()) {
                    Shape[] shapes = record.getShapes ();

                    for (int k = 0; k < shapes.length; k++) {
                        Shape shape = shapes[k];
                        int coorsN = shape.xCoords.length;

                        for (int m = 0; m < coorsN; m++) {
                            double xc = shape.xCoords[m], yc = shape.yCoords[m];
                            shape.xCoords[m] = gm.getX (yc, xc);
                            shape.yCoords[m] = gm.getY (yc, xc);
                        }
                    }
                }
            }
        } // if geo
        else if (Project.CS_METRIC.equals (project.getSourceCS ())) {
            // source is metric
            CoordinatesConverter cc = project.getCoordinatesConverter ();
            if (cc != null) {
                // transform bounds and find bounds in lat-lon
                double xmin = bounds.x;
                double ymin = bounds.y;
                double xmax = bounds.x2;
                double ymax = bounds.y2;

                double lat1 = cc.getLat (xmin, ymin);
                double lat2 = cc.getLat (xmax, ymax);
                double lat3 = cc.getLat (xmin, ymax);
                double lat4 = cc.getLat (xmax, ymin);

                double lon1 = cc.getLon (xmin, ymin);
                double lon2 = cc.getLon (xmax, ymax);
                double lon3 = cc.getLon (xmin, ymax);
                double lon4 = cc.getLon (xmax, ymin);

                double west = Math.min (Math.min (lon1, lon2), Math.min (lon3, lon4));
                double east = Math.max (Math.max (lon1, lon2), Math.max (lon3, lon4));
                double north = Math.max (Math.max (lat1, lat2), Math.max (lat3, lat4));
                double south = Math.min (Math.min (lat1, lat2), Math.min (lat3, lat4));

                Geo2Meters gm = new Geo2Meters (west, south, east, north);
                project.setGeo2meters (gm);

                // transform metric to geo
                for (Layer layer : project.getLayers ()) {
                    for (Record record : layer.getRecords ()) {
                        Shape[] shapes = record.getShapes ();

                        for (int k = 0; k < shapes.length; k++) {
                            Shape shape = shapes[k];
                            int coorsN = shape.xCoords.length;

                            for (int m = 0; m < coorsN; m++) {
                                double xc = shape.xCoords[m];
                                double yc = shape.yCoords[m];
                                double lat = cc.getLat (xc, yc);
                                double lon = cc.getLon (xc, yc);
                                double xt = gm.getX (lat, lon);
                                double yt = gm.getY (lat, lon);
                                shape.xCoords[m] = xt;
                                shape.yCoords[m] = yt;
                            }
                        }
                    }
                }
            }
        } // if metric
        else {
            project.setTransformX ( -bounds.x);
            project.setTransformY ( -bounds.y);

            double dx = project.getTransformX ();
            double dy = project.getTransformY ();

            // offset transform
            for (Layer layer : project.getLayers ()) {
                for (Record record : layer.getRecords ()) {
                    Shape[] shapes = record.getShapes ();

                    for (int k = 0; k < shapes.length; k++) {
                        Shape shape = shapes[k];
                        int coorsN = shape.xCoords.length;

                        for (int m = 0; m < coorsN; m++) {
                            shape.xCoords[m] += dx;
                            shape.yCoords[m] += dy;
                        }
                    }
                }
            }
        } // if transform

        // update bounds
        city.setBounds (findBounds (project));
    }

    private Rectangle2 findBounds (Project project) {
        double xmin = Double.MAX_VALUE, xmax = Double.MIN_VALUE;
        double ymin = Double.MAX_VALUE, ymax = Double.MIN_VALUE;

        for (Layer layer : project.getLayers ()) {
            for (Record record : layer.getRecords ()) {
                Shape[] shapes = record.getShapes ();

                for (int k = 0; k < shapes.length; k++) {
                    Shape shape = shapes[k];
                    int coorsN = shape.xCoords.length;

                    for (int m = 0; m < coorsN; m++) {
                        double xc = shape.xCoords[m], yc = shape.yCoords[m];
                        if (xc < xmin) {
                            xmin = xc;
                        }
                        if (xc > xmax) {
                            xmax = xc;
                        }
                        if (yc < ymin) {
                            ymin = yc;
                        }
                        if (yc > ymax) {
                            ymax = yc;
                        }
                    }
                }
            }
        }

        double exp = Math.max((xmax - xmin) / 1000, (ymax - ymin) / 1000);

        return new Rectangle2 (xmin - exp, ymin - exp, xmax + exp, ymax + exp);
    }

    /**
     * Transcode graphic layer
     * @param project Project
     * @param layer Layer
     * @throws ImporterException
     */
    private void makeGraphicLayer (Project project, Layer layer) throws ImporterException {

        Target targetA = getLayerTarget (project, layer);

        if (targetA instanceof GraphicTarget) {
            makeGraphicLayerForGraphicTarget (layer, (GraphicTarget) targetA);
        } else if (targetA instanceof DataTarget) {
            makeGraphicLayerForDataTarget (layer, (DataTarget) targetA);
        } else {
            throw new ImporterException ("Target must be specified!");
        }
    }

    private void makeGraphicLayerForDataTarget (Layer layer, DataTarget dataTarget) throws
        ImporterException {

        boolean isAutoid = layer.getParamAsBoolean ("autoid", false);

        int categoryIdConst = layer.getParamAsInt ("category", 0);

        int categoryIdIndex = layer.getFieldIndex ("category");
        int nameIndex = layer.getFieldIndex ("name");
        int iconIndex = layer.getFieldIndex ("icon");
        int idIndex = layer.getFieldIndex ("id");

        List<Record> records = layer.getRecords ();
        for (ListIterator<Record> jt = records.listIterator (); jt.hasNext (); ) {
            Record record = jt.next ();
            Shape[] shapes = record.getShapes ();

            // tables support
            Map<String, String> table = new Hashtable<String, String>();
            if (layer.hasTables ()) {
                String a = record.getFieldAsString (layer.getNativeFieldIndex ("polish.geometry"));
                if (a != null) {
                    table.put ("$geometry", a);
                }
                a = record.getFieldAsString (layer.getNativeFieldIndex ("polish.type"));
                if (a != null) {
                    table.put ("$type", a);
                }
                a = record.getFieldAsString (layer.getNativeFieldIndex ("polish.typeHex"));
                if (a != null) {
                    table.put ("$typeHex", a);
                }

                if (categoryIdIndex >= 0) {
                    String cat = record.getFieldAsString (categoryIdIndex);
                    table.put ("category", cat);
                }
            }
            Map<String, String> vals = layer.resolveTables (table);

            String categoryFromTables = vals.get ("CATEGORY");

            int dataCategoryId = categoryFromTables == null ?
                                 ((categoryIdIndex >= 0) ?
                                  record.getFieldAsInt (categoryIdIndex) : categoryIdConst) :
                                 Integer.parseInt (categoryFromTables);

            String dataName = record.getFieldAsString (nameIndex);
            String dataIcon = record.getFieldAsString (iconIndex);

            for (int k = 0; k < shapes.length; k++) {
                Shape shape = shapes[k];
                int coorsN = shape.xCoords.length;

                if (coorsN == 0) {
                    continue;
                }

                double xc = shape.xCoords[0], yc = shape.yCoords[0];

                if (coorsN > 1) {
                    for (int u = 1; u < coorsN; u++) {
                        xc += shape.xCoords[u];
                        yc += shape.yCoords[u];
                    }
                }

                int dataId = isAutoid ? dataTarget.getNextId () : record.getFieldAsInt (idIndex);
                Map<String, String> meta = readMeta (layer, record);

                dataTarget.addPOI (xc / coorsN, yc / coorsN,
                                   dataId, dataCategoryId, dataName, dataIcon, meta);
            }
        }
    }

    private void makeGraphicLayerForGraphicTarget (Layer layer, GraphicTarget graphicTarget)
        throws ImporterException {

        Color fillColor = layer.getFillColor ();
        if (fillColor == null) {
            fillColor = layer.getParamAsColor ("fill", null);
        }
        Color outlineColor = layer.getOutlineColor ();
        if (outlineColor == null) {
            outlineColor = layer.getParamAsColor ("outline", null);
        }
        boolean isFilled = layer.getParamAsBoolean ("filled", true);
        boolean isOutlined = layer.getParamAsBoolean ("outlined", true);

        layer.optimize ();

        try {
            if (fillColor != null) {
                graphicTarget.gSetFillColor (fillColor);
            }

            if (outlineColor != null) {
                graphicTarget.gSetOutlineColor (outlineColor);
            }

            List<Record> records = layer.getRecords ();
            for (ListIterator<Record> jt = records.listIterator (); jt.hasNext (); ) {
                Record record = jt.next ();

                Shape[] shapes = record.getShapes ();

                Color recordFillColor = fillColor;
                Color recordOutlineColor = outlineColor;

                // tables support
                Map<String, String> table = new Hashtable<String, String>();
                if (layer.hasTables ()) {
                    String a = record.getFieldAsString (layer.getNativeFieldIndex ("polish.geometry"));
                    if (a != null) {
                        table.put ("$geometry", a);
                    }
                    a = record.getFieldAsString (layer.getNativeFieldIndex ("polish.type"));
                    if (a != null) {
                        table.put ("$type", a);
                    }
                    a = record.getFieldAsString (layer.getNativeFieldIndex ("polish.typeHex"));
                    if (a != null) {
                        table.put ("$typeHex", a);
                    }
                }
                Map<String, String> vals = layer.resolveTables (table);

                Brush brush = record.getBrush ();
                if (isFilled && recordFillColor == null && brush != null) {
                    recordFillColor = new Color (brush.getColor ());
                }

                Pen pen = record.getPen ();
                if (isOutlined && recordOutlineColor == null && pen != null) {
                    recordOutlineColor = new Color (pen.getColor ());
                }

                if (layer.hasTables ()) {
                    if (vals.get ("FILL") != null) {
                        recordFillColor = new Color (Integer.parseInt (vals.get ("FILL"), 16));
                    }
                    if (vals.get ("OUTLINE") != null) {
                        recordOutlineColor = new Color (Integer.parseInt (vals.get ("OUTLINE"), 16));
                    }
                }

                if (recordFillColor == null && recordOutlineColor == null) {
                    continue; // ignore invisible records
                }

                if (recordFillColor != null) {
                    graphicTarget.gSetFillColor (recordFillColor);
                }
                if (recordOutlineColor != null) {
                    graphicTarget.gSetOutlineColor (recordOutlineColor);
                }

                for (int k = 0; k < shapes.length; k++) {
                    Shape shape = shapes[k];
                    int coorsN = shape.xCoords.length;

                    if (coorsN == 0) {
                        continue;
                    }

                    double xc = shape.xCoords[0], yc = shape.yCoords[0];

                    if (coorsN > 1) {
                        int x = (int) shape.xCoords[0];
                        int y = (int) shape.yCoords[0];

                        if (graphicTarget != null) {
                            graphicTarget.gMoveTo (x, y);
                        }

                        for (int u = 1; u < coorsN; u++) {
                            xc += shape.xCoords[u];
                            yc += shape.yCoords[u];

                            x = (int) shape.xCoords[u];
                            y = (int) shape.yCoords[u];

                            if (graphicTarget != null) {
                                graphicTarget.gLineTo (x, y);
                            }
                        }
                        if (recordFillColor != null && graphicTarget != null &&
                            shape.getType() == Shape.Type.POLYGON) {
                            graphicTarget.gFill ();
                        }
                        if (recordOutlineColor != null && graphicTarget != null) {
                            graphicTarget.gOutline ();
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new ImporterException (ex.getMessage ());
        }
    }

    private Map<String, String> readMeta (Layer layer, Record record) {
        String metaParam = layer.getParam ("meta");
        if (metaParam == null) {
            return null;
        }

        Map<String, String> meta = new Hashtable<String, String>();

        StringTokenizer st = new StringTokenizer (metaParam, ", ");
        while (st.hasMoreTokens ()) {
            String field = st.nextToken ();
            int ind = layer.getFieldIndex (field);

            String val = record.getFieldAsString (ind);
            if (val != null) {
                meta.put (field, val);
            }
            else {
                System.err.println ("Meta field '" + field + "' refers to non-existent layer field.");
            }
        }

        String page = layer.getParam ("page");
        if (page != null) {
            meta.put ("page", page);
        }

        return meta;
    }

    /**
     * Get target of specified layer
     * @param project Project
     * @param layer Layer
     * @return Target
     * @throws ImporterException
     */
    private Target getLayerTarget (Project project, Layer layer) throws
        ImporterException {
        String targetId = layer.getTargetId ();
        if (targetId == null) {
            throw new ImporterException ("no target specified for layer");
        }
        Target target = project.getTarget (targetId);
        if (target == null) {
            throw new ImporterException ("layer refers to non-existing target");
        }

        return target;
    }

    /**
     * Transcode streets layer
     * @param project Project
     * @param layer Layer
     * @throws ImporterException
     */
    private void makeStreetsLayer (Project project, Layer layer) throws ImporterException {
        int streetNameIndex = layer.getFieldIndex ("streetname");
        int streetTypeIndex = layer.getFieldIndex ("streettype");
        int streetVillageIndex = layer.getFieldIndex ("streetvillage");
        int streetIdIndex = layer.getFieldIndex ("id");
        int roadClassIndex = layer.getFieldIndex ("roadclass");
        int directionIndex = layer.getFieldIndex ("direction");
        int addressLeftFromIndex = layer.getFieldIndex ("addressleftfrom");
        int addressLeftToIndex = layer.getFieldIndex ("addressleftto");
        int addressRightFromIndex = layer.getFieldIndex ("addressrightfrom");
        int addressRightToIndex = layer.getFieldIndex ("addressrightto");

        List<Record> records = layer.getRecords ();

        Map<String, Integer> streetsMap = new HashMap<String, Integer>(100, 100);
        int streetN = 1;

        List<StreetRecord> streets = new Vector<StreetRecord>(100);

        GraphGenerator gg = new GraphGenerator ();

        String thresholdS = layer.getParam ("threshold");
        if (thresholdS != null) {
            gg.setThreshold (Integer.parseInt (thresholdS));
        }

        String intersectS = layer.getParam ("intersect");
        boolean isIntersect = false;
        if (intersectS != null) {
            isIntersect = intersectS.compareTo ("true") == 0;
            gg.setIntersectSegments (isIntersect);
            if (isIntersect) {
                Thread.currentThread ().setPriority (Thread.MIN_PRIORITY);
            }
            else {
                project.getParams ().setProperty ("mobimap.optimize.crossroads", "true");
            }
        }
        layer.optimize (); // simplify is supported

        // read list of street types
        String streetTypesS = layer.getParam ("streettypes");
        List<String> streetTypes = null;
        if (streetTypesS != null) {
            streetTypes = new Vector<String>();
            StringTokenizer st = new StringTokenizer (streetTypesS, " ,;");
            while (st.hasMoreTokens ()) {
                String token = st.nextToken ();
                streetTypes.add (token);
            }
        }

        // read and parse records
        int lim = 0;
        for (ListIterator<Record> jt = records.listIterator (); jt.hasNext (); ) {
            Record record = jt.next ();

            Object[] fields = record.getFields ();
            Shape[] shapes = record.getShapes ();

            String dataStreetId = record.getFieldAsString (streetIdIndex);
            String dataStreetName = record.getFieldAsString (streetNameIndex);
            String dataStreetType = record.getFieldAsString (streetTypeIndex);
            String dataStreetVillage = record.getFieldAsString (streetVillageIndex);
            int dataRoadClass = record.getFieldAsInt (roadClassIndex);
            int dataDirection = record.getFieldAsInt (directionIndex);
            int dataAddressLeftFrom = record.getFieldAsInt (addressLeftFromIndex);
            int dataAddressLeftTo = record.getFieldAsInt (addressLeftToIndex);
            int dataAddressRightFrom = record.getFieldAsInt (addressRightFromIndex);
            int dataAddressRightTo = record.getFieldAsInt (addressRightToIndex);

            // get street-id or add new street
            int streetId = 0;

            // remove brackets []
            if (!StringUtils.isNullOrEmpty (dataStreetName)) {
                dataStreetName = dataStreetName.replace ('~', ' ');

                int openB = dataStreetName.indexOf ('[');
                if (openB >= 0) {
                    int closeB = dataStreetName.indexOf (']');
                    if (closeB >= 0) {
                        dataStreetName = dataStreetName.substring (0, openB) +
                                         dataStreetName.substring (closeB + 1);
                    }
                    else {
                        dataStreetName = dataStreetName.substring (0, openB);
                    }

                    dataStreetName = dataStreetName.trim ();
                }
            }

            // match street to id
            if (!StringUtils.isNullOrEmpty (dataStreetName)) {
                String unique = makeUnique (new String[] {
                                            dataStreetId,
                                            dataStreetName,
                                            dataStreetType, dataStreetVillage
                });
                if (streetsMap.containsKey (unique)) {
                    streetId = streetsMap.get (unique).intValue ();
                }
                else {
                    streetId = streetN;
                    streetsMap.put (unique, new Integer (streetN));

                    String[] split = splitStreetNameAndType (streetTypes, dataStreetName);

                    String name = split[0];
                    String type = (dataStreetType == null) ? split[1] : dataStreetType;
                    String village = (dataStreetVillage == null) ? split[2] : dataStreetVillage;

                    streets.add (new StreetRecord (streetN, name, type, village, dataStreetId));

                    streetN++;
                    if (isIntersect) {
                        if (streetN % 100 == 0) {
                            logger.message ("Streets processed: " + streetN);
                        }
                    }
                }
            }

            // add segments to graph
            for (int k = 0; k < shapes.length; k++) {
                Shape shape = shapes[k];
                int coorsN = shape.xCoords.length;

                if (coorsN > 1) {
                    for (int u = 1; u < coorsN; u++) {
                        double x1 = shape.xCoords[u - 1];
                        double y1 = shape.yCoords[u - 1];
                        double x2 = shape.xCoords[u];
                        double y2 = shape.yCoords[u];

                        gg.createSeg (streetId, x1, y1, x2, y2, dataDirection, dataRoadClass);
                    }
                }
            }
            lim++;
            //if (lim == 2000) break;
        }

        logger.message ("Streets count: " + (streetN - 1));

        // send all calulated data to target
        Target targetA = getLayerTarget (project, layer);
        if (!(targetA instanceof DataTarget)) {
            throw new ImporterException ("data target is required!");
        }
        DataTarget target = (DataTarget) targetA;

        target.setCrossroads (gg.getCrossroads ());
        target.setStreets (streets);
    }

    private String makeUnique (String[] s) {
        String r = "";
        for (int i = 0; i < s.length; i++) {
            r += s[i] + '\u263C';
        }
        return r;
    }

    /**
     * Transcode addresses layer
     * @param project Project
     * @param layer Layer
     * @throws ImporterException
     */
    private void makeAddressesLayer (Project project, Layer layer) throws ImporterException {
        int streetIdIndex = layer.getFieldIndex ("streetid");
        int streetNameIndex = layer.getFieldIndex ("streetname");
        int houseNumberIndex = layer.getFieldIndex ("housenumber");
        int houseLetterIndex = layer.getFieldIndex ("houseletter");
        int houseBuildingIndex = layer.getFieldIndex ("housebuilding");

        int streetIdIndex2 = layer.getFieldIndex ("streetid2");
        int streetNameIndex2 = layer.getFieldIndex ("streetname2");
        int houseNumberIndex2 = layer.getFieldIndex ("housenumber2");
        int houseLetterIndex2 = layer.getFieldIndex ("houseletter2");
        int houseBuildingIndex2 = layer.getFieldIndex ("housebuilding2");

        boolean allowDuplicates = layer.getParamAsBoolean ("allowduplicates", false);

        // read list of street types
        String streetTypesS = layer.getParam ("streettypes");
        List<String> streetTypes = null;
        if (streetTypesS != null) {
            streetTypes = new Vector<String>();
            StringTokenizer st = new StringTokenizer (streetTypesS, " ,;");
            while (st.hasMoreTokens ()) {
                String token = st.nextToken ();
                streetTypes.add (token);
            }
        }

        List<Record> records = layer.getRecords ();
        List<AddressRecord> addresses = new Vector<AddressRecord>(1000, 1000);
        Map<String, AddressRecord> cache = new HashMap<String, AddressRecord>(1000);

        for (ListIterator<Record> jt = records.listIterator (); jt.hasNext (); ) {
            Record record = jt.next ();

            Object[] fields = record.getFields ();
            Shape[] shapes = record.getShapes ();

            String dataStreetId = record.getFieldAsString (streetIdIndex);
            String dataStreetName = record.getFieldAsString (streetNameIndex);
            String dataHouseNumber = record.getFieldAsString (houseNumberIndex);
            String dataHouseLetter = record.getFieldAsString (houseLetterIndex);
            String dataHouseBuilding = record.getFieldAsString (houseBuildingIndex);

            String dataStreetId2 = record.getFieldAsString (streetIdIndex2);
            String dataStreetName2 = record.getFieldAsString (streetNameIndex2);
            String dataHouseNumber2 = record.getFieldAsString (houseNumberIndex2);
            String dataHouseLetter2 = record.getFieldAsString (houseLetterIndex2);
            String dataHouseBuilding2 = record.getFieldAsString (houseBuildingIndex2);

            addOneAddress (streetIdIndex, allowDuplicates, streetTypes, addresses, cache, shapes,
                           dataStreetId, dataStreetName, dataHouseNumber, dataHouseLetter, dataHouseBuilding);
            addOneAddress (streetIdIndex2, allowDuplicates, streetTypes, addresses, cache, shapes,
                           dataStreetId2, dataStreetName2, dataHouseNumber2, dataHouseLetter2, dataHouseBuilding2);
        }

        logger.message ("Addresses count: " + addresses.size ());

        // send all addresses data to target
        Target targetA = getLayerTarget (project, layer);
        if (!(targetA instanceof DataTarget)) {
            throw new ImporterException ("data target is required!");
        }
        DataTarget target = (DataTarget) targetA;

        target.setAddresses (addresses);
        target.setAddressToStreetMatchSimple (streetTypes == null);
        target.setAddressToStreetMatchById (streetIdIndex >= 0);
        target.setTransformX (project.getTransformX ());
        target.setTransformY (project.getTransformY ());
    }

    private void addOneAddress (int streetIdIndex, boolean allowDuplicates, List<String>
        streetTypes, List<AddressRecord> addresses, Map<String, AddressRecord> cache, Shape[] shapes,
        String dataStreetId, String dataStreetName,
        String dataHouseNumber, String dataHouseLetter, String dataHouseBuilding) {

        if (!StringUtils.isNullOrEmpty (dataHouseNumber) &&
            (!StringUtils.isNullOrEmpty (dataStreetName) || streetIdIndex >= 0)) {
            double xc = 0, yc = 0;
            int total = 0;
            for (int k = 0; k < shapes.length; k++) {
                Shape shape = shapes[k];
                int coorsN = shape.xCoords.length;

                for (int u = 0; u < coorsN; u++) {
                    xc += shape.xCoords[u];
                    yc += shape.yCoords[u];
                    total++;
                }
            }
            if (total > 0) {
                String[] split = splitStreetNameAndType (streetTypes, dataStreetName);
                String streetName = split[0];
                String streetType = split[1];
                String streetVillage = split[2];

                String fullNumber = AddressRecord.formFullName (dataHouseNumber,
                    dataHouseLetter, dataHouseBuilding);

                if (fullNumber != null) {
                    double x = xc / total;
                    double y = yc / total;
                    AddressRecord ar = new AddressRecord (x, y,
                        dataStreetId, streetName, streetType,
                        streetVillage, fullNumber);
                    String key = makeUnique (new String[] {
                                             streetName, streetType, streetVillage,
                                             fullNumber, dataStreetId
                    });
                    if (allowDuplicates || cache.get (key) == null) {
                        addresses.add (ar);
                        cache.put (key, ar);
                    }
                }
            }
        }
    }

    /**
     * Splits street name into name, type and village. Type is matched against
     * streetTypes list. Village is part of string enclosed in brackets.
     * @param streetTypes List
     * @param dataStreetName String
     * @return String[] [0] - name, [1] - type, [2] - village. If omitted, then null
     */
    private String[] splitStreetNameAndType (List<String> streetTypes, String dataStreetName) {
        String type = null;
        String name = null;
        String village = null;

        if (dataStreetName != null) {
            dataStreetName = dataStreetName.replace ('.', ' ');

            int pos = dataStreetName.indexOf ('(');
            if (pos >= 0) {
                int end = dataStreetName.lastIndexOf (')');
                if (end > pos) {
                    village = dataStreetName.substring (pos + 1, end);
                    dataStreetName = dataStreetName.substring (0, pos) +
                                     dataStreetName.substring (end + 1);
                }
            }

            boolean haveType = false;
            name = "";
            if (streetTypes != null) {
                StringTokenizer st = new StringTokenizer (dataStreetName, " ");
                while (st.hasMoreTokens ()) {
                    String token = st.nextToken ();

                    String tokenLower = token.toLowerCase ();

                    boolean match = false;
                    for (String oneType : streetTypes) {
                        // value can be "str" or "str.", but "." should stay unchanged
                        if (oneType.equals (tokenLower)) {
                            match = true;
                            type = oneType;
                            break;
                        }
                        else if (oneType.startsWith (tokenLower) &&
                                 oneType.charAt (tokenLower.length ()) == '.') {
                            match = true;
                            type = oneType;
                            break;
                        }
                    }

                    if (match) {
                        haveType = true;
                    }
                    else {
                        name += token + " ";
                    }
                }
            }
            if (!haveType) {
                name = dataStreetName;
            }

            name = name.trim ();
        }

        return new String[] {name, type, village};
    }
}
