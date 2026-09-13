package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.*;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.RecurringTransactionRepository;
import com.example.expense_tracker.repositories.TransactionCategoryRepository;
import com.example.expense_tracker.repositories.TransactionRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringTransactionService {
    private static final Logger log = LoggerFactory.getLogger(RecurringTransactionService.class);

    @Autowired
    private RecurringTransactionRepository recurringRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    public List<RecurringTransaction> getRecurringByUserId(int userId) {
        log.info("Fetching recurring transactions for user: {}", userId);
        return recurringRepository.findAllByUserId(userId);
    }

    public Optional<RecurringTransaction> getById(int id) {
        return recurringRepository.findById(id);
    }

    @Transactional
    public RecurringTransaction createRecurring(int userId, RecurringTransaction recurring) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        recurring.setUser(user);

        if (recurring.getNextExecutionDate() == null) {
            recurring.setNextExecutionDate(recurring.getStartDate());
        }

        log.info("Creating recurring transaction: '{}' for user: {}", recurring.getName(), userId);
        return recurringRepository.save(recurring);
    }

    @Transactional
    public RecurringTransaction updateRecurring(int id, RecurringTransaction updated) {
        RecurringTransaction existing = recurringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id));

        if (updated.getName() != null && !updated.getName().isBlank()) existing.setName(updated.getName());
        if (updated.getAmount() > 0) existing.setAmount(updated.getAmount());
        if (updated.getTransactionType() != null) existing.setTransactionType(updated.getTransactionType());
        if (updated.getFrequency() != null) existing.setFrequency(updated.getFrequency());
        if (updated.getEndDate() != null) existing.setEndDate(updated.getEndDate());
        if (updated.getNextExecutionDate() != null) existing.setNextExecutionDate(updated.getNextExecutionDate());
        existing.setActive(updated.isActive());

        return recurringRepository.save(existing);
    }

    @Transactional
    public void deleteRecurring(int id) {
        if (!recurringRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recurring transaction not found: " + id);
        }
        recurringRepository.deleteById(id);
    }

    /**
     * Nightly job at 01:00 AM to materialize recurring transactions
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public int materializeDueTransactions() {
        return processDueTransactionsUpTo(LocalDate.now());
    }

    /**
     * Catch-up execution for a specific user (called on user login or refresh)
     */
    @Transactional
    public int catchUpForUser(int userId) {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> userDue = recurringRepository.findAllByUserIdAndActiveTrue(userId);
        int generatedCount = 0;

        for (RecurringTransaction rt : userDue) {
            generatedCount += materializeSingleRecurring(rt, today);
        }
        return generatedCount;
    }

    @Transactional
    public int processDueTransactionsUpTo(LocalDate asOfDate) {
        List<RecurringTransaction> due = recurringRepository.findDueRecurringTransactions(asOfDate);
        log.info("Processing {} due recurring transactions as of {}", due.size(), asOfDate);
        int generated = 0;

        for (RecurringTransaction rt : due) {
            generated += materializeSingleRecurring(rt, asOfDate);
        }
        return generated;
    }

    private int materializeSingleRecurring(RecurringTransaction rt, LocalDate asOfDate) {
        int generated = 0;
        while (rt.isActive() && !rt.getNextExecutionDate().isAfter(asOfDate)) {
            LocalDate execDate = rt.getNextExecutionDate();

            if (rt.getEndDate() != null && execDate.isAfter(rt.getEndDate())) {
                rt.setActive(false);
                break;
            }

            Transaction t = new Transaction();
            t.setUser(rt.getUser());
            t.setTransactionCategory(rt.getTransactionCategory());
            t.setAccount(rt.getAccount());
            t.setTransactionName(rt.getName() + " (Auto)");
            t.setTransactionAmount(rt.getAmount());
            t.setTransactionDate(execDate);
            t.setTransactionType(rt.getTransactionType());
            transactionRepository.save(t);
            generated++;

            rt.setLastExecutionDate(execDate);
            LocalDate nextDate = computeNextDate(execDate, rt.getFrequency());
            rt.setNextExecutionDate(nextDate);

            if (rt.getEndDate() != null && nextDate.isAfter(rt.getEndDate())) {
                rt.setActive(false);
            }
        }
        recurringRepository.save(rt);
        return generated;
    }

    public static LocalDate computeNextDate(LocalDate current, RecurringTransaction.Frequency frequency) {
        if (frequency == null) return current.plusMonths(1);
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
