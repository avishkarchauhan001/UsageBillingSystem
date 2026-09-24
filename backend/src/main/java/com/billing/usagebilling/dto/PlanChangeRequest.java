package com.billing.usagebilling.dto;

import java.time.LocalDate;

public class PlanChangeRequest {
    private Long newPlanId;

    public PlanChangeRequest() {}

    public PlanChangeRequest(Long newPlanId) {
        this.newPlanId = newPlanId;
    }

    public Long getNewPlanId() { return newPlanId; }
    public void setNewPlanId(Long newPlanId) { this.newPlanId = newPlanId; }
}
