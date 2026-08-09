package com.campussphere.repository;

import com.campussphere.entity.registration.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"institution", "event", "leader"})
    Optional<Team> findByIdAndDeletedFalse(Long id);

    List<Team> findByEvent_IdAndDeletedFalse(Long eventId);

    List<Team> findByInstitution_IdAndDeletedFalse(Long institutionId);

    boolean existsByEvent_IdAndTeamNameIgnoreCaseAndDeletedFalse(Long eventId, String teamName);

    boolean existsByEvent_IdAndTeamCodeIgnoreCaseAndDeletedFalse(Long eventId, String teamCode);

    boolean existsByEvent_IdAndTeamNameIgnoreCaseAndIdNotAndDeletedFalse(Long eventId, String teamName, Long id);

    boolean existsByEvent_IdAndTeamCodeIgnoreCaseAndIdNotAndDeletedFalse(Long eventId, String teamCode, Long id);

    long countByEvent_Institution_IdAndDeletedFalse(Long institutionId);
}
