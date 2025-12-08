package com.bank.dao;

import com.bank.db.Database;
import com.bank.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDao {

    public List<Account> findByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = """
                SELECT a.id, a.user_id, a.name, a.iban, a.balance, u.username AS owner
                FROM accounts a
                JOIN users u ON a.user_id = u.id
                WHERE a.user_id = ?
                ORDER BY a.id
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch accounts", e);
        }
        return accounts;
    }

    public List<Account> findAllExceptUser(int userId) {
        List<Account> accounts = new ArrayList<>();
        String sql = """
                SELECT a.id, a.user_id, a.name, a.iban, a.balance, u.username AS owner
                FROM accounts a
                JOIN users u ON a.user_id = u.id
                WHERE a.user_id <> ?
                ORDER BY a.user_id, a.id
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch other accounts", e);
        }
        return accounts;
    }

    public Optional<Account> findById(int id) {
        String sql = """
                SELECT a.id, a.user_id, a.name, a.iban, a.balance, u.username AS owner
                FROM accounts a
                JOIN users u ON a.user_id = u.id
                WHERE a.id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find account", e);
        }
        return Optional.empty();
    }

    public void updateBalance(int accountId, BigDecimal newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update balance", e);
        }
    }

    public void transfer(int fromId, int toId, BigDecimal amount) {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal fromBalance = getBalance(conn, fromId);
                BigDecimal toBalance = getBalance(conn, toId);
                if (fromBalance == null || toBalance == null) {
                    throw new IllegalArgumentException("Account not found");
                }
                if (fromBalance.compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Недостаточно средств");
                }
                updateBalance(conn, fromId, fromBalance.subtract(amount));
                updateBalance(conn, toId, toBalance.add(amount));
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to transfer", e);
        }
    }

    private BigDecimal getBalance(Connection conn, int accountId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
            }
        }
        return null;
    }

    private void updateBalance(Connection conn, int accountId, BigDecimal value) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE accounts SET balance = ? WHERE id = ?")) {
            ps.setBigDecimal(1, value);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    private Account map(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String name = rs.getString("name");
        String iban = rs.getString("iban");
        BigDecimal balance = rs.getBigDecimal("balance");
        String owner = rs.getString("owner");
        return new Account(id, userId, name, iban, balance, owner);
    }
}

