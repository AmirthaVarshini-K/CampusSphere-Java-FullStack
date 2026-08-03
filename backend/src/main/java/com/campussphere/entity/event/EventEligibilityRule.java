package com.campussphere.entity.event;

import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.Department;
import com.campussphere.entity.Programme;
import com.campussphere.entity.Section;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_eligibility_rules")
public class EventEligibilityRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id")
    private Programme programme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Enumerated(EnumType.STRING)
    @Column(name = "participant_type", nullable = false, length = 24)
    private ParticipantType participantType = ParticipantType.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 24)
    private EligibilityRuleType ruleType = EligibilityRuleType.INCLUDE;

    @Column(name = "minimum_year")
    private Integer minimumYear;

    @Column(name = "maximum_year")
    private Integer maximumYear;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Programme getProgramme() {
        return programme;
    }

    public void setProgramme(Programme programme) {
        this.programme = programme;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public ParticipantType getParticipantType() {
        return participantType;
    }

    public void setParticipantType(ParticipantType participantType) {
        this.participantType = participantType;
    }

    public EligibilityRuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(EligibilityRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Integer getMinimumYear() {
        return minimumYear;
    }

    public void setMinimumYear(Integer minimumYear) {
        this.minimumYear = minimumYear;
    }

    public Integer getMaximumYear() {
        return maximumYear;
    }

    public void setMaximumYear(Integer maximumYear) {
        this.maximumYear = maximumYear;
    }
}
