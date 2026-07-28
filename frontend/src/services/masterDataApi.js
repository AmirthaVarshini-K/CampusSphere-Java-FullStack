import apiClient from './apiClient';

function buildQuery(params = {}) {
  return {
    params
  };
}

export const masterDataApi = {
  listInstitutions(params) {
    return apiClient.get('/institutions', buildQuery(params));
  },
  getInstitution(id) {
    return apiClient.get(`/institutions/${id}`);
  },
  createInstitution(payload) {
    return apiClient.post('/institutions', payload);
  },
  updateInstitution(id, payload) {
    return apiClient.put(`/institutions/${id}`, payload);
  },
  updateInstitutionStatus(id, active) {
    return apiClient.patch(`/institutions/${id}/status`, null, buildQuery({ active }));
  },
  listDepartments(params) {
    return apiClient.get('/departments', buildQuery(params));
  },
  getDepartment(id) {
    return apiClient.get(`/departments/${id}`);
  },
  createDepartment(payload) {
    return apiClient.post('/departments', payload);
  },
  updateDepartment(id, payload) {
    return apiClient.put(`/departments/${id}`, payload);
  },
  updateDepartmentStatus(id, active) {
    return apiClient.patch(`/departments/${id}/status`, null, buildQuery({ active }));
  },
  listAcademicYears(params) {
    return apiClient.get('/academic-years', buildQuery(params));
  },
  getAcademicYear(id) {
    return apiClient.get(`/academic-years/${id}`);
  },
  createAcademicYear(payload) {
    return apiClient.post('/academic-years', payload);
  },
  updateAcademicYear(id, payload) {
    return apiClient.put(`/academic-years/${id}`, payload);
  },
  setCurrentAcademicYear(id) {
    return apiClient.patch(`/academic-years/${id}/current`);
  },
  updateAcademicYearStatus(id, active) {
    return apiClient.patch(`/academic-years/${id}/status`, null, buildQuery({ active }));
  },
  listProgrammes(params) {
    return apiClient.get('/programmes', buildQuery(params));
  },
  getProgramme(id) {
    return apiClient.get(`/programmes/${id}`);
  },
  createProgramme(payload) {
    return apiClient.post('/programmes', payload);
  },
  updateProgramme(id, payload) {
    return apiClient.put(`/programmes/${id}`, payload);
  },
  updateProgrammeStatus(id, active) {
    return apiClient.patch(`/programmes/${id}/status`, null, buildQuery({ active }));
  },
  listDepartmentProgrammes(params) {
    return apiClient.get('/department-programmes', buildQuery(params));
  },
  createDepartmentProgramme(payload) {
    return apiClient.post('/department-programmes', payload);
  },
  updateDepartmentProgrammeStatus(id, active) {
    return apiClient.patch(`/department-programmes/${id}/status`, null, buildQuery({ active }));
  },
  listSemesters(params) {
    return apiClient.get('/semesters', buildQuery(params));
  },
  getSemester(id) {
    return apiClient.get(`/semesters/${id}`);
  },
  createSemester(payload) {
    return apiClient.post('/semesters', payload);
  },
  updateSemester(id, payload) {
    return apiClient.put(`/semesters/${id}`, payload);
  },
  updateSemesterStatus(id, active) {
    return apiClient.patch(`/semesters/${id}/status`, null, buildQuery({ active }));
  },
  listSections(params) {
    return apiClient.get('/sections', buildQuery(params));
  },
  getSection(id) {
    return apiClient.get(`/sections/${id}`);
  },
  createSection(payload) {
    return apiClient.post('/sections', payload);
  },
  updateSection(id, payload) {
    return apiClient.put(`/sections/${id}`, payload);
  },
  updateSectionStatus(id, active) {
    return apiClient.patch(`/sections/${id}/status`, null, buildQuery({ active }));
  }
};
