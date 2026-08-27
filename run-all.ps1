Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Launching Fintel Complete System...    " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan

$serverScript = "$PSScriptRoot\run-server.ps1"
$clientScript = "$PSScriptRoot\run-client.ps1"

# Launch Backend in new window
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$serverScript`""

Write-Host "Waiting 4 seconds for Spring Boot backend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 4

# Launch Frontend in new window
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$clientScript`""

Write-Host "Both Backend and Frontend have been launched successfully!" -ForegroundColor Green
