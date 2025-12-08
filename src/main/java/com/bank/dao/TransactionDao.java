package com.bank.dao;

import com.bank.db.Database;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDao {

    public void insertDeposit(int accountId, BigDecimal amount) {
        insert(TransactionType.DEPOSIT, null, accountId, amount);
    }

    public void insertWithdraw(int accountId, BigDecimal amount) {
        insert(TransactionType.WITHDRAW, accountId, null, amount);
    }

    public void insertTransfer(int fromId, int toId, BigDecimal amount) {
        insert(TransactionType.TRANSFER, fromId, toId, amount);
    }

    private void insert(TransactionType type, Integer fromId, Integer toId, BigDecimal amount) {
        String sql = "INSERT INTO transactions(type, from_account_id, to_account_id, amount, reversed) VALUES(?,?,?,?,0)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            if (fromId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, fromId);
            if (toId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, toId);
            ps.setBigDecimal(4, amount);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert transaction", e);
        }
    }

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = """
                SELECT t.id, t.type, t.from_account_id, t.to_account_id, t.amount, t.reversed, t.created_at,
                       af.name AS from_name, au.username AS from_owner,
                       at.name AS to_name, tu.username AS to_owner
                FROM transactions t
                LEFT JOIN accounts af ON t.from_account_id = af.id
                LEFT JOIN users au ON af.user_id = au.id
                LEFT JOIN accounts at ON t.to_account_id = at.id
                LEFT JOIN users tu ON at.user_id = tu.id
                ORDER BY t.id DESC
                """;
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch transactions", e);
        }
        return list;
    }

    public Optional<Transaction> findById(int id) {
        String sql = """
                SELECT t.id, t.type, t.from_account_id, t.to_account_id, t.amount, t.reversed, t.created_at,
                       af.name AS from_name, au.username AS from_owner,
                       at.name AS to_name, tu.username AS to_owner
                FROM transactions t
                LEFT JOIN accounts af ON t.from_account_id = af.id
                LEFT JOIN users au ON af.user_id = au.id
                LEFT JOIN accounts at ON t.to_account_id = at.id
                LEFT JOIN users tu ON at.user_id = tu.id
                WHERE t.id = ?
                """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find transaction", e);
        }
        return Optional.empty();
    }

    public void markReversed(int id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE transactions SET reversed = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark reversed", e);
        }
    }

    private Transaction map(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        TransactionType type = TransactionType.valueOf(rs.getString("type"));
        Integer fromId = rs.getObject("from_account_id") == null ? null : rs.getInt("from_account_id");
        Integer toId = rs.getObject("to_account_id") == null ? null : rs.getInt("to_account_id");
        BigDecimal amount = rs.getBigDecimal("amount");
        boolean reversed = rs.getInt("reversed") == 1;
        LocalDateTime created = rs.getTimestamp("created_at").toLocalDateTime();
        String fromLabel = rs.getString("from_name");
        String fromOwner = rs.getString("from_owner");
        String toLabel = rs.getString("to_name");
        String toOwner = rs.getString("to_owner");
        String from = fromLabel == null ? "" : fromLabel + " (" + fromOwner + ")";
        String to = toLabel == null ? "" : toLabel + " (" + toOwner + ")";
        return new Transaction(id, type, fromId, toId, amount, reversed, created, from, to);
    }
}

