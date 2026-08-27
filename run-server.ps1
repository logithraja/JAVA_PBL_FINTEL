$mavenCmd = "mvn"
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Users\logit\tools\apache-maven-3.9.9\bin\mvn.cmd") {
        $mavenCmd = "C:\Users\logit\tools\apache-maven-3.9.9\bin\mvn.cmd"
    }
}
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Starting Fintel Spring Boot Server... " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Set-Location -Path "$PSScriptRoot\fintel-springboot-server"
& $mavenCmd spring-boot:run
