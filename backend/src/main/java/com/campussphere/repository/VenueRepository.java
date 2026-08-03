package com.campussphere.repository;

import com.campussphere.entity.event.Venue;
import com.campussphere.entity.event.VenueType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long>, JpaSpecificationExecutor<Venue> {

    Optional<Venue> findByIdAndDeletedFalse(Long id);

    boolean existsByInstitution_IdAndVenueCodeIgnoreCaseAndDeletedFalse(Long institutionId, String venueCode);

    boolean existsByInstitution_IdAndVenueNameIgnoreCaseAndDeletedFalse(Long institutionId, String venueName);

    boolean existsByInstitution_IdAndVenueCodeIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String venueCode, Long id);

    boolean existsByInstitution_IdAndVenueNameIgnoreCaseAndIdNotAndDeletedFalse(Long institutionId, String venueName, Long id);

    long countByInstitution_IdAndVenueTypeAndDeletedFalse(Long institutionId, VenueType venueType);
}
