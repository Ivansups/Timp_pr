package com.bank.dao;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionDaoTest {

    private final TransactionDao transactionDao = new TransactionDao();
    private final AccountDao accountDao = new AccountDao();
    private int testAccountId;

    @BeforeEach
    void setUp() {
        // Получаем ID счета для тестов
        UserDao userDao = new UserDao();
        int userId = userDao.findByCredentials("user", "pass")
                .orElseThrow(() -> new RuntimeException("Тестовый пользователь не найден"))
                .getId();
        List<Account> accounts = accountDao.findByUserId(userId);
        assertFalse(accounts.isEmpty());
        testAccountId = accounts.get(0).getId();
    }

    @Test
    void testInsertDeposit() {
        // Тест создания транзакции пополнения
        BigDecimal amount = new BigDecimal("50.00");
        
        transactionDao.insertDeposit(testAccountId, amount);
        
        // Проверяем, что транзакция создана
        List<Transaction> transactions = transactionDao.findAll();
        Transaction deposit = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT
                        && t.getToAccountId() != null
                        && t.getToAccountId() == testAccountId
                        && t.getAmount().compareTo(amount) == 0
                        && !t.isReversed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция пополнения не найдена"));
        
        assertNotNull(deposit);
        assertEquals(TransactionType.DEPOSIT, deposit.getType());
        assertNull(deposit.getFromAccountId());
        assertEquals(testAccountId, deposit.getToAccountId());
        assertEquals(0, amount.compareTo(deposit.getAmount()));
        assertFalse(deposit.isReversed());
    }

    @Test
    void testInsertWithdraw() {
        // Тест создания транзакции снятия
        BigDecimal amount = new BigDecimal("25.00");
        
        transactionDao.insertWithdraw(testAccountId, amount);
        
        // Проверяем, что транзакция создана
        List<Transaction> transactions = transactionDao.findAll();
        Transaction withdraw = transactions.stream()
                .filter(t -> t.getType() == TransactionType.WITHDRAW
                        && t.getFromAccountId() != null
                        && t.getFromAccountId() == testAccountId
                        && t.getAmount().compareTo(amount) == 0
                        && !t.isReversed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция снятия не найдена"));
        
        assertNotNull(withdraw);
        assertEquals(TransactionType.WITHDRAW, withdraw.getType());
        assertEquals(testAccountId, withdraw.getFromAccountId());
        assertNull(withdraw.getToAccountId());
        assertEquals(0, amount.compareTo(withdraw.getAmount()));
        assertFalse(withdraw.isReversed());
    }

    @Test
    void testInsertTransfer() {
        // Тест создания транзакции перевода
        List<Account> otherAccounts = accountDao.findAllExceptUser(
                accountDao.findById(testAccountId).orElseThrow().getUserId());
        assertFalse(otherAccounts.isEmpty());
        
        int toAccountId = otherAccounts.get(0).getId();
        BigDecimal amount = new BigDecimal("15.00");
        
        transactionDao.insertTransfer(testAccountId, toAccountId, amount);
        
        // Проверяем, что транзакция создана
        List<Transaction> transactions = transactionDao.findAll();
        Transaction transfer = transactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER
                        && t.getFromAccountId() != null
                        && t.getFromAccountId() == testAccountId
                        && t.getToAccountId() != null
                        && t.getToAccountId() == toAccountId
                        && t.getAmount().compareTo(amount) == 0
                        && !t.isReversed())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Транзакция перевода не найдена"));
        
        assertNotNull(transfer);
        assertEquals(TransactionType.TRANSFER, transfer.getType());
        assertEquals(testAccountId, transfer.getFromAccountId());
        assertEquals(toAccountId, transfer.getToAccountId());
        assertEquals(0, amount.compareTo(transfer.getAmount()));
        assertFalse(transfer.isReversed());
    }

    @Test
    void testFindAll() {
        // Тест получения всех транзакций
        List<Transaction> transactions = transactionDao.findAll();
        
        assertNotNull(transactions);
        
        // Проверяем структуру транзакций
        transactions.forEach(transaction -> {
            assertNotNull(transaction.getType());
            assertNotNull(transaction.getAmount());
            assertTrue(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(transaction.getCreatedAt());
            assertTrue(transaction.getId() > 0);
            
            // Проверяем логику типов транзакций
            switch (transaction.getType()) {
                case DEPOSIT -> {
                    assertNull(transaction.getFromAccountId());
                    assertNotNull(transaction.getToAccountId());
                }
                case WITHDRAW -> {
                    assertNotNull(transaction.getFromAccountId());
                    assertNull(transaction.getToAccountId());
                }
                case TRANSFER -> {
                    assertNotNull(transaction.getFromAccountId());
                    assertNotNull(transaction.getToAccountId());
                }
            }
        });
    }

    @Test
    void testFindById() {
        // Тест поиска транзакции по ID
        List<Transaction> allTransactions = transactionDao.findAll();
        assertFalse(allTransactions.isEmpty());
        
        Transaction firstTransaction = allTransactions.get(0);
        Optional<Transaction> found = transactionDao.findById(firstTransaction.getId());
        
        assertTrue(found.isPresent());
        Transaction transaction = found.get();
        assertEquals(firstTransaction.getId(), transaction.getId());
        assertEquals(firstTransaction.getType(), transaction.getType());
        assertEquals(firstTransaction.getAmount(), transaction.getAmount());
        assertEquals(firstTransaction.isReversed(), transaction.isReversed());
    }

    @Test
    void testFindByIdNonExistent() {
        // Тест поиска несуществующей транзакции
        Optional<Transaction> transaction = transactionDao.findById(99999);
        
        assertFalse(transaction.isPresent());
    }

    @Test
    void testMarkReversed() {
        // Тест пометки транзакции как откатанной
        BigDecimal amount = new BigDecimal("30.00");
        transactionDao.insertDeposit(testAccountId, amount);
        
        // Находим созданную транзакцию
        List<Transaction> transactions = transactionDao.findAll();
        Transaction deposit = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEPOSIT
                        && t.getToAccountId() != null
                        && t.getToAccountId() == testAccountId
                        && !t.isReversed())
                .findFirst()
                .orElseThrow();
        
        assertFalse(deposit.isReversed());
        
        // Помечаем как откатанную
        transactionDao.markReversed(deposit.getId());
        
        // Проверяем обновление
        Optional<Transaction> updated = transactionDao.findById(deposit.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isReversed());
    }

    @Test
    void testTransactionProperties() {
        // Тест свойств транзакции
        List<Transaction> transactions = transactionDao.findAll();
        assertFalse(transactions.isEmpty());
        
        Transaction transaction = transactions.get(0);
        
        // Проверяем геттеры
        assertTrue(transaction.getId() > 0);
        assertNotNull(transaction.getType());
        assertNotNull(transaction.getAmount());
        assertTrue(transaction.getAmount().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(transaction.getCreatedAt());
        
        // Проверяем метки счетов
        if (transaction.getFromAccountId() != null) {
            assertNotNull(transaction.getFromLabel());
        }
        if (transaction.getToAccountId() != null) {
            assertNotNull(transaction.getToLabel());
        }
    }

    @Test
    void testTransactionsOrderedByDesc() {
        // Тест, что транзакции отсортированы по убыванию ID
        List<Transaction> transactions = transactionDao.findAll();
        
        if (transactions.size() > 1) {
            for (int i = 0; i < transactions.size() - 1; i++) {
                int currentId = transactions.get(i).getId();
                int nextId = transactions.get(i + 1).getId();
                assertTrue(currentId >= nextId, 
                        "Транзакции должны быть отсортированы по убыванию ID");
            }
        }
    }
}

