$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $root 'backend'
$frontendDir = Join-Path $root 'frontend'

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
  Write-Host '[CampusSphere] Java was not found on PATH.' -ForegroundColor Red
  exit 1
}

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
  Write-Host '[CampusSphere] Node.js was not found on PATH.' -ForegroundColor Red
  exit 1
}

if (-not (Test-Path (Join-Path $backendDir 'mvnw.cmd'))) {
  Write-Host '[CampusSphere] Missing backend\\mvnw.cmd.' -ForegroundColor Red
  exit 1
}

if (-not (Test-Path (Join-Path $frontendDir 'package.json'))) {
  Write-Host '[CampusSphere] Missing frontend\\package.json.' -ForegroundColor Red
  exit 1
}

Write-Host '[CampusSphere] Backend URL: http://localhost:8080/api'
Write-Host '[CampusSphere] Frontend URL: http://localhost:5173'
Write-Host '[CampusSphere] Do not open frontend/index.html with Live Server.'

Start-Process -FilePath 'cmd.exe' -ArgumentList "/k cd /d `"$backendDir`" && call mvnw.cmd -Dspring-boot.run.profiles=local spring-boot:run" -WindowStyle Normal
Start-Process -FilePath 'cmd.exe' -ArgumentList "/k cd /d `"$frontendDir`" && if not exist node_modules (call npm install) && call npm run dev" -WindowStyle Normal
