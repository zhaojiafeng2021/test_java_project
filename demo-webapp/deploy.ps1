#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Deploy demo-webapp from GitHub with JaCoCo tcpserver agent

.DESCRIPTION
    1. Clone/pull demo-webapp branch from GitHub
    2. Compile with Maven
    3. Start with JaCoCo agent (tcpserver port 6300, app port 18080)

.PARAMETER Reset
    Stop existing process and remove deployment directory before deploy
#>

param(
    [switch]$Reset
)

$ErrorActionPreference = "Stop"

# Configuration
$REPO_URL = "https://github.com/zhaojiafeng2021/test_java_project.git"
$BRANCH = "demo-webapp"
$DEPLOY_DIR = "c:\Users\gg.zhao\tools\deployments\demo-webapp"
$MODULE_PATH = "demo-webapp"
$MAVEN = "C:\Users\gg.zhao\tools\maven\apache-maven-3.9.16\bin\mvn.cmd"
$JACOCO_AGENT = "c:\Users\gg.zhao\tools\jacoco\jacocoagent.jar"
$AGENT_PORT = 6300
$APP_PORT = 18080
$PID_FILE = "$DEPLOY_DIR\.pid"

function Write-Status {
    param([string]$Message)
    Write-Host "▸ $Message" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

# Stop existing process
if (Test-Path $PID_FILE) {
    $pid = Get-Content $PID_FILE -Raw | Out-String | ForEach-Object { $_.Trim() }
    if ($pid -match '^\d+$') {
        Write-Status "Stopping existing process (PID: $pid)..."
        try {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Start-Sleep -Milliseconds 500
            Write-Success "Process stopped"
        } catch {
            Write-Host "  Process not running or already stopped" -ForegroundColor Yellow
        }
    }
    Remove-Item $PID_FILE -ErrorAction SilentlyContinue
}

# Clean deployment directory if Reset
if ($Reset -and (Test-Path $DEPLOY_DIR)) {
    Write-Status "Removing deployment directory..."
    Remove-Item $DEPLOY_DIR -Recurse -Force
    Write-Success "Clean complete"
}

# Clone or pull repository
if (-not (Test-Path $DEPLOY_DIR)) {
    Write-Status "Cloning repository..."
    $parentDir = Split-Path $DEPLOY_DIR -Parent
    if (-not (Test-Path $parentDir)) {
        New-Item -ItemType Directory -Path $parentDir -Force | Out-Null
    }
    git clone --depth 1 --branch $BRANCH $REPO_URL $DEPLOY_DIR
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Clone failed"
        exit 1
    }
    Write-Success "Repository cloned"
} else {
    Write-Status "Pulling latest changes..."
    Push-Location $DEPLOY_DIR
    try {
        git fetch origin $BRANCH
        git reset --hard "origin/$BRANCH"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Pull failed"
            exit 1
        }
        Write-Success "Repository updated"
    } finally {
        Pop-Location
    }
}

# Compile with Maven
$buildPath = Join-Path $DEPLOY_DIR $MODULE_PATH
Write-Status "Compiling with Maven..."
Push-Location $buildPath
try {
    & $MAVEN -B -q clean compile
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Compilation failed"
        exit 1
    }
    Write-Success "Compilation complete"
} finally {
    Pop-Location
}

# Start with JaCoCo agent
Write-Status "Starting application with JaCoCo agent..."
$env:APP_PORT = $APP_PORT
$agentArg = "-javaagent:$JACOCO_AGENT=output=tcpserver,address=*,port=$AGENT_PORT"
$classPath = Join-Path $buildPath "target\classes"

$process = Start-Process -FilePath "java" `
    -ArgumentList $agentArg, "-cp", $classPath, "com.example.demo.App" `
    -WorkingDirectory $buildPath `
    -PassThru `
    -WindowStyle Hidden

# Save PID
$process.Id | Out-File -FilePath $PID_FILE -Encoding utf8 -NoNewline

Write-Success "Application started (PID: $($process.Id))"
Write-Host ""
Write-Host "Application URL:  http://localhost:$APP_PORT" -ForegroundColor White
Write-Host "JaCoCo Agent:     localhost:$AGENT_PORT" -ForegroundColor White
Write-Host "PID file:         $PID_FILE" -ForegroundColor White
Write-Host ""
Write-Host "To stop: " -NoNewline -ForegroundColor Gray
Write-Host "Stop-Process -Id $($process.Id)" -ForegroundColor White
