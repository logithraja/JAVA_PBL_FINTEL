package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.Transaction;
import com.example.expense_tracker.entities.TransactionCategory;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.TransactionService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;
    private TransactionCategory sampleCategory;
    private Transaction sampleTransaction;
    private UsernamePasswordAuthenticationToken authPrincipalUser1;
    private UsernamePasswordAuthenticationToken authPrincipalUser2;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Alice", "alice@example.com", "password", null);
        sampleCategory = new TransactionCategory(1, sampleUser, "Food", "#FF5733");
        sampleTransaction = new Transaction(10, sampleCategory, sampleUser, "Coffee", 5.0,
                LocalDate.of(2026, 8, 25), "09:00 AM", "expense");

        UserPrincipal principal1 = new UserPrincipal(1, "Alice", "alice@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authPrincipalUser1 = new UsernamePasswordAuthenticationToken(principal1, null, principal1.getAuthorities());

        UserPrincipal principal2 = new UserPrincipal(2, "Bob", "bob@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authPrincipalUser2 = new UsernamePasswordAuthenticationToken(principal2, null, principal2.getAuthorities());
    }

    @Test
    void testGetTransactions_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/user/1?year=2026"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetTransactions_WithAuth_Success() throws Exception {
        when(transactionService.getAllTransactionsByUserIdAndYear(1, 2026))
                .thenReturn(List.of(sampleTransaction));

        mockMvc.perform(get("/api/v1/transaction/user/1?year=2026")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionName").value("Coffee"))
                .andExpect(jsonPath("$[0].transactionAmount").value(5.0));
    }

    @Test
    void testGetTransactions_CrossUserAccess_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/user/1?year=2026")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateTransaction_Success() throws Exception {
        when(transactionService.createTransaction(any(Transaction.class))).thenReturn(sampleTransaction);

        mockMvc.perform(post("/api/v1/transaction")
                        .with(authentication(authPrincipalUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionName").value("Coffee"));
    }

    @Test
    void testUpdateTransaction_Success() throws Exception {
        when(transactionService.getTransactionById(10)).thenReturn(Optional.of(sampleTransaction));
        when(transactionService.updateTransaction(any(Transaction.class))).thenReturn(sampleTransaction);

        mockMvc.perform(put("/api/v1/transaction")
                        .with(authentication(authPrincipalUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTransaction)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteTransaction_Success() throws Exception {
        when(transactionService.getTransactionById(10)).thenReturn(Optional.of(sampleTransaction));

        mockMvc.perform(delete("/api/v1/transaction/10")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteTransaction_CrossUser_Returns403() throws Exception {
        when(transactionService.getTransactionById(10)).thenReturn(Optional.of(sampleTransaction));

        mockMvc.perform(delete("/api/v1/transaction/10")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCheckDuplicates_Success() throws Exception {
        LocalDate date = LocalDate.of(2026, 8, 25);
        when(transactionService.findPossibleDuplicates(1, "Coffee", 5.0, date))
                .thenReturn(List.of(sampleTransaction));

        mockMvc.perform(get("/api/v1/transaction/duplicate-check?userId=1&merchant=Coffee&amount=5.0&date=2026-08-25")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionName").value("Coffee"))
                .andExpect(jsonPath("$[0].transactionAmount").value(5.0));
    }

    @Test
    void testCheckDuplicates_CrossUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/duplicate-check?userId=1&merchant=Coffee&amount=5.0&date=2026-08-25")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetMonthlySpendingForecast_Success() throws Exception {
        com.example.expense_tracker.dtos.SpendingForecastResponse forecast =
                new com.example.expense_tracker.dtos.SpendingForecastResponse(
                        50.0, 2.0, 62.0, 25, 6, 31, 100.0, -38.0, "UNDER_BUDGET", "At this pace: ₹62.00 by month-end"
                );
        when(transactionService.calculateMonthlySpendingForecast(1, 2026, 8, 100.0)).thenReturn(forecast);

        mockMvc.perform(get("/api/v1/transaction/forecast/1?year=2026&month=8&budget=100.0")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spentSoFar").value(50.0))
                .andExpect(jsonPath("$.projectedTotal").value(62.0))
                .andExpect(jsonPath("$.status").value("UNDER_BUDGET"));
    }

    @Test
    void testGetMonthlySpendingForecast_CrossUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/transaction/forecast/1?year=2026&month=8")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }
}
