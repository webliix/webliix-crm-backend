-- V62: Implement Database-Driven RBAC with Data Scoping and Permissions Catalog

-- 1. Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    module VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_permissions_module ON permissions(module);
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);

-- 2. Enhance existing roles table
ALTER TABLE roles ADD COLUMN IF NOT EXISTS display_name VARCHAR(100);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_system BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS is_super_admin BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS default_department_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_roles_department'
    ) THEN
        ALTER TABLE roles
            ADD CONSTRAINT fk_roles_department
            FOREIGN KEY (default_department_id) REFERENCES departments(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 3. Create role_permissions table with scope (OWN, DEPARTMENT, ALL)
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    scope VARCHAR(20) NOT NULL DEFAULT 'OWN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX IF NOT EXISTS idx_role_permissions_perm ON role_permissions(permission_id);

-- 4. Ensure user_roles has unique constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_user_roles_user_role'
    ) THEN
        ALTER TABLE user_roles ADD CONSTRAINT uq_user_roles_user_role UNIQUE (user_id, role_id);
    END IF;
EXCEPTION WHEN OTHERS THEN
    -- Ignore if duplicate rows already existed or constraint exists under another name
    NULL;
END $$;

-- 5. Create user_permission_overrides table (GRANT or DENY with scope and optional expiry)
CREATE TABLE IF NOT EXISTS user_permission_overrides (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    effect VARCHAR(10) NOT NULL, -- GRANT or DENY
    scope VARCHAR(20) NOT NULL DEFAULT 'OWN',
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_permission_override UNIQUE (user_id, permission_id),
    CONSTRAINT fk_user_permission_overrides_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_permission_overrides_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_perm_overrides_user ON user_permission_overrides(user_id);

-- 6. Create menu_items table for server-side dynamic navigation
CREATE TABLE IF NOT EXISTS menu_items (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(100) NOT NULL,
    path VARCHAR(255) NOT NULL,
    icon VARCHAR(100),
    module VARCHAR(50) NOT NULL,
    required_permission VARCHAR(100),
    sort_order INT NOT NULL DEFAULT 0,
    parent_key VARCHAR(100) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_menu_items_sort ON menu_items(sort_order);

-- 7. Add token_version to users for instant session invalidation
ALTER TABLE users ADD COLUMN IF NOT EXISTS token_version INT NOT NULL DEFAULT 0;

-- 8. Add ownership / creator columns to core entities
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_expenses_created_by_user') THEN
        ALTER TABLE expenses ADD CONSTRAINT fk_expenses_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

ALTER TABLE invoices ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_invoices_created_by_user') THEN
        ALTER TABLE invoices ADD CONSTRAINT fk_invoices_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

ALTER TABLE quotations ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_quotations_created_by_user') THEN
        ALTER TABLE quotations ADD CONSTRAINT fk_quotations_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

ALTER TABLE payments ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_payments_created_by_user') THEN
        ALTER TABLE payments ADD CONSTRAINT fk_payments_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

ALTER TABLE leads ADD COLUMN IF NOT EXISTS assigned_to BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_leads_assigned_to_user') THEN
        ALTER TABLE leads ADD CONSTRAINT fk_leads_assigned_to_user FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

ALTER TABLE customers ADD COLUMN IF NOT EXISTS owner_id BIGINT;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_customers_owner_user') THEN
        ALTER TABLE customers ADD CONSTRAINT fk_customers_owner_user FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Backfill created_by_user_id from users matching email/name if any
UPDATE expenses e
SET created_by_user_id = u.id
FROM users u
WHERE e.created_by_user_id IS NULL AND e.created_by = u.email;

-- 9. SEED PERMISSIONS CATALOG (Section 1.2)
INSERT INTO permissions (code, module, action, description) VALUES
-- Dashboard
('DASHBOARD_VIEW', 'Dashboard', 'VIEW', 'View dashboard metrics and operational overview'),

-- Expenses
('EXPENSES_VIEW', 'Expenses', 'VIEW', 'View business expense records'),
('EXPENSES_CREATE', 'Expenses', 'CREATE', 'Submit new expense reports'),
('EXPENSES_EDIT', 'Expenses', 'EDIT', 'Edit existing expense records'),
('EXPENSES_APPROVE', 'Expenses', 'APPROVE', 'Approve or reject pending expenses (maker-checker enforced)'),
('EXPENSES_PAY', 'Expenses', 'PAY', 'Mark approved expenses as paid'),
('EXPENSES_DELETE', 'Expenses', 'DELETE', 'Delete expense records'),
('EXPENSES_EXPORT', 'Expenses', 'EXPORT', 'Export expenses to CSV or spreadsheets'),

-- Invoices
('INVOICES_VIEW', 'Invoices', 'VIEW', 'View invoices and billing history'),
('INVOICES_CREATE', 'Invoices', 'CREATE', 'Generate new client invoices'),
('INVOICES_EDIT', 'Invoices', 'EDIT', 'Modify draft or issued invoices'),
('INVOICES_DELETE', 'Invoices', 'DELETE', 'Delete invoices'),
('INVOICES_SEND', 'Invoices', 'SEND', 'Send invoice notifications to clients'),
('INVOICES_EXPORT', 'Invoices', 'EXPORT', 'Export invoice lists and print official invoices'),

-- Payments
('PAYMENTS_VIEW', 'Payments', 'VIEW', 'View recorded payments and receipts'),
('PAYMENTS_RECORD', 'Payments', 'RECORD', 'Record new incoming payments against invoices'),
('PAYMENT_SUBMISSIONS_VIEW', 'Payments', 'VIEW', 'View employee-submitted client payments'),
('PAYMENT_SUBMISSIONS_SUBMIT', 'Payments', 'SUBMIT', 'Submit client payment proofs from field operations'),
('PAYMENT_SUBMISSIONS_REVIEW', 'Payments', 'REVIEW', 'Review and approve/reject employee payment submissions'),

-- Quotations
('QUOTATIONS_VIEW', 'Quotations', 'VIEW', 'View estimates and quotations'),
('QUOTATIONS_CREATE', 'Quotations', 'CREATE', 'Draft new sales quotations'),
('QUOTATIONS_EDIT', 'Quotations', 'EDIT', 'Modify existing quotations'),
('QUOTATIONS_DELETE', 'Quotations', 'DELETE', 'Delete quotations'),
('QUOTATIONS_CONVERT', 'Quotations', 'CONVERT', 'Convert accepted quotations into invoices'),

-- Billing / Plans
('BILLING_VIEW', 'Billing', 'VIEW', 'View corporate billing, subscription plans and financial health'),
('BILLING_MANAGE', 'Billing', 'MANAGE', 'Manage subscription plans and billing configurations'),

-- Reports
('REPORTS_FINANCE_VIEW', 'Reports', 'VIEW', 'View financial revenue, profit and balance reports'),
('REPORTS_SALES_VIEW', 'Reports', 'VIEW', 'View sales performance and conversion analytics'),
('REPORTS_HR_VIEW', 'Reports', 'VIEW', 'View human resources and workforce statistics'),
('REPORTS_PROJECTS_VIEW', 'Reports', 'VIEW', 'View project milestones and budget analytics'),
('REPORTS_EXPORT', 'Reports', 'EXPORT', 'Export comprehensive analytical reports'),

-- Leads
('LEADS_VIEW', 'Leads', 'VIEW', 'View CRM prospect leads'),
('LEADS_CREATE', 'Leads', 'CREATE', 'Create new incoming leads'),
('LEADS_EDIT', 'Leads', 'EDIT', 'Update lead status and communications'),
('LEADS_DELETE', 'Leads', 'DELETE', 'Delete prospect leads'),
('LEADS_CONVERT', 'Leads', 'CONVERT', 'Convert qualified leads to customers'),
('LEADS_ASSIGN', 'Leads', 'ASSIGN', 'Assign leads to sales representatives'),

-- Customers
('CUSTOMERS_VIEW', 'Customers', 'VIEW', 'View customer company directory'),
('CUSTOMERS_CREATE', 'Customers', 'CREATE', 'Add new customer accounts'),
('CUSTOMERS_EDIT', 'Customers', 'EDIT', 'Modify customer profiles and contacts'),
('CUSTOMERS_DELETE', 'Customers', 'DELETE', 'Delete or archive customer profiles'),

-- Projects
('PROJECTS_VIEW', 'Projects', 'VIEW', 'View client project portfolios and tasks'),
('PROJECTS_CREATE', 'Projects', 'CREATE', 'Create new projects and milestones'),
('PROJECTS_EDIT', 'Projects', 'EDIT', 'Update project progress, tasks and status'),
('PROJECTS_DELETE', 'Projects', 'DELETE', 'Delete projects'),
('PROJECTS_BILLING_VIEW', 'Projects', 'VIEW', 'View financial billing summaries of projects'),

-- Tickets
('TICKETS_VIEW', 'Tickets', 'VIEW', 'View customer support tickets'),
('TICKETS_CREATE', 'Tickets', 'CREATE', 'Open support tickets'),
('TICKETS_EDIT', 'Tickets', 'EDIT', 'Update support ticket status, priority and messages'),
('TICKETS_ASSIGN', 'Tickets', 'ASSIGN', 'Assign tickets to support representatives'),
('TICKETS_DELETE', 'Tickets', 'DELETE', 'Delete support tickets'),

-- Work logs
('WORKLOGS_SUBMIT', 'Work logs', 'SUBMIT', 'Submit daily work logs and activities'),
('WORKLOGS_VIEW', 'Work logs', 'VIEW', 'View daily work logs'),
('WORKLOGS_REVIEW', 'Work logs', 'REVIEW', 'Review and approve/reject employee work logs'),

-- HR core
('EMPLOYEES_VIEW', 'HR core', 'VIEW', 'View employee roster and profiles'),
('EMPLOYEES_MANAGE', 'HR core', 'MANAGE', 'Create, update and manage employee records'),
('EMPLOYEES_SALARY_VIEW', 'HR core', 'VIEW', 'View employee compensation and sensitive salary fields'),
('DEPARTMENTS_MANAGE', 'HR core', 'MANAGE', 'Manage corporate departments'),
('DESIGNATIONS_MANAGE', 'HR core', 'MANAGE', 'Manage corporate job designations'),

-- Attendance
('ATTENDANCE_VIEW', 'Attendance', 'VIEW', 'View employee attendance logs and timesheets'),
('ATTENDANCE_MANAGE', 'Attendance', 'MANAGE', 'Record and rectify employee attendance'),

-- Leave
('LEAVE_APPLY', 'Leave', 'APPLY', 'Apply for leave and time-off requests'),
('LEAVE_VIEW', 'Leave', 'VIEW', 'View leave requests and balances'),
('LEAVE_APPROVE', 'Leave', 'APPROVE', 'Approve or reject employee leave requests'),
('LEAVE_BALANCE_MANAGE', 'Leave', 'MANAGE', 'Configure employee annual leave quotas and balances'),

-- Payroll
('PAYROLL_VIEW', 'Payroll', 'VIEW', 'View company payroll sheets'),
('PAYROLL_GENERATE', 'Payroll', 'GENERATE', 'Generate monthly salary payroll'),
('PAYROLL_PAY', 'Payroll', 'PAY', 'Authorize payroll disbursement and bank export'),
('PAYSLIP_VIEW_OWN', 'Payroll', 'VIEW', 'View and download own monthly payslips'),

-- Marketing
('BLOGS_MANAGE', 'Marketing', 'MANAGE', 'Create, edit, publish and delete blog articles'),
('NEWSLETTER_MANAGE', 'Marketing', 'MANAGE', 'Manage newsletter subscribers and send broadcasts'),
('REVIEWS_MANAGE', 'Marketing', 'MANAGE', 'Moderate customer testimonials and public reviews'),
('OFFERS_MANAGE', 'Marketing', 'MANAGE', 'Manage promotional deals and landing page banners'),

-- Platform
('USERS_MANAGE', 'Platform', 'MANAGE', 'Create and modify user logins and assign roles'),
('USERS_DELETE', 'Platform', 'DELETE', 'Delete system user accounts'),
('ROLES_MANAGE', 'Platform', 'MANAGE', 'Create and configure roles and role permissions'),
('SETTINGS_VIEW', 'Platform', 'VIEW', 'View system settings and configurations'),
('SETTINGS_MANAGE', 'Platform', 'MANAGE', 'Modify system settings, SMTP, and integrations'),
('AUDIT_VIEW', 'Platform', 'VIEW', 'Inspect system audit trail and security activity logs'),
('AUTOMATION_MANAGE', 'Platform', 'MANAGE', 'Configure workflow automation rules and trigger webhooks'),
('STORAGE_UPLOAD', 'Platform', 'UPLOAD', 'Upload documents, receipts and media assets'),
('STORAGE_MANAGE', 'Platform', 'MANAGE', 'Delete and manage cloud storage assets'),
('API_TOKENS_MANAGE', 'Platform', 'MANAGE', 'Generate and revoke API keys and third-party tokens'),
('MONITORING_VIEW', 'Platform', 'VIEW', 'Monitor system health, cache and database performance'),
('NOTIFICATIONS_BROADCAST', 'Platform', 'BROADCAST', 'Dispatch system-wide notifications and announcements'),
('EMAIL_TEMPLATES_MANAGE', 'Platform', 'MANAGE', 'Edit automated outbound transactional email templates'),

-- Portal
('EMPLOYEE_PORTAL_ACCESS', 'Portal', 'ACCESS', 'Access employee self-service portal'),
('CLIENT_PORTAL_ACCESS', 'Portal', 'ACCESS', 'Access client self-service customer portal')
ON CONFLICT (code) DO NOTHING;

-- 10. SEED & ENHANCE ROLES (Section 1.3)
INSERT INTO roles (name, display_name, description, is_system, is_super_admin, active) VALUES
('SUPER_ADMIN', 'Super Administrator', 'Complete unrestricted system access across all modules and security settings', true, true, true),
('ADMIN', 'Administrator', 'Administrative authority across business operations, staff and platform modules', true, false, true),
('MANAGER', 'Department Manager', 'Manages department projects, employees, expenses and approvals', true, false, true),
('HR', 'HR Manager', 'Manages employee records, attendance, leaves, designations and payroll generation', true, false, true),
('ACCOUNTS', 'Accounts & Finance', 'Handles corporate expenses, billing, invoices, payments and financial reports', false, false, true),
('SALES', 'Sales Representative', 'Handles customer acquisition, leads, quotations, and sales analytics', false, false, true),
('SUPPORT', 'Support Agent', 'Resolves client support tickets, manages customer inquiries and documentation', false, false, true),
('MARKETING', 'Marketing Specialist', 'Manages blogs, newsletters, promotional offers and reviews', false, false, true),
('EMPLOYEE', 'Employee (Baseline)', 'Standard employee self-service access to work logs, attendance, leaves, payslips and assigned tasks', true, false, true),
('USER', 'Client Portal User', 'External customer account with access to client portal projects, invoices and tickets', true, false, true)
ON CONFLICT (name) DO UPDATE SET
    display_name = EXCLUDED.display_name,
    description = EXCLUDED.description,
    is_system = EXCLUDED.is_system,
    is_super_admin = EXCLUDED.is_super_admin,
    active = EXCLUDED.active;

-- Ensure CLIENT alias exists if referenced
INSERT INTO roles (name, display_name, description, is_system, is_super_admin, active)
VALUES ('CLIENT', 'Client Portal User', 'External client portal account', true, false, true)
ON CONFLICT (name) DO NOTHING;

-- 11. SEED DEFAULT ROLE PERMISSIONS (Section 1.3 Matrix)

-- Helper DO block to map role permissions cleanly and idempotently
DO $$
DECLARE
    v_role_id BIGINT;
    v_perm_id BIGINT;
    v_client_role_id BIGINT;
    r RECORD;
BEGIN
    -- === 11.A ACCOUNTS ROLE ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'ACCOUNTS';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY[
                'EXPENSES_VIEW', 'EXPENSES_APPROVE', 'EXPENSES_PAY', 'EXPENSES_EXPORT',
                'INVOICES_VIEW', 'INVOICES_CREATE', 'INVOICES_EDIT', 'INVOICES_SEND', 'INVOICES_EXPORT',
                'PAYMENTS_VIEW', 'PAYMENTS_RECORD', 'PAYMENT_SUBMISSIONS_VIEW', 'PAYMENT_SUBMISSIONS_REVIEW',
                'BILLING_VIEW', 'BILLING_MANAGE', 'QUOTATIONS_VIEW', 'REPORTS_FINANCE_VIEW', 'REPORTS_SALES_VIEW',
                'CUSTOMERS_VIEW', 'LEADS_VIEW', 'PROJECTS_VIEW', 'PROJECTS_BILLING_VIEW',
                'EMPLOYEES_SALARY_VIEW', 'PAYROLL_VIEW', 'PAYROLL_PAY', 'DASHBOARD_VIEW'
            ]) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY['EXPENSES_CREATE', 'PAYSLIP_VIEW_OWN']) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.B SALES ROLE ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'SALES';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY[
                'QUOTATIONS_VIEW', 'QUOTATIONS_CREATE', 'QUOTATIONS_EDIT', 'QUOTATIONS_CONVERT',
                'REPORTS_SALES_VIEW', 'CUSTOMERS_VIEW', 'CUSTOMERS_CREATE', 'CUSTOMERS_EDIT',
                'LEADS_VIEW', 'LEADS_CREATE', 'LEADS_EDIT', 'LEADS_CONVERT', 'LEADS_ASSIGN',
                'PROJECTS_VIEW', 'TICKETS_VIEW', 'DASHBOARD_VIEW'
            ]) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY['EXPENSES_CREATE', 'EXPENSES_VIEW', 'INVOICES_VIEW', 'PAYSLIP_VIEW_OWN']) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.C HR ROLE ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'HR';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY[
                'EMPLOYEES_VIEW', 'EMPLOYEES_MANAGE', 'EMPLOYEES_SALARY_VIEW',
                'DEPARTMENTS_MANAGE', 'DESIGNATIONS_MANAGE', 'ATTENDANCE_VIEW', 'ATTENDANCE_MANAGE',
                'LEAVE_VIEW', 'LEAVE_APPROVE', 'LEAVE_BALANCE_MANAGE',
                'PAYROLL_VIEW', 'PAYROLL_GENERATE', 'WORKLOGS_VIEW', 'WORKLOGS_REVIEW',
                'REPORTS_HR_VIEW', 'DASHBOARD_VIEW'
            ]) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY['EXPENSES_CREATE', 'EXPENSES_VIEW', 'LEAVE_APPLY', 'PAYSLIP_VIEW_OWN']) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.D MANAGER ROLE ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'MANAGER';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY['PROJECTS_VIEW', 'PROJECTS_CREATE', 'PROJECTS_EDIT', 'DASHBOARD_VIEW']) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY[
                'EXPENSES_VIEW', 'EXPENSES_APPROVE', 'INVOICES_VIEW', 'QUOTATIONS_VIEW',
                'REPORTS_SALES_VIEW', 'REPORTS_PROJECTS_VIEW', 'CUSTOMERS_VIEW', 'LEADS_VIEW',
                'EMPLOYEES_VIEW', 'ATTENDANCE_VIEW', 'LEAVE_VIEW', 'LEAVE_APPROVE',
                'TICKETS_VIEW', 'TICKETS_CREATE', 'TICKETS_EDIT', 'TICKETS_ASSIGN',
                'WORKLOGS_VIEW', 'WORKLOGS_REVIEW'
            ]) AS code, 'DEPARTMENT' AS scope
            UNION ALL
            SELECT unnest(ARRAY['EXPENSES_CREATE', 'LEAVE_APPLY', 'PAYSLIP_VIEW_OWN']) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.E SUPPORT ROLE ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'SUPPORT';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY[
                'TICKETS_VIEW', 'TICKETS_CREATE', 'TICKETS_EDIT', 'TICKETS_ASSIGN', 'DASHBOARD_VIEW'
            ]) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY['EXPENSES_CREATE', 'EXPENSES_VIEW', 'CUSTOMERS_VIEW', 'PAYSLIP_VIEW_OWN']) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.F MARKETING ROLE ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'MARKETING';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY[
                'BLOGS_MANAGE', 'NEWSLETTER_MANAGE', 'REVIEWS_MANAGE', 'OFFERS_MANAGE',
                'LEADS_VIEW', 'LEADS_CREATE', 'CUSTOMERS_VIEW', 'DASHBOARD_VIEW'
            ]) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY['EXPENSES_CREATE', 'EXPENSES_VIEW', 'PAYSLIP_VIEW_OWN']) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.G EMPLOYEE ROLE (BASELINE) ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'EMPLOYEE';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT unnest(ARRAY['EMPLOYEE_PORTAL_ACCESS']) AS code, 'ALL' AS scope
            UNION ALL
            SELECT unnest(ARRAY[
                'EXPENSES_CREATE', 'EXPENSES_VIEW',
                'PAYMENT_SUBMISSIONS_SUBMIT', 'PAYMENT_SUBMISSIONS_VIEW',
                'WORKLOGS_SUBMIT', 'WORKLOGS_VIEW',
                'ATTENDANCE_VIEW', 'LEAVE_APPLY', 'LEAVE_VIEW', 'PAYSLIP_VIEW_OWN',
                'PROJECTS_VIEW', 'TICKETS_VIEW', 'TICKETS_CREATE'
            ]) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

    -- === 11.H ADMIN ROLE (ALL PERMISSIONS WITH 'ALL' SCOPE EXCEPT SUPER ADMIN OVERRIDES) ===
    SELECT id INTO v_role_id FROM roles WHERE name = 'ADMIN';
    IF v_role_id IS NOT NULL THEN
        FOR r IN
            SELECT code, 'ALL' AS scope
            FROM permissions
            WHERE code NOT IN ('CLIENT_PORTAL_ACCESS')
        LOOP
            SELECT id INTO v_perm_id FROM permissions WHERE code = r.code;
            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END IF;

   
    -- === 11.I USER / CLIENT ROLE ===
    FOR v_client_role_id IN
        SELECT id FROM roles WHERE name IN ('USER', 'CLIENT')
    LOOP
        FOR r IN
            SELECT unnest(ARRAY[
                'CLIENT_PORTAL_ACCESS',
                'INVOICES_VIEW',
                'QUOTATIONS_VIEW',
                'PROJECTS_VIEW',
                'TICKETS_VIEW',
                'TICKETS_CREATE'
            ]) AS code, 'OWN' AS scope
        LOOP
            SELECT id INTO v_perm_id
            FROM permissions
            WHERE code = r.code;

            IF v_perm_id IS NOT NULL THEN
                INSERT INTO role_permissions (role_id, permission_id, scope)
                VALUES (v_client_role_id, v_perm_id, r.scope)
                ON CONFLICT (role_id, permission_id)
                DO UPDATE SET scope = EXCLUDED.scope;
            END IF;
        END LOOP;
    END LOOP;
