/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.target;

import java.io.*;
import java.util.*;
import java.util.List;

import java.awt.*;

import org.w3c.dom.*;
import com.ish.isrt.core.data.*;
import com.ish.mobimapbuilder.data.*;
import com.ish.mobimapbuilder.data.City;
import com.ish.mobimapbuilder.transform.*;
import com.ish.mobimapbuilder.xml.*;
import com.ish.mobimapbuilder.util.*;

public class DataTarget extends Target
{
    public static final String TYPE = "data";

    private static final int CAPACITY = 1000;

    private static final int CLASSID_CROSSROAD = 1;
    private static final int CLASSID_STREET = 2;
    private static final int CLASSID_BUILDING = 4;
    private static final int CLASSID_CATEGORY = 8;
    private static final int CLASSID_ICON = 9;
    private static final int CLASSID_ADDRESS = 16;

    private static final int CATEGORY_ROOT = 0;

    private List<CrossroadRecord> crossroads = null;
    private List<StreetRecord> streets = null;
    private List<CategoryRecord> categories = null;
    private List<BuildingRecord> buildings = null;
    private List<AddressRecord> addresses = null;
    private List<IconRecord> icons = null;

    private List<AddressRecord> addressRecords = null;
    private Map<String,IconRecord> iconRecords = null;

    private Map<Integer,Integer> roadClassTranscode = null;

    private double transformX = 0;
    private double transformY = 0;

    private boolean addressToStreetMatchSimple = false;
    private boolean addressToStreetMatchById = false;

    private int autoid = 1;

	public DataTarget ()
	{
        super(TYPE);
        buildings = new Vector<BuildingRecord>(CAPACITY, CAPACITY);
        iconRecords = new HashMap<String,IconRecord>();
        categories = new Vector<CategoryRecord>();
        roadClassTranscode = new HashMap<Integer,Integer>();
	}

    public boolean load(Node targetNode)
    {
        boolean result = false;

        for (Node node = targetNode.getFirstChild();
             node != null;
             node = node.getNextSibling() )
        {
            NamedNodeMap attribs = node.getAttributes ();
            String nodeName = node.getNodeName();

            super.loadParameters(nodeName, attribs);

            if (nodeName.compareTo("categorization") == 0)
            {
                for (Node categoryNode = node.getFirstChild();
                     categoryNode != null;
                     categoryNode = categoryNode.getNextSibling() )
                {
                    attribs = categoryNode.getAttributes ();
                    nodeName = categoryNode.getNodeName();

                    if (nodeName.compareTo ("category") == 0 && attribs != null)
                    {
                        String id = XMLUtil.getAttributeValue (attribs, "id");
                        String parent = XMLUtil.getAttributeValue (attribs, "parent");
                        String name = XMLUtil.getAttributeValue (attribs, "name");
                        String icon = XMLUtil.getAttributeValue (attribs, "icon");
                        String color = XMLUtil.getAttributeValue (attribs, "color");
                        String fontName = XMLUtil.getAttributeValue (attribs, "fontname");
                        String fontSize = XMLUtil.getAttributeValue (attribs, "fontsize");
                        String fontStyle = XMLUtil.getAttributeValue (attribs, "fontstyle");
                        String pointSize = XMLUtil.getAttributeValue (attribs, "pointsize");
                        String label = XMLUtil.getAttributeValue (attribs, "label");

                        // get all attributes
                        Map<String,String> allAttributes = new Hashtable<String,String>();
                        int n = attribs.getLength();
                        for (int i=0; i < n; i++)
                        {
                            Node nd = attribs.item(i);
                            String key = nd.getNodeName();
                            String value = nd.getNodeValue();
                            allAttributes.put(key, value);
                        }

                        if (id != null && name != null)
                        {
                            int idI = atoi(id);
                            if (idI > 5)
                            {
								int parentI = parent == null? CATEGORY_ROOT: atoi (parent);
								if (parentI == 0)
									parentI = CATEGORY_ROOT;
								int fontSizeI = atoi (fontSize);
								int fontStyleI = atoi (fontStyle);
								int pointSizeI = atoi (pointSize);

                                // add category
								CategoryRecord c = new CategoryRecord ();
								c.id = idI;
								c.parent = parentI;
								c.name = name;
								c.color = (color == null)? Color.GRAY:
                                    new Color (Integer.parseInt (color,	16));
                                c.attributes = allAttributes;
								categories.add (c);

                                // add icon
                                if (icon != null)
                                    addIcon(icon, new tref(CLASSID_CATEGORY, idI));
							}
                            else
                                System.err.println ("Category ID is less or equal 5!");
                        }
                    }
                }
            }
            else if (nodeName.compareTo("roadclass") == 0)
            {
                String source = getAttributeValue (attribs, "source");
                String target = getAttributeValue (attribs, "target");

                if (source != null && target != null)
                {
					try {
						Integer s = Integer.valueOf (source);
						Integer t = Integer.valueOf (target);

                        roadClassTranscode.put(s, t);
					}
					catch (NumberFormatException ex) {
					}
                }
            }
            else if (nodeName.compareTo("param") == 0)
            {
                String pname = getAttributeValue (attribs, "name");
                String pvalue = getAttributeValue (attribs, "value");

                if (pname != null && pvalue != null)
                {
                    if ("autoidstart".equals(pname))
                    {
                        autoid = Integer.parseInt(pvalue);
                    }
                }
            }
        }

        result = getId() != null;

        return result;
    }

