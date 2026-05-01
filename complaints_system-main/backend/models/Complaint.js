/**
 * models/Complaint.js
 *
 * Defines the shape of a complaint document in MongoDB.
 * Each complaint is linked to the user who submitted it.
 */

const mongoose = require('mongoose');

const complaintSchema = new mongoose.Schema(
  {
    title: {
      type:     String,
      required: [true, 'Title is required'],
      trim:     true,
      minlength: [5,  'Title must be at least 5 characters'],
      maxlength: [100, 'Title cannot exceed 100 characters']
    },
    description: {
      type:     String,
      required: [true, 'Description is required'],
      minlength: [20, 'Description must be at least 20 characters']
    },
    category: {
      type:     String,
      required: true,
      enum: [
        'IT Support',
        'Hostels',
        'Academics',
        'Fees / Finance',
        'Maintenance',
        'Transport',
        'Security / Discipline',
        'Administration',
        'Other'
      ]
    },
    status: {
      type:    String,
      enum:    ['pending', 'in-progress', 'resolved', 'rejected'],
      default: 'pending'
    },

    // ── ML-predicted fields ────────────────────────────────
    severity: {
      type:    String,
      enum:    ['Low', 'Medium', 'High', 'Unclassified'],
      default: 'Unclassified'
    },
    severityConfidence: {
      type:    Number,
      default: null
    },

    // ── Relationships ──────────────────────────────────────
    submittedBy: {
      type:     mongoose.Schema.Types.ObjectId,
      ref:      'User',          // reference to the User collection
      required: true
    },
    adminNotes: {
      type:    String,
      default: ''
    }
  },
  { timestamps: true }
);

module.exports = mongoose.model('Complaint', complaintSchema);
