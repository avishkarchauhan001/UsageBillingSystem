package com.billing.usagebilling.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_name", nullable = false, unique = true, length = 50)
    private String packageName;

    @Column(name = "data_allowance_gb", nullable = false)
    private Double dataAllowanceGb;

    @Column(name = "monthly_charge_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyChargeUsd;

    @Column(name = "charges_after_limit_per_mb", nullable = false, precision = 10, scale = 4)
    private BigDecimal chargesAfterLimitPerMb;

    @Column(name = "plan_state", nullable = false, length = 20)
    private String planState = "Activated"; // "Activated", "Deactivated"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Plan() {}

    public Plan(String packageName, Double dataAllowanceGb, BigDecimal monthlyChargeUsd, BigDecimal chargesAfterLimitPerMb) {
        this.packageName = packageName;
        this.dataAllowanceGb = dataAllowanceGb;
        this.monthlyChargeUsd = monthlyChargeUsd;
        this.chargesAfterLimitPerMb = chargesAfterLimitPerMb;
        this.planState = "Activated";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public Double getDataAllowanceGb() { return dataAllowanceGb; }
    public void setDataAllowanceGb(Double dataAllowanceGb) { this.dataAllowanceGb = dataAllowanceGb; }

    public BigDecimal getMonthlyChargeUsd() { return monthlyChargeUsd; }
    public void setMonthlyChargeUsd(BigDecimal monthlyChargeUsd) { this.monthlyChargeUsd = monthlyChargeUsd; }

    public BigDecimal getChargesAfterLimitPerMb() { return chargesAfterLimitPerMb; }
    public void setChargesAfterLimitPerMb(BigDecimal chargesAfterLimitPerMb) { this.chargesAfterLimitPerMb = chargesAfterLimitPerMb; }

    public String getPlanState() { return planState; }
    public void setPlanState(String planState) { this.planState = planState; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
