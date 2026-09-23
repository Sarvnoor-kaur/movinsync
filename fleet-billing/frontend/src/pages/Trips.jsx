import React, { useState, useEffect } from 'react';
import { tripApi } from '../api/tripApi';
import { vehicleApi } from '../api/vehicleApi';
import { StatusBadge } from '../components/StatusBadge';
import { Modal } from '../components/Modal';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { formatCurrency } from '../utils/formatCurrency';
import { formatDateTime } from '../utils/formatDate';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';

export const Trips = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [trips, setTrips] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTrip, setEditingTrip] = useState(null);
  const [formData, setFormData] = useState({
    externalTripId: '',
    vehicleId: '',
    tripDate: new Date().toISOString().split('T')[0],
    startTime: '09:00',
    endTime: '11:00',
    distanceKm: '25.5',
    dutyHours: '2.0',
    waitingHours: '0.0',
    tollAmountRupees: '50',
    night: false,
    status: 'COMPLETED',
    startLocation: '',
    endLocation: '',
  });

  const loadTrips = async (p = 0, vId = selectedVehicleId) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (vId) params.vehicleId = vId;
      const res = await tripApi.getAll(params);
      setTrips(res.data.content || []);
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
      setVehicles(res.data.content || []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadVehicles();
    loadTrips(0, '');
  }, []);

  const handleFilterChange = (e) => {
    const vId = e.target.value;
    setSelectedVehicleId(vId);
    loadTrips(0, vId);
  };

  const openCreateModal = () => {
    setEditingTrip(null);
    setFormData({
      externalTripId: `TRIP-${Date.now().toString().slice(-6)}`,
      vehicleId: vehicles.length > 0 ? vehicles[0].id : '',
      tripDate: new Date().toISOString().split('T')[0],
      startTime: '09:00',
      endTime: '11:00',
      distanceKm: '25.0',
      dutyHours: '2.0',
      waitingHours: '0.0',
      tollAmountRupees: '0',
      night: false,
      status: 'COMPLETED',
      startLocation: 'Office HQ',
      endLocation: 'Tech Park',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const payload = {
        externalTripId: formData.externalTripId,
        vehicleId: Number(formData.vehicleId),
        tripDate: formData.tripDate,
        startTime: formData.startTime ? `${formData.startTime}:00` : null,
        endTime: formData.endTime ? `${formData.endTime}:00` : null,
        distanceKm: Number(formData.distanceKm),
        dutyHours: Number(formData.dutyHours),
        waitingHours: Number(formData.waitingHours),
        tollAmountPaisa: Math.round(Number(formData.tollAmountRupees || 0) * 100),
        night: formData.night,
        status: formData.status,
        startLocation: formData.startLocation,
        endLocation: formData.endLocation,
      };

      if (editingTrip) {
        await tripApi.update(editingTrip.id, payload);
        setSuccess(`Trip #${editingTrip.id} updated!`);
      } else {
        await tripApi.create(payload);
        setSuccess(`Trip ${formData.externalTripId} logged successfully!`);
      }
      setIsModalOpen(false);
      loadTrips(page, selectedVehicleId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Are you sure you want to delete Trip #${id}?`)) return;
    try {
      await tripApi.delete(id);
      setSuccess(`Trip #${id} deleted.`);
      loadTrips(page, selectedVehicleId);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Trip Logs</h1>
          <p className="page-subtitle">Track individual vehicle trips, distance, duty hours, night surcharges, and tolls.</p>
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
            <button className="btn btn-primary" onClick={openCreateModal}>
              + Log New Trip
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
                <th>Trip Ref</th>
                <th>Vehicle</th>
                <th>Date</th>
                <th>Distance</th>
                <th>Duty Hours</th>
                <th>Toll</th>
                <th>Night</th>
                <th>Status</th>
                {isManager && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {trips.length === 0 ? (
                <tr>
                  <td colSpan={isManager ? 10 : 9} style={{ textAlign: 'center', padding: '2rem' }}>
                    No trips found for selected vehicle.
                  </td>
                </tr>
              ) : (
                trips.map((t) => (
                  <tr key={t.id}>
                    <td>#{t.id}</td>
                    <td><strong>{t.externalTripId}</strong></td>
                    <td>Vehicle #{t.vehicleId}</td>
                    <td>{t.tripDate}</td>
                    <td>{t.distanceKm} km</td>
                    <td>{t.dutyHours} hrs</td>
                    <td>{formatCurrency(t.tollAmountPaisa)}</td>
                    <td>{t.night ? <span className="badge badge-purple">NIGHT</span> : 'Day'}</td>
                    <td><StatusBadge status={t.status} /></td>
                    {isManager && (
                      <td>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(t.id)}>
                          Delete
                        </button>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadTrips(p, selectedVehicleId)} />

      {/* Modal: Log Trip */}
      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Log New Vehicle Trip">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">External Trip ID / Reference *</label>
            <input
              type="text"
              className="form-control"
              required
              value={formData.externalTripId}
              onChange={(e) => setFormData({ ...formData, externalTripId: e.target.value })}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Vehicle *</label>
            <select
              className="form-control"
              required
              value={formData.vehicleId}
              onChange={(e) => setFormData({ ...formData, vehicleId: e.target.value })}
            >
              <option value="">Select Vehicle</option>
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.registrationNumber} (#{v.id})
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Trip Date *</label>
              <input
                type="date"
                className="form-control"
                required
                value={formData.tripDate}
                onChange={(e) => setFormData({ ...formData, tripDate: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Status *</label>
              <select
                className="form-control"
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="COMPLETED">COMPLETED</option>
                <option value="PENDING">PENDING</option>
                <option value="FLAGGED">FLAGGED</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Start Time</label>
              <input
                type="time"
                className="form-control"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">End Time</label>
              <input
                type="time"
                className="form-control"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Distance (km) *</label>
              <input
                type="number"
                step="0.1"
                className="form-control"
                required
                value={formData.distanceKm}
                onChange={(e) => setFormData({ ...formData, distanceKm: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Duty Hours *</label>
              <input
                type="number"
                step="0.1"
                className="form-control"
                required
                value={formData.dutyHours}
                onChange={(e) => setFormData({ ...formData, dutyHours: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Waiting Hrs *</label>
              <input
                type="number"
                step="0.1"
                className="form-control"
                required
                value={formData.waitingHours}
                onChange={(e) => setFormData({ ...formData, waitingHours: e.target.value })}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Toll Amount (₹)</label>
              <input
                type="number"
                className="form-control"
                value={formData.tollAmountRupees}
                onChange={(e) => setFormData({ ...formData, tollAmountRupees: e.target.value })}
              />
            </div>
            <div className="form-group" style={{ justifyContent: 'center' }}>
              <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', marginTop: '1.5rem' }}>
                <input
                  type="checkbox"
                  checked={formData.night}
                  onChange={(e) => setFormData({ ...formData, night: e.target.checked })}
                />
                Is Night Duty?
              </label>
            </div>
          </div>

          <div className="modal-footer" style={{ padding: '1rem 0 0 0' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Submit Trip Log
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
