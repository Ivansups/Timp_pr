package com.bank.dao;

import com.bank.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountDaoTest {

    private final AccountDao accountDao = new AccountDao();
    private int testUserId;

    @BeforeEach
    void setUp() {
        // Получаем ID пользователя для тестов
        UserDao userDao = new UserDao();
        testUserId = userDao.findByCredentials("user", "pass")
                .orElseThrow(() -> new RuntimeException("Тестовый пользователь не найден"))
                .getId();
    }

    @Test
    void testFindByUserId() {
        // Тест поиска счетов по ID пользователя
        List<Account> accounts = accountDao.findByUserId(testUserId);
        
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
        
        // Проверяем структуру счетов
        accounts.forEach(account -> {
            assertEquals(testUserId, account.getUserId());
            assertNotNull(account.getName());
            assertNotNull(account.getIban());
            assertNotNull(account.getBalance());
            assertNotNull(account.getOwner());
            assertTrue(account.getId() > 0);
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        });
    }

    @Test
    void testFindByUserIdNonExistent() {
        // Тест поиска счетов несуществующего пользователя
        List<Account> accounts = accountDao.findByUserId(99999);
        
        assertNotNull(accounts);
        assertTrue(accounts.isEmpty());
    }

    @Test
    void testFindAllExceptUser() {
        // Тест поиска счетов других пользователей
        List<Account> otherAccounts = accountDao.findAllExceptUser(testUserId);
        
        assertNotNull(otherAccounts);
        
        // Проверяем, что ни один счет не принадлежит текущему пользователю
        otherAccounts.forEach(account -> {
            assertNotEquals(testUserId, account.getUserId());
            assertNotNull(account.getName());
            assertNotNull(account.getIban());
            assertNotNull(account.getBalance());
            assertNotNull(account.getOwner());
        });
    }

    @Test
    void testFindById() {
        // Тест поиска счета по ID
        List<Account> accounts = accountDao.findByUserId(testUserId);
        assertFalse(accounts.isEmpty());
        
        Account firstAccount = accounts.get(0);
        Optional<Account> found = accountDao.findById(firstAccount.getId());
        
        assertTrue(found.isPresent());
        Account account = found.get();
        assertEquals(firstAccount.getId(), account.getId());
        assertEquals(firstAccount.getUserId(), account.getUserId());
        assertEquals(firstAccount.getName(), account.getName());
        assertEquals(firstAccount.getIban(), account.getIban());
        assertEquals(firstAccount.getBalance(), account.getBalance());
        assertEquals(firstAccount.getOwner(), account.getOwner());
    }

    @Test
    void testFindByIdNonExistent() {
        // Тест поиска несуществующего счета
        Optional<Account> account = accountDao.findById(99999);
        
        assertFalse(account.isPresent());
    }

    @Test
    void testUpdateBalance() {
        // Тест обновления баланса счета
        List<Account> accounts = accountDao.findByUserId(testUserId);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal originalBalance = account.getBalance();
        BigDecimal newBalance = originalBalance.add(new BigDecimal("100.00"));
        
        accountDao.updateBalance(account.getId(), newBalance);
        
        // Проверяем обновление
        Optional<Account> updated = accountDao.findById(account.getId());
        assertTrue(updated.isPresent());
        assertEquals(0, newBalance.compareTo(updated.get().getBalance()));
        
        // Восстанавливаем исходный баланс
        accountDao.updateBalance(account.getId(), originalBalance);
    }

    @Test
    void testUpdateBalanceToZero() {
        // Тест обновления баланса до нуля
        List<Account> accounts = accountDao.findByUserId(testUserId);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal originalBalance = account.getBalance();
        
        accountDao.updateBalance(account.getId(), BigDecimal.ZERO);
        
        Optional<Account> updated = accountDao.findById(account.getId());
        assertTrue(updated.isPresent());
        assertEquals(BigDecimal.ZERO, updated.get().getBalance());
        
        // Восстанавливаем исходный баланс
        accountDao.updateBalance(account.getId(), originalBalance);
    }

    @Test
    void testTransfer() {
        // Тест перевода между счетами
        List<Account> userAccounts = accountDao.findByUserId(testUserId);
        assertFalse(userAccounts.isEmpty());
        
        // Получаем счета другого пользователя
        List<Account> otherAccounts = accountDao.findAllExceptUser(testUserId);
        assertFalse(otherAccounts.isEmpty());
        
        Account fromAccount = userAccounts.get(0);
        Account toAccount = otherAccounts.get(0);
        
        BigDecimal fromOriginalBalance = fromAccount.getBalance();
        BigDecimal toOriginalBalance = toAccount.getBalance();
        BigDecimal transferAmount = new BigDecimal("10.00");
        
        // Убеждаемся, что на счету достаточно средств
        if (fromOriginalBalance.compareTo(transferAmount) < 0) {
            accountDao.updateBalance(fromAccount.getId(), transferAmount);
            fromOriginalBalance = transferAmount;
        }
        
        // Выполняем перевод
        accountDao.transfer(fromAccount.getId(), toAccount.getId(), transferAmount);
        
        // Проверяем результат
        Account updatedFrom = accountDao.findById(fromAccount.getId()).orElseThrow();
        Account updatedTo = accountDao.findById(toAccount.getId()).orElseThrow();
        
        assertEquals(0, fromOriginalBalance.subtract(transferAmount).compareTo(updatedFrom.getBalance()));
        assertEquals(0, toOriginalBalance.add(transferAmount).compareTo(updatedTo.getBalance()));
        
        // Восстанавливаем исходные балансы
        accountDao.updateBalance(fromAccount.getId(), fromOriginalBalance);
        accountDao.updateBalance(toAccount.getId(), toOriginalBalance);
    }

    @Test
    void testTransferInsufficientFunds() {
        // Тест перевода при недостаточных средствах
        List<Account> userAccounts = accountDao.findByUserId(testUserId);
        List<Account> otherAccounts = accountDao.findAllExceptUser(testUserId);
        
        assertFalse(userAccounts.isEmpty());
        assertFalse(otherAccounts.isEmpty());
        
        Account fromAccount = userAccounts.get(0);
        Account toAccount = otherAccounts.get(0);
        
        BigDecimal balance = fromAccount.getBalance();
        BigDecimal transferAmount = balance.add(new BigDecimal("1000.00"));
        
        assertThrows(RuntimeException.class, () -> {
            accountDao.transfer(fromAccount.getId(), toAccount.getId(), transferAmount);
        });
    }

    @Test
    void testTransferToNonExistentAccount() {
        // Тест перевода на несуществующий счет
        List<Account> userAccounts = accountDao.findByUserId(testUserId);
        assertFalse(userAccounts.isEmpty());
        
        Account fromAccount = userAccounts.get(0);
        BigDecimal balance = fromAccount.getBalance();
        
        // Убеждаемся, что на счету достаточно средств
        if (balance.compareTo(BigDecimal.ONE) < 0) {
            accountDao.updateBalance(fromAccount.getId(), BigDecimal.ONE);
            balance = BigDecimal.ONE;
        }
        
        assertThrows(RuntimeException.class, () -> {
            accountDao.transfer(fromAccount.getId(), 99999, BigDecimal.ONE);
        });
    }

    @Test
    void testAccountProperties() {
        // Тест свойств счета
        List<Account> accounts = accountDao.findByUserId(testUserId);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        
        // Проверяем геттеры
        assertTrue(account.getId() > 0);
        assertTrue(account.getUserId() > 0);
        assertNotNull(account.getName());
        assertFalse(account.getName().isEmpty());
        assertNotNull(account.getIban());
        assertFalse(account.getIban().isEmpty());
        assertNotNull(account.getBalance());
        assertNotNull(account.getOwner());
        assertFalse(account.getOwner().isEmpty());
        
        // Проверяем toString
        String toString = account.toString();
        assertNotNull(toString);
        assertTrue(toString.contains(account.getName()));
        assertTrue(toString.contains(account.getIban()));
    }
}

