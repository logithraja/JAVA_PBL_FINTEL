package com.example.expense_tracker.security;

import com.example.expense_tracker.dtos.AIDtos;
import com.example.expense_tracker.dtos.AuthDtos;
import com.example.expense_tracker.services.AIProxyService;
import com.example.expense_tracker.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitingService rateLimitingService;

    @MockBean
    private UserService userService;

    @MockBean
    private AIProxyService aiProxyService;

    @BeforeEach
    void setUp() {
        rateLimitingService.reset();
    }

    @Test
    void testAuthRateLimiting_ExceedLimitReturns429() throws Exception {
        AuthDtos.LoginRequest loginRequest = new AuthDtos.LoginRequest("ratelimit@example.com", "Password123!");
        when(userService.login(any(), any()))
                .thenReturn(new AuthDtos.AuthResponse("dummy-token", 1, "Rate User", "ratelimit@example.com"));

        // First 5 requests should pass (either 200 or whatever status, not 429)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        // 6th request from the same IP should be blocked with 429 Too Many Requests
        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    @WithMockUser(username = "ai-user@example.com")
    void testAIRateLimiting_ExceedLimitReturns429() throws Exception {
        AIDtos.ChatRequest chatRequest = new AIDtos.ChatRequest("system", "hello");
        when(aiProxyService.generateChat(any(), any())).thenReturn("Mock response");

        // First 20 AI requests should pass
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/v1/ai/chat")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(chatRequest)))
                    .andExpect(status().isOk());
        }

        // 21st request should be rate-limited
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
    }
}
