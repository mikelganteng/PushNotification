@echo off
setlocal
echo ========================================
echo  Install Android SDK (RINGAN)
echo  Tanpa Android Studio (~200MB)
echo ========================================
echo.

set "SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
set "CMD_TOOLS=%SDK_ROOT%\cmdline-tools\latest"

if exist "%CMD_TOOLS%\bin\sdkmanager.bat" goto :install_packages

echo [1/3] Download Android Command Line Tools...
set "TOOLS_ZIP=%TEMP%\cmdline-tools.zip"
powershell -Command ^
  "Invoke-WebRequest -Uri 'https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip' -OutFile '%TOOLS_ZIP%' -UseBasicParsing"
if errorlevel 1 (
    echo Download gagal.
    pause
    exit /b 1
)

echo [2/3] Extract...
mkdir "%SDK_ROOT%\cmdline-tools" 2>nul
powershell -Command "Expand-Archive -Path '%TOOLS_ZIP%' -DestinationPath '%SDK_ROOT%\cmdline-tools' -Force"
if exist "%SDK_ROOT%\cmdline-tools\cmdline-tools" (
    move "%SDK_ROOT%\cmdline-tools\cmdline-tools" "%SDK_ROOT%\cmdline-tools\latest"
)

:install_packages
echo [3/3] Install SDK packages (butuh internet, ~500MB)...
set "ANDROID_HOME=%SDK_ROOT%"
set "PATH=%CMD_TOOLS%\bin;%PATH%"

echo y | "%CMD_TOOLS%\bin\sdkmanager.bat" --licenses >nul 2>&1
"%CMD_TOOLS%\bin\sdkmanager.bat" "platform-tools" "platforms;android-34" "build-tools;34.0.0"
if errorlevel 1 (
    echo Install gagal. Coba jalankan sebagai Administrator.
    pause
    exit /b 1
)

echo.
echo SDK siap di: %SDK_ROOT%
echo.
echo Buat file local.properties...
(
echo sdk.dir=%SDK_ROOT:\=\\%
) > "%~dp0android-app\local.properties"

echo.
echo Selesai! Sekarang jalankan: build-apk.bat
pause
