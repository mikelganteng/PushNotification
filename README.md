# 📱 Mutasi Push Notification

Aplikasi monitoring dan sinkronisasi otomatis notifikasi transaksi banking ke server. Cocok untuk server pulsa, payment gateway, atau sistem yang membutuhkan auto-konfirmasi transaksi dari notifikasi banking.

## ✨ Fitur Utama

### 🔔 Monitoring Otomatis
- Deteksi otomatis notifikasi transaksi dari berbagai bank & e-wallet
- Support transaksi regular dan QRIS
- Parsing otomatis nominal, nomor rekening, dan detail transaksi
- Monitoring real-time 24/7 tanpa perlu buka aplikasi

### 🏦 Support Multi-Bank
- BCA (Bank Central Asia)
- Mandiri
- BRI (Bank Rakyat Indonesia)
- BNI (Bank Negara Indonesia)
- Jago
- Seabank
- DANA
- OVO
- GoPay
- Dan bank lain yang memiliki notifikasi transaksi

### 🌐 Integrasi Server
- Kirim data transaksi langsung ke server Anda
- Konfigurasi URL server custom tanpa rebuild aplikasi
- Support multiple server URLs (dropdown preset)
- Format data JSON standar REST API
- API Key authentication

### ⚡ Handal & Cepat
- Retry otomatis jika pengiriman gagal
- Queue system untuk transaksi pending
- Foreground service untuk monitoring berkelanjutan
- Local database sebagai backup
- Status tracking real-time (Success/Pending/Failed)

### 📊 Dashboard Transaksi
- Lihat history transaksi yang terdeteksi
- Filter transaksi regular dan QRIS
- Status transfer real-time
- Statistik transaksi (total, QRIS count, dll)
- Auto refresh setelah transaksi berhasil terkirim
- Visual indicator (📥 credit, 📤 debit)

## 🔒 Keamanan & Privasi

✅ **Data Aman**
- Data transaksi hanya dikirim ke server yang Anda konfigurasi
- Koneksi HTTPS terenkripsi
- Tidak ada akses pihak ketiga
- Tidak ada penyimpanan cloud eksternal

✅ **Privacy First**
- Data disimpan lokal di device Anda
- Anda memiliki kontrol penuh atas data
- Notification Listener digunakan HANYA untuk parsing transaksi
- Tidak ada akses ke notifikasi private lainnya

## ⚙️ Cara Menggunakan

1. Install aplikasi
2. Berikan permission Notification Listener
3. Konfigurasi URL server Anda di Settings
4. Pilih aplikasi banking yang ingin dimonitor
5. Selesai! Aplikasi akan otomatis mendeteksi dan mengirim transaksi

## 🛠️ Setup Server

### Quick Start
```bash
cd server
npm install
npm start
```

Server akan berjalan di `http://localhost:3000`

### Environment Variables
```bash
PORT=3000
API_KEY=mutasi-secret-key
```

### API Endpoints

#### POST /api/notifications
Terima notifikasi dengan data transaksi
```json
{
  "package_name": "com.bca",
  "app_name": "BCA mobile",
  "title": "Transfer Masuk",
  "body": "Rp 500,000",
  "bank_name": "BCA",
  "transaction_type": "credit",
  "amount": 500000,
  "account_number": "1234567890",
  "is_qris": false
}
```

#### GET /api/notifications
List semua notifikasi
- Query params: `limit`, `offset`, `status`

#### GET /api/stats
Statistik transaksi
```json
{
  "total_notifications": 100,
  "total_transactions": 85,
  "qris_transactions": 25,
  "credit_transactions": 70,
  "debit_transactions": 15,
  "total_amount": 50000000,
  "by_bank": {
    "BCA": { "count": 40, "total": 20000000 },
    "Mandiri": { "count": 30, "total": 15000000 }
  }
}
```

#### GET /api/health
Health check server

## 🏗️ Teknologi

### Android App
- Kotlin
- Coroutines
- OkHttp3
- Material Design 3
- ViewBinding
- NotificationListenerService

### Server
- Node.js
- Express.js
- In-memory storage (extendable to database)
- REST API

## 📋 Requirements

### Android App
- Android 8.0 (Oreo) atau lebih tinggi (API 26+)
- Notification Listener permission
- Internet access

### Server
- Node.js 14+ 
- npm/yarn

## 🚀 Build & Deploy

### Build APK via GitHub Actions
1. Push ke branch `main`
2. GitHub Actions otomatis build APK
3. Download artifact dari Actions tab

### Build APK Lokal
```bash
cd android-app
./gradlew assembleDebug
```

APK akan tersedia di: `app/build/outputs/apk/debug/app-debug.apk`

## 📝 License

MIT License - Feel free to use for personal or commercial projects

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest new features
- Submit pull requests
- Add support for more banks

## 💡 Use Cases

- **Server Pulsa**: Auto-konfirmasi deposit pelanggan
- **Payment Gateway**: Monitoring transaksi real-time
- **Toko Online**: Notifikasi pembayaran otomatis
- **Kasir Digital**: Tracking transaksi masuk
- **Accounting**: Pencatatan transaksi otomatis

## 📞 Support

- GitHub Issues: Report bugs atau request features
- Documentation: Check `Current_Status.md` untuk update terbaru

## 🔄 Version History

### v1.0.1 (2026-07-16)
- ✨ Banking transaction parser
- ✨ QRIS detection
- ✨ Transaction statistics
- ✨ Enhanced dashboard
- 🐛 Fix Toast context in coroutine
- 🐛 Fix force close on server URL change

### v1.0.0 (Initial Release)
- Basic notification capture
- Server forwarding
- Retry mechanism
- App filtering
- Multiple server options

---

**Made with ❤️ for BOB RESEARCH LABS**

Consortium: Palo Alto, CrowdStrike, SentinelOne, Trend Micro, d1337.ai
