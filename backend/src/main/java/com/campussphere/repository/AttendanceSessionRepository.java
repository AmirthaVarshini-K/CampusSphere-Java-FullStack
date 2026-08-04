package com.campussphere.repository;

import com.campussphere.entity.attendance.AttendanceSession;
import com.campussphere.entity.attendance.AttendanceSessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    @EntityGraph(attributePaths = {"institution", "event", "eventSession", "openedBy", "closedBy"})
    Optional<AttendanceSession> findByIdAndDeletedFalse(Long id);

    List<AttendanceSession> findByEvent_IdAndDeletedFalseOrderByOpenedAtDesc(Long eventId);

    List<AttendanceSession> findByInstitution_IdAndDeletedFalseOrderByOpenedAtDesc(Long institutionId);

    long countByEvent_IdAndAttendanceSessionStatusAndDeletedFalse(Long eventId, AttendanceSessionStatus status);
}
