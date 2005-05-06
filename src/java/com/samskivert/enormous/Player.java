//
// $Id$

package com.samskivert.enormous;

/**
 * Represents a particular player on a particular team.
 */
public class Player
{
    public int teamIndex;

    public String name;

    public int score;

    public int stars;

    public Player (int teamIndex, String name)
    {
        this.teamIndex = teamIndex;
        this.name = name;
    }
}
