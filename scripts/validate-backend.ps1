$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " VALIDACION COMPLETA BACKEND BEMBOS EVENTS" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$testPath = ".\backend\src\test\java"

$testFiles = Get-ChildItem `
    $testPath `
    -Recurse `
    -Filter "*Test.java" |
    Where-Object {
        $_.Name -ne "BembosEventsApplicationTests.java"
    }

$tests = ($testFiles.BaseName -join ",")

if ([string]::IsNullOrWhiteSpace($tests)) {
    Write-Host "NO SE ENCONTRARON TESTS" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Tests detectados:" -ForegroundColor Yellow
$testFiles.BaseName | ForEach-Object {
    Write-Host " - $_"
}

Write-Host ""
Write-Host "EJECUTANDO TESTS..." -ForegroundColor Cyan

docker run --rm `
    -v "${PWD}\backend:/app" `
    -w /app `
    maven:3.9.9-eclipse-temurin-21 `
    mvn clean "-Dtest=$tests" test -B

if ($LASTEXITCODE -ne 0) {

    Write-Host ""
    Write-Host "TESTS FALLARON" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "TESTS OK" -ForegroundColor Green

Write-Host ""
Write-Host "EJECUTANDO DOCKER BUILD..." -ForegroundColor Cyan

docker build `
    -t bembosevents-backend `
    ./backend

if ($LASTEXITCODE -ne 0) {

    Write-Host ""
    Write-Host "BUILD FALLIDO" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host " BACKEND VALIDADO CORRECTAMENTE" -ForegroundColor Green
Write-Host " Tests: OK" -ForegroundColor Green
Write-Host " Docker Build: OK" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green