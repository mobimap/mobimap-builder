//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.export;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.text.*;
import java.nio.charset.Charset;

import com.ish.isrt.core.CityLoader;

public class Archivator
{
    private final static String DIR_EXPORT = "export";
    private final static String DIR_BINARIES = "binaries";
    private final static String DIR_RES = "res";
    private final static String DIR_STORE = "store";

    public final static int PROFILE_N = 24;
    public final static int PROFILE_PARAMSANDFEATURES = 0;
    public final static int PROFILE_SERVLET = 1;
    public final static int PROFILE_FEEDBACK = 2;
    public final static int PROFILE_PAGE_INFO = 3;
    public final static int PROFILE_MAP_INFO = 4;
    public final static int PROFILE_PACKAGE = 5;
    public final static int PROFILE_LOCALE = 6;
    public final static int PROFILE_START_LINK_TYPE = 7;
    public final static int PROFILE_START_LINK = 8;
    public final static int PROFILE_FONTSEGS = 9;
    public final static int PROFILE_SMSNUMBER = 10;
    public final static int PROFILE_SMSMESSAGE = 11;
    public final static int PROFILE_ADV_MENU = 12;
    public final static int PROFILE_ADV_ICON = 13;
    public final static int PROFILE_ADV_LINK_TYPE = 14;
    public final static int PROFILE_ADV_LINK = 15;
    public final static int PROFILE_EXPIRE = 16;
    public final static int PROFILE_PROGRESS = 17;
    public final static int PROFILE_EXPIRE_LINK_TYPE = 18;
    public final static int PROFILE_EXPIRE_LINK = 19;
    public final static int PROFILE_ADV2_MENU = 20;
    public final static int PROFILE_ADV2_ICON = 21;
    public final static int PROFILE_ADV2_LINK_TYPE = 22;
    public final static int PROFILE_ADV2_LINK = 23;

    public final static int FEATURE_N = 18;
    public final static int FEATURE_ONLINE_OBJECTS = 0;
    public final static int FEATURE_ONLINE_ADDRESSES = 1;
    public final static int FEATURE_ONLINE_CATEGORIES = 2;
    public final static int FEATURE_XMAP = 3;
    public final static int FEATURE_CFGKEYBOARD = 4;
    public final static int FEATURE_CFGDETAIL = 5;
    public final static int FEATURE_CFGNAMES = 6;
    public final static int FEATURE_CFGSEARCH = 7;
    public final static int FEATURE_CFGPALETTE = 8;
    public final static int FEATURE_CFGFONT = 9;
    public final static int FEATURE_BROWSER = 10;
    public final static int FEATURE_NOT_USED_11 = 11;
    public final static int FEATURE_SERVER_SEARCH = 12;
    public final static int FEATURE_NOT_USED_13 = 13;
    public final static int FEATURE_SHOWMEMORY = 14;
    public final static int FEATURE_SUN = 15;
    public final static int FEATURE_ROUTING = 16;
    public final static int FEATURE_ROADDIRS = 17;

    private Properties ini;       // params of export

    private AbstractExport exporter;

    private CityLoader cityLoader;

    private boolean[] features;
    private String copyright;

    private String sep;
    private String codeRoot;
    private String resRoot;

    public Archivator(CityLoader cityLoader,
                      AbstractExport exporter, Properties ini)
    {
        this.cityLoader = cityLoader;
        this.exporter = exporter;
        this.ini = ini;

        features = new boolean[FEATURE_N];
    }

    public void setFeature(int featureCode, boolean value)
    {
        features[featureCode] = value;
    }

    public void setCopyright(String copyright)
    {
        this.copyright = copyright;
    }

