import axios from 'axios';
import { clearSession, readSession, setSessionNotice } from './session';

const isLocalhost = typeof window !== 'undefined' && ['localhost', '127.0.0.1'].includes(window.location.hostname);
const defaultBaseURL = isLocalhost ? 'http://localhost:8080/api' : '/api';

const apiClient = axios.create({
  baseURL: import.meta.env?.VITE_API_BASE_URL ?? defaultBaseURL,
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
