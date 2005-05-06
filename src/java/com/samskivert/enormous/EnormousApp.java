//
// $Id$

package com.samskivert.enormous;

import java.awt.EventQueue;
import javax.swing.JFrame;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.RunQueue;

import com.threerings.media.FrameManager;

/**
 * Creates the necessary bits and starts up the game application.
 */
public class EnormousApp
{
    public static RunQueue queue = new RunQueue() {
        public void postRunnable (Runnable r) {
            EventQueue.invokeLater(r);
        }
        public boolean isDispatchThread () {
            return EventQueue.isDispatchThread();
        }
    };

    public static void main (String[] args)
    {
        // create and display the interface
        JFrame frame = new JFrame("That's ENORMOUS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FrameManager fmgr = FrameManager.newInstance(frame);
        EnormousPanel panel = new EnormousPanel(fmgr);
        frame.setContentPane(panel);
        frame.pack();
        EnormousConfig.init(frame.getWidth(), frame.getHeight());
        SwingUtil.centerWindow(frame);
        frame.setVisible(true);

        // start up the frame manager
        fmgr.start();
    }
}
