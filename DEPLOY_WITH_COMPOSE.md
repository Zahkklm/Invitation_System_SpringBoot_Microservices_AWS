# 🚀 Deploy to AWS ECS Using Docker Compose

## ⚠️ Important Note on Docker Compose ECS Integration

**Docker's native ECS integration was deprecated in November 2023.** However, there are better alternatives:

1. **AWS Copilot** (Recommended) - Official AWS tool, works like Docker Compose
2. **ECS CLI v2** - AWS native alternative
3. **Manual ECS with your docker-compose.yml as reference**

---

## Option 1: AWS Copilot (Easiest) ⭐ RECOMMENDED

AWS Copilot is designed to work like Docker Compose but for AWS. It's the official successor to Docker's ECS integration.

### Installation

**Windows (PowerShell):**
```powershell
# Download Copilot
Invoke-WebRequest -OutFile copilot-windows.exe https://github.com/aws/copilot-cli/releases/latest/download/copilot-windows.exe

# Move to a directory in your PATH
Move-Item .\copilot-windows.exe C:\Users\$env:USERNAME\AppData\Local\Microsoft\WindowsApps\copilot.exe
```

**Linux/Mac:**
```bash
# Download and install
curl -Lo copilot https://github.com/aws/copilot-cli/releases/latest/download/copilot-linux
chmod +x copilot
sudo mv copilot /usr/local/bin/copilot
```

### Deploy Your Application

```bash
# Initialize the application
copilot app init digitopia

# Create an environment (like dev, staging, prod)
copilot env init --name production --profile default --default-config

# Deploy each service
copilot svc init --name eureka-server --svc-type "Load Balanced Web Service" --dockerfile eureka-server/Dockerfile
copilot svc init --name api-gateway --svc-type "Load Balanced Web Service" --dockerfile api-gateway/Dockerfile
copilot svc init --name user-service --svc-type "Backend Service" --dockerfile user-service/Dockerfile
copilot svc init --name organization-service --svc-type "Backend Service" --dockerfile organization-service/Dockerfile
copilot svc init --name invitation-service --svc-type "Backend Service" --dockerfile invitation-service/Dockerfile

# Deploy all services
copilot svc deploy --name eureka-server
copilot svc deploy --name api-gateway
copilot svc deploy --name user-service
copilot svc deploy --name organization-service
copilot svc deploy --name invitation-service
```

**Benefits:**
- ✅ Similar workflow to Docker Compose
- ✅ Fully supported by AWS
- ✅ Automatic CI/CD pipeline generation
- ✅ Built-in service discovery
- ✅ Auto-scaling out of the box

---

## Option 2: Convert docker-compose.yml for ECS

Since your `docker-compose.yml` is already well-structured, we can create an ECS-optimized version that deploys seamlessly.

### Create `docker-compose.aws.yml`

This file extends your local compose file with AWS-specific configurations:

```yaml
version: '3.8'

x-aws-cloudformation:
  Resources:
    # Use existing VPC or create default
    DefaultVPC:
      Type: AWS::EC2::VPC
      Properties:
        CidrBlock: 10.0.0.0/16
        EnableDnsSupport: true
        EnableDnsHostnames: true

services:
  # === Infrastructure Services ===
  postgres-combined:
    image: postgres:15-alpine
    environment:
      POSTGRES_MULTIPLE_DATABASES: user_service_db,organization_service_db,invitation_service_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    x-aws-autoscaling:
      min: 1
      max: 2
      cpu: 75
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 2048M

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    depends_on:
      - zookeeper
    x-aws-autoscaling:
      min: 1
      max: 3
      cpu: 75
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: 2048M

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M

  # === Application Services ===
  eureka-server:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/eureka-server:latest
    ports:
      - "8761:8761"
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
    x-aws-autoscaling:
      min: 1
      max: 2
      cpu: 70

  api-gateway:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/api-gateway:latest
    ports:
      - "80:8080"
    environment:
      - COGNITO_REGION=${COGNITO_REGION}
      - COGNITO_USER_POOL_ID=${COGNITO_USER_POOL_ID}
      - COGNITO_JWK_SET_URI=${COGNITO_JWK_SET_URI}
    depends_on:
      - eureka-server
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
    x-aws-autoscaling:
      min: 2
      max: 4
      cpu: 70
    x-aws-role:
      Version: '2012-10-17'
      Statement:
        - Effect: Allow
          Action:
            - logs:CreateLogGroup
            - logs:CreateLogStream
            - logs:PutLogEvents
          Resource: '*'

  user-service:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/user-service:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-combined:5432/user_service_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - postgres-combined
      - kafka
      - eureka-server
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
      replicas: 2
    x-aws-autoscaling:
      min: 2
      max: 4
      cpu: 70

  organization-service:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/organization-service:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-combined:5432/organization_service_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - postgres-combined
      - eureka-server
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
      replicas: 2
    x-aws-autoscaling:
      min: 2
      max: 4
      cpu: 70

  invitation-service:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/invitation-service:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-combined:5432/invitation_service_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - postgres-combined
      - kafka
      - eureka-server
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M
      replicas: 2
    x-aws-autoscaling:
      min: 2
      max: 4
      cpu: 70

volumes:
  postgres-data:
    driver: local
    x-aws-efs:
      FileSystemId: ${EFS_ID}
      AccessPoint: ${EFS_ACCESS_POINT}

x-aws-vpc: ${VPC_ID}
x-aws-cluster: digitopia-cluster

x-aws-loadbalancer:
  api-gateway:
    TargetGroupArn: ${TARGET_GROUP_ARN}
    ListenerArn: ${LISTENER_ARN}
```

