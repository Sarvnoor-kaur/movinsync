import React, { useState, useEffect } from 'react';
import { StatCard } from '../components/StatCard';
import { StatusBadge } from '../components/StatusBadge';
import { formatCurrency } from '../utils/formatCurrency';
import { formatDateTime } from '../utils/formatDate';
import { billingApi } from '../api/billingApi';
import { fraudApi } from '../api/fraudApi';
import { vendorApi } from '../api/vendorApi';
import { vehicleApi } from '../api/vehicleApi';
import { tripApi } from '../api/tripApi';
import { Link } from 'react-router-dom';

export const Dashboard = () => {
  const [stats, setStats] = useState({
    vendorsCount: 0,
    vehiclesCount: 0,
    tripsCount: 0,
    fraudAlertsCount: 0,
  });
  const [recentRuns, setRecentRuns] = useState([]);
  const [recentAlerts, setRecentAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadDashboardData() {
      try {
        const [vRes, vehRes, tRes, alertRes, runsRes] = await Promise.allSettled([
          vendorApi.getAll({ size: 1 }),
          vehicleApi.getAll({ size: 1 }),
          tripApi.getAll({ size: 1 }),
          fraudApi.getAlerts({ status: 'OPEN', size: 5 }),
          billingApi.getRuns({ size: 5 }),
        ]);

        setStats({
          vendorsCount: vRes.status === 'fulfilled' ? vRes.value.data.totalElements || 0 : 0,
          vehiclesCount: vehRes.status === 'fulfilled' ? vehRes.value.data.totalElements || 0 : 0,
          tripsCount: tRes.status === 'fulfilled' ? tRes.value.data.totalElements || 0 : 0,
          fraudAlertsCount: alertRes.status === 'fulfilled' ? alertRes.value.data.totalElements || 0 : 0,
        });

        if (runsRes.status === 'fulfilled') {
          setRecentRuns(runsRes.value.data.content || []);
        }
        if (alertRes.status === 'fulfilled') {
          setRecentAlerts(alertRes.value.data.content || []);
        }
      } catch (err) {
        console.error('Dashboard load error', err);
      } finally {
        setLoading(false);
      }
    }
    loadDashboardData();
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Fleet Dashboard</h1>
          <p className="page-subtitle">Overview of fleet operations, cost allocations, and fraud security alerts.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Link to="/billing-runs" className="btn btn-primary">
            + Execute Billing Run
          </Link>
          <Link to="/trips" className="btn btn-secondary">
            + Log Trip
          </Link>
        </div>
      </div>

      <div className="stat-grid">
        <StatCard title="Active Vendors" value={stats.vendorsCount} icon="🏢" color="#3b82f6" />
        <StatCard title="Total Vehicles" value={stats.vehiclesCount} icon="🚘" color="#6366f1" />
        <StatCard title="Trips Logged" value={stats.tripsCount} icon="🗺️" color="#10b981" />
        <StatCard title="Open Fraud Alerts" value={stats.fraudAlertsCount} icon="🚨" color="#f43f5e" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '1.5rem' }}>
        {/* Recent Billing Runs */}
        <div className="glass-card">
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Recent Billing Runs</h3>
            <Link to="/billing-runs" style={{ color: 'var(--accent-blue)', textDecoration: 'none', fontSize: '0.85rem' }}>
              View All →
            </Link>
          </div>
          {loading ? (
            <div className="flex-center" style={{ padding: '2rem' }}><div className="spinner"></div></div>
          ) : recentRuns.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '2rem 0' }}>
              No billing runs recorded yet.
            </p>
          ) : (
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Vehicle ID</th>
                    <th>Period</th>
                    <th>Total Charge</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {recentRuns.map((run) => (
                    <tr key={run.id}>
                      <td>#{run.id}</td>
                      <td>Vehicle #{run.vehicleId}</td>
                      <td>{run.billingMonth}/{run.billingYear}</td>
                      <td><strong>{formatCurrency(run.totalAmountPaisa)}</strong></td>
                      <td><StatusBadge status={run.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Recent Fraud Alerts */}
        <div className="glass-card">
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>Open Security Alerts</h3>
            <Link to="/fraud-alerts" style={{ color: 'var(--accent-rose)', textDecoration: 'none', fontSize: '0.85rem' }}>
              Review Alerts →
            </Link>
          </div>
          {loading ? (
            <div className="flex-center" style={{ padding: '2rem' }}><div className="spinner"></div></div>
          ) : recentAlerts.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '2rem 0' }}>
              🎉 No open security or fraud alerts detected!
            </p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {recentAlerts.map((alert) => (
                <div
                  key={alert.id}
                  style={{
                    padding: '0.85rem',
                    background: 'rgba(244, 63, 94, 0.08)',
                    border: '1px solid rgba(244, 63, 94, 0.25)',
                    borderRadius: 'var(--radius-md)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <StatusBadge status={alert.severity} />
                      <strong style={{ fontSize: '0.9rem' }}>{alert.alertType}</strong>
                    </div>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                      {alert.description}
                    </p>
                  </div>
                  <Link to="/fraud-alerts" className="btn btn-danger btn-sm">
                    Inspect
                  </Link>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
