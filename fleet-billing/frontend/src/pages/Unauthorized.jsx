import React from 'react';
import { Link } from 'react-router-dom';

export const Unauthorized = () => {
  return (
    <div className="flex-center" style={{ minHeight: '80vh', flexDirection: 'column', gap: '1rem', textAlign: 'center' }}>
      <h1 style={{ fontSize: '3rem', color: 'var(--accent-rose)' }}>403</h1>
      <h2 style={{ fontSize: '1.5rem' }}>Access Denied</h2>
      <p style={{ color: 'var(--text-secondary)', maxWidth: '400px' }}>
        You do not have the required permissions to access this page. Please contact your system administrator.
      </p>
      <Link to="/dashboard" className="btn btn-primary" style={{ marginTop: '1rem' }}>
        Back to Dashboard
      </Link>
    </div>
  );
};
