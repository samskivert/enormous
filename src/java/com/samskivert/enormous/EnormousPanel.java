//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.HashMap;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.util.Interval;

import com.threerings.media.FrameManager;
import com.threerings.media.MediaPanel;
import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;

/**
 * Manages the main game display.
 */
public class EnormousPanel extends MediaPanel
    implements ControllerProvider, KeyListener
{
    public EnormousPanel (FrameManager fmgr)
    {
        super(fmgr);
        _ctrl = new EnormousController(this);
        fmgr.getFrame().addKeyListener(this);
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        // start the first round if appropriate
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

        // recreate our team sprites
        int width = getWidth() - 2*GAP, height = getHeight();
        int tcount = EnormousConfig.getTeamCount();
        int twidth = (width - (tcount-1)*GAP) / tcount;
        int tx = GAP;
        _tsprites = new TeamSprite[tcount];
        for (int ii = 0; ii < _tsprites.length; ii++) {
            _tsprites[ii] = new TeamSprite(
                twidth, FOOTER, ii, EnormousConfig.teamFont,
                EnormousConfig.getTeamColor(ii));
            _tsprites[ii].setLocation(tx, height - FOOTER - GAP);
            addSprite(_tsprites[ii]);
            tx += (twidth + GAP);
        }

        // compute some metrics for the round
        int catcount = EnormousConfig.getCategoryCount(_round);
        int qcount = EnormousConfig.getQuestionCount(_round);
        int colwidth = (getWidth() - GAP) / catcount - GAP;
        int rowheight = (getHeight() - HEADER - FOOTER - 3*GAP) / qcount - GAP;

        _catsprites = new SausageSprite[catcount];

        // now create category and question sprites for the round
        int cx = GAP;
        for (int cc = 0; cc < catcount; cc++) {
            String ctitle = EnormousConfig.getCategory(_round, cc);
            SausageSprite cat = new SausageSprite(
                colwidth, 100, ctitle, EnormousConfig.categoryFont,
                EnormousConfig.categoryColor, null);
            cat.setLocation(cx, GAP);
            addSprite(cat);
            _catsprites[cc] = cat;
            cx += (colwidth + GAP);
        }

        _qsprites.clear();

        int cy = GAP + 100 + GAP;
        for (int qq = 0; qq < qcount; qq++) {
            cx = GAP;
            for (int cc = 0; cc < catcount; cc++) {
                String qname = EnormousConfig.getQuestionName(_round, cc, qq);
                SausageSprite qsprite = new SausageSprite(
                    colwidth, rowheight, qname, EnormousConfig.categoryFont,
                    EnormousConfig.questionColor, "question:" + cc + ":" + qq);
                qsprite.setLocation(cx, cy);
                _qsprites.put(cc*10+qq, qsprite);
                addSprite(qsprite);
                cx += (colwidth + GAP);
            }
            cy += (rowheight + GAP);
        }
    }

    /**
     * Returns the question sprite in question.
     */
    public SausageSprite getQuestionSprite (int catidx, int qidx)
    {
        return _qsprites.get(catidx * 10 + qidx);
    }

    /**
     * Returns the active question sprite if any.
     */
    public SausageSprite getQuestionSprite ()
    {
        return _qsprite;
    }

    /**
     * Returns the team sprite for the specified team.
     */
    public TeamSprite getTeamSprite (int teamIdx)
    {
        return _tsprites[teamIdx];
    }

    /**
     * Displays the specified question.
     */
    public void displayQuestion (int catidx, int qidx)
    {
        // dismiss any existing question
        if (_qsprite != null) {
            dismissQuestion();
        }

        // note that this is the active question
        _acidx = catidx;
        _aqidx = qidx;

        // mark the question itself as having been seen
        getQuestionSprite(catidx, qidx).setBackground(
            EnormousConfig.seenQuestionColor);

        // display only the category in question
        for (int ii = 0; ii < _catsprites.length; ii++) {
            if (ii != catidx) {
                removeSprite(_catsprites[ii]);
            }
        }

        int width = getWidth() - 2*GAP;
        int height = getHeight() - HEADER - FOOTER - 4*GAP;
        String qtext = EnormousConfig.getQuestion(_round, catidx, qidx);
        _qsprite = new SausageSprite(
            width, height, qtext, EnormousConfig.questionFont,
            EnormousConfig.questionColor, "dismiss");
        _qsprite.setRenderOrder(25);
        _qsprite.setLocation(-width, HEADER + 2*GAP);
        _qsprite.move(new LinePath(new Point(GAP, HEADER + 2*GAP), 500L));
        addSprite(_qsprite);
    }

    /**
     * Dismisses the currently displayed question.
     */
    public void dismissQuestion ()
    {
        // reenable the category titles
        for (int ii = 0; ii < _catsprites.length; ii++) {
            if (!isManaged(_catsprites[ii])) {
                addSprite(_catsprites[ii]);
            }
        }

        // clear the active question and player
        _acidx = _aqidx = -1;
        clearActivePlayer();

        if (_qsprite == null) {
            return;
        }
        _qsprite.addSpriteObserver(new PathAdapter() {
            public void pathCompleted (Sprite sprite, Path path, long when) {
                removeSprite(sprite);
            }
        });
        _qsprite.move(new LinePath(new Point(getWidth(), 100 + 2*GAP), 500L));
        _qsprite = null;
    }

    // documentation inherited from interface ControllerProvider
    public Controller getController ()
    {
        return _ctrl;
    }

    // documentation inherited from interface KeyListener
    public void keyTyped (KeyEvent e)
    {
        // NADA
    }

    // documentation inherited from interface KeyListener
    public void keyPressed (KeyEvent e)
    {
        // if we have a question active, pressing a key indicates that one
        // of the players is responding to the question
        if (_qsprite != null) {
            char key = e.getKeyChar();
            if (key == 'y') {
                _qsprite.setText("Correct!");
                new Interval(EnormousApp.queue) {
                    public void expired () {
                        dismissQuestion();
                    }
                }.schedule(1000l);

            } else if (key == 'n') {
                _qsprite.setText("Bzzzzzt!");
                new Interval(EnormousApp.queue) {
                    public void expired () {
                        clearActivePlayer();
                        if (_acidx != -1) {
                            _qsprite.setText(EnormousConfig.getQuestion(
                                                 _round, _acidx, _aqidx));
                        }
                    }
                }.schedule(1000l);

            } else if (key == 'c') {
                clearActivePlayer();
                if (_acidx != -1) {
                    _qsprite.setText(EnormousConfig.getQuestion(
                                         _round, _acidx, _aqidx));
                }

            } else if (key >= '1' && key <= '9' && _apidx == -1) {
                int pidx = key - '1';
                if (pidx >= 0 && pidx < _tsprites.length) {
                    handlePlayerResponse(pidx);
                }
            }
        }
    }

    // documentation inherited from interface KeyListener
    public void keyReleased (KeyEvent e)
    {
        // NADA
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        return new Dimension(800, 600);
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);
        gfx.setColor(EnormousConfig.backgroundColor);
        gfx.fill(dirtyRect);
    }

    protected void handlePlayerResponse (int pidx)
    {
        // note that this player is the "active" player
        _apidx = pidx;

        // show only the active player's team display
        for (int ii = 0; ii < _tsprites.length; ii++) {
            if (_apidx != ii) {
                removeSprite(_tsprites[ii]);
            }
        }

        // let the controller know
        _ctrl.playerResponded(pidx);
    }

    protected void clearActivePlayer ()
    {
        _apidx = -1;
        for (int ii = 0; ii < _tsprites.length; ii++) {
            if (!isManaged(_tsprites[ii])) {
                addSprite(_tsprites[ii]);
            }
        }
    }

    protected EnormousController _ctrl;
    protected int _round;

    protected SausageSprite[] _catsprites;
    protected HashMap<Integer,SausageSprite> _qsprites =
        new HashMap<Integer,SausageSprite>();
    protected TeamSprite[] _tsprites;

    protected SausageSprite _qsprite;
    protected int _acidx = -1, _aqidx = -1, _apidx = -1;

    protected static final int HEADER = 100;
    protected static final int FOOTER = 100;
    protected static final int GAP = 25;
}
