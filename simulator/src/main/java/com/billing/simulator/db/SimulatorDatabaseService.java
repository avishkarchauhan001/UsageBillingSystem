package com.billing.simulator.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.billing.simulator.config.SimulatorConfig;
import com.billing.simulator.model.CustomerContext;
import com.billing.simulator.model.RecognizedDeviceContext;

public class SimulatorDatabaseService {

    private final SimulatorConfig config;

    public SimulatorDatabaseService(SimulatorConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getDbUrl(), config.getDbUsername(), config.getDbPassword());
    }

    public List<CustomerContext> fetchEligibleCustomers() throws SQLException {
        List<CustomerContext> list = new ArrayList<>();
        String sql = """
            SELECT u.id AS user_id, u.username, p.id AS plan_id, p.package_name,
                   p.data_allowance_gb, p.monthly_charge_usd, p.charges_after_limit_per_mb
            FROM users u
            JOIN customer_plans cp ON cp.user_id = u.id AND cp.status = 'ACTIVE'
            JOIN plans p ON p.id = cp.plan_id
            WHERE u.role = 'CUSTOMER' AND u.user_state = 'Activated'
            ORDER BY u.id
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new CustomerContext(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getLong("plan_id"),
                        rs.getString("package_name"),
                        rs.getDouble("data_allowance_gb"),
                        rs.getBigDecimal("monthly_charge_usd"),
                        rs.getBigDecimal("charges_after_limit_per_mb")
                ));
            }
        }
        return list;
    }

    public List<RecognizedDeviceContext> fetchActiveRecognizedDevices() throws SQLException {
        List<RecognizedDeviceContext> list = new ArrayList<>();
        String sql = "SELECT id, device_name, ip_address, mac_address, hostname FROM recognized_devices WHERE status = 'ACTIVE'";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(new RecognizedDeviceContext(
                        rs.getLong("id"),
                        rs.getString("device_name"),
                        rs.getString("ip_address"),
                        rs.getString("mac_address"),
                        rs.getString("hostname")
                ));
            }
        }
        return list;
    }
}
