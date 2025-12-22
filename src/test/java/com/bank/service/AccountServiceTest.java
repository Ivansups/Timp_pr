package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.model.Account;
import com.bank.model.Role;
import com.bank.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;
    private User testUser;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
        // Используем реального пользователя из БД для тестов
        AuthService authService = new AuthService();
        authService.login("user", "pass");
        testUser = authService.getCurrentUser();
    }

    @Test
    void testGetAccounts() {
        // Тест получения счетов пользователя
        List<Account> accounts = accountService.getAccounts(testUser);
        
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        // Проверяем, что все счета принадлежат пользователю
        accounts.forEach(account -> {
            assertEquals(testUser.getId(), account.getUserId());
            assertNotNull(account.getName());
            assertNotNull(account.getIban());
            assertNotNull(account.getBalance());
        });
    }

    @Test
    void testGetOtherAccounts() {
        // Тест получения счетов других пользователей
        List<Account> otherAccounts = accountService.getOtherAccounts(testUser);
        
        assertNotNull(otherAccounts);
        // Проверяем, что ни один счет не принадлежит текущему пользователю
        otherAccounts.forEach(account -> {
            assertNotEquals(testUser.getId(), account.getUserId());
        });
    }

    @Test
    void testDeposit() {
        // Тест пополнения счета
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal initialBalance = account.getBalance();
        BigDecimal depositAmount = new BigDecimal("100.00");
        
        Account updatedAccount = accountService.deposit(account.getId(), depositAmount);
        
        assertEquals(0, initialBalance.add(depositAmount).compareTo(updatedAccount.getBalance()));
        
        // Проверяем, что баланс действительно обновился в БД
        Account reloaded = accountService.getAccounts(testUser).stream()
                .filter(a -> a.getId() == account.getId())
                .findFirst()
                .orElseThrow();
        assertEquals(0, initialBalance.add(depositAmount).compareTo(reloaded.getBalance()));
    }

    @Test
    void testDepositWithZeroAmount() {
        // Тест пополнения нулевой суммой
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(account.getId(), BigDecimal.ZERO);
        });
    }

    @Test
    void testDepositWithNegativeAmount() {
        // Тест пополнения отрицательной суммой
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(account.getId(), new BigDecimal("-10.00"));
        });
    }

    @Test
    void testDepositNonExistentAccount() {
        // Тест пополнения несуществующего счета
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.deposit(99999, new BigDecimal("100.00"));
        });
    }

    @Test
    void testWithdraw() {
        // Тест снятия средств
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal initialBalance = account.getBalance();
        BigDecimal withdrawAmount = new BigDecimal("50.00");
        
        // Убеждаемся, что на счету достаточно средств
        if (initialBalance.compareTo(withdrawAmount) < 0) {
            // Пополняем счет для теста
            accountService.deposit(account.getId(), withdrawAmount);
            initialBalance = accountService.getAccounts(testUser).stream()
                    .filter(a -> a.getId() == account.getId())
                    .findFirst()
                    .orElseThrow()
                    .getBalance();
        }
        
        Account updatedAccount = accountService.withdraw(account.getId(), withdrawAmount);
        
        assertEquals(0, initialBalance.subtract(withdrawAmount).compareTo(updatedAccount.getBalance()));
    }

    @Test
    void testWithdrawInsufficientFunds() {
        // Тест снятия при недостаточных средствах
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal balance = account.getBalance();
        BigDecimal withdrawAmount = balance.add(new BigDecimal("1000.00"));
        
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(account.getId(), withdrawAmount);
        });
    }

    @Test
    void testWithdrawZeroAmount() {
        // Тест снятия нулевой суммы
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.withdraw(account.getId(), BigDecimal.ZERO);
        });
    }

    @Test
    void testTransfer() {
        // Тест перевода между счетами
        List<Account> userAccounts = accountService.getAccounts(testUser);
        List<Account> otherAccounts = accountService.getOtherAccounts(testUser);
        
        assertFalse(userAccounts.isEmpty());
        assertFalse(otherAccounts.isEmpty());
        
        Account fromAccount = userAccounts.get(0);
        Account toAccount = otherAccounts.get(0);
        
        BigDecimal initialFromBalance = fromAccount.getBalance();
        BigDecimal initialToBalance = toAccount.getBalance();
        BigDecimal transferAmount = new BigDecimal("25.00");
        
        // Убеждаемся, что на счету достаточно средств
        if (initialFromBalance.compareTo(transferAmount) < 0) {
            accountService.deposit(fromAccount.getId(), transferAmount);
            initialFromBalance = accountService.getAccounts(testUser).stream()
                    .filter(a -> a.getId() == fromAccount.getId())
                    .findFirst()
                    .orElseThrow()
                    .getBalance();
        }
        
        accountService.transfer(fromAccount.getId(), toAccount.getId(), transferAmount);
        
        // Проверяем балансы после перевода
        Account updatedFrom = accountService.getAccounts(testUser).stream()
                .filter(a -> a.getId() == fromAccount.getId())
                .findFirst()
                .orElseThrow();
        Account updatedTo = accountService.getOtherAccounts(testUser).stream()
                .filter(a -> a.getId() == toAccount.getId())
                .findFirst()
                .orElseThrow();
        
        assertEquals(0, initialFromBalance.subtract(transferAmount).compareTo(updatedFrom.getBalance()));
        assertEquals(0, initialToBalance.add(transferAmount).compareTo(updatedTo.getBalance()));
    }

    @Test
    void testTransferToSameAccount() {
        // Тест перевода на тот же счет
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(account.getId(), account.getId(), new BigDecimal("10.00"));
        });
    }

    @Test
    void testTransferZeroAmount() {
        // Тест перевода нулевой суммы
        List<Account> userAccounts = accountService.getAccounts(testUser);
        List<Account> otherAccounts = accountService.getOtherAccounts(testUser);
        
        assertFalse(userAccounts.isEmpty());
        assertFalse(otherAccounts.isEmpty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.transfer(userAccounts.get(0).getId(), otherAccounts.get(0).getId(), BigDecimal.ZERO);
        });
    }
}

