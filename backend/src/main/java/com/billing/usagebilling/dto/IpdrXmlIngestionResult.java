package com.billing.usagebilling.dto;

import java.util.ArrayList;
import java.util.List;

public class IpdrXmlIngestionResult {

    private int totalFilesProcessed;
    private int totalRecordsParsed;
    private int acceptedRecords;
    private int rejectedRecords;
    private List<String> details = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public IpdrXmlIngestionResult() {}

    public int getTotalFilesProcessed() {
        return totalFilesProcessed;
    }

    public void setTotalFilesProcessed(int totalFilesProcessed) {
        this.totalFilesProcessed = totalFilesProcessed;
    }

    public int getTotalRecordsParsed() {
        return totalRecordsParsed;
    }

    public void setTotalRecordsParsed(int totalRecordsParsed) {
        this.totalRecordsParsed = totalRecordsParsed;
    }

    public int getAcceptedRecords() {
        return acceptedRecords;
    }

    public void setAcceptedRecords(int acceptedRecords) {
        this.acceptedRecords = acceptedRecords;
    }

    public int getRejectedRecords() {
        return rejectedRecords;
    }

    public void setRejectedRecords(int rejectedRecords) {
        this.rejectedRecords = rejectedRecords;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addDetail(String detail) {
        this.details.add(detail);
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
