/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2007-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.gg;

import java.util.*;
import static java.lang.Math.*;

import com.ish.mobimapbuilder.data.*;

/**
 * <p>Class for generating road net.</p>
 */
public class GraphGenerator {
    private int threshold = 20; // precision of calc.
    private boolean intersectSegments = false;

    /**
     * Start of segment. Origin for crossroad sorting
     */
    private double X1, Y1;

    private class ElementCrossroad implements Comparable<ElementCrossroad> {
        int n = 0;

        ElementCrossroad (int n) {
            this.n = n;
        }

        public boolean equals (Object a) {
            ElementCrossroad other = (ElementCrossroad) a;
            return n == other.n;
        }

        public int compareTo (ElementCrossroad other) {
            double p1x = pc (n).x;
            double p1y = pc (n).y;
            double p2x = pc (other.n).x;
            double p2y = pc (other.n).y;

            double s1 = hypot (X1 - p1x, Y1 - p1y);
            double s2 = hypot (X1 - p2x, Y1 - p2y);

            return s1 < s2 ? -1 : 1;
        }
    };

    /**
     * Class for crossroad candidates
     */
    private class CrossroadCandidate {
        double x, y;
        int str;
        int left, right;
    };

    static final int CROSSROADS_CAPACITY = 500;
    static final int CACHE_CELL = 1000;

    private List<CrossroadRecordDouble> crossroads;
    private Map<String, List<Integer>> oldCache;

    private Cache cache = new Cache ();

    public GraphGenerator () {
        crossroads = new ArrayList<CrossroadRecordDouble>(CROSSROADS_CAPACITY);
        crossroads.add (new CrossroadRecordDouble ( -1000, -1000));
//        crossroads.add (null);

        oldCache = new HashMap<String, List<Integer>>(CROSSROADS_CAPACITY / 3,
            CROSSROADS_CAPACITY / 3);
    }

    /**
     * Creates segment between two points
     * @param cur_street int
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @param direction int
     * @param type int
     */
    public void createSeg (int cur_street, double x1, double y1, double x2, double y2,
                           int direction, int type) {
        if (intersectSegments) {
            createSegWithIntersect (cur_street, x1, y1, x2, y2, direction, type);
        } else {
            createSegWithoutIntersect (cur_street, x1, y1, x2, y2, direction, type);
        }
    }