	public void setCrossroads (List crossroads)
	{
		this.crossroads = crossroads;
	}

	public void setStreets (List streets)
	{
		this.streets = streets;
	}

    public void setAddresses (List<AddressRecord> addresses)
    {
        this.addressRecords = addresses;
    }

	public void setTransformY (double transformY)
	{
		this.transformY = transformY;
	}

	public void setTransformX (double transformX)
	{
		this.transformX = transformX;
	}

	public void setAddressToStreetMatchSimple (boolean addressToStreetMatchSimple)
	{
		this.addressToStreetMatchSimple = addressToStreetMatchSimple;
	}

	public void setAddressToStreetMatchById (boolean addressToStreetMatchById)
	{
		this.addressToStreetMatchById = addressToStreetMatchById;
	}

	public int getNextId()
    {
        return autoid++;
    }

	public void addPOI (double x, double y, int uid, int categoryId, String name, String icon,
                        Map<String,String> meta)
    {
        if (name == null) return;

        BuildingRecord b = new BuildingRecord();
        b.x = (int)x;
        b.y = (int)y;
        b.name = name;
        b.ref = new tref(CLASSID_CATEGORY, categoryId);
        b.id = buildings.size() + 1;
        b.uid = uid;
        b.meta = meta;
        buildings.add (b);

        if (icon != null)
            addIcon (icon, new tref (CLASSID_BUILDING, b.id));
    }

    private void addIcon (String icon, tref ref)
    {
        if (!StringUtils.isNullOrEmpty(icon))
        {
			IconRecord ir = iconRecords.get (icon);
			if (ir == null)
				ir = new IconRecord ();

			ir.file = icon;
			ir.places.add (ref);

			iconRecords.put (icon, ir);
		}
    }

    /**
     ******************** PREPARE ********************
     */
    private void prepare()
    {
        prepareAddresses();
        prepareCategories();
        prepareIcons();
    }

	private void prepareAddresses ()
	{
		if (addressRecords == null)
			return;

        addresses = new Vector<AddressRecord>(addressRecords.size());

		Map<String, Integer> streetsMap = new HashMap<String, Integer>(CAPACITY);

		for (int i = 0; i < streets.size (); i++) {
			StreetRecord street = streets.get (i);
			String name = street.getName ();
			int id = street.id;
			String streetType = street.getType ();
			if (streetType == null)
				streetType = "";
			String streetVillage = street.getVillage ();
			if (streetVillage == null)
				streetVillage = "";

			if (!addressToStreetMatchSimple)
				name = name + "|" + streetType + "|" + streetVillage;

			if (addressToStreetMatchById)
				name = street.getStreetId();
			else
				name = name.toLowerCase ();

			streetsMap.put (name, new Integer (id));
		}

		for (int i = 0; i < addressRecords.size (); i++) {
			AddressRecord rec = addressRecords.get (i);
			String s = rec.getStreetName ();
			if (s != null) {

				String streetType = rec.getStreetType ();
				if (streetType == null)
					streetType = "";
				String streetVillage = rec.getStreetVillage ();
				if (streetVillage == null)
					streetVillage = "";

				if (!addressToStreetMatchSimple)
					s = (s + "|" + streetType + "|" + streetVillage).toLowerCase ();
				if (addressToStreetMatchById)
					s = rec.getStreetId ();
				else
					s = s.toLowerCase ();

				Integer ind = streetsMap.get (s);
				if (ind != null) {
					rec.id = addresses.size ();
					rec.setRef (new tref (CLASSID_STREET, ind.intValue ()));
					addresses.add (rec);
				}
			}
		}
	}
    private void prepareCategories()
    {
    }
	private void prepareIcons ()
    {
        icons = new Vector<IconRecord>();
        Set<String> set = iconRecords.keySet();
        int id = 1;
        for (Iterator<String> it = set.iterator(); it.hasNext();)
        {
            String key = it.next();
            IconRecord ir = iconRecords.get(key);
            ir.id = id++;
            icons.add(ir);
        }
    }

