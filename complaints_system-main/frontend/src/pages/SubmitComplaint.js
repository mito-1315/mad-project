/**
 * pages/SubmitComplaint.js
 *
 * Student fills this form → backend calls ML → severity stored in DB.
 * After submit, user is redirected to their dashboard.
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Navbar from '../components/Navbar';

const CATEGORIES = [
  'IT Support', 'Hostels', 'Academics', 'Fees / Finance',
  'Maintenance', 'Transport', 'Security / Discipline', 'Administration', 'Other'
];

export default function SubmitComplaint() {
  const navigate = useNavigate();

  const [form, setForm]         = useState({ title: '', description: '', category: '' });
  const [errors, setErrors]     = useState({});
  const [apiError, setApiError] = useState('');
  const [success, setSuccess]   = useState('');
  const [loading, setLoading]   = useState(false);

  const validate = () => {
    const e = {};
    if (!form.title || form.title.trim().length < 5)          e.title       = 'Title must be at least 5 characters';
    if (!form.description || form.description.trim().length < 20) e.description = 'Description must be at least 20 characters';
    if (!form.category)                                        e.category    = 'Please select a category';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError(''); setSuccess('');
    const ve = validate();
    if (Object.keys(ve).length) { setErrors(ve); return; }

    setLoading(true);
    try {
      const { data } = await axios.post('/api/complaints', form);
      const sev = data.complaint.severity;
      setSuccess(`✅ Complaint submitted! Severity auto-classified as: ${sev}`);
      setForm({ title: '', description: '', category: '' });

      // Redirect after 2.5 seconds
      setTimeout(() => navigate('/dashboard'), 2500);
    } catch (err) {
      const msg = err.response?.data?.errors?.[0]?.msg
        || err.response?.data?.message
        || 'Submission failed. Try again.';
      setApiError(msg);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
  };

  return (
    <div className="page-wrapper">
      <Navbar />
      <div className="main-content">
        <div style={{ maxWidth: '640px', margin: '0 auto' }}>
          <div className="page-header">
            <div>
              <h1>Submit a Complaint</h1>
              <p style={{ color: 'var(--muted)', fontSize: '.9rem', marginTop: '.25rem' }}>
                Your complaint will be automatically classified by severity using AI.
              </p>
            </div>
          </div>

          <div className="card">
            {apiError && <div className="alert alert-error">{apiError}</div>}
            {success  && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit} noValidate>
              <div className="form-group">
                <label htmlFor="title">Complaint Title *</label>
                <input
                  id="title" name="title" type="text"
                  className={`form-control ${errors.title ? 'error' : ''}`}
                  value={form.title} onChange={handleChange}
                  placeholder="Brief summary of the issue (min 5 characters)"
                />
                {errors.title && <p className="error-text">{errors.title}</p>}
              </div>

              <div className="form-group">
                <label htmlFor="category">Category *</label>
                <select
                  id="category" name="category"
                  className={`form-control ${errors.category ? 'error' : ''}`}
                  value={form.category} onChange={handleChange}
                >
                  <option value="">-- Select a department --</option>
                  {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
                {errors.category && <p className="error-text">{errors.category}</p>}
              </div>

              <div className="form-group">
                <label htmlFor="description">
                  Description *
                  <span style={{ color: 'var(--muted)', fontWeight: '400', marginLeft: '.5rem' }}>
                    ({form.description.length} chars — min 20)
                  </span>
                </label>
                <textarea
                  id="description" name="description"
                  className={`form-control ${errors.description ? 'error' : ''}`}
                  value={form.description} onChange={handleChange}
                  placeholder="Describe your issue in detail. Include when it started, how it affects you, and any steps you've already taken."
                  rows={6}
                />
                {errors.description && <p className="error-text">{errors.description}</p>}
              </div>

              {/* AI classification notice */}
              <div className="alert alert-info" style={{ fontSize: '.85rem', marginBottom: '1rem' }}>
                🤖 <strong>AI Severity Classification:</strong> Our ML model will automatically classify your complaint as Low, Medium, or High severity to help prioritise responses.
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? '⏳ Submitting...' : '📤 Submit Complaint'}
                </button>
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => navigate('/dashboard')}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
