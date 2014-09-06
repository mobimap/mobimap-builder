/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import java.text.*;

public class StreetRecord
    extends AbstractRecord
    implements Comparable
{
	private String name;
	private String type;
	private String village;
    private String streetId;

	public StreetRecord ()
	{
	}
    public StreetRecord (int id, String name, String type, String village, String streetId)
    {
        this();
        this.id = id;
        this.name = name.trim();
        this.type = type;
        this.village = village;
        this.streetId = streetId;
    }
	public String toString ()
	{
		return name;
	}
    public int compareTo(Object a)
    {
        StreetRecord s = (StreetRecord)a;
        Collator myCollator = Collator.getInstance();
        myCollator.setStrength(Collator.IDENTICAL);
        myCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        int c = myCollator.compare(name, s.name);
        if (c == 0 && type != null && s.type != null)
            c = myCollator.compare (type, s.type);
        if (c == 0 && village != null && s.village != null)
            c = myCollator.compare(village, s.village);
        return c;
    }

	public void setType (String type)
	{
		this.type = type;
	}

	public void setName (String name)
	{
		this.name = name;
	}

	public void setVillage (String village)
	{
		this.village = village;
	}

	public void setStreetId (String streetId)
	{
		this.streetId = streetId;
	}

	public String getType ()
	{
		return type;
	}

	public String getName ()
	{
		return name;
	}

	public String getVillage ()
	{
		return village;
	}

	public String getStreetId ()
	{
		return streetId;
	}

	public String getFullName()
    {
        String fullName = name;
        if (type != null)
            fullName += " " + type;
        if (village != null)
            fullName += " (" + village + ")";
        return fullName;
    }
}
