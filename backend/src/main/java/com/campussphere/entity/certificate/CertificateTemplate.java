package com.campussphere.entity.certificate;

import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.Institution;
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
        name = "certificate_templates",
        uniqueConstraints = @UniqueConstraint(name = "uk_certificate_templates_institution_code", columnNames = {"institution_id", "template_code"})
)
public class CertificateTemplate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "template_code", nullable = false, length = 40)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 160)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_type", nullable = false, length = 40)
    private CertificateType certificateType = CertificateType.PARTICIPATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private CertificateTemplateOrientation orientation = CertificateTemplateOrientation.PORTRAIT;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "institution_logo_url", length = 512)
    private String institutionLogoUrl;

    @Column(name = "organizer_logo_url", length = 512)
    private String organizerLogoUrl;

    @Column(name = "background_image_url", length = 512)
    private String backgroundImageUrl;

    @Column(name = "signature_left_url", length = 512)
    private String signatureLeftUrl;

    @Column(name = "signature_right_url", length = 512)
    private String signatureRightUrl;

    @Column(name = "seal_url", length = 512)
    private String sealUrl;

    @Column(name = "primary_color", length = 16)
    private String primaryColor;

    @Column(name = "accent_color", length = 16)
    private String accentColor;

    @Column(name = "watermark_text", length = 120)
    private String watermarkText;

    @Column(name = "margin_top_mm")
    private Integer marginTopMm;

    @Column(name = "margin_right_mm")
    private Integer marginRightMm;

    @Column(name = "margin_bottom_mm")
    private Integer marginBottomMm;

    @Column(name = "margin_left_mm")
    private Integer marginLeftMm;

    @Column(name = "qr_code_enabled", nullable = false)
    private boolean qrCodeEnabled = true;

    @Column(name = "verification_url_base", length = 512)
    private String verificationUrlBase;

    @Column(name = "template_html", columnDefinition = "TEXT")
    private String templateHtml;

    @Column(name = "variables_json", columnDefinition = "TEXT")
    private String variablesJson;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType certificateType) {
        this.certificateType = certificateType;
    }

    public CertificateTemplateOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(CertificateTemplateOrientation orientation) {
        this.orientation = orientation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstitutionLogoUrl() {
        return institutionLogoUrl;
    }

    public void setInstitutionLogoUrl(String institutionLogoUrl) {
        this.institutionLogoUrl = institutionLogoUrl;
    }

    public String getOrganizerLogoUrl() {
        return organizerLogoUrl;
    }

    public void setOrganizerLogoUrl(String organizerLogoUrl) {
        this.organizerLogoUrl = organizerLogoUrl;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String backgroundImageUrl) {
        this.backgroundImageUrl = backgroundImageUrl;
    }

    public String getSignatureLeftUrl() {
        return signatureLeftUrl;
    }

    public void setSignatureLeftUrl(String signatureLeftUrl) {
        this.signatureLeftUrl = signatureLeftUrl;
    }

    public String getSignatureRightUrl() {
        return signatureRightUrl;
    }

    public void setSignatureRightUrl(String signatureRightUrl) {
        this.signatureRightUrl = signatureRightUrl;
    }

    public String getSealUrl() {
        return sealUrl;
    }

    public void setSealUrl(String sealUrl) {
        this.sealUrl = sealUrl;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public Integer getMarginTopMm() {
        return marginTopMm;
    }

    public void setMarginTopMm(Integer marginTopMm) {
        this.marginTopMm = marginTopMm;
    }

    public Integer getMarginRightMm() {
        return marginRightMm;
    }

    public void setMarginRightMm(Integer marginRightMm) {
        this.marginRightMm = marginRightMm;
    }

    public Integer getMarginBottomMm() {
        return marginBottomMm;
    }

    public void setMarginBottomMm(Integer marginBottomMm) {
        this.marginBottomMm = marginBottomMm;
    }

    public Integer getMarginLeftMm() {
        return marginLeftMm;
    }

    public void setMarginLeftMm(Integer marginLeftMm) {
        this.marginLeftMm = marginLeftMm;
    }

    public boolean isQrCodeEnabled() {
        return qrCodeEnabled;
    }

    public void setQrCodeEnabled(boolean qrCodeEnabled) {
        this.qrCodeEnabled = qrCodeEnabled;
    }

    public String getVerificationUrlBase() {
        return verificationUrlBase;
    }

    public void setVerificationUrlBase(String verificationUrlBase) {
        this.verificationUrlBase = verificationUrlBase;
    }

    public String getTemplateHtml() {
        return templateHtml;
    }

    public void setTemplateHtml(String templateHtml) {
        this.templateHtml = templateHtml;
    }

    public String getVariablesJson() {
        return variablesJson;
    }

    public void setVariablesJson(String variablesJson) {
        this.variablesJson = variablesJson;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
