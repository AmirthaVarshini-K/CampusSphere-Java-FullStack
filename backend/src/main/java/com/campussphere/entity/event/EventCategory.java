package com.campussphere.entity.event;

import com.campussphere.entity.BaseEntity;
import com.campussphere.entity.Institution;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "event_categories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_categories_institution_code", columnNames = {"institution_id", "category_code"}),
                @UniqueConstraint(name = "uk_event_categories_institution_name", columnNames = {"institution_id", "category_name"})
        }
)
public class EventCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "category_code", nullable = false, length = 40)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 160)
    private String categoryName;

    @Column(length = 255)
    private String description;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
