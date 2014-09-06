/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.isrt;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ish.isrt.core.CityLoader;
import com.ish.mobimapbuilder.MobimapConfiguration;

public class Builder2IsrtFacade {
    public Builder2IsrtFacade () {
    }

    public void process (List < com.ish.mobimapbuilder.I7Container > model, MobimapConfiguration mobimapConfiguration) {
        String dataFile = null;

        Map<String, byte[]> binaryContents = new HashMap<String, byte[]>();

        for (com.ish.mobimapbuilder.I7Container container : model) {
            for (int i = 0; i < container.getPacketCount (); i++) {
                String packetName = container.getPacket (i).getName ();
                binaryContents.put (packetName, container.getBinaryContent ());

                if (packetName.equals(mobimapConfiguration.getParams().getProperty("data")))
                    dataFile = packetName;
            }
        }

        // generate temporary project
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                   "<map name=\"temp\">\n" +
                   "<data file=\"" + dataFile + "\" />\n";

        Properties params = mobimapConfiguration.getParams ();

        for (Enumeration en = params.propertyNames (); en.hasMoreElements (); ) {
            String name = (String) en.nextElement ();
            String value = params.getProperty (name);

            s += "<param name=\"" + name + "\" value=\"" + value + "\"/>\n";
            s += "<param name=\"mobimap." + name + "\" value=\"" + value + "\"/>\n";
        }
        s += "</map>\n";

        CityLoader loader = new CityLoader (binaryContents, mobimapConfiguration);
        byte[] data = null;
        try {
            data = s.getBytes ("UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        loader.init (data);

        loader.export ();
    }
}
