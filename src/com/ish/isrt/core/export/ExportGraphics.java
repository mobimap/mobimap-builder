/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.isrt.core.export;

import com.ish.mobimapbuilder.MobimapConfiguration;
import java.io.DataOutputStream;
import com.ish.isrt.core.GraphicStreamLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Point;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.io.*;
import com.ish.isrt.io.Serializator;

public class ExportGraphics {

    private static final char FIELD_CONF_LAYERS = 'l';
    private static final char FIELD_CONF_STREAMS = 's';
    private static final char FIELD_CONF_DATA = 'd';

    private static final char FIELD_LAYER_ID = 'i';
    private static final char FIELD_LAYER_NAME = 'n';
    private static final char FIELD_LAYER_STREAMS = 's';

    private static final char FIELD_DATA_ID = 'i';
    private static final char FIELD_DATA_FILE = 'f';

    private static final char FIELD_STREAM_LAYERID = 'L';
    private static final char FIELD_STREAM_DATAID = 'S';

    private static final char FIELD_STREAM_LEVEL = 'l';
    private static final char FIELD_STREAM_ZOOMMIN = 'z';
    private static final char FIELD_STREAM_ZOOMMAX = 'Z';
    private static final char FIELD_STREAM_XMIN = 'x';
    private static final char FIELD_STREAM_XMAX = 'X';
    private static final char FIELD_STREAM_YMIN = 'y';
    private static final char FIELD_STREAM_YMAX = 'Y';
    private static final char FIELD_STREAM_FILLED = 'f';
    private static final char FIELD_STREAM_FILL = 'F';
    private static final char FIELD_STREAM_OUTLINED = 'o';
    private static final char FIELD_STREAM_OUTLINE = 'O';

    private MobimapConfiguration mobimapConfiguration;
    private String exportDirData;
    private int globalScale;

    private static final Map<String,Character> dictionary;

    static {
        dictionary = new HashMap<String,Character>();
        dictionary.put("level", FIELD_STREAM_LEVEL);
        dictionary.put("zoomMin", FIELD_STREAM_ZOOMMIN);
        dictionary.put("zoomMax", FIELD_STREAM_ZOOMMAX);
        dictionary.put("xmin", FIELD_STREAM_XMIN);
        dictionary.put("xmax", FIELD_STREAM_XMAX);
        dictionary.put("ymin", FIELD_STREAM_YMIN);
        dictionary.put("ymax", FIELD_STREAM_YMAX);
        dictionary.put("filled", FIELD_STREAM_FILLED);
        dictionary.put("fill", FIELD_STREAM_FILL);
        dictionary.put("outlined", FIELD_STREAM_OUTLINED);
        dictionary.put("outline", FIELD_STREAM_OUTLINE);
    }

    public ExportGraphics (MobimapConfiguration mobimapConfiguration, String exportDirData, int globalScale) {
        this.mobimapConfiguration = mobimapConfiguration;
        this.exportDirData = exportDirData;
        this.globalScale = globalScale;
    }

    public void make () {

        // find unique streams
        Set<String> streams = new HashSet<String>();

        Vector layersSer = new Vector ();

        for (int layerId = 0; layerId < mobimapConfiguration.getLayers ().size(); layerId++) {
            MobimapConfiguration.Layer layer = mobimapConfiguration.getLayers ().get(layerId);

            Hashtable rec = new Hashtable();
            layersSer.add(rec);
            rec.put(FIELD_LAYER_ID, layerId);
            rec.put(FIELD_LAYER_NAME, layer.getName());

            for (MobimapConfiguration.Stream stream : layer.getStreams ()) {
                streams.add (stream.getSource ());
            }
        }

        // export unique streams
        Map<String, Integer> streamName2DataIdIndex = new HashMap<String, Integer>();

        Vector dataSer = new Vector();

        int dataId = 0;
        Iterator<String> it = streams.iterator ();
        while (it.hasNext ()) {
            String name = it.next ();
            String fileName = AbstractExport.file_gs4 + dataId;
            exportGsName (name, fileName);

            streamName2DataIdIndex.put (name, dataId);

            Hashtable rec = new Hashtable();
            dataSer.add(rec);
            rec.put(FIELD_DATA_ID, dataId);
            rec.put(FIELD_DATA_FILE, fileName);

            dataId++;
        }

        // stream records work
        Vector streamsSer = new Vector ();

        for (int layerId = 0; layerId < mobimapConfiguration.getLayers ().size(); layerId++) {
            MobimapConfiguration.Layer layer = mobimapConfiguration.getLayers ().get(layerId);

            for (MobimapConfiguration.Stream stream : layer.getStreams ()) {

                Hashtable streamRecordSer = new Hashtable ();
                streamsSer.add (streamRecordSer);

                for (Iterator<String> parIt = stream.getParams ().keySet().iterator(); parIt.hasNext();) {
                    String name = parIt.next();
                    Object value = stream.getParams ().get(name);

                    if (dictionary.containsKey(name)) {
                        streamRecordSer.put(dictionary.get(name), value);
                    } else {
                        streamRecordSer.put(name, value);
                    }
                }
                streamRecordSer.put (FIELD_STREAM_DATAID, streamName2DataIdIndex.get (stream.getSource ()));
                streamRecordSer.put (FIELD_STREAM_LAYERID, layerId);
            }
        }

        // export the packet
        Hashtable packet = new Hashtable();
        packet.put(FIELD_CONF_LAYERS, layersSer);
        packet.put(FIELD_CONF_DATA, dataSer);
        packet.put(FIELD_CONF_STREAMS, streamsSer);

        writePacket(packet);
    }

