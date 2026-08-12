INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'USER_READ', 'USER READ', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'USER_READ');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'USER_WRITE', 'USER WRITE', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'USER_WRITE');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'USER_DELETE', 'USER DELETE', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'USER_DELETE');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'ROLE_ASSIGN', 'ROLE ASSIGN', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ROLE_ASSIGN');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'EVENT_READ', 'EVENT READ', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'EVENT_READ');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'EVENT_WRITE', 'EVENT WRITE', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'EVENT_WRITE');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'ATTENDANCE_VERIFY', 'ATTENDANCE VERIFY', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ATTENDANCE_VERIFY');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'CERTIFICATE_GENERATE', 'CERTIFICATE GENERATE', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'CERTIFICATE_GENERATE');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'REPORT_VIEW', 'REPORT VIEW', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'REPORT_VIEW');

INSERT INTO permissions (
    code,
    name,
    module,
    description,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'SYSTEM_CONFIG', 'SYSTEM CONFIG', 'AUTH', 'Auto-seeded permission for CampusSphere foundation.', CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'SYSTEM_CONFIG');

INSERT INTO roles (
    code,
    name,
    description,
    system_role,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'ADMINISTRATOR', 'Administrator', 'Full system access', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'ADMINISTRATOR');

INSERT INTO roles (
    code,
    name,
    description,
    system_role,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'FACULTY_COORDINATOR', 'Faculty Coordinator', 'Academic event coordination access', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'FACULTY_COORDINATOR');

INSERT INTO roles (
    code,
    name,
    description,
    system_role,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT 'STUDENT', 'Student', 'Student portal access', TRUE, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), NULL, NULL, FALSE, NULL, 0, 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'STUDENT');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('USER_READ', 'USER_WRITE', 'USER_DELETE', 'ROLE_ASSIGN', 'EVENT_READ', 'EVENT_WRITE', 'ATTENDANCE_VERIFY', 'CERTIFICATE_GENERATE', 'REPORT_VIEW', 'SYSTEM_CONFIG')
WHERE r.code = 'ADMINISTRATOR'
AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('EVENT_READ', 'EVENT_WRITE', 'ATTENDANCE_VERIFY', 'CERTIFICATE_GENERATE', 'REPORT_VIEW')
WHERE r.code = 'FACULTY_COORDINATOR'
AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('EVENT_READ', 'REPORT_VIEW')
WHERE r.code = 'STUDENT'
AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
