/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/

/**
 (c) http://www.cse.unsw.edu.au/~cs3421/slides/clip/PolyClipApplet.html
 */

package com.ish.mobimapbuilder.optimizer;

import java.awt.*;

import com.ish.mobimapbuilder.model.*;

/* Sutherland-Hodgeman polygon clipping algorithm */

public class SutherlandHodgemanAlgorithm
{
	/* clip p against r and return result */
	Polygon clipPolygon (final Polygon p, Rectangle r)
	{
		Polygon lastClipped = p;
		lastClipped = clipSide (lastClipped,
								new Edge (r.x, r.y, r.x, r.y + r.height));
		lastClipped = clipSide (lastClipped,
								new Edge (r.x, r.y + r.height, r.x + r.width,
										  r.y + r.height));
		lastClipped = clipSide (lastClipped,
								new Edge (r.x + r.width, r.y + r.height, r.x + r.width,
										  r.y));
		lastClipped = clipSide (lastClipped,
								new Edge (r.x + r.width, r.y, r.x, r.y));

		return lastClipped;
	}

	/* clip p against Edge e and return result */
	Polygon clipSide (final Polygon p, Edge e)
	{
		Point2 intersect;
		if (p.npoints == 0) {
			return p; //nothing to do
		}
		Polygon clipped = new Polygon ();
		Point2 lastp = new Point2 (p.xpoints[p.npoints - 1], p.ypoints[p.npoints - 1]);
		for (int i = 0; i < p.npoints; i++)
        {
			Point2 thisp = new Point2 (p.xpoints[i], p.ypoints[i]);
			if (e.inside (thisp) && e.inside (lastp)) {
				clipped.addPoint ((int)thisp.x, (int)thisp.y);
			}
			else if (!e.inside (thisp) && e.inside (lastp)) {
				intersect = e.intersect (thisp, lastp);
				clipped.addPoint ((int)intersect.x, (int)intersect.y);
			}
			else if (!e.inside (thisp) && !e.inside (lastp)) {
                /*nothing */
			}
			else if (e.inside (thisp) && !e.inside (lastp)) {
				intersect = e.intersect (lastp, thisp);
				clipped.addPoint ((int)intersect.x, (int)intersect.y);
				clipped.addPoint ((int)thisp.x, (int)thisp.y);
			}
			lastp = thisp;
		}
		return clipped;
	}
}
