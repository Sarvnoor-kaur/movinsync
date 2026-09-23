import React from 'react';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from '../components/StatusBadge';
import { User, Mail, Shield, Key, Calendar } from 'lucide-react';

export const Profile = () => {
  const { user } = useAuth();

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">User Profile</h1>
          <p className="page-subtitle">Manage your FleetFlow enterprise account profile and security session.</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
        {/* Account Details */}
        <div className="saas-card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1.5rem', paddingBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
            <div
              style={{
                width: 56,
                height: 56,
                borderRadius: '50%',
                backgroundColor: 'var(--primary-light)',
                color: 'var(--primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.5rem',
                fontWeight: 700,
                border: '1px solid var(--border-color)',
              }}
            >
              {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
            </div>
            <div>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)' }}>{user?.name || 'User Account'}</h2>
              <div style={{ marginTop: '0.2rem' }}>
                <StatusBadge status={user?.role || 'EMPLOYEE'} />
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <Mail size={18} color="var(--text-muted)" />
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Email Address</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600 }}>{user?.email || 'N/A'}</div>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <Shield size={18} color="var(--text-muted)" />
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Assigned Operational Role</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600 }}>{user?.role || 'EMPLOYEE'}</div>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <Key size={18} color="var(--text-muted)" />
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Authentication Provider</div>
                <div style={{ fontSize: '0.9rem', fontWeight: 600 }}>Spring Security JWT Bearer Token</div>
              </div>
            </div>
          </div>
        </div>

        {/* Role Permissions Card */}
        <div className="saas-card">
          <h3 style={{ fontSize: '1.05rem', fontWeight: 600, marginBottom: '1rem' }}>Role Capabilities Overview</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <div style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.25rem' }}>ADMIN</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Full system access: vendors, vehicles, contracts, slabs, trips, billing engine runs, invoices, fraud security.</div>
            </div>
            <div style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.25rem' }}>HR</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Operational management: view fleet, execute billing runs, allocate fixed monthly fees, inspect tax invoices, review fraud alerts.</div>
            </div>
            <div style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '0.25rem' }}>EMPLOYEE</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Operational user: view assigned vehicles, log completed trips, and view personal operational history.</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
