package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.AIDtos;
import com.example.expense_tracker.services.AIProxyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIProxyService aiProxyService;

    @Test
    void testChatEndpoint_UnauthenticatedReturns401() throws Exception {
        AIDtos.ChatRequest request = new AIDtos.ChatRequest("system", "hello");
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testChatEndpoint_AuthenticatedSuccess() throws Exception {
        AIDtos.ChatRequest request = new AIDtos.ChatRequest("system", "How can I budget better?");
        when(aiProxyService.generateChat(eq("system"), eq("How can I budget better?")))
                .thenReturn("Track your expenses weekly.");

        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Track your expenses weekly."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testVisionEndpoint_AuthenticatedSuccess() throws Exception {
        AIDtos.VisionRequest request = new AIDtos.VisionRequest("fakeBase64", "image/jpeg", "Extract receipt details");
        when(aiProxyService.processVision(eq("fakeBase64"), eq("image/jpeg"), eq("Extract receipt details")))
                .thenReturn("{\"totalAmount\":15.5,\"vendorName\":\"Cafe\"}");

        mockMvc.perform(post("/api/v1/ai/vision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("{\"totalAmount\":15.5,\"vendorName\":\"Cafe\"}"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testTranscribeEndpoint_AuthenticatedSuccess() throws Exception {
        byte[] audio = new byte[]{1, 2, 3};
        String base64Audio = Base64.getEncoder().encodeToString(audio);
        AIDtos.TranscribeRequest request = new AIDtos.TranscribeRequest(base64Audio, "audio/wav");

        when(aiProxyService.transcribeAudio(any(), eq("audio/wav")))
                .thenReturn("How is my budget?");

        mockMvc.perform(post("/api/v1/ai/transcribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("How is my budget?"));
    }
}
