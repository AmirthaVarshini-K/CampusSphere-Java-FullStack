package com.campussphere.entity.registration;

import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.Institution;
import com.campussphere.entity.User;
import com.campussphere.entity.event.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "teams",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teams_event_name", columnNames = {"event_id", "team_name"}),
                @UniqueConstraint(name = "uk_teams_event_code", columnNames = {"event_id", "team_code"})
        }
)
public class Team extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "team_name", nullable = false, length = 160)
    private String teamName;

    @Column(name = "team_code", nullable = false, length = 40)
    private String teamCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User leader;

    @Column(name = "primary_contact", length = 160)
    private String primaryContact;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public String getPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(String primaryContact) {
        this.primaryContact = primaryContact;
    }
}
