# Deploy to AWS ECS Fargate - PowerShell Version
Write-Host "🚀 Deploying to AWS ECS Fargate" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$env:AWS_REGION = if ($env:AWS_REGION) { $env:AWS_REGION } else { "us-east-1" }
$PROJECT_NAME = "digitopia"
$CLUSTER_NAME = "$PROJECT_NAME-cluster"

# Get AWS Account ID
Write-Host "🔍 Detecting AWS Account..." -ForegroundColor Yellow
try {
    $AWS_ACCOUNT_ID = (aws sts get-caller-identity --query Account --output text 2>$null)
    if (-not $AWS_ACCOUNT_ID) {
        throw "AWS CLI not configured"
    }
} catch {
    Write-Host "❌ Error: AWS CLI not configured. Run 'aws configure' first." -ForegroundColor Red
    exit 1
}

Write-Host "✅ AWS Account: $AWS_ACCOUNT_ID" -ForegroundColor Green
Write-Host "✅ Region: $env:AWS_REGION" -ForegroundColor Green
Write-Host ""

# Step 1: Create ECR Repositories
Write-Host "📦 Step 1/5: Creating ECR repositories..." -ForegroundColor Yellow
$services = @("eureka-server", "api-gateway", "user-service", "organization-service", "invitation-service")

foreach ($service in $services) {
    Write-Host "   Creating repository for $service..." -ForegroundColor Gray
    $repoExists = aws ecr describe-repositories --repository-names "$PROJECT_NAME/$service" --region $env:AWS_REGION 2>$null
    if (-not $repoExists) {
        aws ecr create-repository --repository-name "$PROJECT_NAME/$service" --region $env:AWS_REGION | Out-Null
    }
}
Write-Host "✅ ECR repositories ready" -ForegroundColor Green
Write-Host ""

# Step 2: Build and Push Docker Images
Write-Host "📦 Step 2/5: Building and pushing Docker images..." -ForegroundColor Yellow
Write-Host "   Logging in to ECR..." -ForegroundColor Gray
$ecrPassword = aws ecr get-login-password --region $env:AWS_REGION
$ecrPassword | docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com"

Write-Host "   Building images..." -ForegroundColor Gray
docker-compose build

Write-Host "   Tagging and pushing images to ECR..." -ForegroundColor Gray
foreach ($service in $services) {
    Write-Host "   - Pushing $service..." -ForegroundColor Gray
    docker tag "${service}:latest" "$AWS_ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com/$PROJECT_NAME/${service}:latest"
    docker push "$AWS_ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com/$PROJECT_NAME/${service}:latest"
}
Write-Host "✅ All images pushed to ECR" -ForegroundColor Green
Write-Host ""

# Step 3: Create ECS Cluster
Write-Host "🏗️  Step 3/5: Creating ECS cluster..." -ForegroundColor Yellow
$clusterExists = aws ecs describe-clusters --clusters $CLUSTER_NAME --region $env:AWS_REGION 2>$null | Select-String "ACTIVE"
if (-not $clusterExists) {
    aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $env:AWS_REGION | Out-Null
}
Write-Host "✅ ECS cluster ready: $CLUSTER_NAME" -ForegroundColor Green
Write-Host ""

# Step 4: Create CloudWatch Log Groups
Write-Host "📊 Step 4/5: Creating CloudWatch log groups..." -ForegroundColor Yellow
foreach ($service in $services) {
    aws logs create-log-group --log-group-name "/ecs/$PROJECT_NAME-$service" --region $env:AWS_REGION 2>$null
}
Write-Host "✅ Log groups created" -ForegroundColor Green
Write-Host ""

# Step 5: Display Next Steps
Write-Host "✅ Step 5/5: Infrastructure ready!" -ForegroundColor Green
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "📋 NEXT STEPS - Manual Configuration Required:" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  Create RDS PostgreSQL Database:" -ForegroundColor White
Write-Host "   - Go to AWS Console → RDS → Create Database"
Write-Host "   - Engine: PostgreSQL"
Write-Host "   - Instance: db.t3.micro"
Write-Host "   - DB identifier: digitopia-postgres"
Write-Host "   - Master username: digitopia_admin"
Write-Host "   - Create databases: user_service_db, organization_service_db, invitation_service_db"
Write-Host ""
Write-Host "2️⃣  Create Amazon MSK (Kafka) Cluster:" -ForegroundColor White
Write-Host "   - Go to AWS Console → Amazon MSK → Create cluster"
Write-Host "   - Cluster name: digitopia-kafka"
Write-Host "   - Kafka version: 3.5.1"
Write-Host "   - Broker type: kafka.t3.small"
Write-Host "   - Number of brokers: 2"
Write-Host ""
Write-Host "3️⃣  Create Application Load Balancer:" -ForegroundColor White
Write-Host "   - Go to AWS Console → EC2 → Load Balancers → Create"
Write-Host "   - Type: Application Load Balancer"
Write-Host "   - Name: digitopia-alb"
Write-Host "   - Listeners: HTTP:80"
Write-Host "   - Create target groups for each service"
Write-Host ""
Write-Host "4️⃣  Create VPC & Security Groups:" -ForegroundColor White
Write-Host "   - Allow inbound traffic on required ports"
Write-Host "   - Fargate tasks: 8080-8084"
Write-Host "   - RDS: 5432"
Write-Host "   - Kafka: 9092"
Write-Host "   - ALB: 80, 443"
Write-Host ""
Write-Host "5️⃣  Create ECS Task Definitions:" -ForegroundColor White
Write-Host "   Use the AWS Console or see AWS_ECS_FARGATE_DEPLOYMENT.md"
Write-Host ""
Write-Host "6️⃣  Create ECS Services:" -ForegroundColor White
Write-Host "   Use the AWS Console or see AWS_ECS_FARGATE_DEPLOYMENT.md"
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""
Write-Host "📚 For detailed instructions, see: AWS_ECS_FARGATE_DEPLOYMENT.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎉 Your Docker images are now in ECR at:" -ForegroundColor Green
Write-Host "   $AWS_ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com/$PROJECT_NAME/" -ForegroundColor White
Write-Host ""
