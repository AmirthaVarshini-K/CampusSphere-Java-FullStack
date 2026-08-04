package com.campussphere.repository;

import com.campussphere.entity.attendance.AttendanceRecord;
import com.campussphere.entity.registration.AttendanceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @EntityGraph(attributePaths = {"institution", "event", "eventSession", "attendanceSession", "registration", "participant", "checkedInBy", "qrToken"})
    Optional<AttendanceRecord> findByIdAndDeletedFalse(Long id);

    List<AttendanceRecord> findByEvent_IdAndDeletedFalseOrderByCheckInTimeDesc(Long eventId);

    List<AttendanceRecord> findByAttendanceSession_IdAndDeletedFalseOrderByCheckInTimeDesc(Long attendanceSessionId);

    List<AttendanceRecord> findByRegistration_IdAndDeletedFalseOrderByCheckInTimeDesc(Long registrationId);

    List<AttendanceRecord> findByParticipant_IdAndDeletedFalseOrderByCheckInTimeDesc(Long participantId);

    List<AttendanceRecord> findByInstitution_IdAndDeletedFalseOrderByCheckInTimeDesc(Long institutionId);

    boolean existsByAttendanceSession_IdAndRegistration_IdAndDeletedFalse(Long attendanceSessionId, Long registrationId);

    long countByEvent_IdAndAttendanceStatusAndDeletedFalse(Long eventId, AttendanceStatus attendanceStatus);

    long countByAttendanceSession_IdAndAttendanceStatusAndDeletedFalse(Long attendanceSessionId, AttendanceStatus attendanceStatus);

    long countByEvent_Institution_IdAndDeletedFalse(Long institutionId);
}
