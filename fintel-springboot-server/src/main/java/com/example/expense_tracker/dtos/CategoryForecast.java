package com.example.expense_tracker.dtos;

import java.util.List;

public class CategoryForecast {
    private String categoryName;
    private String categoryId;
    private boolean sufficientData;
    private List<Double> historicalMonthlySpend;
    private List<Double> projectedMonthlySpend;
    private double slope;
    private double intercept;
    private double rSquared;
    private String trend;
    private double averageMonthlySpend;
    private double projectedTotalNext12Months;

    public CategoryForecast() {}

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public boolean isSufficientData() { return sufficientData; }
    public void setSufficientData(boolean sufficientData) { this.sufficientData = sufficientData; }
    public List<Double> getHistoricalMonthlySpend() { return historicalMonthlySpend; }
    public void setHistoricalMonthlySpend(List<Double> historicalMonthlySpend) { this.historicalMonthlySpend = historicalMonthlySpend; }
    public List<Double> getProjectedMonthlySpend() { return projectedMonthlySpend; }
    public void setProjectedMonthlySpend(List<Double> projectedMonthlySpend) { this.projectedMonthlySpend = projectedMonthlySpend; }
    public double getSlope() { return slope; }
    public void setSlope(double slope) { this.slope = slope; }
    public double getIntercept() { return intercept; }
    public void setIntercept(double intercept) { this.intercept = intercept; }
    public double getRSquared() { return rSquared; }
    public void setRSquared(double rSquared) { this.rSquared = rSquared; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public double getAverageMonthlySpend() { return averageMonthlySpend; }
    public void setAverageMonthlySpend(double averageMonthlySpend) { this.averageMonthlySpend = averageMonthlySpend; }
    public double getProjectedTotalNext12Months() { return projectedTotalNext12Months; }
    public void setProjectedTotalNext12Months(double projectedTotalNext12Months) { this.projectedTotalNext12Months = projectedTotalNext12Months; }
}
