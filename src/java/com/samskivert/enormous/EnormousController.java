//
// $Id$

package com.samskivert.enormous;

import java.applet.AudioClip;
import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.base.Predicate;

import com.samskivert.swing.Controller;
import com.samskivert.util.RandomUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.StringUtil;

import com.samskivert.enormous.EnormousConfig.Alarm;

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

        // load up our team sounds
        for (int ii = 0; ii < _teams.length; ii++) {
            loadSound("team_sound." + ii);
        }

        loadSound("cash_in");
        loadSound("correct");
        loadSound("incorrect");
        loadSound("enorm_correct");
        loadSound("enorm_incorrect");
        loadSound("round_over");
        loadSound("question_warn");
        loadSound("question_cancel");

        for (Alarm alarm : EnormousConfig.getAlarms()) {
            loadSound("alarm_sound." + alarm.index);
        }

        // determine our question timers
        _warner = new Interval(EnormousApp.queue) {
            public void expired () {
                playSound("question_warn");
            }
        };
        _canceler = new Interval(EnormousApp.queue) {
            public void expired () {
                playSound("question_cancel");
                if (EnormousConfig.getQuestionCancelDismis()) {
                    dismissQuestion();
                }
            }
        };
        _answerer = new Interval(EnormousApp.queue) {
            public void expired () {
                _panel.timeoutAnswer();
            }
        };
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
            // clear out our "questions since change" as well
            _changeQs.clear();
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
        int[] spoints = EnormousConfig.getStarPoints();
        if (team.active.score < spoints[0]) {
            return;
        }

        playSound("cash_in");

        int stars = 0;
        while (stars < spoints.length && spoints[stars] <= team.active.score) {
            stars++;
        }
        team.active.score -= spoints[stars-1];
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

        // play the sound associated with this team
        playSound("team_sound." + teamIndex);

        // highlight only this team in the display
        _responder = teamIndex;
        _panel.highlightTeam(_responder);

        // pause our question timers
        pauseQuestionTimers();

        // start the answer timeout
        _answerer.schedule(EnormousConfig.getAnswerCancelTimer());
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

        final int responder = clearResponder();
        boolean enormous = (qidx == EnormousConfig.getQuestionCount(round)-1);
        int qscore = EnormousConfig.getQuestionScore(round, catidx, qidx);
        playSound(enormous ? "enorm_correct" : "correct");

        final int points = qscore + _bonus;
        _panel.replaceQuestion(enormous ? "That's ENORMOUS!" : "Correct!", true);
        new Interval(EnormousApp.queue) {
            public void expired () {
                // score points for the active player
                _teams[responder].active.score += points;
                _panel.getTeamSprite(responder).setPlayer(_teams[responder].active);
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

        final int responder = clearResponder();
        boolean enormous = (qidx == EnormousConfig.getQuestionCount(round)-1);
        final int points = EnormousConfig.getQuestionScore(round, catidx, qidx);
        playSound(enormous ? "enorm_incorrect" : "incorrect");

        _panel.replaceQuestion(enormous ? "That's ENORMOUSly wrong!" : "Bzzzzt!", false);
        new Interval(EnormousApp.queue) {
            public void expired () {
                // deduct points for the active player
                _teams[responder].active.score = Math.max(_teams[responder].active.score-points, 0);
                _panel.getTeamSprite(responder).setPlayer(_teams[responder].active);
                _panel.showAllTeams();
                _panel.restoreQuestion();
                restartQuestionTimers();
            }
        }.schedule(1000l);
    }

    /**
     * Called when the MC clicks 'c' after a player has responded.
     */
    public void answerWasCanceled ()
    {
        clearResponder();
        _panel.restoreQuestion();
        restartQuestionTimers();
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        String cmd = action.getActionCommand();
        if (cmd.startsWith("question:")) {
            String[] bits = StringUtil.split(cmd, ":");
            questionClicked(Integer.parseInt(bits[1]), Integer.parseInt(bits[2]), bits.length > 3);

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

        } else if (cmd.equals("toggle_status")) {
            _panel.toggleRoundStatus();

        } else if (cmd.equals("end_round")) {
            if ((action.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
                _panel.endRound();
            } else {
                _panel.toggleRoundStatus();
            }

        } else if (cmd.equals("next_round")) {
            _panel.setRound(_panel._round+1);
            // clear out our various trackers
            _questions = 0;
            _changeQs.clear();

        } else if (cmd.equals("dismiss")) {
            dismissQuestion();

        } else if (cmd.equals("clear_overlay")) {
            _panel.clearOverlaySprite();

        } else if (cmd.equals("noop")) {
            // they clicked on a cleared out sprite, no problem

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    protected void questionClicked (int catidx, int qidx, boolean noalarm)
    {
        // clear out any unclaimed bonus if we might have a new alarm
        if (!noalarm) {
            _bonus = 0;
        }

        // occasionally, we pop up an alarm instead of immediately going to a new question
        Alarm alarm = noalarm ? null : pickAlarm(catidx, qidx);
        if (alarm != null) {
            String text = alarm.text;
            _bonus = alarm.bonus;
            if (_bonus > 0) {
                text += "\nNext question bonus: " + _bonus;
            }

            // display the alarm
            playSound("alarm_sound." + alarm.index);
            _panel.displayAlarm(text, (_bonus == 0) ? "dismiss" :
                                "question:" + catidx + ":" + qidx + ":noalarm");

            // clear out our "questions since last alarm" counter
            _questions = 0;
            return;
        }

        // display the qeustion
        _panel.displayQuestion(catidx, qidx);

        // increment our count of questions since the last alarm
        _questions++;

        // start the questions timer
        startQuestionTimers(0L);

        // add this question to our "questions since change" set
        _changeQs.add(catidx + ":" + qidx);
    }

    protected Alarm pickAlarm (int catidx, int qidx)
    {
        // filter out alarms that only happen after more post-change questions
        List<Alarm> alarms = Lists.newArrayList(
            Iterables.filter(EnormousConfig.getAlarms(), new Predicate<Alarm>() {
            public boolean apply (Alarm alarm) {
                return alarm.minPostChangeQs < _changeQs.size();
            }
        }));
        if (alarms.isEmpty()) {
            return null;
        }

        int[] weights = new int[alarms.size()];
        for (int ii = 0; ii < weights.length; ii++) {
            weights[ii] = alarms.get(ii).weight;
        }
        return alarms.get(RandomUtil.getWeightedIndex(weights));
    }

    /**
     * Returns the current responder, clears out the tracked responder and cancels our answer
     * timeout.
     */
    protected int clearResponder ()
    {
        int responder = _responder;
        _answerer.cancel();
        _responder = -1;
        return responder;
    }

    protected void dismissQuestion ()
    {
        clearResponder();
        pauseQuestionTimers();
        _panel.dismissQuestion(false);
    }

    protected void loadSound (String name)
    {
        AudioClip clip = _panel.loadSound(EnormousConfig.getSound(name));
        if (clip != null) {
            _clips.put(name, clip);
        } else {
            System.err.println("No sound: " + name);
        }
    }

    protected void playSound (String name)
    {
        AudioClip clip = _clips.get(name);
        if (clip != null) {
            clip.play();
        }
    }

    protected void startQuestionTimers (long elapsed)
    {
        _questionStart = System.currentTimeMillis() - elapsed;
        // don't restart the warning timer if there's not at least a second left
        if (EnormousConfig.getQuestionWarnTimer() - elapsed > 1000L) {
            _warner.schedule(EnormousConfig.getQuestionWarnTimer() - elapsed);
        }
        // always give them one second before canceling
        _canceler.schedule(Math.max(EnormousConfig.getQuestionCancelTimer() - elapsed, 1000L));
    }

    protected void pauseQuestionTimers ()
    {
        _questionPause = System.currentTimeMillis();
        _warner.cancel();
        _canceler.cancel();
    }

    protected void restartQuestionTimers ()
    {
        startQuestionTimers(_questionPause - _questionStart);
    }

    protected EnormousPanel _panel;
    protected Team[] _teams;
    protected Map<String,AudioClip> _clips = Maps.newHashMap();

    /** The set of questions asked since we changed players. */
    protected Set<String> _changeQs = Sets.newHashSet();

    protected int _responder = -1;
    protected int _questions = 0;
    protected int _bonus;

    protected long _questionStart, _questionPause;
    protected Interval _warner, _canceler, _answerer;
}
