/**
 * pages/AdminPanel.js
 *
 * Admin can:
 *  - See all complaints with stats
 *  - Filter by severity, status, category
 *  - Update status and add notes inline
 *  - Delete complaints
 */

import React, { useEffect, useState, useCallback } from 'react';
import axios from 'axios';
import Navbar from '../components/Navbar';
import { SeverityBadge, StatusBadge } from '../components/Badges';

const STATUS_OPTIONS  = ['pending', 'in-progress', 'resolved', 'rejected'];
const SEVERITY_OPTIONS = ['All', 'High', 'Medium', 'Low', 'Unclassified'];
const CATEGORY_OPTIONS = [
  'All', 'IT Support', 'Hostels', 'Academics', 'Fees / Finance',
  'Maintenance', 'Transport', 'Security / Discipline', 'Administration', 'Other'
];

export default function AdminPanel() {
  const [complaints, setComplaints] = useState([]);
  const [stats,      setStats]      = useState(null);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState('');

  // Filters
  const [sevFilter, setSevFilter] = useState('All');
  const [catFilter, setCatFilter] = useState('All');
  const [staFilter, setStaFilter] = useState('All');

  // Edit state: { [complaintId]: { status, adminNotes } }
  const [edits,   setEdits]   = useState({});
  const [saving,  setSaving]  = useState({});
  const [saved,   setSaved]   = useState({});

  const fetchAll = useCallback(async () => {
    try {
      const params = {};
      if (sevFilter !== 'All') params.severity = sevFilter;
      if (catFilter !== 'All') params.category = catFilter;
      if (staFilter !== 'All') params.status   = staFilter;

      const [compRes, statsRes] = await Promise.all([
        axios.get('/api/complaints', { params }),
        axios.get('/api/complaints/stats')
      ]);
      setComplaints(compRes.data.complaints);
      setStats(statsRes.data);
    } catch (err) {
      setError('Failed to load data.');
    } finally {
      setLoading(false);
    }
  }, [sevFilter, catFilter, staFilter]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  // Update a single field in the local edit state
  const handleEditChange = (id, field, value) => {
    setEdits(prev => ({ ...prev, [id]: { ...prev[id], [field]: value } }));
  };

  // Get current value for a complaint field (local edit overrides DB)
  const getVal = (complaint, field) =>
    edits[complaint._id]?.[field] ?? complaint[field];

  // Save changes for one complaint
  const handleSave = async (id) => {
    const changes = edits[id];
    if (!changes) return;
    setSaving(prev => ({ ...prev, [id]: true }));
    try {
      const { data } = await axios.put(`/api/complaints/${id}`, changes);
      // Update complaints list with fresh data from server
      setComplaints(prev => prev.map(c => c._id === id ? data.complaint : c));
      setEdits(prev => { const n = { ...prev }; delete n[id]; return n; });
      setSaved(prev => ({ ...prev, [id]: true }));
      setTimeout(() => setSaved(prev => ({ ...prev, [id]: false })), 2000);
    } catch (err) {
      alert('Failed to save changes.');
    } finally {
      setSaving(prev => ({ ...prev, [id]: false }));
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this complaint permanently?')) return;
    try {
      await axios.delete(`/api/complaints/${id}`);
      setComplaints(prev => prev.filter(c => c._id !== id));
    } catch {
      alert('Delete failed.');
    }
  };

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="main-content">

        <div className="page-header">
          <div>
            <h1>🛠️ Admin Panel</h1>
            <p style={{ color: 'var(--muted)', fontSize: '.9rem' }}>Manage and triage all submitted complaints</p>
          </div>
        </div>

        {/* Stats cards */}
        {stats && (
          <div className="stats-grid" style={{ marginBottom: '1.5rem' }}>
            {[
              { label: 'Total',       value: stats.total,                   icon: '📋', color: 'var(--text)' },
              { label: 'Pending',     value: stats.byStatus?.pending || 0,  icon: '⏳', color: '#1d4ed8' },
              { label: 'High Severity', value: stats.bySeverity?.High || 0, icon: '🔴', color: '#c81e1e' },
              { label: 'Resolved',    value: stats.byStatus?.resolved || 0, icon: '✅', color: '#057a55' },
            ].map(({ label, value, icon, color }) => (
              <div className="stat-card" key={label}>
                <div className="stat-label">{icon} {label}</div>
                <div className="stat-value" style={{ color }}>{value}</div>
              </div>
            ))}
          </div>
        )}

        {/* Severity distribution bar */}
        {stats && (
          <div className="card" style={{ marginBottom: '1.5rem' }}>
            <div className="card-header"><h2>Severity Distribution</h2></div>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              {[
                { label: 'High',   count: stats.bySeverity?.High   || 0, cls: 'badge-high' },
                { label: 'Medium', count: stats.bySeverity?.Medium || 0, cls: 'badge-medium' },
                { label: 'Low',    count: stats.bySeverity?.Low    || 0, cls: 'badge-low' },
              ].map(({ label, count, cls }) => {
                const pct = stats.total ? Math.round((count / stats.total) * 100) : 0;
                return (
                  <div key={label} style={{ flex: 1, minWidth: '120px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '.3rem', fontSize: '.85rem' }}>
                      <span className={`badge ${cls}`}>{label}</span>
                      <span style={{ fontWeight: '700', fontFamily: 'DM Mono, monospace' }}>{count} ({pct}%)</span>
                    </div>
                    <div style={{ height: '8px', background: '#e2e8f0', borderRadius: '99px', overflow: 'hidden' }}>
                      <div style={{ height: '100%', width: `${pct}%`, background: label === 'High' ? '#ef4444' : label === 'Medium' ? '#f59e0b' : '#10b981', borderRadius: '99px', transition: 'width .5s' }} />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            {[
              { label: 'Severity', value: sevFilter, setter: setSevFilter, opts: SEVERITY_OPTIONS },
              { label: 'Category', value: catFilter, setter: setCatFilter, opts: CATEGORY_OPTIONS },
              { label: 'Status',   value: staFilter, setter: setStaFilter, opts: ['All', ...STATUS_OPTIONS] },
            ].map(({ label, value, setter, opts }) => (
              <div key={label} style={{ flex: 1, minWidth: '160px' }}>
                <label style={{ fontSize: '.8rem', fontWeight: '600', color: 'var(--muted)', display: 'block', marginBottom: '.3rem' }}>
                  Filter by {label}
                </label>
                <select className="form-control" value={value} onChange={e => setter(e.target.value)} style={{ fontSize: '.85rem' }}>
                  {opts.map(o => <option key={o} value={o}>{o}</option>)}
                </select>
              </div>
            ))}
          </div>
        </div>

        {/* Complaints table */}
        <div className="card">
          <div className="card-header">
            <h2>All Complaints ({complaints.length})</h2>
          </div>

          {error   && <div className="alert alert-error">{error}</div>}
          {loading ? (
            <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--muted)' }}>Loading...</div>
          ) : complaints.length === 0 ? (
            <div className="empty-state">
              <div className="icon">🔍</div>
              <h3>No complaints match your filters</h3>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Complaint</th>
                    <th>Submitted by</th>
                    <th>Category</th>
                    <th>AI Severity</th>
                    <th style={{ minWidth: '140px' }}>Status</th>
                    <th style={{ minWidth: '200px' }}>Admin Notes</th>
                    <th>Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {complaints.map(c => (
                    <tr key={c._id}>
                      <td style={{ maxWidth: '200px' }}>
                        <span style={{ fontWeight: '600', display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={c.title}>
                          {c.title}
                        </span>
                        <span style={{ fontSize: '.75rem', color: 'var(--muted)', display: 'block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={c.description}>
                          {c.description.substring(0, 55)}...
                        </span>
                      </td>
                      <td style={{ fontSize: '.82rem' }}>
                        <span style={{ fontWeight: '600', display: 'block' }}>{c.submittedBy?.name}</span>
                        <span style={{ color: 'var(--muted)' }}>{c.submittedBy?.email}</span>
                      </td>
                      <td>
                        <span style={{ fontSize: '.78rem', background: '#f1f5f9', padding: '.2em .55em', borderRadius: '6px' }}>
                          {c.category}
                        </span>
                      </td>
                      <td>
                        <SeverityBadge severity={c.severity} />
                        {c.severityConfidence && (
                          <span style={{ display: 'block', fontSize: '.72rem', color: 'var(--muted)', marginTop: '.15rem' }}>
                            {(c.severityConfidence * 100).toFixed(0)}% conf.
                          </span>
                        )}
                      </td>
                      <td>
                        <select
                          className="form-control"
                          style={{ fontSize: '.82rem', padding: '.4rem .6rem' }}
                          value={getVal(c, 'status')}
                          onChange={e => handleEditChange(c._id, 'status', e.target.value)}
                        >
                          {STATUS_OPTIONS.map(s => (
                            <option key={s} value={s} style={{ textTransform: 'capitalize' }}>
                              {s.charAt(0).toUpperCase() + s.slice(1)}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <input
                          type="text"
                          className="form-control"
                          style={{ fontSize: '.82rem', padding: '.4rem .6rem' }}
                          value={getVal(c, 'adminNotes') || ''}
                          onChange={e => handleEditChange(c._id, 'adminNotes', e.target.value)}
                          placeholder="Add a note..."
                        />
                      </td>
                      <td style={{ fontSize: '.8rem', color: 'var(--muted)', whiteSpace: 'nowrap' }}>
                        {new Date(c.createdAt).toLocaleDateString()}
                      </td>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '.4rem' }}>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleSave(c._id)}
                            disabled={saving[c._id] || !edits[c._id]}
                          >
                            {saving[c._id] ? '...' : saved[c._id] ? '✓ Saved' : 'Save'}
                          </button>
                          <button
                            className="btn btn-sm"
                            style={{ background: '#fee2e2', color: '#c81e1e', border: 'none', cursor: 'pointer' }}
                            onClick={() => handleDelete(c._id)}
                          >
                            Delete
                          </button>
                        </div>
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
