//
// $Id$

package com.samskivert.enormous;

import java.awt.event.ActionEvent;

import com.samskivert.swing.Controller;
import com.samskivert.util.StringUtil;

/**
 * Manages the flow of the game.
 */
public class EnormousController extends Controller
{
    public EnormousController (EnormousPanel panel)
    {
        _panel = panel;
    }

    /**
     * Called when a player clicks their buzzer in response to a question.
     */
    public void playerResponded (int teamIndex)
    {
        System.err.println("Player " + teamIndex);
        _panel.getQuestionSprite().setText("Player " + (teamIndex+1));
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

        } else if (cmd.equals("dismiss")) {
            _panel.dismissQuestion();

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    protected EnormousPanel _panel;
}
