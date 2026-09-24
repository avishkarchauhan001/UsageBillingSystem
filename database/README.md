# Usage Mediation Billing System - Database Documentation

## 1. Database Overview
- **Database Engine**: MySQL 8.0+
- **Database Name**: `billing`
- **Character Set**: `utf8mb4`
- **Collation**: `utf8mb4_unicode_ci`

## 2. Table Definitions & Relationships

### `users`
Stores system users across Admin, Operator, and Customer roles.
- `id` (BIGINT, PK, AUTO_INCREMENT): Unique user identifier.
- `username` (VARCHAR(50), UNIQUE, NOT NULL): Login username.
- `password` (VARCHAR(255), NOT NULL): Encrypted password or legacy hash.
- `role` (VARCHAR(20), NOT NULL): Role (`ADMIN`, `OPERATOR`, `CUSTOMER`).
- `user_state` (VARCHAR(20), NOT NULL, DEFAULT 'Activated'): User status (`Activated`, `Deactivated`). Supports soft delete as required by SRS US06.
- `security_question` (VARCHAR(255)): Used for self-service forgot password (SRS US21).
- `security_answer` (VARCHAR(255)): Stored answer for security verification.
- `created_at` (DATETIME): Timestamp when user registered.

### `plans`
Contains data-driven broadband plans defined and maintained by Operators.
- `id` (BIGINT, PK, AUTO_INCREMENT): Plan identifier.
- `package_name` (VARCHAR(50), UNIQUE, NOT NULL): Name of the plan package (e.g., MomNDad, SocialTeen, VideoMate, TorrentGuy).
- `data_allowance_gb` (DOUBLE, NOT NULL): Quota included in the monthly plan in GB.
- `monthly_charge_usd` (DECIMAL(10,2), NOT NULL): Fixed base charge in USD.
- `charges_after_limit_per_mb` (DECIMAL(10,4), NOT NULL): Out-of-bundle rate per MB.
- `plan_state` (VARCHAR(20), NOT NULL, DEFAULT 'Activated'): Plan availability (`Activated`, `Deactivated`). Only activated plans can be selected by customers.
- `created_at` / `updated_at`: Audit timestamps.

### `customer_plans`
Manages customer subscriptions, active plans, and future scheduled plan changes.
- `id` (BIGINT, PK, AUTO_INCREMENT): Subscription ID.
- `user_id` (BIGINT, FK -> users.id): The customer.
- `plan_id` (BIGINT, FK -> plans.id): The subscribed plan.
- `status` (VARCHAR(20), NOT NULL, DEFAULT 'ACTIVE'): Subscription status (`ACTIVE`, `SCHEDULED`, `EXPIRED`).
- `start_date` (DATE, NOT NULL): Effective start date.
- `expiry_date` (DATE, NOT NULL): Plan expiry date (end of current billing cycle).
- *Business Rule (SRS US19)*: When a customer requests a plan change, a `SCHEDULED` record is created with `start_date = current_plan.expiry_date`. The current plan stays `ACTIVE` until expiry.

### `bills`
Detailed billing statements computed from aggregated IPDR usage and plan tariffs.
- `id` (BIGINT, PK, AUTO_INCREMENT): Primary key.
- `bill_number` (VARCHAR(50), UNIQUE, NOT NULL): Formatted transaction/bill code (e.g. `T183`, `T341`).
- `user_id` (BIGINT, FK -> users.id): Customer owner.
- `plan_id` (BIGINT, FK -> plans.id): Plan used for rating.
- `plan_name` (VARCHAR(50), NOT NULL): Snapshot of package name.
- `billing_start_date` / `billing_end_date` (DATE): Billing period.
- `total_usage_bytes` (BIGINT): Total bytes consumed (upload + download).
- `usage_in_gb` (DOUBLE): Usage in GB.
- `data_allowance_gb` (DOUBLE): Included quota.
- `remaining_data_mb` (DOUBLE): Remaining quota in MB (or 0 if exceeded).
- `data_after_limit_gb` (DOUBLE): Overage usage in GB.
- `base_charge_usd` (DECIMAL(10,2)): Monthly package charge.
- `excess_charge_usd` (DECIMAL(10,2)): Charges applied to overage data.
- `total_amount_usd` (DECIMAL(10,2)): Base charge + excess charge.
- `status` (VARCHAR(20), NOT NULL, DEFAULT 'PENDING'): Payment state (`PENDING`, `PAID`).
- `payment_mode` (VARCHAR(50)): Mode of payment upon settlement (`PayTM`, `Net banking`, `UPI`, `Online`, etc.).
- `remark` (VARCHAR(255)): Notes or status description.
- `generated_date` / `due_date` / `paid_date`: Lifecycle dates.

### `payments`
Maintains payment transaction receipts.
- `id` (BIGINT, PK, AUTO_INCREMENT): Payment receipt ID.
- `bill_id` (BIGINT, FK -> bills.id): Associated bill.
- `user_id` (BIGINT, FK -> users.id): Customer paying.
- `amount_paid` (DECIMAL(10,2)): Amount paid.
- `payment_mode` (VARCHAR(50)): Payment mode selected.
- `transaction_reference` (VARCHAR(100), UNIQUE): Unique payment gateway/reference code.
- `payment_status` (VARCHAR(20)): `SUCCESS` / `FAILED`.
- `payment_date` (DATETIME): Timestamp of payment.

### `ipdr_records`
Broadband session usage records conforming to the IPDR specification.
- `id` (BIGINT, PK, AUTO_INCREMENT): Record ID.
- `service_identifier` (VARCHAR(100), NOT NULL): Associates IPDR session with customer username/account.
- `user_id` (BIGINT, FK -> users.id, NULLABLE): Resolved customer reference.
- `ip_address` (VARCHAR(45), NOT NULL): Session IP.
- `mac_address` (VARCHAR(20), NOT NULL): Device MAC address.
- `input_octets` (BIGINT): Upload bytes.
- `output_octets` (BIGINT): Download bytes.
- `service_direction` (INT): 1 = Upload, 2 = Download.
- `hostname` (VARCHAR(100), NOT NULL): Valid gateway hostname.
- `session_start` / `session_end` (DATETIME): Session time range.
- `bill_id` (BIGINT, FK -> bills.id, NULLABLE): Bill where this record was mediated and included.

---

## 3. Database Initialization Instructions

1. Start the MySQL Server:
   ```powershell
   Get-Service -Name MySQL80
   ```
2. Run `schema.sql` to initialize tables, constraints, and indexes:
   ```powershell
   mysql -u root -p<YOUR_PASSWORD> < database/schema.sql
   ```
3. Run `seed.sql` to populate initial users, default plans, sample customer subscription, IPDR records, and bills:
   ```powershell
   mysql -u root -p<YOUR_PASSWORD> < database/seed.sql
   ```
