# Webliix API Endpoint Audit & Repair Catalog

This document catalogues the full endpoint inventory of the Webliix Spring Boot REST API (~250 endpoints), mapping each route to its controller, authentication requirement, request/response structures, target consumer, and operational status.

---

## 1. Authentication & Security Endpoints (`com.webliix.security.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | `AuthController` | None | CRM / Portal | `LoginRequest` (email, password) | `ApiResponse<AuthResponse>` (token, refreshToken, user) | ✅ Active |
| `POST` | `/api/v1/auth/register` | `AuthController` | None | Public / CRM | `RegisterRequest` (email, password, firstName, lastName) | `ApiResponse<AuthResponse>` | ✅ Active |
| `POST` | `/api/v1/auth/refresh` | `AuthController` | None | CRM / Portal | `RefreshTokenRequest` (refreshToken) | `ApiResponse<AuthResponse>` | ✅ Active |
| `POST` | `/api/v1/auth/logout` | `AuthController` | Bearer Token | CRM / Portal | None | `ApiResponse<Void>` | ✅ Active |
| `GET` | `/api/v1/auth/verify-email` | `AuthController` | None | Public | `token` (query param) | `ApiResponse<String>` | ✅ Active |
| `POST` | `/api/v1/auth/forgot-password` | `AuthController` | None | Public | `email` (body) | `ApiResponse<String>` | ✅ Active |
| `GET` | `/api/v1/users` | `UserController` | ADMIN / SUPER_ADMIN | CRM | `page`, `size` | `ApiResponse<Page<UserResponse>>` | ✅ Active |
| `GET` | `/api/v1/users/{id}` | `UserController` | ADMIN / SUPER_ADMIN | CRM | `id` (path) | `ApiResponse<UserResponse>` | ✅ Active |
| `POST` | `/api/v1/users` | `UserController` | ADMIN / SUPER_ADMIN | CRM | `CreateUserRequest` | `ApiResponse<UserResponse>` | ✅ Active |
| `PUT` | `/api/v1/users/{id}` | `UserController` | ADMIN / SUPER_ADMIN | CRM | `UpdateUserRequest` | `ApiResponse<UserResponse>` | ✅ Active |
| `DELETE` | `/api/v1/users/{id}` | `UserController` | SUPER_ADMIN | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Active |
| `GET` | `/api/v1/users/api-tokens` | `ApiTokenController` | Authenticated | CRM | None | `ApiResponse<List<ApiTokenResponse>>` | ✅ Active |
| `POST` | `/api/v1/users/api-tokens` | `ApiTokenController` | Authenticated | CRM | `CreateApiTokenRequest` | `ApiResponse<ApiTokenResponse>` | ✅ Active |
| `DELETE` | `/api/v1/users/api-tokens/{id}` | `ApiTokenController` | Authenticated | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Active |

---

## 2. Leads & Opportunities Endpoints (`com.webliix.crm.lead.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/leads` | `LeadController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<LeadResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/leads/search` | `LeadController` | Authenticated | CRM | `keyword`, `page`, `size` | `ApiResponse<Page<LeadResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/leads/{id}` | `LeadController` | Authenticated | CRM | `id` (path) | `ApiResponse<LeadResponse>` | ✅ Repaired |
| `POST` | `/api/v1/leads` | `LeadController` | Authenticated | CRM | `CreateLeadRequest` | `ApiResponse<LeadResponse>` | ✅ Repaired |
| `PUT` | `/api/v1/leads/{id}` | `LeadController` | Authenticated | CRM | `CreateLeadRequest` | `ApiResponse<LeadResponse>` | ✅ Repaired |
| `DELETE` | `/api/v1/leads/{id}` | `LeadController` | Authenticated | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Repaired |
| `POST` | `/api/v1/leads/{id}/convert` | `LeadController` | Authenticated | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Repaired |
| `POST` | `/api/v1/public/leads` | `PublicLeadController` | None (Public) | Website / Landing | `PublicLeadRequest` | `ApiResponse<PublicLeadResponse>` | ✅ Active |

---

