import api from './axios';

export const contractApi = {
  // Contracts
  getAll: (params) => api.get('/contracts', { params }),
  getContracts: (params) => api.get('/contracts', { params }),
  getById: (id) => api.get(`/contracts/${id}`),
  create: (data) => api.post('/contracts', data),
  createContract: (data) => api.post('/contracts', data),
  update: (id, data) => api.put(`/contracts/${id}`, data),
  delete: (id) => api.delete(`/contracts/${id}`),

  // Contract Versions
  getVersions: (contractId) => api.get(`/contracts/${contractId}/versions`),
  getVersionById: (contractId, versionId) => api.get(`/contracts/${contractId}/versions/${versionId}`),
  createVersion: (contractId, data) => api.post(`/contracts/${contractId}/versions`, data),
  updateVersion: (contractId, versionId, data) => api.put(`/contracts/${contractId}/versions/${versionId}`, data),
  deleteVersion: (contractId, versionId) => api.delete(`/contracts/${contractId}/versions/${versionId}`),

  // Pricing Slabs
  getSlabs: (arg1, arg2) => {
    const versionId = arg2 !== undefined ? arg2 : arg1;
    const contractId = arg2 !== undefined ? arg1 : 1;
    return api.get(`/contracts/${contractId}/versions/${versionId}/slabs`);
  },
  getPricingSlabs: (arg1, arg2) => {
    const versionId = arg2 !== undefined ? arg2 : arg1;
    const contractId = arg2 !== undefined ? arg1 : 1;
    return api.get(`/contracts/${contractId}/versions/${versionId}/slabs`);
  },
  createSlab: (arg1, arg2, arg3) => {
    const contractId = arg3 !== undefined ? arg1 : 1;
    const versionId = arg3 !== undefined ? arg2 : arg1;
    const payload = arg3 !== undefined ? arg3 : arg2;
    return api.post(`/contracts/${contractId}/versions/${versionId}/slabs`, payload);
  },
  createPricingSlab: (arg1, arg2, arg3) => {
    const contractId = arg3 !== undefined ? arg1 : 1;
    const versionId = arg3 !== undefined ? arg2 : arg1;
    const payload = arg3 !== undefined ? arg3 : arg2;
    return api.post(`/contracts/${contractId}/versions/${versionId}/slabs`, payload);
  },
  updateSlab: (contractId, versionId, slabId, data) => api.put(`/contracts/${contractId}/versions/${versionId}/slabs/${slabId}`, data),
  deleteSlab: (contractId, versionId, slabId) => api.delete(`/contracts/${contractId}/versions/${versionId}/slabs/${slabId}`),
};
