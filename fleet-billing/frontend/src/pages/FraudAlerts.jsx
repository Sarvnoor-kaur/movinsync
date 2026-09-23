import React, { useState, useEffect } from 'react';
import { fraudApi } from '../api/fraudApi';
import { StatusBadge } from '../components/StatusBadge';
import { Pagination } from '../components/Pagination';
import { NotificationBanner } from '../components/NotificationBanner';
import { SkeletonLoader } from '../components/SkeletonLoader';
import { EmptyState } from '../components/EmptyState';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/errorHandler';
import { ShieldAlert, CheckCircle, XCircle, Search } from 'lucide-react';

export const FraudAlerts = () => {
  const { hasRole } = useAuth();
  const isManager = hasRole('ADMIN', 'HR');

  const [alerts, setAlerts] = useState([]);
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadAlerts = async (p = 0, status = statusFilter) => {
    setLoading(true);
    setError('');
    try {
      const params = { page: p, size: 10 };
      if (status) params.status = status;
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
    loadAlerts(0, '');
  }, []);

  const handleFilterChange = (e) => {
    const s = e.target.value;
    setStatusFilter(s);
    loadAlerts(0, s);
  };

  const handleResolve = async (id) => {
    setError('');
    setSuccess('');
    try {
      await fraudApi.resolveAlert(id);
      setSuccess(`Fraud alert #${id} marked as RESOLVED.`);
      loadAlerts(page, statusFilter);
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
      loadAlerts(page, statusFilter);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Fraud & Security Alerts</h1>
          <p className="page-subtitle">Monitor duplicate trip submissions, impossible distance anomalies, and suspicious billing flags.</p>
        </div>
        <select
          className="form-control"
          style={{ width: '200px' }}
          value={statusFilter}
          onChange={handleFilterChange}
        >
          <option value="">All Statuses</option>
          <option value="OPEN">OPEN</option>
          <option value="REVIEWED">REVIEWED</option>
          <option value="RESOLVED">RESOLVED</option>
          <option value="DISMISSED">DISMISSED</option>
        </select>
      </div>

      <NotificationBanner type="error" message={error} onClose={() => setError('')} />
      <NotificationBanner type="success" message={success} onClose={() => setSuccess('')} />

      <div className="table-card">
        <div className="table-header-bar">
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Security Anomaly Register</h3>
        </div>
        {loading ? (
          <div style={{ padding: '1.25rem' }}><SkeletonLoader rows={5} /></div>
        ) : alerts.length === 0 ? (
          <EmptyState
            icon={ShieldAlert}
            title="No security alerts found"
            description="🎉 All operational trips and billing calculations pass security integrity checks."
          />
        ) : (
          <div className="table-container">
            <table className="saas-table">
              <thead>
                <tr>
                  <th>Alert ID</th>
                  <th>Severity</th>
                  <th>Alert Type</th>
                  <th>Description</th>
                  <th>Status</th>
                  {isManager && <th style={{ textAlign: 'right' }}>Actions</th>}
                </tr>
              </thead>
              <tbody>
                {alerts.map((a) => (
                  <tr key={a.id}>
                    <td>#{a.id}</td>
                    <td><StatusBadge status={a.severity} /></td>
                    <td><strong>{a.alertType}</strong></td>
                    <td style={{ maxWidth: 360 }}>{a.description}</td>
                    <td><StatusBadge status={a.status} /></td>
                    {isManager && (
                      <td style={{ textAlign: 'right' }}>
                        {a.status === 'OPEN' || a.status === 'REVIEWED' ? (
                          <div style={{ display: 'inline-flex', gap: '0.35rem' }}>
                            <button
                              className="btn btn-primary btn-sm"
                              onClick={() => handleResolve(a.id)}
                              title="Resolve Alert"
                            >
                              <CheckCircle size={14} /> Resolve
                            </button>
                            <button
                              className="btn btn-secondary btn-sm"
                              onClick={() => handleDismiss(a.id)}
                              title="Dismiss Alert"
                            >
                              <XCircle size={14} /> Dismiss
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.775rem', color: 'var(--text-muted)' }}>—</span>
                        )}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Pagination page={page} totalPages={totalPages} onPageChange={(p) => loadAlerts(p, statusFilter)} />
      </div>
    </div>
  );
};
