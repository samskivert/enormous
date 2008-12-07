//
// $Id$

package com.samskivert.enormous;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Contains information on a particular team.
 */
public class Team
{
    public int stars;

    public Player active;

    public List<Player> players = Lists.newArrayList();
}
