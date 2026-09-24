package com.billing.usagebilling.dto;

import java.time.LocalDate;

public class PlanChangeResponse {
    private String currentPlanName;
    private LocalDate currentPlanExpiryDate;
    private String newPlanName;
    private LocalDate scheduledActivationDate;
    private String status;
    private String message;

    public PlanChangeResponse() {}

    public PlanChangeResponse(String currentPlanName, LocalDate currentPlanExpiryDate, String newPlanName, LocalDate scheduledActivationDate, String status, String message) {
        this.currentPlanName = currentPlanName;
        this.currentPlanExpiryDate = currentPlanExpiryDate;
        this.newPlanName = newPlanName;
        this.scheduledActivationDate = scheduledActivationDate;
        this.status = status;
        this.message = message;
    }

    public String getCurrentPlanName() { return currentPlanName; }
    public void setCurrentPlanName(String currentPlanName) { this.currentPlanName = currentPlanName; }

    public LocalDate getCurrentPlanExpiryDate() { return currentPlanExpiryDate; }
    public void setCurrentPlanExpiryDate(LocalDate currentPlanExpiryDate) { this.currentPlanExpiryDate = currentPlanExpiryDate; }

    public String getNewPlanName() { return newPlanName; }
    public void setNewPlanName(String newPlanName) { this.newPlanName = newPlanName; }

    public LocalDate getScheduledActivationDate() { return scheduledActivationDate; }
    public void setScheduledActivationDate(LocalDate scheduledActivationDate) { this.scheduledActivationDate = scheduledActivationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
