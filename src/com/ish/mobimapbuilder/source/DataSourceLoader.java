/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.ish.mobimapbuilder.model.Layer;
import com.ish.mobimapbuilder.util.StringUtils;

public class DataSourceLoader {
    /**
     * Loads data from files specified in layer
     */
    public void loadLayer (Layer layer) throws DataParserException {
        String dataFileName = layer.getDataFileName ();
        String geometryFileName = layer.getGeometryFileName ();

        InputStream dataStream = null;
        InputStream geometryStream = null;

        try {
            if (dataFileName == null && geometryFileName == null) {
                throw new DataParserException ("Nor data, nor geometry is defined");
            }

            if (dataFileName != null) {
                File dataFile = new File (dataFileName);
                try {
                    dataStream = new FileInputStream (dataFile);
                } catch (FileNotFoundException ex) {
                    throw new DataParserException ("Data file " + dataFileName + " isn't found");
                }
            }

            if (geometryFileName != null) {
                File geometryFile = new File (geometryFileName);
                try {
                    geometryStream = new FileInputStream (geometryFile);
                } catch (FileNotFoundException ex) {
                    throw new DataParserException ("Geometry file " + geometryFileName + " isn't found");
                }
            }

            String type = StringUtils.extractFileExtension ((geometryFileName != null) ? geometryFileName : dataFileName).
                          toLowerCase ();
            SourceParser parser = SourceParserFactory.getSourceParser (type);
            parser.load (layer, dataStream, geometryStream);
        } catch (DataParserException ex2) {
            throw ex2;
        } finally {
            if (dataStream != null) {
                try {
                    dataStream.close ();
                } catch (IOException ex3) {
                    // ignore exception on close
                }
            }
            if (geometryStream != null) {
                try {
                    geometryStream.close ();
                } catch (IOException ex3) {
                    // ignore exception on close
                }
            }
        }
    }
}
