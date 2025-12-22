package com.bank.ui.screens;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LoginScreenTest {

    private AtomicBoolean loginCalled;
    private AtomicReference<String> loginUsername;
    private AtomicReference<String> loginPassword;
    private LoginScreen loginScreen;

    @BeforeEach
    void setUp() {
        loginCalled = new AtomicBoolean(false);
        loginUsername = new AtomicReference<>();
        loginPassword = new AtomicReference<>();
        
        // Создаем LoginScreen с мокированным обработчиком
        loginScreen = new LoginScreen((username, password) -> {
            loginCalled.set(true);
            loginUsername.set(username);
            loginPassword.set(password);
        });
    }

    private JTextField findTextField(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField && !(comp instanceof JPasswordField)) {
                return (JTextField) comp;
            }
            if (comp instanceof Container) {
                JTextField found = findTextField((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JPasswordField findPasswordField(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPasswordField) {
                return (JPasswordField) comp;
            }
            if (comp instanceof Container) {
                JPasswordField found = findPasswordField((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JButton findButton(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (text.equals(button.getText())) {
                    return button;
                }
            }
            if (comp instanceof Container) {
                JButton found = findButton((Container) comp, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JLabel findLabel(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (text.equals(label.getText())) {
                    return label;
                }
            }
            if (comp instanceof Container) {
                JLabel found = findLabel((Container) comp, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JLabel findErrorLabel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getForeground().equals(Color.PINK)) {
                    return label;
                }
            }
            if (comp instanceof Container) {
                JLabel found = findErrorLabel((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Test
    void testLoginScreenComponentsVisible() {
        // Тест видимости компонентов экрана входа
        JTextField userField = findTextField(loginScreen);
        JPasswordField passField = findPasswordField(loginScreen);
        
        assertNotNull(userField);
        assertNotNull(passField);
        assertTrue(userField.isVisible());
        assertTrue(passField.isVisible());
    }

    @Test
    void testLoginScreenTitle() {
        // Тест наличия заголовка
        JLabel titleLabel = findLabel(loginScreen, "Добро пожаловать");
        assertNotNull(titleLabel);
        assertTrue(titleLabel.isVisible());
    }

    @Test
    void testEnterUsername() {
        // Тест ввода имени пользователя
        JTextField userField = findTextField(loginScreen);
        assertNotNull(userField);
        userField.setText("testuser");
        assertEquals("testuser", userField.getText());
    }

    @Test
    void testEnterPassword() {
        // Тест ввода пароля
        JPasswordField passField = findPasswordField(loginScreen);
        assertNotNull(passField);
        passField.setText("testpass");
        assertTrue(passField.getPassword().length > 0);
    }

    @Test
    void testLoginButtonClick() {
        // Тест нажатия кнопки входа
        JTextField userField = findTextField(loginScreen);
        JPasswordField passField = findPasswordField(loginScreen);
        JButton loginButton = findButton(loginScreen, "Войти");
        
        assertNotNull(userField);
        assertNotNull(passField);
        assertNotNull(loginButton);
        
        userField.setText("user");
        passField.setText("pass");
        loginButton.doClick();
        
        // Проверяем, что обработчик был вызван
        assertTrue(loginCalled.get());
        assertEquals("user", loginUsername.get());
        assertEquals("pass", loginPassword.get());
    }

    @Test
    void testShowError() {
        // Тест отображения ошибки
        loginScreen.showError("Тестовая ошибка");
        
        // Проверяем, что ошибка отображается
        JLabel errorLabel = findErrorLabel(loginScreen);
        assertNotNull(errorLabel);
        assertEquals("Тестовая ошибка", errorLabel.getText());
    }

    @Test
    void testClearError() {
        // Тест очистки ошибки
        loginScreen.showError("Ошибка");
        loginScreen.clearError();
        
        // Проверяем, что ошибка очищена (метод clearError устанавливает пробел)
        JLabel errorLabel = findErrorLabel(loginScreen);
        assertNotNull(errorLabel);
        assertEquals(" ", errorLabel.getText());
    }

    @Test
    void testLoginWithEmptyFields() {
        // Тест входа с пустыми полями
        JButton loginButton = findButton(loginScreen, "Войти");
        assertNotNull(loginButton);
        loginButton.doClick();
        
        assertTrue(loginCalled.get());
        assertEquals("", loginUsername.get());
        assertEquals("", loginPassword.get());
    }

    @Test
    void testLoginWithTrimmedText() {
        // Тест обрезки пробелов в логине
        JTextField userField = findTextField(loginScreen);
        JPasswordField passField = findPasswordField(loginScreen);
        JButton loginButton = findButton(loginScreen, "Войти");
        
        assertNotNull(userField);
        assertNotNull(passField);
        assertNotNull(loginButton);
        
        userField.setText("  user  ");
        passField.setText("pass");
        loginButton.doClick();
        
        assertEquals("user", loginUsername.get());
    }

    @Test
    void testTextFieldStyling() {
        // Тест стилизации полей ввода
        JTextField userField = findTextField(loginScreen);
        assertNotNull(userField);
        assertNotNull(userField.getBackground());
        assertNotNull(userField.getForeground());
        assertNotNull(userField.getBorder());
    }

    @Test
    void testPasswordFieldIsPasswordField() {
        // Тест, что поле пароля действительно JPasswordField
        JPasswordField passField = findPasswordField(loginScreen);
        assertNotNull(passField);
        assertTrue(passField instanceof JPasswordField);
    }
}
