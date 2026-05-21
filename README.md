# Mutasi Push Notif

Aplikasi untuk menangkap notifikasi dari HP Android (mutasi bank, e-wallet, dll), mengirim ke server secara cepat, dan bisa **resend** jika gagal.

## Fitur

- **Terima notifikasi** — Android app menangkap semua push notification via Notification Listener
- **Kirim ke server** — Otomatis POST ke server begitu notif masuk (< 5 detik)
- **Resend** — Kirim ulang per-notif atau retry semua yang gagal
- **Kirim cepat** — OkHttp dengan timeout 5 detik, langsung forward tanpa antrian panjang
- **Dashboard web** — Lihat semua notifikasi di browser
- **Filter app** — Opsional, hanya tangkap notif dari app tertentu (BCA, Dana, dll)

## Struktur Project

```
MUTASI PUSH NOTIF/
├── server/          → Node.js API + Dashboard web
├── android-app/     → Android app (Kotlin)
└── start-server.bat → Jalankan server di Windows
```

## Cara Pakai

### 1. Jalankan Server (PC/Laptop)

```bat
start-server.bat
```

Atau manual:

```bash
cd server
npm install
npm start
```

Server jalan di **http://localhost:3000**
Dashboard: **http://localhost:3000**

Default API Key: `mutasi-secret-key`

### 2. Build & Install Android App

**Tanpa Android Studio?** Lihat **[BUILD-TANPA-ANDROID-STUDIO.md](BUILD-TANPA-ANDROID-STUDIO.md)**

Cara termudah: upload ke GitHub → Actions → download APK.

Atau di PC:
```bat
setup-build.bat
install-sdk-minimal.bat
build-apk.bat
```

Alternatif: buka `android-app` di Android Studio → Build APK.

### 3. Setup di HP

1. Buka app **Mutasi Push Notif**
2. Tap **Aktifkan Akses Notifikasi** → izinkan app ini
3. Isi **URL Server** dengan IP PC kamu, contoh:
   - `http://192.168.1.100:3000` (ganti dengan IP WiFi PC)
4. API Key: `mutasi-secret-key` (sama dengan server)
5. Tap **Simpan Pengaturan**
6. Pastikan status **Server: Online ✓** dan **Akses Notifikasi: Aktif ✓**

### 4. Filter App (Opsional)

Di field Filter App, isi package name dipisah koma:

```
com.bca, id.dana, com.gojek.app
```

Kosongkan = tangkap semua notifikasi.

## API Endpoints

| Method | Endpoint | Deskripsi |
|--------|----------|-----------|
| POST | `/api/notifications` | Terima notifikasi |
| POST | `/api/notifications/batch` | Terima batch |
| POST | `/api/notifications/:id/resend` | Resend by ID |
| GET | `/api/notifications` | List notifikasi |
| GET | `/api/health` | Health check |

Header: `X-API-Key: mutasi-secret-key`

## Tips

- HP dan PC harus **satu jaringan WiFi**
- Matikan battery optimization untuk app ini agar tetap jalan di background
- Cek firewall Windows — buka port **3000**
- Ganti API Key di production: set env `API_KEY=your-secret`

## Environment Variables (Server)

| Variable | Default | Deskripsi |
|----------|---------|-----------|
| PORT | 3000 | Port server |
| API_KEY | mutasi-secret-key | API key untuk auth |
