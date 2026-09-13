package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.Account;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Endpoints for managing user bank accounts and wallets")
public class AccountController {

    @Autowired
    private AccountService accountService;

    private void verifyUserOwnership(int userId, UserPrincipal principal) {
        if (principal == null || principal.getId() != userId) {
            throw new UnauthorizedException("You are not authorized to access accounts for user ID: " + userId);
        }
    }

    @Operation(summary = "Get all accounts for a user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(
            @PathVariable int userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @Operation(summary = "Create an account for a user")
    @PostMapping("/user/{userId}")
    public ResponseEntity<Account> createAccount(
            @PathVariable int userId,
            @Valid @RequestBody Account account,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        verifyUserOwnership(userId, principal);
        Account created = accountService.createAccount(userId, account);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an account")
    @PutMapping("/{accountId}")
    public ResponseEntity<Account> updateAccount(
            @PathVariable int accountId,
            @RequestBody Account account,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Account existing = accountService.getAccountById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        verifyUserOwnership(existing.getUser().getId(), principal);
        return ResponseEntity.ok(accountService.updateAccount(accountId, account));
    }

    @Operation(summary = "Delete an account")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable int accountId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Account existing = accountService.getAccountById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        verifyUserOwnership(existing.getUser().getId(), principal);
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
