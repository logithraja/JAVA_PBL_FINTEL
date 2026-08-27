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

    public AuthDtos.AuthResponse login(String email, String rawPassword) {
        log.info("Authenticating user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String token = jwtTokenProvider.generateToken(principal);

        return new AuthDtos.AuthResponse(token, user.getId(), user.getName(), user.getEmail());
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
