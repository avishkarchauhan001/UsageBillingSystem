package com.billing.usagebilling.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BillDto {
    private Long id;
    private String billNumber;
    private Long planId;
    private String planName;
    private LocalDate billingStartDate;
    private LocalDate billingEndDate;
    private Long totalUsageBytes;
    private Double usageInGb;
    private Double dataAllowanceGb;
    private Double remainingDataMb;
    private Double dataAfterLimitGb;
    private BigDecimal baseChargeUsd;
    private BigDecimal excessChargeUsd;
    private BigDecimal totalAmountUsd;
    private String status; // "PENDING", "PAID"
    private String paymentMode;
    private String remark;
    private LocalDate generatedDate;
    private LocalDate dueDate;
    private LocalDateTime paidDate;

    public BillDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }

    public LocalDate getBillingStartDate() { return billingStartDate; }
    public void setBillingStartDate(LocalDate billingStartDate) { this.billingStartDate = billingStartDate; }

    public LocalDate getBillingEndDate() { return billingEndDate; }
    public void setBillingEndDate(LocalDate billingEndDate) { this.billingEndDate = billingEndDate; }

    public Long getTotalUsageBytes() { return totalUsageBytes; }
    public void setTotalUsageBytes(Long totalUsageBytes) { this.totalUsageBytes = totalUsageBytes; }

    public Double getUsageInGb() { return usageInGb; }
    public void setUsageInGb(Double usageInGb) { this.usageInGb = usageInGb; }

    public Double getDataAllowanceGb() { return dataAllowanceGb; }
    public void setDataAllowanceGb(Double dataAllowanceGb) { this.dataAllowanceGb = dataAllowanceGb; }

    public Double getRemainingDataMb() { return remainingDataMb; }
    public void setRemainingDataMb(Double remainingDataMb) { this.remainingDataMb = remainingDataMb; }

    public Double getDataAfterLimitGb() { return dataAfterLimitGb; }
    public void setDataAfterLimitGb(Double dataAfterLimitGb) { this.dataAfterLimitGb = dataAfterLimitGb; }

    public BigDecimal getBaseChargeUsd() { return baseChargeUsd; }
    public void setBaseChargeUsd(BigDecimal baseChargeUsd) { this.baseChargeUsd = baseChargeUsd; }

    public BigDecimal getExcessChargeUsd() { return excessChargeUsd; }
    public void setExcessChargeUsd(BigDecimal excessChargeUsd) { this.excessChargeUsd = excessChargeUsd; }

    public BigDecimal getTotalAmountUsd() { return totalAmountUsd; }
    public void setTotalAmountUsd(BigDecimal totalAmountUsd) { this.totalAmountUsd = totalAmountUsd; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDate getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDateTime paidDate) { this.paidDate = paidDate; }
}
