import React, { useState, useEffect } from 'react';
import { fraudApi } from '../api/fraudApi';
import { StatusBadge } from '../components/StatusBadge';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { formatDateTime } from '../utils/formatDate';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';

export const FraudAlerts = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [alerts, setAlerts] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadAlerts = async (p = 0, status = statusFilter, severity = severityFilter) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (status) params.status = status;
      if (severity) params.severity = severity;
      const res = await fraudApi.getAlerts(params);
      setAlerts(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setPage(p);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAlerts(0, '', '');
  }, []);

  const handleStatusFilter = (e) => {
    const s = e.target.value;
    setStatusFilter(s);
    loadAlerts(0, s, severityFilter);
  };

  const handleSeverityFilter = (e) => {
    const sev = e.target.value;
    setSeverityFilter(sev);
    loadAlerts(0, statusFilter, sev);
  };

  const handleResolve = async (id) => {
    setError('');
    setSuccess('');
    try {
      await fraudApi.resolveAlert(id);
      setSuccess(`Fraud alert #${id} marked as RESOLVED.`);
      loadAlerts(page, statusFilter, severityFilter);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const handleDismiss = async (id) => {
    setError('');
    setSuccess('');
    try {
      await fraudApi.dismissAlert(id);
      setSuccess(`Fraud alert #${id} DISMISSED.`);
      loadAlerts(page, statusFilter, severityFilter);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Fraud & Anomaly Alerts</h1>
          <p className="page-subtitle">Monitor suspicious trip overlaps, abnormal km spikes, missing contracts, and audit logs.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <select className="form-control" style={{ width: '160px' }} value={statusFilter} onChange={handleStatusFilter}>
            <option value="">All Statuses</option>
            <option value="OPEN">OPEN</option>
            <option value="RESOLVED">RESOLVED</option>
            <option value="DISMISSED">DISMISSED</option>
          </select>
          <select className="form-control" style={{ width: '160px' }} value={severityFilter} onChange={handleSeverityFilter}>
            <option value="">All Severities</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="HIGH">HIGH</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="LOW">LOW</option>
          </select>
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
                <th>Alert Type</th>
                <th>Severity</th>
                <th>Description</th>
                <th>Vehicle / Trip</th>
                <th>Detected At</th>
                <th>Status</th>
                {isManager && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {alerts.length === 0 ? (
                <tr>
                  <td colSpan={isManager ? 8 : 7} style={{ textAlign: 'center', padding: '2rem' }}>
                    🎉 No security or fraud alerts found.
                  </td>
                </tr>
              ) : (
                alerts.map((alert) => (
                  <tr key={alert.id}>
                    <td>#{alert.id}</td>
                    <td><strong>{alert.alertType}</strong></td>
                    <td><StatusBadge status={alert.severity} /></td>
                    <td style={{ maxWidth: '300px' }}>{alert.description}</td>
                    <td>
                      {alert.vehicleId && <div>Vehicle #{alert.vehicleId}</div>}
                      {alert.tripId && <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Trip #{alert.tripId}</div>}
                    </td>
                    <td>{formatDateTime(alert.createdAt)}</td>
                    <td><StatusBadge status={alert.status} /></td>
                    {isManager && (
                      <td>
                        {alert.status === 'OPEN' ? (
                          <div style={{ display: 'flex', gap: '0.35rem' }}>
                            <button className="btn btn-primary btn-sm" onClick={() => handleResolve(alert.id)}>
                              Resolve
                            </button>
                            <button className="btn btn-secondary btn-sm" onClick={() => handleDismiss(alert.id)}>
                              Dismiss
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Actioned</span>
                        )}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadAlerts(p, statusFilter, severityFilter)} />
    </div>
  );
};
