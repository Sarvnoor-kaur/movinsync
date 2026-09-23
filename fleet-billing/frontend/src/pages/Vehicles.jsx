import React, { useState, useEffect } from 'react';
import { vehicleApi } from '../api/vehicleApi';
import { vendorApi } from '../api/vendorApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { SkeletonLoader } from '../components/SkeletonLoader';
import { EmptyState } from '../components/EmptyState';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';
import { Car, Plus, Edit2, Trash2 } from 'lucide-react';

export const Vehicles = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [vehicles, setVehicles] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentVehicleId, setCurrentVehicleId] = useState(null);
  const [formData, setFormData] = useState({
    registrationNumber: '',
    vendorId: '',
    make: '',
    model: '',
    vehicleType: 'CAB',
  });
  const [submitting, setSubmitting] = useState(false);

  const loadVendors = async () => {
    try {
      const res = await vendorApi.getAll({ size: 100 });
      setVendors(res.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  const loadVehicles = async (p = 0) => {
    setLoading(true);
    setError('');
    try {
      const res = await vehicleApi.getAll({ page: p, size: 10 });
      setVehicles(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVendors();
    loadVehicles(0);
  }, []);

  const handleOpenAdd = () => {
    setIsEditing(false);
    setCurrentVehicleId(null);
    setFormData({
      registrationNumber: '',
      vendorId: vendors.length > 0 ? vendors[0].id : '',
      make: '',
      model: '',
      vehicleType: 'CAB',
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (v) => {
    setIsEditing(true);
    setCurrentVehicleId(v.id);
    setFormData({
      registrationNumber: v.registrationNumber || '',
      vendorId: v.vendorId || v.vendor?.id || '',
      make: v.make || '',
      model: v.model || '',
      vehicleType: v.vehicleType || 'CAB',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      const payload = {
        ...formData,
        vendorId: Number(formData.vendorId),
      };
      if (isEditing) {
        await vehicleApi.update(currentVehicleId, payload);
        setSuccess('Vehicle updated successfully.');
      } else {
        await vehicleApi.create(payload);
        setSuccess('Vehicle added successfully.');
      }
      setIsModalOpen(false);
      loadVehicles(page);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate this vehicle?')) return;
    setError('');
    setSuccess('');
    try {
      await vehicleApi.delete(id);
      setSuccess('Vehicle deactivated successfully.');
      loadVehicles(page);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Vehicle Fleet</h1>
          <p className="page-subtitle">Track fleet registration numbers, vehicle types, and vendor assignments.</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={handleOpenAdd}>
            <Plus size={18} /> Add New Vehicle
          </button>
        )}
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div className="table-card">
        {loading ? (
          <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
        ) : vehicles.length === 0 ? (
          <EmptyState
            icon={Car}
            title="No vehicles in fleet"
            description="Add your first vehicle registration to start recording trip metrics."
            actionLabel={isAdmin ? 'Add Vehicle' : null}
            onAction={handleOpenAdd}
          />
        ) : (
          <div className="table-container">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>Vehicle Reg Number</th>
                  <th>Vendor ID / Name</th>
                  <th>Make & Model</th>
                  <th>Vehicle Type</th>
                  <th>Status</th>
                  {isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {vehicles.map((v) => (
                  <tr key={v.id}>
                    <td><strong>{v.registrationNumber}</strong></td>
                    <td>{v.vendorName || `Vendor #${v.vendorId || v.vendor?.id}`}</td>
                    <td>{v.make && v.model ? `${v.make} ${v.model}` : v.make || v.model || '—'}</td>
                    <td><StatusBadge status={v.vehicleType || 'CAB'} /></td>
                    <td><StatusBadge status={v.active !== false ? 'ACTIVE' : 'INACTIVE'} /></td>
                    {isAdmin && (
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEdit(v)} title="Edit Vehicle">
                            <Edit2 size={14} /> Edit
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(v.id)} title="Delete Vehicle">
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Pagination page={page} totalPages={totalPages} onPageChange={loadVehicles} />
      </div>

      {/* Vehicle Form Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={isEditing ? 'Edit Vehicle' : 'Add Vehicle'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Registration Number *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. KA-01-AB-1234"
              value={formData.registrationNumber}
              onChange={(e) => setFormData({ ...formData, registrationNumber: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Assigned Vendor *</label>
            <select
              className="form-control"
              value={formData.vendorId}
              onChange={(e) => setFormData({ ...formData, vendorId: e.target.value })}
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
            <label className="form-label">Make</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Toyota"
              value={formData.make}
              onChange={(e) => setFormData({ ...formData, make: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Model</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Innova Crysta"
              value={formData.model}
              onChange={(e) => setFormData({ ...formData, model: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Vehicle Type *</label>
            <select
              className="form-control"
              value={formData.vehicleType}
              onChange={(e) => setFormData({ ...formData, vehicleType: e.target.value })}
            >
              <option value="CAB">Cab / Sedan</option>
              <option value="SUV">SUV</option>
              <option value="VAN">Van / Traveller</option>
              <option value="BUS">Bus</option>
            </select>
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving Vehicle...' : isEditing ? 'Update Vehicle' : 'Save Vehicle'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