END $$;

-- 12. SEED MENU ITEMS (Dynamic Navigation Catalog)
INSERT INTO menu_items (key, label, path, icon, module, required_permission, sort_order) VALUES
('dashboard', 'Dashboard', '/dashboard', 'DashboardOutlined', 'Dashboard', 'DASHBOARD_VIEW', 1),
('leads', 'Leads', '/leads', 'ContactPhoneOutlined', 'Leads', 'LEADS_VIEW', 2),
('customers', 'Customers', '/customers', 'PeopleOutlined', 'Customers', 'CUSTOMERS_VIEW', 3),
('projects', 'Projects', '/projects', 'FolderSpecialOutlined', 'Projects', 'PROJECTS_VIEW', 4),
('invoices', 'Invoices & Billing', '/invoices', 'ReceiptLongOutlined', 'Invoices', 'INVOICES_VIEW', 5),
('quotations', 'Quotations', '/quotations', 'RequestQuoteOutlined', 'Quotations', 'QUOTATIONS_VIEW', 6),
('expenses', 'Expenses', '/expenses', 'AccountBalanceWalletOutlined', 'Expenses', 'EXPENSES_VIEW', 7),
('tickets', 'Support Tickets', '/tickets', 'ConfirmationNumberOutlined', 'Tickets', 'TICKETS_VIEW', 8),
('employees', 'Employees & HR', '/employees', 'BadgeOutlined', 'HR core', 'EMPLOYEES_VIEW', 9),
('payrolls', 'Payroll', '/payrolls', 'PaymentsOutlined', 'Payroll', 'PAYROLL_VIEW', 10),
('reports', 'Reports & Analytics', '/reports', 'BarChartOutlined', 'Reports', 'REPORTS_FINANCE_VIEW', 11),
('automations', 'Automations', '/automations', 'SmartToyOutlined', 'Platform', 'AUTOMATION_MANAGE', 12),
('blogs', 'Blog Management', '/blogs', 'ArticleOutlined', 'Marketing', 'BLOGS_MANAGE', 13),
('newsletter', 'Newsletter', '/newsletter', 'MarkEmailReadOutlined', 'Marketing', 'NEWSLETTER_MANAGE', 14),
('reviews', 'Reviews', '/reviews', 'StarRateOutlined', 'Marketing', 'REVIEWS_MANAGE', 15),
('users', 'User Management', '/users', 'ManageAccountsOutlined', 'Platform', 'USERS_MANAGE', 16),
('roles', 'Roles & Permissions', '/roles', 'AdminPanelSettingsOutlined', 'Platform', 'ROLES_MANAGE', 17),
('audit', 'Audit Logs', '/audit-logs', 'HistoryOutlined', 'Platform', 'AUDIT_VIEW', 18),
('settings', 'Settings', '/settings', 'SettingsOutlined', 'Platform', 'SETTINGS_VIEW', 19)
ON CONFLICT (key) DO UPDATE SET
    label = EXCLUDED.label,
    path = EXCLUDED.path,
    icon = EXCLUDED.icon,
    module = EXCLUDED.module,
    required_permission = EXCLUDED.required_permission,
    sort_order = EXCLUDED.sort_order;