## 3. Customer Management Endpoints (`com.webliix.crm.customer.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/customers` | `CustomerController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<CustomerResponse>>` | ✅ Active |
| `GET` | `/api/v1/customers/{id}` | `CustomerController` | Authenticated | CRM | `id` (path) | `ApiResponse<CustomerResponse>` | ✅ Active |
| `POST` | `/api/v1/customers` | `CustomerController` | Authenticated | CRM | `CreateCustomerRequest` | `ApiResponse<CustomerResponse>` | ✅ Active |
| `PUT` | `/api/v1/customers/{id}` | `CustomerController` | Authenticated | CRM | `CreateCustomerRequest` | `ApiResponse<CustomerResponse>` | ✅ Active |
| `DELETE` | `/api/v1/customers/{id}` | `CustomerController` | Authenticated | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Active |
| `GET` | `/api/v1/customers/statistics` | `CustomerController` | Authenticated | CRM | None | `ApiResponse<CustomerStatsResponse>` | ✅ Active |
| `GET` | `/api/v1/customers/{id}/contacts` | `CustomerController` | Authenticated | CRM | `id` (path) | `ApiResponse<List<CustomerContactResponse>>` | ✅ Active |
| `POST` | `/api/v1/customers/{id}/contacts` | `CustomerController` | Authenticated | CRM | `CreateContactRequest` | `ApiResponse<CustomerContactResponse>` | ✅ Active |
| `GET` | `/api/v1/customers/{id}/notes` | `CustomerController` | Authenticated | CRM | `id` (path) | `ApiResponse<List<CustomerNoteResponse>>` | ✅ Active |
| `POST` | `/api/v1/customers/{id}/notes` | `CustomerController` | Authenticated | CRM | `CreateNoteRequest` | `ApiResponse<CustomerNoteResponse>` | ✅ Active |

---

## 4. Projects, Milestones, Tasks & Chat Endpoints (`com.webliix.projects.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/projects` | `ProjectController` | Authenticated | CRM / Portal | `page`, `size`, `customerId` (optional) | `ApiResponse<Page<ProjectResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/projects/{id}` | `ProjectController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<ProjectResponse>` | ✅ Repaired |
| `POST` | `/api/v1/projects` | `ProjectController` | Authenticated (Staff) | CRM | `CreateProjectRequest` | `ApiResponse<ProjectResponse>` | ✅ Repaired |
| `PUT` | `/api/v1/projects/{id}` | `ProjectController` | Authenticated (Staff) | CRM | `CreateProjectRequest` | `ApiResponse<ProjectResponse>` | ✅ Repaired |
| `DELETE` | `/api/v1/projects/{id}` | `ProjectController` | Authenticated (Staff) | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Repaired |
| `PATCH` | `/api/v1/projects/{id}/progress` | `ProjectController` | Authenticated (Staff) | CRM | `progressPercentage`, `status`, `updateNote` | `ApiResponse<ProjectResponse>` | ✅ Active |
| `GET` | `/api/v1/projects/{id}/milestones` | `ProjectController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<List<ProjectMilestoneResponse>>` | ✅ Active |
| `POST` | `/api/v1/projects/{id}/milestones` | `ProjectController` | Authenticated | CRM | `CreateProjectMilestoneRequest` | `ApiResponse<ProjectMilestoneResponse>` | ✅ Active |
| `PUT` | `/api/v1/projects/{id}/milestones/{mId}` | `ProjectController` | Authenticated | CRM | `CreateProjectMilestoneRequest` | `ApiResponse<ProjectMilestoneResponse>` | ✅ Active |
| `DELETE` | `/api/v1/projects/{id}/milestones/{mId}` | `ProjectController` | Authenticated | CRM | `id`, `mId` (path) | `ApiResponse<Void>` | ✅ Active |
| `GET` | `/api/v1/projects/{id}/tasks` | `ProjectController` | Authenticated | CRM | `id` (path) | `ApiResponse<List<ProjectTaskResponse>>` | ✅ Active |
| `POST` | `/api/v1/projects/{id}/tasks` | `ProjectController` | Authenticated | CRM | `CreateProjectTaskRequest` | `ApiResponse<ProjectTaskResponse>` | ✅ Active |
| `PUT` | `/api/v1/projects/{id}/tasks/{tId}` | `ProjectController` | Authenticated | CRM | `CreateProjectTaskRequest` | `ApiResponse<ProjectTaskResponse>` | ✅ Active |
| `DELETE` | `/api/v1/projects/{id}/tasks/{tId}` | `ProjectController` | Authenticated | CRM | `id`, `tId` (path) | `ApiResponse<Void>` | ✅ Active |
| `GET` | `/api/v1/projects/{id}/comments` | `ProjectController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<List<ProjectCommentResponse>>` | ✅ Repaired |
| `POST` | `/api/v1/projects/{id}/comments` | `ProjectController` | Authenticated | CRM / Portal | `CreateProjectCommentRequest` | `ApiResponse<ProjectCommentResponse>` | ✅ Repaired |
| `GET` | `/api/v1/projects/dashboard` | `ProjectController` | Authenticated | CRM | None | `ApiResponse<ProjectDashboardResponse>` | ✅ Active |

