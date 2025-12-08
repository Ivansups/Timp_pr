package com.bank.db;

import com.bank.model.Role;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Database {
    private static final Path DB_PATH = Paths.get(System.getProperty("user.dir"), "bank.db");
    private static final String URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();

    static {
        init();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private static void init() {
        try {
            if (DB_PATH.getParent() != null) {
                Files.createDirectories(DB_PATH.getParent());
            }

            try (Connection conn = getConnection()) {
                try (Statement st = conn.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                    st.execute("""
                            CREATE TABLE IF NOT EXISTS users (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                username TEXT UNIQUE NOT NULL,
                                password TEXT NOT NULL,
                                role TEXT NOT NULL
                            )
                            """);
                    st.execute("""
                            CREATE TABLE IF NOT EXISTS accounts (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                user_id INTEGER NOT NULL,
                                name TEXT NOT NULL,
                                iban TEXT NOT NULL,
                                balance DECIMAL(12,2) NOT NULL DEFAULT 0,
                                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                            )
                            """);
                    st.execute("""
                            CREATE TABLE IF NOT EXISTS transactions (
                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                type TEXT NOT NULL,
                                from_account_id INTEGER NULL,
                                to_account_id INTEGER NULL,
                                amount DECIMAL(12,2) NOT NULL,
                                reversed INTEGER NOT NULL DEFAULT 0,
                                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY(from_account_id) REFERENCES accounts(id) ON DELETE SET NULL,
                                FOREIGN KEY(to_account_id) REFERENCES accounts(id) ON DELETE SET NULL
                            )
                            """);
                }

                if (isEmpty(conn)) {
                    seed(conn);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init database", e);
        }
    }

    private static boolean isEmpty(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static void seed(Connection conn) throws SQLException {
        List<SeedUser> users = Arrays.asList(
                new SeedUser("admin", "admin", Role.ADMIN, new BigDecimal[]{}, new String[]{}), // админ без счетов
                new SeedUser("user", "pass", Role.USER, new BigDecimal[]{new BigDecimal("1250.00"), new BigDecimal("3200.00")}, new String[]{"Daily Card", "Savings Vault"}),
                new SeedUser("alice", "pass", Role.USER, new BigDecimal[]{new BigDecimal("980.00"), new BigDecimal("2100.00")}, new String[]{"Spending", "Travel"}),
                new SeedUser("bob", "pass", Role.USER, new BigDecimal[]{new BigDecimal("1500.00"), new BigDecimal("8200.00")}, new String[]{"Checking", "Invest"})
        );

        try (PreparedStatement insertUser = conn.prepareStatement(
                "INSERT INTO users(username, password, role) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertAccount = conn.prepareStatement(
                     "INSERT INTO accounts(user_id, name, iban, balance) VALUES(?,?,?,?)")) {

            for (SeedUser u : users) {
                insertUser.setString(1, u.username());
                insertUser.setString(2, u.password());
                insertUser.setString(3, u.role().name());
                insertUser.executeUpdate();
                try (ResultSet keys = insertUser.getGeneratedKeys()) {
                    if (keys.next()) {
                        int userId = keys.getInt(1);
                        for (int i = 0; i < u.balances().length; i++) {
                            insertAccount.setInt(1, userId);
                            insertAccount.setString(2, u.names()[i]);
                            insertAccount.setString(3, fakeIban(u.username(), i));
                            insertAccount.setBigDecimal(4, u.balances()[i]);
                            insertAccount.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    private static String fakeIban(String username, int index) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "BK" + username.substring(0, Math.min(4, username.length())).toUpperCase() + index + suffix;
    }

    private record SeedUser(String username, String password, Role role, BigDecimal[] balances, String[] names) {
    }
}