    /**
     * Intersect segments
     * @param cur_street int
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @param direction int
     * @param type int
     */
    private void createSegWithIntersect (int cur_street, double x1, double y1, double x2,
                                         double y2, int direction, int type) {
        int N = pcN ();
        LinkedList<ElementCrossroad> per = new LinkedList<ElementCrossroad>();

        X1 = x1;
        Y1 = y1;

        List<CrossroadCandidate> addpc = new Vector<CrossroadCandidate>(); // list for add-pc-transactions

        // check that there're existing crossroads that can belong to the segment
        List<CrossroadRecordDouble> potentialOwn = cache.retrieveCrossroads ((int) x1, (int) y1);
        potentialOwn.addAll (cache.retrieveCrossroads ((int) x2, (int) y2));

        for (int i = 0; i < potentialOwn.size (); i++) {
            CrossroadRecordDouble p = potentialOwn.get (i);
            double x3 = p.x, y3 = p.y;

            if (areSame (x1, y1, x3, y3) || areSame (x2, y2, x3, y3)) {
                per.add (new ElementCrossroad (p.id));
            }
        }

        // check intersections of the segment with existing crossroads
        List<Pair> links = cache.retrieveLinks ((int) x1, (int) y1, (int) x2, (int) y2);
        for (Pair pair : links) {
            int first = pair.first;
            int second = pair.second;

            CrossroadRecordDouble p = pc (first);
            double x3 = p.x, y3 = p.y;

            CrossroadRecordDouble n = pc (second);
            double x4 = n.x, y4 = n.y;

            double x, y; // coordinates of intersection

            // поиск общей точки
            double divider = (y2 - y1) * (x4 - x3) - (y4 - y3) * (x2 - x1);
            double divident = (y3 * x4 - y4 * x3) * (x2 - x1) - (y1 * x2 - y2 * x1) * (x4 - x3);

            if (Math.abs (divider) < 0.001) {
                continue;
            } else {
                x = (divident / divider);

                if (x2 == x1) {
                    y = (y4 - y3) / (x4 - x3) * (x - x3) + y3;
                } else {
                    y = (y2 - y1) / (x2 - x1) * (x - x1) + y1;
                }
            }

            boolean inside1 = isPointInsideRectangle (x, y, x1, y1, x2, y2);
            boolean inside2 = isPointInsideRectangle (x, y, x3, y3, x4, y4);

            boolean hasIntersection = false;
            if (inside1 && inside2) {
                hasIntersection = true;
            } else if (inside1 && !inside2) {
                double[] prj3 = distanceFromPointToSegment (x3, y3, x1, y1, x2, y2);
                double[] prj4 = distanceFromPointToSegment (x4, y4, x1, y1, x2, y2);

                double[] prj = (prj3[0] < prj4[0]) ? prj3 : prj4;
                if (prj[0] < threshold) {
                    x = prj[1];
                    y = prj[2];
                    if (isPointInsideRectangle (x, y, x1, y1, x2, y2)) {
                        hasIntersection = true;
                    }
                }
            } else if (!inside1 && inside2) {
                double[] prj1 = distanceFromPointToSegment (x1, y1, x3, y3, x4, y4);
                double[] prj2 = distanceFromPointToSegment (x2, y2, x3, y3, x4, y4);

                double[] prj = (prj1[0] < prj2[0]) ? prj1 : prj2;
                if (prj[0] < threshold) {
                    x = prj[1];
                    y = prj[2];
                    if (isPointInsideRectangle (x, y, x3, y3, x4, y4)) {
                        hasIntersection = true;
                    }
                }
            } else {
                double d13 = distanceSquared (x1, y1, x3, y3);
                double d23 = distanceSquared (x2, y2, x3, y3);
                double d14 = distanceSquared (x1, y1, x4, y4);
                double d24 = distanceSquared (x2, y2, x4, y4);

                double th2 = threshold * threshold;
                if (d13 < th2) {
                    x = (x1 + x3) / 2;
                    y = (y1 + y3) / 2;
                    hasIntersection = true;
                } else if (d23 < th2) {
                    x = (x2 + x3) / 2;
                    y = (y2 + y3) / 2;
                    hasIntersection = true;
                } else if (d14 < th2) {
                    x = (x1 + x4) / 2;
                    y = (y1 + y4) / 2;
                    hasIntersection = true;
                } else if (d24 < th2) {
                    x = (x2 + x4) / 2;
                    y = (y2 + y4) / 2;
                    hasIntersection = true;
                }
            }

            if (hasIntersection) {
                // проверка, не принадлежит ли точка пересечения перекрестку
                if (areSame (x, y, x3, y3)) {
                    per.add (new ElementCrossroad (first));
                    p.x = x;
                    p.y = y; // "притянуть" перекресток к сегменту
                } else if (areSame (x, y, x4, y4)) {
                    per.add (new ElementCrossroad (second));
                    n.x = x;
                    n.y = y; // "притянуть" перекресток к сегменту
                } else {
                    // insert between i and next
                    int str = 0;
                    for (Connection con : p.con) {
                        if (con.pc == second) {
                            str = con.street;
                        }
                    }

                    CrossroadCandidate q = new CrossroadCandidate ();
                    q.x = x;
                    q.y = y;
                    q.str = str;
                    q.left = first;
                    q.right = second;
                    addpc.add (q);
                }
            }
        }

        // Create pc from add-pc-transactions list
        for (ListIterator<CrossroadCandidate> it = addpc.listIterator (); it.hasNext (); ) {
            CrossroadCandidate q = it.next ();
            int cur = add (q.x, q.y);
            per.add (new ElementCrossroad (cur));
            int d = getDirection (q.left, q.right);
            int t = getType (q.left, q.right);
            breakLink (q.left, q.right);
            addLink (q.left, cur, q.str, d, t);
            addLink (cur, q.right, q.str, d, t);
        }

        // 4. Сортировка pc по дальности, добавление концов сегмента
        Collections.sort (per);

        if (per.size () > 0) {
            int first = per.get (0).n;
            if (!areSame (x1, y1, pc (first).x, pc (first).y)) {
                per.addFirst (new ElementCrossroad (add (x1, y1)));
            }
            int last = per.get (per.size () - 1).n;
            if (!areSame (x2, y2, pc (last).x, pc (last).y)) {
                per.addLast (new ElementCrossroad (add (x2, y2)));
            }
        } else {
            per.addFirst (new ElementCrossroad (add (x1, y1)));
            per.addLast (new ElementCrossroad (add (x2, y2)));
        }

        // 5. Соединение перекрестков
        int perSize = per.size ();
        for (int i = 0; i < perSize - 1; i++) {
            int u = i;
            u++;
            if (per.get (i).n == (per.get (u)).n) { // удаление повторных
                continue;
            }
            addLink (per.get (i).n, per.get (u).n, cur_street, direction, type);
        }
    }