    public void archive ()
    {
        try
        {
            sep = System.getProperty("file.separator");

            String providerIdS = cityLoader.getParameter ("mobimap.provider");
            if (providerIdS == null) {
                // no provider -> no need to archive anything
                return;
            }
            int providerId = Integer.parseInt(providerIdS);

            String deviceName = (String) ini. get ("provider." + providerId + ".id");

            String midletVersion = (String) ini. get ("midlet.version");
            String midletVendor = (String) ini. get ("midlet.vendor");
            String midletMain = (String) ini. get ("midlet.main");
            String midletIcon = (String) ini. get ("midlet.icon");
            String deviceMidp = (String) ini. get ("provider." + providerId + ".midp");
            String deviceCldc = (String) ini. get ("provider." + providerId + ".cldc");
            String deviceText = (String) ini. get ("provider." + providerId + ".text");
            String mapName = cityLoader.getParameter ("mobimap.name");
            String mapNameLatin = cityLoader.getParameter ("mobimap.namelatin");
            String midletExecutable = "mobimap-" + mapName;
            String midletName = "Mobimap " + mapNameLatin;
            String midletDescription = "Mobimap / " + mapNameLatin;
            Calendar now = Calendar.getInstance();
            String mapVersion = "" + ((now.get(Calendar.YEAR) - 2000) * 10000 +
                (now.get(Calendar.MONTH) + 1) * 100 + now.get(Calendar.DAY_OF_MONTH));
            boolean isClassic = ini. get ("provider." + providerId + ".classic") != null;

            String sv = (String) ini.get ("provider." + providerId + ".midlet.version");
            if (sv != null) midletVersion = sv;

            String providerCodeS = (String) ini. get ("provider." + providerId + ".code");
            int providerCode = 0;
            if (providerCodeS != null)
            {
                providerCode = Integer.parseInt(providerCodeS);
            }

            String ignore [] = new String [20];
            int ignoreN = 0;

            boolean myhasAddresses = exporter.hasAddresses;
            boolean myhasTransport = exporter.hasTransport;
            boolean myhasGS = exporter.hasGS;

            if (!myhasAddresses)
            {
                ignore[ignoreN++] = exporter.file_blx;
                ignore[ignoreN++] = exporter.file_bly;
                ignore[ignoreN++] = exporter.file_blst;
                ignore[ignoreN++] = exporter.file_blname;
            }
            if (!myhasTransport)
            {
                ignore[ignoreN++] = exporter.file_sp;
                ignore[ignoreN++] = exporter.file_sprtref;
                ignore[ignoreN++] = exporter.file_rt;
            }

            String s = cityLoader.getParameter ("mobimap.cityid");
            int cityId = (s == null)? 0: Integer. valueOf (s). intValue ();
            String locale = cityLoader.getParameter ("mobimap.locale");
            mapName = cityLoader.getParameter ("mobimap.name");
            s = cityLoader.getParameter ("timezone");
            int timezone = (s == null)? 0: Integer. valueOf (s). intValue ();

            String distribPath = (String)ini.get ("distribFolder");

            String exportRoot = getExportRoot();
            String storeRoot = isClassic? (exportRoot + DIR_STORE + sep): (distribPath + sep);
            String storeCityRoot = storeRoot + (isClassic? cityId: mapName) + sep;
            String dir0 = isClassic?
                storeCityRoot + deviceName + sep + mapVersion + sep: storeCityRoot;
            String jarFileName = dir0 + midletExecutable + ".jar";
            String jadFileName = dir0 + midletExecutable + ".jad";
            String descFileName = dir0 + midletExecutable + ".desc";

            deleteFiles (new File (storeCityRoot));
            new File (dir0). mkdirs();

            codeRoot = DIR_EXPORT + sep + DIR_BINARIES + sep + deviceName + sep;

            String manifestContents =
                "Manifest-Version: 1.0\r\n" +
                "MIDlet-1: " + midletName + ", /" + midletIcon + ", " + midletMain + "\r\n" +
                "MIDlet-Icon: /" + midletIcon + "\r\n" +
                "MIDlet-Description: " + midletDescription + "\r\n" +
                "MIDlet-Name: " + midletName + "\r\n" +
                "MIDlet-Vendor: " + midletVendor + "\r\n" +
                "MIDlet-Version: " + midletVersion + "\r\n" +
                "MicroEdition-Configuration: " + deviceCldc + "\r\n" +
                "MicroEdition-Profile: " + deviceMidp + "\r\n";


            InputStream manifestIS = new ByteArrayInputStream (manifestContents. getBytes ());
            Manifest manifest = new Manifest (manifestIS);
            ByteArrayOutputStream baos = new ByteArrayOutputStream (10000);
            JarOutputStream jos = new JarOutputStream (baos);
            ZipEntry ze;

            ze = new ZipEntry ("META-INF/");
            ze.setMethod(java.util.zip.ZipOutputStream.STORED);
            ze.setSize(0);
            ze.setCrc(0);
            jos.putNextEntry (ze);
            jos.closeEntry ();

            ze = new ZipEntry ("META-INF/MANIFEST.MF");
            jos.putNextEntry (ze);
            jos.write (manifestContents. getBytes ());
            jos.closeEntry();

            resRoot = DIR_EXPORT + sep + "res" + sep + mapName + sep;

            /**
             * WRITE FILES (CODE + FONT + ICON)
             */
            writeFilesToArchive(jos, codeRoot + sep + "files", null);

            /**
             * WRITE LOCALE-DEPENDENT FILES (PAGES)
             */
            writeFilesToArchive(jos, codeRoot + sep + locale, null);

            /**
             * WRITE PROFILE
             */
            writeProfile(jos, providerId, locale);

            /**
             * WRITE LOCALE
             */
            ByteArrayOutputStream localeBaos = new ByteArrayOutputStream (1000);
            DataOutputStream los = new DataOutputStream (localeBaos);

            String localeFile = codeRoot + "locale_" + locale + ".txt";
            InputStream localeIS = getInputStream(localeFile);
            if (localeIS == null)
            {
                System.err.println ("Locale '" + localeFile + "' isn't found");
                return;
            }
            Reader rutf = new BufferedReader(new InputStreamReader(localeIS, "UTF8"));
            LineNumberReader r = new LineNumberReader (rutf);

            Vector v = new Vector (100);

            for (int n = 0; r.ready (); n++)
            {
                String a = r.readLine ();

                int ps = a.indexOf ('=');
                String key = a.substring(0, ps);
                String value = a.substring(ps+1);

                int k = Integer.parseInt(key);

                v.setSize(Math.max (k+1, v.size()));
                v.set(k, value);
            }

            int n = v.size();
            los.writeShort ((short)n);
            for (int i=0; i < n; i++)
            {
                los.writeUTF ( (String) v.get (i));
            }
            los.close ();
            r.close ();

            ze = new ZipEntry ("locale");
            jos.putNextEntry (ze);
            jos.write (localeBaos. toByteArray());
            jos.closeEntry();

            /**
             * WRITE DATA FILES
             */
            String dataSrc = "d";

            File dir = new File (resRoot + dataSrc);
            String files [] = dir. list ();

            ze = new ZipEntry ("d/");
            ze.setMethod(java.util.zip.ZipOutputStream.STORED);
            ze.setSize(0);
            ze.setCrc(0);
            jos.putNextEntry (ze);
            jos.closeEntry();

            for (int i=0; i < files. length; i++)
            {
                String srcFile = resRoot + dataSrc + sep + files [i];
                String destFile = "d/" + files [i];

                boolean f = true;
                for (int j=0; j < ignoreN; j++)
                    if (files [i].compareTo (ignore [j]) == 0) f = false;

                if (f)
                {
                    int compression = (files[i].compareTo (exporter.file_city) == 0) ?
                        java.util.zip.ZipOutputStream.STORED :
                        java.util.zip.ZipOutputStream.DEFLATED;

                    writeJarEntry (jos, srcFile, destFile, compression);
                }
            }

            /**
             * WRITE FONT
             */
//            dataSrc = "f";
//
//            dir = new File (codeRoot + "f");
//            files = dir. list ();
//
//            if (files != null)
//            {
//                ze = new ZipEntry ("f/");
//                ze.setMethod (java.util.zip.ZipOutputStream.STORED);
//                ze.setSize (0);
//                ze.setCrc (0);
//                jos.putNextEntry (ze);
//                jos.closeEntry ();
//
//                s = cityLoader.getParameter ("mobimap.font");
//                int fonts = s == null? 7: Integer.valueOf (s).intValue();
//
//                for (int i=0; i < 3; i++)
//                    if (((fonts >> i) & 0x1) != 0)
//                    {
//                        int a = i+1;
//                        writeJarEntry (jos,
//                                       codeRoot + "f" + sep + "mg" + a,
//                                       "f/" + "mg" + a,
//                                       java.util.zip.ZipOutputStream.STORED);
//                        writeJarEntry (jos,
//                                       codeRoot + "f" + sep + "mm" + a,
//                                       "f/" + "mm" + a,
//                                       java.util.zip.ZipOutputStream.DEFLATED);
//                    }
//            }

            /**
             * WRITE ICONS
             */
//            dataSrc = "i";
//
//            dir = new File (codeRoot + "i");
//            files = dir. list ();
//
//            if (files != null)
//            {
//                ze = new ZipEntry ("i/");
//                ze.setMethod (java.util.zip.ZipOutputStream.STORED);
//                ze.setSize (0);
//                ze.setCrc (0);
//                jos.putNextEntry (ze);
//                jos.closeEntry ();
//
//                for (int i = 0; i < files.length; i++)
//                {
//                    String srcFile = codeRoot + "i" + sep + files[i];
//                    String destFile = "i/" + files[i];
//
//                    writeJarEntry (jos, srcFile, destFile,
//                                   java.util.zip.ZipOutputStream.STORED);
//                }
//            }

            /**
             * WRITE PAGES
             */
            String pages = cityLoader.getParameter ("mobimap.pages");
            if (pages != null)
            {
                StringTokenizer st = new StringTokenizer(pages, " ,");
                while (st.hasMoreTokens())
                {
                    String srcFile = st.nextToken();
                    String destFile = srcFile;
                    int a = srcFile.lastIndexOf('/');
                    if (a >= 0 && a < srcFile.length())
                    {
						destFile = destFile.substring(a+1);
					}
                    writeJarEntry (jos, srcFile, destFile,
                                   java.util.zip.ZipOutputStream.DEFLATED);
                }
            }

            // close stream
            jos. close ();
            byte jarContents [] = baos. toByteArray();

            FileOutputStream fos = new FileOutputStream (jarFileName);
            fos.write (jarContents);
            fos. close ();

            // find .city offsets
            byte data [] = jarContents;
            int jarLength = jarContents.length;
            int crc1Offset=0, crc2Offset=0,dataOffset=0;

            for (int i=0; i < jarLength-4; i++)
            {
                byte b1 = data [i];
                byte b2 = data [i+1];
                byte b3 = data [i+2];
                byte b4 = data [i+3];

                if (b1 == 0x50 && b2 == 0x4b && b3 == 3 && b4 == 4)
                // local file header
                {
                    int name = i + 30;
                    if (data [name] == 'd' && data [name+1] == '/' &&
                        data [name+2] == 'e')
                    {
                        crc1Offset = i + 14;
                        dataOffset = i + 30 + data [i+26] + data [i+28];
                    }
                }
                if (b1 == 0x50 && b2 == 0x4b && b3 == 1 && b4 == 2)
                // central directory
                {
                    int name = i + 46;
                    if (data [name] == 'd' && data [name+1] == '/' &&
                        data [name+2] == 'e')
                    {
                        crc2Offset = i + 16;
                    }
                }

            }

            /**
             * WRITE .JAD
             */

            OutputStreamWriter w = new OutputStreamWriter (new FileOutputStream (jadFileName));

            String jad = "MIDlet-1: " + midletName + ", /" + midletIcon + ", " +
                midletMain + "\n" +
                "MIDlet-Description: " + midletDescription + "\n" +
                "MIDlet-Icon: /" + midletIcon + "\n" +
                "MIDlet-Name: " + midletName + "\n" +
                "MIDlet-Vendor: " + midletVendor + "\n" +
                "MIDlet-Version: " + midletVersion + "\n" +
                "MIDlet-Jar-Size: " + jarLength + "\n" +
                "MIDlet-Jar-URL: " + midletExecutable + ".jar\n" +
                "MicroEdition-Configuration: " + deviceCldc + "\n" +
                "MicroEdition-Profile: " + deviceMidp + "\n";

            w.write (jad);
            w.close ();

            /**
             * WRITE .DESC
             */
            if ("true".equalsIgnoreCase(cityLoader.getParameter("mobimap.hasdesc")))
            {
				w = new OutputStreamWriter (new FileOutputStream (descFileName),
											Charset.forName ("UTF-8"));

				String desc = "# Mobimap package description\n" +
					"cityid=" + cityId + "\ndevice=" + deviceName + "\nversion=" +
					mapVersion +
					"\nname=" + mapName + "\nnamelatin=" + mapNameLatin +
					"\nnamelocal=" + cityLoader.theMap.cityName +
					"\nmidletname=" + midletExecutable +
					"\nmidletsize=" + jarLength + "\nmidletversion=" + midletVersion +
					"\nlocale=" + locale + "\nreg=" + 'n' +
					"\npcn=" + exporter.pcExport + "\nstreetn=" + exporter.streetExport +
					"\nelementn=" + exporter.elementExport + "\nnamesnmax=" +
					exporter.namesNmaxExport +
					"\ncityx=" + exporter.cityX + "\ncityy=" + exporter.cityY +
					"\noriginx=" + exporter.originX + "\noriginy=" + exporter.originY +
					"\nscale=" + exporter.scale + "\ncitydata=" + dataOffset +
					"\ncrc1=" + crc1Offset + "\ncrc2=" + crc2Offset +
					"\nhasaddresses=" + false +"\nhaslabels=" + false +
					"\nhastransport=" + false +"\nhasgs=" + true +
					"\nonlineaddresses=" + false +"\nonlinelabels=" + false +
					"\nonlinemps=" + false +"\nmidp=" + deviceMidp +
					"\ncldc=" + deviceCldc + "\ndevicetext=" + deviceText +
					"\nhardkey=" + exporter.hardKey +
					"\nproviderid=" + providerCode +
					"\n";

				w.write (desc);
				w.close ();
			}

            System.out.println ("Midlet saved to: " + jarFileName + " (" + jarLength + " b)");
        }
        catch (IOException ex)
        {
            System.err.println(ex);
        }
    }

