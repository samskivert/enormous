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
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.applet.Applet;
import java.applet.AudioClip;

import java.io.IOException;
import java.net.URL;

import java.util.HashMap;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.util.Interval;
import com.samskivert.util.StringUtil;

import com.threerings.media.FrameManager;
import com.threerings.media.MediaPanel;
import com.threerings.media.image.BufferedMirage;
import com.threerings.media.sprite.ImageSprite;
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
        _ctrl.configureTeams(_tsprites);

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
                EnormousConfig.categoryColor, "end_round");
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

        // if we have a round sprite, add it back and then scroll it off
        // the screen
        if (_rsprite == null) {
            return;
        }
        addSprite(_rsprite);
        _rsprite.addSpriteObserver(new PathAdapter() {
            public void pathCompleted (Sprite sprite, Path path, long when) {
                removeSprite(sprite);
            }
        });
        _rsprite.move(new LinePath(new Point(GAP, -getHeight()), 500L));
        _rsprite = null;
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
     * Displays the specified alarm.
     */
    public void displayAlarm (String text, String command)
    {
        int width = getWidth() - 2*GAP;
        int height = getHeight() - HEADER - FOOTER - 4*GAP;
        _qsprite = new SausageSprite(
            width, height, text, EnormousConfig.questionFont,
            EnormousConfig.questionColor, command);
        _qsprite.setRenderOrder(25);
        _qsprite.setLocation(-width, HEADER + 2*GAP);
        _qsprite.move(new LinePath(new Point(GAP, HEADER + 2*GAP), 500L));
        addSprite(_qsprite);
    }

    /**
     * Displays the specified question.
     */
    public void displayQuestion (int catidx, int qidx)
    {
        // no go if we're display a team configuration currently
        if (_tconfig != null) {
            return;
        }

        // dismiss any existing question
        if (_qsprite != null) {
            dismissQuestion(false);
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

        String file = EnormousConfig.getQuestionFile(_round, catidx, qidx);
        if (file.endsWith("jpg") || file.endsWith("png")) {
            displayImage(file);
        } else if (file.endsWith("wav") || file.endsWith("mp3")) {
            playSound(file);
        } else if (!StringUtil.isBlank(file)) {
            System.err.println("Unknown file type '" + file + "'.");
        }
    }

    public void replaceQuestion (String text)
    {
        _qsprite.setText(text);
        if (_isprite != null) {
            // temporarily remove the image sprite
            removeSprite(_isprite);
        }
    }

    public void restoreQuestion ()
    {
        if (_acidx != -1) {
            _qsprite.setText(
                EnormousConfig.getQuestion(_round, _acidx, _aqidx));
        }
        if (_isprite != null && !isManaged(_isprite)) {
            // restore the image sprite
            addSprite(_isprite);
        }
    }

    /**
     * Dismisses the currently displayed question.
     */
    public void dismissQuestion (boolean remove)
    {
        // reenable the category titles
        for (int ii = 0; ii < _catsprites.length; ii++) {
            if (!isManaged(_catsprites[ii])) {
                addSprite(_catsprites[ii]);
            }
        }

        // if we are to remove the original question, do so now
        if (remove && _acidx != -1 && _aqidx != -1) {
            removeSprite(_qsprites.remove(_acidx * 10 + _aqidx));
        }

        // if there is an image visible, dismiss that
        if (_isprite != null) {
            if (isManaged(_isprite)) {
                removeSprite(_isprite);
            }
            _isprite = null;
        }

        // if there is an audio clip playing, stop it
        if (_aclip != null) {
            _aclip.stop();
            _aclip = null;
        }

        // clear the active question and player
        _acidx = _aqidx = -1;
        showAllTeams();

        if (_qsprite == null) {
            return;
        }
        _qsprite.addSpriteObserver(new PathAdapter() {
            public void pathCompleted (Sprite sprite, Path path, long when) {
                removeSprite(sprite);
                if (_qsprites.size() == 0) {
                    endRound();
                }
            }
        });
        _qsprite.move(new LinePath(new Point(getWidth(), 100 + 2*GAP), 500L));
        _qsprite = null;
    }

    public void highlightTeam (int teamIdx)
    {
        for (int ii = 0; ii < _tsprites.length; ii++) {
            if (teamIdx != ii) {
                removeSprite(_tsprites[ii]);
            }
        }
    }

    public void showAllTeams ()
    {
        for (int ii = 0; ii < _tsprites.length; ii++) {
            if (!isManaged(_tsprites[ii])) {
                addSprite(_tsprites[ii]);
            }
        }
    }

    public void showTeamConfig (int teamIdx, Player active)
    {
        if (_tconfig == null && _qsprite == null) {
            removeSprite(_tsprites[teamIdx]);
            _tconfig = new TeamConfigDialog(_ctrl, teamIdx, active);
            _tconfig.setBounds(_tsprites[teamIdx].getBounds());
            EnormousApp.frame.getLayeredPane().add(_tconfig);
            _tconfig.setVisible(true);
        }
    }

    public void clearTeamConfig ()
    {
        if (_tconfig != null) {
            EnormousApp.frame.getLayeredPane().remove(_tconfig);
            EnormousApp.frame.requestFocus();
            _tconfig = null;
            showAllTeams();
            revalidate();
            repaint();
        }
    }

    public void endRound ()
    {
        // slide a giant "end of round" sprite over the whole board
        int width = getWidth() - 2*GAP;
        int height = getHeight() - FOOTER - 3*GAP;
        _rsprite = new InterRoundSprite(
            width, height, _ctrl._teams, _round,
            EnormousConfig.questionFont, EnormousConfig.questionColor);
        _rsprite.setRenderOrder(25);
        _rsprite.setLocation(GAP, -height);
        _rsprite.move(new LinePath(new Point(GAP, GAP), 500L));
        addSprite(_rsprite);

        // play a sound
        _ctrl.playSound("round_over");
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
                _ctrl.answerWasCorrect(_round, _acidx, _aqidx);

            } else if (key == 'n') {
                _ctrl.answerWasIncorrect(_round, _acidx, _aqidx);

            } else if (key == 'c') {
                _ctrl.answerWasCanceled();

            } else if (key >= '1' && key <= '9') {
                int pidx = key - '1';
                if (pidx >= 0 && pidx < _tsprites.length && _aqidx != -1) {
                    _ctrl.playerResponded(pidx);
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
        return new Dimension(1024, 768);
    }

    // documentation inherited
    protected void paintBehind (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBehind(gfx, dirtyRect);
        gfx.setColor(EnormousConfig.backgroundColor);
        gfx.fill(dirtyRect);
    }

    protected void displayImage (String path)
    {
        try {
            BufferedImage image = ImageIO.read(
                EnormousConfig.getResourceAsStream(path));
            _isprite = new ImageSprite(new BufferedMirage(image));
            _isprite.setRenderOrder(50);
            new Interval(EnormousApp.queue) {
                public void expired () {
                    if (_isprite != null) {
                        addSprite(_isprite);
                        int x = (getWidth() - _isprite.getBounds().width)/2;
                        int y = (getHeight() - _isprite.getBounds().height)/2;
                        _isprite.setLocation(x, y);
                    }
                }
            }.schedule(2000L);

        } catch (IOException ioe) {
            System.err.println("Failed to load '" + path + "'.");
            ioe.printStackTrace(System.err);
        }
    }

    protected AudioClip loadSound (String path)
    {
        if (path.endsWith("mp3")) {
            return new MP3Player(EnormousConfig.getResourceAsStream(path));
        } else if (path.endsWith("wav")) {
            return Applet.newAudioClip(EnormousConfig.getResource(path));
        } else {
            return null;
        }
    }

    protected void playSound (String path)
    {
        _aclip = loadSound(path);
        new Interval(EnormousApp.queue) {
            public void expired () {
                if (_aclip != null) {
                    _aclip.play();
                }
            }
        }.schedule(2000L);
    }

    protected EnormousController _ctrl;
    protected int _round = -1;

    protected SausageSprite[] _catsprites;
    protected HashMap<Integer,SausageSprite> _qsprites =
        new HashMap<Integer,SausageSprite>();
    protected TeamSprite[] _tsprites;

    protected SausageSprite _qsprite, _rsprite;
    protected int _acidx = -1, _aqidx = -1;

    protected ImageSprite _isprite;
    protected AudioClip _aclip;
    protected TeamConfigDialog _tconfig;

    protected static final int HEADER = 100;
    protected static final int FOOTER = 100;
    protected static final int GAP = 25;
}
