/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

import java.awt.Color;
import java.util.*;
import java.util.List;

import com.ish.mobimapbuilder.optimizer.PolylineSimplifier;
import com.ish.mobimapbuilder.optimizer.Splitter;
import com.ish.mobimapbuilder.util.CaseInsensitiveHashMap;
import com.ish.mobimapbuilder.util.StringUtils;

public class Layer {
    static public final int TYPE_GRAPHIC = 1;
    static public final int TYPE_STREETS = 2;
    static public final int TYPE_ADDRESSES = 3;

    private static int counter = 1;

    private List<Record> records = null;
    private List<Field> fields = null;

    private Map<String, String> fieldAssociation = null;
    private Map<String, Integer> fieldIndexes = null;

    private Map<String, String> params = null;

    private int type = 0;
    private String name;

    private String geometryFileName = null;
    private String dataFileName = null;
    private Color fillColor = null;
    private Color outlineColor = null;
    private String targetId = null;
    private String encoding = null;

    private Map<String, String> optimizerMethods = null;

    private final static String OPTIMIZER_SPLIT = "split";
    private final static String OPTIMIZER_SIMPLIFY = "simplify";

    private List<Table> tables;

    public Layer (int type, String name) {
        this.type = type;
        this.name = StringUtils.isNullOrEmpty(name)? ("layer_" + counter) : name;

        records = new Vector<Record>(100, 100);
        fieldAssociation = new CaseInsensitiveHashMap<String>();
        optimizerMethods = new CaseInsensitiveHashMap<String>();
        params = new CaseInsensitiveHashMap<String>();
        tables = new ArrayList<Table>();

        fieldIndexes = new CaseInsensitiveHashMap<Integer>();

        counter++;
    }

    public void addRecord (Record r) {
        records.add (r);
    }

    /**
     * Apply optimizations to layer
     */
    public void optimize () {
        // simplify
        String toleranceS = optimizerMethods.get (OPTIMIZER_SIMPLIFY);
        if (toleranceS != null) {
            double tolerance = Double.parseDouble (toleranceS);
            PolylineSimplifier simplifier = new PolylineSimplifier (tolerance);

            for (ListIterator<Record> jt = records.listIterator (); jt.hasNext (); ) {
                Record record = jt.next ();
                simplifier.make (record);
            }
        }

        // split
        String val = optimizerMethods.get (OPTIMIZER_SPLIT);
        if (val != null) {
            int step = Integer.parseInt (val);
            Splitter splitter = new Splitter (step);

            List<Record> newList = new Vector<Record>(100, 100);

            for (ListIterator<Record> jt = records.listIterator (); jt.hasNext (); ) {
                Record record = jt.next ();

                newList.addAll (splitter.make (record));
            }
            records = newList;
        }
    }


    public String toString () {
        return "Layer \"" + name + "\" [Geometry: " + geometryFileName +
            " Data: " + dataFileName + " Target: " + targetId + "]";
    }

    public String getName() {
        return name;
    }

    public List<Record> getRecords () {
        return records;
    }

    public Color getOutlineColor () {
        return outlineColor;
    }

    public String getGeometryFileName () {
        return geometryFileName;
    }

    public Color getFillColor () {
        return fillColor;
    }

