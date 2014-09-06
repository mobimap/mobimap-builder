/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import com.ish.mobimapbuilder.data.City;
import com.ish.mobimapbuilder.model.Layer;
import com.ish.mobimapbuilder.target.Target;
import com.ish.mobimapbuilder.transform.Geo2Meters;

public class Project {
    public static final String PARAM_SOURCE_CS = "sourceCS";

    public static final String CS_GEO = "geo";
    public static final String CS_METRIC = "metric";

    private List<Layer> layers;
    private Map<String, Target> targets;
    private double transformX, transformY;
    private CoordinatesConverter coordinatesConverter;

    private String cityName = "";
    private int cityX = 10000, cityY = 10000;

    private String sourceCS;
    private Geo2Meters geo2meters;

    private City city;
    private Properties params;

    private MobimapConfiguration mobimapConfiguration;

    public Project () {
        layers = new Vector<Layer>();
        targets = new HashMap<String, Target>();
        params = new Properties ();
    }

    public void addLayer (Layer layer) {
        layers.add (layer);
    }

    public List<Layer> getLayers () {
        return layers;
    }

    public void addTarget (Target target) {
        targets.put (target.getId (), target);
    }

    public Target getTarget (String id) {
        return targets.get (id);
    }

    public Map<String, Target> getTargets () {
        return targets;
    }

    public void addParam (String name, String value) {
        params.setProperty (name, value);
    }

    public String getParam (String name) {
        return params.getProperty (name);
    }

    public void setTransformY (double transformY) {
        this.transformY = transformY;
    }

    public void setTransformX (double transformX) {
        this.transformX = transformX;
    }

    public void setCityY (int cityY) {
        this.cityY = cityY;
    }

    public void setCityX (int cityX) {
        this.cityX = cityX;
    }

    public void setCityName (String cityName) {
        this.cityName = cityName;
    }

    public void setCoordinatesConverter (CoordinatesConverter coordinatesConverter) {
        this.coordinatesConverter = coordinatesConverter;
    }

    public void setCity (City city) {
        this.city = city;
    }

    public void setSourceCS (String sourceCS) {
        this.sourceCS = sourceCS;
    }

    public void setGeo2meters (Geo2Meters geo2meters) {
        this.geo2meters = geo2meters;
    }

    public void setParams (Properties params) {
        this.params = params;
    }

    public void setMobimapConfiguration (MobimapConfiguration mobimapConfiguration) {
        this.mobimapConfiguration = mobimapConfiguration;
    }

    public double getTransformY () {
        return transformY;
    }

    public double getTransformX () {
        return transformX;
    }

    public int getCityY () {
        return cityY;
    }

    public int getCityX () {
        return cityX;
    }

    public String getCityName () {
        return cityName;
    }

    public CoordinatesConverter getCoordinatesConverter () {
        return coordinatesConverter;
    }

    public City getCity () {
        return city;
    }

    public String getSourceCS () {
        return sourceCS;
    }

    public Geo2Meters getGeo2meters () {
        return geo2meters;
    }

    public Properties getParams () {
        return params;
    }

    public MobimapConfiguration getMobimapConfiguration () {
        return mobimapConfiguration;
    }
}
