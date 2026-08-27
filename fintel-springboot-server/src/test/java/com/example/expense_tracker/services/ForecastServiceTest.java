package com.example.expense_tracker.services;

import com.example.expense_tracker.dtos.BaselineForecastResponse;
import com.example.expense_tracker.dtos.CategoryForecast;
import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.repositories.TransactionRepository;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ForecastServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ForecastService forecastService;

    private User testUser;
    private TransactionCategory foodCategory;
    private TransactionCategory transportCategory;

    @BeforeEach
    void setUp() {
        testUser = new User(1, "Test User", "test@test.com", "pass", null);
        foodCategory = new TransactionCategory(1, testUser, "Food", "#FF0000");
        transportCategory = new TransactionCategory(2, testUser, "Transport", "#0000FF");
    }

    @Test
    void testLinearRegression_slopeAndIntercept() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(0, 100);
        regression.addData(1, 120);
        regression.addData(2, 140);
        regression.addData(3, 160);
        regression.addData(4, 180);

        assertEquals(20.0, regression.getSlope(), 0.01);
        assertEquals(100.0, regression.getIntercept(), 0.01);
        assertEquals(1.0, regression.getRSquare(), 0.001);
    }

    @Test
    void testLinearRegression_flatData() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(0, 500);
        regression.addData(1, 500);
        regression.addData(2, 500);
        regression.addData(3, 500);

        assertEquals(0.0, regression.getSlope(), 0.01);
        assertEquals(500.0, regression.getIntercept(), 0.01);
    }

    @Test
    void testLinearRegression_negativeSlope() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(0, 200);
        regression.addData(1, 180);
        regression.addData(2, 160);
        regression.addData(3, 140);

        assertTrue(regression.getSlope() < 0, "Slope should be negative for decreasing data");
        assertEquals(-20.0, regression.getSlope(), 0.01);
    }

    @Test
    void testInsufficientData_returnsFallback() {
        User user = new User(1, "New User", "new@test.com", "pass", null);
        when(transactionRepository.findDistinctYears(1)).thenReturn(List.of(2026));

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        List<Transaction> onlyOneMonth = List.of(
                createTransaction(1, foodCategory, user, 500, LocalDate.of(2026, 7, 5), "expense")
        );
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, start, end))
                .thenReturn(onlyOneMonth);

        BaselineForecastResponse response = forecastService.computeBaselineForecast(1);

        assertFalse(response.isSufficientData());
        assertEquals(1, response.getMonthsOfDataAvailable());
        assertEquals(ForecastService.MINIMUM_MONTHS_FOR_REGRESSION, response.getMinimumRequiredMonths());
        assertTrue(response.getStatusMessage().contains("Insufficient data"));
        assertTrue(response.getCategoryForecasts().isEmpty());
    }

    @Test
    void testInsufficientData_twoMonthsAlsoInsufficient() {
        when(transactionRepository.findDistinctYears(1)).thenReturn(List.of(2026));

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        List<Transaction> twoMonths = List.of(
                createTransaction(1, foodCategory, testUser, 500, LocalDate.of(2026, 6, 5), "expense"),
                createTransaction(2, foodCategory, testUser, 600, LocalDate.of(2026, 7, 10), "expense")
        );
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, start, end))
                .thenReturn(twoMonths);

        BaselineForecastResponse response = forecastService.computeBaselineForecast(1);

        assertFalse(response.isSufficientData());
        assertEquals(2, response.getMonthsOfDataAvailable());
    }

    @Test
    void testSufficientData_generatesForecast() {
        when(transactionRepository.findDistinctYears(1)).thenReturn(List.of(2026));

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        List<Transaction> fourMonths = List.of(
                createTransaction(1, foodCategory, testUser, 100, LocalDate.of(2026, 1, 15), "expense"),
                createTransaction(2, foodCategory, testUser, 120, LocalDate.of(2026, 2, 10), "expense"),
                createTransaction(3, foodCategory, testUser, 140, LocalDate.of(2026, 3, 20), "expense"),
                createTransaction(4, foodCategory, testUser, 160, LocalDate.of(2026, 4, 5), "expense")
        );
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, start, end))
                .thenReturn(fourMonths);

        BaselineForecastResponse response = forecastService.computeBaselineForecast(1);

        assertTrue(response.isSufficientData());
        assertEquals(4, response.getMonthsOfDataAvailable());
        assertFalse(response.getCategoryForecasts().isEmpty());

        CategoryForecast foodForecast = response.getCategoryForecasts().get(0);
        assertEquals("Food", foodForecast.getCategoryName());
        assertEquals(20.0, foodForecast.getSlope(), 1.0);
        assertEquals("INCREASING", foodForecast.getTrend());
        assertEquals(4, foodForecast.getHistoricalMonthlySpend().size());
        assertEquals(ForecastService.PROJECTION_MONTHS, foodForecast.getProjectedMonthlySpend().size());
        assertTrue(foodForecast.getProjectedTotalNext12Months() > 0);
    }

    @Test
    void testMultipleCategories_sortedByProjectedAmount() {
        when(transactionRepository.findDistinctYears(1)).thenReturn(List.of(2026));

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);

        List<Transaction> transactions = List.of(
                createTransaction(1, foodCategory, testUser, 5000, LocalDate.of(2026, 1, 10), "expense"),
                createTransaction(2, foodCategory, testUser, 5000, LocalDate.of(2026, 2, 10), "expense"),
                createTransaction(3, foodCategory, testUser, 5000, LocalDate.of(2026, 3, 10), "expense"),
                createTransaction(4, transportCategory, testUser, 1000, LocalDate.of(2026, 1, 15), "expense"),
                createTransaction(5, transportCategory, testUser, 1000, LocalDate.of(2026, 2, 15), "expense"),
                createTransaction(6, transportCategory, testUser, 1000, LocalDate.of(2026, 3, 15), "expense")
        );
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, start, end))
                .thenReturn(transactions);

        BaselineForecastResponse response = forecastService.computeBaselineForecast(1);

        assertTrue(response.isSufficientData());
        assertEquals(2, response.getCategoryForecasts().size());
        assertEquals("Food", response.getCategoryForecasts().get(0).getCategoryName());
        assertTrue(response.getCategoryForecasts().get(0).getProjectedTotalNext12Months() >
                response.getCategoryForecasts().get(1).getProjectedTotalNext12Months());
    }

    @Test
    void testAggregateMonthlyByCategory() {
        List<Transaction> expenses = List.of(
                createTransaction(1, foodCategory, testUser, 100, LocalDate.of(2026, 1, 5), "expense"),
                createTransaction(2, foodCategory, testUser, 200, LocalDate.of(2026, 1, 20), "expense"),
                createTransaction(3, transportCategory, testUser, 50, LocalDate.of(2026, 1, 10), "expense")
        );

        Map<YearMonth, Map<String, Double>> result = forecastService.aggregateMonthlyByCategory(expenses);

        assertEquals(1, result.size());
        Map<String, Double> jan = result.get(YearMonth.of(2026, 1));
        assertEquals(300.0, jan.get("Food"), 0.01);
        assertEquals(50.0, jan.get("Transport"), 0.01);
    }

    @Test
    void testUncategorizedTransactions_groupedCorrectly() {
        List<Transaction> expenses = List.of(
                createTransaction(1, null, testUser, 150, LocalDate.of(2026, 1, 5), "expense")
        );

        Map<YearMonth, Map<String, Double>> result = forecastService.aggregateMonthlyByCategory(expenses);
        assertEquals(150.0, result.get(YearMonth.of(2026, 1)).get("Uncategorized"), 0.01);
    }

    @Test
    void testMonteCarloSimulation_returnsReasonableRange() {
        Map<Integer, Double> result = forecastService.runMonteCarloSimulation(1000, 200, 12, 1000);

        assertNotNull(result, "Result map should not be null");
        assertEquals(3, result.size(), "Should have 3 percentile entries");
        assertTrue(result.containsKey(Integer.valueOf(0)));
        assertTrue(result.containsKey(Integer.valueOf(10)));
        assertTrue(result.containsKey(Integer.valueOf(90)));

        double p10 = result.get(Integer.valueOf(10));
        double p50 = result.get(Integer.valueOf(0));
        double p90 = result.get(Integer.valueOf(90));

        assertTrue(p10 <= p50, "10th percentile should be <= median");
        assertTrue(p50 <= p90, "Median should be <= 90th percentile");
        assertTrue(p10 > 0, "10th percentile should be positive");
    }

    @Test
    void testNoTransactions_returnsInsufficientData() {
        when(transactionRepository.findDistinctYears(1)).thenReturn(Collections.emptyList());

        BaselineForecastResponse response = forecastService.computeBaselineForecast(1);

        assertFalse(response.isSufficientData());
        assertEquals(0, response.getMonthsOfDataAvailable());
    }

    private Transaction createTransaction(int id, TransactionCategory category, User user,
                                          double amount, LocalDate date, String type) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setTransactionCategory(category);
        t.setUser(user);
        t.setTransactionAmount(amount);
        t.setTransactionDate(date);
        t.setTransactionType(type);
        t.setTransactionName("Test Transaction");
        return t;
    }
}
