package com.example.expense_tracker.dtos;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpendingForecastResponse {
    private double spentSoFar;
    private double dailyAvgRate;
    private double projectedTotal;
    private int daysElapsed;
    private int daysRemaining;
    private int totalDaysInMonth;
    private Double budget;
    private Double projectedOverUnder;
    private String status;
    private String message;

    public SpendingForecastResponse() {}

    public SpendingForecastResponse(double spentSoFar, double dailyAvgRate, double projectedTotal,
                                    int daysElapsed, int daysRemaining, int totalDaysInMonth,
                                    Double budget, Double projectedOverUnder, String status, String message) {
        this.spentSoFar = round(spentSoFar);
        this.dailyAvgRate = round(dailyAvgRate);
        this.projectedTotal = round(projectedTotal);
        this.daysElapsed = daysElapsed;
        this.daysRemaining = daysRemaining;
        this.totalDaysInMonth = totalDaysInMonth;
        this.budget = budget != null ? round(budget) : null;
        this.projectedOverUnder = projectedOverUnder != null ? round(projectedOverUnder) : null;
        this.status = status;
        this.message = message;
    }

    private static double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public double getSpentSoFar() {
        return spentSoFar;
    }

    public void setSpentSoFar(double spentSoFar) {
        this.spentSoFar = spentSoFar;
    }

    public double getDailyAvgRate() {
        return dailyAvgRate;
    }

    public void setDailyAvgRate(double dailyAvgRate) {
        this.dailyAvgRate = dailyAvgRate;
    }

    public double getProjectedTotal() {
        return projectedTotal;
    }

    public void setProjectedTotal(double projectedTotal) {
        this.projectedTotal = projectedTotal;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    public void setDaysElapsed(int daysElapsed) {
        this.daysElapsed = daysElapsed;
    }

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public int getTotalDaysInMonth() {
        return totalDaysInMonth;
    }

    public void setTotalDaysInMonth(int totalDaysInMonth) {
        this.totalDaysInMonth = totalDaysInMonth;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getProjectedOverUnder() {
        return projectedOverUnder;
    }

    public void setProjectedOverUnder(Double projectedOverUnder) {
        this.projectedOverUnder = projectedOverUnder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
