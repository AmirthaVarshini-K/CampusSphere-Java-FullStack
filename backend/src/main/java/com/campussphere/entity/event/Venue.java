package com.campussphere.entity.event;

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
        name = "venues",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_venues_institution_code", columnNames = {"institution_id", "venue_code"}),
                @UniqueConstraint(name = "uk_venues_institution_name", columnNames = {"institution_id", "venue_name"})
        }
)
public class Venue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(name = "venue_code", nullable = false, length = 40)
    private String venueCode;

    @Column(name = "venue_name", nullable = false, length = 160)
    private String venueName;

    @Column(length = 120)
    private String building;

    @Column(length = 40)
    private String floor;

    @Column(name = "room_number", length = 40)
    private String roomNumber;

    @Column(length = 255)
    private String address;

    @Column
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "venue_type", nullable = false, length = 40)
    private VenueType venueType = VenueType.OTHER;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getVenueCode() {
        return venueCode;
    }

    public void setVenueCode(String venueCode) {
        this.venueCode = venueCode;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public VenueType getVenueType() {
        return venueType;
    }

    public void setVenueType(VenueType venueType) {
        this.venueType = venueType;
    }
}
