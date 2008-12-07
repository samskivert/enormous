//
// $Id$

package com.samskivert.enormous;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.RunAnywhere;
import com.samskivert.util.RunQueue;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJFrame;
import com.threerings.media.util.ModeUtil;

/**
 * Creates the necessary bits and starts up the game application.
 */
public class EnormousApp
{
    public static final RunQueue queue = RunQueue.AWT;

    public static ManagedJFrame frame;

    public static void main (String[] args)
    {
        // create and display the interface
        frame = new ManagedJFrame("That's ENORMOUS");
        frame.setDefaultCloseOperation(ManagedJFrame.EXIT_ON_CLOSE);

        final FrameManager fmgr = FrameManager.newInstance(frame);
        final EnormousPanel panel = new EnormousPanel(fmgr);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        System.out.println("FS " + System.getProperty("full_screen") +
                           " SUP " + gd.isFullScreenSupported());
        if (System.getProperty("full_screen") != null &&
            gd.isFullScreenSupported()) {
            // set up full screen mode if we're on vinders
            frame.setUndecorated(true);
            gd.setFullScreenWindow(frame);
            DisplayMode pmode = ModeUtil.getDisplayMode(gd, 1024, 768, 16, 15);
            if (pmode != null) {
                gd.setDisplayMode(pmode);
            }
            frame.pack();
        } else {
            frame.pack();
            SwingUtil.centerWindow(frame);
            frame.setVisible(true);
        }
        EnormousConfig.init(frame.getWidth(), frame.getHeight());

        // start up the frame manager
        new Interval(queue) {
            public void expired () {
                fmgr.start();
            }
        }.schedule(2000L);

        // start the first round
        EventQueue.invokeLater(new Runnable() {
            public void run () {
                panel.setRound(0);
            }
        });
    }
}
