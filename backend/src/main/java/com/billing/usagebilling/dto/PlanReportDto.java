package com.billing.usagebilling.dto;

import java.math.BigDecimal;

public class PlanReportDto {

    private Long planId;
    private String packageName;
    private double dataAllowanceGb;
    private BigDecimal monthlyChargeUsd;
    private BigDecimal chargesAfterLimitPerMb;
    private String planState;
    private long subscriberCount;
    private double totalUsageGb;
    private BigDecimal totalRevenueUsd;
    private double subscriberPercentage;

    public PlanReportDto() {}

    public PlanReportDto(Long planId, String packageName, double dataAllowanceGb, BigDecimal monthlyChargeUsd,
                         BigDecimal chargesAfterLimitPerMb, String planState, long subscriberCount,
                         double totalUsageGb, BigDecimal totalRevenueUsd, double subscriberPercentage) {
        this.planId = planId;
        this.packageName = packageName;
        this.dataAllowanceGb = dataAllowanceGb;
        this.monthlyChargeUsd = monthlyChargeUsd;
        this.chargesAfterLimitPerMb = chargesAfterLimitPerMb;
        this.planState = planState;
        this.subscriberCount = subscriberCount;
        this.totalUsageGb = totalUsageGb;
        this.totalRevenueUsd = totalRevenueUsd;
        this.subscriberPercentage = subscriberPercentage;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public double getDataAllowanceGb() {
        return dataAllowanceGb;
    }

    public void setDataAllowanceGb(double dataAllowanceGb) {
        this.dataAllowanceGb = dataAllowanceGb;
    }

    public BigDecimal getMonthlyChargeUsd() {
        return monthlyChargeUsd;
    }

    public void setMonthlyChargeUsd(BigDecimal monthlyChargeUsd) {
        this.monthlyChargeUsd = monthlyChargeUsd;
    }

    public BigDecimal getChargesAfterLimitPerMb() {
        return chargesAfterLimitPerMb;
    }

    public void setChargesAfterLimitPerMb(BigDecimal chargesAfterLimitPerMb) {
        this.chargesAfterLimitPerMb = chargesAfterLimitPerMb;
    }

    public String getPlanState() {
        return planState;
    }

    public void setPlanState(String planState) {
        this.planState = planState;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public double getTotalUsageGb() {
        return totalUsageGb;
    }

    public void setTotalUsageGb(double totalUsageGb) {
        this.totalUsageGb = totalUsageGb;
    }

    public BigDecimal getTotalRevenueUsd() {
        return totalRevenueUsd;
    }

    public void setTotalRevenueUsd(BigDecimal totalRevenueUsd) {
        this.totalRevenueUsd = totalRevenueUsd;
    }

    public double getSubscriberPercentage() {
        return subscriberPercentage;
    }

    public void setSubscriberPercentage(double subscriberPercentage) {
        this.subscriberPercentage = subscriberPercentage;
    }
}
