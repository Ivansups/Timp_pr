package com.bank.dao;

import com.bank.db.Database;
import com.bank.model.Role;
import com.bank.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public Optional<User> findByCredentials(String username, String password) {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user", e);
        }
        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, role FROM users ORDER BY username";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch users", e);
        }
        return users;
    }

    private User map(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        String u = rs.getString("username");
        String p = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        return new User(id, u, p, role);
    }
}

