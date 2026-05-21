@echo off
setlocal
cd /d "%~dp0"

echo ========================================
echo  Upload Mutasi Push Notif ke GitHub
echo ========================================
echo.

where git >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git belum terinstall.
    echo Download: https://git-scm.com/download/win
    pause
    exit /b 1
)

if not exist ".git" (
    echo [1/4] Init git repository...
    git init
    git branch -M main
) else (
    echo [1/4] Git repo sudah ada.
)

echo [2/4] Add semua file...
git add .

echo [3/4] Commit...
git commit -m "Mutasi Push Notif - initial upload" 2>nul
if errorlevel 1 (
    echo Tidak ada perubahan baru, lanjut push...
)

echo.
echo [4/4] Push ke GitHub
echo.
echo Sebelum lanjut, buat repo kosong di github.com:
echo   New repository -^> nama: mutasi-push-notif -^> Create
echo.
set /p REPO_URL="Paste URL repo (contoh https://github.com/user/mutasi-push-notif.git): "
if "%REPO_URL%"=="" (
    echo URL kosong, dibatalkan.
    pause
    exit /b 1
)

git remote remove origin 2>nul
git remote add origin "%REPO_URL%"

echo.
echo Pushing... (login GitHub mungkin diminta)
git push -u origin main

if errorlevel 1 (
    echo.
    echo [ERROR] Push gagal.
    echo Tips:
    echo   - Pakai Personal Access Token sebagai password
    echo   - Buat di: github.com -^> Settings -^> Developer settings -^> Tokens
    echo   - Centang permission "repo"
    pause
    exit /b 1
)

echo.
echo ========================================
echo  BERHASIL UPLOAD!
echo ========================================
echo.
echo Langkah berikutnya:
echo   1. Buka repo di browser
echo   2. Tab Actions -^> Build APK -^> Run workflow
echo   3. Download APK dari Artifacts
echo.
echo Panduan lengkap: UPLOAD-GITHUB.md
pause
