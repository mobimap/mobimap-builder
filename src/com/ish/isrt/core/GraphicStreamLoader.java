//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import java.awt.Component;

public class GraphicStreamLoader
{
    private Component hoster;

    private HashMap<String,int[]> streams;

    final int GS_GRANULARITY = 4000;

    public GraphicStreamLoader ()
    {
        streams = new HashMap<String,int[]>();
    }
    public GraphicStreamLoader (Component hoster)
    {
        this();
        this.hoster = hoster;
    }

    public void loadStream (String name)
    {
        int[] stream = loadStreamInternal (name);
        streams.put(name, stream);

//        if (hoster != null) hoster.repaint ();
    }

    private int[] loadStreamInternal (String name)
    {
        int[] data = null;
        try
        {
			I7Container container = new I7Container ();
			container.setFileName (name);
			container.load ();

			int pn = container.getPacketCount ();
			if (pn < 1)
                return null;

			I7Packet packet = container.getPacket (0);
			InputStream fis = new GZIPInputStream (packet.getInputStream ());

            Reader r = new BufferedReader (new InputStreamReader (fis));
            StreamTokenizer st = new StreamTokenizer (r);

            st.eolIsSignificant (false);
            st.wordChars (64, 255);

            data = new int[GS_GRANULARITY];
            int i = 0;

            while (true)
            {
                int res = st.nextToken ();
                if (res == StreamTokenizer.TT_NUMBER)
                    data[i++] = (int) st.nval;
                else if (res == StreamTokenizer.TT_EOF)
                    break;

                if (i > data.length - 5)
                {
                    int t[] = new int[data.length + GS_GRANULARITY];
                    for (int j = 0; j < data.length; j++)
                        t[j] = data[j];
                    data = t;
                }
            }
        }
        catch (java.io.IOException a)
        { }

        return data;
    }

    public int[] getStream (String name)
    {
        return name == null? null: streams.get(name);
    }
}
