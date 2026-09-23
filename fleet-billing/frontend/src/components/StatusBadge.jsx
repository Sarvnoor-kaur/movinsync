import React from 'react';

export const StatusBadge = ({ status }) => {
  if (!status) return <span className="badge badge-info">N/A</span>;

  const s = String(status).toUpperCase();

  let badgeClass = 'badge-info';
  if (['COMPLETED', 'RESOLVED', 'ACTIVE', 'PAID'].includes(s)) badgeClass = 'badge-success';
  if (['PENDING', 'OPEN', 'IN_PROGRESS', 'DRAFT'].includes(s)) badgeClass = 'badge-warning';
  if (['FAILED', 'FLAGGED', 'CANCELLED', 'SUSPECTED_OVERLAP', 'SUSPECTED_KM_SPIKE', 'MISSING_CONTRACT', 'DISMISSED'].includes(s)) badgeClass = 'badge-danger';
  if (['HIGH', 'CRITICAL'].includes(s)) badgeClass = 'badge-danger';
  if (['MEDIUM'].includes(s)) badgeClass = 'badge-warning';
  if (['LOW'].includes(s)) badgeClass = 'badge-info';

  return <span className={`badge ${badgeClass}`}>{s.replace(/_/g, ' ')}</span>;
};
