import React, { useState, useEffect } from 'react';
import { vehicleApi } from '../api/vehicleApi';
import { vendorApi } from '../api/vendorApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';

export const Vehicles = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [vehicles, setVehicles] = useState([]);
  const [vendors, setVendors] = useState([]);
  const [selectedVendorId, setSelectedVendorId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState(null);
  const [formData, setFormData] = useState({
    registrationNumber: '',
    vendorId: '',
    vehicleType: 'SEDAN',
    make: '',
    model: '',
    active: true,
  });

  const loadVehicles = async (p = 0, vId = selectedVendorId) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (vId) params.vendorId = vId;
      const res = await vehicleApi.getAll(params);
      setVehicles(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const loadVendorsList = async () => {
    try {
      const res = await vendorApi.getAll({ size: 100 });
      setVendors(res.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadVendorsList();
    loadVehicles(0, '');
  }, []);

  const handleFilterChange = (e) => {
    const vId = e.target.value;
    setSelectedVendorId(vId);
    loadVehicles(0, vId);
  };

  const openCreateModal = () => {
    setEditingVehicle(null);
    setFormData({
      registrationNumber: '',
      vendorId: vendors.length > 0 ? vendors[0].id : '',
      vehicleType: 'SEDAN',
      make: '',
      model: '',
      active: true,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (veh) => {
    setEditingVehicle(veh);
    setFormData({
      registrationNumber: veh.registrationNumber || '',
      vendorId: veh.vendorId || '',
      vehicleType: veh.vehicleType || 'SEDAN',
      make: veh.make || '',
      model: veh.model || '',
      active: veh.active !== undefined ? veh.active : true,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        ...formData,
        vendorId: Number(formData.vendorId),
      };

      if (editingVehicle) {
        await vehicleApi.update(editingVehicle.id, payload);
        setSuccess(`Vehicle ${formData.registrationNumber} updated successfully!`);
      } else {
        await vehicleApi.create(payload);
        setSuccess(`Vehicle ${formData.registrationNumber} created successfully!`);
      }
      setIsModalOpen(false);
      loadVehicles(page, selectedVendorId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleDelete = async (id, regNo) => {
    if (!window.confirm(`Are you sure you want to delete vehicle ${regNo}?`)) return;
    try {
      await vehicleApi.delete(id);
      setSuccess(`Vehicle ${regNo} deleted.`);
      loadVehicles(page, selectedVendorId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Vehicle Management</h1>
          <p className="page-subtitle">Track registered fleet vehicles, types, and vendor assignments.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <select
            className="form-control"
            style={{ width: '220px' }}
            value={selectedVendorId}
            onChange={handleFilterChange}
          >
            <option value="">All Vendors</option>
            {vendors.map((v) => (
              <option key={v.id} value={v.id}>
                {v.name}
              </option>
            ))}
          </select>
          {isManager && (
            <button className="btn btn-primary" onClick={openCreateModal}>
              + Add Vehicle
            </button>
          )}
        </div>
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      {loading ? (
        <div className="flex-center" style={{ padding: '3rem' }}><div className="spinner"></div></div>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Registration No.</th>
                <th>Vendor ID</th>
                <th>Vehicle Type</th>
                <th>Make / Model</th>
                <th>Status</th>
                {isManager && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {vehicles.length === 0 ? (
                <tr>
                  <td colSpan={isManager ? 7 : 6} style={{ textAlign: 'center', padding: '2rem' }}>
                    No vehicles found. Register a vehicle to begin.
                  </td>
                </tr>
              ) : (
                vehicles.map((veh) => (
                  <tr key={veh.id}>
                    <td>#{veh.id}</td>
                    <td><strong>{veh.registrationNumber}</strong></td>
                    <td>Vendor #{veh.vendorId}</td>
                    <td><span className="badge badge-purple">{veh.vehicleType}</span></td>
                    <td>{veh.make || ''} {veh.model || ''}</td>
                    <td><StatusBadge status={veh.active ? 'ACTIVE' : 'INACTIVE'} /></td>
                    {isManager && (
                      <td>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(veh)}>
                            Edit
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(veh.id, veh.registrationNumber)}>
                            Delete
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadVehicles(p, selectedVendorId)} />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingVehicle ? `Edit Vehicle #${editingVehicle.id}` : 'Register New Vehicle'}
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Registration Number *</label>
            <input
              type="text"
              className="form-control"
              required
              placeholder="e.g. KA01AB1234"
              value={formData.registrationNumber}
              onChange={(e) => setFormData({ ...formData, registrationNumber: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Vendor *</label>
            <select
              className="form-control"
              required
              value={formData.vendorId}
              onChange={(e) => setFormData({ ...formData, vendorId: e.target.value })}
            >
              <option value="">Select Vendor</option>
              {vendors.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.name} (#{v.id})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Vehicle Type *</label>
            <select
              className="form-control"
              value={formData.vehicleType}
              onChange={(e) => setFormData({ ...formData, vehicleType: e.target.value })}
            >
              <option value="SEDAN">SEDAN</option>
              <option value="SUV">SUV</option>
              <option value="HATCHBACK">HATCHBACK</option>
              <option value="BUS">BUS</option>
              <option value="VAN">VAN</option>
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
              placeholder="e.g. Innova"
              value={formData.model}
              onChange={(e) => setFormData({ ...formData, model: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingVehicle ? 'Save Changes' : 'Create Vehicle'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
