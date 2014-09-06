//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.export;

import java.util.Properties;

import com.ish.isrt.core.CityLoader;

abstract public class AbstractExport
{
    // file names obfuscation
    static final String file_blname = "a";
    static final String file_blst = "b";
    static final String file_blx = "c";
    static final String file_bly = "d";
    static final String file_city = "e";
    static final String file_conn = "f";
    static final String file_conp = "g";
    static final String file_conpi = "h";
    static final String file_ct = "i";
    static final String file_ct4 = "i";
    static final String file_elcon = "j";
    static final String file_ellen = "k";
    static final String file_gs = "l";
    static final String file_gs3 = "l3";
    static final String file_gs4 = "l";
    static final String file_gsi = "m";
    static final String file_gsi3 = "m3";
    static final String file_lb = "n";
    static final String file_name = "o";
    static final String file_pcx = "p";
    static final String file_pcy = "q";
    static final String file_rt = "r";
    static final String file_sp = "s";
    static final String file_sprtref = "t";
    static final String file_stname = "u";
    static final String file_img = "v";
    static final String file_info = "w";
    static final String file_gsindex = "x";

    protected boolean hasAddresses, hasLabels, hasTransport, hasGS;
    protected boolean hasCoordinates, hasRouting, hasRoadDirs;

    protected int pcExport, elementExport, streetExport, namesNmaxExport,
    cityX, cityY, originX, originY, scale;

    protected int hardKey;
    protected int providerCode;

//    public void go (java.awt.Frame frame, CityLoader parent) {}

    public void make (CityLoader cityLoader, Properties ini) {}

//    public void archive (int n) {}
}
