import apiClient from './apiClient';

function buildQuery(params = {}) {
  return {
    params
  };
}

export const analyticsApi = {
  getOverview(params) {
    return apiClient.get('/analytics/overview', buildQuery(params));
  },
  getMyInsights(params) {
    return apiClient.get('/analytics/me', buildQuery(params));
  },
  getCoordinatorInsights(params) {
    return apiClient.get('/analytics/coordinator', buildQuery(params));
  },
  listEvents(params) {
    return apiClient.get('/analytics/events', buildQuery(params));
  },
  listRegistrations(params) {
    return apiClient.get('/analytics/registrations', buildQuery(params));
  },
  listAttendance(params) {
    return apiClient.get('/analytics/attendance', buildQuery(params));
  },
  listCertificates(params) {
    return apiClient.get('/analytics/certificates', buildQuery(params));
  },
  listDepartments(params) {
    return apiClient.get('/analytics/departments', buildQuery(params));
  },
  exportReport(reportType, params) {
    return apiClient.get('/analytics/export', {
      params: { ...params, reportType },
      responseType: 'blob'
    });
  }
};
