//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

public class Matrix
{
    protected double x [][];

    public Matrix () { x = new double [3][3]; }
    public Matrix (double v)
    {
        x = new double [3][3];
        for ( int i = 0; i < 3; i++)
            for ( int j = 0; j < 3; j++)
                x [i][j] = (i == j) ? v : 0.0;

        x [2][2] = 1;
    }
    public void copyfrom (Matrix a)
    {
        for ( int i = 0; i < 3; i++)
            for ( int j = 0; j < 3; j++)
                x [i][j] = a.x [i][j];
    }


    public void Invert ()
    {
        Matrix Out = new Matrix (1);

        for ( int i = 0; i < 3; i++ )
        {
            double	d = x [i][i];

            if ( d != 1.0)
            {
                for ( int j = 0; j < 3; j++ )
                {
                    Out.x [i][j] /= d;
                    x [i][j]     /= d;
                }
            }

            for ( int j = 0; j < 3; j++ )
            {
                if ( j != i )
                {
                    if ( x [j][i] != 0.0)
                    {
                        double	mulby = x[j][i];

                        for ( int k = 0; k < 3; k++ )
                        {
                            x [j][k]     -= mulby * x [i][k];
                            Out.x [j][k] -= mulby * Out.x [i][k];
                        }
                    }
                }
            }
        }

        this. copyfrom (Out);
    }
    public Matrix	mult ( Matrix A, Matrix B )
    {
        Matrix	res = new Matrix (0);

        for ( int i = 0; i < 3; i++ )
            for ( int j = 0; j < 3; j++ )
            {
                double sum = 0;

                for ( int k = 0; k < 3; k++ )
                    sum += A.x [i][k] * B.x [k][j];

                res.x [i][j] = sum;
            }

        return res;
    }
    public Matrix multeq (Matrix B )
    {
        Matrix	res = new Matrix (0);

        for ( int i = 0; i < 3; i++ )
            for ( int j = 0; j < 3; j++ )
            {
                double sum = 0;

                for ( int k = 0; k < 3; k++ )
                    sum += x [i][k] * B. x [k][j];

                res.x [i][j] = sum;
            }
        this. copyfrom (res);
        return this;
    }
};

