import React from 'react';
import { NavLink } from 'react-router-dom';

export const Sidebar = () => {
  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/vendors', label: 'Vendors', icon: '🏢' },
    { path: '/vehicles', label: 'Vehicles', icon: '🚘' },
    { path: '/contracts', label: 'Contracts', icon: '📄' },
    { path: '/trips', label: 'Trips', icon: '🗺️' },
    { path: '/billing-runs', label: 'Billing Runs', icon: '💰' },
    { path: '/invoices', label: 'Invoices', icon: '🧾' },
    { path: '/fraud-alerts', label: 'Fraud Alerts', icon: '🚨' },
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <span>🚗 FleetSync</span>
      </div>
      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => (isActive ? 'nav-item active' : 'nav-item')}
          >
            <span className="nav-icon">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
};