    /**
     ******************** SORT ********************
     */
    private void sort()
    {
        sortStreets();
        sortBuildings();
        sortAddresses();
    }
    private void sortStreets()
    {
        if (streets == null) return;

        int streetN = streets.size()+2;

        // remove empty streets (without crossroads)
        boolean[] hasCrossroads = new boolean[streetN];
        for (int i=1; i < crossroads.size(); i++)
        {
			CrossroadRecord pc = crossroads.get (i);
			for (int j = 0; j < pc.con.size (); j++)
                hasCrossroads[pc.con.get(j).street] = true;
		}
        List<StreetRecord> streets2 = new Vector<StreetRecord>(streetN);
        for (int i=0; i < streets.size(); i++)
            if (hasCrossroads[streets.get(i).id])
                streets2.add(streets.get(i));

        streets = streets2;

        // prepare indexes
        int[] indexStreets = new int[streetN];
        indexStreets[0] = 0;

        Collections.sort(streets);

        for (int i=0; i < streets.size(); i++)
            indexStreets [streets.get(i).id] = i+1;

        // reindex streets
        for (int i=0; i < streets.size(); i++)
        {
//            String name = streets.get(i).getName();
//            int a = name.indexOf('[');
//            if (a > 0) streets.get(i).setName(name.substring(0, a-1));
            streets.get(i).id = i+1;
        }

        // reindex crossroads
        for (int i=1; i < crossroads.size(); i++)
        {
            CrossroadRecord pc = crossroads.get(i);
            pc.id = i;
            for (int j = 0; j < pc.con.size(); j++)
            {
                Connection cn = pc.con.get(j);
                cn.street = indexStreets[cn.street];
            }
        }

        // reindex addresses
		if (addresses != null)
			for (int i = 0; i < addresses.size (); i++) {
				AddressRecord b = addresses.get (i);
				b.getRef ().objectid = indexStreets[b.getRef ().objectid];
			}
    }
    private void sortBuildings()
    {
        if (buildings == null) return;

        int buildingN = buildings.size();

        // check if object refers to existing category
        List<BuildingRecord> filtered = new ArrayList<BuildingRecord>(buildingN);
        for (BuildingRecord br : buildings)
        {
            if (br.ref.classid == CLASSID_CATEGORY)
            {
                boolean f = false;
                for (CategoryRecord cat : categories)
                {
                    if (cat.id == br.ref.objectid)
                    {
                        f = true;
                        break;
                    }
                }
                if (f)
                    filtered.add(br);
            }
        }

        int[] indexBuildings = new int[buildingN+1];
        indexBuildings [0] = 0;
        Collections.sort (filtered);

        int filteredN = filtered.size();
        for (int i=0; i < filteredN; i++)
            indexBuildings [filtered.get(i).id] = i;

        // reindex buildings
        for (int i=0; i < filteredN; i++)
        {
            filtered.get(i).id = indexBuildings[filtered.get(i).id];
        }

        // reindex icons
        for (int i=0; i < icons.size(); i++)
        {
            IconRecord a = icons.get(i);
            for (ListIterator<tref> it = a.places.listIterator(); it.hasNext();)
            {
                tref ref = it.next();
                if (ref.classid == CLASSID_BUILDING)
                    ref.objectid = indexBuildings[ref.objectid];
            }
        }

        // change buildings list to filtered one
        buildings = filtered;
    }
    private void sortAddresses()
    {
        if (addresses == null) return;
        Collections.sort (addresses);
    }

