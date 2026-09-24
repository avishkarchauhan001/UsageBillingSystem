package com.billing.usagebilling.dto;

import java.math.BigDecimal;

public class PlanDto {
    private Long id;
    private String packageName;
    private Double dataAllowanceGb;
    private BigDecimal monthlyChargeUsd;
    private BigDecimal chargesAfterLimitPerMb;
    private String planState; // "Activated", "Deactivated"

    public PlanDto() {}

    public PlanDto(Long id, String packageName, Double dataAllowanceGb, BigDecimal monthlyChargeUsd, BigDecimal chargesAfterLimitPerMb, String planState) {
        this.id = id;
        this.packageName = packageName;
        this.dataAllowanceGb = dataAllowanceGb;
        this.monthlyChargeUsd = monthlyChargeUsd;
        this.chargesAfterLimitPerMb = chargesAfterLimitPerMb;
        this.planState = planState;
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
}
