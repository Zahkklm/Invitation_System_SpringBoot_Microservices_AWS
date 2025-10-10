# Digitopia Invitation System

Welcome to the **Digitopia Invitation System** documentation - a comprehensive event-driven microservices platform built with Spring Boot, Kotlin, Apache Kafka, and AWS.

## 🚀 Overview

This project demonstrates senior-level backend development practices through a production-ready microservices architecture featuring:

- **5 Microservices** with distinct responsibilities
- **Event-Driven Architecture** using Apache Kafka
- **Service Discovery** with Netflix Eureka
- **API Gateway** for routing and security
- **PostgreSQL** databases with Flyway migrations
- **Docker Compose** for local development
- **AWS deployment** options (ECS Fargate, Elastic Beanstalk)

---

## ⚡ Quick Start

Get the entire system running locally in 30 seconds:

```bash
docker-compose up
```

That's it! All microservices, databases, and Kafka will be running.

[See detailed quick start guide →](getting-started/quick-start.md)

---

## 🏗️ Architecture

### System Components

```
API Gateway (8080)
    ↓
├── Eureka Server (8761) - Service Discovery
├── User Service (8084) - User management + Kafka consumer
├── Organization Service (8082) - Organization CRUD
└── Invitation Service (8085) - Invitations + Kafka producer
    ↓
Apache Kafka - Event streaming
    ↓
PostgreSQL (3 databases) - Data persistence
```

### Key Features

✅ **Event-Driven Communication** - Asynchronous processing with Kafka  
✅ **Service Discovery** - Automatic service registration with Eureka  
✅ **API Gateway** - Centralized routing and JWT validation  
✅ **Database per Service** - Microservices best practices  
✅ **Health Monitoring** - Spring Boot Actuator endpoints  
✅ **Container-Ready** - Full Docker Compose support  

[Learn more about the architecture →](architecture/overview.md)

---

## 💰 Cost-Effective Testing

| Approach | Cost | Setup Time | Recommended |
|----------|------|------------|-------------|
| **Local (Docker Compose)** | $0.00 | 30 seconds | ⭐⭐⭐⭐⭐ |
| AWS ECS Fargate | $0.39/hour | 15 minutes | For production |
| AWS Elastic Beanstalk | $0.20/hour | 10 minutes | Simpler AWS option |

**We strongly recommend testing locally first!**

[See cost calculator →](aws/cost-calculator.md)

---

## 📚 Documentation Sections

### Getting Started
- [Quick Start Guide](getting-started/quick-start.md) - Get running in 30 seconds
- [Simple Start](getting-started/simple-start.md) - Step-by-step walkthrough
- [Testing Guide](getting-started/testing.md) - Unit and integration tests

### Architecture
- [System Overview](architecture/overview.md) - Architecture and design
- [Event-Driven Architecture](architecture/event-driven.md) - Kafka integration
- [Data Models](architecture/data-models.md) - Database schemas
- [Health Endpoints](architecture/health-endpoints.md) - Monitoring

### AWS Deployment
- [Deployment Comparison](aws/deployment-comparison.md) - Compare deployment options
- [Reality Check](aws/deployment-reality.md) - What works and what doesn't
- [AWS Setup Guide](aws/setup-guide.md) - AWS credentials and IAM
- [ECS Fargate Guide](aws/ecs-fargate.md) - Production deployment
- [Copilot Issues](aws/copilot-issues.md) - Common problems and solutions
- [Cost Calculator](aws/cost-calculator.md) - Detailed cost breakdown

### Implementation
- [Final Summary](implementation/final-summary.md) - Project completion status
- [Testing Status](implementation/testing.md) - Test coverage and results

---

## 🛠️ Technology Stack

**Core:**
- Kotlin 1.9.10
- Spring Boot 3.1.5
- Gradle 8.4

**Infrastructure:**
- PostgreSQL 15
- Apache Kafka 7.5.0
- Docker & Docker Compose

**Cloud:**
- AWS ECS Fargate
- AWS RDS (PostgreSQL)
- AWS MSK (Managed Kafka)
- AWS Cognito (Authentication)

---

## ⚠️ Important Notes

### For Testing

**✅ DO:** Run locally with `docker-compose up`
- FREE ($0.00)
- Fast (30 seconds)
- Identical to AWS

**❌ DON'T:** Deploy to AWS for testing
- Costs $0.39/hour or $291/month
- Takes 15+ minutes to set up
- Requires AWS credentials and IAM setup
- Same functionality as local

### For Production

When you're ready for production:
1. Test everything locally first
2. Follow the [ECS Fargate guide](aws/ecs-fargate.md)
3. Use managed services (RDS, MSK)
4. Set up proper monitoring

[See deployment reality check →](aws/deployment-reality.md)

---

## 🎯 Common Use Cases

### Scenario 1: Testing the Application

```bash
# Start locally (FREE)
docker-compose up

# Test endpoints
curl http://localhost:8080/actuator/health

# When done
docker-compose down
```

**Cost:** $0.00  
**Time:** 30 seconds

### Scenario 2: Production Deployment

Follow the [ECS Fargate deployment guide](aws/ecs-fargate.md) for:
- Serverless container orchestration
- Auto-scaling
- Production monitoring
- Cost: ~$291/month

### Scenario 3: Quick Demo

Use local Docker Compose for:
- Fast demonstrations
- Development
- Testing new features
- Zero cost

---

## 📊 Project Status

✅ **Completed Features:**
- All 5 microservices implemented
- Event-driven architecture with Kafka
- Service discovery with Eureka
- API Gateway with routing
- Unit tests (41 tests passing)
- Docker Compose setup
- Health monitoring endpoints

🚀 **Production Ready:**
- Comprehensive AWS deployment guides
- Cost calculators and comparisons
- Troubleshooting documentation
- Multiple deployment options

---

## 🔗 Quick Links

- [GitHub Repository](https://github.com/Zahkklm/Invitation_System_SpringBoot_Microservices_AWS)
- [Quick Start](getting-started/quick-start.md)
- [Architecture Overview](architecture/overview.md)
- [AWS Deployment](aws/deployment-comparison.md)
- [Cost Calculator](aws/cost-calculator.md)

---

## 💡 Need Help?

- **Getting started?** → [Quick Start Guide](getting-started/quick-start.md)
- **AWS deployment issues?** → [Copilot Issues](aws/copilot-issues.md)
- **Cost questions?** → [Cost Calculator](aws/cost-calculator.md)
- **Architecture questions?** → [System Overview](architecture/overview.md)

---

**Ready to begin?** Start with the [Quick Start Guide](getting-started/quick-start.md) or jump straight to running locally:

```bash
docker-compose up
```

🎉 **Happy coding!**
