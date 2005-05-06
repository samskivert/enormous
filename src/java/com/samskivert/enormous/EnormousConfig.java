//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Font;

import com.samskivert.util.Config;

/**
 * Provides information on the game configuration.
 */
public class EnormousConfig
{
    /** Provides access to configuration data. */
    public static Config config = new Config("enormous");

    /** The font used to display category selection text. */
    public static Font categoryFont;

    /** The font used to display question text. */
    public static Font questionFont;

    /** The font used to display the team display. */
    public static Font teamFont;

    /** The background of the whole screen. */
    public static Color backgroundColor = Color.blue;

    /** The background of the categories on the main display. */
    public static Color categoryColor = Color.red;

    /** The background of the questions on the main display. */
    public static Color questionColor = new Color(0x99CC66);

    /** The background of the questions on the main display. */
    public static Color seenQuestionColor = new Color(0x336600);

    /**
     * Configures our various bits based on the size of the screen.
     */
    public static void init (int width, int height)
    {
        categoryFont = new Font("Helvetica", Font.BOLD, 18);
        questionFont = new Font("Helvetica", Font.BOLD, 48);
        teamFont = new Font("Helvetica", Font.BOLD, 18);
    }

    /**
     * Returns the number of teams.
     */
    public static int getTeamCount ()
    {
        return 3;
    }

    /**
     * Returns the name of the specified time.
     */
    public static String getTeamName (int teamIdx)
    {
        switch (teamIdx) {
        case 0: return "Red";
        case 1: return "White";
        case 2: return "Blue";
        }
        return "???";
    }

    /**
     * Returns the background color for the specified team.
     */
    public static Color getTeamColor (int teamIdx)
    {
        switch (teamIdx) {
        case 0: return Color.red;
        case 1: return Color.white;
        case 2: return Color.blue;
        }
        return Color.black;
    }

    /**
     * Returns the number of categories in a particular round.
     */
    public static int getCategoryCount (int round)
    {
        return 5;
    }

    /**
     * Returns the number of questions in each category in the specified
     * round.
     */
    public static int getQuestionCount (int round)
    {
        return 4;
    }

    /**
     * Returns the name of the specified category in the specified round.
     */
    public static String getCategory (int round, int index)
    {
        switch (index) {
        case 0: return "American History";
        case 1: return "Farmyard Fun";
        case 2: return "Famous Bald People";
        case 3: return "Huns Across America";
        case 4: return "Art Haus";
        }
        return "category:" + round + ":" + index;
    }

    /**
     * Returns the name of the specified question.
     */
    public static String getQuestionName (int round, int catidx, int questidx)
    {
        switch (questidx) {
        case 0: return "Small";
        case 1: return "Medium";
        case 2: return "Large";
        case 3: return "ENORMOUS!";
        }
        return "???";
    }

    /**
     * Returns the type of the specified question.
     */
    public static String getQuestionType (int round, int catidx, int questidx)
    {
        return "normal";
    }

    /**
     * Returns the text of the specified question.
     */
    public static String getQuestion (int round, int catidx, int questidx)
    {
        return "What is the airspeed velocity of an unladen swallow?";
    }
}
