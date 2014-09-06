/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.awt.*;
import javax.swing.*;

public class Builder implements Runnable, Logger {
    boolean packFrame = false;

    ProjectLoader loader;

    private static String projectFile = "project.xml";

    private MainWindow mainWindow = null;

    public Builder () {
        MainWindow frame = new MainWindow ();
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) {
            frame.pack ();
        }
        else {
            frame.validate ();
        }

        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
        Dimension frameSize = frame.getSize ();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation ((screenSize.width - frameSize.width) / 2,
                           (screenSize.height - frameSize.height) / 2);

        mainWindow = frame;
        new Thread (this).start ();
    }

    public void run () {
//        mainWindow.setVisible(true);
        mainWindow.textArea.setText ("Processing file: " + projectFile + "…\n");
        System.out.println ("Processing file: " + projectFile);

        loader = new ProjectLoader (this);
        if (!loader.load (projectFile)) {
            System.exit (1);
        }
        loader.parse ();
        loader.export ();

        message ("\nDone!");
        try {
            Thread.sleep (1000);
        } catch (InterruptedException ex) {
        }
        System.exit (0);
    }

    public void message (Object msg) {
        if (msg != null) {
            mainWindow.textArea.append (msg.toString () + "\n");
            System.out.println (msg);
        }
    }

    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println ("MobimapConverter [project.xml]");
            return;
        }
        projectFile = args[0];

        if ("/0".equals (projectFile)) {
            com.ish.isrt.core.export.Export2MobimapQuadra.calcHardkeyS ();
            return;
        }

        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName ());
                } catch (Exception exception) {
                    exception.printStackTrace ();
                }

                new Builder ();
            }
        });
    }
}
