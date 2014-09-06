//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.awt.*;
import java.awt.geom.*;

import java.awt.datatransfer.*;

import javax.swing.ImageIcon;

import com.ish.isrt.core.data.*;
import java.awt.event.MouseEvent;

public class Map
{
    public static final int ZOOM_MAX = 80;
    public static final int ZOOM_MIN = 1;

    // layer types
    public static final String LAYER_GS = "gs";
    public static final String LAYER_STREETS = "streets";
    public static final String LAYER_POINTS = "points";
    public static final String LAYER_ADDRESSES = "addresses";
    public static final String LAYER_STREET_NAMES= "streetNames";
    public static final String LAYER_BACKGROUND = "background";
    public static final String LAYER_PATH = "path";

    // default values
    private static final int DEFAULT_COLOR_SELECTED = 0xDF0000;
    private static final int DEFAULT_COLOR_CURSOR = 0xBEAA8C; //0x00DF00;
    private static final int DEFAULT_COLOR_PATH = 0x0040DF;
    private static final int DEFAULT_COLOR_PATH_START_FILL = 0xFFFF60;
    private static final int DEFAULT_COLOR_PATH_END_FILL = 0x00FFFF;
    private static final int DEFAULT_COLOR_PATH_START_OUTLINE = 0x00DF00;
    private static final int DEFAULT_COLOR_PATH_END_OUTLINE = 0x0040DF;
    private static final int DEFAULT_COLOR_PIN_FILL = 0xFFFFFF;
    private static final int DEFAULT_COLOR_PIN_OUTLINE = 0xDF0000;

    private static final float DEFAULT_OPACITY_SELECTED = 0.6f;
    private static final float DEFAULT_OPACITY_CURSOR = 0.5f;
    private static final float DEFAULT_OPACITY_PATH = 0.8f;
    private static final float DEFAULT_OPACITY_PATH_START = 1.0f;
    private static final float DEFAULT_OPACITY_PATH_END = 1.0f;
    private static final float DEFAULT_OPACITY_PIN = 1.0f;

    private static final int DEFAULT_COLOR_BACKGROUND = 0xe9d79f;
    private static final int DEFAULT_COLOR_STREET = 0xFFFFFF;
    private static final int DEFAULT_COLOR_TEXT_VERTICAL = 0x800080;
    private static final int DEFAULT_COLOR_TEXT_HORIZONTAL = 0x303000;
    private static final int DEFAULT_COLOR_TEXT_SHADOW = 0xffffff;
    private static final int DEFAULT_COLOR_ADDRESS = 0xEF0000;

    private static final int DEFAULT_COLOR_ONLINE_CATEGORIES = 0xA8D5FF;

    // parameter names
    private static final String PARAMETER_COLOR_SELECTED = "color.selected";
    private static final String PARAMETER_COLOR_CURSOR = "color.cursor";
    private static final String PARAMETER_COLOR_PATH = "color.path";
    private static final String PARAMETER_COLOR_PATH_START_FILL = "color.path.start.fill";
    private static final String PARAMETER_COLOR_PATH_END_FILL = "color.path.end.fill";
    private static final String PARAMETER_COLOR_PATH_START_OUTLINE = "color.path.start.outline";
    private static final String PARAMETER_COLOR_PATH_END_OUTLINE = "color.path.end.outline";
    private static final String PARAMETER_COLOR_PIN_FILL = "color.pin.fill";
    private static final String PARAMETER_COLOR_PIN_OUTLINE = "color.pin.outline";
    private static final String PARAMETER_OPACITY_SELECTED = "opacity.selected";
    private static final String PARAMETER_OPACITY_CURSOR = "opacity.cursor";
    private static final String PARAMETER_OPACITY_PATH = "opacity.path";
    private static final String PARAMETER_OPACITY_PATH_START = "opacity.path.start";
    private static final String PARAMETER_OPACITY_PATH_END = "opacity.path.end";
    private static final String PARAMETER_OPACITY_PIN = "opacity.pin";

    /**
     * Map project.
     */
    public Project project;

    /**
     * Data storage.
     */
    public Storage storage;

    public int crossroadsN, elementsN, streetsN, routesN, iconsN;
    public int pcRealN, pcStopN, pcRouteN, pcStartN, pcFinishN;

    public Crossroad crossroads [];
    public Element elements [];
    public Street streets [];
    public Vector<Address> addresses;
    public Vector<POI> pois;
    public Route routes [];
    public Icon icons [];
    public Hashtable<Integer,Category> categories;

