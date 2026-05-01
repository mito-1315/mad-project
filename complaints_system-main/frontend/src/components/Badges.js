/**
 * components/Badges.js
 * Reusable badge components for severity and status
 */

import React from 'react';

export function SeverityBadge({ severity }) {
  const map = {
    Low:           'badge badge-low',
    Medium:        'badge badge-medium',
    High:          'badge badge-high',
    Unclassified:  'badge badge-unclassified',
  };
  const icons = { Low: '🟢', Medium: '🟡', High: '🔴', Unclassified: '⚪' };
  return (
    <span className={map[severity] || 'badge badge-unclassified'}>
      {icons[severity]} {severity}
    </span>
  );
}

export function StatusBadge({ status }) {
  const map = {
    'pending':     'badge badge-pending',
    'in-progress': 'badge badge-in-progress',
    'resolved':    'badge badge-resolved',
    'rejected':    'badge badge-rejected',
  };
  const labels = {
    'pending':     'Pending',
    'in-progress': 'In Progress',
    'resolved':    'Resolved',
    'rejected':    'Rejected',
  };
  return <span className={map[status] || 'badge'}>{labels[status] || status}</span>;
}
