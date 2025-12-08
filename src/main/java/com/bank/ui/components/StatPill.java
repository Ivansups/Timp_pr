package com.bank.ui.components;

import com.bank.ui.Palette;

import javax.swing.*;
import java.awt.*;

public class StatPill extends JPanel {
    private final JLabel val;

    public StatPill(String label, String value) {
        setLayout(new BorderLayout());
        setBackground(new Color(255, 255, 255, 20));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setOpaque(true);

        JLabel title = new JLabel(label);
        title.setForeground(Palette.TEXT_MUTED);
        title.setFont(title.getFont().deriveFont(Font.PLAIN, 12f));

        val = new JLabel(value);
        val.setForeground(Palette.TEXT_PRIMARY);
        val.setFont(val.getFont().deriveFont(Font.BOLD, 16f));

        add(title, BorderLayout.NORTH);
        add(val, BorderLayout.SOUTH);
    }

    public void setValue(String text) {
        val.setText(text);
    }
}

