package com.billing.usagebilling.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", nullable = false, unique = true, length = 50)
    private String billNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "plan_name", nullable = false, length = 50)
    private String planName;

    @Column(name = "billing_start_date", nullable = false)
    private LocalDate billingStartDate;

    @Column(name = "billing_end_date", nullable = false)
    private LocalDate billingEndDate;

    @Column(name = "total_usage_bytes", nullable = false)
    private Long totalUsageBytes = 0L;

    @Column(name = "usage_in_gb", nullable = false)
    private Double usageInGb = 0.0;

    @Column(name = "data_allowance_gb", nullable = false)
    private Double dataAllowanceGb;

    @Column(name = "remaining_data_mb", nullable = false)
    private Double remainingDataMb = 0.0;

    @Column(name = "data_after_limit_gb", nullable = false)
    private Double dataAfterLimitGb = 0.0;

    @Column(name = "base_charge_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseChargeUsd;

    @Column(name = "excess_charge_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal excessChargeUsd = BigDecimal.ZERO;

    @Column(name = "total_amount_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmountUsd;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // "PENDING", "PAID"

    @Column(name = "payment_mode", length = 50)
    private String paymentMode;

    @Column(length = 255)
    private String remark;

    @Column(name = "generated_date", nullable = false)
    private LocalDate generatedDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Bill() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Plan getPlan() { return plan; }
    public void setPlan(Plan plan) { this.plan = plan; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
