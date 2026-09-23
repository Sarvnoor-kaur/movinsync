import React from 'react';

export const StatusBadge = ({ status }) => {
  if (!status) return null;
  const s = String(status).toUpperCase();

  let variant = 'neutral';
  if (['COMPLETED', 'ACTIVE', 'RESOLVED', 'ADMIN'].includes(s)) {
    variant = 'success';
  } else if (['STARTED', 'PROCESSING', 'HR', 'HIGH', 'MEDIUM', 'REVIEWED'].includes(s)) {
    variant = 'warning';
  } else if (['FAILED', 'CANCELLED', 'CRITICAL', 'DISMISSED', 'SUSPICIOUS_TRIP', 'IMPOSSIBLE_DISTANCE'].includes(s)) {
    variant = 'danger';
  } else if (['EMPLOYEE', 'OPEN', 'PER_KM', 'PER_TRIP', 'FIXED_MONTHLY'].includes(s)) {
    variant = 'info';
  }

  return (
    <span className={`status-badge ${variant}`}>
      <span className="dot" />
      {s.replace(/_/g, ' ')}
    </span>
  );
};
