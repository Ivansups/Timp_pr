package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Role;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import com.bank.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService transactionService;
    private AccountService accountService;
    private User testUser;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();
        accountService = new AccountService();
        AuthService authService = new AuthService();
        authService.login("user", "pass");
        testUser = authService.getCurrentUser();
    }

    @Test
    void testListTransactions() {
        // Тест получения списка транзакций
        List<Transaction> transactions = transactionService.list();
        
        assertNotNull(transactions);
        // Проверяем структуру транзакций
        transactions.forEach(transaction -> {
            assertNotNull(transaction.getType());
            assertNotNull(transaction.getAmount());
            assertTrue(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0);
        });
    }

    @Test
    void testRollbackDeposit() {
        // Тест отката пополнения
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal initialBalance = account.getBalance();
        BigDecimal depositAmount = new BigDecimal("75.00");
        
        // Создаем транзакцию пополнения
        accountService.deposit(account.getId(), depositAmount);
        
        // Находим созданную транзакцию
        List<Transaction> transactions = transactionService.list();
        Transaction depositTx = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT 
                        && t.getToAccountId() != null 
                        && t.getToAccountId() == account.getId()
                        && t.getAmount().compareTo(depositAmount) == 0
                        && !t.isReversed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция пополнения не найдена"));
        
        // Откатываем транзакцию
        transactionService.rollback(depositTx.getId());
        
        // Проверяем, что баланс вернулся к исходному
        Account updatedAccount = accountService.getAccounts(testUser).stream()
                .filter(a -> a.getId() == account.getId())
                .findFirst()
                .orElseThrow();
        assertEquals(initialBalance, updatedAccount.getBalance());
        
        // Проверяем, что транзакция помечена как откатанная
        Transaction rolledBackTx = transactionService.list().stream()
                .filter(t -> t.getId() == depositTx.getId())
                .findFirst()
                .orElseThrow();
        assertTrue(rolledBackTx.isReversed());
    }

    @Test
    void testRollbackWithdraw() {
        // Тест отката снятия
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal initialBalance = account.getBalance();
        BigDecimal withdrawAmount = new BigDecimal("30.00");
        
        // Убеждаемся, что на счету достаточно средств
        if (initialBalance.compareTo(withdrawAmount) < 0) {
            accountService.deposit(account.getId(), withdrawAmount);
            initialBalance = accountService.getAccounts(testUser).stream()
                    .filter(a -> a.getId() == account.getId())
                    .findFirst()
                    .orElseThrow()
                    .getBalance();
        }
        
        // Создаем транзакцию снятия
        accountService.withdraw(account.getId(), withdrawAmount);
        
        // Находим созданную транзакцию
        List<Transaction> transactions = transactionService.list();
        Transaction withdrawTx = transactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAW 
                        && t.getFromAccountId() != null 
                        && t.getFromAccountId() == account.getId()
                        && t.getAmount().compareTo(withdrawAmount) == 0
                        && !t.isReversed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция снятия не найдена"));
        
        // Откатываем транзакцию
        transactionService.rollback(withdrawTx.getId());
        
        // Проверяем, что баланс вернулся к исходному
        Account updatedAccount = accountService.getAccounts(testUser).stream()
                .filter(a -> a.getId() == account.getId())
                .findFirst()
                .orElseThrow();
        assertEquals(initialBalance, updatedAccount.getBalance());
    }

    @Test
    void testRollbackTransfer() {
        // Тест отката перевода
        List<Account> userAccounts = accountService.getAccounts(testUser);
        List<Account> otherAccounts = accountService.getOtherAccounts(testUser);
        
        assertFalse(userAccounts.isEmpty());
        assertFalse(otherAccounts.isEmpty());
        
        Account fromAccount = userAccounts.get(0);
        Account toAccount = otherAccounts.get(0);
        
        BigDecimal initialFromBalance = fromAccount.getBalance();
        BigDecimal initialToBalance = toAccount.getBalance();
        BigDecimal transferAmount = new BigDecimal("20.00");
        
        // Убеждаемся, что на счету достаточно средств
        if (initialFromBalance.compareTo(transferAmount) < 0) {
            accountService.deposit(fromAccount.getId(), transferAmount);
            initialFromBalance = accountService.getAccounts(testUser).stream()
                    .filter(a -> a.getId() == fromAccount.getId())
                    .findFirst()
                    .orElseThrow()
                    .getBalance();
        }
        
        // Создаем транзакцию перевода
        accountService.transfer(fromAccount.getId(), toAccount.getId(), transferAmount);
        
        // Находим созданную транзакцию
        List<Transaction> transactions = transactionService.list();
        Transaction transferTx = transactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER 
                        && t.getFromAccountId() != null 
                        && t.getFromAccountId() == fromAccount.getId()
                        && t.getToAccountId() != null
                        && t.getToAccountId() == toAccount.getId()
                        && t.getAmount().compareTo(transferAmount) == 0
                        && !t.isReversed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция перевода не найдена"));
        
        // Откатываем транзакцию
        transactionService.rollback(transferTx.getId());
        
        // Проверяем, что балансы вернулись к исходным
        Account updatedFrom = accountService.getAccounts(testUser).stream()
                .filter(a -> a.getId() == fromAccount.getId())
                .findFirst()
                .orElseThrow();
        Account updatedTo = accountService.getOtherAccounts(testUser).stream()
                .filter(a -> a.getId() == toAccount.getId())
                .findFirst()
                .orElseThrow();
        
        assertEquals(initialFromBalance, updatedFrom.getBalance());
        assertEquals(initialToBalance, updatedTo.getBalance());
    }

    @Test
    void testRollbackNonExistentTransaction() {
        // Тест отката несуществующей транзакции
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.rollback(99999);
        });
    }

    @Test
    void testRollbackAlreadyReversedTransaction() {
        // Тест повторного отката транзакции
        List<Account> accounts = accountService.getAccounts(testUser);
        assertFalse(accounts.isEmpty());
        
        Account account = accounts.get(0);
        BigDecimal depositAmount = new BigDecimal("50.00");
        
        // Создаем и сразу откатываем транзакцию
        accountService.deposit(account.getId(), depositAmount);
        
        List<Transaction> transactions = transactionService.list();
        Transaction depositTx = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT 
                        && t.getToAccountId() != null 
                        && t.getToAccountId() == account.getId()
                        && !t.isReversed())
                .findFirst()
                .orElseThrow();
        
        transactionService.rollback(depositTx.getId());
        
        // Пытаемся откатить повторно
        assertThrows(IllegalStateException.class, () -> {
            transactionService.rollback(depositTx.getId());
        });
    }
}

