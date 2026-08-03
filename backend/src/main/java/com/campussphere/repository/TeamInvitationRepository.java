package com.campussphere.repository;

import com.campussphere.entity.registration.InvitationStatus;
import com.campussphere.entity.registration.TeamInvitation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    @EntityGraph(attributePaths = {"team", "team.event", "invitedUser", "invitedBy"})
    Optional<TeamInvitation> findByIdAndDeletedFalse(Long id);

    List<TeamInvitation> findByTeam_IdAndDeletedFalse(Long teamId);

    List<TeamInvitation> findByInvitedUser_IdAndDeletedFalseOrderByInvitedAtDesc(Long invitedUserId);

    boolean existsByTeam_IdAndInvitedUser_IdAndDeletedFalse(Long teamId, Long invitedUserId);

    long countByTeam_Event_Institution_IdAndDeletedFalse(Long institutionId);

    long countByInvitedUser_IdAndInvitationStatusAndDeletedFalse(Long invitedUserId, InvitationStatus status);
}
