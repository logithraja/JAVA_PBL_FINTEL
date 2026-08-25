package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionCategoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionCategoryService transactionCategoryService;

    private User sampleUser;
    private TransactionCategory sampleCategory;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Test User", "test@example.com", "password", null);
        sampleCategory = new TransactionCategory(1, sampleUser, "Dining", "#33FF57");
    }

    @Test
    void testCreateTransactionCategory_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenReturn(sampleCategory);

        TransactionCategory created = transactionCategoryService.createTransactionCategory(1, "Dining", "#33FF57");

        assertNotNull(created);
        assertEquals("Dining", created.getCategoryName());
        verify(transactionCategoryRepository).save(any(TransactionCategory.class));
    }

    @Test
    void testCreateTransactionCategory_UserNotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                transactionCategoryService.createTransactionCategory(999, "Dining", "#33FF57"));
    }

    @Test
    void testGetAllTransactionCategoriesByUserId() {
        when(transactionCategoryRepository.findAllByUserId(1)).thenReturn(List.of(sampleCategory));

        List<TransactionCategory> list = transactionCategoryService.getAllTransactionCategoriesByUserId(1);

        assertEquals(1, list.size());
        assertEquals("Dining", list.get(0).getCategoryName());
    }

    @Test
    void testUpdateTransactionCategoryById_Success() {
        when(transactionCategoryRepository.findById(1)).thenReturn(Optional.of(sampleCategory));
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenReturn(sampleCategory);

        TransactionCategory updated = transactionCategoryService.updateTransactionCategoryById(1, "Food & Drinks", "#112233");

        assertNotNull(updated);
        assertEquals("Food & Drinks", updated.getCategoryName());
        assertEquals("#112233", updated.getCategoryColor());
    }

    @Test
    void testDeleteTransactionCategoryById_Success_UnlinksTransactions() {
        when(transactionCategoryRepository.findById(1)).thenReturn(Optional.of(sampleCategory));
        when(transactionRepository.unlinkCategory(1)).thenReturn(3);

        boolean result = transactionCategoryService.deleteTransactionCategoryById(1);

        assertTrue(result);
        verify(transactionRepository).unlinkCategory(1);
        verify(transactionCategoryRepository).delete(sampleCategory);
    }

    @Test
    void testDeleteTransactionCategoryById_NotFound_ReturnsFalse() {
        when(transactionCategoryRepository.findById(999)).thenReturn(Optional.empty());

        boolean result = transactionCategoryService.deleteTransactionCategoryById(999);

        assertFalse(result);
        verify(transactionCategoryRepository, never()).delete(any());
    }
}
