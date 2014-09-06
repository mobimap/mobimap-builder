/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import java.util.*;

public class IdRecordComparator
	implements Comparator
{
	public IdRecordComparator ()
	{
	}

	public int compare (Object o1, Object o2)
	{
        AbstractRecord br1 = (AbstractRecord)o1;
        AbstractRecord br2 = (AbstractRecord)o2;

		return (br1.id < br2.id)?-1: (br1.id == br2.id)? 0: +1;
	}
}
