@echo off
start "Fintel Backend (Spring Boot)" cmd /k "%~dp0run-server.bat"
echo Waiting 10 seconds for Spring Boot backend to initialize...
timeout /t 10 /nobreak >nul
start "Fintel Client (JavaFX)" cmd /k "%~dp0run-client.bat"
