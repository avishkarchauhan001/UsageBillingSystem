package com.billing.simulator.model;

public class RecognizedDeviceContext {

    private Long id;
    private String deviceName;
    private String ipAddress;
    private String macAddress;
    private String hostname;

    public RecognizedDeviceContext(Long id, String deviceName, String ipAddress, String macAddress, String hostname) {
        this.id = id;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.hostname = hostname;
    }

    public Long getId() { return id; }
    public String getDeviceName() { return deviceName; }
    public String getIpAddress() { return ipAddress; }
    public String getMacAddress() { return macAddress; }
    public String getHostname() { return hostname; }
}
