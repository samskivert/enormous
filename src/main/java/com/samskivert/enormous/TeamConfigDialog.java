//
// $Id$

package com.samskivert.enormous;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.VGroupLayout;

/**
 * Displays configuration information for a particular team.
 */
public class TeamConfigDialog extends JPanel
{
    public TeamConfigDialog (
        EnormousController ctrl, int teamIdx, Player active)
    {
        setLayout(new VGroupLayout());
        setBackground(EnormousConfig.backgroundColor);

        _ctrl = ctrl;
        _teamIdx = teamIdx;

        JLabel label = new JLabel("Change player:");
        label.setForeground(Color.white);
        add(label);

        String ptext = (active == null) ? "" : active.name;
        add(_text = new JTextField(ptext) {
            public Dimension getPreferredSize () {
                Dimension d = super.getPreferredSize();
                d.width = 150;
                return d;
            }
        });
        _text.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                _ctrl.setActivePlayer(_teamIdx, _text.getText());
            }
        });
    }

    public void addNotify ()
    {
        super.addNotify();
        _text.requestFocus();
    }

    protected EnormousController _ctrl;
    protected int _teamIdx;
    protected JTextField _text;
}
