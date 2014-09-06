/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2007-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.gg;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 2D Map
 */
public class Map2D<T> {
    private Map<Point, List<T>> map = new HashMap<Point, List<T>>();

    /**
     * Put value at specified point
     * @param point Point
     * @param value T
     */
    public void put (Point point, T value) {
        List<T> list = map.get (point);
        if (list == null) {
            list = new ArrayList<T>();
            map.put (point, list);
        }
        list.add (value);
    }

    /**
     * Get value at specified point
     * @param point Point
     * @return List
     */
    public List<T> get (Point point) {
        return map.get (point);
    }

    /**
     * Remove value at specified point
     * @param point Point
     * @param value T
     */
    public void remove (Point point, T value) {
        List<T> list = get (point);
        if (list != null) {
            list.remove (value);
            if (list.size() == 0) {
                map.remove(point);
            }
        }
    }
}
