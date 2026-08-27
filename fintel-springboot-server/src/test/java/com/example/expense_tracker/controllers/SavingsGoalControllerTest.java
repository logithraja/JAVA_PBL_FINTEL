package com.example.expense_tracker.controllers;

import com.example.expense_tracker.entities.SavingsGoal;
import com.example.expense_tracker.entities.User;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.SavingsGoalService;
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

import java.math.BigDecimal;
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
public class SavingsGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SavingsGoalService savingsGoalService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private User sampleUser;
    private SavingsGoal sampleGoal;
    private UsernamePasswordAuthenticationToken authPrincipalUser1;
    private UsernamePasswordAuthenticationToken authPrincipalUser2;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1, "Alice", "alice@example.com", "password", null);
        sampleGoal = new SavingsGoal(1, sampleUser, "Vacation",
                BigDecimal.valueOf(2000), BigDecimal.valueOf(500),
                LocalDate.of(2026, 12, 31), false);

        UserPrincipal principal1 = new UserPrincipal(1, "Alice", "alice@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authPrincipalUser1 = new UsernamePasswordAuthenticationToken(principal1, null, principal1.getAuthorities());

        UserPrincipal principal2 = new UserPrincipal(2, "Bob", "bob@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authPrincipalUser2 = new UsernamePasswordAuthenticationToken(principal2, null, principal2.getAuthorities());
    }

    @Test
    void testGetGoals_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/savings-goals/user/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetGoals_WithAuth_Success() throws Exception {
        when(savingsGoalService.listByUser(1)).thenReturn(List.of(sampleGoal));

        mockMvc.perform(get("/api/v1/savings-goals/user/1")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Vacation"));
    }

    @Test
    void testGetGoals_CrossUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/savings-goals/user/1")
                        .with(authentication(authPrincipalUser2)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateGoal_Success() throws Exception {
        when(savingsGoalService.create(any(SavingsGoal.class), eq(1))).thenReturn(sampleGoal);

        mockMvc.perform(post("/api/v1/savings-goals")
                        .with(authentication(authPrincipalUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGoal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Vacation"));
    }

    @Test
    void testDeleteGoal_Success() throws Exception {
        when(savingsGoalService.getById(1)).thenReturn(Optional.of(sampleGoal));

        mockMvc.perform(delete("/api/v1/savings-goals/1")
                        .with(authentication(authPrincipalUser1)))
                .andExpect(status().isOk());
    }
}
