package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public class TransactionService {
    private final TransactionDao transactionDao = new TransactionDao();
    private final AccountDao accountDao = new AccountDao();

    public List<Transaction> list() {
        return transactionDao.findAll();
    }

    public void rollback(int id) {
        Transaction tx = transactionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Транзакция не найдена"));
        if (tx.isReversed()) {
            throw new IllegalStateException("Уже откатано");
        }

        switch (tx.getType()) {
            case DEPOSIT -> rollbackDeposit(tx);
            case WITHDRAW -> rollbackWithdraw(tx);
            case TRANSFER -> rollbackTransfer(tx);
            default -> throw new IllegalStateException("Неизвестный тип");
        }
        transactionDao.markReversed(id);
    }

    private void rollbackDeposit(Transaction tx) {
        Account acc = accountDao.findById(tx.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
        BigDecimal newBalance = acc.getBalance().subtract(tx.getAmount());
        accountDao.updateBalance(acc.getId(), newBalance);
    }

    private void rollbackWithdraw(Transaction tx) {
        Account acc = accountDao.findById(tx.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
        BigDecimal newBalance = acc.getBalance().add(tx.getAmount());
        accountDao.updateBalance(acc.getId(), newBalance);
    }

    private void rollbackTransfer(Transaction tx) {
        Account from = accountDao.findById(tx.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Счет отправителя не найден"));
        Account to = accountDao.findById(tx.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Счет получателя не найден"));
        if (to.getBalance().compareTo(tx.getAmount()) < 0) {
            throw new IllegalArgumentException("Недостаточно средств для отката");
        }
        accountDao.updateBalance(from.getId(), from.getBalance().add(tx.getAmount()));
        accountDao.updateBalance(to.getId(), to.getBalance().subtract(tx.getAmount()));
    }
}

