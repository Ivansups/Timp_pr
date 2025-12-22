package com.bank.ui.screens;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DashboardScreenTest {

    private DashboardScreen dashboardScreen;

    @BeforeEach
    void setUp() {
        dashboardScreen = new DashboardScreen();
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

    @Test
    void testDashboardScreenVisible() {
        // Тест видимости экрана дашборда
        assertTrue(dashboardScreen.isVisible());
    }

    @Test
    void testDashboardHeader() {
        // Тест наличия заголовка
        JLabel headerLabel = findLabel(dashboardScreen, "Дашборд банка");
        assertNotNull(headerLabel);
        assertTrue(headerLabel.isVisible());
    }

    @Test
    void testDashboardSubtitle() {
        // Тест наличия подзаголовка
        JLabel subtitleLabel = findLabel(dashboardScreen, "Быстрый обзор продуктов и атмосферы банка");
        assertNotNull(subtitleLabel);
        assertTrue(subtitleLabel.isVisible());
    }

    @Test
    void testDashboardTagline() {
        // Тест наличия слогана
        JLabel taglineLabel = findLabel(dashboardScreen, "Лёгкость, скорость и прозрачность — без лишних действий.");
        assertNotNull(taglineLabel);
        assertTrue(taglineLabel.isVisible());
    }

    @Test
    void testDashboardContent() {
        // Тест наличия контента с маркерами
        Component[] components = dashboardScreen.getComponents();
        assertTrue(components.length > 0);
    }

    @Test
    void testRefreshMethod() {
        // Тест метода обновления (no-op)
        assertDoesNotThrow(() -> {
            dashboardScreen.refresh(10, new BigDecimal("1000.00"));
        });
    }

    @Test
    void testDashboardLayout() {
        // Тест структуры layout
        assertNotNull(dashboardScreen.getLayout());
        assertTrue(dashboardScreen.getLayout() instanceof GridBagLayout);
    }

    @Test
    void testDashboardIsOpaque() {
        // Тест прозрачности панели
        assertFalse(dashboardScreen.isOpaque());
    }

    @Test
    void testDashboardContainsCardPanel() {
        // Тест наличия CardPanel в структуре
        Component[] components = dashboardScreen.getComponents();
        boolean hasCardPanel = false;
        for (Component comp : components) {
            if (comp.getClass().getSimpleName().equals("CardPanel")) {
                hasCardPanel = true;
                break;
            }
        }
        assertTrue(hasCardPanel, "Dashboard должен содержать CardPanel");
    }

    private JTextArea findTextArea(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextArea) {
                return (JTextArea) comp;
            }
            if (comp instanceof Container) {
                JTextArea found = findTextArea((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Test
    void testDashboardTextArea() {
        // Тест наличия текстовой области с описанием
        JTextArea textArea = findTextArea(dashboardScreen);
        assertNotNull(textArea, "Dashboard должен содержать JTextArea");
        assertFalse(textArea.isEditable());
        assertTrue(textArea.getText().contains("Пополнение"));
    }
}
