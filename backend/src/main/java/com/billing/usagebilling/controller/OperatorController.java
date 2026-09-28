package com.billing.usagebilling.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.dto.PlanReportDto;
import com.billing.usagebilling.service.PlanService;

@RestController
@RequestMapping("/api/operator")
@CrossOrigin(origins = "*")
public class OperatorController {

    private final PlanService planService;

    public OperatorController(PlanService planService) {
        this.planService = planService;
    }

    private void checkOperatorAuthorization(String role) {
        if (role != null && "CUSTOMER".equalsIgnoreCase(role.trim())) {
            throw new SecurityException("Access Denied: Customer role cannot perform Operator actions.");
        }
    }

    /**
     * SRS US11: Plans listing with pagination (10 per screen by default)
     */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanDto>> getActivePlans(
            @RequestParam(required = false, defaultValue = "true") boolean activeOnly,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkOperatorAuthorization(role);
        if (activeOnly) {
            return ResponseEntity.ok(planService.getActivePlans());
        }
        return ResponseEntity.ok(planService.getAllPlans());
    }

    /**
     * SRS US11: Server-side paginated plans
     */
    @GetMapping("/plans/paginated")
    public ResponseEntity<Page<PlanDto>> getPaginatedPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkOperatorAuthorization(role);
        return ResponseEntity.ok(planService.getActivePlansPaginated(page, size));
    }

    /**
     * SRS US12: Add plan
     */
    @PostMapping("/plans")
    public ResponseEntity<PlanDto> createPlan(
            @RequestBody PlanDto dto,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkOperatorAuthorization(role);
        return new ResponseEntity<>(planService.createPlan(dto), HttpStatus.CREATED);
    }

    /**
     * SRS US13: Edit plan
     */
    @PutMapping("/plans/{id}")
    public ResponseEntity<PlanDto> updatePlan(
            @PathVariable Long id,
            @RequestBody PlanDto dto,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkOperatorAuthorization(role);
        return ResponseEntity.ok(planService.updatePlan(id, dto));
    }

    /**
     * SRS US14: Deactivate plan
     */
    @DeleteMapping("/plans/{id}")
    public ResponseEntity<Map<String, String>> deactivatePlan(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkOperatorAuthorization(role);
        planService.deactivatePlan(id);
        return ResponseEntity.ok(Map.of("message", "Plan deactivated successfully"));
    }

    @PatchMapping("/plans/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivatePlanPatch(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return deactivatePlan(id, role);
    }

    /**
     * SRS US07: Plan Report / Sales pictorial analysis
     */
    @GetMapping("/report")
    public ResponseEntity<List<PlanReportDto>> getPlanSalesReport(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        checkOperatorAuthorization(role);
        return ResponseEntity.ok(planService.getPlanSalesReport());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
