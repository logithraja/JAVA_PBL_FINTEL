package com.example.expense_tracker.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "recurring_transactions", indexes = {
        @Index(name = "idx_recurring_user_next", columnList = "user_id, next_execution_date, active")
})
public class RecurringTransaction {

    public enum Frequency { DAILY, WEEKLY, MONTHLY, YEARLY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private TransactionCategory transactionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @NotBlank(message = "Transaction name cannot be blank")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Column(nullable = false)
    private double amount;

    @NotBlank(message = "Transaction type cannot be blank")
    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 50)
    private Frequency frequency;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull(message = "Next execution date is required")
    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;

    @Column(name = "last_execution_date")
    private LocalDate lastExecutionDate;

    @Column(nullable = false)
    private boolean active = true;

    public RecurringTransaction() {}

    public RecurringTransaction(Integer id, User user, TransactionCategory transactionCategory, Account account,
                                String name, double amount, String transactionType, Frequency frequency,
                                LocalDate startDate, LocalDate endDate, LocalDate nextExecutionDate,
                                LocalDate lastExecutionDate, boolean active) {
        this.id = id;
        this.user = user;
        this.transactionCategory = transactionCategory;
        this.account = account;
        this.name = name;
        this.amount = amount;
        this.transactionType = transactionType;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextExecutionDate = nextExecutionDate;
        this.lastExecutionDate = lastExecutionDate;
        this.active = active;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public TransactionCategory getTransactionCategory() { return transactionCategory; }
    public void setTransactionCategory(TransactionCategory transactionCategory) { this.transactionCategory = transactionCategory; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public Frequency getFrequency() { return frequency; }
    public void setFrequency(Frequency frequency) { this.frequency = frequency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public void setNextExecutionDate(LocalDate nextExecutionDate) { this.nextExecutionDate = nextExecutionDate; }

    public LocalDate getLastExecutionDate() { return lastExecutionDate; }
    public void setLastExecutionDate(LocalDate lastExecutionDate) { this.lastExecutionDate = lastExecutionDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
