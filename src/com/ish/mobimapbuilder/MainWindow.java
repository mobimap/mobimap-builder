/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder;

import java.awt.*;
import javax.swing.*;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;

public class MainWindow extends JFrame {
    JPanel contentPane;
    BorderLayout borderLayout1 = new BorderLayout ();
    JScrollPane jScrollPane1 = new JScrollPane ();
    JTextArea textArea = new JTextArea ();

    public MainWindow () {
        try {
            setDefaultCloseOperation (EXIT_ON_CLOSE);
            jbInit ();
        } catch (Exception exception) {
            exception.printStackTrace ();
        }
    }

    /**
     * Component initialization.
     *
     * @throws java.lang.Exception
     */
    private void jbInit () throws Exception {
        contentPane = (JPanel) getContentPane ();
        setSize (new Dimension (500, 300));
        setTitle ("Mobimap Converter");
        this.addWindowListener (new WindowAdapter () {
            public void windowClosing (WindowEvent e) {
                this_windowClosing (e);
            }
        });
        contentPane.setLayout (borderLayout1);
        textArea.setFont (new java.awt.Font ("Dialog", Font.PLAIN, 10));
        textArea.setText ("jTextArea1");
        contentPane.add (jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport ().add (textArea);
    }

    public void this_windowClosing (WindowEvent e) {
        System.exit (0);
    }
}
