package com.campussphere.entity.event;

import com.campussphere.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "event_registration_configs",
        uniqueConstraints = @UniqueConstraint(name = "uk_event_registration_configs_event", columnNames = "event_id")
)
public class EventRegistrationConfig extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registration_required", nullable = false)
    private boolean registrationRequired = true;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Column(name = "waitlist_enabled", nullable = false)
    private boolean waitlistEnabled;

    @Column(name = "team_event", nullable = false)
    private boolean teamEvent;

    @Column(name = "minimum_team_size")
    private Integer minimumTeamSize;

    @Column(name = "maximum_team_size")
    private Integer maximumTeamSize;

    @Column(name = "allow_external_participants", nullable = false)
    private boolean allowExternalParticipants;

    @Column(name = "allow_multiple_registrations", nullable = false)
    private boolean allowMultipleRegistrations;

    @Column(name = "certificate_enabled", nullable = false)
    private boolean certificateEnabled;

    @Column(name = "attendance_required_for_certificate", nullable = false)
    private boolean attendanceRequiredForCertificate;

    @Column(name = "cancellation_allowed", nullable = false)
    private boolean cancellationAllowed;

    @Column(name = "cancellation_deadline")
    private LocalDateTime cancellationDeadline;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isRegistrationRequired() {
        return registrationRequired;
    }

    public void setRegistrationRequired(boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public boolean isWaitlistEnabled() {
        return waitlistEnabled;
    }

    public void setWaitlistEnabled(boolean waitlistEnabled) {
        this.waitlistEnabled = waitlistEnabled;
    }

    public boolean isTeamEvent() {
        return teamEvent;
    }

    public void setTeamEvent(boolean teamEvent) {
        this.teamEvent = teamEvent;
    }

    public Integer getMinimumTeamSize() {
        return minimumTeamSize;
    }

    public void setMinimumTeamSize(Integer minimumTeamSize) {
        this.minimumTeamSize = minimumTeamSize;
    }

    public Integer getMaximumTeamSize() {
        return maximumTeamSize;
    }

    public void setMaximumTeamSize(Integer maximumTeamSize) {
        this.maximumTeamSize = maximumTeamSize;
    }

    public boolean isAllowExternalParticipants() {
        return allowExternalParticipants;
    }

    public void setAllowExternalParticipants(boolean allowExternalParticipants) {
        this.allowExternalParticipants = allowExternalParticipants;
    }

    public boolean isAllowMultipleRegistrations() {
        return allowMultipleRegistrations;
    }

    public void setAllowMultipleRegistrations(boolean allowMultipleRegistrations) {
        this.allowMultipleRegistrations = allowMultipleRegistrations;
    }

    public boolean isCertificateEnabled() {
        return certificateEnabled;
    }

    public void setCertificateEnabled(boolean certificateEnabled) {
        this.certificateEnabled = certificateEnabled;
    }

    public boolean isAttendanceRequiredForCertificate() {
        return attendanceRequiredForCertificate;
    }

    public void setAttendanceRequiredForCertificate(boolean attendanceRequiredForCertificate) {
        this.attendanceRequiredForCertificate = attendanceRequiredForCertificate;
    }

    public boolean isCancellationAllowed() {
        return cancellationAllowed;
    }

    public void setCancellationAllowed(boolean cancellationAllowed) {
        this.cancellationAllowed = cancellationAllowed;
    }

    public LocalDateTime getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDateTime cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
    }
}
