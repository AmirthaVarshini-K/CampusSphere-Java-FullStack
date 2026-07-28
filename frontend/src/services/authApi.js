import apiClient from './apiClient';

export const authApi = {
  login(payload) {
    return apiClient.post('/auth/login', payload);
  },
  logout(payload) {
    return apiClient.post('/auth/logout', payload);
  },
  registerStudent(payload) {
    return apiClient.post('/auth/register/student', payload);
  },
  forgotPassword(payload) {
    return apiClient.post('/auth/forgot-password', payload);
  },
  resetPassword(payload) {
    return apiClient.post('/auth/reset-password', payload);
  },
  changePassword(payload) {
    return apiClient.post('/auth/change-password', payload);
  },
  refreshToken(payload) {
    return apiClient.post('/auth/refresh-token', payload);
  }
};
