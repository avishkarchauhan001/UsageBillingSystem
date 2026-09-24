package com.billing.usagebilling.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CustomerHomeDto {
    private String username;
    private Long planId;
    private String packageName;
    private LocalDate billingStartDate;
    private LocalDate billingEndDate;
    private Double usageInGb;
    private Double remainingDataMb;
    private Double dataAllowanceGb;
    private Double dataAfterLimitGb;
    private BigDecimal monthlyChargeUsd;
    private BigDecimal excessChargeUsd;
    private BigDecimal totalAmountUsd;
    private long pendingBillsCount;
    private Long currentBillId;
    private String currentBillNumber;

    // Scheduled plan change info (SRS US19)
    private boolean hasScheduledPlan;
    private String scheduledPackageName;
    private LocalDate scheduledActivationDate;

    public CustomerHomeDto() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public LocalDate getBillingStartDate() { return billingStartDate; }
    public void setBillingStartDate(LocalDate billingStartDate) { this.billingStartDate = billingStartDate; }

    public LocalDate getBillingEndDate() { return billingEndDate; }
    public void setBillingEndDate(LocalDate billingEndDate) { this.billingEndDate = billingEndDate; }

    public Double getUsageInGb() { return usageInGb; }
    public void setUsageInGb(Double usageInGb) { this.usageInGb = usageInGb; }

    public Double getRemainingDataMb() { return remainingDataMb; }
    public void setRemainingDataMb(Double remainingDataMb) { this.remainingDataMb = remainingDataMb; }

    public Double getDataAllowanceGb() { return dataAllowanceGb; }
    public void setDataAllowanceGb(Double dataAllowanceGb) { this.dataAllowanceGb = dataAllowanceGb; }

    public Double getDataAfterLimitGb() { return dataAfterLimitGb; }
    public void setDataAfterLimitGb(Double dataAfterLimitGb) { this.dataAfterLimitGb = dataAfterLimitGb; }

    public BigDecimal getMonthlyChargeUsd() { return monthlyChargeUsd; }
    public void setMonthlyChargeUsd(BigDecimal monthlyChargeUsd) { this.monthlyChargeUsd = monthlyChargeUsd; }

    public BigDecimal getExcessChargeUsd() { return excessChargeUsd; }
    public void setExcessChargeUsd(BigDecimal excessChargeUsd) { this.excessChargeUsd = excessChargeUsd; }

    public BigDecimal getTotalAmountUsd() { return totalAmountUsd; }
    public void setTotalAmountUsd(BigDecimal totalAmountUsd) { this.totalAmountUsd = totalAmountUsd; }

    public long getPendingBillsCount() { return pendingBillsCount; }
    public void setPendingBillsCount(long pendingBillsCount) { this.pendingBillsCount = pendingBillsCount; }

    public Long getCurrentBillId() { return currentBillId; }
    public void setCurrentBillId(Long currentBillId) { this.currentBillId = currentBillId; }

    public String getCurrentBillNumber() { return currentBillNumber; }
    public void setCurrentBillNumber(String currentBillNumber) { this.currentBillNumber = currentBillNumber; }

    public boolean isHasScheduledPlan() { return hasScheduledPlan; }
    public void setHasScheduledPlan(boolean hasScheduledPlan) { this.hasScheduledPlan = hasScheduledPlan; }

    public String getScheduledPackageName() { return scheduledPackageName; }
    public void setScheduledPackageName(String scheduledPackageName) { this.scheduledPackageName = scheduledPackageName; }

    public LocalDate getScheduledActivationDate() { return scheduledActivationDate; }
    public void setScheduledActivationDate(LocalDate scheduledActivationDate) { this.scheduledActivationDate = scheduledActivationDate; }
}
