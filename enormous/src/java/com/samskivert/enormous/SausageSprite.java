//
// $Id$

package com.samskivert.enormous;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import com.samskivert.swing.Label;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.sprite.LabelSprite;

/**
 * Displays a sausage title.
 */
public class SausageSprite extends LabelSprite
{
    public SausageSprite (int width, int height, String text,
                          Font font, Color bgcolor)
    {
        super(new Label(text));

        _bounds.width = width;
        _bounds.height = height;
        _bgcolor = bgcolor;

        _label.setTextColor(Color.white);
        _label.setFont(font);
        _label.setTargetWidth(width-2*BORDER);
        _label.setAlignment(Label.CENTER);
        setAntiAliased(true);
    }

    // documentation inherited
    public void updateBounds ()
    {
        // we handle our own bounds
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(_bgcolor);
        gfx.fillRoundRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height,
                          2*BORDER, 2*BORDER);

        Object obj = SwingUtil.activateAntiAliasing(gfx);

        Dimension size = _label.getSize();
        int dx = _bounds.width - size.width;
        int dy = _bounds.height - size.height;
        _label.render(gfx, _bounds.x + dx/2, _bounds.y + dy/2);

        Stroke ostroke = gfx.getStroke();
        gfx.setStroke(FAT_STROKE);
        gfx.setColor(Color.white);
        gfx.drawRoundRect(_bounds.x, _bounds.y, _bounds.width, _bounds.height,
                          2*BORDER, 2*BORDER);
        gfx.setStroke(ostroke);

        SwingUtil.restoreAntiAliasing(gfx, obj);
    }

    protected Color _bgcolor;

    protected static final int BORDER = 10;
    protected static final Stroke FAT_STROKE = new BasicStroke(3f);
}
