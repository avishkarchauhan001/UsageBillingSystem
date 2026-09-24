-- =====================================================================
-- USAGE MEDIATION BILLING SYSTEM - SEED DATA (MySQL 8)
-- =====================================================================

USE billing;

-- 1. USERS SEED DATA
-- Default Admin, Operator, and Customer accounts
-- Passwords:
-- admin: 1
-- operator: Operator@123
-- customer1: Customer@123
-- customer2: Customer@123
INSERT INTO users (id, username, password, role, user_state, security_question, security_answer, created_at)
VALUES 
(1, 'admin', '1', 'ADMIN', 'Activated', 'What is your pet name?', 'admin', NOW()),
(2, 'operator', 'Operator@123', 'OPERATOR', 'Activated', 'What is your birthplace?', 'New York', NOW()),
(3, 'customer1', 'Customer@123', 'CUSTOMER', 'Activated', 'What is your pet name?', 'Fluffy', NOW()),
(4, 'customer2', 'Customer@123', 'CUSTOMER', 'Activated', 'What is your birthplace?', 'London', NOW())
ON DUPLICATE KEY UPDATE 
    role = VALUES(role),
    user_state = VALUES(user_state);

-- 2. PLANS SEED DATA
-- Plans directly from SRS Specification (MomNDad, SocialTeen, VideoMate, TorrentGuy)
INSERT INTO plans (id, package_name, data_allowance_gb, monthly_charge_usd, charges_after_limit_per_mb, plan_state, created_at)
VALUES
(1, 'MomNDad', 2.0, 10.00, 0.0100, 'Activated', NOW()),
(2, 'SocialTeen', 5.0, 15.00, 0.0080, 'Activated', NOW()),
(3, 'VideoMate', 10.0, 20.00, 0.0050, 'Activated', NOW()),
(4, 'TorrentGuy', 20.0, 30.00, 0.0020, 'Activated', NOW())
ON DUPLICATE KEY UPDATE 
    data_allowance_gb = VALUES(data_allowance_gb),
    monthly_charge_usd = VALUES(monthly_charge_usd),
    charges_after_limit_per_mb = VALUES(charges_after_limit_per_mb),
    plan_state = VALUES(plan_state);

-- 3. CUSTOMER PLANS SEED DATA
-- Customer1 currently has MomNDad active (billing cycle: Sep 1 to Sep 30, 2026)
INSERT INTO customer_plans (id, user_id, plan_id, status, start_date, expiry_date, created_at)
VALUES
(1, 3, 1, 'ACTIVE', '2026-09-01', '2026-10-01', NOW()),
(2, 4, 2, 'ACTIVE', '2026-09-01', '2026-10-01', NOW())
ON DUPLICATE KEY UPDATE 
    status = VALUES(status),
    start_date = VALUES(start_date),
    expiry_date = VALUES(expiry_date);

-- 4. BILLS SEED DATA
-- Past paid bills and current pending bill for customer1
INSERT INTO bills (id, bill_number, user_id, plan_id, plan_name, billing_start_date, billing_end_date, total_usage_bytes, usage_in_gb, data_allowance_gb, remaining_data_mb, data_after_limit_gb, base_charge_usd, excess_charge_usd, total_amount_usd, status, payment_mode, remark, generated_date, due_date, paid_date, created_at)
VALUES
(1, 'T341', 3, 1, 'MomNDad', '2026-07-01', '2026-08-01', 3758096384, 3.500, 2.0, 0.0, 1.500, 10.00, 15.36, 25.36, 'PAID', 'PayTM', 'Monthly settlement July', '2026-08-01', '2026-08-15', '2026-08-05 14:20:00', NOW()),
(2, 'T27', 3, 1, 'MomNDad', '2026-08-01', '2026-09-01', 2899102924, 2.700, 2.0, 0.0, 0.700, 10.00, 7.17, 17.17, 'PAID', 'PayTM', 'Monthly settlement August', '2026-09-01', '2026-09-15', '2026-09-03 10:15:00', NOW()),
(3, 'T183', 3, 1, 'MomNDad', '2026-09-01', '2026-10-01', 2453488230, 2.285, 2.0, 0.0, 0.285, 10.00, 2.92, 12.92, 'PENDING', NULL, 'Pending payment for September', '2026-09-24', '2026-10-05', NULL, NOW())
ON DUPLICATE KEY UPDATE 
    status = VALUES(status),
    total_amount_usd = VALUES(total_amount_usd);

-- 5. PAYMENTS SEED DATA
INSERT INTO payments (id, bill_id, user_id, amount_paid, payment_mode, transaction_reference, payment_status, payment_date, created_at)
VALUES
(1, 1, 3, 25.36, 'PayTM', 'TXN-20260805-001', 'SUCCESS', '2026-08-05 14:20:00', NOW()),
(2, 2, 3, 17.17, 'PayTM', 'TXN-20260903-002', 'SUCCESS', '2026-09-03 10:15:00', NOW())
ON DUPLICATE KEY UPDATE 
    payment_status = VALUES(payment_status);

-- 6. IPDR RECORDS SEED DATA
-- Sample IPDR sessions for customer1
INSERT INTO ipdr_records (id, service_identifier, user_id, ip_address, mac_address, input_octets, output_octets, service_direction, hostname, session_start, session_end, bill_id, created_at)
VALUES
(1, 'customer1', 3, '192.168.1.101', '00:1A:2B:3C:4D:5E', 450000000, 850000000, 2, 'isp-gw01.net', '2026-09-10 08:30:00', '2026-09-10 11:45:00', 3, NOW()),
(2, 'customer1', 3, '192.168.1.101', '00:1A:2B:3C:4D:5E', 320000000, 520000000, 2, 'isp-gw01.net', '2026-09-15 14:00:00', '2026-09-15 17:30:00', 3, NOW()),
(3, 'customer1', 3, '192.168.1.101', '00:1A:2B:3C:4D:5E', 113488230, 200000000, 1, 'isp-gw01.net', '2026-09-20 19:15:00', '2026-09-20 22:00:00', 3, NOW())
ON DUPLICATE KEY UPDATE 
    input_octets = VALUES(input_octets),
    output_octets = VALUES(output_octets);
