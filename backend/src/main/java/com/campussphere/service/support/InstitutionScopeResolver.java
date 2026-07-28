package com.campussphere.service.support;

import com.campussphere.entity.Institution;
import com.campussphere.entity.User;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.InstitutionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class InstitutionScopeResolver {

    private final InstitutionRepository institutionRepository;
    private final CurrentUserContext currentUserContext;

    public InstitutionScopeResolver(InstitutionRepository institutionRepository, CurrentUserContext currentUserContext) {
        this.institutionRepository = institutionRepository;
        this.currentUserContext = currentUserContext;
    }

    public Institution resolveForWrite(User currentUser, Long requestedInstitutionId) {
        if (currentUserContext.isAdministrator(currentUser)) {
            if (requestedInstitutionId == null) {
                throw new IllegalArgumentException("Institution is required.");
            }
            return institutionRepository.findById(requestedInstitutionId)
                    .filter(institution -> !institution.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found."));
        }

        if (currentUser.getInstitution() == null) {
            throw new AccessDeniedException("Your account is not assigned to an institution.");
        }
        if (requestedInstitutionId != null && !currentUser.getInstitution().getId().equals(requestedInstitutionId)) {
            throw new AccessDeniedException("You cannot modify another institution's data.");
        }
        return currentUser.getInstitution();
    }

    public Institution resolveForRead(User currentUser, Long requestedInstitutionId) {
        if (currentUserContext.isAdministrator(currentUser)) {
            if (requestedInstitutionId == null) {
                return null;
            }
            return institutionRepository.findById(requestedInstitutionId)
                    .filter(institution -> !institution.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Institution not found."));
        }

        if (currentUser.getInstitution() == null) {
            throw new AccessDeniedException("Your account is not assigned to an institution.");
        }
        if (requestedInstitutionId != null && !currentUser.getInstitution().getId().equals(requestedInstitutionId)) {
            throw new AccessDeniedException("You cannot access another institution's data.");
        }
        return currentUser.getInstitution();
    }
}
