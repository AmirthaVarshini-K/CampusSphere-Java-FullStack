package com.campussphere.repository;

import com.campussphere.entity.registration.TeamMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @EntityGraph(attributePaths = {"team", "team.event", "user"})
    Optional<TeamMember> findByIdAndDeletedFalse(Long id);

    List<TeamMember> findByTeam_IdAndDeletedFalse(Long teamId);

    Optional<TeamMember> findByTeam_IdAndUser_IdAndDeletedFalse(Long teamId, Long userId);

    long countByTeam_IdAndDeletedFalse(Long teamId);
}
