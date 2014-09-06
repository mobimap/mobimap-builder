/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.optimizer;

import com.ish.mobimapbuilder.model.*;

public class PolylineSimplifier
{
    double tolerance;

	public PolylineSimplifier (double tolerance)
	{
        this.tolerance = tolerance;
	}

    private double dot (Point2 u, Point2 v)
    {
        return u.x * v.x + u.y * v.y;
    }
    private double norm2 (Point2 v)
    {
        return dot (v, v);
    }
    private double norm (Point2 v)
    {
        return Math.sqrt (norm2 (v));
    }
    private double d2 (Point2 u, Point2 v)
    {
        return (u.x - v.x) * (u.x - v.x) + (u.y - v.y) * (u.y - v.y);
    }
    private double d (Point2 u, Point2 v)
    {
        return Math.sqrt (d2 (u, v));
    }

    class Segment
    {
        public Point2 P0, P1;
    }

// poly_simplify():
//    Input:  tol = approximation tolerance
//            V[] = polyline array of vertex points
//            n   = the number of points in V[]
//    Output: sV[]= simplified polyline vertices (max is n)
//    Return: m   = the number of points in sV[]
    int poly_simplify (double tol, Point2 V[], int n, Point2 sV[], boolean mk [])
    {
        int i, k, m, pv; // misc counters
        double tol2 = tol * tol; // tolerance squared
        Point2 vt[] = new Point2[n]; // vertex buffer
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
        for (i = m = 0; i < n; i++)
        {
            if (mk[i])
                sV[m++] = V[i];
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
    void simplifyDP (double tol, Point2 v[], int j, int k, boolean mk[])
    {
        if (k <= j + 1) // there is nothing to simplify
            return;

        // check for adequate approximation by segment S from v[j] to v[k]
        int maxi = j; // index of vertex farthest from S
        double maxd2 = 0; // distance squared of farthest vertex
        double tol2 = tol * tol; // tolerance squared
        Segment S = new Segment ();
        S.P0 = v[j];
        S.P1 = v[k];
        Point2 u = new Point2 (S.P1.x - S.P0.x, S.P1.y - S.P0.y); // segment direction vector
        double cu = dot (u, u); // segment length squared

        // test each vertex v[i] for max distance from S
        // compute using the Feb 2001 Algorithm's dist_FloatPoint_to_Segment()
        // Note: this works in any dimension (2D, 3D, ...)
        Point2 w;
        Point2 Pb; // base of perpendicular from v[i] to S
        double b, cw, dv2; // dv2 = distance v[i] to S squared

        for (int i = j + 1; i < k; i++)
        {
            // compute distance squared
            w = new Point2 (v[i].x - S.P0.x, v[i].y - S.P0.y);
            cw = dot (w, u);
            if (cw <= 0)
                dv2 = d2 (v[i], S.P0);
            else if (cu <= cw)
                dv2 = d2 (v[i], S.P1);
            else
            {
                b = cw / cu;
                Pb = new Point2 ((int) (S.P0.x + b * u.x),
                                (int) (S.P0.y + b * u.y));
                dv2 = d2 (v[i], Pb);
            }
            // test with current max distance squared
            if (dv2 <= maxd2)
                continue;
            // v[i] is a new max vertex
            maxi = i;
            maxd2 = dv2;
        }
        if (maxd2 > tol2) // error is worse than the tolerance
        {
            // split the polyline at the farthest vertex from S
            mk[maxi] = true; // mark v[maxi] for the simplified polyline
            // recursively simplify the two subpolylines at v[maxi]
            simplifyDP (tol, v, j, maxi, mk); // polyline v[j] to v[maxi]
            simplifyDP (tol, v, maxi, k, mk); // polyline v[maxi] to v[k]
        }
        // else the approximation is OK, so ignore intermediate vertices
        return;
    }

    public Record make (Record initial)
    {
        Record result = initial.makeCopy();

        Shape[] shapes = initial.getShapes ();

        for (int k = 0; k < shapes.length; k++)
        {
            Shape shape = shapes[k];
            int n = shape.xCoords.length;

            // fill source array
            Point2 [] fps = new Point2 [n];
            for (int i=0; i < n; i++)
            {
                fps [i] = new Point2 (shape.xCoords[i], shape.yCoords[i]);
            }

            Point2[] opt = new Point2[n];
            boolean mk[] = new boolean[n];
            int m = poly_simplify (tolerance, fps, n, opt, mk);

            shape.xCoords = new double[m];
            shape.yCoords = new double[m];

            for (int i=0; i < m; i++)
            {
                shape.xCoords[i] = opt[i].x;
                shape.yCoords[i] = opt[i].y;
            }
        }
        return result;
    }
}
