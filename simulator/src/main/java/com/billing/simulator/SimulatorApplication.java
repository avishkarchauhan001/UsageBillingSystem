package com.billing.simulator;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.billing.simulator.config.SimulatorConfig;
import com.billing.simulator.db.SimulatorDatabaseService;
import com.billing.simulator.generator.IpdrTrafficGenerator;
import com.billing.simulator.model.CustomerContext;
import com.billing.simulator.model.GeneratedIpdrSession;
import com.billing.simulator.model.RecognizedDeviceContext;
import com.billing.simulator.xml.IpdrXmlGenerator;

/**
 * Standalone Java IPDR Simulator (SRS US16).
 * Simulates network traffic sessions for registered customers and outputs canonical DOCSIS IPDR XML.
 */
public class SimulatorApplication {

    private static final AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("               USAGE MEDIATION BILLING SYSTEM - IPDR SIMULATOR                  ");
        System.out.println("               (Standalone Java Traffic & XML Generation Engine)                ");
        System.out.println("================================================================================");

        SimulatorConfig config = SimulatorConfig.load();

        boolean runOnce = false;
        int maxCount = -1;

        // Parse CLI arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--once".equalsIgnoreCase(arg) || "-1".equalsIgnoreCase(arg)) {
                runOnce = true;
                maxCount = 1;
            } else if ("--count".equalsIgnoreCase(arg) && i + 1 < args.length) {
                maxCount = Integer.parseInt(args[++i]);
            } else if ("--interval".equalsIgnoreCase(arg) && i + 1 < args.length) {
                config.setIntervalSeconds(Integer.parseInt(args[++i]));
            } else if ("--out".equalsIgnoreCase(arg) && i + 1 < args.length) {
                config.setOutputDirectory(args[++i]);
            }
        }

        System.out.println("[CONFIG] Database URL     : " + config.getDbUrl());
        System.out.println("[CONFIG] Database User    : " + config.getDbUsername());
        System.out.println("[CONFIG] Output Directory : " + config.getOutputDirectory());
        System.out.println("[CONFIG] Cycle Interval   : " + config.getIntervalSeconds() + " seconds");
        System.out.println("[CONFIG] Execution Mode   : " + (runOnce ? "Single Run (--once)" : (maxCount > 0 ? maxCount + " cycles" : "Continuous Loop")));
        System.out.println("--------------------------------------------------------------------------------");

        SimulatorDatabaseService dbService = new SimulatorDatabaseService(config);
        IpdrTrafficGenerator generator = new IpdrTrafficGenerator(config);
        IpdrXmlGenerator xmlGenerator = new IpdrXmlGenerator(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SHUTDOWN] Terminating IPDR Simulator gracefully...");
            running.set(false);
        }));

        int cycleCount = 0;

        while (running.get()) {
            cycleCount++;
            try {
                // 1. Fetch eligible customers from database
                List<CustomerContext> customers = dbService.fetchEligibleCustomers();
                if (customers.isEmpty()) {
                    System.err.println("[WARN] No eligible customers with ACTIVE plans found in database. Retrying in " + config.getIntervalSeconds() + "s...");
                    Thread.sleep(config.getIntervalSeconds() * 1000L);
                    continue;
                }

                // 2. Fetch recognized devices from database
                List<RecognizedDeviceContext> devices = dbService.fetchActiveRecognizedDevices();
                if (devices.isEmpty()) {
                    System.err.println("[WARN] No ACTIVE recognized devices found in database. Retrying in " + config.getIntervalSeconds() + "s...");
                    Thread.sleep(config.getIntervalSeconds() * 1000L);
                    continue;
                }

                // 3. Generate random usage session for a random customer
                GeneratedIpdrSession session = generator.generateSession(customers, devices);

                // 4. Generate XML file and write to output directory
                File xmlFile = xmlGenerator.writeIpdrXml(session);

                // 5. Display session details
                double sessionMb = session.getOctetsPassed() / (1024.0 * 1024.0);
                String dirName = (session.getDirection() == 1) ? "Upload" : "Download";

                System.out.println();
                System.out.println("================================================================================");
                System.out.printf(" [IPDR SIMULATOR] Generated Traffic Session #%d%n", cycleCount);
                System.out.println("--------------------------------------------------------------------------------");
                System.out.printf(" Selected Customer     : %s%n", session.getServiceIdentifier());
                System.out.printf(" Service Identifier    : %s%n", session.getServiceIdentifier());
                System.out.printf(" Subscribed Plan       : %s (Data Allowance: %.1f GB)%n", session.getPlanName(), session.getPlanAllowanceGb());
                System.out.printf(" Service Direction     : %s (%d)%n", dirName, session.getDirection());
                System.out.printf(" Bytes Passed          : %,d octets (%.2f MB)%n", session.getOctetsPassed(), sessionMb);
                System.out.printf(" Recognized Hostname   : %s%n", session.getHostname());
                System.out.printf(" CMTS / Gateway IP     : %s%n", session.getCmtsIp());
                System.out.printf(" Client IP Address     : %s%n", session.getClientIp());
                System.out.printf(" Client MAC Address    : %s%n", session.getCmMac());
                System.out.printf(" Session Window        : %s to %s%n", session.getSessionStart(), session.getSessionEnd());
                System.out.printf(" Generated XML File    : %s%n", xmlFile.getAbsolutePath());
                System.out.println(" Status                : Written to disk successfully");
                System.out.println("================================================================================");

                if (runOnce || (maxCount > 0 && cycleCount >= maxCount)) {
                    System.out.println("[INFO] Completed requested cycles (" + cycleCount + "). Exiting.");
                    break;
                }

                System.out.println("[SIMULATOR] Waiting " + config.getIntervalSeconds() + " seconds until next simulation cycle...");
                Thread.sleep(config.getIntervalSeconds() * 1000L);

            } catch (InterruptedException ie) {
                System.out.println("[INFO] Simulator interrupted.");
                break;
            } catch (Exception ex) {
                System.err.println("[ERROR] Simulation cycle failed: " + ex.getMessage());
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
