@echo off
set "PATH=%PATH%;C:\Users\logit\tools\apache-maven-3.9.9\bin"
cd /d "%~dp0fintel-client"
mvn compile javafx:run
pause
