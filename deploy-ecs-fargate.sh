#!/bin/bash
set -e

echo "🚀 Deploying to AWS ECS Fargate"
echo "================================"
echo ""

# Configuration
export AWS_REGION=${AWS_REGION:-us-east-1}
export PROJECT_NAME="digitopia"
export CLUSTER_NAME="${PROJECT_NAME}-cluster"

# Get AWS Account ID
echo "🔍 Detecting AWS Account..."
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text 2>/dev/null)
if [ -z "$AWS_ACCOUNT_ID" ]; then
    echo "❌ Error: AWS CLI not configured. Run 'aws configure' first."
    exit 1
fi

echo "✅ AWS Account: $AWS_ACCOUNT_ID"
echo "✅ Region: $AWS_REGION"
echo ""

# Step 1: Create ECR Repositories
echo "📦 Step 1/5: Creating ECR repositories..."
for service in eureka-server api-gateway user-service organization-service invitation-service; do
    echo "   Creating repository for ${service}..."
    aws ecr describe-repositories --repository-names ${PROJECT_NAME}/${service} --region $AWS_REGION 2>/dev/null || \
    aws ecr create-repository --repository-name ${PROJECT_NAME}/${service} --region $AWS_REGION > /dev/null
done
echo "✅ ECR repositories ready"
echo ""

# Step 2: Build and Push Docker Images
echo "📦 Step 2/5: Building and pushing Docker images..."
echo "   Logging in to ECR..."
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

echo "   Building images..."
docker-compose build

echo "   Tagging and pushing images to ECR..."
for service in eureka-server api-gateway user-service organization-service invitation-service; do
    echo "   - Pushing ${service}..."
    docker tag ${service}:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${PROJECT_NAME}/${service}:latest
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${PROJECT_NAME}/${service}:latest
done
echo "✅ All images pushed to ECR"
echo ""

# Step 3: Create ECS Cluster
echo "🏗️  Step 3/5: Creating ECS cluster..."
aws ecs describe-clusters --clusters $CLUSTER_NAME --region $AWS_REGION 2>/dev/null | grep -q "ACTIVE" || \
aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $AWS_REGION > /dev/null
echo "✅ ECS cluster ready: $CLUSTER_NAME"
echo ""

# Step 4: Create CloudWatch Log Groups
echo "📊 Step 4/5: Creating CloudWatch log groups..."
for service in eureka-server api-gateway user-service organization-service invitation-service; do
    aws logs create-log-group --log-group-name /ecs/${PROJECT_NAME}-${service} --region $AWS_REGION 2>/dev/null || true
done
echo "✅ Log groups created"
echo ""

# Step 5: Display Next Steps
echo "✅ Step 5/5: Infrastructure ready!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📋 NEXT STEPS - Manual Configuration Required:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1️⃣  Create RDS PostgreSQL Database:"
echo "   - Go to AWS Console → RDS → Create Database"
echo "   - Engine: PostgreSQL"
echo "   - Instance: db.t3.micro"
echo "   - DB identifier: digitopia-postgres"
echo "   - Master username: digitopia_admin"
echo "   - Create databases: user_service_db, organization_service_db, invitation_service_db"
echo ""
echo "2️⃣  Create Amazon MSK (Kafka) Cluster:"
echo "   - Go to AWS Console → Amazon MSK → Create cluster"
echo "   - Cluster name: digitopia-kafka"
echo "   - Kafka version: 3.5.1"
echo "   - Broker type: kafka.t3.small"
echo "   - Number of brokers: 2"
echo ""
echo "3️⃣  Create Application Load Balancer:"
echo "   - Go to AWS Console → EC2 → Load Balancers → Create"
echo "   - Type: Application Load Balancer"
echo "   - Name: digitopia-alb"
echo "   - Listeners: HTTP:80"
echo "   - Create target groups for each service"
echo ""
echo "4️⃣  Create VPC & Security Groups:"
echo "   - Allow inbound traffic on required ports"
echo "   - Fargate tasks: 8080-8084"
echo "   - RDS: 5432"
echo "   - Kafka: 9092"
echo "   - ALB: 80, 443"
echo ""
echo "5️⃣  Create ECS Task Definitions:"
echo "   Use the AWS Console or run:"
echo "   ./create-ecs-tasks.sh"
echo ""
echo "6️⃣  Create ECS Services:"
echo "   Use the AWS Console or run:"
echo "   ./create-ecs-services.sh"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "📚 For detailed instructions, see: AWS_ECS_FARGATE_DEPLOYMENT.md"
echo ""
echo "🎉 Your Docker images are now in ECR at:"
echo "   $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/${PROJECT_NAME}/"
echo ""
