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
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { SkeletonLoader } from '../components/SkeletonLoader';
import { EmptyState } from '../components/EmptyState';
import {
  Building2,
  Car,
  MapPin,
  ShieldAlert,
  Receipt,
  Plus,
  ArrowRight,
  TrendingUp,
  FileText,
  FileCheck2,
} from 'lucide-react';

export const Dashboard = () => {
  const { user, hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN');
  const isManager = hasRole('ADMIN', 'HR');

  const [stats, setStats] = useState({
    vendorsCount: 0,
    vehiclesCount: 0,
    activeVehiclesCount: 0,
    tripsCount: 0,
    fraudAlertsCount: 0,
  });
  const [recentRuns, setRecentRuns] = useState([]);
  const [recentTrips, setRecentTrips] = useState([]);
  const [recentAlerts, setRecentAlerts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadDashboardData() {
      try {
        const [vRes, vehRes, tRes, alertRes, runsRes, tripsRes] = await Promise.allSettled([
          vendorApi.getAll({ size: 1 }),
          vehicleApi.getAll({ size: 100 }),
          tripApi.getAll({ size: 1 }),
          fraudApi.getAlerts({ status: 'OPEN', size: 5 }),
          billingApi.getRuns({ size: 5 }),
          tripApi.getAll({ page: 0, size: 5 }),
        ]);

        const allVehicles = vehRes.status === 'fulfilled' ? vehRes.value.data.content || [] : [];
        const activeVehicles = allVehicles.filter((v) => v.active !== false);

        setStats({
          vendorsCount: vRes.status === 'fulfilled' ? vRes.value.data.totalElements || 0 : 0,
          vehiclesCount: vehRes.status === 'fulfilled' ? vehRes.value.data.totalElements || allVehicles.length : 0,
          activeVehiclesCount: activeVehicles.length,
          tripsCount: tRes.status === 'fulfilled' ? tRes.value.data.totalElements || 0 : 0,
          fraudAlertsCount: alertRes.status === 'fulfilled' ? alertRes.value.data.totalElements || 0 : 0,
        });

        if (runsRes.status === 'fulfilled') {
          setRecentRuns(runsRes.value.data.content || []);
        }
        if (tripsRes.status === 'fulfilled') {
          setRecentTrips(tripsRes.value.data.content || []);
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

  const activeRatio = stats.vehiclesCount > 0 ? Math.round((stats.activeVehiclesCount / stats.vehiclesCount) * 100) : 100;

  return (
    <div>
      {/* Dashboard Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">Good morning, {user?.name || 'Operator'}</h1>
          <p className="page-subtitle">Here's what's happening across your fleet today.</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          {isManager ? (
            <Link to="/billing-runs" className="btn btn-primary">
              <Plus size={18} /> Execute Billing Run
            </Link>
          ) : (
            <Link to="/trips" className="btn btn-primary">
              <Plus size={18} /> Log Trip
            </Link>
          )}
        </div>
      </div>

      {/* KPI Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '1.25rem',
          marginBottom: '1.75rem',
        }}
      >
        <StatCard
          title="Active Vendors"
          value={stats.vendorsCount}
          icon={Building2}
          color="#2563eb"
          subtitle="Registered suppliers"
        />
        <StatCard
          title="Total Vehicles"
          value={stats.vehiclesCount}
          icon={Car}
          color="#4f46e5"
          subtitle={`${stats.activeVehiclesCount} currently active`}
        />
        <StatCard
          title="Trips Logged"
          value={stats.tripsCount}
          icon={MapPin}
          color="#16a34a"
          subtitle="Recorded fleet journeys"
        />
        <StatCard
          title="Open Fraud Alerts"
          value={stats.fraudAlertsCount}
          icon={ShieldAlert}
          color="#dc2626"
          subtitle={stats.fraudAlertsCount > 0 ? 'Requires security review' : 'No open alerts'}
        />
      </div>

      {/* Quick Actions Shortcuts */}
      <div className="saas-card" style={{ marginBottom: '1.75rem', padding: '1rem 1.25rem' }}>
        <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: '0.75rem' }}>
          Quick Operational Actions
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          {isAdmin && (
            <Link to="/vendors" className="btn btn-secondary btn-sm">
              <Building2 size={16} /> Manage Vendors
            </Link>
          )}
          {isAdmin && (
            <Link to="/vehicles" className="btn btn-secondary btn-sm">
              <Car size={16} /> Add Vehicle
            </Link>
          )}
          {isAdmin && (
            <Link to="/contracts" className="btn btn-secondary btn-sm">
              <FileText size={16} /> Rate Contracts
            </Link>
          )}
          <Link to="/trips" className="btn btn-secondary btn-sm">
            <MapPin size={16} /> Record Trip
          </Link>
          {isManager && (
            <Link to="/billing-runs" className="btn btn-secondary btn-sm">
              <Receipt size={16} /> Billing Engine
            </Link>
          )}
          <Link to="/invoices" className="btn btn-secondary btn-sm">
            <FileCheck2 size={16} /> Tax Invoices
          </Link>
        </div>
      </div>

      {/* Main Grid Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(420px, 1fr))', gap: '1.5rem' }}>
        {/* Fleet Status & Active Ratio */}
        <div className="saas-card">
          <div className="flex-between" style={{ marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Fleet Operational Status</h3>
            <span className="status-badge success">{activeRatio}% Operational</span>
          </div>

          <div style={{ marginBottom: '1.25rem' }}>
            <div className="flex-between" style={{ fontSize: '0.85rem', marginBottom: '0.35rem' }}>
              <span>Active Vehicles</span>
              <strong>{stats.activeVehiclesCount} / {stats.vehiclesCount}</strong>
            </div>
            <div style={{ width: '100%', height: '8px', backgroundColor: '#e2e8f0', borderRadius: '9999px', overflow: 'hidden' }}>
              <div style={{ width: `${activeRatio}%`, height: '100%', backgroundColor: '#16a34a' }} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', background: '#f8fafc', padding: '1rem', borderRadius: 'var(--radius-sm)' }}>
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Active Status</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#16a34a' }}>{stats.activeVehiclesCount}</div>
            </div>
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Inactive / Maintenance</div>
              <div style={{ fontSize: '1.25rem', fontWeight: 700, color: '#64748b' }}>
                {Math.max(stats.vehiclesCount - stats.activeVehiclesCount, 0)}
              </div>
            </div>
          </div>
        </div>

        {/* Recent Billing Runs */}
        <div className="table-card">
          <div className="table-header-bar">
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Recent Billing Runs</h3>
            <Link to="/billing-runs" style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--primary)' }}>
              View All →
            </Link>
          </div>
          {loading ? (
            <div style={{ padding: '1rem' }}><SkeletonLoader rows={3} /></div>
          ) : recentRuns.length === 0 ? (
            <EmptyState
              icon={Receipt}
              title="No billing runs executed yet"
              description="Execute your first billing run to start calculating contract costs."
            />
          ) : (
            <div className="table-container">
              <table className="saas-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Vehicle</th>
                    <th>Month</th>
                    <th>Total Charge</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {recentRuns.map((run) => {
                    const runId = run.billingRunId || run.id;
                    return (
                      <tr key={runId}>
                        <td>#{runId}</td>
                        <td>{run.vehicleRegistrationNumber || `Vehicle #${run.vehicleId}`}</td>
                        <td>{run.billingMonth}</td>
                        <td><strong>{formatCurrency(run.totalPaisa)}</strong></td>
                        <td><StatusBadge status={run.status} /></td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Recent Trips */}
        <div className="table-card" style={{ gridColumn: '1 / -1' }}>
          <div className="table-header-bar">
            <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Recent Trip Activity</h3>
            <Link to="/trips" style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--primary)' }}>
              View All Trips →
            </Link>
          </div>
          {loading ? (
            <div style={{ padding: '1rem' }}><SkeletonLoader rows={4} /></div>
          ) : recentTrips.length === 0 ? (
            <EmptyState
              icon={MapPin}
              title="No trips logged"
              description="Record completed trip journeys to calculate distance and duty hours."
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
                    <th>Distance (km)</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {recentTrips.map((t) => (
                    <tr key={t.id}>
                      <td>#{t.id}</td>
                      <td><strong>{t.externalTripId || `TR-${t.id}`}</strong></td>
                      <td>Vehicle #{t.vehicleId}</td>
                      <td>{t.tripDate}</td>
                      <td>{t.distanceKm != null ? `${t.distanceKm} km` : '—'}</td>
                      <td><StatusBadge status={t.status} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Fraud Security Alerts Widget */}
        <div className="saas-card" style={{ gridColumn: '1 / -1' }}>
          <div className="flex-between" style={{ marginBottom: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <ShieldAlert color="#dc2626" size={20} />
              <h3 style={{ fontSize: '1.05rem', fontWeight: 600 }}>Security & Fraud Monitor</h3>
            </div>
            <Link to="/fraud-alerts" style={{ fontSize: '0.8rem', fontWeight: 600, color: '#dc2626' }}>
              Inspect Alerts →
            </Link>
          </div>

          {loading ? (
            <SkeletonLoader rows={2} />
          ) : recentAlerts.length === 0 ? (
            <div style={{ padding: '1rem', background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 'var(--radius-sm)', color: '#16a34a', fontSize: '0.875rem' }}>
              🎉 No open security anomalies or duplicate trip alerts detected across your fleet.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {recentAlerts.map((alert) => (
                <div
                  key={alert.id}
                  style={{
                    padding: '0.85rem 1rem',
                    background: '#fef2f2',
                    border: '1px solid #fecaca',
                    borderRadius: 'var(--radius-sm)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <StatusBadge status={alert.severity} />
                      <strong style={{ fontSize: '0.875rem', color: '#0f172a' }}>{alert.alertType}</strong>
                    </div>
                    <p style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '0.25rem' }}>
                      {alert.description}
                    </p>
                  </div>
                  <Link to="/fraud-alerts" className="btn btn-danger btn-sm">
                    Inspect Alert
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