---

## 5. Finance: Invoices, Quotations, Expenses & Reports (`com.webliix.finance`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/invoices` | `InvoiceController` | Authenticated | CRM / Portal | `page`, `size` | `ApiResponse<Page<InvoiceResponse>>` | ✅ Active |
| `GET` | `/api/v1/invoices/{id}` | `InvoiceController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<InvoiceResponse>` | ✅ Active |
| `POST` | `/api/v1/invoices` | `InvoiceController` | Authenticated | CRM | `CreateInvoiceRequest` | `ApiResponse<InvoiceResponse>` | ✅ Active |
| `PUT` | `/api/v1/invoices/{id}` | `InvoiceController` | Authenticated | CRM | `CreateInvoiceRequest` | `ApiResponse<InvoiceResponse>` | ✅ Active |
| `GET` | `/api/v1/invoices/dashboard` | `InvoiceController` | Authenticated | CRM | None | `ApiResponse<InvoiceDashboardResponse>` | ✅ Active |
| `GET` | `/api/v1/invoices/{id}/payments` | `PaymentController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<List<PaymentResponse>>` | ✅ Active |
| `POST` | `/api/v1/invoices/{id}/payments` | `PaymentController` | Authenticated | CRM / Portal | `PaymentRequest` | `ApiResponse<PaymentResponse>` | ✅ Active |
| `GET` | `/api/v1/quotations` | `QuotationController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<QuotationResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/quotations/{id}` | `QuotationController` | Authenticated | CRM | `id` (path) | `ApiResponse<QuotationResponse>` | ✅ Active |
| `POST` | `/api/v1/quotations` | `QuotationController` | Authenticated | CRM | `CreateQuotationRequest` | `ApiResponse<QuotationResponse>` | ✅ Active |
| `PUT` | `/api/v1/quotations/{id}` | `QuotationController` | Authenticated | CRM | `CreateQuotationRequest` | `ApiResponse<QuotationResponse>` | ✅ Active |
| `POST` | `/api/v1/quotations/{id}/convert` | `QuotationController` | Authenticated | CRM | `id` (path) | `ApiResponse<InvoiceResponse>` | ✅ Active |
| `GET` | `/api/v1/expenses` | `ExpenseController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<ExpenseResponse>>` | ✅ Active |
| `GET` | `/api/v1/expenses/{id}` | `ExpenseController` | Authenticated | CRM | `id` (path) | `ApiResponse<ExpenseResponse>` | ✅ Active |
| `POST` | `/api/v1/expenses` | `ExpenseController` | Authenticated | CRM | `CreateExpenseRequest` | `ApiResponse<ExpenseResponse>` | ✅ Active |
| `PUT` | `/api/v1/expenses/{id}` | `ExpenseController` | Authenticated | CRM | `CreateExpenseRequest` | `ApiResponse<ExpenseResponse>` | ✅ Active |
| `DELETE` | `/api/v1/expenses/{id}` | `ExpenseController` | Authenticated | CRM | `id` (path) | `ApiResponse<Void>` | ✅ Active |
| `GET` | `/api/v1/reports/revenue` | `ReportController` | Authenticated | CRM | None | `ApiResponse<RevenueStatisticsResponse>` | ✅ Active |
| `GET` | `/api/v1/reports/profit` | `ReportController` | Authenticated | CRM | None | `ApiResponse<ProfitResponse>` | ✅ Active |
| `GET` | `/api/v1/reports/revenue/monthly` | `ReportController` | Authenticated | CRM | None | `ApiResponse<List<MonthlyRevenueResponse>>` | ✅ Active |

---

