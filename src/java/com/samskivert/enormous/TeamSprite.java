//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

import com.samskivert.swing.Label;

/**
 * Displays information on a team and their current player.
 */
public class TeamSprite extends SausageSprite
{
    public TeamSprite (int width, int height, int teamIdx,
                       Font font, Color bgcolor)
    {
        super(width, height, EnormousConfig.getTeamName(teamIdx),
              font, bgcolor, "team:" + teamIdx);

        _plabel = new Label("???");
        _plabel.setTextColor(bgcolor == Color.white ? Color.blue : Color.white);
        _plabel.setFont(font);
        _plabel.setTargetWidth(width-2*BORDER);
        _plabel.setAlignment(Label.CENTER);

        try {
            if (_star == null) {
                String path = "rsrc/media/star.png";
                InputStream in =
                    getClass().getClassLoader().getResourceAsStream(path);
                _star = ImageIO.read(in);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    public void setTeamStars (int stars)
    {
        _stars = stars;
        invalidate();
    }

    public int getTeamStars ()
    {
        return _stars;
    }

    public void setPlayer (Player player)
    {
        _player = player;
        _plabel.setText(player.name + ": " + player.score);
        if (_mgr != null) {
            layoutLabel();
            invalidate();
        }
    }

    // documentation inherited
    protected void layoutLabel (Graphics2D gfx)
    {
        super.layoutLabel(gfx);

        _plabel.layout(gfx);

        Dimension size = _label.getSize();
        Dimension psize = _plabel.getSize();
        int height = size.height + GAP + psize.height + GAP + _star.getHeight();

        _ly = (_bounds.height - height) / 2;
        _plx = (_bounds.width - psize.width)/2;
        _ply = _ly + size.height + GAP;
    }

    // documentation inherited
    protected void renderLabel (Graphics2D gfx)
    {
        super.renderLabel(gfx);

        _plabel.render(gfx, _bounds.x + _plx, _bounds.y + _ply);

        if (_star != null && _stars > 0) {
            int swidth = _star.getWidth() * _stars + (_stars-1) * GAP;
            int dx = (_star.getWidth() + GAP);
            if (swidth > _bounds.width - 2*GAP) {
                dx = (_bounds.width - 2*GAP - _star.getWidth()) / (_stars-1);
                swidth = dx * (_stars-1) + _star.getWidth();
            }
            int sx = _bounds.x + (_bounds.width - swidth) / 2;
            int sy = _bounds.y + _ply + _plabel.getSize().height + GAP;
            for (int ii = 0; ii < _stars; ii++) {
                gfx.drawImage(_star, sx, sy, null);
                sx += dx;
            }
        }
    }

    protected int _stars;
    protected Player _player;
    protected Label _plabel;
    protected int _plx, _ply;

    protected static BufferedImage _star;

    protected static final int GAP = 5;
}
