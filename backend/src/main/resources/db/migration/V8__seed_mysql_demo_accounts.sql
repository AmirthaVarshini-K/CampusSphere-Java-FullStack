INSERT INTO institutions (
    institution_code,
    institution_name,
    short_name,
    institution_type,
    affiliation,
    accreditation,
    email,
    phone,
    website,
    address_line_1,
    address_line_2,
    city,
    state,
    country,
    postal_code,
    logo_url,
    timezone,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    'CS-001',
    'CampusSphere Institute of Technology',
    'CSIT',
    'COLLEGE',
    'Autonomous',
    NULL,
    'info@campussphere-institute.edu',
    '+91 90000 00001',
    NULL,
    NULL,
    NULL,
    'Bengaluru',
    'Karnataka',
    'India',
    NULL,
    NULL,
    'Asia/Kolkata',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM institutions
    WHERE institution_code = 'CS-001'
);

INSERT INTO users (
    institution_id,
    first_name,
    last_name,
    email,
    register_number,
    employee_id,
    department,
    academic_year,
    section,
    phone_number,
    profile_picture_url,
    password_hash,
    terms_accepted,
    password_changed_at,
    last_login_at,
    last_login_ip,
    failed_login_attempts,
    locked_until,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    NULL,
    'System',
    'Administrator',
    'admin@campussphere.local',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    '$2a$10$2s./p5cEGQ7GXDf9pIJz1.q9lSSv2W0j1TL3clEg2mtBfMBWVBKk2',
    TRUE,
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    0,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@campussphere.local'
);

INSERT INTO users (
    institution_id,
    first_name,
    last_name,
    email,
    register_number,
    employee_id,
    department,
    academic_year,
    section,
    phone_number,
    profile_picture_url,
    password_hash,
    terms_accepted,
    password_changed_at,
    last_login_at,
    last_login_ip,
    failed_login_attempts,
    locked_until,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    (SELECT id FROM institutions WHERE institution_code = 'CS-001'),
    'Faculty',
    'Coordinator',
    'coordinator@campussphere.local',
    NULL,
    'FAC-1001',
    'Computer Science',
    NULL,
    NULL,
    NULL,
    NULL,
    '$2a$10$G/jKHcjNmhj3j4aVLmZHN.pB8KFqhJhIyBG2Wci9jL5tP0Kc3EjUG',
    TRUE,
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    0,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'coordinator@campussphere.local'
);

INSERT INTO users (
    institution_id,
    first_name,
    last_name,
    email,
    register_number,
    employee_id,
    department,
    academic_year,
    section,
    phone_number,
    profile_picture_url,
    password_hash,
    terms_accepted,
    password_changed_at,
    last_login_at,
    last_login_ip,
    failed_login_attempts,
    locked_until,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    (SELECT id FROM institutions WHERE institution_code = 'CS-001'),
    'Asha',
    'Menon',
    'student@campussphere.local',
    '24CSE0001',
    NULL,
    'Computer Science and Engineering',
    'III',
    'A',
    NULL,
    NULL,
    '$2a$10$TklI5.GvdyX/rA7L5TiKy.NCVYx0BnFBokqkq0ELrdGAp9qJV5Rj2',
    TRUE,
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    0,
    NULL,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'student@campussphere.local'
);

UPDATE users
SET
    institution_id = NULL,
    first_name = 'System',
    last_name = 'Administrator',
    register_number = NULL,
    employee_id = NULL,
    department = NULL,
    academic_year = NULL,
    section = NULL,
    phone_number = NULL,
    profile_picture_url = NULL,
    password_hash = '$2a$10$2s./p5cEGQ7GXDf9pIJz1.q9lSSv2W0j1TL3clEg2mtBfMBWVBKk2',
    terms_accepted = TRUE,
    password_changed_at = CURRENT_TIMESTAMP(6),
    last_login_at = NULL,
    last_login_ip = NULL,
    failed_login_attempts = 0,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP(6),
    deleted = FALSE,
    deleted_at = NULL,
    version = COALESCE(version, 0),
    status = 'ACTIVE'
WHERE email = 'admin@campussphere.local';

UPDATE users
SET
    institution_id = (SELECT id FROM institutions WHERE institution_code = 'CS-001'),
    first_name = 'Faculty',
    last_name = 'Coordinator',
    register_number = NULL,
    employee_id = 'FAC-1001',
    department = 'Computer Science',
    academic_year = NULL,
    section = NULL,
    phone_number = NULL,
    profile_picture_url = NULL,
    password_hash = '$2a$10$G/jKHcjNmhj3j4aVLmZHN.pB8KFqhJhIyBG2Wci9jL5tP0Kc3EjUG',
    terms_accepted = TRUE,
    password_changed_at = CURRENT_TIMESTAMP(6),
    last_login_at = NULL,
    last_login_ip = NULL,
    failed_login_attempts = 0,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP(6),
    deleted = FALSE,
    deleted_at = NULL,
    version = COALESCE(version, 0),
    status = 'ACTIVE'
WHERE email = 'coordinator@campussphere.local';

UPDATE users
SET
    institution_id = (SELECT id FROM institutions WHERE institution_code = 'CS-001'),
    first_name = 'Asha',
    last_name = 'Menon',
    register_number = '24CSE0001',
    employee_id = NULL,
    department = 'Computer Science and Engineering',
    academic_year = 'III',
    section = 'A',
    phone_number = NULL,
    profile_picture_url = NULL,
    password_hash = '$2a$10$TklI5.GvdyX/rA7L5TiKy.NCVYx0BnFBokqkq0ELrdGAp9qJV5Rj2',
    terms_accepted = TRUE,
    password_changed_at = CURRENT_TIMESTAMP(6),
    last_login_at = NULL,
    last_login_ip = NULL,
    failed_login_attempts = 0,
    locked_until = NULL,
    updated_at = CURRENT_TIMESTAMP(6),
    deleted = FALSE,
    deleted_at = NULL,
    version = COALESCE(version, 0),
    status = 'ACTIVE'
WHERE email = 'student@campussphere.local';

INSERT INTO user_roles (
    user_id,
    role_id,
    assigned_at,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    u.id,
    r.id,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
FROM users u
JOIN roles r ON r.code = 'ADMINISTRATOR'
WHERE u.email = 'admin@campussphere.local'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO user_roles (
    user_id,
    role_id,
    assigned_at,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    u.id,
    r.id,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
FROM users u
JOIN roles r ON r.code = 'FACULTY_COORDINATOR'
WHERE u.email = 'coordinator@campussphere.local'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );

INSERT INTO user_roles (
    user_id,
    role_id,
    assigned_at,
    created_at,
    updated_at,
    created_by,
    updated_by,
    deleted,
    deleted_at,
    version,
    status
)
SELECT
    u.id,
    r.id,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6),
    NULL,
    NULL,
    FALSE,
    NULL,
    0,
    'ACTIVE'
FROM users u
JOIN roles r ON r.code = 'STUDENT'
WHERE u.email = 'student@campussphere.local'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_id = r.id
  );
