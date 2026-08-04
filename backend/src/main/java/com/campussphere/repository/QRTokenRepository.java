package com.campussphere.repository;

import com.campussphere.entity.attendance.QRToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QRTokenRepository extends JpaRepository<QRToken, Long> {

    @EntityGraph(attributePaths = {"institution", "registration", "generatedBy"})
    Optional<QRToken> findByIdAndDeletedFalse(Long id);

    @EntityGraph(attributePaths = {"institution", "registration", "generatedBy"})
    Optional<QRToken> findByTokenHashAndDeletedFalse(String tokenHash);

    @EntityGraph(attributePaths = {"institution", "registration", "generatedBy"})
    Optional<QRToken> findByRegistration_IdAndDeletedFalse(Long registrationId);

    List<QRToken> findByRegistration_Event_IdAndDeletedFalseOrderByCreatedAtDesc(Long eventId);
}
