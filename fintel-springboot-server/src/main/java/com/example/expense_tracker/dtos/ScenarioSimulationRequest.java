package com.example.expense_tracker.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ScenarioSimulationRequest {
    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Target month is required")
    @Min(value = 1, message = "Target month must be 1-12")
    private Integer targetMonth;

    private Integer targetYear;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getTargetMonth() { return targetMonth; }
    public void setTargetMonth(Integer targetMonth) { this.targetMonth = targetMonth; }
    public Integer getTargetYear() { return targetYear; }
    public void setTargetYear(Integer targetYear) { this.targetYear = targetYear; }
}
