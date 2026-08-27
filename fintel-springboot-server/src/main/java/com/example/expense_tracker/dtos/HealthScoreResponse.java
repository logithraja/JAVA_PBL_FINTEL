package com.example.expense_tracker.dtos;

import java.util.List;

public class HealthScoreResponse {

    private boolean sufficientData;
    private String statusMessage;
    private int overallScore;
    private String grade;
    private ComponentScore savingsRate;
    private ComponentScore spendingConsistency;
    private ComponentScore goalProgress;
    private ComponentScore budgetAdherence;
    private String summary;
    private List<String> recommendations;
    private int monthsAnalyzed;

    public HealthScoreResponse() {}

    public boolean isSufficientData() { return sufficientData; }
    public void setSufficientData(boolean sufficientData) { this.sufficientData = sufficientData; }

    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }

    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public ComponentScore getSavingsRate() { return savingsRate; }
    public void setSavingsRate(ComponentScore savingsRate) { this.savingsRate = savingsRate; }

    public ComponentScore getSpendingConsistency() { return spendingConsistency; }
    public void setSpendingConsistency(ComponentScore spendingConsistency) { this.spendingConsistency = spendingConsistency; }

    public ComponentScore getGoalProgress() { return goalProgress; }
    public void setGoalProgress(ComponentScore goalProgress) { this.goalProgress = goalProgress; }

    public ComponentScore getBudgetAdherence() { return budgetAdherence; }
    public void setBudgetAdherence(ComponentScore budgetAdherence) { this.budgetAdherence = budgetAdherence; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public int getMonthsAnalyzed() { return monthsAnalyzed; }
    public void setMonthsAnalyzed(int monthsAnalyzed) { this.monthsAnalyzed = monthsAnalyzed; }

    public static class ComponentScore {
        private String name;
        private int score;
        private double rawValue;
        private String label;
        private String detail;

        public ComponentScore() {}

        public ComponentScore(String name, int score, double rawValue, String label, String detail) {
            this.name = name;
            this.score = score;
            this.rawValue = rawValue;
            this.label = label;
            this.detail = detail;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public double getRawValue() { return rawValue; }
        public void setRawValue(double rawValue) { this.rawValue = rawValue; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
    }
}
