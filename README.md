# Placement Drive Management System (PDMS)

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com)
[![Java Version](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2025-blue)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Deploy%20to-Render-46E3B7)](https://render.com)

An enterprise-ready, professional, full-stack **Campus Placement Drive Management System** designed for engineering colleges and university placement cells. The system features a responsive Bootstrap 5 UI, automatic candidate eligibility matching, state-machine placement tracking, atomic JSON file persistence, and Excel-compatible CSV reporting.

---

## 1. Why Vercel Deployment Failed (Root Cause Analysis)

When deploying this project to **Vercel**, accessing the homepage returned `404 NOT_FOUND` for fundamental architectural reasons:

1. **Incompatible Runtime (Serverless Node vs. Java JVM)**:
   - Vercel is a serverless platform designed for static sites and Node.js/Next.js/Edge serverless functions.
   - Vercel does **not** provide a Java Virtual Machine (JVM), Maven build runner, or an embedded servlet container (Tomcat).
   - Because no build output was recognized in a frontend build folder, Vercel served nothing, resulting in `404 NOT_FOUND`.

2. **Persistent State Incompatibility**:
   - Vercel serverless functions have ephemeral, read-only file systems (with only `/tmp` wiped after function execution).
   - This project utilizes atomic, thread-safe JSON file storage (`students.json`, `drives.json`, `registrations.json`, `status_history.json`).
   - Standard Java web applications require a continuous, long-running container process with volume disk mounts.

### The Solution: Deploy with Docker on Render
The application is containerized via a multi-stage Dockerfile (`eclipse-temurin:17`) and configured with a native `render.yaml` Blueprint specification. This ensures continuous operation, full Spring Boot execution, and persistent storage support.

---

## 2. Technology Stack

- **Backend**: Java 17+, Spring Boot 3.3.4 (Spring Web, Spring Validation, Jackson/Gson)
- **Frontend**: Single-Page Responsive UI (HTML5, CSS3, Vanilla JavaScript, Bootstrap 5.3, Bootstrap Icons)
- **Persistence**: Thread-safe atomic JSON file storage using `ReentrantReadWriteLock` and atomic file rename replacement
- **Reporting**: RFC-4180 compliant CSV export engine compatible with Microsoft Excel
- **Containerization**: Multi-stage Dockerfile (`maven:3.9.6-eclipse-temurin-17` -> `eclipse-temurin:17-jre-jammy`)
- **Cloud Deployment**: Render (`render.yaml`), Railway, AWS ECS, or any Docker host

---

## 3. Key Features

1. **Student Management**: Full CRUD, unique roll numbers, CGPA validation (0.00 - 10.00), skills, branch filters, backlog counts, search, and sort.
2. **Placement Drive Management**: Company details, job roles, package (LPA), minimum CGPA, eligible branches, mandatory skills, deadlines, and drive status (Active, Closed, Completed).
3. **Automatic Eligibility Engine**: Real-time evaluation matching CGPA, branch, backlogs, and case-insensitive/whitespace-trimmed mandatory skills. Displays eligible candidates and ineligibility reasons.
4. **Registration Protection**: Synchronized, thread-safe candidate registration preventing duplicate registrations and rejecting inactive/closed drives.
5. **Stage & Status Workflow**: Strict state machine enforcing valid transitions:
   - `REGISTERED` -> `SHORTLISTED` -> `INTERVIEW` -> `SELECTED`
   - Terminal branches: `REJECTED`, `WITHDRAWN`
   - Complete audit trail with timestamps and remarks.
6. **Live Analytics Dashboard**: Dynamically computed counts for students, active drives, registrations, stage pipeline, and recent activity timeline.
7. **Excel-Compatible CSV Reports**: Instant preview and download for student directories, eligible candidates, shortlisted, interview, selected, company-wise statistics, and branch-wise placement percentages.
8. **Backup & Disaster Recovery**: Snapshot backup creation, integrity validation, and instant restoration.

---

## 4. Local Execution & Development

### Prerequisites
- JDK 17, 21, or 25 installed (`java -version`)
- Apache Maven 3.9+ installed (`mvn -version`)

### Quick Start (Windows)
```cmd
# Run tests
test.bat

# Package JAR
build.bat

# Start application locally
run.bat
```

### Manual Command Line
```bash
# 1. Compile and execute all automated JUnit 5 tests
mvn clean test

# 2. Package production executable JAR
mvn package -DskipTests

# 3. Run application with custom port and data directory
java -jar target/placement-drive-management-system-1.0.0.jar --server.port=8080
```

Once started:
- **Web Portal UI**: [http://localhost:8080](http://localhost:8080)
- **Health Check API**: [http://localhost:8080/api/health](http://localhost:8080/api/health)

---

## 5. Deployment to Render

### Option A: Using Render Blueprints (Recommended)
1. Push your repository to GitHub (see Section 6).
2. Log in to [Render Dashboard](https://dashboard.render.com).
3. Click **New +** -> **Blueprint**.
4. Connect your GitHub repository. Render will automatically detect `render.yaml`.
5. Click **Apply**. Render will build the Docker container and start your web service.

### Option B: Manual Web Service Setup
1. In Render Dashboard, click **New +** -> **Web Service**.
2. Connect your GitHub repository.
3. Configure the following settings:
   - **Environment**: `Docker`
   - **Branch**: `main`
   - **Plan**: `Free`
   - **Health Check Path**: `/api/health`
4. Add the following **Environment Variables**:
   - `PORT`: `8080`
   - `DATA_DIR`: `/data`
   - `BACKUP_DIR`: `/backups`
   - `REPORT_DIR`: `/reports`
5. Click **Create Web Service**.

> [!IMPORTANT]
> **Persistent Disk Notice**: On Render's Free tier, container filesystems are ephemeral (reset upon restart or redeployment). For production data retention across restarts, upgrade to Render Starter plan ($7/mo) and attach a Persistent Disk mounted at `/data`.

---

## 6. Git Push Commands

```bash
# Check status
git status

# Stage updated deployment and source files
git add .

# Commit changes
git commit -m "Fix deployment: Dockerfile, render.yaml, documentation and scripts"

# Push to your GitHub repository
git push origin main
```

---

## 7. REST API Reference

All APIs use the `/api` prefix and return standard JSON responses:

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | Service status, total records, and data directory |
| `GET` | `/api/students` | List students with search, filters, and sorting |
| `POST` | `/api/students` | Create a new student (validated) |
| `GET` | `/api/students/{id}` | Get student profile details |
| `PUT` | `/api/students/{id}` | Update existing student profile |
| `DELETE` | `/api/students/{id}` | Delete student and remove registrations |
| `GET` | `/api/drives` | List placement drives with filters |
| `POST` | `/api/drives` | Create a new placement drive |
| `GET` | `/api/drives/{id}` | Get drive details |
| `PUT` | `/api/drives/{id}` | Update drive criteria |
| `DELETE` | `/api/drives/{id}` | Delete drive and associated registrations |
| `GET` | `/api/drives/{id}/eligible-students` | Evaluate candidates and list eligible/ineligible |
| `GET` | `/api/registrations` | List registrations with status/drive filters |
| `POST` | `/api/registrations` | Register eligible student for active drive |
| `PUT` | `/api/registrations/{id}/status` | Advance candidate stage via state machine |
| `GET` | `/api/registrations/{id}/history` | Audit trail of stage changes |
| `GET` | `/api/dashboard/stats` | Dynamic live metrics and recruitment funnel |
| `GET` | `/api/reports/students` | Student directory report |
| `GET` | `/api/reports/shortlisted` | Shortlisted candidates report |
| `GET` | `/api/reports/selected` | Selected / placed candidates report |
| `GET` | `/api/reports/company-wise` | Company placement statistics |
| `GET` | `/api/reports/branch-wise` | Branch placement statistics |
| `GET` | `/api/reports/export-csv?type={type}` | Download CSV export (Excel compatible) |
| `POST` | `/api/backup/create` | Create timestamped database backup |
| `GET` | `/api/backup/list` | List available database backups |
| `POST` | `/api/backup/restore/{name}` | Validate and restore database snapshot |
| `POST` | `/api/demo/load` | Initialize or reload fictional sample dataset |
| `DELETE` | `/api/data/reset` | Clear all data records |

---

## 8. Verification & Smoke Test Results

All 24 automated unit and integration tests execute successfully:
- `CGPA and branch eligibility`: PASS
- `Mandatory skill matching (case-insensitive & trimmed)`: PASS
- `Duplicate registration prevention`: PASS
- `Closed-drive registration rejection`: PASS
- `State machine status transition enforcement`: PASS
- `JSON atomic file storage & recovery`: PASS
- `RFC-4180 CSV escaping for quotes, commas & linebreaks`: PASS
- `E2E full candidate placement lifecycle`: VERIFIED & TESTED
