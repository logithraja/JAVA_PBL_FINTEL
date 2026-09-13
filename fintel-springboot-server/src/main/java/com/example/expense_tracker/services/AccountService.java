package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.Account;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.BadRequestException;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.AccountRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Account> getAccountsByUserId(int userId) {
        log.info("Fetching accounts for user: {}", userId);
        return accountRepository.findAllByUserId(userId);
    }

    public Optional<Account> getAccountById(int accountId) {
        return accountRepository.findById(accountId);
    }

    @Transactional
    public Account createAccount(int userId, Account account) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        account.setUser(user);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        // If marked default or first account, ensure others aren't default
        List<Account> existing = accountRepository.findAllByUserId(userId);
        if (existing.isEmpty() || account.isDefault()) {
            account.setDefault(true);
            for (Account a : existing) {
                if (a.isDefault()) {
                    a.setDefault(false);
                    accountRepository.save(a);
                }
            }
        }

        log.info("Creating account: {} for user: {}", account.getName(), userId);
        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(int accountId, Account updated) {
        Account existing = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        if (updated.getName() != null && !updated.getName().isBlank()) {
            existing.setName(updated.getName());
        }
        if (updated.getAccountType() != null && !updated.getAccountType().isBlank()) {
            existing.setAccountType(updated.getAccountType());
        }
        if (updated.getBalance() != null) {
            existing.setBalance(updated.getBalance());
        }
        if (updated.getCurrency() != null && !updated.getCurrency().isBlank()) {
            existing.setCurrency(updated.getCurrency());
        }
        if (updated.isDefault() && !existing.isDefault()) {
            existing.setDefault(true);
            List<Account> userAccounts = accountRepository.findAllByUserId(existing.getUser().getId());
            for (Account a : userAccounts) {
                if (a.getId() != accountId && a.isDefault()) {
                    a.setDefault(false);
                    accountRepository.save(a);
                }
            }
        }

        log.info("Updated account: {}", accountId);
        return accountRepository.save(existing);
    }

    @Transactional
    public void deleteAccount(int accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found: " + accountId);
        }
        log.info("Deleting account: {}", accountId);
        accountRepository.deleteById(accountId);
    }

    @Transactional
    public void adjustAccountBalance(int accountId, BigDecimal delta) {
        accountRepository.findById(accountId).ifPresent(account -> {
            account.setBalance(account.getBalance().add(delta));
            accountRepository.save(account);
            log.info("Adjusted balance for account: {} by: {}", accountId, delta);
        });
    }
}
