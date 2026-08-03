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
        name = "event_types",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_types_institution_code", columnNames = {"institution_id", "type_code"}),
                @UniqueConstraint(name = "uk_event_types_institution_name", columnNames = {"institution_id", "type_name"})
        }
)
public class EventType extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "type_code", nullable = false, length = 40)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 160)
    private String typeName;

    @Column(length = 255)
    private String description;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
