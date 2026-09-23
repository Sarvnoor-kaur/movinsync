import React, { useState, useEffect } from 'react';
import { vendorApi } from '../api/vendorApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { SkeletonLoader } from '../components/SkeletonLoader';
import { EmptyState } from '../components/EmptyState';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';
import { Building2, Plus, Edit2, Trash2 } from 'lucide-react';

export const Vendors = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [vendors, setVendors] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentVendorId, setCurrentVendorId] = useState(null);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    contactName: '',
    contactEmail: '',
    contactPhone: '',
    address: '',
  });
  const [submitting, setSubmitting] = useState(false);

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

  const handleOpenAdd = () => {
    setIsEditing(false);
    setCurrentVendorId(null);
    setFormData({ code: '', name: '', contactName: '', contactEmail: '', contactPhone: '', address: '' });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (v) => {
    setIsEditing(true);
    setCurrentVendorId(v.id);
    setFormData({
      code: v.code || '',
      name: v.name || '',
      contactName: v.contactName || '',
      contactEmail: v.contactEmail || '',
      contactPhone: v.contactPhone || '',
      address: v.address || '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      if (isEditing) {
        await vendorApi.update(currentVendorId, formData);
        setSuccess('Vendor updated successfully.');
      } else {
        await vendorApi.create(formData);
        setSuccess('Vendor created successfully.');
      }
      setIsModalOpen(false);
      loadVendors(page);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to deactivate/delete this vendor?')) return;
    setError('');
    setSuccess('');
    try {
      await vendorApi.delete(id);
      setSuccess('Vendor deleted successfully.');
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
          <p className="page-subtitle">Manage registered fleet suppliers and vendor contact profiles.</p>
        </div>
        {isAdmin && (
          <button className="btn btn-primary" onClick={handleOpenAdd}>
            <Plus size={18} /> Register New Vendor
          </button>
        )}
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div className="table-card">
        {loading ? (
          <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
        ) : vendors.length === 0 ? (
          <EmptyState
            icon={Building2}
            title="No vendors registered"
            description="Add your first fleet vendor supplier to start assigning vehicles and contracts."
            actionLabel={isAdmin ? 'Register Vendor' : null}
            onAction={handleOpenAdd}
          />
        ) : (
          <div className="table-container">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>Vendor Code</th>
                  <th>Vendor Name</th>
                  <th>Contact Person</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  {isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {vendors.map((v) => (
                  <tr key={v.id}>
                    <td><strong>{v.code}</strong></td>
                    <td>{v.name}</td>
                    <td>{v.contactName || '—'}</td>
                    <td>{v.contactEmail || '—'}</td>
                    <td>{v.contactPhone || '—'}</td>
                    <td><StatusBadge status={v.active !== false ? 'ACTIVE' : 'INACTIVE'} /></td>
                    {isAdmin && (
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEdit(v)} title="Edit Vendor">
                            <Edit2 size={14} /> Edit
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(v.id)} title="Delete Vendor">
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
        <Pagination page={page} totalPages={totalPages} onPageChange={loadVendors} />
      </div>

      {/* Vendor Form Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={isEditing ? 'Edit Vendor' : 'Register Vendor'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Vendor Code *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. VEND001"
              value={formData.code}
              onChange={(e) => setFormData({ ...formData, code: e.target.value })}
              required
              disabled={isEditing}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Vendor Name *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Metro Fleet Logistics Ltd."
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contact Person Name</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. Rajesh Kumar"
              value={formData.contactName}
              onChange={(e) => setFormData({ ...formData, contactName: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contact Email Address</label>
            <input
              type="email"
              className="form-control"
              placeholder="contact@metrofleet.com"
              value={formData.contactEmail}
              onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Contact Phone Number</label>
            <input
              type="text"
              className="form-control"
              placeholder="+91 98765 43210"
              value={formData.contactPhone}
              onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Address</label>
            <textarea
              className="form-control"
              rows={2}
              placeholder="Fleet Terminal 4, Airport Road, Bengaluru"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
            />
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving Vendor...' : isEditing ? 'Update Vendor' : 'Save Vendor'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
