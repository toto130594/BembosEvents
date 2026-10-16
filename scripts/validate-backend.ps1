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

$dockerCommand = (
    'docker run --rm -v "{0}\backend:/app" -w /app ' +
    'maven:3.9.9-eclipse-temurin-21 ' +
    'mvn clean "-Dtest={1}" test -B 2>&1'
) -f $PWD.Path, $tests

& cmd.exe /d /s /c $dockerCommand

$dockerExitCode = $LASTEXITCODE

if ($dockerExitCode -ne 0) {

    Write-Host ""
    Write-Host "TESTS FALLARON" -ForegroundColor Red
    exit $dockerExitCode
}

Write-Host ""
Write-Host "TESTS OK" -ForegroundColor Green

Write-Host ""
Write-Host "EJECUTANDO DOCKER BUILD..." -ForegroundColor Cyan

$dockerBuildCommand = (
    'docker build -t bembosevents-backend "{0}\backend" 2>&1'
) -f $PWD.Path

& cmd.exe /d /s /c $dockerBuildCommand

$dockerBuildExitCode = $LASTEXITCODE

if ($dockerBuildExitCode -ne 0) {

    Write-Host ""
    Write-Host "BUILD FALLIDO" -ForegroundColor Red
    exit $dockerBuildExitCode
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host " BACKEND VALIDADO CORRECTAMENTE" -ForegroundColor Green
Write-Host " Tests: OK" -ForegroundColor Green
Write-Host " Docker Build: OK" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green