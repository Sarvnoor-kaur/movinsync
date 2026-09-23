import React, { createContext, useContext, useState, useEffect } from 'react';
import { getToken, getUser, setAuth, clearAuth } from '../utils/auth';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getUser());
  const [token, setToken] = useState(getToken());
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token && !user) {
      authApi.me()
        .then((res) => {
          setUser(res.data);
        })
        .catch(() => {
          logout();
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (credentials) => {
    // credentials: { email, password }
    const res = await authApi.login(credentials);
    const data = res.data; // AuthResponse: { accessToken, userId, name, email, role }
    const jwtToken = data.accessToken || data.token;
    setAuth(jwtToken, data);
    setToken(jwtToken);
    setUser(data);
    return data;
  };

  const register = async (reqData) => {
    // reqData: { name, email, password, role }
    await authApi.register(reqData);
    // Auto login after registration to get JWT token
    return await login({ email: reqData.email, password: reqData.password });
  };

  const logout = () => {
    clearAuth();
    setToken(null);
    setUser(null);
  };

  const hasRole = (...roles) => {
    if (!user || !user.role) return false;
    const userRoleStr = String(user.role).replace('ROLE_', '');
    return roles.some((r) => userRoleStr === r || userRoleStr === `ROLE_${r}`);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
};
