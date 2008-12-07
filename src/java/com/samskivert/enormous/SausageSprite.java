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
    public SausageSprite (int width, int height, String text,
                          Font font, Color bgcolor, String action)
    {
        super(width, height);

        _bgcolor = bgcolor;
        _action = action;

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
        _bgcolor = bgcolor;
        invalidate();
    }

    /**
     * Configures this sprite with an image background.
     */
    public void setBackground (BufferedImage image)
    {
        // if we have a background, put an outline on our font
        _label.setStyle(Label.OUTLINE);
        _label.setAlternateColor(Color.black);

        // configure the background and clipping region
        _bgimage = image;
        _clip = new RoundRectangle2D.Float(
            _bounds.x+2, _bounds.y+2, _bounds.width-4,
            _bounds.height-4, 2*BORDER, 2*BORDER);
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
        if (_bgimage != null) {
            Shape oclip = gfx.getClip();
            gfx.clip(_clip);
            gfx.drawImage(_bgimage, _bounds.x+2, _bounds.y+2, null);
            gfx.setClip(oclip);
        } else {
            gfx.setColor(_bgcolor);
            gfx.fillRoundRect(_bounds.x+2, _bounds.y+2, _bounds.width-4,
                              _bounds.height-4, 2*BORDER, 2*BORDER);
        }

        Object obj = SwingUtil.activateAntiAliasing(gfx);
        renderLabel(gfx);

        Stroke ostroke = gfx.getStroke();
        gfx.setStroke(FAT_STROKE);
        gfx.setColor(Color.white);
        gfx.drawRoundRect(_bounds.x+2, _bounds.y+2, _bounds.width-4,
                          _bounds.height-4, 2*BORDER, 2*BORDER);
        gfx.setStroke(ostroke);
        SwingUtil.restoreAntiAliasing(gfx, obj);
    }

    // documentation inherited
    protected void init ()
    {
        super.init();
        layoutLabel();
    }

    // documentation inherited
    protected void updateRenderOrigin ()
    {
        super.updateRenderOrigin();
        if (_clip != null) {
            _clip = new RoundRectangle2D.Float(
                _bounds.x+2, _bounds.y+2, _bounds.width-4,
                _bounds.height-4, 2*BORDER, 2*BORDER);
        }
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

    protected Color _bgcolor;
    protected String _action;

    protected BufferedImage _bgimage;
    protected Shape _clip;

    protected Label _label;
    protected int _lx, _ly;

    protected static final int BORDER = 10;
    protected static final Stroke FAT_STROKE = new BasicStroke(3f);
}
