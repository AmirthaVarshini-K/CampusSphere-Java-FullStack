CREATE INDEX idx_events_institution_status_start ON events (institution_id, event_status, start_date_time);
CREATE INDEX idx_events_institution_start ON events (institution_id, start_date_time);

CREATE INDEX idx_event_registrations_institution_status_date ON event_registrations (institution_id, registration_status, registration_date);
CREATE INDEX idx_event_registrations_event_date ON event_registrations (event_id, registration_date);
CREATE INDEX idx_event_registrations_participant_date ON event_registrations (participant_user_id, registration_date);

CREATE INDEX idx_attendance_records_institution_status_checkin ON attendance_records (institution_id, attendance_status, check_in_time);
CREATE INDEX idx_attendance_records_event_checkin ON attendance_records (event_id, check_in_time);
CREATE INDEX idx_attendance_records_participant_checkin ON attendance_records (participant_user_id, check_in_time);

CREATE INDEX idx_certificates_institution_generated ON certificates (institution_id, generated_at);
CREATE INDEX idx_certificates_event_generated ON certificates (event_id, generated_at);
CREATE INDEX idx_certificates_recipient_generated ON certificates (recipient_user_id, generated_at);
CREATE INDEX idx_certificates_status_generated ON certificates (certificate_status, generated_at);

CREATE INDEX idx_certificate_verifications_verified_at ON certificate_verifications (verified_at);

CREATE INDEX idx_event_coordinators_event_user ON event_coordinators (event_id, user_id);
CREATE INDEX idx_event_coordinators_user_event ON event_coordinators (user_id, event_id);

CREATE INDEX idx_team_invitations_team_status ON team_invitations (team_id, invitation_status);
CREATE INDEX idx_team_invitations_invited_user_status ON team_invitations (invited_user_id, invitation_status);

CREATE INDEX idx_notifications_recipient_read_created ON in_app_notifications (recipient_user_id, read_at, created_at);
