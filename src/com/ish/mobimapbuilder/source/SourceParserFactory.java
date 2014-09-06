package com.ish.mobimapbuilder.source;

public class SourceParserFactory {

    private static final String TYPE_MAPINFO = "mif";
    private static final String TYPE_SHAPEFILE = "shp";
    private static final String TYPE_POLISH = "mp";

    public static SourceParser getSourceParser(String type) throws DataParserException {
        if (TYPE_MAPINFO.equals(type)) {
            return new MapinfoParser();
        } else if (TYPE_SHAPEFILE.equals(type)) {
            return new ShapefileParser();
        } else if (TYPE_POLISH.equals(type)) {
            return new PolishParser();
        }
        throw new DataParserException("Unknown source type = " + type);
    }
}
