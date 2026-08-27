package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.Budget;
import com.example.expense_tracker.exceptions.BadRequestException;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budgets", description = "Endpoints for managing user budgets")
public class BudgetController {
    private static final Logger log = LoggerFactory.getLogger(BudgetController.class);

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @Operation(summary = "Get all budgets for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> byUser(
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Fetching budgets for user: {}", userId);
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @Operation(summary = "Create a new budget")
    @PostMapping
    public ResponseEntity<Budget> create(
            @Valid @RequestBody Budget b,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Integer userId = (b.getUser() != null ? b.getUser().getId() : null);
        if (userId == null && principal != null) {
            userId = principal.getId();
        }
        if (userId == null) {
            throw new BadRequestException("user.id is required in payload");
        }

        verifyUserOwnership(userId, principal);
        log.info("Creating budget '{}' for user: {}", b.getCategory(), userId);
        Budget created = service.create(b, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing budget")
    @PutMapping
    public ResponseEntity<Budget> update(
            @Valid @RequestBody Budget b,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (b.getId() != null) {
            Budget existing = service.getById(b.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + b.getId()));
            if (existing.getUser() != null) {
                verifyUserOwnership(existing.getUser().getId(), principal);
            }
        }

        Integer userId = (b.getUser() != null ? b.getUser().getId() : null);
        if (userId == null && principal != null) {
            userId = principal.getId();
        }

        log.info("Updating budget '{}' for user: {}", b.getCategory(), userId);
        Budget updated = service.upsert(b, userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a budget by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Deleting budget with id: {}", id);

        Budget existing = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id: " + id));

        if (existing.getUser() != null) {
            verifyUserOwnership(existing.getUser().getId(), principal);
        }

        service.delete(id);
        return ResponseEntity.ok().build();
    }

    private void verifyUserOwnership(Integer userId, UserPrincipal principal) {
        if (principal != null && !principal.getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to access budgets of user ID: " + userId);
        }
    }
}
