$mavenCmd = "mvn"
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Users\logit\tools\apache-maven-3.9.9\bin\mvn.cmd") {
        $mavenCmd = "C:\Users\logit\tools\apache-maven-3.9.9\bin\mvn.cmd"
    }
}
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Starting Fintel JavaFX Desktop App...  " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Set-Location -Path "$PSScriptRoot\fintel-client"
& $mavenCmd compile javafx:run
