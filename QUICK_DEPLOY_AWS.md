# 🚀 AWS Deployment - Quick Reference Card

## ⚡ FASTEST WAY TO DEPLOY (5 Minutes)

### Step 1: Install Tools (One Time)
```bash
pip install awscli awsebcli
aws configure  # Enter your AWS credentials
```

### Step 2: Deploy
```bash
# Windows
.\deploy-to-aws.ps1

# Linux/Mac
chmod +x deploy-to-aws.sh
./deploy-to-aws.sh
```

### Step 3: Configure (After First Deploy)
```bash
eb setenv \
  SPRING_DATASOURCE_URL="jdbc:postgresql://YOUR-RDS-ENDPOINT:5432/postgres" \
  SPRING_DATASOURCE_USERNAME="digitopia_admin" \
  SPRING_DATASOURCE_PASSWORD="YourPassword123!" \
  KAFKA_BOOTSTRAP_SERVERS="YOUR-KAFKA-ENDPOINT:9092"
```

### Step 4: Open
```bash
eb open
```

**DONE! 🎉**

---

## 📋 Essential Commands

| Command | Purpose |
|---------|---------|
| `eb deploy` | Deploy/update application |
| `eb open` | Open app in browser |
| `eb logs` | View application logs |
| `eb health` | Check service health |
| `eb status` | Environment status |
| `eb ssh` | SSH into instance |
| `eb scale 3` | Scale to 3 instances |
| `eb terminate` | Delete environment |

---

## 💰 Cost Estimates

| Setup | Monthly Cost | Best For |
|-------|--------------|----------|
| **Minimal** | $50-80 | Testing/Demo |
| **Production** | $100-150 | Small-Medium apps |
| **Enterprise** | $200-400 | High availability |

---

## 🛠️ Required AWS Services

### Must Have:
- ✅ **RDS PostgreSQL** - Databases ($15-30/mo)
- ✅ **MSK or MQ** - Kafka ($40-80/mo)
- ✅ **Cognito** - Auth (FREE tier)
- ✅ **Elastic Beanstalk** - Hosting ($30-60/mo)

### Create RDS (One Time):
```bash
aws rds create-db-instance \
  --db-instance-identifier digitopia-postgres \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username digitopia_admin \
  --master-user-password "YourPassword123!" \
  --allocated-storage 20 \
  --region us-east-1
```

### Create Databases in RDS:
```sql
CREATE DATABASE user_service_db;
CREATE DATABASE organization_service_db;
CREATE DATABASE invitation_service_db;
```

---

## 🔧 Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://RDS-ENDPOINT:5432/user_service_db
SPRING_DATASOURCE_USERNAME=digitopia_admin
SPRING_DATASOURCE_PASSWORD=YourPassword123!

# Kafka
KAFKA_BOOTSTRAP_SERVERS=MSK-ENDPOINT:9092

# AWS Cognito
AWS_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
AWS_COGNITO_CLIENT_ID=xxxxxxxxxx
AWS_REGION=us-east-1

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

---

## 🚨 Troubleshooting

### Can't connect to RDS?
```bash
# Check security group allows port 5432
# Verify RDS endpoint in SPRING_DATASOURCE_URL
```

### Services unhealthy?
```bash
eb logs                    # Check logs
eb health --refresh        # Detailed health
```

### Need to rollback?
```bash
eb appversion list         # List versions
eb deploy --version v1     # Deploy old version
```

---

## 🎯 Alternative: AWS Copilot (Super Easy!)

```bash
# Install Copilot
brew install aws/tap/copilot-cli

# Deploy everything
copilot app init digitopia
copilot init --name user-service --dockerfile ./user-service/Dockerfile
copilot deploy --all
```

**Copilot automatically creates:**
- VPC, Load Balancer, ECS Cluster
- Auto-scaling, Logging, Security Groups

---

## 📊 Monitoring

### View Logs:
```bash
eb logs              # Latest logs
eb logs --all        # All logs
eb logs -f           # Follow real-time
```

### Check Health:
```bash
eb health            # Service health
curl http://YOUR-URL/actuator/health
```

---

## 💾 Backup & Scale

### Enable Auto-Scaling:
```bash
# Edit .elasticbeanstalk/config.yml
aws:autoscaling:asg:
  MinSize: 2
  MaxSize: 6
```

### Backup RDS:
```bash
aws rds create-db-snapshot \
  --db-instance-identifier digitopia-postgres \
  --db-snapshot-identifier digitopia-backup-$(date +%Y%m%d)
```

---

## 🧹 Cleanup (When Done Testing)

```bash
# Terminate EB environment
eb terminate digitopia-prod

# Delete RDS
aws rds delete-db-instance \
  --db-instance-identifier digitopia-postgres \
  --skip-final-snapshot

# Delete MSK (through AWS Console)
```

---

## 📚 Full Documentation

- **Detailed Guide**: [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md)
- **Main README**: [README.md](README.md)
- **Testing**: [TESTING.md](TESTING.md)

---

## ⚡ TL;DR - Deploy in 3 Commands

```bash
# 1. Install (once)
pip install awscli awsebcli && aws configure

# 2. Deploy
.\deploy-to-aws.ps1     # Windows
./deploy-to-aws.sh      # Linux/Mac

# 3. Open
eb open
```

**That's it! Your app is live on AWS! 🚀**

---

## 🎓 Learning Resources

- AWS Elastic Beanstalk: https://docs.aws.amazon.com/elasticbeanstalk/
- AWS RDS: https://docs.aws.amazon.com/rds/
- AWS MSK: https://docs.aws.amazon.com/msk/
- AWS Copilot: https://aws.github.io/copilot-cli/

---

**Need Help?** Check [AWS_DEPLOYMENT.md](AWS_DEPLOYMENT.md) for detailed troubleshooting.

**Questions?** Open an issue on GitHub.

**Good luck! 🍀**
