-- V66: Add AUDIT_DELETE permission, grant permissions to Super Admin and baseline Employee

INSERT INTO permissions (code, module, action, description)
VALUES ('AUDIT_DELETE', 'AUDIT', 'DELETE', 'Delete or batch purge audit log entries')
ON CONFLICT (code) DO NOTHING;

-- Grant AUDIT_DELETE to SUPER_ADMIN and ADMIN roles
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'ALL'
FROM roles r, permissions p
WHERE r.name IN ('SUPER_ADMIN', 'ROLE_SUPER_ADMIN', 'ADMIN', 'ROLE_ADMIN')
  AND p.code = 'AUDIT_DELETE'
ON CONFLICT (role_id, permission_id) DO UPDATE SET scope = 'ALL';

-- Ensure EMPLOYEE role has DASHBOARD_VIEW, ATTENDANCE_VIEW, LEAVE_VIEW, LEAVE_APPLY permissions
INSERT INTO role_permissions (role_id, permission_id, scope)
SELECT r.id, p.id, 'OWN'
FROM roles r, permissions p
WHERE r.name IN ('EMPLOYEE', 'ROLE_EMPLOYEE')
  AND p.code IN ('DASHBOARD_VIEW', 'ATTENDANCE_VIEW', 'LEAVE_VIEW', 'LEAVE_APPLY')
ON CONFLICT (role_id, permission_id) DO NOTHING;
