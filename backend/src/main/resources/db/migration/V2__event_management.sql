CREATE TABLE IF NOT EXISTS event_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    category_code VARCHAR(40) NOT NULL,
    category_name VARCHAR(160) NOT NULL,
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
    CONSTRAINT fk_event_categories_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT uk_event_categories_institution_code UNIQUE (institution_id, category_code),
    CONSTRAINT uk_event_categories_institution_name UNIQUE (institution_id, category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_categories_institution_id ON event_categories (institution_id);
CREATE INDEX idx_event_categories_status ON event_categories (status);

CREATE TABLE IF NOT EXISTS event_types (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    type_code VARCHAR(40) NOT NULL,
    type_name VARCHAR(160) NOT NULL,
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
    CONSTRAINT fk_event_types_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT uk_event_types_institution_code UNIQUE (institution_id, type_code),
    CONSTRAINT uk_event_types_institution_name UNIQUE (institution_id, type_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_types_institution_id ON event_types (institution_id);
CREATE INDEX idx_event_types_status ON event_types (status);

CREATE TABLE IF NOT EXISTS venues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    venue_code VARCHAR(40) NOT NULL,
    venue_name VARCHAR(160) NOT NULL,
    building VARCHAR(120) NULL,
    floor VARCHAR(40) NULL,
    room_number VARCHAR(40) NULL,
    address VARCHAR(255) NULL,
    capacity INT NULL,
    venue_type VARCHAR(40) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_venues_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT uk_venues_institution_code UNIQUE (institution_id, venue_code),
    CONSTRAINT uk_venues_institution_name UNIQUE (institution_id, venue_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_venues_institution_id ON venues (institution_id);
CREATE INDEX idx_venues_type ON venues (venue_type);
CREATE INDEX idx_venues_status ON venues (status);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    event_code VARCHAR(40) NOT NULL,
    slug VARCHAR(200) NULL,
    short_description VARCHAR(500) NULL,
    full_description VARCHAR(4000) NULL,
    event_category_id BIGINT NOT NULL,
    event_type_id BIGINT NOT NULL,
    organizing_department_id BIGINT NULL,
    academic_year_id BIGINT NULL,
    venue_id BIGINT NULL,
    mode VARCHAR(20) NOT NULL,
    visibility VARCHAR(24) NOT NULL,
    event_status VARCHAR(32) NOT NULL,
    start_date_time TIMESTAMP(6) NULL,
    end_date_time TIMESTAMP(6) NULL,
    registration_start_date_time TIMESTAMP(6) NULL,
    registration_end_date_time TIMESTAMP(6) NULL,
    cancellation_deadline TIMESTAMP(6) NULL,
    online_meeting_url VARCHAR(512) NULL,
    maximum_participants INT NULL,
    minimum_participants INT NULL,
    registration_fee DECIMAL(12,2) NULL,
    currency VARCHAR(12) NULL,
    banner_image_url VARCHAR(512) NULL,
    contact_email VARCHAR(160) NULL,
    contact_phone VARCHAR(24) NULL,
    published_at TIMESTAMP(6) NULL,
    approved_at TIMESTAMP(6) NULL,
    approved_by_user_id BIGINT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_events_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_events_category FOREIGN KEY (event_category_id) REFERENCES event_categories (id),
    CONSTRAINT fk_events_type FOREIGN KEY (event_type_id) REFERENCES event_types (id),
    CONSTRAINT fk_events_department FOREIGN KEY (organizing_department_id) REFERENCES departments (id),
    CONSTRAINT fk_events_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years (id),
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues (id),
    CONSTRAINT uk_events_institution_code UNIQUE (institution_id, event_code),
    CONSTRAINT uk_events_institution_slug UNIQUE (institution_id, slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_events_institution_id ON events (institution_id);
CREATE INDEX idx_events_status ON events (event_status);
CREATE INDEX idx_events_mode ON events (mode);
CREATE INDEX idx_events_category_id ON events (event_category_id);
CREATE INDEX idx_events_type_id ON events (event_type_id);
CREATE INDEX idx_events_department_id ON events (organizing_department_id);
CREATE INDEX idx_events_venue_id ON events (venue_id);
CREATE INDEX idx_events_start_date_time ON events (start_date_time);

CREATE TABLE IF NOT EXISTS event_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(1000) NULL,
    session_start TIMESTAMP(6) NULL,
    session_end TIMESTAMP(6) NULL,
    venue_id BIGINT NULL,
    speaker_name VARCHAR(160) NULL,
    sequence_number INT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_event_sessions_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_event_sessions_venue FOREIGN KEY (venue_id) REFERENCES venues (id),
    CONSTRAINT uk_event_sessions_event_sequence UNIQUE (event_id, sequence_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_sessions_event_id ON event_sessions (event_id);
CREATE INDEX idx_event_sessions_status ON event_sessions (status);

CREATE TABLE IF NOT EXISTS event_coordinators (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    coordinator_role VARCHAR(40) NOT NULL,
    primary_coordinator BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_event_coordinators_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_event_coordinators_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_event_coordinators_event_user_role UNIQUE (event_id, user_id, coordinator_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_coordinators_event_id ON event_coordinators (event_id);
CREATE INDEX idx_event_coordinators_user_id ON event_coordinators (user_id);
CREATE INDEX idx_event_coordinators_status ON event_coordinators (status);

CREATE TABLE IF NOT EXISTS event_eligibility_rules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    department_id BIGINT NULL,
    programme_id BIGINT NULL,
    section_id BIGINT NULL,
    participant_type VARCHAR(24) NOT NULL,
    rule_type VARCHAR(24) NOT NULL,
    minimum_year INT NULL,
    maximum_year INT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_event_eligibility_rules_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_event_eligibility_rules_department FOREIGN KEY (department_id) REFERENCES departments (id),
    CONSTRAINT fk_event_eligibility_rules_programme FOREIGN KEY (programme_id) REFERENCES programmes (id),
    CONSTRAINT fk_event_eligibility_rules_section FOREIGN KEY (section_id) REFERENCES sections (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_eligibility_rules_event_id ON event_eligibility_rules (event_id);
CREATE INDEX idx_event_eligibility_rules_department_id ON event_eligibility_rules (department_id);
CREATE INDEX idx_event_eligibility_rules_programme_id ON event_eligibility_rules (programme_id);
CREATE INDEX idx_event_eligibility_rules_section_id ON event_eligibility_rules (section_id);
CREATE INDEX idx_event_eligibility_rules_status ON event_eligibility_rules (status);

CREATE TABLE IF NOT EXISTS event_registration_configs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    registration_required BOOLEAN NOT NULL DEFAULT TRUE,
    approval_required BOOLEAN NOT NULL DEFAULT FALSE,
    waitlist_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    team_event BOOLEAN NOT NULL DEFAULT FALSE,
    minimum_team_size INT NULL,
    maximum_team_size INT NULL,
    allow_external_participants BOOLEAN NOT NULL DEFAULT FALSE,
    allow_multiple_registrations BOOLEAN NOT NULL DEFAULT FALSE,
    certificate_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    attendance_required_for_certificate BOOLEAN NOT NULL DEFAULT FALSE,
    cancellation_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    cancellation_deadline TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_event_registration_configs_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT uk_event_registration_configs_event UNIQUE (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_registration_configs_event_id ON event_registration_configs (event_id);
CREATE INDEX idx_event_registration_configs_status ON event_registration_configs (status);
