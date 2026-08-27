@echo off
set "PATH=%PATH%;C:\Users\logit\tools\apache-maven-3.9.9\bin"
cd /d "%~dp0fintel-springboot-server"
mvn spring-boot:run
pause
