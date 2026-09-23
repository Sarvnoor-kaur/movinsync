# FleetFlow — Relational Database Schema & Data Modeling

## 1. Relational Database Overview

FleetFlow utilizes **MySQL 8.0** as its primary relational store of truth. The database schema enforces data integrity through foreign key constraints, unique indexes, and audit timestamps (`created_at`, `updated_at`). All financial amounts are stored as 64-bit BigInt / Long integers representing values in **paisa** (1 INR = 100 Paisa) to avoid floating-point rounding inaccuracies.

---

## 2. Mermaid Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar email UK
        varchar username
        varchar password_hash
        varchar name
        varchar role
        boolean enabled
        datetime created_at
        datetime updated_at
    }

    VENDORS {
        bigint id PK
        varchar code UK
        varchar name
        varchar tax_id
        varchar contact_email
        varchar contact_phone
        text address
        datetime created_at
        datetime updated_at
    }

    VEHICLES {
        bigint id PK
        bigint vendor_id FK
        varchar registration_number UK
        varchar vehicle_type
        varchar make
        varchar model
        boolean active
        datetime created_at
        datetime updated_at
    }

    CONTRACTS {
        bigint id PK
        varchar contract_number UK
        bigint vendor_id FK
        bigint vehicle_id FK
        varchar name
        varchar billing_type
        date start_date
        date end_date
        varchar status
        datetime created_at
        datetime updated_at
    }

    CONTRACT_VERSIONS {
        bigint id PK
        bigint contract_id FK
        integer version_number
        date effective_from
        date effective_to
        varchar billing_type
        bigint monthly_fixed_fee_paisa
        integer free_km
        integer free_hours
        bigint overage_per_km_paisa
        bigint overage_per_hour_paisa
        bigint night_charge_paisa
        bigint waiting_charge_per_hour_paisa
        bigint toll_handling_charge_paisa
        datetime created_at
        datetime updated_at
    }

    PRICING_SLABS {
        bigint id PK
        bigint contract_version_id FK
        integer slab_order
        integer from_value
        integer to_value
        bigint rate_paisa
        varchar unit_type
        datetime created_at
        datetime updated_at
    }

    TRIPS {
        bigint id PK
        varchar trip_number UK
        bigint vehicle_id FK
        varchar driver_name
        datetime start_time
        datetime end_time
        double distance_km
        double duty_hours
        double waiting_hours
        boolean is_night_trip
        bigint toll_amount_paisa
        varchar status
        datetime created_at
        datetime updated_at
    }

    BILLING_RUNS {
        bigint id PK
        varchar billing_number UK
        bigint vehicle_id FK
        varchar year_month
        bigint total_amount_paisa
        integer total_trips
        varchar status
        datetime created_at
        datetime updated_at
    }

    INVOICES {
        bigint id PK
        varchar invoice_number UK
        bigint billing_run_id FK
        bigint vendor_id FK
        bigint vehicle_id FK
        varchar billing_month
        bigint total_amount_paisa
        varchar status
        datetime created_at
        datetime updated_at
    }

    FRAUD_ALERTS {
        bigint id PK
        bigint vehicle_id FK
        bigint trip_id FK
        bigint billing_run_id FK
        varchar alert_type
        varchar severity
        varchar status
        text details
        datetime created_at
        datetime updated_at
    }

    IDEMPOTENCY_RECORDS {
        bigint id PK
        varchar idempotency_key UK
        varchar endpoint
        varchar http_method
        varchar request_fingerprint
        integer response_status
        text response_body
        varchar status
        datetime expires_at
        datetime created_at
    }

    VENDORS ||--o{ VEHICLES : "operates"
    VENDORS ||--o{ CONTRACTS : "signs"
    VEHICLES ||--o{ CONTRACTS : "assigned_to"
    CONTRACTS ||--o{ CONTRACT_VERSIONS : "has_versions"
    CONTRACT_VERSIONS ||--o{ PRICING_SLABS : "defines_tiers"
    VEHICLES ||--o{ TRIPS : "performs"
    VEHICLES ||--o{ BILLING_RUNS : "billed_in"
    BILLING_RUNS ||--|| INVOICES : "generates"
    VENDORS ||--o{ INVOICES : "receives"
    VEHICLES ||--o{ FRAUD_ALERTS : "flagged_in"
```

---

## 3. Core Database Tables Specification

### 3.1 `users`
Stores user accounts for authentication and role-based security.
- **`id`** (`BIGINT`, PK, Auto-Increment)
- **`email`** (`VARCHAR(255)`, `UNIQUE`, `NOT NULL`)
- **`username`** (`VARCHAR(255)`, `NOT NULL`)
- **`password_hash`** (`VARCHAR(255)`, `NOT NULL`) — BCrypt hashed password
- **`role`** (`VARCHAR(50)`, `NOT NULL`) — `ADMIN`, `HR`, `EMPLOYEE`
- **`enabled`** (`BOOLEAN`, Default: `TRUE`)

### 3.2 `vendors`
Stores logistics transport companies providing fleet services.
- **`id`** (`BIGINT`, PK, Auto-Increment)
- **`code`** (`VARCHAR(50)`, `UNIQUE`, `NOT NULL`) — e.g. `VEN-DELHI-001`
- **`name`** (`VARCHAR(255)`, `NOT NULL`)
- **`tax_id`** (`VARCHAR(100)`) — GSTIN / Tax Identification Number

### 3.3 `vehicles`
Stores vehicle assets assigned to vendors.
- **`id`** (`BIGINT`, PK, Auto-Increment)
- **`vendor_id`** (`BIGINT`, FK ➔ `vendors.id`, `NOT NULL`)
- **`registration_number`** (`VARCHAR(50)`, `UNIQUE`, `NOT NULL`) — e.g. `DL01AB1234`
- **`vehicle_type`** (`VARCHAR(50)`, `NOT NULL`) — `CAB`, `SUV`, `VAN`, `BUS`
- **`active`** (`BOOLEAN`, Default: `TRUE`)

### 3.4 `contracts` & `contract_versions`
Stores master contracts and versioned rate structures.
- **`contracts.contract_number`** (`VARCHAR(100)`, `UNIQUE`, `NOT NULL`)
- **`contract_versions.effective_from`** (`DATE`, `NOT NULL`)
- **`contract_versions.effective_to`** (`DATE`)
- **`contract_versions.monthly_fixed_fee_paisa`** (`BIGINT`) — Retainer fee in paisa

### 3.5 `trips`
Stores completed vehicle trips and logs.
- **`distance_km`** (`DOUBLE`, `NOT NULL`)
- **`duty_hours`** (`DOUBLE`, `NOT NULL`)
- **`waiting_hours`** (`DOUBLE`, `NOT NULL`)
- **`toll_amount_paisa`** (`BIGINT`) — Pass-through toll charges in paisa

---

## 4. Referential Integrity & Constraints

1. **Unique Indexes**:
   - `users(email)`
   - `vendors(code)`
   - `vehicles(registration_number)`
   - `contracts(contract_number)`
   - `idempotency_records(idempotency_key)`
2. **On Delete Restrictions**:
   - `vendors` deletion is **blocked** if child `vehicles` exist.
   - `vehicles` deletion is **blocked** if active `contracts` or `trips` exist.
