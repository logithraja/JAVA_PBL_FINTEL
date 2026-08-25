package com.example.expense_tracker.services;

import com.example.expense_tracker.entities.SavingsGoal;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.SavingsGoalRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SavingsGoalService savingsGoalService;

    private User sampleUser;
    private SavingsGoal sampleGoal;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Test User", "test@example.com", "password", null);
        sampleGoal = new SavingsGoal(1, sampleUser, "Emergency Fund",
                BigDecimal.valueOf(5000), BigDecimal.valueOf(1000),
                LocalDate.of(2026, 12, 31), false);
    }

    @Test
    void testCreate_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(sampleGoal);

        SavingsGoal created = savingsGoalService.create(sampleGoal, 1);

        assertNotNull(created);
        assertEquals("Emergency Fund", created.getName());
        verify(savingsGoalRepository).save(sampleGoal);
    }

    @Test
    void testCreate_UserNotFound_ThrowsException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                savingsGoalService.create(sampleGoal, 999));
    }

    @Test
    void testListByUser() {
        when(savingsGoalRepository.findByUserId(1)).thenReturn(List.of(sampleGoal));

        List<SavingsGoal> result = savingsGoalService.listByUser(1);

        assertEquals(1, result.size());
        assertEquals("Emergency Fund", result.get(0).getName());
    }

    @Test
    void testUpsert_UpdateExisting() {
        when(savingsGoalRepository.findById(1)).thenReturn(Optional.of(sampleGoal));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(sampleGoal);

        sampleGoal.setName("Updated Fund");
        SavingsGoal updated = savingsGoalService.upsert(sampleGoal, 1);

        assertNotNull(updated);
        assertEquals("Updated Fund", updated.getName());
    }

    @Test
    void testDelete_Success() {
        when(savingsGoalRepository.existsById(1)).thenReturn(true);

        savingsGoalService.delete(1);

        verify(savingsGoalRepository).deleteById(1);
    }

    @Test
    void testDelete_NotFound_ThrowsException() {
        when(savingsGoalRepository.existsById(999)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                savingsGoalService.delete(999));
    }
}
