package com.bank.ui.components;

import com.bank.ui.Palette;

import javax.swing.*;
import java.awt.*;

public class SectionHeader extends JLabel {
    public SectionHeader(String text) {
        super(text);
        setForeground(Palette.TEXT_PRIMARY);
        setFont(getFont().deriveFont(Font.BOLD, 16f));
    }
}

