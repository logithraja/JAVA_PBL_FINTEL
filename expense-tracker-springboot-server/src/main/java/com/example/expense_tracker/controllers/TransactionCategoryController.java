package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.TransactionCategoryService;
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
@RequestMapping("/api/v1/transaction-category")
@Tag(name = "Transaction Categories", description = "Endpoints for user transaction categories")
public class TransactionCategoryController {
    private static final Logger log = LoggerFactory.getLogger(TransactionCategoryController.class);

    @Autowired
    private TransactionCategoryService transactionCategoryService;

    @Operation(summary = "Get all transaction categories for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionCategory>> getAllTransactionCategoriesByUserId(
            @PathVariable int userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Getting all transaction categories for user: {}", userId);
        List<TransactionCategory> transactionCategories = transactionCategoryService.getAllTransactionCategoriesByUserId(userId);
        return ResponseEntity.ok(transactionCategories);
    }

    @Operation(summary = "Get transaction category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionCategory> getTransactionCategoryById(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Getting transaction category with id: {}", id);

        TransactionCategory category = transactionCategoryService.getTransactionCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (category.getUser() != null) {
            verifyUserOwnership(category.getUser().getId(), principal);
        }

        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Create a new transaction category")
    @PostMapping
    public ResponseEntity<TransactionCategory> createTransactionCategory(
            @Valid @RequestBody TransactionCategory transactionCategory,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        int userId;
        if (transactionCategory.getUser() != null && transactionCategory.getUser().getId() != null) {
            userId = transactionCategory.getUser().getId();
            verifyUserOwnership(userId, principal);
        } else if (principal != null) {
            userId = principal.getId();
        } else {
            throw new UnauthorizedException("User must be authenticated to create a category");
        }

        log.info("Creating category '{}' for user: {}", transactionCategory.getCategoryName(), userId);
        TransactionCategory created = transactionCategoryService.createTransactionCategory(
                userId,
                transactionCategory.getCategoryName(),
                transactionCategory.getCategoryColor()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing transaction category")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionCategory> updateTransactionCategoryById(
            @PathVariable int id,
            @RequestParam String newCategoryName,
            @RequestParam String newCategoryColor,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Updating transaction category with id: {}", id);

        TransactionCategory existing = transactionCategoryService.getTransactionCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (existing.getUser() != null) {
            verifyUserOwnership(existing.getUser().getId(), principal);
        }

        TransactionCategory updated = transactionCategoryService.updateTransactionCategoryById(
                id, newCategoryName, newCategoryColor
        );
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a transaction category by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransactionCategoryById(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Deleting transaction category with id: {}", id);

        TransactionCategory existing = transactionCategoryService.getTransactionCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (existing.getUser() != null) {
            verifyUserOwnership(existing.getUser().getId(), principal);
        }

        boolean deleted = transactionCategoryService.deleteTransactionCategoryById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        return ResponseEntity.ok().build();
    }

    private void verifyUserOwnership(int userId, UserPrincipal principal) {
        if (principal != null && !principal.getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to access categories of user ID: " + userId);
        }
    }
}