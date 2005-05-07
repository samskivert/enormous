//
// $Id$

package com.samskivert.enormous;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.samskivert.swing.Controller;
import com.threerings.util.RandomUtil;
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
        _teams = new Team[EnormousConfig.getTeamCount()];
        for (int ii = 0; ii < _teams.length; ii++) {
            _teams[ii] = new Team();
        }
    }

    public void setActivePlayer (int teamIndex, String name)
    {
        Team team = _teams[teamIndex];
        String oactive = (team.active == null) ? null : team.active.name;

        // check for an existing player record
        Player active = null;
        for (Player p : team.players) {
            if (p.name.equalsIgnoreCase(name)) {
                active = p;
                break;
            }
        }
        if (active == null) {
            active = new Player(teamIndex, name);
            team.players.add(active);
        }
        team.active = active;
        // only reset the score if the player actually changed
        if (!active.name.equals(oactive)) {
            active.score = 0;
        }
        _panel.getTeamSprite(teamIndex).setPlayer(active);
        _panel.clearTeamConfig();
    }

    public void configureTeams (TeamSprite[] sprites)
    {
        for (int ii = 0; ii < _teams.length; ii++) {
            if (_teams[ii].active != null) {
                sprites[ii].setPlayer(_teams[ii].active);
            }
            sprites[ii].setStars(_teams[ii].stars);
        }
    }

    public void cashInPoints (int teamIndex)
    {
        Team team = _teams[teamIndex];
        if (team.active == null) {
            return;
        }
        if (team.active.score < 6) {
            return;
        }
        int stars = (team.active.score - 3) / 3;
        team.active.score = team.active.score % 3;
        team.active.stars += stars;
        team.stars += stars;
        _panel.getTeamSprite(teamIndex).setPlayer(team.active);
        _panel.getTeamSprite(teamIndex).setStars(team.stars);
    }

    /**
     * Called when a player clicks their buzzer in response to a question.
     */
    public void playerResponded (int teamIndex)
    {
        // ignore subsequent "clicks" while we've got an active responder
        if (_responder != -1) {
            System.err.println(_teams[_responder].active +
                               " is already responding.");
            return;
        }

        if (_teams[teamIndex].active == null) {
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
    public void answerWasCorrect (int round, int catidx, int qidx)
    {
        if (_responder == -1 || _teams[_responder].active == null) {
            System.err.println("No active responder.");
            return;
        }
        final int responder = _responder;
        _responder = -1;

        final int points = _bonus +
            EnormousConfig.getQuestionScore(round, catidx, qidx);
        _panel.replaceQuestion("Correct!");
        new Interval(EnormousApp.queue) {
            public void expired () {
                // score points for the active player
                _teams[responder].active.score += points;
                _panel.getTeamSprite(responder).setPlayer(
                    _teams[responder].active);
                _panel.dismissQuestion(true);
            }
        }.schedule(1000l);
    }

    /**
     * Called when the MC clicks 'n' after a player has responded.
     */
    public void answerWasIncorrect (int round, int catidx, int qidx)
    {
        if (_responder == -1 || _teams[_responder].active == null) {
            System.err.println("No active responder.");
            return;
        }
        final int responder = _responder;
        _responder = -1;

        final int points = EnormousConfig.getQuestionScore(round, catidx, qidx);
        _panel.replaceQuestion("Bzzzzzt!");
        new Interval(EnormousApp.queue) {
            public void expired () {
                // deduct points for the active player
                _teams[responder].active.score -= points;
                _panel.getTeamSprite(responder).setPlayer(
                    _teams[responder].active);
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

            // clear out any unclaimed bonus
            if (bits.length == 3) {
                _bonus = 0;
            }

            // randomly during a round, we pop up a special alarm instead
            // of immediately going to a new question
            int[] aweights = EnormousConfig.getAlarmWeights();
            if (bits.length == 3 && aweights.length > 0 && _questions > 1 &&
                RandomUtil.getInt(100) < EnormousConfig.getAlarmFrequency()) {
                int aidx = RandomUtil.getWeightedIndex(aweights);
                String text = EnormousConfig.getAlarmText(aidx);
                _bonus = EnormousConfig.getAlarmBonus(aidx);
                if (_bonus > 0) {
                    text += "\nNext question bonus: " + _bonus;
                }
                String acmd = (_bonus > 0) ? (cmd + ":noalarm") : "dismiss";
                _panel.displayAlarm(text, acmd);
                _questions = 0;

            } else {
                try {
                    int catidx = Integer.parseInt(bits[1]);
                    int qidx = Integer.parseInt(bits[2]);
                    _panel.displayQuestion(catidx, qidx);
                    _questions++;
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }

        } else if (cmd.startsWith("team:")) {
            String[] bits = StringUtil.split(cmd, ":");
            try {
                int tidx = Integer.parseInt(bits[1]);
                if ((action.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                    cashInPoints(tidx);
                } else {
                    _panel.showTeamConfig(tidx, _teams[tidx].active);
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        } else if (cmd.equals("end_round")) {
            if ((action.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                _panel.endRound();
            }

        } else if (cmd.equals("next_round")) {
            _panel.setRound(_panel._round+1);
            _questions = 0;

        } else if (cmd.equals("dismiss")) {
            _panel.dismissQuestion(false);

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    protected EnormousPanel _panel;
    protected Team[] _teams;
    protected int _responder = -1;
    protected int _questions = 0;
    protected int _bonus;
}
