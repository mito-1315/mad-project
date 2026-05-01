/**
 * server.js — Entry point for the Express backend
 *
 * This file:
 *   1. Loads environment variables
 *   2. Connects to MongoDB
 *   3. Registers all API routes
 *   4. Starts listening on PORT
 */

const express    = require('express');
const mongoose   = require('mongoose');
const cors       = require('cors');
require('dotenv').config();          // reads .env file into process.env

const authRoutes      = require('./routes/auth');
const complaintRoutes = require('./routes/complaints');

const app = express();

// ── Middleware ────────────────────────────────────────────
app.use(cors({
  // Allow React dev server AND Android emulator (10.0.2.2 is the emulator's host loopback)
  origin: ['http://localhost:3000', 'http://10.0.2.2:3000', 'http://10.0.2.2'],
  credentials: true
}));
app.use(express.json());             // parse JSON request bodies

// ── API Routes ────────────────────────────────────────────
app.use('/api/auth',       authRoutes);
app.use('/api/complaints', complaintRoutes);

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date() });
});

// ── Connect to MongoDB, then start server ─────────────────
const PORT     = process.env.PORT     || 5000;
const MONGO_URI = process.env.MONGO_URI;

mongoose
  .connect(MONGO_URI)
  .then(() => {
    console.log('✔ MongoDB connected');
    app.listen(PORT, () => {
      console.log(`✔ Server running on http://localhost:${PORT}`);
    });
  })
  .catch(err => {
    console.error('✘ MongoDB connection error:', err.message);
    process.exit(1);
  });
