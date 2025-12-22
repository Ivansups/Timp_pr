package com.bank.service;

import com.bank.model.Role;
import com.bank.model.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void testSuccessfulLogin() {
        // Тест успешного входа
        AuthService service = new AuthService();
        Optional<User> result = service.login("user", "pass");
        
        assertTrue(result.isPresent());
        assertEquals("user", result.get().getUsername());
        assertTrue(service.isAuthenticated());
        assertNotNull(service.getCurrentUser());
    }

    @Test
    void testFailedLogin() {
        // Тест неудачного входа
        AuthService service = new AuthService();
        Optional<User> result = service.login("wronguser", "wrongpass");
        
        assertFalse(result.isPresent());
        assertFalse(service.isAuthenticated());
        assertNull(service.getCurrentUser());
    }

    @Test
    void testLogout() {
        // Тест выхода
        AuthService service = new AuthService();
        service.login("user", "pass");
        assertTrue(service.isAuthenticated());
        
        service.logout();
        assertFalse(service.isAuthenticated());
        assertNull(service.getCurrentUser());
    }

    @Test
    void testGetCurrentUserAfterLogin() {
        // Тест получения текущего пользователя после входа
        AuthService service = new AuthService();
        Optional<User> loginResult = service.login("user", "pass");
        
        assertTrue(loginResult.isPresent());
        User currentUser = service.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals("user", currentUser.getUsername());
        assertEquals(Role.USER, currentUser.getRole());
    }

    @Test
    void testIsAuthenticatedBeforeLogin() {
        // Тест проверки аутентификации до входа
        AuthService service = new AuthService();
        assertFalse(service.isAuthenticated());
    }

    @Test
    void testAdminLogin() {
        // Тест входа администратора
        AuthService service = new AuthService();
        Optional<User> result = service.login("admin", "admin");
        
        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals(Role.ADMIN, result.get().getRole());
        assertTrue(service.isAuthenticated());
    }
}

