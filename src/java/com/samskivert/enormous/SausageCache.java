//
// $Id$

package com.samskivert.enormous;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.Map;

import com.samskivert.swing.Label;
import com.samskivert.swing.util.SwingUtil;

/**
 * Caches sausaged images.
 */
public class SausageCache
{
    public static class Key {
        public final int width;
        public final int height;
        public final Font font;
        public final Color bgcolor;
        public final BufferedImage bgimage;
        public final String text;
        public final int align;

        public Key (int width, int height, Font font, Color bgcolor, BufferedImage bgimage,
                    String text, int align) {
            this.width = width;
            this.height = height;
            this.font = font;
            this.bgcolor = bgcolor;
            this.bgimage = bgimage;
            this.text = text;
            this.align = align;
        }

        public Key setBackground (BufferedImage nbgimage) {
            return new Key(width, height, font, bgcolor, nbgimage, text, align);
        }

        public Key setBackground (Color nbgcolor) {
            return new Key(width, height, font, nbgcolor, bgimage, text, align);
        }

        public Key setText (String ntext) {
            return new Key(width, height, font, bgcolor, bgimage, ntext, align);
        }

        public Key setAlignment (int nalign) {
            return new Key(width, height, font, bgcolor, bgimage, text, nalign);
        }

        @Override public boolean equals (Object other) {
            Key okey = (Key)other;
            return (okey.width == width && okey.height == height && okey.font.equals(font) &&
                    okey.bgcolor.equals(bgcolor) && okey.bgimage == bgimage &&
                    okey.text.equals(text) && okey.align == align);
        }

        @Override public int hashCode () {
            int code = 17;
            code = 31 * code + width;
            code = 31 * code + height;
            code = 31 * code + font.hashCode();
            code = 31 * code + bgcolor.hashCode();
            if (bgimage != null) {
                code = 31 * code + bgimage.hashCode();
            }
            code = 31 * code + text.hashCode();
            code = 31 * code + align;
            return code;
        }
    }

    public static BufferedImage getSausage (Key key)
    {
        BufferedImage image = _cache.get(key);
        if (image != null) {
            return image;
        }

        image = _gc.createCompatibleImage(key.width, key.height, Transparency.TRANSLUCENT);
        Graphics2D gfx = image.createGraphics();
        try {
            // render our background image or background color
            if (key.bgimage != null) {
                Shape oclip = gfx.getClip();
                gfx.clip(new RoundRectangle2D.Float(2, 2, key.width-4, key.height-4, BSIZE, BSIZE));
                gfx.drawImage(key.bgimage, 2, 2, null);
                gfx.setClip(oclip);
            } else {
                gfx.setColor(key.bgcolor);
                gfx.fillRoundRect(2, 2, key.width-4, key.height-4, BSIZE, BSIZE);
            }

            Object obj = SwingUtil.activateAntiAliasing(gfx);

//             // create, configure and render our label
//             Label label = new Label(key.text);
//             label.setTextColor(key.bgcolor == Color.white ? Color.blue : Color.white);
//             label.setFont(key.font);
//             label.setTargetWidth(key.width-BSIZE);
//             label.setAlignment(key.align);
//             if (key.bgimage != null) {
//                 label.setStyle(Label.OUTLINE);
//                 label.setAlternateColor(Color.black);
//             }
//             label.layout(gfx);
//             Dimension size = label.getSize();
//             label.render(gfx, (key.width - size.width)/2, (key.height - size.height)/2);

            // render the border of the sausage
            Stroke ostroke = gfx.getStroke();
            gfx.setStroke(FAT_STROKE);
            gfx.setColor(Color.white);
            gfx.drawRoundRect(2, 2, key.width-4, key.height-4, BSIZE, BSIZE);
            gfx.setStroke(ostroke);
            SwingUtil.restoreAntiAliasing(gfx, obj);

        } finally {
            gfx.dispose();
        }

        _cache.put(key, image);
        return image;
    }

    protected SausageCache ()
    {
        // no constructy
    }

    protected static Map<Key, BufferedImage> _cache = new HashMap<Key, BufferedImage>();
    protected static GraphicsConfiguration _gc;
    static {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        _gc = env.getDefaultScreenDevice().getDefaultConfiguration();
    }

    protected static final int BSIZE = 2*SausageSprite.BORDER;
    protected static final Stroke FAT_STROKE = new BasicStroke(3f);
}
