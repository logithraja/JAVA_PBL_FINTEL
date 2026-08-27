package com.example.expense_tracker.services;

import com.example.expense_tracker.dtos.BaselineForecastResponse;
import com.example.expense_tracker.dtos.CategoryForecast;
import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.repositories.TransactionRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastService.class);
    public static final int MINIMUM_MONTHS_FOR_REGRESSION = 3;
    public static final int PROJECTION_MONTHS = 12;

    @Autowired
    private TransactionRepository transactionRepository;

    public BaselineForecastResponse computeBaselineForecast(int userId) {
        log.info("Computing baseline forecast for user: {}", userId);

        List<Transaction> allExpenses = fetchAllExpenses(userId);

        Map<YearMonth, Map<String, Double>> monthlyByCategory = aggregateMonthlyByCategory(allExpenses);

        int monthsOfData = monthlyByCategory.size();
        boolean sufficientData = monthsOfData >= MINIMUM_MONTHS_FOR_REGRESSION;

        BaselineForecastResponse response = new BaselineForecastResponse();
        response.setSufficientData(sufficientData);
        response.setMonthsOfDataAvailable(monthsOfData);
        response.setMinimumRequiredMonths(MINIMUM_MONTHS_FOR_REGRESSION);

        if (!sufficientData) {
            response.setStatusMessage(String.format(
                    "Insufficient data: %d months available, %d required. Please record transactions for at least %d different months to enable regression-based forecasting.",
                    monthsOfData, MINIMUM_MONTHS_FOR_REGRESSION, MINIMUM_MONTHS_FOR_REGRESSION));
            response.setCategoryForecasts(Collections.emptyList());
            response.setTotalMonthlyBaseline(Collections.emptyMap());
            response.setTotalProjectedNext12Months(0);
            return response;
        }

        response.setStatusMessage(String.format("Forecast generated from %d months of transaction history.", monthsOfData));

        List<String> sortedMonthKeys = monthlyByCategory.keySet().stream()
                .sorted()
                .map(YearMonth::toString)
                .collect(Collectors.toList());

        Set<String> allCategories = new LinkedHashSet<>();
        for (Map<String, Double> catMap : monthlyByCategory.values()) {
            allCategories.addAll(catMap.keySet());
        }

        List<CategoryForecast> forecasts = new ArrayList<>();
        double totalProjected = 0;
        Map<String, Double> monthlyBaseline = new LinkedHashMap<>();

        for (String categoryName : allCategories) {
            CategoryForecast cf = new CategoryForecast();
            cf.setCategoryName(categoryName);

            List<Double> historical = new ArrayList<>();
            for (String monthKey : sortedMonthKeys) {
                double val = monthlyByCategory.get(YearMonth.parse(monthKey))
                        .getOrDefault(categoryName, 0.0);
                historical.add(val);
            }
            cf.setHistoricalMonthlySpend(historical);

            double avg = historical.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            cf.setAverageMonthlySpend(round(avg));

            SimpleRegression regression = new SimpleRegression();
            for (int i = 0; i < historical.size(); i++) {
                regression.addData(i, historical.get(i));
            }

            double slope = regression.getSlope();
            double intercept = regression.getIntercept();
            double rSq = regression.getRSquare();

            cf.setSlope(round(slope));
            cf.setIntercept(round(intercept));
            cf.setRSquared(round(rSq));

            if (slope > 0.5) {
                cf.setTrend("INCREASING");
            } else if (slope < -0.5) {
                cf.setTrend("DECREASING");
            } else {
                cf.setTrend("STABLE");
            }

            List<Double> projected = new ArrayList<>();
            double projectedTotal = 0;
            for (int i = 0; i < PROJECTION_MONTHS; i++) {
                double val = Math.max(0, intercept + slope * (historical.size() + i));
                projected.add(round(val));
                projectedTotal += val;
            }
            cf.setProjectedMonthlySpend(projected);
            cf.setProjectedTotalNext12Months(round(projectedTotal));
            cf.setSufficientData(true);

            forecasts.add(cf);
            totalProjected += projectedTotal;
        }

        forecasts.sort((a, b) -> Double.compare(b.getProjectedTotalNext12Months(), a.getProjectedTotalNext12Months()));

        for (int i = 0; i < sortedMonthKeys.size(); i++) {
            double totalForMonth = 0;
            String monthKey = sortedMonthKeys.get(i);
            Map<String, Double> catMap = monthlyByCategory.get(YearMonth.parse(monthKey));
            for (double v : catMap.values()) {
                totalForMonth += v;
            }
            monthlyBaseline.put(monthKey, round(totalForMonth));
        }

        response.setCategoryForecasts(forecasts);
        response.setTotalMonthlyBaseline(monthlyBaseline);
        response.setTotalProjectedNext12Months(round(totalProjected));

        return response;
    }

    public BaselineForecastResponse computeForecastForCategory(int userId, String categoryName) {
        BaselineForecastResponse full = computeBaselineForecast(userId);
        if (!full.isSufficientData()) return full;

        full.setCategoryForecasts(full.getCategoryForecasts().stream()
                .filter(cf -> cf.getCategoryName().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList()));
        return full;
    }

    List<Transaction> fetchAllExpenses(int userId) {
        List<Transaction> all = new ArrayList<>();
        List<Integer> years = transactionRepository.findDistinctYears(userId);
        if (years == null || years.isEmpty()) {
            return all;
        }
        for (Integer year : years) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            List<Transaction> yearTx = transactionRepository
                    .findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end);
            if (yearTx != null) {
                for (Transaction t : yearTx) {
                    if ("expense".equalsIgnoreCase(t.getTransactionType())) {
                        all.add(t);
                    }
                }
            }
        }
        return all;
    }

    Map<YearMonth, Map<String, Double>> aggregateMonthlyByCategory(List<Transaction> expenses) {
        Map<YearMonth, Map<String, Double>> result = new TreeMap<>();

        for (Transaction t : expenses) {
            YearMonth ym = YearMonth.from(t.getTransactionDate());
            String catName = t.getTransactionCategory() != null
                    ? t.getTransactionCategory().getCategoryName()
                    : "Uncategorized";

            result.computeIfAbsent(ym, k -> new LinkedHashMap<>())
                    .merge(catName, t.getTransactionAmount(), Double::sum);
        }

        return result;
    }

    public Map<Integer, Double> runMonteCarloSimulation(double baseMonthly, double stdDev, int months, int simulations) {
        org.apache.commons.math3.distribution.NormalDistribution dist =
                new org.apache.commons.math3.distribution.NormalDistribution(baseMonthly, stdDev > 0 ? stdDev : baseMonthly * 0.1);

        Map<Integer, double[]> percentiles = new LinkedHashMap<>();
        double[][] allRuns = new double[simulations][months];

        Random rng = new Random(42);
        for (int s = 0; s < simulations; s++) {
            double cumulative = 0;
            for (int m = 0; m < months; m++) {
                double sampled = Math.max(0, dist.sample());
                cumulative += sampled;
                allRuns[s][m] = cumulative;
            }
        }

        double[] p10 = new double[months];
        double[] p50 = new double[months];
        double[] p90 = new double[months];

        for (int m = 0; m < months; m++) {
            double[] values = new double[simulations];
            for (int s = 0; s < simulations; s++) {
                values[s] = allRuns[s][m];
            }
            Arrays.sort(values);
            p10[m] = values[(int) (simulations * 0.10)];
            p50[m] = values[(int) (simulations * 0.50)];
            p90[m] = values[(int) (simulations * 0.90)];
        }

        Map<Integer, Double> summary = new LinkedHashMap<>();
        summary.put(Integer.valueOf(0), round(p50[months - 1]));
        summary.put(Integer.valueOf(10), round(p10[months - 1]));
        summary.put(Integer.valueOf(90), round(p90[months - 1]));
        return summary;
    }

    private static double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
