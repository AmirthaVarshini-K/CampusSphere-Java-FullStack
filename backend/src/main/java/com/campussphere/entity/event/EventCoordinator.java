package com.campussphere.entity.event;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "event_coordinators",
        uniqueConstraints = @UniqueConstraint(name = "uk_event_coordinators_event_user_role", columnNames = {"event_id", "user_id", "coordinator_role"})
)
public class EventCoordinator extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "coordinator_role", nullable = false, length = 40)
    private CoordinatorRole coordinatorRole = CoordinatorRole.COORDINATOR;

    @Column(name = "primary_coordinator", nullable = false)
    private boolean primaryCoordinator;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CoordinatorRole getCoordinatorRole() {
        return coordinatorRole;
    }

    public void setCoordinatorRole(CoordinatorRole coordinatorRole) {
        this.coordinatorRole = coordinatorRole;
    }

    public boolean isPrimaryCoordinator() {
        return primaryCoordinator;
    }

    public void setPrimaryCoordinator(boolean primaryCoordinator) {
        this.primaryCoordinator = primaryCoordinator;
    }
}
