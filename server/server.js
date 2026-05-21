const express = require('express');
const cors = require('cors');
const Database = require('better-sqlite3');
const { v4: uuidv4 } = require('uuid');
const path = require('path');
const fs = require('fs');

const PORT = process.env.PORT || 3000;
const API_KEY = process.env.API_KEY || 'mutasi-secret-key';

const dataDir = path.join(__dirname, 'data');
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });

const db = new Database(path.join(dataDir, 'notifications.db'));

db.exec(`
  CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    package_name TEXT NOT NULL,
    app_name TEXT,
    title TEXT,
    body TEXT,
    big_text TEXT,
    posted_at INTEGER NOT NULL,
    received_at INTEGER NOT NULL,
    device_id TEXT,
    status TEXT DEFAULT 'received',
    resend_count INTEGER DEFAULT 0,
    raw_json TEXT
  );
  CREATE INDEX IF NOT EXISTS idx_posted_at ON notifications(posted_at DESC);
  CREATE INDEX IF NOT EXISTS idx_status ON notifications(status);
`);

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

// Terima notifikasi dari Android
app.post('/api/notifications', authMiddleware, (req, res) => {
  const {
    package_name,
    app_name,
    title,
    body,
    big_text,
    posted_at,
    device_id,
    resend,
    original_id,
  } = req.body;

  if (!package_name) {
    return res.status(400).json({ error: 'package_name required' });
  }

  const now = Date.now();
  const id = original_id && resend ? original_id : uuidv4();

  if (resend && original_id) {
    const existing = db.prepare('SELECT * FROM notifications WHERE id = ?').get(original_id);
    if (!existing) {
      return res.status(404).json({ error: 'Notification not found' });
    }
    db.prepare(`
      UPDATE notifications
      SET status = 'resent', resend_count = resend_count + 1, received_at = ?
      WHERE id = ?
    `).run(now, original_id);

    return res.json({
      success: true,
      id: original_id,
      message: 'Notification resent',
      resend_count: existing.resend_count + 1,
    });
  }

  db.prepare(`
    INSERT INTO notifications
    (id, package_name, app_name, title, body, big_text, posted_at, received_at, device_id, status, raw_json)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'received', ?)
  `).run(
    id,
    package_name,
    app_name || null,
    title || null,
    body || null,
    big_text || null,
    posted_at || now,
    now,
    device_id || null,
    JSON.stringify(req.body)
  );

  res.status(201).json({ success: true, id, message: 'Notification received' });
});

// Batch terima (kirim cepat banyak sekaligus)
app.post('/api/notifications/batch', authMiddleware, (req, res) => {
  const { notifications } = req.body;
  if (!Array.isArray(notifications) || notifications.length === 0) {
    return res.status(400).json({ error: 'notifications array required' });
  }

  const now = Date.now();
  const insert = db.prepare(`
    INSERT OR IGNORE INTO notifications
    (id, package_name, app_name, title, body, big_text, posted_at, received_at, device_id, status, raw_json)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'received', ?)
  `);

  const insertMany = db.transaction((items) => {
    let inserted = 0;
    for (const n of items) {
      const id = n.id || uuidv4();
      const result = insert.run(
        id,
        n.package_name,
        n.app_name || null,
        n.title || null,
        n.body || null,
        n.big_text || null,
        n.posted_at || now,
        now,
        n.device_id || null,
        JSON.stringify(n)
      );
      if (result.changes > 0) inserted++;
    }
    return inserted;
  });

  const count = insertMany(notifications);
  res.status(201).json({ success: true, inserted: count, total: notifications.length });
});

// Resend notifikasi by ID
app.post('/api/notifications/:id/resend', authMiddleware, (req, res) => {
  const { id } = req.params;
  const row = db.prepare('SELECT * FROM notifications WHERE id = ?').get(id);
  if (!row) return res.status(404).json({ error: 'Not found' });

  const now = Date.now();
  db.prepare(`
    UPDATE notifications SET status = 'resent', resend_count = resend_count + 1, received_at = ?
    WHERE id = ?
  `).run(now, id);

  res.json({
    success: true,
    notification: { ...row, status: 'resent', resend_count: row.resend_count + 1 },
  });
});

// List semua notifikasi
app.get('/api/notifications', (req, res) => {
  const limit = Math.min(parseInt(req.query.limit) || 50, 200);
  const offset = parseInt(req.query.offset) || 0;
  const status = req.query.status;

  let query = 'SELECT * FROM notifications';
  const params = [];
  if (status) {
    query += ' WHERE status = ?';
    params.push(status);
  }
  query += ' ORDER BY posted_at DESC LIMIT ? OFFSET ?';
  params.push(limit, offset);

  const rows = db.prepare(query).all(...params);
  const total = db.prepare('SELECT COUNT(*) as c FROM notifications').get().c;

  res.json({ total, limit, offset, data: rows });
});

// Detail satu notifikasi
app.get('/api/notifications/:id', (req, res) => {
  const row = db.prepare('SELECT * FROM notifications WHERE id = ?').get(req.params.id);
  if (!row) return res.status(404).json({ error: 'Not found' });
  res.json(row);
});

// Hapus notifikasi
app.delete('/api/notifications/:id', authMiddleware, (req, res) => {
  const result = db.prepare('DELETE FROM notifications WHERE id = ?').run(req.params.id);
  if (result.changes === 0) return res.status(404).json({ error: 'Not found' });
  res.json({ success: true });
});

// Health check
app.get('/api/health', (req, res) => {
  const count = db.prepare('SELECT COUNT(*) as c FROM notifications').get().c;
  res.json({ status: 'ok', notifications: count, uptime: process.uptime() });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Mutasi Notif Server running on http://0.0.0.0:${PORT}`);
  console.log(`Dashboard: http://localhost:${PORT}`);
  console.log(`API Key: ${API_KEY}`);
});
