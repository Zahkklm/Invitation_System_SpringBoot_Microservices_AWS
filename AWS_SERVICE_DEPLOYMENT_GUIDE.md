# 🚀 AWS ECS Service Deployment Guide

You've successfully pushed your Docker images to ECR! Now let's deploy them to ECS Fargate.

## ✅ Current Status
- ✅ Docker images built and pushed to ECR
- ✅ ECS cluster created: `digitopia-cluster`
- ✅ Task definitions created in AWS Console

## 📋 Service Deployment Order

**IMPORTANT**: Services must be deployed in this order because of dependencies:

1. **Eureka Server** (Service Discovery) - Deploy FIRST
2. **Kafka** (Message Broker) - Deploy SECOND
3. **PostgreSQL Databases** (3 separate databases)
4. **User Service** (depends on Eureka + DB + Kafka)
5. **Organization Service** (depends on Eureka + DB + Kafka)
6. **Invitation Service** (depends on Eureka + DB + Kafka)
7. **API Gateway** (depends on Eureka) - Deploy LAST

---

## 🔧 Step 1: Deploy Eureka Server

### Create Service:
1. Go to: https://console.aws.amazon.com/ecs/v2/clusters/digitopia-cluster/services
2. Click **"Create"**
3. Configure:
   - **Compute configuration**: 
     - Launch type: **FARGATE**
     - Platform version: LATEST
   - **Deployment configuration**:
     - Application type: **Service**
     - Task definition**: Select `eureka-server` (latest revision)
     - Service name: `eureka-service`
     - Desired tasks: **1**
   
4. **Networking**:
   - VPC: **Default VPC**
   - Subnets: **Select ALL available subnets**
   - Security group: 
     - Create new security group: `eureka-sg`
     - Inbound rules:
       - Port **8761** from Anywhere (0.0.0.0/0) - HTTP
   - **Public IP**: **ENABLED** ⚠️ CRITICAL!

5. **Load balancing**: None (for now)

6. Click **"Create"**

### Get Eureka URL:
1. Wait 2-3 minutes for task to start
2. Go to **Tasks** tab → Click the running task
3. Copy the **Public IP** address
4. Your Eureka URL: `http://<public-ip>:8761`
5. **Test it**: Open browser → `http://<public-ip>:8761` → Should see Eureka dashboard

---

## ⚠️ WAIT! Before Deploying Other Services

**Problem**: Your microservices need:
- PostgreSQL databases (3 of them)
- Kafka broker
- Environment variables pointing to these services

**Two Options:**

### Option A: Use AWS Managed Services (Recommended for Production)
- **RDS PostgreSQL**: Managed database service
- **Amazon MSK**: Managed Kafka service
- **Cost**: ~$150-200/month (always running)
- **Time**: 20-30 minutes to set up

### Option B: Run Everything in ECS Fargate (Testing)
- Run PostgreSQL containers in Fargate
- Run Kafka containers in Fargate
- **Cost**: ~$0.40/hour (~$291/month if always on)
- **Time**: 10 minutes to set up
- **Warning**: Data will be lost when tasks restart

---

## 🎯 Recommended Approach for Testing

### **Use Local Docker Compose** (FREE!)

Instead of spending money on AWS for testing, run everything locally:

```bash
cd C:\Users\zagor\spring_boot_microservices
docker-compose up
```

This gives you:
- ✅ All 5 microservices
- ✅ All 3 PostgreSQL databases
- ✅ Kafka + Zookeeper
- ✅ Eureka Server
- ✅ API Gateway
- ✅ **Cost: $0.00**
- ✅ **Same functionality as AWS**

Test all endpoints locally first, then deploy to AWS when you need:
- Public access
- High availability
- Production workloads

---

## 🚀 If You Want to Continue with AWS

### Step 2: Deploy PostgreSQL Databases

You need 3 separate PostgreSQL databases. Two options:

#### Option 2A: AWS RDS (Recommended)
1. Go to: https://console.aws.amazon.com/rds
2. Create 3 RDS instances:
   - `user-db` (PostgreSQL 15)
   - `organization-db` (PostgreSQL 15)
   - `invitation-db` (PostgreSQL 15)
3. Instance type: `db.t3.micro` ($0.017/hour each = ~$37/month for all 3)
4. Note connection strings for each

