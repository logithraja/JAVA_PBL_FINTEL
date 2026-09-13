package com.example.expense_tracker.repositories;

import com.example.expense_tracker.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findAllByUserId(int userId);
    Optional<Account> findByUserIdAndIsDefaultTrue(int userId);
}
