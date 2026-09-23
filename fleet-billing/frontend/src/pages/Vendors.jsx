import React, { useState, useEffect } from 'react';
import { vendorApi } from '../api/vendorApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';

export const Vendors = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [vendors, setVendors] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingVendor, setEditingVendor] = useState(null);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    contactName: '',
    contactEmail: '',
    contactPhone: '',
    address: '',
    active: true,
  });

  const loadVendors = async (p = 0) => {
    setLoading(true);
    setError('');
    try {
      const res = await vendorApi.getAll({ page: p, size: 10 });
      setVendors(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVendors(0);
  }, []);

  const openCreateModal = () => {
    setEditingVendor(null);
    setFormData({
      code: `VND-${Date.now().toString().slice(-4)}`,
      name: '',
      contactName: '',
      contactEmail: '',
      contactPhone: '',
      address: '',
      active: true,
    });
    setIsModalOpen(true);
  };

  const openEditModal = (vendor) => {
    setEditingVendor(vendor);
    setFormData({
      code: vendor.code || '',
      name: vendor.name || '',
      contactName: vendor.contactName || '',
      contactEmail: vendor.contactEmail || '',
      contactPhone: vendor.contactPhone || '',
      address: vendor.address || '',
      active: vendor.active !== undefined ? vendor.active : true,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      if (editingVendor) {
        await vendorApi.update(editingVendor.id, {
          name: formData.name,
          contactName: formData.contactName,
          contactEmail: formData.contactEmail,
          contactPhone: formData.contactPhone,
          address: formData.address,
          active: formData.active,
        });
        setSuccess(`Vendor "${formData.name}" updated successfully!`);
      } else {
        await vendorApi.create({
          code: formData.code,
          name: formData.name,
          contactName: formData.contactName,
          contactEmail: formData.contactEmail,
          contactPhone: formData.contactPhone,
          address: formData.address,
        });
        setSuccess(`Vendor "${formData.name}" registered successfully!`);
      }
      setIsModalOpen(false);
      loadVendors(page);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleDelete = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete vendor "${name}"?`)) return;
    try {
      await vendorApi.delete(id);
      setSuccess(`Vendor "${name}" deleted.`);
      loadVendors(page);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Vendor Management</h1>
          <p className="page-subtitle">Manage fleet suppliers, transport partners, and contract entities.</p>
        </div>
        {isManager && (
          <button className="btn btn-primary" onClick={openCreateModal}>
            + Add Vendor
          </button>
        )}
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
                <th>Code</th>
                <th>Vendor Name</th>
                <th>Contact Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Address</th>
                <th>Status</th>
                {isManager && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {vendors.length === 0 ? (
                <tr>
                  <td colSpan={isManager ? 9 : 8} style={{ textAlign: 'center', padding: '2rem' }}>
                    No vendors found. Click "+ Add Vendor" to register a fleet vendor.
                  </td>
                </tr>
              ) : (
                vendors.map((vendor) => (
                  <tr key={vendor.id}>
                    <td>#{vendor.id}</td>
                    <td><span className="badge badge-info">{vendor.code}</span></td>
                    <td><strong>{vendor.name}</strong></td>
                    <td>{vendor.contactName || '—'}</td>
                    <td>{vendor.contactEmail || '—'}</td>
                    <td>{vendor.contactPhone || '—'}</td>
                    <td>{vendor.address || '—'}</td>
                    <td><StatusBadge status={vendor.active ? 'ACTIVE' : 'INACTIVE'} /></td>
                    {isManager && (
                      <td>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(vendor)}>
                            Edit
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(vendor.id, vendor.name)}>
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

      <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadVendors(p)} />

      {/* Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingVendor ? `Edit Vendor #${editingVendor.id}` : 'Register New Vendor'}
      >
        <form onSubmit={handleSubmit}>
          {!editingVendor && (
            <div className="form-group">
              <label className="form-label">Vendor Code *</label>
              <input
                type="text"
                className="form-control"
                required
                placeholder="e.g. VND-001"
                value={formData.code}
                onChange={(e) => setFormData({ ...formData, code: e.target.value })}
              />
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Vendor Name *</label>
            <input
              type="text"
              className="form-control"
              required
              placeholder="e.g. Cityline Mobility Services"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contact Person Name *</label>
            <input
              type="text"
              className="form-control"
              required
              placeholder="e.g. Rajesh Kumar"
              value={formData.contactName}
              onChange={(e) => setFormData({ ...formData, contactName: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contact Email *</label>
            <input
              type="email"
              className="form-control"
              required
              placeholder="rajesh@cityline.com"
              value={formData.contactEmail}
              onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contact Phone</label>
            <input
              type="text"
              className="form-control"
              placeholder="+91 9876543210"
              value={formData.contactPhone}
              onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Address</label>
            <textarea
              className="form-control"
              rows="2"
              placeholder="123 Fleet Way, Tech Hub"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
            ></textarea>
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              {editingVendor ? 'Save Changes' : 'Create Vendor'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