    private void writeFilesToArchive (JarOutputStream jos, String srcName, String destName)
        throws IOException
    {
        File dir = new File (srcName);
        File[] files = dir.listFiles ();
        if (files == null)
            return;

        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            String name = f.getName();

            String src = srcName + sep + name;
            String dest = destName == null? name: (destName + "/" + name);

            if (f.isFile ())
            {
                writeJarEntry (jos, src, dest, java.util.zip.ZipOutputStream.DEFLATED);
            }
            else if (f.isDirectory())
            {
                ZipEntry ze = new ZipEntry (dest + "/");
                ze.setMethod(java.util.zip.ZipOutputStream.STORED);
                ze.setSize(0);
                ze.setCrc(0);
                jos.putNextEntry (ze);
                jos.closeEntry();

                writeFilesToArchive(jos, src, dest);
            }
        }
    }

    private void writeProfile (JarOutputStream jos, int providerId, String locale)
        throws IOException
    {
        String servlet = cityLoader.getParameter ("mobimap.server");
        if (servlet == null)
            servlet = (String) ini.get ("provider." + providerId + ".servlet");

        if (servlet == null)
        {
            features[FEATURE_ONLINE_OBJECTS] = false;
            features[FEATURE_ONLINE_CATEGORIES] = false;
            features[FEATURE_ONLINE_ADDRESSES] = false;
            features[FEATURE_BROWSER] = false;
        }

        String[] profileContents = new String [PROFILE_N];

        profileContents[PROFILE_PARAMSANDFEATURES] =
            makeFeatures (providerId, locale, profileContents);

        profileContents[PROFILE_SERVLET] = servlet;
        profileContents[PROFILE_FEEDBACK] =
            (String) ini.get ("provider." + providerId + ".feedback");
        profileContents [PROFILE_MAP_INFO] = cityLoader.getParameter ("mobimap.about");;

        profileContents[PROFILE_PAGE_INFO] = cityLoader.getParameter ("mobimap.page.info");
        profileContents[PROFILE_START_LINK] = cityLoader.getParameter ("mobimap.start.link");
        profileContents[PROFILE_START_LINK_TYPE] = cityLoader.getParameter ("mobimap.start.linktype");

        profileContents[PROFILE_ADV_MENU] = cityLoader.getParameter ("mobimap.adv.menu");
        profileContents[PROFILE_ADV_LINK_TYPE] = cityLoader.getParameter ("mobimap.adv.linktype");
        profileContents[PROFILE_ADV_LINK] = cityLoader.getParameter ("mobimap.adv.link");

        profileContents[PROFILE_ADV2_MENU] = cityLoader.getParameter ("mobimap.adv2.menu");
        profileContents[PROFILE_ADV2_LINK_TYPE] = cityLoader.getParameter ("mobimap.adv2.linktype");
        profileContents[PROFILE_ADV2_LINK] = cityLoader.getParameter ("mobimap.adv2.link");

        String expire = cityLoader.getParameter ("mobimap.expire");
        /// TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1111111
        // must be null
        profileContents[PROFILE_EXPIRE] = Long.toString(Long.MAX_VALUE >> 16);
        if (expire != null)
        {
            SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy");

            expire.replace('/', '.');
			try {
				long time = df.parse (expire).getTime();
                profileContents[PROFILE_EXPIRE] = Long.toString(time);
			}
			catch (ParseException ex) {
                System.err.println ("ERROR: Couldn't parse expire date '" + expire + "'");
			}
		}

        profileContents[PROFILE_EXPIRE_LINK_TYPE] = cityLoader.getParameter ("mobimap.expire.linktype");
        profileContents[PROFILE_EXPIRE_LINK] = cityLoader.getParameter ("mobimap.expire.link");

        // adv icon
        String srcFile = cityLoader.getParameter ("mobimap.adv.icon");
        if (srcFile != null)
        {
			String destFile = srcFile;
			int a = srcFile.lastIndexOf ('/');
			if (a >= 0 && a < srcFile.length ()) {
				destFile = destFile.substring (a + 1);
			}
			writeJarEntry (jos, srcFile, destFile,
						   java.util.zip.ZipOutputStream.STORED);
            profileContents[PROFILE_ADV_ICON] = "/" + destFile;
		}

        // adv2 icon
        srcFile = cityLoader.getParameter ("mobimap.adv2.icon");
        if (srcFile != null)
        {
            String destFile = srcFile;
            int a = srcFile.lastIndexOf ('/');
            if (a >= 0 && a < srcFile.length ()) {
                destFile = destFile.substring (a + 1);
            }
            writeJarEntry (jos, srcFile, destFile,
                           java.util.zip.ZipOutputStream.STORED);
            profileContents[PROFILE_ADV2_ICON] = "/" + destFile;
		}

        // banner progress-bar icon
        srcFile = cityLoader.getParameter ("mobimap.banner");
        if (srcFile != null)
        {
            String destFile = srcFile;
            int a = srcFile.lastIndexOf ('/');
            if (a >= 0 && a < srcFile.length ()) {
                destFile = destFile.substring (a + 1);
            }
            writeJarEntry (jos, srcFile, destFile,
                           java.util.zip.ZipOutputStream.STORED);
            profileContents[PROFILE_PROGRESS] = "/" + destFile;
        }

        SimpleDateFormat sdf = new SimpleDateFormat ("yyMMdd");
        profileContents [PROFILE_PACKAGE] = sdf.format(new java.util.Date ());
        profileContents [PROFILE_LOCALE] = locale;

        String s = cityLoader.getParameter ("mobimap.font");
        profileContents [PROFILE_FONTSEGS] = s == null? "7": s;

        ByteArrayOutputStream profileBaos = new ByteArrayOutputStream (100);
        DataOutputStream os = new DataOutputStream (profileBaos);
        os.writeShort (PROFILE_N);
        for(int i=0; i < PROFILE_N; i++)
        {
            String a = profileContents[i];
            os.writeUTF (a == null? "": a);
        }
        os.close();

        ZipEntry ze = new ZipEntry ("profile");
        jos.putNextEntry (ze);
        jos.write (profileBaos. toByteArray());
        jos.closeEntry();
    }

    private String makeFeatures (int providerId, String locale,
                               String[] profileContents) throws NumberFormatException
    {
        String s = null;

        int profileKeyboard = 0;
        int profileDetail = 2;
        int profileNames = 0;
        int profileSearch = 1;
        int profilePalette = 1;
        int profileFont = 0;
        s = cityLoader.getParameter ("mobimap.profile.detail");
        int mapDetail = (s == null)? 0: Integer. valueOf (s). intValue ();
        s = cityLoader.getParameter ("mobimap.profile.names");
        int mapNames = (s == null)? 0: Integer. valueOf (s). intValue ();

        s = (String) ini.get ("provider." + providerId + ".feat.messenger");
        int featMessenger = (s == null)? 0: Integer.valueOf (s).intValue ();
        s = (String) ini.get ("provider." + providerId + ".feat.debug");
        int featMemoryInfo = (s == null)? 0: Integer.valueOf (s).intValue ();
        s = (String) ini.get ("provider." + providerId + ".feat.sun");
        int featSun = (s == null || !exporter.hasCoordinates)? 0: Integer.valueOf (s).intValue ();
        s = (String) ini.get ("provider." + providerId + ".feat.routing");
        int featRouting = (s == null || !exporter.hasRouting)? 0: Integer.valueOf (s).intValue ();
        int featRoadDirs = exporter.hasRoadDirs? 1: 0;

        if (locale.compareTo ("en") == 0) profileKeyboard = 2;

        char[] fs = new char[FEATURE_N];

        for (int i=0; i < fs.length; i++)
            fs[i] = '0';

        fs[FEATURE_ONLINE_OBJECTS] = features[FEATURE_ONLINE_OBJECTS]? '1':'0';
        fs[FEATURE_ONLINE_ADDRESSES] = features[FEATURE_ONLINE_ADDRESSES]? '1':'0';
        fs[FEATURE_ONLINE_CATEGORIES] = features[FEATURE_ONLINE_CATEGORIES]? '1':'0';
        fs[FEATURE_XMAP] = features[FEATURE_XMAP]? '1':'0';
        fs[FEATURE_CFGKEYBOARD] = (char)(profileKeyboard + '0');
        fs[FEATURE_CFGDETAIL] = (char)(Math.max (profileDetail, mapDetail) + '0');
        fs[FEATURE_CFGNAMES] = (char)(Math.max (profileNames, mapNames) + '0');
        fs[FEATURE_CFGSEARCH] = (char)(profileSearch + '0');
        fs[FEATURE_CFGPALETTE] = (char)(profilePalette + '0');
        fs[FEATURE_CFGFONT] = (char)(profileFont + '0');
        fs[FEATURE_BROWSER] = features[FEATURE_BROWSER]? '1':'0';
        fs[FEATURE_SERVER_SEARCH] = '0';
        fs[FEATURE_SHOWMEMORY] = (char)(featMemoryInfo + '0');
        fs[FEATURE_SUN] = (char)(featSun + '0');
        fs[FEATURE_ROUTING] = (char)(featRouting + '0');
        fs[FEATURE_ROADDIRS] = (char)(featRoadDirs + '0');

        return new String(fs);
    }

    private void writeJarEntry (JarOutputStream jos, String srcFile,
                                String destFile, int compression)
        throws IOException
    {
        ZipEntry ze = new ZipEntry (destFile);
        ze. setMethod (compression);

        srcFile = srcFile.replace('\\', '/');
        InputStream is = getInputStream(srcFile);
        if (is == null)
            return;

        byte[] bf = loadFromInputStream(is);
        int size = bf.length;

        if (compression == java.util.zip.ZipOutputStream.STORED)
        {
            ze.setSize (size);
            CRC32 c = new CRC32 ();
            c.update (bf);
            ze.setCrc (c.getValue());
        }

        jos.putNextEntry (ze);
        jos.write (bf);
        jos.closeEntry ();
    }

    private byte[] loadFromInputStream (InputStream in)
        throws IOException
    {
        byte[] result = null;

        if (in != null) {
            final int READ_AT_ONCE_LIMIT = 4096;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(READ_AT_ONCE_LIMIT);

            byte[] buf = new byte[READ_AT_ONCE_LIMIT];
            int howmany;
            while ((howmany = in.read(buf)) > 0)
            {
                bos.write(buf, 0, howmany);
            }
            in.close();

            result = bos.toByteArray();
        }
        return result;
    }

    private String getExportRoot ()
    {
        String sep = System.getProperty("file.separator");
        return System.getProperty ("user.dir") + sep + "export" + sep;
    }

    private void deleteFiles (File f)
    {
        File files[] = f.listFiles ();
        if (files != null)
            for (int i = 0; i < files.length; i++)
            {
                File c = files[i];

                if (c.isDirectory ())
                {
                    deleteFiles (c);
                    c.delete ();
                }
                else
                    c.delete ();
            }
    }

    private void copyFiles (String src, String dest)
    {
        File srcFile = new File (src);

        if (srcFile. isFile ())
        {
            try
            {
                FileInputStream fis = new FileInputStream (srcFile);
                byte bf[] = new byte[(int) srcFile.length ()];
                fis.read (bf);
                fis.close ();
                FileOutputStream fos = new FileOutputStream (dest);
                fos.write (bf);
                fos.close ();
            }
            catch (IOException ex)
            {
                System.out.println(ex);
            }
        }
        else if (srcFile. isDirectory())
        {
            String sep = System.getProperty("file.separator");
            new File (dest).mkdir ();
            String files [] = srcFile.list ();

            for (int i=0; i < files.length; i++)
            {
                String newSrc = src + sep + files [i];
                String newDest = dest + sep + files [i];

                copyFiles (newSrc, newDest);
            }
        }
    }

    private InputStream getInputStream (String fileName)
    {
        InputStream in = getClass().getResourceAsStream("/" + fileName);
        if (in == null)
        {
			try {
				in = new FileInputStream (fileName);
			}
			catch (FileNotFoundException ex) {
                System.err.println ("ERROR! File " + fileName + " isn't found");
			}
        }
        return in;
    }

}
