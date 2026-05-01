/**
 * pages/SignupPage.js
 */

import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

export default function SignupPage() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [form, setForm]         = useState({ name: '', email: '', password: '', confirm: '', role: 'student' });
  const [errors, setErrors]     = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading]   = useState(false);

  const validate = () => {
    const e = {};
    if (!form.name || form.name.trim().length < 2)   e.name     = 'Name must be at least 2 characters';
    if (!form.email || !/\S+@\S+\.\S+/.test(form.email)) e.email = 'Enter a valid email';
    if (!form.password || form.password.length < 6)  e.password = 'Password must be at least 6 characters';
    if (form.password !== form.confirm)              e.confirm  = 'Passwords do not match';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) { setErrors(validationErrors); return; }

    setLoading(true);
    try {
      const { data } = await axios.post('/api/auth/signup', {
        name: form.name, email: form.email, password: form.password, role: form.role
      });
      login(data.token, data.user);
      navigate(data.user.role === 'admin' ? '/admin' : '/dashboard');
    } catch (err) {
      setApiError(err.response?.data?.message || 'Signup failed. Try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div style={{ fontSize: '2.5rem' }}>📝</div>
          <h1>Create account</h1>
          <p>Join the complaint management portal</p>
        </div>

        {apiError && <div className="alert alert-error">{apiError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          {[
            { id: 'name',     label: 'Full name',        type: 'text',     placeholder: 'Your Name' },
            { id: 'email',    label: 'Email address',    type: 'email',    placeholder: 'you@university.edu' },
            { id: 'password', label: 'Password',         type: 'password', placeholder: '••••••••' },
            { id: 'confirm',  label: 'Confirm password', type: 'password', placeholder: '••••••••' },
          ].map(({ id, label, type, placeholder }) => (
            <div className="form-group" key={id}>
              <label htmlFor={id}>{label}</label>
              <input
                id={id} name={id} type={type}
                className={`form-control ${errors[id] ? 'error' : ''}`}
                value={form[id]} onChange={handleChange} placeholder={placeholder}
              />
              {errors[id] && <p className="error-text">{errors[id]}</p>}
            </div>
          ))}

          <div className="form-group">
            <label htmlFor="role">Account type</label>
            <select id="role" name="role" className="form-control" value={form.role} onChange={handleChange}>
              <option value="student">Student</option>
              <option value="admin">Admin</option>
            </select>
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading} style={{ marginTop: '.5rem' }}>
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p style={{ textAlign: 'center', marginTop: '1.25rem', fontSize: '.9rem', color: '#64748b' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--primary)', fontWeight: '600' }}>Sign in</Link>
        </p>
      </div>
    </div>
  );
}
