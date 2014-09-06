/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.target;

import java.io.*;

import java.awt.*;

import org.w3c.dom.*;

public class GraphicTarget extends Target
{
    public static final String TYPE = "graphic";

    private static final int GS_EOF = 0;
    private static final int GS_MOVETO = 1;
    private static final int GS_LINETO = 2;
    private static final int GS_CURVETO = 3;
    private static final int GS_STROKECOLOR = 4;
    private static final int GS_FILLCOLOR = 5;
    private static final int GS_SETLINEWIDTH = 6;
    private static final int GS_STROKE = 7;
    private static final int GS_CLOSESTROKE = 8;
    private static final int GS_FILL = 9;

    private static final int STREAM_CAPACITY = 1000;

    private static final String TAB = "\t";
    private static final String EOL = "\n";

    private int lastX = 0;
    private int lastY = 0;

	public GraphicTarget ()
	{
        super(TYPE);
	}

    public boolean load(Node targetNode)
    {
        boolean result = false;

        for (Node node = targetNode.getFirstChild();
             node != null;
             node = node.getNextSibling() )
        {
            NamedNodeMap attribs = node.getAttributes ();
            String nodeName = node.getNodeName();

            super.loadParameters(nodeName, attribs);
            loadParameters(nodeName, attribs);
        }

        result = getId() != null;

        return result;
    }
    protected void loadParameters (String nodeName, NamedNodeMap attribs)
    {
    }

    public void save ()
    {
    }

    public void gSetFillColor (Color color) throws IOException
    {
        fileWriter.write ("" + GS_FILLCOLOR + TAB +
                   color.getRed () + TAB + color.getGreen () + TAB +
                   color.getBlue () + EOL);
    }
    public void gSetOutlineColor (Color color) throws IOException
    {
        fileWriter.write ("" + GS_STROKECOLOR + TAB +
                   color.getRed () + TAB + color.getGreen () + TAB +
                   color.getBlue () + EOL);
    }
    public void gMoveTo (int x, int y) throws IOException
    {
        fileWriter.write ("" + GS_MOVETO + TAB +
                   (x - lastX) + TAB + (y - lastY) + EOL);
        lastX = x;
        lastY = y;
    }
    public void gLineTo (int x, int y) throws IOException
    {
        fileWriter.write ("" + GS_LINETO + TAB +
                   (x - lastX) + TAB + (y - lastY) + EOL);
        lastX = x;
        lastY = y;
    }
    public void gFill() throws IOException
    {
        fileWriter.write ("" + GS_FILL + EOL);
    }
    public void gOutline() throws IOException
    {
        fileWriter.write ("" + GS_STROKE + EOL);
    }
}
