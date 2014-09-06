/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/

/**
 (c) http://www.cse.unsw.edu.au/~cs3421/slides/clip/PolyClipApplet.html
 */
package com.ish.mobimapbuilder.optimizer;

import com.ish.mobimapbuilder.model.Point2;

/**
 * Edge.java represents a half plane defined by an edge of a clip polygon for
 * a polygon clipping algorithm
 */

class Edge
{
  private double x1,y1; //start point
  private double x2,y2; //end point
  private double a,b,c; // (x,y) is inside the edge if a*x + b*y + c > 0;
  //we use ints to store the equation to avoid round off problems

  Edge(double x1, double y1, double x2, double y2) {
    a = y2 - y1;
    b = x1 - x2;
    c = -a*x1 - b*y1;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  /* is this point inside the half plane defined by the edge? */
  public boolean inside(Point2 p){
    return a*p.x + b*p.y + c > 0;
  }

  /* return the intersection of this edge with another one from p1 to p2 */
  /* rounded to nearest int */
  public Point2 intersect(Point2 p1, Point2 p2)
  {
    double d = p2.y - p1.y;
    double e = p1.x - p2.x;
    double f = -d*p1.x - e*p1.y;
    double denom = e*a - b*d;
    double x = Math.round((b*f - e*c)/denom);
    double y = Math.round((d*c - a*f)/denom);
    return new Point2(x,y);
  }

  /* Convert to a string - handy for debugging */
  public String toString() {
    return "("+x1+","+y1+")-("+x2+","+y2+")";
  }

}