## 6. HR, Payroll & Attendance (`com.webliix.hr.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/employees` | `EmployeeController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<EmployeeResponse>>` | ✅ Active |
| `GET` | `/api/v1/employees/{id}` | `EmployeeController` | Authenticated | CRM | `id` (path) | `ApiResponse<EmployeeResponse>` | ✅ Active |
| `POST` | `/api/v1/employees` | `EmployeeController` | Authenticated | CRM | `CreateEmployeeRequest` | `ApiResponse<EmployeeResponse>` | ✅ Active |
| `PUT` | `/api/v1/employees/{id}` | `EmployeeController` | Authenticated | CRM | `CreateEmployeeRequest` | `ApiResponse<EmployeeResponse>` | ✅ Active |
| `GET` | `/api/v1/employees/statistics` | `EmployeeController` | Authenticated | CRM | None | `ApiResponse<EmployeeStatsResponse>` | ✅ Active |
| `GET` | `/api/v1/departments` | `DepartmentController` | Authenticated | CRM | None | `ApiResponse<List<DepartmentResponse>>` | ✅ Active |
| `POST` | `/api/v1/departments` | `DepartmentController` | Authenticated | CRM | `CreateDepartmentRequest` | `ApiResponse<DepartmentResponse>` | ✅ Active |
| `GET` | `/api/v1/designations` | `DesignationController` | Authenticated | CRM | None | `ApiResponse<List<DesignationResponse>>` | ✅ Active |
| `POST` | `/api/v1/designations` | `DesignationController` | Authenticated | CRM | `CreateDesignationRequest` | `ApiResponse<DesignationResponse>` | ✅ Active |
| `GET` | `/api/v1/payrolls` | `PayrollController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<PayrollResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/payrolls/{id}` | `PayrollController` | Authenticated | CRM | `id` (path) | `ApiResponse<PayrollResponse>` | ✅ Active |
| `POST` | `/api/v1/payrolls/generate` | `PayrollController` | Authenticated | CRM | `month`, `year` | `ApiResponse<List<PayrollResponse>>` | ✅ Active |
| `GET` | `/api/v1/payrolls/dashboard` | `PayrollController` | Authenticated | CRM | None | `ApiResponse<PayrollDashboardResponse>` | ✅ Active |
| `GET` | `/api/v1/attendances` | `AttendanceController` | Authenticated | CRM | `employeeId`, `date` | `ApiResponse<List<AttendanceResponse>>` | ✅ Active |
| `POST` | `/api/v1/attendances/check-in` | `AttendanceController` | Authenticated | CRM / Mobile | `CheckInRequest` | `ApiResponse<AttendanceResponse>` | ✅ Active |
| `POST` | `/api/v1/attendances/check-out` | `AttendanceController` | Authenticated | CRM / Mobile | `CheckOutRequest` | `ApiResponse<AttendanceResponse>` | ✅ Active |
| `GET` | `/api/v1/leaves` | `LeaveController` | Authenticated | CRM | `page`, `size` | `ApiResponse<Page<LeaveResponse>>` | ✅ Active |
| `POST` | `/api/v1/leaves` | `LeaveController` | Authenticated | CRM | `CreateLeaveRequest` | `ApiResponse<LeaveResponse>` | ✅ Active |

---

## 7. Support Tickets & Helpdesk (`com.webliix.tickets.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/tickets` | `TicketController` | Authenticated | CRM / Portal | `page`, `size` | `ApiResponse<Page<TicketResponse>>` | ✅ Active |
| `GET` | `/api/v1/tickets/{id}` | `TicketController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<TicketResponse>` | ✅ Active |
| `POST` | `/api/v1/tickets` | `TicketController` | Authenticated | CRM / Portal | `CreateTicketRequest` | `ApiResponse<TicketResponse>` | ✅ Active |
| `PUT` | `/api/v1/tickets/{id}` | `TicketController` | Authenticated | CRM | `UpdateTicketRequest` | `ApiResponse<TicketResponse>` | ✅ Active |
| `GET` | `/api/v1/tickets/{id}/comments` | `TicketController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<List<TicketCommentResponse>>` | ✅ Active |
| `POST` | `/api/v1/tickets/{id}/comments` | `TicketController` | Authenticated | CRM / Portal | `CreateTicketCommentRequest` | `ApiResponse<TicketCommentResponse>` | ✅ Active |
| `GET` | `/api/v1/tickets/dashboard` | `TicketController` | Authenticated | CRM | None | `ApiResponse<TicketDashboardResponse>` | ✅ Active |
| `POST` | `/api/v1/public/tickets` | `PublicTicketController` | None (Public) | Website Helpdesk | `PublicTicketRequest` | `ApiResponse<TicketResponse>` | ✅ Active |

---

