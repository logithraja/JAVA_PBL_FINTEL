package com.example.expense_tracker.services;

import com.example.expense_tracker.dtos.AuthDtos;
import com.example.expense_tracker.entities.PasswordResetToken;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.BadRequestException;
import com.example.expense_tracker.exceptions.DuplicateResourceException;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.repositories.PasswordResetTokenRepository;
import com.example.expense_tracker.repositories.UserRepository;
import com.example.expense_tracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Test User", "test@example.com", "encodedPassword", LocalDateTime.now());
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User created = userService.createUser("Test User", "test@example.com", "rawPassword");

        assertNotNull(created);
        assertEquals("test@example.com", created.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateUser_DuplicateEmail_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        assertThrows(DuplicateResourceException.class, () ->
                userService.createUser("Test User", "test@example.com", "rawPassword"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any())).thenReturn("mockJwtToken");

        AuthDtos.AuthResponse response = userService.login("test@example.com", "rawPassword");

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void testLogin_WrongPassword_ThrowsBadCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () ->
                userService.login("test@example.com", "wrongPassword"));
    }

    @Test
    void testLogin_UserNotFound_ThrowsBadCredentials() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () ->
                userService.login("nonexistent@example.com", "anyPassword"));
    }

    @Test
    void testCreatePasswordResetToken_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(i -> i.getArgument(0));

        String token = userService.createPasswordResetToken("test@example.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void testCreatePasswordResetToken_UserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.createPasswordResetToken("unknown@example.com"));
    }

    @Test
    void testResetPassword_Success() {
        PasswordResetToken resetToken = new PasswordResetToken("token-123", sampleUser, LocalDateTime.now().plusMinutes(10));
        when(passwordResetTokenRepository.findByToken("token-123")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        userService.resetPassword("token-123", "newPassword");

        assertTrue(resetToken.isUsed());
        verify(userRepository).save(sampleUser);
        verify(passwordResetTokenRepository).save(resetToken);
    }

    @Test
    void testResetPassword_ExpiredToken_ThrowsBadRequest() {
        PasswordResetToken expiredToken = new PasswordResetToken("token-123", sampleUser, LocalDateTime.now().minusMinutes(5));
        when(passwordResetTokenRepository.findByToken("token-123")).thenReturn(Optional.of(expiredToken));

        assertThrows(BadRequestException.class, () ->
                userService.resetPassword("token-123", "newPassword"));
    }
}