#### Option 2B: PostgreSQL in Fargate (Testing Only)
1. Create task definitions for each PostgreSQL instance
2. Not recommended - data loss on restart
3. Complex networking setup required

### Step 3: Deploy Kafka

#### Option 3A: Amazon MSK (Recommended)
1. Go to: https://console.aws.amazon.com/msk
2. Create MSK cluster: `digitopia-kafka`
3. Instance type: `kafka.t3.small` (~$0.07/hour = ~$50/month)
4. Note broker endpoints

#### Option 3B: Kafka in Fargate (Testing Only)
1. Create task definitions for Kafka + Zookeeper
2. Complex networking and persistence issues
3. Not recommended for real testing

### Step 4: Update Environment Variables

For each microservice task definition, you need to add environment variables:

**User Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/user_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-password>
SPRING_KAFKA_BOOTSTRAP_SERVERS=<kafka-broker>:9092
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://<eureka-ip>:8761/eureka/
```

**Organization Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/organization_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-password>
SPRING_KAFKA_BOOTSTRAP_SERVERS=<kafka-broker>:9092
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://<eureka-ip>:8761/eureka/
```

**Invitation Service**:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/invitation_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-password>
SPRING_KAFKA_BOOTSTRAP_SERVERS=<kafka-broker>:9092
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://<eureka-ip>:8761/eureka/
```

### Step 5: Deploy Microservices

After databases and Kafka are running, deploy services in this order:

1. **User Service**
2. **Organization Service**  
3. **Invitation Service**
4. **API Gateway** (last)

For each service:
1. Create service in ECS
2. Use Fargate launch type
3. Desired tasks: 1
4. Enable public IP
5. Configure security groups (allow required ports)

---

## 💰 Cost Summary

### For 1 Hour of Testing:
- ECR Storage: $0.01
- Eureka (already running): $0.04
- 4 Microservices: $0.16
- 3 RDS instances: $0.05
- MSK Kafka: $0.07
- **Total: ~$0.33/hour**

### For Always-On Production:
- ~$291/month

### For Local Testing:
- **$0.00** (FREE!)

---

## ❓ What Do You Want to Do?

### Choice 1: Test Locally First (Recommended)
```bash
docker-compose up
```
- Test all endpoints for FREE
- No AWS complexity
- Deploy to AWS only when ready for production

### Choice 2: Continue AWS Deployment
- Set up RDS for databases (~30 minutes)
- Set up MSK for Kafka (~20 minutes)
- Deploy all services (~20 minutes)
- **Total time**: ~70 minutes
- **Cost**: Starts immediately (~$7/day if left running)

### Choice 3: Hybrid Approach
- Keep Eureka in AWS (already deployed)
- Run everything else locally with docker-compose
- Modify local services to connect to AWS Eureka
- Test service discovery in cloud environment
- **Cost**: ~$0.04/hour (just Eureka)

---

## 🎓 My Recommendation

Since you already have Eureka running in AWS:

1. **Test the Eureka dashboard**: Open `http://<eureka-public-ip>:8761` in browser
2. **Run everything else locally**: `docker-compose up`
3. **Verify all functionality works** locally (FREE!)
4. **Then decide** if you want to pay for full AWS deployment

This way you:
- ✅ Learn AWS ECS deployment (Eureka is already running)
- ✅ Don't waste money on testing
- ✅ Have a working system locally
- ✅ Can deploy to AWS when you need public access

---

## 📝 Current Status Summary

**Deployed to AWS:**
- ✅ Eureka Server (Task Definition created - need to create service)

**In ECR (Ready to deploy):**
- ✅ API Gateway image
- ✅ User Service image
- ✅ Organization Service image
- ✅ Invitation Service image

**Still Need:**
- ⚠️ PostgreSQL databases (3)
- ⚠️ Kafka broker
- ⚠️ Environment variable configuration
- ⚠️ Networking setup between services

---

## 🆘 Need Help?

Let me know which path you want to take:
- **"Let's deploy everything to AWS"** → I'll guide you through RDS + MSK setup
- **"Let's test locally first"** → I'll help you run docker-compose and test endpoints
- **"Just Eureka in AWS, rest local"** → I'll show you how to connect local services to AWS Eureka

What do you prefer?