## 8. Storage, Document Vault & Files (`com.webliix.storage.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/storage/upload` | `StorageController` | Authenticated | CRM / Portal | `file` (multipart), `module`, `category`, `referenceId` | `ApiResponse<StoredFileResponse>` | ✅ Repaired |
| `GET` | `/api/v1/storage/files` | `StorageController` | Authenticated | CRM / Portal | None | `ApiResponse<List<StoredFileResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/storage/module/{module}/{refId}` | `StorageController` | Authenticated | CRM / Portal | `module`, `refId` (path) | `ApiResponse<List<StoredFileResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/storage/download/{fileId}` | `StorageController` | Authenticated | CRM / Portal | `fileId` (path) | Binary Resource Stream | ✅ Repaired |

---

## 9. Audit Logs & Security Tracing (`com.webliix.audit.controller`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/audit/logs` | `AuditController` | ADMIN / SUPER_ADMIN | CRM | `module`, `action`, `status`, `page`, `size` | `ApiResponse<Page<AuditLogResponse>>` | ✅ Repaired |
| `POST` | `/api/v1/audit/logs` | `AuditController` | ADMIN / SUPER_ADMIN | CRM | `AuditSearchRequest` | `ApiResponse<Page<AuditLogResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/audit` | `AuditController` | ADMIN / SUPER_ADMIN | CRM | `AuditSearchRequest` (params) | `ApiResponse<Page<AuditLogResponse>>` | ✅ Repaired |
| `POST` | `/api/v1/audit` | `AuditController` | ADMIN / SUPER_ADMIN | CRM | `AuditSearchRequest` (body) | `ApiResponse<Page<AuditLogResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/audit/dashboard` | `AuditController` | ADMIN / SUPER_ADMIN | CRM | None | `ApiResponse<AuditDashboardResponse>` | ✅ Repaired |
| `GET` | `/api/v1/users/{id}/activity` | `AuditController` | ADMIN / SUPER_ADMIN | CRM | `id` (path) | `ApiResponse<List<UserActivityResponse>>` | ✅ Repaired |

---

## 10. Automations & Notifications (`com.webliix.automations`, `com.webliix.notifications`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/automation/rules` | `AutomationController` | Authenticated | CRM | None | `ApiResponse<List<AutomationRuleResponse>>` | ✅ Repaired |
| `GET` | `/api/v1/automation/rules/{id}` | `AutomationController` | Authenticated | CRM | `id` (path) | `ApiResponse<AutomationRuleResponse>` | ✅ Active |
| `POST` | `/api/v1/automation/rules` | `AutomationController` | Authenticated | CRM | `CreateAutomationRuleRequest` | `ApiResponse<AutomationRuleResponse>` | ✅ Active |
| `PUT` | `/api/v1/automation/rules/{id}` | `AutomationController` | Authenticated | CRM | `UpdateAutomationRuleRequest` | `ApiResponse<AutomationRuleResponse>` | ✅ Active |
| `PATCH` | `/api/v1/automation/rules/{id}/toggle` | `AutomationController` | Authenticated | CRM | `id` (path) | `ApiResponse<AutomationRuleResponse>` | ✅ Repaired |
| `GET` | `/api/v1/notifications` | `NotificationController` | Authenticated | CRM / Portal | `unreadOnly`, `page`, `size` | `ApiResponse<Page<NotificationResponse>>` | ✅ Active |
| `PATCH` | `/api/v1/notifications/{id}/read` | `NotificationController` | Authenticated | CRM / Portal | `id` (path) | `ApiResponse<NotificationResponse>` | ✅ Active |
| `PATCH` | `/api/v1/notifications/read-all` | `NotificationController` | Authenticated | CRM / Portal | None | `ApiResponse<Void>` | ✅ Active |

---

## 11. System Settings & Monitoring (`com.webliix.system`, `com.webliix.monitoring`)

| HTTP Method | Route | Controller | Auth Required | Consumer | Request / Params | Response Structure | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/settings` | `SystemSettingController` | Authenticated | CRM | None | `ApiResponse<List<SystemSettingResponse>>` | ✅ Active |
| `POST` | `/api/v1/settings` | `SystemSettingController` | Authenticated (Admin) | CRM | `SystemSettingRequest` | `ApiResponse<SystemSettingResponse>` | ✅ Active |
| `GET` | `/api/v1/monitoring/health` | `MonitoringController` | Authenticated (Admin) | CRM | None | `ApiResponse<SystemHealthResponse>` | ✅ Active |
| `GET` | `/api/v1/monitoring/metrics` | `MonitoringController` | Authenticated (Admin) | CRM | None | `ApiResponse<TenantMetricsResponse>` | ✅ Active |
