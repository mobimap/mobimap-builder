/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

public class AbstractRecord
{
    /**
     * Local object id.
     */
    public int id = 0;

    /**
     * Global object id. Used for accessing objects from DB-server
     */
    public int uid = 0;
}
