import apiClient from './apiClient';

function buildQuery(params = {}) {
  return {
    params
  };
}

export const attendanceApi = {
  getDashboard(params = {}) {
    return apiClient.get('/attendance/dashboard', buildQuery(params));
  },
  listSessions(params = {}) {
    return apiClient.get('/attendance/sessions', buildQuery(params));
  },
  openSession(payload) {
    return apiClient.post('/attendance/sessions', payload);
  },
  closeSession(sessionId) {
    return apiClient.patch(`/attendance/sessions/${sessionId}/close`);
  },
  generateQrToken(payload) {
    return apiClient.post('/attendance/qr-tokens', payload);
  },
  getTokenForRegistration(registrationId) {
    return apiClient.get(`/attendance/registrations/${registrationId}/qr-token`);
  },
  validateQrToken(payload) {
    return apiClient.post('/attendance/qr-tokens/validate', payload);
  },
  checkIn(payload) {
    return apiClient.post('/attendance/check-in', payload);
  },
  markManualAttendance(payload) {
    return apiClient.post('/attendance/manual', payload);
  },
  bulkAttendance(payload) {
    return apiClient.post('/attendance/bulk', payload);
  },
  undoAttendance(recordId, remarks) {
    return apiClient.patch(`/attendance/records/${recordId}/undo`, null, buildQuery({ remarks }));
  },
  listHistory(params = {}) {
    return apiClient.get('/attendance/history', buildQuery(params));
  },
  getReport(params = {}) {
    return apiClient.get('/attendance/reports', buildQuery(params));
  },
  exportReport(params = {}, format = 'csv') {
    return apiClient.get('/attendance/reports/export', {
      ...buildQuery({ ...params, format }),
      responseType: 'blob'
    });
  },
  invalidateToken(tokenId) {
    return apiClient.patch(`/attendance/qr-tokens/${tokenId}/invalidate`);
  }
};
