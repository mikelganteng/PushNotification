@echo off
cd /d "%~dp0server"
echo Installing dependencies...
call npm install
if errorlevel 1 exit /b 1
echo.
echo Starting Mutasi Notif Server on port 3000...
echo Dashboard: http://localhost:3000
echo.
call npm start
