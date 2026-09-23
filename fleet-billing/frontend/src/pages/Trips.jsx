import React, { useState, useEffect } from 'react';
import { tripApi } from '../api/tripApi';
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
import { MapPin, Plus, Edit2, Trash2 } from 'lucide-react';

export const Trips = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');

  const [trips, setTrips] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentTripId, setCurrentTripId] = useState(null);
  const [formData, setFormData] = useState({
    externalTripId: '',
    vehicleId: '',
    tripDate: '2026-09-15',
    distanceKm: 25.5,
    dutyHours: 4.0,
    waitingHours: 0.5,
    night: false,
    tollAmountPaisa: 15000,
    status: 'COMPLETED',
    startLocation: 'Electronic City Phase 1',
    endLocation: 'Kempegowda International Airport',
  });
  const [submitting, setSubmitting] = useState(false);

  const loadVehicles = async () => {
    try {
      const res = await vehicleApi.getAll({ size: 100 });
      const vList = res.data.content || [];
      setVehicles(vList);
      if (vList.length > 0 && !formData.vehicleId) {
        setFormData((prev) => ({ ...prev, vehicleId: vList[0].id }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const loadTrips = async (p = 0) => {
    setLoading(true);
    setError('');
    try {
      const res = await tripApi.getAll({ page: p, size: 10 });
      setTrips(res.data.content || []);
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
    loadTrips(0);
  }, []);

  const handleOpenAdd = () => {
    setIsEditing(false);
    setCurrentTripId(null);
    setFormData({
      externalTripId: `TR-${Date.now().toString().slice(-6)}`,
      vehicleId: vehicles.length > 0 ? vehicles[0].id : '',
      tripDate: '2026-09-15',
      distanceKm: 25.5,
      dutyHours: 4.0,
      waitingHours: 0.5,
      night: false,
      tollAmountPaisa: 15000,
      status: 'COMPLETED',
      startLocation: 'Electronic City Phase 1',
      endLocation: 'Kempegowda International Airport',
    });
    setIsModalOpen(true);
  };

  const handleOpenEdit = (t) => {
    setIsEditing(true);
    setCurrentTripId(t.id);
    setFormData({
      externalTripId: t.externalTripId || '',
      vehicleId: t.vehicleId || '',
      tripDate: t.tripDate || '',
      distanceKm: t.distanceKm || 0,
      dutyHours: t.dutyHours || 0,
      waitingHours: t.waitingHours || 0,
      night: Boolean(t.night),
      tollAmountPaisa: t.tollAmountPaisa || 0,
      status: t.status || 'COMPLETED',
      startLocation: t.startLocation || '',
      endLocation: t.endLocation || '',
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
        vehicleId: Number(formData.vehicleId),
        distanceKm: Number(formData.distanceKm),
        dutyHours: Number(formData.dutyHours),
        waitingHours: Number(formData.waitingHours),
        tollAmountPaisa: Number(formData.tollAmountPaisa),
      };
      if (isEditing) {
        await tripApi.update(currentTripId, payload);
        setSuccess('Trip updated successfully.');
      } else {
        await tripApi.create(payload);
        setSuccess('Trip logged successfully.');
      }
      setIsModalOpen(false);
      loadTrips(page);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this trip record?')) return;
    setError('');
    setSuccess('');
    try {
      await tripApi.delete(id);
      setSuccess('Trip record deleted.');
      loadTrips(page);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Trip Records</h1>
          <p className="page-subtitle">Log completed vehicle journeys, distance covered, duty hours, and pass-through tolls.</p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenAdd}>
          <Plus size={18} /> Record New Trip
        </button>
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div className="table-card">
        {loading ? (
          <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
        ) : trips.length === 0 ? (
          <EmptyState
            icon={MapPin}
            title="No trips recorded"
            description="Log your first trip journey to record mileage and operational metrics."
            actionLabel="Record Trip"
            onAction={handleOpenAdd}
          />
        ) : (
          <div className="table-container">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>Trip ID</th>
                  <th>External ID</th>
                  <th>Vehicle ID</th>
                  <th>Trip Date</th>
                  <th>Route</th>
                  <th>Distance</th>
                  <th>Duty / Waiting</th>
                  <th>Toll</th>
                  <th>Status</th>
                  {isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {trips.map((t) => (
                  <tr key={t.id}>
                    <td>#{t.id}</td>
                    <td><strong>{t.externalTripId || `TR-${t.id}`}</strong></td>
                    <td>Vehicle #{t.vehicleId}</td>
                    <td>{t.tripDate}</td>
                    <td>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-main)' }}>
                        {t.startLocation || 'Start'} → {t.endLocation || 'End'}
                      </div>
                    </td>
                    <td><strong>{t.distanceKm} km</strong></td>
                    <td>{t.dutyHours}h duty / {t.waitingHours || 0}h wait</td>
                    <td>{formatCurrency(t.tollAmountPaisa || 0)}</td>
                    <td><StatusBadge status={t.status} /></td>
                    {isAdmin && (
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', gap: '0.5rem' }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleOpenEdit(t)}>
                            <Edit2 size={14} /> Edit
                          </button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(t.id)}>
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
        <Pagination page={page} totalPages={totalPages} onPageChange={loadTrips} />
      </div>

      {/* Trip Form Modal */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={isEditing ? 'Edit Trip Record' : 'Record New Trip'}>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">External Trip ID *</label>
            <input
              type="text"
              className="form-control"
              placeholder="e.g. TR-998811"
              value={formData.externalTripId}
              onChange={(e) => setFormData({ ...formData, externalTripId: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Assigned Vehicle *</label>
            <select
              className="form-control"
              value={formData.vehicleId}
              onChange={(e) => setFormData({ ...formData, vehicleId: e.target.value })}
              required
            >
              <option value="">Select Vehicle...</option>
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.registrationNumber} (#{v.id})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Trip Date *</label>
            <input
              type="date"
              className="form-control"
              value={formData.tripDate}
              onChange={(e) => setFormData({ ...formData, tripDate: e.target.value })}
              required
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Distance (KM) *</label>
              <input
                type="number"
                step="0.1"
                className="form-control"
                value={formData.distanceKm}
                onChange={(e) => setFormData({ ...formData, distanceKm: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Duty Hours *</label>
              <input
                type="number"
                step="0.5"
                className="form-control"
                value={formData.dutyHours}
                onChange={(e) => setFormData({ ...formData, dutyHours: e.target.value })}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Waiting Hours</label>
              <input
                type="number"
                step="0.5"
                className="form-control"
                value={formData.waitingHours}
                onChange={(e) => setFormData({ ...formData, waitingHours: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Toll Pass-Through (Paisa)</label>
              <input
                type="number"
                className="form-control"
                placeholder="15000 (₹150)"
                value={formData.tollAmountPaisa}
                onChange={(e) => setFormData({ ...formData, tollAmountPaisa: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Trip Status *</label>
            <select
              className="form-control"
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
            >
              <option value="COMPLETED">COMPLETED</option>
              <option value="CANCELLED">CANCELLED</option>
              <option value="NO_SHOW">NO_SHOW</option>
              <option value="MISSING">MISSING</option>
            </select>
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving Trip...' : isEditing ? 'Update Trip' : 'Save Trip'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
