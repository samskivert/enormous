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
import java.util.List;
import java.util.Properties;

import com.google.common.collect.Lists;

import com.samskivert.util.Config;

/**
 * Provides information on the game configuration.
 */
public class EnormousConfig
{
    public static class Alarm
    {
        public final int index;
        public final String text;
        public final int bonus;
        public final int weight;
        public final int minPostChangeQs;

        public Alarm (int index, String text, int bonus, int weight, int minPostChangeQs) {
            this.index = index;
            this.text = text;
            this.bonus = bonus;
            this.weight = weight;
            this.minPostChangeQs = minPostChangeQs;
        }
    }

    /** Provides access to configuration data. */
    public static Config config;

    /** Provides access to question data. */
    public static Config questions;

    /** The font used to display category selection text. */
    public static Font categoryFont;

    /** The font used to display question text. */
    public static Font questionFont;

    /** The font used to display the team display. */
    public static Font teamFont;

    /** The background of the whole screen. */
    public static Color backgroundColor = new Color(0xAABBCC);

    /** The background of the categories on the main display. */
    public static Color categoryColor = new Color(176, 22, 28);

    /** The background of the questions on the main display. */
    public static Color questionColor = new Color(0x1B2C4A);

    /** The background of the questions on the main display. */
    public static Color seenQuestionColor = new Color(0x3B4C6A);

    static {
        // load up our configuration
        config = makeConfig("enormous");
        questions = makeConfig("questions");
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

    public static File getResourceFile (String path)
    {
        return new File(getPath(path));
    }

    protected static String getPath (String path)
    {
        File home = new File(System.getProperty("user.home"));
        File desktop = new File(home, "Desktop");
        File enormous = new File(desktop, "enormous");
        if (enormous.exists()) {
            return new File(enormous, path).getPath();
        }

        File cwd = new File(System.getProperty("user.dir"));
        File sample = new File(cwd, "sample");
        if (sample.exists()) {
            return new File(sample, path).getPath();
        }

        if (desktop.exists()) {
            return new File(desktop, path).getPath();
        } else {
            return new File(home, path).getPath();
        }
    }

    public static boolean fullScreen () {
        return config.getValue("full_screen", false);
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
     * Returns the sound file to be played when the specified team responds.
     */
    public static String getTeamSound (int teamIdx)
    {
        return config.getValue("team_sound." + teamIdx, "");
    }

    /**
     * Returns the sound file of the specified type.
     */
    public static String getSound (String type)
    {
        if (type.indexOf("sound") == -1) {
            type = type + "_sound";
        }
        return config.getValue(type, "");
    }

    /**
     * Returns the background color for the specified team.
     */
    public static Color getTeamColor (int teamIdx)
    {
        String colstr = config.getValue("team_color." + teamIdx, "#FFCC99");
        try {
            return new Color(Integer.parseInt(colstr.substring(1), 16));
        } catch (Exception e) {
            System.err.println("Invalid team color " + teamIdx + ": " + colstr);
            return Color.blue;
        }
    }

    /**
     * Returns the background image for the specified team.
     */
    public static String getTeamImage (int teamIdx)
    {
        return config.getValue("team_image." + teamIdx, (String)null);
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
     * Returns the number millis seconds after which we play the timeout warning sound.
     */
    public static long getQuestionWarnTimer ()
    {
        return config.getValue("question_warn_timer", 40) * 1000L;
    }

    /**
     * Returns the number of millis after which a question times out.
     */
    public static long getQuestionCancelTimer ()
    {
        return config.getValue("question_cancel_timer", 60) * 1000L;
    }

    /**
     * Returns whether or not we auto-dismiss the question when canceled.
     */
    public static boolean getQuestionCancelDismis ()
    {
        return config.getValue("question_cancel_dismiss", true);
    }

    /**
     * Returns the number of millis after which an answer times out.
     */
    public static long getAnswerCancelTimer ()
    {
        return config.getValue("answer_cancel_timer", 20) * 1000L;
    }

    /**
     * Returns an array indicating how many points are required to get certain numbers of stars.
     */
    public static int[] getStarPoints ()
    {
        return config.getValue("star_points", new int[] { 6, 9, 12, 15, 18 });
    }

    /**
     * Returns the name of the specified category in the specified round.
     */
    public static String getCategory (int round, int catidx)
    {
        return questions.getValue("category_name." + round + "." + catidx,
                                  "category." + round + "." + catidx);
    }

    /**
     * Returns true if we have a value defined for the specified category.
     */
    public static boolean haveCategory (int round, int catidx)
    {
        String key = "category_name." + round + "." + catidx;
        return questions.getValue(key, (String)null) != null;
    }

    /**
     * Returns the background image for the specified category in the specified
     * round.
     */
    public static String getCategoryImage (int round, int index)
    {
        String defimg = config.getValue("category_image", (String)null);
        defimg = questions.getValue("category_image." + index, defimg);
        return questions.getValue("category_image." + round + "." + index, defimg);
    }

    /**
     * Returns the name of the specified question.
     */
    public static String getQuestionName (int round, int catidx, int questidx)
    {
        return config.getValue("question_name." + questidx, "question." + questidx);
    }

    /**
     * Returns the file associated with the specified question.
     */
    public static String getQuestionFile (int round, int catidx, int questidx)
    {
        String key = "question_file." + round + "." + catidx + "." + questidx;
        return questions.getValue(key, "");
    }

    /**
     * Returns the score for the specified question.
     */
    public static int getQuestionScore (int round, int catidx, int questidx)
    {
        // look for a custom score for this question
        int score = questions.getValue(
            "question_score." + round + "." + catidx + "." + questidx, -1);
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
        String def = "Oh noez! This question is missing!";
        return questions.getValue(key, def);
    }

    /**
     * Returns true if we have a value defined for the specified question.
     */
    public static boolean haveQuestion (int round, int catidx, int questidx)
    {
        String key = "question." + round + "." + catidx + "." + questidx;
        return questions.getValue(key, (String)null) != null;
    }

    /**
     * Returns the number of times out of 100 that an alarm should go off.
     */
    public static int getAlarmFrequency ()
    {
        return config.getValue("alarm_freq", 15);
    }

    /**
     * Returns the alarms that are available.
     */
    public static List<Alarm> getAlarms ()
    {
        List<Alarm> alarms = Lists.newArrayList();
        String text;
        for (int ii = 0; (text = config.getValue("alarm_text." + ii, (String)null)) != null; ii++) {
            alarms.add(new Alarm(ii, text, config.getValue("alarm_bonus." + ii, 0),
                                 config.getValue("alarm_weight." + ii, 1),
                                 config.getValue("alarm_changeqs." + ii, 1)));
        }
        return alarms;
    }

    /**
     * Returns the path to the audio file that should be played when the
     * specified function key is pressed, or null.
     */
    public static String getAudioFile (String fkey)
    {
        return config.getValue("audio_file." + fkey, (String)null);
    }

    protected static Config makeConfig (String propsId)
    {
        Properties props = new Properties();
        String path = getPath(propsId + ".properties");
        try {
            props.load(new FileInputStream(path));
        } catch (IOException ioe) {
            System.err.println("Error loading '" + path + "': " + ioe);
        }
        return new Config(props);
    }
}