    /**
     * Just merge segments at end-points
     * @param cur_street int
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @param direction int
     * @param type int
     */
    private void createSegWithoutIntersect (int cur_street, double x1, double y1,
                                            double x2, double y2,
                                            int direction, int type) {
        int N = pcN ();
        LinkedList<ElementCrossroad> per = new LinkedList<ElementCrossroad>();

        X1 = x1;
        Y1 = y1;

        int xlow = (int) Math.floor ((x1 < x2) ? x1 : x2);
        int xhigh = (int) Math.floor ((x1 > x2) ? x1 : x2) + CACHE_CELL;
        int ylow = (int) Math.ceil ((y1 < y2) ? y1 : y2);
        int yhigh = (int) Math.ceil ((y1 > y2) ? y1 : y2) + CACHE_CELL;

        for (int ax = xlow; ax < xhigh; ax += CACHE_CELL) {
            for (int ay = ylow; ay < yhigh; ay += CACHE_CELL) {
                List<Integer> list = oldCache.get (genXY ((int) ax, (int) ay));
                if (list == null) {
                    continue;
                }

                for (int li = 0; li < list.size (); li++) {
                    int i = list.get (li).intValue ();

                    if (pc (i) != null) {
                        CrossroadRecordDouble p = pc (i);
                        double x3 = p.x, y3 = p.y;

                        // test to find out if pc belonges on segment
                        if (areSame (x1, y1, x3, y3) || areSame (x2, y2, x3, y3)) {
                            per.add (new ElementCrossroad (i));
                        }
                    }
                }
            }
        }

        // 4. Сортировка pc по дальности, добавление концов сегмента
        Collections.sort (per);

        if (per.size () > 0) {
            int first = per.get (0).n;
            if (!areSame (x1, y1, pc (first).x, pc (first).y)) {
                per.addFirst (new ElementCrossroad (add (x1, y1)));
            }
            int last = per.get (per.size () - 1).n;
            if (!areSame (x2, y2, pc (last).x, pc (last).y)) {
                per.addLast (new ElementCrossroad (add (x2, y2)));
            }
        } else {
            per.addFirst (new ElementCrossroad (add (x1, y1)));
            per.addLast (new ElementCrossroad (add (x2, y2)));
        }

        // 5. Соединение перекрестков
        int perSize = per.size ();
        for (int i = 0; i < perSize - 1; i++) {
            int u = i;
            u++;
            if (per.get (i).n == (per.get (u)).n) { // удаление повторных
                continue;
            }
            addLink (per.get (i).n, per.get (u).n, cur_street, direction, type);
        }
    }

    private CrossroadRecordDouble pc (int n) {
        return crossroads.get (n);
    }

    private int pcN () {
        return crossroads.size ();
    }

