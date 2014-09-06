//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

import java.util.*;

public class Storage
{
    private City city;
    private Crossroad[] crossroads;
    private Street[] streets;
    private Vector<Address> addresses;
    private Vector<POI> pois;
    private Hashtable<Integer, Category> categories;
    private Icon[] icons;

    public Storage ()
    {
        crossroads = new Crossroad[0];
        streets = new Street[0];
        addresses = new Vector<Address>();
        pois = new Vector<POI>();
        categories = new Hashtable<Integer, Category>();
        icons = new Icon[0];
    }

    /**
     * Prepare data. Split buildings into addresses, pois and stops.
     */
    public void prepare()
    {
    }

    public Category[] getCategoriesAsArray()
    {
        List<Category> cats = new Vector<Category>(categories.size());
        for (Enumeration<Category> en = categories.elements(); en.hasMoreElements();)
        {
            Category c = en.nextElement();
            cats.add(c);
        }

        Collections.sort(cats, new Comparator<Category>(){
            public int compare (Category c1, Category c2)
            {
                return c2.uid - c1.uid;
            }
        });

        return (Category[])cats.toArray();
    }

    public Vector getAddresses ()
    {
        return addresses;
    }

    public Hashtable getCategories ()
    {
        return categories;
    }

    public City getCity ()
    {
        return city;
    }

    public Crossroad[] getCrossroads ()
    {
        return crossroads;
    }

    public Icon[] getIcons ()
    {
        return icons;
    }

    public Vector getPois ()
    {
        return pois;
    }

    public Street[] getStreets ()
    {
        return streets;
    }

    public void setStreets (Street[] streets)
    {
        this.streets = streets;
    }

    public void setPois (Vector pois)
    {
        this.pois = pois;
    }

    public void setIcons (Icon[] icons)
    {
        this.icons = icons;
    }

    public void setCrossroads (Crossroad[] crossroads)
    {
        this.crossroads = crossroads;
    }

    public void setCity (City city)
    {
        this.city = city;
    }

    public void setCategories (Hashtable categories)
    {
        this.categories = categories;
    }

    public void setAddresses (Vector addresses)
    {
        this.addresses = addresses;
    }
}
