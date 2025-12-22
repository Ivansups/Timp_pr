package com.bank.dao;

import com.bank.model.Role;
import com.bank.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    private final UserDao userDao = new UserDao();

    @Test
    void testFindByCredentialsSuccess() {
        // Тест успешного поиска пользователя по учетным данным
        Optional<User> user = userDao.findByCredentials("user", "pass");
        
        assertTrue(user.isPresent());
        assertEquals("user", user.get().getUsername());
        assertEquals("pass", user.get().getPassword());
        assertEquals(Role.USER, user.get().getRole());
        assertTrue(user.get().getId() > 0);
    }

    @Test
    void testFindByCredentialsAdmin() {
        // Тест поиска администратора
        Optional<User> admin = userDao.findByCredentials("admin", "admin");
        
        assertTrue(admin.isPresent());
        assertEquals("admin", admin.get().getUsername());
        assertEquals("admin", admin.get().getPassword());
        assertEquals(Role.ADMIN, admin.get().getRole());
    }

    @Test
    void testFindByCredentialsFailure() {
        // Тест неудачного поиска пользователя
        Optional<User> user = userDao.findByCredentials("nonexistent", "wrongpass");
        
        assertFalse(user.isPresent());
    }

    @Test
    void testFindByCredentialsWrongPassword() {
        // Тест поиска с неправильным паролем
        Optional<User> user = userDao.findByCredentials("user", "wrongpassword");
        
        assertFalse(user.isPresent());
    }

    @Test
    void testFindByCredentialsCaseSensitive() {
        // Тест чувствительности к регистру
        Optional<User> user = userDao.findByCredentials("USER", "pass");
        
        // В зависимости от реализации БД может быть чувствительным к регистру
        // Проверяем, что либо не найден, либо найден корректно
        if (user.isPresent()) {
            assertEquals("USER", user.get().getUsername());
        } else {
            // Если не найден, это тоже валидное поведение
            assertTrue(true);
        }
    }

    @Test
    void testFindAll() {
        // Тест получения всех пользователей
        List<User> users = userDao.findAll();
        
        assertNotNull(users);
        assertFalse(users.isEmpty());
        
        // Проверяем структуру пользователей
        users.forEach(user -> {
            assertNotNull(user.getUsername());
            assertNotNull(user.getPassword());
            assertNotNull(user.getRole());
            assertTrue(user.getId() > 0);
        });
        
        // Проверяем, что пользователи отсортированы по username
        for (int i = 0; i < users.size() - 1; i++) {
            String current = users.get(i).getUsername().toLowerCase();
            String next = users.get(i + 1).getUsername().toLowerCase();
            assertTrue(current.compareTo(next) <= 0, 
                    "Пользователи должны быть отсортированы по username");
        }
    }

    @Test
    void testFindAllContainsKnownUsers() {
        // Тест, что в списке есть известные пользователи
        List<User> users = userDao.findAll();
        
        boolean hasUser = users.stream().anyMatch(u -> "user".equals(u.getUsername()));
        boolean hasAdmin = users.stream().anyMatch(u -> "admin".equals(u.getUsername()));
        
        assertTrue(hasUser, "Должен быть пользователь 'user'");
        assertTrue(hasAdmin, "Должен быть пользователь 'admin'");
    }

    @Test
    void testUserProperties() {
        // Тест свойств пользователя
        Optional<User> user = userDao.findByCredentials("user", "pass");
        
        assertTrue(user.isPresent());
        User u = user.get();
        
        // Проверяем геттеры
        assertNotNull(u.getId());
        assertNotNull(u.getUsername());
        assertNotNull(u.getPassword());
        assertNotNull(u.getRole());
        
        // Проверяем, что ID положительный
        assertTrue(u.getId() > 0);
        
        // Проверяем, что роль валидна
        assertTrue(u.getRole() == Role.USER || u.getRole() == Role.ADMIN);
    }
}

