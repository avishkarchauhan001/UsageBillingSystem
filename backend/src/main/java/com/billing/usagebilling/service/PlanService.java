package com.billing.usagebilling.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.entity.Plan;
import com.billing.usagebilling.repository.PlanRepository;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanDto> getActivePlans() {
        return planRepository.findByPlanStateOrderByMonthlyChargeUsdAsc("Activated").stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PlanDto> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan with ID " + id + " not found"));
    }

    @Transactional
    public PlanDto createPlan(PlanDto dto) {
        if (planRepository.existsByPackageName(dto.getPackageName())) {
            throw new RuntimeException("Plan package name already exists!");
        }

        validatePlanDto(dto);

        Plan plan = new Plan(
                dto.getPackageName(),
                dto.getDataAllowanceGb(),
                dto.getMonthlyChargeUsd(),
                dto.getChargesAfterLimitPerMb()
        );
        plan.setPlanState("Activated");
        Plan saved = planRepository.save(plan);
        return toDto(saved);
    }

    @Transactional
    public PlanDto updatePlan(Long id, PlanDto dto) {
        Plan plan = getPlanById(id);
        validatePlanDto(dto);

        // Check unique package name if changed
        if (!plan.getPackageName().equalsIgnoreCase(dto.getPackageName())
                && planRepository.existsByPackageName(dto.getPackageName())) {
            throw new RuntimeException("Plan package name already exists!");
        }

        plan.setPackageName(dto.getPackageName());
        plan.setDataAllowanceGb(dto.getDataAllowanceGb());
        plan.setMonthlyChargeUsd(dto.getMonthlyChargeUsd());
        plan.setChargesAfterLimitPerMb(dto.getChargesAfterLimitPerMb());
        plan.setUpdatedAt(LocalDateTime.now());
        if (dto.getPlanState() != null && !dto.getPlanState().isBlank()) {
            plan.setPlanState(dto.getPlanState());
        }

        return toDto(planRepository.save(plan));
    }

    @Transactional
    public void deactivatePlan(Long id) {
        Plan plan = getPlanById(id);
        plan.setPlanState("Deactivated");
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);
    }

    private void validatePlanDto(PlanDto dto) {
        if (dto.getPackageName() == null || dto.getPackageName().trim().isEmpty()) {
            throw new RuntimeException("Package name is required");
        }
        if (dto.getDataAllowanceGb() == null || dto.getDataAllowanceGb() <= 0) {
            throw new RuntimeException("Data in GB must be greater than 0");
        }
        if (dto.getMonthlyChargeUsd() == null || dto.getMonthlyChargeUsd().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Monthly charge in USD cannot be negative");
        }
        if (dto.getChargesAfterLimitPerMb() == null || dto.getChargesAfterLimitPerMb().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Charges after limit cannot be negative");
        }
    }

    public PlanDto toDto(Plan plan) {
        return new PlanDto(
                plan.getId(),
                plan.getPackageName(),
                plan.getDataAllowanceGb(),
                plan.getMonthlyChargeUsd(),
                plan.getChargesAfterLimitPerMb(),
                plan.getPlanState()
        );
    }
}
