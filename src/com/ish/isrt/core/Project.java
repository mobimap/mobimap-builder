//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core;

import java.util.*;

public class Project
{
    private List<Layer> layers;
    private Properties params;
    private String dataFileName;

    public Project ()
    {
        layers = new ArrayList<Layer>();
        params = new Properties();
    }

    public void addLayer(Layer layer)
    {
        layers.add(layer);
    }

    public void setParameter(String name, String value)
    {
        params.put(name, value);
    }

    public Properties getParams ()
    {
        return params;
    }

    public String getParameter(String name)
    {
        return params.getProperty(name);
    }

    public List<Layer> getLayers ()
    {
        return layers;
    }

    public void setDataFileName (String dataFileName)
    {

        this.dataFileName = dataFileName;
    }

    public String getDataFileName ()
    {
        return dataFileName;
    }
}
