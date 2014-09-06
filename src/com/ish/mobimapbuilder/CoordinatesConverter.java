/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.DOMException;
import com.ish.mobimapbuilder.xml.XMLUtil;

public class CoordinatesConverter {
    private static CoordinatesConverter instance = null;

    private Matrix trans;
    private Matrix revers;

    private CoordinatesConverter () {

    }

    synchronized public static CoordinatesConverter getInstance (Node rootNode) {
        if (instance == null) {
            instance = new CoordinatesConverter ();
            if (!instance.set (rootNode)) {
                instance = null;
            }
        }
        return instance;
    }

    private boolean set (Node rootNode) {
        boolean isSet = false;
        int pointN = 0;
        double[] aX = new double[3];
        double[] aY = new double[3];
        double[] aLat = new double[3];
        double[] aLon = new double[3];

        try {

            for (Node pointNode = rootNode.getFirstChild ();
                                  pointNode != null && pointN < 3;
                                  pointNode = pointNode.getNextSibling ()) {
                String pointNodeName = pointNode.getNodeName ();
                if (pointNodeName.equals ("point")) {
                    NamedNodeMap attribs = pointNode.getAttributes ();
                    if (attribs == null) {
                        continue;
                    }

                    String strX = XMLUtil.getAttributeValue (attribs, "x");
                    String strY = XMLUtil.getAttributeValue (attribs, "y");
                    String strLat = XMLUtil.getAttributeValue (attribs, "latitude");
                    if (strLat == null) {
                        strLat = XMLUtil.getAttributeValue (attribs, "lat");
                    }
                    String strLon = XMLUtil.getAttributeValue (attribs, "longitude");
                    if (strLon == null) {
                        strLon = XMLUtil.getAttributeValue (attribs, "lon");
                    }

                    if (strX == null || strY == null || strLat == null || strLon == null) {
                        continue;
                    }

                    try {
                        double x = Double.parseDouble (strX);
                        double y = Double.parseDouble (strY);
                        double lat = Double.parseDouble (strLat);
                        double lon = Double.parseDouble (strLon);

                        aX[pointN] = x;
                        aY[pointN] = y;
                        aLat[pointN] = lat;
                        aLon[pointN] = lon;

                        pointN++;
                    } catch (NumberFormatException ex1) {
                    }
                }
            }
        } catch (DOMException ex) {
        }

        if (pointN == 3) {
            calcMatrix (aX, aY, aLat, aLon);
            isSet = true;

//            System.out.println (trans);
//            System.out.println (revers);
//
//            System.out.println (getX(aLat[0], aLon[0]));
//            System.out.println (getY(aLat[0], aLon[0]));
//            System.out.println (getLat(aX[0], aY[0]));
//            System.out.println (getLon(aX[0], aY[0]));
        }

        return isSet;
    }

    private void calcMatrix (double[] aX, double[] aY, double[] aLat, double[] aLon) {
        Matrix A = new Matrix (aX[0], aY[0], 1,
                               aX[1], aY[1], 1,
                               aX[2], aY[2], 1);

        // calculate first colomn
        Matrix A1 = new Matrix (A);
        A1.replaceColomn (0, aLat[0], aLat[1], aLat[2]);
        Matrix A2 = new Matrix (A);
        A2.replaceColomn (1, aLat[0], aLat[1], aLat[2]);
        Matrix A3 = new Matrix (A);
        A3.replaceColomn (2, aLat[0], aLat[1], aLat[2]);

        double detA = A.det ();
        double m11 = A1.det () / detA;
        double m21 = A2.det () / detA;
        double m31 = A3.det () / detA;

        // calculate second colomn
        Matrix B1 = new Matrix (A);
        B1.replaceColomn (0, aLon[0], aLon[1], aLon[2]);
        Matrix B2 = new Matrix (A);
        B2.replaceColomn (1, aLon[0], aLon[1], aLon[2]);
        Matrix B3 = new Matrix (A);
        B3.replaceColomn (2, aLon[0], aLon[1], aLon[2]);

        double m12 = B1.det () / detA;
        double m22 = B2.det () / detA;
        double m32 = B3.det () / detA;

        trans = new Matrix (m11, m12, 0, m21, m22, 0, m31, m32, 1);

        revers = new Matrix (trans);
        revers.invert ();
    }

    public double getLat (double x, double y) {
        double lat = x * trans.v[0][0] + y * trans.v[1][0] + trans.v[2][0];
        return lat;
    }

    public double getLon (double x, double y) {
        double lon = x * trans.v[0][1] + y * trans.v[1][1] + trans.v[2][1];
        return lon;
    }

    public double getX (double lat, double lon) {
        double x = lat * revers.v[0][0] + lon * revers.v[1][0] + revers.v[2][0];
        return x;
    }

    public double getY (double lat, double lon) {
        double y = lat * revers.v[0][1] + lon * revers.v[1][1] + revers.v[2][1];
        return y;
    }

    private class Matrix {
        double v[][];

        Matrix (double a11, double a12, double a13,
                double a21, double a22, double a23,
                double a31, double a32, double a33) {
            v = new double[3][];
            v[0] = new double[3];
            v[1] = new double[3];
            v[2] = new double[3];

            v[0][0] = a11;
            v[0][1] = a12;
            v[0][2] = a13;
            v[1][0] = a21;
            v[1][1] = a22;
            v[1][2] = a23;
            v[2][0] = a31;
            v[2][1] = a32;
            v[2][2] = a33;
        }

        Matrix (Matrix m) {
            v = new double[3][];
            for (int i = 0; i < 3; i++) {
                v[i] = new double[3];
                for (int j = 0; j < 3; j++) {
                    v[i][j] = m.v[i][j];
                }
            }
        }

        Matrix (double a) {
            v = new double[3][];
            for (int i = 0; i < 3; i++) {
                v[i] = new double[3];
                for (int j = 0; j < 3; j++) {
                    v[i][j] = 0;
                }
            }
            v[0][0] = v[1][1] = a;
            v[2][2] = 1;
        }

        double det () {
            double d = v[0][0] * (v[1][1] * v[2][2] - v[1][2] * v[2][1]) +
                       v[0][1] * (v[1][2] * v[2][0] - v[1][0] * v[2][2]) +
                       v[0][2] * (v[1][0] * v[2][1] - v[1][1] * v[2][0]);
            return d;
        }

        void replaceColomn (int n, double b1, double b2, double b3) {
            v[0][n] = b1;
            v[1][n] = b2;
            v[2][n] = b3;
        }

        void invert () {
            Matrix out = new Matrix (1);

            for (int i = 0; i < 3; i++) {
                double d = v[i][i];

                for (int j = 0; j < 3; j++) {
                    out.v[i][j] /= d;
                    v[i][j] /= d;
                }
                for (int j = 0; j < 3; j++) {
                    if (j != i) {
                        if (v[j][i] != 0.0) {
                            double mulby = v[j][i];

                            for (int k = 0; k < 3; k++) {
                                v[j][k] -= mulby * v[i][k];
                                out.v[j][k] -= mulby * out.v[i][k];
                            }
                        }
                    }
                }
            }
            this.v = out.v;
        }

        public String toString () {
            return "| " + v[0][0] + " " + v[0][1] + " " + v[0][2] + " |\n" +
                "| " + v[1][0] + " " + v[1][1] + " " + v[1][2] + " |\n" +
                "| " + v[2][0] + " " + v[2][1] + " " + v[2][2] + " |";
        }
    }
}
