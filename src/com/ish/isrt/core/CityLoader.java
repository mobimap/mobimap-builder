//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.io.*;
import java.util.*;
import com.ish.isrt.core.export.*;
import com.ish.mobimapbuilder.MobimapConfiguration;

public class CityLoader
{
    private MobimapConfiguration mobimapConfiguration;

    public com.ish.isrt.core.Map theMap;
    public boolean loaded;

    public String ownerKey;

    public boolean loadFromCache;
    public String cacheName;
    public String cacheRoutes;

    static public java.util.Map<String,byte[]> binaryContents;

    public Project project;

    public CityLoader (java.util.Map<String,byte[]> binaryContents, MobimapConfiguration mobimapConfiguration)
    {
        this.binaryContents = binaryContents;
        this.mobimapConfiguration = mobimapConfiguration;
    }

    public MobimapConfiguration getMobimapConfiguration() {
        return mobimapConfiguration;
    }

    public String getParameter (String a)
    {
        String s = mobimapConfiguration.getParams().getProperty(a);
        if (s == null) {
            if (a.startsWith("mobimap.")) {
                s = mobimapConfiguration.getParams ().getProperty (a.substring("mobimap.".length()));
            }
        }
        return s;
    }

    public String getRootParameter (String a)
    {
        return ""; //(String) desktop.ini.get (a);
    }

    public void init (byte[] d)
    {
        InputStream input = new ByteArrayInputStream(d);
        I7MapParser parser = new I7MapParser(input);
        project = parser.make();

        theMap = new com.ish.isrt.core.Map (this);
        theMap.load();

        theMap.loadGS ();
        theMap.prepare ();
    }

    public void export()
    {
        AbstractExport exporter = ExportFactory.getExportEngine(4);

        Properties ini = new Properties ();
        boolean hasIni = false;
        try
        {
            InputStream os = getClass().getResourceAsStream("/export/export.ini");
            if (os == null) {
                try {
                    os = new FileInputStream ("export/export.ini");
                } catch (FileNotFoundException ex) {
                }
            }
            if (os != null) {
                ini.load (os);
                hasIni = true;
            }
        }
        catch (IOException x) {
            System.err.println (x);
        }

        // export data
        exporter.make(this, ini);

        if (hasIni) {
            Archivator archivator = new Archivator (this, exporter, ini);
            archivator.setFeature (Archivator.FEATURE_ONLINE_CATEGORIES, true);
            archivator.setFeature (Archivator.FEATURE_ONLINE_ADDRESSES, false);
            archivator.setFeature (Archivator.FEATURE_ONLINE_OBJECTS, true);
            archivator.setFeature (Archivator.FEATURE_BROWSER, true);
            archivator.setCopyright ("");

            archivator.archive ();
        }
    }
}
