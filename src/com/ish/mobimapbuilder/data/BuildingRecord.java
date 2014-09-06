/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import java.util.Map;
import java.text.*;
import com.ish.isrt.core.data.tref;

public class BuildingRecord
    extends AbstractRecord
    implements Comparable
{
    public int x, y;
    public tref ref = new tref();
    public String name;
    public int icon;
    public Map<String,String> meta;

	public BuildingRecord ()
	{
	}

    public int compareTo (Object oj)
    {
        BuildingRecord r = (BuildingRecord)oj;
        Collator myCollator = Collator.getInstance();
        myCollator.setStrength(Collator.IDENTICAL);
        myCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        return myCollator.compare(name, r.name);
    }
}
