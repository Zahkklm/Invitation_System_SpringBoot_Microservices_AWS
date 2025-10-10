# AWS Copilot Quick Deploy - PowerShell
Write-Host "🚀 AWS Copilot Quick Deploy" -ForegroundColor Cyan
Write-Host "============================" -ForegroundColor Cyan
Write-Host ""

# Check if Copilot is installed
$copilotExists = Get-Command copilot -ErrorAction SilentlyContinue
if (-not $copilotExists) {
    Write-Host "📥 Installing AWS Copilot..." -ForegroundColor Yellow
    $copilotUrl = "https://github.com/aws/copilot-cli/releases/latest/download/copilot-windows.exe"
    $destination = "$env:USERPROFILE\AppData\Local\Microsoft\WindowsApps\copilot.exe"
    
    Invoke-WebRequest -Uri $copilotUrl -OutFile $destination
    Write-Host "✅ Copilot installed!" -ForegroundColor Green
} else {
    Write-Host "✅ Copilot already installed" -ForegroundColor Green
}

Write-Host ""
Write-Host "🏗️  Initializing Digitopia application..." -ForegroundColor Yellow

# Initialize app (only if not already initialized)
if (-not (Test-Path "copilot")) {
    copilot app init digitopia
    Write-Host "✅ Application initialized" -ForegroundColor Green
} else {
    Write-Host "✅ Application already initialized" -ForegroundColor Green
}

Write-Host ""
Write-Host "🌍 Creating production environment..." -ForegroundColor Yellow

# Check if environment exists
$envList = copilot env ls 2>$null | Out-String
if ($envList -notmatch "production") {
    copilot env init --name production --profile default --default-config
    copilot env deploy --name production
    Write-Host "✅ Environment created" -ForegroundColor Green
} else {
    Write-Host "✅ Environment already exists" -ForegroundColor Green
}

Write-Host ""
Write-Host "📦 Initializing services..." -ForegroundColor Yellow

# Function to initialize a service
function Initialize-Service {
    param(
        [string]$ServiceName,
        [string]$ServiceType,
        [string]$Dockerfile
    )
    
    Write-Host ""
    Write-Host "🔧 Setting up $ServiceName..." -ForegroundColor Cyan
    
    # Check if service manifest exists
    if (-not (Test-Path "copilot\$ServiceName\manifest.yml")) {
        copilot svc init --name $ServiceName --svc-type $ServiceType --dockerfile $Dockerfile
        Write-Host "✅ $ServiceName initialized" -ForegroundColor Green
    } else {
        Write-Host "✅ $ServiceName already initialized" -ForegroundColor Green
    }
}

# Initialize all services
Initialize-Service "eureka-server" "Load Balanced Web Service" "eureka-server/Dockerfile"
Initialize-Service "api-gateway" "Load Balanced Web Service" "api-gateway/Dockerfile"
Initialize-Service "user-service" "Backend Service" "user-service/Dockerfile"
Initialize-Service "organization-service" "Backend Service" "organization-service/Dockerfile"
Initialize-Service "invitation-service" "Backend Service" "invitation-service/Dockerfile"

Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "✅ Services initialized!" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Next Steps:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1️⃣  Deploy Eureka Server (service discovery):" -ForegroundColor White
Write-Host "   copilot svc deploy --name eureka-server" -ForegroundColor Gray
Write-Host ""
Write-Host "2️⃣  Deploy API Gateway:" -ForegroundColor White
Write-Host "   copilot svc deploy --name api-gateway" -ForegroundColor Gray
Write-Host ""
Write-Host "3️⃣  Deploy Backend Services:" -ForegroundColor White
Write-Host "   copilot svc deploy --name user-service" -ForegroundColor Gray
Write-Host "   copilot svc deploy --name organization-service" -ForegroundColor Gray
Write-Host "   copilot svc deploy --name invitation-service" -ForegroundColor Gray
Write-Host ""
Write-Host "🚀 Or deploy all at once:" -ForegroundColor Yellow
Write-Host "   copilot svc deploy --all" -ForegroundColor White
Write-Host ""
Write-Host "📊 View deployment status:" -ForegroundColor Yellow
Write-Host "   copilot svc status" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Get service URL:" -ForegroundColor Yellow
Write-Host "   copilot svc show --name api-gateway" -ForegroundColor White
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Tip: Copilot will automatically create:" -ForegroundColor Cyan
Write-Host "   - ECS Cluster with Fargate"
Write-Host "   - Application Load Balancer"
Write-Host "   - Service Discovery (Cloud Map)"
Write-Host "   - CloudWatch Logs"
Write-Host "   - Auto-scaling policies"
Write-Host ""
Write-Host "📚 Documentation: https://aws.github.io/copilot-cli/" -ForegroundColor Cyan
Write-Host ""