    public void setDataFileName (String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public void setOutlineColor (Color outlineColor) {
        this.outlineColor = outlineColor;
    }

    public void setGeometryFileName (String geometryFileName) {
        this.geometryFileName = geometryFileName;
    }

    public void setFillColor (Color fillColor) {
        this.fillColor = fillColor;
    }

    public String getDataFileName () {
        return dataFileName;
    }

    public int getType () {
        return type;
    }

    public String getEncoding () {
        return encoding;
    }

    public String getTargetId () {
        return targetId;
    }

    public void setFields (List<Field> list) {
        fields = list;
        for (ListIterator<Field> it = fields.listIterator (); it.hasNext (); ) {
            Field f = it.next ();
            String name = f.getName ();
            int index = f.getIndex ();
            fieldIndexes.put (name, new Integer (index));
        }
    }

    public List<Field> getFields() {
        return fields;
    }

    public int getFieldCount() {
        return fields.size();
    }

    public void setEncoding (String encoding) {
        this.encoding = encoding;
    }

    public void setTargetId (String targetId) {
        this.targetId = targetId;
    }

    public void addFieldAssociation (String source, String target) {
        fieldAssociation.put (target, source);
    }

    public String getAssociatedField (String target) {
        return fieldAssociation.get (target);
    }

    public int getFieldIndex (String target) {
        int res = -1;
        String s = fieldAssociation.get (target);
        if (s != null)
            target = s;

        Integer i = fieldIndexes.get (target);
        if (i != null)
            res = i.intValue ();
        return res;
    }

    public int getNativeFieldIndex (String s) {
        int res = -1;
        Integer i = fieldIndexes.get (s);
        if (i != null)
            res = i.intValue ();
        return res;
    }

    public void addParam (String name, String value) {
        params.put (name, value);
    }

    public String getParam (String name) {
        return params.get (name);
    }

    public boolean getParamAsBoolean (String name, boolean defaultValue) {
        String s = getParam (name);
        if (s != null) {
            try {
                boolean r = Boolean.parseBoolean (s);
                return r;
            } catch (Exception ex) {
            }
        }
        return defaultValue;
    }

    public int getParamAsInt (String name, int defaultValue) {
        String s = getParam (name);
        if (s != null) {
            try {
                int r = Integer.parseInt (s);
                return r;
            } catch (Exception ex) {
            }
        }
        return defaultValue;
    }

    public double getParamAsDouble (String name, double defaultValue) {
        String s = getParam (name);
        if (s != null) {
            try {
                double r = Double.parseDouble (s);
                return r;
            } catch (Exception ex) {
            }
        }
        return defaultValue;
    }

    public Color getParamAsColor (String name, Color defaultValue) {
        String s = getParam (name);
        if (s != null) {
            try {
                int r = Integer.parseInt (s, 16);
                return new Color (r);
            } catch (Exception ex) {
            }
        }
        return defaultValue;
    }

    public void addOptimizerMethod (String method, String value) {
        optimizerMethods.put (method, value);
    }

    public String getOptimizerMethod (String method) {
        return optimizerMethods.get (method);
    }

    /**
     * Describe table structure
     * @return String
     */
    public String[] describeFields() {
        String[] result = new String[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            result[i] = fields.get(i).toString();
        }
        return result;
    }

    public void addTable (String source, String target,
                          List<String> caseSources, List<String> caseTargets) {
        Table t = new Table ();
        t.target = target;
        StringTokenizer st = new StringTokenizer (source, " ,");
        while (st.hasMoreTokens ()) {
            String token = st.nextToken ();
            t.variables.add (token);
        }

        for (int i = 0; i < caseSources.size (); i++) {
            String cs = caseSources.get (i);
            String ct = caseTargets.get (i);

            t.cases.put (cs, ct);
        }
        tables.add (t);
    }

    public boolean hasTables () {
        return tables.size () > 0;
    }

    public Map<String, String> resolveTables (Map<String, String> values) {
        Map<String, String> result = new CaseInsensitiveHashMap<String>();

        for (Table table : tables) {
            // parse table.source and change variables to values
            String value = null;
            for (String var : table.variables) {
                String v = values.get (var);
                if (v == null)
                    break;

                if (value == null)
                    value = v;
                else
                    value += "," + v;
            }
            String rt = table.cases.get (value);
            if (rt != null)
                result.put (table.target, rt);
        }
        return result;
    }

    public List<Table> getTables () {
        return tables;
    }

    public class Table {
        Map<String, String> cases;
        List<String> variables;
        String target;

        public Table () {
            cases = new CaseInsensitiveHashMap<String>();
            variables = new ArrayList<String>();
        }
    }
}
