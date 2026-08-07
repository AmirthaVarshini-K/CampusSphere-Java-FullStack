@echo off
setlocal enabledelayedexpansion

set "ROOT=%~dp0"
pushd "%ROOT%" >nul

where java >nul 2>&1
if errorlevel 1 (
  echo [CampusSphere] Java was not found on PATH.
  echo Install Java 21 before starting the backend.
  exit /b 1
)

where node >nul 2>&1
if errorlevel 1 (
  echo [CampusSphere] Node.js was not found on PATH.
  echo Install Node.js 18+ before starting the frontend.
  exit /b 1
)

if not exist "backend\mvnw.cmd" (
  echo [CampusSphere] Missing backend\mvnw.cmd.
  exit /b 1
)

if not exist "frontend\package.json" (
  echo [CampusSphere] Missing frontend\package.json.
  exit /b 1
)

echo [CampusSphere] Backend URL: http://localhost:8080/api
echo [CampusSphere] Frontend URL: http://localhost:5173
echo [CampusSphere] Do not open frontend\index.html with Live Server.
echo [CampusSphere] Starting separate windows...

start "CampusSphere Backend" cmd /k "cd /d \"%ROOT%backend\" && call mvnw.cmd -Dspring-boot.run.profiles=local spring-boot:run"
start "CampusSphere Frontend" cmd /k "cd /d \"%ROOT%frontend\" && if not exist node_modules (call npm install) && call npm run dev"

popd >nul
endlocal
