import api from './axios';

export const billingApi = {
  createRun: (data, idempotencyKey) =>
    api.post('/billing/runs', data, {
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {},
    }),
  previewRun: (data) => api.post('/billing/preview', data),
  getRunById: (id) => api.get(`/billing/runs/${id}`),
  getRuns: (params) => api.get('/billing/runs', { params }),
  allocateFixedFee: (id, idempotencyKey) =>
    api.post(`/billing/runs/${id}/allocate-fixed-fee`, {}, {
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {},
    }),

  // Invoices
  getInvoiceById: (id) => api.get(`/invoices/${id}`),
  getInvoices: (params) => api.get('/invoices', { params }),
};
