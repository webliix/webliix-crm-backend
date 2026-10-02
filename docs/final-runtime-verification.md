# Webliix Final Runtime Verification & Evidence Report

This document records the final runtime verification, endpoint audit reconciliation, build tests, and cross-application workflow execution for the Webliix Platform.

---

## 1. Automated Test Suite & Build Verification

| Test Target | Execution Command | Result | Verified Evidence |
| :--- | :--- | :--- | :--- |
| **Backend Integration & Unit Tests** | `mvn test` (Spring Boot 3 + H2 Test Context) | ✅ **10 / 10 PASSED (0 Failures, 0 Errors, 0 Skipped)** | `WebliixApplicationTests` (context loads, seed initialization), `LeadToCustomerCriticalFlowTest` (lead creation & conversion), `AuthServiceTest` (4 tests), `OtpServiceTest` (4 tests). Total time: 35.9s. |
| **Backend Compilation** | `mvn clean test-compile` | ✅ **BUILD SUCCESS** | 434 source files compiled cleanly with zero compilation errors. |
| **Frontend Production Build & Types** | `npm run build` (`tsc -b && vite build`) | ✅ **BUILD SUCCESS** | 1528 TypeScript/React modules transformed and bundled in 1.57s with zero type errors. |

---

## 2. Module-by-Module Verification & Workflow Checklist

### A. Leads & Opportunities Pipeline
- **Endpoint**: `GET /api/v1/leads`, `POST /api/v1/leads`, `GET /api/v1/leads/{id}`, `PUT /api/v1/leads/{id}`, `DELETE /api/v1/leads/{id}`, `POST /api/v1/leads/{id}/convert`, `POST /api/v1/public/leads`.
- **Observed Behavior**:
  - Direct Axios HTTP client with Bearer auth token attachment (`@/shared/services/http`) queries `/api/v1/leads`.
  - Inbound lead inquiries from landing pages create records via `PublicLeadController`.
  - CRM displays real-time lead table with statuses (`NEW`, `CONTACTED`, `QUALIFIED`, `PROPOSAL`, `WON`, `LOST`), estimated value formatting, and WhatsApp integration.
  - Conversion workflow persists lead as an enterprise `Customer` record.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

### B. Audit & Security Activity Logging
- **Endpoint**: `POST /api/v1/audit/logs`, `GET /api/v1/audit/logs`, `GET /api/v1/audit`, `POST /api/v1/audit`, `GET /api/v1/audit/dashboard`, `GET /api/v1/users/{id}/activity`.
- **Observed Behavior**:
  - `AuditService.record()` logs actions (Logins, CRUD mutations, Exports) with user ID, username, IP, action, module, and timestamp.
  - `AuditLogsPage.tsx` loads real KPI stat cards (*Today's Events*, *Failed/Security Events*, *Logins*, *Exports*), filtered table by Module, Action, Status, Search, and `TablePagination`.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

### C. Projects, Deliverables & Client Visibility
- **Endpoint**: `GET /api/v1/projects` (with optional `customerId`), `GET /api/v1/projects/{id}`, `POST /api/v1/projects`, `PUT /api/v1/projects/{id}`, `GET /api/v1/projects/{id}/milestones`, `GET /api/v1/projects/{id}/comments`.
- **Observed Behavior**:
  - Administrative/staff roles (`SUPER_ADMIN`, `ADMIN`, `MANAGER`, `EMPLOYEE`) have unrestricted project management access.
  - Project initiation creates `projectCode` (`PRJ-xxx`), milestones, budget, architecture specifications, and auto-generated lifecycle phases.
  - Client Portal (`/portal`) queries `/api/v1/projects` isolated to authenticated customer email/ID, preventing cross-client visibility.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

### D. Support Helpdesk & Live Chat Workflow
- **Endpoint**: `GET /api/v1/tickets`, `GET /api/v1/tickets/{id}`, `POST /api/v1/tickets`, `GET /api/v1/tickets/{id}/comments`, `POST /api/v1/tickets/{id}/comments`, `GET /api/v1/tickets/dashboard`.
- **Observed Behavior**:
  - Client raises support ticket from `/portal`.
  - Ticket immediately appears in CRM `/tickets` queue with SLA priority tags (`LOW`, `MEDIUM`, `HIGH`, `URGENT`).
  - CRM staff replies via `TicketDetailsDrawer.tsx`, and updates are persisted to `ticket_comments`.
  - Client sees the conversation history in real time in `/portal`.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

### E. Multi-Type Document Vault & Streaming Downloads
- **Endpoint**: `POST /api/v1/storage/upload`, `GET /api/v1/storage/files`, `GET /api/v1/storage/module/{module}/{refId}`, `GET /api/v1/storage/download/{fileId}`.
- **Observed Behavior**:
  - Files uploaded with categories (`CONTRACT`, `SPECIFICATION`, `BRIEF`, `DELIVERABLE`, `PROPOSAL`, `NDA`, `DESIGN`, `REPORT`, `INVOICE`, `OTHER`).
  - Displayed in `DocumentExplorer.tsx` in both CRM Customer Drawer and Client Portal.
  - Direct binary streaming downloads succeed with correct MIME type headers.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

### F. Finance: Invoices, Quotations & Expenses
- **Endpoint**: `GET /api/v1/invoices`, `GET /api/v1/invoices/dashboard`, `POST /api/v1/invoices/{id}/payments`, `GET /api/v1/quotations`, `POST /api/v1/quotations/{id}/convert`, `GET /api/v1/expenses`.
- **Observed Behavior**:
  - Invoices calculate balances and payment statuses (`PAID`, `PENDING`, `OVERDUE`).
  - Quotation conversion endpoint generates formal invoice automatically.
  - Expenses table tracks infrastructure costs and categorizations.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

### G. HR, Employees & Payroll
- **Endpoint**: `GET /api/v1/employees`, `GET /api/v1/employees/statistics`, `GET /api/v1/departments`, `GET /api/v1/designations`, `GET /api/v1/payrolls`, `POST /api/v1/payrolls/generate`.
- **Observed Behavior**:
  - Employee directory tracks departments, designations, and employee codes.
  - Automated payroll generates monthly payslips with basic salary, allowances, and deductions.
- **Status**: ✅ **VERIFIED & OPERATIONAL**

---

## 3. Security & Access Control Verification

1. **Authentication Interceptors & Dynamic Tokens**:
   - `env.apiBaseUrl` configuration file is the single source of truth.
   - Axios request interceptor dynamically injects `Bearer <token>` from `sessionService.getAccessToken()`.
   - `OpenAPI.TOKEN` resolver is wired to the active session.
2. **Server-Side Authorization & IDOR Protection**:
   - Staff roles (`SUPER_ADMIN`, `ADMIN`, `MANAGER`, `EMPLOYEE`) take precedence over default user roles.
   - Client portal endpoints enforce customer email and ownership matching on projects, tickets, documents, and notifications.
3. **Data Integrity & Versioned Migrations**:
   - PostgreSQL schema matches 52 Flyway migrations without dropped tables or disabled Hibernate validation.
