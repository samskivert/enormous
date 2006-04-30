//
// $Id$

package com.samskivert.enormous;

import java.applet.AudioClip;
import java.io.File;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import com.samskivert.util.LoopingThread;

/**
 * Does something extraordinary.
 */
public class WAVPlayer
    implements AudioClip
{
    public WAVPlayer (File clip)
    {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(clip);
            AudioFormat format = stream.getFormat();
            int length = (int)stream.getFrameLength() * format.getFrameSize();
            DataLine.Info info = new DataLine.Info(Clip.class, format, length);
            _clip = (Clip)AudioSystem.getLine(info);
            _clip.open(stream);

        } catch (Exception e) {
            System.err.println("Unable to load clip: " + e);
        }
    }

    // documentation inherited from interface AudioClip
    public void play ()
    {
        if (_clip != null) {
            _clip.setFramePosition(0);
            _clip.start();
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
        if (_clip != null) {
            _clip.stop();
        }
    }

    protected Clip _clip;
    protected LoopingThread _thread;
}
