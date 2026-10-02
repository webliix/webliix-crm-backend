# Webliix Database to UI Screen Coverage Report

This report cross-references every database table in the PostgreSQL database (52 versioned Flyway migrations) against its Spring Boot JPA Entity, REST Controller, and frontend UI interface in the CRM and Client Portal.

---

## 1. Complete Coverage Matrix

| Table Name | JPA Entity | Spring Boot Controller | Primary CRUD Endpoints | CRM Screen | Client Portal Screen | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `leads` | `Lead` | `LeadController`, `PublicLeadController` | `/api/v1/leads`, `/api/v1/public/leads` | `/leads`, `/leads/create`, `/leads/:id/edit`, `LeadDetailsDrawer` | N/A (Public Lead Form) | ✅ Complete |
| `audit_logs` | `AuditLog` | `AuditController` | `/api/v1/audit/logs`, `/api/v1/audit/dashboard` | `/audit` (`AuditLogsPage.tsx`) | N/A (Staff Only) | ✅ Complete |
| `customers` | `Customer` | `CustomerController` | `/api/v1/customers`, `/api/v1/customers/statistics` | `/customers` (`CustomerListPage.tsx`, `CustomerDetailsDrawer.tsx`) | `/portal` (Own Account) | ✅ Complete |
| `customer_contacts` | `CustomerContact` | `CustomerController` | `/api/v1/customers/{id}/contacts` | `CustomerDetailsDrawer.tsx` | N/A | ✅ Complete |
| `customer_notes` | `CustomerNote` | `CustomerController` | `/api/v1/customers/{id}/notes` | `CustomerDetailsDrawer.tsx` | N/A (Private CRM) | ✅ Complete |
| `projects` | `Project` | `ProjectController` | `/api/v1/projects` (supports `customerId`) | `/projects` (`ProjectListPage.tsx`), `/projects/:id` (`ProjectDetailPage.tsx`) | `/portal` (Active Deliverables tab) | ✅ Complete |
| `project_milestones`| `ProjectMilestone` | `ProjectController` | `/api/v1/projects/{id}/milestones` | `ProjectDetailPage.tsx` (Milestones tab) | `/portal` (Milestone progress) | ✅ Complete |
| `project_tasks` | `ProjectTask` | `ProjectController` | `/api/v1/projects/{id}/tasks` | `ProjectDetailPage.tsx` (Tasks Kanban/List) | N/A | ✅ Complete |
| `project_members` | `ProjectMember` | `ProjectController` | `/api/v1/projects/{id}/members` | `ProjectDetailPage.tsx` (Team tab) | N/A | ✅ Complete |
| `project_comments`| `ProjectComment` | `ProjectController` | `/api/v1/projects/{id}/comments` | `ProjectDetailPage.tsx` (Live Chat & Updates) | `/projects/:id` (Client view) | ✅ Complete |
| `invoices` | `Invoice` | `InvoiceController` | `/api/v1/invoices`, `/api/v1/invoices/dashboard` | `/invoices` (`InvoiceListPage.tsx`) | `/portal` (Billing Overview) | ✅ Complete |
| `invoice_items` | `InvoiceItem` | `InvoiceController` | `/api/v1/invoices/{id}` | `InvoiceDetailPage.tsx` | `/portal` (Invoice Specs) | ✅ Complete |
| `payments` | `Payment` | `PaymentController` | `/api/v1/invoices/{id}/payments` | `InvoiceDetailPage.tsx` (Payments tab) | `/portal` (Payment Receipts) | ✅ Complete |
| `quotations` | `Quotation` | `QuotationController` | `/api/v1/quotations`, `/api/v1/quotations/{id}/convert` | `/quotations` (`QuotationListPage.tsx`) | `/portal` (Proposals) | ✅ Complete |
| `quotation_items` | `QuotationItem` | `QuotationController` | `/api/v1/quotations/{id}` | `QuotationListPage.tsx` | `/portal` | ✅ Complete |
| `expenses` | `Expense` | `ExpenseController` | `/api/v1/expenses` | `/expenses` (`ExpenseListPage.tsx`) | N/A (Staff Only) | ✅ Complete |
| `employees` | `Employee` | `EmployeeController` | `/api/v1/employees`, `/api/v1/employees/statistics` | `/hr/employees` (`EmployeeListPage.tsx`) | N/A (Staff Only) | ✅ Complete |
| `departments` | `Department` | `DepartmentController` | `/api/v1/departments` | `EmployeeListPage.tsx`, `SettingsPage.tsx` | N/A | ✅ Complete |
| `designations` | `Designation` | `DesignationController` | `/api/v1/designations` | `EmployeeListPage.tsx`, `SettingsPage.tsx` | N/A | ✅ Complete |
| `payrolls` | `Payroll` | `PayrollController` | `/api/v1/payrolls`, `/api/v1/payrolls/generate` | `/hr/payroll` (`PayrollListPage.tsx`) | N/A (Staff Only) | ✅ Complete |
| `attendance` | `Attendance` | `AttendanceController` | `/api/v1/attendances`, `/api/v1/attendances/check-in` | `EmployeeListPage.tsx` | N/A | ✅ Complete |
| `leave_requests` | `LeaveRequest` | `LeaveController` | `/api/v1/leaves` | `EmployeeListPage.tsx` | N/A | ✅ Complete |
| `tickets` | `Ticket` | `TicketController`, `PublicTicketController` | `/api/v1/tickets`, `/api/v1/public/tickets` | `/tickets` (`TicketListPage.tsx`, `TicketDetailsDrawer.tsx`) | `/portal` (Live Support Helpdesk tab) | ✅ Complete |
| `ticket_comments` | `TicketComment` | `TicketController` | `/api/v1/tickets/{id}/comments` | `TicketDetailsDrawer.tsx` (Live Chat) | `TicketDetailsDrawer.tsx` in `/portal` | ✅ Complete |
| `ticket_attachments`| `TicketAttachment` | `TicketController` | `/api/v1/tickets/{id}/attachments` | `TicketDetailsDrawer.tsx` | `TicketDetailsDrawer.tsx` in `/portal` | ✅ Complete |
| `stored_files` | `StoredFile` | `StorageController` | `/api/v1/storage/files`, `/api/v1/storage/upload` | `CustomerDetailsDrawer.tsx`, `DocumentExplorer.tsx` | `/portal` (Document Library tab) | ✅ Complete |
| `notifications` | `Notification` | `NotificationController` | `/api/v1/notifications`, `/api/v1/notifications/read-all` | `NotificationDrawer.tsx`, `DashboardHeader.tsx` | `NotificationDrawer.tsx` in `/portal` | ✅ Complete |
| `automation_rules`| `AutomationRule` | `AutomationController` | `/api/v1/automation/rules`, `/api/v1/automation/rules/{id}/toggle` | `/automations` (`AutomationsListPage.tsx`) | N/A (Staff Only) | ✅ Complete |
| `automation_executions`| `AutomationExecution` | `AutomationController` | `/api/v1/automation/rules/{id}/executions` | `/automations` | N/A | ✅ Complete |
| `system_settings` | `SystemSetting` | `SystemSettingController` | `/api/v1/settings` | `/settings` (`SettingsPage.tsx`) | N/A (Admin Only) | ✅ Complete |
| `users` | `User` | `UserController` | `/api/v1/users` | `/system/users` (`UserListPage.tsx`) | `/profile` (ProfilePage.tsx) | ✅ Complete |
| `roles` | `Role` | `UserController` | `/api/v1/users/roles` | `/system/users` | N/A | ✅ Complete |
| `blog_posts` | `BlogPost` | `BlogController`, `PublicBlogController` | `/api/v1/blog`, `/api/v1/public/blogs` | `/blog` (`BlogListPage.tsx`) | Public `/blog`, `/blog/:slug` | ✅ Complete |
| `newsletter_subscribers`| `NewsletterSubscriber` | `NewsletterController`, `PublicNewsletterController` | `/api/v1/newsletter`, `/api/v1/public/newsletter` | `/newsletter` (`SubscriberListPage.tsx`) | Public Footer Subscribe | ✅ Complete |
| `reviews` | `Review` | `ReviewController`, `PublicReviewController` | `/api/v1/reviews`, `/api/v1/public/reviews` | `/reviews` (`ReviewListPage.tsx`) | Public Reviews & Ratings | ✅ Complete |

---

## 2. Unification Summary
Every major business table now has a direct, functional, and fully-typed interface in either the internal CRM application, the external client portal, or public pages, backed by Spring Boot REST controllers.