    private double[] distanceFromPointToSegment (double x, double y, double x1, double y1, double x2, double y2) {
        // formula is taken from http://algolist.manual.ru/maths/geom/distance/pointline.php
        double v2 = distanceSquared (x1 - x2, y1 - y2);
        double u = (y1 - y2) * x + (x2 - x1) * y + (x1 * y2 - x2 * y1);
        double u2 = u * u;
        double d2 = u2 / v2;

        double delta2 = d2;

        double w2 = distanceSquared (x - x1, y - y1);
        double t2 = distanceSquared (x - x2, y - y2);

        double a2 = w2 - d2;
        if (a2 > v2)
            delta2 = t2;

        double b2 = t2 - d2;
        if (b2 > v2)
            delta2 = w2;

        if (a2 < 0) {
            a2 = 0; // a2 can be slightly below 0
        }

        double v = sqrt (v2);
        double a = sqrt (a2);

        double[] result = new double[3];
        result[0] = sqrt (delta2);
        result[1] = ((v > 1) ? (x1 + ((x2 - x1) * a) / v) : x1 + 1);
        result[2] = ((v > 1) ? (y1 + ((y2 - y1) * a) / v) : y1 + 1);

        return result;
    }

    private double distanceSquared (double a, double b) {
        return a * a + b * b;
    }

    private double distanceSquared (double x1, double y1, double x2, double y2) {
        return distanceSquared (x1 - x2, y1 - y2);
    }

    private boolean isPointInsideRectangle (double x, double y, double x1, double y1, double x2, double y2) {
        return x >= min (x1, x2) && x <= max (x1, x2) && y >= min (y1, y2) && y <= max (y1, y2);
    }

    /**
     * Compares two points. Returns true if they are close to each other
     * @param x1 double
     * @param y1 double
     * @param x2 double
     * @param y2 double
     * @return boolean
     */
    private boolean areSame (double x1, double y1, double x2, double y2) {
        return hypot (x2 - x1, y2 - y1) < threshold;
    }

    private int add (double x, double y) {
        CrossroadRecordDouble crossroad = new CrossroadRecordDouble (x, y);
        crossroads.add (crossroad);
        int n = crossroads.size () - 1;
        crossroad.id = n;

        int ix = (int) round (x);
        int iy = (int) round (y);
        String key = genXY (ix, iy);
        List<Integer> list = oldCache.containsKey (key) ?
                             oldCache.get (key) : new Vector<Integer>(CROSSROADS_CAPACITY / 3);
        list.add (new Integer (n));
        oldCache.put (key, list);

        cache.addCrossroad (crossroad);

        return n;
    }

    private int getDirection (int left, int right) {
        return crossroads.get (left).getDirection (right);
    }

    private int getType (int left, int right) {
        return crossroads.get (left).getType (right);
    }

    private void addLink (int left, int right, int street, int direction, int type) {
        crossroads.get (left).addLink (right, street, direction, type);
        crossroads.get (right).addLink (left, street, -direction, type);

        cache.addLink (new Pair (left, right));
        cache.addLink (new Pair (right, left));
    }

    private void breakLink (int left, int right) {
        cache.removeLink (new Pair (left, right));
        cache.removeLink (new Pair (right, left));

        crossroads.get (left).breakLink (right);
        crossroads.get (right).breakLink (left);
    }

    private String genXY (int x, int y) {
        return "" + (x / CACHE_CELL) + "," + (y / CACHE_CELL);
    }

    /**
     * Set custom threshold
     * @param threshold int
     */
    public void setThreshold (int threshold) {
        this.threshold = threshold;
    }

    /**
     * Set segments intersection mode.
     * @param intersectSegments boolean true, if segments can intersect each other; false
     * if segments can only connect each other
     */
    public void setIntersectSegments (boolean intersectSegments) {
        this.intersectSegments = intersectSegments;
    }

    /**
     * Get resulting list of crossroads
     * @return List
     */
    public List<CrossroadRecord> getCrossroads () {
        List<CrossroadRecord> list = new ArrayList<CrossroadRecord>(crossroads.size());
        for (CrossroadRecordDouble c : crossroads) {
            CrossroadRecord r = new CrossroadRecord((int)round(c.x), (int)round(c.y));
            r.con = c.con;
            r.id = c.id;
            r.uid = c.uid;
            list.add(r);

            if (abs(r.x) + abs(r.y) < 100) {
                System.out.println ("");
            }
        }
        return list;
    }


    /**
     * Crossroad cache.
     */
    class Cache implements CellParameters {

        private Map2D<Pair> pairCache = new Map2D<Pair>();
        private Map2D<CrossroadRecordDouble> crossroadCache = new Map2D<CrossroadRecordDouble>();

