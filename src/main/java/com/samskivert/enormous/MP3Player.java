//
// $Id$

package com.samskivert.enormous;

import java.applet.AudioClip;
import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import com.samskivert.util.LoopingThread;

/**
 * Plays an MP3 providing an {@link AudioClip} interface to its control.
 */
public class MP3Player
    implements AudioClip
{
    public MP3Player (InputStream clip)
    {
        try {
            _player = new Player(clip);
            _thread = new LoopingThread() {
                protected void iterate () {
                    try {
                        _player.play(50);
                    } catch (JavaLayerException jle) {
                        jle.printStackTrace(System.err);
                        shutdown();
                    }
                }
                protected void didShutdown () {
                    _player.close();
                }
            };
        } catch (JavaLayerException jle) {
            jle.printStackTrace(System.err);
        }
    }

    // documentation inherited from interface AudioClip
    public void play ()
    {
        if (_thread != null) {
            _thread.start();
        }
    }

    // documentation inherited from interface AudioClip
    public void loop ()
    {
        // not supported
    }

    // documentation inherited from interface AudioClip
    public void stop ()
    {
        if (_thread != null) {
            _thread.shutdown();
        }
    }

    protected Player _player;
    protected LoopingThread _thread;
}
