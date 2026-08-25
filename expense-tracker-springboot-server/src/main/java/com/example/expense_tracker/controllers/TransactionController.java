package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
@Tag(name = "Transactions", description = "Endpoints for managing user financial transactions")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Operation(summary = "Get all transactions for a user filtered by year and optional month")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getAllTransactionsByUserIdAndYearOrMonth(
            @PathVariable int userId,
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Getting transactions for user: {}, year: {}, month: {}", userId, year, month);

        List<Transaction> transactionsList;
        if (month == null) {
            transactionsList = transactionService.getAllTransactionsByUserIdAndYear(userId, year);
        } else {
            transactionsList = transactionService.getAllTransactionsByUserIdAndYearAndMonth(userId, year, month);
        }
        return ResponseEntity.ok(transactionsList);
    }

    @Operation(summary = "Get recent transactions for a user with pagination")
    @GetMapping("/recent/user/{userId}")
    public ResponseEntity<List<Transaction>> getRecentTransactionsByUserId(
            @PathVariable int userId,
            @RequestParam(defaultValue = "0") int startPage,
            @RequestParam(defaultValue = "0") int endPage,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Getting recent transactions for user: {}, pages: [{}, {}]", userId, startPage, endPage);

        List<Transaction> recentTransactionList = transactionService.getRecentTransactionsByUserId(
                userId,
                startPage,
                endPage,
                size
        );
        return ResponseEntity.ok(recentTransactionList);
    }

    @Operation(summary = "Get all distinct transaction years for a user")
    @GetMapping("/years/{userId}")
    public ResponseEntity<List<Integer>> getDistinctTransactionYears(
            @PathVariable int userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Getting distinct years for user: {}", userId);
        return ResponseEntity.ok(transactionService.getDistinctTransactionYears(userId));
    }

    @Operation(summary = "Check for possible duplicate transactions by merchant, amount and date")
    @GetMapping("/duplicate-check")
    public ResponseEntity<List<Transaction>> checkDuplicates(
            @RequestParam int userId,
            @RequestParam String merchant,
            @RequestParam double amount,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Checking duplicates for user: {}, merchant: '{}', amount: {}, date: {}", userId, merchant, amount, date);
        List<Transaction> duplicates = transactionService.findPossibleDuplicates(userId, merchant, amount, date);
        return ResponseEntity.ok(duplicates);
    }

    @Operation(summary = "Get month-end spending forecast for a user")
    @GetMapping("/forecast/{userId}")
    public ResponseEntity<com.example.expense_tracker.dtos.SpendingForecastResponse> getMonthlySpendingForecast(
            @PathVariable int userId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Double budget,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Fetching spending forecast for user: {}, year: {}, month: {}, budget: {}", userId, year, month, budget);
        com.example.expense_tracker.dtos.SpendingForecastResponse forecast = transactionService.calculateMonthlySpendingForecast(userId, year, month, budget);
        return ResponseEntity.ok(forecast);
    }

    @Operation(summary = "Create a new transaction")
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody Transaction transaction,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (transaction.getUser() != null && transaction.getUser().getId() != null) {
            verifyUserOwnership(transaction.getUser().getId(), principal);
        } else if (principal != null) {
            if (transaction.getUser() == null) {
                transaction.setUser(new com.example.expense_tracker.entities.User());
            }
            transaction.getUser().setId(principal.getId());
        }

        log.info("Creating transaction: {}", transaction.getTransactionName());
        Transaction created = transactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing transaction")
    @PutMapping
    public ResponseEntity<Transaction> updateTransaction(
            @Valid @RequestBody Transaction transaction,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Updating transaction with id: {}", transaction.getId());

        Transaction existing = transactionService.getTransactionById(transaction.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transaction.getId()));

        if (existing.getUser() != null) {
            verifyUserOwnership(existing.getUser().getId(), principal);
        }

        Transaction updated = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a transaction by ID")
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransactionById(
            @PathVariable int transactionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Deleting transaction with id: {}", transactionId);

        Transaction existing = transactionService.getTransactionById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        if (existing.getUser() != null) {
            verifyUserOwnership(existing.getUser().getId(), principal);
        }

        transactionService.deleteTransactionById(transactionId);
        return ResponseEntity.ok().build();
    }

    private void verifyUserOwnership(int userId, UserPrincipal principal) {
        if (principal != null && !principal.getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to access transactions of user ID: " + userId);
        }
    }
}
