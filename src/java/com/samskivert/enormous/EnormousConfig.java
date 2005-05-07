//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.samskivert.util.Config;

/**
 * Provides information on the game configuration.
 */
public class EnormousConfig
{
    /** Provides access to configuration data. */
    public static Config config;

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

    static {
        // load up our configuration
        Properties props = new Properties();
        String path = getPath("enormous.properties");
        try {
            props.load(new FileInputStream(path));
        } catch (IOException ioe) {
            System.err.println("Error loading '" + path + "': " + ioe);
        }
        config = new Config("enormous", props);
    }

    /**
     * Configures our various bits based on the size of the screen.
     */
    public static void init (int width, int height)
    {
        categoryFont = new Font("Helvetica", Font.BOLD, 18);
        questionFont = new Font("Helvetica", Font.BOLD, 48);
        teamFont = new Font("Helvetica", Font.BOLD, 18);
    }

    public static InputStream getResourceAsStream (String path)
    {
        path = getPath(path);
        try {
            return new FileInputStream(path);
        } catch (FileNotFoundException fnfe) {
            System.err.println("No such file '" + path + "'.");
            return null;
        }
    }

    public static URL getResource (String path)
    {
        path = getPath(path);
        try {
            return new URL("file://" + path);
        } catch (MalformedURLException mue) {
            System.err.println("Invalid path '" + path + "'.");
            return null;
        }
    }

    protected static String getPath (String path)
    {
        File home = new File(System.getProperty("user.home"));
        File desktop = new File(home, "Desktop");
        File enormous = new File(desktop, "enormous");
        if (enormous.exists()) {
            return new File(enormous, path).getPath();
        } else if (desktop.exists()) {
            return new File(desktop, path).getPath();
        } else {
            return new File(home, path).getPath();
        }
    }

    /**
     * Returns the number of teams.
     */
    public static int getTeamCount ()
    {
        return config.getValue("team_count", 3);
    }

    /**
     * Returns the name of the specified time.
     */
    public static String getTeamName (int teamIdx)
    {
        return config.getValue("team_name." + teamIdx, "team_" + teamIdx);
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
        return config.getValue("category_count", 5);
    }

    /**
     * Returns the number of questions in each category in the specified
     * round.
     */
    public static int getQuestionCount (int round)
    {
        return config.getValue("question_count", 4);
    }

    /**
     * Returns the name of the specified category in the specified round.
     */
    public static String getCategory (int round, int index)
    {
        return config.getValue("category_name." + round + "." + index,
                               "category." + round + "." + index);
    }

    /**
     * Returns the name of the specified question.
     */
    public static String getQuestionName (int round, int catidx, int questidx)
    {
        return config.getValue("question_name." + questidx,
                               "question." + questidx);
    }

    /**
     * Returns the file associated with the specified question.
     */
    public static String getQuestionFile (int round, int catidx, int questidx)
    {
        String key = "question_file." + round + "." + catidx + "." + questidx;
        return config.getValue(key, "");
    }

    /**
     * Returns the score for the specified question.
     */
    public static int getQuestionScore (int round, int catidx, int questidx)
    {
        // look for a custom score for this question
        int score = config.getValue("question_score." + round + "." +
                                    catidx + "." + questidx, -1);
        if (score == -1) {
            // fall back to the default score for a question of this size
            score = config.getValue("question_score." + questidx, 1);
        }
        return score;
    }

    /**
     * Returns the text of the specified question.
     */
    public static String getQuestion (int round, int catidx, int questidx)
    {
        String key = "question." + round + "." + catidx + "." + questidx;
        String def = "What is the airspeed velocity of an unladen swallow?";
        return config.getValue(key, def);
    }

    /**
     * Returns the number of times out of 100 that an alarm should go off.
     */
    public static int getAlarmFrequency ()
    {
        return config.getValue("alarm_freq", 15);
    }

    /**
     * Returns the number of different alarms that are available.
     */
    public static int[] getAlarmWeights ()
    {
        return config.getValue("alarm_weights", new int[0]);
    }

    /**
     * Returns the number of different alarms that are available.
     */
    public static String getAlarmText (int alarm)
    {
        return config.getValue("alarm_text." + alarm, "Food Fight!");
    }

    /**
     * Returns the number of bonus points associated with the question
     * following the alarm, if any.
     */
    public static int getAlarmBonus (int alarm)
    {
        return config.getValue("alarm_bonus." + alarm, 0);
    }
}
