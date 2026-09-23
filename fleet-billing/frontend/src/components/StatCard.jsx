import React from 'react';

export const StatCard = ({ title, value, icon: Icon, color = '#2563eb', subtitle, trend }) => {
  return (
    <div className="stat-card">
      <div className="stat-header">
        <span className="stat-title">{title}</span>
        {Icon && (
          <div
            className="stat-icon"
            style={{ backgroundColor: `${color}15`, color: color }}
          >
            <Icon size={20} />
          </div>
        )}
      </div>
      <div className="stat-value">{value}</div>
      {(subtitle || trend) && (
        <div className="stat-footer">
          {trend && (
            <span style={{ color: trend.startsWith('+') ? 'var(--success)' : 'var(--danger)', fontWeight: 600 }}>
              {trend}
            </span>
          )}
          {subtitle && <span>{subtitle}</span>}
        </div>
      )}
    </div>
  );
};
