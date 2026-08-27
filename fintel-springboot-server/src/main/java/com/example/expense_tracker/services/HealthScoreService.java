package com.example.expense_tracker.services;

import com.example.expense_tracker.dtos.HealthScoreResponse;
import com.example.expense_tracker.dtos.HealthScoreResponse.ComponentScore;
import com.example.expense_tracker.entities.SavingsGoal;
import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.repositories.SavingsGoalRepository;
import com.example.expense_tracker.repositories.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HealthScoreService {

    private static final Logger log = LoggerFactory.getLogger(HealthScoreService.class);

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SavingsGoalRepository savingsGoalRepository;

    private static final int MIN_MONTHS = 3;

    public HealthScoreResponse compute(int userId) {
        HealthScoreResponse response = new HealthScoreResponse();

        LocalDate sixMonthsAgo = YearMonth.now().minusMonths(6).atDay(1);
        List<Transaction> transactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, sixMonthsAgo, LocalDate.now());

        if (transactions.isEmpty()) {
            response.setSufficientData(false);
            response.setStatusMessage("No transaction data found. Add at least " + MIN_MONTHS + " months of transactions.");
            response.setOverallScore(0);
            response.setGrade("N/A");
            response.setMonthsAnalyzed(0);
            return response;
        }

        Map<YearMonth, double[]> monthlyTotals = buildMonthlyTotals(transactions);
        int monthsAnalyzed = monthlyTotals.size();

        if (monthsAnalyzed < MIN_MONTHS) {
            response.setSufficientData(false);
            response.setStatusMessage("Only " + monthsAnalyzed + " month(s) of data. Need at least " + MIN_MONTHS + " months.");
            response.setOverallScore(0);
            response.setGrade("N/A");
            response.setMonthsAnalyzed(monthsAnalyzed);
            return response;
        }

        response.setSufficientData(true);
        response.setMonthsAnalyzed(monthsAnalyzed);

        ComponentScore savingsRate = computeSavingsRate(monthlyTotals);
        ComponentScore consistency = computeSpendingConsistency(monthlyTotals);
        ComponentScore goalProg = computeGoalProgress(userId);
        ComponentScore budgetAdh = computeBudgetAdherence(monthlyTotals);

        response.setSavingsRate(savingsRate);
        response.setSpendingConsistency(consistency);
        response.setGoalProgress(goalProg);
        response.setBudgetAdherence(budgetAdh);

        int overall = (int) Math.round(
                savingsRate.getScore() * 0.30 +
                consistency.getScore() * 0.25 +
                goalProg.getScore() * 0.25 +
                budgetAdh.getScore() * 0.20
        );
        response.setOverallScore(clamp(overall));
        response.setGrade(gradeFor(overall));

        response.setRecommendations(buildRecommendations(savingsRate, consistency, goalProg, budgetAdh));
        response.setSummary(buildSummary(response));

        return response;
    }

    private Map<YearMonth, double[]> buildMonthlyTotals(List<Transaction> txns) {
        Map<YearMonth, double[]> map = new LinkedHashMap<>();
        for (Transaction t : txns) {
            YearMonth ym = YearMonth.from(t.getTransactionDate());
            double[] totals = map.computeIfAbsent(ym, k -> new double[]{0, 0});
            if ("income".equalsIgnoreCase(t.getTransactionType())) {
                totals[0] += t.getTransactionAmount();
            } else {
                totals[1] += t.getTransactionAmount();
            }
        }
        return map;
    }

    private ComponentScore computeSavingsRate(Map<YearMonth, double[]> monthlyTotals) {
        double totalIncome = 0;
        double totalExpense = 0;
        for (double[] pair : monthlyTotals.values()) {
            totalIncome += pair[0];
            totalExpense += pair[1];
        }

        if (totalIncome == 0) {
            return new ComponentScore("Savings Rate", 0, 0, "No Income", "No income transactions recorded.");
        }

        double rate = (totalIncome - totalExpense) / totalIncome;
        double pct = rate * 100;
        int score;
        if (pct >= 20) score = 100;
        else if (pct >= 10) score = 80;
        else if (pct >= 5) score = 60;
        else if (pct >= 0) score = 40;
        else score = 10;

        String label = String.format("%.1f%% saved", pct);
        String detail;
        if (pct >= 20) detail = "Excellent — you're saving more than 20% of your income.";
        else if (pct >= 10) detail = "Good — you're saving a healthy portion of income.";
        else if (pct >= 5) detail = "Moderate — consider increasing savings to 10%+ of income.";
        else if (pct >= 0) detail = "Low — you're barely breaking even. Look for expenses to trim.";
        else detail = "Negative — you're spending more than you earn. Urgent action needed.";

        return new ComponentScore("Savings Rate", score, pct, label, detail);
    }

    private ComponentScore computeSpendingConsistency(Map<YearMonth, double[]> monthlyTotals) {
        List<Double> expenses = monthlyTotals.values().stream()
                .map(p -> p[1])
                .collect(Collectors.toList());

        if (expenses.size() < 2) {
            return new ComponentScore("Spending Consistency", 50, 0, "Insufficient data", "Need more months to assess consistency.");
        }

        double mean = expenses.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        if (mean == 0) {
            return new ComponentScore("Spending Consistency", 100, 0, "No spending", "No expenses recorded.");
        }

        double variance = expenses.stream()
                .mapToDouble(e -> Math.pow(e - mean, 2))
                .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = stdDev / mean;

        int score;
        if (cv <= 0.10) score = 100;
        else if (cv <= 0.20) score = 80;
        else if (cv <= 0.35) score = 60;
        else if (cv <= 0.50) score = 40;
        else score = 20;

        String label = String.format("CV: %.2f", cv);
        String detail;
        if (cv <= 0.10) detail = "Very consistent — your spending is stable month to month.";
        else if (cv <= 0.20) detail = "Fairly consistent — minor fluctuations in spending.";
        else if (cv <= 0.35) detail = "Moderately variable — some months see large spending swings.";
        else detail = "Highly variable — unpredictable spending makes budgeting difficult.";

        return new ComponentScore("Spending Consistency", score, cv, label, detail);
    }

    private ComponentScore computeGoalProgress(int userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);

        if (goals.isEmpty()) {
            return new ComponentScore("Goal Progress", 50, 0, "No goals", "Set savings goals to track progress.");
        }

        double totalProgress = 0;
        int count = 0;
        for (SavingsGoal goal : goals) {
            if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) continue;
            double pct = goal.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 1, RoundingMode.HALF_UP)
                    .doubleValue();
            pct = Math.min(pct, 100);
            totalProgress += pct;
            count++;
        }

        if (count == 0) {
            return new ComponentScore("Goal Progress", 50, 0, "No valid goals", "Set target amounts for your goals.");
        }

        double avgProgress = totalProgress / count;
        int score;
        if (avgProgress >= 75) score = 100;
        else if (avgProgress >= 50) score = 80;
        else if (avgProgress >= 25) score = 60;
        else if (avgProgress >= 10) score = 40;
        else score = 20;

        String label = String.format("%.0f%% avg progress", avgProgress);
        String detail;
        if (avgProgress >= 75) detail = "Excellent — you're close to reaching your goals.";
        else if (avgProgress >= 50) detail = "Good progress — you're over halfway to your goals.";
        else if (avgProgress >= 25) detail = "Getting there — consider increasing contributions.";
        else detail = "Early stages — keep up the momentum on your savings goals.";

        return new ComponentScore("Goal Progress", score, avgProgress, label, detail);
    }

    private ComponentScore computeBudgetAdherence(Map<YearMonth, double[]> monthlyTotals) {
        if (monthlyTotals.size() < 2) {
            return new ComponentScore("Budget Adherence", 50, 0, "Insufficient data", "Need more months to assess adherence.");
        }

        double[] expenses = monthlyTotals.values().stream()
                .mapToDouble(p -> p[1])
                .toArray();

        double mean = Arrays.stream(expenses).average().orElse(0);
        if (mean == 0) {
            return new ComponentScore("Budget Adherence", 100, 100, "No spending", "No expenses to assess.");
        }

        int monthsOnBudget = 0;
        for (double e : expenses) {
            if (e <= mean) monthsOnBudget++;
        }
        double adherenceRate = (double) monthsOnBudget / expenses.length;
        double pct = adherenceRate * 100;

        int score;
        if (pct >= 80) score = 100;
        else if (pct >= 60) score = 80;
        else if (pct >= 40) score = 60;
        else score = 30;

        String label = String.format("%.0f%% months on budget", pct);
        String detail;
        if (pct >= 80) detail = "Excellent — you stay within budget most months.";
        else if (pct >= 60) detail = "Good — some months go over but generally you're consistent.";
        else detail = "Needs improvement — you frequently exceed your average spending.";

        return new ComponentScore("Budget Adherence", score, pct, label, detail);
    }

    private List<String> buildRecommendations(ComponentScore savings, ComponentScore consistency,
                                               ComponentScore goals, ComponentScore adherence) {
        List<String> recs = new ArrayList<>();
        if (savings.getScore() < 60) recs.add("Try to save at least 10% of your income each month.");
        if (consistency.getScore() < 60) recs.add("Track recurring expenses to stabilize your monthly spending.");
        if (goals.getScore() < 60) recs.add("Set or update savings goals to stay motivated.");
        if (adherence.getScore() < 60) recs.add("Create a monthly budget and check it against your actual spending.");
        if (recs.isEmpty()) recs.add("You're doing great — keep up the strong financial habits!");
        return recs;
    }

    private String buildSummary(HealthScoreResponse r) {
        int s = r.getOverallScore();
        if (s >= 80) return "Your finances are in great shape! Strong savings rate and consistent spending keep you on track.";
        if (s >= 60) return "You're doing well overall with a few areas to improve. Focus on the suggestions below.";
        if (s >= 40) return "There's room for improvement. Small changes to your spending and savings can make a big difference.";
        return "Your finances need attention. Review the recommendations and take one step at a time.";
    }

    private String gradeFor(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C+";
        if (score >= 40) return "C";
        if (score >= 30) return "D";
        return "F";
    }

    private int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}
