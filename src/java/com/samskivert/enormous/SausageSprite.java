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

    // documentation inherited from interface ActionSprite
    public String getActionCommand ()
    {
        return _action;
    }

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        gfx.setColor(_bgcolor);
        gfx.fillRoundRect(_bounds.x+2, _bounds.y+2, _bounds.width-4,
                          _bounds.height-4, 2*BORDER, 2*BORDER);

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

    protected void layoutLabel ()
    {
        Graphics2D gfx = (Graphics2D)_mgr.getMediaPanel().getGraphics();
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

    protected Label _label;
    protected int _lx, _ly;

    protected static final int BORDER = 10;
    protected static final Stroke FAT_STROKE = new BasicStroke(3f);
}
