package com.bank;

import com.bank.ui.Palette;
import com.bank.ui.screens.MainFrame;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            UIManager.put("Component.arc", 16);
            UIManager.put("Button.arc", 16);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("ScrollBar.thumb", Palette.ACCENT);

            MainFrame frame = new MainFrame();
            frame.getContentPane().setBackground(Color.BLACK);
            frame.setVisible(true);
        });
    }
}

