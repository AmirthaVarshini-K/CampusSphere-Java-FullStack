package com.campussphere.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(max = 80, message = "First name is too long.")
    private String firstName;

    @Size(max = 80, message = "Last name is too long.")
    private String lastName;

    @Email(message = "Email format is invalid.")
    @Size(max = 160, message = "Email is too long.")
    private String email;

    @Pattern(regexp = "^[A-Za-z0-9/-]{4,40}$", message = "Register number format is invalid.")
    private String registerNumber;

    @Pattern(regexp = "^[A-Za-z0-9/-]{4,40}$", message = "Employee ID format is invalid.")
    private String employeeId;

    @Size(max = 120, message = "Department is too long.")
    private String department;

    @Size(max = 16, message = "Academic year is too long.")
    private String academicYear;

    @Pattern(regexp = "^[A-Z]$", message = "Section is invalid.")
    private String section;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number is invalid.")
    private String phoneNumber;

    private String profilePictureUrl;

    private Boolean termsAccepted;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Boolean getTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(Boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }
}
