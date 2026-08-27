package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.HealthScoreResponse;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.HealthScoreService;
import com.example.expense_tracker.services.TransactionService;
import com.example.expense_tracker.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HealthScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HealthScoreService healthScoreService;
    @MockBean private TransactionService transactionService;
    @MockBean private UserService userService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private UsernamePasswordAuthenticationToken authUser1;

    @BeforeEach
    void setUp() {
        UserPrincipal principal = new UserPrincipal(1, "Alice", "alice@test.com", "hashedpw",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authUser1 = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private HealthScoreResponse mockHealthyScore() {
        HealthScoreResponse resp = new HealthScoreResponse();
        resp.setSufficientData(true);
        resp.setOverallScore(82);
        resp.setGrade("A");
        resp.setMonthsAnalyzed(6);
        resp.setSummary("Your finances are in great shape!");
        resp.setSavingsRate(new HealthScoreResponse.ComponentScore("Savings Rate", 80, 15.2, "15.2% saved", "Good"));
        resp.setSpendingConsistency(new HealthScoreResponse.ComponentScore("Spending Consistency", 80, 0.15, "CV: 0.15", "Fairly consistent"));
        resp.setGoalProgress(new HealthScoreResponse.ComponentScore("Goal Progress", 90, 65.0, "65% avg progress", "Good progress"));
        resp.setBudgetAdherence(new HealthScoreResponse.ComponentScore("Budget Adherence", 80, 83.3, "83% months on budget", "Excellent"));
        resp.setRecommendations(Collections.singletonList("You're doing great!"));
        return resp;
    }

    @Test
    void getHealthScore_authenticated_returnsOk() throws Exception {
        when(healthScoreService.compute(1)).thenReturn(mockHealthyScore());

        mockMvc.perform(get("/api/v1/health")
                        .param("userId", "1")
                        .with(authentication(authUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").value(82))
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.sufficientData").value(true))
                .andExpect(jsonPath("$.monthsAnalyzed").value(6))
                .andExpect(jsonPath("$.savingsRate.score").value(80))
                .andExpect(jsonPath("$.spendingConsistency.score").value(80))
                .andExpect(jsonPath("$.goalProgress.score").value(90))
                .andExpect(jsonPath("$.budgetAdherence.score").value(80));
    }

    @Test
    void getHealthScore_crossUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .param("userId", "999")
                        .with(authentication(authUser1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getHealthScore_insufficientData_returnsFalse() throws Exception {
        HealthScoreResponse resp = new HealthScoreResponse();
        resp.setSufficientData(false);
        resp.setOverallScore(0);
        resp.setGrade("N/A");
        resp.setStatusMessage("No transaction data found.");
        resp.setMonthsAnalyzed(0);
        when(healthScoreService.compute(1)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/health")
                        .param("userId", "1")
                        .with(authentication(authUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sufficientData").value(false))
                .andExpect(jsonPath("$.overallScore").value(0))
                .andExpect(jsonPath("$.grade").value("N/A"));
    }

    @Test
    void getHealthScore_noAuth_returns401or403() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getHealthScore_lowScore_validResponse() throws Exception {
        HealthScoreResponse resp = new HealthScoreResponse();
        resp.setSufficientData(true);
        resp.setOverallScore(32);
        resp.setGrade("D");
        resp.setMonthsAnalyzed(4);
        resp.setSummary("Your finances need attention.");
        resp.setSavingsRate(new HealthScoreResponse.ComponentScore("Savings Rate", 10, -5.0, "-5.0% saved", "Negative"));
        resp.setSpendingConsistency(new HealthScoreResponse.ComponentScore("Spending Consistency", 20, 0.55, "CV: 0.55", "Highly variable"));
        resp.setGoalProgress(new HealthScoreResponse.ComponentScore("Goal Progress", 20, 5.0, "5% avg progress", "Early stages"));
        resp.setBudgetAdherence(new HealthScoreResponse.ComponentScore("Budget Adherence", 30, 25.0, "25% months on budget", "Needs improvement"));
        resp.setRecommendations(java.util.Arrays.asList(
                "Try to save at least 10% of your income each month.",
                "Track recurring expenses to stabilize your monthly spending."
        ));
        when(healthScoreService.compute(1)).thenReturn(resp);

        mockMvc.perform(get("/api/v1/health")
                        .param("userId", "1")
                        .with(authentication(authUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").value(32))
                .andExpect(jsonPath("$.grade").value("D"))
                .andExpect(jsonPath("$.sufficientData").value(true))
                .andExpect(jsonPath("$.recommendations.length()").value(2));
    }
}