    private void writePacket (Object packet) {

        try {
            FileOutputStream ostream = new FileOutputStream (exportDirData + AbstractExport.file_gsindex);
            DataOutputStream out = new DataOutputStream (ostream);

            Serializator.writeObject (out, packet);

            out.close ();
        } catch (IOException ex) {
        }
    }

    private void exportGsName (String gsName, String gsFile) {
        GraphicStreamLoader gsl = new GraphicStreamLoader ();

        gsl.loadStream (gsName);
        int[] data = gsl.getStream (gsName);

        try {
            exportGSinternal (data, gsFile, 0);
        } catch (IOException ex) {
        }
    }

    private void exportGSinternal (int data[], String gsName, int optimizeGS) throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream ();
        DataOutputStream out = new DataOutputStream (baout);

        List<Integer> gsp = new ArrayList<Integer>(10000);

        final int GS_EOF = 0;
        final int GS_MOVETO = 1;
        final int GS_LINETO = 2;
        final int GS_CURVETO = 3;
        final int GS_STROKECOLOR = 4;
        final int GS_FILLCOLOR = 5;
        final int GS_SETLINEWIDTH = 6;
        final int GS_STROKE = 7;
        final int GS_CLOSESTROKE = 8;
        final int GS_FILL = 9;

        int x = 0, y = 0, r = 0, g = 0, b = 0, rf = 0xff, gf = 0xff, bf = 0xff;
        int xMem = 0, yMem = 0;

        final int segLimit = 1000;
        int segx[] = new int[segLimit + 1];
        int segy[] = new int[segLimit + 1];
        int segN = 0;
        int opts = 0, optcrv = 0;

        int i = 0;
        while (data[i] != GS_EOF && i < data.length - 6) {
            int command = data[i], commandnext = data[i + 1];
            switch (command) {
                case GS_MOVETO:
                    segN = 0;
                    x = data[++i];
                    y = data[++i];
                    x += xMem;
                    y += yMem;
                    xMem = x;
                    yMem = y;
                    segx[segN] = x;
                    segy[segN] = y;
                    if (segN < segLimit) {
                        segN++;
                    }
                    break;
                case GS_LINETO:
                    x = data[++i];
                    y = data[++i];
                    x += xMem;
                    y += yMem;
                    xMem = x;
                    yMem = y;
                    segx[segN] = x;
                    segy[segN] = y;
                    if (segN < segLimit) {
                        segN++;
                    }
                    break;
                case GS_CURVETO:
                    int tmpx[] = new int[4];
                    int tmpy[] = new int[4];
                    tmpx[0] = xMem;
                    tmpy[0] = yMem;
                    for (int j = 0; j < 3; j++) {
                        x = data[++i];
                        y = data[++i];
                        x += xMem;
                        y += yMem;
                        xMem = x;
                        yMem = y;
                        tmpx[j + 1] = x;
                        tmpy[j + 1] = y;
                    }
                    for (int j = 1; j <= 4; j++) {
                        int v = Bezier (tmpx, j);
                        int w = Bezier (tmpy, j);
                        segx[segN] = v;
                        segy[segN] = w;
                        if (segN < segLimit) {
                            segN++;
                        }
                    }
                    break;
                case GS_STROKECOLOR:
                    r = data[++i];
                    g = data[++i];
                    b = data[++i];
                    break;
                case GS_FILLCOLOR:
                    rf = data[++i];
                    gf = data[++i];
                    bf = data[++i];
                    break;
                case GS_SETLINEWIDTH:
                    i++;
                    break;
                case GS_CLOSESTROKE:
                    segx[segN] = segx[0];
                    segy[segN] = segy[0];
                    segN++;
                case GS_STROKE:
                case GS_FILL:
                    if (segN < 1000 && segN > 0) {
                        int gs2command = 0;
                        if (command == GS_STROKE || command == GS_CLOSESTROKE || commandnext == GS_STROKE) {
                            gs2command |= 1;
                        }
                        if (command == GS_FILL || commandnext == GS_FILL) {
                            gs2command |= 2;
                        }

                        int vn = optimizeGSandWrite (out, segx, segy, segN, r, g, b,
                            optimizeGS, gs2command, rf, gf, bf);

                        if (vn > 0) {
                            gsp.add (vn);
                        } else {
                            optcrv++;
                        }
                        opts += segN - (vn - 11) / 2;
                    }
                    segN = 0;
                    break;
            }
            i++;
        }
        out.close ();

