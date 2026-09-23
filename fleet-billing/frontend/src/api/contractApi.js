import api from './axios';

export const contractApi = {
  // Contracts
  getAll: (params) => api.get('/contracts', { params }),
  getById: (id) => api.get(`/contracts/${id}`),
  create: (data) => api.post('/contracts', data),
  update: (id, data) => api.put(`/contracts/${id}`, data),
  delete: (id) => api.delete(`/contracts/${id}`),

  // Contract Versions
  getVersions: (contractId) => api.get(`/contracts/${contractId}/versions`),
  getVersionById: (contractId, versionId) => api.get(`/contracts/${contractId}/versions/${versionId}`),
  createVersion: (contractId, data) => api.post(`/contracts/${contractId}/versions`, data),
  updateVersion: (contractId, versionId, data) => api.put(`/contracts/${contractId}/versions/${versionId}`, data),
  deleteVersion: (contractId, versionId) => api.delete(`/contracts/${contractId}/versions/${versionId}`),

  // Pricing Slabs
  getSlabs: (contractId, versionId) => api.get(`/contracts/${contractId}/versions/${versionId}/slabs`),
  createSlab: (contractId, versionId, data) => api.post(`/contracts/${contractId}/versions/${versionId}/slabs`, data),
  updateSlab: (contractId, versionId, slabId, data) => api.put(`/contracts/${contractId}/versions/${versionId}/slabs/${slabId}`, data),
  deleteSlab: (contractId, versionId, slabId) => api.delete(`/contracts/${contractId}/versions/${versionId}/slabs/${slabId}`),
};
