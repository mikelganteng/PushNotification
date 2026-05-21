@echo off
setlocal
cd /d "%~dp0android-app"

if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat belum ada.
    echo Jalankan dulu: setup-build.bat
    pause
    exit /b 1
)

if "%ANDROID_HOME%"=="" (
    if exist "%LOCALAPPDATA%\Android\Sdk" (
        set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
    ) else (
        echo.
        echo [ERROR] Android SDK tidak ditemukan.
        echo.
        echo Pilih salah satu:
        echo   1. Tanpa install apapun: upload project ke GitHub, build otomatis, download APK
        echo      Lihat: BUILD-TANPA-ANDROID-STUDIO.md
        echo.
        echo   2. Install SDK ringan saja ^(tanpa Android Studio^):
        echo      Jalankan: install-sdk-minimal.bat
        echo.
        pause
        exit /b 1
    )
)

echo Building APK...
call gradlew.bat assembleDebug --no-daemon
if errorlevel 1 (
    echo.
    echo [ERROR] Build gagal.
    pause
    exit /b 1
)

echo.
echo ========================================
echo  APK SIAP!
echo ========================================
echo.
echo Lokasi:
echo   android-app\app\build\outputs\apk\debug\app-debug.apk
echo.
echo Copy file APK ke HP, lalu install.
echo.
pause
