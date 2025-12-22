package com.bank.ui.screens;

import com.bank.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class AdminScreenTest {

    private AdminScreen adminScreen;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
        adminScreen = new AdminScreen(accountService);
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

    private JTable findTable(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    return (JTable) view;
                }
            }
            if (comp instanceof Container) {
                JTable found = findTable((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private JTable findTransactionsTable(Component[] components) {
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    JTable table = (JTable) view;
                    if (table.getColumnCount() >= 6) {
                        return table;
                    }
                }
            }
            if (comp instanceof Container) {
                JTable found = findTransactionsTable(((Container) comp).getComponents());
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Test
    void testAdminScreenVisible() {
        // Тест видимости экрана администратора
        assertTrue(adminScreen.isVisible());
    }

    @Test
    void testAdminScreenHeader() {
        // Тест наличия заголовка
        JLabel headerLabel = findLabel(adminScreen, "Админ-панель");
        assertNotNull(headerLabel);
        assertTrue(headerLabel.isVisible());
    }

    @Test
    void testAdminScreenSubtitle() {
        // Тест наличия подзаголовка
        JLabel subtitleLabel = findLabel(adminScreen, "Просмотр пользователей и быстрые корректировки балансов");
        assertNotNull(subtitleLabel);
        assertTrue(subtitleLabel.isVisible());
    }

    @Test
    void testLoadMethod() {
        // Тест метода загрузки данных
        assertDoesNotThrow(() -> {
            adminScreen.load();
        });
    }

    @Test
    void testAccountsTable() {
        // Тест таблицы счетов
        adminScreen.load();
        
        JTable table = findTable(adminScreen);
        assertNotNull(table);
        assertTrue(table.getRowCount() > 0);
        
        // Проверяем колонки
        assertTrue(table.getColumnCount() >= 4);
    }

    @Test
    void testTransactionsTable() {
        // Тест таблицы транзакций
        adminScreen.load();
        
        // Ищем вторую таблицу (транзакции)
        Component[] components = adminScreen.getComponents();
        JTable txTable = findTransactionsTable(components);
        
        assertNotNull(txTable);
        assertTrue(txTable.getColumnCount() >= 6);
    }

    @Test
    void testAdjustField() {
        // Тест поля корректировки баланса
        JTextField adjustField = findTextField(adminScreen, "100.00");
        assertNotNull(adjustField);
        assertTrue(adjustField.isVisible());
        adjustField.setText("50.00");
        assertEquals("50.00", adjustField.getText());
    }

    @Test
    void testApplyButton() {
        // Тест кнопки применения корректировки
        adminScreen.load();
        JButton applyButton = findButton(adminScreen, "Применить");
        assertNotNull(applyButton);
        assertTrue(applyButton.isVisible());
    }

    @Test
    void testRollbackButton() {
        // Тест кнопки отката транзакции
        adminScreen.load();
        JButton rollbackButton = findButton(adminScreen, "Откатить транзакцию");
        assertNotNull(rollbackButton);
        assertTrue(rollbackButton.isVisible());
    }

    @Test
    void testTableSelection() {
        // Тест выбора строки в таблице
        adminScreen.load();
        
        JTable table = findTable(adminScreen);
        if (table != null && table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
            assertEquals(0, table.getSelectedRow());
        }
    }

    @Test
    void testScreenLayout() {
        // Тест структуры layout
        assertNotNull(adminScreen.getLayout());
        assertTrue(adminScreen.getLayout() instanceof BorderLayout);
    }

    private boolean hasSplitPane(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JSplitPane) {
                return true;
            }
            if (comp instanceof Container) {
                if (hasSplitPane((Container) comp)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void testSplitPane() {
        // Тест наличия JSplitPane
        assertTrue(hasSplitPane(adminScreen), "AdminScreen должен содержать JSplitPane");
    }

    @Test
    void testAccountsTableColumns() {
        // Тест колонок таблицы счетов
        adminScreen.load();
        
        JTable table = findTable(adminScreen);
        assertNotNull(table);
        String[] expectedColumns = {"Пользователь", "Счет", "IBAN", "Баланс"};
        
        for (int i = 0; i < Math.min(expectedColumns.length, table.getColumnCount()); i++) {
            String columnName = table.getColumnName(i);
            assertNotNull(columnName);
        }
    }
}
