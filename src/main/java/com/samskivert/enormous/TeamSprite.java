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
        _plabel.setTextColor(_label.getTextColor());
        _plabel.setFont(font);
        _plabel.setTargetWidth(width-2*BORDER);
        _plabel.setAlignment(Label.CENTER);
        _plabel.setStyle(Label.OUTLINE);
        _plabel.setAlternateColor(Color.black);

        try {
            if (_star == null) {
                String path = "media/star.png";
                InputStream in =
                    getClass().getClassLoader().getResourceAsStream(path);
                _star = ImageIO.read(in);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    public void setStars (int stars)
    {
        _stars = stars;
        invalidate();
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
            drawStars(gfx, _bounds.width - 2*GAP, _bounds.x + GAP,
                      _bounds.y + _ply + _plabel.getSize().height + GAP,
                      _stars, true);
        }
    }

    public static void drawStars (
        Graphics2D gfx, int width, int sx, int sy, int count, boolean center)
    {
        int swidth = _star.getWidth() * count + (count-1) * GAP;
        int dx = (_star.getWidth() + GAP);
        if (swidth > width) {
            dx = (width - _star.getWidth()) / (count-1);
            swidth = dx * (count-1) + _star.getWidth();
        }
        int ssx = center ? (sx + (width - swidth) / 2) : sx;
        for (int ii = 0; ii < count; ii++) {
            gfx.drawImage(_star, ssx, sy, null);
            ssx += dx;
        }
    }

    protected int _stars;
    protected Player _player;
    protected Label _plabel;
    protected int _plx, _ply;

    protected static BufferedImage _star;

    protected static final int GAP = 5;
}
