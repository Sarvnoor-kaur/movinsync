import React, { useState, useEffect } from 'react';
import { billingApi } from '../api/billingApi';
import { vehicleApi } from '../api/vehicleApi';
import { StatusBadge } from '../components/StatusBadge';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { SkeletonLoader } from '../components/SkeletonLoader';
import { EmptyState } from '../components/EmptyState';
import { formatCurrency } from '../utils/formatCurrency';
import { getErrorMessage } from '../utils/errorHandler';
import { FileCheck2, Eye, Printer } from 'lucide-react';

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

  const loadVehicles = async () => {
    try {
      const res = await vehicleApi.getAll({ size: 100 });
      setVehicles(res.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

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

  useEffect(() => {
    loadVehicles();
    loadInvoices(0, '');
  }, []);

  const handleFilterChange = (e) => {
    const vId = e.target.value;
    setSelectedVehicleId(vId);
    loadInvoices(0, vId);
  };

  const handleInspectInvoice = async (invoiceId) => {
    try {
      const res = await billingApi.getInvoiceById(invoiceId);
      setSelectedInvoice(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handlePrintInvoice = () => {
    window.print();
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Tax Invoices</h1>
          <p className="page-subtitle">Review generated tax invoices, line items, and itemized trip cost calculations.</p>
        </div>
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

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />

      <div style={{ display: 'grid', gridTemplateColumns: selectedInvoice ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Invoices Table */}
        <div className="table-card">
          <div className="table-header-bar">
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Invoice Register</h3>
          </div>
          {loading ? (
            <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
          ) : invoices.length === 0 ? (
            <EmptyState
              icon={FileCheck2}
              title="No tax invoices found"
              description="Tax invoices are automatically created when a billing run is executed."
            />
          ) : (
            <div className="table-container">
              <table className="saas-table">
                <thead>
                  <tr>
                    <th>Invoice #</th>
                    <th>Vehicle</th>
                    <th>Date</th>
                    <th>Billing Month</th>
                    <th>Total Charge</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.map((inv) => {
                    const isSelected = selectedInvoice?.id === inv.id;
                    return (
                      <tr key={inv.id} style={{ backgroundColor: isSelected ? '#eff6ff' : 'transparent' }}>
                        <td><strong>{inv.invoiceNumber}</strong></td>
                        <td>{inv.vehicleRegistrationNumber || `Vehicle #${inv.vehicleId}`}</td>
                        <td>{inv.invoiceDate}</td>
                        <td>{inv.billingMonth}</td>
                        <td><strong style={{ color: 'var(--success)' }}>{formatCurrency(inv.totalPaisa)}</strong></td>
                        <td>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleInspectInvoice(inv.id)}>
                            <Eye size={14} /> View Items
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadInvoices(p, selectedVehicleId)} />
        </div>

        {/* Selected Invoice Item Breakdown Drawer */}
        {selectedInvoice && (
          <div className="saas-card" id="invoice-print-area">
            <div className="flex-between" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', fontWeight: 700 }}>Tax Invoice {selectedInvoice.invoiceNumber}</h3>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Date: {selectedInvoice.invoiceDate}</span>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button className="btn btn-secondary btn-sm" onClick={handlePrintInvoice} title="Print Invoice">
                  <Printer size={14} /> Print
                </button>
                <button
                  style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
                  onClick={() => setSelectedInvoice(null)}
                >
                  ✕
                </button>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1.25rem', background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
              <div>Vehicle: <strong>{selectedInvoice.vehicleRegistrationNumber || `#${selectedInvoice.vehicleId}`}</strong></div>
              <div>Billing Period: <strong>{selectedInvoice.billingMonth}</strong></div>
              <div>Subtotal: <strong>{formatCurrency(selectedInvoice.subtotalPaisa)}</strong></div>
              <div>Invoice Total: <strong style={{ color: 'var(--success)', fontSize: '1.1rem' }}>{formatCurrency(selectedInvoice.totalPaisa)}</strong></div>
            </div>

            <h4 style={{ fontSize: '0.95rem', fontWeight: 600, marginBottom: '0.5rem' }}>Itemized Line Items</h4>
            {(!selectedInvoice.items || selectedInvoice.items.length === 0) ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No line items attached.</p>
            ) : (
              <div className="table-container">
                <table className="saas-table">
                  <thead>
                    <tr>
                      <th>Trip ID</th>
                      <th>Description</th>
                      <th>Type</th>
                      <th>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedInvoice.items.map((item) => (
                      <tr key={item.id}>
                        <td>{item.externalTripId ? `#${item.externalTripId}` : '—'}</td>
                        <td>
                          <div>{item.description}</div>
                          {item.explanation && (
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{item.explanation}</div>
                          )}
                        </td>
                        <td><StatusBadge status={item.pricingType} /></td>
                        <td><strong>{formatCurrency(item.amountPaisa)}</strong></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
