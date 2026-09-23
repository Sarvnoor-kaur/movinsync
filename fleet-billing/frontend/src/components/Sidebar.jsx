import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Building2,
  Car,
  FileText,
  MapPin,
  Receipt,
  FileCheck2,
  ShieldAlert,
  User,
  LogOut,
  Layers,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar = ({ collapsed, onToggleCollapse, mobileOpen, onMobileClose }) => {
  const { user, logout } = useAuth();

  const navGroups = [
    {
      group: 'OPERATIONS',
      items: [
        { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
        { path: '/vendors', label: 'Vendors', icon: Building2 },
        { path: '/vehicles', label: 'Vehicles', icon: Car },
        { path: '/trips', label: 'Trips', icon: MapPin },
      ],
    },
    {
      group: 'FINANCE',
      items: [
        { path: '/contracts', label: 'Contracts', icon: FileText },
        { path: '/billing-runs', label: 'Billing Engine', icon: Receipt },
        { path: '/invoices', label: 'Tax Invoices', icon: FileCheck2 },
      ],
    },
    {
      group: 'MONITORING',
      items: [{ path: '/fraud-alerts', label: 'Fraud Alerts', icon: ShieldAlert }],
    },
    {
      group: 'ACCOUNT',
      items: [{ path: '/profile', label: 'User Profile', icon: User }],
    },
  ];

  const sidebarStyle = {
    position: 'fixed',
    top: 0,
    bottom: 0,
    left: 0,
    width: collapsed ? 'var(--sidebar-collapsed)' : 'var(--sidebar-width)',
    backgroundColor: 'var(--bg-sidebar)',
    borderRight: '1px solid #1e293b',
    display: 'flex',
    flexDirection: 'column',
    zIndex: 40,
    transition: 'width 0.2s ease, transform 0.2s ease',
  };

  return (
    <>
      {/* Mobile Backdrop */}
      {mobileOpen && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(15, 23, 42, 0.7)',
            zIndex: 35,
          }}
          onClick={onMobileClose}
        />
      )}

      <aside style={sidebarStyle} className={`sidebar ${mobileOpen ? 'mobile-open' : ''}`}>
        {/* Brand Header */}
        <div
          style={{
            height: 'var(--topbar-height)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'space-between',
            padding: collapsed ? '0' : '0 1.25rem',
            borderBottom: '1px solid #1e293b',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', overflow: 'hidden' }}>
            <div
              style={{
                width: 34,
                height: 34,
                borderRadius: '8px',
                background: 'linear-gradient(135deg, #2563eb, #3b82f6)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff',
                flexShrink: 0,
              }}
            >
              <Layers size={20} />
            </div>
            {!collapsed && (
              <div style={{ whiteSpace: 'nowrap' }}>
                <div style={{ fontSize: '1.05rem', fontWeight: 700, color: '#ffffff', lineHeight: 1.1 }}>
                  FleetFlow
                </div>
                <div style={{ fontSize: '0.7rem', color: '#94a3b8', fontWeight: 500 }}>
                  Billing & Operations
                </div>
              </div>
            )}
          </div>

          {!collapsed && (
            <button
              onClick={onToggleCollapse}
              style={{
                background: 'none',
                border: 'none',
                color: '#94a3b8',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
              }}
            >
              <ChevronLeft size={18} />
            </button>
          )}
        </div>

        {/* Expand Toggle when collapsed */}
        {collapsed && (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '0.5rem 0' }}>
            <button
              onClick={onToggleCollapse}
              style={{
                background: 'none',
                border: 'none',
                color: '#94a3b8',
                cursor: 'pointer',
                padding: '0.35rem',
              }}
            >
              <ChevronRight size={18} />
            </button>
          </div>
        )}

        {/* Navigation Section */}
        <div style={{ flex: 1, overflowY: 'auto', padding: '1rem 0.75rem' }}>
          {navGroups.map((group) => (
            <div key={group.group} style={{ marginBottom: '1.25rem' }}>
              {!collapsed && (
                <div
                  style={{
                    fontSize: '0.675rem',
                    fontWeight: 700,
                    color: '#64748b',
                    letterSpacing: '0.08em',
                    padding: '0 0.5rem 0.5rem 0.5rem',
                  }}
                >
                  {group.group}
                </div>
              )}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                {group.items.map((item) => {
                  const Icon = item.icon;
                  return (
                    <NavLink
                      key={item.path}
                      to={item.path}
                      onClick={onMobileClose}
                      style={({ isActive }) => ({
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.75rem',
                        padding: collapsed ? '0.65rem 0' : '0.6rem 0.75rem',
                        justifyContent: collapsed ? 'center' : 'flex-start',
                        borderRadius: '6px',
                        color: isActive ? '#ffffff' : '#94a3b8',
                        backgroundColor: isActive ? 'var(--bg-sidebar-active)' : 'transparent',
                        fontWeight: isActive ? 600 : 500,
                        fontSize: '0.875rem',
                        textDecoration: 'none',
                        transition: 'background-color 0.15s ease, color 0.15s ease',
                      })}
                      title={collapsed ? item.label : undefined}
                    >
                      <Icon size={18} />
                      {!collapsed && <span>{item.label}</span>}
                    </NavLink>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        {/* User Footer / Logout */}
        <div
          style={{
            padding: '0.85rem 0.75rem',
            borderTop: '1px solid #1e293b',
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'space-between',
          }}
        >
          {!collapsed && (
            <div style={{ overflow: 'hidden', paddingRight: '0.5rem' }}>
              <div style={{ fontSize: '0.825rem', fontWeight: 600, color: '#ffffff', truncate: 'true' }}>
                {user?.name || 'User Account'}
              </div>
              <div style={{ fontSize: '0.725rem', color: '#64748b' }}>{user?.role || 'EMPLOYEE'}</div>
            </div>
          )}
          <button
            onClick={logout}
            style={{
              background: 'none',
              border: 'none',
              color: '#f43f5e',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.35rem',
              padding: '0.35rem',
            }}
            title="Log Out"
          >
            <LogOut size={18} />
          </button>
        </div>
      </aside>
    </>
  );
};
