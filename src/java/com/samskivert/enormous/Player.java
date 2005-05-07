//
// $Id$

package com.samskivert.enormous;

/**
 * Represents a particular player on a particular team.
 */
public class Player
    implements Comparable<Player>
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

    public int compareTo (Player other)
    {
        if (stars == other.stars) {
            return name.compareTo(other.name);
        } else {
            return other.stars - stars;
        }
    }
}
