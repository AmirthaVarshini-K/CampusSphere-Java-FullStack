CREATE TABLE IF NOT EXISTS teams (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    team_name VARCHAR(160) NOT NULL,
    team_code VARCHAR(40) NOT NULL,
    leader_user_id BIGINT NOT NULL,
    primary_contact VARCHAR(160) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_teams_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_teams_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_teams_leader FOREIGN KEY (leader_user_id) REFERENCES users (id),
    CONSTRAINT uk_teams_event_name UNIQUE (event_id, team_name),
    CONSTRAINT uk_teams_event_code UNIQUE (event_id, team_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_teams_institution_id ON teams (institution_id);
CREATE INDEX idx_teams_event_id ON teams (event_id);
CREATE INDEX idx_teams_leader_user_id ON teams (leader_user_id);
CREATE INDEX idx_teams_status ON teams (status);

CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(24) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_team_members_team_id ON team_members (team_id);
CREATE INDEX idx_team_members_user_id ON team_members (user_id);
CREATE INDEX idx_team_members_status ON team_members (status);

CREATE TABLE IF NOT EXISTS team_invitations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    team_id BIGINT NOT NULL,
    invited_user_id BIGINT NOT NULL,
    invited_by_user_id BIGINT NOT NULL,
    invitation_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    invited_at TIMESTAMP(6) NOT NULL,
    responded_at TIMESTAMP(6) NULL,
    message VARCHAR(300) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_team_invitations_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_invitations_invited_user FOREIGN KEY (invited_user_id) REFERENCES users (id),
    CONSTRAINT fk_team_invitations_invited_by FOREIGN KEY (invited_by_user_id) REFERENCES users (id),
    CONSTRAINT uk_team_invitations_team_user UNIQUE (team_id, invited_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_team_invitations_team_id ON team_invitations (team_id);
CREATE INDEX idx_team_invitations_invited_user_id ON team_invitations (invited_user_id);
CREATE INDEX idx_team_invitations_status ON team_invitations (invitation_status);

CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    institution_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    participant_user_id BIGINT NOT NULL,
    registration_number VARCHAR(40) NOT NULL,
    registration_type VARCHAR(24) NOT NULL DEFAULT 'INDIVIDUAL',
    registration_status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    registration_date TIMESTAMP(6) NOT NULL,
    approved_by_user_id BIGINT NULL,
    approved_at TIMESTAMP(6) NULL,
    rejection_reason VARCHAR(500) NULL,
    attendance_status VARCHAR(24) NOT NULL DEFAULT 'NOT_MARKED',
    certificate_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    waitlist_position INT NULL,
    remarks VARCHAR(500) NULL,
    team_id BIGINT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_event_registrations_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT fk_event_registrations_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_event_registrations_participant FOREIGN KEY (participant_user_id) REFERENCES users (id),
    CONSTRAINT fk_event_registrations_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users (id),
    CONSTRAINT fk_event_registrations_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT uk_event_registrations_event_participant UNIQUE (event_id, participant_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_registrations_institution_id ON event_registrations (institution_id);
CREATE INDEX idx_event_registrations_event_id ON event_registrations (event_id);
CREATE INDEX idx_event_registrations_participant_user_id ON event_registrations (participant_user_id);
CREATE INDEX idx_event_registrations_status ON event_registrations (registration_status);
CREATE INDEX idx_event_registrations_team_id ON event_registrations (team_id);
CREATE INDEX idx_event_registrations_waitlist_position ON event_registrations (waitlist_position);

CREATE TABLE IF NOT EXISTS in_app_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    recipient_user_id BIGINT NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message VARCHAR(500) NOT NULL,
    related_entity_type VARCHAR(60) NULL,
    related_entity_id BIGINT NULL,
    read_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    created_by VARCHAR(120) NULL,
    updated_by VARCHAR(120) NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP(6) NULL,
    version BIGINT NULL,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id),
    CONSTRAINT fk_in_app_notifications_recipient FOREIGN KEY (recipient_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_in_app_notifications_recipient_user_id ON in_app_notifications (recipient_user_id);
CREATE INDEX idx_in_app_notifications_created_at ON in_app_notifications (created_at);
CREATE INDEX idx_in_app_notifications_status ON in_app_notifications (status);
