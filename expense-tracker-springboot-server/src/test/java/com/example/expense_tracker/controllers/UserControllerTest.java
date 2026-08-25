package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.AuthDtos;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void testSignup_Success() throws Exception {
        AuthDtos.SignUpRequest request = new AuthDtos.SignUpRequest("John Doe", "john@example.com", "Password123!");
        User createdUser = new User(1, "John Doe", "john@example.com", "hashed", LocalDateTime.now());

        when(userService.createUser("John Doe", "john@example.com", "Password123!")).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testSignup_ValidationFailure() throws Exception {
        AuthDtos.SignUpRequest request = new AuthDtos.SignUpRequest("", "invalid-email", "123");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void testLogin_Success() throws Exception {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("john@example.com", "Password123!");
        AuthDtos.AuthResponse authResponse = new AuthDtos.AuthResponse("jwt-token-123", 1, "John Doe", "john@example.com");

        when(userService.login("john@example.com", "Password123!")).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testLogin_BadCredentials() throws Exception {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest("john@example.com", "WrongPassword");

        when(userService.login("john@example.com", "WrongPassword"))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCheckEmailExists_Public() throws Exception {
        when(userService.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(get("/api/v1/user/exists?email=test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void testForgotPassword_Success() throws Exception {
        AuthDtos.ForgotPasswordRequest request = new AuthDtos.ForgotPasswordRequest("test@example.com");
        when(userService.createPasswordResetToken("test@example.com")).thenReturn("reset-token-xyz");

        mockMvc.perform(post("/api/v1/user/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetToken").value("reset-token-xyz"));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        AuthDtos.ResetPasswordRequest request = new AuthDtos.ResetPasswordRequest("reset-token-xyz", "NewPassword123!");

        mockMvc.perform(post("/api/v1/user/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
