# 🚀 AWS Deployment Guide - Quick Start

## Easiest Way: AWS Elastic Beanstalk (5 Minutes)

### Prerequisites
- AWS Account
- AWS CLI installed
- Docker installed locally

### Step-by-Step Deployment

#### 1. Install AWS CLI and EB CLI
```bash
# Install AWS CLI
pip install awscli

# Configure AWS credentials
aws configure
# Enter: Access Key ID, Secret Access Key, Region (us-east-1), Output format (json)

# Install Elastic Beanstalk CLI
pip install awsebcli
```

#### 2. Set Up AWS Resources

**Create RDS PostgreSQL Instance:**
```bash
aws rds create-db-instance \
  --db-instance-identifier digitopia-postgres \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username digitopia_admin \
  --master-user-password "YourSecurePassword123!" \
  --allocated-storage 20 \
  --publicly-accessible \
  --backup-retention-period 7 \
  --region us-east-1

# Wait for RDS to be available (5-10 minutes)
aws rds wait db-instance-available \
  --db-instance-identifier digitopia-postgres

# Get RDS endpoint
aws rds describe-db-instances \
  --db-instance-identifier digitopia-postgres \
  --query 'DBInstances[0].Endpoint.Address' \
  --output text
```

**Create Amazon MSK Cluster (Managed Kafka):**
```bash
# This is more complex - easier through AWS Console:
# 1. Go to Amazon MSK Console
# 2. Click "Create cluster"
# 3. Choose "Quick create"
# 4. Cluster name: digitopia-kafka
# 5. Select default VPC
# 6. Choose broker type: kafka.t3.small
# 7. Number of brokers: 2
# 8. Create cluster (takes 10-15 minutes)
```

**Or use Amazon MQ (simpler alternative to MSK):**
```bash
# Amazon MQ is easier for small deployments
# Go to Amazon MQ Console → Create broker
# Choose Apache Kafka
# Deployment mode: Single-instance broker
# Broker instance type: mq.t3.micro
```

#### 3. Deploy with Elastic Beanstalk

```bash
# Navigate to your project
cd spring_boot_microservices

# Initialize Elastic Beanstalk
eb init -p docker digitopia-invitation-system --region us-east-1

# Create environment
eb create digitopia-prod \
  --instance-type t3.medium \
  --envvars \
    SPRING_DATASOURCE_URL="jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/postgres",\
    SPRING_DATASOURCE_USERNAME="digitopia_admin",\
    SPRING_DATASOURCE_PASSWORD="YourSecurePassword123!",\
    KAFKA_BOOTSTRAP_SERVERS="YOUR_KAFKA_ENDPOINT:9092",\
    AWS_COGNITO_USER_POOL_ID="your-pool-id",\
    AWS_COGNITO_CLIENT_ID="your-client-id",\
    AWS_REGION="us-east-1"

# Deploy application
eb deploy

# Open application in browser
eb open
```

#### 4. Create Databases in RDS

```bash
# Connect to RDS
psql -h YOUR_RDS_ENDPOINT -U digitopia_admin -d postgres

# Create databases
CREATE DATABASE user_service_db;
CREATE DATABASE organization_service_db;
CREATE DATABASE invitation_service_db;

# Exit
\q
```

---

## Alternative: AWS Copilot (Even Easier!)

AWS Copilot automates everything:

```bash
# Install Copilot
brew install aws/tap/copilot-cli  # macOS
# Or download from: https://github.com/aws/copilot-cli/releases

# Initialize application
copilot app init digitopia

# Add environment
copilot env init --name prod --profile default --default-config

# Deploy services
copilot svc init --name user-service \
  --svc-type "Load Balanced Web Service" \
  --dockerfile ./user-service/Dockerfile

copilot svc init --name org-service \
  --svc-type "Load Balanced Web Service" \
  --dockerfile ./organization-service/Dockerfile

copilot svc init --name invitation-service \
  --svc-type "Load Balanced Web Service" \
  --dockerfile ./invitation-service/Dockerfile

# Deploy everything
copilot deploy --all
```

**Copilot automatically creates:**
- VPC with public/private subnets
- Application Load Balancer
- ECS cluster with Fargate
- CloudWatch logging
- Auto-scaling groups
- Security groups

---

## Simplest Option: Use Pre-configured Script

Create `deploy-aws.sh`:

```bash
#!/bin/bash

echo "🚀 Deploying Digitopia to AWS..."

# Check prerequisites
command -v aws >/dev/null 2>&1 || { echo "❌ AWS CLI not found. Install: pip install awscli"; exit 1; }
command -v eb >/dev/null 2>&1 || { echo "❌ EB CLI not found. Install: pip install awsebcli"; exit 1; }

# Build Docker images
echo "📦 Building Docker images..."
docker-compose build

# Initialize EB if not done
if [ ! -d ".elasticbeanstalk" ]; then
  echo "🎯 Initializing Elastic Beanstalk..."
  eb init -p docker digitopia --region us-east-1
fi

# Check if environment exists
if eb list | grep -q "digitopia-prod"; then
  echo "♻️  Environment exists, updating..."
  eb deploy
else
  echo "🆕 Creating new environment..."
  eb create digitopia-prod --instance-type t3.medium
fi

# Show results
echo "✅ Deployment complete!"
echo ""
echo "🔗 Application URL:"
eb status | grep CNAME | awk '{print $2}'
echo ""
echo "📊 View logs: eb logs"
echo "🌐 Open app: eb open"
echo "⚙️  SSH access: eb ssh"
```

