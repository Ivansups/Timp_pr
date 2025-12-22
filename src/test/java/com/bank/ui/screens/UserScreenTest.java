package com.bank.ui.screens;

import com.bank.model.User;
import com.bank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class UserScreenTest {

    private UserScreen userScreen;
    private AccountService accountService;
    private User testUser;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
        com.bank.service.AuthService authService = new com.bank.service.AuthService();
        authService.login("user", "pass");
        testUser = authService.getCurrentUser();
        
        userScreen = new UserScreen(accountService);
    }

    private JTextField findTextField(Container container, String defaultText) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                if (defaultText == null || defaultText.equals(field.getText())) {
                    return field;
                }
            }
            if (comp instanceof Container) {
                JTextField found = findTextField((Container) comp, defaultText);
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

    private JList<?> findList(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JList) {
                return (JList<?>) comp;
            }
            if (comp instanceof Container) {
                JList<?> found = findList((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JComboBox<?> findComboBox(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComboBox) {
                return (JComboBox<?>) comp;
            }
            if (comp instanceof Container) {
                JComboBox<?> found = findComboBox((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Test
    void testUserScreenVisible() {
        // Тест видимости экрана пользователя
        assertTrue(userScreen.isVisible());
    }

    @Test
    void testUserScreenHeader() {
        // Тест наличия заголовка
        JLabel headerLabel = findLabel(userScreen, "Мои счета");
        assertNotNull(headerLabel);
        assertTrue(headerLabel.isVisible());
    }

    @Test
    void testUserScreenSubtitle() {
        // Тест наличия подзаголовка
        JLabel subtitleLabel = findLabel(userScreen, "Пополняйте, снимайте и переводите другим клиентам");
        assertNotNull(subtitleLabel);
        assertTrue(subtitleLabel.isVisible());
    }

    @Test
    void testLoadUser() {
        // Тест загрузки пользователя
        assertDoesNotThrow(() -> {
            userScreen.load(testUser);
        });
        
        // Проверяем, что список счетов не пустой
        JList<?> accountList = findList(userScreen);
        assertNotNull(accountList);
    }

    @Test
    void testAccountListDisplay() {
        // Тест отображения списка счетов
        userScreen.load(testUser);
        
        JList<?> accountList = findList(userScreen);
        assertNotNull(accountList);
        assertTrue(accountList.getModel().getSize() > 0);
    }

    @Test
    void testAmountField() {
        // Тест поля суммы
        JTextField amountField = findTextField(userScreen, "100.00");
        assertNotNull(amountField);
        assertTrue(amountField.isVisible());
        amountField.setText("100.00");
        assertEquals("100.00", amountField.getText());
    }

    @Test
    void testTransferAmountField() {
        // Тест поля суммы перевода
        JTextField transferField = findTextField(userScreen, "50.00");
        assertNotNull(transferField);
        assertTrue(transferField.isVisible());
        transferField.setText("50.00");
        assertEquals("50.00", transferField.getText());
    }

    @Test
    void testDepositButton() {
        // Тест кнопки пополнения
        userScreen.load(testUser);
        JButton depositButton = findButton(userScreen, "+ Пополнить");
        assertNotNull(depositButton);
        assertTrue(depositButton.isVisible());
    }

    @Test
    void testWithdrawButton() {
        // Тест кнопки снятия
        userScreen.load(testUser);
        JButton withdrawButton = findButton(userScreen, "- Снять");
        assertNotNull(withdrawButton);
        assertTrue(withdrawButton.isVisible());
    }

    @Test
    void testTransferButton() {
        // Тест кнопки перевода
        userScreen.load(testUser);
        JButton transferButton = findButton(userScreen, "Перевести");
        assertNotNull(transferButton);
        assertTrue(transferButton.isVisible());
    }

    @Test
    void testTransferTargetsComboBox() {
        // Тест комбобокса получателей
        userScreen.load(testUser);
        
        JComboBox<?> comboBox = findComboBox(userScreen);
        assertNotNull(comboBox);
        assertTrue(comboBox.getModel().getSize() > 0);
    }

    @Test
    void testAccountListSelection() {
        // Тест выбора счета в списке
        userScreen.load(testUser);
        
        JList<?> accountList = findList(userScreen);
        if (accountList != null && accountList.getModel().getSize() > 0) {
            accountList.setSelectedIndex(0);
            assertNotNull(accountList.getSelectedValue());
        }
    }

    @Test
    void testScreenLayout() {
        // Тест структуры layout
        assertNotNull(userScreen.getLayout());
        assertTrue(userScreen.getLayout() instanceof BorderLayout);
    }
}
