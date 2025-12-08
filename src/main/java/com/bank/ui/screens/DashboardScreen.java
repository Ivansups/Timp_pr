package com.bank.ui.screens;

import com.bank.ui.Palette;
import com.bank.ui.components.CardPanel;
import com.bank.ui.components.SectionHeader;
import com.bank.ui.components.StatPill;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;

public class DashboardScreen extends JPanel {
    public DashboardScreen() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(840, 520));

        SectionHeader header = new SectionHeader("Дашборд банка");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 26f));
        JLabel subtitle = new JLabel("Быстрый обзор продуктов и атмосферы банка");
        subtitle.setForeground(Palette.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));

        JLabel tagline = new JLabel("Лёгкость, скорость и прозрачность — без лишних действий.");
        tagline.setForeground(Palette.TEXT_PRIMARY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        tagline.setFont(tagline.getFont().deriveFont(Font.BOLD, 18f));

        JTextArea bullets = new JTextArea("""
                • Пополнение, снятие и переводы выполняются мгновенно.
                • Прозрачные балансы — никаких скрытых комиссий.
                • Интуитивный интерфейс: действия в один клик.
                • Фиолетовая палитра для ощущения премиальности и спокойствия.
                • Поддержка 24/7, безопасность данных в SQLite.
                """);
        bullets.setWrapStyleWord(true);
        bullets.setLineWrap(true);
        bullets.setOpaque(false);
        bullets.setEditable(false);
        bullets.setForeground(Palette.TEXT_PRIMARY);
        bullets.setFont(bullets.getFont().deriveFont(Font.PLAIN, 15f));
        bullets.setBorder(BorderFactory.createEmptyBorder(12, 10, 0, 10));

        card.add(Box.createVerticalStrut(8));
        card.add(header);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(tagline);
        card.add(Box.createVerticalStrut(12));
        card.add(bullets);
        card.add(Box.createVerticalGlue());

        add(card, new GridBagConstraints());
    }

    public void refresh(int accounts, BigDecimal sum) {
        // no-op: метрики скрыты от пользователя
    }
}

