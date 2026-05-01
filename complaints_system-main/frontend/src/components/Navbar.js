/**
 * components/Navbar.js
 */

import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => { logout(); navigate('/login'); };

  const isActive = (path) => location.pathname === path
    ? { color: 'var(--primary)', fontWeight: '700' }
    : { color: 'var(--muted)', fontWeight: '500' };

  return (
    <nav className="navbar">
      <Link to={user?.role === 'admin' ? '/admin' : '/dashboard'} className="navbar-brand">
        Complaint Portal
      </Link>

      <div className="navbar-links">
        {user?.role === 'student' && (
          <>
            <Link to="/dashboard" style={{ ...isActive('/dashboard'), textDecoration: 'none', fontSize: '.9rem' }}>
              My Complaints
            </Link>
            <Link to="/submit" style={{ ...isActive('/submit'), textDecoration: 'none', fontSize: '.9rem' }}>
              + Submit
            </Link>
          </>
        )}
        {user?.role === 'admin' && (
          <Link to="/admin" style={{ ...isActive('/admin'), textDecoration: 'none', fontSize: '.9rem' }}>
            Admin Panel
          </Link>
        )}

        <div style={{ display: 'flex', alignItems: 'center', gap: '.75rem', marginLeft: '.5rem', paddingLeft: '.75rem', borderLeft: '1px solid var(--border)' }}>
          <span style={{ fontSize: '.85rem', color: 'var(--muted)' }}>
            👤 {user?.name}
            <span style={{ fontSize: '.75rem', background: user?.role === 'admin' ? '#eff6ff' : '#f0fdf4', color: user?.role === 'admin' ? '#1d4ed8' : '#15803d', padding: '.1em .5em', borderRadius: '99px', marginLeft: '.4rem', fontWeight: '600' }}>
              {user?.role}
            </span>
          </span>
          <button onClick={handleLogout} className="btn btn-outline btn-sm">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}
