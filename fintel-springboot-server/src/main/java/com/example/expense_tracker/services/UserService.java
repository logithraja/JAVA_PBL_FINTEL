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
import com.example.expense_tracker.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.example.expense_tracker.repositories.RefreshTokenRepository refreshTokenRepository;

    @org.springframework.beans.factory.annotation.Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    private String hashToken(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public Optional<User> getUserById(int userId) {
        log.info("Getting the user by id: {}", userId);
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        log.info("Getting the user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User createUser(String name, String email, String rawPassword) {
        log.info("Creating user with email: {}", email);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateResourceException("An account with email " + email + " already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public String createRefreshToken(Integer userId) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String hash = hashToken(rawToken);
        java.time.Instant expiresAt = java.time.Instant.now().plusMillis(refreshExpirationMs);
        com.example.expense_tracker.entities.RefreshToken rt = new com.example.expense_tracker.entities.RefreshToken(hash, userId, expiresAt);
        refreshTokenRepository.save(rt);
        return rawToken;
    }

    @Transactional
    public AuthDtos.TokenRefreshResponse refreshAccessToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new com.example.expense_tracker.exceptions.UnauthorizedException("Refresh token is required");
        }

        String hash = hashToken(rawRefreshToken.trim());
        com.example.expense_tracker.entities.RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new com.example.expense_tracker.exceptions.UnauthorizedException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            log.warn("Refresh token reuse detected for user ID: {}! Revoking all refresh tokens.", storedToken.getUserId());
            refreshTokenRepository.revokeAllByUserId(storedToken.getUserId());
            throw new com.example.expense_tracker.exceptions.UnauthorizedException("Refresh token reuse detected. Please log in again.");
        }

        if (storedToken.isExpired()) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new com.example.expense_tracker.exceptions.UnauthorizedException("Refresh token expired. Please log in again.");
        }

        // Invalidate the consumed refresh token (token rotation)
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = jwtTokenProvider.generateToken(principal);
        String newRefreshToken = createRefreshToken(user.getId());

        return new AuthDtos.TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        String hash = hashToken(rawRefreshToken.trim());
        refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Refresh token revoked for user ID: {}", rt.getUserId());
        });
    }

    @Transactional
    public AuthDtos.AuthResponse login(String email, String rawPassword) {
        log.info("Authenticating user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String token = jwtTokenProvider.generateToken(principal);
        String refreshToken = createRefreshToken(user.getId());

        return new AuthDtos.AuthResponse(token, refreshToken, user.getId(), user.getName(), user.getEmail());
    }

    @Transactional
    public String createPasswordResetToken(String email) {
        log.info("Generating password reset token for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + email));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                LocalDateTime.now().plusMinutes(15)
        );

        passwordResetTokenRepository.save(resetToken);
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Attempting password reset with token");
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isExpired() || resetToken.isUsed()) {
            throw new BadRequestException("Password reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        log.info("Password successfully reset for user: {}", user.getEmail());
    }
}
