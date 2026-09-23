import React from 'react';

export const NotificationBanner = ({ type = 'error', message, onClose }) => {
  if (!message) return null;

  const isErr = type === 'error';
  const bgColor = isErr ? 'rgba(244, 63, 94, 0.15)' : 'rgba(16, 185, 129, 0.15)';
  const borderColor = isErr ? 'rgba(244, 63, 94, 0.4)' : 'rgba(16, 185, 129, 0.4)';
  const textColor = isErr ? '#f87171' : '#34d399';

  return (
    <div
      style={{
        background: bgColor,
        border: `1px solid ${borderColor}`,
        color: textColor,
        padding: '0.75rem 1rem',
        borderRadius: 'var(--radius-md)',
        marginBottom: '1rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontSize: '0.9rem',
      }}
    >
      <span>{message}</span>
      {onClose && (
        <button
          onClick={onClose}
          style={{ background: 'none', border: 'none', color: textColor, cursor: 'pointer', fontSize: '1rem' }}
        >
          ✕
        </button>
      )}
    </div>
  );
};
