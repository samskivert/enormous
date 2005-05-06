//
// $Id$

package com.samskivert.enormous;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.samskivert.swing.Controller;
import com.samskivert.util.Interval;
import com.samskivert.util.StringUtil;

/**
 * Manages the flow of the game.
 */
public class EnormousController extends Controller
{
    public EnormousController (EnormousPanel panel)
    {
        _panel = panel;

        // create our team arrays
        _teams = new PlayerList[EnormousConfig.getTeamCount()];
        for (int ii = 0; ii < _teams.length; ii++) {
            _teams[ii] = new PlayerList();
        }
        _active = new Player[_teams.length];
    }

    public void setActivePlayer (int teamIndex, String name)
    {
        // check for an existing player record
        Player active = null;
        for (Player p : _teams[teamIndex]) {
            if (p.name.equalsIgnoreCase(name)) {
                active = p;
                break;
            }
        }
        if (active == null) {
            active = new Player(teamIndex, name);
            _teams[teamIndex].add(active);
        }
        _active[teamIndex] = active;
        active.score = 0;
        _panel.getTeamSprite(teamIndex).setPlayer(active);
    }

    /**
     * Called when a player clicks their buzzer in response to a question.
     */
    public void playerResponded (int teamIndex)
    {
        // ignore subsequent "clicks" while we've got an active responder
        if (_responder != -1) {
            System.err.println(_active[_responder] + " is already responding.");
            return;
        }

        if (_active[teamIndex] == null) {
            System.err.println("No active player! [tidx=" + teamIndex + "].");
            return;
        }

        // highlight only this team in the display
        _responder = teamIndex;
        _panel.highlightTeam(_responder);
    }

    /**
     * Called when the MC clicks 'y' after a player has responded.
     */
    public void answerWasCorrect ()
    {
        if (_responder == -1 || _active[_responder] == null) {
            System.err.println("No active responder.");
            return;
        }

        _panel.getQuestionSprite().setText("Correct!");
        new Interval(EnormousApp.queue) {
            public void expired () {
                // score a point for the active player
                _active[_responder].score++;
                _panel.getTeamSprite(_responder).setPlayer(_active[_responder]);
                _responder = -1;
                _panel.dismissQuestion();
            }
        }.schedule(1000l);
    }

    /**
     * Called when the MC clicks 'n' after a player has responded.
     */
    public void answerWasIncorrect ()
    {
        if (_responder == -1 || _active[_responder] == null) {
            System.err.println("No active responder.");
            return;
        }

        _panel.getQuestionSprite().setText("Bzzzzzt!");
        new Interval(EnormousApp.queue) {
            public void expired () {
                // score lose points for the active player
                _active[_responder].score--;
                _panel.getTeamSprite(_responder).setPlayer(_active[_responder]);
                _responder = -1;
                _panel.showAllTeams();
                _panel.restoreQuestion();
            }
        }.schedule(1000l);
    }

    /**
     * Called when the MC clicks 'c' after a player has responded.
     */
    public void answerWasCanceled ()
    {
        _responder = -1;
        _panel.restoreQuestion();
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        String cmd = action.getActionCommand();
        if (cmd.startsWith("question:")) {
            String[] bits = StringUtil.split(cmd, ":");
            try {
                int catidx = Integer.parseInt(bits[1]);
                int qidx = Integer.parseInt(bits[2]);
                _panel.displayQuestion(catidx, qidx);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        } else if (cmd.startsWith("team:")) {
            String[] bits = StringUtil.split(cmd, ":");
            try {
                int tidx = Integer.parseInt(bits[1]);
                _panel.showTeamConfig(tidx, _active[tidx]);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        } else if (cmd.startsWith("dismiss_config")) {
            _panel.clearTeamConfig();

        } else if (cmd.equals("dismiss")) {
            _panel.dismissQuestion();

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    protected static class PlayerList extends ArrayList<Player>
    {
    }

    protected EnormousPanel _panel;
    protected PlayerList[] _teams;
    protected Player[] _active;
    protected int _responder = -1;
}
