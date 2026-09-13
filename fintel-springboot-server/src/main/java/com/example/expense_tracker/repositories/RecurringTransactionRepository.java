package com.example.expense_tracker.repositories;

import com.example.expense_tracker.entities.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Integer> {
    List<RecurringTransaction> findAllByUserId(int userId);
    List<RecurringTransaction> findAllByUserIdAndActiveTrue(int userId);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.active = true AND r.nextExecutionDate <= :date")
    List<RecurringTransaction> findDueRecurringTransactions(@Param("date") LocalDate date);
}
