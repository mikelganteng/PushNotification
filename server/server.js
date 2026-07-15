const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const path = require('path');
const store = require('./store');

const PORT = process.env.PORT || 3000;
const API_KEY = process.env.API_KEY || 'mutasi-secret-key';

const app = express();
app.use(cors());
app.use(express.json({ limit: '1mb' }));
app.use(express.static(path.join(__dirname, 'public')));

function authMiddleware(req, res, next) {
  const key = req.headers['x-api-key'];
  if (key !== API_KEY) {
    return res.status(401).json({ error: 'Unauthorized' });
  }
  next();
}

function makeRow(body, id, now) {
  return {
    id,
    package_name: body.package_name,
    app_name: body.app_name || null,
    title: body.title || null,
    body: body.body || null,
    big_text: body.big_text || null,
    posted_at: body.posted_at || now,
    received_at: now,
    device_id: body.device_id || null,
    status: 'received',
    resend_count: 0,
    // Transaction fields
    bank_name: body.bank_name || null,
    transaction_type: body.transaction_type || null,
    amount: body.amount || null,
    account_number: body.account_number || null,
    sender_name: body.sender_name || null,
    is_qris: body.is_qris || false,
    raw_json: JSON.stringify(body),
  };
}

app.post('/api/notifications', authMiddleware, (req, res) => {
  const {
    package_name,
    resend,
    original_id,
  } = req.body;

  if (!package_name) {
    return res.status(400).json({ error: 'package_name required' });
  }

  const now = Date.now();

  if (resend && original_id) {
    const existing = store.getById(original_id);
    if (!existing) {
      return res.status(404).json({ error: 'Notification not found' });
    }
    const updated = store.update(original_id, {
      status: 'resent',
      resend_count: (existing.resend_count || 0) + 1,
      received_at: now,
    });
    return res.json({
      success: true,
      id: original_id,
      message: 'Notification resent',
      resend_count: updated.resend_count,
    });
  }

  const id = uuidv4();
  store.insert(makeRow(req.body, id, now));
  res.status(201).json({ success: true, id, message: 'Notification received' });
});

app.post('/api/notifications/batch', authMiddleware, (req, res) => {
  const { notifications } = req.body;
  if (!Array.isArray(notifications) || notifications.length === 0) {
    return res.status(400).json({ error: 'notifications array required' });
  }

  const now = Date.now();
  let inserted = 0;
  for (const n of notifications) {
    const id = n.id || uuidv4();
    if (store.insert(makeRow(n, id, now))) inserted++;
  }

  res.status(201).json({ success: true, inserted, total: notifications.length });
});

app.post('/api/notifications/:id/resend', authMiddleware, (req, res) => {
  const { id } = req.params;
  const row = store.getById(id);
  if (!row) return res.status(404).json({ error: 'Not found' });

  const updated = store.update(id, {
    status: 'resent',
    resend_count: (row.resend_count || 0) + 1,
    received_at: Date.now(),
  });

  res.json({ success: true, notification: updated });
});

app.get('/api/notifications', (req, res) => {
  const limit = Math.min(parseInt(req.query.limit) || 50, 200);
  const offset = parseInt(req.query.offset) || 0;
  const status = req.query.status || null;
  const result = store.list({ limit, offset, status });
  res.json({ ...result, limit, offset });
});

app.get('/api/notifications/:id', (req, res) => {
  const row = store.getById(req.params.id);
  if (!row) return res.status(404).json({ error: 'Not found' });
  res.json(row);
});

app.delete('/api/notifications/:id', authMiddleware, (req, res) => {
  if (!store.remove(req.params.id)) return res.status(404).json({ error: 'Not found' });
  res.json({ success: true });
});

app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', notifications: store.getAll().length, uptime: process.uptime() });
});

app.get('/api/stats', (req, res) => {
  const all = store.getAll();
  const transactions = all.filter(n => n.amount != null);
  const totalAmount = transactions.reduce((sum, n) => sum + (n.amount || 0), 0);
  const qrisTransactions = transactions.filter(n => n.is_qris);
  const creditTransactions = transactions.filter(n => n.transaction_type === 'credit');
  const debitTransactions = transactions.filter(n => n.transaction_type === 'debit');
  
  // Group by bank
  const byBank = {};
  transactions.forEach(n => {
    if (n.bank_name) {
      if (!byBank[n.bank_name]) {
        byBank[n.bank_name] = { count: 0, total: 0 };
      }
      byBank[n.bank_name].count++;
      byBank[n.bank_name].total += n.amount || 0;
    }
  });
  
  res.json({
    total_notifications: all.length,
    total_transactions: transactions.length,
    qris_transactions: qrisTransactions.length,
    credit_transactions: creditTransactions.length,
    debit_transactions: debitTransactions.length,
    total_amount: totalAmount,
    by_bank: byBank
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Mutasi Notif Server running on http://0.0.0.0:${PORT}`);
  console.log(`Dashboard: http://localhost:${PORT}`);
  console.log(`API Key: ${API_KEY}`);
});
