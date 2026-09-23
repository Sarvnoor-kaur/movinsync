import React, { useState, useEffect } from 'react';
import { billingApi } from '../api/billingApi';
import { vehicleApi } from '../api/vehicleApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { SkeletonLoader } from '../components/SkeletonLoader';
import { EmptyState } from '../components/EmptyState';
import { formatCurrency } from '../utils/formatCurrency';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';
import { Link } from 'react-router-dom';
import { Receipt, Play, Eye, DollarSign } from 'lucide-react';

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
      const runId = res.data.billingRunId || res.data.id;
      setSuccess(`Billing Run #${runId} executed successfully! Amount: ${formatCurrency(res.data.totalPaisa)}`);
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
      setSuccess(`Fixed fee allocated across shift trips for Run #${runId}. Amount: ${formatCurrency(res.data.totalAllocatedPaisa)}`);
      loadRuns(page, selectedVehicleId);
      if ((selectedRun?.billingRunId || selectedRun?.id) === runId) {
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
          <p className="page-subtitle">Execute monthly contract billing runs, preview calculations, and allocate fixed monthly fees.</p>
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
              <Play size={18} /> Run Billing Engine
            </button>
          )}
        </div>
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div style={{ display: 'grid', gridTemplateColumns: selectedRun ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Billing Runs Table */}
        <div className="table-card">
          <div className="table-header-bar">
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Billing Run Records</h3>
          </div>
          {loading ? (
            <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
          ) : runs.length === 0 ? (
            <EmptyState
              icon={Receipt}
              title="No billing runs executed"
              description="Select a vehicle and click 'Run Billing Engine' to calculate contract billing."
              actionLabel={isManager ? 'Run Billing Engine' : null}
              onAction={() => setIsModalOpen(true)}
            />
          ) : (
            <div className="table-container">
              <table className="saas-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Vehicle</th>
                    <th>Month</th>
                    <th>Trips</th>
                    <th>Total Charge</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {runs.map((r) => {
                    const runId = r.billingRunId || r.id;
                    const isSelected = (selectedRun?.billingRunId || selectedRun?.id) === runId;
                    return (
                      <tr key={runId} style={{ backgroundColor: isSelected ? '#eff6ff' : 'transparent' }}>
                        <td>#{runId}</td>
                        <td>{r.vehicleRegistrationNumber || `Vehicle #${r.vehicleId}`}</td>
                        <td>{r.billingMonth}</td>
                        <td>{r.tripCount || 0}</td>
                        <td><strong>{formatCurrency(r.totalPaisa)}</strong></td>
                        <td><StatusBadge status={r.status} /></td>
                        <td>
                          <div style={{ display: 'inline-flex', gap: '0.35rem' }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => handleInspectRun(runId)}>
                              <Eye size={14} /> Inspect
                            </button>
                            {isManager && (
                              <button
                                className="btn btn-primary btn-sm"
                                title="Allocate Fixed Monthly Fee"
                                onClick={() => handleAllocateFixedFee(runId)}
                              >
                                <DollarSign size={14} /> Allocate
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadRuns(p, selectedVehicleId)} />
        </div>

        {/* Selected Run Inspection Drawer */}
        {selectedRun && (
          <div className="saas-card">
            <div className="flex-between" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Billing Run #{selectedRun.billingRunId || selectedRun.id} Inspection</h3>
              <button
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
                onClick={() => setSelectedRun(null)}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1.25rem', background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
              <div>Vehicle: <strong>{selectedRun.vehicleRegistrationNumber || `#${selectedRun.vehicleId}`}</strong></div>
              <div>Billing Month: <strong>{selectedRun.billingMonth}</strong></div>
              <div>Status: <StatusBadge status={selectedRun.status} /></div>
              <div>Total Charge: <strong style={{ color: 'var(--success)', fontSize: '1.1rem' }}>{formatCurrency(selectedRun.totalPaisa)}</strong></div>
            </div>

            {isManager && (
              <div style={{ marginBottom: '1.25rem' }}>
                <button className="btn btn-primary btn-sm" style={{ width: '100%' }} onClick={() => handleAllocateFixedFee(selectedRun.billingRunId || selectedRun.id)}>
                  <DollarSign size={16} /> Allocate Fixed Monthly Fee Across Shift Trips
                </button>
              </div>
            )}

            <h4 style={{ fontSize: '0.95rem', fontWeight: 600, marginBottom: '0.5rem' }}>Generated Tax Invoice</h4>
            {selectedRun.invoiceId ? (
              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                  <span>Invoice Reference:</span>
                  <strong>Invoice #{selectedRun.invoiceId}</strong>
                </div>
                <Link to="/invoices" className="btn btn-secondary btn-sm" style={{ width: '100%', marginTop: '0.5rem' }}>
                  View Complete Tax Invoice Breakdown →
                </Link>
              </div>
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Tax invoice generated automatically upon completion.</p>
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
            <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: 'var(--radius-sm)', padding: '1rem', marginBottom: '1.25rem' }}>
              <h4 style={{ color: '#2563eb', fontSize: '0.95rem', marginBottom: '0.5rem' }}>Billing Calculation Preview</h4>
              <div style={{ fontSize: '0.85rem', display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                <div>Vehicle: <strong>{previewData.vehicleRegistrationNumber || `#${previewData.vehicleId}`}</strong></div>
                <div>Billing Month: <strong>{previewData.billingMonth}</strong></div>
                <div>Total Trips: <strong>{previewData.tripCount || 0}</strong></div>
                <div style={{ borderTop: '1px solid #cbd5e1', paddingTop: '0.5rem', marginTop: '0.25rem', fontSize: '1rem' }}>
                  Estimated Charge: <strong style={{ color: '#16a34a' }}>{formatCurrency(previewData.estimatedTotalPaisa)}</strong>
                </div>
              </div>
            </div>
          )}

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="button" className="btn btn-primary" onClick={handleExecuteRun} disabled={submitting}>
              {submitting ? 'Executing Billing Run...' : 'Confirm & Execute Run'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