    /**
     ******************** OUTPUT ********************
     */
    public void save()
    {
        prepare();
        sort();

        //saveToIsrt5Format();
		try {
			String format = getFileFormat ();
			if (format == null || "i7".equalsIgnoreCase (format)) {
				saveFormat ();
			}
		}
		catch (IOException ex) {
		}
    }
    /*
     * Save to mobimap.desktop (isrt7) format
     */
    private void saveFormat()
        throws IOException
    {
		XMLOutput xml = new XMLOutput (fileWriter);
		xml.openTag (DataXmlConst.TAG_MAP);

		saveCity (xml);
		saveCrossroads (xml);
		saveStreets (xml);
		saveBuildings (xml);
		saveAddresses (xml);
		saveCategories (xml);
		saveIcons (xml);

		xml.closeTag (DataXmlConst.TAG_MAP);
    }
    private void saveCity (XMLOutput xml)
        throws IOException
    {
        City city = getProject().getCity();
        String name = city.getName(); // getProject().getCityName();
        String x = Integer.toString((int)Math.ceil(city.getBounds().x2));
        String y = Integer.toString((int)Math.ceil(city.getBounds().y2));

        List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();
        attributes.add(new XMLAttribute(DataXmlConst.ATTR_NAME, name));
        attributes.add(new XMLAttribute(DataXmlConst.ATTR_X, x));
        attributes.add(new XMLAttribute(DataXmlConst.ATTR_Y, y));

        Geo2Meters gm = getProject().getGeo2meters();
        if (gm != null)
        {
            attributes.add(new XMLAttribute("west", Float.toString((float)gm.getLon(0, 0))));
            attributes.add(new XMLAttribute("east", Float.toString((float)gm.getLon(city.getBounds().x2, 0))));
            attributes.add(new XMLAttribute("south", Float.toString((float)gm.getLat(0, 0))));
            attributes.add(new XMLAttribute("north", Float.toString((float)gm.getLat(0, city.getBounds().y2))));
        }

		xml.openAndCloseTag (DataXmlConst.TAG_CITY, attributes);
    }

    private void saveCrossroads (XMLOutput xml)
        throws IOException
    {
        if (crossroads == null) return;
        if (crossroads.size() == 0) return;

        double dx = 0; //getProject().getTransformX();
        double dy = 0; //getProject().getTransformY();

        xml.openTag(DataXmlConst.TAG_CROSSROADS_STREAM,
                    new String[] {DataXmlConst.ATTR_COUNT, DataXmlConst.ATTR_CLASSID},
                    new String[] {Integer.toString(getMaxId(crossroads)), Integer.toString(CLASSID_CROSSROAD)});

        int pcN = crossroads.size();
        for (int i=1; i < pcN; i++)
        {
            CrossroadRecord pc = crossroads.get(i);

            int x = (int)(pc.x + dx);
            int y = (int)(pc.y + dy);
            int conn = pc.con.size();

			xml.openTag (DataXmlConst.TAG_CROSSROAD,
						 new String[] {
						 DataXmlConst.ATTR_ID, DataXmlConst.ATTR_X, DataXmlConst.ATTR_Y,
						 DataXmlConst.ATTR_COUNT
			},
				new String[] {
				Integer.toString (i), Integer.toString (x), Integer.toString (y),
				Integer.toString (conn)
			});

            for (int j = 0; j < conn; j++)
            {
                Connection cn = pc.con.get(j);

                int roadClass = cn.type;
                Integer v = roadClassTranscode.get(new Integer(roadClass));
                if (v != null)
                    roadClass = v.intValue();

                List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

                attributes.add(new XMLAttribute(DataXmlConst.ATTR_CROSSROAD, Integer.toString(cn.pc)));
                if (cn.street > 0)
                    attributes.add(new XMLAttribute(DataXmlConst.ATTR_STREET, Integer.toString(cn.street)));
                if (cn.direction > 0)
                    attributes.add(new XMLAttribute(DataXmlConst.ATTR_DIRECTION, Integer.toString(cn.direction)));
                if (roadClass > 0)
                    attributes.add(new XMLAttribute(DataXmlConst.ATTR_TYPE, Integer.toString(roadClass)));

                xml.openAndCloseTag(DataXmlConst.TAG_CON, attributes);
            }
            xml.closeTag (DataXmlConst.TAG_CROSSROAD);
        }
        xml.closeTag(DataXmlConst.TAG_CROSSROADS_STREAM);
    }

