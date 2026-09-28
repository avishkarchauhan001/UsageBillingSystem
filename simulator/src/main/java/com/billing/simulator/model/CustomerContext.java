package com.billing.simulator.model;

import java.math.BigDecimal;

public class CustomerContext {

    private Long userId;
    private String username;
    private Long planId;
    private String packageName;
    private double dataAllowanceGb;
    private BigDecimal monthlyChargeUsd;
    private BigDecimal chargesAfterLimitPerMb;

    public CustomerContext(Long userId, String username, Long planId, String packageName,
                           double dataAllowanceGb, BigDecimal monthlyChargeUsd, BigDecimal chargesAfterLimitPerMb) {
        this.userId = userId;
        this.username = username;
        this.planId = planId;
        this.packageName = packageName;
        this.dataAllowanceGb = dataAllowanceGb;
        this.monthlyChargeUsd = monthlyChargeUsd;
        this.chargesAfterLimitPerMb = chargesAfterLimitPerMb;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Long getPlanId() { return planId; }
    public String getPackageName() { return packageName; }
    public double getDataAllowanceGb() { return dataAllowanceGb; }
    public BigDecimal getMonthlyChargeUsd() { return monthlyChargeUsd; }
    public BigDecimal getChargesAfterLimitPerMb() { return chargesAfterLimitPerMb; }
}
