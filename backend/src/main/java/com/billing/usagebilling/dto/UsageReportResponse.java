package com.billing.usagebilling.dto;

import java.time.LocalDate;
import java.util.List;

public class UsageReportResponse {
    private String username;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Double totalUsageGb;
    private Double totalUploadGb;
    private Double totalDownloadGb;
    private List<UsageReportItemDto> dailyUsage;

    public UsageReportResponse() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public Double getTotalUsageGb() { return totalUsageGb; }
    public void setTotalUsageGb(Double totalUsageGb) { this.totalUsageGb = totalUsageGb; }

    public Double getTotalUploadGb() { return totalUploadGb; }
    public void setTotalUploadGb(Double totalUploadGb) { this.totalUploadGb = totalUploadGb; }

    public Double getTotalDownloadGb() { return totalDownloadGb; }
    public void setTotalDownloadGb(Double totalDownloadGb) { this.totalDownloadGb = totalDownloadGb; }

    public List<UsageReportItemDto> getDailyUsage() { return dailyUsage; }
    public void setDailyUsage(List<UsageReportItemDto> dailyUsage) { this.dailyUsage = dailyUsage; }
}
