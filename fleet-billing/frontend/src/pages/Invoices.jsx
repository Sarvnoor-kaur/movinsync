import React, { useState, useEffect } from 'react';
import { billingApi } from '../api/billingApi';
import { vehicleApi } from '../api/vehicleApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { formatCurrency } from '../utils/formatCurrency';
import { formatDateTime } from '../utils/formatDate';
import { getErrorMessage } from '../utils/errorHandler';

export const Invoices = () => {
  const [invoices, setInvoices] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Selected Invoice Detail Drawer
  const [selectedInvoice, setSelectedInvoice] = useState(null);

  const loadInvoices = async (p = 0, vId = selectedVehicleId) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (vId) params.vehicleId = vId;
      const res = await billingApi.getInvoices(params);
      setInvoices(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const loadVehicles = async () => {
    try {
      const res = await vehicleApi.getAll({ size: 100 });
      setVehicles(res.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadVehicles();
    loadInvoices(0, '');
  }, []);

  const handleFilterChange = (e) => {
    const vId = e.target.value;
    setSelectedVehicleId(vId);
    loadInvoices(0, vId);
  };

  const handleInspectInvoice = async (id) => {
    try {
      const res = await billingApi.getInvoiceById(id);
      setSelectedInvoice(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Tax Invoices</h1>
          <p className="page-subtitle">Review generated itemized fleet invoices, taxes, cost allocations, and line items.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select
            className="form-control"
            style={{ width: '220px' }}
            value={selectedVehicleId}
            onChange={handleFilterChange}
          >
            <option value="">All Vehicles</option>
            {vehicles.map((v) => (
              <option key={v.id} value={v.id}>
                {v.registrationNumber} (#{v.id})
              </option>
            ))}
          </select>
        </div>
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />

      <div style={{ display: 'grid', gridTemplateColumns: selectedInvoice ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Table of Invoices */}
        <div className="glass-card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>Generated Invoices</h3>
          {loading ? (
            <div className="flex-center" style={{ padding: '3rem' }}><div className="spinner"></div></div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Invoice No.</th>
                    <th>Vehicle</th>
                    <th>Billing Month</th>
                    <th>Subtotal</th>
                    <th>Tax</th>
                    <th>Total</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>
                        No invoices found. Run the billing engine to generate invoices.
                      </td>
                    </tr>
                  ) : (
                    invoices.map((inv) => (
                      <tr key={inv.id} style={{ background: selectedInvoice?.id === inv.id ? 'rgba(59, 130, 246, 0.1)' : 'transparent' }}>
                        <td><strong>{inv.invoiceNumber}</strong></td>
                        <td>{inv.vehicleRegistrationNumber || `Vehicle #${inv.vehicleId}`}</td>
                        <td>{inv.billingMonth}</td>
                        <td>{formatCurrency(inv.subtotalPaisa)}</td>
                        <td>{formatCurrency(inv.taxPaisa)}</td>
                        <td><strong style={{ color: 'var(--accent-emerald)' }}>{formatCurrency(inv.totalPaisa)}</strong></td>
                        <td>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleInspectInvoice(inv.id)}>
                            Inspect
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadInvoices(p, selectedVehicleId)} />
        </div>

        {/* Selected Invoice View */}
        {selectedInvoice && (
          <div className="glass-card" style={{ background: '#ffffff', color: '#0f172a', padding: '2rem' }}>
            <div className="flex-between" style={{ borderBottom: '2px solid #e2e8f0', paddingBottom: '1rem', marginBottom: '1.5rem' }}>
              <div>
                <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#1e293b' }}>TAX INVOICE</h2>
                <div style={{ fontSize: '0.85rem', color: '#64748b' }}>Reference: #{selectedInvoice.invoiceNumber}</div>
              </div>
              <button
                style={{ background: '#f1f5f9', border: 'none', padding: '0.4rem 0.8rem', borderRadius: '6px', cursor: 'pointer', fontWeight: 600 }}
                onClick={() => setSelectedInvoice(null)}
              >
                ✕ Close
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
              <div>
                <span style={{ color: '#64748b' }}>Vehicle Reg:</span> <strong>{selectedInvoice.vehicleRegistrationNumber || selectedInvoice.vehicleId}</strong>
              </div>
              <div>
                <span style={{ color: '#64748b' }}>Billing Month:</span> <strong>{selectedInvoice.billingMonth}</strong>
              </div>
              <div>
                <span style={{ color: '#64748b' }}>Invoice Date:</span> <strong>{selectedInvoice.invoiceDate || '—'}</strong>
              </div>
              <div>
                <span style={{ color: '#64748b' }}>Billing Run ID:</span> <strong>#{selectedInvoice.billingRunId}</strong>
              </div>
            </div>

            <h4 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: '0.75rem', color: '#1e293b' }}>Itemized Breakdown</h4>
            <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', marginBottom: '1.5rem' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
                <thead style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#475569' }}>
                  <tr>
                    <th style={{ padding: '0.75rem 1rem' }}>Description</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Type</th>
                    <th style={{ padding: '0.75rem 1rem' }}>Rate</th>
                    <th style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedInvoice.items && selectedInvoice.items.map((item) => (
                    <tr key={item.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                      <td style={{ padding: '0.75rem 1rem' }}>
                        <div><strong>{item.description}</strong></div>
                        {item.explanation && <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{item.explanation}</div>}
                      </td>
                      <td style={{ padding: '0.75rem 1rem' }}>{item.pricingType || 'TRIP'}</td>
                      <td style={{ padding: '0.75rem 1rem' }}>{formatCurrency(item.ratePaisa)}</td>
                      <td style={{ padding: '0.75rem 1rem', textAlign: 'right', fontWeight: 600 }}>{formatCurrency(item.amountPaisa)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '0.35rem', fontSize: '0.95rem' }}>
              <div>Subtotal: <strong>{formatCurrency(selectedInvoice.subtotalPaisa)}</strong></div>
              <div>Tax (GST 18%): <strong>{formatCurrency(selectedInvoice.taxPaisa)}</strong></div>
              <div style={{ fontSize: '1.2rem', fontWeight: 800, color: '#059669', borderTop: '2px solid #e2e8f0', paddingTop: '0.5rem', marginTop: '0.25rem' }}>
                Total Payable: {formatCurrency(selectedInvoice.totalPaisa)}
              </div>
            </div>

            <div style={{ marginTop: '2rem', textAlign: 'right' }}>
              <button
                className="btn btn-primary"
                onClick={() => window.print()}
              >
                🖨️ Print / Export Invoice
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
