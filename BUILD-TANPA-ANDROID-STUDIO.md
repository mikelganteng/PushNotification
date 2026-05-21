# Build APK Tanpa Android Studio

**Android Studio TIDAK wajib.** Pilih salah satu cara di bawah.

---

## Cara 1 — Paling Mudah: Build di GitHub (Gratis)

Tidak perlu install apapun di PC. Cukup punya akun GitHub.

### Langkah

1. Upload folder project ini ke GitHub (buat repo baru)
2. Buka repo → tab **Actions**
3. Klik workflow **Build APK** → **Run workflow**
4. Tunggu ~5 menit sampai selesai (hijau ✓)
5. Scroll ke bawah → download **mutasi-push-notif-apk**
6. Extract → dapat file `app-debug.apk`
7. Copy APK ke HP → install

> **Catatan:** Aktifkan "Allow from unknown sources" di HP untuk install APK manual.

---

## Cara 2 — Build di PC (SDK Ringan, Tanpa Android Studio)

Install hanya Android SDK Command Line Tools (~200MB), bukan Android Studio (1GB+).

### Langkah

```
1. setup-build.bat          ← sekali saja (download Gradle wrapper)
2. install-sdk-minimal.bat  ← sekali saja (download SDK, ~500MB)
3. build-apk.bat            ← build APK
```

APK ada di:
```
android-app\app\build\outputs\apk\debug\app-debug.apk
```

Copy ke HP → install.

### Syarat PC

- Windows 10/11
- Internet
- Java 17+ (download: https://adoptium.net/temurin/releases/?version=17)
  - Java 8 yang sudah terpasang **tidak cukup** untuk build Android modern

---

## Cara 3 — Minta APK dari Orang Lain

Kalau ada teman/komputer lain yang punya Android Studio atau sudah build:

1. Jalankan `build-apk.bat` di PC mereka, atau
2. Build di GitHub Actions (Cara 1)

Kirim file `app-debug.apk` ke kamu via WhatsApp / Google Drive.

---

## Setelah APK Terinstall

1. Jalankan server: `start-server.bat`
2. Buka app di HP → aktifkan **Akses Notifikasi**
3. Isi URL server: `http://[IP-PC]:3000`
4. API Key: `mutasi-secret-key`
5. Simpan

---

## Perbandingan

| Cara | Install di PC | Ukuran download | Waktu |
|------|---------------|-----------------|-------|
| GitHub Actions | Tidak perlu | 0 | ~5 menit |
| SDK minimal | Ya (SDK saja) | ~700MB | ~10 menit pertama |
| Android Studio | Ya (full IDE) | ~1.5GB | ~15 menit |

**Rekomendasi:** pakai **Cara 1 (GitHub)** kalau tidak mau install apapun.
