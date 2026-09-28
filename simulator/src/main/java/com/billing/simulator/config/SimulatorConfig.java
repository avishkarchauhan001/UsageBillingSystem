package com.billing.simulator.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class SimulatorConfig {

    private String dbUrl = "jdbc:mysql://localhost:3306/billing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private String dbUsername = "root";
    private String dbPassword = "Sooo*648734";
    private int intervalSeconds = 15;
    private String outputDirectory = "c:/NetworkCapstoneProject/simulator/output/ipdr";
    private int minSessionMb = 50;
    private int maxSessionMb = 450;

    public static SimulatorConfig load() {
        SimulatorConfig config = new SimulatorConfig();
        Properties props = new Properties();

        // 1. Try local file simulator.properties
        File file = new File("simulator.properties");
        if (!file.exists()) {
            file = new File("simulator/simulator.properties");
        }

        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (Exception e) {
                System.err.println("[WARN] Could not load simulator.properties: " + e.getMessage());
            }
        }

        if (props.containsKey("db.url")) config.dbUrl = props.getProperty("db.url");
        if (props.containsKey("db.username")) config.dbUsername = props.getProperty("db.username");
        if (props.containsKey("db.password")) config.dbPassword = props.getProperty("db.password");
        if (props.containsKey("simulator.interval.seconds")) {
            config.intervalSeconds = Integer.parseInt(props.getProperty("simulator.interval.seconds"));
        }
        if (props.containsKey("simulator.output.directory")) {
            config.outputDirectory = props.getProperty("simulator.output.directory");
        }
        if (props.containsKey("simulator.min.session.mb")) {
            config.minSessionMb = Integer.parseInt(props.getProperty("simulator.min.session.mb"));
        }
        if (props.containsKey("simulator.max.session.mb")) {
            config.maxSessionMb = Integer.parseInt(props.getProperty("simulator.max.session.mb"));
        }

        return config;
    }

    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }
    public int getIntervalSeconds() { return intervalSeconds; }
    public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
    public String getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
    public int getMinSessionMb() { return minSessionMb; }
    public int getMaxSessionMb() { return maxSessionMb; }
}
