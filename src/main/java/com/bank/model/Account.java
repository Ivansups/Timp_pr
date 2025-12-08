package com.bank.model;

import java.math.BigDecimal;

public class Account {
    private int id;
    private int userId;
    private String name;
    private String iban;
    private BigDecimal balance;
    private String owner;

    public Account(int id, int userId, String name, String iban, BigDecimal balance, String owner) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.iban = iban;
        this.balance = balance;
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return name + " · " + iban;
    }
}

