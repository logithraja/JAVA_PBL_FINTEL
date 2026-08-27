package com.example.expense_tracker.repositories;

import com.example.expense_tracker.entities.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    List<Budget> findByUserId(Integer userId);
}
