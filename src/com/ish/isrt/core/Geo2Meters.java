//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import org.w3c.dom.*;
import java.io.IOException;
import java.io.DataOutputStream;

public class Geo2Meters
{
    private static double west = 0, east = 0, north = 0, south = 0;
    private static double kx = 1, ky = 1;
    private static double globalScale = 1;

    public Geo2Meters(double west, double south, double east, double north, double globalScale)
    {
        this.west = west;
        this.east = east;
        this.south = south;
        this.north = north;
        this.globalScale = globalScale;

        double lon = (west + east) / 2;
        double lat = (north + south) / 2;
        double deltaLon = east - west;
        double deltaLat = north - south;

        kx = find_distance (lat, lon-0.5, lat, lon+0.5) / globalScale;
        ky = find_distance (lat-0.5, lon, lat+0.5, lon) / globalScale;
    }

    public static double getX (double lat, double lon)
    {
        return (lon - west) * kx;
    }

    public static double getY (double lat, double lon)
    {
        return (lat - south) * ky;
    }

    public static double getLat (double x, double y)
    {
        return y / ky + south;
    }

    public static double getLon (double x, double y)
    {
        return x / kx + west;
    }

    private double find_distance (double StartLat, double StartLong, double EndLat,
                                  double EndLong)
    {
        /*
          // ������������ ������/������� � �������� � ����� �����
          StartLat  : Double;                           // ��������� ������
          StartLong : Double;                           // ��������� �������
          EndLat    : Double;                           // �������� ������
          EndLong   : Double;                           // �������� �������
          // ����������, ������������ ��� ���������� �������� � ����������
          fPhimean  : Double;                           // ������� ������
          fdLambda  : Double;                           // ������� ����� ����� ���������� �������
          fdPhi     : Double;                           // ������� ����� ����� ���������� ������
          fAlpha    : Double;                           // ��������
          fRho      : Double;                           // ������������ ������ ��������
          fNu       : Double;                           // ���������� ������ ��������
          fR        : Double;                           // ������ ����� �����
          fz        : Double;                           // ������� ���������� �� ������ ��������
          fTemp     : Double;                           // ��������� ����������, �������������� � �����������
          Distance  : Double;                           // ����������� ���������� � ������
          Bearing   : Double;                           // ����������� �� � �� ��������
         */

        // ���������, ������������ ��� ���������� �������� � ����������
        double D2R = 0.017453; // ��������� ��� �������������� �������� � �������
        double R2D = 57.295781; // ��������� ��� �������������� ������ � �������
        double a = 6378137.0; // �������� �������
        double b = 6356752.314245; // ���������� �������
        double e2 = 0.006739496742337; // ������� ��������������� ����������
        double f = 0.003352810664747; // ������������ ����������

        // ��������� ������� ����� ����� ��������� � �������� � �������� ������� ������
        double fdLambda = (StartLong - EndLong) * D2R;
        double fdPhi = (StartLat - EndLat) * D2R;
        double fPhimean = ((StartLat + EndLat) / 2.0) * D2R;
        // ��������� ����������� � ���������� ������� �������� ������� ������
        double fTemp = 1 - e2 * (Math.pow (Math.sin (fPhimean), 2));
        double fRho = (a * (1 - e2)) / Math.pow (fTemp, 1.5);
        double fNu = a / (Math.sqrt (1 - e2 * (Math.sin (fPhimean) * Math.sin (fPhimean))));
        // ��������� ������� ����������
        double fz = Math.sqrt (Math.pow (Math.sin (fdPhi / 2.0), 2) + Math.cos (EndLat * D2R) *
                               Math.cos (StartLat * D2R) *
                               Math.pow (Math.sin (fdLambda / 2.0), 2));
        fz = 2 * Math.asin (fz);
        // ��������� ��������
        double fAlpha = Math.cos (EndLat * D2R) * Math.sin (fdLambda) * 1 / Math.sin (fz);
        fAlpha = Math.asin (fAlpha);
        // ��������� ������ �����
        double fR = (fRho * fNu) /
            ((fRho * Math.pow (Math.sin (fAlpha), 2)) + (fNu * Math.pow (Math.cos (fAlpha), 2)));
        // �������� �������� � ����������
        double $Distance = (fz * fR);

        return $Distance;
    }

    public void store (DataOutputStream os, int scale) throws IOException
    {
        os.writeInt ((int) (south * 100000));
        os.writeInt ((int) (west * 100000));
        os.writeInt ((int) kx / scale);
        os.writeInt ((int) ky / scale);
    }
}
