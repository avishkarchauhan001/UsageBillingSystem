package com.billing.usagebilling.dto;

public class IpdrSimulateRequest {
    private String serviceIdentifier; // Customer username
    private String ipAddress;
    private String macAddress;
    private Long inputOctets; // Bytes uploaded
    private Long outputOctets; // Bytes downloaded
    private Integer serviceDirection; // 1: Upload, 2: Download
    private String hostname;

    public IpdrSimulateRequest() {}

    public String getServiceIdentifier() { return serviceIdentifier; }
    public void setServiceIdentifier(String serviceIdentifier) { this.serviceIdentifier = serviceIdentifier; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public Long getInputOctets() { return inputOctets; }
    public void setInputOctets(Long inputOctets) { this.inputOctets = inputOctets; }

    public Long getOutputOctets() { return outputOctets; }
    public void setOutputOctets(Long outputOctets) { this.outputOctets = outputOctets; }

    public Integer getServiceDirection() { return serviceDirection; }
    public void setServiceDirection(Integer serviceDirection) { this.serviceDirection = serviceDirection; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
}
