# ============================================================
#  run-tests.ps1  -  Ejecuta todos los tests del backend
#  Uso: .\run-tests.ps1
# ============================================================

$ROOT = $PSScriptRoot

$SERVICES = @(
    "api-gateway",
    "desigeo-ai-agent-service",
    "desigeo-analytics-service",
    "desigeo-auth-service",
    "desigeo-notification-service",
    "desigeo-report-service",
    "desigeo-support-service",
    "gestion-de-usuarios"
)

function Green($msg)  { Write-Host $msg -ForegroundColor Green }
function Red($msg)    { Write-Host $msg -ForegroundColor Red }
function Yellow($msg) { Write-Host $msg -ForegroundColor Yellow }
function Cyan($msg)   { Write-Host $msg -ForegroundColor Cyan }

$startTime = Get-Date
$startStr  = $startTime.ToString('yyyy-MM-dd HH:mm:ss')

Cyan ""
Cyan "============================================================"
Cyan "  DESIGEO - Test Suite"
Cyan "  Inicio: $startStr"
Cyan "============================================================"

$results    = @()
$totalRun   = 0
$totalFail  = 0
$totalError = 0
$totalSkip  = 0

foreach ($svc in $SERVICES) {
    $svcPath = [IO.Path]::Combine($ROOT, $svc)
    $mvnw    = [IO.Path]::Combine($svcPath, "mvnw.cmd")

    if (-not (Test-Path $svcPath)) {
        Yellow "  [SKIP] $svc - directorio no encontrado"
        $results += [PSCustomObject]@{ Service=$svc; Status="SKIP"; Run=0; Failures=0; Errors=0; Skipped=0; Summary="Directorio no encontrado" }
        continue
    }

    if (-not (Test-Path $mvnw)) {
        Yellow "  [SKIP] $svc - mvnw.cmd no encontrado"
        $results += [PSCustomObject]@{ Service=$svc; Status="SKIP"; Run=0; Failures=0; Errors=0; Skipped=0; Summary="mvnw.cmd no encontrado" }
        continue
    }

    Write-Host ""
    Write-Host "  Ejecutando: " -NoNewline
    Cyan $svc

    Push-Location $svcPath
    $output   = & $mvnw test 2>&1
    $exitCode = $LASTEXITCODE
    Pop-Location

    $summaryLine = $output | Select-String "Tests run: \d+" | Select-Object -Last 1
    $summary     = if ($summaryLine) { $summaryLine.Line.Trim() } else { "Sin datos de tests" }

    $run = 0; $fail = 0; $err = 0; $skip = 0
    if ($summary -match "Tests run: (\d+)") { $run  = [int]$Matches[1] }
    if ($summary -match "Failures: (\d+)")  { $fail = [int]$Matches[1] }
    if ($summary -match "Errors: (\d+)")    { $err  = [int]$Matches[1] }
    if ($summary -match "Skipped: (\d+)")   { $skip = [int]$Matches[1] }

    $totalRun   += $run
    $totalFail  += $fail
    $totalError += $err
    $totalSkip  += $skip

    $status = if ($fail -eq 0 -and $err -eq 0 -and $run -gt 0) { "PASS" } else { "FAIL" }

    $results += [PSCustomObject]@{ Service=$svc; Status=$status; Run=$run; Failures=$fail; Errors=$err; Skipped=$skip; Summary=$summary }

    if ($status -eq "PASS") {
        Green "  [PASS] $summary"
    } else {
        Red   "  [FAIL] $summary"
        $output | Select-String "\[ERROR\]" | Select-Object -First 5 | ForEach-Object {
            Red "         $($_.Line.Trim())"
        }
    }
}

$endTime  = Get-Date
$endStr   = $endTime.ToString('yyyy-MM-dd HH:mm:ss')
$duration = $endTime - $startTime
$durMin   = [math]::Round($duration.TotalMinutes, 1)
$durSec   = [math]::Round($duration.TotalSeconds)

# ── Resumen final ─────────────────────────────────────────────
Write-Host ""
Cyan "============================================================"
Cyan "  RESUMEN FINAL"
Cyan "============================================================"
Write-Host ""

$maxLen = ($SERVICES | ForEach-Object { $_.Length } | Measure-Object -Maximum).Maximum

foreach ($r in $results) {
    $pad     = $r.Service.PadRight($maxLen + 2)
    $runLine = "Tests run: $($r.Run), Failures: $($r.Failures), Errors: $($r.Errors), Skipped: $($r.Skipped)"
    switch ($r.Status) {
        "PASS" { Green  "  [PASS]  $pad  $runLine" }
        "SKIP" { Yellow "  [SKIP]  $pad  $($r.Summary)" }
        "FAIL" { Red    "  [FAIL]  $pad  $runLine" }
    }
}

$failCount   = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
$totalLine   = "  TOTAL    Tests run: $totalRun, Failures: $totalFail, Errors: $totalError, Skipped: $totalSkip"
$durLine     = "  Duracion: $durMin min - $durSec s"

Write-Host ""
Cyan "------------------------------------------------------------"

if ($failCount -eq 0) {
    Green  $totalLine
    Green  "  BUILD SUCCESS"
} else {
    Red    $totalLine
    Red    "  BUILD FAILURE - $failCount servicio(s) con errores"
}

Cyan "------------------------------------------------------------"
Cyan "  Inicio:  $startStr"
Cyan "  Fin:     $endStr"
Cyan $durLine
Cyan "============================================================"
Write-Host ""

if ($failCount -gt 0) { exit 1 } else { exit 0 }
