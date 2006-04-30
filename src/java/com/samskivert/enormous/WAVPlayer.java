//
// $Id$

package com.samskivert.enormous;

import java.applet.AudioClip;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import com.samskivert.util.LoopingThread;

/**
 * Does something extraordinary.
 */
public class WAVPlayer
    implements AudioClip
{
    public WAVPlayer (InputStream clip)
    {
        try {
            _stream = AudioSystem.getAudioInputStream(clip);
            DataLine.Info info = new DataLine.Info(
                SourceDataLine.class, _stream.getFormat());
            _source = (SourceDataLine)AudioSystem.getLine(info);

            _thread = new LoopingThread() {
                protected void willStart() {
                    try {
                        _source.open(_stream.getFormat());
                        _source.start();
                    } catch (Exception e) {
                        System.err.println("Unable to open source: " + e);
                    }
                }

                protected void iterate () {
                    try {
                        _count = _stream.read(_buffer, 0, _buffer.length);
                        if (_count == -1) {
                            shutdown();
                        } else {
                            _source.write(_buffer, 0, _count);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed reading audio: " + e);
                        shutdown();
                    }
                }

                protected void didShutdown () {
                    _source.drain();
                    _source.close();
                }

                protected int _count;
                protected byte[] _buffer = new byte[4096];
            };

        } catch (Exception e) {
            System.err.println("Unable to load clip: " + e);
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

    protected AudioInputStream _stream;
    protected SourceDataLine _source;
    protected LoopingThread _thread;
}
