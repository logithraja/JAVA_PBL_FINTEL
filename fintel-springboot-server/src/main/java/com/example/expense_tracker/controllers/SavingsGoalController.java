package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.SavingsGoal;
import com.example.expense_tracker.exceptions.BadRequestException;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.SavingsGoalService;
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
@RequestMapping("/api/v1/savings-goals")
@Tag(name = "Savings Goals", description = "Endpoints for managing user savings goals")
public class SavingsGoalController {
    private static final Logger log = LoggerFactory.getLogger(SavingsGoalController.class);

    private final SavingsGoalService service;

    public SavingsGoalController(SavingsGoalService service) {
        this.service = service;
    }

    @Operation(summary = "Get all savings goals for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SavingsGoal>> byUser(
            @PathVariable Integer userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        log.info("Fetching savings goals for user: {}", userId);
        return ResponseEntity.ok(service.listByUser(userId));
    }

    @Operation(summary = "Create a new savings goal")
    @PostMapping
    public ResponseEntity<SavingsGoal> create(
            @Valid @RequestBody SavingsGoal g,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Integer userId = (g.getUser() != null ? g.getUser().getId() : null);
        if (userId == null && principal != null) {
            userId = principal.getId();
        }
        if (userId == null) {
            throw new BadRequestException("user.id is required in payload");
        }

        verifyUserOwnership(userId, principal);
        log.info("Creating savings goal '{}' for user: {}", g.getName(), userId);
        SavingsGoal created = service.create(g, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing savings goal")
    @PutMapping
    public ResponseEntity<SavingsGoal> update(
            @Valid @RequestBody SavingsGoal g,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (g.getId() != null) {
            SavingsGoal existing = service.getById(g.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + g.getId()));
            if (existing.getUser() != null) {
                verifyUserOwnership(existing.getUser().getId(), principal);
            }
        }

        Integer userId = (g.getUser() != null ? g.getUser().getId() : null);
        if (userId == null && principal != null) {
            userId = principal.getId();
        }

        log.info("Updating savings goal '{}' for user: {}", g.getName(), userId);
        SavingsGoal updated = service.upsert(g, userId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a savings goal by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Deleting savings goal with id: {}", id);

        SavingsGoal existing = service.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal not found with id: " + id));

        if (existing.getUser() != null) {
            verifyUserOwnership(existing.getUser().getId(), principal);
        }

        service.delete(id);
        return ResponseEntity.ok().build();
    }

    private void verifyUserOwnership(Integer userId, UserPrincipal principal) {
        if (principal != null && !principal.getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to access savings goals of user ID: " + userId);
        }
    }
}
