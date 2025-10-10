# Simple ECS Deployment Script - No Copilot Required
# This script deploys your microservices to AWS ECS using your existing docker-compose.yml

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   Digitopia ECS Deployment (Simple Method)      " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$REGION = "us-east-1"
$CLUSTER_NAME = "digitopia-cluster"
$ECR_REPO_PREFIX = "digitopia"

# Check AWS credentials
Write-Host "[1/6] Checking AWS credentials..." -ForegroundColor Yellow
$awsIdentity = aws sts get-caller-identity 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: AWS credentials not configured properly" -ForegroundColor Red
    Write-Host "Run: aws configure" -ForegroundColor Yellow
    exit 1
}
Write-Host "SUCCESS: AWS credentials valid" -ForegroundColor Green
Write-Host ""

# Create ECR repositories
Write-Host "[2/6] Creating ECR repositories..." -ForegroundColor Yellow
$services = @("eureka-server", "api-gateway", "user-service", "organization-service", "invitation-service")
foreach ($service in $services) {
    $repoName = "$ECR_REPO_PREFIX/$service"
    Write-Host "  Creating repository: $repoName" -ForegroundColor Cyan
    aws ecr create-repository --repository-name $repoName --region $REGION 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  SUCCESS: Repository created" -ForegroundColor Green
    } else {
        Write-Host "  INFO: Repository already exists (OK)" -ForegroundColor Gray
    }
}
Write-Host ""

# Get AWS account ID
Write-Host "[3/6] Getting AWS account details..." -ForegroundColor Yellow
$accountId = (aws sts get-caller-identity --query Account --output text)
$ECR_REGISTRY = "$accountId.dkr.ecr.$REGION.amazonaws.com"
Write-Host "  Account ID: $accountId" -ForegroundColor Cyan
Write-Host "  ECR Registry: $ECR_REGISTRY" -ForegroundColor Cyan
Write-Host ""

# Login to ECR
Write-Host "[4/6] Logging into ECR..." -ForegroundColor Yellow
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to login to ECR" -ForegroundColor Red
    exit 1
}
Write-Host "SUCCESS: Logged into ECR" -ForegroundColor Green
Write-Host ""

# Build and push Docker images
Write-Host "[5/6] Building and pushing Docker images..." -ForegroundColor Yellow
foreach ($service in $services) {
    Write-Host "  Building $service..." -ForegroundColor Cyan
    
    # Build the image from root directory with context
    docker build -f ./$service/Dockerfile -t "$ECR_REPO_PREFIX/$service`:latest" .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ERROR: Failed to build $service" -ForegroundColor Red
        exit 1
    }
    
    # Tag for ECR
    docker tag "$ECR_REPO_PREFIX/$service`:latest" "$ECR_REGISTRY/$ECR_REPO_PREFIX/$service`:latest"
    
    # Push to ECR
    Write-Host "  Pushing $service to ECR..." -ForegroundColor Cyan
    docker push "$ECR_REGISTRY/$ECR_REPO_PREFIX/$service`:latest"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ERROR: Failed to push $service" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "  SUCCESS: $service pushed to ECR" -ForegroundColor Green
}
Write-Host ""

# Create ECS cluster
Write-Host "[6/6] Creating ECS cluster..." -ForegroundColor Yellow
aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $REGION 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS: Cluster created" -ForegroundColor Green
} else {
    Write-Host "INFO: Cluster already exists (OK)" -ForegroundColor Gray
}
Write-Host ""

Write-Host "==================================================" -ForegroundColor Green
Write-Host "   Docker Images Successfully Pushed to ECR!     " -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Create ECS Task Definitions (we'll do this next)" -ForegroundColor White
Write-Host "2. Create ECS Services" -ForegroundColor White
Write-Host "3. Deploy to Fargate" -ForegroundColor White
Write-Host ""
Write-Host "Your images are at:" -ForegroundColor Yellow
foreach ($service in $services) {
    Write-Host "  $ECR_REGISTRY/$ECR_REPO_PREFIX/$service`:latest" -ForegroundColor Cyan
}
