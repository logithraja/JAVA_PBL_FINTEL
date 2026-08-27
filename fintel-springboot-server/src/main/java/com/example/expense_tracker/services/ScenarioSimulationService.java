package com.example.expense_tracker.services;

import com.example.expense_tracker.dtos.*;
import com.example.expense_tracker.entities.SavingsGoal;
import com.example.expense_tracker.repositories.SavingsGoalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScenarioSimulationService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioSimulationService.class);

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private VulnerabilityClassifier vulnerabilityClassifier;

    @Autowired
    private SavingsGoalRepository savingsGoalRepository;

    public ScenarioSimulationResponse simulate(int userId, double amount, String category, int targetMonth, Integer targetYear) {
        log.info("Simulating scenario: user={}, amount={}, category={}, month={}/{}", userId, amount, category, targetMonth, targetYear);

        int year = (targetYear != null) ? targetYear : LocalDate.now().getYear();

        BaselineForecastResponse baseline = forecastService.computeBaselineForecast(userId);
        VulnerabilityResponse vulnerability = vulnerabilityClassifier.classify(userId);

        ScenarioSimulationResponse response = new ScenarioSimulationResponse();
        response.setSufficientData(baseline.isSufficientData());
        response.setBaselineForecast(baseline);
        response.setVulnerabilityAnalysis(vulnerability);

        ScenarioSimulationResponse.ScenarioInput input = new ScenarioSimulationResponse.ScenarioInput();
        input.setAmount(amount);
        input.setCategory(category);
        input.setTargetMonth(targetMonth);
        input.setTargetYear(year);
        response.setInput(input);

        if (!baseline.isSufficientData()) {
            response.setStatusMessage(String.format(
                    "Simulation completed with limited data (%d of %d months). Projections are estimates.",
                    baseline.getMonthsOfDataAvailable(), baseline.getMinimumRequiredMonths()));
            response.setGoalImpacts(Collections.emptyList());
            response.setCategoryAdjustments(Collections.emptyList());
            response.setMonthlyProjection(buildMinimalProjection(baseline, amount, category, targetMonth));
            response.setOverallSummary("Insufficient transaction history for precise simulation. Record more transactions to improve accuracy.");
            return response;
        }

        List<ScenarioSimulationResponse.GoalImpact> goalImpacts = computeGoalImpacts(userId, amount, targetMonth, year);
        response.setGoalImpacts(goalImpacts);

        List<ScenarioSimulationResponse.CategoryAdjustment> adjustments = computeCategoryAdjustments(
                baseline, vulnerability, category, amount, targetMonth);
        response.setCategoryAdjustments(adjustments);

        ScenarioSimulationResponse.MonthlyProjectionSummary projection = computeMonthlyProjection(
                baseline, amount, category, targetMonth, year);
        response.setMonthlyProjection(projection);

        response.setStatusMessage("Simulation complete.");
        response.setOverallSummary(buildOverallSummary(amount, category, targetMonth, year, goalImpacts, adjustments, projection));

        return response;
    }

    private List<ScenarioSimulationResponse.GoalImpact> computeGoalImpacts(int userId, double scenarioAmount, int targetMonth, int year) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserId(userId);
        List<ScenarioSimulationResponse.GoalImpact> impacts = new ArrayList<>();

        if (goals == null || goals.isEmpty()) return impacts;

        for (SavingsGoal goal : goals) {
            if (goal.isCompleted()) continue;

            ScenarioSimulationResponse.GoalImpact impact = new ScenarioSimulationResponse.GoalImpact();
            impact.setGoalName(goal.getName());
            impact.setTargetAmount(goal.getTargetAmount().doubleValue());
            impact.setCurrentAmount(goal.getCurrentAmount().doubleValue());

            double remaining = goal.getTargetAmount().doubleValue() - goal.getCurrentAmount().doubleValue();
            if (remaining <= 0) {
                impact.setOnTrackAfter(true);
                impact.setOriginalDeadline(goal.getDeadline() != null ? goal.getDeadline().toString() : "N/A");
                impact.setProjectedDeadline(goal.getDeadline() != null ? goal.getDeadline().toString() : "N/A");
                impact.setMonthlySavingNeededBefore(0);
                impact.setMonthlySavingNeededAfter(0);
                impact.setAdditionalMonthlyBurden(0);
                impacts.add(impact);
                continue;
            }

            LocalDate deadline = goal.getDeadline();
            impact.setOriginalDeadline(deadline != null ? deadline.toString() : "N/A");

            if (deadline != null) {
                long monthsLeft = ChronoUnit.MONTHS.between(LocalDate.now(), deadline);
                if (monthsLeft < 1) monthsLeft = 1;

                double monthlyNeededBefore = remaining / monthsLeft;
                impact.setMonthlySavingNeededBefore(Math.round(monthlyNeededBefore * 100.0) / 100.0);

                double monthlyNeededAfter = (remaining + scenarioAmount) / monthsLeft;
                impact.setMonthlySavingNeededAfter(Math.round(monthlyNeededAfter * 100.0) / 100.0);

                impact.setAdditionalMonthlyBurden(Math.round((monthlyNeededAfter - monthlyNeededBefore) * 100.0) / 100.0);

                LocalDate newDeadline = LocalDate.now().plusMonths((long) Math.ceil((remaining + scenarioAmount) / monthlyNeededBefore));
                impact.setProjectedDeadline(newDeadline.toString());
                impact.setOnTrackAfter(newDeadline.isBefore(deadline) || newDeadline.isEqual(deadline));
            } else {
                double monthlyNeededBefore = remaining / 12;
                double monthlyNeededAfter = (remaining + scenarioAmount) / 12;
                impact.setMonthlySavingNeededBefore(Math.round(monthlyNeededBefore * 100.0) / 100.0);
                impact.setMonthlySavingNeededAfter(Math.round(monthlyNeededAfter * 100.0) / 100.0);
                impact.setAdditionalMonthlyBurden(Math.round((monthlyNeededAfter - monthlyNeededBefore) * 100.0) / 100.0);
                impact.setProjectedDeadline("No deadline set");
                impact.setOnTrackAfter(true);
            }

            impacts.add(impact);
        }

        return impacts;
    }

    private List<ScenarioSimulationResponse.CategoryAdjustment> computeCategoryAdjustments(
            BaselineForecastResponse baseline, VulnerabilityResponse vulnerability,
            String scenarioCategory, double scenarioAmount, int targetMonth) {

        List<ScenarioSimulationResponse.CategoryAdjustment> adjustments = new ArrayList<>();

        Set<String> vulnerableCategories = new HashSet<>();
        if (vulnerability.getVulnerableCategories() != null) {
            for (VulnerabilityResult vr : vulnerability.getVulnerableCategories()) {
                vulnerableCategories.add(vr.getCategoryName().toLowerCase());
            }
        }

        for (CategoryForecast cf : baseline.getCategoryForecasts()) {
            ScenarioSimulationResponse.CategoryAdjustment adj = new ScenarioSimulationResponse.CategoryAdjustment();
            adj.setCategoryName(cf.getCategoryName());
            adj.setCurrentAvgMonthly(cf.getAverageMonthlySpend());
            adj.setVulnerable(vulnerableCategories.contains(cf.getCategoryName().toLowerCase()));

            if (cf.getCategoryName().equalsIgnoreCase(scenarioCategory)) {
                adj.setAdjustedBudget(cf.getAverageMonthlySpend() + scenarioAmount);
                adj.setPercentCut(0);
                adj.setRecommendation(String.format(
                        "This is the target category. Expected one-time increase of ₹%.0f in month %d.",
                        scenarioAmount, targetMonth));
            } else if (adj.isVulnerable()) {
                double cutAmount = scenarioAmount * 0.15;
                double adjusted = Math.max(0, cf.getAverageMonthlySpend() - cutAmount);
                adj.setAdjustedBudget(Math.round(adjusted * 100.0) / 100.0);
                adj.setPercentCut(Math.round((cutAmount / cf.getAverageMonthlySpend()) * 10000.0) / 100.0);
                adj.setRecommendation(String.format(
                        "Vulnerable category — consider reducing by ~%.0f%% (₹%.0f/mo) to offset the scenario.",
                        adj.getPercentCut(), cutAmount));
            } else {
                adj.setAdjustedBudget(cf.getAverageMonthlySpend());
                adj.setPercentCut(0);
                adj.setRecommendation("No adjustment needed — spending in this category is stable.");
            }

            adjustments.add(adj);
        }

        adjustments.sort((a, b) -> Double.compare(b.getPercentCut(), a.getPercentCut()));
        return adjustments;
    }

    private ScenarioSimulationResponse.MonthlyProjectionSummary computeMonthlyProjection(
            BaselineForecastResponse baseline, double scenarioAmount, String scenarioCategory,
            int targetMonth, int year) {

        ScenarioSimulationResponse.MonthlyProjectionSummary summary = new ScenarioSimulationResponse.MonthlyProjectionSummary();
        summary.setTotalBaselineNext12Months(baseline.getTotalProjectedNext12Months());

        double totalWithScenario = baseline.getTotalProjectedNext12Months() + scenarioAmount;
        summary.setTotalWithScenario(totalWithScenario);
        summary.setScenarioImpact(scenarioAmount);

        Map<String, Double> breakdown = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            double monthTotal = 0;
            for (CategoryForecast cf : baseline.getCategoryForecasts()) {
                if (cf.getProjectedMonthlySpend() != null && m <= cf.getProjectedMonthlySpend().size()) {
                    monthTotal += cf.getProjectedMonthlySpend().get(m - 1);
                }
            }
            if (m == targetMonth) {
                monthTotal += scenarioAmount;
            }
            breakdown.put(String.format("%d-%02d", year, m), Math.round(monthTotal * 100.0) / 100.0);
        }
        summary.setMonthlyBreakdown(breakdown);

        return summary;
    }

    private ScenarioSimulationResponse.MonthlyProjectionSummary buildMinimalProjection(
            BaselineForecastResponse baseline, double scenarioAmount, String scenarioCategory, int targetMonth) {

        ScenarioSimulationResponse.MonthlyProjectionSummary summary = new ScenarioSimulationResponse.MonthlyProjectionSummary();
        summary.setTotalBaselineNext12Months(0);
        summary.setTotalWithScenario(scenarioAmount);
        summary.setScenarioImpact(scenarioAmount);
        summary.setMonthlyBreakdown(Collections.emptyMap());
        return summary;
    }

    private String buildOverallSummary(double amount, String category, int targetMonth, int year,
                                        List<ScenarioSimulationResponse.GoalImpact> goalImpacts,
                                        List<ScenarioSimulationResponse.CategoryAdjustment> adjustments,
                                        ScenarioSimulationResponse.MonthlyProjectionSummary projection) {

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Scenario: ₹%.0f on %s in %d-%02d. ", amount, category, year, targetMonth));

        long vulnerableCount = adjustments.stream().filter(ScenarioSimulationResponse.CategoryAdjustment::isVulnerable).count();
        if (vulnerableCount > 0) {
            sb.append(String.format("%d category(s) flagged as vulnerable and may need cuts. ", vulnerableCount));
        }

        if (!goalImpacts.isEmpty()) {
            long delayedGoals = goalImpacts.stream().filter(g -> !g.isOnTrackAfter()).count();
            if (delayedGoals > 0) {
                sb.append(String.format("%d savings goal(s) will be delayed. ", delayedGoals));
            } else {
                sb.append("Savings goals remain on track. ");
            }
        }

        sb.append(String.format("Total 12-month projected spend: ₹%.0f (baseline ₹%.0f + ₹%.0f scenario).",
                projection.getTotalWithScenario(), projection.getTotalBaselineNext12Months(), amount));

        return sb.toString();
    }
}
