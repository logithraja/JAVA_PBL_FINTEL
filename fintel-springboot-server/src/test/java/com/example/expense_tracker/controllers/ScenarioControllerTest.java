package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.BaselineForecastResponse;
import com.example.expense_tracker.dtos.ScenarioSimulationRequest;
import com.example.expense_tracker.dtos.ScenarioSimulationResponse;
import com.example.expense_tracker.dtos.VulnerabilityResponse;
import com.example.expense_tracker.security.CustomUserDetailsService;
import com.example.expense_tracker.security.JwtTokenProvider;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.ScenarioSimulationService;
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

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScenarioSimulationService scenarioSimulationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UsernamePasswordAuthenticationToken authUser1;
    private UsernamePasswordAuthenticationToken authUser2;

    @BeforeEach
    void setUp() {
        UserPrincipal principal1 = new UserPrincipal(1, "Alice", "alice@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authUser1 = new UsernamePasswordAuthenticationToken(principal1, null, principal1.getAuthorities());

        UserPrincipal principal2 = new UserPrincipal(2, "Bob", "bob@example.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authUser2 = new UsernamePasswordAuthenticationToken(principal2, null, principal2.getAuthorities());
    }

    @Test
    void testSimulate_WithoutAuth_Returns401() throws Exception {
        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(80000.0);
        request.setCategory("Shopping");
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSimulate_WithValidInput_Success() throws Exception {
        ScenarioSimulationResponse mockResponse = new ScenarioSimulationResponse();
        mockResponse.setSufficientData(true);
        mockResponse.setStatusMessage("Simulation complete.");

        ScenarioSimulationResponse.ScenarioInput input = new ScenarioSimulationResponse.ScenarioInput();
        input.setAmount(80000);
        input.setCategory("Shopping");
        input.setTargetMonth(9);
        input.setTargetYear(2026);
        mockResponse.setInput(input);
        mockResponse.setOverallSummary("Test summary");

        when(scenarioSimulationService.simulate(eq(1), eq(80000.0), eq("Shopping"), eq(9), anyInt()))
                .thenReturn(mockResponse);

        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(80000.0);
        request.setCategory("Shopping");
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sufficientData").value(true))
                .andExpect(jsonPath("$.statusMessage").value("Simulation complete."))
                .andExpect(jsonPath("$.input.amount").value(80000))
                .andExpect(jsonPath("$.input.category").value("Shopping"));
    }

    @Test
    void testSimulate_MissingAmount_Returns400() throws Exception {
        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setCategory("Shopping");
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSimulate_MissingCategory_Returns400() throws Exception {
        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(50000.0);
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSimulate_MissingTargetMonth_Returns400() throws Exception {
        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(50000.0);
        request.setCategory("Food");

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSimulate_CrossUserAccess_Returns403() throws Exception {
        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(80000.0);
        request.setCategory("Shopping");
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSimulate_InsufficientData_ReturnsGracefulResponse() throws Exception {
        ScenarioSimulationResponse mockResponse = new ScenarioSimulationResponse();
        mockResponse.setSufficientData(false);
        mockResponse.setStatusMessage("Insufficient data for precise simulation.");
        mockResponse.setOverallSummary("Insufficient transaction history.");

        when(scenarioSimulationService.simulate(eq(1), eq(80000.0), eq("Shopping"), eq(9), anyInt()))
                .thenReturn(mockResponse);

        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(80000.0);
        request.setCategory("Shopping");
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sufficientData").value(false))
                .andExpect(jsonPath("$.statusMessage").value("Insufficient data for precise simulation."));
    }

    @Test
    void testSimulate_NegativeAmount_Returns400() throws Exception {
        ScenarioSimulationRequest request = new ScenarioSimulationRequest();
        request.setUserId(1);
        request.setAmount(-5000.0);
        request.setCategory("Shopping");
        request.setTargetMonth(9);

        mockMvc.perform(post("/api/v1/scenario/simulate")
                        .with(authentication(authUser1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
