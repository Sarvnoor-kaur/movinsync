import React from 'react';

export const StatCard = ({ title, value, icon, color = '#3b82f6', subtitle }) => {
  return (
    <div className="stat-card">
      <div>
        <div className="stat-label">{title}</div>
        <div className="stat-val">{value}</div>
        {subtitle && <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>{subtitle}</div>}
      </div>
      <div className="stat-icon" style={{ background: `${color}20`, color: color }}>
        {icon}
      </div>
    </div>
  );
};
