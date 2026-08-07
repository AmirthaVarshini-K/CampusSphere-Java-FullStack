import axios from 'axios';
import { clearSession, readSession, setSessionNotice } from './session';

const apiClient = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL ?? 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use(config => {
  const session = readSession();
  if (session?.accessToken) {
    config.headers.Authorization = `Bearer ${session.accessToken}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error?.response?.status === 401) {
      setSessionNotice('Your session has expired. Please sign in again.');
      clearSession();
    }
    return Promise.reject(error);
  }
);

export default apiClient;
