# USAGE MEDIATION BILLING SYSTEM

A comprehensive, end-to-end broadband usage mediation, rating, and billing platform built with Spring Boot, MySQL, and React (TypeScript + Bootstrap) based on the Infosys Engineering Academy Capstone Specification.

---

## 1. System Architecture & Tech Stack

- **Backend**: Java 17, Spring Boot 4.1.x, Spring Data JPA / Hibernate, Spring Security, REST APIs
- **Database**: MySQL 8.0+ (`billing` database)
- **Frontend**: React 19, TypeScript, React Router 7, Bootstrap 5
- **Integrated IPDR Simulator & Rating Engine**: Built directly into Spring Boot (`/api/ipdr/simulate-and-download/{username}`) with DOCSIS 3.1 XML generation & download
- **Build Tools**: Maven (`./mvnw.cmd`), npm

---

## 2. Database Setup & Architecture

### Schema & Seed Deliverables
- [`database/schema.sql`](file:///c:/NetworkCapstoneProject/database/schema.sql): Complete DDL for all 7 tables, primary keys, foreign keys, constraints, and indexes.
- [`database/seed.sql`](file:///c:/NetworkCapstoneProject/database/seed.sql): Initial seed data for Admin, Operator, Customers, broadband plans, sample subscriptions, recognized network devices, IPDR records, and bills.
- [`database/README.md`](file:///c:/NetworkCapstoneProject/database/README.md): Detailed database documentation.

### Core Tables & Relationships
1. **`users`**: Authentication, roles (`ADMIN`, `OPERATOR`, `CUSTOMER`), user lifecycle state (`Activated`, `Deactivated`), security questions/answers.
2. **`plans`**: Operator-managed broadband packages (`MomNDad`, `SocialTeen`, `VideoMate`, `TorrentGuy`) with quota allowances, base monthly rates, overage rates, and plan state (`Activated`, `Deactivated`).
3. **`customer_plans`**: Subscriptions mapping customers to plans. Supports **scheduled plan changes** activating upon current plan expiry (SRS US19).
4. **`recognized_devices`**: Approved network gateways, CMTS routers, and proxy nodes with IP address, MAC address, hostname, and active status (`ACTIVE`, `INACTIVE`). Incoming IPDR records must originate from recognized devices.
5. **`ipdr_records`**: Internet Protocol Detail Records capturing broadband session metrics (service direction, octets passed, service identifier, hostname, IP address, MAC address, start/end timestamps).
6. **`bills`**: Billing invoices generated from mediated IPDR usage and rating tariffs (`PENDING`, `PAID`).
7. **`payments`**: Payment transaction receipts with unique references and payment modes (`PayTM`, `Net banking`, `UPI`, `Online`, etc.).

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

## 3. How to Run the System

### A. Backend (Spring Boot Server)
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
   Backend runs at `http://localhost:8081`.

### B. Frontend (React Application)
1. Launch development server:
   ```powershell
   cd c:\NetworkCapstoneProject\frontend
   npm start
   ```
   Frontend starts at `http://localhost:3000`.

### C. Integrated Usage Simulation & Canonical XML Generation (SRS US16)
The usage simulator is **fully integrated** into the backend REST service and frontend UI:
- **1-Click Simulation in Dashboard**: Log in as a customer (e.g. `customer1`) and click **`⚡ Ingest Usage (Download XML)`**. The system instantly simulates network session traffic, mediates against active tariffs, updates the pending bill and remaining quota metrics, and automatically downloads the canonical DOCSIS 3.1 `ipdr_usage_<username>.xml` file.
- **REST API Endpoint**:
  ```http
  GET /api/ipdr/simulate-and-download/{username}
  POST /api/ipdr/simulate-and-download/{username}
  POST /api/ipdr/simulate/{username}?count=3
  ```
- **Direct XML Ingestion**: If you have an external or custom XML document to test, send it via:
  ```http
  POST /api/ipdr/upload-xml
  Content-Type: application/xml
  ```

---

## 4. Test Credentials (Seed Data)

| Role | Username | Password | Security Question / Answer |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `1` | *What is your pet name?* / `admin` |
| **OPERATOR** | `operator` | `Operator@123` | *What is your birthplace?* / `New York` |
| **CUSTOMER** | `customer1` | `Customer@123` | *What is your pet name?* / `Fluffy` |
| **CUSTOMER** | `customer2` | `Customer@123` | *What is your birthplace?* / `London` |

---

## 5. Operator Module (SRS US10 – US14 + US07)

1. **Operator Home Page (US10)**:
   - Dedicated dashboard accessible upon Operator login at `/operator/dashboard`.
   - Top navigation bar featuring logged-in username, Logout, Change Password, and 5 functional tabs:
     - `PLANS`
     - `ADD PLAN`
     - `EDIT PLAN`
     - `DELETE PLAN`
     - `REPORT`
   - Role-Based Access Control (RBAC): Customer roles attempting to access Operator endpoints receive an HTTP 403 Forbidden.

2. **Plans Listing & Pagination (US11)**:
   - Displays all active plans in a structured table: Package Name, Data in GB, Monthly Charge (USD), Charges After Limit (per MB), and Status (`Activated`).
   - Server-side pagination strictly enforcing the SRS requirement: **maximum 10 plans per screen** with page navigation buttons (`[1] [2]...`).
   - Deactivated plans are automatically filtered out from active listing.

3. **Add Plan (US12)**:
   - Form inputs: Package Name, Data Allowance (GB), Monthly Charge (USD), Charges After Limit (per MB).
   - Strict validation:
     - Package name must be alphanumeric (`^[a-zA-Z0-9]+$`). Special characters are rejected on both frontend and backend.
     - Data Allowance must be numeric and > 0.
     - Monthly Charge and Charges After Limit must be non-negative numeric values.
   - Newly created plans are automatically assigned `Activated` state by the backend.
   - Redirects to the Plans view upon creation with confirmation banner.

4. **Edit Plan (US13)**:
   - Allows operator to select an active plan, pre-populating existing parameters.
   - Validates updated values and displays a confirmation prompt before applying changes.
   - **Historical Bill Protection**: Plan edits do not alter or overwrite historical bills. Past invoices store snapshot copies of tariff terms (`data_allowance_gb`, `base_charge_usd`, `excess_charge_usd`), ensuring historical invoices remain 100% stable.

5. **Deactivate Plan (US14)**:
   - Operator selects an active plan to deactivate.
   - Confirmation modal: `"ARE YOU SURE YOU WANT TO DELETE <PackageName> ?"`.
   - **Logical Deactivation**: Changes `plan_state` to `Deactivated`. Does NOT physically delete the database record, preserving foreign-key integrity for customer subscriptions and billing history.
   - Deactivated plans immediately disappear from the active plans list and are blocked from new customer selections.

6. **Operator Report / Sales Analysis (US07)**:
   - Real-time sales and subscriber breakdown powered by live MySQL data.
   - Interactive SVG Pie Chart displaying subscriber distribution across all active packages.
   - Aggregate sales breakdown table: Package Name, Allowance, Monthly Charge, Subscriber Count, Subscriber %, Total Data Consumed (GB), and Total Revenue ($ USD).

---

## 6. Integrated IPDR Simulation & Mediation Pipeline (SRS US16)

### Pipeline Architecture

```
+-------------------------------------------------------------+
|        INTEGRATED USAGE SIMULATION & XML GENERATION         |
|   (Triggered via UI "⚡ Ingest Usage" or REST API)           |
|                                                             |
|   1. Fetches Customer Subscription & Active Plan Tariff     |
|   2. Selects Approved Gateway from recognized_devices       |
|   3. Simulates realistic upload/download session traffic    |
|   4. Generates Canonical DOCSIS 3.1 IPDR XML                |
|   5. Returns XML as direct browser attachment download      |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|             BACKEND USAGE MEDIATION & RATING                |
|                                                             |
|   1. XML / Session Parsing extracts IPDR records            |
|   2. Validation Engine verifies:                            |
|        - Recognized Device Check (Hostname & IP in DB)      |
|        - Service Identifier Maps to Active Customer         |
|        - Service Direction is 1 (Upload) or 2 (Download)    |
|        - IPv4 & MAC format validation                       |
|        - Positive octets passed                             |
|   3. Usage Aggregation: Inputs + Outputs converted to GB   |
|   4. Tariff Rating Engine:                                  |
|        - excess_mb = max(0, (total_gb - allowance_gb)*1024) |
|        - excess_charge = excess_mb * rate_per_mb            |
|        - total_bill = base_monthly_charge + excess_charge   |
|   5. Persist/Update Bill in bills table                     |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                     CUSTOMER PORTAL                         |
|   Customer sees updated live usage & pending invoice        |
+-------------------------------------------------------------+
```

### Canonical IPDR XML Schema (DOCSIS 3.1)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<IPDRDoc xmlns="urn:ipdr:namespaces:ipdr" version="3.1">
  <IPDR xsi:type="DOCSIS-Type" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <CMTSHostName>isp-gw01.net</CMTSHostName>
    <CMTSipAddress>10.0.0.1</CMTSipAddress>
    <CMmacAddress>00:1A:2B:3C:4D:5E</CMmacAddress>
    <serviceIdentifier>SID-1001</serviceIdentifier>
    <serviceDirection>2</serviceDirection>
    <serviceOctetsPassed>245348823</serviceOctetsPassed>
  </IPDR>
</IPDRDoc>
```

---

## 7. Customer Module Features (SRS US15 – US21)

1. **Customer Home / Current Usage (US15)**: Displays active plan name, cycle dates, total usage in GB, remaining quota in MB, total current amount, and overage in GB.
2. **Bill Details & Search (US16)**: Transaction history with search filtering by invoice ID.
3. **Pending Bills (US18)**: Direct listing of unpaid invoices with instant `PAY` action.
4. **Payment (US17)**: Multi-channel checkout (`PayTM`, `Net banking`, `UPI`, `Credit Card`, `Debit Card`, `Online`) with confirmation prompt and receipt generation.
5. **Change Plan (US19)**: Delayed activation schedule activating upon current plan expiry date.
6. **User Report (US20)**: Visual daily consumption bar chart and upload/download statistics.
7. **Forgot Password (US21)**: Self-service security question password reset.

---

## 8. Verification & Integration Testing

### A. Run Maven Integration Tests
```powershell
cd c:\NetworkCapstoneProject\backend
.\mvnw.cmd test
```
Tests passed:
- `OperatorAndSimulatorIntegrationTests`: 9/9 passed (Operator CRUD, pagination, validation, recognized device validation, XML ingestion & rating, RBAC).
- `CustomerModuleIntegrationTests`: 13/13 passed (Customer home, bills, payments, delayed plan change, reports, forgot password).
- `UsagebillingApplicationTests`: 1/1 passed.

### B. Run End-to-End Suite
```powershell
cd c:\NetworkCapstoneProject
node scratch/test_e2e.js
```
Automated end-to-end suite exercises the entire application lifecycle:
1. Operator authentication & role assignment.
2. Customer authentication.
3. RBAC validation: Customer accessing Operator endpoints receives 403 Forbidden.
4. Active plans listing with 10-plan pagination.
5. Add plan with alphanumeric validation & automatic `Activated` state.
6. Add plan negative testing (rejecting illegal symbols).
7. Edit plan with parameter updates.
8. Deactivate plan: logical deactivation with preservation of past customer references.
9. Operator Report: live SVG sales pie chart and revenue breakdown.
10. Baseline Customer home usage.
11. Standalone Java IPDR Simulator execution (`--once`).
12. IPDR XML Ingestion and recognized device validation.
13. Customer home usage and bill amount update verification.
14. Negative XML testing: rejection of unrecognized network devices and rogue hostnames.
