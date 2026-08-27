package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.BadRequestException;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.TransactionCategoryRepository;
import com.example.expense_tracker.repositories.TransactionRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User sampleUser;
    private TransactionCategory sampleCategory;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Test User", "test@example.com", "password", null);
        sampleCategory = new TransactionCategory(1, sampleUser, "Food", "#FF5733");
        sampleTransaction = new Transaction(1, sampleCategory, sampleUser, "Groceries", 50.0,
                LocalDate.of(2026, 8, 25), "12:00 PM", "expense");
    }

    @Test
    void testCreateTransaction_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(transactionCategoryRepository.findById(1)).thenReturn(Optional.of(sampleCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        Transaction created = transactionService.createTransaction(sampleTransaction);

        assertNotNull(created);
        assertEquals("Groceries", created.getTransactionName());
        assertEquals(50.0, created.getTransactionAmount());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_MissingUser_ThrowsBadRequest() {
        sampleTransaction.setUser(null);
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(sampleTransaction));
    }

    @Test
    void testGetRecentTransactionsByUserId() {
        when(transactionRepository.findAllByUserIdOrderByTransactionDateDesc(eq(1), any(Pageable.class)))
                .thenReturn(List.of(sampleTransaction));

        List<Transaction> result = transactionService.getRecentTransactionsByUserId(1, 0, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getTransactionName());
    }

    @Test
    void testGetAllTransactionsByUserIdAndYear() {
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                eq(1), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 12, 31))))
                .thenReturn(List.of(sampleTransaction));

        List<Transaction> result = transactionService.getAllTransactionsByUserIdAndYear(1, 2026);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdateTransaction_Success() {
        when(transactionRepository.findById(1)).thenReturn(Optional.of(sampleTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);

        sampleTransaction.setTransactionName("Updated Groceries");
        Transaction updated = transactionService.updateTransaction(sampleTransaction);

        assertNotNull(updated);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testUpdateTransaction_NotFound_ThrowsException() {
        when(transactionRepository.findById(999)).thenReturn(Optional.empty());
        sampleTransaction.setId(999);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransaction(sampleTransaction));
    }

    @Test
    void testDeleteTransactionById_Success() {
        when(transactionRepository.findById(1)).thenReturn(Optional.of(sampleTransaction));

        transactionService.deleteTransactionById(1);

        verify(transactionRepository).delete(sampleTransaction);
    }

    @Test
    void testDeleteTransactionById_NotFound_ThrowsException() {
        when(transactionRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransactionById(999));
    }

    @Test
    void testFindPossibleDuplicates_ExactMatch() {
        LocalDate date = LocalDate.of(2026, 8, 25);
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, date, date))
                .thenReturn(List.of(sampleTransaction));

        List<Transaction> dups = transactionService.findPossibleDuplicates(1, "Groceries", 50.0, date);

        assertEquals(1, dups.size());
        assertEquals("Groceries", dups.get(0).getTransactionName());
    }

    @Test
    void testFindPossibleDuplicates_FuzzyNameAndToleranceAmount() {
        LocalDate date = LocalDate.of(2026, 8, 25);
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, date, date))
                .thenReturn(List.of(sampleTransaction));

        // "Groceries Store" contains "Groceries", and amount is 50.50 which is within 2% of 50.0 (diff 0.50 <= 1.01)
        List<Transaction> dups = transactionService.findPossibleDuplicates(1, "Groceries Store", 50.50, date);

        assertEquals(1, dups.size());
    }

    @Test
    void testFindPossibleDuplicates_NoMatch_DifferentAmount() {
        LocalDate date = LocalDate.of(2026, 8, 25);
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, date, date))
                .thenReturn(List.of(sampleTransaction));

        // Amount 80.0 is well beyond 2%
        List<Transaction> dups = transactionService.findPossibleDuplicates(1, "Groceries", 80.0, date);

        assertEquals(0, dups.size());
    }

    @Test
    void testCalculateMonthlySpendingForecast_UnderBudget() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, startDate, endDate))
                .thenReturn(List.of(sampleTransaction)); // 50.0 spent

        com.example.expense_tracker.dtos.SpendingForecastResponse forecast =
                transactionService.calculateMonthlySpendingForecast(1, 2026, 8, 500.0);

        assertNotNull(forecast);
        assertEquals(50.0, forecast.getSpentSoFar());
        assertTrue(forecast.getProjectedTotal() > 0);
        assertEquals(500.0, forecast.getBudget());
        assertEquals("UNDER_BUDGET", forecast.getStatus());
        assertTrue(forecast.getMessage().contains("under budget"));
    }

    @Test
    void testCalculateMonthlySpendingForecast_OverBudget() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        Transaction bigExpense = new Transaction(2, sampleCategory, sampleUser, "Rent", 2500.0,
                LocalDate.of(2026, 8, 5), "10:00 AM", "expense");
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, startDate, endDate))
                .thenReturn(List.of(bigExpense));

        com.example.expense_tracker.dtos.SpendingForecastResponse forecast =
                transactionService.calculateMonthlySpendingForecast(1, 2026, 8, 1000.0);

        assertNotNull(forecast);
        assertEquals(2500.0, forecast.getSpentSoFar());
        assertEquals("OVER_BUDGET", forecast.getStatus());
        assertTrue(forecast.getMessage().contains("over budget"));
    }

    @Test
    void testCalculateMonthlySpendingForecast_NoBudget() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        when(transactionRepository.findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(1, startDate, endDate))
                .thenReturn(List.of(sampleTransaction));

        com.example.expense_tracker.dtos.SpendingForecastResponse forecast =
                transactionService.calculateMonthlySpendingForecast(1, 2026, 8, null);

        assertNotNull(forecast);
        assertEquals(50.0, forecast.getSpentSoFar());
        assertNull(forecast.getBudget());
        assertEquals("NO_BUDGET", forecast.getStatus());
    }
}
