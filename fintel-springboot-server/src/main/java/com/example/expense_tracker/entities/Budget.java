package com.example.expense_tracker.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "budgets", indexes = {
        @Index(name = "idx_budget_user_category", columnList = "user_id, category")
})
public class Budget {

    public enum PeriodType { MONTHLY, QUARTERLY, YEARLY }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @Column(nullable = false, length = 120)
    private String category;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit amount must be greater than zero")
    @Column(name = "limit_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal limitAmount = BigDecimal.ZERO;

    @Column(name = "spent_amount", precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private int year;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "`month`")
    private Integer month;

    private Integer quarter;

    @Column(nullable = false)
    private boolean rollover = false;

    public Budget() {}

    public Budget(Integer id, User user, String category, BigDecimal limitAmount,
                  BigDecimal spentAmount, int year, PeriodType periodType, Integer month, Integer quarter) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount == null ? BigDecimal.ZERO : spentAmount;
        this.year = year;
        this.periodType = periodType;
        this.month = month;
        this.quarter = quarter;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public boolean isRollover() { return rollover; }
    public void setRollover(boolean rollover) { this.rollover = rollover; }
}
