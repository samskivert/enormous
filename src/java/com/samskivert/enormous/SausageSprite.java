//
// $Id$

package com.samskivert.enormous;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import com.samskivert.swing.Label;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.action.ActionSprite;

/**
 * Displays a sausage title.
 */
public class SausageSprite extends Sprite
    implements ActionSprite
{
    public SausageSprite (
        int width, int height, String text, Font font, Color bgcolor, String action)
    {
        super(width, height);
        _action = action;
        _bgkey = new SausageCache.Key(width, height, bgcolor, null, font, text, Label.CENTER);

        _label = new Label(text);
        _label.setTextColor(bgcolor == Color.white ? Color.blue : Color.white);
        _label.setFont(font);
        _label.setTargetWidth(width-2*BORDER);
        _label.setAlignment(Label.CENTER);
    }

    /**
     * Reconfigures this sprite's background color.
     */
    public void setBackground (Color bgcolor)
    {
        _bgkey = _bgkey.setBackground(bgcolor);
        invalidate();
    }

    /**
     * Configures this sprite with an image background.
     */
    public void setBackground (BufferedImage image)
    {
        // we have a background now, so put an outline on our font
        _label.setStyle(Label.OUTLINE);
        _label.setAlternateColor(Color.black);

        _bgkey = _bgkey.setBackground(image);
        invalidate();
    }

    /**
     * Reconfigures this sprite's text.
     */
    public void setText (String text)
    {
        _label.setText(text);
        if (_mgr != null) {
            layoutLabel();
            invalidate();
        }

//         _bgkey = _bgkey.setText(text);
//         invalidate();
    }

    /**
     * Changes this label's alignment (the default is {@link Label#CENTER}).
     */
    public void setAlignment (int align)
    {
        _label.setAlignment(align);
//         _bgkey = _bgkey.setAlignment(align);
//         invalidate();
    }

    /**
     * Reconfigures this sprite's action command.
     */
    public void setActionCommand (String action)
    {
        _action = action;
    }

    // documentation inherited from interface ActionSprite
    public String getActionCommand ()
    {
        return _action;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.drawImage(SausageCache.getSausage(_bgkey), _bounds.x, _bounds.y, null);
        renderLabel(gfx);
    }

    // documentation inherited
    protected void init ()
    {
        super.init();
        layoutLabel();
    }

    protected void layoutLabel ()
    {
        Graphics2D gfx = _mgr.createGraphics();
        if (gfx != null) {
            SwingUtil.activateAntiAliasing(gfx);
            layoutLabel(gfx);
            gfx.dispose();
        }
    }

    protected void layoutLabel (Graphics2D gfx)
    {
        _label.layout(gfx);

        // update the location of our label
        Dimension size = _label.getSize();
        _lx = (_bounds.width - size.width)/2;
        _ly = (_bounds.height - size.height)/2;
    }

    protected void renderLabel (Graphics2D gfx)
    {
        _label.render(gfx, _bounds.x + _lx, _bounds.y + _ly);
    }

    protected String _action;
    protected SausageCache.Key _bgkey;

    protected Label _label;
    protected int _lx, _ly;

    protected static final int BORDER = 10;
}