        /**
         * Add crossroad to crossroad cache
         * @param crossroad CrossroadRecordDouble
         */
        public void addCrossroad (CrossroadRecordDouble crossroad) {
            crossroadCache.put (new Point ((int)crossroad.x, (int)crossroad.y), crossroad);
        }

        /**
         * Get crossroad from cache
         * @param x int
         * @param y int
         * @return List
         */
        public List<CrossroadRecordDouble> retrieveCrossroads (int x, int y) {

            List<CrossroadRecordDouble> list = new ArrayList<CrossroadRecordDouble>();
            List cached = crossroadCache.get (new Point (x, y));
            if (cached != null) {
                list.addAll (cached);
            }
            return list;
        }

        /**
         * Add link to cache
         * @param pair Pair
         */
        public void addLink (Pair pair) {
            CrossroadRecordDouble crossroad = pc (pair.first);
            int x1 = (int)crossroad.x;
            int y1 = (int)crossroad.y;

            int neigh = pair.second;
            if (neigh < pair.first) {
                // for uniqueness
                return;
            }

            int x2 = (int)pc (neigh).x;
            int y2 = (int)pc (neigh).y;

            putRectangleToCache (x1, y1, x2, y2, new Pair (crossroad.id, neigh));
        }

        private void putRectangleToCache (int x1, int y1, int x2, int y2, Pair crossroadPair) {
            int xmin = min (x1, x2) - CELL_THRESHOLD;
            int xmax = max (x1, x2) + CELL_THRESHOLD;
            int ymin = min (y1, y2) - CELL_THRESHOLD;
            int ymax = max (y1, y2) + CELL_THRESHOLD;

            for (int x = xmin; x <= xmax; x += CELL_SIZE) {
                for (int y = ymin; y <= ymax; y += CELL_SIZE) {
                    pairCache.put (new Point (x, y), crossroadPair);
                }
            }
        }

        /**
         * Retrieve links from cache for specified rectangle
         * @param x1 int
         * @param y1 int
         * @param x2 int
         * @param y2 int
         * @return List
         */
        public List<Pair> retrieveLinks (int x1, int y1, int x2, int y2) {

            List<Pair> list = new ArrayList<Pair>();
            Set<Pair> unique = new HashSet<Pair>();

            int xmin = min (x1, x2) - CELL_SIZE;
            int xmax = max (x1, x2) + CELL_SIZE;
            int ymin = min (y1, y2) - CELL_SIZE;
            int ymax = max (y1, y2) + CELL_SIZE;

            for (int x = xmin; x <= xmax; x += CELL_SIZE) {
                for (int y = ymin; y <= ymax; y += CELL_SIZE) {
                    List<Pair> oneCell = pairCache.get (new Point (x, y));
                    if (oneCell == null) {
                        continue;
                    }
                    for (Pair pair : oneCell) {
                        if (!unique.contains (pair)) {
                            list.add (pair);
                            unique.add (pair);
                        }
                    }
                }
            }

            return list;
        }

        /**
         * Remove link from cache
         * @param pair Pair
         */
        public void removeLink (Pair pair) {
            CrossroadRecordDouble crossroad = pc (pair.first);
            int x1 = (int)crossroad.x;
            int y1 = (int)crossroad.y;

            int neigh = pair.second;
            if (neigh < pair.first) {
                // for uniqueness
                return;
            }

            int x2 = (int) pc (neigh).x;
            int y2 = (int) pc (neigh).y;

            removeRectangleFromCache (x1, y1, x2, y2, new Pair (crossroad.id, neigh));
        }

        private void removeRectangleFromCache (int x1, int y1, int x2, int y2, Pair crossroadPair) {
            int xmin = min (x1, x2) - CELL_THRESHOLD;
            int xmax = max (x1, x2) + CELL_THRESHOLD;
            int ymin = min (y1, y2) - CELL_THRESHOLD;
            int ymax = max (y1, y2) + CELL_THRESHOLD;

            for (int x = xmin; x <= xmax; x += CELL_SIZE) {
                for (int y = ymin; y <= ymax; y += CELL_SIZE) {
                    pairCache.remove (new Point (x, y), crossroadPair);
                }
            }
        }
    }
}
