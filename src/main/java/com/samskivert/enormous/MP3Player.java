//
// $Id$

package com.samskivert.enormous;

import java.applet.AudioClip;
import java.io.File;
import java.io.FileInputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import com.samskivert.util.LoopingThread;

/**
 * Plays an MP3 providing an {@link AudioClip} interface to its control.
 */
public class MP3Player
    implements AudioClip
{
    public MP3Player (File clip)
    {
        _clip = clip;
    }

    // documentation inherited from interface AudioClip
    public void play ()
    {
        stop();
        try {
            final Player player = new Player(new FileInputStream(_clip));
            _thread = new LoopingThread() {
                protected void iterate () {
                    try {
                        player.play(50);
                    } catch (JavaLayerException jle) {
                        jle.printStackTrace(System.err);
                        shutdown();
                    }
                }
                protected void didShutdown () {
                    player.close();
                    if (_thread == this) _thread = null;
                }
            };
            _thread.start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    // documentation inherited from interface AudioClip
    public void loop () {} // not supported

    // documentation inherited from interface AudioClip
    public void stop ()
    {
        if (_thread != null) {
            _thread.shutdown();
            _thread = null;
        }
    }

    protected File _clip;
    protected LoopingThread _thread;
}
