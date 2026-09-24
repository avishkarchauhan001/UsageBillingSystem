-- =====================================================================
-- USAGE MEDIATION BILLING SYSTEM - COMPLETE DATABASE SCHEMA (MySQL 8)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS billing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE billing;

-- 1. USERS TABLE
-- Stores credentials, role, security question/answer, and user lifecycle state.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'OPERATOR', 'CUSTOMER'
    user_state VARCHAR(20) NOT NULL DEFAULT 'Activated', -- 'Activated', 'Deactivated'
    security_question VARCHAR(255),
    security_answer VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username),
    INDEX idx_users_role (role),
    INDEX idx_users_state (user_state)
) ENGINE=InnoDB;

-- 2. PLANS TABLE
-- Package-based broadband plans managed by Operators.
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_name VARCHAR(50) NOT NULL UNIQUE,
    data_allowance_gb DOUBLE NOT NULL,
    monthly_charge_usd DECIMAL(10, 2) NOT NULL,
    charges_after_limit_per_mb DECIMAL(10, 4) NOT NULL,
    plan_state VARCHAR(20) NOT NULL DEFAULT 'Activated', -- 'Activated', 'Deactivated'
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plans_name (package_name),
    INDEX idx_plans_state (plan_state)
) ENGINE=InnoDB;

-- 3. CUSTOMER PLANS TABLE (Subscriptions & Future Scheduled Changes)
-- Supports current active plan and scheduled plan changes activating upon current plan expiry.
CREATE TABLE IF NOT EXISTS customer_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'SCHEDULED', 'EXPIRED'
    start_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_plans_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE RESTRICT,
    INDEX idx_customer_plans_user_status (user_id, status)
) ENGINE=InnoDB;

-- 4. BILLS TABLE
-- Billing and transaction statements generated from mediated IPDR data and rating rules.
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(50) NOT NULL UNIQUE, -- e.g. T183, T534, INV-2026-001
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    plan_name VARCHAR(50) NOT NULL,
    billing_start_date DATE NOT NULL,
    billing_end_date DATE NOT NULL,
    total_usage_bytes BIGINT NOT NULL DEFAULT 0,
    usage_in_gb DOUBLE NOT NULL DEFAULT 0.0,
    data_allowance_gb DOUBLE NOT NULL,
    remaining_data_mb DOUBLE NOT NULL DEFAULT 0.0,
    data_after_limit_gb DOUBLE NOT NULL DEFAULT 0.0,
    base_charge_usd DECIMAL(10, 2) NOT NULL,
    excess_charge_usd DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount_usd DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PAID'
    payment_mode VARCHAR(50) NULL, -- 'PayTM', 'Net banking', 'UPI', 'Credit Card', 'Debit Card', 'Online'
    remark VARCHAR(255) NULL,
    generated_date DATE NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bills_plan FOREIGN KEY (plan_id) REFERENCES plans(id) ON DELETE RESTRICT,
    INDEX idx_bills_user_status (user_id, status),
    INDEX idx_bills_number (bill_number)
) ENGINE=InnoDB;

-- 5. PAYMENTS TABLE
-- Transaction payment history records linked to bills and customers.
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount_paid DECIMAL(10, 2) NOT NULL,
    payment_mode VARCHAR(50) NOT NULL, -- 'PayTM', 'Net banking', 'UPI', 'Credit Card', 'Debit Card', 'Online'
    transaction_reference VARCHAR(100) NOT NULL UNIQUE,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS', -- 'SUCCESS', 'FAILED'
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_payments_user (user_id),
    INDEX idx_payments_bill (bill_id)
) ENGINE=InnoDB;

-- 6. IPDR RECORDS TABLE
-- Raw and mediated Internet Protocol Detail Records for customer sessions.
CREATE TABLE IF NOT EXISTS ipdr_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_identifier VARCHAR(100) NOT NULL, -- Maps to Customer username
    user_id BIGINT NULL,
    ip_address VARCHAR(45) NOT NULL,
    mac_address VARCHAR(20) NOT NULL,
    input_octets BIGINT NOT NULL DEFAULT 0, -- Bytes uploaded
    output_octets BIGINT NOT NULL DEFAULT 0, -- Bytes downloaded
    service_direction INT NOT NULL, -- 1: Upload, 2: Download
    hostname VARCHAR(100) NOT NULL,
    session_start DATETIME NOT NULL,
    session_end DATETIME NOT NULL,
    bill_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ipdr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_ipdr_bill FOREIGN KEY (bill_id) REFERENCES bills(id) ON DELETE SET NULL,
    INDEX idx_ipdr_service_id (service_identifier),
    INDEX idx_ipdr_user (user_id),
    INDEX idx_ipdr_session (session_start, session_end)
) ENGINE=InnoDB;
