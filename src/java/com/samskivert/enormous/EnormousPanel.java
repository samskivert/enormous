//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

import com.threerings.media.FrameManager;
import com.threerings.media.MediaPanel;

/**
 * Manages the main game display.
 */
public class EnormousPanel extends MediaPanel
    implements ControllerProvider
{
    public EnormousPanel (FrameManager fmgr)
    {
        super(fmgr);
        _ctrl = new EnormousController(this);
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        if (_round == 0) {
            // queue up an event to start round one when the AWT is done
            // with its initialization business
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    setRound(0);
                }
            });
        }
    }

    /**
     * Configures the display for a particular round.
     */
    public void setRound (int round)
    {
        // clear all old sprites
        clearSprites();

        // note the round
        _round = round;

        // compute some metrics for the round
        int catcount = EnormousConfig.getCategoryCount(_round);
        int qcount = EnormousConfig.getQuestionCount(_round);
        int colwidth = (getWidth() - GAP) / catcount - GAP;
        int rowheight = (getHeight() - 100 - 150) / qcount - GAP;

        // now create category and question sprites for the round
        int cx = GAP;
        for (int cc = 0; cc < catcount; cc++) {
            String ctitle = EnormousConfig.getCategory(_round, cc);
            SausageSprite cat = new SausageSprite(
                colwidth, 100, ctitle, EnormousConfig.categoryFont,
                EnormousConfig.categoryColor);
            cat.setLocation(cx, GAP);
            addSprite(cat);
            cx += (colwidth + GAP);
        }

        int cy = GAP + 100 + GAP;
        for (int qq = 0; qq < qcount; qq++) {
            cx = GAP;
            for (int cc = 0; cc < catcount; cc++) {
                String qname = EnormousConfig.getQuestionName(_round, cc, qq);
                SausageSprite qsprite = new SausageSprite(
                    colwidth, rowheight, qname, EnormousConfig.categoryFont,
                    EnormousConfig.questionColor);
                qsprite.setLocation(cx, cy);
                addSprite(qsprite);
                cx += (colwidth + GAP);
            }
            cy += (rowheight + GAP);
        }
    }

    // documentation inherited from interface ControllerProvider
    public Controller getController ()
    {
        return _ctrl;
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);
        gfx.setColor(Color.blue);
        gfx.fill(dirtyRect);
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(800, 600);
    }

    protected int _round;
    protected EnormousController _ctrl;

    protected static final int GAP = 25;
}
