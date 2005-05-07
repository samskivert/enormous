//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;

import com.samskivert.swing.Label;

/**
 * Displays the status in between rounds.
 */
public class InterRoundSprite extends SausageSprite
{
    public InterRoundSprite (int width, int height, Team[] teams, int round,
                             Font font, Color bgcolor)
    {
        super(width, height, "End of round " + (round+1),
              font, bgcolor, "next_round");

        _teams = teams;
        _colwidth = (width - 2*WBORDER - (teams.length-1) * BORDER) /
            teams.length;
        _labels = new Label[teams.length][];

        for (int ii = 0; ii < teams.length; ii++) {
            _labels[ii] = new Label[teams[ii].players.size() + 1];
            _labels[ii][0] = makeLabel(EnormousConfig.getTeamName(ii));
            _labels[ii][0].setAlignment(Label.CENTER);
            _labels[ii][0].setTextColor(EnormousConfig.getTeamColor(ii));
            Collections.sort(teams[ii].players);
            for (int pp = 0; pp < teams[ii].players.size(); pp++) {
                Player p = teams[ii].players.get(pp);
                _labels[ii][pp+1] = makeLabel(p.name);
            }
        }
    }

    protected Label makeLabel (String text)
    {
        Label label = new Label(text);
        label.setTextColor(Color.white);
        label.setFont(EnormousConfig.teamFont);
        label.setTargetWidth(_colwidth);
        return label;
    }

    protected void layoutLabel (Graphics2D gfx)
    {
        super.layoutLabel(gfx);

        _ly = BORDER;
        for (int ii = 0; ii < _labels.length; ii++) {
            for (int ll = 0; ll < _labels[ii].length; ll++) {
                _labels[ii][ll].layout(gfx);
            }
        }
    }

    protected void renderLabel (Graphics2D gfx)
    {
        super.renderLabel(gfx);

        int cx = WBORDER;
        for (int ii = 0; ii < _labels.length; ii++) {
            int sy = _ly + _label.getSize().height + BORDER;
            for (int ll = 0; ll < _labels[ii].length; ll++) {
                Label label = _labels[ii][ll];
                label.render(gfx, _bounds.x + cx, _bounds.y + sy);
                if (ll != 0) {
                    int stars = _teams[ii].players.get(ll-1).stars;
                    if (stars > 0) {
                        int sx = _bounds.x + cx + label.getSize().width + 5;
                        int width = _colwidth - label.getSize().width - 10;
                        TeamSprite.drawStars(gfx, width, sx,
                                             _bounds.y + sy - 2, stars, false);
                    }
                }
                sy += (label.getSize().height + BORDER);
            }
            cx += _colwidth + BORDER;
        }
    }

    protected Team[] _teams;
    protected int _colwidth;
    protected Label[][] _labels;

    protected static int WBORDER = 50;
}
