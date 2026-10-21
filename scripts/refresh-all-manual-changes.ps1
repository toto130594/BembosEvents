param(
    [switch]$NoCache,
    [switch]$SkipLocalBuilds,
    [switch]$SkipSmokes
)

& {
    $ErrorActionPreference = "Stop"

    $root = "D:\Proyectos\Claude\BembosEvents"
    $frontendProject = Join-Path $root "frontend\BembosEvents.Web.csproj"
    $backendDir = Join-Path $root "backend"
    $backendPom = Join-Path $backendDir "pom.xml"

    $outputDir = Join-Path $root "local-output"
    $outputFile = Join-Path $outputDir "refresh-all-manual-changes-v3.txt"

    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null

    $transcriptStarted = $false

    function Write-Section {
        param([string]$Title)

        Write-Output ""
        Write-Output "============================================================"
        Write-Output (" {0}" -f $Title)
        Write-Output "============================================================"
    }

    function Show-ServiceLogs {
        param(
            [string]$Service,
            [int]$Tail = 160
        )

        Write-Output ""
        Write-Output ("=== {0} LOGS LAST {1} LINES ===" -f $Service.ToUpper(), $Tail)

        try {
            docker compose logs --tail=$Tail --no-color $Service
        }
        catch {
            Write-Output ("No se pudieron leer logs de {0}: {1}" -f $Service, $_.Exception.Message)
        }
    }

    function Get-PublishedPort {
        param(
            [string]$Service,
            [int]$ContainerPort
        )

        try {
            $containerId = (docker compose ps -q $Service).Trim()

            if ([string]::IsNullOrWhiteSpace($containerId)) {
                return $null
            }

            foreach ($line in @(docker port $containerId "$ContainerPort/tcp")) {
                $match = [regex]::Match(
                    [string]$line,
                    ':(?<port>\d+)\s*$'
                )

                if ($match.Success) {
                    return [int]$match.Groups["port"].Value
                }
            }
        }
        catch {
            return $null
        }

        return $null
    }

    function Invoke-Get {
        param(
            [string]$Uri,
            [int]$TimeoutSec = 20
        )

        return Invoke-WebRequest `
            -Uri $Uri `
            -UseBasicParsing `
            -TimeoutSec $TimeoutSec `
            -MaximumRedirection 5 `
            -ErrorAction Stop
    }

    function Wait-BackendReady {
        param(
            [int]$Attempts = 60,
            [int]$DelaySeconds = 2
        )

        Write-Section "WAIT FOR BACKEND READY"

        for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
            try {
                $containerId = (docker compose ps -q backend).Trim()

                if ([string]::IsNullOrWhiteSpace($containerId)) {
                    Write-Output ("BACKEND WAIT {0}/{1}: container not created yet" -f $attempt, $Attempts)
                    Start-Sleep -Seconds $DelaySeconds
                    continue
                }

                $state = (docker inspect -f '{{.State.Status}}' $containerId).Trim()

                if ($state -eq "exited" -or $state -eq "dead") {
                    Show-ServiceLogs -Service "backend" -Tail 200
                    throw ("Backend container state: {0}" -f $state)
                }

                $logs = [string](docker compose logs --tail=220 --no-color backend)

                if (
                    $logs.IndexOf(
                        "APPLICATION FAILED TO START",
                        [System.StringComparison]::OrdinalIgnoreCase
                    ) -ge 0
                ) {
                    Show-ServiceLogs -Service "backend" -Tail 220
                    throw "Spring Boot reporto APPLICATION FAILED TO START."
                }

                if (
                    $logs.IndexOf(
                        "Started BembosEventsApplication",
                        [System.StringComparison]::OrdinalIgnoreCase
                    ) -ge 0
                ) {
                    Write-Output ("BACKEND READY: attempt {0}, state {1}" -f $attempt, $state)
                    return
                }

                Write-Output ("BACKEND WAIT {0}/{1}: state {2}, Spring Boot still starting..." -f $attempt, $Attempts, $state)
            }
            catch {
                if (
                    $_.Exception.Message -like "*APPLICATION FAILED*" -or
                    $_.Exception.Message -like "*container state*"
                ) {
                    throw
                }

                Write-Output ("BACKEND WAIT {0}/{1}: {2}" -f $attempt, $Attempts, $_.Exception.Message)
            }

            Start-Sleep -Seconds $DelaySeconds
        }

        Show-ServiceLogs -Service "backend" -Tail 220
        throw "Backend no llego a estado READY dentro del tiempo esperado."
    }

    function Wait-FrontendReady {
        param(
            [int]$Attempts = 30,
            [int]$DelaySeconds = 2
        )

        Write-Section "WAIT FOR FRONTEND READY"

        for ($attempt = 1; $attempt -le $Attempts; $attempt++) {
            try {
                $frontendPort = Get-PublishedPort -Service "frontend" -ContainerPort 8080

                if ($null -eq $frontendPort) {
                    Write-Output ("FRONTEND WAIT {0}/{1}: published port not ready" -f $attempt, $Attempts)
                    Start-Sleep -Seconds $DelaySeconds
                    continue
                }

                $baseUrl = "http://localhost:{0}" -f $frontendPort
                $response = Invoke-Get -Uri ($baseUrl + "/") -TimeoutSec 10

                if ($response.StatusCode -eq 200) {
                    Write-Output (
                        "FRONTEND READY: attempt {0}, HTTP 200, port {1}" -f
                        $attempt,
                        $frontendPort
                    )

                    return @{
                        BaseUrl = $baseUrl
                        Port = $frontendPort
                    }
                }

                Write-Output (
                    "FRONTEND WAIT {0}/{1}: HTTP {2}" -f
                    $attempt,
                    $Attempts,
                    $response.StatusCode
                )
            }
            catch {
                Write-Output (
                    "FRONTEND WAIT {0}/{1}: {2}" -f
                    $attempt,
                    $Attempts,
                    $_.Exception.Message
                )
            }

            Start-Sleep -Seconds $DelaySeconds
        }

        Show-ServiceLogs -Service "frontend" -Tail 180
        throw "Frontend no respondio HTTP 200 dentro del tiempo esperado."
    }

    function Build-FrontendLocal {
        if (-not (Test-Path $frontendProject)) {
            Write-Output "FRONTEND LOCAL BUILD: SKIPPED - csproj no encontrado."
            return
        }

        Write-Section "LOCAL BUILD - FRONTEND"

        & dotnet build ".\frontend\BembosEvents.Web.csproj" -c Release --nologo

        if ($LASTEXITCODE -ne 0) {
            throw "dotnet build frontend fallo."
        }

        Write-Output "FRONTEND RELEASE BUILD: OK"
    }

    function Build-BackendLocalIfAvailable {
        if (-not (Test-Path $backendPom)) {
            Write-Output "BACKEND LOCAL BUILD: SKIPPED - pom.xml no encontrado."
            return
        }

        Write-Section "LOCAL BUILD - BACKEND"

        $mvnwCmd = Join-Path $backendDir "mvnw.cmd"
        $mvnw = Join-Path $backendDir "mvnw"
        $mvnCommand = Get-Command mvn -ErrorAction SilentlyContinue

        Push-Location $backendDir

        try {
            if (Test-Path $mvnwCmd) {
                Write-Output "MAVEN SOURCE: mvnw.cmd"
                & $mvnwCmd clean package -DskipTests
            }
            elseif (Test-Path $mvnw) {
                Write-Output "MAVEN SOURCE: mvnw"
                & $mvnw clean package -DskipTests
            }
            elseif ($null -ne $mvnCommand) {
                Write-Output ("MAVEN SOURCE: {0}" -f $mvnCommand.Source)
                & $mvnCommand.Source clean package -DskipTests
            }
            else {
                Write-Output "BACKEND LOCAL BUILD: SKIPPED"
                Write-Output "Maven/mvnw no esta disponible en Windows."
                Write-Output "El backend se compilara dentro de Docker Compose."
                return
            }

            if ($LASTEXITCODE -ne 0) {
                throw "Maven backend build fallo."
            }

            Write-Output "BACKEND PACKAGE BUILD: OK"
        }
        finally {
            Pop-Location
        }
    }

    try {
        if (-not (Test-Path $root)) {
            throw "Root no encontrado: $root"
        }

        Set-Location $root

        Start-Transcript -Path $outputFile -Force | Out-Null
        $transcriptStarted = $true

        Write-Section "BEMBOS EVENTS - GLOBAL REFRESH V3"

        Write-Output "PURPOSE:"
        Write-Output "- Refrescar TODO despues de cambios manuales"
        Write-Output "- Compilar frontend local"
        Write-Output "- Compilar backend local solo si Maven/mvnw existe"
        Write-Output "- Reconstruir TODAS las imagenes de Docker Compose"
        Write-Output "- Recrear TODOS los servicios"
        Write-Output "- ESPERAR backend Spring Boot antes de probar frontend"
        Write-Output "- Mantener volumenes y datos"
        Write-Output "- NO docker compose down -v"
        Write-Output "- NO modificar codigo fuente"
        Write-Output "- NO commit"
        Write-Output "- NO push"
        Write-Output ""
        Write-Output ("NO CACHE: {0}" -f $NoCache.IsPresent)
        Write-Output ("SKIP LOCAL BUILDS: {0}" -f $SkipLocalBuilds.IsPresent)
        Write-Output ("SKIP SMOKES: {0}" -f $SkipSmokes.IsPresent)

        Write-Section "GIT STATUS BEFORE REFRESH"

        try {
            git status --short
        }
        catch {
            Write-Output ("Git status warning: {0}" -f $_.Exception.Message)
        }

        Write-Section "DOCKER COMPOSE SERVICES"

        $services = @(docker compose config --services)

        if ($LASTEXITCODE -ne 0 -or $services.Count -eq 0) {
            throw "No se pudieron detectar servicios de Docker Compose."
        }

        foreach ($service in $services) {
            Write-Output ("- {0}" -f $service)
        }

        if (-not ($services -contains "backend")) {
            throw "docker compose no contiene el servicio 'backend'."
        }

        if (-not ($services -contains "frontend")) {
            throw "docker compose no contiene el servicio 'frontend'."
        }

        if (-not $SkipLocalBuilds.IsPresent) {
            Build-FrontendLocal
            Build-BackendLocalIfAvailable
        }
        else {
            Write-Section "LOCAL BUILDS"
            Write-Output "SKIPPED"
        }

        Write-Section "DOCKER COMPOSE BUILD - ALL"

        if ($NoCache.IsPresent) {
            Write-Output "Running: docker compose build --no-cache"
            & docker compose build --no-cache
        }
        else {
            Write-Output "Running: docker compose build"
            & docker compose build
        }

        if ($LASTEXITCODE -ne 0) {
            throw "docker compose build fallo."
        }

        Write-Output "DOCKER COMPOSE BUILD ALL: OK"

        Write-Section "DOCKER COMPOSE RECREATE - ALL"

        & docker compose up -d --force-recreate

        if ($LASTEXITCODE -ne 0) {
            throw "docker compose up -d --force-recreate fallo."
        }

        Write-Output "DOCKER COMPOSE RECREATE ALL: OK"

        Write-Section "DOCKER COMPOSE STATUS"

        docker compose ps

        if ($LASTEXITCODE -ne 0) {
            throw "docker compose ps fallo."
        }

        Wait-BackendReady

        Start-Sleep -Seconds 2

        $frontendState = Wait-FrontendReady
        $baseUrl = [string]$frontendState.BaseUrl

        if (-not $SkipSmokes.IsPresent) {
            Write-Section "FRONTEND ROUTE SMOKES"

            foreach ($route in @(
                "/",
                "/paquetes",
                "/tematicas",
                "/sedes",
                "/servicios"
            )) {
                $routeOk = $false

                for ($attempt = 1; $attempt -le 5; $attempt++) {
                    try {
                        $response = Invoke-Get `
                            -Uri ($baseUrl + $route) `
                            -TimeoutSec 20

                        Write-Output (
                            "{0}: HTTP {1} (attempt {2})" -f
                            $route,
                            $response.StatusCode,
                            $attempt
                        )

                        if (
                            $response.StatusCode -ge 200 -and
                            $response.StatusCode -lt 400
                        ) {
                            $routeOk = $true
                            break
                        }
                    }
                    catch {
                        Write-Output (
                            "{0}: retry {1}/5 - {2}" -f
                            $route,
                            $attempt,
                            $_.Exception.Message
                        )
                    }

                    Start-Sleep -Seconds 2
                }

                if (-not $routeOk) {
                    Show-ServiceLogs -Service "backend" -Tail 140
                    Show-ServiceLogs -Service "frontend" -Tail 180
                    throw ("Route smoke failed after retries: {0}" -f $route)
                }
            }
        }
        else {
            Write-Section "FRONTEND ROUTE SMOKES"
            Write-Output "SKIPPED"
        }

        Write-Section "GIT DIFF CHECK"

        git diff --check

        if ($LASTEXITCODE -ne 0) {
            throw "git diff --check fallo."
        }

        Write-Output "GIT DIFF CHECK: OK"

        Write-Section "GLOBAL REFRESH V3 COMPLETE"

        Write-Output ("PREVIEW: {0}" -f $baseUrl)
        Write-Output "BACKEND SPRING BOOT READY: YES"
        Write-Output "FRONTEND MANUAL CHANGES: REFRESHED"
        Write-Output "BACKEND MANUAL CHANGES: REFRESHED"
        Write-Output "DOCKER COMPOSE BUILD ALL: OK"
        Write-Output "DOCKER COMPOSE RECREATE ALL: OK"
        Write-Output "VOLUMES DELETED: NO"
        Write-Output "DATABASE RESET: NO"
        Write-Output "SOURCE FILES MODIFIED BY SCRIPT: NO"
        Write-Output "COMMIT: NO"
        Write-Output "PUSH: NO"
        Write-Output ("LOG: {0}" -f $outputFile)
        Write-Output ""
        Write-Output "Abre PREVIEW y usa Ctrl+F5 si el navegador conserva cache."
    }
    catch {
        Write-Output ""
        Write-Output "============================================================"
        Write-Output " GLOBAL REFRESH V3 FAILED"
        Write-Output "============================================================"
        Write-Output $_.Exception.Message

        Show-ServiceLogs -Service "backend" -Tail 180
        Show-ServiceLogs -Service "frontend" -Tail 180

        throw
    }
    finally {
        if ($transcriptStarted) {
            try {
                Stop-Transcript | Out-Null
            }
            catch {
            }
        }
    }
}
