CREATE TABLE IF NOT EXISTS institutions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_code VARCHAR(40) NOT NULL,
    institution_name VARCHAR(160) NOT NULL,
    short_name VARCHAR(80) NULL,
    institution_type VARCHAR(40) NOT NULL,
    affiliation VARCHAR(160) NULL,
    accreditation VARCHAR(160) NULL,
    email VARCHAR(160) NULL,
    phone VARCHAR(24) NULL,
    website VARCHAR(255) NULL,
    address_line_1 VARCHAR(255) NULL,
    address_line_2 VARCHAR(255) NULL,
    city VARCHAR(100) NULL,
    state VARCHAR(100) NULL,
    country VARCHAR(100) NULL,
    postal_code VARCHAR(20) NULL,
    logo_url VARCHAR(512) NULL,
    timezone VARCHAR(64) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_institutions_code UNIQUE (institution_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_institutions_status ON institutions (status);
CREATE INDEX idx_institutions_deleted ON institutions (deleted);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    module VARCHAR(80) NOT NULL,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_permissions_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(48) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255) NULL,
    system_role BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_code UNIQUE (code),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL,
    register_number VARCHAR(40) NULL,
    employee_id VARCHAR(40) NULL,
    department VARCHAR(120) NULL,
    academic_year VARCHAR(16) NULL,
    section VARCHAR(16) NULL,
    phone_number VARCHAR(24) NULL,
    profile_picture_url VARCHAR(512) NULL,
    password_hash VARCHAR(255) NOT NULL,
    terms_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    password_changed_at TIMESTAMP(6) NULL,
    last_login_at TIMESTAMP(6) NULL,
    last_login_ip VARCHAR(64) NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_register_number UNIQUE (register_number),
    CONSTRAINT uk_users_employee_id UNIQUE (employee_id),
    CONSTRAINT fk_users_institution FOREIGN KEY (institution_id) REFERENCES institutions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_register_number ON users (register_number);
CREATE INDEX idx_users_employee_id ON users (employee_id);
CREATE INDEX idx_users_status ON users (status);
CREATE INDEX idx_users_deleted ON users (deleted);

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    used_at TIMESTAMP(6) NULL,
    request_ip VARCHAR(64) NULL,
    request_user_agent VARCHAR(255) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NULL,
    last_used_at TIMESTAMP(6) NULL,
    device_name VARCHAR(120) NULL,
    remember_me BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_revoked_at ON refresh_tokens (revoked_at);

CREATE TABLE IF NOT EXISTS academic_years (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    year_label VARCHAR(24) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    current_year BOOLEAN NOT NULL DEFAULT FALSE,
    registration_open BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_academic_years_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT uk_academic_years_institution_label UNIQUE (institution_id, year_label)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_academic_years_institution_id ON academic_years (institution_id);
CREATE INDEX idx_academic_years_current_year ON academic_years (current_year);

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    department_code VARCHAR(40) NOT NULL,
    department_name VARCHAR(160) NOT NULL,
    short_name VARCHAR(80) NULL,
    description VARCHAR(255) NULL,
    department_email VARCHAR(160) NULL,
    department_phone VARCHAR(24) NULL,
    head_of_department_user_id BIGINT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_departments_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_departments_head FOREIGN KEY (head_of_department_user_id) REFERENCES users (id),
    CONSTRAINT uk_departments_institution_code UNIQUE (institution_id, department_code),
    CONSTRAINT uk_departments_institution_name UNIQUE (institution_id, department_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_departments_institution_id ON departments (institution_id);

CREATE TABLE IF NOT EXISTS programmes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    programme_code VARCHAR(40) NOT NULL,
    programme_name VARCHAR(160) NOT NULL,
    programme_level VARCHAR(32) NOT NULL,
    duration_years INT NOT NULL,
    duration_semesters INT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_programmes_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT uk_programmes_institution_code UNIQUE (institution_id, programme_code),
    CONSTRAINT uk_programmes_institution_name UNIQUE (institution_id, programme_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_programmes_institution_id ON programmes (institution_id);

CREATE TABLE IF NOT EXISTS department_programmes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    programme_id BIGINT NOT NULL,
    academic_year_id BIGINT NULL,
    intake_capacity INT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_department_programmes_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_department_programmes_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_department_programmes_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_department_programmes_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years (id),
    CONSTRAINT uk_department_programmes_pair UNIQUE (department_id, programme_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_department_programmes_institution_id ON department_programmes (institution_id);
CREATE INDEX idx_department_programmes_department_id ON department_programmes (department_id);
CREATE INDEX idx_department_programmes_programme_id ON department_programmes (programme_id);

CREATE TABLE IF NOT EXISTS semesters (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    programme_id BIGINT NOT NULL,
    semester_number INT NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_semesters_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_semesters_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT uk_semesters_programme_number UNIQUE (programme_id, semester_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_semesters_institution_id ON semesters (institution_id);
CREATE INDEX idx_semesters_programme_id ON semesters (programme_id);

CREATE TABLE IF NOT EXISTS sections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    programme_id BIGINT NOT NULL,
    academic_year_id BIGINT NOT NULL,
    semester_id BIGINT NULL,
    advisor_user_id BIGINT NULL,
    study_year INT NULL,
    section_name VARCHAR(80) NOT NULL,
    capacity INT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_sections_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_sections_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_sections_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_sections_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years (id),
    CONSTRAINT fk_sections_semester FOREIGN KEY (semester_id) REFERENCES semesters (id),
    CONSTRAINT fk_sections_advisor FOREIGN KEY (advisor_user_id) REFERENCES users (id),
    CONSTRAINT uk_sections_context UNIQUE (institution_id, department_id, programme_id, academic_year_id, semester_id, section_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sections_institution_id ON sections (institution_id);
CREATE INDEX idx_sections_department_id ON sections (department_id);
CREATE INDEX idx_sections_programme_id ON sections (programme_id);
CREATE INDEX idx_sections_academic_year_id ON sections (academic_year_id);
CREATE INDEX idx_sections_semester_id ON sections (semester_id);
