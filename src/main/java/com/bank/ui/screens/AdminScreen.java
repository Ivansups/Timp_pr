package com.bank.ui.screens;

import com.bank.dao.UserDao;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.TransactionService;
import com.bank.ui.Palette;
import com.bank.ui.components.CardPanel;
import com.bank.ui.components.PrimaryButton;
import com.bank.ui.components.SectionHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminScreen extends JPanel {
    private final AccountService accountService;
    private final TransactionService transactionService = new TransactionService();
    private final UserDao userDao = new UserDao();
    private final JTable table;
    private final DefaultTableModel model;
    private final JTable txTable;
    private final DefaultTableModel txModel;
    private final JTextField adjustField = new JTextField("100.00", 8);
    private final JLabel feedback = new JLabel(" ");
    private final Map<Integer, User> usersById = new HashMap<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public AdminScreen(AccountService accountService) {
        this.accountService = accountService;
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));

        SectionHeader header = new SectionHeader("Админ-панель");
        JLabel subtitle = new JLabel("Просмотр пользователей и быстрые корректировки балансов");
        subtitle.setForeground(Palette.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(subtitle, BorderLayout.SOUTH);

        String[] columns = {"Пользователь", "Счет", "IBAN", "Баланс", "ID"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(Palette.SURFACE);
        table.setForeground(Palette.TEXT_PRIMARY);
        table.setGridColor(Palette.PURPLE_START);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        String[] txCols = {"ID", "Тип", "Откуда", "Куда", "Сумма", "Дата", "Статус"};
        txModel = new DefaultTableModel(txCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        txTable = new JTable(txModel);
        txTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        txTable.setBackground(Palette.SURFACE);
        txTable.setForeground(Palette.TEXT_PRIMARY);
        txTable.setGridColor(Palette.PURPLE_START);
        JScrollPane txScroll = new JScrollPane(txTable);
        txScroll.setBorder(BorderFactory.createEmptyBorder());

        CardPanel actions = new CardPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JLabel lbl = new JLabel("Коррекция (+/-):");
        lbl.setForeground(Palette.TEXT_MUTED);
        PrimaryButton apply = new PrimaryButton("Применить");
        apply.addActionListener(e -> onAdjust());
        feedback.setForeground(Color.PINK);
        actions.add(lbl);
        actions.add(adjustField);
        actions.add(apply);
        actions.add(feedback);

        CardPanel txActions = new CardPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        PrimaryButton rollback = new PrimaryButton("Откатить транзакцию");
        rollback.addActionListener(e -> onRollback());
        JLabel note = new JLabel("Доступны депозит/снятие/перевод, статус \"откатано\" — уже отменено");
        note.setForeground(Palette.TEXT_MUTED);
        txActions.add(rollback);
        txActions.add(note);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, txScroll);
        split.setResizeWeight(0.55);
        split.setBorder(BorderFactory.createEmptyBorder());

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(actions, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        center.add(txActions, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void onAdjust() {
        int row = table.getSelectedRow();
        if (row < 0) {
            feedback.setText("Выберите счет");
            return;
        }
        int accountId = (Integer) table.getValueAt(row, 4); // hidden id
        try {
            BigDecimal delta = new BigDecimal(adjustField.getText());
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                accountService.deposit(accountId, delta);
                feedback.setText("Пополнение выполнено");
            } else {
                accountService.withdraw(accountId, delta.abs());
                feedback.setText("Списание выполнено");
            }
            load();
        } catch (Exception ex) {
            feedback.setText(ex.getMessage());
        }
    }

    public void load() {
        List<User> users = userDao.findAll();
        usersById.clear();
        users.forEach(u -> usersById.put(u.getId(), u));
        model.setRowCount(0);
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase("admin")) {
                continue; // админ не управляет своими счетами, и их нет
            }
            List<Account> accounts = accountService.getAccounts(u);
            for (Account acc : accounts) {
                model.addRow(new Object[]{
                        u.getUsername(),
                        acc.getName(),
                        acc.getIban(),
                        currency.format(acc.getBalance()),
                        acc.getId()
                });
            }
        }
        hideIdColumn();
        loadTransactions();
    }

    private void hideIdColumn() {
        if (table.getColumnCount() > 4) {
            var col = table.getColumnModel().getColumn(4);
            table.removeColumn(col);
        }
    }

    private void loadTransactions() {
        txModel.setRowCount(0);
        for (Transaction tx : transactionService.list()) {
            txModel.addRow(new Object[]{
                    tx.getId(),
                    tx.getType().name(),
                    tx.getFromLabel(),
                    tx.getToLabel(),
                    currency.format(tx.getAmount()),
                    dtf.format(tx.getCreatedAt()),
                    tx.isReversed() ? "откатано" : "активно"
            });
        }
    }

    private void onRollback() {
        int row = txTable.getSelectedRow();
        if (row < 0) {
            feedback.setText("Выберите транзакцию");
            return;
        }
        int txId = (Integer) txTable.getValueAt(row, 0);
        try {
            transactionService.rollback(txId);
            feedback.setText("Транзакция откатана");
            loadTransactions();
            load();
        } catch (Exception ex) {
            feedback.setText(ex.getMessage());
        }
    }
}