Run:
```bash
chmod +x deploy-aws.sh
./deploy-aws.sh
```

---

## Cost Estimate

### Minimal Setup (~$50-80/month)
- RDS db.t3.micro: $15/month
- EC2 t3.medium (1 instance): $30/month
- Amazon MQ t3.micro: $15/month
- CloudWatch Logs: $5/month
- Data Transfer: $5/month

### Recommended Setup (~$100-150/month)
- RDS db.t3.small: $30/month
- EC2 t3.medium (2 instances): $60/month
- Amazon MSK (2 brokers): $40/month
- Load Balancer: $20/month
- CloudWatch + misc: $10/month

### Production Setup (~$200-400/month)
- RDS db.t3.medium: $60/month
- ECS Fargate tasks: $80/month
- Amazon MSK (3 brokers): $120/month
- Load Balancer: $20/month
- CloudWatch, backups, etc: $40/month

---

## Environment Variables Reference

Set these in Elastic Beanstalk or ECS:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RDS_ENDPOINT:5432/user_service_db
SPRING_DATASOURCE_USERNAME=digitopia_admin
SPRING_DATASOURCE_PASSWORD=YourSecurePassword123!

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=YOUR_MSK_ENDPOINT:9092
SPRING_KAFKA_BOOTSTRAP_SERVERS=YOUR_MSK_ENDPOINT:9092

# AWS Cognito Configuration
AWS_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
AWS_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxx
AWS_COGNITO_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
AWS_REGION=us-east-1

# Eureka Configuration
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

---

## Post-Deployment Checklist

- [ ] Verify all services are healthy: `eb health`
- [ ] Check logs for errors: `eb logs`
- [ ] Test API endpoints
- [ ] Configure custom domain (Route 53)
- [ ] Set up SSL certificate (ACM)
- [ ] Configure CloudWatch alarms
- [ ] Set up auto-scaling policies
- [ ] Enable RDS automated backups
- [ ] Configure security groups properly
- [ ] Test Kafka event flow

---

## Monitoring & Maintenance

**View Logs:**
```bash
eb logs                          # Latest logs
eb logs --all                    # All log files
eb logs -f                       # Follow logs in real-time
```

**Check Health:**
```bash
eb health                        # Service health status
eb status                        # Environment status
```

**Scale Application:**
```bash
eb scale 3                       # Scale to 3 instances
```

**SSH Access:**
```bash
eb ssh                           # SSH into instance
```

**Update Configuration:**
```bash
eb setenv SPRING_PROFILES_ACTIVE=prod
```

---

## Troubleshooting

### Issue: Cannot connect to RDS
**Solution:** 
1. Check security group allows inbound traffic on port 5432
2. Ensure RDS is in same VPC as EB environment
3. Verify RDS endpoint is correct

### Issue: Kafka connection timeout
**Solution:**
1. Verify MSK cluster is active
2. Check security groups allow port 9092
3. Ensure MSK and EB are in same VPC

### Issue: Service shows as "Degraded"
**Solution:**
```bash
eb health --refresh               # Check detailed health
eb logs                           # Check application logs
```

### Issue: 502 Bad Gateway
**Solution:**
1. Check if services are running: `eb ssh` then `docker ps`
2. Verify health endpoint: `/actuator/health`
3. Check port mappings in Dockerrun.aws.json

---

## Rollback Deployment

If something goes wrong:
```bash
# List previous versions
eb appversion list

# Deploy previous version
eb deploy --version <version-label>
```

---

## Clean Up Resources

To avoid charges when testing:
```bash
# Terminate EB environment
eb terminate digitopia-prod

# Delete RDS instance
aws rds delete-db-instance \
  --db-instance-identifier digitopia-postgres \
  --skip-final-snapshot

# Delete MSK cluster (through AWS Console)
```

---

## Next Steps

1. **Set up CI/CD:** Use AWS CodePipeline to automate deployments
2. **Add monitoring:** Configure CloudWatch dashboards
3. **Implement caching:** Add Amazon ElastiCache (Redis)
4. **Enable CDN:** Use CloudFront for static assets
5. **Add WAF:** AWS WAF for security

---

## Support

For issues or questions:
- AWS Documentation: https://docs.aws.amazon.com/
- Elastic Beanstalk Guide: https://docs.aws.amazon.com/elasticbeanstalk/
- AWS Copilot: https://aws.github.io/copilot-cli/

---

**Good luck with your deployment! 🚀**
