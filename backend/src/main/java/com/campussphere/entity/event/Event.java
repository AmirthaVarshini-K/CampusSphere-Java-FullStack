package com.campussphere.entity.event;

import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.Department;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "events",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_events_institution_code", columnNames = {"institution_id", "event_code"}),
                @UniqueConstraint(name = "uk_events_institution_slug", columnNames = {"institution_id", "slug"})
        }
)
public class Event extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(name = "event_code", nullable = false, length = 40)
    private String eventCode;

    @Column(length = 200)
    private String slug;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "full_description", length = 4000)
    private String fullDescription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_category_id", nullable = false)
    private EventCategory eventCategory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_type_id", nullable = false)
    private EventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizing_department_id")
    private Department organizingDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventMode mode = EventMode.OFFLINE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EventVisibility visibility = EventVisibility.INSTITUTION_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_status", nullable = false, length = 32)
    private EventStatus eventStatus = EventStatus.DRAFT;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "registration_start_date_time")
    private LocalDateTime registrationStartDateTime;

    @Column(name = "registration_end_date_time")
    private LocalDateTime registrationEndDateTime;

    @Column(name = "cancellation_deadline")
    private LocalDateTime cancellationDeadline;

    @Column(name = "online_meeting_url", length = 512)
    private String onlineMeetingUrl;

    @Column(name = "maximum_participants")
    private Integer maximumParticipants;

    @Column(name = "minimum_participants")
    private Integer minimumParticipants;

    @Column(name = "registration_fee", precision = 12, scale = 2)
    private BigDecimal registrationFee;

    @Column(length = 12)
    private String currency;

    @Column(name = "banner_image_url", length = 512)
    private String bannerImageUrl;

    @Column(name = "contact_email", length = 160)
    private String contactEmail;

    @Column(name = "contact_phone", length = 24)
    private String contactPhone;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Department getOrganizingDepartment() {
        return organizingDepartment;
    }

    public void setOrganizingDepartment(Department organizingDepartment) {
        this.organizingDepartment = organizingDepartment;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public EventMode getMode() {
        return mode;
    }

    public void setMode(EventMode mode) {
        this.mode = mode;
    }

    public EventVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(EventVisibility visibility) {
        this.visibility = visibility;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public LocalDateTime getRegistrationStartDateTime() {
        return registrationStartDateTime;
    }

    public void setRegistrationStartDateTime(LocalDateTime registrationStartDateTime) {
        this.registrationStartDateTime = registrationStartDateTime;
    }

    public LocalDateTime getRegistrationEndDateTime() {
        return registrationEndDateTime;
    }

    public void setRegistrationEndDateTime(LocalDateTime registrationEndDateTime) {
        this.registrationEndDateTime = registrationEndDateTime;
    }

    public LocalDateTime getCancellationDeadline() {
        return cancellationDeadline;
    }

    public void setCancellationDeadline(LocalDateTime cancellationDeadline) {
        this.cancellationDeadline = cancellationDeadline;
    }

    public String getOnlineMeetingUrl() {
        return onlineMeetingUrl;
    }

    public void setOnlineMeetingUrl(String onlineMeetingUrl) {
        this.onlineMeetingUrl = onlineMeetingUrl;
    }

    public Integer getMaximumParticipants() {
        return maximumParticipants;
    }

    public void setMaximumParticipants(Integer maximumParticipants) {
        this.maximumParticipants = maximumParticipants;
    }

    public Integer getMinimumParticipants() {
        return minimumParticipants;
    }

    public void setMinimumParticipants(Integer minimumParticipants) {
        this.minimumParticipants = minimumParticipants;
    }

    public BigDecimal getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(BigDecimal registrationFee) {
        this.registrationFee = registrationFee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(Long approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }
}
