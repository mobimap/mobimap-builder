//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.export;

import java.io.*;
import java.util.*;
import java.util.List;

import java.awt.Point;

import com.ish.isrt.core.CityLoader;
import com.ish.isrt.core.GraphicStreamLoader;
import com.ish.isrt.core.Map;
import com.ish.isrt.core.data.*;
import com.ish.isrt.io.Serializator;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

public class Export2MobimapQuadra
    extends AbstractExport
{
    String exportDirData;
    Map map;
    CityLoader parent;

    private Archivator theArchivator;

    Properties ini;       // params of export

    int crossroadsN, elementsN, streetsN, routesN,
                photosN;
    int pcRealN, pcStopN, pcRouteN, pcStartN, pcFinishN;

    Crossroad[] crossroads;
    Element[] elements;
    Street[] streets;
    List<Address> addresses;
    POI[] pois;
    Route[] routes;
    Icon[] icons;
    Hashtable<Integer,Category> categories;

    String cityName, mapName;
    int cityId;
    int providerCode;
    int timezone;

    private int globalScale;

    public Export2MobimapQuadra ()
    {
    }

    public void make (CityLoader cityLoader, Properties ini)
    {
        this.parent = cityLoader;
        this.map = parent.theMap;
        this.ini = ini;

        String s = parent.getParameter ("mobimap.cityid");
        cityId = (s == null)? 0: Integer. valueOf (s). intValue ();
        mapName = parent.getParameter ("mobimap.name");
        s = parent.getParameter ("timezone");
            timezone = (s == null)? 0: Integer. valueOf (s). intValue ();
        s = parent.getParameter ("mobimap.scale");

        if (s == null) {
            globalScale = (int)Math.ceil(Math.log(Math.max(map.cityX, map.cityY)/32000.0f)/Math.log(2)) + 1;
            if (globalScale < 1)
                globalScale = 1;
            System.out.println ("Mobimap scale: " + globalScale);
        }
        else {
            globalScale = Integer.valueOf (s).intValue ();
        }

        String providerIdS = cityLoader.getParameter ("mobimap.provider");
        if (providerIdS != null && ini != null) {
            int providerId = Integer.parseInt (providerIdS);

            providerCode = Integer.valueOf ((String) ini.get ("provider." + providerId + ".code")).intValue ();
            if (providerCode == 0 || providerCode > 0x7fff) {
                System.err.println ("Illegal Provider Code: " + providerCode);
                return;
            }
        } else {
            providerCode = (new java.util.Random()).nextInt(0x7fff);
        }

        crossroadsN = map.crossroadsN;
        elementsN = map.elementsN;
        streetsN = map.streetsN;
        routesN = map.routesN;
        photosN = map.iconsN;
        pcRealN = map.pcRealN;
        pcStopN = map.pcStopN;
        pcRouteN = map.pcRouteN;

        crossroads = map.crossroads;
        elements = map.elements;
        streets = map.streets;
        routes = map.routes;
        categories = map.categories;
        icons = map.icons;

        cityName = map.cityName;

        // copy points
        pois = new POI[map.pois.size()];
        int i = 0;
        for (POI poi : map.pois)
            pois[i++] = poi;

        // copy addresses
        addresses = new ArrayList<Address>(map.addresses.size());
        addresses.addAll(map.addresses);

        s = parent.getParameter ("mobimap.optimize.addresses");
        boolean optAddrs = s == null? false: Boolean.parseBoolean(s);
        if (optAddrs)
            optimizeAddresses2 ();

        s = parent.getParameter ("mobimap.optimize.crossroads");
        boolean optCrs = s == null? false: Boolean.parseBoolean(s);

        if (optCrs)
        {
			optimizer ();
            hasRouting = false;
		}
        else
            hasRouting = true;

        optimizeOnlineBuildings ();

        prepareDirectory();

        worker ();
    }

    // internal part
    private void worker ()
    {
        try
        {
            FileOutputStream ostream;
            DataOutputStream out;

            // calculate element's lenghths
            int elLength [] = new int [elementsN];
            for (int i=0; i<elementsN; i++)
            {
                Element a = elements [i];
                int l = 0;
                for (int j=a.pcL+1; j<a.pcR-1; j++)
                {
                    if (crossroads [a.pc [j]] == null || crossroads [a. pc [j+1]] == null)
                        System.err.println("el=" + i + " pc1=" + a.pc[j] + " pc2=" + a.pc[j+1] + " street: " + streets [a.street]);
                    l += DistanceBetween (a.pc[j], a.pc[j + 1]);
                }

                l /= 60;
                elLength [i] = (l<1)? 1: ((l > 15)?15: l);
                //System.out.print (elLength [i] + " ");
            }

            // sort pc
            int sortpc [] = new int [pcRealN];
            int sortpcR [] = new int [pcRealN];

            int pcCount = sortCrossroads (crossroads, sortpc, sortpcR);

            // sort pc-con
            for (int i=0; i < pcRealN; i++)
                if (crossroads [i] != null)
                {
                    Crossroad a = crossroads [i];
                    for (int j=0; j < a.conN-1; j++)
                        for (int k=j+1; k < a.conN; k++)
                        {
                            if (a.con [j].pc < pcRealN && a.con [k].pc < pcRealN)
                            if (sortpc [a.con [j]. pc] > sortpc [a.con [k].pc])
                            {
                                ConBlock t = a.con [j];
                                a. con [j] = a.con [k];
                                a. con [k] = t;
                            }
                        }
                }

            // sort building
            List<Address> addressesList = new Vector<Address>(addresses.size());
            List<POI> pointsList = new Vector<POI>(pois.length);

            for (int i=0; i < pois.length; i++)
                pointsList.add(pois[i]);

            sortAddresses(addresses);
            addressesList.addAll(addresses);
            sortPoints(pointsList);


            /*****************************
             * WRITE PC
             *****************************/
            ostream = new FileOutputStream (exportDirData + file_pcx);
            out = new DataOutputStream (ostream);

            out. writeShort (globalScale * map.globalScale);

            // write 'x' array
            for (short i=0; i < pcCount; i++)
            {
                int x = global (crossroads [sortpcR [i]].x);
                if (x < 0 || x >= global (map.cityX))
                    System.err.println ("Crossroad [" + crossroads [sortpcR [i]].id + "].x = " + x + " is out of range!");

                x = x ^ providerCode;
                out. writeShort ((short) x);
            }
            // write transform matrix
            if (map.geo2meters != null)
            {
                out.writeShort (1);
                map.geo2meters.store (out, globalScale);
                hasCoordinates = true;
                out.writeShort (timezone);
            }
            else if (parent.getParameter(".transformX") != null && parent.getParameter(".transformY") != null)
            {
                double tx = Double.parseDouble(parent.getParameter(".transformX"));
                double ty = Double.parseDouble(parent.getParameter(".transformY"));

                out.writeShort (1);
                out.writeInt ((int) (-ty));
                out.writeInt ((int) (-tx));
                out.writeInt ((int) 100000 / globalScale);
                out.writeInt ((int) 100000 / globalScale);
                out.writeShort (0);
            }
            else
                out.writeShort (0);

            out. flush (); out. close(); ostream. close ();

            ostream = new FileOutputStream (exportDirData + file_pcy);
            out = new DataOutputStream (ostream);

            // write 'y' array
            short prevy = 0;
            for (short i=0; i < pcCount; i++)
            {
                short y = (short) global (crossroads [sortpcR [i]].y);
                out. writeShort ((y - prevy));
                prevy = y;

                if (y < 0 || y >= global (map.cityY))
                    System.err.println ("Crossroad [" + crossroads [sortpcR [i]].id + "].y = " + y + " is out of range!");
            }

            out. flush (); out. close(); ostream. close ();

            // file .con
            ostream = new FileOutputStream (exportDirData + file_conp);
            out = new DataOutputStream (ostream);

            short con [] = new short [pcRealN*4];
            short conel [] = new short [pcRealN*4];
            int conp = 0;
            byte conpi [] = new byte [pcRealN];

            for (short i=0, conbit=0; i<pcCount; i++)
            {
                Crossroad a = crossroads [sortpcR [i]];

                boolean f = false;
                byte cnt = 0, cnti = 0;
                for (short j = 0; j < a.conN; j++)
                {
                    boolean q = a.con[j].trans | a.con[j].type > 0;
                    int r = a.con[j].pc;
                    if (r >= pcRealN)
                        continue;
                    int conj = sortpc[r];
                    int diff = i - conj;

                    if (diff > 0x2000)
                        System.err.println("Difference b/w pcs exceeds limit in 0x2000. pc: " + sortpcR [i]);

                    if (diff > 0)
                    {
                        if (a.con[j].direction > 0) hasRoadDirs = true;
                        con[conp] = (short) (diff | (q ? 0x8000 : 0) | ((a.con[j].direction & 0x3) << 13));
                        int el = getElementByStreet (a.con[j].street, sortpcR[i]);
                        conel[conp] = (short) ((elLength[el] << 12));
                        conp++;
                        f = true;
                        cnt++;
                    }
                    else
                    {
                        cnti++;
                    }
                }
                out.writeByte (cnt);
                conpi[i] = cnti;
            }
            out. flush (); out. close(); ostream. close ();

            ostream = new FileOutputStream (exportDirData + file_conpi);
            out = new DataOutputStream (ostream);

            for (short j=0; j < pcCount; j++)
                out. writeByte (conpi [j]);

            out. flush (); out. close(); ostream. close ();

            ostream = new FileOutputStream (exportDirData + file_conn);
            out = new DataOutputStream (ostream);

            out. writeShort (conp);

            for (short j=0; j < conp; j++)
                out. writeShort (con[j]);

            out. flush (); out. close(); ostream. close ();

            // ***************
            // WRITE ELEMENTS

            // write names array
            int streetCount = streetsN;

            int ptr = 0;
            int namesNmax = 0;

            ostream = new FileOutputStream (exportDirData + file_stname);
            out = new DataOutputStream (ostream);
            char names [] = new char [elementsN * 40];
            int namesN = 0;

            for (int st=0; st < streetCount; st++)
            {
                if (streets[st] != null)
                {
                    String name = getStreetNameMM (streets[st].getFullName());
                    name.getChars (0, name.length (), names, namesN);
                    out.writeByte ((byte) name.length ());
                    namesN += name.length ();
                }
                else
                    out.writeByte (0);

                if (namesN > namesNmax) namesNmax = namesN;
            }
            //out. writeShort (namesN);
            out. writeInt (namesN);

            for (int j=0; j < namesN; j++)
                out. writeShort (encodeChar (names [j]));

            out. flush (); out. close(); ostream. close ();


            // write pcs array
            ostream = new FileOutputStream (exportDirData + file_elcon);
            out = new DataOutputStream (ostream);

            conp = 0;
            for (short i=0, conbit=0; i<elementsN; i++, conbit ^= (1 << 15))
            {
                Element a = elements [i];
                short cnt=0;
                for (int j=a.pcL+1; j<a.pcR; j++)
                {
                    int connow = 0, cur = a.pc [j];
                    if (cnt == 0) connow = sortpc [a.pc [j]];
                    else
                    {
                        int prev = a.pc [j-1];

                        Crossroad ap = crossroads [prev];
                        int n1 = 0, n2 = 0;

                        for (short k=0; k < ap.conN; k++)
                        {
                            int r = ap.con [k].pc;
                            if (r >= pcRealN) continue;
                            int conj = sortpc [r];

                            if (sortpc [prev] > conj)
                            {
                                if (n1 > 0x7) System.err.println ("Con overflow: pc " + r + " " + crossroads [r].toString());
                                if (cur == r) connow = n1;
                                n1++;
                            }
                            else
                            {
                                if (n2 > 0x7) System.err.println ("Con overflow: pc " + r + " " + crossroads [r].toString());
                                if (cur == r) connow = n2 | 0x8;
                                n2++;
                            }
                        }
                    }
                    con [conp++] = (short) (connow | conbit);
                    cnt++;
                }
            }
            con [conp] = (short)(~con [conp-1]);
            conp ++;
            out. writeShort (conp);

            for (short j=0; j < conp; j++)
                out. writeShort (con[j]);

            out. flush (); out. close(); ostream. close ();

            // write length array
            ostream = new FileOutputStream (exportDirData + file_ellen);
            out = new DataOutputStream (ostream);

            int st = 0;
            for (int i=0; i < elementsN; i++)
            {
                int a = elements [i]. street;
                out. writeByte ((byte) ((elLength [i] & 0xf) | ((a==st)?0x10: 0)));
                st = a;
            }
            out. flush (); out. close(); ostream. close ();


            // *****************
            // WRITE ADDRESSES

            int addressesCount = addressesList.size();
            if (addressesCount > 0)
            {
                hasAddresses = true;

                ostream = new FileOutputStream (exportDirData + file_blx);
                out = new DataOutputStream (ostream);

                // write 'x' array
                int prevr = -1, prx = 0;
                for (int i=0; i < addressesCount; i++)
                {
                    Address a = addressesList.get(i);
                    int ou = 0, r = a.getStreetId();
                    if (r != prevr) ou = global (a.x);
                    else ou = (global (a.x) - prx) + 0x100;

                    out. writeShort ((short) ou);
                    prx = global (a.x); prevr = r;

                }
                out. flush (); out. close(); ostream. close ();

                ostream = new FileOutputStream (exportDirData + file_bly);
                out = new DataOutputStream (ostream);

                // write 'y' array
                prevr = -1;
                int pry = 0;
                for (short i=0; i<addressesCount; i++)
                {
                    Address a = addressesList.get(i);
                    int ou = 0, r = a.getStreetId();
                    if (r != prevr) ou = global (a.y);
                    else ou = (global (a.y) - pry) + 0x100;

                    out. writeShort ((short) ou);
                    pry = global (a.y); prevr = r;
                }
                out. flush (); out. close(); ostream. close ();

                // write 'street' array
                ostream = new FileOutputStream (exportDirData + file_blst);
                out = new DataOutputStream (ostream);

                short prv = 0;
                for (short i=0; i<addressesCount; i++)
                {
                    Address a = addressesList.get(i);
                    short r = (short) (a.getStreetId());
                    out. writeShort (r - prv);
                    prv = r;
                }

                out. flush (); out. close(); ostream. close ();

                // write 'names' array
                ostream = new FileOutputStream (exportDirData + file_blname);
                out = new DataOutputStream (ostream);

                out.writeInt (addressesCount);
                StringBuffer namesStorage = new StringBuffer(1000);

                for (short i=0; i < addressesCount; i++)
                {
                    Building a = addressesList.get(i);
                    String s = a.getName();

                    boolean isNumeric = false;
                    try
                    {
                        int m = Integer.parseInt (s);
                        if (m > 0 && m < 230)
                        {
                            isNumeric = true;
                            out.writeByte (m);
                        }
                    }
                    catch (NumberFormatException z)
                    {
                    }

                    if (!isNumeric)
                    {
                        short ln = (short) s.length ();
                        if (ln > 20)
                            ln = 20;

                        out.writeByte (230 + ln);
                        namesStorage.append(s.substring(0, ln));
                    }
                }

                char[] ns = namesStorage.toString().toCharArray();
                for (int i = 0, j = 0; i < ns.length; i++)
                {
                    out.writeChar (ns[i]);
                }

                System.out.println ("nameN: " + namesStorage.length());

                out. flush (); out. close(); ostream. close ();
            }

            // ******************************
            // LABELS & CATEGORIES
            // write category information

            exportCategoriesQuadra();

            exportPoiInfo();

            //
            // WRITE LABELS AKA OBJECTS
            //
            int[] lbRef = exportPoints();


            // *********************
            // IMAGES
            //
            ByteArrayOutputStream baos = new ByteArrayOutputStream (1000);
            out = new DataOutputStream (baos);

            int photoCount = 0, photoCtN = 0, photoOjN = 0;

            for (int i=0; i < icons.length; i++)
            {
                Icon one = icons [i];
                if (one != null)
                {
                    one.loadIconData();
                    byte[] iconData = one.getIconData();
                    if (iconData == null) {
                        System.err.println ("Error loading image file " + one.getFileName());
						continue;
					}

                    int placesN = one.getPlaces().length;
                    int [] newrefs = new int [placesN];
                    int newrefsN = 0;

                    for (int k=0; k < placesN; k++)
                    {
                        Reference ref = one.getPlaces()[k];
                        int refcl = ref.classid;
                        int refit = ref.objectid;
                        int newref = -1;

                        if (refcl == DataType.CLASS_POI)
                        {
                            int a = lbRef[refit];
                            if (a >= 0)
                            {
                                newref = 0x4000000 | a;
                                photoOjN++;
                            }
                        }
                        if (newref > 0)
                        {
                            newrefs [newrefsN++] = newref;
                        }
                    }
                    if (newrefsN > 0)
                    {
                        out.writeInt (iconData.length);
                        out.write (iconData);

                        out.writeShort (newrefsN);
                        for (int j=0; j < newrefsN; j++)
                        {
                            out.writeInt (newrefs[j]);
                        }

                        photoCount++;
                    }
                }
            }
            out.close ();

            if (photoCount > 0)
            {
                ostream = new FileOutputStream (exportDirData + file_img);
                out = new DataOutputStream (ostream);
                out.writeShort (photoCount);
                out.write (baos.toByteArray ());
                out.close ();
            }


            // *********************
            // CITY
            // write city variables
            ostream = new FileOutputStream (exportDirData + file_city);
            baos = new ByteArrayOutputStream (1000);
            out = new DataOutputStream (baos);

            out. writeUTF (cityName);

            int keyA = 5507;
            int keyB = 187;

            long t = System.currentTimeMillis();
            int fileTime = (int)(t / 1000);

            out. writeShort (14);
            out. writeInt (fileTime);
            out. writeShort (cityId);

            int userIdHigh = (int)((System.currentTimeMillis() >> 4) & 0x7fff), userIdLow = 0xB242;
            out. writeShort (userIdHigh);
            out. writeShort (userIdLow);

            final int paramN = 13;
            out.writeShort (paramN);

            pcExport = pcCount; elementExport = elementsN; streetExport = streetCount;
            namesNmaxExport = namesNmax;
            cityX = global(map.cityX); cityY = global(map.cityY);

            String ox = parent.getParameter("mobimap.default.originx");
            if (ox != null)
                originX = (int)Double.parseDouble(ox);

            if (ox == null || originX < 0 || originX > cityX)
                originX = global(map.origin.x);

            String oy = parent.getParameter("mobimap.default.originy");
            if (oy != null)
                originY = (int)Double.parseDouble(oy);

            if (oy == null || originY < 0 || originY > cityY)
                originY = global(map.origin.y);

            String sl = parent.getParameter("mobimap.default.scale");
            if (sl != null)
                scale = Integer.parseInt(sl);
            else
                scale = (int)(map.scale * globalScale);

            Random rnd = new Random ();
                int params [] = new int [paramN+1];
                params [0] = pcCount;
                params [1] = streetCount;
                params [2] = elementsN;
                params [3] = namesNmax;
                params [4] = providerCode;
                params [5] = cityX;
                params [6] = cityY;
                params [7] = 0;
                params [8] = 0;
                params [9] = 0;
                params [10] = originX;
                params [11] = originY;
                params [12] = scale;
                params [13] = 12345678;

                for (int i=0; i < 14; i++)
                    out. writeInt (params [i]);

            String report =
                "\ncrCount: " + pcCount + "\nstreetCount: " + (streetCount - 1) +
                "\nelementN: " + elementsN +
                "\nnamesNmax: " + namesNmax + "\naddress: " + addressesCount +
                "\ncityX: " + cityX + "\ncityY: " + cityY +
                "\noriginx: " + originX +
                "\noriginy: " + originY + "\nscale: " + scale;
            System.out.println (report);

            Properties license = new Properties();
            int encodingKey = loadLicense(license);
            hardKey = encodingKey;

			out.writeShort (2);
			out.writeUTF (license.getProperty ("name"));
			out.writeUTF (license.getProperty ("number"));

            int dataLength = baos.size ();

            for (int i=0; i < 994 - dataLength; i++)
                out.writeByte (rnd.nextInt () & 0xff);

            out. close();

            // encode all data...
            byte [] arr = baos.toByteArray();
            int arrN = arr.length;
            byte [] enc = new byte [arrN];

            for (int i=arrN-1; i>=0; i--)
            {
                enc [i] = (byte) (arr [i] ^ encodingKey);
                encodingKey = random (arr [i]);
            }

            // write to file
            out = new DataOutputStream (ostream);
            out.writeShort (994);
            out.writeShort (keyA);
            out.writeShort (keyB);
            out.write (enc);
            out. close ();

            exportAllGS ();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println (e);
        }
        //System. out. println ("ME export is done.");
    }

    public static void calcHardkeyS()
    {
        Export2MobimapQuadra emq = new Export2MobimapQuadra();
        Properties license = new Properties();
        int encodingKey = emq.loadLicense(license);

        System.out.println ("");
        System.out.println ("License: " + license);
        System.out.println ("Hardkey: " + encodingKey);
    }

    private int random (int r)
    {
        return (int) ((r * 5507L + 187L) & 0x7fffffff);
    }
    int getElementByStreet (int st, int p)
    {
        if (st > 0)
        {
            int jM = streets [st]. elementN;
            for (int j=0; j < jM; j++)
            {
                int cel = ((Integer) streets [st]. element. elementAt (j)). intValue ();
                    for (int k = elements [cel]. pcL+1; k < elements [cel]. pcR; k++)
                    if (elements [cel].pc [k] == p) return cel;
            }
        }
        return 0;
    }
    private int DistanceBetween (int a, int b)
    {
        int r1 = crossroads [a]. x - crossroads [b]. x, r2 = crossroads [a]. y - crossroads [b]. y;
        return (int) Math.sqrt (r1*r1 + r2*r2);
    }
    private double hypot (int a, int b)
    {
        return Math.sqrt (a*a+b*b);
    }
    private int global (int n)
    {
        return n / globalScale;
    }
    /**
     * Remove street type from street name
     * @param name String
     * @return String
     */
    static public String getStreetNameMM (String name)
    {
        char namec [] = name. toCharArray ();

        final String[] strips = {" ул", " str", " utca"};

        for (int i=0; i < strips.length; i++)
        {
            int idx = name.indexOf (strips[i]);
            if (idx > 0)
            {
                char[] chars = strips[i].toCharArray();
                for (int j=0; j < chars.length; j++)
                    namec[idx + j] = ' ';
            }
        }

        int idx = name. indexOf ('.');
        if (idx > 0) namec [idx] = ' ';

        idx = name. indexOf ('(');
        if (idx > 0) name = new String (namec, 0, idx);
        else name = new String (namec);

        name = name. trim ();
        return name;
    }
    private short encodeChar (char ch)
    {
        return (short) (ch ^ providerCode);
    }

    /**
     * Sort crossroads by Y-axis.
     * @param crossroads Crossroad[] array of crossroads
     * @param sortpc int[] old-to-new
     * @param sortpcR int[] new-to-old
     * @return int
     */
    private int sortCrossroads (Crossroad[] crossroads, int[] sortpc, int[] sortpcR)
    {
        int n = crossroads.length;
        Vector<Crossroad> list = new Vector<Crossroad>(n);

        for (int i = 0, m = 1; i < n; i++)
            if (crossroads[i] != null)
                list.add (crossroads[i]);

        Collections.sort(list, new Comparator<Crossroad>() {
            public int compare (Crossroad o1, Crossroad o2)
            {
                return o1.y - o2.y;
            }
        });

        int m = list.size();
        for (int i=0; i < m; i++)
        {
            Crossroad c = list.get(i);
            sortpcR[i] = c.id;
            sortpc[c.id] = i;
        }

        return m;
    }

    /**
     * Sort addresses
     * @param addresses List
     */
    private void sortAddresses (List<Address> addresses)
    {
        Collections.sort(addresses, new Comparator<Address>() {
            public int compare (Address o1, Address o2)
            {
                int res = o1.getStreetId() - o2.getStreetId();
                if (res == 0)
                {
                    int n1 = parseNumber(o1.getName());
                    int n2 = parseNumber(o2.getName());
                    res = n1 - n2;
                }
                return res;
            }
        });
    }
    /**
     * Sort points
     * @param points List
     */
    private void sortPoints (List<POI> points)
    {
        Collections.sort(points, new Comparator<Building>() {
            public int compare (Building o1, Building o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
    private int parseNumber(String s)
    {
        int length = s.length();
        int n = 0;
        for (int j=0; j < length; j++)
        {
            int a = s.charAt(j) - '0';
            if (a >= 0 && a <= 9) n = n * 10 + a;
            else break;
        }
        return n;
    }

    /**
     * Export categories for Quadra
     */
    private void exportCategoriesQuadra()
    {
        List<Category> categoriesList = new Vector<Category>();

        for (Category cat : categories.values())
        {
            if (!cat.isOnline() && cat.getId() >= Category.CATEGORY_FIRST_AVAILABLE)
                categoriesList.add (cat);
        }

        try
        {
            OutputStream ostream = new FileOutputStream (exportDirData + file_ct4);
            DataOutputStream out = new DataOutputStream (ostream);

            CategoryPacket categoryPacket = new CategoryPacket(categoriesList);
            Envelope envelope = categoryPacket.serialize();
            Serializator.writeObject(out, envelope.serialize());

            out.close();
        }
        catch (IOException ex)
        {
        }
    }

    private void exportPoiInfo()
    {
        List data = new Vector();
        for (POI poi: pois)
        {
            Properties meta = poi.getMeta();
            if (meta == null) continue;

            int uid = poi.getUid();

            Hashtable<Object,Object> m = new Hashtable<Object,Object>();
            m.putAll(meta);
            m.put('I', uid);

            String page = meta.getProperty("page");
            if (page == null)
                continue;

            m.remove("page");
            m.put('P', page);

            data.add(m);
        }

        if (data.size() == 0) {
            return;
        }

        try
        {
            OutputStream ostream = new FileOutputStream (exportDirData + file_info);
            DataOutputStream out = new DataOutputStream (ostream);

            Serializator.writeObject(out, data);

            out.close();
        }
        catch (IOException ex)
        {
        }
    }

    /**
     * Export points
     * @return int[]
     */
    private int[] exportPoints()
    {
        int n = pois.length;
        int lbRef[] = new int[n];
        for (int i = 0; i < n; i++)
        {
            lbRef[i] = -1;
        }

        try
        {
            OutputStream ostream = new FileOutputStream (exportDirData + file_lb);
            DataOutputStream out = new DataOutputStream (ostream);

            short[] lbx = new short[n];
            short[] lby = new short[n];
            short[] lbct = new short[n];
            byte[] lbnamep = new byte[n];
            int[] lbuid = new int[n];

            int labelCount = 0;

            StringBuffer namesStorage = new StringBuffer(1000);

            for (int i = 0; i < n; i++)
            {
                if (pois[i] != null)
                {
                    POI b = pois[i];

                    {
                        lbx[labelCount] = (short) (b.x / globalScale);
                        lby[labelCount] = (short) (b.y / globalScale);
                        int ct = b.getCategoryId();

                        lbct[labelCount] = (short) ct;

                        String name = b.name;
                        int nameLength = name.length();
                        if (nameLength == 0)
                        {
                            name = " ";
                            nameLength = 1;
                        }
                        else if (name.charAt (0) == '@')
                        {
                            int sp = name.indexOf (' ') + 1;
                            name = name.substring (sp);
                            nameLength = name.length();
                        }

                        if (nameLength > 127) // cut down
                        {
                            name = name.substring(0, 127);
                            nameLength = 127;
                        }
                        lbnamep[labelCount] = (byte)nameLength;

                        namesStorage.append(name);

                        lbuid[labelCount] = b.uid;
                        lbRef[i] = labelCount;

//                        System.out.println("i: " + i + " name: " + b.name + " lbRef [i]: " + labelCount);

                        labelCount++;
                    }
                }
            }
            hasLabels = labelCount > 1;

            // count : int
            out.writeInt (labelCount);
            // x : short[]
            for (int i = 0; i < labelCount; i++)
            {
                out.writeShort (lbx[i]);
            }
            // y : short[]
            for (int i = 0; i < labelCount; i++)
            {
                out.writeShort (lby[i]);
            }
            // categoryId : short[]
            for (int i = 0; i < labelCount; i++)
            {
                out.writeShort (lbct[i]);
            }
            // uid : int[]
            for (int i = 0; i < labelCount; i++)
            {
                out.writeInt (lbuid[i]);
            }
            // name_lemgth : byte[]
            for (int i = 0; i < labelCount; i++)
            {
                out.writeByte (lbnamep[i]);
            }
            // names : char[]
            char[] ns = namesStorage.toString().toCharArray();
            for (int i = 0, j = 0; i < ns.length; i++)
            {
                out.writeChar (ns[i]);
            }

            out.flush ();
            out.close ();
            ostream.close ();
        }
        catch (FileNotFoundException ex)
        {
        }
        catch (IOException ex)
        {
        }
        return lbRef;
    }


    ///////////////////////////////////////////////////////////////////////////
    //                        O P T I M I Z E R
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Optimize crossroad net.
     * Removes all crossroad on straight streets that don't change net geometry.
     */
    private void optimizer ()
    {
        try
        {
            int delpc = 0, delcon = 0;

            for (int i = 0; i < elementsN; i++)
            {
                Element a = elements[i];

                int n = a.pcR - a.pcL - 1;
                if (n == 2) continue;

                Point curve[] = new Point [n];
                int curvePc [] = new int [n];
                int elpc [] = new int [n+2];
                int elpcN = 1;

                boolean quality = false;

                boolean pcqualityOr = false, pcdirection = false;
                boolean pcqualityAnd = true;
                for (int j = a.pcL + 1; j < a.pcR-1; j++)
                {
                    int cur = a.pc [j], next = a.pc [j+1];
                    Crossroad r = crossroads [cur];  // forward
                    for (int h=0; h < r.conN; h++)
                        if (r.con [h].pc == next)
                        {
                            pcqualityOr |= r.con[h].trans;
                            pcqualityOr |= r.con[h].type > 0;
                            pcdirection |= r.con[h].direction > 0;
                            pcqualityAnd &= r.con[h].trans || r.con[h].type > 0;
                        }

                    r = crossroads [next];   // backward
                    for (int h=0; h < r.conN; h++)
                        if (r.con [h].pc == next)
                        {
                            pcqualityOr |= r.con[h].trans;
                            pcqualityOr |= r.con[h].type > 0;
                            pcdirection |= r.con[h].direction > 0;
                            pcqualityAnd |= r.con[h].trans || r.con[h].type > 0;
                        }
                }
//                if ((!quality && pcqualityOr) || pcdirection) continue;
                if (pcdirection || (pcqualityOr != pcqualityAnd)) continue;

                quality = pcqualityOr;

                for (int j = a.pcL + 1, k = 0; j < a.pcR; j++, k++)
                {
                    curve [k] = new Point (crossroads[a.pc[j]].x, crossroads[a.pc[j]].y);
                    curvePc [k] = a.pc [j];
                }

                int tol = 13;
                Point opt[] = new Point[n];
                boolean mk [] = new boolean [n];
                int m = poly_simplify (tol, curve, n, opt, mk);

                // delete all connections
                for (int j=0; j < n-1; j++)
                {
                    delCon (curvePc [j], curvePc [j+1], a.street);
                    delcon ++;
                }

                // add required connections
                int prev = curvePc [0];
                elpc [elpcN++] = prev;
                for (int j=1; j < n; j++)
                {
                    if (mk [j] || DistanceBetween (prev, curvePc [j]) > 1000)
                    {
                        addCon (prev, curvePc[j], a.street, quality);
                        elpc [elpcN++] = curvePc [j];
                        prev = curvePc[j];
                        delcon --;
                    }
                }
                elements [i].pc = elpc;
                elements [i].pcL = 0;
                elements [i].pcR = elpcN;
            }

            for (int i=0; i < pcRealN; i++)
            {
                Crossroad a = crossroads [i];
                if (a == null) continue;

                ConBlock conew [] = new ConBlock [a.conN];
                int conewN = 0;

                for (int j=0; j < a.conN; j++)
                {
                    if (a.con [j].pc != 0)
                    {
                        conew [conewN] = new ConBlock (a.con[j].pc, a.con[j].street, 0);
                        conew [conewN]. trans = a.con [j].trans;
                        conew [conewN]. weight = a.con [j].weight;
                        conew [conewN]. transport = a.con [j].transport;
                        conew [conewN]. direction = a.con [j].direction;
                        conew [conewN]. type = a.con [j].type;
                        conewN++;
                        delcon --;
                    }
                }
                if (conewN > 0)
                {
                    crossroads [i].con = conew;
                    crossroads [i]. conN = conewN;
                }
                else
                {

                    crossroads [i] = null;
                    delpc ++;
                }
            }
        }
        catch (Exception z)
        {
            z.printStackTrace();
        }
    }
    private void delCon (int i1, int i2, int street)
    {
        Crossroad pc1 = crossroads [i1];
        Crossroad pc2 = crossroads [i2];

        int f = 0;

        for (int i=0; i < pc1.conN; i++)
        {
            ConBlock con = pc1.con[i];
            if (con.pc == i2 && con.street == street)
            {
                con.pc = 0;
                con.street = 0;
                con.trans = false;
                f++;
                break;
            }
        }
        for (int i=0; i < pc2.conN; i++)
        {
            ConBlock con = pc2.con[i];
            if (con.pc == i1 && con.street == street)
            {
                con.pc = 0;
                con.street = 0;
                con.trans = false;
                f++;
                break;
            }
        }
        if (f < 2)
            System.err.println ("-  f=" + f + " i1=" + i1 + "(" + pc1.x + ", " + pc1.y + ") i2=" + i2);
    }
    private void addCon (int i1, int i2, int st, boolean quality)
    {
        Crossroad pc1 = crossroads [i1];
        Crossroad pc2 = crossroads [i2];

        int w = 0;
        for (int i=0; i < pc1.conN; i++)
            if (pc1.con [i]. pc == 0)
            {
                pc1. con [i]. pc = i2;
                pc1. con [i]. street = st;
                pc1. con [i]. trans = quality;
                w++;
                break;
            }

        for (int i=0; i < pc2.conN; i++)
            if (pc2.con [i]. pc == 0)
            {
                pc2. con [i]. pc = i1;
                pc2. con [i]. street = st;
                pc2. con [i]. trans = quality;
                w++;
                break;
            }
        if (w < 2)
            System.err.println ("+  w=" + w + " i1=" + i1 + " i2=" + i2);
    }

    private double dot (Point u, Point v)
    {
        return u.x * v.x + u.y * v.y;
    }
    private double norm2 (Point v)
    {
        return dot (v, v);
    }
    private double norm (Point v)
    {
        return Math.sqrt (norm2 (v));
    }
    private double d2 (Point u, Point v)
    {
        return (u.x - v.x) * (u.x - v.x) + (u.y - v.y) * (u.y - v.y);
    }
    private double d (Point u, Point v)
    {
        return Math.sqrt (d2 (u, v));
    }

    class Segment
    {
        public Point P0, P1;
    }

// poly_simplify():
//    Input:  tol = approximation tolerance
//            V[] = polyline array of vertex points
//            n   = the number of points in V[]
//    Output: sV[]= simplified polyline vertices (max is n)
//    Return: m   = the number of points in sV[]
    int poly_simplify (float tol, Point V[], int n, Point sV[], boolean mk [])
    {
        int i, k, m, pv; // misc counters
        float tol2 = tol * tol; // tolerance squared
        Point vt[] = new Point[n]; // vertex buffer
        //boolean mk[] = new boolean [n]; // marker buffer
        mk[0] = false;

        // STAGE 1.  Vertex Reduction within tolerance of prior vertex cluster
        //vt[0] = V[0]; // start at the beginning
        //for (i = k = 1, pv = 0; i < n; i++)
        //{
            //if (d2 (V[i], V[pv]) < tol2)
            //	continue;
        //	vt[k++] = V[i];
            //pv = i;
        //}
        //if (pv < n - 1)
        //	vt[k++] = V[n - 1]; // finish at the end

            // STAGE 2.  Douglas-Peucker polyline simplification
        mk[0] = mk[n - 1] = true; // mark the first and last vertices
        simplifyDP (tol, V, 0, n - 1, mk);

        // copy marked vertices to the output simplified polyline
        for (i = m = 0; i < n; i++)
        {
            if (mk[i])
                sV[m++] = V[i];
        }
        return m; // m vertices in simplified polyline
    }

// simplifyDP():
//  This is the Douglas-Peucker recursive simplification routine
//  It just marks vertices that are part of the simplified polyline
//  for approximating the polyline subchain v[j] to v[k].
//    Input:  tol = approximation tolerance
//            v[] = polyline array of vertex points
//            j,k = indices for the subchain v[j] to v[k]
//    Output: mk[] = array of markers matching vertex array v[]
    void simplifyDP (double tol, Point v[], int j, int k, boolean mk[])
    {
        if (k <= j + 1) // there is nothing to simplify
            return;

        // check for adequate approximation by segment S from v[j] to v[k]
        int maxi = j; // index of vertex farthest from S
        double maxd2 = 0; // distance squared of farthest vertex
        double tol2 = tol * tol; // tolerance squared
        Segment S = new Segment ();
        S.P0 = v[j];
        S.P1 = v[k];
        Point u = new Point (S.P1.x - S.P0.x, S.P1.y - S.P0.y); // segment direction vector
        double cu = dot (u, u); // segment length squared

        // test each vertex v[i] for max distance from S
        // compute using the Feb 2001 Algorithm's dist_Point_to_Segment()
        // Note: this works in any dimension (2D, 3D, ...)
        Point w;
        Point Pb; // base of perpendicular from v[i] to S
        double b, cw, dv2; // dv2 = distance v[i] to S squared

        for (int i = j + 1; i < k; i++)
        {
            // compute distance squared
            w = new Point (v[i].x - S.P0.x, v[i].y - S.P0.y);
            cw = dot (w, u);
            if (cw <= 0)
                dv2 = d2 (v[i], S.P0);
            else if (cu <= cw)
                dv2 = d2 (v[i], S.P1);
            else
            {
                b = cw / cu;
                Pb = new Point ((int) (S.P0.x + b * u.x),
                                (int) (S.P0.y + b * u.y));
                dv2 = d2 (v[i], Pb);
            }
            // test with current max distance squared
            if (dv2 <= maxd2)
                continue;
            // v[i] is a new max vertex
            maxi = i;
            maxd2 = dv2;
        }
        if (maxd2 > tol2) // error is worse than the tolerance
        {
            // split the polyline at the farthest vertex from S
            mk[maxi] = true; // mark v[maxi] for the simplified polyline
            // recursively simplify the two subpolylines at v[maxi]
            simplifyDP (tol, v, j, maxi, mk); // polyline v[j] to v[maxi]
            simplifyDP (tol, v, maxi, k, mk); // polyline v[maxi] to v[k]
        }
        // else the approximation is OK, so ignore intermediate vertices
        return;
    }

    // Optimize addresses
    private void optimizeAddresses2 ()
    {
        boolean[] bf = new boolean[addresses.size()];

        for (int i = 0; i < streetsN; i++)
        {
            Street street = streets[i];
            if (street == null) continue;

            Vector<Crossroad> elPc = new Vector<Crossroad>();

            // find list of crossroads for current street
            for (int j = 0; j < street.element.size(); j++)
            {
                int elId = ((Integer)street.element.get(j)).intValue();
                Element el = elements[elId];

                for (int k = el.pcL + 1; k < el.pcR; k++)
                {
                    int pc = el.pc[k];
                    elPc.add(crossroads[pc]);
                }
            }

            int elPcN = elPc.size();

            Hashtable<Crossroad,Vector<BuildingDistancePair>> even =
                new Hashtable<Crossroad,Vector<BuildingDistancePair>>();

            Hashtable<Crossroad,Vector<BuildingDistancePair>> odd =
                new Hashtable<Crossroad,Vector<BuildingDistancePair>>();

            // for every address find closest crossroad
            for (int j = 0; j < addresses.size(); j++)
            {
                Address b = addresses.get(j);
                if (b == null) continue;
                if (b.getStreetId() == i)
                {
                    Integer number = null;
                    try
                    {
                        number = Integer.parseInt (b.name);
                    }
                    catch (NumberFormatException ex)
                    {
                        continue;
                    }
                    boolean isEven = number.intValue() % 2 == 0;

                    double dist = Integer.MAX_VALUE;
                    Crossroad closestPc = null;

                    for (int k = 0; k < elPcN; k++)
                    {
                        Crossroad pc = elPc.get(k);
                        double ds = d2(new Point(pc.x, pc.y), new Point(b.x, b.y));
                        if (ds < dist)
                        {
                            dist = ds;
                            closestPc = pc;
                        }
                    }

                    Hashtable<Crossroad,Vector<BuildingDistancePair>> hash =
                        isEven? even: odd;

                    Vector<BuildingDistancePair> list = hash.get(closestPc);
                    if (list == null) list = new Vector<BuildingDistancePair>();
                    list.add(new BuildingDistancePair(b, dist));
                    hash.put(closestPc, list);
                }
            }

            // for every crossroad find upto 2 closest addresses
            filterClosestAddresses(bf, even);
            filterClosestAddresses(bf, odd);
        }

        // leave only addresses with set flag
        List<Address> newAddresses = new Vector<Address>();
        for (int j = 0; j < addresses.size(); j++)
        {
            Address b = addresses.get(j);
            if (b == null) continue;
            if (bf[j])
                newAddresses.add(b);
        }
        addresses.clear();
        addresses.addAll(newAddresses);
    }

    private void filterClosestAddresses (boolean[] bf, Hashtable<Crossroad,
                                         Vector<BuildingDistancePair>> even)
    {
        for (Enumeration<Crossroad> en = even.keys(); en.hasMoreElements(); )
        {
            Crossroad key = en.nextElement();
            Vector<BuildingDistancePair> list = even.get(key);

            Collections.sort (list);

            BuildingDistancePair bdp = list.get(0);
            bf[bdp.building.id] = true;
        }
    }

    class BuildingDistancePair
        implements Comparable<BuildingDistancePair>
    {
        public Building building;
        public double distance;

        public BuildingDistancePair (Building building, double distance)
        {
            this.building = building;
            this.distance = distance;
        }

        public int compareTo (BuildingDistancePair a)
        {
            return (int)(distance - a.distance);
        }
    }

    private void optimizeOnlineBuildings ()
    {
        for (int i=0; i < pois.length; i++)
        {
            POI bl = pois [i];
            if (bl != null)
            {
                int ref = bl.getCategoryId ();
                if (categories.get (ref).isOnline ())
                {
                    //System.out.println(bl.name);
                    pois[i] = null;
                }
            }
        }

    }

    private void exportAllGS ()
    {
//        ExportGraphics exportGraphics = new ExportGraphics(parent.getMobimapConfiguration(), exportDirData, globalScale);
//        exportGraphics.make();

        //exportGsName ("gs", file_gs, dialog.getOptimizeGS () ? 35 : 0, 1);
        //exportGsName ("gsi", file_gsi, 200, 1);

        exportGsName ("graphic", file_gs3, 0, 2);
        exportGsName ("minimap", file_gsi3, 200, 2);
    }

    private void exportGsName (String gsName, String gsFile, int optimizeGSvalue, int level)
    {
        GraphicStreamLoader gsl = new GraphicStreamLoader ();

        String s = parent.getParameter ("mobimap." + gsName);
        gsl.loadStream (s);
        int[] data = gsl.getStream (s);

        try
        {
            exportGSinternal (level, data, gsFile, optimizeGSvalue);
            hasGS = true;
        }
        catch (IOException ex)
        {
        }
    }
    private void exportGSinternal (int level, int data [], String gsName, int optimizeGS) throws IOException
    {
        ByteArrayOutputStream baout = new ByteArrayOutputStream ();
        DataOutputStream out = new DataOutputStream (baout);

//        final int gspLimit = 20000;
//        int gsp [] = new int [gspLimit];
//        int gspN = 0;
        List<Integer> gsp = new ArrayList<Integer>(10000);

        final int GS_EOF = 0;
        final int GS_MOVETO = 1;
        final int GS_LINETO = 2;
        final int GS_CURVETO = 3;
        final int GS_STROKECOLOR = 4;
        final int GS_FILLCOLOR = 5;
        final int GS_SETLINEWIDTH = 6;
        final int GS_STROKE = 7;
        final int GS_CLOSESTROKE = 8;
        final int GS_FILL = 9;

        int x=0, y=0, r=0, g=0, b=0, rf=0xff, gf=0xff, bf=0xff;
        int xMem=0, yMem=0;

        final int segLimit = 1000;
        int segx [] = new int [segLimit+1];
        int segy [] = new int [segLimit+1];
        int segN = 0;
        int opts = 0, optcrv = 0;

        int i = 0;
        while (data [i] != GS_EOF && i < data.length-6)
        {
            int command = data [i], commandnext = data [i+1];
            switch (command)
            {
                case GS_MOVETO:
                    segN = 0;
                    x = data[++i]; y = data[++i];
                    x += xMem; y += yMem;
                    xMem = x; yMem = y;
                    segx[segN] = x;
                    segy[segN] = y;
                    if (segN < segLimit)
                        segN++;
                    break;
                case GS_LINETO:
                    x = data[++i]; y = data[++i];
                    x += xMem; y += yMem;
                    xMem = x; yMem = y;
                    segx[segN] = x;
                    segy[segN] = y;
                    if (segN < segLimit)
                        segN++;
                    break;
                case GS_CURVETO:
                    int tmpx [] = new int [4];
                    int tmpy [] = new int [4];
                    tmpx [0] = xMem; tmpy [0] = yMem;
                    for (int j = 0; j < 3; j++)
                    {
                        x = data[++i]; y = data[++i];
                        x += xMem; y += yMem;
                        xMem = x; yMem = y;
                        tmpx [j+1] = x; tmpy [j+1] = y;
                    }
                    for (int j=1; j<=4; j++)
                    {
                        int v = Bezier (tmpx, j);
                        int w = Bezier (tmpy, j);
                        segx[segN] = v; segy[segN] = w;
                        if (segN < segLimit) segN++;
                    }
                    break;
                case GS_STROKECOLOR:
                    r = data[++i];
                    g = data[++i];
                    b = data[++i];
                    break;
                case GS_FILLCOLOR:
                    rf = data[++i];
                    gf = data[++i];
                    bf = data[++i];
                    break;
                case GS_SETLINEWIDTH:
                    i++;
                    break;
                case GS_CLOSESTROKE:
                    segx [segN] = segx [0];
                    segy [segN] = segy [0];
                    segN++;
                case GS_STROKE:
                case GS_FILL:
                    if (segN < 1000 && segN > 0)
                    {
                        int gs2command = 0;
                        if (command == GS_STROKE || command == GS_CLOSESTROKE || commandnext == GS_STROKE) gs2command |= 1;
                        if (command == GS_FILL || commandnext == GS_FILL) gs2command |= 2;

                        int vn = optimizeGSandWrite (level, out, segx, segy, segN, r, g, b,
                            optimizeGS, gs2command, rf, gf, bf);

                        if (vn > 0)
                        {
//                            gsp[gspN++] = vn;
                            gsp.add(vn);
                        }
                        else optcrv ++;
                        opts += segN - (vn - ((level == 1)?7:11)) / 2;

//                        if (gspN == gspLimit-1)
//                        {
//                            dialog.print ("GS optimizer: TOO MUCH CURVES!");
//                            break;
//                        }
                    }
                    segN = 0;
                    break;
            }
            i++;
        }
        out.close ();

        int len = baout.toByteArray().length / 2;

        FileOutputStream gsstream = new FileOutputStream (exportDirData + gsName);
        out = new DataOutputStream (gsstream);

        if (level == 1)
        {
            out.writeShort (len);
            out.write (baout.toByteArray());
//            out.writeShort (gspN);
//            for (i=0; i < gspN; i++)
//                out.writeShort(gsp [i]);
            out.writeShort(gsp.size());
            for (i=0; i < gsp.size(); i++)
                out.writeShort(gsp.get(i));
        }
        else
        {
            out.writeInt (len);
            out.write (baout.toByteArray());
//            out.writeInt (gspN);
//            for (i=0; i < gspN; i++)
//                out.writeShort(gsp [i]);
            out.writeInt(gsp.size());
            for (i=0; i < gsp.size(); i++)
                out.writeShort(gsp.get(i));
        }
        out.close ();
    }
    private int Bezier (int k [], int n)
    {
        double t = ((double)(n)) / 4;
        double s = 1-t, t2 = t*t;
        return (int) (((s*k[0] + 3*t*k[1])*s + 3*t2*k[2])*s + t2*t*k[3]);
    }
    private int optimizeGSandWrite (int level, DataOutputStream out, int vx[],
                                    int vy[], int n, int r, int g, int b,
                                    int optimizeGS, int command, int rf, int gf,
                                    int bf) throws IOException
    {
        Point [] src = new Point [n];
        for (int i=0; i < n; i++)
            src [i] = new Point (vx[i], vy[i]);

        Point opt[];
        int m = n;
        if (optimizeGS > 0)
        {
            opt = new Point[n];
            boolean mk[] = new boolean[n];
            m = poly_simplify (optimizeGS, src, n, opt, mk);
        }
        else opt = src;

        int xmin = 0xffff, ymin = 0xffff, xmax = 0, ymax = 0;
        for (int j = 0; j < m; j++)
        {
            int x = opt[j].x;
            int y = opt[j].y;
            if (x < xmin)
                xmin = x;
            if (x > xmax)
                xmax = x;
            if (y < ymin)
                ymin = y;
            if (y > ymax)
                ymax = y;
        }

        int dim = Math.max (xmax-xmin, ymax-ymin);

        if (dim < optimizeGS)
            return 0;

        if (level == 1)
        {
            out.writeShort (xmin / globalScale);
            out.writeShort (ymin / globalScale);
            out.writeShort (xmax / globalScale);
            out.writeShort (ymax / globalScale);
            out.writeShort (r);
            out.writeShort (g);
            out.writeShort (b);
        }
        else if (level == 2)
        {
            int cx = (xmax + xmin) / 2, cy = (ymax + ymin) / 2;

            out.writeShort (cx / globalScale);
            out.writeShort (cy / globalScale);
            out.writeShort ((cx - xmin) / globalScale);
            out.writeShort ((cy - ymin) / globalScale);

            out.writeShort (command);

            out.writeShort (r); out.writeShort (g); out.writeShort (b);
            out.writeShort (rf); out.writeShort (gf); out.writeShort (bf);
        }

        int dx = xmin / globalScale, dy = ymin / globalScale;
        for (int j = 0; j < m; j++)
        {
            int x = opt[j].x / globalScale;
            int y = opt[j].y / globalScale;
            out.writeShort (x - dx);
            out.writeShort (y - dy);

            dx = x;
            dy = y;
        }

        int len = (level == 1)? (7 + m * 2):(11 + m * 2);

        return len;
    }

    /*
     Make archives
    */

    private String getExportRoot ()
    {
        String sep = System.getProperty("file.separator");
        return System.getProperty ("user.dir") + sep + "export" + sep;
    }

    private void prepareDirectory ()
    {
        String sep = System.getProperty("file.separator");
        File f = new File (getExportRoot() + "res" + sep + mapName + sep + "d");

        if (f.exists())
        {
            deleteFiles (f);
        }
        new File (getExportRoot() + "res" + sep + mapName + sep). mkdir ();
        f.mkdir ();

        exportDirData = getExportRoot() + "res" + sep + mapName + sep + "d" + sep;
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

    private int calcHardkey ()
    {
        make_crc_table();
        String fn = getClass().getName().replace('.', '/') + ".cla" + "ss";
        InputStream is = getInputStream(fn);
		try {
			byte[] data = loadFromInputStream (is);
            long c = crc(data, data.length);

            return (int)(c & 0x7fff);
		}
		catch (IOException ex) {
		}
        return 0x7531;
    }

    private long crc_table[];
    private boolean crc_table_computed = false;

    private void make_crc_table ()
    {
        crc_table = new long[256];

        long c;
        int n, k;

        for (n = 0; n < 256; n++)
        {
            c = (long) n;

            for (k = 0; k < 8; k++)
            {
                if ( (c & 1) > 0)
                    c = 0xedb88320L ^ (c >> 1);
                else
                    c = c >> 1;
            }
            crc_table[n] = c;
        }
        crc_table_computed = true;
    }

    private long update_crc (long crc, byte[] buf, int len)
    {
        long c = crc;
        int n;

        if (!crc_table_computed)
            make_crc_table ();

        for (n = 0; n < len; n++)
        {
            c = crc_table[ (int) (c ^ buf[n]) & 0xff] ^ (c >> 8);
        }
        return c;
    }

    private long crc (byte[] buf, int len)
    {
        return update_crc (0xffffffffL, buf, len) ^ 0xffffffffL;
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
            }
        }
        return in;
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

    private int loadLicense(Properties license)
    {
        int result = 0xff;

        InputStream is = getInputStream("key.bin");
        if (is == null) return result;
        byte[] data = null;
		try {
			data = loadFromInputStream (is);
		}
		catch (IOException ex) {
            return result;
		}

        int len = data.length;
        int dgLen = data[len-1];

        byte[] bin = new byte[len - dgLen - dgLen - 1];
        byte[] dg = new byte[dgLen];
        byte[] xmlDg = new byte[dgLen];

        System.arraycopy(data, 0, bin, 0, bin.length);
        System.arraycopy(data, bin.length, dg, 0, dgLen);
        System.arraycopy(data, bin.length + dgLen, xmlDg, 0, dgLen);

        result >>= 16;

        // check MD5 of binary file
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            md.reset ();
            md.update (bin);
            byte sign [] = md. digest ();

            boolean equal = true;
            for (int j=0; j < dg.length; j++)
            {
				equal &= sign[j] == dg[j];
                result ^= (sign[j] ^ dg[j]);
			}
            if (!equal)
                result ^= 0x2300;
        }
        catch (NoSuchAlgorithmException ex1) {
            System.err.println (ex1.getMessage());
        }

        // check MD5 of xml
        is = getInputStream("key.xml");
        if (is == null) return result;
        try {
            byte[] xml = loadFromInputStream (is);

            try {
                MessageDigest md = MessageDigest.getInstance ("MD5");
                md.reset ();
                md.update (xml);
                byte sign [] = md. digest ();

                boolean equal = true;
                for (int j=0; j < dg.length; j++)
                {
					equal &= sign[j] == xmlDg[j];
                    result ^= sign[j] ^ xmlDg[j];
				}
                if (!equal)
                    result ^= 0x1B00;
            }
            catch (NoSuchAlgorithmException ex1) {
                System.err.println (ex1.getMessage());
            }
        }
        catch (IOException ex) {
            return result;
        }

        // decode
        for (int i = 4, r = bin[1]; i < bin.length; i++)
        {
            bin[i] = (byte)(bin[i] ^ r);
            r = random2(r);
        }

		try {
			DataInputStream input = new DataInputStream (new GZIPInputStream (new
				ByteArrayInputStream (bin)));

            input.readInt();
            int n = input.readInt();
            boolean f = false;
            for (int i=0; i < n; i++)
            {
                String k = input.readUTF();
                String v = input.readUTF();

                if ("key".equals(k))
                {
                    try {
                        result ^= Integer.parseInt (v);
                        f = true;
                    }
                    catch (NumberFormatException ex) {
                    }
                    catch (NullPointerException ex) {
                    }
                }

                license.setProperty(k, v);
            }
            long c = crc (data, data.length);
            if (!f)
            {
				result ^= (int)(c & 0x7fff);
				result ^= calcHardkey ();
			}
		}
		catch (IOException ex2) {
		}
        return result;
    }

    private int random2 (int r)
    {
        return (int) ((r * 2341L + 153L) & 0x7fffffff);
    }
}
