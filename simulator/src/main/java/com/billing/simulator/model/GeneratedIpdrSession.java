package com.billing.simulator.model;

import java.time.LocalDateTime;

public class GeneratedIpdrSession {

    private String serviceIdentifier;
    private String hostname;
    private String cmtsIp;
    private String clientIp;
    private String cmMac;
    private int direction; // 1: Upload, 2: Download
    private long octetsPassed;
    private LocalDateTime sessionStart;
    private LocalDateTime sessionEnd;
    private String planName;
    private double planAllowanceGb;

    public GeneratedIpdrSession(String serviceIdentifier, String hostname, String cmtsIp, String clientIp,
                                String cmMac, int direction, long octetsPassed, LocalDateTime sessionStart,
                                LocalDateTime sessionEnd, String planName, double planAllowanceGb) {
        this.serviceIdentifier = serviceIdentifier;
        this.hostname = hostname;
        this.cmtsIp = cmtsIp;
        this.clientIp = clientIp;
        this.cmMac = cmMac;
        this.direction = direction;
        this.octetsPassed = octetsPassed;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
        this.planName = planName;
        this.planAllowanceGb = planAllowanceGb;
    }

    public String getServiceIdentifier() { return serviceIdentifier; }
    public String getHostname() { return hostname; }
    public String getCmtsIp() { return cmtsIp; }
    public String getClientIp() { return clientIp; }
    public String getCmMac() { return cmMac; }
    public int getDirection() { return direction; }
    public long getOctetsPassed() { return octetsPassed; }
    public LocalDateTime getSessionStart() { return sessionStart; }
    public LocalDateTime getSessionEnd() { return sessionEnd; }
    public String getPlanName() { return planName; }
    public double getPlanAllowanceGb() { return planAllowanceGb; }
}
