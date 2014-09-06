//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

public class Vector2D
{
    public double x, y;

    public Vector2D () {};
    public Vector2D ( double v ) { x = y = v; }
    public Vector2D ( Vector2D v ) { x = v.x; y = v.y; }
    public Vector2D ( double vx, double vy ) { x = vx; y = vy; }

    public Vector2D equals ( Vector2D v ) { x = v.x; y = v.y; return this; }
    public Vector2D equals ( double f ) { x = y = f; return this; }
    public Vector2D unminus () {return new Vector2D( -x, -y);}
    public Vector2D sum (  Vector2D u,  Vector2D v ) {
        return new Vector2D( u.x + v.x, u.y + v.y);
    }
    public Vector2D dif (  Vector2D u,  Vector2D v ) {
        return new Vector2D( u.x - v.x, u.y - v.y);
    }
    public Vector2D	mult (  Vector2D u,  Vector2D v ) {
        return new Vector2D( u.x * v.x, u.y * v.y );
    }
    public Vector2D	mult ( Vector2D u, double f ) {
        return new Vector2D( u.x * f, u.y * f );
    }
    public Vector2D	mult ( double f,  Vector2D v )	{
        return new Vector2D( f * v.x, f * v.y );
    }
    public Vector2D	div (  Vector2D v, double f ) {
        return new Vector2D( v.x / f, v.y / f);
    }

    public Vector2D	mult ( Vector2D v, Matrix M )
    {
        Vector2D res = new Vector2D(1);

        res.x = v.x * M.x [0][0] + v.y * M.x [1][0] + M.x [2][0];
        res.y = v.x * M.x [0][1] + v.y * M.x [1][1] + M.x [2][1];

        return res;
    }
    public Vector2D	multeq ( Matrix M )
    {
        double rx = x * M.x [0][0] + y * M.x [1][0] + M.x [2][0];
        double ry = x * M.x [0][1] + y * M.x [1][1] + M.x [2][1];

        x = rx; y = ry;
        return this;
    }

    public Vector2D addeq ( Vector2D v ) {
        x += v.x; y += v.y;
        return this;
    }

    public double length () { return Math. sqrt ( x*x + y*y); };
};
