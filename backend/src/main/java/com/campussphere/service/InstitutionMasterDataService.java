package com.campussphere.service;

import com.campussphere.dto.PageResponse;
import com.campussphere.dto.masterdata.AcademicYearDtos.AcademicYearOptionResponse;
import com.campussphere.dto.masterdata.AcademicYearDtos.AcademicYearRequest;
import com.campussphere.dto.masterdata.AcademicYearDtos.AcademicYearResponse;
import com.campussphere.dto.masterdata.DepartmentDtos.DepartmentOptionResponse;
import com.campussphere.dto.masterdata.DepartmentDtos.DepartmentRequest;
import com.campussphere.dto.masterdata.DepartmentDtos.DepartmentResponse;
import com.campussphere.dto.masterdata.DepartmentProgrammeDtos.DepartmentProgrammeRequest;
import com.campussphere.dto.masterdata.DepartmentProgrammeDtos.DepartmentProgrammeResponse;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionOptionResponse;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionRequest;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionResponse;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionSummaryResponse;
import com.campussphere.dto.masterdata.ProgrammeDtos.ProgrammeOptionResponse;
import com.campussphere.dto.masterdata.ProgrammeDtos.ProgrammeRequest;
import com.campussphere.dto.masterdata.ProgrammeDtos.ProgrammeResponse;
import com.campussphere.dto.masterdata.SectionDtos.SectionRequest;
import com.campussphere.dto.masterdata.SectionDtos.SectionResponse;
import com.campussphere.dto.masterdata.SemesterDtos.SemesterRequest;
import com.campussphere.dto.masterdata.SemesterDtos.SemesterResponse;
import com.campussphere.entity.AcademicYear;
import com.campussphere.entity.Department;
import com.campussphere.entity.DepartmentProgramme;
import com.campussphere.entity.Institution;
import com.campussphere.entity.RecordStatus;
import com.campussphere.entity.Programme;
import com.campussphere.entity.Section;
import com.campussphere.entity.Semester;
import com.campussphere.entity.User;
import com.campussphere.exception.BusinessRuleViolationException;
import com.campussphere.exception.DuplicateResourceException;
import com.campussphere.exception.InvalidInstitutionRelationshipException;
import com.campussphere.exception.ResourceNotFoundException;
import com.campussphere.repository.AcademicYearRepository;
import com.campussphere.repository.DepartmentRepository;
import com.campussphere.repository.DepartmentProgrammeRepository;
import com.campussphere.repository.InstitutionRepository;
import com.campussphere.repository.ProgrammeRepository;
import com.campussphere.repository.SectionRepository;
import com.campussphere.repository.SemesterRepository;
import com.campussphere.repository.UserRepository;
import com.campussphere.service.support.CurrentUserContext;
import com.campussphere.service.support.InstitutionScopeResolver;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class InstitutionMasterDataService {

    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ProgrammeRepository programmeRepository;
    private final DepartmentProgrammeRepository departmentProgrammeRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final CurrentUserContext currentUserContext;
    private final InstitutionScopeResolver scopeResolver;

    public InstitutionMasterDataService(
            InstitutionRepository institutionRepository,
            DepartmentRepository departmentRepository,
            AcademicYearRepository academicYearRepository,
            ProgrammeRepository programmeRepository,
            DepartmentProgrammeRepository departmentProgrammeRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            CurrentUserContext currentUserContext,
            InstitutionScopeResolver scopeResolver
    ) {
        this.institutionRepository = institutionRepository;
        this.departmentRepository = departmentRepository;
        this.academicYearRepository = academicYearRepository;
        this.programmeRepository = programmeRepository;
        this.departmentProgrammeRepository = departmentProgrammeRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.currentUserContext = currentUserContext;
        this.scopeResolver = scopeResolver;
    }

    public PageResponse<InstitutionSummaryResponse> listInstitutions(String currentUserEmail, String search, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        if (!currentUserContext.isAdministrator(currentUser)) {
            Institution institution = currentUser.getInstitution();
            if (institution == null) {
                throw new org.springframework.security.access.AccessDeniedException("Your account is not assigned to an institution.");
            }
            return page(
                    List.of(toInstitutionSummary(institution)),
                    page,
                    size
            );
        }

        List<Institution> institutions = institutionRepository.findAll().stream()
                .filter(institution -> !institution.isDeleted())
                .filter(institution -> matchesInstitutionSearch(institution, search))
                .filter(institution -> active == null || isActive(institution) == active)
                .sorted(Comparator.comparing(Institution::getInstitutionName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return page(institutions.stream().map(this::toInstitutionSummary).toList(), page, size);
    }

    public InstitutionResponse getInstitution(String currentUserEmail, Long id) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = institutionRepository.findById(id)
                .filter(entity -> !entity.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found."));
        if (!currentUserContext.isAdministrator(currentUser) && currentUser.getInstitution() != null
                && !Objects.equals(currentUser.getInstitution().getId(), institution.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another institution's data.");
        }
        return toInstitutionResponse(institution);
    }

    public InstitutionResponse createInstitution(String currentUserEmail, InstitutionRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        ensureAdministrator(currentUser);
        validateInstitutionDuplicates(request.institutionCode(), request.institutionName(), null);
        Institution institution = new Institution();
        applyInstitutionRequest(institution, request);
        institution.setStatus(RecordStatus.ACTIVE);
        institutionRepository.save(institution);
        return toInstitutionResponse(institution);
    }

    public InstitutionResponse updateInstitution(String currentUserEmail, Long id, InstitutionRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        ensureAdministrator(currentUser);
        Institution institution = institutionRepository.findById(id)
                .filter(entity -> !entity.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found."));
        validateInstitutionDuplicates(request.institutionCode(), request.institutionName(), institution.getId());
        applyInstitutionRequest(institution, request);
        institutionRepository.save(institution);
        return toInstitutionResponse(institution);
    }

    public InstitutionResponse updateInstitutionStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        ensureAdministrator(currentUser);
        Institution institution = institutionRepository.findById(id)
                .filter(entity -> !entity.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Institution not found."));
        institution.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        institutionRepository.save(institution);
        return toInstitutionResponse(institution);
    }

    public PageResponse<DepartmentResponse> listDepartments(String currentUserEmail, Long institutionId, String search, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<Department> departments = departmentRepository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .filter(entity -> scope == null || Objects.equals(entity.getInstitution().getId(), scope.getId()))
                .filter(entity -> matchesDepartmentSearch(entity, search))
                .filter(entity -> active == null || isActive(entity) == active)
                .sorted(Comparator.comparing(Department::getDepartmentName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return page(departments.stream().map(this::toDepartmentResponse).toList(), page, size);
    }

    public DepartmentResponse getDepartment(String currentUserEmail, Long id) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        scopeResolver.resolveForRead(currentUser, department.getInstitution().getId());
        return toDepartmentResponse(department);
    }

    public DepartmentResponse createDepartment(String currentUserEmail, DepartmentRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateDepartmentDuplicates(institution.getId(), request.departmentCode(), request.departmentName(), null);
        Department department = new Department();
        department.setInstitution(institution);
        department.setDepartmentCode(trim(request.departmentCode()));
        department.setDepartmentName(trim(request.departmentName()));
        department.setShortName(trimToNull(request.shortName()));
        department.setDescription(trimToNull(request.description()));
        department.setDepartmentEmail(trimToNull(request.departmentEmail()));
        department.setDepartmentPhone(trimToNull(request.departmentPhone()));
        if (request.headOfDepartmentUserId() != null) {
            department.setHeadOfDepartment(resolveUser(request.headOfDepartmentUserId()));
        }
        department.setStatus(RecordStatus.ACTIVE);
        departmentRepository.save(department);
        return toDepartmentResponse(department);
    }

    public DepartmentResponse updateDepartment(String currentUserEmail, Long id, DepartmentRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId() != null ? request.institutionId() : department.getInstitution().getId());
        if (!Objects.equals(institution.getId(), department.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("Department cannot be moved to a different institution.");
        }
        validateDepartmentDuplicates(institution.getId(), request.departmentCode(), request.departmentName(), department.getId());
        department.setDepartmentCode(trim(request.departmentCode()));
        department.setDepartmentName(trim(request.departmentName()));
        department.setShortName(trimToNull(request.shortName()));
        department.setDescription(trimToNull(request.description()));
        department.setDepartmentEmail(trimToNull(request.departmentEmail()));
        department.setDepartmentPhone(trimToNull(request.departmentPhone()));
        if (request.headOfDepartmentUserId() != null) {
            department.setHeadOfDepartment(resolveUser(request.headOfDepartmentUserId()));
        }
        departmentRepository.save(department);
        return toDepartmentResponse(department);
    }

    public DepartmentResponse updateDepartmentStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Department department = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        scopeResolver.resolveForWrite(currentUser, department.getInstitution().getId());
        department.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        departmentRepository.save(department);
        return toDepartmentResponse(department);
    }

    public PageResponse<AcademicYearResponse> listAcademicYears(String currentUserEmail, Long institutionId, String search, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<AcademicYear> years = academicYearRepository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .filter(entity -> scope == null || Objects.equals(entity.getInstitution().getId(), scope.getId()))
                .filter(entity -> matchesAcademicYearSearch(entity, search))
                .filter(entity -> active == null || isActive(entity) == active)
                .sorted(Comparator.comparing(AcademicYear::getStartDate).reversed())
                .toList();
        return page(years.stream().map(this::toAcademicYearResponse).toList(), page, size);
    }

    public AcademicYearResponse getAcademicYear(String currentUserEmail, Long id) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        AcademicYear year = academicYearRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        scopeResolver.resolveForRead(currentUser, year.getInstitution().getId());
        return toAcademicYearResponse(year);
    }

    public AcademicYearResponse createAcademicYear(String currentUserEmail, AcademicYearRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateAcademicYearDates(request.startDate(), request.endDate());
        validateAcademicYearDuplicate(institution.getId(), request.yearLabel(), null);
        AcademicYear year = new AcademicYear();
        year.setInstitution(institution);
        year.setYearLabel(trim(request.yearLabel()));
        year.setStartDate(request.startDate());
        year.setEndDate(request.endDate());
        year.setRegistrationOpen(request.registrationOpen());
        year.setCurrentYear(request.currentYear());
        if (request.currentYear()) {
            unsetCurrentAcademicYear(institution.getId(), null);
        }
        year.setStatus(RecordStatus.ACTIVE);
        academicYearRepository.save(year);
        return toAcademicYearResponse(year);
    }

    public AcademicYearResponse updateAcademicYear(String currentUserEmail, Long id, AcademicYearRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        AcademicYear year = academicYearRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId() != null ? request.institutionId() : year.getInstitution().getId());
        if (!Objects.equals(institution.getId(), year.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("Academic year cannot be moved to a different institution.");
        }
        validateAcademicYearDates(request.startDate(), request.endDate());
        validateAcademicYearDuplicate(institution.getId(), request.yearLabel(), year.getId());
        year.setYearLabel(trim(request.yearLabel()));
        year.setStartDate(request.startDate());
        year.setEndDate(request.endDate());
        year.setRegistrationOpen(request.registrationOpen());
        year.setCurrentYear(request.currentYear());
        if (request.currentYear()) {
            unsetCurrentAcademicYear(institution.getId(), year.getId());
        }
        academicYearRepository.save(year);
        return toAcademicYearResponse(year);
    }

    public AcademicYearResponse updateAcademicYearStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        AcademicYear year = academicYearRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        scopeResolver.resolveForWrite(currentUser, year.getInstitution().getId());
        year.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        academicYearRepository.save(year);
        return toAcademicYearResponse(year);
    }

    public AcademicYearResponse setCurrentAcademicYear(String currentUserEmail, Long id) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        AcademicYear year = academicYearRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        Institution institution = scopeResolver.resolveForWrite(currentUser, year.getInstitution().getId());
        unsetCurrentAcademicYear(institution.getId(), year.getId());
        year.setCurrentYear(true);
        year.setStatus(RecordStatus.ACTIVE);
        academicYearRepository.save(year);
        return toAcademicYearResponse(year);
    }

    public PageResponse<ProgrammeResponse> listProgrammes(String currentUserEmail, Long institutionId, String search, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<Programme> programmes = programmeRepository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .filter(entity -> scope == null || Objects.equals(entity.getInstitution().getId(), scope.getId()))
                .filter(entity -> matchesProgrammeSearch(entity, search))
                .filter(entity -> active == null || isActive(entity) == active)
                .sorted(Comparator.comparing(Programme::getProgrammeName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return page(programmes.stream().map(this::toProgrammeResponse).toList(), page, size);
    }

    public ProgrammeResponse getProgramme(String currentUserEmail, Long id) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Programme programme = programmeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found."));
        scopeResolver.resolveForRead(currentUser, programme.getInstitution().getId());
        return toProgrammeResponse(programme);
    }

    public ProgrammeResponse createProgramme(String currentUserEmail, ProgrammeRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        validateProgrammeDuplicates(institution.getId(), request.programmeCode(), request.programmeName(), null);
        Programme programme = new Programme();
        programme.setInstitution(institution);
        programme.setProgrammeCode(trim(request.programmeCode()));
        programme.setProgrammeName(trim(request.programmeName()));
        programme.setProgrammeLevel(request.programmeLevel());
        programme.setDurationYears(request.durationYears());
        programme.setDurationSemesters(request.durationSemesters());
        programme.setStatus(RecordStatus.ACTIVE);
        programmeRepository.save(programme);
        return toProgrammeResponse(programme);
    }

    public ProgrammeResponse updateProgramme(String currentUserEmail, Long id, ProgrammeRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Programme programme = programmeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found."));
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId() != null ? request.institutionId() : programme.getInstitution().getId());
        if (!Objects.equals(institution.getId(), programme.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("Programme cannot be moved to a different institution.");
        }
        validateProgrammeDuplicates(institution.getId(), request.programmeCode(), request.programmeName(), programme.getId());
        programme.setProgrammeCode(trim(request.programmeCode()));
        programme.setProgrammeName(trim(request.programmeName()));
        programme.setProgrammeLevel(request.programmeLevel());
        programme.setDurationYears(request.durationYears());
        programme.setDurationSemesters(request.durationSemesters());
        programmeRepository.save(programme);
        return toProgrammeResponse(programme);
    }

    public ProgrammeResponse updateProgrammeStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Programme programme = programmeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found."));
        scopeResolver.resolveForWrite(currentUser, programme.getInstitution().getId());
        programme.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        programmeRepository.save(programme);
        return toProgrammeResponse(programme);
    }

    public PageResponse<DepartmentProgrammeResponse> listDepartmentProgrammes(String currentUserEmail, Long institutionId, Long departmentId, Long programmeId, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<DepartmentProgramme> mappings = departmentProgrammeRepository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .filter(entity -> scope == null || Objects.equals(entity.getInstitution().getId(), scope.getId()))
                .filter(entity -> departmentId == null || Objects.equals(entity.getDepartment().getId(), departmentId))
                .filter(entity -> programmeId == null || Objects.equals(entity.getProgramme().getId(), programmeId))
                .filter(entity -> active == null || isActive(entity) == active)
                .sorted(Comparator.comparing(mapping -> mapping.getDepartment().getDepartmentName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        return page(mappings.stream().map(this::toDepartmentProgrammeResponse).toList(), page, size);
    }

    public DepartmentProgrammeResponse createDepartmentProgramme(String currentUserEmail, DepartmentProgrammeRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        Department department = requireDepartment(institution.getId(), request.departmentId());
        Programme programme = requireProgramme(institution.getId(), request.programmeId());
        validateDepartmentProgrammeDuplicate(department.getId(), programme.getId(), null);
        DepartmentProgramme mapping = new DepartmentProgramme();
        mapping.setInstitution(institution);
        mapping.setDepartment(department);
        mapping.setProgramme(programme);
        mapping.setAcademicYear(request.academicYearId() == null ? null : requireAcademicYear(institution.getId(), request.academicYearId()));
        mapping.setIntakeCapacity(request.intakeCapacity());
        mapping.setStatus(RecordStatus.ACTIVE);
        departmentProgrammeRepository.save(mapping);
        return toDepartmentProgrammeResponse(mapping);
    }

    public DepartmentProgrammeResponse updateDepartmentProgrammeStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        DepartmentProgramme mapping = departmentProgrammeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department-programme mapping not found."));
        scopeResolver.resolveForWrite(currentUser, mapping.getInstitution().getId());
        mapping.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        departmentProgrammeRepository.save(mapping);
        return toDepartmentProgrammeResponse(mapping);
    }

    public PageResponse<SemesterResponse> listSemesters(String currentUserEmail, Long institutionId, Long programmeId, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<Semester> semesters = semesterRepository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .filter(entity -> scope == null || Objects.equals(entity.getInstitution().getId(), scope.getId()))
                .filter(entity -> programmeId == null || Objects.equals(entity.getProgramme().getId(), programmeId))
                .filter(entity -> active == null || isActive(entity) == active)
                .sorted(Comparator.comparingInt(Semester::getSemesterNumber))
                .toList();
        return page(semesters.stream().map(this::toSemesterResponse).toList(), page, size);
    }

    public SemesterResponse createSemester(String currentUserEmail, SemesterRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        Programme programme = requireProgramme(institution.getId(), request.programmeId());
        validateSemesterDuplicate(programme.getId(), request.semesterNumber(), null);
        validateSemesterRange(programme, request.semesterNumber());
        Semester semester = new Semester();
        semester.setInstitution(institution);
        semester.setProgramme(programme);
        semester.setSemesterNumber(request.semesterNumber());
        semester.setDisplayName(trim(request.displayName()));
        semester.setStatus(RecordStatus.ACTIVE);
        semesterRepository.save(semester);
        return toSemesterResponse(semester);
    }

    public SemesterResponse updateSemesterStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Semester semester = semesterRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found."));
        scopeResolver.resolveForWrite(currentUser, semester.getInstitution().getId());
        semester.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        semesterRepository.save(semester);
        return toSemesterResponse(semester);
    }

    public PageResponse<SectionResponse> listSections(String currentUserEmail, Long institutionId, Long departmentId, Long programmeId, Long academicYearId, Long semesterId, Boolean active, int page, int size) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution scope = scopeResolver.resolveForRead(currentUser, institutionId);
        List<Section> sections = sectionRepository.findAll().stream()
                .filter(entity -> !entity.isDeleted())
                .filter(entity -> scope == null || Objects.equals(entity.getInstitution().getId(), scope.getId()))
                .filter(entity -> departmentId == null || Objects.equals(entity.getDepartment().getId(), departmentId))
                .filter(entity -> programmeId == null || Objects.equals(entity.getProgramme().getId(), programmeId))
                .filter(entity -> academicYearId == null || Objects.equals(entity.getAcademicYear().getId(), academicYearId))
                .filter(entity -> semesterId == null || (entity.getSemester() != null && Objects.equals(entity.getSemester().getId(), semesterId)))
                .filter(entity -> active == null || isActive(entity) == active)
                .sorted(Comparator.comparing(Section::getSectionName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return page(sections.stream().map(this::toSectionResponse).toList(), page, size);
    }

    public SectionResponse createSection(String currentUserEmail, SectionRequest request) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Institution institution = scopeResolver.resolveForWrite(currentUser, request.institutionId());
        Department department = requireDepartment(institution.getId(), request.departmentId());
        Programme programme = requireProgramme(institution.getId(), request.programmeId());
        AcademicYear academicYear = requireAcademicYear(institution.getId(), request.academicYearId());
        Semester semester = requireSemester(institution.getId(), request.semesterId());
        validateSectionPlacement(department, programme, semester);
        validateSectionDuplicate(institution.getId(), department.getId(), programme.getId(), academicYear.getId(), semester.getId(), request.sectionName(), null);
        Section section = new Section();
        section.setInstitution(institution);
        section.setDepartment(department);
        section.setProgramme(programme);
        section.setAcademicYear(academicYear);
        section.setSemester(semester);
        section.setSectionName(trim(request.sectionName()));
        section.setCapacity(request.capacity());
        section.setStudyYear(request.studyYear());
        if (request.advisorUserId() != null) {
            section.setAdvisor(resolveUser(request.advisorUserId()));
        }
        section.setStatus(RecordStatus.ACTIVE);
        sectionRepository.save(section);
        return toSectionResponse(section);
    }

    public SectionResponse updateSectionStatus(String currentUserEmail, Long id, boolean active) {
        User currentUser = currentUserContext.requireCurrentUser(currentUserEmail);
        Section section = sectionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found."));
        scopeResolver.resolveForWrite(currentUser, section.getInstitution().getId());
        section.setStatus(active ? RecordStatus.ACTIVE : RecordStatus.INACTIVE);
        sectionRepository.save(section);
        return toSectionResponse(section);
    }

    private void ensureAdministrator(User currentUser) {
        if (!currentUserContext.isAdministrator(currentUser)) {
            throw new org.springframework.security.access.AccessDeniedException("Administrator access is required.");
        }
    }

    private void validateInstitutionDuplicates(String institutionCode, String institutionName, Long id) {
        String normalizedCode = trim(institutionCode);
        String normalizedName = trim(institutionName);
        if (id == null) {
            if (institutionRepository.existsByInstitutionCodeIgnoreCaseAndDeletedFalse(normalizedCode)) {
                throw new DuplicateResourceException("Institution code already exists.");
            }
            if (institutionRepository.existsByInstitutionNameIgnoreCaseAndDeletedFalse(normalizedName)) {
                throw new DuplicateResourceException("Institution name already exists.");
            }
        } else {
            if (institutionRepository.existsByInstitutionCodeIgnoreCaseAndIdNotAndDeletedFalse(normalizedCode, id)) {
                throw new DuplicateResourceException("Institution code already exists.");
            }
            if (institutionRepository.existsByInstitutionNameIgnoreCaseAndIdNotAndDeletedFalse(normalizedName, id)) {
                throw new DuplicateResourceException("Institution name already exists.");
            }
        }
    }

    private void applyInstitutionRequest(Institution institution, InstitutionRequest request) {
        institution.setInstitutionCode(trim(request.institutionCode()));
        institution.setInstitutionName(trim(request.institutionName()));
        institution.setShortName(trimToNull(request.shortName()));
        institution.setInstitutionType(request.institutionType());
        institution.setAffiliation(trimToNull(request.affiliation()));
        institution.setAccreditation(trimToNull(request.accreditation()));
        institution.setEmail(trimToNull(request.email()));
        institution.setPhone(trimToNull(request.phone()));
        institution.setWebsite(trimToNull(request.website()));
        institution.setAddressLine1(trimToNull(request.addressLine1()));
        institution.setAddressLine2(trimToNull(request.addressLine2()));
        institution.setCity(trimToNull(request.city()));
        institution.setState(trimToNull(request.state()));
        institution.setCountry(trimToNull(request.country()));
        institution.setPostalCode(trimToNull(request.postalCode()));
        institution.setLogoUrl(trimToNull(request.logoUrl()));
        institution.setTimezone(trimToNull(request.timezone()));
    }

    private InstitutionResponse toInstitutionResponse(Institution institution) {
        return new InstitutionResponse(
                institution.getId(),
                institution.getInstitutionCode(),
                institution.getInstitutionName(),
                institution.getShortName(),
                institution.getInstitutionType(),
                institution.getAffiliation(),
                institution.getAccreditation(),
                institution.getEmail(),
                institution.getPhone(),
                institution.getWebsite(),
                institution.getAddressLine1(),
                institution.getAddressLine2(),
                institution.getCity(),
                institution.getState(),
                institution.getCountry(),
                institution.getPostalCode(),
                institution.getLogoUrl(),
                institution.getTimezone(),
                isActive(institution),
                institution.getCreatedAt(),
                institution.getUpdatedAt()
        );
    }

    private InstitutionSummaryResponse toInstitutionSummary(Institution institution) {
        return new InstitutionSummaryResponse(institution.getId(), institution.getInstitutionCode(), institution.getInstitutionName(), isActive(institution));
    }

    private DepartmentResponse toDepartmentResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getInstitution().getId(),
                department.getInstitution().getInstitutionCode(),
                department.getInstitution().getInstitutionName(),
                department.getDepartmentCode(),
                department.getDepartmentName(),
                department.getShortName(),
                department.getDescription(),
                department.getDepartmentEmail(),
                department.getDepartmentPhone(),
                department.getHeadOfDepartment() == null ? null : department.getHeadOfDepartment().getId(),
                department.getHeadOfDepartment() == null ? null : department.getHeadOfDepartment().getFirstName() + " " + department.getHeadOfDepartment().getLastName(),
                isActive(department),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    private AcademicYearResponse toAcademicYearResponse(AcademicYear year) {
        return new AcademicYearResponse(
                year.getId(),
                year.getInstitution().getId(),
                year.getInstitution().getInstitutionCode(),
                year.getInstitution().getInstitutionName(),
                year.getYearLabel(),
                year.getStartDate(),
                year.getEndDate(),
                year.isCurrentYear(),
                year.isRegistrationOpen(),
                isActive(year),
                year.getCreatedAt(),
                year.getUpdatedAt()
        );
    }

    private ProgrammeResponse toProgrammeResponse(Programme programme) {
        return new ProgrammeResponse(
                programme.getId(),
                programme.getInstitution().getId(),
                programme.getInstitution().getInstitutionCode(),
                programme.getInstitution().getInstitutionName(),
                programme.getProgrammeCode(),
                programme.getProgrammeName(),
                programme.getProgrammeLevel(),
                programme.getDurationYears(),
                programme.getDurationSemesters(),
                isActive(programme),
                programme.getCreatedAt(),
                programme.getUpdatedAt()
        );
    }

    private boolean matchesInstitutionSearch(Institution institution, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = search.toLowerCase(Locale.ROOT);
        return contains(institution.getInstitutionCode(), term)
                || contains(institution.getInstitutionName(), term)
                || contains(institution.getShortName(), term)
                || contains(institution.getCity(), term)
                || contains(institution.getCountry(), term);
    }

    private boolean matchesDepartmentSearch(Department department, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = search.toLowerCase(Locale.ROOT);
        return contains(department.getDepartmentCode(), term)
                || contains(department.getDepartmentName(), term)
                || contains(department.getShortName(), term)
                || contains(department.getDescription(), term);
    }

    private boolean matchesAcademicYearSearch(AcademicYear year, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = search.toLowerCase(Locale.ROOT);
        return contains(year.getYearLabel(), term);
    }

    private boolean matchesProgrammeSearch(Programme programme, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = search.toLowerCase(Locale.ROOT);
        return contains(programme.getProgrammeCode(), term)
                || contains(programme.getProgrammeName(), term)
                || contains(programme.getProgrammeLevel() == null ? null : programme.getProgrammeLevel().name(), term);
    }

    private void validateDepartmentDuplicates(Long institutionId, String code, String name, Long id) {
        if (id == null) {
            if (departmentRepository.existsByInstitution_IdAndDepartmentCodeIgnoreCaseAndDeletedFalse(institutionId, trim(code))) {
                throw new DuplicateResourceException("Department code already exists for this institution.");
            }
            if (departmentRepository.existsByInstitution_IdAndDepartmentNameIgnoreCaseAndDeletedFalse(institutionId, trim(name))) {
                throw new DuplicateResourceException("Department name already exists for this institution.");
            }
        } else {
            if (departmentRepository.existsByInstitution_IdAndDepartmentCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, trim(code), id)) {
                throw new DuplicateResourceException("Department code already exists for this institution.");
            }
            if (departmentRepository.existsByInstitution_IdAndDepartmentNameIgnoreCaseAndIdNotAndDeletedFalse(institutionId, trim(name), id)) {
                throw new DuplicateResourceException("Department name already exists for this institution.");
            }
        }
    }

    private void validateAcademicYearDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new BusinessRuleViolationException("Academic year start date must be before the end date.");
        }
    }

    private void validateAcademicYearDuplicate(Long institutionId, String label, Long id) {
        if (id == null) {
            if (academicYearRepository.existsByInstitution_IdAndYearLabelIgnoreCaseAndDeletedFalse(institutionId, trim(label))) {
                throw new DuplicateResourceException("Academic year label already exists for this institution.");
            }
        } else if (academicYearRepository.existsByInstitution_IdAndYearLabelIgnoreCaseAndIdNotAndDeletedFalse(institutionId, trim(label), id)) {
            throw new DuplicateResourceException("Academic year label already exists for this institution.");
        }
    }

    private void unsetCurrentAcademicYear(Long institutionId, Long keepId) {
        academicYearRepository.findByInstitution_IdAndDeletedFalseOrderByStartDateDesc(institutionId).stream()
                .filter(year -> !Objects.equals(year.getId(), keepId))
                .filter(AcademicYear::isCurrentYear)
                .forEach(year -> {
                    year.setCurrentYear(false);
                    academicYearRepository.save(year);
                });
    }

    private void validateProgrammeDuplicates(Long institutionId, String code, String name, Long id) {
        if (id == null) {
            if (programmeRepository.existsByInstitution_IdAndProgrammeCodeIgnoreCaseAndDeletedFalse(institutionId, trim(code))) {
                throw new DuplicateResourceException("Programme code already exists for this institution.");
            }
            if (programmeRepository.existsByInstitution_IdAndProgrammeNameIgnoreCaseAndDeletedFalse(institutionId, trim(name))) {
                throw new DuplicateResourceException("Programme name already exists for this institution.");
            }
        } else {
            if (programmeRepository.existsByInstitution_IdAndProgrammeCodeIgnoreCaseAndIdNotAndDeletedFalse(institutionId, trim(code), id)) {
                throw new DuplicateResourceException("Programme code already exists for this institution.");
            }
            if (programmeRepository.existsByInstitution_IdAndProgrammeNameIgnoreCaseAndIdNotAndDeletedFalse(institutionId, trim(name), id)) {
                throw new DuplicateResourceException("Programme name already exists for this institution.");
            }
        }
    }

    private void validateDepartmentProgrammeDuplicate(Long departmentId, Long programmeId, Long id) {
        boolean exists = id == null
                ? departmentProgrammeRepository.existsByDepartment_IdAndProgramme_IdAndDeletedFalse(departmentId, programmeId)
                : departmentProgrammeRepository.existsByDepartment_IdAndProgramme_IdAndIdNotAndDeletedFalse(departmentId, programmeId, id);
        if (exists) {
            throw new DuplicateResourceException("Department and programme are already linked.");
        }
    }

    private void validateSemesterDuplicate(Long programmeId, int semesterNumber, Long id) {
        boolean exists = id == null
                ? semesterRepository.existsByProgramme_IdAndSemesterNumberAndDeletedFalse(programmeId, semesterNumber)
                : semesterRepository.existsByProgramme_IdAndSemesterNumberAndIdNotAndDeletedFalse(programmeId, semesterNumber, id);
        if (exists) {
            throw new DuplicateResourceException("Semester number already exists for this programme.");
        }
    }

    private void validateSemesterRange(Programme programme, int semesterNumber) {
        if (semesterNumber < 1) {
            throw new BusinessRuleViolationException("Semester number must be positive.");
        }
        if (semesterNumber > programme.getDurationSemesters()) {
            throw new BusinessRuleViolationException("Semester number cannot exceed the configured programme duration.");
        }
    }

    private void validateSectionPlacement(Department department, Programme programme, Semester semester) {
        if (!Objects.equals(department.getInstitution().getId(), programme.getInstitution().getId())
                || !Objects.equals(department.getInstitution().getId(), semester.getInstitution().getId())) {
            throw new InvalidInstitutionRelationshipException("All linked records must belong to the same institution.");
        }
        if (!departmentProgrammeRepository.existsByDepartment_IdAndProgramme_IdAndDeletedFalse(department.getId(), programme.getId())) {
            throw new InvalidInstitutionRelationshipException("Department must be mapped to the selected programme.");
        }
        if (!Objects.equals(semester.getProgramme().getId(), programme.getId())) {
            throw new InvalidInstitutionRelationshipException("Semester must belong to the selected programme.");
        }
    }

    private void validateSectionDuplicate(Long institutionId, Long departmentId, Long programmeId, Long academicYearId, Long semesterId, String sectionName, Long id) {
        boolean exists = id == null
                ? sectionRepository.existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndAcademicYear_IdAndSemester_IdAndSectionNameIgnoreCaseAndDeletedFalse(
                        institutionId, departmentId, programmeId, academicYearId, semesterId, trim(sectionName))
                : sectionRepository.existsByInstitution_IdAndDepartment_IdAndProgramme_IdAndAcademicYear_IdAndSemester_IdAndSectionNameIgnoreCaseAndIdNotAndDeletedFalse(
                        institutionId, departmentId, programmeId, academicYearId, semesterId, trim(sectionName), id);
        if (exists) {
            throw new DuplicateResourceException("Section already exists for the selected context.");
        }
    }

    private DepartmentProgrammeResponse toDepartmentProgrammeResponse(DepartmentProgramme mapping) {
        return new DepartmentProgrammeResponse(
                mapping.getId(),
                mapping.getInstitution().getId(),
                mapping.getInstitution().getInstitutionCode(),
                mapping.getDepartment().getId(),
                mapping.getDepartment().getDepartmentCode(),
                mapping.getDepartment().getDepartmentName(),
                mapping.getProgramme().getId(),
                mapping.getProgramme().getProgrammeCode(),
                mapping.getProgramme().getProgrammeName(),
                mapping.getAcademicYear() == null ? null : mapping.getAcademicYear().getId(),
                mapping.getAcademicYear() == null ? null : mapping.getAcademicYear().getYearLabel(),
                mapping.getIntakeCapacity(),
                isActive(mapping),
                mapping.getCreatedAt(),
                mapping.getUpdatedAt()
        );
    }

    private SemesterResponse toSemesterResponse(Semester semester) {
        return new SemesterResponse(
                semester.getId(),
                semester.getInstitution().getId(),
                semester.getInstitution().getInstitutionCode(),
                semester.getProgramme().getId(),
                semester.getProgramme().getProgrammeCode(),
                semester.getProgramme().getProgrammeName(),
                semester.getSemesterNumber(),
                semester.getDisplayName(),
                isActive(semester),
                semester.getCreatedAt(),
                semester.getUpdatedAt()
        );
    }

    private SectionResponse toSectionResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getInstitution().getId(),
                section.getInstitution().getInstitutionCode(),
                section.getDepartment().getId(),
                section.getDepartment().getDepartmentCode(),
                section.getDepartment().getDepartmentName(),
                section.getProgramme().getId(),
                section.getProgramme().getProgrammeCode(),
                section.getProgramme().getProgrammeName(),
                section.getAcademicYear().getId(),
                section.getAcademicYear().getYearLabel(),
                section.getSemester() == null ? null : section.getSemester().getId(),
                section.getSemester() == null ? 0 : section.getSemester().getSemesterNumber(),
                section.getSemester() == null ? null : section.getSemester().getDisplayName(),
                section.getSectionName(),
                section.getCapacity(),
                section.getStudyYear(),
                section.getAdvisor() == null ? null : section.getAdvisor().getId(),
                section.getAdvisor() == null ? null : section.getAdvisor().getFirstName() + " " + section.getAdvisor().getLastName(),
                isActive(section),
                section.getCreatedAt(),
                section.getUpdatedAt()
        );
    }

    private Department requireDepartment(Long institutionId, Long departmentId) {
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found."));
        if (!Objects.equals(department.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Department does not belong to the selected institution.");
        }
        return department;
    }

    private Programme requireProgramme(Long institutionId, Long programmeId) {
        Programme programme = programmeRepository.findByIdAndDeletedFalse(programmeId)
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found."));
        if (!Objects.equals(programme.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Programme does not belong to the selected institution.");
        }
        return programme;
    }

    private AcademicYear requireAcademicYear(Long institutionId, Long academicYearId) {
        AcademicYear year = academicYearRepository.findByIdAndDeletedFalse(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found."));
        if (!Objects.equals(year.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Academic year does not belong to the selected institution.");
        }
        return year;
    }

    private Semester requireSemester(Long institutionId, Long semesterId) {
        Semester semester = semesterRepository.findByIdAndDeletedFalse(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found."));
        if (!Objects.equals(semester.getInstitution().getId(), institutionId)) {
            throw new InvalidInstitutionRelationshipException("Semester does not belong to the selected institution.");
        }
        return semester;
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private <T> PageResponse<T> page(List<T> content, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(safePage * safeSize, content.size());
        int toIndex = Math.min(fromIndex + safeSize, content.size());
        List<T> pageContent = content.subList(fromIndex, toIndex);
        int totalPages = content.isEmpty() ? 0 : (int) Math.ceil((double) content.size() / safeSize);
        return PageResponse.of(pageContent, safePage, safeSize, content.size(), totalPages, safePage == 0, safePage >= Math.max(totalPages - 1, 0));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isActive(com.campussphere.entity.BaseEntity entity) {
        return entity.getStatus() == RecordStatus.ACTIVE && !entity.isDeleted();
    }

    private User resolveUser(Long id) {
        return userRepository.findWithRolesByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found."));
    }
}
