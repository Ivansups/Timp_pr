package com.bank.ui.screens;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.ui.Palette;
import com.bank.ui.components.CardPanel;
import com.bank.ui.components.PrimaryButton;
import com.bank.ui.components.SectionHeader;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

public class UserScreen extends JPanel {
    private final AccountService accountService;
    private User user;
    private final DefaultListModel<Account> accountModel = new DefaultListModel<>();
    private final JList<Account> accountList = new JList<>(accountModel);
    private final JComboBox<Account> transferTargets = new JComboBox<>();
    private final JTextField amountField = new JTextField("100.00", 10);
    private final JTextField transferAmountField = new JTextField("50.00", 10);
    private final JLabel feedback = new JLabel(" ");
    private final NumberFormat currency = NumberFormat.getCurrencyInstance();
    private final DefaultComboBoxModel<Account> targetModel = new DefaultComboBoxModel<>();

    public UserScreen(AccountService accountService) {
        this.accountService = accountService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        SectionHeader header = new SectionHeader("Мои счета");
        JLabel subtitle = new JLabel("Пополняйте, снимайте и переводите другим клиентам");
        subtitle.setForeground(Palette.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.SOUTH);

        accountList.setCellRenderer(new AccountRenderer());
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setBackground(Palette.SURFACE);
        accountList.setForeground(Palette.TEXT_PRIMARY);
        accountList.setVisibleRowCount(-1);
        accountList.setFixedCellHeight(110);
        accountList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(accountList);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        transferTargets.setBackground(Palette.SURFACE);
        transferTargets.setForeground(Palette.TEXT_PRIMARY);
        transferTargets.setModel(targetModel);
        transferTargets.setRenderer(new TargetRenderer());

        styleField(amountField);
        styleField(transferAmountField);

        CardPanel actions = new CardPanel(new GridLayout(0, 1, 8, 8));
        actions.add(actionRow("Сумма для +/-", amountField, createButtons()));
        actions.add(transferRow());
        feedback.setForeground(Color.PINK);
        actions.add(feedback);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.EAST);
    }

    private JPanel createButtons() {
        PrimaryButton plus = new PrimaryButton("+ Пополнить");
        PrimaryButton minus = new PrimaryButton("- Снять");
        plus.addActionListener(e -> onAdjust(true));
        minus.addActionListener(e -> onAdjust(false));

        JPanel row = new JPanel(new GridLayout(1, 2, 6, 0));
        row.setOpaque(false);
        row.add(plus);
        row.add(minus);
        return row;
    }

    private void styleField(JTextField field) {
        field.setBackground(Palette.SURFACE);
        field.setForeground(Palette.TEXT_PRIMARY);
        field.setCaretColor(Palette.TEXT_PRIMARY);
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    private JPanel actionRow(String label, JComponent field, JComponent trailing) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(Palette.TEXT_MUTED);
        row.add(l, BorderLayout.NORTH);
        row.add(field, BorderLayout.CENTER);
        row.add(trailing, BorderLayout.SOUTH);
        return row;
    }

    private JPanel transferRow() {
        PrimaryButton send = new PrimaryButton("Перевести");
        send.addActionListener(e -> onTransfer());

        JPanel row = new JPanel(new GridLayout(0, 1, 6, 6));
        row.setOpaque(false);
        JLabel l = new JLabel("Перевод на другой счет");
        l.setForeground(Palette.TEXT_MUTED);
        row.add(l);
        row.add(transferTargets);
        row.add(transferAmountField);
        row.add(send);
        return row;
    }

    private void onAdjust(boolean isTopUp) {
        Account selected = accountList.getSelectedValue();
        if (selected == null) {
            feedback.setText("Выберите счет");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(amountField.getText());
            if (isTopUp) {
                accountService.deposit(selected.getId(), amount);
                feedback.setText("Баланс пополнен");
            } else {
                accountService.withdraw(selected.getId(), amount);
                feedback.setText("Снятие выполнено");
            }
            reload();
        } catch (Exception ex) {
            feedback.setText(ex.getMessage());
        }
    }

    private void onTransfer() {
        Account from = accountList.getSelectedValue();
        Account to = (Account) transferTargets.getSelectedItem();
        if (from == null || to == null) {
            feedback.setText("Выберите свой счет и получателя");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(transferAmountField.getText());
            accountService.transfer(from.getId(), to.getId(), amount);
            feedback.setText("Перевод выполнен");
            reload();
        } catch (Exception ex) {
            feedback.setText(ex.getMessage());
        }
    }

    public void load(User user) {
        this.user = user;
        reload();
    }

    private void reload() {
        if (user == null) return;
        List<Account> accounts = accountService.getAccounts(user);
        accountModel.clear();
        accounts.forEach(accountModel::addElement);
        List<Account> targets = accountService.getOtherAccounts(user);
        targetModel.removeAllElements();
        for (Account a : targets) {
            targetModel.addElement(a);
        }
        if (!accounts.isEmpty()) {
            accountList.setSelectedIndex(0);
        }
    }

    private String maskIban(String iban) {
        if (iban == null || iban.length() <= 4) return iban;
        String last = iban.substring(iban.length() - 4);
        return "•••• " + last;
    }

    private class AccountRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Account acc = (Account) value;
            if (acc == null) {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
            CardPanel card = new CardPanel(new BorderLayout());
            card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            card.setBackground(isSelected ? Palette.PURPLE_END : Palette.CARD);

            JLabel title = new JLabel(acc.getName());
            title.setForeground(Palette.TEXT_PRIMARY);
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

            JLabel owner = new JLabel("Владелец: " + acc.getOwner());
            owner.setForeground(Palette.TEXT_MUTED);
            owner.setFont(owner.getFont().deriveFont(Font.PLAIN, 12f));

            JLabel iban = new JLabel("Счёт: " + maskIban(acc.getIban()));
            iban.setForeground(Palette.TEXT_MUTED);
            iban.setFont(iban.getFont().deriveFont(Font.PLAIN, 12f));

            JLabel balance = new JLabel(currency.format(acc.getBalance()));
            balance.setForeground(Palette.TEXT_PRIMARY);
            balance.setFont(balance.getFont().deriveFont(Font.BOLD, 20f));
            balance.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(title);
            left.add(Box.createVerticalStrut(4));
            left.add(owner);
            left.add(iban);

            card.add(left, BorderLayout.WEST);
            card.add(balance, BorderLayout.EAST);
            return card;
        }
    }

    private class TargetRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Account acc = (Account) value;
            if (acc != null) {
                setText(acc.getOwner() + " · " + maskIban(acc.getIban()));
            } else {
                setText("");
            }
            return this;
        }
    }
}

