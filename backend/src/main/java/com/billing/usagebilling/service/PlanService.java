package com.billing.usagebilling.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.billing.usagebilling.dto.PlanDto;
import com.billing.usagebilling.dto.PlanReportDto;
import com.billing.usagebilling.entity.Bill;
import com.billing.usagebilling.entity.Plan;
import com.billing.usagebilling.repository.BillRepository;
import com.billing.usagebilling.repository.CustomerPlanRepository;
import com.billing.usagebilling.repository.PlanRepository;

@Service
public class PlanService {

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]+$");

    private final PlanRepository planRepository;
    private final CustomerPlanRepository customerPlanRepository;
    private final BillRepository billRepository;

    public PlanService(
            PlanRepository planRepository,
            CustomerPlanRepository customerPlanRepository,
            BillRepository billRepository) {
        this.planRepository = planRepository;
        this.customerPlanRepository = customerPlanRepository;
        this.billRepository = billRepository;
    }

    public List<PlanDto> getActivePlans() {
        return planRepository.findByPlanStateOrderByMonthlyChargeUsdAsc("Activated").stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<PlanDto> getActivePlansPaginated(int page, int size) {
        List<PlanDto> allActive = getActivePlans();
        int total = allActive.size();
        int start = Math.min(page * size, total);
        int end = Math.min(start + size, total);
        List<PlanDto> pageContent = (start <= end) ? allActive.subList(start, end) : List.of();
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(pageContent, pageable, total);
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
        validatePlanDto(dto);

        if (planRepository.existsByPackageName(dto.getPackageName().trim())) {
            throw new RuntimeException("Plan package name already exists!");
        }

        Plan plan = new Plan(
                dto.getPackageName().trim(),
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
        if (!plan.getPackageName().equalsIgnoreCase(dto.getPackageName().trim())
                && planRepository.existsByPackageName(dto.getPackageName().trim())) {
            throw new RuntimeException("Plan package name already exists!");
        }

        plan.setPackageName(dto.getPackageName().trim());
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
        if ("Deactivated".equalsIgnoreCase(plan.getPlanState())) {
            throw new RuntimeException("Plan is already deactivated");
        }
        plan.setPlanState("Deactivated");
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);
    }

    public List<PlanReportDto> getPlanSalesReport() {
        List<Plan> activePlans = planRepository.findByPlanStateOrderByMonthlyChargeUsdAsc("Activated");
        long totalSubscribers = 0;
        List<PlanReportDto> reportList = new ArrayList<>();

        for (Plan plan : activePlans) {
            long subscribers = customerPlanRepository.countByPlanIdAndStatus(plan.getId(), "ACTIVE");
            totalSubscribers += subscribers;

            List<Bill> bills = billRepository.findByPlanId(plan.getId());
            double totalUsageGb = bills.stream().mapToDouble(Bill::getUsageInGb).sum();
            BigDecimal totalRevenue = bills.stream()
                    .map(Bill::getTotalAmountUsd)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            PlanReportDto dto = new PlanReportDto(
                    plan.getId(),
                    plan.getPackageName(),
                    plan.getDataAllowanceGb(),
                    plan.getMonthlyChargeUsd(),
                    plan.getChargesAfterLimitPerMb(),
                    plan.getPlanState(),
                    subscribers,
                    Math.round(totalUsageGb * 100.0) / 100.0,
                    totalRevenue,
                    0.0
            );
            reportList.add(dto);
        }

        for (PlanReportDto r : reportList) {
            if (totalSubscribers > 0) {
                double pct = ((double) r.getSubscriberCount() / totalSubscribers) * 100.0;
                r.setSubscriberPercentage(Math.round(pct * 10.0) / 10.0);
            } else {
                r.setSubscriberPercentage(0.0);
            }
        }

        return reportList;
    }

    private void validatePlanDto(PlanDto dto) {
        if (dto.getPackageName() == null || dto.getPackageName().trim().isEmpty()) {
            throw new RuntimeException("Package name is required");
        }
        if (!ALPHANUMERIC_PATTERN.matcher(dto.getPackageName().trim()).matches()) {
            throw new RuntimeException("Package name may contain only alphanumeric characters");
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
