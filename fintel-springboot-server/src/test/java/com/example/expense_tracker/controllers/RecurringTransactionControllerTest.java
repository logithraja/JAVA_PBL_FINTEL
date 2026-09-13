package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.RecurringTransaction;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.RecurringTransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecurringTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecurringTransactionService recurringService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;
    private RecurringTransaction sampleRecurring;
    private UsernamePasswordAuthenticationToken authPrincipalUser1;
    private UsernamePasswordAuthenticationToken authPrincipalUser2;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Test User", "test@example.com", "password", LocalDateTime.now());
        sampleRecurring = new RecurringTransaction(
                1, sampleUser, null, null, "Netflix", 499.0, "EXPENSE",
                RecurringTransaction.Frequency.MONTHLY,
                LocalDate.now(), null, LocalDate.now(), null, true
        );

        UserPrincipal principalUser1 = UserPrincipal.create(sampleUser);
        authPrincipalUser1 = new UsernamePasswordAuthenticationToken(principalUser1, null, principalUser1.getAuthorities());

        User user2 = new User(2, "Other", "other@example.com", "password", LocalDateTime.now());
        UserPrincipal principalUser2 = UserPrincipal.create(user2);
        authPrincipalUser2 = new UsernamePasswordAuthenticationToken(principalUser2, null, principalUser2.getAuthorities());
    }

    @Test
    void testGetRecurring_Success() throws Exception {
        when(recurringService.getRecurringByUserId(1)).thenReturn(List.of(sampleRecurring));

        mockMvc.perform(get("/api/v1/recurring/user/1")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Netflix"))
                .andExpect(jsonPath("$[0].amount").value(499.0));
    }

    @Test
    void testGetRecurring_CrossUser_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/recurring/user/1")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCatchUp_Success() throws Exception {
        when(recurringService.catchUpForUser(1)).thenReturn(2);

        mockMvc.perform(post("/api/v1/recurring/user/1/catch-up")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generatedTransactions").value(2));
    }
}