        int len = baout.toByteArray ().length / 2;

        FileOutputStream gsstream = new FileOutputStream (exportDirData + gsName);
        out = new DataOutputStream (gsstream);

        out.writeInt (len);
        out.write (baout.toByteArray ());
        out.writeInt (gsp.size ());
        for (i = 0; i < gsp.size (); i++) {
            out.writeShort (gsp.get (i));
        }

        out.close ();
    }

    private int Bezier (int k[], int n) {
        double t = ((double) (n)) / 4;
        double s = 1 - t, t2 = t * t;
        return (int) (((s * k[0] + 3 * t * k[1]) * s + 3 * t2 * k[2]) * s + t2 * t * k[3]);
    }

    private int optimizeGSandWrite (DataOutputStream out, int vx[],
                                    int vy[], int n, int r, int g, int b,
                                    int optimizeGS, int command, int rf, int gf,
                                    int bf) throws IOException {
        Point[] src = new Point[n];
        for (int i = 0; i < n; i++) {
            src[i] = new Point (vx[i], vy[i]);
        }

        Point opt[];
        int m = n;
        if (optimizeGS > 0) {
            opt = new Point[n];
            boolean mk[] = new boolean[n];
            m = poly_simplify (optimizeGS, src, n, opt, mk);
        } else {
            opt = src;
        }

        int xmin = 0xffff, ymin = 0xffff, xmax = 0, ymax = 0;
        for (int j = 0; j < m; j++) {
            int x = opt[j].x;
            int y = opt[j].y;
            if (x < xmin) {
                xmin = x;
            }
            if (x > xmax) {
                xmax = x;
            }
            if (y < ymin) {
                ymin = y;
            }
            if (y > ymax) {
                ymax = y;
            }
        }

        int dim = Math.max (xmax - xmin, ymax - ymin);

        if (dim < optimizeGS) {
            return 0;
        }

        int cx = (xmax + xmin) / 2, cy = (ymax + ymin) / 2;

        out.writeShort (cx / globalScale);
        out.writeShort (cy / globalScale);
        out.writeShort ((cx - xmin) / globalScale);
        out.writeShort ((cy - ymin) / globalScale);

        out.writeShort (command);

        out.writeShort (r);
        out.writeShort (g);
        out.writeShort (b);
        out.writeShort (rf);
        out.writeShort (gf);
        out.writeShort (bf);

        int dx = xmin / globalScale, dy = ymin / globalScale;
        for (int j = 0; j < m; j++) {
            int x = opt[j].x / globalScale;
            int y = opt[j].y / globalScale;
            out.writeShort (x - dx);
            out.writeShort (y - dy);

            dx = x;
            dy = y;
        }

        int len = 11 + m * 2;

        return len;
    }

    private double dot (Point u, Point v) {
        return u.x * v.x + u.y * v.y;
    }

    private double norm2 (Point v) {
        return dot (v, v);
    }

    private double norm (Point v) {
        return Math.sqrt (norm2 (v));
    }

    private double d2 (Point u, Point v) {
        return (u.x - v.x) * (u.x - v.x) + (u.y - v.y) * (u.y - v.y);
    }

    private double d (Point u, Point v) {
        return Math.sqrt (d2 (u, v));
    }

    class Segment {
        public Point P0, P1;
    }


