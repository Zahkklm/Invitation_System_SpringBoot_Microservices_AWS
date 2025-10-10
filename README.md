# 🚀 Digitopia Invitation System - Spring Boot Microservices

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-blue.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.4-green.svg)](https://gradle.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

A comprehensive event-driven microservices system for managing users, organizations, and invitations with real-time synchronization via Apache Kafka. Built for the Digitopia case study demonstrating senior-level backend development practices.

---

## ☁️ AWS ECS Deployment

**✅ Successfully Deployed to AWS:**
- Docker images pushed to AWS ECR (Elastic Container Registry)
- ECS cluster created: `digitopia-cluster`
- All 5 microservices containerized and ready

**📖 Deployment Guide:**

### Step 1: Push Images to ECR
```powershell
.\deploy-ecs-simple.ps1
```

This automated script:
- Creates ECR repositories for all services
- Builds Docker images locally
- Pushes images to AWS ECR
- Creates ECS cluster

**Time:** 10-15 minutes  
**Status:** ✅ **COMPLETED**

### Step 2: Deploy Services to ECS Fargate

After images are in ECR, deploy services using AWS Console:

1. Go to: https://console.aws.amazon.com/ecs/v2/clusters/digitopia-cluster
2. Create Task Definitions for each service
3. Create ECS Services using the task definitions
4. Configure networking, load balancers, and environment variables

**Full Guide:** See `AWS_SERVICE_DEPLOYMENT_GUIDE.md` for detailed step-by-step instructions.

**💰 Cost:** ~$0.40/hour for all services (~$291/month if always running)

**💡 Tip:** Test locally with `docker-compose up` first (FREE!) before deploying to AWS.

---

## 📚 Documentation Website

**View the complete documentation as a professional website:**

```bash
# Start the documentation server
pip install mkdocs
mkdocs serve
```

Then open: **http://127.0.0.1:8000**

The documentation includes:
- 🚀 Quick Start Guide
- 🏗️ Architecture Overview
- ☁️ AWS Deployment Options
- 💰 Cost Calculator
- 🧪 Testing Guide
- And much more!

---

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Technology Stack](#-technology-stack)
- [Key Features](#-key-features)
- [Data Models](#-data-models)
- [API Documentation](#-api-documentation)
- [Getting Started](#-getting-started)
- [Event-Driven Architecture](#-event-driven-architecture)
- [Security & Authentication](#-security--authentication)
- [Testing](#-testing)
- [Case Study Compliance](#-case-study-compliance)

---

## 🏗️ Architecture Overview

This project implements a **microservices architecture** with **event-driven communication** using Apache Kafka for asynchronous processing and eventual consistency.

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cognito                              │
│                  (Authentication & Identity)                     │
└─────────────────────┬───────────────────────────────────────────┘
                      │ JWT Validation
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (Port 8080)                     │
│        • Route Management  • JWT Validation  • RBAC             │
└──┬──────────────────┬──────────────────┬────────────────────┬───┘
   │                  │                  │                    │
   ▼                  ▼                  ▼                    ▼
┌──────────┐   ┌──────────┐      ┌──────────┐      ┌──────────┐
│  User    │   │  Org     │      │ Invite   │      │ Eureka   │
│ Service  │◄──┤ Service  │      │ Service  │      │ Server   │
│ (8084)   │   │ (8082)   │      │ (8085)   │      │ (8761)   │
└────┬─────┘   └──────────┘      └────┬─────┘      └──────────┘
     │                                 │
     │         Apache Kafka            │
     │      (Event Streaming)          │
     └──────────────┬──────────────────┘
                    │
         ┌──────────┴──────────┐
         │  invitation-events  │
         │     user-events     │
         └─────────────────────┘
```

### Microservices

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| **Eureka Server** | 8761 | Service discovery and registry | N/A |
| **API Gateway** | 8080 | Entry point, routing, JWT validation | N/A |
| **User Service** | 8084 | User management, Kafka consumer | PostgreSQL |
| **Organization Service** | 8082 | Organization CRUD operations | PostgreSQL |
| **Invitation Service** | 8085 | Invitation lifecycle, Kafka producer | PostgreSQL |
| **Kafka** | 9092 | Event streaming platform | N/A |
| **Zookeeper** | 2181 | Kafka coordination | N/A |

---

## 🛠️ Technology Stack

### Core Technologies
- **Language:** Kotlin 1.9.10
- **Framework:** Spring Boot 3.1.5
- **Build Tool:** Gradle 8.4 (Kotlin DSL)
- **JVM:** Java 17+

### Spring Ecosystem
- **Spring Cloud Gateway** - API Gateway
- **Spring Cloud Netflix Eureka** - Service Discovery
- **Spring Data JPA** - Data Access Layer
- **Spring Kafka** - Event Streaming
- **Spring Cloud Stream** - Event-Driven Microservices
- **Spring Boot Actuator** - Health Monitoring
- **Spring Security** - JWT Validation

### Infrastructure
- **Database:** PostgreSQL 15
- **Message Broker:** Apache Kafka 7.5.0
- **Coordination:** Apache Zookeeper
- **Container:** Docker & Docker Compose
- **Migration:** Flyway

### Cloud & Authentication
- **AWS Cognito** - User Authentication & Identity Management
- **JWT** - Token-based Authentication

### Documentation & Validation
- **OpenAPI 3.0** - API Specification
- **Swagger UI** - Interactive API Documentation
- **Jakarta Validation** - Input Validation

### Documentation & Validation
- **OpenAPI 3.0** - API Specification
- **Swagger UI** - Interactive API Documentation
- **Jakarta Validation** - Input Validation

---

## ✨ Key Features

### ✅ Required Features (All Implemented)
- [x] **Complete CRUD Operations** for Users, Organizations, and Invitations
- [x] **Input Validation & Sanitization** using Jakarta Validation & custom sanitizers
- [x] **Database Indexes** on all searchable fields (email, normalized names, etc.)
- [x] **Health Endpoints** via Spring Boot Actuator (`/actuator/health`)
- [x] **Automatic Invitation Expiration** - Scheduled job runs daily at 2 AM
- [x] **User-Organization Relationships** - Many-to-many with automatic sync
- [x] **Search Endpoints with Pagination** - All search operations support pagination
- [x] **Unique Constraints** - Email uniqueness, registry number uniqueness
- [x] **Audit Fields** - All entities have `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

### ⭐ Optional Features (Implemented)
- [x] **Event-Driven Architecture** with Apache Kafka
- [x] **AWS Cognito Integration** for authentication
- [x] **OpenAPI/Swagger Documentation** - Auto-generated API docs
- [x] **Role-Based Access Control** (ADMIN, MANAGER, USER)
- [x] **Service Discovery** with Eureka
- [x] **API Gateway** with JWT validation
- [x] **Docker Deployment** - Complete docker-compose setup
- [x] **Audit Trail** - Event publishing for all critical operations

### 🎯 Advanced Features
- [x] **Eventual Consistency** - User-organization sync via Kafka events
- [x] **Microservice Communication** - REST + Event-driven hybrid
- [x] **Idempotent Event Processing** - Kafka producer with acks=all
- [x] **Consumer Groups** - Scalable event consumption
- [x] **Health Checks** - Readiness and liveness probes
- [x] **Centralized Configuration** - Environment-based config

---

## 📊 Data Models

### User Entity
```kotlin
@Entity
@Table(name = "users")
class User {
    val id: UUID                           // Primary key
    var cognitoSub: String?                // AWS Cognito subject ID
    var email: String                      // Unique, indexed
    var status: UserStatus                 // ACTIVE, PENDING, DEACTIVATED, DELETED
    var fullName: String                   // Letters only
    var normalizedName: String             // Lowercase, ASCII, alphanumeric, indexed
    var role: Role                         // ADMIN, MANAGER, USER
    var organizationIds: Set<UUID>         // Many-to-many relationship
    val createdAt: LocalDateTime
    var updatedAt: LocalDateTime
    val createdBy: UUID
    var updatedBy: UUID
}
```

**Validation Rules:**
- Email must be unique across all users
- Full name: letters and spaces only (regex: `^[a-zA-Z\s]+$`)
- Normalized name: auto-generated (lowercase, ASCII, alphanumeric)
- Email format validation via `@Email` annotation

**Indexes:**
- `idx_user_email` (unique)
- `idx_user_normalized_name`
- `idx_user_cognito_sub` (unique)

### Organization Entity
```kotlin
@Entity
@Table(name = "organizations")
class Organization {
    val id: UUID                           // Primary key
    var name: String                       // Alphanumeric
    var normalizedName: String             // Lowercase, ASCII, alphanumeric, indexed
    var registryNumber: String             // Unique, alphanumeric
    var contactEmail: String               // Email format
    var companySize: Int                   // Min: 1
    var yearFounded: Int                   // Range: 1800-2100
    val createdAt: LocalDateTime
    var updatedAt: LocalDateTime
    val createdBy: UUID
    var updatedBy: UUID
}
```

**Validation Rules:**
- Registry number must be unique
- Name: alphanumeric characters only (regex: `^[a-zA-Z0-9\s]+$`)
- Company size: minimum 1
- Year founded: 1800-2100

**Indexes:**
- `idx_organization_normalized_name`
- `idx_organization_year_founded`
- `idx_organization_company_size`

### Invitation Entity
```kotlin
@Entity
@Table(name = "invitations")
class Invitation {
    val id: UUID                           // Primary key
    var userId: UUID                       // Foreign key to User
    var organizationId: UUID               // Foreign key to Organization
    var message: String                    // Invitation message
    var status: InvitationStatus           // ACCEPTED, REJECTED, PENDING, EXPIRED
    val createdAt: LocalDateTime
    var updatedAt: LocalDateTime
    val createdBy: UUID
    var updatedBy: UUID
}
```

**Business Rules:**
- Only ONE pending invitation per user per organization
- Invitations expire after 7 days (based on createdAt)
- User can be reinvited if invitation is expired
- User CANNOT be reinvited if last invitation was rejected
- Daily scheduled job updates expired invitations to EXPIRED status

**Indexes:**
- `idx_invitations_user_id`
- `idx_invitations_organization_id`
- Unique constraint: `(user_id, organization_id, status)` when status = PENDING

---

## 📚 API Documentation

All services provide **Swagger UI** for interactive API documentation:

- **User Service**: http://localhost:8084/swagger-ui.html
- **Organization Service**: http://localhost:8082/swagger-ui.html
- **Invitation Service**: http://localhost:8085/swagger-ui.html

### User Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/users` | Create a new user |
| PUT | `/api/v1/users/{id}` | Update user details |
| GET | `/api/v1/users/{id}` | Get user by ID |
| GET | `/api/v1/users/search?name={name}` | Search users by normalized name (paginated) |
| GET | `/api/v1/users/email/{email}` | Find user by email |
| GET | `/api/v1/users/{id}/organizations` | Get all organizations a user belongs to |
| GET | `/api/v1/users/organization/{orgId}` | Get all users in an organization |

### Organization Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/organizations` | Create a new organization |
| PUT | `/api/v1/organizations/{id}` | Update organization details |
| GET | `/api/v1/organizations/search` | Search organizations (by name, year, size) |
| GET | `/api/v1/organizations/registry/{number}` | Find organization by registry number |
| GET | `/api/v1/organizations/{id}/users` | Get all users belonging to an organization |

### Invitation Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/invitations` | Create a new invitation |
| PUT | `/api/v1/invitations/{id}/status` | Update invitation status (ACCEPT/REJECT) |
| GET | `/api/v1/invitations/user/{userId}` | Get all invitations for a user |
| GET | `/api/v1/invitations/organization/{orgId}` | Get all invitations for an organization |

### Health Endpoints

All services expose health check endpoints via Spring Boot Actuator:
- **Eureka**: http://localhost:8761/actuator/health
- **API Gateway**: http://localhost:8080/actuator/health
- **User Service**: http://localhost:8084/actuator/health
- **Organization Service**: http://localhost:8082/actuator/health
- **Invitation Service**: http://localhost:8085/actuator/health

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** (JDK 17 or later)
- **Docker** & **Docker Compose**
- **Gradle 8.4+** (or use included wrapper)
- **AWS Account** with Cognito User Pool (for authentication)

### Environment Variables

Create a `.env` file or export these variables:

```bash
# AWS Cognito Configuration
AWS_COGNITO_USER_POOL_ID=your-user-pool-id
AWS_COGNITO_CLIENT_ID=your-client-id
AWS_COGNITO_CLIENT_SECRET=your-client-secret
AWS_REGION=us-east-1

# Database Configuration (handled by Docker Compose)
# No manual setup needed for local development
```

### Quick Start with Docker Compose (Recommended)

The easiest way to run the entire system:

```bash
# Clone the repository
git clone <repository-url>
cd spring_boot_microservices

# Build and start all services
docker-compose up --build

# Services will start in order:
# 1. PostgreSQL databases (3 instances)
# 2. Zookeeper
# 3. Kafka
# 4. Eureka Server
# 5. API Gateway
# 6. User Service (Kafka consumer)
# 7. Organization Service
# 8. Invitation Service (Kafka producer)
```

**Access Points:**
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- User Service API: http://localhost:8084/swagger-ui.html
- Organization Service API: http://localhost:8082/swagger-ui.html
- Invitation Service API: http://localhost:8085/swagger-ui.html

### Manual Build & Run

If you prefer to run services individually:

```bash
# Build the project
./gradlew clean build

# Run Eureka Server
./gradlew :eureka-server:bootRun

# Run API Gateway (in new terminal)
./gradlew :api-gateway:bootRun

# Run User Service (in new terminal)
./gradlew :user-service:bootRun

# Run Organization Service (in new terminal)
./gradlew :organization-service:bootRun

# Run Invitation Service (in new terminal)
./gradlew :invitation-service:bootRun
```

### Verify Installation

```bash
# Check all services are healthy
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8085/actuator/health

# Check Eureka Dashboard
open http://localhost:8761

# All 5 services should be registered
```

---

## 🎭 Event-Driven Architecture

This project implements a sophisticated **event-driven architecture** using Apache Kafka for asynchronous, loosely-coupled microservice communication.

### Event Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                    Event-Driven Flow                          │
└──────────────────────────────────────────────────────────────┘

1. User accepts invitation
   │
   ▼
┌────────────────────┐
│ Invitation Service │ ──► InvitationAcceptedEvent
└────────────────────┘            │
                                  ▼
                          ┌──────────────┐
                          │    Kafka     │
                          │   Topic:     │
                          │ invitation-  │
                          │   events     │
                          └──────────────┘
                                  │
                                  ▼
                      ┌─────────────────────┐
                      │   User Service      │
                      │  (Kafka Consumer)   │
                      │                     │
                      │ 1. Consumes event   │
                      │ 2. Updates user     │
                      │ 3. Adds org to user │
                      │ 4. Publishes audit  │
                      └─────────────────────┘
                                  │
                                  ▼
                      UserOrganizationAddedEvent
                                  │
                                  ▼
                          ┌──────────────┐
                          │    Kafka     │
                          │   Topic:     │
                          │ user-events  │
                          └──────────────┘
```

### Domain Events

**7 Event Types Implemented:**

1. **UserCreatedEvent** - Published when new user is created
2. **UserUpdatedEvent** - Published when user is modified
3. **UserOrganizationAddedEvent** - Published when org is added to user
4. **InvitationCreatedEvent** - Published when invitation is created
5. **InvitationAcceptedEvent** - Published when invitation is accepted
6. **InvitationRejectedEvent** - Published when invitation is rejected
7. **InvitationExpiredEvent** - Published when invitations expire

### Kafka Topics

- **`invitation-events`** - All invitation lifecycle events
- **`user-events`** - All user-related events

### Event Processing

**Producer Configuration (Invitation Service):**
```yaml
spring:
  kafka:
    producer:
      acks: all                    # Wait for all replicas
      retries: 3                   # Retry failed sends
      enable-idempotence: true     # Exactly-once semantics
```

**Consumer Configuration (User Service):**
```yaml
spring:
  kafka:
    consumer:
      group-id: user-service-group
      auto-offset-reset: earliest
      enable-auto-commit: false    # Manual commit for control
```

### Monitoring Kafka Events

```bash
# List all topics
docker exec -it $(docker ps -qf "name=kafka") \
  kafka-topics --list --bootstrap-server localhost:9092

# Watch invitation events in real-time
docker exec -it $(docker ps -qf "name=kafka") \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic invitation-events \
  --from-beginning

# Watch user events
docker exec -it $(docker ps -qf "name=kafka") \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

For detailed Event-Driven Architecture documentation, see: [EVENT_DRIVEN_ARCHITECTURE.md](EVENT_DRIVEN_ARCHITECTURE.md)

---

## 🔒 Security & Authentication

### Authentication Flow

```
1. User Registration (AWS Cognito)
   ├─► User signs up via Cognito UI
   ├─► Cognito creates user account
   ├─► Post-confirmation Lambda triggers
   └─► Lambda creates user in User Service

2. User Login
   ├─► User authenticates with Cognito
   ├─► Cognito validates credentials
   └─► Returns JWT access token with claims

3. API Request
   ├─► Client includes JWT in Authorization header
   ├─► API Gateway validates JWT against Cognito JWK
   ├─► Gateway extracts user claims (sub, email, role)
   ├─► Gateway adds X-User-Id header for downstream services
   └─► Request routed to microservice
```

### Role-Based Access Control (RBAC)

| Role | Permissions |
|------|-------------|
| **ADMIN** | • Full access to all endpoints<br>• Can create ACTIVE users directly<br>• Can perform DELETE operations<br>• Can manage all resources |
| **MANAGER** | • Cannot perform DELETE operations<br>• Can only create PENDING users<br>• Access to management endpoints<br>• Can manage own organization |
| **USER** | • Access to own records only<br>• Can view own profile<br>• Can manage invitations to own organizations<br>• Read-only for other users |

### JWT Token Structure

```json
{
  "sub": "cognito-user-id",
  "email": "user@example.com",
  "cognito:groups": ["USER"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

### Securing API Calls

```bash
# Example: Create user with JWT
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "fullName": "Test User",
    "role": "USER"
  }'
```

---

## 🧪 Testing

### Automated Unit Tests (Docker-Based) ✅

**Complete test suite with isolated test databases!**

Run all tests in Docker containers:
```bash
# Windows PowerShell
.\run-tests.ps1

# Linux/Mac
./run-tests.sh
```

**What it does:**
- Spins up isolated PostgreSQL test databases (ports 5434-5436)
- Runs unit tests for all 3 microservices
- Automatic cleanup after completion
- **Status:** ✅ All tests passing!

**Test Results:**
- ✅ **User Service**: 8 repository tests + 15 service tests
- ✅ **Organization Service**: 8 repository tests
- ✅ **Invitation Service**: 10 repository tests

**Manual test execution:**
```bash
# Individual service tests
./gradlew :user-service:test
./gradlew :organization-service:test
./gradlew :invitation-service:test

# All tests
./gradlew test
```

See [TESTING.md](TESTING.md) for detailed testing documentation.

---

### Quick End-to-End Test

See [QUICK_START.md](QUICK_START.md) for detailed testing instructions.

**Complete Event-Driven Flow Test:**

```bash
# 1. Create User
curl -X POST http://localhost:8084/api/v1/users \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{"email":"test@test.com","fullName":"Test User","role":"USER"}'
# Save user ID from response

# 2. Create Organization
curl -X POST http://localhost:8082/api/v1/organizations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{"name":"Test Org","registryNumber":"REG123","contactEmail":"org@test.com","companySize":50,"yearFounded":2023}'
# Save org ID from response

# 3. Create Invitation
curl -X POST http://localhost:8085/api/v1/invitations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 00000000-0000-0000-0000-000000000001" \
  -d '{"userId":"<USER_ID>","organizationId":"<ORG_ID>","message":"Join us!"}'
# Save invitation ID

# 4. Accept Invitation (Triggers Kafka Event!)
curl -X PUT http://localhost:8085/api/v1/invitations/<INVITATION_ID>/status \
  -H "Content-Type: application/json" \
  -H "X-User-Id: <USER_ID>" \
  -d '{"status":"ACCEPTED"}'

# 5. Verify User's Organizations (Auto-updated via Kafka!)
curl http://localhost:8084/api/v1/users/<USER_ID>/organizations
# Should return: ["<ORG_ID>"]

# 6. Verify Organization's Users
curl http://localhost:8082/api/v1/organizations/<ORG_ID>/users
# Should include the user
```

---

## ✅ Case Study Compliance

### Digitopia Case Study Requirements

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| **Required Knowledge** | | |
| Kotlin/Java 12+ | ✅ | Kotlin 1.9.10, Java 17 |
| Spring Boot with annotations | ✅ | Spring Boot 3.1.5 |
| JPA/Hibernate | ✅ | Spring Data JPA |
| Gradle/Maven | ✅ | Gradle 8.4 Kotlin DSL |
| Microservice Architecture | ✅ | 5 services + Kafka |
| Cloud Services | ✅ | AWS Cognito |
| **Optional Knowledge** | | |
| Unit Testing (JUnit) | ⏳ | In progress |
| OpenAPI/Swagger | ✅ | Fully implemented |
| Event-Driven & Queues | ✅ | Kafka + 7 events |
| **Data Structure** | | |
| User CRUD | ✅ | Complete |
| Organization CRUD | ✅ | Complete |
| Invitation CRUD | ✅ | Complete |
| Input validation | ✅ | Jakarta Validation |
| Text sanitization | ✅ | Custom sanitizers |
| Database indexes | ✅ | All searchable fields |
| Audit fields | ✅ | All entities |
| **Business Rules** | | |
| User-org many-to-many | ✅ | Event-driven sync |
| Unique email | ✅ | Database constraint |
| Unique registry number | ✅ | Database constraint |
| Invitation expiration (7d) | ✅ | Scheduled job @ 2 AM |
| One pending invitation | ✅ | Unique constraint |
| Reinvite rules | ✅ | Business logic |
| **Endpoints** | | |
| Health checks | ✅ | Spring Actuator |
| User search by name | ✅ | With pagination |
| User search by email | ✅ | Single result |
| User's organizations | ✅ | GET /users/{id}/organizations |
| Org's users | ✅ | GET /organizations/{id}/users |
| Org search (multi-param) | ✅ | With pagination |
| Org search by registry | ✅ | Single result |

### Exceeds Requirements ⭐

- **Event-Driven Architecture** - Full Kafka implementation
- **Automatic user-organization sync** - Via Kafka events
- **Complete audit trail** - All events published
- **Microservice communication** - REST + Event hybrid
- **Service discovery** - Eureka
- **API Gateway** - Centralized entry point
- **Docker deployment** - Production-ready
- **Comprehensive documentation** - Multiple MD files

---

## 📁 Project Structure

```
spring_boot_microservices/
├── api-gateway/                 # API Gateway Service
│   ├── src/main/kotlin/
│   │   └── com/digitopia/gateway/
│   │       ├── ApiGatewayApplication.kt
│   │       ├── config/
│   │       │   ├── GatewayConfig.kt
│   │       │   ├── SecurityConfig.kt
│   │       │   └── CorsConfig.kt
│   │       └── filter/
│   │           └── AuthenticationFilter.kt
│   └── build.gradle.kts
├── eureka-server/               # Service Discovery
│   ├── src/main/kotlin/
│   │   └── com/digitopia/eureka/
│   │       ├── EurekaServerApplication.kt
│   │       └── controller/
│   └── build.gradle.kts
├── user-service/                # User Management
│   ├── src/main/kotlin/
│   │   └── com/digitopia/
│   │       ├── common/
│   │       │   ├── entity/BaseEntity.kt
│   │       │   ├── events/DomainEvents.kt
│   │       │   └── exception/
│   │       └── user/
│   │           ├── UserServiceApplication.kt
│   │           ├── controller/UserController.kt
│   │           ├── service/UserService.kt
│   │           ├── repository/UserRepository.kt
│   │           ├── model/User.kt
│   │           ├── dto/UserDtos.kt
│   │           ├── event/
│   │           │   ├── UserEventPublisher.kt
│   │           │   └── UserEventListener.kt
│   │           └── config/KafkaConfig.kt
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       └── V1__Create_User_Table.sql
│   └── build.gradle.kts
├── organization-service/        # Organization Management
│   ├── src/main/kotlin/
│   │   └── com/digitopia/organization/
│   │       ├── OrganizationServiceApplication.kt
│   │       ├── controller/OrganizationController.kt
│   │       ├── service/OrganizationService.kt
│   │       ├── repository/OrganizationRepository.kt
│   │       ├── model/Organization.kt
│   │       ├── dto/OrganizationDtos.kt
│   │       ├── client/UserServiceClient.kt
│   │       └── config/RestTemplateConfig.kt
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       └── V1__Create_Organization_Table.sql
│   └── build.gradle.kts
├── invitation-service/          # Invitation Management
│   ├── src/main/kotlin/
│   │   └── com/digitopia/invitation/
│   │       ├── InvitationServiceApplication.kt
│   │       ├── controller/InvitationController.kt
│   │       ├── service/InvitationService.kt
│   │       ├── repository/InvitationRepository.kt
│   │       ├── model/Invitation.kt
│   │       ├── dto/InvitationDtos.kt
│   │       ├── event/InvitationEventPublisher.kt
│   │       └── config/KafkaConfig.kt
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       └── V1__Create_Invitation_Table.sql
│   └── build.gradle.kts
├── aws-lambda/                  # AWS Lambda Functions
│   ├── cognito-post-confirmation.py
│   └── README.md
├── docker-compose.yml           # Container Orchestration
├── build.gradle.kts             # Root Build Config
├── settings.gradle.kts          # Multi-project Setup
├── EVENT_DRIVEN_ARCHITECTURE.md # EDA Documentation
├── FINAL_IMPLEMENTATION_SUMMARY.md
├── QUICK_START.md               # Quick Reference
└── README.md                    # This file
```

---

## 📖 Additional Documentation

- **[EVENT_DRIVEN_ARCHITECTURE.md](EVENT_DRIVEN_ARCHITECTURE.md)** - Complete EDA guide with event flow diagrams
- **[QUICK_START.md](QUICK_START.md)** - Quick testing guide with curl examples
- **[FINAL_IMPLEMENTATION_SUMMARY.md](FINAL_IMPLEMENTATION_SUMMARY.md)** - Comprehensive implementation summary
- **[HEALTH_ENDPOINTS.md](HEALTH_ENDPOINTS.md)** - Health check migration guide

---

## 💡 Local Development (Recommended)

**Test everything locally first - it's FREE and identical to AWS!**

```bash
# Start all services
docker-compose up

# Stop all services
docker-compose down
```

**What you get:**
- ✅ All 5 microservices
- ✅ PostgreSQL databases (3)
- ✅ Kafka + Zookeeper
- ✅ Eureka Server
- ✅ API Gateway
- ✅ **Cost: $0.00**

**Access Points:**
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- User Service: http://localhost:8084
- Organization Service: http://localhost:8082
- Invitation Service: http://localhost:8085

---

## ☁️ AWS Deployment (Production)

### 🎯 Deploy to AWS ECS Fargate

### 🎯 Deploy to AWS ECS Fargate

**We've successfully deployed the Docker images to AWS ECR. Here's the process:**

#### Step 1: Build & Push Images to ECR (✅ COMPLETED)

```powershell
# Run the automated deployment script
.\deploy-ecs-simple.ps1
```

**This script automatically:**
1. Creates ECR repositories for all 5 services
2. Builds Docker images locally (avoiding network issues)
3. Pushes images to AWS ECR
4. Creates ECS cluster: `digitopia-cluster`

**Status:** ✅ **Successfully deployed!**

Your images are now stored at:
```
493272324121.dkr.ecr.us-east-1.amazonaws.com/digitopia/eureka-server:latest
493272324121.dkr.ecr.us-east-1.amazonaws.com/digitopia/api-gateway:latest
493272324121.dkr.ecr.us-east-1.amazonaws.com/digitopia/user-service:latest
493272324121.dkr.ecr.us-east-1.amazonaws.com/digitopia/organization-service:latest
493272324121.dkr.ecr.us-east-1.amazonaws.com/digitopia/invitation-service:latest
```

#### Step 2: Deploy Services to Fargate

**See the complete guide:** `AWS_SERVICE_DEPLOYMENT_GUIDE.md`

**Summary:**
1. Create Task Definitions in AWS Console for each service
2. Configure networking (VPC, subnets, security groups)
3. Set up environment variables (database URLs, Kafka endpoints)
4. Create ECS Services
5. Configure load balancers (optional)

**💰 Estimated Cost:**
- **Per Hour:** ~$0.40 (all 5 services)
- **Per Month:** ~$291 (if always running)
- **For Testing:** ~$0.41 for 1 hour of testing

**💡 Recommendation:** Test locally first with `docker-compose up` (FREE), then deploy to AWS when you need public access or want to demo the application.

---

### 📚 AWS Deployment Documentation

| Document | Purpose | Status |
|----------|---------|--------|
| `deploy-ecs-simple.ps1` | PowerShell script to push images to ECR | ✅ Working |
| `AWS_SERVICE_DEPLOYMENT_GUIDE.md` | Step-by-step guide for creating ECS services | 📖 Complete |
| `SIMPLE_AWS_DEPLOY.md` | Quick reference for ECR deployment | � Reference |

---

### ⚠️ Important Notes

1. **Local Testing First:** Always test with `docker-compose up` before deploying to AWS
2. **Cost Management:** Stop ECS services when not in use to avoid charges
3. **Database Setup:** You'll need to set up RDS PostgreSQL (3 databases) and Amazon MSK (Kafka) for full deployment
4. **Root Account:** Don't use AWS root account - create an IAM user with appropriate permissions

---

## 🤝 Contributing

This is a case study project demonstrating microservices architecture best practices. Contributions for improvements are welcome!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is proprietary and confidential. Created for the Digitopia case study.

---

## 👤 Author

**Zagor**
- Repository: [Invitation_System_SpringBoot_Microservices_AWS](https://github.com/Zahkklm/Invitation_System_SpringBoot_Microservices_AWS)

---

## 🙏 Acknowledgments

- Spring Boot Team for the excellent framework
- Apache Kafka for event streaming platform
- AWS for Cognito authentication service
- Digitopia for the comprehensive case study requirements

---

**Built with ❤️ using Kotlin, Spring Boot, and Kafka**

## Authentication Flow

Authentication is handled by **AWS Cognito** directly:

1. **User Registration** (via Cognito):
   - User signs up through Cognito-hosted UI or custom UI
   - Post-confirmation Lambda creates user in User Service
   - User receives Cognito sub ID

2. **User Login** (via Cognito):
   - User authenticates with Cognito
   - Receives JWT access token
   - Token includes user claims (sub, email, role)

3. **API Requests**:
   - Include JWT in Authorization header: `Bearer <token>`
   - API Gateway validates token against Cognito JWK
   - Gateway adds user claims to headers for downstream services

## Role-Based Access Control

- **ADMIN**
  - Full access to all endpoints
  - Can create ACTIVE users
  - Can perform DELETE operations

- **MANAGER**
  - Cannot perform DELETE operations
  - Can only create PENDING users
  - Access to management endpoints

- **USER**
    - Access to own records only
  - Can manage own profile
  - Can handle invitations

---

## User Data Strategy

### Where to Store User Information

- **AWS Cognito** is used for authentication and identity management. It stores user credentials, handles registration, login, and issues JWTs containing user identity (such as `sub`, `email`, etc.).
- **User Service** is the source of truth for all application-specific user data (such as user profile, preferences, roles, and business-related information). It manages the internal `user_id` used throughout your microservices.
- On first login or registration, the User Service should create a user record that maps Cognito's `sub` (subject) to your internal `user_id`.
- All other services reference users by the `user_id` managed by the User Service, not by Cognito’s internal IDs.

**Summary:**
- Use Cognito for authentication and identity.
- Use the User Service for all business/user data and as the source of truth for user references across services.

This approach gives you flexibility, control, and avoids vendor lock-in for your application’s user model.

## Project Structure
```
.
├── api-gateway/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/digitopia/gateway/
│   │   └── resources/
│   └── build.gradle.kts
├── auth-service/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/digitopia/auth/
│   │   └── resources/
│   └── build.gradle.kts
├── eureka-server/
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   └── com/digitopia/eureka/
│   │   └── resources/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Health Checks

Each service provides a health check endpoint via Spring Boot Actuator:
- **Eureka Server**: http://localhost:8761/actuator/health
- **API Gateway**: http://localhost:8080/actuator/health
- **User Service**: http://localhost:8084/actuator/health
- **Organization Service**: http://localhost:8082/actuator/health
- **Invitation Service**: http://localhost:8085/actuator/health

## Contributing

1. Create a new branch for your feature
2. Make your changes
3. Submit a pull request

## License

This project is proprietary and confidential.