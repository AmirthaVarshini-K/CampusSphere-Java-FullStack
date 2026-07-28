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
  listDepartments(params) {
    return apiClient.get('/departments', buildQuery(params));
  },
  listAcademicYears(params) {
    return apiClient.get('/academic-years', buildQuery(params));
  },
  listProgrammes(params) {
    return apiClient.get('/programmes', buildQuery(params));
  },
  listDepartmentProgrammes(params) {
    return apiClient.get('/department-programmes', buildQuery(params));
  },
  listSemesters(params) {
    return apiClient.get('/semesters', buildQuery(params));
  },
  listSections(params) {
    return apiClient.get('/sections', buildQuery(params));
  }
};
