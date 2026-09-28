package com.billing.simulator.generator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import com.billing.simulator.config.SimulatorConfig;
import com.billing.simulator.model.CustomerContext;
import com.billing.simulator.model.GeneratedIpdrSession;
import com.billing.simulator.model.RecognizedDeviceContext;

public class IpdrTrafficGenerator {

    private final SimulatorConfig config;
    private final Random random = new Random();

    public IpdrTrafficGenerator(SimulatorConfig config) {
        this.config = config;
    }

    public GeneratedIpdrSession generateSession(List<CustomerContext> customers, List<RecognizedDeviceContext> devices) {
        if (customers.isEmpty()) {
            throw new IllegalStateException("No active customers available in the database.");
        }
        if (devices.isEmpty()) {
            throw new IllegalStateException("No active recognized devices available in the database.");
        }

        // Randomly select customer
        CustomerContext customer = customers.get(random.nextInt(customers.size()));

        // Randomly select recognized network device
        RecognizedDeviceContext device = devices.get(random.nextInt(devices.size()));

        // Client IP and MAC
        String clientIp = "192.168.1." + (100 + (customer.getUserId() != null ? (customer.getUserId().intValue() % 150) : 1));
        String clientMac = device.getMacAddress();

        // Service direction: 1 = Upload, 2 = Download (typical broadband ratio: 75% download)
        int direction = (random.nextInt(100) < 75) ? 2 : 1;

        // Generate usage scaled to plan data allowance
        long minMb = config.getMinSessionMb();
        long maxMb = config.getMaxSessionMb();
        if (customer.getDataAllowanceGb() > 5.0) {
            maxMb = (long) (maxMb * 1.5);
        }

        long sessionMb = minMb + random.nextInt((int) Math.max(1, (maxMb - minMb)));
        long octetsPassed = sessionMb * 1024L * 1024L;

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMinutes(15 + random.nextInt(120));

        return new GeneratedIpdrSession(
                customer.getUsername(),
                device.getHostname(),
                device.getIpAddress(),
                clientIp,
                clientMac,
                direction,
                octetsPassed,
                start,
                end,
                customer.getPackageName(),
                customer.getDataAllowanceGb()
        );
    }
}