// poly_simplify():
//    Input:  tol = approximation tolerance
//            V[] = polyline array of vertex points
//            n   = the number of points in V[]
//    Output: sV[]= simplified polyline vertices (max is n)
//    Return: m   = the number of points in sV[]
    int poly_simplify (float tol, Point V[], int n, Point sV[], boolean mk[]) {
        int i, k, m, pv; // misc counters
        float tol2 = tol * tol; // tolerance squared
        Point vt[] = new Point[n]; // vertex buffer
        //boolean mk[] = new boolean [n]; // marker buffer
        mk[0] = false;

        // STAGE 1.  Vertex Reduction within tolerance of prior vertex cluster
        //vt[0] = V[0]; // start at the beginning
        //for (i = k = 1, pv = 0; i < n; i++)
        //{
        //if (d2 (V[i], V[pv]) < tol2)
        //	continue;
        //	vt[k++] = V[i];
        //pv = i;
        //}
        //if (pv < n - 1)
        //	vt[k++] = V[n - 1]; // finish at the end

        // STAGE 2.  Douglas-Peucker polyline simplification
        mk[0] = mk[n - 1] = true; // mark the first and last vertices
        simplifyDP (tol, V, 0, n - 1, mk);

        // copy marked vertices to the output simplified polyline
        for (i = m = 0; i < n; i++) {
            if (mk[i]) {
                sV[m++] = V[i];
            }
        }
        return m; // m vertices in simplified polyline
    }

// simplifyDP():
//  This is the Douglas-Peucker recursive simplification routine
//  It just marks vertices that are part of the simplified polyline
//  for approximating the polyline subchain v[j] to v[k].
//    Input:  tol = approximation tolerance
//            v[] = polyline array of vertex points
//            j,k = indices for the subchain v[j] to v[k]
//    Output: mk[] = array of markers matching vertex array v[]
    void simplifyDP (double tol, Point v[], int j, int k, boolean mk[]) {
        if (k <= j + 1) { // there is nothing to simplify
            return;
        }

        // check for adequate approximation by segment S from v[j] to v[k]
        int maxi = j; // index of vertex farthest from S
        double maxd2 = 0; // distance squared of farthest vertex
        double tol2 = tol * tol; // tolerance squared
        Segment S = new Segment ();
        S.P0 = v[j];
        S.P1 = v[k];
        Point u = new Point (S.P1.x - S.P0.x, S.P1.y - S.P0.y); // segment direction vector
        double cu = dot (u, u); // segment length squared

        // test each vertex v[i] for max distance from S
        // compute using the Feb 2001 Algorithm's dist_Point_to_Segment()
        // Note: this works in any dimension (2D, 3D, ...)
        Point w;
        Point Pb; // base of perpendicular from v[i] to S
        double b, cw, dv2; // dv2 = distance v[i] to S squared

        for (int i = j + 1; i < k; i++) {
            // compute distance squared
            w = new Point (v[i].x - S.P0.x, v[i].y - S.P0.y);
            cw = dot (w, u);
            if (cw <= 0) {
                dv2 = d2 (v[i], S.P0);
            } else if (cu <= cw) {
                dv2 = d2 (v[i], S.P1);
            } else {
                b = cw / cu;
                Pb = new Point ((int) (S.P0.x + b * u.x),
                                (int) (S.P0.y + b * u.y));
                dv2 = d2 (v[i], Pb);
            }
            // test with current max distance squared
            if (dv2 <= maxd2) {
                continue;
            }
            // v[i] is a new max vertex
            maxi = i;
            maxd2 = dv2;
        }
        if (maxd2 > tol2) { // error is worse than the tolerance
            // split the polyline at the farthest vertex from S
            mk[maxi] = true; // mark v[maxi] for the simplified polyline
            // recursively simplify the two subpolylines at v[maxi]
            simplifyDP (tol, v, j, maxi, mk); // polyline v[j] to v[maxi]
            simplifyDP (tol, v, maxi, k, mk); // polyline v[maxi] to v[k]
        }
        // else the approximation is OK, so ignore intermediate vertices
        return;
    }

}
