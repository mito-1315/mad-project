/**
 * routes/auth.js
 *
 * POST /api/auth/signup  — create a new account
 * POST /api/auth/login   — get JWT token
 * GET  /api/auth/me      — get current user info (protected)
 */

const express = require('express');
const jwt     = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');

const User              = require('../models/User');
const { authMiddleware } = require('../middleware/auth');

const router = express.Router();

// ── Helper: generate a JWT token ─────────────────────────
const generateToken = (userId) =>
  jwt.sign(
    { userId },                        // payload
    process.env.JWT_SECRET,            // secret
    { expiresIn: '7d' }               // expires in 7 days
  );

// ────────────────────────────────────────────────────────
// POST /api/auth/signup
// ────────────────────────────────────────────────────────
router.post(
  '/signup',
  // Input validation rules
  [
    body('name').trim().isLength({ min: 2 }).withMessage('Name must be at least 2 characters'),
    body('email').isEmail().normalizeEmail().withMessage('Please enter a valid email'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
  ],
  async (req, res) => {
    // Check validation errors
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    try {
      const { name, email, password, role } = req.body;

      // Check if user already exists
      const existing = await User.findOne({ email });
      if (existing) {
        return res.status(409).json({ message: 'Email already registered.' });
      }

      // Create new user (password is hashed by the pre-save hook in User model)
      const user = await User.create({
        name,
        email,
        password,
        // Only allow 'admin' role if explicitly set AND you trust the source
        // In production, remove this and handle admin creation separately
        role: role === 'admin' ? 'admin' : 'student'
      });

      const token = generateToken(user._id);

      res.status(201).json({
        message: 'Account created successfully!',
        token,
        user: { id: user._id, name: user.name, email: user.email, role: user.role }
      });
    } catch (err) {
      console.error('Signup error:', err);
      res.status(500).json({ message: 'Server error during signup.' });
    }
  }
);

// ────────────────────────────────────────────────────────
// POST /api/auth/login
// ────────────────────────────────────────────────────────
router.post(
  '/login',
  [
    body('email').isEmail().normalizeEmail().withMessage('Please enter a valid email'),
    body('password').notEmpty().withMessage('Password is required'),
  ],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      return res.status(400).json({ errors: errors.array() });
    }

    try {
      const { email, password } = req.body;

      // Find user by email
      const user = await User.findOne({ email });
      if (!user) {
        return res.status(401).json({ message: 'Invalid email or password.' });
      }

      // Compare password using the model method we defined
      const isMatch = await user.comparePassword(password);
      if (!isMatch) {
        return res.status(401).json({ message: 'Invalid email or password.' });
      }

      const token = generateToken(user._id);

      res.json({
        message: 'Login successful!',
        token,
        user: { id: user._id, name: user.name, email: user.email, role: user.role }
      });
    } catch (err) {
      console.error('Login error:', err);
      res.status(500).json({ message: 'Server error during login.' });
    }
  }
);

// ────────────────────────────────────────────────────────
// GET /api/auth/me  (protected — requires JWT)
// ────────────────────────────────────────────────────────
router.get('/me', authMiddleware, (req, res) => {
  // authMiddleware already attached req.user
  res.json({
    user: {
      id:    req.user._id,
      name:  req.user.name,
      email: req.user.email,
      role:  req.user.role
    }
  });
});

module.exports = router;
