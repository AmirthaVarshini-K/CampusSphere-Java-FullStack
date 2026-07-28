package com.campussphere.dto.auth;

import com.campussphere.validation.StrongPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StudentRegistrationRequest {

    @NotBlank(message = "First name is required.")
    @Size(max = 80, message = "First name is too long.")
    private String firstName;

    @NotBlank(message = "Last name is required.")
    @Size(max = 80, message = "Last name is too long.")
    private String lastName;

    @NotBlank(message = "Register number is required.")
    @Pattern(regexp = "^[A-Za-z0-9/-]{4,40}$", message = "Register number format is invalid.")
    private String registerNumber;

    @NotBlank(message = "Department is required.")
    @Size(max = 120, message = "Department is too long.")
    private String department;

    @NotBlank(message = "Academic year is required.")
    @Pattern(regexp = "^(I|II|III|IV|1|2|3|4)$", message = "Academic year is invalid.")
    private String academicYear;

    @NotBlank(message = "Section is required.")
    @Pattern(regexp = "^[A-Z]$", message = "Section is invalid.")
    private String section;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    @Size(max = 160, message = "Email is too long.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number is invalid.")
    private String phoneNumber;

    @StrongPassword
    private String password;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;

    private String profilePictureUrl;

    @AssertTrue(message = "You must accept the terms to continue.")
    private boolean termsAccepted;

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

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }
}
