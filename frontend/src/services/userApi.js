import apiClient from './apiClient';

export const userApi = {
  me() {
    return apiClient.get('/users/me');
  },
  updateProfile(payload) {
    return apiClient.put('/users/profile', payload);
  }
};
