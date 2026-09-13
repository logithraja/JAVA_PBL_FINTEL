Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Launching Fintel Complete System...    " -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan

$serverScript = "$PSScriptRoot\run-server.ps1"
$clientScript = "$PSScriptRoot\run-client.ps1"

# Launch Backend in new window
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$serverScript`""

Write-Host "Waiting for Spring Boot backend to initialize and become ready..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$ready = $false

while ($attempt -lt $maxAttempts) {
    Start-Sleep -Seconds 1
    $attempt++
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2 -ErrorAction Stop
        if ($response.status -eq "UP") {
            $ready = $true
            break
        }
    } catch {
        # Still booting up
    }
}

if ($ready) {
    Write-Host "Backend is UP and healthy!" -ForegroundColor Green
} else {
    Write-Host "Backend is still initializing; launching client..." -ForegroundColor Yellow
}

# Launch Frontend in new window
Start-Process powershell -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$clientScript`""

Write-Host "Both Backend and Frontend have been launched successfully!" -ForegroundColor Green
