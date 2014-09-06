//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.export;

public class ExportFactory
{
    private ExportFactory ()
    {
    }

    public static AbstractExport getExportEngine (int level)
    {
        AbstractExport exporter = null;
        if (level == 4)
            exporter = new Export2MobimapQuadra();
        else
            throw new IllegalArgumentException("Requested level " + level + " is unsupported!");

        return exporter;
    }
}
