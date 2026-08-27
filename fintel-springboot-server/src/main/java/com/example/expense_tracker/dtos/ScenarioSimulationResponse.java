package com.example.expense_tracker.dtos;

import java.util.List;
import java.util.Map;

public class ScenarioSimulationResponse {
    private boolean sufficientData;
    private String statusMessage;
    private ScenarioInput input;
    private BaselineForecastResponse baselineForecast;
    private VulnerabilityResponse vulnerabilityAnalysis;
    private List<GoalImpact> goalImpacts;
    private List<CategoryAdjustment> categoryAdjustments;
    private MonthlyProjectionSummary monthlyProjection;
    private String overallSummary;

    public ScenarioSimulationResponse() {}

    public boolean isSufficientData() { return sufficientData; }
    public void setSufficientData(boolean sufficientData) { this.sufficientData = sufficientData; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    public ScenarioInput getInput() { return input; }
    public void setInput(ScenarioInput input) { this.input = input; }
    public BaselineForecastResponse getBaselineForecast() { return baselineForecast; }
    public void setBaselineForecast(BaselineForecastResponse baselineForecast) { this.baselineForecast = baselineForecast; }
    public VulnerabilityResponse getVulnerabilityAnalysis() { return vulnerabilityAnalysis; }
    public void setVulnerabilityAnalysis(VulnerabilityResponse vulnerabilityAnalysis) { this.vulnerabilityAnalysis = vulnerabilityAnalysis; }
    public List<GoalImpact> getGoalImpacts() { return goalImpacts; }
    public void setGoalImpacts(List<GoalImpact> goalImpacts) { this.goalImpacts = goalImpacts; }
    public List<CategoryAdjustment> getCategoryAdjustments() { return categoryAdjustments; }
    public void setCategoryAdjustments(List<CategoryAdjustment> categoryAdjustments) { this.categoryAdjustments = categoryAdjustments; }
    public MonthlyProjectionSummary getMonthlyProjection() { return monthlyProjection; }
    public void setMonthlyProjection(MonthlyProjectionSummary monthlyProjection) { this.monthlyProjection = monthlyProjection; }
    public String getOverallSummary() { return overallSummary; }
    public void setOverallSummary(String overallSummary) { this.overallSummary = overallSummary; }

    public static class ScenarioInput {
        private double amount;
        private String category;
        private int targetMonth;
        private int targetYear;

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public int getTargetMonth() { return targetMonth; }
        public void setTargetMonth(int targetMonth) { this.targetMonth = targetMonth; }
        public int getTargetYear() { return targetYear; }
        public void setTargetYear(int targetYear) { this.targetYear = targetYear; }
    }

    public static class GoalImpact {
        private String goalName;
        private double targetAmount;
        private double currentAmount;
        private String originalDeadline;
        private String projectedDeadline;
        private double monthlySavingNeededBefore;
        private double monthlySavingNeededAfter;
        private double additionalMonthlyBurden;
        private boolean onTrackAfter;

        public String getGoalName() { return goalName; }
        public void setGoalName(String goalName) { this.goalName = goalName; }
        public double getTargetAmount() { return targetAmount; }
        public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
        public double getCurrentAmount() { return currentAmount; }
        public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
        public String getOriginalDeadline() { return originalDeadline; }
        public void setOriginalDeadline(String originalDeadline) { this.originalDeadline = originalDeadline; }
        public String getProjectedDeadline() { return projectedDeadline; }
        public void setProjectedDeadline(String projectedDeadline) { this.projectedDeadline = projectedDeadline; }
        public double getMonthlySavingNeededBefore() { return monthlySavingNeededBefore; }
        public void setMonthlySavingNeededBefore(double monthlySavingNeededBefore) { this.monthlySavingNeededBefore = monthlySavingNeededBefore; }
        public double getMonthlySavingNeededAfter() { return monthlySavingNeededAfter; }
        public void setMonthlySavingNeededAfter(double monthlySavingNeededAfter) { this.monthlySavingNeededAfter = monthlySavingNeededAfter; }
        public double getAdditionalMonthlyBurden() { return additionalMonthlyBurden; }
        public void setAdditionalMonthlyBurden(double additionalMonthlyBurden) { this.additionalMonthlyBurden = additionalMonthlyBurden; }
        public boolean isOnTrackAfter() { return onTrackAfter; }
        public void setOnTrackAfter(boolean onTrackAfter) { this.onTrackAfter = onTrackAfter; }
    }

    public static class CategoryAdjustment {
        private String categoryName;
        private double currentAvgMonthly;
        private double adjustedBudget;
        private double percentCut;
        private boolean isVulnerable;
        private String recommendation;

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public double getCurrentAvgMonthly() { return currentAvgMonthly; }
        public void setCurrentAvgMonthly(double currentAvgMonthly) { this.currentAvgMonthly = currentAvgMonthly; }
        public double getAdjustedBudget() { return adjustedBudget; }
        public void setAdjustedBudget(double adjustedBudget) { this.adjustedBudget = adjustedBudget; }
        public double getPercentCut() { return percentCut; }
        public void setPercentCut(double percentCut) { this.percentCut = percentCut; }
        public boolean isVulnerable() { return isVulnerable; }
        public void setVulnerable(boolean vulnerable) { isVulnerable = vulnerable; }
        public String getRecommendation() { return recommendation; }
        public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    }

    public static class MonthlyProjectionSummary {
        private double totalBaselineNext12Months;
        private double totalWithScenario;
        private double scenarioImpact;
        private Map<String, Double> monthlyBreakdown;

        public double getTotalBaselineNext12Months() { return totalBaselineNext12Months; }
        public void setTotalBaselineNext12Months(double totalBaselineNext12Months) { this.totalBaselineNext12Months = totalBaselineNext12Months; }
        public double getTotalWithScenario() { return totalWithScenario; }
        public void setTotalWithScenario(double totalWithScenario) { this.totalWithScenario = totalWithScenario; }
        public double getScenarioImpact() { return scenarioImpact; }
        public void setScenarioImpact(double scenarioImpact) { this.scenarioImpact = scenarioImpact; }
        public Map<String, Double> getMonthlyBreakdown() { return monthlyBreakdown; }
        public void setMonthlyBreakdown(Map<String, Double> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
    }
}
