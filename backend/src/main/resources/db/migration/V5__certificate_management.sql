CREATE TABLE IF NOT EXISTS certificate_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    template_code VARCHAR(40) NOT NULL,
    template_name VARCHAR(160) NOT NULL,
    certificate_type VARCHAR(40) NOT NULL,
    orientation VARCHAR(24) NOT NULL,
    description VARCHAR(500) NULL,
    institution_logo_url VARCHAR(512) NULL,
    organizer_logo_url VARCHAR(512) NULL,
    background_image_url VARCHAR(512) NULL,
    signature_left_url VARCHAR(512) NULL,
    signature_right_url VARCHAR(512) NULL,
    seal_url VARCHAR(512) NULL,
    primary_color VARCHAR(16) NULL,
    accent_color VARCHAR(16) NULL,
    watermark_text VARCHAR(120) NULL,
    margin_top_mm INT NULL,
    margin_right_mm INT NULL,
    margin_bottom_mm INT NULL,
    margin_left_mm INT NULL,
    qr_code_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    verification_url_base VARCHAR(512) NULL,
    template_html TEXT NULL,
    variables_json TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_certificate_templates_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT uk_certificate_templates_institution_code UNIQUE (institution_id, template_code),
    CONSTRAINT uk_certificate_templates_institution_name UNIQUE (institution_id, template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_certificate_templates_institution_id ON certificate_templates (institution_id);
CREATE INDEX idx_certificate_templates_type ON certificate_templates (certificate_type);
CREATE INDEX idx_certificate_templates_status ON certificate_templates (status);

CREATE TABLE IF NOT EXISTS certificates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    certificate_number VARCHAR(60) NOT NULL,
    certificate_uuid VARCHAR(64) NOT NULL,
    verification_token VARCHAR(128) NOT NULL,
    issue_date TIMESTAMP(6) NOT NULL,
    generated_at TIMESTAMP(6) NOT NULL,
    issue_by_user_id BIGINT NULL,
    recipient_user_id BIGINT NOT NULL,
    recipient_name VARCHAR(180) NOT NULL,
    recipient_role VARCHAR(80) NOT NULL,
    event_id BIGINT NOT NULL,
    event_session_id BIGINT NULL,
    academic_year_id BIGINT NULL,
    template_id BIGINT NOT NULL,
    certificate_type VARCHAR(40) NOT NULL,
    certificate_status VARCHAR(24) NOT NULL,
    verification_status VARCHAR(24) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP(6) NULL,
    revoked_by_user_id BIGINT NULL,
    revocation_reason VARCHAR(500) NULL,
    position VARCHAR(60) NULL,
    prize VARCHAR(120) NULL,
    attendance_percentage INT NULL,
    verification_url VARCHAR(512) NULL,
    pdf_file_name VARCHAR(180) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_certificates_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_certificates_issue_by FOREIGN KEY (issue_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_certificates_recipient FOREIGN KEY (recipient_user_id) REFERENCES users (id),
    CONSTRAINT fk_certificates_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_certificates_session FOREIGN KEY (event_session_id) REFERENCES event_sessions (id),
    CONSTRAINT fk_certificates_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years (id),
    CONSTRAINT fk_certificates_template FOREIGN KEY (template_id) REFERENCES certificate_templates (id),
    CONSTRAINT fk_certificates_revoked_by FOREIGN KEY (revoked_by_user_id) REFERENCES users (id),
    CONSTRAINT uk_certificates_institution_number UNIQUE (institution_id, certificate_number),
    CONSTRAINT uk_certificates_uuid UNIQUE (certificate_uuid),
    CONSTRAINT uk_certificates_token UNIQUE (verification_token),
    CONSTRAINT uk_certificates_duplicate UNIQUE (institution_id, event_id, recipient_user_id, certificate_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_certificates_institution_id ON certificates (institution_id);
CREATE INDEX idx_certificates_event_id ON certificates (event_id);
CREATE INDEX idx_certificates_recipient_user_id ON certificates (recipient_user_id);
CREATE INDEX idx_certificates_type ON certificates (certificate_type);
CREATE INDEX idx_certificates_status ON certificates (certificate_status);
CREATE INDEX idx_certificates_verification_status ON certificates (verification_status);

CREATE TABLE IF NOT EXISTS certificate_issue_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    certificate_id BIGINT NOT NULL,
    actor_user_id BIGINT NULL,
    action_type VARCHAR(24) NOT NULL,
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
    CONSTRAINT fk_certificate_issue_logs_certificate FOREIGN KEY (certificate_id) REFERENCES certificates (id),
    CONSTRAINT fk_certificate_issue_logs_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_certificate_issue_logs_certificate_id ON certificate_issue_logs (certificate_id);
CREATE INDEX idx_certificate_issue_logs_occurred_at ON certificate_issue_logs (occurred_at);
CREATE INDEX idx_certificate_issue_logs_status ON certificate_issue_logs (status);

CREATE TABLE IF NOT EXISTS certificate_verifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    certificate_id BIGINT NULL,
    verification_token VARCHAR(128) NOT NULL,
    verified_at TIMESTAMP(6) NOT NULL,
    verified_ip VARCHAR(80) NULL,
    user_agent VARCHAR(255) NULL,
    verification_status VARCHAR(24) NOT NULL,
    message VARCHAR(500) NULL,
    certificate_number VARCHAR(60) NULL,
    recipient_name VARCHAR(180) NULL,
    institution_name VARCHAR(180) NULL,
    event_title VARCHAR(180) NULL,
    certificate_type VARCHAR(40) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_certificate_verifications_certificate FOREIGN KEY (certificate_id) REFERENCES certificates (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_certificate_verifications_token ON certificate_verifications (verification_token);
CREATE INDEX idx_certificate_verifications_status ON certificate_verifications (verification_status);

CREATE TABLE IF NOT EXISTS certificate_audits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    certificate_id BIGINT NULL,
    action_type VARCHAR(24) NOT NULL,
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
    CONSTRAINT fk_certificate_audits_certificate FOREIGN KEY (certificate_id) REFERENCES certificates (id),
    CONSTRAINT fk_certificate_audits_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_certificate_audits_certificate_id ON certificate_audits (certificate_id);
CREATE INDEX idx_certificate_audits_occurred_at ON certificate_audits (occurred_at);
CREATE INDEX idx_certificate_audits_status ON certificate_audits (status);
