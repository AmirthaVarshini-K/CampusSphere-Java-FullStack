CREATE TABLE IF NOT EXISTS attendance_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    event_session_id BIGINT NULL,
    session_title VARCHAR(180) NOT NULL,
    attendance_status VARCHAR(24) NOT NULL DEFAULT 'OPEN',
    opened_at TIMESTAMP(6) NOT NULL,
    closed_at TIMESTAMP(6) NULL,
    opened_by_user_id BIGINT NULL,
    closed_by_user_id BIGINT NULL,
    remarks VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_attendance_sessions_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_attendance_sessions_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_attendance_sessions_event_session FOREIGN KEY (event_session_id) REFERENCES event_sessions (id),
    CONSTRAINT fk_attendance_sessions_opened_by FOREIGN KEY (opened_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_attendance_sessions_closed_by FOREIGN KEY (closed_by_user_id) REFERENCES users (id),
    CONSTRAINT uk_attendance_sessions_event_session UNIQUE (event_id, event_session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attendance_sessions_institution_id ON attendance_sessions (institution_id);
CREATE INDEX idx_attendance_sessions_event_id ON attendance_sessions (event_id);
CREATE INDEX idx_attendance_sessions_status ON attendance_sessions (attendance_status);

CREATE TABLE IF NOT EXISTS qr_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    registration_id BIGINT NOT NULL,
    token_hash VARCHAR(128) NOT NULL,
    token_prefix VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP(6) NULL,
    used_at TIMESTAMP(6) NULL,
    invalidated_at TIMESTAMP(6) NULL,
    one_time_use BOOLEAN NOT NULL DEFAULT TRUE,
    regenerated_at TIMESTAMP(6) NULL,
    generated_by_user_id BIGINT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_qr_tokens_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_qr_tokens_registration FOREIGN KEY (registration_id) REFERENCES event_registrations (id),
    CONSTRAINT fk_qr_tokens_generated_by FOREIGN KEY (generated_by_user_id) REFERENCES users (id),
    CONSTRAINT uk_qr_tokens_registration UNIQUE (registration_id),
    CONSTRAINT uk_qr_tokens_token_hash UNIQUE (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_qr_tokens_institution_id ON qr_tokens (institution_id);
CREATE INDEX idx_qr_tokens_registration_id ON qr_tokens (registration_id);
CREATE INDEX idx_qr_tokens_status ON qr_tokens (status);

CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    event_session_id BIGINT NULL,
    attendance_session_id BIGINT NOT NULL,
    registration_id BIGINT NOT NULL,
    participant_user_id BIGINT NOT NULL,
    attendance_status VARCHAR(24) NOT NULL DEFAULT 'PRESENT',
    attendance_method VARCHAR(24) NOT NULL DEFAULT 'QR',
    check_in_time TIMESTAMP(6) NOT NULL,
    checked_in_by_user_id BIGINT NULL,
    qr_token_id BIGINT NULL,
    device_info VARCHAR(255) NULL,
    ip_address VARCHAR(80) NULL,
    remarks VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_attendance_records_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_attendance_records_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_attendance_records_event_session FOREIGN KEY (event_session_id) REFERENCES event_sessions (id),
    CONSTRAINT fk_attendance_records_attendance_session FOREIGN KEY (attendance_session_id) REFERENCES attendance_sessions (id),
    CONSTRAINT fk_attendance_records_registration FOREIGN KEY (registration_id) REFERENCES event_registrations (id),
    CONSTRAINT fk_attendance_records_participant FOREIGN KEY (participant_user_id) REFERENCES users (id),
    CONSTRAINT fk_attendance_records_checked_in_by FOREIGN KEY (checked_in_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_attendance_records_qr_token FOREIGN KEY (qr_token_id) REFERENCES qr_tokens (id),
    CONSTRAINT uk_attendance_records_session_registration UNIQUE (attendance_session_id, registration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attendance_records_institution_id ON attendance_records (institution_id);
CREATE INDEX idx_attendance_records_event_id ON attendance_records (event_id);
CREATE INDEX idx_attendance_records_attendance_session_id ON attendance_records (attendance_session_id);
CREATE INDEX idx_attendance_records_registration_id ON attendance_records (registration_id);
CREATE INDEX idx_attendance_records_participant_user_id ON attendance_records (participant_user_id);
CREATE INDEX idx_attendance_records_status ON attendance_records (attendance_status);
CREATE INDEX idx_attendance_records_check_in_time ON attendance_records (check_in_time);

CREATE TABLE IF NOT EXISTS attendance_audits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    attendance_record_id BIGINT NULL,
    action_type VARCHAR(32) NOT NULL,
    previous_status VARCHAR(24) NULL,
    new_status VARCHAR(24) NULL,
    actor_user_id BIGINT NULL,
    occurred_at TIMESTAMP(6) NOT NULL,
    reason VARCHAR(500) NULL,
    details VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_attendance_audits_record FOREIGN KEY (attendance_record_id) REFERENCES attendance_records (id),
    CONSTRAINT fk_attendance_audits_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_attendance_audits_record_id ON attendance_audits (attendance_record_id);
CREATE INDEX idx_attendance_audits_occurred_at ON attendance_audits (occurred_at);
CREATE INDEX idx_attendance_audits_status ON attendance_audits (status);
