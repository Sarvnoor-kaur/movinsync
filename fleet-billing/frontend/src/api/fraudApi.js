import api from './axios';

export const fraudApi = {
  getAlerts: (params) => api.get('/fraud-alerts', { params }),
  getAlertById: (id) => api.get(`/fraud-alerts/${id}`),
  resolveAlert: (id) => api.patch(`/fraud-alerts/${id}/resolve`),
  dismissAlert: (id) => api.patch(`/fraud-alerts/${id}/dismiss`),
};
