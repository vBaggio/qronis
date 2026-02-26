param(
    [int[]]$Ports = @(5434, 8080, 5173),
    [string]$HostName = "localhost"
)

$allAlive = $true

foreach ($port in $Ports) {
    $result = Test-NetConnection -ComputerName $HostName -Port $port -WarningAction SilentlyContinue
    if (-not $result.TcpTestSucceeded) {
        Write-Host "FAIL: Port $port is offline." -ForegroundColor Red
        $allAlive = $false
    }
}

if ($allAlive) {
    Write-Host "SUCCESS: All foundations ($($Ports -join ', ')) are ready on $HostName." -ForegroundColor Green
    exit 0
} else {
    Write-Host "Please start the missing services before proceeding." -ForegroundColor Yellow
    exit 1
}
