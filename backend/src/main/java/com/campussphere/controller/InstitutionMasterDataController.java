package com.campussphere.controller;

import com.campussphere.dto.ApiResponse;
import com.campussphere.dto.PageResponse;
import com.campussphere.dto.masterdata.AcademicYearDtos.AcademicYearRequest;
import com.campussphere.dto.masterdata.AcademicYearDtos.AcademicYearResponse;
import com.campussphere.dto.masterdata.DepartmentDtos.DepartmentRequest;
import com.campussphere.dto.masterdata.DepartmentDtos.DepartmentResponse;
import com.campussphere.dto.masterdata.DepartmentProgrammeDtos.DepartmentProgrammeRequest;
import com.campussphere.dto.masterdata.DepartmentProgrammeDtos.DepartmentProgrammeResponse;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionRequest;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionResponse;
import com.campussphere.dto.masterdata.InstitutionDtos.InstitutionSummaryResponse;
import com.campussphere.dto.masterdata.ProgrammeDtos.ProgrammeRequest;
import com.campussphere.dto.masterdata.ProgrammeDtos.ProgrammeResponse;
import com.campussphere.dto.masterdata.SectionDtos.SectionRequest;
import com.campussphere.dto.masterdata.SectionDtos.SectionResponse;
import com.campussphere.dto.masterdata.SemesterDtos.SemesterRequest;
import com.campussphere.dto.masterdata.SemesterDtos.SemesterResponse;
import com.campussphere.service.InstitutionMasterDataService;
import com.campussphere.util.ApiResponseFactory;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InstitutionMasterDataController {

    private final InstitutionMasterDataService service;

    public InstitutionMasterDataController(InstitutionMasterDataService service) {
        this.service = service;
    }

    @GetMapping("/institutions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<InstitutionSummaryResponse>> listInstitutions(@RequestParam(required = false) String search,
                                                                                  @RequestParam(required = false) Boolean active,
                                                                                  @RequestParam(defaultValue = "0") int page,
                                                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Institutions retrieved successfully.", service.listInstitutions(currentUserEmail(), search, active, page, size));
    }

    @GetMapping("/institutions/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<InstitutionResponse> getInstitution(@PathVariable Long id) {
        return ApiResponseFactory.success("Institution retrieved successfully.", service.getInstitution(currentUserEmail(), id));
    }

    @PostMapping("/institutions")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<InstitutionResponse> createInstitution(@Valid @RequestBody InstitutionRequest request) {
        return ApiResponseFactory.success("Institution created successfully.", service.createInstitution(currentUserEmail(), request));
    }

    @PutMapping("/institutions/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<InstitutionResponse> updateInstitution(@PathVariable Long id, @Valid @RequestBody InstitutionRequest request) {
        return ApiResponseFactory.success("Institution updated successfully.", service.updateInstitution(currentUserEmail(), id, request));
    }

    @PatchMapping("/institutions/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<InstitutionResponse> updateInstitutionStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Institution status updated successfully.", service.updateInstitutionStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/departments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<DepartmentResponse>> listDepartments(@RequestParam(required = false) Long institutionId,
                                                                         @RequestParam(required = false) String search,
                                                                         @RequestParam(required = false) Boolean active,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Departments retrieved successfully.", service.listDepartments(currentUserEmail(), institutionId, search, active, page, size));
    }

    @GetMapping("/departments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DepartmentResponse> getDepartment(@PathVariable Long id) {
        return ApiResponseFactory.success("Department retrieved successfully.", service.getDepartment(currentUserEmail(), id));
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponseFactory.success("Department created successfully.", service.createDepartment(currentUserEmail(), request));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<DepartmentResponse> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponseFactory.success("Department updated successfully.", service.updateDepartment(currentUserEmail(), id, request));
    }

    @PatchMapping("/departments/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<DepartmentResponse> updateDepartmentStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Department status updated successfully.", service.updateDepartmentStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/academic-years")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<AcademicYearResponse>> listAcademicYears(@RequestParam(required = false) Long institutionId,
                                                                             @RequestParam(required = false) String search,
                                                                             @RequestParam(required = false) Boolean active,
                                                                             @RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Academic years retrieved successfully.", service.listAcademicYears(currentUserEmail(), institutionId, search, active, page, size));
    }

    @PostMapping("/academic-years")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AcademicYearResponse> createAcademicYear(@Valid @RequestBody AcademicYearRequest request) {
        return ApiResponseFactory.success("Academic year created successfully.", service.createAcademicYear(currentUserEmail(), request));
    }

    @PutMapping("/academic-years/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AcademicYearResponse> updateAcademicYear(@PathVariable Long id, @Valid @RequestBody AcademicYearRequest request) {
        return ApiResponseFactory.success("Academic year updated successfully.", service.updateAcademicYear(currentUserEmail(), id, request));
    }

    @PatchMapping("/academic-years/{id}/current")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AcademicYearResponse> setCurrentAcademicYear(@PathVariable Long id) {
        return ApiResponseFactory.success("Academic year marked as current successfully.", service.setCurrentAcademicYear(currentUserEmail(), id));
    }

    @PatchMapping("/academic-years/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<AcademicYearResponse> updateAcademicYearStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Academic year status updated successfully.", service.updateAcademicYearStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/programmes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<ProgrammeResponse>> listProgrammes(@RequestParam(required = false) Long institutionId,
                                                                       @RequestParam(required = false) String search,
                                                                       @RequestParam(required = false) Boolean active,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Programmes retrieved successfully.", service.listProgrammes(currentUserEmail(), institutionId, search, active, page, size));
    }

    @GetMapping("/programmes/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProgrammeResponse> getProgramme(@PathVariable Long id) {
        return ApiResponseFactory.success("Programme retrieved successfully.", service.getProgramme(currentUserEmail(), id));
    }

    @PostMapping("/programmes")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ProgrammeResponse> createProgramme(@Valid @RequestBody ProgrammeRequest request) {
        return ApiResponseFactory.success("Programme created successfully.", service.createProgramme(currentUserEmail(), request));
    }

    @PutMapping("/programmes/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ProgrammeResponse> updateProgramme(@PathVariable Long id, @Valid @RequestBody ProgrammeRequest request) {
        return ApiResponseFactory.success("Programme updated successfully.", service.updateProgramme(currentUserEmail(), id, request));
    }

    @PatchMapping("/programmes/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<ProgrammeResponse> updateProgrammeStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Programme status updated successfully.", service.updateProgrammeStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/department-programmes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<DepartmentProgrammeResponse>> listDepartmentProgrammes(@RequestParam(required = false) Long institutionId,
                                                                                          @RequestParam(required = false) Long departmentId,
                                                                                          @RequestParam(required = false) Long programmeId,
                                                                                          @RequestParam(required = false) Boolean active,
                                                                                          @RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Department-programme mappings retrieved successfully.", service.listDepartmentProgrammes(currentUserEmail(), institutionId, departmentId, programmeId, active, page, size));
    }

    @PostMapping("/department-programmes")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<DepartmentProgrammeResponse> createDepartmentProgramme(@Valid @RequestBody DepartmentProgrammeRequest request) {
        return ApiResponseFactory.success("Department-programme mapping created successfully.", service.createDepartmentProgramme(currentUserEmail(), request));
    }

    @PatchMapping("/department-programmes/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<DepartmentProgrammeResponse> updateDepartmentProgrammeStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Department-programme mapping status updated successfully.", service.updateDepartmentProgrammeStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/semesters")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<SemesterResponse>> listSemesters(@RequestParam(required = false) Long institutionId,
                                                                     @RequestParam(required = false) Long programmeId,
                                                                     @RequestParam(required = false) Boolean active,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Semesters retrieved successfully.", service.listSemesters(currentUserEmail(), institutionId, programmeId, active, page, size));
    }

    @PostMapping("/semesters")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<SemesterResponse> createSemester(@Valid @RequestBody SemesterRequest request) {
        return ApiResponseFactory.success("Semester created successfully.", service.createSemester(currentUserEmail(), request));
    }

    @PatchMapping("/semesters/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<SemesterResponse> updateSemesterStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Semester status updated successfully.", service.updateSemesterStatus(currentUserEmail(), id, active));
    }

    @GetMapping("/sections")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<SectionResponse>> listSections(@RequestParam(required = false) Long institutionId,
                                                                    @RequestParam(required = false) Long departmentId,
                                                                    @RequestParam(required = false) Long programmeId,
                                                                    @RequestParam(required = false) Long academicYearId,
                                                                    @RequestParam(required = false) Long semesterId,
                                                                    @RequestParam(required = false) Boolean active,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResponseFactory.success("Sections retrieved successfully.", service.listSections(currentUserEmail(), institutionId, departmentId, programmeId, academicYearId, semesterId, active, page, size));
    }

    @PostMapping("/sections")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<SectionResponse> createSection(@Valid @RequestBody SectionRequest request) {
        return ApiResponseFactory.success("Section created successfully.", service.createSection(currentUserEmail(), request));
    }

    @PatchMapping("/sections/{id}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ApiResponse<SectionResponse> updateSectionStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ApiResponseFactory.success("Section status updated successfully.", service.updateSectionStatus(currentUserEmail(), id, active));
    }

    private String currentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
