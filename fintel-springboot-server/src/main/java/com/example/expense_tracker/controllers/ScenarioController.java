package com.example.expense_tracker.controllers;

import com.example.expense_tracker.dtos.ScenarioSimulationRequest;
import com.example.expense_tracker.dtos.ScenarioSimulationResponse;
import com.example.expense_tracker.exceptions.UnauthorizedException;
import com.example.expense_tracker.security.UserPrincipal;
import com.example.expense_tracker.services.ScenarioSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scenario")
@Tag(name = "Scenario Simulation", description = "Future You — what-if scenario simulation endpoints")
public class ScenarioController {

    private static final Logger log = LoggerFactory.getLogger(ScenarioController.class);

    @Autowired
    private ScenarioSimulationService scenarioSimulationService;

    @Operation(summary = "Simulate a hypothetical spending scenario and project its impact over 12 months")
    @PostMapping("/simulate")
    public ResponseEntity<ScenarioSimulationResponse> simulate(
            @Valid @RequestBody ScenarioSimulationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal != null && !principal.getId().equals(request.getUserId())) {
            throw new UnauthorizedException("You are not authorized to simulate scenarios for this user.");
        }

        log.info("Scenario simulation: user={}, amount={}, category={}, month={}",
                request.getUserId(), request.getAmount(), request.getCategory(), request.getTargetMonth());

        int targetYear = (request.getTargetYear() != null) ? request.getTargetYear() : java.time.LocalDate.now().getYear();

        ScenarioSimulationResponse response = scenarioSimulationService.simulate(
                request.getUserId(),
                request.getAmount(),
                request.getCategory(),
                request.getTargetMonth(),
                targetYear
        );

        return ResponseEntity.ok(response);
    }
}
