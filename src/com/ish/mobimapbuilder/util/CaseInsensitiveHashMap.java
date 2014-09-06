/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.util;

import java.util.HashMap;

public class CaseInsensitiveHashMap<V> extends HashMap<String, V> {
    public V put (String key, V value) {
        return super.put (key.toUpperCase (), value);
    }

    public V get (Object key) {
        return super.get (((String) key).toUpperCase ());
    }
}
