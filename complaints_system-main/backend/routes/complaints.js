/**
 * routes/complaints.js
 *
 * GET    /api/complaints          — list complaints (student sees own, admin sees all)
 * POST   /api/complaints          — submit new complaint (triggers ML classification)
 * GET    /api/complaints/:id      — get single complaint
 * PUT    /api/complaints/:id      — update status/notes (admin only)
 * DELETE /api/complaints/:id      — delete complaint (admin only)
 * GET    /api/complaints/stats    — dashboard statistics (admin only)
 */

const express = require('express');
const fetch   = require('node-fetch');
const { body, validationResult } = require('express-validator');

const Complaint              = require('../models/Complaint');
const { authMiddleware, adminOnly } = require('../middleware/auth');

const router = express.Router();
const ML_URL = process.env.ML_SERVICE_URL || 'http://localhost:5001';

// ── Helper: call the Python ML service ───────────────────
async function classifySeverity(text, department) {
  try {
    const response = await fetch(`${ML_URL}/predict`, {
      method:  'POST',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ text, department }),
      timeout: 5000    // 5 second timeout
    });

    if (!response.ok) throw new Error('ML service error');
    return await response.json();
  } catch (err) {
    console.warn('ML service unavailable, using fallback:', err.message);
    // Graceful fallback — don't crash if ML is down
    return { severity: 'Unclassified', confidence: null };
  }
}

// ────────────────────────────────────────────────────────
// POST /api/complaints  — submit a new complaint
// ────────────────────────────────────────────────────────
router.post(
  '/',
  authMiddleware,
  [
    body('title').trim().isLength({ min: 5, max: 100 }).withMessage('Title must be 5–100 characters'),
    body('description').trim().isLength({ min: 20 }).withMessage('Description must be at least 20 characters'),
    body('category').notEmpty().withMessage('Category is required'),
  ],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    try {
      const { title, description, category } = req.body;

      // ── Step 1: Call ML service to predict severity ───
      console.log(`Classifying complaint: "${title.substring(0, 40)}..."`);
      const mlResult = await classifySeverity(
        `${title}. ${description}`,  // combine for better context
        category
      );

      // ── Step 2: Save to MongoDB ───────────────────────
      const complaint = await Complaint.create({
        title,
        description,
        category,
        submittedBy:        req.user._id,
        severity:           mlResult.severity,
        severityConfidence: mlResult.confidence
      });

      // Populate submitter name for the response
      await complaint.populate('submittedBy', 'name email');

      console.log(`✔ Complaint saved | Severity: ${mlResult.severity} (${(mlResult.confidence * 100).toFixed(0)}%)`);

      res.status(201).json({
        message: 'Complaint submitted successfully!',
        complaint
      });
    } catch (err) {
      console.error('Create complaint error:', err);
      res.status(500).json({ message: 'Failed to submit complaint.' });
    }
  }
);

// ────────────────────────────────────────────────────────
// GET /api/complaints — list complaints
// ────────────────────────────────────────────────────────
router.get('/', authMiddleware, async (req, res) => {
  try {
    const filter = {};

    // Students can only see their own complaints
    if (req.user.role === 'student') {
      filter.submittedBy = req.user._id;
    }

    // Optional query params for filtering (admin use)
    if (req.query.status)   filter.status   = req.query.status;
    if (req.query.severity) filter.severity = req.query.severity;
    if (req.query.category) filter.category = req.query.category;

    const complaints = await Complaint
      .find(filter)
      .populate('submittedBy', 'name email')
      .sort({ createdAt: -1 });   // newest first

    res.json({ count: complaints.length, complaints });
  } catch (err) {
    console.error('List complaints error:', err);
    res.status(500).json({ message: 'Failed to fetch complaints.' });
  }
});

// ────────────────────────────────────────────────────────
// GET /api/complaints/stats  — dashboard stats (admin)
// ────────────────────────────────────────────────────────
router.get('/stats', authMiddleware, adminOnly, async (req, res) => {
  try {
    const [total, byStatus, bySeverity, byCategory] = await Promise.all([
      Complaint.countDocuments(),
      Complaint.aggregate([{ $group: { _id: '$status',   count: { $sum: 1 } } }]),
      Complaint.aggregate([{ $group: { _id: '$severity', count: { $sum: 1 } } }]),
      Complaint.aggregate([{ $group: { _id: '$category', count: { $sum: 1 } } }])
    ]);

    res.json({
      total,
      byStatus:   Object.fromEntries(byStatus.map(x   => [x._id, x.count])),
      bySeverity: Object.fromEntries(bySeverity.map(x => [x._id, x.count])),
      byCategory: Object.fromEntries(byCategory.map(x => [x._id, x.count]))
    });
  } catch (err) {
    res.status(500).json({ message: 'Failed to fetch stats.' });
  }
});

// ────────────────────────────────────────────────────────
// GET /api/complaints/:id  — get single complaint
// ────────────────────────────────────────────────────────
router.get('/:id', authMiddleware, async (req, res) => {
  try {
    const complaint = await Complaint
      .findById(req.params.id)
      .populate('submittedBy', 'name email');

    if (!complaint) {
      return res.status(404).json({ message: 'Complaint not found.' });
    }

    // Students can only view their own complaints
    if (
      req.user.role === 'student' &&
      complaint.submittedBy._id.toString() !== req.user._id.toString()
    ) {
      return res.status(403).json({ message: 'Access denied.' });
    }

    res.json({ complaint });
  } catch (err) {
    res.status(500).json({ message: 'Failed to fetch complaint.' });
  }
});

// ────────────────────────────────────────────────────────
// PUT /api/complaints/:id  — update status/notes (admin)
// ────────────────────────────────────────────────────────
router.put('/:id', authMiddleware, adminOnly, async (req, res) => {
  try {
    const { status, adminNotes } = req.body;

    const complaint = await Complaint.findByIdAndUpdate(
      req.params.id,
      { ...(status     && { status }),
        ...(adminNotes !== undefined && { adminNotes }) },
      { new: true, runValidators: true }
    ).populate('submittedBy', 'name email');

    if (!complaint) {
      return res.status(404).json({ message: 'Complaint not found.' });
    }

    res.json({ message: 'Complaint updated.', complaint });
  } catch (err) {
    res.status(500).json({ message: 'Failed to update complaint.' });
  }
});

// ────────────────────────────────────────────────────────
// DELETE /api/complaints/:id  (admin only)
// ────────────────────────────────────────────────────────
router.delete('/:id', authMiddleware, adminOnly, async (req, res) => {
  try {
    const complaint = await Complaint.findByIdAndDelete(req.params.id);
    if (!complaint) {
      return res.status(404).json({ message: 'Complaint not found.' });
    }
    res.json({ message: 'Complaint deleted.' });
  } catch (err) {
    res.status(500).json({ message: 'Failed to delete complaint.' });
  }
});

module.exports = router;
