# FleetFlow — Rental Fleet Billing & Fair Cost Split

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green.svg?style=flat-square&logo=springsecurity)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Caching-red.svg?style=flat-square&logo=redis)](https://redis.io/)
[![React](https://img.shields.io/badge/React-18.x-61DAFB.svg?style=flat-square&logo=react)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg?style=flat-square&logo=docker)](https://www.docker.com/)

> An enterprise-grade, multi-tenant fleet billing and cost allocation platform designed to automate vendor management, vehicle tracking, contract versioning, trip processing, billing engine calculations, fixed fee distribution, and fraud anomaly detection.

---

## 📌 Project Overview

**FleetFlow** solves complex B2B logistics billing challenges faced by corporate transport operations. When companies hire multiple vehicle vendors (operating cabs, SUVs, vans, and buses) under tiered rate contracts, calculating accurate monthly invoices manually leads to over-billing, rate version disputes, rounding errors, and fraudulent trip claims.

FleetFlow automates this entire lifecycle through a secure Spring Boot 3.3.4 backend, Redis caching layer, MySQL relational database, and an enterprise React dashboard.

---

## 💡 Problem Statement & Solution

### The Business Challenge
1. **Multi-Vendor Complexity**: Companies manage dozens of transport vendors under varying contract types (`PER_TRIP`, `PER_KM`, `SLAB_BASED`, `MONTHLY_FIXED`).
2. **Contract Mid-Month Revisions**: Rates change over time; applying old rates to new trips or new rates retroactively causes financial disputes.
3. **Fixed Monthly Fee Allocation**: Monthly vehicle fixed retainers must be split fairly across corporate trips using mathematical precision (Largest Remainder Method) without losing rounding paisa.
4. **Duplicate Billing & Fraud**: Re-submitting billing runs or logging overlapping/unusual trips causes accidental double payments.

### The Engineering Solution
- **Contract Versioning**: Enforces strict date-range validation to ensure trips are calculated against the exact contract version effective on the trip date.
- **Automated Billing Engine**: Computes base charges, distance/hourly overages, night charges, waiting fees, and pass-through tolls.
- **Idempotency Protection**: Ensures duplicate POST requests with identical `Idempotency-Key` headers return cached execution results without re-running calculations.
- **Redis High-Performance Caching**: Caches rate contracts and vehicle metadata with automatic TTLs and graceful MySQL fallback during Redis outages.
- **End-to-End Tracing**: Implements `X-Correlation-ID` header propagating through SLF4J MDC context for distributed request tracing.

---

## ✨ Key Features Matrix

| Feature | Technical Implementation | Business Value |
|:---|:---|:---|
| **Role-Based Security (RBAC)** | Spring Security 6 + JWT (`ADMIN`, `HR`, `EMPLOYEE`) | Granular access control for operations and finance teams. |
| **Vendor & Fleet Registry** | JPA Entities with unique registration and tax ID constraints | Centralized vendor and vehicle lifecycle management. |
| **Contract Versioning** | Date range overlap validation (`effectiveFrom`, `effectiveTo`) | Eliminates contract disputes during mid-month rate changes. |
| **Dynamic Tiered Pricing** | Tiered pricing slabs (`fromValue`, `toValue`, `ratePaisa`) | Flexible billing rules per vehicle type or route length. |
| **Billing Engine** | Paired strategy calculations in integer **paisa** | Zero floating-point rounding errors in financial transactions. |
| **Fixed Fee Allocation** | Largest Remainder Method (Hamilton/Vinton Algorithm) | Fair, exact distribution of fixed retainers across trips. |
| **Fraud Detection Engine** | Automated rule evaluators for overlapping/unusual trips | Real-time anomaly alerts with resolution workflows. |
| **Idempotency Layer** | Request hash storage (`IdempotencyRecord` + Key) | Prevents duplicate billing execution on network retries. |
| **Redis Caching & Fallback** | Spring Cache (`@Cacheable`, `@CacheEvict`) + `CacheErrorHandler` | Sub-5ms lookups with seamless fallback to MySQL if Redis is down. |
| **Correlation ID Tracing** | `OncePerRequestFilter` + SLF4J MDC + HTTP Response Headers | Complete request traceability across backend microservices. |

---

## 🛠️ Technology Stack

| Layer | Technologies Used |
|:---|:---|
| **Frontend** | React 18, Vite, React Router DOM, Lucide React, Axios, Vanilla CSS Design Tokens |
| **Backend Framework** | Java 21, Spring Boot 3.3.4, Spring Data JPA, Hibernate, Jakarta Validation |
| **Security & Auth** | Spring Security 6, JJWT (Java JWT), BCrypt Password Hashing, MDC Tracing Filter |
| **Database & Cache** | MySQL 8.0, Spring Data Redis, GenericJackson2JsonRedisSerializer |
| **Testing & Tools** | JUnit 5, Mockito, Spring Security Test, Postman Collection v2.1, Maven |
| **DevOps & Containerization**| Docker, Docker Compose, Multi-stage Dockerfile |

---

## 📐 System Architecture

```text
                          ┌──────────────────────────┐
                          │   React Frontend (Vite)  │
                          └────────────┬─────────────┘
                                       │ HTTP / REST
                                       ▼
                          ┌──────────────────────────┐
                          │  Spring Security Filter  │
                          │   (JWT + CorrelationId)  │
                          └────────────┬─────────────┘
                                       │
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
     ┌─────────────────────┐                       ┌─────────────────────┐
     │   REST Controllers  │                       │   Redis Cache Layer │
     └──────────┬──────────┘                       │  (Contracts/Slabs)  │
                │                                  └──────────▲──────────┘
                ▼                                             │ Cache Hit / Miss
     ┌─────────────────────┐                                  │
     │  Services & Engine  ├──────────────────────────────────┘
     └──────────┬──────────┘
                │
                ▼
     ┌─────────────────────┐
     │  Spring Data JPA    │
     └──────────┬──────────┘
                │
                ▼
     ┌─────────────────────┐
     │  MySQL Database 8.0 │
     └──────────┬──────────┘
```

> 📖 **Detailed Architecture Document**: Learn more about the request lifecycle, security filters, and caching strategies in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 🔄 End-to-End Business Workflow

```text
   ┌─────────────────┐       ┌─────────────────┐       ┌──────────────────┐
   │ 1. Create Vendor│ ────► │2. Register Cab  │ ────► │ 3. Create Rate   │
   │   (Vendor ID)   │       │   (Vehicle ID)  │       │     Contract     │
   └─────────────────┘       └─────────────────┘       └────────┬─────────┘
                                                                │
   ┌─────────────────┐       ┌─────────────────┐                │
   │ 6. Invoice &    │ ◄──── │ 5. Execute      │ ◄──────────────┘
   │    Fixed Fee    │       │    Billing Run  │       ┌──────────────────┐
   │   Allocation    │       │   (Paisa Calculations) │ 4. Record Trip   │
   └─────────────────┘       └─────────────────┘ ◄──── │   (Distance/Hrs) │
                                                       └──────────────────┘
```

---

## 📚 Complete Project Documentation Index

| Document | Description | Link |
|:---|:---|:---|
| 🎤 **Project Presentation** | Master presentation guide & complete file-by-file codebase breakdown. | [docs/PROJECT_PRESENTATION.md](docs/PROJECT_PRESENTATION.md) |
| 📋 **Project Overview** | Complete business domain analysis, target users, and RBAC permissions. | [docs/PROJECT_OVERVIEW.md](docs/PROJECT_OVERVIEW.md) |
| 📐 **System Architecture** | Technical layers, security filter chain, Redis caching flow, and request lifecycle. | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| 🗄️ **Database Design** | Relational schema, Mermaid ER diagram, indexes, constraints, and entity mappings. | [docs/DATABASE.md](docs/DATABASE.md) |
| ⚙️ **Business Logic** | Detailed algorithms for tier pricing, contract versioning, Largest Remainder, and fraud rules. | [docs/BUSINESS_LOGIC.md](docs/BUSINESS_LOGIC.md) |
| 🔌 **API Reference** | Complete 50-action REST API documentation with JSON request/response schemas. | [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) |
| 🧪 **API Testing Report** | Postman test results, negative authorization/validation tests, and Redis test cases. | [docs/API_TESTING.md](docs/API_TESTING.md) |
| 🚀 **Deployment Guide** | Step-by-step local setup, Docker Compose commands, environment configuration, and health checks. | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) |

---

## 🧪 Postman API Testing & Verification

All 50 backend actions have been verified using Postman:
- **Authentication & RBAC**: Tested 200 OK access for `ADMIN`, `HR`, `EMPLOYEE` and `403 Forbidden` cross-role restrictions.
- **Validation**: Tested `400 Bad Request` field-level validation errors (e.g. self-registration as `ADMIN` blocked).
- **Idempotency**: Retried `POST /api/billing/runs` with identical `Idempotency-Key` headers to confirm duplicate execution protection.
- **Redis Caching**: Verified sub-5ms cache hits (`vehicles::1`) and verified seamless MySQL fallback when stopping Redis (`docker stop fleet-redis`).

> 📥 **Postman Collection File**: Import [`postman/Fleet-Billing-API.postman_collection.json`](postman/Fleet-Billing-API.postman_collection.json) directly into Postman to run tests.

---

## 🚀 Quick Start Guide

### Prerequisites
- Java 21 JDK
- Node.js 18+ & npm
- MySQL 8.0
- Docker & Docker Compose (Optional for Redis/MySQL containers)

### 1. Run Backend (Spring Boot)
```bash
cd fleet-billing
mvn spring-boot:run
```
*(Backend starts on `http://localhost:8089`)*

### 2. Run Frontend (React + Vite)
```bash
cd fleet-billing/frontend
npm install
npm run dev
```
*(Frontend opens on `http://localhost:5173`)*

### 3. Run Optional Redis Container
```bash
docker run -d --name fleet-redis -p 6379:6379 redis:alpine
```

---

## 📁 Repository Structure

```text
movinsync/
│
├── README.md                      <-- Root Landing Page & Overview
│
├── problems_resolved/             <-- Comprehensive Problems & Solutions Guide Folder
│   ├── README.md                  <-- Master Problems Index
│   ├── 01_FINANCIAL_ROUNDING_PAISA_PRECISION.md
│   ├── 02_MONTHLY_RETAINER_SPLIT_LARGEST_REMAINDER.md
│   ├── 03_CONTRACT_VERSIONING_OVERLAP_PREVENTION.md
│   ├── 04_IDEMPOTENCY_DUPLICATE_INVOICE_PREVENTION.md
│   ├── 05_AUTOMATED_FRAUD_ANOMALY_DETECTION.md
│   ├── 06_REDIS_CACHE_HIGH_AVAILABILITY.md
│   └── 07_LOG_TRACEABILITY_CORRELATION_ID.md
│
├── docs/                          <-- Comprehensive Enterprise Documentation Suite
│   ├── PROJECT_OVERVIEW.md
│   ├── PROJECT_PRESENTATION.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── BUSINESS_LOGIC.md
│   ├── API_DOCUMENTATION.md
│   ├── API_TESTING.md
│   └── DEPLOYMENT.md
│
├── postman/                       <-- Production Postman Collection v2.1
│   └── Fleet-Billing-API.postman_collection.json
│
├── screenshots/                   <-- Verification Evidence & UI Screenshots
│   ├── project/
│   ├── api-testing/
│   └── database/
│
└── fleet-billing/                 <-- Main Spring Boot & React Application Source
    ├── src/                       <-- Java 21 Spring Boot Backend Code
    ├── frontend/                  <-- React 18 + Vite Frontend Application
    ├── pom.xml                    <-- Maven Dependency Management
    ├── Dockerfile                 <-- Multi-stage Docker Container Configuration
    └── docker-compose.yml         <-- Services Orchestration (MySQL + Redis + App)
```

---

## 🔮 Future Enhancements
- Automated multi-tenant invoice PDF exports.
- WebSocket real-time fleet GPS tracking map overlays.
- OAuth2 Single Sign-On (SSO) integration for enterprise Google/Azure AD login.
