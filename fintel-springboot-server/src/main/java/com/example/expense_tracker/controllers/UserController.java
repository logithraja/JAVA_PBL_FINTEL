package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.AuthDtos;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.exceptions.ResourceNotFoundException;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User & Authentication", description = "Endpoints for signup, login, password recovery, and user profiles")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Check if an email is already registered")
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @Operation(summary = "Get user details by email")
    @GetMapping
    public ResponseEntity<AuthDtos.UserResponse> getUserByEmail(
            @RequestParam String email,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Getting user by email: {}", email);

        if (principal != null && !principal.getEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("You are not authorized to view another user's profile");
        }

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        AuthDtos.UserResponse response = new AuthDtos.UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login and receive JWT authentication token")
    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> loginUser(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestBody(required = false) AuthDtos.LoginRequest loginRequest
    ) {
        String loginEmail = (loginRequest != null && loginRequest.getEmail() != null) ? loginRequest.getEmail() : email;
        String loginPassword = (loginRequest != null && loginRequest.getPassword() != null) ? loginRequest.getPassword() : password;

        if (loginEmail == null || loginPassword == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        log.info("Login attempt for email: {}", loginEmail);
        AuthDtos.AuthResponse authResponse = userService.login(loginEmail, loginPassword);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Create / register a new user account")
    @PostMapping
    public ResponseEntity<AuthDtos.UserResponse> createUser(@Valid @RequestBody AuthDtos.SignUpRequest request) {
        log.info("Creating new user with email: {}", request.getEmail());
        User newUser = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        AuthDtos.UserResponse response = new AuthDtos.UserResponse(
                newUser.getId(),
                newUser.getName(),
                newUser.getEmail(),
                newUser.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Request password reset token")
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
        String token = userService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Password reset token generated successfully",
                "resetToken", token
        ));
    }

    @Operation(summary = "Reset password using reset token")
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been successfully reset"));
    }

    @Operation(summary = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthDtos.TokenRefreshResponse> refreshToken(@Valid @RequestBody AuthDtos.RefreshTokenRequest request) {
        log.info("Refreshing access token");
        AuthDtos.TokenRefreshResponse response = userService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout and revoke refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody(required = false) AuthDtos.RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            log.info("Revoking refresh token on logout");
            userService.revokeRefreshToken(request.getRefreshToken());
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
