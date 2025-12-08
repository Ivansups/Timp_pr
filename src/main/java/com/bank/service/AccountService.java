package com.bank.service;

import com.bank.dao.AccountDao;
import com.bank.dao.TransactionDao;
import com.bank.model.Account;
import com.bank.model.User;

import java.math.BigDecimal;
import java.util.List;

public class AccountService {
    private final AccountDao accountDao = new AccountDao();
    private final TransactionDao transactionDao = new TransactionDao();

    public List<Account> getAccounts(User user) {
        return accountDao.findByUserId(user.getId());
    }

    public List<Account> getOtherAccounts(User user) {
        return accountDao.findAllExceptUser(user.getId());
    }

    public Account deposit(int accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть > 0");
        }
        var account = accountDao.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
        account.setBalance(account.getBalance().add(amount));
        accountDao.updateBalance(accountId, account.getBalance());
        transactionDao.insertDeposit(accountId, amount);
        return account;
    }

    public Account withdraw(int accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть > 0");
        }
        var account = accountDao.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Счет не найден"));
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountDao.updateBalance(accountId, account.getBalance());
        transactionDao.insertWithdraw(accountId, amount);
        return account;
    }

    public void transfer(int fromId, int toId, BigDecimal amount) {
        if (fromId == toId) {
            throw new IllegalArgumentException("Нельзя перевести на свой счет");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть > 0");
        }
        accountDao.transfer(fromId, toId, amount);
        transactionDao.insertTransfer(fromId, toId, amount);
    }
}

