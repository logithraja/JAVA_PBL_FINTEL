package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.Budget;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.BudgetRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BudgetService {
    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgets;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgets, UserRepository userRepository) {
        this.budgets = budgets;
        this.userRepository = userRepository;
    }

    public Optional<Budget> getById(Integer id) {
        log.info("Getting budget with id: {}", id);
        return budgets.findById(id);
    }

    public List<Budget> listByUser(Integer userId) {
        log.info("Listing budgets for user: {}", userId);
        return budgets.findByUserId(userId);
    }

    public Budget create(Budget b, Integer userId) {
        log.info("Creating budget '{}' for user: {}", b.getCategory(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        b.setUser(user);
        return budgets.save(b);
    }

    public Budget upsert(Budget b, Integer userId) {
        log.info("Upserting budget '{}' for user: {}", b.getCategory(), userId);
        if (b.getId() != null) {
            Budget existing = budgets.findById(b.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + b.getId()));
            existing.setCategory(b.getCategory());
            existing.setLimitAmount(b.getLimitAmount());
            existing.setSpentAmount(b.getSpentAmount());
            existing.setYear(b.getYear());
            existing.setPeriodType(b.getPeriodType());
            existing.setMonth(b.getMonth());
            existing.setQuarter(b.getQuarter());
            if (b.getUser() != null) {
                existing.setUser(b.getUser());
            }
            return budgets.save(existing);
        }

        if (b.getUser() == null && userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            b.setUser(user);
        }
        return budgets.save(b);
    }

    public void delete(Integer id) {
        log.info("Deleting budget with id: {}", id);
        if (!budgets.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found with id: " + id);
        }
        budgets.deleteById(id);
    }
}
