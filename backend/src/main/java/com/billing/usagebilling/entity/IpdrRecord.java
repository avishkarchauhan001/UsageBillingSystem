package com.billing.usagebilling.entity;

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
@Table(name = "ipdr_records")
public class IpdrRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_identifier", nullable = false, length = 100)
    private String serviceIdentifier; // Maps to customer username / account

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "mac_address", nullable = false, length = 20)
    private String macAddress;

    @Column(name = "input_octets", nullable = false)
    private Long inputOctets = 0L; // Bytes uploaded

    @Column(name = "output_octets", nullable = false)
    private Long outputOctets = 0L; // Bytes downloaded

    @Column(name = "service_direction", nullable = false)
    private Integer serviceDirection = 2; // 1: Upload, 2: Download

    @Column(name = "hostname", nullable = false, length = 100)
    private String hostname;

    @Column(name = "session_start", nullable = false)
    private LocalDateTime sessionStart;

    @Column(name = "session_end", nullable = false)
    private LocalDateTime sessionEnd;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IpdrRecord() {}

    public IpdrRecord(String serviceIdentifier, User user, String ipAddress, String macAddress,
                      Long inputOctets, Long outputOctets, Integer serviceDirection,
                      String hostname, LocalDateTime sessionStart, LocalDateTime sessionEnd) {
        this.serviceIdentifier = serviceIdentifier;
        this.user = user;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.inputOctets = inputOctets;
        this.outputOctets = outputOctets;
        this.serviceDirection = serviceDirection;
        this.hostname = hostname;
        this.sessionStart = sessionStart;
        this.sessionEnd = sessionEnd;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceIdentifier() { return serviceIdentifier; }
    public void setServiceIdentifier(String serviceIdentifier) { this.serviceIdentifier = serviceIdentifier; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public LocalDateTime getSessionStart() { return sessionStart; }
    public void setSessionStart(LocalDateTime sessionStart) { this.sessionStart = sessionStart; }

    public LocalDateTime getSessionEnd() { return sessionEnd; }
    public void setSessionEnd(LocalDateTime sessionEnd) { this.sessionEnd = sessionEnd; }

    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