    private void saveStreets (XMLOutput xml)
        throws IOException
    {
        if (streets == null) return;
        if (streets.size() == 0) return;

        xml.openTag(DataXmlConst.TAG_STREETS_STREAM,
                    new String[] {DataXmlConst.ATTR_COUNT, DataXmlConst.ATTR_CLASSID},
                    new String[] {Integer.toString(getMaxId(streets)), Integer.toString(CLASSID_STREET)});

        int streetN = streets.size();
        for (int i=0; i < streetN; i++)
        {
			StreetRecord st = streets.get (i);

            List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_ID, Integer.toString (st.id)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_FULL_NAME, st.getFullName ()));
            if (st.getType() != null)
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_TYPE, st.getType()));
            if (st.getName() != null)
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_NAME, st.getName()));
            if (st.getVillage() != null)
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_VILLAGE, st.getVillage()));

			xml.openAndCloseTag (DataXmlConst.TAG_STREET, attributes);
        }
        xml.closeTag(DataXmlConst.TAG_STREETS_STREAM);
    }

    /**
     * Save POIs
     * @param xml XMLOutput
     * @throws IOException
     */
    private void saveBuildings (XMLOutput xml)
        throws IOException
    {
        if (buildings == null) return;
        if (buildings.size() == 0) return;

        double dx = 0; //getProject().getTransformX();
        double dy = 0; //getProject().getTransformY();

        xml.openTag(DataXmlConst.TAG_POINTS_STREAM,
                    new String[] {DataXmlConst.ATTR_COUNT, DataXmlConst.ATTR_CLASSID},
                    new String[] {Integer.toString(getMaxId(buildings)), Integer.toString(CLASSID_BUILDING)});

        Geo2Meters gm = getProject().getGeo2meters();

        int buildingN = buildings.size();
        for (int i=0; i < buildingN; i++)
        {
            BuildingRecord b = buildings.get(i);

            // check if object refers to existing category
            if (b.ref.classid == CLASSID_CATEGORY)
            {
                boolean f = false;
                for (CategoryRecord cat : categories)
                {
                    if (cat.id == b.ref.objectid)
                    {
                        f = true;
                        break;
                    }
                }
                if (!f) continue;
            }

            int x = (int)(b.x + dx);
            int y = (int)(b.y + dy);

            List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

            attributes.add(new XMLAttribute(DataXmlConst.ATTR_ID, Integer.toString(b.id)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_X, Integer.toString(x)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_Y, Integer.toString(y)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_NAME, b.name));
//            attributes.add(new XMLAttribute(DataXmlConst.ATTR_REFERENCE, refToString(b.ref)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_CATEGORY, Integer.toString(b.ref.objectid)));

            if (gm != null)
            {
                float lat = (float)gm.getLat(x, y);
                float lon = (float)gm.getLon(x, y);
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_LATITUDE, Float.toString(lat)));
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_LONGITUDE, Float.toString(lon)));
            }

            if (b.uid > 0)
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_UID, Integer.toString(b.uid)));

            if (b.meta != null)
            {
                for (String key : b.meta.keySet())
                {
                    String value = b.meta.get(key);
                    attributes.add(new XMLAttribute("meta." + key, value));
                }
            }

            xml.openAndCloseTag(DataXmlConst.TAG_POINT, attributes);
        }

        xml.closeTag(DataXmlConst.TAG_POINTS_STREAM);
    }

    /**
     * Save Addresses
     * @param xml XMLOutput
     * @throws IOException
     */
    private void saveAddresses (XMLOutput xml)
        throws IOException
    {
        if (addresses == null) return;
        if (addresses.size() == 0) return;

        double dx = 0; //getProject().getTransformX();
        double dy = 0; //getProject().getTransformY();

        xml.openTag(DataXmlConst.TAG_ADDRESSES_STREAM,
                    new String[] {DataXmlConst.ATTR_COUNT, DataXmlConst.ATTR_CLASSID},
                    new String[] {Integer.toString(getMaxId(addresses)), Integer.toString(CLASSID_ADDRESS)});

        Geo2Meters gm = getProject().getGeo2meters();

        int addressesN = addresses.size();
        for (int i=0; i < addressesN; i++)
        {
            AddressRecord ad = addresses.get(i);

            int x = (int)(ad.getX() + dx);
            int y = (int)(ad.getY() + dy);

            List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

            attributes.add(new XMLAttribute(DataXmlConst.ATTR_ID, Integer.toString(ad.id)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_X, Integer.toString(x)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_Y, Integer.toString(y)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_NAME, ad.getHouseNumber()));
//            attributes.add(new XMLAttribute(DataXmlConst.ATTR_REFERENCE, refToString(ad.getRef())));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_STREET, Integer.toString(ad.getRef().objectid)));

            if (gm != null)
            {
                float lat = (float)gm.getLat(x, y);
                float lon = (float)gm.getLon(x, y);
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_LATITUDE, Float.toString(lat)));
                attributes.add(new XMLAttribute(DataXmlConst.ATTR_LONGITUDE, Float.toString(lon)));
            }

            xml.openAndCloseTag(DataXmlConst.TAG_ADDRESS, attributes);
        }

        xml.closeTag(DataXmlConst.TAG_ADDRESSES_STREAM);
    }

    private void saveCategories (XMLOutput xml)
        throws IOException
    {
        if (categories == null) return;
        if (categories.size() == 0) return;

        Collections.sort(categories, new Comparator<CategoryRecord>(){
            public int compare(CategoryRecord c1, CategoryRecord c2)
            {
                return c1.name.compareTo(c2.name);
            }
        });

        xml.openTag(DataXmlConst.TAG_CATEGORIES_STREAM,
                    new String[] {DataXmlConst.ATTR_COUNT, DataXmlConst.ATTR_CLASSID},
                    new String[] {Integer.toString(getMaxId(categories)), Integer.toString(CLASSID_CATEGORY)});

        int categoriesN = categories.size();
        for (int i=0; i < categoriesN; i++)
        {
            CategoryRecord c = categories.get(i);

            List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

            if (c.attributes != null)
            {
                for (Iterator<String> jt = c.attributes.keySet().iterator(); jt.hasNext();)
                {
                    String key = jt.next();
                    String value = c.attributes.get(key);
                    attributes.add(new XMLAttribute(key, value));
                }
            }

            xml.openAndCloseTag(DataXmlConst.TAG_CATEGORY, attributes);
        }
        xml.closeTag(DataXmlConst.TAG_CATEGORIES_STREAM);
    }
    private void saveIcons (XMLOutput xml)
        throws IOException
    {
        if (icons == null) return;
        if (icons.size() == 0) return;

        xml.openTag(DataXmlConst.TAG_ICONS_STREAM,
                    new String[] {DataXmlConst.ATTR_COUNT, DataXmlConst.ATTR_CLASSID},
                    new String[] {Integer.toString(getMaxId(icons)), Integer.toString(CLASSID_ICON)});

        int iconsN = icons.size();
        for (int i=0; i < iconsN; i++)
        {
            IconRecord ir = icons.get(i);

            List<XMLAttribute> attributes = new ArrayList<XMLAttribute>();

            attributes.add(new XMLAttribute(DataXmlConst.ATTR_ID, Integer.toString(ir.id)));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_FILE, ir.file));
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_TYPE, Integer.toString(ir.type)));

            int n = ir.places.size();
            attributes.add(new XMLAttribute(DataXmlConst.ATTR_COUNT, Integer.toString(n)));

            xml.openTag(DataXmlConst.TAG_ICON, attributes);

            for (int j = 0; j < n; j++)
            {
                tref p = ir.places.get(j);

                xml.openAndCloseTag(DataXmlConst.TAG_PLACE,
                            new String[] {DataXmlConst.ATTR_REFERENCE},
                            new String[] {refToString(p)});
            }
            xml.closeTag(DataXmlConst.TAG_ICON);
        }
        xml.closeTag(DataXmlConst.TAG_ICONS_STREAM);
    }
    /**
     * Get max id for specified collection
     * @param list List
     * @return int
     */
    private int getMaxId (List<? extends AbstractRecord> list)
    {
        return Collections.max(list, new IdRecordComparator()).id;
    }
    private String refToString (tref ref)
    {
        return "" + ref.classid + Character.toString(DataXmlConst.SEPARATOR) + ref.objectid;
    }
    private String colorToString (Color c)
    {
        return Integer.toHexString(c.getRGB() & 0xffffff);
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
			try {
				res = Integer.parseInt (s);
			}
			catch (NumberFormatException ex) {
			}
        }
        return res;
    }
}
