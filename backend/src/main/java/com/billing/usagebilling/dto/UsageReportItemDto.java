package com.billing.usagebilling.dto;

import java.time.LocalDate;

public class UsageReportItemDto {
    private LocalDate date;
    private Double uploadMb;
    private Double downloadMb;
    private Double totalMb;
    private Double totalGb;

    public UsageReportItemDto() {}

    public UsageReportItemDto(LocalDate date, Double uploadMb, Double downloadMb, Double totalMb, Double totalGb) {
        this.date = date;
        this.uploadMb = uploadMb;
        this.downloadMb = downloadMb;
        this.totalMb = totalMb;
        this.totalGb = totalGb;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getUploadMb() { return uploadMb; }
    public void setUploadMb(Double uploadMb) { this.uploadMb = uploadMb; }

    public Double getDownloadMb() { return downloadMb; }
    public void setDownloadMb(Double downloadMb) { this.downloadMb = downloadMb; }

    public Double getTotalMb() { return totalMb; }
    public void setTotalMb(Double totalMb) { this.totalMb = totalMb; }

    public Double getTotalGb() { return totalGb; }
    public void setTotalGb(Double totalGb) { this.totalGb = totalGb; }
}
