package com.campussphere.repository;

import com.campussphere.entity.attendance.AttendanceAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceAuditRepository extends JpaRepository<AttendanceAudit, Long> {

    List<AttendanceAudit> findByAttendanceRecord_IdAndDeletedFalseOrderByOccurredAtDesc(Long attendanceRecordId);
}
