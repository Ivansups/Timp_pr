package com.bank.ui.screens;

import com.bank.ui.Palette;
import com.bank.ui.components.CardPanel;
import com.bank.ui.components.GradientPanel;
import com.bank.ui.components.PrimaryButton;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class LoginScreen extends GradientPanel {
    private final JTextField userField = new JTextField(16);
    private final JPasswordField passField = new JPasswordField(16);
    private final JLabel errorLabel = new JLabel(" ");

    public LoginScreen(BiConsumer<String, String> onLogin) {
        setLayout(new GridBagLayout());

        CardPanel card = new CardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(560, 400));

        JLabel title = new JLabel("Добро пожаловать");
        title.setForeground(Palette.TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Войдите как user/pass или admin/admin");
        subtitle.setForeground(Palette.TEXT_MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 16f));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel.setForeground(Color.PINK);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        styleField(userField);
        styleField(passField);

        PrimaryButton loginBtn = new PrimaryButton("Войти");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(400, 52));
        loginBtn.addActionListener(e -> onLogin.accept(userField.getText().trim(), new String(passField.getPassword())));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(labeled("Логин", userField));
        form.add(Box.createVerticalStrut(12));
        form.add(labeled("Пароль", passField));
        form.add(Box.createVerticalStrut(18));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLabel);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(16));
        card.add(form);
        card.add(Box.createVerticalGlue());

        add(card, new GridBagConstraints());
    }

    public void showError(String text) {
        errorLabel.setText(text);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    private JPanel labeled(String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setForeground(Palette.TEXT_MUTED);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 15f));
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(l);
        wrapper.add(Box.createVerticalStrut(6));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        wrapper.add(field);
        return wrapper;
    }

    private void styleField(JTextField field) {
        field.setOpaque(true);
        field.setBackground(new Color(245, 243, 255));
        field.setForeground(new Color(20, 18, 35));
        field.setCaretColor(new Color(50, 40, 90));
        field.setSelectionColor(new Color(123, 92, 255, 120));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Palette.ACCENT, 2),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        field.setFont(field.getFont().deriveFont(Font.PLAIN, 18f));
        field.setPreferredSize(new Dimension(0, 48));
    }
}

