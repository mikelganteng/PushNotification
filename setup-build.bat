@echo off
setlocal
echo Setup Gradle Wrapper...
echo.

if exist "%~dp0android-app\gradlew.bat" (
    if exist "%~dp0android-app\gradle\wrapper\gradle-wrapper.jar" (
        echo Gradle wrapper sudah siap!
        echo.
        echo Langkah berikutnya:
        echo   - install-sdk-minimal.bat  lalu  build-apk.bat
        echo   - ATAU build via GitHub ^(lihat BUILD-TANPA-ANDROID-STUDIO.md^)
        pause
        exit /b 0
    )
)

echo Download gradle-wrapper.jar...
powershell -Command "New-Item -ItemType Directory -Force -Path '%~dp0android-app\gradle\wrapper' | Out-Null; Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar' -OutFile '%~dp0android-app\gradle\wrapper\gradle-wrapper.jar' -UseBasicParsing"

if exist "%~dp0android-app\gradlew.bat" (
    echo Setup selesai!
) else (
    echo [ERROR] File wrapper belum lengkap. Cek folder android-app.
)
echo.
echo Lihat BUILD-TANPA-ANDROID-STUDIO.md untuk langkah build APK.
pause
