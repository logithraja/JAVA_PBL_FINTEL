@echo off
start "Fintel Backend (Spring Boot)" cmd /k "%~dp0run-server.bat"
timeout /t 4 /nobreak >nul
start "Fintel Client (JavaFX)" cmd /k "%~dp0run-client.bat"
