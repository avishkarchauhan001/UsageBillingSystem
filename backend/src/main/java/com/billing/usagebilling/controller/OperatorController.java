package com.billing.usagebilling.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.service.PlanService;

@RestController
@RequestMapping("/api/operator")
@CrossOrigin(origins = "*")
public class OperatorController {

    private final PlanService planService;

    public OperatorController(PlanService planService) {
        this.planService = planService;
    }

    /**
     * SRS US11: Plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    /**
     * SRS US12: Add plan
     */
    @PostMapping("/plans")
    public ResponseEntity<PlanDto> createPlan(@RequestBody PlanDto dto) {
        return new ResponseEntity<>(planService.createPlan(dto), HttpStatus.CREATED);
    }

    /**
     * SRS US13: Edit plan
     */
    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanDto> updatePlan(@PathVariable Long id, @RequestBody PlanDto dto) {
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }

    /**
     * SRS US14: Deactivate plan
     */
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deactivatePlan(@PathVariable Long id) {
        planService.deactivatePlan(id);
        return ResponseEntity.ok(Map.of("message", "Plan deactivated successfully"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
