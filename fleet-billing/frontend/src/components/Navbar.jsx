import React, { useState } from 'react';
import { Search, Bell, Menu, User, LogOut, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from './StatusBadge';
import { Link, useLocation } from 'react-router-dom';

export const Navbar = ({ onMobileMenuOpen }) => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  // Derive human readable page title from route path
  const getPageTitle = (path) => {
    switch (path) {
      case '/dashboard': return 'Fleet Dashboard';
      case '/vendors': return 'Vendor Management';
      case '/vehicles': return 'Vehicle Fleet';
      case '/contracts': return 'Rate Contracts & Slabs';
      case '/trips': return 'Trip Records';
      case '/billing-runs': return 'Billing Engine';
      case '/invoices': return 'Tax Invoices';
      case '/fraud-alerts': return 'Fraud & Security Alerts';
      case '/profile': return 'User Profile';
      default: return 'Overview';
    }
  };

  return (
    <header className="topbar">
      <div className="topbar-left">
        <button
          onClick={onMobileMenuOpen}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--text-main)',
            cursor: 'pointer',
            padding: '0.35rem',
            display: 'flex',
            alignItems: 'center',
          }}
          className="lg-hidden"
          aria-label="Toggle menu"
        >
          <Menu size={22} />
        </button>

        <div>
          <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-main)', lineHeight: 1.2 }}>
            {getPageTitle(location.pathname)}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
            Enterprise Operations Portal
          </div>
        </div>
      </div>

      <div className="topbar-right">
        {/* Global Search Mockup */}
        <div
          style={{
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
            width: 240,
          }}
        >
          <Search
            size={16}
            style={{ position: 'absolute', left: 10, color: 'var(--text-muted)' }}
          />
          <input
            type="text"
            placeholder="Search trips, vehicles..."
            className="form-control"
            style={{
              paddingLeft: '2.1rem',
              height: '36px',
              fontSize: '0.8rem',
              backgroundColor: '#f8fafc',
            }}
          />
        </div>

        {/* Notification Bell */}
        <button
          style={{
            background: 'none',
            border: '1px solid var(--border-color)',
            borderRadius: 'var(--radius-sm)',
            width: 36,
            height: 36,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--text-muted)',
            cursor: 'pointer',
            position: 'relative',
          }}
          title="Notifications"
        >
          <Bell size={18} />
          <span
            style={{
              position: 'absolute',
              top: 6,
              right: 6,
              width: 8,
              height: 8,
              backgroundColor: '#ef4444',
              borderRadius: '50%',
            }}
          />
        </button>

        {/* User Chip Dropdown */}
        <div style={{ position: 'relative' }}>
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            style={{
              background: 'none',
              border: 'none',
              display: 'flex',
              alignItems: 'center',
              gap: '0.65rem',
              cursor: 'pointer',
              padding: '0.25rem',
            }}
          >
            <div
              style={{
                width: 34,
                height: 34,
                borderRadius: '50%',
                backgroundColor: 'var(--primary-light)',
                color: 'var(--primary)',
                fontWeight: 700,
                fontSize: '0.875rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '1px solid var(--border-color)',
              }}
            >
              {user?.name ? user.name.charAt(0).toUpperCase() : 'U'}
            </div>
            <div style={{ textAlign: 'left', display: 'none' }} className="sm-block">
              <div style={{ fontSize: '0.825rem', fontWeight: 600, color: 'var(--text-main)', lineHeight: 1.1 }}>
                {user?.name || 'Sarvnoor Kaur'}
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                {user?.email || 'sarvn@movinsync.com'}
              </div>
            </div>
            <StatusBadge status={user?.role || 'ADMIN'} />
          </button>

          {dropdownOpen && (
            <div
              style={{
                position: 'absolute',
                top: '110%',
                right: 0,
                width: 220,
                backgroundColor: '#ffffff',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                boxShadow: 'var(--shadow-lg)',
                padding: '0.5rem',
                zIndex: 50,
              }}
              onClick={() => setDropdownOpen(false)}
            >
              <div style={{ padding: '0.5rem', borderBottom: '1px solid var(--border-color)', marginBottom: '0.35rem' }}>
                <div style={{ fontSize: '0.85rem', fontWeight: 600 }}>{user?.name}</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user?.email}</div>
              </div>
              <Link
                to="/profile"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.5rem 0.65rem',
                  fontSize: '0.85rem',
                  color: 'var(--text-main)',
                  borderRadius: '4px',
                  textDecoration: 'none',
                }}
              >
                <User size={16} /> My Profile
              </Link>
              <button
                onClick={logout}
                style={{
                  width: '100%',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  padding: '0.5rem 0.65rem',
                  fontSize: '0.85rem',
                  color: '#dc2626',
                  borderRadius: '4px',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  textAlign: 'left',
                }}
              >
                <LogOut size={16} /> Log Out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
