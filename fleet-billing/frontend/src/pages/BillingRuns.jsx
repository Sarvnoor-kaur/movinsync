import React, { useState, useEffect } from 'react';
import { billingApi } from '../api/billingApi';
import { vehicleApi } from '../api/vehicleApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { formatCurrency } from '../utils/formatCurrency';
import { formatDateTime } from '../utils/formatDate';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';
import { Link } from 'react-router-dom';

export const BillingRuns = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [runs, setRuns] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Selected Run Detail
  const [selectedRun, setSelectedRun] = useState(null);

  // Modal State for New Billing Run
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [billingMonth, setBillingMonth] = useState('2026-09');
  const [targetVehicleId, setTargetVehicleId] = useState('');
  const [previewData, setPreviewData] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const loadRuns = async (p = 0, vId = selectedVehicleId) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (vId) params.vehicleId = vId;
      const res = await billingApi.getRuns(params);
      setRuns(res.data.content || []);
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
      const vList = res.data.content || [];
      setVehicles(vList);
      if (vList.length > 0) setTargetVehicleId(vList[0].id);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadVehicles();
    loadRuns(0, '');
  }, []);

  const handleFilterChange = (e) => {
    const vId = e.target.value;
    setSelectedVehicleId(vId);
    loadRuns(0, vId);
  };

  const handlePreview = async () => {
    if (!targetVehicleId || !billingMonth) return;
    setPreviewLoading(true);
    setError('');
    setPreviewData(null);
    try {
      const res = await billingApi.previewRun({
        vehicleId: Number(targetVehicleId),
        billingMonth,
      });
      setPreviewData(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleExecuteRun = async () => {
    if (!targetVehicleId || !billingMonth) return;
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      const idempotencyKey = `KEY-RUN-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`;
      const res = await billingApi.createRun(
        { vehicleId: Number(targetVehicleId), billingMonth },
        idempotencyKey
      );
      setSuccess(`Billing Run #${res.data.id} created successfully! Total amount: ${formatCurrency(res.data.totalAmountPaisa)}`);
      setIsModalOpen(false);
      setPreviewData(null);
      loadRuns(page, selectedVehicleId);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleAllocateFixedFee = async (runId) => {
    setError('');
    setSuccess('');
    try {
      const idempotencyKey = `KEY-ALLOC-${Date.now()}-${Math.random().toString(36).substr(2, 6)}`;
      const res = await billingApi.allocateFixedFee(runId, idempotencyKey);
      setSuccess(`Fixed Fee Allocated successfully for Run #${runId}! Allocated Amount: ${formatCurrency(res.data.allocatedAmountPaisa)}`);
      loadRuns(page, selectedVehicleId);
      if (selectedRun?.id === runId) {
        const detailRes = await billingApi.getRunById(runId);
        setSelectedRun(detailRes.data);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleInspectRun = async (runId) => {
    try {
      const res = await billingApi.getRunById(runId);
      setSelectedRun(res.data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Billing Engine & Cost Splits</h1>
          <p className="page-subtitle">Execute monthly billing runs, preview calculations, allocate fixed fees, and review invoice summaries.</p>
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
          {isManager && (
            <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
              ⚡ Run Billing Engine
            </button>
          )}
        </div>
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div style={{ display: 'grid', gridTemplateColumns: selectedRun ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Table of Runs */}
        <div className="glass-card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>Billing Runs</h3>
          {loading ? (
            <div className="flex-center" style={{ padding: '3rem' }}><div className="spinner"></div></div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Vehicle</th>
                    <th>Period</th>
                    <th>Trips</th>
                    <th>Total Charge</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {runs.length === 0 ? (
                    <tr>
                      <td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>
                        No billing runs executed yet.
                      </td>
                    </tr>
                  ) : (
                    runs.map((r) => (
                      <tr key={r.id} style={{ background: selectedRun?.id === r.id ? 'rgba(59, 130, 246, 0.1)' : 'transparent' }}>
                        <td>#{r.id}</td>
                        <td>Vehicle #{r.vehicleId}</td>
                        <td>{r.billingMonth}/{r.billingYear}</td>
                        <td>{r.totalTrips || 0}</td>
                        <td><strong>{formatCurrency(r.totalAmountPaisa)}</strong></td>
                        <td><StatusBadge status={r.status} /></td>
                        <td>
                          <div style={{ display: 'flex', gap: '0.35rem' }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => handleInspectRun(r.id)}>
                              Inspect
                            </button>
                            {isManager && (
                              <button
                                className="btn btn-primary btn-sm"
                                title="Allocate Fixed Monthly Fee"
                                onClick={() => handleAllocateFixedFee(r.id)}
                              >
                                Allocate Fee
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadRuns(p, selectedVehicleId)} />
        </div>

        {/* Selected Run Inspection Drawer */}
        {selectedRun && (
          <div className="glass-card">
            <div className="flex-between" style={{ marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 600 }}>Billing Run #{selectedRun.id} Details</h3>
              <button
                style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '1.2rem' }}
                onClick={() => setSelectedRun(null)}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1.5rem', background: 'rgba(15, 23, 42, 0.5)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
              <div>Vehicle ID: <strong>#{selectedRun.vehicleId}</strong></div>
              <div>Billing Month: <strong>{selectedRun.billingMonth}/{selectedRun.billingYear}</strong></div>
              <div>Status: <StatusBadge status={selectedRun.status} /></div>
              <div>Total Charge: <strong style={{ color: 'var(--accent-emerald)', fontSize: '1.1rem' }}>{formatCurrency(selectedRun.totalAmountPaisa)}</strong></div>
            </div>

            {isManager && (
              <div style={{ marginBottom: '1.5rem' }}>
                <button className="btn btn-primary btn-sm" style={{ width: '100%' }} onClick={() => handleAllocateFixedFee(selectedRun.id)}>
                  💰 Allocate Fixed Fee Across Shift Cost Centers
                </button>
              </div>
            )}

            <h4 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.75rem' }}>Generated Invoice Breakdown</h4>
            {selectedRun.invoiceId ? (
              <div style={{ background: 'rgba(255, 255, 255, 0.04)', padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
                <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                  <span>Invoice Reference:</span>
                  <strong>Invoice #{selectedRun.invoiceId}</strong>
                </div>
                <Link to="/invoices" className="btn btn-secondary btn-sm" style={{ width: '100%', marginTop: '0.5rem' }}>
                  View Complete Invoice →
                </Link>
              </div>
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Invoice detail attached to billing run.</p>
            )}
          </div>
        )}
      </div>

      {/* Modal: Execute Billing Run */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Execute Billing Run Engine">
        <div>
          <div className="form-group">
            <label className="form-label">Vehicle *</label>
            <select
              className="form-control"
              value={targetVehicleId}
              onChange={(e) => setTargetVehicleId(e.target.value)}
            >
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.registrationNumber} (#{v.id})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Billing Month (YYYY-MM) *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. 2026-09"
              value={billingMonth}
              onChange={(e) => setBillingMonth(e.target.value)}
            />
          </div>

          <button className="btn btn-secondary" style={{ width: '100%', marginBottom: '1rem' }} onClick={handlePreview} disabled={previewLoading}>
            {previewLoading ? 'Calculating Preview...' : '🔍 Preview Calculation'}
          </button>

          {previewData && (
            <div style={{ background: 'rgba(59, 130, 246, 0.1)', border: '1px solid var(--accent-blue)', borderRadius: 'var(--radius-md)', padding: '1rem', marginBottom: '1.25rem' }}>
              <h4 style={{ color: 'var(--accent-blue)', fontSize: '0.95rem', marginBottom: '0.5rem' }}>Billing Run Calculation Preview</h4>
              <div style={{ fontSize: '0.85rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                <div>Total Trips: <strong>{previewData.totalTrips || 0}</strong></div>
                <div>Total Distance: <strong>{previewData.totalDistanceKm || 0} km</strong></div>
                <div>Base Trip Charges: <strong>{formatCurrency(previewData.baseTripChargePaisa)}</strong></div>
                <div>Overage Km Charges: <strong>{formatCurrency(previewData.overageKmChargePaisa)}</strong></div>
                <div>Night Charges: <strong>{formatCurrency(previewData.nightChargePaisa)}</strong></div>
                <div>Toll Charges: <strong>{formatCurrency(previewData.tollChargePaisa)}</strong></div>
                <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.5rem', marginTop: '0.25rem', fontSize: '1rem' }}>
                  Estimated Total: <strong style={{ color: 'var(--accent-emerald)' }}>{formatCurrency(previewData.totalAmountPaisa)}</strong>
                </div>
              </div>
            </div>
          )}

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="button" className="btn btn-primary" onClick={handleExecuteRun} disabled={submitting}>
              {submitting ? 'Executing Billing Run...' : '⚡ Confirm & Execute Run'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
