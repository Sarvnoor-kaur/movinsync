import React from 'react';

export const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
      <button
        className="btn btn-secondary btn-sm"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        ← Previous
      </button>
      <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
        Page {page + 1} of {totalPages}
      </span>
      <button
        className="btn btn-secondary btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Next →
      </button>
    </div>
  );
};
