package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.HealthScoreResponse;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.HealthScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Financial Health Score", description = "Compute the user's financial health score")
public class HealthScoreController {

    private static final Logger log = LoggerFactory.getLogger(HealthScoreController.class);

    @Autowired
    private HealthScoreService healthScoreService;

    @Operation(summary = "Get the user's financial health score and component breakdown")
    @GetMapping
    public ResponseEntity<HealthScoreResponse> getHealthScore(
            @RequestParam int userId,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal != null && !principal.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        log.info("Health score requested for user {}", userId);
        HealthScoreResponse response = healthScoreService.compute(userId);
        return ResponseEntity.ok(response);
    }
}
