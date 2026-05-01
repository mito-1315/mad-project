/**
 * App.js — Root component with routing
 *
 * React Router 6 handles navigation between pages.
 * Protected routes redirect to /login if not authenticated.
 */

import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

import LoginPage    from './pages/LoginPage';
import SignupPage   from './pages/SignupPage';
import StudentDash  from './pages/StudentDashboard';
import AdminPanel   from './pages/AdminPanel';
import SubmitForm   from './pages/SubmitComplaint';
import './styles.css';

// ── Protected Route: redirects to /login if not logged in ─
function ProtectedRoute({ children, adminOnly = false }) {
  const { user, loading } = useAuth();

  if (loading) return <div className="loading-spinner">Loading...</div>;

  if (!user) return <Navigate to="/login" replace />;

  if (adminOnly && user.role !== 'admin') {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

function AppRoutes() {
  const { user } = useAuth();

  return (
    <Routes>
      {/* Public routes */}
      <Route path="/login"  element={user ? <Navigate to="/dashboard" /> : <LoginPage />} />
      <Route path="/signup" element={user ? <Navigate to="/dashboard" /> : <SignupPage />} />

      {/* Student routes */}
      <Route path="/dashboard" element={
        <ProtectedRoute>
          {user?.role === 'admin' ? <Navigate to="/admin" /> : <StudentDash />}
        </ProtectedRoute>
      } />
      <Route path="/submit" element={
        <ProtectedRoute><SubmitForm /></ProtectedRoute>
      } />

      {/* Admin routes */}
      <Route path="/admin" element={
        <ProtectedRoute adminOnly><AdminPanel /></ProtectedRoute>
      } />

      {/* Default redirect */}
      <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
