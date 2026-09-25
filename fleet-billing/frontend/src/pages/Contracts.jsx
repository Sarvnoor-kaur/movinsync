import React, { useState, useEffect } from 'react';
import { contractApi } from '../api/contractApi';
import { vendorApi } from '../api/vendorApi';
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
import { FileText, Plus, Eye, Layers } from 'lucide-react';

export const Contracts = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [contracts, setContracts] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Selected Contract Drawer & Versions
  const [selectedContract, setSelectedContract] = useState(null);
  const [versions, setVersions] = useState([]);
  const [slabs, setSlabs] = useState([]);

  // Modal State: Create Contract
  const [isContractModalOpen, setIsContractModalOpen] = useState(false);
  const [contractForm, setContractForm] = useState({
    contractNumber: '',
    name: '',
    vendorId: '',
    vehicleId: '',
    billingType: 'FIXED_MONTHLY',
    startDate: '2026-09-01',
    endDate: '',
  });

  // Modal State: Create Version
  const [isVersionModalOpen, setIsVersionModalOpen] = useState(false);
  const [versionForm, setVersionForm] = useState({
    versionNumber: 1,
    effectiveFrom: '2026-09-01',
    billingType: 'FIXED_MONTHLY',
    monthlyFixedFeePaisa: 0,
    overagePerKmPaisa: 0,
    overagePerHourPaisa: 0,
    nightChargePaisa: 0,
    waitingChargePerHourPaisa: 0,
  });

  // Modal State: Create Pricing Slab
  const [isSlabModalOpen, setIsSlabModalOpen] = useState(false);
  const [targetVersionId, setTargetVersionId] = useState(null);
  const [slabForm, setSlabForm] = useState({
    unitType: 'KM',
    slabOrder: 1,
    fromValue: 0,
    toValue: 100,
    ratePaisa: 0,
  });

  const [submitting, setSubmitting] = useState(false);

  const loadData = async () => {
    try {
      const [vRes, vehRes] = await Promise.all([
        vendorApi.getAll({ size: 100 }),
        vehicleApi.getAll({ size: 100 }),
      ]);
      setVendors(vRes.data.content || []);
      setVehicles(vehRes.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const loadContracts = async (p = 0) => {
    setLoading(true);
    setError('');
    try {
      const res = await contractApi.getContracts({ page: p, size: 10 });
      setContracts(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    loadContracts(0);
  }, []);

  const handleInspectContract = async (contract) => {
    setSelectedContract(contract);
    setError('');
    try {
      const vRes = await contractApi.getVersions(contract.id);
      setVersions(vRes.data || []);
      if (vRes.data && vRes.data.length > 0) {
        const sRes = await contractApi.getPricingSlabs(contract.id, vRes.data[0].id);
        setSlabs(sRes.data || []);
      } else {
        setSlabs([]);
      }
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleCreateContract = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        ...contractForm,
        vendorId: Number(contractForm.vendorId),
        vehicleId: contractForm.vehicleId ? Number(contractForm.vehicleId) : null,
      };
      await contractApi.createContract(payload);
      setSuccess('Contract created successfully.');
      setIsContractModalOpen(false);
      loadContracts(page);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreateVersion = async (e) => {
    e.preventDefault();
    if (!selectedContract) return;
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await contractApi.createVersion(selectedContract.id, versionForm);
      setSuccess('Contract Version added successfully.');
      setIsVersionModalOpen(false);
      handleInspectContract(selectedContract);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleCreateSlab = async (e) => {
    e.preventDefault();
    if (!targetVersionId) return;
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await contractApi.createPricingSlab(selectedContract.id, targetVersionId, slabForm);
      setSuccess('Pricing Slab created successfully.');
      setIsSlabModalOpen(false);
      handleInspectContract(selectedContract);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Rate Contracts & Slabs</h1>
          <p className="page-subtitle">Configure vendor contracts, version effective dates, and tiered pricing slabs.</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => setIsContractModalOpen(true)}>
            <Plus size={18} /> Create Contract
          </button>
        )}
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div style={{ display: 'grid', gridTemplateColumns: selectedContract ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Contracts Table */}
        <div className="table-card">
          <div className="table-header-bar">
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Active Rate Contracts</h3>
          </div>
          {loading ? (
            <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
          ) : contracts.length === 0 ? (
            <EmptyState
              icon={FileText}
              title="No contracts configured"
              description="Create a rate contract to define pricing slabs and billing rules."
              actionLabel={isAdmin ? 'Create Contract' : null}
              onAction={() => setIsContractModalOpen(true)}
            />
          ) : (
            <div className="table-container">
              <table className="saas-table">
                <thead>
                  <tr>
                    <th>Contract #</th>
                    <th>Name</th>
                    <th>Billing Type</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {contracts.map((c) => {
                    const isSelected = selectedContract?.id === c.id;
                    return (
                      <tr key={c.id} style={{ backgroundColor: isSelected ? '#eff6ff' : 'transparent' }}>
                        <td><strong>{c.contractNumber}</strong></td>
                        <td>{c.name}</td>
                        <td><StatusBadge status={c.billingType} /></td>
                        <td><StatusBadge status={c.status} /></td>
                        <td>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleInspectContract(c)}>
                            <Eye size={14} /> Inspect
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={loadContracts} />
        </div>

        {/* Selected Contract Inspection Drawer */}
        {selectedContract && (
          <div className="saas-card">
            <div className="flex-between" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Contract #{selectedContract.contractNumber} Details</h3>
              <button
                style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
                onClick={() => setSelectedContract(null)}
              >
                ✕
              </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1.25rem', background: '#f8fafc', padding: '0.85rem', borderRadius: 'var(--radius-sm)' }}>
              <div>Contract Name: <strong>{selectedContract.name}</strong></div>
              <div>Billing Type: <StatusBadge status={selectedContract.billingType} /></div>
              <div>Start Date: <strong>{selectedContract.startDate}</strong></div>
              <div>Status: <StatusBadge status={selectedContract.status} /></div>
            </div>

            <div className="flex-between" style={{ marginBottom: '0.75rem' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 600 }}>Contract Versions</h4>
              {isAdmin && (
                <button
                  className="btn btn-secondary btn-sm"
                  onClick={() => {
                    setVersionForm({ ...versionForm, billingType: selectedContract.billingType });
                    setIsVersionModalOpen(true);
                  }}
                >
                  <Plus size={14} /> Add Version
                </button>
              )}
            </div>

            {versions.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', marginBottom: '1.25rem' }}>No versions defined for this contract.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.25rem' }}>
                {versions.map((v) => (
                  <div key={v.id} style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '0.85rem' }}>
                    <div className="flex-between" style={{ marginBottom: '0.35rem' }}>
                      <strong>Version v{v.versionNumber} ({v.billingType})</strong>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Effective: {v.effectiveFrom}</span>
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.35rem', marginBottom: '0.5rem' }}>
                      <div>Fixed Fee: <strong>{formatCurrency(v.monthlyFixedFeePaisa || 0)}</strong></div>
                      <div>Overage/Km: <strong>{formatCurrency(v.overagePerKmPaisa || 0)}</strong></div>
                      <div>Night Charge: <strong>{formatCurrency(v.nightChargePaisa || 0)}</strong></div>
                      <div>Waiting/Hr: <strong>{formatCurrency(v.waitingChargePerHourPaisa || 0)}</strong></div>
                    </div>
                    {isAdmin && (
                      <button
                        className="btn btn-secondary btn-sm"
                        style={{ width: '100%' }}
                        onClick={() => {
                          setTargetVersionId(v.id);
                          setIsSlabModalOpen(true);
                        }}
                      >
                        + Add Pricing Slab
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}

            <h4 style={{ fontSize: '0.95rem', fontWeight: 600, marginBottom: '0.5rem' }}>Active Pricing Slabs</h4>
            {slabs.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No tier slabs added yet.</p>
            ) : (
              <div className="table-container">
                <table className="saas-table">
                  <thead>
                    <tr>
                      <th>Order</th>
                      <th>Unit</th>
                      <th>Range</th>
                      <th>Rate</th>
                    </tr>
                  </thead>
                  <tbody>
                    {slabs.map((s) => (
                      <tr key={s.id}>
                        <td>#{s.slabOrder}</td>
                        <td>{s.unitType}</td>
                        <td>{s.fromValue} – {s.toValue}</td>
                        <td><strong>{formatCurrency(s.ratePaisa)}</strong></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Modal: Create Contract */}
      <Modal isOpen={isContractModalOpen} onClose={() => setIsContractModalOpen(false)} title="Create Rate Contract">
        <form onSubmit={handleCreateContract}>
          <div className="form-group">
            <label className="form-label">Contract Number *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. CNT-2026-001"
              value={contractForm.contractNumber}
              onChange={(e) => setContractForm({ ...contractForm, contractNumber: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contract Name *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Metro Dedicated Fleet Contract 2026"
              value={contractForm.name}
              onChange={(e) => setContractForm({ ...contractForm, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Vendor *</label>
            <select
              className="form-control"
              value={contractForm.vendorId}
              onChange={(e) => setContractForm({ ...contractForm, vendorId: e.target.value })}
              required
            >
              <option value="">Select Vendor...</option>
              {vendors.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.name} ({v.code})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Billing Type *</label>
            <select
              className="form-control"
              value={contractForm.billingType}
              onChange={(e) => setContractForm({ ...contractForm, billingType: e.target.value })}
            >
              <option value="FIXED_MONTHLY">FIXED_MONTHLY</option>
              <option value="PER_KM">PER_KM</option>
              <option value="PER_TRIP">PER_TRIP</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Start Date *</label>
            <input
              type="date"
              className="form-control"
              value={contractForm.startDate}
              onChange={(e) => setContractForm({ ...contractForm, startDate: e.target.value })}
              required
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsContractModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Creating Contract...' : 'Create Contract'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal: Create Version */}
      <Modal isOpen={isVersionModalOpen} onClose={() => setIsVersionModalOpen(false)} title="Add Contract Version">
        <form onSubmit={handleCreateVersion}>
          <div className="form-group">
            <label className="form-label">Version Number *</label>
            <input
              type="number"
              className="form-control"
              value={versionForm.versionNumber}
              onChange={(e) => setVersionForm({ ...versionForm, versionNumber: Number(e.target.value) })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Effective From Date *</label>
            <input
              type="date"
              className="form-control"
              value={versionForm.effectiveFrom}
              onChange={(e) => setVersionForm({ ...versionForm, effectiveFrom: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Monthly Fixed Fee (Paisa)</label>
            <input
              type="number"
              className="form-control"
              placeholder="e.g. 4500000 (₹45,000)"
              value={versionForm.monthlyFixedFeePaisa}
              onChange={(e) => setVersionForm({ ...versionForm, monthlyFixedFeePaisa: Number(e.target.value) })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Overage Per KM (Paisa)</label>
            <input
              type="number"
              className="form-control"
              placeholder="e.g. 1500 (₹15/km)"
              value={versionForm.overagePerKmPaisa}
              onChange={(e) => setVersionForm({ ...versionForm, overagePerKmPaisa: Number(e.target.value) })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsVersionModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving Version...' : 'Save Version'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Modal: Create Pricing Slab */}
      <Modal isOpen={isSlabModalOpen} onClose={() => setIsSlabModalOpen(false)} title="Add Pricing Slab Tier">
        <form onSubmit={handleCreateSlab}>
          <div className="form-group">
            <label className="form-label">Unit Type *</label>
            <select
              className="form-control"
              value={slabForm.unitType}
              onChange={(e) => setSlabForm({ ...slabForm, unitType: e.target.value })}
            >
              <option value="KM">KM</option>
              <option value="HOUR">HOUR</option>
              <option value="TRIP">TRIP</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Slab Order *</label>
            <input
              type="number"
              className="form-control"
              value={slabForm.slabOrder}
              onChange={(e) => setSlabForm({ ...slabForm, slabOrder: Number(e.target.value) })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">From Value *</label>
            <input
              type="number"
              className="form-control"
              value={slabForm.fromValue}
              onChange={(e) => setSlabForm({ ...slabForm, fromValue: Number(e.target.value) })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">To Value *</label>
            <input
              type="number"
              className="form-control"
              value={slabForm.toValue}
              onChange={(e) => setSlabForm({ ...slabForm, toValue: Number(e.target.value) })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Rate (Paisa) *</label>
            <input
              type="number"
              className="form-control"
              placeholder="e.g. 1800 (₹18)"
              value={slabForm.ratePaisa}
              onChange={(e) => setSlabForm({ ...slabForm, ratePaisa: Number(e.target.value) })}
              required
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsSlabModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving Slab...' : 'Save Slab'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
