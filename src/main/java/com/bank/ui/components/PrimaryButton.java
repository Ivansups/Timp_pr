package com.bank.ui.components;

import com.bank.ui.Palette;

import javax.swing.*;
import java.awt.*;

public class PrimaryButton extends JButton {
    public PrimaryButton(String text) {
        super(text);
        setFocusPainted(false);
        setForeground(Palette.TEXT_PRIMARY);
        setBackground(Palette.ACCENT);
        setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        setFont(getFont().deriveFont(Font.BOLD, 14f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}

