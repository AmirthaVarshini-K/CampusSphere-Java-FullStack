package com.campussphere.dto.institution;

import com.campussphere.entity.InstitutionType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class InstitutionRequest {

    @NotBlank(message = "Institution code is required.")
    @Size(max = 40, message = "Institution code is too long.")
    private String institutionCode;

    @NotBlank(message = "Institution name is required.")
    @Size(max = 160, message = "Institution name is too long.")
    private String institutionName;

    @Size(max = 80, message = "Short name is too long.")
    private String shortName;

    @NotNull(message = "Institution type is required.")
    private InstitutionType institutionType;

    @Size(max = 160, message = "Affiliation is too long.")
    private String affiliation;

    @Size(max = 160, message = "Accreditation is too long.")
    private String accreditation;

    @Email(message = "Email format is invalid.")
    @Size(max = 160, message = "Email is too long.")
    private String email;

    @Size(max = 24, message = "Phone is too long.")
    private String phone;

    @Size(max = 255, message = "Website is too long.")
    private String website;

    @Size(max = 255, message = "Address line 1 is too long.")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 is too long.")
    private String addressLine2;

    @Size(max = 100, message = "City is too long.")
    private String city;

    @Size(max = 100, message = "State is too long.")
    private String state;

    @Size(max = 100, message = "Country is too long.")
    private String country;

    @Size(max = 20, message = "Postal code is too long.")
    private String postalCode;

    @Size(max = 512, message = "Logo URL is too long.")
    private String logoUrl;

    @Size(max = 64, message = "Timezone is too long.")
    private String timezone;

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getAccreditation() {
        return accreditation;
    }

    public void setAccreditation(String accreditation) {
        this.accreditation = accreditation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
