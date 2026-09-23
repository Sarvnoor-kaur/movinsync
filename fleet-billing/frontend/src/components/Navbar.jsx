import React from 'react';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, logout } = useAuth();

  return (
    <header className="navbar">
      <div className="navbar-title">
        <span>⚡ Rental Fleet Billing</span>
      </div>
      {user && (
        <div className="navbar-user">
          <div className="user-badge">
            <span style={{ fontWeight: 600 }}>{user.name || user.email}</span>
            {user.role && (
              <span className="role-pill">
                {String(user.role).replace('ROLE_', '')}
              </span>
            )}
          </div>
          <button className="btn btn-secondary btn-sm" onClick={logout}>
            Logout
          </button>
        </div>
      )}
    </header>
  );
};
