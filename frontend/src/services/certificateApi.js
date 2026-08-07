import apiClient from './apiClient';

function buildQuery(params = {}) {
  return { params };
}

export const certificateApi = {
  getDashboard(params) {
    return apiClient.get('/certificates/dashboard', buildQuery(params));
  },
  listCertificates(params) {
    return apiClient.get('/certificates', buildQuery(params));
  },
  listMyCertificates() {
    return apiClient.get('/certificates/me');
  },
  getCertificate(id) {
    return apiClient.get(`/certificates/${id}`);
  },
  issueCertificate(payload) {
    return apiClient.post('/certificates', payload);
  },
  previewCertificate(payload) {
    return apiClient.post('/certificates/preview', payload);
  },
  issueBulkCertificates(payload) {
    return apiClient.post('/certificates/bulk', payload);
  },
  regenerateCertificate(id) {
    return apiClient.post(`/certificates/${id}/regenerate`);
  },
  revokeCertificate(id, payload) {
    return apiClient.post(`/certificates/${id}/revoke`, payload);
  },
  downloadCertificate(id) {
    return apiClient.get(`/certificates/${id}/download`, { responseType: 'blob' });
  },
  listTemplates(params) {
    return apiClient.get('/certificate-templates', buildQuery(params));
  },
  getTemplate(id) {
    return apiClient.get(`/certificate-templates/${id}`);
  },
  previewTemplate(id) {
    return apiClient.get(`/certificate-templates/${id}/preview`);
  },
  createTemplate(payload) {
    return apiClient.post('/certificate-templates', payload);
  },
  updateTemplate(id, payload) {
    return apiClient.put(`/certificate-templates/${id}`, payload);
  },
  duplicateTemplate(id, payload) {
    return apiClient.post(`/certificate-templates/${id}/duplicate`, payload);
  },
  toggleTemplateStatus(id, active) {
    return apiClient.patch(`/certificate-templates/${id}/status`, null, buildQuery({ active }));
  },
  deleteTemplate(id) {
    return apiClient.delete(`/certificate-templates/${id}`);
  },
  getSettings(params) {
    return apiClient.get('/certificates/settings', buildQuery(params));
  },
  verifyCertificate(token) {
    return apiClient.get(`/certificates/verify/${encodeURIComponent(token)}`);
  }
};
