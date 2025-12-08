package com.bank.ui.screens;

import com.bank.dao.AccountDao;
import com.bank.model.Account;
import com.bank.model.Role;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.AuthService;
import com.bank.ui.Palette;
import com.bank.ui.components.GradientPanel;
import com.bank.ui.components.PrimaryButton;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class MainFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final AccountService accountService = new AccountService();
    private final AccountDao accountDao = new AccountDao();

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

    private final LoginScreen loginScreen;
    private final DashboardScreen dashboardScreen;
    private final UserScreen userScreen;
    private final AdminScreen adminScreen;

    private final JButton adminBtn = new PrimaryButton("Админ");

    public MainFrame() {
        super("Swing Bank");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout(0, 10));
        setContentPane(root);

        loginScreen = new LoginScreen(this::handleLogin);
        dashboardScreen = new DashboardScreen();
        userScreen = new UserScreen(accountService);
        adminScreen = new AdminScreen(accountService);

        nav.setOpaque(false);
        nav.setVisible(false);

        PrimaryButton dashBtn = new PrimaryButton("Дашборд");
        PrimaryButton accountsBtn = new PrimaryButton("Счета");
        PrimaryButton logoutBtn = new PrimaryButton("Выход");

        dashBtn.addActionListener(e -> cards.show(content, "dashboard"));
        accountsBtn.addActionListener(e -> {
            if (authService.getCurrentUser() != null) {
                userScreen.load(authService.getCurrentUser());
            }
            cards.show(content, "user");
        });
        adminBtn.addActionListener(e -> {
            adminScreen.load();
            cards.show(content, "admin");
        });
        logoutBtn.addActionListener(e -> logout());

        nav.add(dashBtn);
        nav.add(accountsBtn);
        nav.add(adminBtn);
        nav.add(logoutBtn);

        content.setOpaque(false);
        content.add(loginScreen, "login");
        content.add(dashboardScreen, "dashboard");
        content.add(userScreen, "user");
        content.add(adminScreen, "admin");

        root.add(nav, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);

        cards.show(content, "login");
    }

    private void handleLogin(String username, String password) {
        loginScreen.clearError();
        authService.login(username, password).ifPresentOrElse(user -> {
            nav.setVisible(true);
            adminBtn.setVisible(user.getRole() == Role.ADMIN);
            refreshDashboard();
            userScreen.load(user);
            if (user.getRole() == Role.ADMIN) {
                adminScreen.load();
            }
            cards.show(content, "dashboard");
        }, () -> loginScreen.showError("Неверный логин или пароль"));
    }

    private void refreshDashboard() {
        List<Account> accounts = accountDao.findAllExceptUser(-1);
        BigDecimal total = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboardScreen.refresh(accounts.size(), total);
    }

    private void logout() {
        authService.logout();
        nav.setVisible(false);
        cards.show(content, "login");
    }
}

