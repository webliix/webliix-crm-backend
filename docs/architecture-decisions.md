# Webliix Architecture Decision Records (ADR)

This document records the core architectural decisions, interface designs, and infrastructure patterns enforced across the Webliix Platform.

---

## 1. Single Source of Truth for API Gateway Configuration

### Context
Previous frontend code had fragmented API URLs, generated openapi clients using unauthenticated `fetch`, and instances where hardcoded backend base URLs conflicted with central environments.

### Decision
- The central file `Frontend/frontend/src/config/env.ts` (`env.apiBaseUrl`) is the **sole single source of truth** for all network requests in the application.
- All services, modules, and OpenAPI generated clients must route requests through the centralized Axios client (`@/shared/services/http`) or resolve `OpenAPI.TOKEN` dynamically via `sessionService.getAccessToken()`.
- Local/Cloud switching is handled strictly in `env.ts` without scattering environment variables across components.

---

## 2. Customer vs Staff Role Separation in Project Service

### Context
In `ProjectServiceImpl.java`, users possessing `ROLE_USER` or `USER` were unconditionally intercepted by `isCustomer(auth)` and restricted to email queries, causing administrators and managers with hybrid role assignments to see empty project lists or be blocked from project operations.

### Decision
- `isCustomer(Authentication auth)` now checks for administrative/staff roles (`SUPER_ADMIN`, `ADMIN`, `MANAGER`, `EMPLOYEE`) first. If present, the user is granted full staff visibility.
- Added explicit `@RequestParam(required = false) Long customerId` support to `GET /api/v1/projects` so that both the Client Portal and CRM customer-specific views can query projects deterministically.

---

## 3. Separation of CRM and Client Portal Boundaries

### Context
Client-facing features (deliverables tracking, document downloads, support tickets, notifications) needed a clean separation from internal staff operations (lead conversion, employee management, payroll processing, system audits).

### Decision
- Created a dedicated Client Portal surface (`/portal` in `ClientPortalPage.tsx`) guarded by route permissions.
- Secured backend endpoints ensure that client users can only read projects, documents, tickets, and notifications associated with their verified account email / ID.
- Internal CRM modules (Leads, Quotations, Invoices, HR, Automations, Audit Logs, Settings) remain strictly staff-accessible.

---

## 4. Multi-Type Document Repository Architecture

### Context
Customers and projects require diverse document attachments beyond contracts (e.g. specifications, briefs, design deliverables, proposals, NDAs, invoices).

### Decision
- Standardized `StoredFile` metadata with a categorized `file_category` enum: `CONTRACT`, `SPECIFICATION`, `BRIEF`, `DELIVERABLE`, `PROPOSAL`, `NDA`, `DESIGN`, `REPORT`, `INVOICE`, `OTHER`.
- Implemented `DocumentExplorer.tsx` with filtering, preview metadata, size computation, and direct streaming download via `GET /api/v1/storage/download/{fileId}`.

---

## 5. Audit Logging Architecture

### Context
Audit logging needed unified REST endpoints supporting both GET query parameter searches and POST payload searches, with dashboard aggregations and pagination.

### Decision
- Updated `AuditController.java` with aliases for `GET /api/v1/audit/logs`, `POST /api/v1/audit/logs`, `GET /api/v1/audit`, `POST /api/v1/audit`, and `GET /api/v1/audit/dashboard`.
- Wrapped responses in `ApiResponse<Page<AuditLogResponse>>` and `ApiResponse<AuditDashboardResponse>`.
- Refactored `AuditLogsPage.tsx` with KPI stat cards, module/action/status dropdowns, search, and pagination.
