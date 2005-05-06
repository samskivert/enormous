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

    /** The background of the categories on the main display. */
    public static Color categoryColor = new Color(0x003366);

    /** The background of the questions on the main display. */
    public static Color questionColor = new Color(0xFF9900);

    /**
     * Configures our various bits based on the size of the screen.
     */
    public static void init (int width, int height)
    {
        int catsize = 18;
        categoryFont = new Font("Helvetica", Font.BOLD, catsize);
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
