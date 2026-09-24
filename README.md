# USAGE MEDIATION BILLING SYSTEM

A comprehensive, end-to-end broadband usage mediation, rating, and billing platform built with Spring Boot, MySQL, and React (TypeScript + Bootstrap) based on the Infosys Engineering Academy Capstone Specification.

---

## 1. System Architecture & Tech Stack

- **Backend**: Java 17, Spring Boot 4.1.x, Spring Data JPA / Hibernate, Spring Security, REST APIs
- **Database**: MySQL 8.0+
- **Frontend**: React 19, TypeScript, React Router 7, Bootstrap 5
- **Build Tools**: Maven (`./mvnw.cmd`), npm

---

## 2. Database Setup & Architecture

### Schema & Seed Deliverables
- [database/schema.sql](file:///c:/NetworkCapstoneProject/database/schema.sql): Complete DDL for all 6 tables, primary keys, foreign keys, constraints, and indexes.
- [database/seed.sql](file:///c:/NetworkCapstoneProject/database/seed.sql): Initial seed data for Admin, Operator, Customers, broadband plans, sample subscriptions, IPDR records, and bills.
- [database/README.md](file:///c:/NetworkCapstoneProject/database/README.md): Detailed database documentation.

### Core Tables & Relationships
1. **`users`**: Authentication, roles (`ADMIN`, `OPERATOR`, `CUSTOMER`), user lifecycle state (`Activated`, `Deactivated`), security questions/answers.
2. **`plans`**: Operator-managed broadband packages (`MomNDad`, `SocialTeen`, `VideoMate`, `TorrentGuy`) with quota allowances, base monthly rates, and overage rates.
3. **`customer_plans`**: Subscriptions mapping customers to plans. Supports **scheduled plan changes** activating upon current plan expiry (SRS US19).
4. **`bills`**: Billing invoices generated from mediated IPDR usage and rating tariffs (`PENDING`, `PAID`).
5. **`payments`**: Payment transaction receipts with unique references and payment modes (`PayTM`, `Net banking`, `UPI`, `Online`, etc.).
6. **`ipdr_records`**: Internet Protocol Detail Records capturing broadband session metrics (InputOctets, OutputOctets, service direction, hostname, start/end timestamps).

### Initializing MySQL Database
Run the following in PowerShell:
```powershell
# 1. Ensure MySQL service is running
Get-Service -Name MySQL80

# 2. Execute schema.sql
Get-Content "c:\NetworkCapstoneProject\database\schema.sql" | mysql -u root -p<YOUR_PASSWORD> billing

# 3. Populate seed.sql
Get-Content "c:\NetworkCapstoneProject\database\seed.sql" | mysql -u root -p<YOUR_PASSWORD> billing
```

---

## 3. How to Run the Application

### A. Backend (Spring Boot)
1. Verify database credentials in [backend/src/main/resources/application.properties](file:///c:/NetworkCapstoneProject/backend/src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/billing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=<YOUR_PASSWORD>
   ```
2. Start backend server:
   ```powershell
   cd c:\NetworkCapstoneProject\backend
   .\mvnw.cmd spring-boot:run
   ```
   Backend listens at `http://localhost:8081`.

### B. Frontend (React)
1. Install dependencies & launch:
   ```powershell
   cd c:\NetworkCapstoneProject\frontend
   npm start
   ```
   Frontend starts at `http://localhost:3000`.

---

## 4. Test Credentials (Seed Data)

| Role | Username | Password | Security Question / Answer |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `1` | *What is your pet name?* / `admin` |
| **OPERATOR** | `operator` | `Operator@123` | *What is your birthplace?* / `New York` |
| **CUSTOMER** | `customer1` | `Customer@123` | *What is your pet name?* / `Fluffy` |
| **CUSTOMER** | `customer2` | `Customer@123` | *What is your birthplace?* / `London` |

---

## 5. Customer Module Features (SRS US15 – US21)

1. **Customer Home / Current Usage (US15)**:
   - Displays current plan name (`MOMNDAD`), billing cycle dates (`FROM: 2026-09-01 TO: 2026-10-01`), total usage in GB, remaining quota in MB, total current amount, and data after limit in GB.
   - Shows active banner for scheduled plan changes.
   - Direct shortcuts to Pending Bills and Live Usage Simulation.

2. **Bill Details & Search (US16)**:
   - Transaction history for the customer.
   - Live search filter by Bill ID (e.g., `T183`, `T27`, `T341`).
   - Detailed breakdown modal (usage, quota, base charge, overage charge, payment status, payment mode).

3. **Pending Bills (US18)**:
   - Lists unpaid invoices with invoice number, date, amount, and status.
   - Direct `PAY` action navigating to payment.

4. **Payment (US17)**:
   - Displays invoice breakdown and payable amount.
   - Supported payment modes: `PayTM`, `Net banking`, `UPI`, `Credit Card`, `Debit Card`, `Online`.
   - Confirmation dialog before executing payment.
   - Updates bill status to `PAID`, records payment receipt, and removes bill from pending list.

5. **Change Plan (US19)**:
   - **Critical SRS Rule**: *"When a customer changes to a new plan, the new plan should be activated only after the current plan expires."*
   - Current plan remains `ACTIVE` until `expiry_date`.
   - The selected plan is saved as `SCHEDULED` with activation date equal to current plan expiry date.

6. **User Report (US20)**:
   - Date range selector (`From Date` to `To Date`).
   - Visual bar chart showing daily consumption in MB.
   - Summary statistics (Total consumed GB, Upload GB, Download GB).

7. **Forgot Password (US21)**:
   - Self-service password recovery from login page:
     1. Enter username -> fetches registered security question.
     2. Answer security question -> verifies correctness.
     3. Enter new password & confirm -> validates and hashes password -> redirects to login.

8. **Change Password**:
   - Available via header for all authenticated users with Captcha verification and password strength rules.

---

## 6. IPDR Mediation & Rating Pipeline

```
Broadband Session (IPDR)
  -> Hostname Validation
  -> Customer Identification (service_identifier)
  -> Octet Aggregation (InputOctets + OutputOctets)
  -> Tariff Engine & Plan Matching
  -> Quota vs Overage Calculation:
       excess_mb = (usage_gb - allowance_gb) * 1024
       excess_charge = excess_mb * rate_per_mb
       total_amount = base_monthly_charge + excess_charge
  -> Bill Generation & Persistence
  -> Customer Portal Visualization
```

---

## 7. Automated Testing

Run the full integration test suite covering all functional criteria:
```powershell
cd c:\NetworkCapstoneProject\backend
.\mvnw.cmd test
```
All 13 integration tests pass covering authentication, customer home, bills, isolation, payments, change plan delayed activation, reports, forgot password, and IPDR mediation.
"# UsageBillingSystem" 
