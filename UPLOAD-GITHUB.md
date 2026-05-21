# Cara Upload ke GitHub & Download APK

## Bagian A — Buat Akun & Repo di GitHub (Sekali)

1. Buka https://github.com → **Sign up** (gratis) kalau belum punya akun
2. Login → klik **+** (kanan atas) → **New repository**
3. Isi:
   - **Repository name:** `mutasi-push-notif` (bebas, boleh nama lain)
   - **Public** ✓
   - **Jangan** centang "Add a README" (biar kosong)
4. Klik **Create repository**
5. Catat URL repo, contoh:
   ```
   https://github.com/USERNAME-KAMU/mutasi-push-notif.git
   ```

---

## Bagian B — Upload Project (Pilih salah satu)

### Opsi 1 — Pakai Script (Paling Cepat)

1. Double-click **`upload-github.bat`**
2. Ikuti petunjuk di layar
3. Saat diminta URL repo, paste URL dari Bagian A (langkah 5)
4. Login GitHub kalau diminta (browser akan terbuka)

### Opsi 2 — Manual lewat Command

Buka **Command Prompt** atau **PowerShell** di folder project, lalu:

```bat
cd "C:\Users\WINDOWS 10\Desktop\MUTASI PUSH NOTIF"
git init
git add .
git commit -m "Initial commit - Mutasi Push Notif"
git branch -M main
git remote add origin https://github.com/USERNAME-KAMU/mutasi-push-notif.git
git push -u origin main
```

Ganti `USERNAME-KAMU` dengan username GitHub kamu.

> Saat `git push`, masukkan username + **Personal Access Token** (bukan password).
> Buat token: GitHub → Settings → Developer settings → Personal access tokens → Generate new token (classic) → centang **repo** → Generate → copy token.

### Opsi 3 — Upload lewat Website (Tanpa Git)

1. Di halaman repo GitHub yang baru dibuat, klik **uploading an existing file**
2. Drag & drop **semua isi folder** `MUTASI PUSH NOTIF` ke browser
   - Pastikan folder `.github` ikut ter-upload (penting untuk build APK)
3. Scroll bawah → **Commit changes**

---

## Bagian C — Build APK di GitHub Actions

1. Buka repo kamu di GitHub
2. Klik tab **Actions** (atas)
3. Kalau diminta "Workflows aren't being run" → klik **I understand my workflows, go ahead and enable them**
4. Di kiri pilih **Build APK**
5. Klik **Run workflow** → **Run workflow** (hijau)
6. Tunggu ~5–10 menit (ikon kuning → hijau ✓)
7. Klik run yang selesai → scroll ke **Artifacts**
8. Download **mutasi-push-notif-apk**
9. Extract ZIP → dapat file **`app-debug.apk`**
10. Copy ke HP → install

---

## Troubleshooting

| Masalah | Solusi |
|---------|--------|
| Tab Actions tidak ada | Pastikan folder `.github/workflows/build-apk.yml` ikut ter-upload |
| Push ditolak / auth error | Pakai Personal Access Token, bukan password |
| Build gagal | Klik run merah → baca log error |
| Tidak bisa install APK di HP | Settings → izinkan install dari sumber tidak dikenal |

---

## Setelah APK Terinstall

1. Jalankan `start-server.bat` di PC
2. Buka app di HP → aktifkan akses notifikasi
3. URL server: `http://[IP-PC]:3000`
4. API Key: `mutasi-secret-key`
