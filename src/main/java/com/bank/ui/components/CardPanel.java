package com.bank.ui.components;

import com.bank.ui.Palette;

import javax.swing.*;
import java.awt.*;

public class CardPanel extends JPanel {
    public CardPanel(LayoutManager layout) {
        super(layout);
        configure();
    }

    public CardPanel() {
        super();
        configure();
    }

    private void configure() {
        setBackground(Palette.CARD);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        setOpaque(true);
    }
}

