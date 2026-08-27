package com.example.expense_tracker.dtos;

import java.util.List;
import java.util.Map;

public class BaselineForecastResponse {
    private boolean sufficientData;
    private int monthsOfDataAvailable;
    private int minimumRequiredMonths;
    private String statusMessage;
    private List<CategoryForecast> categoryForecasts;
    private Map<String, Double> totalMonthlyBaseline;
    private double totalProjectedNext12Months;

    public BaselineForecastResponse() {}

    public boolean isSufficientData() { return sufficientData; }
    public void setSufficientData(boolean sufficientData) { this.sufficientData = sufficientData; }
    public int getMonthsOfDataAvailable() { return monthsOfDataAvailable; }
    public void setMonthsOfDataAvailable(int monthsOfDataAvailable) { this.monthsOfDataAvailable = monthsOfDataAvailable; }
    public int getMinimumRequiredMonths() { return minimumRequiredMonths; }
    public void setMinimumRequiredMonths(int minimumRequiredMonths) { this.minimumRequiredMonths = minimumRequiredMonths; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    public List<CategoryForecast> getCategoryForecasts() { return categoryForecasts; }
    public void setCategoryForecasts(List<CategoryForecast> categoryForecasts) { this.categoryForecasts = categoryForecasts; }
    public Map<String, Double> getTotalMonthlyBaseline() { return totalMonthlyBaseline; }
    public void setTotalMonthlyBaseline(Map<String, Double> totalMonthlyBaseline) { this.totalMonthlyBaseline = totalMonthlyBaseline; }
    public double getTotalProjectedNext12Months() { return totalProjectedNext12Months; }
    public void setTotalProjectedNext12Months(double totalProjectedNext12Months) { this.totalProjectedNext12Months = totalProjectedNext12Months; }
}
