# Script to stop Qronis frontend and backend services
Write-Host "Stopping Vite Frontend (Port 5173)..."
$frontendPids = Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($frontendPids) {
    Stop-Process -Id $frontendPids -Force
    Write-Host "Frontend stopped." -ForegroundColor Green
} else {
    Write-Host "Frontend is not running." -ForegroundColor Yellow
}

Write-Host "Stopping Spring Boot Backend (Port 8080)..."
$backendPids = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($backendPids) {
    Stop-Process -Id $backendPids -Force
    Write-Host "Backend stopped." -ForegroundColor Green
} else {
    Write-Host "Backend is not running." -ForegroundColor Yellow
}
