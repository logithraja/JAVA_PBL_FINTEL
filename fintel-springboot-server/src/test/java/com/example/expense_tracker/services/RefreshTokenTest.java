package com.example.expense_tracker.services;

import com.example.expense_tracker.dtos.AuthDtos;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.repositories.RefreshTokenRepository;
import com.example.expense_tracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RefreshTokenTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Refresh User");
        testUser.setEmail("refresh@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    void testCreateAndRefreshSuccess() {
        String refreshToken = userService.createRefreshToken(testUser.getId());
        assertNotNull(refreshToken);

        AuthDtos.TokenRefreshResponse response = userService.refreshAccessToken(refreshToken);
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(refreshToken, response.getRefreshToken(), "Rotated refresh token must be different from old token");
    }

    @Test
    void testTokenReuseDetection() {
        String initialToken = userService.createRefreshToken(testUser.getId());

        // First use: rotates token successfully
        AuthDtos.TokenRefreshResponse response = userService.refreshAccessToken(initialToken);
        assertNotNull(response.getToken());

        // Second use of the same token: must trigger reuse detection and fail
        assertThrows(UnauthorizedException.class, () -> userService.refreshAccessToken(initialToken));
    }

    @Test
    void testRevokeRefreshToken() {
        String token = userService.createRefreshToken(testUser.getId());
        userService.revokeRefreshToken(token);

        assertThrows(UnauthorizedException.class, () -> userService.refreshAccessToken(token));
    }
}
