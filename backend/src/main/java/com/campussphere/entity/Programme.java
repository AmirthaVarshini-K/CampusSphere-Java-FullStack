package com.campussphere.entity;

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
        name = "programmes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_programmes_institution_code", columnNames = {"institution_id", "programme_code"}),
                @UniqueConstraint(name = "uk_programmes_institution_name", columnNames = {"institution_id", "programme_name"})
        }
)
public class Programme extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "programme_code", nullable = false, length = 40)
    private String programmeCode;

    @Column(name = "programme_name", nullable = false, length = 160)
    private String programmeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "programme_level", nullable = false, length = 32)
    private ProgrammeLevel programmeLevel = ProgrammeLevel.UNDERGRADUATE;

    @Column(name = "duration_years", nullable = false)
    private int durationYears;

    @Column(name = "duration_semesters", nullable = false)
    private int durationSemesters;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getProgrammeCode() {
        return programmeCode;
    }

    public void setProgrammeCode(String programmeCode) {
        this.programmeCode = programmeCode;
    }

    public String getProgrammeName() {
        return programmeName;
    }

    public void setProgrammeName(String programmeName) {
        this.programmeName = programmeName;
    }

    public ProgrammeLevel getProgrammeLevel() {
        return programmeLevel;
    }

    public void setProgrammeLevel(ProgrammeLevel programmeLevel) {
        this.programmeLevel = programmeLevel;
    }

    public int getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(int durationYears) {
        this.durationYears = durationYears;
    }

    public int getDurationSemesters() {
        return durationSemesters;
    }

    public void setDurationSemesters(int durationSemesters) {
        this.durationSemesters = durationSemesters;
    }
}
