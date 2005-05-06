//
// $Id$

package com.samskivert.enormous;

import com.samskivert.swing.Controller;

/**
 * Manages the flow of the game.
 */
public class EnormousController extends Controller
{
    public EnormousController (EnormousPanel panel)
    {
        _panel = panel;
    }

    protected EnormousPanel _panel;
}
