@echo off
start "Finvora Backend (Spring Boot)" cmd /k "%~dp0run-server.bat"
timeout /t 4 /nobreak >nul
start "Finvora Client (JavaFX)" cmd /k "%~dp0run-client.bat"
