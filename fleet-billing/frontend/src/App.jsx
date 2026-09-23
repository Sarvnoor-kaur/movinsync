import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AppLayout } from './components/AppLayout';

import { LandingPage } from './pages/LandingPage';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { Vendors } from './pages/Vendors';
import { Vehicles } from './pages/Vehicles';
import { Contracts } from './pages/Contracts';
import { Trips } from './pages/Trips';
import { BillingRuns } from './pages/BillingRuns';
import { Invoices } from './pages/Invoices';
import { FraudAlerts } from './pages/FraudAlerts';
import { Profile } from './pages/Profile';
import { Unauthorized } from './pages/Unauthorized';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Landing Page */}
          <Route path="/" element={<LandingPage />} />

          {/* Authentication Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Enterprise Routes wrapped in AppLayout */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/vendors" element={<Vendors />} />
              <Route path="/vehicles" element={<Vehicles />} />
              <Route path="/contracts" element={<Contracts />} />
              <Route path="/trips" element={<Trips />} />
              <Route path="/billing-runs" element={<BillingRuns />} />
              <Route path="/billing" element={<Navigate to="/billing-runs" replace />} />
              <Route path="/invoices" element={<Invoices />} />
              <Route path="/fraud-alerts" element={<FraudAlerts />} />
              <Route path="/fraud" element={<Navigate to="/fraud-alerts" replace />} />
              <Route path="/profile" element={<Profile />} />
              <Route path="/unauthorized" element={<Unauthorized />} />
            </Route>
          </Route>

          {/* Fallback Route */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};
