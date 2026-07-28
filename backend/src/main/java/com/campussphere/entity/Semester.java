package com.campussphere.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "semesters",
        uniqueConstraints = @UniqueConstraint(name = "uk_semesters_department_programme_number", columnNames = {"department_programme_id", "semester_number"})
)
public class Semester extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_programme_id", nullable = false)
    private DepartmentProgramme departmentProgramme;

    @Column(name = "semester_number", nullable = false)
    private int semesterNumber;

    @Column(name = "semester_name", nullable = false, length = 80)
    private String semesterName;

    public DepartmentProgramme getDepartmentProgramme() {
        return departmentProgramme;
    }

    public void setDepartmentProgramme(DepartmentProgramme departmentProgramme) {
        this.departmentProgramme = departmentProgramme;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getSemesterName() {
        return semesterName;
    }

    public void setSemesterName(String semesterName) {
        this.semesterName = semesterName;
    }
}
