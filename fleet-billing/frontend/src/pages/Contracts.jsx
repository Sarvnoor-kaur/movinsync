import React, { useState, useEffect } from 'react';
import { contractApi } from '../api/contractApi';
import { vendorApi } from '../api/vendorApi';
import { vehicleApi } from '../api/vehicleApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { formatCurrency } from '../utils/formatCurrency';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';

export const Contracts = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [contracts, setContracts] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Selected Contract Drawer / Versions view
  const [selectedContract, setSelectedContract] = useState(null);
  const [versions, setVersions] = useState([]);
  const [selectedVersion, setSelectedVersion] = useState(null);
  const [slabs, setSlabs] = useState([]);

  // Modals
  const [isContractModalOpen, setIsContractModalOpen] = useState(false);
  const [isVersionModalOpen, setIsVersionModalOpen] = useState(false);
  const [isSlabModalOpen, setIsSlabModalOpen] = useState(false);

  // Forms
  const [contractForm, setContractForm] = useState({
    vendorId: '',
    vehicleId: '',
    contractCode: '',
    name: '',
    status: 'ACTIVE',
    billingType: 'FIXED_MONTHLY',
    startDate: '',
    endDate: '',
  });

  const [versionForm, setVersionForm] = useState({
    versionNumber: 1,
    effectiveFrom: '',
    effectiveTo: '',
    billingType: 'FIXED_MONTHLY',
    monthlyFixedFeeRupees: '30000',
    freeKm: '1000',
    freeHours: '100',
    overagePerKmRupees: '12',
    overagePerHourRupees: '150',
    nightChargeRupees: '300',
    waitingChargePerHourRupees: '50',
    tollHandlingChargeRupees: '0',
  });

  const [slabForm, setSlabForm] = useState({
    slabOrder: 1,
    minValue: 0,
    maxValue: 500,
    rateRupees: '10',
    unitType: 'PER_KM',
  });

  const loadContracts = async (p = 0) => {
    setLoading(true);
    setError('');
    try {
      const res = await contractApi.getAll({ page: p, size: 10 });
      setContracts(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const loadDropdowns = async () => {
    try {
      const [vRes, vehRes] = await Promise.all([
        vendorApi.getAll({ size: 100 }),
        vehicleApi.getAll({ size: 100 }),
      ]);
      const vList = vRes.data.content || [];
      const vehList = vehRes.data.content || [];
      setVendors(vList);
      setVehicles(vehList);
      if (vList.length > 0) {
        setContractForm((prev) => ({ ...prev, vendorId: vList[0].id }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadDropdowns();
    loadContracts(0);
  }, []);

  const openContractModal = () => {
    setContractForm({
      vendorId: vendors.length > 0 ? vendors[0].id : '',
      vehicleId: '',
      contractCode: `CNT-${Date.now().toString().slice(-4)}`,
      name: 'Enterprise Fleet Rental Agreement',
      status: 'ACTIVE',
      billingType: 'FIXED_MONTHLY',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '',
    });
    setIsContractModalOpen(true);
  };

  const openVersionModal = () => {
    setVersionForm({
      versionNumber: versions.length + 1,
      effectiveFrom: new Date().toISOString().split('T')[0],
      effectiveTo: '',
      billingType: selectedContract?.billingType || 'FIXED_MONTHLY',
      monthlyFixedFeeRupees: '30000',
      freeKm: '1000',
      freeHours: '100',
      overagePerKmRupees: '12',
      overagePerHourRupees: '150',
      nightChargeRupees: '300',
      waitingChargePerHourRupees: '50',
      tollHandlingChargeRupees: '0',
    });
    setIsVersionModalOpen(true);
  };

  // Expand contract versions
  const handleSelectContract = async (contract) => {
    setSelectedContract(contract);
    setSelectedVersion(null);
    setSlabs([]);
    try {
      const res = await contractApi.getVersions(contract.id);
      setVersions(res.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleSelectVersion = async (version) => {
    setSelectedVersion(version);
    try {
      const res = await contractApi.getSlabs(selectedContract.id, version.id);
      setSlabs(res.data || []);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  // Submit Handlers
  const handleContractSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        contractCode: contractForm.contractCode,
        name: contractForm.name,
        vendorId: Number(contractForm.vendorId),
        vehicleId: contractForm.vehicleId ? Number(contractForm.vehicleId) : null,
        billingType: contractForm.billingType,
        startDate: contractForm.startDate,
        endDate: contractForm.endDate || null,
        status: contractForm.status,
      };
      await contractApi.create(payload);
      setSuccess(`Contract ${contractForm.contractCode} created!`);
      setIsContractModalOpen(false);
      loadContracts(page);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleVersionSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        versionNumber: Number(versionForm.versionNumber),
        effectiveFrom: versionForm.effectiveFrom,
        effectiveTo: versionForm.effectiveTo || null,
        billingType: versionForm.billingType,
        monthlyFixedFeePaisa: Math.round(Number(versionForm.monthlyFixedFeeRupees || 0) * 100),
        freeKm: Number(versionForm.freeKm || 0),
        freeHours: Number(versionForm.freeHours || 0),
        overagePerKmPaisa: Math.round(Number(versionForm.overagePerKmRupees || 0) * 100),
        overagePerHourPaisa: Math.round(Number(versionForm.overagePerHourRupees || 0) * 100),
        nightChargePaisa: Math.round(Number(versionForm.nightChargeRupees || 0) * 100),
        waitingChargePerHourPaisa: Math.round(Number(versionForm.waitingChargePerHourRupees || 0) * 100),
        tollHandlingChargePaisa: Math.round(Number(versionForm.tollHandlingChargeRupees || 0) * 100),
      };
      await contractApi.createVersion(selectedContract.id, payload);
      setSuccess(`Version #${versionForm.versionNumber} added to contract!`);
      setIsVersionModalOpen(false);
      handleSelectContract(selectedContract);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleSlabSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        slabOrder: Number(slabForm.slabOrder),
        minValue: Number(slabForm.minValue),
        maxValue: slabForm.maxValue ? Number(slabForm.maxValue) : null,
        ratePaisa: Math.round(Number(slabForm.rateRupees || 0) * 100),
        unitType: slabForm.unitType,
      };
      await contractApi.createSlab(selectedContract.id, selectedVersion.id, payload);
      setSuccess('Pricing slab added!');
      setIsSlabModalOpen(false);
      handleSelectVersion(selectedVersion);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Contract Management</h1>
          <p className="page-subtitle">Configure monthly contracts, version history, free allowances, and slab pricing.</p>
        </div>
        {isManager && (
          <button className="btn btn-primary" onClick={openContractModal}>
            + Create Contract
          </button>
        )}
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div style={{ display: 'grid', gridTemplateColumns: selectedContract ? '1fr 1fr' : '1fr', gap: '1.5rem' }}>
        {/* Contract Table */}
        <div className="glass-card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>Contracts</h3>
          {loading ? (
            <div className="flex-center" style={{ padding: '2rem' }}><div className="spinner"></div></div>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Code</th>
                    <th>Vendor</th>
                    <th>Billing Type</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {contracts.length === 0 ? (
                    <tr>
                      <td colSpan={5} style={{ textAlign: 'center', padding: '2rem' }}>
                        No contracts found. Click "+ Create Contract" to configure an agreement.
                      </td>
                    </tr>
                  ) : (
                    contracts.map((c) => (
                      <tr key={c.id} style={{ background: selectedContract?.id === c.id ? 'rgba(59, 130, 246, 0.1)' : 'transparent' }}>
                        <td><strong>{c.contractCode}</strong></td>
                        <td>Vendor #{c.vendorId}</td>
                        <td><span className="badge badge-purple">{c.billingType}</span></td>
                        <td><StatusBadge status={c.status} /></td>
                        <td>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleSelectContract(c)}>
                            {selectedContract?.id === c.id ? 'Viewing' : 'Inspect'}
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
          <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadContracts(p)} />
        </div>

        {/* Selected Contract Details & Version History */}
        {selectedContract && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div className="glass-card">
              <div className="flex-between" style={{ marginBottom: '1rem' }}>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>
                  Contract #{selectedContract.contractCode} Versions
                </h3>
                {isManager && (
                  <button className="btn btn-primary btn-sm" onClick={openVersionModal}>
                    + New Version
                  </button>
                )}
              </div>

              {versions.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No contract versions defined yet. Add a version to define pricing.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {versions.map((ver) => (
                    <div
                      key={ver.id}
                      onClick={() => handleSelectVersion(ver)}
                      style={{
                        padding: '1rem',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid ' + (selectedVersion?.id === ver.id ? 'var(--accent-blue)' : 'var(--border-color)'),
                        background: selectedVersion?.id === ver.id ? 'rgba(59, 130, 246, 0.15)' : 'rgba(15, 23, 42, 0.4)',
                        cursor: 'pointer',
                      }}
                    >
                      <div className="flex-between">
                        <strong>Version #{ver.versionNumber}</strong>
                        <span className="badge badge-info">{ver.billingType}</span>
                      </div>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.5rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.35rem' }}>
                        <div>Fixed Fee: <strong>{formatCurrency(ver.monthlyFixedFeePaisa)}</strong></div>
                        <div>Free Km: <strong>{ver.freeKm || 0} km</strong></div>
                        <div>Overage/Km: <strong>{formatCurrency(ver.overagePerKmPaisa)}</strong></div>
                        <div>Night Charge: <strong>{formatCurrency(ver.nightChargePaisa)}</strong></div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Pricing Slabs */}
            {selectedVersion && (
              <div className="glass-card">
                <div className="flex-between" style={{ marginBottom: '1rem' }}>
                  <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>
                    Slabs for Version #{selectedVersion.versionNumber}
                  </h3>
                  {isManager && (
                    <button className="btn btn-secondary btn-sm" onClick={() => setIsSlabModalOpen(true)}>
                      + Add Slab
                    </button>
                  )}
                </div>

                {slabs.length === 0 ? (
                  <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No pricing slabs added to this version.</p>
                ) : (
                  <div className="table-container">
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>Order</th>
                          <th>Range</th>
                          <th>Rate</th>
                          <th>Unit</th>
                        </tr>
                      </thead>
                      <tbody>
                        {slabs.map((s) => (
                          <tr key={s.id}>
                            <td>#{s.slabOrder}</td>
                            <td>{s.minValue} - {s.maxValue ?? '∞'}</td>
                            <td><strong>{formatCurrency(s.ratePaisa)}</strong></td>
                            <td><span className="badge badge-info">{s.unitType}</span></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Modal: Create Contract */}
      <Modal isOpen={isContractModalOpen} onClose={() => setIsContractModalOpen(false)} title="Create New Contract">
        <form onSubmit={handleContractSubmit}>
          <div className="form-group">
            <label className="form-label">Contract Code *</label>
            <input
              type="text"
              className="form-control"
              required
              placeholder="e.g. CNT-2026-001"
              value={contractForm.contractCode}
              onChange={(e) => setContractForm({ ...contractForm, contractCode: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contract Name</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Enterprise Monthly Rental"
              value={contractForm.name}
              onChange={(e) => setContractForm({ ...contractForm, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Vendor *</label>
            <select
              className="form-control"
              required
              value={contractForm.vendorId}
              onChange={(e) => setContractForm({ ...contractForm, vendorId: e.target.value })}
            >
              <option value="">Select Vendor</option>
              {vendors.map((v) => (
                <option key={v.id} value={v.id}>{v.name} (#{v.id})</option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Vehicle (Optional)</label>
            <select
              className="form-control"
              value={contractForm.vehicleId}
              onChange={(e) => setContractForm({ ...contractForm, vehicleId: e.target.value })}
            >
              <option value="">Fleet-wide (All Vehicles)</option>
              {vehicles.map((vh) => (
                <option key={vh.id} value={vh.id}>{vh.registrationNumber} (#{vh.id})</option>
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
              <option value="FIXED_MONTHLY">FIXED_MONTHLY (Monthly Fixed + Overage)</option>
              <option value="PER_KM">PER_KM (Per Kilometre)</option>
              <option value="PER_TRIP">PER_TRIP (Per Trip Fixed)</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Start Date *</label>
            <input
              type="date"
              className="form-control"
              required
              value={contractForm.startDate}
              onChange={(e) => setContractForm({ ...contractForm, startDate: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsContractModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Create Contract</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Create Version */}
      <Modal isOpen={isVersionModalOpen} onClose={() => setIsVersionModalOpen(false)} title={`Create Version for Contract #${selectedContract?.contractCode}`}>
        <form onSubmit={handleVersionSubmit}>
          <div className="form-group">
            <label className="form-label">Version Number *</label>
            <input
              type="number"
              className="form-control"
              required
              min="1"
              value={versionForm.versionNumber}
              onChange={(e) => setVersionForm({ ...versionForm, versionNumber: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Effective From *</label>
            <input
              type="date"
              className="form-control"
              required
              value={versionForm.effectiveFrom}
              onChange={(e) => setVersionForm({ ...versionForm, effectiveFrom: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Billing Type *</label>
            <select
              className="form-control"
              value={versionForm.billingType}
              onChange={(e) => setVersionForm({ ...versionForm, billingType: e.target.value })}
            >
              <option value="FIXED_MONTHLY">FIXED_MONTHLY (Monthly Fixed + Overage)</option>
              <option value="PER_KM">PER_KM (Per Kilometre)</option>
              <option value="PER_TRIP">PER_TRIP (Per Trip Fixed)</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Monthly Fixed Fee (₹)</label>
            <input
              type="number"
              className="form-control"
              value={versionForm.monthlyFixedFeeRupees}
              onChange={(e) => setVersionForm({ ...versionForm, monthlyFixedFeeRupees: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Free Kilometres (km)</label>
            <input
              type="number"
              className="form-control"
              value={versionForm.freeKm}
              onChange={(e) => setVersionForm({ ...versionForm, freeKm: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Overage Rate per Km (₹)</label>
            <input
              type="number"
              className="form-control"
              value={versionForm.overagePerKmRupees}
              onChange={(e) => setVersionForm({ ...versionForm, overagePerKmRupees: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Night Charge Rate (₹)</label>
            <input
              type="number"
              className="form-control"
              value={versionForm.nightChargeRupees}
              onChange={(e) => setVersionForm({ ...versionForm, nightChargeRupees: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsVersionModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Save Version</button>
          </div>
        </form>
      </Modal>

      {/* Modal: Create Slab */}
      <Modal isOpen={isSlabModalOpen} onClose={() => setIsSlabModalOpen(false)} title="Add Pricing Slab">
        <form onSubmit={handleSlabSubmit}>
          <div className="form-group">
            <label className="form-label">Slab Order *</label>
            <input
              type="number"
              className="form-control"
              required
              min="1"
              value={slabForm.slabOrder}
              onChange={(e) => setSlabForm({ ...slabForm, slabOrder: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Min Value *</label>
            <input
              type="number"
              className="form-control"
              required
              min="0"
              value={slabForm.minValue}
              onChange={(e) => setSlabForm({ ...slabForm, minValue: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Max Value (Leave blank for unbounded ∞)</label>
            <input
              type="number"
              className="form-control"
              value={slabForm.maxValue || ''}
              onChange={(e) => setSlabForm({ ...slabForm, maxValue: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Rate (₹) *</label>
            <input
              type="number"
              className="form-control"
              required
              step="0.01"
              value={slabForm.rateRupees}
              onChange={(e) => setSlabForm({ ...slabForm, rateRupees: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Unit Type *</label>
            <select
              className="form-control"
              value={slabForm.unitType}
              onChange={(e) => setSlabForm({ ...slabForm, unitType: e.target.value })}
            >
              <option value="PER_KM">PER_KM</option>
              <option value="PER_HOUR">PER_HOUR</option>
              <option value="PER_TRIP">PER_TRIP</option>
            </select>
          </div>
          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsSlabModalOpen(false)}>Cancel</button>
            <button type="submit" className="btn btn-primary">Add Slab</button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
