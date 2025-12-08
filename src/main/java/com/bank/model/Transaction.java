package com.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private TransactionType type;
    private Integer fromAccountId;
    private Integer toAccountId;
    private BigDecimal amount;
    private boolean reversed;
    private LocalDateTime createdAt;
    private String fromLabel;
    private String toLabel;

    public Transaction(int id, TransactionType type, Integer fromAccountId, Integer toAccountId,
                       BigDecimal amount, boolean reversed, LocalDateTime createdAt,
                       String fromLabel, String toLabel) {
        this.id = id;
        this.type = type;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.reversed = reversed;
        this.createdAt = createdAt;
        this.fromLabel = fromLabel;
        this.toLabel = toLabel;
    }

    public int getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public Integer getFromAccountId() {
        return fromAccountId;
    }

    public Integer getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isReversed() {
        return reversed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFromLabel() {
        return fromLabel;
    }

    public String getToLabel() {
        return toLabel;
    }
}

