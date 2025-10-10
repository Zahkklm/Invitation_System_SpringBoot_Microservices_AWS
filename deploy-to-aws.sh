#!/bin/bash

# AWS Deployment Script for Digitopia Invitation System
# This script deploys the application to AWS Elastic Beanstalk

set -e  # Exit on error

echo "🚀 Deploying Digitopia Invitation System to AWS..."
echo "=================================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI not found${NC}"
    echo "Install with: pip install awscli"
    exit 1
fi

if ! command -v eb &> /dev/null; then
    echo -e "${RED}❌ Elastic Beanstalk CLI not found${NC}"
    echo "Install with: pip install awsebcli"
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker not found${NC}"
    echo "Install Docker from: https://www.docker.com/"
    exit 1
fi

echo -e "${GREEN}✅ All prerequisites met${NC}"
echo ""

# Check AWS credentials
echo "🔐 Verifying AWS credentials..."
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}❌ AWS credentials not configured${NC}"
    echo "Run: aws configure"
    exit 1
fi

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo -e "${GREEN}✅ Authenticated as AWS Account: $ACCOUNT_ID${NC}"
echo ""

# Build Docker images
echo "📦 Building Docker images..."
if docker-compose build; then
    echo -e "${GREEN}✅ Docker images built successfully${NC}"
else
    echo -e "${RED}❌ Failed to build Docker images${NC}"
    exit 1
fi
echo ""

# Initialize Elastic Beanstalk (if not already done)
if [ ! -d ".elasticbeanstalk" ]; then
    echo "🎯 Initializing Elastic Beanstalk..."
    eb init -p "Multi-container Docker" digitopia-invitation-system --region us-east-1
    echo -e "${GREEN}✅ Elastic Beanstalk initialized${NC}"
else
    echo -e "${YELLOW}⚠️  Elastic Beanstalk already initialized${NC}"
fi
echo ""

# Check if environment exists
echo "🔍 Checking for existing environment..."
if eb list 2>/dev/null | grep -q "digitopia-prod"; then
    echo -e "${YELLOW}♻️  Environment 'digitopia-prod' exists, updating...${NC}"
    
    # Deploy update
    echo "📤 Deploying update to existing environment..."
    if eb deploy digitopia-prod; then
        echo -e "${GREEN}✅ Deployment successful${NC}"
    else
        echo -e "${RED}❌ Deployment failed${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}🆕 Creating new environment 'digitopia-prod'...${NC}"
    echo ""
    echo "⚠️  You will need to configure environment variables after creation:"
    echo "   - SPRING_DATASOURCE_URL"
    echo "   - SPRING_DATASOURCE_USERNAME"
    echo "   - SPRING_DATASOURCE_PASSWORD"
    echo "   - KAFKA_BOOTSTRAP_SERVERS"
    echo "   - AWS_COGNITO_USER_POOL_ID"
    echo "   - AWS_COGNITO_CLIENT_ID"
    echo ""
    
    # Create environment
    if eb create digitopia-prod --instance-type t3.medium; then
        echo -e "${GREEN}✅ Environment created successfully${NC}"
    else
        echo -e "${RED}❌ Failed to create environment${NC}"
        exit 1
    fi
fi
echo ""

# Show deployment results
echo "=================================================="
echo -e "${GREEN}✅ Deployment Complete!${NC}"
echo "=================================================="
echo ""

# Get environment info
echo "📊 Environment Information:"
eb status | grep -E "Environment Name|CNAME|Status|Health"
echo ""

# Get application URL
APP_URL=$(eb status | grep CNAME | awk '{print $2}')
if [ -n "$APP_URL" ]; then
    echo "🌐 Application URL: http://$APP_URL"
else
    echo -e "${YELLOW}⚠️  Application URL not available yet${NC}"
fi
echo ""

# Provide next steps
echo "📝 Next Steps:"
echo ""
echo "1. Configure environment variables:"
echo "   eb setenv SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/digitopia"
echo ""
echo "2. View application logs:"
echo "   eb logs"
echo ""
echo "3. Check application health:"
echo "   eb health"
echo ""
echo "4. Open application in browser:"
echo "   eb open"
echo ""
echo "5. SSH into instance (if needed):"
echo "   eb ssh"
echo ""

echo -e "${GREEN}🎉 Deployment script completed successfully!${NC}"
