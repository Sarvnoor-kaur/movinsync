import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from '../components/StatusBadge';
import {
  User as UserIcon,
  Mail,
  ShieldCheck,
  KeyRound,
  Sparkles,
  Lock,
  Activity,
  Award,
  CheckCircle2,
  Clock,
  Laptop,
  Fingerprint,
  Building,
} from 'lucide-react';

export const Profile = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('overview');

  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    return name.slice(0, 2).toUpperCase();
  };

  const getRoleGradient = (role) => {
    switch (role) {
      case 'ADMIN':
        return 'linear-gradient(135deg, #7c3aed, #4f46e5)';
      case 'HR':
        return 'linear-gradient(135deg, #2563eb, #0284c7)';
      case 'VENDOR':
        return 'linear-gradient(135deg, #d97706, #ca8a04)';
      default:
        return 'linear-gradient(135deg, #059669, #10b981)';
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', paddingBottom: '3rem' }}>
      {/* 🌟 Hero Header Banner */}
      <div
        style={{
          borderRadius: '16px',
          background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #1e1b4b 100%)',
          color: '#ffffff',
          padding: '2.5rem 2rem',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.15), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
          position: 'relative',
          overflow: 'hidden',
          marginBottom: '2rem',
        }}
      >
        {/* Subtle Decorative Glow Circles */}
        <div
          style={{
            position: 'absolute',
            top: '-50px',
            right: '-50px',
            width: '240px',
            height: '240px',
            borderRadius: '50%',
            background: 'radial-gradient(circle, rgba(99, 102, 241, 0.25) 0%, rgba(0,0,0,0) 70%)',
            pointerEvents: 'none',
          }}
        />

        <div style={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: '2rem', flexWrap: 'wrap' }}>
          {/* User Avatar Badge */}
          <div style={{ position: 'relative' }}>
            <div
              style={{
                width: 90,
                height: 90,
                borderRadius: '22px',
                background: getRoleGradient(user?.role),
                color: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '2.25rem',
                fontWeight: 800,
                boxShadow: '0 10px 20px rgba(0, 0, 0, 0.3)',
                border: '3px solid rgba(255, 255, 255, 0.2)',
                letterSpacing: '1px',
              }}
            >
              {getInitials(user?.name)}
            </div>
            <div
              style={{
                position: 'absolute',
                bottom: -4,
                right: -4,
                width: 20,
                height: 20,
                borderRadius: '50%',
                backgroundColor: '#10b981',
                border: '3px solid #0f172a',
              }}
              title="Session Active"
            />
          </div>

          {/* User Meta Info */}
          <div style={{ flex: 1, minWidth: 260 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.4rem', flexWrap: 'wrap' }}>
              <h1 style={{ fontSize: '1.85rem', fontWeight: 800, color: '#ffffff', margin: 0, letterSpacing: '-0.02em' }}>
                {user?.name || 'User Account'}
              </h1>
              <span
                style={{
                  padding: '0.25rem 0.75rem',
                  borderRadius: '20px',
                  fontSize: '0.75rem',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  letterSpacing: '0.05em',
                  background: 'rgba(255, 255, 255, 0.15)',
                  backdropFilter: 'blur(10px)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                }}
              >
                {user?.role || 'EMPLOYEE'}
              </span>
            </div>

            <p style={{ fontSize: '0.925rem', color: '#94a3b8', margin: 0, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <Mail size={16} color="#38bdf8" /> {user?.email || 'user@movinsync.com'}
            </p>
          </div>

          {/* Metric Stats Pills */}
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
            <div
              style={{
                background: 'rgba(255, 255, 255, 0.07)',
                backdropFilter: 'blur(12px)',
                border: '1px solid rgba(255, 255, 255, 0.12)',
                padding: '0.85rem 1.25rem',
                borderRadius: '12px',
                textAlign: 'center',
                minWidth: 120,
              }}
            >
              <div style={{ fontSize: '0.725rem', textTransform: 'uppercase', color: '#94a3b8', fontWeight: 600, letterSpacing: '0.05em' }}>
                Account Status
              </div>
              <div style={{ fontSize: '0.95rem', fontWeight: 700, color: '#34d399', marginTop: '0.2rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.3rem' }}>
                <CheckCircle2 size={16} /> Verified Active
              </div>
            </div>

            <div
              style={{
                background: 'rgba(255, 255, 255, 0.07)',
                backdropFilter: 'blur(12px)',
                border: '1px solid rgba(255, 255, 255, 0.12)',
                padding: '0.85rem 1.25rem',
                borderRadius: '12px',
                textAlign: 'center',
                minWidth: 120,
              }}
            >
              <div style={{ fontSize: '0.725rem', textTransform: 'uppercase', color: '#94a3b8', fontWeight: 600, letterSpacing: '0.05em' }}>
                Security Level
              </div>
              <div style={{ fontSize: '0.95rem', fontWeight: 700, color: '#38bdf8', marginTop: '0.2rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.3rem' }}>
                <Lock size={16} /> JWT HMAC-SHA256
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* ⚡ Navigation Tabs */}
      <div
        style={{
          display: 'flex',
          gap: '0.75rem',
          marginBottom: '1.75rem',
          borderBottom: '2px solid #e2e8f0',
          paddingBottom: '0.5rem',
        }}
      >
        <button
          onClick={() => setActiveTab('overview')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.65rem 1.25rem',
            borderRadius: '8px',
            border: 'none',
            fontSize: '0.9rem',
            fontWeight: 600,
            cursor: 'pointer',
            backgroundColor: activeTab === 'overview' ? '#2563eb' : 'transparent',
            color: activeTab === 'overview' ? '#ffffff' : '#64748b',
            transition: 'all 0.2s ease',
          }}
        >
          <UserIcon size={18} /> Profile & Identity
        </button>

        <button
          onClick={() => setActiveTab('permissions')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.65rem 1.25rem',
            borderRadius: '8px',
            border: 'none',
            fontSize: '0.9rem',
            fontWeight: 600,
            cursor: 'pointer',
            backgroundColor: activeTab === 'permissions' ? '#2563eb' : 'transparent',
            color: activeTab === 'permissions' ? '#ffffff' : '#64748b',
            transition: 'all 0.2s ease',
          }}
        >
          <ShieldCheck size={18} /> Role Capabilities
        </button>

        <button
          onClick={() => setActiveTab('security')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '0.65rem 1.25rem',
            borderRadius: '8px',
            border: 'none',
            fontSize: '0.9rem',
            fontWeight: 600,
            cursor: 'pointer',
            backgroundColor: activeTab === 'security' ? '#2563eb' : 'transparent',
            color: activeTab === 'security' ? '#ffffff' : '#64748b',
            transition: 'all 0.2s ease',
          }}
        >
          <KeyRound size={18} /> Security & Session
        </button>
      </div>

      {/* 📄 TAB 1: Profile & Identity Overview */}
      {activeTab === 'overview' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))', gap: '1.5rem' }}>
          {/* Card: Personal Details */}
          <div className="saas-card" style={{ padding: '1.75rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.85rem' }}>
              <div style={{ padding: '8px', borderRadius: '8px', background: '#eff6ff', color: '#2563eb' }}>
                <Sparkles size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>Personal Details</h3>
                <span style={{ fontSize: '0.8rem', color: '#64748b' }}>Account credentials and system parameters</span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                <UserIcon size={20} style={{ marginTop: 2, color: '#64748b' }} />
                <div>
                  <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 600, letterSpacing: '0.04em' }}>Full Name</div>
                  <div style={{ fontSize: '0.975rem', fontWeight: 700, color: '#0f172a', marginTop: 2 }}>{user?.name || 'N/A'}</div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                <Mail size={20} style={{ marginTop: 2, color: '#64748b' }} />
                <div>
                  <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 600, letterSpacing: '0.04em' }}>Email Address</div>
                  <div style={{ fontSize: '0.975rem', fontWeight: 700, color: '#0f172a', marginTop: 2 }}>{user?.email || 'N/A'}</div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                <Building size={20} style={{ marginTop: 2, color: '#64748b' }} />
                <div>
                  <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 600, letterSpacing: '0.04em' }}>Enterprise Domain</div>
                  <div style={{ fontSize: '0.975rem', fontWeight: 700, color: '#0f172a', marginTop: 2 }}>MovInSync Fleet Management Platform</div>
                </div>
              </div>

              <div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem' }}>
                <Fingerprint size={20} style={{ marginTop: 2, color: '#64748b' }} />
                <div>
                  <div style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: '#64748b', fontWeight: 600, letterSpacing: '0.04em' }}>System User ID</div>
                  <div style={{ fontSize: '0.95rem', fontWeight: 600, fontFamily: 'monospace', color: '#2563eb', marginTop: 2 }}>
                    USR-{user?.userId || user?.id || '101'}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Card: Active Session & Metadata */}
          <div className="saas-card" style={{ padding: '1.75rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem', borderBottom: '1px solid #f1f5f9', paddingBottom: '0.85rem' }}>
              <div style={{ padding: '8px', borderRadius: '8px', background: '#f0fdf4', color: '#16a34a' }}>
                <Activity size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>Active Session Overview</h3>
                <span style={{ fontSize: '0.8rem', color: '#64748b' }}>Real-time JWT bearer token context</span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Authentication Mode</div>
                <div style={{ fontSize: '0.925rem', fontWeight: 700, color: '#0f172a', marginTop: '0.2rem' }}>Spring Security Stateless JWT</div>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Correlation ID Tracing</div>
                <div style={{ fontSize: '0.875rem', fontWeight: 600, fontFamily: 'monospace', color: '#0284c7', marginTop: '0.2rem' }}>
                  SLF4J MDC X-Correlation-ID Filter Attached
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.75rem', color: '#64748b', fontWeight: 600, textTransform: 'uppercase' }}>Financial Money Standard</div>
                <div style={{ fontSize: '0.875rem', fontWeight: 600, color: '#059669', marginTop: '0.2rem' }}>
                  Integer Paisa Precision (₹1 = 100 Paisa)
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 📄 TAB 2: Role Capabilities */}
      {activeTab === 'permissions' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Admin Role Box */}
          <div
            className="saas-card"
            style={{
              padding: '1.5rem',
              borderLeft: user?.role === 'ADMIN' ? '6px solid #7c3aed' : '1px solid #e2e8f0',
              backgroundColor: user?.role === 'ADMIN' ? '#faf5ff' : '#ffffff',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '8px', borderRadius: '8px', background: '#7c3aed', color: '#ffffff' }}>
                  <Award size={18} />
                </div>
                <div>
                  <h4 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>ADMIN Role</h4>
                  <span style={{ fontSize: '0.775rem', color: '#64748b' }}>Full Enterprise Governance & Configuration</span>
                </div>
              </div>
              {user?.role === 'ADMIN' && <StatusBadge status="ACTIVE_ROLE" />}
            </div>
            <p style={{ fontSize: '0.875rem', color: '#475569', lineHeight: 1.6 }}>
              Full operational and financial governance permissions. Can manage transport vendors, configure rate contract versions, design tiered pricing slabs, execute monthly billing runs, resolve fraud alerts, and manage user accounts.
            </p>
          </div>

          {/* HR Role Box */}
          <div
            className="saas-card"
            style={{
              padding: '1.5rem',
              borderLeft: user?.role === 'HR' ? '6px solid #2563eb' : '1px solid #e2e8f0',
              backgroundColor: user?.role === 'HR' ? '#eff6ff' : '#ffffff',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '8px', borderRadius: '8px', background: '#2563eb', color: '#ffffff' }}>
                  <ShieldCheck size={18} />
                </div>
                <div>
                  <h4 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>HR / Transport Manager Role</h4>
                  <span style={{ fontSize: '0.775rem', color: '#64748b' }}>Operational Billing & Fleet Oversight</span>
                </div>
              </div>
              {user?.role === 'HR' && <StatusBadge status="ACTIVE_ROLE" />}
            </div>
            <p style={{ fontSize: '0.875rem', color: '#475569', lineHeight: 1.6 }}>
              Operational management permissions. Can view fleet cabs, execute monthly billing engine runs, trigger fixed fee retainer allocations, inspect vendor tax invoices, and review/resolve fraudulent trip anomaly alerts.
            </p>
          </div>

          {/* EMPLOYEE Role Box */}
          <div
            className="saas-card"
            style={{
              padding: '1.5rem',
              borderLeft: user?.role === 'EMPLOYEE' ? '6px solid #10b981' : '1px solid #e2e8f0',
              backgroundColor: user?.role === 'EMPLOYEE' ? '#f0fdf4' : '#ffffff',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '8px', borderRadius: '8px', background: '#10b981', color: '#ffffff' }}>
                  <UserIcon size={18} />
                </div>
                <div>
                  <h4 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: '#0f172a' }}>EMPLOYEE Role</h4>
                  <span style={{ fontSize: '0.775rem', color: '#64748b' }}>Duty Logging & Standard Operational User</span>
                </div>
              </div>
              {user?.role === 'EMPLOYEE' && <StatusBadge status="ACTIVE_ROLE" />}
            </div>
            <p style={{ fontSize: '0.875rem', color: '#475569', lineHeight: 1.6 }}>
              Standard operational user permissions. Can view assigned vehicles, log completed cab duty logs, track trip statuses, and view personal operational history.
            </p>
          </div>
        </div>
      )}

      {/* 📄 TAB 3: Security & Session */}
      {activeTab === 'security' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
          <div className="saas-card" style={{ padding: '1.75rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Lock size={18} color="#2563eb" /> Security & Encryption Standards
            </h3>
            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                <CheckCircle2 size={18} color="#10b981" style={{ marginTop: 2 }} />
                <div>
                  <strong style={{ fontSize: '0.9rem', color: '#0f172a' }}>BCrypt Password Hashing</strong>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', margin: '0.1rem 0 0 0' }}>Passwords are never stored in plaintext. Secure BCrypt hashing with salted iterations is enforced.</p>
                </div>
              </li>

              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                <CheckCircle2 size={18} color="#10b981" style={{ marginTop: 2 }} />
                <div>
                  <strong style={{ fontSize: '0.9rem', color: '#0f172a' }}>Idempotency Protection Layer</strong>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', margin: '0.1rem 0 0 0' }}>POST write endpoints check `Idempotency-Key` headers via SHA-256 request fingerprinting to prevent duplicate billing.</p>
                </div>
              </li>

              <li style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                <CheckCircle2 size={18} color="#10b981" style={{ marginTop: 2 }} />
                <div>
                  <strong style={{ fontSize: '0.9rem', color: '#0f172a' }}>Automated Fraud Engine</strong>
                  <p style={{ fontSize: '0.8rem', color: '#64748b', margin: '0.1rem 0 0 0' }}>Every logged trip is automatically evaluated against speed (&gt;150 km/h) and distance (&gt;1000 km) rules.</p>
                </div>
              </li>
            </ul>
          </div>

          <div className="saas-card" style={{ padding: '1.75rem' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: '#0f172a', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Laptop size={18} color="#0284c7" /> Device & Session Parameters
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', fontSize: '0.875rem' }}>
              <div className="flex-between" style={{ paddingBottom: '0.5rem', borderBottom: '1px solid #f1f5f9' }}>
                <span style={{ color: '#64748b' }}>JWT Signature Algorithm:</span>
                <strong style={{ color: '#0f172a' }}>HMAC-SHA256</strong>
              </div>

              <div className="flex-between" style={{ paddingBottom: '0.5rem', borderBottom: '1px solid #f1f5f9' }}>
                <span style={{ color: '#64748b' }}>Session Expiration Window:</span>
                <strong style={{ color: '#0f172a' }}>24 Hours Rolling</strong>
              </div>

              <div className="flex-between" style={{ paddingBottom: '0.5rem', borderBottom: '1px solid #f1f5f9' }}>
                <span style={{ color: '#64748b' }}>Caching High Availability:</span>
                <strong style={{ color: '#10b981' }}>Redis + MySQL Fallback</strong>
              </div>

              <div className="flex-between">
                <span style={{ color: '#64748b' }}>MDC Log Filter:</span>
                <strong style={{ color: '#2563eb' }}>CorrelationIdFilter Active</strong>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
