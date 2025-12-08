package com.bank.service;

import com.bank.dao.UserDao;
import com.bank.model.User;

import java.util.Optional;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private User currentUser;

    public Optional<User> login(String username, String password) {
        Optional<User> found = userDao.findByCredentials(username, password);
        found.ifPresent(u -> currentUser = u);
        return found;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}

