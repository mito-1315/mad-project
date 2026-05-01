/**
 * pages/StudentDashboard.js
 *
 * Shows the logged-in student's own complaints.
 * Each row shows title, category, severity (from ML), status.
 */

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { SeverityBadge, StatusBadge } from '../components/Badges';
import { useAuth } from '../context/AuthContext';

export default function StudentDashboard() {
  const { user } = useAuth();
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState('');
  const [filter, setFilter]         = useState('all');   // status filter

  useEffect(() => {
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    try {
      const { data } = await axios.get('/api/complaints');
      setComplaints(data.complaints);
    } catch (err) {
      setError('Failed to load complaints.');
    } finally {
      setLoading(false);
    }
  };

  const filtered = filter === 'all'
    ? complaints
    : complaints.filter(c => c.status === filter);

  // ── Stats from complaints array ───────────────────────
  const stats = {
    total:      complaints.length,
    pending:    complaints.filter(c => c.status === 'pending').length,
    resolved:   complaints.filter(c => c.status === 'resolved').length,
    highSev:    complaints.filter(c => c.severity === 'High').length,
  };

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="main-content">

        {/* Welcome header */}
        <div className="page-header">
          <div>
            <h1>👋 Hello, {user?.name}</h1>
            <p style={{ color: 'var(--muted)', fontSize: '.9rem' }}>Track your submitted complaints below</p>
          </div>
          <Link to="/submit" className="btn btn-primary">+ New Complaint</Link>
        </div>

        {/* Stats cards */}
        <div className="stats-grid">
          {[
            { label: 'Total Submitted', value: stats.total,   icon: '📋' },
            { label: 'Pending',         value: stats.pending,  icon: '⏳' },
            { label: 'Resolved',        value: stats.resolved, icon: '✅' },
            { label: 'High Severity',   value: stats.highSev,  icon: '🔴' },
          ].map(({ label, value, icon }) => (
            <div className="stat-card" key={label}>
              <div className="stat-label">{icon} {label}</div>
              <div className="stat-value">{value}</div>
            </div>
          ))}
        </div>

        {/* Complaints list */}
        <div className="card">
          <div className="card-header">
            <h2>My Complaints</h2>
            {/* Status filter */}
            <div style={{ display: 'flex', gap: '.5rem' }}>
              {['all', 'pending', 'in-progress', 'resolved', 'rejected'].map(s => (
                <button
                  key={s}
                  onClick={() => setFilter(s)}
                  className={`btn btn-sm ${filter === s ? 'btn-primary' : 'btn-outline'}`}
                  style={{ textTransform: 'capitalize' }}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>

          {error   && <div className="alert alert-error">{error}</div>}

          {loading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--muted)' }}>Loading...</div>
          ) : filtered.length === 0 ? (
            <div className="empty-state">
              <div className="icon">📭</div>
              <h3>{filter === 'all' ? 'No complaints yet' : `No ${filter} complaints`}</h3>
              <p style={{ marginBottom: '1rem' }}>
                {filter === 'all' ? 'Submit your first complaint to get started.' : 'Try a different filter.'}
              </p>
              {filter === 'all' && (
                <Link to="/submit" className="btn btn-primary">Submit a Complaint</Link>
              )}
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Category</th>
                    <th>AI Severity</th>
                    <th>Status</th>
                    <th>Submitted</th>
                    <th>Admin Notes</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map(c => (
                    <tr key={c._id}>
                      <td style={{ fontWeight: '600', maxWidth: '220px' }}>
                        <span title={c.title} style={{ display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {c.title}
                        </span>
                        <span style={{ fontSize: '.75rem', color: 'var(--muted)', display: 'block', marginTop: '.15rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                          {c.description.substring(0, 60)}...
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: '.82rem', background: '#f1f5f9', padding: '.2em .6em', borderRadius: '6px' }}>
                          {c.category}
                        </span>
                      </td>
                      <td>
                        <SeverityBadge severity={c.severity} />
                        {c.severityConfidence && (
                          <span style={{ display: 'block', fontSize: '.72rem', color: 'var(--muted)', marginTop: '.15rem' }}>
                            {(c.severityConfidence * 100).toFixed(0)}% confidence
                          </span>
                        )}
                      </td>
                      <td><StatusBadge status={c.status} /></td>
                      <td style={{ fontSize: '.82rem', color: 'var(--muted)', whiteSpace: 'nowrap' }}>
                        {new Date(c.createdAt).toLocaleDateString()}
                      </td>
                      <td style={{ fontSize: '.82rem', color: 'var(--muted)', fontStyle: c.adminNotes ? 'normal' : 'italic' }}>
                        {c.adminNotes || '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
