package com.billing.usagebilling.dto;

public class PaymentRequest {
    private Long billId;
    private String paymentMode; // PayTM, Net banking, UPI, Credit Card, Debit Card, Online

    public PaymentRequest() {}

    public PaymentRequest(Long billId, String paymentMode) {
        this.billId = billId;
        this.paymentMode = paymentMode;
    }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
}
