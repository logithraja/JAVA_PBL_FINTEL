package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.BadRequestException;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.TransactionCategoryRepository;
import com.example.expense_tracker.repositories.TransactionRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<Transaction> getTransactionById(int id) {
        return transactionRepository.findById(id);
    }

    public List<Transaction> getRecentTransactionsByUserId(int userId, int startPage, int endPage, int size) {
        log.info("Getting recent transactions for user: {}, pages: [{}, {}], size: {}", userId, startPage, endPage, size);
        List<Transaction> combinedResults = new ArrayList<>();

        int safeSize = Math.max(1, Math.min(size, 1000));
        int safeStart = Math.max(0, startPage);
        int safeEnd = Math.max(safeStart, endPage);

        for (int page = safeStart; page <= safeEnd; page++) {
            Pageable pageable = PageRequest.of(page, safeSize);
            List<Transaction> pageResults = transactionRepository.findAllByUserIdOrderByTransactionDateDesc(
                    userId,
                    pageable
            );
            combinedResults.addAll(pageResults);
        }
        return combinedResults;
    }

    public List<Transaction> getAllTransactionsByUserIdAndYear(int userId, int year) {
        log.info("Getting all transactions in year: {} for user: {}", year, userId);
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                userId,
                startDate,
                endDate
        );
    }

    public List<Transaction> getAllTransactionsByUserIdAndYearAndMonth(int userId, int year, int month) {
        log.info("Getting all transactions in month: {} and year: {} for user: {}", month, year, userId);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());

        return transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                userId,
                startDate,
                endDate
        );
    }

    public List<Integer> getDistinctTransactionYears(int userId) {
        log.info("Getting distinct transaction years for user: {}", userId);
        return transactionRepository.findDistinctYears(userId);
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        log.info("Creating transaction: {}", transaction.getTransactionName());

        if (transaction.getUser() == null || transaction.getUser().getId() == null) {
            throw new BadRequestException("User ID is required to create a transaction");
        }

        User user = userRepository.findById(transaction.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + transaction.getUser().getId()));

        TransactionCategory category = null;
        if (transaction.getTransactionCategory() != null && transaction.getTransactionCategory().getId() != null) {
            category = transactionCategoryRepository.findById(transaction.getTransactionCategory().getId())
                    .orElse(null);
        }

        Transaction newTransaction = new Transaction();
        newTransaction.setUser(user);
        newTransaction.setTransactionCategory(category);
        newTransaction.setTransactionName(transaction.getTransactionName());
        newTransaction.setTransactionAmount(transaction.getTransactionAmount());
        newTransaction.setTransactionDate(transaction.getTransactionDate());
        newTransaction.setTransactionTime(transaction.getTransactionTime());
        newTransaction.setTransactionType(transaction.getTransactionType());

        return transactionRepository.save(newTransaction);
    }

    @Transactional
    public Transaction updateTransaction(Transaction transaction) {
        log.info("Updating transaction with id: {}", transaction.getId());

        if (transaction.getId() == null) {
            throw new BadRequestException("Transaction ID must not be null for update");
        }

        Transaction existing = transactionRepository.findById(transaction.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transaction.getId()));

        if (transaction.getTransactionName() != null) {
            existing.setTransactionName(transaction.getTransactionName());
        }
        if (transaction.getTransactionAmount() > 0) {
            existing.setTransactionAmount(transaction.getTransactionAmount());
        }
        if (transaction.getTransactionDate() != null) {
            existing.setTransactionDate(transaction.getTransactionDate());
        }
        if (transaction.getTransactionTime() != null) {
            existing.setTransactionTime(transaction.getTransactionTime());
        }
        if (transaction.getTransactionType() != null) {
            existing.setTransactionType(transaction.getTransactionType());
        }
        if (transaction.getTransactionCategory() != null) {
            if (transaction.getTransactionCategory().getId() != null) {
                TransactionCategory cat = transactionCategoryRepository.findById(transaction.getTransactionCategory().getId()).orElse(null);
                existing.setTransactionCategory(cat);
            } else {
                existing.setTransactionCategory(null);
            }
        }

        return transactionRepository.save(existing);
    }

    public List<Transaction> findPossibleDuplicates(int userId, String merchant, double amount, LocalDate date) {
        log.info("Checking possible duplicates for user: {}, merchant: '{}', amount: {}, date: {}", userId, merchant, amount, date);
        if (date == null) {
            date = LocalDate.now();
        }

        List<Transaction> sameDayTransactions = transactionRepository
                .findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, date, date);

        List<Transaction> duplicates = new ArrayList<>();
        String normalizedMerchant = normalizeString(merchant);

        for (Transaction tx : sameDayTransactions) {
            double txAmount = tx.getTransactionAmount();
            double diff = Math.abs(txAmount - amount);
            double maxAllowedDiff = Math.max(0.01, 0.02 * amount);

            if (diff <= maxAllowedDiff) {
                String txMerchant = normalizeString(tx.getTransactionName());
                if (isFuzzyMatch(normalizedMerchant, txMerchant)) {
                    duplicates.add(tx);
                }
            }
        }
        return duplicates;
    }

    public com.example.expense_tracker.dtos.SpendingForecastResponse calculateMonthlySpendingForecast(int userId, int year, int month, Double budget) {
        log.info("Calculating spending forecast for user: {}, year: {}, month: {}, budget: {}", userId, year, month, budget);

        List<Transaction> monthTransactions = getAllTransactionsByUserIdAndYearAndMonth(userId, year, month);

        double spentSoFar = 0.0;
        for (Transaction t : monthTransactions) {
            if ("expense".equalsIgnoreCase(t.getTransactionType())) {
                spentSoFar += t.getTransactionAmount();
            }
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        int totalDaysInMonth = yearMonth.lengthOfMonth();

        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);

        int daysElapsed;
        int daysRemaining;

        if (yearMonth.isBefore(currentYearMonth)) {
            daysElapsed = totalDaysInMonth;
            daysRemaining = 0;
        } else if (yearMonth.isAfter(currentYearMonth)) {
            daysElapsed = 1;
            daysRemaining = totalDaysInMonth;
        } else {
            daysElapsed = Math.max(1, today.getDayOfMonth());
            daysRemaining = Math.max(0, totalDaysInMonth - daysElapsed);
        }

        double dailyAvgRate = spentSoFar / daysElapsed;
        double projectedTotal = spentSoFar + (dailyAvgRate * daysRemaining);

        Double projectedOverUnder = null;
        String status = "NO_BUDGET";
        String message;

        if (budget != null && budget > 0) {
            projectedOverUnder = projectedTotal - budget;
            if (projectedOverUnder > 0) {
                status = "OVER_BUDGET";
                message = String.format("At this pace: ₹%.2f by month-end (over budget by ₹%.2f)", projectedTotal, projectedOverUnder);
            } else {
                status = "UNDER_BUDGET";
                message = String.format("At this pace: ₹%.2f by month-end (under budget by ₹%.2f)", projectedTotal, Math.abs(projectedOverUnder));
            }
        } else {
            message = String.format("At this pace: ₹%.2f projected by month-end", projectedTotal);
        }

        return new com.example.expense_tracker.dtos.SpendingForecastResponse(
                spentSoFar,
                dailyAvgRate,
                projectedTotal,
                daysElapsed,
                daysRemaining,
                totalDaysInMonth,
                budget,
                projectedOverUnder,
                status,
                message
        );
    }

    private String normalizeString(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean isFuzzyMatch(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) return true;
        if (s1.isEmpty() || s2.isEmpty()) return false;
        if (s1.equals(s2) || s1.contains(s2) || s2.contains(s1)) return true;

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen <= 2) return s1.equals(s2);

        int dist = computeLevenshteinDistance(s1, s2);
        return dist <= 2 || (1.0 - (double) dist / maxLen) >= 0.70;
    }

    private int computeLevenshteinDistance(String a, String b) {
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++) costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    @Transactional
    public void deleteTransactionById(int transactionId) {
        log.info("Deleting transaction with id: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        transactionRepository.delete(transaction);
    }
}
