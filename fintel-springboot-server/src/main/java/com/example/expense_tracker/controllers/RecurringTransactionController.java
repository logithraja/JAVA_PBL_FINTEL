package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.RecurringTransaction;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.RecurringTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recurring")
@Tag(name = "Recurring Transactions", description = "Endpoints for managing recurring scheduled income/expense rules")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringService;

    private void verifyUserOwnership(int userId, UserPrincipal principal) {
        if (principal == null || principal.getId() != userId) {
            throw new UnauthorizedException("You are not authorized to access recurring transactions for user ID: " + userId);
        }
    }

    @Operation(summary = "Get all recurring transactions for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringTransaction>> getRecurringByUserId(
            @PathVariable int userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        return ResponseEntity.ok(recurringService.getRecurringByUserId(userId));
    }

    @Operation(summary = "Create a new recurring transaction schedule")
    @PostMapping("/user/{userId}")
    public ResponseEntity<RecurringTransaction> createRecurring(
            @PathVariable int userId,
            @Valid @RequestBody RecurringTransaction recurring,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        RecurringTransaction created = recurringService.createRecurring(userId, recurring);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update a recurring transaction")
    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransaction> updateRecurring(
            @PathVariable int id,
            @RequestBody RecurringTransaction recurring,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RecurringTransaction existing = recurringService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id));
        verifyUserOwnership(existing.getUser().getId(), principal);
        return ResponseEntity.ok(recurringService.updateRecurring(id, recurring));
    }

    @Operation(summary = "Delete a recurring transaction")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurring(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RecurringTransaction existing = recurringService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found: " + id));
        verifyUserOwnership(existing.getUser().getId(), principal);
        recurringService.deleteRecurring(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Trigger catch-up execution for overdue recurring transactions")
    @PostMapping("/user/{userId}/catch-up")
    public ResponseEntity<Map<String, Object>> catchUp(
            @PathVariable int userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        int generated = recurringService.catchUpForUser(userId);
        return ResponseEntity.ok(Map.of("message", "Catch-up complete", "generatedTransactions", generated));
    }
}
