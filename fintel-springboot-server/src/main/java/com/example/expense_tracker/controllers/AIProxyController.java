package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.AIDtos;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.AIProxyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Proxy", description = "Centralized proxy endpoints for AI chat, receipt scanning, transcription, and speech")
public class AIProxyController {

    private static final Logger log = LoggerFactory.getLogger(AIProxyController.class);

    @Autowired
    private AIProxyService aiProxyService;

    @Operation(summary = "Generate financial advisor or assistant text completion")
    @PostMapping("/chat")
    public ResponseEntity<AIDtos.ChatResponse> chat(
            @Valid @RequestBody AIDtos.ChatRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Integer userId = principal != null ? principal.getId() : null;
        log.info("AI Proxy chat request received from user ID: {}", userId);

        String systemPrompt = request.getSystemPrompt() != null ? request.getSystemPrompt() : "";
        String reply = aiProxyService.generateChat(systemPrompt, request.getUserMessage());
        return ResponseEntity.ok(new AIDtos.ChatResponse(reply));
    }

    @Operation(summary = "Process receipt or invoice image with OCR/Vision")
    @PostMapping("/vision")
    public ResponseEntity<AIDtos.VisionResponse> vision(
            @Valid @RequestBody AIDtos.VisionRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Integer userId = principal != null ? principal.getId() : null;
        log.info("AI Proxy vision request received from user ID: {}", userId);

        String jsonResult = aiProxyService.processVision(
                request.getBase64Image(),
                request.getMimeType() != null ? request.getMimeType() : "image/jpeg",
                request.getPrompt()
        );
        return ResponseEntity.ok(new AIDtos.VisionResponse(jsonResult));
    }

    @Operation(summary = "Transcribe audio recording to text")
    @PostMapping("/transcribe")
    public ResponseEntity<AIDtos.TranscribeResponse> transcribe(
            @Valid @RequestBody AIDtos.TranscribeRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Integer userId = principal != null ? principal.getId() : null;
        log.info("AI Proxy audio transcription request received from user ID: {}", userId);

        byte[] audioBytes = Base64.getDecoder().decode(request.getBase64Audio());
        String transcript = aiProxyService.transcribeAudio(
                audioBytes,
                request.getMimeType() != null ? request.getMimeType() : "audio/wav"
        );
        return ResponseEntity.ok(new AIDtos.TranscribeResponse(transcript));
    }

    @Operation(summary = "Generate speech audio stream from text")
    @PostMapping("/voice")
    public ResponseEntity<byte[]> voice(
            @Valid @RequestBody AIDtos.VoiceRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Integer userId = principal != null ? principal.getId() : null;
        log.info("AI Proxy voice synthesis request received from user ID: {}", userId);

        try {
            byte[] audioBytes = aiProxyService.generateSpeech(request.getText());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                    .body(audioBytes);
        } catch (Exception e) {
            log.error("AI Proxy voice synthesis error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
