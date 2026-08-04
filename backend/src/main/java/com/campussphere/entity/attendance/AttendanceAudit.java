package com.campussphere.entity.attendance;

import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_audits")
public class AttendanceAudit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_record_id")
    private AttendanceRecord attendanceRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 32)
    private AttendanceActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 24)
    private com.campussphere.entity.registration.AttendanceStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 24)
    private com.campussphere.entity.registration.AttendanceStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(length = 500)
    private String reason;

    @Column(length = 1000)
    private String details;

    public AttendanceRecord getAttendanceRecord() {
        return attendanceRecord;
    }

    public void setAttendanceRecord(AttendanceRecord attendanceRecord) {
        this.attendanceRecord = attendanceRecord;
    }

    public AttendanceActionType getActionType() {
        return actionType;
    }

    public void setActionType(AttendanceActionType actionType) {
        this.actionType = actionType;
    }

    public com.campussphere.entity.registration.AttendanceStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(com.campussphere.entity.registration.AttendanceStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public com.campussphere.entity.registration.AttendanceStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(com.campussphere.entity.registration.AttendanceStatus newStatus) {
        this.newStatus = newStatus;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