### Deployment Script

**File: `deploy-compose-to-ecs.ps1`**

```powershell
# Configuration
$AWS_REGION = "us-east-1"
$AWS_ACCOUNT_ID = (aws sts get-caller-identity --query Account --output text)
$DB_PASSWORD = "ChangeMe123Secure!"

Write-Host "🚀 Deploying docker-compose to ECS Fargate" -ForegroundColor Cyan

# Step 1: Build and push images
Write-Host "`n📦 Building and pushing Docker images..." -ForegroundColor Yellow
& .\deploy-ecs-fargate.ps1

# Step 2: Set environment variables
$env:AWS_ACCOUNT_ID = $AWS_ACCOUNT_ID
$env:AWS_REGION = $AWS_REGION
$env:DB_PASSWORD = $DB_PASSWORD

# Step 3: Create CloudFormation stack from docker-compose
Write-Host "`n🏗️  Creating CloudFormation stack..." -ForegroundColor Yellow
docker compose -f docker-compose.aws.yml convert > ecs-cloudformation.yaml

# Step 4: Deploy CloudFormation stack
Write-Host "`n☁️  Deploying to AWS..." -ForegroundColor Yellow
aws cloudformation create-stack `
  --stack-name digitopia-ecs-stack `
  --template-body file://ecs-cloudformation.yaml `
  --capabilities CAPABILITY_IAM `
  --region $AWS_REGION

Write-Host "`n✅ Deployment initiated!" -ForegroundColor Green
Write-Host "Monitor progress: https://console.aws.amazon.com/cloudformation" -ForegroundColor Cyan
```

---

## Option 3: Use Managed Services (Hybrid Approach) 🔥 BEST FOR PRODUCTION

Instead of running Kafka and PostgreSQL in containers, use AWS managed services:

- **PostgreSQL** → Amazon RDS
- **Kafka** → Amazon MSK (Managed Streaming for Kafka)
- **Service Discovery** → AWS Cloud Map (automatic with ECS)

This gives you:
- ✅ Better reliability
- ✅ Automated backups
- ✅ Easy scaling
- ✅ Lower maintenance
- ✅ Better security

**Modified Compose File:**

```yaml
version: '3.8'
services:
  eureka-server:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/eureka-server:latest
    ports:
      - "8761:8761"
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 1024M

  api-gateway:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/api-gateway:latest
    ports:
      - "80:8080"
    environment:
      - COGNITO_REGION=${AWS_REGION}
      - COGNITO_USER_POOL_ID=${COGNITO_USER_POOL_ID}
      - COGNITO_JWK_SET_URI=${COGNITO_JWK_SET_URI}
    depends_on:
      - eureka-server

  user-service:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/user-service:latest
    environment:
      # Use RDS endpoint instead of container
      SPRING_DATASOURCE_URL: jdbc:postgresql://${RDS_ENDPOINT}:5432/user_service_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      # Use MSK bootstrap servers instead of container
      SPRING_KAFKA_BOOTSTRAP_SERVERS: ${MSK_BOOTSTRAP_SERVERS}
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
    deploy:
      replicas: 2

  organization-service:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/organization-service:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://${RDS_ENDPOINT}:5432/organization_service_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
    deploy:
      replicas: 2

  invitation-service:
    image: ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/digitopia/invitation-service:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://${RDS_ENDPOINT}:5432/invitation_service_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: ${MSK_BOOTSTRAP_SERVERS}
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka-server:8761/eureka/
    depends_on:
      - eureka-server
    deploy:
      replicas: 2
```

---

## 📊 Comparison Table

| Approach | Ease of Use | Production Ready | Cost | Maintenance |
|----------|-------------|------------------|------|-------------|
| **AWS Copilot** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | $$ | Low |
| **Docker Compose → ECS** | ⭐⭐⭐⭐ | ⭐⭐⭐ | $$ | Medium |
| **Hybrid (Managed Services)** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | $$$ | Very Low |
| **Manual ECS Setup** | ⭐⭐ | ⭐⭐⭐⭐⭐ | $$ | Medium |

---

## 🎯 My Recommendation

**For your use case, I recommend AWS Copilot:**

1. It's officially supported by AWS (unlike deprecated Docker ECS integration)
2. Works similarly to Docker Compose
3. Handles all the ECS complexity automatically
4. Generates production-ready infrastructure
5. Includes CI/CD pipeline generation

**Quick Start with Copilot:**

```bash
# 1. Install Copilot
curl -Lo copilot https://github.com/aws/copilot-cli/releases/latest/download/copilot-linux
chmod +x copilot && sudo mv copilot /usr/local/bin/

# 2. Initialize your app
copilot app init digitopia

# 3. Deploy with one command per service
copilot init --app digitopia \
  --name api-gateway \
  --type "Load Balanced Web Service" \
  --dockerfile api-gateway/Dockerfile \
  --deploy
```

That's it! Copilot handles everything else.

---

## 🆘 Need Help?

- **AWS Copilot Docs**: https://aws.github.io/copilot-cli/
- **ECS Compose Spec**: https://docs.docker.com/cloud/ecs-compose-features/
- **My Detailed Guide**: See `AWS_ECS_FARGATE_DEPLOYMENT.md`

---

**Ready to deploy? Start with AWS Copilot for the easiest experience!** 🚀
