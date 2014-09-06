package com.ish.mobimapbuilder.source;

import com.ish.mobimapbuilder.model.Layer;
import java.io.InputStream;

public interface SourceParser {
    public void load(Layer layer, InputStream dataStream, InputStream geomeryStream)
        throws DataParserException;
}