    public Hashtable<Object,Integer> ctRemoteToLocal;
    public Hashtable<Integer,Object> ctLocalToRemote;

    public int currentCategory;
    public int currentItem;

    public Reference currentObject;
    public Reference cursorObject;

    public Point origin;

    public int cityX, cityY;
    static public int cityXStatic, cityYStatic;
    public double scale;
    public String fontmap;
    public int fontmapsize;
    private CityLoader parent;
    public int focusScale, widthMult, widthAdd, widthScale;
    public int globalScale = 1;
    public int iconScale, iconRad;
    public boolean browserQuickLaunch;
    boolean blinkerSynch, synchDraw;
    public String cityName;
    public int cityId;

    int[] out_pc;

    public GraphicStreamLoader theGraphicStreamLoader;

    public Geo2Meters geo2meters;
    public boolean showCoordinates;

    public Color backgroundColor = new Color(DEFAULT_COLOR_BACKGROUND);

    /**
     * Create Map object
     * @param parent CityLoader
     */
    public Map (CityLoader parent)
    {
        this.parent = parent;
        project = parent.project;
    }

    public boolean read (String file)
    {
        try
        {
            InputStream istream;
            istream = new FileInputStream (file);
            GZIPInputStream gz = new GZIPInputStream (istream);
            ObjectInputStream in = new ObjectInputStream (gz);

            int wr [] = (int []) in. readObject ();
            crossroadsN = wr [0]; elementsN = wr [1]; streetsN = wr [2];
                routesN = wr [4]; iconsN = wr [5];
                pcRealN = wr [9]; pcStopN = wr [10]; pcRouteN = wr [11];
                pcStartN = wr [12]; pcFinishN = wr [13];
                cityX = wr [14]; cityY = wr [15];
                cityXStatic = cityX; cityYStatic = cityY;
            cityName = (String) in. readObject ();
            crossroads = (Crossroad []) in. readObject ();
            streets = (Street []) in. readObject ();
            elements = (Element []) in. readObject ();
            addresses = (Vector<Address>) in. readObject ();
            pois = (Vector<POI>) in.readObject();
            routes = (Route []) in. readObject ();
            categories = (Hashtable<Integer,Category>) in.readObject ();
            icons = (Icon []) in. readObject ();

            in.close();
            gz.close();
            istream.close();
            return true;
        }
        catch (Exception e) {}
        return false;
    }
    public void write (String file)
    {
        try
        {
            FileOutputStream ostream = new FileOutputStream (file);
            GZIPOutputStream gz = new GZIPOutputStream (ostream);
            ObjectOutputStream out = new ObjectOutputStream (gz);

            int wr [] = {crossroadsN, elementsN, streetsN, 0, routesN,
                        iconsN, 0, 0, 0,
                        pcRealN, pcStopN, pcRouteN, pcStartN, pcFinishN,
                        cityX, cityY};
            out. writeObject (wr);
            out. writeObject (cityName);
            out. writeObject (crossroads);
            out. writeObject (streets);
            out. writeObject (elements);
            out. writeObject (addresses);
            out. writeObject (pois);
            out. writeObject (routes);
            out. writeObject (categories);
            out. writeObject (icons);

            out. flush ();
            out. close();
            gz. close ();
            ostream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load data from file
     * @return boolean
     * @throws MalformedURLException
     */
    public boolean load ()
    {
        Storage storage = new Storage ();
        try
        {
            I7Container container = new I7Container ();
            container.setFileName (project.getDataFileName());
            container.load ();

            int pn = container.getPacketCount ();
            if (pn < 1)
                return false;

            I7Packet packet = container.getPacket (0);
            InputStream is = new GZIPInputStream (packet.getInputStream ());

            I7DataParser parser = new I7DataParser (is, storage);
            parser.make ();

            storage.prepare();
        }
        catch (Exception ex)
        {
            return false;
        }

        City city = storage.getCity();
        cityName = storage.getCity().getName();
        if (city.isGeo())
        {
            geo2meters = new Geo2Meters (city.getWest (), city.getSouth (), city.getEast (),
                                         city.getNorth (), globalScale);
            cityX = (int)Math.ceil(geo2meters.getX(city.getNorth(), city.getEast()));
            cityY = (int)Math.ceil(geo2meters.getY(city.getNorth(), city.getEast()));
        }
        else
        {
            cityX = city.getWidth();
            cityY = city.getHeight();
        }
        cityXStatic = cityX; cityYStatic = cityY;

        crossroadsN = storage.getCrossroads().length;
        crossroads = new Crossroad[crossroadsN + 2];
        for (int i = 0; i < crossroadsN; i++)
            crossroads[i] = storage.getCrossroads()[i];
        pcRealN = crossroadsN + 2;
        pcStartN = pcRealN - 2;
        pcFinishN = pcRealN - 1;
        pcStopN = pcRealN;

        for (int i=0; i < crossroadsN; i++)
        {
            Crossroad cs = crossroads[i];
            if (cs != null)
                if (cs.x < 0 || cs.y < 0 || cs.x > cityX || cs.y > cityY)
                    System.err.println ("Crossroad " + i + " has illegal coors: " + cs.x + ", " + cs.y);
        }

        streetsN = storage.getStreets().length;
        streets = storage.getStreets();

        addresses = storage.getAddresses();

        pois = storage.getPois();

        categories = storage.getCategories();

        Category ch = new Category ();
        ch.setName("root");
        ch.setParent (-1);
        ch.visibility = true;
        ch.setId(Category.CATEGORY_ROOT);
        ch.setUid(Category.CATEGORY_ROOT);
        categories.put(Category.CATEGORY_ROOT, ch);

        ch = new Category ();
        ch.setName ("streets");
        ch.setParent (Category.CATEGORY_ROOT);
        ch.visibility = true;
        ch.setId(Category.CATEGORY_STREET);
        ch.setUid(Category.CATEGORY_STREET);
        categories.put(Category.CATEGORY_STREET, ch);

        ch = new Category ();
        ch.setName ("search");
        ch.setParent (Category.CATEGORY_ROOT);
        ch.visibility = true;
        ch.setId(Category.CATEGORY_SEARCH);
        ch.setUid(Category.CATEGORY_SEARCH);
        categories.put(Category.CATEGORY_SEARCH, ch);

        categories.get(Category.CATEGORY_ROOT).setName("root");

        // patch old-school category root id
        for (Category cat : categories.values())
        {
            if (cat.getParent() == 1) cat.setParent(0);
        }

        icons = storage.getIcons();
        iconsN = icons.length;

        origin = new Point(cityX / 2, cityY / 2);
        scale = 10;

        return true;
    }

    /**
     * Load graphic streams
     */
    public void loadGS ()
    {
        theGraphicStreamLoader = new GraphicStreamLoader (null);

        for (Layer layer : parent.project.getLayers())
        {
            String type = (String)layer.getAttribute(Layer.ATTRIBUTE_TYPE);
            if (LAYER_GS.equals(type))
            {
                String fileName = (String)layer.getAttribute("file");
                if (fileName == null) continue;

                theGraphicStreamLoader.loadStream(fileName);
            }
        }
    }

    // *********** P R E P A R I N G   D A T A ********************

    public double castBuilding2Pc (Building bl, boolean castCoors)
    {
        double d = 0xffffff;
        int xn = 0, yn = 0;
        int x = bl.x, y = bl.y;

        for (int i=1; i < pcRealN; i++)
            if (crossroads [i] != null)
            {
                Crossroad p = crossroads [i];
                for (int j=0; j < p. conN; j++)
                {
                    int pj = p. con [j]. pc;
                    if (pj < pcRealN)
                    {
                        Crossroad r = crossroads [pj];
                        if (r == null) continue;

                        double l = hypot (p.x - r.x, p.y - r.y);
                        double a = hypot (x - p.x, y - p.y);
                        double b = hypot (x - r.x, y - r.y);
                        double e = (a + b) / l;

                        if (e < d)
                        {
                            d = e;
                            double z = ((double)(r.x-p.x))*(y-p.y) - ((double)(r.y-p.y))*(x-p.x);
                            if (z<0) { bl.pc1 = i; bl.pc2 = pj; }
                            else { bl.pc1 = pj; bl.pc2 = i; }

                            xn = (l>10)?(int)(p.x + ((r.x-p.x)*a)/l):p.x+1;
                            yn = (l>10)?(int)(p.y + ((r.y-p.y)*a)/l):p.y+1;
                        }
                    }
                }
            }
        if (castCoors) { bl.x = xn; bl.y = yn; }
        return d;
    }
    private static double hypot (int a, int b)
    {
        return Math.sqrt (a*a+b*b);
    }

    /**
     * Prepare icons
     */
    public void prepare_icons ()
    {
        for (int i = 0; i < iconsN; i++)
        {
            Icon icon = icons[i];
            if (icon == null)
                continue;

            icon.loadIconData();

            Reference[] places = icon.getPlaces();
            for (int j = 0; j < places.length; j++)
            {
                int cl = places[j].classid;
                int oj = places[j].objectid;

                if (cl == DataType.CLASS_CATEGORY)
                {
                    Category cat = categories.get(oj);
                    if (cat != null)
                    {
                        cat.setIcon (icon);
                        cat.setImageIcon (icon.getImage ());
                    }
                }
                else if (cl == DataType.CLASS_POI)
                {
                    for (POI poi : pois)
                    {
                        if (poi.getId() == oj)
                        {
                            poi.setImageIcon(icon.getImage());
                            break;
                        }
                    }
                }
            }
        }
    }

    // calculate elements data
    public void prepare ()
    {
        elementsN = 0;

        // creating temporary data
        for (int n = 1; n < streetsN; n++)
            if (streets [n] != null) streets [n].pc = new java.util.Vector (100, 20);

        java.util.Vector elist = new java.util.Vector (100, 100);

        // 1. Перебор всех Pc и добавление информации в Street
        for (int pos = 1; pos < pcRealN; pos++)
          if (crossroads [pos] != null)
            for (int j = 0; j < crossroads [pos]. conN; j++)
            {
                int n = crossroads [pos]. con [j]. street;

                if (n > 0)
                {
                    Street curStreet = streets [n];

                    if (!curStreet. pc. contains (new Integer (pos)))
                        curStreet. pc. addElement (new Integer (pos));
                }
            }

        // 2. Составление списка связности по каждой улице
        for (int n = 1; n < streetsN; n++)
        {
            Street curStreet = streets [n];
            if (curStreet == null) continue;

            //if (n % 20 == 0) parent.progress ((n * 100) / streetN);

            int curN = curStreet. pc. size();
            int cur [] = new int [curN];
            // список непросмотренных перекрестков для данной улицы
            boolean cb [] = new boolean [curN];
            // список непросмотренных перекрестков в общем списке
            boolean pf [] = new boolean [crossroadsN];

            for (int i=0; i < curN; i++)
            {
                cur [i] = ((Integer) curStreet. pc. elementAt (i)). intValue ();
                cb [i] = true;
                pf [cur [i]] = true;
            }

            while (true)
            {
                int index = -1;
                for (int i=0; i< curN; i++)
                    if (cb [i])
                        { index = i; break; }

                if (index == -1) break;

                int begin = cur [index];
                cb [index] = false;
                pf [begin] = false;

                // create new element
                Element curElement = new Element ();
                elist. addElement (curElement);

                curElement. street = n;
                curStreet. element. addElement (new Integer (elementsN));
                curStreet. elementN = curStreet. element. size ();

                elementsN++;

                int epc [] = new int [pcRealN * 2];
                int epcR = pcRealN;
                int epcL = epcR - 1;

                epc [epcR++] = begin;

                // 2.a) adding Pcs to Element

                int left = 0, right = 0;
                Crossroad pcBegin = crossroads [begin];
                for (int j = 0; j < pcBegin. conN; j++)
                    if (pcBegin. con [j]. street == n)
                        if (left == 0) left = pcBegin. con [j]. pc;
                        else if (right == 0) right = pcBegin. con [j]. pc;

                if (left != 0)		// move to left
                {
                    int next = left;		// connect to
                    int prev = begin;
                    boolean nextFlag = true;

                    while (nextFlag)
                    {
                        epc [epcL--] = next;

                        for (int f = 0; f < curN; f++)
                            if (cur [f] == next) cb [f] = false;

                        pf [next] = false;

                        Crossroad pcNext = crossroads [next];
                        nextFlag = false;

                        for (int k=0; k < pcNext. conN; k++)
                        {
                            int nowpc = pcNext. con [k]. pc;
                            if (pf [nowpc] && pcNext. con [k]. street == n)   // bug/feature
                            {
                                prev = next;
                                next = nowpc;
                                nextFlag = true;
                                break;
                            }
                        }
                    }
                }

                if (right != 0)		// move to right
                {
                    int next = right;		// connect to
                    int prev = begin;
                    boolean nextFlag = true;

                    while (nextFlag)
                    {
                        epc [epcR++] = next;

                        for (int f = 0; f < curN; f++)
                            if (cur [f] == next) cb [f] = false;

                        pf [next] = false;

                        Crossroad pcNext = crossroads [next];
                        nextFlag = false;

                        for (int k=0; k < pcNext. conN; k++)
                        {
                            int nowpc = pcNext. con [k]. pc;
                            if (pf [nowpc] && pcNext. con [k]. street == n)   // bug/feature
                            {
                                prev = next;
                                next = nowpc;
                                nextFlag = true;
                                break;
                            }
                        }
                    }
                }
                // копирование списка перекрестков в постоянный массив
                curElement. pc = new int [epcR - epcL + 1];
                curElement. pcL = 0;
                curElement. pcR = epcR - epcL;

                for (int k=epcL; k <= epcR; k++)
                    curElement. pc [k-epcL] = epc [k];

            }
        }

        // clearing temporary data
        for (int n = 1; n < streetsN; n++)
            if (streets [n] != null) streets [n]. pc = null;

        elements = new Element [elementsN];
        for (int i=0; i < elementsN; i++)
            elements [i] = (Element) elist. elementAt (i);

        prepare_names ();

        prepare_icons ();

        prepareProject();

        System. gc ();
    }
    void prepare_names ()
    {
        for (Street st : streets)
        {
            if (st != null)
            {
                String name = st.getName ();
                if (st.getType () != null)
                    name = name + " " + st.getType ();
                if (st.getVillage () != null)
                    name = name + " (" + st.getVillage () + ")";

                st.setFullName (name);
            }
        }

        for (int i=1; i<pcRealN; i++)
            if (crossroads [i] != null)
            {
                Crossroad a = crossroads [i];

                int st1 = 0, st2 = 0;

                for (int j=0; j < a.conN; j++)
                {
                    int st = a.con [j]. street;
                    if (st != 0)
                        if (st1 == 0) st1 = st;
                        else if (st2 == 0 && st != st1) { st2 = st; break; }
                }
                String name = a.x + "," + a.y;
                if (st2 != 0) name = streets [st1] + " x " + streets [st2];
                else if (st1 != 0) name = streets [st1].toString ();
                    else name = "";

                crossroads [i]. fullName = name;
            }

        for (Address ad : addresses)
        {
            ad.setFullName (streets[ad.getStreetId ()].getFullName() + ", " + ad.getName ());
        }

        for (POI poi : pois)
        {
            poi.setFullName (poi.getName() + " (" + categories.get(poi.getCategoryId ()).getName() + ")");
        }
    }

    /**
     * Check project. If it misses some layers that we have data for, add them
     */
    private void prepareProject()
    {
        List<Layer> layers = project.getLayers();

        if (findLayer(layers, LAYER_BACKGROUND) == null)
        {
            // add background
            Layer layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_BACKGROUND);
            layer.setAttribute(Layer.ATTRIBUTE_FILL, Integer.toHexString(DEFAULT_COLOR_BACKGROUND));
            layers.add(0, layer);
        }
        if (findLayer(layers, LAYER_STREETS) == null &&
            crossroads.length > 0)
        {
            // add street layer
            Layer layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_STREETS);
            layer.setAttribute(Layer.ATTRIBUTE_FILL, Integer.toHexString(DEFAULT_COLOR_STREET));
            layers.add(layer);
        }
        if (findLayer(layers, LAYER_POINTS) == null &&
            pois.size() > 0)
        {
            // add points layer
            Layer layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_POINTS);
            layer.setAttribute("zoomMax", "30");
            layers.add(layer);
        }
        if (findLayer(layers, LAYER_ADDRESSES) == null &&
            addresses.size()> 0)
        {
            // add addresses layers
            Layer layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_ADDRESSES);
            layer.setAttribute("zoomMax", "14");
            layer.setAttribute("zoomMin", "3");
            layer.setAttribute("showBox", "false");
            layers.add(layer);

            layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_ADDRESSES);
            layer.setAttribute("zoomMax", "3");
            layer.setAttribute("fontSize", "10");
            layers.add(layer);
        }
        if (findLayer(layers, LAYER_STREET_NAMES) == null &&
            streets.length > 0)
        {
            // add street names layer
            Layer layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_STREET_NAMES);
            layers.add(layer);
        }
        if (findLayer(layers, LAYER_PATH) == null &&
            crossroads.length > 0)
        {
            // add path layer
            Layer layer = new Layer();
            layer.setAttribute(Layer.ATTRIBUTE_TYPE, LAYER_PATH);
            layers.add(layer);
        }
    }

    /**
     * Find layer with specified type
     * @param layers List
     * @param type String
     * @return Layer
     */
    private Layer findLayer(List<Layer> layers, String type)
    {
        for (Layer layer : layers)
        {
            if (type.equals(layer.getAttribute(Layer.ATTRIBUTE_TYPE)))
                return layer;
        }
        return null;
    }
}
