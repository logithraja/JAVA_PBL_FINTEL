# Fintel Client Packaging Script
# Uses Maven Shade + JDK jpackage to generate a native standalone application bundle / installer.

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$clientDir = Join-Path $scriptDir "fintel-client"
$targetDir = Join-Path $clientDir "target"
$distDir = Join-Path $clientDir "dist"

Write-Host "==> Packaging Fintel Client (Fat JAR)..." -ForegroundColor Cyan
Push-Location $clientDir
try {
    mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw "Maven build failed."
    }
} finally {
    Pop-Location
}

$jarFile = Join-Path $targetDir "fintel-client-1.0-SNAPSHOT.jar"
if (-not (Test-Path $jarFile)) {
    throw "Client JAR not found at $jarFile"
}

Write-Host "==> Shaded client executable JAR created at: $jarFile" -ForegroundColor Green

# Check if jpackage is available in current PATH or JAVA_HOME
$jpackageCmd = Get-Command "jpackage" -ErrorAction SilentlyContinue
if (-not $jpackageCmd -and $env:JAVA_HOME) {
    $potentialJpackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
    if (Test-Path $potentialJpackage) {
        $jpackageCmd = $potentialJpackage
    }
}

if ($jpackageCmd) {
    Write-Host "==> Found jpackage ($jpackageCmd). Building native application image..." -ForegroundColor Cyan
    if (Test-Path $distDir) {
        Remove-Item -Recurse -Force $distDir
    }
    New-Item -ItemType Directory -Force -Path $distDir | Out-Null

    & $jpackageCmd `
        --type app-image `
        --input $targetDir `
        --dest $distDir `
        --name "Fintel" `
        --main-jar "fintel-client-1.0-SNAPSHOT.jar" `
        --main-class "org.example.AppLauncher"

    Write-Host "==> Native application image created in: $distDir\Fintel" -ForegroundColor Green
} else {
    Write-Host "==> Note: 'jpackage' not found in PATH or JAVA_HOME. Run using: java -jar `"$jarFile`"" -ForegroundColor Yellow
}
