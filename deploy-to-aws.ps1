# AWS Deployment Script for Digitopia Invitation System (PowerShell)
# This script deploys the application to AWS Elastic Beanstalk

$ErrorActionPreference = "Stop"

Write-Host "🚀 Deploying Digitopia Invitation System to AWS..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "📋 Checking prerequisites..." -ForegroundColor Yellow

# Check AWS CLI
try {
    $null = aws --version
    Write-Host "✅ AWS CLI found" -ForegroundColor Green
} catch {
    Write-Host "❌ AWS CLI not found" -ForegroundColor Red
    Write-Host "Install with: pip install awscli" -ForegroundColor Yellow
    exit 1
}

# Check EB CLI
try {
    $null = eb --version
    Write-Host "✅ Elastic Beanstalk CLI found" -ForegroundColor Green
} catch {
    Write-Host "❌ Elastic Beanstalk CLI not found" -ForegroundColor Red
    Write-Host "Install with: pip install awsebcli" -ForegroundColor Yellow
    exit 1
}

# Check Docker
try {
    $null = docker --version
    Write-Host "✅ Docker found" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker not found" -ForegroundColor Red
    Write-Host "Install Docker from: https://www.docker.com/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Check AWS credentials
Write-Host "🔐 Verifying AWS credentials..." -ForegroundColor Yellow
try {
    $accountId = aws sts get-caller-identity --query Account --output text
    Write-Host "✅ Authenticated as AWS Account: $accountId" -ForegroundColor Green
} catch {
    Write-Host "❌ AWS credentials not configured" -ForegroundColor Red
    Write-Host "Run: aws configure" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Build Docker images
Write-Host "📦 Building Docker images..." -ForegroundColor Yellow
try {
    docker-compose build
    Write-Host "✅ Docker images built successfully" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to build Docker images" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Initialize Elastic Beanstalk (if not already done)
if (-Not (Test-Path ".elasticbeanstalk")) {
    Write-Host "🎯 Initializing Elastic Beanstalk..." -ForegroundColor Yellow
    eb init -p "Multi-container Docker" digitopia-invitation-system --region us-east-1
    Write-Host "✅ Elastic Beanstalk initialized" -ForegroundColor Green
} else {
    Write-Host "⚠️  Elastic Beanstalk already initialized" -ForegroundColor Yellow
}
Write-Host ""

# Check if environment exists
Write-Host "🔍 Checking for existing environment..." -ForegroundColor Yellow
$ebList = eb list 2>&1
if ($ebList -match "digitopia-prod") {
    Write-Host "♻️  Environment 'digitopia-prod' exists, updating..." -ForegroundColor Yellow
    
    # Deploy update
    Write-Host "📤 Deploying update to existing environment..." -ForegroundColor Yellow
    try {
        eb deploy digitopia-prod
        Write-Host "✅ Deployment successful" -ForegroundColor Green
    } catch {
        Write-Host "❌ Deployment failed" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "🆕 Creating new environment 'digitopia-prod'..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "⚠️  You will need to configure environment variables after creation:" -ForegroundColor Yellow
    Write-Host "   - SPRING_DATASOURCE_URL"
    Write-Host "   - SPRING_DATASOURCE_USERNAME"
    Write-Host "   - SPRING_DATASOURCE_PASSWORD"
    Write-Host "   - KAFKA_BOOTSTRAP_SERVERS"
    Write-Host "   - AWS_COGNITO_USER_POOL_ID"
    Write-Host "   - AWS_COGNITO_CLIENT_ID"
    Write-Host ""
    
    # Create environment
    try {
        eb create digitopia-prod --instance-type t3.medium
        Write-Host "✅ Environment created successfully" -ForegroundColor Green
    } catch {
        Write-Host "❌ Failed to create environment" -ForegroundColor Red
        exit 1
    }
}
Write-Host ""

# Show deployment results
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "✅ Deployment Complete!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Get environment info
Write-Host "📊 Environment Information:" -ForegroundColor Yellow
eb status | Select-String -Pattern "Environment Name|CNAME|Status|Health"
Write-Host ""

# Get application URL
$statusOutput = eb status
$appUrl = ($statusOutput | Select-String -Pattern "CNAME:").ToString() -replace ".*CNAME:\s*", ""
if ($appUrl) {
    Write-Host "🌐 Application URL: http://$appUrl" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  Application URL not available yet" -ForegroundColor Yellow
}
Write-Host ""

# Provide next steps
Write-Host "📝 Next Steps:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Configure environment variables:"
Write-Host "   eb setenv SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/digitopia"
Write-Host ""
Write-Host "2. View application logs:"
Write-Host "   eb logs"
Write-Host ""
Write-Host "3. Check application health:"
Write-Host "   eb health"
Write-Host ""
Write-Host "4. Open application in browser:"
Write-Host "   eb open"
Write-Host ""
Write-Host "5. SSH into instance (if needed):"
Write-Host "   eb ssh"
Write-Host ""

Write-Host "🎉 Deployment script completed successfully!" -ForegroundColor Green
